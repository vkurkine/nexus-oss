package org.sonatype.nexus.component.source.api.support;

/**
 * @since 3.0
 */
public enum AutoBlockState
{
  /**
   * The source is not auto-blocked.
   */
  NOT_BLOCKED(true),

  /**
   * The source has been automatically blocked, and shouldn't be queried nor automatically checked.
   */
  AUTOBLOCKED(false),

  /**
   * The source has been blocked, but the minimum delay before automatically rechecking the source has elapsed. The
   * connection, however, has yet to be checked.
   */
  AUTOBLOCKED_STALE(true);

  private final boolean canQuery;

  private AutoBlockState(final boolean canQuery) {
    this.canQuery = canQuery;
  }

  /**
   * Is it okay to send requests to this source?
   */
  public boolean isRequestingAllowed() {
    return canQuery;
  }
}
