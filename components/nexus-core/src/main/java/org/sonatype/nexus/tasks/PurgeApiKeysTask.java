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
package org.sonatype.nexus.tasks;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.scheduling.TaskSupport;
import org.sonatype.security.events.AuthorizationConfigurationChanged;
import org.sonatype.sisu.goodies.eventbus.EventBus;

@Named
public class PurgeApiKeysTask
    extends TaskSupport<Void>
{
  private EventBus eventBus;

  @Inject
  public void setEventBus(final EventBus eventBus) {
    this.eventBus = eventBus;
  }

  @Override
  protected Void execute() {
    // triggers the expiry of any orphaned cached user principals
    eventBus.post(new AuthorizationConfigurationChanged());
    return null;
  }

  @Override
  public String getMessage() {
    return "Purging Orphaned API Keys.";
  }

}
