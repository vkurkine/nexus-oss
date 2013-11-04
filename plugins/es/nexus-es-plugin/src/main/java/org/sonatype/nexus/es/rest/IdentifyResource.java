/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.es.rest;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.sonatype.nexus.es.ESPlugin;
import org.sonatype.nexus.es.searcher.Searcher;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.siesta.common.Resource;

import org.apache.shiro.authz.annotation.RequiresPermissions;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

/**
 * Capabilities REST resource.
 *
 * @since 2.7.0
 */
@Named
@Singleton
@Path(IdentifyResource.RESOURCE_URI)
public class IdentifyResource
    extends ComponentSupport
    implements Resource
{

  public static final String RESOURCE_URI = ESPlugin.REST_PREFIX + "/identify";

  private final Searcher searcher;

  @Inject
  public IdentifyResource(final Searcher searcher) {
    this.searcher = checkNotNull(searcher);
  }

  @GET
  @Path("/{sha1}")
  @Produces({APPLICATION_XML, APPLICATION_JSON})
  @RequiresPermissions(ESPlugin.PERMISSION_PREFIX + "read") // TODO
  public Object get(final @PathParam("sha1") String sha1)
  {
    return searcher.searchBySha1(sha1);
  }
}
