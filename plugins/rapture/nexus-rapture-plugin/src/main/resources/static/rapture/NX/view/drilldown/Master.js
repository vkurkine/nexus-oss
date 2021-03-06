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
/*global Ext, NX*/

/**
 * Master/Detail tabs.
 *
 * @since 3.0
 */
Ext.define('NX.view.drilldown.Master', {
  extend: 'Ext.grid.Panel',
  alias: 'widget.nx-drilldown-master',

  listeners: {
    columnschanged: 'refreshDrilldown',
    viewready: function(view) { view.refreshDrilldown(view.headerCt) }
  },

  /**
   * Put a drilldown affordance ‘>’ at the end of each item in the list
   *
   * @param ct The content header for the grid
   */
  refreshDrilldown: function(ct) {
    this.suspendEvents(false);

    // Remove existing drilldown affordances
    var columns = ct.items.items.filter(function(e) { return e.cls && e.cls == 'nx-drilldown-affordance' });
    columns.forEach(function(e) { ct.remove(e) });

    // Add a drilldown affordance to the end of the list
    ct.add(
      {
        width: 24,
        hideable: false,
        sortable: false,
        menuDisabled: true,
        resizable: false,
        cls: 'nx-drilldown-affordance',

        defaultRenderer: function() {
          return Ext.DomHelper.markup(
            {
              tag: 'span',
              html: '&#xf105;', // Right angle
              style: 'font-family: FontAwesome;'
            }
          )
        }
      }
    );

    this.resumeEvents();
  }
});
