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
package org.sonatype.nexus.templates;

import javax.inject.Inject;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;

import org.codehaus.plexus.util.StringUtils;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractTemplateProvider<T extends Template>
    implements TemplateProvider
{

  private ApplicationConfiguration applicationConfiguration;

  @Inject
  public void setApplicationConfiguration(final ApplicationConfiguration applicationConfiguration) {
    this.applicationConfiguration = checkNotNull(applicationConfiguration);
  }

  public ApplicationConfiguration getApplicationConfiguration() {
    return applicationConfiguration;
  }

  public Template getTemplateById(String id)
      throws NoSuchTemplateIdException
  {
    TemplateSet templates = getTemplates();

    for (Template template : templates) {
      if (StringUtils.equals(id, template.getId())) {
        return template;
      }
    }

    throw new NoSuchTemplateIdException("Template for Id='" + id + "' not found!");
  }

}
