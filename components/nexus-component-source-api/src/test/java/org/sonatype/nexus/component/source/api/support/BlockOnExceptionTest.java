package org.sonatype.nexus.component.source.api.support;

import org.sonatype.nexus.util.sequence.FibonacciNumberSequence;
import org.sonatype.nexus.util.time.TimeSource;

import org.joda.time.DateTime;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class BlockOnExceptionTest
{
  private static final String SOURCE_NAME = "testSource";

  @Test
  public void reportsAutoBlockEnabled() {
    final BlockOnException strategy = new BlockOnException(SOURCE_NAME, fibonacci(1, 1));

    assertThat(strategy.isAutoBlockEnabled(), is(equalTo(true)));
  }

  @Test
  public void blocksOnException() {
    final BlockOnException strategy = new BlockOnException(SOURCE_NAME, fibonacci(1, 1));

    assertThat(strategy.getAutoBlockState(), is(equalTo(AutoBlockState.NOT_BLOCKED)));

    strategy.processException(new RuntimeException("pretend exception"));

    assertThat(strategy.getAutoBlockState(), is(equalTo(AutoBlockState.AUTOBLOCKED)));
  }

  @Test
  public void unblocksWhenToldTo() {
    final BlockOnException strategy = new BlockOnException(SOURCE_NAME, fibonacci(1, 1));

    strategy.processException(new RuntimeException("pretend exception"));

    strategy.successfulCallMade();

    assertThat(strategy.getAutoBlockState(), is(equalTo(AutoBlockState.NOT_BLOCKED)));
  }

  @Test
  public void autoblocksBecomeStale() {
    final BlockOnException strategy = new BlockOnException(SOURCE_NAME, fibonacci(1, 1));
    final StubTimeSource timeSource = new StubTimeSource();
    strategy.setTimeSource(timeSource);

    strategy.processException(new RuntimeException("pretend exception"));

    assertThat(strategy.getAutoBlockState(), is(equalTo(AutoBlockState.AUTOBLOCKED)));

    timeSource.plusMinutes(2);

    assertThat(strategy.getAutoBlockState(), is(equalTo(AutoBlockState.AUTOBLOCKED_STALE)));
  }

  @Test
  public void testEscalatingTime() {
    final StubTimeSource stubTimeSource = new StubTimeSource();

    final int delayOne = 5;
    final int delayTwo = 7;

    final BlockOnException strategy = new BlockOnException(SOURCE_NAME,
        fibonacci(delayOne, delayTwo));
    strategy.setTimeSource(stubTimeSource);

    strategy.processException(new RuntimeException());

    // We should be okay to check again after a 14-minute delay
    final DateTime firstCheckTime = new DateTime(0).plusMinutes(delayOne);

    assertThat(strategy.getBlockedUntil(), is(equalTo(firstCheckTime)));

    // Before that time, additional exceptions do nothing
    strategy.processException(new RuntimeException());
    assertThat(strategy.getBlockedUntil(), is(equalTo(firstCheckTime)));

    // Once that time has passed, however, new exceptions will increment the time
    stubTimeSource.plusMinutes(delayOne + 1);
    strategy.processException(new RuntimeException());

    // We should be okay to check again after ANOTHER 14-minute delay
    final DateTime secondCheckTime = new DateTime(0).plusMinutes(delayOne + delayTwo);

    assertThat(strategy.getBlockedUntil(), is(equalTo(secondCheckTime)));
  }

  private FibonacciNumberSequence fibonacci(final int delayOne, final int delayTwo) {
    return new FibonacciNumberSequence(delayOne, delayTwo);
  }

  private static class StubTimeSource
      implements TimeSource
  {
    public DateTime time = new DateTime(0L);

    public void plusMinutes(int minutes) {
      time = time.plusMinutes(minutes);
    }

    @Override
    public DateTime currentTime() {
      return time;
    }

    @Override
    public long currentTimeMillis() {
      return time.getMillis();
    }
  }
}