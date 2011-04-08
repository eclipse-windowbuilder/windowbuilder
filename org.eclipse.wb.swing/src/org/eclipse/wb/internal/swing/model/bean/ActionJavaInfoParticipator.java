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
package org.eclipse.wb.internal.swing.model.bean;

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.model.IJavaInfoInitializationParticipator;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.ui.ImageImageDescriptor;
import org.eclipse.wb.internal.core.utils.ui.MenuManagerEx;
import org.eclipse.wb.internal.swing.Activator;
import org.eclipse.wb.internal.swing.ToolkitProvider;
import org.eclipse.wb.internal.swing.model.ModelMessages;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.graphics.Image;

import java.util.List;

import javax.swing.AbstractButton;

/**
 * Implementation of {@link IJavaInfoInitializationParticipator} that contributes {@link ActionInfo}
 * actions into context menu.
 * 
 * @author scheglov_ke
 * @coverage swing.model
 */
public final class ActionJavaInfoParticipator implements IJavaInfoInitializationParticipator {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IJavaInfoInitializationParticipator
  //
  ////////////////////////////////////////////////////////////////////////////
  public void process(final JavaInfo javaInfo) throws Exception {
    if (javaInfo.getDescription().getToolkit() == ToolkitProvider.DESCRIPTION) {
      javaInfo.addBroadcastListener(new ObjectEventListener() {
        @Override
        public void addContextMenu(List<? extends ObjectInfo> objects,
            ObjectInfo object,
            IMenuManager manager) throws Exception {
          if (objects != null) {
            JavaInfo root = javaInfo.getRootJava();
            if (javaInfo == root) {
              contextMenu_contribute(root, objects, object, manager);
            }
          }
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Contributes {@link IAction}'s into context menu.
   */
  private void contextMenu_contribute(JavaInfo root,
      List<? extends ObjectInfo> objects,
      ObjectInfo object,
      IMenuManager manager) throws Exception {
    boolean isFirst = objects.indexOf(object) == 0;
    // prepare button
    ComponentInfo button = contextMenu_getButton(object);
    if (button == null) {
      return;
    }
    // append sub-menu with Actions's
    MenuManagerEx groupsManager =
        new MenuManagerEx(ModelMessages.ActionJavaInfoParticipator_setActionManager);
    groupsManager.setImage(Activator.getImage("info/Action/action.gif"));
    manager.appendToGroup(IContextMenuConstants.GROUP_ADDITIONAL, groupsManager);
    // append actions
    contextMenu_noAction(groupsManager, root, objects, isFirst);
    groupsManager.add(new Separator());
    contextMenu_existingAction(groupsManager, root, objects, isFirst);
    groupsManager.add(new Separator());
    contextMenu_newAction(groupsManager, root, objects, isFirst);
  }

  /**
   * Adds {@link IAction}'s for removing {@link ActionInfo} for all selected {@link AbstractButton}
   * 's.
   */
  private void contextMenu_noAction(MenuManagerEx groupsManager,
      JavaInfo root,
      final List<? extends ObjectInfo> objects,
      boolean isFirst) {
    RunnableEx runnable = new RunnableEx() {
      public void run() throws Exception {
        for (ObjectInfo object : objects) {
          ComponentInfo button = (ComponentInfo) object;
          button.removeMethodInvocations("setAction(javax.swing.Action)");
        }
      }
    };
    IAction action =
        contextMenu_createAction(
            root,
            isFirst,
            ModelMessages.ActionJavaInfoParticipator_none,
            IAction.AS_PUSH_BUTTON,
            runnable);
    groupsManager.add(action);
  }

  /**
   * Adds {@link IAction}'s for setting for all selected {@link AbstractButton}'s newly created
   * {@link ActionInfo}.
   */
  private void contextMenu_newAction(MenuManagerEx groupsManager,
      final JavaInfo root,
      final List<? extends ObjectInfo> objects,
      boolean isFirst) {
    RunnableEx runnable = new RunnableEx() {
      public void run() throws Exception {
        ActionInfo action = ActionInfo.createInner(root.getEditor());
        for (ObjectInfo object : objects) {
          ComponentInfo button = (ComponentInfo) object;
          ActionInfo.setAction(button, action);
        }
      }
    };
    IAction action =
        contextMenu_createAction(
            root,
            isFirst,
            ModelMessages.ActionJavaInfoParticipator_new,
            IAction.AS_PUSH_BUTTON,
            runnable);
    groupsManager.add(action);
  }

  /**
   * Adds {@link IAction}'s for setting for all selected {@link AbstractButton}'s one of the
   * existing {@link ActionInfo}'s.
   */
  private void contextMenu_existingAction(MenuManagerEx groupsManager,
      JavaInfo root,
      final List<? extends ObjectInfo> objects,
      boolean isFirst) throws Exception {
    for (final ActionInfo actionInfo : ActionContainerInfo.getActions(root)) {
      String text = actionInfo.getPresentation().getText();
      Image image = actionInfo.getPresentation().getIcon();
      // add action
      RunnableEx runnable = new RunnableEx() {
        public void run() throws Exception {
          for (ObjectInfo object : objects) {
            ComponentInfo button = (ComponentInfo) object;
            ActionInfo.setAction(button, actionInfo);
          }
        }
      };
      IAction action =
          contextMenu_createAction(root, isFirst, text, IAction.AS_RADIO_BUTTON, runnable);
      action.setImageDescriptor(new ImageImageDescriptor(image));
      groupsManager.add(action);
      // update check state
      {
        boolean hasAction = true;
        for (ObjectInfo object : objects) {
          ComponentInfo button = (ComponentInfo) object;
          hasAction &= ((AbstractButton) button.getObject()).getAction() == actionInfo.getObject();
        }
        action.setChecked(hasAction);
      }
    }
  }

  /**
   * @return the action with given text/style that runs {@link RunnableEx} (if
   *         <code>isFirst == true</code>), or just empty {@link IAction} with given text/style that
   *         does nothing.
   */
  private IAction contextMenu_createAction(JavaInfo root,
      boolean isFirst,
      String text,
      int style,
      final RunnableEx runnable) {
    if (isFirst) {
      return new ObjectInfoAction(root, text, style) {
        @Override
        protected void runEx() throws Exception {
          runnable.run();
        }
      };
    } else {
      return new Action(text, style) {
      };
    }
  }

  /**
   * @return the {@link ComponentInfo} of button, or <code>null</code>.
   */
  private static ComponentInfo contextMenu_getButton(ObjectInfo object) {
    if (object instanceof ComponentInfo) {
      ComponentInfo component = (ComponentInfo) object;
      if (AbstractButton.class.isAssignableFrom(component.getDescription().getComponentClass())) {
        return component;
      }
    }
    return null;
  }
}
