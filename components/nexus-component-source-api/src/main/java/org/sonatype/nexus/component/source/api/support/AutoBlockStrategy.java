package org.sonatype.nexus.component.source.api.support;

/**
 * A pluggable strategy for handling auto-blocking.
 *
 * @since 3.0
 */
public interface AutoBlockStrategy
{
  /**
   * Does this strategy auto-block sometimes?
   */
  boolean isAutoBlockEnabled();

  /**
   * Are we auto-blocked right now?
   */
  boolean isAutoBlocked();

  /**
   * Report that a successful call was made to the remote source.
   */
  void successfulCallMade();

  /**
   * Indicate that during a call to the remote source, an exception arose.
   */
  void processException(Exception e);
}
