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
package org.eclipse.wb.internal.rcp.databinding.ui.property;

import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo.ChildrenContext;
import org.eclipse.wb.internal.core.databinding.model.SynchronizeManager;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.databinding.ui.property.AbstractBindingsProperty;
import org.eclipse.wb.internal.core.databinding.ui.property.BindingAction;
import org.eclipse.wb.internal.core.databinding.ui.property.Context;
import org.eclipse.wb.internal.core.model.nonvisual.NonVisualBeanContainerInfo;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.BeansObserveTypeContainer;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.WidgetsObserveTypeContainer;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.JavaInfoReferenceProvider;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.ui.providers.BindingLabelProvider;

import org.eclipse.jface.action.IMenuManager;

import java.util.List;

/**
 * Complex property for all info bindings.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.ui.properties
 */
public class BindingsProperty extends AbstractBindingsProperty {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BindingsProperty(Context context) {
    super(context);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractBindingsProperty
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Property[] createProperties() throws Exception {
    DatabindingsProvider provider = (DatabindingsProvider) m_context.provider;
    if (m_context.objectInfo.getParent() instanceof NonVisualBeanContainerInfo) {
      BeansObserveTypeContainer container =
          (BeansObserveTypeContainer) provider.getContainer(ObserveType.BEANS);
      m_context.observeObject = container.resolve(m_context.javaInfo());
    } else {
      WidgetsObserveTypeContainer container =
          (WidgetsObserveTypeContainer) provider.getContainer(ObserveType.WIDGETS);
      m_context.observeObject = container.resolve(m_context.javaInfo());
    }
    Assert.isNotNull(m_context.observeObject, SynchronizeManager.class.getName()
        + " isn't work ("
        + m_context.objectInfo
        + ")");
    List<IObserveInfo> observes =
        m_context.observeObject.getChildren(ChildrenContext.ChildrenForPropertiesTable);
    //
    Property[] properties = new Property[observes.size()];
    for (int i = 0; i < properties.length; i++) {
      IObserveInfo observeProperty = observes.get(i);
      if ("input".equals(observeProperty.getPresentation().getText())) {
        properties[i] = new InputObserveProperty(m_context, observeProperty);
      } else {
        properties[i] = new ObserveProperty(m_context, observeProperty);
      }
    }
    return properties;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Menu
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean checkEquals(IObserveInfo observe) throws Exception {
    if (observe instanceof WidgetBindableInfo) {
      WidgetBindableInfo bindable = (WidgetBindableInfo) observe;
      if (m_context.objectInfo == bindable.getJavaInfo()) {
        return true;
      }
    }
    //
    String reference = JavaInfoReferenceProvider.getReference(m_context.javaInfo());
    BindableInfo bindable = (BindableInfo) observe;
    return reference.equals(bindable.getReference());
  }

  @Override
  protected void addBindingAction(IMenuManager menu,
      IBindingInfo binding,
      IObserveInfo observeProperty,
      boolean isTarget) throws Exception {
    BindingAction action = new BindingAction(m_context, binding);
    action.setText(observeProperty.getPresentation().getText()
        + ": "
        + BindingLabelProvider.INSTANCE.getColumnText(binding, isTarget ? 2 : 1));
    action.setIcon(BindingLabelProvider.INSTANCE.getColumnImage(binding, 0));
    menu.add(action);
  }
}