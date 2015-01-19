/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.capability.internal.storage;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.capability.CapabilityIdentity;
import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.nexus.orient.RecordIdObfuscator;
import org.sonatype.sisu.goodies.lifecycle.LifecycleSupport;

import com.google.common.collect.Maps;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.impl.ODocument;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * OrientDB implementation of {@link CapabilityStorage}.
 *
 * @since 3.0
 */
@Named("orient")
@Singleton
public class OrientCapabilityStorage
    extends LifecycleSupport
    implements CapabilityStorage
{
  private final Provider<DatabaseInstance> databaseInstance;

  private final RecordIdObfuscator recordIdObfuscator;

  private final CapabilityStorageItemEntityAdapter entityAdapter = new CapabilityStorageItemEntityAdapter();

  private OClass entityType;

  @Inject
  public OrientCapabilityStorage(final @Named("config") Provider<DatabaseInstance> databaseInstance,
                                 final RecordIdObfuscator recordIdObfuscator)
  {
    this.databaseInstance = checkNotNull(databaseInstance);
    this.recordIdObfuscator = checkNotNull(recordIdObfuscator);
  }

  @Override
  protected void doStart() throws Exception {
    try (ODatabaseDocumentTx db = databaseInstance.get().connect()) {
      // register schema
      entityType = entityAdapter.register(db);
    }
  }

  @Override
  protected void doStop() throws Exception {
    entityType = null;
  }

  /**
   * Open a database connection using the pool.
   */
  private ODatabaseDocumentTx openDb() {
    ensureStarted();
    return databaseInstance.get().acquire();
  }

  private CapabilityIdentity convertId(final ORID rid) {
    String encoded = recordIdObfuscator.encode(entityType, rid);
    return new CapabilityIdentity(encoded);
  }

  private ORID convertId(final CapabilityIdentity id) {
    return recordIdObfuscator.decode(entityType, id.toString());
  }

  @Override
  public CapabilityIdentity add(final CapabilityStorageItem item) throws IOException {
    ORID rid;
    try (ODatabaseDocumentTx db = openDb()) {
      ODocument doc = entityAdapter.create(db, item);
      rid = doc.getIdentity();
    }

    log.debug("Added item with RID: {}", rid);
    return convertId(rid);
  }

  @Override
  public boolean update(final CapabilityIdentity id, final CapabilityStorageItem item) throws IOException {
    ORID rid = convertId(id);

    try (ODatabaseDocumentTx db = openDb()) {
      // load record and apply updated item attributes
      ODocument doc = db.getRecord(rid);
      if (doc == null) {
        log.debug("Unable to update item with RID: {}", rid);
        return false;
      }
      entityAdapter.write(doc, item);
    }

    log.debug("Updated item with RID: {}", rid);
    return true;
  }

  @Override
  public boolean remove(final CapabilityIdentity id) throws IOException {
    ORID rid = convertId(id);

    try (ODatabaseDocumentTx db = openDb()) {
      // if we can't load the record, then abort
      ODocument doc = db.getRecord(rid);
      if (doc == null) {
        log.debug("Unable to delete item with RID: {}", rid);
        return false;
      }
      // else delete the record
      db.delete(doc);
    }

    log.debug("Deleted item with RID: {}", rid);
    return true;
  }

  @Override
  public Map<CapabilityIdentity, CapabilityStorageItem> getAll() throws IOException {
    Map<CapabilityIdentity, CapabilityStorageItem> items = Maps.newHashMap();

    try (ODatabaseDocumentTx db = openDb()) {
      for (ODocument doc : entityAdapter.browse(db)) {
        ORID rid = doc.getIdentity();
        CapabilityStorageItem item = entityAdapter.read(doc);
        items.put(convertId(rid), item);
      }
    }

    return items;
  }
}
