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
package org.sonatype.security.realms;

import java.util.HashSet;
import java.util.Set;

import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.security.AbstractSecurityTestCase;
import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.model.CRole;
import org.sonatype.security.model.CUser;
import org.sonatype.security.realms.privileges.application.ApplicationPrivilegeDescriptor;
import org.sonatype.security.realms.privileges.application.ApplicationPrivilegeMethodPropertyDescriptor;
import org.sonatype.security.realms.privileges.application.ApplicationPrivilegePermissionPropertyDescriptor;
import org.sonatype.security.realms.tools.DefaultConfigurationManager;
import org.sonatype.security.usermanagement.UserStatus;

import junit.framework.Assert;
import org.apache.shiro.authz.permission.RolePermissionResolver;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.SimplePrincipalCollection;

/**
 * Tests for {@link AuthorizingRealmImpl}.
 */
public class AuthorizingRealmImplTest
    extends AbstractSecurityTestCase
{
  private AuthorizingRealmImpl realm;

  private DefaultConfigurationManager configurationManager;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    realm = (AuthorizingRealmImpl) lookup(Realm.class, "NexusAuthorizingRealm");
    realm.setRolePermissionResolver(this.lookup(RolePermissionResolver.class));

    configurationManager = lookup(DefaultConfigurationManager.class);
  }

  public void testAuthorization() throws Exception {
    buildTestAuthorizationConfig();

    // Fails because the configuration requirement in nexus authorizing realm isn't initialized
    // thus NPE
    SimplePrincipalCollection principal = new SimplePrincipalCollection("username", realm.getName());

    Assert.assertTrue(realm.hasRole(principal, "role"));

    // Verify the permission
    Assert.assertTrue(realm.isPermitted(principal, new WildcardPermission("app:config:read")));
    // Verify other method not allowed
    Assert.assertFalse(realm.isPermitted(principal, new WildcardPermission("app:config:create")));
    Assert.assertFalse(realm.isPermitted(principal, new WildcardPermission("app:config:update")));
    Assert.assertFalse(realm.isPermitted(principal, new WildcardPermission("app:config:delete")));

    // Verify other permission not allowed
    Assert.assertFalse(realm.isPermitted(principal, new WildcardPermission("app:ui:read")));
    Assert.assertFalse(realm.isPermitted(principal, new WildcardPermission("app:ui:create")));
    Assert.assertFalse(realm.isPermitted(principal, new WildcardPermission("app:ui:update")));
    Assert.assertFalse(realm.isPermitted(principal, new WildcardPermission("app:ui:delete")));
  }

  private void buildTestAuthorizationConfig() throws InvalidConfigurationException {
    buildTestAuthorizationConfig("username");
  }

  private void buildTestAuthorizationConfig(String userId) throws InvalidConfigurationException {
    CPrivilege priv = new CPrivilege();
    priv.setId("priv");
    priv.setName("somepriv");
    priv.setType(ApplicationPrivilegeDescriptor.TYPE);
    priv.setDescription("somedescription");
    priv.setProperty(ApplicationPrivilegePermissionPropertyDescriptor.ID, "app:config");
    priv.setProperty(ApplicationPrivilegeMethodPropertyDescriptor.ID, "read");

    configurationManager.createPrivilege(priv);

    CRole role = new CRole();
    role.setId("role");
    role.setName("somerole");
    role.setDescription("somedescription");
    role.addPrivilege(priv.getId());

    configurationManager.createRole(role);

    CUser user = new CUser();
    user.setEmail("dummyemail@foo");
    user.setFirstName("dummyFirstName");
    user.setLastName("dummyLastName");
    user.setStatus(UserStatus.active.toString());
    user.setId(userId);
    user.setPassword("password");

    Set<String> roles = new HashSet<String>();
    roles.add(role.getId());

    configurationManager.createUser(user, roles);
  }
}
