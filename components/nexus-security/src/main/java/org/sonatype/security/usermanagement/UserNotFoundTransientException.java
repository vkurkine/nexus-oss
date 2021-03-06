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

/**
 * Thrown when a user could not be found due to a temporary condition, for example when an LDAP server is unavailable.
 * Repeating the operation may succeed in the future without any intervention by the application.
 *
 * @since sonatype-security 2.8
 */
public class UserNotFoundTransientException
    extends UserNotFoundException
{
  private static final long serialVersionUID = 7565547428483146620L;

  public UserNotFoundTransientException(String userId, String message, Throwable cause) {
    super(userId, message, cause);
  }

  public UserNotFoundTransientException(String userId, String message) {
    super(userId, message);
  }

  public UserNotFoundTransientException(String userId) {
    super(userId);
  }
}
