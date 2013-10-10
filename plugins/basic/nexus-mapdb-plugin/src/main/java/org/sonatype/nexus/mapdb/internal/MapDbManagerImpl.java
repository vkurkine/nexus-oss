/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.mapdb.internal;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.mapdb.MapDbManager;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implementation of {@link MapDbManager}.
 *
 * @author cstamas
 */
@Singleton
@Named
public class MapDbManagerImpl
    extends AbstractLoggingComponent
    implements MapDbManager
{
  private final ApplicationConfiguration applicationConfiguration;

  private final Map<String, DB> managedDatabases;

  @Inject
  public MapDbManagerImpl(final EventBus eventBus, final ApplicationConfiguration applicationConfiguration) {
    this.applicationConfiguration = checkNotNull(applicationConfiguration);
    this.managedDatabases = Maps.newHashMap();
    eventBus.register(this);
  }

  @Override
  public DB openManaged(final String dbKey, final DBMakerConfigurator configurator)
      throws IOException
  {
    checkNotNull(dbKey, "dbKey is null");
    final File location = new File(applicationConfiguration.getWorkingDirectory("mapdb"), dbKey);
    synchronized (managedDatabases) {
      if (managedDatabases.containsKey(dbKey)) {
        throw new IllegalArgumentException("Database keyed \"" + dbKey + "\" already created and is managed!");
      }
      final DB result = create(location, configurator);
      managedDatabases.put(dbKey, result);
      return result;
    }
  }

  @Override
  public DB getManaged(final String dbKey) {
    checkNotNull(dbKey, "dbKey is null");
    synchronized (managedDatabases) {
      return managedDatabases.get(dbKey);
    }
  }

  @Override
  public void closeManaged(String dbKey) {
    checkNotNull(dbKey, "dbKey is null");
    synchronized (managedDatabases) {
      final DB db = managedDatabases.get(dbKey);
      if (db != null) {
        closeSilently(dbKey, db);
      }
      managedDatabases.remove(dbKey);
    }
  }

  // == events

  @Subscribe
  public void on(final NexusStoppedEvent evt) {
    getLogger().info("Closing all managed MapDB instances...");
    synchronized (managedDatabases) {
      for (Map.Entry<String, DB> entry : managedDatabases.entrySet()) {
        closeSilently(entry.getKey(), entry.getValue());
      }
      managedDatabases.clear();
    }
  }

  // == internal

  protected DB create(final File file, final DBMakerConfigurator configurator)
      throws IOException
  {
    DBMaker dbm = DBMaker.newFileDB(file);
    if (configurator != null) {
      dbm = configurator.configure(dbm);
    }
    return dbm.make();
  }

  protected void closeSilently(final String dbKey, final DB db) {
    try {
      getLogger().debug("closing {}", dbKey);
      db.close();
    }
    catch (Exception e) {
      getLogger().warn("Problem while closing managed LevelDB {}", dbKey, e);
    }
  }
}
