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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.events.EventSubscriber;
import org.sonatype.nexus.proxy.events.RepositoryConfigurationUpdatedEvent;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventPostRemove;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.eventbus.Subscribe;

import static com.google.common.base.Preconditions.checkNotNull;

@Named
@Singleton
public class ESEventSubscriber
    extends ComponentSupport
    implements EventSubscriber
{
  private final Indexer indexer;

  @Inject
  public ESEventSubscriber(final Indexer indexer) {
    this.indexer = checkNotNull(indexer);
  }

  @Subscribe
  public void on(final RepositoryRegistryEventAdd evt) {
    indexer.repositoryAdd(evt.getRepository());
  }

  @Subscribe
  public void on(final RepositoryRegistryEventPostRemove evt) {
    indexer.repositoryRemove(evt.getRepository());
  }

  @Subscribe
  public void on(final RepositoryConfigurationUpdatedEvent evt) {
    //
  }
}
