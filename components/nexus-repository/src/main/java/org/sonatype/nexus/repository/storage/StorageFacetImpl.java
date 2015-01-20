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
package org.sonatype.nexus.repository.storage;

import java.io.InputStream;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.blobstore.api.BlobRef;
import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.blobstore.api.BlobStoreManager;
import org.sonatype.nexus.common.stateguard.Guarded;
import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.nexus.orient.graph.GraphTx;
import org.sonatype.nexus.repository.FacetSupport;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientEdgeType;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.tinkerpop.blueprints.impls.orient.OrientVertexType;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.common.stateguard.StateGuardLifecycleSupport.State.STARTED;

/**
 * Default {@link StorageFacet} implementation.
 *
 * @since 3.0
 */
@Named
public class StorageFacetImpl
  extends FacetSupport
  implements StorageFacet
{
  private final BlobStoreManager blobStoreManager;

  private final Provider<DatabaseInstance> databaseInstanceProvider;

  private Object bucketId;

  @Inject
  public StorageFacetImpl(final BlobStoreManager blobStoreManager,
                          @Named(ComponentDatabase.NAME) Provider<DatabaseInstance> databaseInstanceProvider) {
    this.blobStoreManager = checkNotNull(blobStoreManager);
    this.databaseInstanceProvider = checkNotNull(databaseInstanceProvider);
  }

  @Override
  protected void doStart() throws Exception {
    initSchema();
    initBucket();
  }

  private void initSchema() {
    // initialize the graph schema if needed
    final CheckedGraphNoTx graph = new CheckedGraphNoTx(databaseInstanceProvider.get().acquire());
    try {
      initVertexType(graph, V_ASSET, new Predicate<OrientVertexType>() {
        @SuppressWarnings("unchecked")
        @Override
        public boolean apply(final OrientVertexType type) {
          type.createProperty(P_PATH, OType.STRING);
          graph.createKeyIndex(P_PATH, Vertex.class, new Parameter("class", V_ASSET));
          return true;
        }
      });
      initVertexType(graph, V_BUCKET, new Predicate<OrientVertexType>() {
        @SuppressWarnings("unchecked")
        @Override
        public boolean apply(final OrientVertexType type) {
          type.createProperty(P_REPOSITORY_NAME, OType.STRING);
          graph.createKeyIndex(P_REPOSITORY_NAME, Vertex.class, new Parameter("type", "UNIQUE"),
              new Parameter("class", V_BUCKET));
          return true;
        }
      });
      initVertexType(graph, V_COMPONENT, null);
      initVertexType(graph, V_LABEL, null);

      initEdgeType(graph, E_CONTAINS_COMPONENTS_WITH_LABEL, null);
      initEdgeType(graph, E_HAS_LABEL, null);
      initEdgeType(graph, E_OWNS_ASSET, null);
      initEdgeType(graph, E_OWNS_COMPONENT, null);
      initEdgeType(graph, E_PART_OF_COMPONENT, null);
    }
    finally {
      graph.shutdown();
    }
  }

  private static class CheckedGraphNoTx extends OrientGraphNoTx
  {
    public CheckedGraphNoTx(ODatabaseDocumentTx db) {
      super(db);
      checkForGraphSchema(db);
    }
  }

  private void initVertexType(CheckedGraphNoTx graph, String name, @Nullable Predicate<OrientVertexType> predicate) {
    if (graph.getVertexType(name) == null) {
      OrientVertexType type = graph.createVertexType(name);
      if (predicate != null) {
        predicate.apply(type);
      }
    }
  }

  private void initEdgeType(CheckedGraphNoTx graph, String name, @Nullable Predicate<OrientEdgeType> predicate) {
    if (graph.getEdgeType(name) == null) {
      OrientEdgeType type = graph.createEdgeType(name);
      if (predicate != null) {
        predicate.apply(type);
      }
    }
  }

  private void initBucket() {
    // get or create the bucket for the repository and set bucketId for fast lookup later
    try (GraphTx graph = new GraphTx(databaseInstanceProvider.get().acquire())) {
      String repositoryName = getRepository().getName();
      Vertex bucket = findVertexWithProperty(graph, P_REPOSITORY_NAME, repositoryName, V_BUCKET);
      if (bucket == null) {
        bucket = createVertex(graph, V_BUCKET);
        bucket.setProperty(P_REPOSITORY_NAME, repositoryName);
        graph.commit();
      }
      bucketId = bucket.getId();
    }
  }

  @Override
  @Guarded(by=STARTED)
  public GraphTx getGraphTx() {
    return new GraphTx(databaseInstanceProvider.get().acquire());
  }

  @Override
  @Guarded(by=STARTED)
  public Vertex getBucket(GraphTx graph) {
    checkNotNull(graph);

    return findVertex(graph, bucketId, null);
  }

  @Override
  @Guarded(by=STARTED)
  public Iterable<Vertex> browseAssets(final Vertex bucket) {
    checkNotNull(bucket);

    return bucket.getVertices(Direction.OUT, E_OWNS_ASSET);
  }

  @Override
  @Guarded(by=STARTED)
  public Iterable<Vertex> browseComponents(final Vertex bucket) {
    checkNotNull(bucket);

    return bucket.getVertices(Direction.OUT, E_OWNS_COMPONENT);
  }

  @Override
  @Guarded(by=STARTED)
  public Iterable<Vertex> browseVertices(final GraphTx graph, @Nullable final String className) {
    checkNotNull(graph);

    if (className == null) {
      return graph.getVertices();
    }
    else {
      return graph.getVerticesOfClass(className);
    }
  }

  @Nullable
  @Override
  @Guarded(by=STARTED)
  public Vertex findAsset(final GraphTx graph, final Object vertexId, final Vertex bucket) {
    checkNotNull(graph);
    checkNotNull(vertexId);
    checkNotNull(bucket);

    Vertex vertex = findVertex(graph, vertexId, V_ASSET);
    return bucketOwns(bucket, E_OWNS_ASSET, vertex) ? vertex : null;
  }

  private boolean bucketOwns(Vertex bucket, String edgeLabel, @Nullable Vertex item) {
    if (item == null) {
      return false;
    }
    Vertex first = Iterables.getFirst(item.getVertices(Direction.IN, edgeLabel), null);
    return bucket.equals(first);
  }

  @Nullable
  @Override
  @Guarded(by=STARTED)
  public Vertex findAssetWithProperty(final GraphTx graph, final String propName, final Object propValue,
                                      final Vertex bucket)
  {
    return findWithPropertyOwnedBy(graph, V_ASSET, propName, propValue, E_OWNS_ASSET, bucket);
  }

  @SuppressWarnings("unchecked")
  private Vertex findWithPropertyOwnedBy(GraphTx graph, String className, String propName, Object propValue,
                                         String edgeLabel, Vertex bucket) {
    checkNotNull(graph);
    checkNotNull(propName);
    checkNotNull(propValue);
    checkNotNull(bucket);

    Map<String, Object> parameters = ImmutableMap.of("propValue", propValue, "bucket", bucket);
    String query = String.format("select from %s where %s = :propValue and in('%s') contains :bucket",
        className, propName, edgeLabel);
    Iterable<Vertex> vertices = (Iterable<Vertex>) graph.command(new OCommandSQL(query)).execute(parameters);
    return Iterables.getFirst(vertices, null);
  }

  @Nullable
  @Override
  @Guarded(by=STARTED)
  public Vertex findComponent(final GraphTx graph, final Object vertexId, final Vertex bucket) {
    checkNotNull(graph);
    checkNotNull(vertexId);
    checkNotNull(bucket);

    Vertex vertex = findVertex(graph, vertexId, V_COMPONENT);
    return bucketOwns(bucket, E_OWNS_COMPONENT, vertex) ? vertex : null;
  }

  @Nullable
  @Override
  @Guarded(by=STARTED)
  public Vertex findComponentWithProperty(final GraphTx graph, final String propName, final Object propValue,
                                          final Vertex bucket)
  {
    return findWithPropertyOwnedBy(graph, V_COMPONENT, propName, propValue, E_OWNS_COMPONENT, bucket);
  }

  @Nullable
  @Override
  @Guarded(by=STARTED)
  public Vertex findVertex(final GraphTx graph, final Object vertexId, @Nullable final String className) {
    checkNotNull(graph);
    checkNotNull(vertexId);

    Vertex vertex = graph.getVertex(vertexId);
    if (vertex != null && className != null && !vertex.getProperty("@class").equals(className)) {
      return null;
    }
    return vertex;
  }

  @Nullable
  @Override
  @Guarded(by=STARTED)
  public Vertex findVertexWithProperty(final GraphTx graph, final String propName, final Object propValue,
                                       @Nullable final String className) {
    checkNotNull(graph);
    checkNotNull(propName);
    checkNotNull(propValue);

    Vertex vertex = Iterables.getFirst(graph.getVertices(propName, propValue), null);
    if (vertex != null && className != null && !vertex.getProperty("@class").equals(className)) {
      return null;
    }
    return vertex;
  }

  @Override
  @Guarded(by=STARTED)
  public Vertex createAsset(final GraphTx graph, final Vertex bucket) {
    checkNotNull(graph);
    checkNotNull(bucket);

    Vertex asset = createVertex(graph, V_ASSET);
    graph.addEdge(null, bucket, asset, E_OWNS_ASSET);
    return asset;
  }

  @Override
  @Guarded(by=STARTED)
  public Vertex createComponent(final GraphTx graph, final Vertex bucket) {
    checkNotNull(graph);
    checkNotNull(bucket);

    Vertex component = createVertex(graph, V_COMPONENT);
    graph.addEdge(null, bucket, component, E_OWNS_COMPONENT);
    return component;
  }

  @Override
  @Guarded(by=STARTED)
  public Vertex createVertex(final GraphTx graph, final String className) {
    checkNotNull(graph);
    checkNotNull(className);

    return graph.addVertex(className, (String) null);
  }

  @Override
  @Guarded(by=STARTED)
  public void deleteVertex(final GraphTx graph, final Vertex vertex) {
    checkNotNull(graph);
    checkNotNull(vertex);

    graph.removeVertex(vertex);
  }

  private BlobStore blobStore() {
    return blobStoreManager.get("default");
  }

  @Override
  @Guarded(by=STARTED)
  public BlobRef createBlob(final InputStream inputStream, Map<String, String> headers) {
    checkNotNull(inputStream);
    checkNotNull(headers);

    Blob blob = blobStore().create(inputStream, headers);
    return new BlobRef("NODE", "STORE", blob.getId().asUniqueString());
  }

  @Nullable
  @Override
  @Guarded(by=STARTED)
  public Blob getBlob(final BlobRef blobRef) {
    checkNotNull(blobRef);

    return blobStore().get(blobRef.getBlobId());
  }

  @Override
  @Guarded(by=STARTED)
  public boolean deleteBlob(final BlobRef blobRef) {
    checkNotNull(blobRef);

    return blobStore().delete(blobRef.getBlobId());
  }
}
