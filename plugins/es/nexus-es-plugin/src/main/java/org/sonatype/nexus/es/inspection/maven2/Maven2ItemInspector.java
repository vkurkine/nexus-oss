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

package org.sonatype.nexus.es.inspection.maven2;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.proxy.attributes.AbstractStorageItemInspector;
import org.sonatype.nexus.proxy.attributes.Attributes;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.gav.Gav;
import org.sonatype.nexus.proxy.maven.gav.GavCalculator;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;

import com.google.common.base.Strings;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Inspector for Maven2 item properties.
 *
 * @since 2.7.0
 */
@Named
@Singleton
public class Maven2ItemInspector
    extends AbstractStorageItemInspector
{
  private final static String GAV_KEY = Maven2ItemInspector.class.getName() + ".gav";

  private final Maven2ContentClass maven2ContentClass;

  @Inject
  public Maven2ItemInspector(final Maven2ContentClass maven2ContentClass) {
    this.maven2ContentClass = checkNotNull(maven2ContentClass);
  }

  @Override
  public boolean isHandled(final StorageItem item) {
    // file item originating from MavenRepository and originating repository is compatible to M2 (exclude M1)
    if (item instanceof StorageFileItem || item instanceof StorageLinkItem) {
      final MavenRepository mavenRepository = item.getRepositoryItemUid().getRepository()
          .adaptToFacet(MavenRepository.class);
      if (mavenRepository != null && maven2ContentClass.isCompatible(mavenRepository.getRepositoryContentClass())) {
        final GavCalculator gavCalculator = mavenRepository.getGavCalculator();
        final Gav gav = gavCalculator.pathToGav(item.getPath());
        if (gav != null) {
          // we basically did the majority of the work, so let's save GAV for later
          item.getItemContext().put(GAV_KEY, gav);
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public void processStorageItem(final StorageItem item) throws Exception {
    final Gav gav = (Gav) item.getItemContext().get(GAV_KEY);
    if (gav != null && !gav.isHash() && !gav.isSignature()) {
      final Attributes attributes = item.getRepositoryItemAttributes();
      // these must exists, so we go risk NPE (to reveal any possible bug)
      attributes.put(Maven2Ontology.GROUP_ID, gav.getGroupId());
      attributes.put(Maven2Ontology.ARTIFACT_ID, gav.getArtifactId());
      attributes.put(Maven2Ontology.VERSION, gav.getVersion());
      attributes.put(Maven2Ontology.BASE_VERSION, gav.getBaseVersion());
      // these below are optional
      putIfNotEmpty(attributes, Maven2Ontology.CLASSIFIER, gav.getClassifier());
      putIfNotEmpty(attributes, Maven2Ontology.FILE_NAME, gav.getName());
      putIfNotEmpty(attributes, Maven2Ontology.FILE_EXTENSION, gav.getExtension());
    }
  }

  private void putIfNotEmpty(final Attributes attributes, final String key, final String value) {
    checkNotNull(attributes);
    checkNotNull(key);
    if (!Strings.isNullOrEmpty(value)) {
      attributes.put(key, value);
    }
  }
}
