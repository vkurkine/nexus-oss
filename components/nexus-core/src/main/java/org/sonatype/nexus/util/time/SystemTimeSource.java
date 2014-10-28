package org.sonatype.nexus.util.time;

import javax.inject.Named;
import javax.inject.Singleton;

import org.joda.time.DateTime;

/**
 * A {@link TimeSource} backed by System.currentTimeMillis();
 *
 * @since 3.0
 */
public class SystemTimeSource
    implements TimeSource
{
  @Override
  public DateTime currentTime() {
    return new DateTime();
  }

  @Override
  public long currentTimeMillis() {
    return System.currentTimeMillis();
  }
}
