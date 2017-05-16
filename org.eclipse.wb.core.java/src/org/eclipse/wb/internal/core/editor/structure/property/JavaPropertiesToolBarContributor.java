/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.editor.structure.property;

import org.eclipse.wb.core.editor.IDesignPageSite;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.Messages;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;

import java.util.List;

/**
 * {@link IPropertiesToolBarContributor} for Java.
 *
 * @author scheglov_ke
 * @coverage core.editor.structure
 */
public final class JavaPropertiesToolBarContributor implements IPropertiesToolBarContributor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IPropertiesToolBarContributor INSTANCE =
      new JavaPropertiesToolBarContributor();

  private JavaPropertiesToolBarContributor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IPropertiesToolBarContributor
  //
  ////////////////////////////////////////////////////////////////////////////
  public void contributeToolBar(IToolBarManager manager, final List<ObjectInfo> objects)
      throws Exception {
    addGotoDefinitionAction(manager, objects);
    addVariableConvertAction(manager, objects);
  }

  private void addGotoDefinitionAction(IToolBarManager manager, List<ObjectInfo> objects) {
    if (objects.size() == 1 && objects.get(0) instanceof JavaInfo) {
      final JavaInfo javaInfo = (JavaInfo) objects.get(0);
      IAction gotoDefinitionAction = new Action() {
        @Override
        public void run() {
          int position = javaInfo.getCreationSupport().getNode().getStartPosition();
          IDesignPageSite site = IDesignPageSite.Helper.getSite(javaInfo);
          site.openSourcePosition(position);
        }
      };
      gotoDefinitionAction.setImageDescriptor(DesignerPlugin.getImageDescriptor("structure/goto_definition.gif"));
      gotoDefinitionAction.setToolTipText(Messages.ComponentsPropertiesPage_goDefinition);
      manager.appendToGroup(GROUP_EDIT, gotoDefinitionAction);
    }
  }

  private void addVariableConvertAction(IToolBarManager manager, List<ObjectInfo> objects) {
    if (objects.size() == 1 && objects.get(0) instanceof JavaInfo) {
      final JavaInfo javaInfo = (JavaInfo) objects.get(0);
      final VariableSupport variableSupport = javaInfo.getVariableSupport();
      // prepare action
      IAction variableConvertAction = new Action() {
        @Override
        public void run() {
          ExecutionUtils.run(javaInfo, new RunnableEx() {
            public void run() throws Exception {
              if (variableSupport.canConvertLocalToField()) {
                variableSupport.convertLocalToField();
              } else if (variableSupport.canConvertFieldToLocal()) {
                variableSupport.convertFieldToLocal();
              }
            }
          });
        }
      };
      boolean enabled = false;
      // to field
      if (variableSupport.canConvertLocalToField()) {
        variableConvertAction.setImageDescriptor(DesignerPlugin.getImageDescriptor("structure/local_to_field.gif"));
        variableConvertAction.setToolTipText(Messages.ComponentsPropertiesPage_convertLocalToFieldAction);
        enabled = true;
      }
      // to local
      if (!enabled && variableSupport.canConvertFieldToLocal()) {
        variableConvertAction.setImageDescriptor(DesignerPlugin.getImageDescriptor("structure/field_to_local.gif"));
        variableConvertAction.setToolTipText(Messages.ComponentsPropertiesPage_convertFieldToLocalAction);
        enabled = true;
      }
      // append action
      if (enabled) {
        manager.appendToGroup(GROUP_EDIT, variableConvertAction);
      }
    }
  }
}
