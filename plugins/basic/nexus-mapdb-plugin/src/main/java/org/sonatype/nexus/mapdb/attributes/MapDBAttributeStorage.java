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

package org.sonatype.nexus.mapdb.attributes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.mapdb.MapDbManager;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.attributes.AbstractAttributeStorage;
import org.sonatype.nexus.proxy.attributes.AttributeStorage;
import org.sonatype.nexus.proxy.attributes.Attributes;
import org.sonatype.nexus.proxy.attributes.JacksonJSONMarshaller;
import org.sonatype.nexus.proxy.attributes.Marshaller;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.RepositoryItemUidLock;

import org.mapdb.DB;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link AttributeStorage} implementation that uses MapDB to store attributes.
 *
 * @author cstamas
 * @since 2.7.0
 */
@Typed(AttributeStorage.class)
@Named("mapdb")
@Singleton
public class MapDBAttributeStorage
    extends AbstractAttributeStorage
    implements AttributeStorage
{
  private static final String DB_KEY = "nx-attributes";

  private static final String ATTRIBUTES_MAP_NAME = Attributes.class.getName();

  private final Marshaller marshaller;

  private final DB mapDb;

  private final Map<String, byte[]> attributesMap;

  /**
   * Instantiates a new LevelDB attribute storage.
   */
  @Inject
  public MapDBAttributeStorage(final MapDbManager mapDbManager)
      throws IOException
  {
    this(mapDbManager, new JacksonJSONMarshaller());
  }

  /**
   * Instantiates a new LevelDB attribute storage.
   */
  public MapDBAttributeStorage(final MapDbManager mapDbManager, final Marshaller marshaller)
      throws IOException
  {
    checkNotNull(mapDbManager);
    this.marshaller = checkNotNull(marshaller);
    // we obtain a managed DB instance, as we need it throughout whole Nx lifecycle, and manager will close it out for us
    mapDb = mapDbManager.openManaged(DB_KEY, null);
    if (mapDb.exists(ATTRIBUTES_MAP_NAME)) {
      mapDb.commit();
      mapDb.compact();
      attributesMap = mapDb.getHashMap(ATTRIBUTES_MAP_NAME);
    }
    else {
      attributesMap = mapDb.createHashMap(ATTRIBUTES_MAP_NAME).make();
    }
    getLogger().info("MapDB AttributeStorage in place, using {} marshaller.", marshaller);
  }

  @Override
  public boolean deleteAttributes(final RepositoryItemUid uid)
      throws IOException
  {
    final RepositoryItemUidLock uidLock = uid.getLock();
    uidLock.lock(Action.delete);
    try {
      getLogger().debug("Deleting attribute on UID={}", uid);
      try {
        attributesMap.remove(uid.getKey());
        mapDb.commit();
        return true;
      }
      catch (Exception e) {
        getLogger().error("Got Exception during deletion of UID=" + uid.toString(), e);
      }
      return false;
    }
    finally {
      uidLock.unlock();
    }
  }

  @Override
  public Attributes getAttributes(final RepositoryItemUid uid)
      throws IOException
  {
    final RepositoryItemUidLock uidLock = uid.getLock();
    uidLock.lock(Action.read);
    try {
      getLogger().debug("Loading attribute on UID={}", uid);
      final byte[] payload = attributesMap.get(uid.getKey());
      if (payload != null) {
        final Attributes result = marshaller.unmarshal(new ByteArrayInputStream(payload));
        result.setRepositoryId(uid.getRepository().getId());
        result.setPath(uid.getPath());
        // fixing remoteChecked
        if (result.getCheckedRemotely() == 0 || result.getCheckedRemotely() == 1) {
          result.setCheckedRemotely(System.currentTimeMillis());
          result.setExpired(true);
        }
        // fixing lastRequested
        if (result.getLastRequested() == 0) {
          result.setLastRequested(System.currentTimeMillis());
        }
        return result;
      }
    }
    catch (Exception e) {
      getLogger().error("Got Exception during retrieval of UID=" + uid.toString(), e);
    }
    finally {
      uidLock.unlock();
    }
    return null;
  }

  @Override
  public void putAttributes(final RepositoryItemUid uid, Attributes attributes)
      throws IOException
  {
    checkNotNull(attributes);
    final RepositoryItemUidLock uidLock = uid.getLock();
    uidLock.lock(Action.create);
    try {
      getLogger().debug("Storing attribute on UID={}", uid);
      try {
        final Attributes onDisk = getAttributes(uid);
        if (onDisk != null && (onDisk.getGeneration() > attributes.getGeneration())) {
          // change detected, overlay the to be saved onto the newer one and swap
          onDisk.overlayAttributes(attributes);
          // and overlay other things too
          onDisk.setRepositoryId(uid.getRepository().getId());
          onDisk.setPath(uid.getPath());
          onDisk.setReadable(attributes.isReadable());
          onDisk.setWritable(attributes.isWritable());
          attributes = onDisk;
        }
        attributes.incrementGeneration();
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        marshaller.marshal(attributes, bos);
        attributesMap.put(uid.getKey(), bos.toByteArray());
        mapDb.commit();
      }
      catch (Exception e) {
        getLogger().error("Got Exception during store of UID=" + uid.toString(), e);
      }
    }
    finally {
      uidLock.unlock();
    }
  }
}
