package org.sonatype.nexus.component.source.api.support;

import javax.annotation.Nullable;

import org.joda.time.DateTime;

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
  AutoBlockState getAutoBlockState();

  /**
   * Report that a successful call was made to the remote source.
   */
  void successfulCallMade();

  /**
   * Indicate that during a call to the remote source, an exception arose.
   */
  void processException(Exception e);

  /**
   * If the source is blocked, this will return the earliest DateTime at which the source will be checked for
   * service to be restored.
   */
  @Nullable
  DateTime getBlockedUntil();
}
