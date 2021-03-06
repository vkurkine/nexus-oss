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
package org.sonatype.security.realms.ldap.internal.realms;


import org.sonatype.ldaptestsuite.LdapServer;
import org.sonatype.ldaptestsuite.LdapServerConfiguration;
import org.sonatype.security.realms.ldap.internal.persist.entity.LdapConfiguration;
import org.sonatype.security.realms.ldap.internal.persist.entity.Mapping;

public class NestedUsersLdapSchemaIT
    extends LdapSchemaTestSupport
{
  @Override
  protected LdapServerConfiguration createServerConfiguration(final String name) {
    return LdapServerConfiguration.builder()
        .withWorkingDirectory(util.createTempDir())
        .withPartitions(createPartition(name))
        .withAdditionalSchemas("org.apache.directory.server.schema.bootstrap.NisSchema")
        .withSasl("localhost", "ldap/localhost@EXAMPLE.COM", "ou=system", "localhost")
        .build();
  }

  @Override
  protected LdapConfiguration createLdapClientConfigurationForServer(final String name, final int order,
                                                                     final LdapServer ldapServer)
  {
    final LdapConfiguration ldapConfiguration = super.createLdapClientConfigurationForServer(name, order, ldapServer);

    // adjust it, ITs by default uses different groups
    final Mapping mapping = ldapConfiguration.getMapping();
    mapping.setGroupMemberFormat("${username}");
    mapping.setGroupObjectClass("posixGroup");
    mapping.setGroupBaseDn("ou=groups");
    mapping.setGroupIdAttribute("cn");
    mapping.setGroupMemberAttribute("memberUid");
    mapping.setUserObjectClass("inetOrgPerson");
    mapping.setUserBaseDn("ou=people");
    mapping.setUserIdAttribute("uid");
    mapping.setUserPasswordAttribute("userPassword");
    mapping.setUserRealNameAttribute("cn");
    mapping.setEmailAddressAttribute("mail");
    mapping.setUserSubtree(true);
    mapping.setLdapGroupsAsRoles(true);

    return ldapConfiguration;
  }
}
