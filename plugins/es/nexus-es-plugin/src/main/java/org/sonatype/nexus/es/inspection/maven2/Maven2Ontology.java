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

/**
 * Maven2 names.
 */
public interface Maven2Ontology
{
  String MAVEN2_PREFIX = "maven2.";

  String GROUP_ID = MAVEN2_PREFIX + "groupId";

  String ARTIFACT_ID = MAVEN2_PREFIX + "artifactId";

  String VERSION = MAVEN2_PREFIX + "version";

  String BASE_VERSION = MAVEN2_PREFIX + "baseVersion";

  String CLASSIFIER = MAVEN2_PREFIX + "classifier";

  String FILE_NAME = MAVEN2_PREFIX + "fname";

  String FILE_EXTENSION = MAVEN2_PREFIX + "fextension";
}
