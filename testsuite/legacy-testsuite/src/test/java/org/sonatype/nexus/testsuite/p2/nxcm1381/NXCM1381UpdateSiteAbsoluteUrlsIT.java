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
package org.sonatype.nexus.testsuite.p2.nxcm1381;

import java.io.File;

import org.sonatype.nexus.plugins.p2.repository.updatesite.UpdateSiteMirrorTask;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.sonatype.nexus.testsuite.p2.AbstractNexusProxyP2IT;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers.exists;

public class NXCM1381UpdateSiteAbsoluteUrlsIT
    extends AbstractNexusProxyP2IT
{

  public NXCM1381UpdateSiteAbsoluteUrlsIT() {
    super("nxcm1381");
  }

  @Test
  public void test()
      throws Exception
  {
    final File nexusDir = new File(nexusWorkDir, "storage/nxcm1381");

    ScheduledServicePropertyResource forceMirror = new ScheduledServicePropertyResource();
    forceMirror.setKey("ForceMirror");
    forceMirror.setValue(Boolean.TRUE.toString());
    ScheduledServicePropertyResource repositoryId = new ScheduledServicePropertyResource();
    repositoryId.setKey("repositoryId");
    repositoryId.setValue("nxcm1381");
    TaskScheduleUtil.runTask("test", UpdateSiteMirrorTask.class.getName(), forceMirror, repositoryId);
    TaskScheduleUtil.waitForAllTasksToStop();

    assertThat(new File(nexusDir, "features/com.sonatype.nexus.p2.its.feature_1.0.0.jar"), exists());
    assertThat(new File(nexusDir, "plugins/com.sonatype.nexus.p2.its.bundle_1.0.0.jar"), exists());
  }

}
