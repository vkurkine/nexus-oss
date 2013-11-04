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

package org.sonatype.nexus.es.inspection.jar;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.proxy.attributes.AbstractStorageItemInspector;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;

/**
 * Inspector for Jar item properties.
 *
 * @since 2.7.0
 */
@Named
@Singleton
public class JarItemInspector
    extends AbstractStorageItemInspector
{
  @Override
  public boolean isHandled(final StorageItem item) {
    if (item instanceof StorageFileItem) {
      final StorageFileItem fileItem = (StorageFileItem) item;
      final String fileMimeType = fileItem.getContentLocator().getMimeType();
      return "application/java-archive".equals(fileMimeType);
    }
    return false;
  }

  @Override
  public void processStorageItem(final StorageItem item) throws Exception {
    // TODO:
  }
}
