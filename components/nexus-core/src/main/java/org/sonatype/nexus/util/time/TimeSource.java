package org.sonatype.nexus.util.time;

import org.joda.time.DateTime;

/**
 * An interface for obtaining the current time,
 *
 * @since 3.0
 */
public interface TimeSource
{
  /**
   * Get the current time as a {@link DateTime}.
   */
  DateTime currentTime();

  /**
   * Get the current time in milliseconds.
   */
  long currentTimeMillis();
}
