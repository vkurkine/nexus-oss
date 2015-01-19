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
package org.sonatype.nexus.security.targets;

import org.sonatype.security.realms.privileges.PrivilegePropertyDescriptor;

public class TargetPrivilegeGroupPropertyDescriptor
    implements PrivilegePropertyDescriptor
{
  public static final String ID = "repositoryGroupId";

  @Override
  public String getHelpText() {
    return "The repository group associated with this privilege.";
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public String getName() {
    return "Repository Group";
  }

  @Override
  public String getType() {
    return "repogroup";
  }
}
