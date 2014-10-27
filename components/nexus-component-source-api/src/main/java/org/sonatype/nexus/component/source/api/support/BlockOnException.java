package org.sonatype.nexus.component.source.api.support;

import org.sonatype.nexus.util.sequence.NumberSequence;
import org.sonatype.nexus.util.time.TimeSource;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An {@link AutoBlockStrategy} that blocks any time an exception occurs, and reports itself blocked until after an
 * ever-escalating time delay.
 *
 * @since 3.0
 */
public class BlockOnException
    extends ComponentSupport
    implements AutoBlockStrategy
{
  private final String sourceName;

  private final NumberSequence nextDelay;

  private final TimeSource timeSource;

  private DateTime blockedAtLeastUntil = null;

  private static final long ONE_MINUTE = 60L * 1000L; // 60 sec * 1000 msec

  public BlockOnException(final String sourceName, final NumberSequence minutesToDelay, final TimeSource timeSource) {
    this.sourceName = checkNotNull(sourceName);
    this.nextDelay = checkNotNull(minutesToDelay);
    this.timeSource = checkNotNull(timeSource);
  }

  @Override
  public boolean isAutoBlockEnabled() {
    return true;
  }

  @Override
  public synchronized boolean isAutoBlocked() {
    return blockedAtLeastUntil != null;
  }

  @Override
  public synchronized void successfulCallMade() {
    if (blockedAtLeastUntil != null) {
      log.info("Source {} is unblocking after a successful connection.", sourceName);
    }
    blockedAtLeastUntil = null;
    nextDelay.reset();
  }

  @Override
  public synchronized void processException(final Exception e) {
    if (!isAutoBlocked()) {
      log.info("Source {} is auto-blocking due to {} communicating with remote source.", sourceName,
          e.getClass().getSimpleName(), e);
      setNextUnblockCheckTime();
      return;
    }

    // We're blocked

    if (timeSource.currentTime().isBefore(blockedAtLeastUntil)) {
      // It's too soon to check whether this source should unblock, so ignore the exception
      return;
    }

    setNextUnblockCheckTime();
  }

  DateTime getBlockedAtLeastUntil() {
    return blockedAtLeastUntil;
  }

  private void setNextUnblockCheckTime() {
    int increment = (int) nextDelay.next();

    if (blockedAtLeastUntil == null) {
      blockedAtLeastUntil = timeSource.currentTime().plusMinutes(increment);
    }
    else {
      blockedAtLeastUntil = blockedAtLeastUntil.plusMinutes(increment);
    }

    log.info("Source {} is auto-blocked until {}.", sourceName, blockedAtLeastUntil);
  }
}
