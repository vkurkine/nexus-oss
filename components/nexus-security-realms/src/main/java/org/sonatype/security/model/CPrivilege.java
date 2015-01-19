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
package org.sonatype.security.model;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * Base privilege class contains items common to all types of
 * privileges.
 *
 * Note: This class has been generated by modello and copied over
 */
@SuppressWarnings("all")
public class CPrivilege
    implements java.io.Serializable, java.lang.Cloneable
{

  //--------------------------/
  //- Class/Member Variables -/
  //--------------------------/

  /**
   * The ID used to reference this privilege.
   */
  private String id;

  // TODO: Remove name?
  /**
   * A descriptive name for the privilege.
   */
  private String name;

  /**
   * A description of the privilege.
   */
  private String description;

  /**
   * The type of privilege.
   */
  private String type;

  /**
   * Privilege properties.
   */
  private Map<String, String> properties;

  /**
   * Marks if this privilege is editable.
   */
  private boolean readOnly = false;

  private String version;

  //-----------/
  //- Methods -/
  //-----------/

  public void setProperty(final String key, final String value) {
    getProperties().put(key, value);
  }

  /**
   * Method clone.
   *
   * @return CPrivilege
   */
  public CPrivilege clone() {
    try {
      CPrivilege copy = (CPrivilege) super.clone();

      if (this.properties != null) {
        copy.properties = Maps.newHashMap(this.properties);
      }

      return copy;
    }
    catch (java.lang.Exception ex) {
      throw (java.lang.RuntimeException) new java.lang.UnsupportedOperationException(getClass().getName()
          + " does not support clone()").initCause(ex);
    }
  } //-- CPrivilege clone()

  /**
   * Get a description of the privilege.
   *
   * @return String
   */
  public String getDescription() {
    return this.description;
  } //-- String getDescription()

  /**
   * Get the ID used to reference this privilege.
   *
   * @return String
   */
  public String getId() {
    return this.id;
  } //-- String getId()

  /**
   * Get a descriptive name for the privilege.
   *
   * @return String
   */
  public String getName() {
    return this.name;
  } //-- String getName()

  public Map<String, String> getProperties() {
    if (this.properties == null) {
      this.properties = Maps.newHashMap();
    }
    return this.properties;
  }

  public String getProperty(final String key) {
    return getProperties().get(key);
  }

  /**
   * Get the type of privilege.
   *
   * @return String
   */
  public String getType() {
    return this.type;
  } //-- String getType()

  /**
   * Get marks if this privilege is editable.
   *
   * @return boolean
   */
  public boolean isReadOnly() {
    return this.readOnly;
  } //-- boolean isReadOnly()

  public void removeProperty(final String key) {
    getProperties().remove(key);
  }

  /**
   * Set a description of the privilege.
   */
  public void setDescription(String description) {
    this.description = description;
  } //-- void setDescription( String )

  /**
   * Set the ID used to reference this privilege.
   */
  public void setId(String id) {
    this.id = id;
  } //-- void setId( String )

  /**
   * Set a descriptive name for the privilege.
   */
  public void setName(String name) {
    this.name = name;
  } //-- void setName( String )

  /**
   * Set properties assigned to this privilege, used to define a
   * permission.
   */
  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  /**
   * Set marks if this privilege is editable.
   */
  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  } //-- void setReadOnly( boolean )

  /**
   * Set the type of privilege.
   */
  public void setType(String type) {
    this.type = type;
  } //-- void setType( String )

  public String getVersion() {
    return version;
  }

  public void setVersion(final String version) {
    this.version = version;
  }

}
