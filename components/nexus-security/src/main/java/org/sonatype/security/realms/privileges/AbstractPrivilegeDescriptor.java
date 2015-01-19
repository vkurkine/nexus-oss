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
package org.sonatype.security.realms.privileges;

import java.util.List;

import javax.inject.Inject;

import org.sonatype.configuration.validation.ValidationMessage;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.realms.validator.ConfigurationIdGenerator;
import org.sonatype.security.realms.validator.SecurityValidationContext;

import org.codehaus.plexus.util.StringUtils;

public abstract class AbstractPrivilegeDescriptor
    implements PrivilegeDescriptor
{
  private ConfigurationIdGenerator idGenerator;

  @Inject
  public void installDependencies(final ConfigurationIdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }

  public ValidationResponse validatePrivilege(CPrivilege privilege, SecurityValidationContext ctx, boolean update) {
    ValidationResponse response = new ValidationResponse();

    if (ctx != null) {
      response.setContext(ctx);
    }

    SecurityValidationContext context = (SecurityValidationContext) response.getContext();
    List<String> existingIds = context.getExistingPrivilegeIds();

    if (existingIds == null) {
      context.addExistingPrivilegeIds();
      existingIds = context.getExistingPrivilegeIds();
    }

    if (!update && (StringUtils.isEmpty(privilege.getId()) || "0".equals(privilege.getId()) || (existingIds.contains(privilege.getId())))) {
      String newId = idGenerator.generateId();

      ValidationMessage message = new ValidationMessage("id",
          "Fixed wrong privilege ID from '" + privilege.getId() + "' to '" + newId + "'");
      response.addValidationWarning(message);
      privilege.setId(newId);
      response.setModified(true);
    }

    if (StringUtils.isEmpty(privilege.getType())) {
      ValidationMessage message = new ValidationMessage("type",
          "Cannot have an empty type", "Privilege cannot have an invalid type");

      response.addValidationError(message);
    }

    if (StringUtils.isEmpty(privilege.getName())) {
      ValidationMessage message = new ValidationMessage("name",
          "Privilege ID '" + privilege.getId() + "' requires a name.", "Name is required.");
      response.addValidationError(message);
    }

    return response;
  }
}
