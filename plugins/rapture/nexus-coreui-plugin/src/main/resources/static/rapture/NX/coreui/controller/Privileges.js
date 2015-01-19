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
 * Privilege controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.Privileges', {
  extend: 'NX.controller.Drilldown',
  requires: [
    'NX.Conditions',
    'NX.Messages',
    'NX.Permissions',
    'NX.I18n'
  ],

  masters: 'nx-coreui-privilege-list',

  stores: [
    'Privilege'
  ],
  views: [
    'privilege.PrivilegeFeature',
    'privilege.PrivilegeList',
    'privilege.PrivilegeAddRepositoryTarget',
    'privilege.PrivilegeTrace'
  ],
  refs: [
    { ref: 'feature', selector: 'nx-coreui-privilege-feature' },
    { ref: 'list', selector: 'nx-coreui-privilege-list' },
    { ref: 'info', selector: 'nx-coreui-privilege-feature nx-info-panel' }
  ],
  icons: {
    'privilege-default': {
      file: 'medal_gold_1.png',
      variants: ['x16', 'x32']
    },
    'privilege-method': {
      file: 'medal_gold_1.png',
      variants: ['x16', 'x32']
    },
    'privilege-repository': {
      file: 'database.png',
      variants: ['x16', 'x32']
    },
    'privilege-target': {
      file: 'target.png',
      variants: ['x16', 'x32']
    }
  },
  features: {
    mode: 'admin',
    path: '/Security/Privileges',
    text: NX.I18n.get('ADMIN_PRIVILEGES_TITLE'),
    description: NX.I18n.get('ADMIN_PRIVILEGES_SUBTITLE'),
    view: { xtype: 'nx-coreui-privilege-feature' },
    iconConfig: {
      file: 'medal_gold_1.png',
      variants: ['x16', 'x32']
    },
    visible: function () {
      return NX.Permissions.check('security:privileges', 'read');
    },
    weight: 10
  },
  permission: 'security:privileges',

  /**
   * @override
   */
  init: function () {
    var me = this;

    me.callParent();

    me.listen({
      component: {
        'nx-coreui-privilege-list menuitem[action=newrepositorytarget]': {
          click: me.showAddWindowRepositoryTarget
        },
        'nx-coreui-privilege-add-repositorytarget form': {
          submitted: me.onSettingsSubmitted
        }
        // FIXME: What does this do?
        //'nx-coreui-privilege-add-repositorytarget #repositoryId': {
        //  change: me.filterRepositoryTargets
        //}
      }
    });
  },

  /**
   * @override
   */
  getDescription: function (model) {
    return model.get('name');
  },

  onSelection: function (list, model) {
    var me = this,
        info = {};

    if (Ext.isDefined(model)) {
      me.getFeature().setItemClass(1, NX.Icons.cls('privilege-' + model.get('type'), 'x16'));

      info[NX.I18n.get('ADMIN_PRIVILEGES_SUMMARY_ID')] = model.getId();
      info[NX.I18n.get('ADMIN_PRIVILEGES_SUMMARY_NAME')] = model.get('name');
      info[NX.I18n.get('ADMIN_PRIVILEGES_SUMMARY_DESCRIPTION')] = model.get('description');
      info[NX.I18n.get('ADMIN_PRIVILEGES_SUMMARY_PERMISSION')] = model.get('permission');

      Ext.iterate(model.get('properties'), function(key, value) {
        info[NX.I18n.format('ADMIN_PRIVILEGES_SUMMARY_PROPERTY', key)] = value;
      });

      me.getInfo().showInfo(info);
    }
  },

  /**
   * @private
   */
  showAddWindowRepositoryTarget: function () {
    var me = this,
      feature = me.getFeature();

    // Show the first panel in the create wizard, and set the breadcrumb
    feature.setItemName(1, NX.I18n.get('ADMIN_PRIVILEGES_CREATE_TITLE'));
    me.loadCreateWizard(1, true, null);
  },

  // FIXME: What does this do?
  ///**
  //* @private
  //*/
  //filterRepositoryTargets: function (repositoryIdCombo) {
  //  var targetCombo = repositoryIdCombo.up('form').down('#repositoryTargetId'),
  //      repositoryId = repositoryIdCombo.getValue(),
  //      format;
  //
  //  targetCombo.setValue(undefined);
  //  if (repositoryId) {
  //    format = repositoryIdCombo.getStore().getById(repositoryId).get('format');
  //    targetCombo.getStore().filterByFormat(format);
  //  }
  //  else {
  //    targetCombo.getStore().removeFilterByFormat();
  //  }
  //},

  /**
   * @protected
   * Enable 'Delete' when user has 'delete' permission and privilege is not read only.
   */
  bindDeleteButton: function (button) {
    var me = this;

    button.mon(
        NX.Conditions.and(
            NX.Conditions.isPermitted(me.permission, 'delete'),
            NX.Conditions.gridHasSelection(me.masters[0], function (model) {
              return !model.get('readOnly');
            })
        ),
        {
          satisfied: button.enable,
          unsatisfied: button.disable,
          scope: button
        }
    );
  },

  /**
   * @private
   */
  onSettingsSubmitted: function (form, action) {
    var me = this,
        win = form.up('nx-coreui-privilege-add-repositorytarget');

    if (win) {
      me.loadStoreAndSelect(action.result.data[0].id, false);
      Ext.Array.each(action.result.data, function (privilege) {
        NX.Messages.add({
          text: NX.I18n.format('ADMIN_PRIVILEGES_MESSAGE_CREATED', privilege.name),
          type: 'success'
        });
      });
    } else {
      me.loadStore(Ext.emptyFn);
    }
  },

  /**
   * @private
   * @override
   * Deletes a privilege.
   * @param model privilege to be deleted
   */
  deleteModel: function (model) {
    var me = this,
        name = model.get('name');

    NX.direct.coreui_Privilege.remove(model.getId(), function (response) {
      me.loadStore();
      if (Ext.isObject(response) && response.success) {
        NX.Messages.add({
          text: NX.I18n.format('ADMIN_PRIVILEGES_MESSAGE_DELETED', name),
          type: 'success'
        });
      }
    });
  }

});
