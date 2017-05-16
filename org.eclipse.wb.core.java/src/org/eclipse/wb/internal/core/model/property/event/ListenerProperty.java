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
package org.eclipse.wb.internal.core.model.property.event;

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.ModelMessages;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.core.utils.ui.MenuManagerEx;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;

import java.text.MessageFormat;

/**
 * Implementation of {@link Property} for single {@link ListenerInfo}.
 *
 * @author scheglov_ke
 * @coverage core.model.property.events
 */
final class ListenerProperty extends AbstractListenerProperty {
  private final ListenerInfo m_listener;
  private final String m_addSignature;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ListenerProperty(JavaInfo javaInfo, ListenerInfo listener) {
    super(javaInfo, listener.getName(), new ListenerPropertyEditor(listener));
    m_listener = listener;
    m_addSignature = listener.getMethodSignature();
    if (listener.isDeprecated()) {
      setCategory(PropertyCategory.ADVANCED);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Property
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isModified() throws Exception {
    return m_javaInfo.getMethodInvocation(m_addSignature) != null;
  }

  @Override
  public void setValue(Object value) throws Exception {
    if (value == UNKNOWN_VALUE) {
      // ask confirmation
      if (MessageDialog.openConfirm(
          DesignerPlugin.getShell(),
          ModelMessages.ListenerProperty_removeTitle,
          MessageFormat.format(ModelMessages.ListenerProperty_removeMessage, m_listener.getName()))) {
        removeListener();
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Removes listener for this event. May be not very effective.
   */
  @Override
  protected void removeListener() throws Exception {
    ListenerMethodProperty[] methodProperties = getMethodProperties();
    ListenerMethodProperty methodProperty = methodProperties[0];
    methodProperty.removeListener();
  }

  private ListenerMethodProperty[] getMethodProperties() throws Exception {
    return ((ListenerPropertyEditor) getEditor()).getProperties(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addListenerActions(IMenuManager manager, IMenuManager implementMenuManager)
      throws Exception {
    // if deprecated - don't show it
    if (m_listener.isDeprecated()) {
      return;
    }
    // add methods
    MenuManagerEx listenerMenuManager = null;
    for (ListenerMethodProperty methodProperty : getMethodProperties()) {
      // prepare listener menu manager
      if (listenerMenuManager == null) {
        ListenerInfo listenerInfo = methodProperty.getListener();
        listenerMenuManager = new MenuManagerEx(listenerInfo.getName());
        implementMenuManager.add(listenerMenuManager);
        // prepare listener menu image
        Image image;
        {
          TypeDeclaration listenerType = methodProperty.findListenerType();
          if (listenerType != null) {
            if (listenerInfo.hasAdapter()) {
              image = EventsPropertyUtils.EXISTING_CLASS_IMAGE;
            } else {
              image = EventsPropertyUtils.EXISTING_INTERFACE_IMAGE;
            }
          } else {
            if (listenerInfo.hasAdapter()) {
              image = EventsPropertyUtils.LISTENER_CLASS_IMAGE;
            } else {
              image = EventsPropertyUtils.LISTENER_INTERFACE_IMAGE;
            }
          }
        }
        listenerMenuManager.setImage(image);
      }
      // actions
      {
        // prepare actions
        IAction[] actions = createListenerMethodActions(methodProperty);
        // append existing stub action
        if (actions[0] != null) {
          manager.appendToGroup(IContextMenuConstants.GROUP_EVENTS, actions[0]);
        }
        // append existing or new method action
        listenerMenuManager.add(actions[0] != null ? actions[0] : actions[1]);
      }
    }
  }

  /**
   * For given {@link ListenerMethodProperty} creates two {@link Action}'s:
   *
   * [0] - for existing stub method, may be <code>null</code>;<br>
   * [1] - for creating new stub method.
   */
  private IAction[] createListenerMethodActions(final ListenerMethodProperty property) {
    IAction[] actions = new IAction[2];
    // try to find existing stub method
    {
      MethodDeclaration stubMethod = property.findStubMethod();
      if (stubMethod != null) {
        actions[0] = new ObjectInfoAction(m_javaInfo) {
          @Override
          protected void runEx() throws Exception {
            property.openStubMethod();
          }
        };
        int line = m_javaInfo.getEditor().getLineNumber(stubMethod.getStartPosition());
        actions[0].setText(property.getMethod().getName()
            + ModelMessages.ListenerProperty_line
            + line);
        actions[0].setImageDescriptor(EventsPropertyUtils.LISTENER_METHOD_IMAGE_DESCRIPTOR);
      }
    }
    // in any case prepare action for creating new stub method
    {
      actions[1] = new ObjectInfoAction(m_javaInfo) {
        @Override
        protected void runEx() throws Exception {
          property.openStubMethod();
        }
      };
      actions[1].setText(property.getMethod().getName());
      actions[1].setImageDescriptor(EventsPropertyUtils.LISTENER_METHOD_IMAGE_DESCRIPTOR);
    }
    //
    return actions;
  }
}
