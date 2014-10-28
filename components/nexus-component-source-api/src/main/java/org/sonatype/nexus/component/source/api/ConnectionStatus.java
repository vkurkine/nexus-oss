package org.sonatype.nexus.component.source.api;

import java.io.IOException;

import javax.annotation.Nullable;

import org.sonatype.nexus.component.source.api.config.ComponentSourceConfig;
import org.sonatype.nexus.component.source.api.config.ComponentSourceConfigId;
import org.sonatype.nexus.component.source.api.config.ComponentSourceConfigStore;
import org.sonatype.nexus.component.source.api.support.AutoBlockState;

import org.joda.time.DateTime;

/**
 * Facilities for inspecting the status of a connection.
 *
 * @since 3.0
 */
public interface ConnectionStatus
{
  /**
   * True if the source will automatically block itself when there are connection difficulties with the remote source
   * (e.g. misconfiguration, network hiccup, etc.)
   *
   * To turn autoblocking functionality on, change the source configuration via {@link
   * ComponentSourceConfigStore#update(ComponentSourceConfigId, ComponentSourceConfig)}.
   */
  boolean isAutoBlockEnabled();

  /**
   * Returns the current state of auto-blocking.
   */
  AutoBlockState getAutoBlockState();

  /**
   * Return the earliest time that the connection might be expected to return to service.
   */
  @Nullable
  DateTime getBlockedUntil();

  /**
   * Connect to the remote resource to verify that the connection works.
   *
   * @throws IOException If the connection is not working.
   */
  void testConnection() throws IOException;
}
