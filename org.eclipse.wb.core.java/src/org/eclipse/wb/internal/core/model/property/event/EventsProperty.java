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
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.JavaInfoEventOpen;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoDelete;
import org.eclipse.wb.core.model.broadcast.ObjectInfoPresentationDecorateIcon;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.ModelMessages;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.utils.ui.SwtResourceManager;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Image;

import java.util.List;

/**
 * Implementation of {@link Property} for editing <code>addXXXListener</code> events.
 *
 * @author scheglov_ke
 * @coverage core.model.property.events
 */
public final class EventsProperty extends AbstractEventProperty {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EventsProperty(JavaInfo javaInfo) {
    super(javaInfo, "Events", EventsPropertyEditor.INSTANCE);
    setCategory(PropertyCategory.HIDDEN);
    installDecoratorListener();
    installContextMenuListener();
    installDeleteListener();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Broadcasts
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Installs listener for decorating icon of component with event listener.
   */
  private void installDecoratorListener() {
    m_javaInfo.addBroadcastListener(new ObjectInfoPresentationDecorateIcon() {
      public void invoke(ObjectInfo object, Image[] icon) throws Exception {
        if (object == m_javaInfo) {
          IPreferenceStore preferences = m_javaInfo.getDescription().getToolkit().getPreferences();
          if (preferences.getBoolean(IPreferenceConstants.P_DECORATE_ICON) && isModified()) {
            Image decorator = DesignerPlugin.getImage("events/decorator.gif");
            icon[0] =
                SwtResourceManager.decorateImage(icon[0], decorator, SwtResourceManager.TOP_LEFT);
          }
        }
      }
    });
  }

  /**
   * Installs listener for adding {@link EventsProperty} menu into component context menu.
   */
  private void installContextMenuListener() {
    m_javaInfo.addBroadcastListener(new ObjectEventListener() {
      @Override
      public void addContextMenu(List<? extends ObjectInfo> objects,
          ObjectInfo object,
          IMenuManager manager) throws Exception {
        if (object == m_javaInfo) {
          contributeActions(manager, ModelMessages.EventsProperty_menuManagerName);
        }
      }
    });
  }

  /**
   * Installs listener for deleting listeners during component delete.
   */
  private void installDeleteListener() {
    m_javaInfo.addBroadcastListener(new ObjectInfoDelete() {
      @Override
      public void before(ObjectInfo parent, ObjectInfo child) throws Exception {
        if (child == m_javaInfo) {
          for (AbstractListenerProperty listenerProperty : getSubProperties()) {
            if (listenerProperty.isModified()) {
              listenerProperty.removeListener();
            }
          }
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Property
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isModified() throws Exception {
    // check if there are listeners
    Property[] properties = getSubProperties();
    for (Property property : properties) {
      if (property.isModified()) {
        return true;
      }
    }
    // not modified
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link AbstractListenerProperty}'s of this {@link EventsProperty}.
   */
  private AbstractListenerProperty[] getSubProperties() throws Exception {
    return EventsPropertyEditor.INSTANCE.getProperties(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Contributes actions into context menu.
   */
  private void contributeActions(IMenuManager manager, String implementTitle) throws Exception {
    // prepare "implement" menu
    IMenuManager implementMenuManager = new MenuManager(implementTitle);
    // add separate actions
    AbstractListenerProperty[] listenerProperties = getSubProperties();
    for (AbstractListenerProperty listenerProperty : listenerProperties) {
      listenerProperty.addListenerActions(manager, implementMenuManager);
    }
    // add "implement" menu
    manager.appendToGroup(IContextMenuConstants.GROUP_EVENTS, implementMenuManager);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Open stub
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates and/or opens in editor listener method for specified <code>methodPath</code>. Used to
   * create and open listener with known info, for example on double click.
   *
   * @param methodPath
   *          the '/' separated name of listener method, for example <code>key/pressed</code>.
   */
  public void openStubMethod(String methodPath) throws Exception {
    if (methodPath.equals("wbp:openSource")) {
      JavaInfoUtils.scheduleOpenNode(m_javaInfo, m_javaInfo.getCreationSupport().getNode());
      return;
    }
    if (methodPath.startsWith("wbp:broadcast")) {
      m_javaInfo.getBroadcast(JavaInfoEventOpen.class).invoke(m_javaInfo, methodPath);
      return;
    }
    // normal event
    String eventMethodPath = "Events/" + methodPath;
    IListenerMethodProperty property =
        (IListenerMethodProperty) PropertyUtils.getByPath(m_javaInfo, eventMethodPath);
    if (property != null) {
      property.openStubMethod();
    }
  }
}
