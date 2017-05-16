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
package org.eclipse.wb.internal.core.databinding.ui.property;

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.databinding.Messages;
import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;

import java.util.List;

/**
 * Complex property for all info bindings.
 *
 * @author lobas_av
 * @coverage bindings.ui.properties
 */
public abstract class AbstractBindingsProperty extends AbstractProperty {
  private Property[] m_properties;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractBindingsProperty(Context context) {
    super(BindingsPropertyEditor.EDITOR, context);
    setCategory(PropertyCategory.system(7));
    m_context.objectInfo.addBroadcastListener(new ObjectEventListener() {
      @Override
      public void addContextMenu(List<? extends ObjectInfo> objects,
          ObjectInfo object,
          IMenuManager manager) throws Exception {
        if (m_context.objectInfo == object) {
          contributeActions(manager);
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public final Property[] getProperties() throws Exception {
    if (m_properties == null) {
      m_properties = createProperties();
    }
    return m_properties;
  }

  /**
   * @return the array with sub properties.
   */
  protected abstract Property[] createProperties() throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Property
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final String getTitle() {
    return Messages.AbstractBindingsProperty_title;
  }

  @Override
  public final boolean isModified() throws Exception {
    for (Property property : getProperties()) {
      if (property.isModified()) {
        return true;
      }
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Menu
  //
  ////////////////////////////////////////////////////////////////////////////
  private void contributeActions(IMenuManager manager) throws Exception {
    IMenuManager menu = new MenuManager(Messages.AbstractBindingsProperty_menuName);
    // fill bindings
    for (IBindingInfo binding : m_context.provider.getBindings()) {
      if (checkEquals(binding.getTarget())) {
        addBindingAction(menu, binding, binding.getTargetProperty(), true);
      } else if (checkEquals(binding.getModel())) {
        addBindingAction(menu, binding, binding.getModelProperty(), false);
      }
    }
    // separator
    menu.add(new Separator());
    // fill properties
    for (Property property : getProperties()) {
      if (property instanceof AbstractObserveProperty) {
        AbstractObserveProperty observeProperty = (AbstractObserveProperty) property;
        menu.add(new ObserveAction(m_context.objectInfo, observeProperty));
      } else if (property instanceof SingleObserveBindingProperty) {
        SingleObserveBindingProperty observeProperty = (SingleObserveBindingProperty) property;
        menu.add(new SingleObserveBindingAction(m_context.objectInfo, observeProperty));
      }
    }
    // add menu
    manager.appendToGroup(IContextMenuConstants.GROUP_LAYOUT, menu);
  }

  /**
   * @return <code>true</code> if given {@link IObserveInfo} represented property info.
   */
  protected abstract boolean checkEquals(IObserveInfo observe) throws Exception;

  /**
   * Add menu action for given binding.
   */
  protected abstract void addBindingAction(IMenuManager menu,
      IBindingInfo binding,
      IObserveInfo observeProperty,
      boolean isTarget) throws Exception;
}