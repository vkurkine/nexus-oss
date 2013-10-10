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

package org.sonatype.nexus.mapdb;

import java.io.File;
import java.io.IOException;

import org.mapdb.DB;
import org.mapdb.DBMaker;

/**
 * A manager of databases that removes the burden of finding a proper place for database files, and manages thier
 * closing (at Nexus shutdown). Primarily, it is meant to ease of use of MapDB in cases when database instances are
 * "long living" (opened during Nexus instance lives). Still, this plugin is also a "provider", hence full MapDB
 * is at your disposal and you are not obliged to use this manager at all, it's here just for convenience.
 * <p/>
 * Using this manager all you have to use is {@link #openManaged(String, DBMakerConfigurator)} method, keep and use
 * the DB instance as you wish, and it removes the burden of finding place/file to store the database files and
 * to manage it's lifecycle, as all "managed" databases will be closed once, when Nexus instance shuts down.
 * <p/>
 * Still, nothing prevents you to use the "native" MapDB way of creating a database that suits your needs, as this
 * plugin exposes complete MapDB too. Databases created "manually" will need to be fully managed by you then.
 * <p>
 * A note about {@code dbKey}s, that are actually plain strings: in current implementation, those will be used to
 * calculate (if no explicit {@link File} passed in) the location of the database, and will end up in path. Hence, only
 * "file system safe" characters should be used for now (currently nothing enforces this!). Keys with prefix "nx-" are
 * reserved for Nexus internal use (this is not enforced eitherF).
 *
 * @author cstamas
 * @see <a href="http://www.mapdb.org/">MapDB</a>
 * @since 2.7.0
 */
public interface MapDbManager
{
  /**
   * A configurator interface that allows you to custize DBMaker before invoking {@link DBMaker#make()} on it.
   */
  public interface DBMakerConfigurator
  {
    /**
     * Method invoked during execution of {@link MapDbManager#openManaged(String, DBMakerConfigurator)} on the passed
     * in configurator instance.
     *
     * @param dbmaker The {@link DBMaker} instance used to create a {@link DB} instance, before make invoked on it.
     */
    public DBMaker configure(DBMaker dbmaker);
  }

  /**
   * Creates and opens a "managed" MapDB instance, keyed with {@code dbKey} parameter and using passed
   * in {@code configurator} (that might be {@code null} if defaults desired). Internally, MapDBManager maintains a
   * registry of databases, and will prevent creation of another database using same key. Location of the database
   * will be the default location in Nexus (in it's work directory) reserved for MapDB instances.
   *
   * @param dbKey        a DB key, never {@code null}.
   * @param configurator MapDB configurator, might be {@code null} when only {@link DBMaker#newFileDB(File)} will be
   *                     invoked on DBMaker instance with a File calculated from default database location appended
   *                     with
   *                     {@code dbKey} parameter.
   * @throws IllegalArgumentException if DB keyed with {@code dbKey} already exists.
   * @throws UnsupportedOperationException If MapDB misconfigured. See MapDB documentation for details.
   */
  DB openManaged(String dbKey, DBMakerConfigurator configurator)
      throws IOException, IllegalArgumentException;

  /**
   * Returns the managed DB instance opened under key {@code dbKey}. If there is no DB under given key, {@code null}
   * is returned.
   *
   * @param dbKey a DB key, never {@code null}.
   */
  DB getManaged(String dbKey);

  /**
   * Closes the managed DB instance opened under the key {@code dbKey}, and removes instance from internal structures
   * (close on it will not be attempted on Nexus shutdown). If there is no DB under given key, nothing happens, call
   * will return cleanly.
   *
   * @param dbKey a DB key, never {@code null}.
   */
  void closeManaged(String dbKey);
}
