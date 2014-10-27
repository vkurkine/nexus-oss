package org.sonatype.nexus.component.source.api.support;

import org.sonatype.nexus.util.sequence.FibonacciNumberSequence;
import org.sonatype.nexus.util.sequence.NumberSequence;
import org.sonatype.nexus.util.time.TimeSource;

import org.joda.time.DateTime;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class BlockOnExceptionTest
{
  private static final String SOURCE_NAME = "testSource";

  @Test
  public void reportsAutoBlockEnabled() {
    final BlockOnException strategy = new BlockOnException(SOURCE_NAME, mock(NumberSequence.class),
        mock(TimeSource.class));

    assertThat(strategy.isAutoBlockEnabled(), is(equalTo(true)));
  }

  @Test
  public void blocksOnException() {
    final BlockOnException strategy = new BlockOnException(SOURCE_NAME, mock(NumberSequence.class),
        new StubTimeSource());

    assertThat(strategy.isAutoBlocked(), is(equalTo(false)));

    strategy.processException(new RuntimeException("pretend exception"));

    assertThat(strategy.isAutoBlocked(), is(equalTo(true)));
  }

  @Test
  public void unblocksWhenToldTo() {
    final BlockOnException strategy = new BlockOnException(SOURCE_NAME, mock(NumberSequence.class),
        new StubTimeSource());

    strategy.processException(new RuntimeException("pretend exception"));

    strategy.successfulCallMade();

    assertThat(strategy.isAutoBlocked(), is(equalTo(false)));
  }

  @Test
  public void testEscalatingTime() {
    final StubTimeSource stubTimeSource = new StubTimeSource();

    final int delayOne = 5;
    final int delayTwo = 7;

    final BlockOnException strategy = new BlockOnException(SOURCE_NAME, new FibonacciNumberSequence(delayOne, delayTwo),
        stubTimeSource);

    stubTimeSource.time = 0L;

    strategy.processException(new RuntimeException());

    // We should be okay to check again after a 14-minute delay
    final DateTime firstCheckTime = new DateTime(0).plusMinutes(delayOne);

    assertThat(strategy.getBlockedAtLeastUntil(), is(equalTo(firstCheckTime)));

    // Before that time, additional exceptions do nothing
    strategy.processException(new RuntimeException());
    assertThat(strategy.getBlockedAtLeastUntil(), is(equalTo(firstCheckTime)));

    // Once that time has passed, however, new exceptions will increment the time
    stubTimeSource.time = new DateTime(0).plusMinutes(delayOne + 1).getMillis();
    strategy.processException(new RuntimeException());

    // We should be okay to check again after ANOTHER 14-minute delay
    final DateTime secondCheckTime = new DateTime(0).plusMinutes(delayOne + delayTwo);

    assertThat(strategy.getBlockedAtLeastUntil(), is(equalTo(secondCheckTime)));
  }

  private static class StubTimeSource
      implements TimeSource
  {
    public long time = 0L;

    @Override
    public DateTime currentTime() {
      return new DateTime(time);
    }

    @Override
    public long currentTimeMillis() {
      return time;
    }
  }
}