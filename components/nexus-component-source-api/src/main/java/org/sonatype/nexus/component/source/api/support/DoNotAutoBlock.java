package org.sonatype.nexus.component.source.api.support;

import javax.annotation.Nullable;

import org.sonatype.sisu.goodies.common.ComponentSupport;

import org.joda.time.DateTime;

/**
 * Logs exceptions but never auto-blocks.
 *
 * @since 3.0
 */
public class DoNotAutoBlock
    extends ComponentSupport
    implements AutoBlockStrategy
{
  @Override
  public boolean isAutoBlockEnabled() {
    return false;
  }

  @Override
  public AutoBlockState getAutoBlockState() {
    return AutoBlockState.NOT_BLOCKED;
  }

  @Override
  public void processException(final Exception e) {
    log.error("Error detected during communication with component source.", e);

    // Nothing else to do, since we never autoblock.
  }

  @Override
  public void successfulCallMade() {
    // does nothing
  }

  @Nullable
  @Override
  public DateTime getBlockedUntil() {
    return null;
  }
}
