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

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.blobstore.api.BlobRef;
import org.sonatype.nexus.orient.graph.GraphTx;
import org.sonatype.nexus.repository.Facet;

import com.tinkerpop.blueprints.Vertex;

/**
 * Storage {@link Facet}, providing component and asset storage for a repository.
 *
 * @since 3.0
 */
@Facet.Exposed
public interface StorageFacet
  extends Facet
{
  static String E_CONTAINS_COMPONENTS_WITH_LABEL = "contains_components_with_label";

  static String E_HAS_LABEL = "has_label";

  static String E_OWNS_ASSET = "owns_asset";

  static String E_OWNS_COMPONENT = "owns_component";

  static String E_PART_OF_COMPONENT = "part_of_component";

  static String P_BLOB_REF = "blob_ref";

  static String P_CONTENT_TYPE = "content_type";

  static String P_PATH = "path";

  static String P_REPOSITORY_NAME = "repository_name";

  static String V_ASSET = "asset";

  static String V_COMPONENT = "component";

  static String V_LABEL = "label";

  static String V_BUCKET = "bucket";

  /**
   * Gets a transaction for working with the graph.
   */
  GraphTx getGraphTx();

  /**
   * Gets the bucket for the current repository.
   */
  Vertex getBucket(GraphTx graph);

  /**
   * Gets all assets owned by the specified bucket.
   */
  Iterable<Vertex> browseAssets(Vertex bucket);

  /**
   * Gets all components owned by the specified bucket.
   */
  Iterable<Vertex> browseComponents(Vertex bucket);

  /**
   * Gets all vertices, optionally limited to those in the specified class.
   */
  Iterable<Vertex> browseVertices(GraphTx graph, @Nullable String className);

  /**
   * Gets an asset by id, owned by the specified bucket, or {@code null} if not found.
   */
  @Nullable
  Vertex findAsset(GraphTx graph, Object vertexId, Vertex bucket);

  /**
   * Gets an asset by some identifying property, owned by the specified bucket, or {@code null} if not found.
   */
  @Nullable
  Vertex findAssetWithProperty(GraphTx graph, String propName, Object propValue, Vertex bucket);

  /**
   * Gets a component by id, owned by the specified bucket, or {@code null} if not found.
   */
  @Nullable
  Vertex findComponent(GraphTx graph, Object vertexId, Vertex bucket);

  /**
   * Gets a component by some identifying property, or {@code null} if not found.
   */
  @Nullable
  Vertex findComponentWithProperty(GraphTx graph, String propName, Object propValue, Vertex bucket);

  /**
   * Gets a vertex by id, optionally limited by class, or {@code null} if not found.
   */
  @Nullable
  Vertex findVertex(GraphTx graph, Object vertexId, @Nullable String className);

  /**
   * Gets a vertex by some identifying property, optionally limited by class, or {@code null} if not found.
   */
  @Nullable
  Vertex findVertexWithProperty(GraphTx graph, String propName, Object propValue, @Nullable String className);

  /**
   * Creates a new asset owned by the specified bucket.
   */
  Vertex createAsset(GraphTx graph, Vertex bucket);

  /**
   * Creates a new component owned by the specified bucket.
   */
  Vertex createComponent(GraphTx graph, Vertex bucket);

  /**
   * Creates a new vertex of the specified class.
   */
  Vertex createVertex(GraphTx graph, String className);

  /**
   * Deletes an existing vertex.
   */
  void deleteVertex(GraphTx graph, Vertex vertex);

  /**
   * Creates a new Blob.
   */
  BlobRef createBlob(InputStream inputStream, Map<String, String> headers);

  /**
   * Gets a Blob, or {@code null if not found}.
   */
  @Nullable
  Blob getBlob(BlobRef blobRef);

  /**
   * Deletes a Blob.
   */
  boolean deleteBlob(BlobRef blobRef);
}
