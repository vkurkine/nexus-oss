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
 * Privilege grid.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.privilege.PrivilegeList', {
  extend: 'NX.view.drilldown.Master',
  alias: 'widget.nx-coreui-privilege-list',
  requires: [
    'NX.Icons',
    'NX.I18n'
  ],

  store: 'Privilege',

  columns: [
    {
      xtype: 'nx-iconcolumn',
      dataIndex: 'type',
      width: 36,
      iconVariant: 'x16',
      iconNamePrefix: 'privilege-'
    },

    // HACK: expose the privilege TYPE in the grid
    { header: 'Type', dataIndex: 'type', flex: 1 },

    // HACK: expose the privilege ID in the grid
    { header: 'ID', dataIndex: 'id', flex: 1 },

    { header: NX.I18n.get('ADMIN_PRIVILEGES_LIST_DESCRIPTION_COLUMN'), dataIndex: 'description', flex: 2 },

    // HACK: Hide these for now
    { header: NX.I18n.get('ADMIN_PRIVILEGES_LIST_NAME_COLUMN'), dataIndex: 'name', flex: 1, hidden: true },
    { header: NX.I18n.get('ADMIN_PRIVILEGES_LIST_TYPE_COLUMN'), dataIndex: 'typeName', flex: 1, hidden: true },

    // HACK: expose the real shiro permission string
    { header: 'Real Permission', dataIndex: 'realPermission', flex: 1 },

    // HACK: Hide these for now
    { header: NX.I18n.get('ADMIN_PRIVILEGES_LIST_TARGET_COLUMN'), dataIndex: 'repositoryTargetName', flex: 1, hidden: true },
    { header: NX.I18n.get('ADMIN_PRIVILEGES_LIST_REPOSITORY_COLUMN'), dataIndex: 'repositoryName', flex: 1, hidden: true },
    { header: NX.I18n.get('ADMIN_PRIVILEGES_LIST_METHOD_COLUMN'), dataIndex: 'method', flex: 1, hidden: true }
  ],

  viewConfig: {
    emptyText: NX.I18n.get('ADMIN_PRIVILEGES_LIST_EMPTY_STATE'),
    deferEmptyText: false
  },

  plugins: [
    { ptype: 'gridfilterbox', emptyText: NX.I18n.get('ADMIN_PRIVILEGES_LIST_FILTER_ERROR') }
  ],

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    me.tbar = [
      { xtype: 'button', text: NX.I18n.get('ADMIN_PRIVILEGES_LIST_NEW_BUTTON'), glyph: 'xf055@FontAwesome' /* fa-plus-circle */, action: 'new', disabled: true,
        menu: [
          { text: NX.I18n.get('ADMIN_PRIVILEGES_LIST_TARGET_ITEM'), action: 'newrepositorytarget', iconCls: NX.Icons.cls('privilege-target',
              'x16') }
        ]
      }
    ];

    me.callParent();
  }
});
