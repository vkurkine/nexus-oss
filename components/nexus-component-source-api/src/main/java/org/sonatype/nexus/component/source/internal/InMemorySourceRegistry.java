/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.component.source.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.component.source.api.ComponentSource;
import org.sonatype.nexus.component.source.api.ComponentSourceId;
import org.sonatype.nexus.component.source.api.ComponentSourceRegistry;
import org.sonatype.nexus.component.source.api.PullComponentSource;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * A registry of the currently available component sources.
 *
 * @since 3.0
 */
@Named
@Singleton
public class InMemorySourceRegistry
    extends ComponentSupport
    implements ComponentSourceRegistry
{
  private final ConcurrentHashMap<ComponentSourceId, ComponentSource> sources = new ConcurrentHashMap<>();

  public synchronized void register(final ComponentSource source) {
    checkNotNull(source);

    final ComponentSource wrap = wrap(source);

    final ComponentSource alreadyBound = sources.putIfAbsent(wrap.getId(), wrap);

    checkState(alreadyBound == null, "A source is already bound to name %s", wrap.getId());

    log.info("Registering component source {}", wrap);
  }

  public boolean unregister(ComponentSource source) {
    checkNotNull(source);

    final ComponentSource removed = sources.remove(source.getId());

    if (removed instanceof ComponentSourceSleeve) {
      ((ComponentSourceSleeve) removed).disable();
    }

    if (removed != null) {
      log.info("Unregistering source {}", source);
    }

    return removed != null;
  }

  @Override
  public <T extends ComponentSource> T getSource(String name) {
    for (Map.Entry<ComponentSourceId, ComponentSource> entry : sources.entrySet()) {
      if (entry.getKey().getName().equals(name)) {
        return (T) entry.getValue();
      }
    }
    return null;
  }

  @Nullable
  @Override
  public <T extends ComponentSource> T getSource(final ComponentSourceId sourceId) {
    return (T) this.sources.get(sourceId);
  }

  private ComponentSource wrap(ComponentSource source) {
    if (source instanceof PullComponentSource) {
      return new ComponentSourceSleeve((PullComponentSource) source);
    }
    return source;
  }
}
