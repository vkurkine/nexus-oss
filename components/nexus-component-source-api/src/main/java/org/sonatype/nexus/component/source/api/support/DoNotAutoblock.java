package org.sonatype.nexus.component.source.api.support;

import org.sonatype.sisu.goodies.common.ComponentSupport;

/**
 * Logs exceptions but never autoblocks.
 *
 * @since 3.0
 */
public class DoNotAutoblock
    extends ComponentSupport
    implements AutoBlockStrategy
{
  @Override
  public boolean isAutoBlockEnabled() {
    return false;
  }

  @Override
  public boolean isAutoBlocked() {
    return false;
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
}
