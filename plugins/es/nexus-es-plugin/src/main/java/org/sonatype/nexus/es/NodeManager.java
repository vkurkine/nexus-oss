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

package org.sonatype.nexus.es;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.util.file.FileSupport;
import org.sonatype.sisu.goodies.lifecycle.LifecycleSupport;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.node.Node;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

/**
 * Elasticsearch Node manager.
 *
 * @since 2.7.0
 */
@Named
@Singleton
public class NodeManager
    extends LifecycleSupport
{
  private final ApplicationConfiguration applicationConfiguration;

  private Node node;

  @Inject
  public NodeManager(final ApplicationConfiguration applicationConfiguration) {
    this.applicationConfiguration = checkNotNull(applicationConfiguration);
  }

  @Override
  protected void doStart() throws Exception {
    Builder settingsBuilder = ImmutableSettings.settingsBuilder();
    settingsBuilder.classLoader(NodeManager.class.getClassLoader());

    final File configurationFile = new File(applicationConfiguration.getConfigurationDirectory(),
        "elasticsearch.yml");
    if (!configurationFile.exists()) {
      FileSupport.copy(getClass().getResourceAsStream("/default-elasticsearch.yml"), configurationFile.toPath());
    }
    settingsBuilder.loadFromUrl(configurationFile.toURI().toURL());

    node = nodeBuilder().settings(settingsBuilder).node();
    node.start();
    node.client().admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet();
  }

  @Override
  protected void doStop() throws Exception {
    if (node != null) {
      node.close();
      node = null;
    }
  }

  public Node node() {
    if (isStarted()) {
      return node;
    }
    else {
      throw new IllegalStateException("ES Node not yet started, premature call?");
    }
  }

  public Client client() {
    return node().client();
  }

  public IndicesAdminClient indicesAdminClient() {
    return client().admin().indices();
  }
}
