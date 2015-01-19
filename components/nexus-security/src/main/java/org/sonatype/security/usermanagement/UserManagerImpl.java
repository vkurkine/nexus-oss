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
package org.sonatype.security.usermanagement;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.authorization.NoSuchRoleException;
import org.sonatype.security.model.CRole;
import org.sonatype.security.model.CUser;
import org.sonatype.security.model.CUserRoleMapping;
import org.sonatype.security.realms.tools.ConfigurationManager;
import org.sonatype.security.realms.tools.NoSuchRoleMappingException;

import org.apache.shiro.authc.credential.PasswordService;
import org.eclipse.sisu.Description;

/**
 * Default {@link UserManager}.
 */
@Singleton
@Typed(UserManager.class)
@Named("default")
@Description("Default")
public class UserManagerImpl
    extends AbstractUserManager
    implements RoleMappingUserManager
{
  public static final String SOURCE = "default";

  private final ConfigurationManager configuration;

  //private final SecurityConfigurationManager securityConfiguration;

  private final SecuritySystem securitySystem;

  private final PasswordService passwordService;

  @Inject
  public UserManagerImpl(final ConfigurationManager configuration,
                         final SecuritySystem securitySystem,
                         final PasswordService passwordService)
  {
    this.configuration = configuration;
    this.securitySystem = securitySystem;
    this.passwordService = passwordService;
  }

  protected CUser toUser(User user) {
    if (user == null) {
      return null;
    }

    CUser secUser = new CUser();

    secUser.setId(user.getUserId());
    secUser.setVersion(user.getVersion());
    secUser.setFirstName(user.getFirstName());
    secUser.setLastName(user.getLastName());
    secUser.setEmail(user.getEmailAddress());
    secUser.setStatus(user.getStatus().name());
    // secUser.setPassword( password )// DO NOT set the users password!

    return secUser;
  }

  protected User toUser(CUser cUser) {
    if (cUser == null) {
      return null;
    }

    User user = new User();

    user.setUserId(cUser.getId());
    user.setVersion(cUser.getVersion());
    user.setFirstName(cUser.getFirstName());
    user.setLastName(cUser.getLastName());
    user.setEmailAddress(cUser.getEmail());
    user.setSource(SOURCE);
    user.setStatus(UserStatus.valueOf(cUser.getStatus()));

    try {
      user.setRoles(this.getUsersRoles(cUser.getId(), SOURCE));
    }
    catch (UserNotFoundException e) {
      // We should NEVER get here
      this.log.warn("Could not find user: '" + cUser.getId() + "' of source: '" + SOURCE
          + "' while looking up the users roles.", e);
    }

    return user;
  }

  protected RoleIdentifier toRole(String roleId) {
    if (roleId == null) {
      return null;
    }

    try {
      CRole role = configuration.readRole(roleId);

      RoleIdentifier roleIdentifier = new RoleIdentifier(SOURCE, role.getId());
      return roleIdentifier;
    }
    catch (NoSuchRoleException e) {
      return null;
    }
  }

  public Set<User> listUsers() {
    Set<User> users = new HashSet<User>();

    for (CUser user : configuration.listUsers()) {
      users.add(toUser(user));
    }

    return users;
  }

  public Set<String> listUserIds() {
    Set<String> userIds = new HashSet<String>();

    for (CUser user : configuration.listUsers()) {
      userIds.add(user.getId());
    }

    return userIds;
  }

  public User getUser(String userId)
      throws UserNotFoundException
  {
    return toUser(configuration.readUser(userId));
  }

  public String getSource() {
    return SOURCE;
  }

  public boolean supportsWrite() {
    return true;
  }

  public User addUser(final User user, String password)
      throws InvalidConfigurationException
  {
    final CUser secUser = this.toUser(user);
    secUser.setPassword(this.hashPassword(password));

    configuration.createUser(secUser, getRoleIdsFromUser(user));

    // TODO: i am starting to feel we shouldn't return a user.
    return user;
  }

  public void changePassword(final String userId, final String newPassword)
      throws UserNotFoundException, InvalidConfigurationException
  {
    final CUser secUser = configuration.readUser(userId);
    secUser.setPassword(hashPassword(newPassword));
    configuration.updateUser(secUser);
  }

  public User updateUser(final User user)
      throws UserNotFoundException, InvalidConfigurationException
  {
    // we need to pull the users password off off the old user object
    CUser oldSecUser = configuration.readUser(user.getUserId());
    CUser newSecUser = toUser(user);
    newSecUser.setPassword(oldSecUser.getPassword());

    configuration.updateUser(newSecUser, getRoleIdsFromUser(user));
    return user;
  }

  public void deleteUser(final String userId)
      throws UserNotFoundException
  {
    configuration.deleteUser(userId);
  }

  public Set<RoleIdentifier> getUsersRoles(final String userId, final String source)
      throws UserNotFoundException
  {
    final Set<RoleIdentifier> roles = new HashSet<RoleIdentifier>();

    try {
      CUserRoleMapping roleMapping = configuration.readUserRoleMapping(userId, source);
      if (roleMapping != null) {
        for (String roleId : roleMapping.getRoles()) {
          RoleIdentifier role = toRole(roleId);
          if (role != null) {
            roles.add(role);
          }
        }
      }
    }
    catch (NoSuchRoleMappingException e) {
      log.debug("No user role mapping found for user: " + userId);
    }

    return roles;
  }

  private void saveConfiguration() {
  }

  public Set<User> searchUsers(final UserSearchCriteria criteria) {
    final Set<User> users = new HashSet<User>();

    users.addAll(filterListInMemeory(listUsers(), criteria));

    // we also need to search through the user role mappings.

    List<CUserRoleMapping> roleMappings = configuration.listUserRoleMappings();
    for (CUserRoleMapping roleMapping : roleMappings) {
      if (!SOURCE.equals(roleMapping.getSource())) {
        if (matchesCriteria(roleMapping.getUserId(), roleMapping.getSource(), roleMapping.getRoles(),
            criteria)) {
          try {
            User user = getSecuritySystem().getUser(roleMapping.getUserId(), roleMapping.getSource());
            users.add(user);
          }
          catch (UserNotFoundException e) {
            log.debug("User: '" + roleMapping.getUserId() + "' of source: '"
                + roleMapping.getSource() + "' could not be found.", e);
          }
          catch (NoSuchUserManagerException e) {
            log.warn("User: '" + roleMapping.getUserId() + "' of source: '"
                + roleMapping.getSource() + "' could not be found.", e);
          }

        }
      }
    }

    return users;
  }

  private SecuritySystem getSecuritySystem() {
    return this.securitySystem;
  }

  private String hashPassword(String clearPassword) {
    // set the password if its not null
    if (clearPassword != null && clearPassword.trim().length() > 0) {
      return this.passwordService.encryptPassword(clearPassword);
    }

    return clearPassword;
  }

  public void setUsersRoles(final String userId, final String userSource, final Set<RoleIdentifier> roleIdentifiers)
      throws UserNotFoundException, InvalidConfigurationException
  {
    // delete if no roleIdentifiers
    if (roleIdentifiers == null || roleIdentifiers.isEmpty()) {
      try {
        configuration.deleteUserRoleMapping(userId, userSource);
      }
      catch (NoSuchRoleMappingException e) {
        log.debug("User role mapping for user: " + userId + " source: " + userSource
            + " could not be deleted because it does not exist.");
      }
    }
    else {
      CUserRoleMapping roleMapping = new CUserRoleMapping();
      roleMapping.setUserId(userId);
      roleMapping.setSource(userSource);

      for (RoleIdentifier roleIdentifier : roleIdentifiers) {
        // make sure we only save roles that we manage
        // TODO: although we shouldn't need to worry about this.
        if (getSource().equals(roleIdentifier.getSource())) {
          roleMapping.addRole(roleIdentifier.getRoleId());
        }
      }

      // try to update first
      try {
        configuration.updateUserRoleMapping(roleMapping);
      }
      catch (NoSuchRoleMappingException e) {
        // update failed try create
        log.debug("Update of user role mapping for user: " + userId + " source: " + userSource
            + " did not exist, creating new one.");
        configuration.createUserRoleMapping(roleMapping);
      }
    }
  }

  public String getAuthenticationRealmName() {
    return "NexusAuthenticatingRealm";
  }

  private Set<String> getRoleIdsFromUser(User user) {
    Set<String> roles = new HashSet<String>();
    for (RoleIdentifier roleIdentifier : user.getRoles()) {
      // TODO: should we just grab the Default roles?
      // these users are managed by this realm so they should ONLY have roles from it anyway.
      roles.add(roleIdentifier.getRoleId());
    }
    return roles;
  }
}
