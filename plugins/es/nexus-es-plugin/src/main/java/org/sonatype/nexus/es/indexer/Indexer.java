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

package org.sonatype.nexus.es.indexer;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.es.NodeManager;
import org.sonatype.nexus.es.inspection.nexus.NexusOntology;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Indexer pushes repository and item changes to ES index.
 *
 * @since 2.7.0
 */
@Named
@Singleton
public class Indexer
    extends ComponentSupport
{
  private final NodeManager nodeManager;

  private final ObjectMapper objectMapper;

  @Inject
  public Indexer(final NodeManager nodeManager) {
    this.nodeManager = checkNotNull(nodeManager);
    this.objectMapper = new ObjectMapper();
  }

  public void repositoryAdd(final Repository repository) {
    final CreateIndexResponse create = nodeManager.indicesAdminClient()
        .create(new CreateIndexRequest(repository.getId())).actionGet();
    if (!create.isAcknowledged()) {
      log.error("Index creation for repository {} was not ACKed!", repository);
    }
  }

  public void repositoryRemove(final Repository repository) {
    final DeleteIndexResponse delete = nodeManager.indicesAdminClient()
        .delete(new DeleteIndexRequest(repository.getId())).actionGet();
    if (!delete.isAcknowledged()) {
      log.error("Index deletion for repository {} was not ACKed!", repository);
    }
  }

  public void itemChange(final StorageItem who, final Repository where, final Action what) {
    if (Action.delete == what) {
      final DeleteResponse response = nodeManager.client().prepareDelete().setIndex(where.getId())
          .setType(NexusOntology.ITEM_TYPE)
          .setId(who.getRepositoryItemUid().getKey()).execute().actionGet();
    }
    else if (Action.create == what || Action.update == what) {
      try {
        final IndexResponse response = nodeManager.client().prepareIndex().setIndex(where.getId())
            .setType(NexusOntology.ITEM_TYPE)
            .setId(who.getRepositoryItemUid().getKey()).setSource(
                objectMapper.writeValueAsString(who.getRepositoryItemAttributes().asMap())).execute().actionGet();
      }
      catch (IOException e) {
        log.warn("Problem during serialization of {} into {} for {}", who, where, what, e);
      }
    }
  }
}
