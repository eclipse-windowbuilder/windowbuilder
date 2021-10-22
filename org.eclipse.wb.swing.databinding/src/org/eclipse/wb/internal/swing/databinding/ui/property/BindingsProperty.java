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
package org.eclipse.wb.internal.swing.databinding.ui.property;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveDecoration;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo.ChildrenContext;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.databinding.ui.decorate.IObserveDecorator;
import org.eclipse.wb.internal.core.databinding.ui.property.AbstractBindingsProperty;
import org.eclipse.wb.internal.core.databinding.ui.property.BindingAction;
import org.eclipse.wb.internal.core.databinding.ui.property.Context;
import org.eclipse.wb.internal.core.model.nonvisual.NonVisualBeanContainerInfo;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.swing.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.swing.databinding.model.ObserveCreationType;
import org.eclipse.wb.internal.swing.databinding.model.ObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.beans.BeansObserveTypeContainer;
import org.eclipse.wb.internal.swing.databinding.model.components.ComponentsObserveTypeContainer;
import org.eclipse.wb.internal.swing.databinding.model.components.JavaInfoReferenceProvider;
import org.eclipse.wb.internal.swing.databinding.ui.providers.BindingLabelProvider;

import org.eclipse.jface.action.IMenuManager;

import java.util.Iterator;
import java.util.List;

/**
 * Complex property for all info bindings.
 *
 * @author lobas_av
 * @coverage bindings.swing.ui.properties
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
      ComponentsObserveTypeContainer container =
          (ComponentsObserveTypeContainer) provider.getContainer(ObserveType.WIDGETS);
      m_context.observeObject = container.resolve(m_context.javaInfo());
    }
    if (m_context.observeObject == null) {
      return new Property[0];
    }
    //
    List<IObserveInfo> observes =
        Lists.newArrayList(m_context.observeObject.getChildren(ChildrenContext.ChildrenForPropertiesTable));
    for (Iterator<IObserveInfo> I = observes.iterator(); I.hasNext();) {
      if (!includeProperty(I.next())) {
        I.remove();
      }
    }
    //
    Property[] properties = new Property[observes.size()];
    for (int i = 0; i < properties.length; i++) {
      ObserveInfo observeProperty = (ObserveInfo) observes.get(i);
      if (observeProperty.getCreationType() == ObserveCreationType.SelfProperty) {
        switch (ComponentsObserveTypeContainer.getCreationType(m_context.javaInfo().getDescription().getComponentClass())) {
          case JListBinding :
            properties[i] = new JListSelfObserveProperty(m_context, observeProperty);
            continue;
          case JTableBinding :
            properties[i] = new JTableSelfObserveProperty(m_context, observeProperty);
            continue;
          case JComboBoxBinding :
            properties[i] = new JComboBoxSelfObserveProperty(m_context, observeProperty);
            continue;
        }
      }
      properties[i] = new ObserveProperty(m_context, observeProperty);
    }
    return properties;
  }

  private static boolean includeProperty(IObserveInfo observe) {
    IObserveDecoration decoration = (IObserveDecoration) observe;
    IObserveDecorator decorator = decoration.getDecorator();
    return decorator == IObserveDecorator.BOLD || decorator == IObserveDecorator.DEFAULT;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Menu
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean checkEquals(IObserveInfo iobserve) throws Exception {
    String reference = JavaInfoReferenceProvider.getReference(m_context.javaInfo());
    ObserveInfo observe = (ObserveInfo) iobserve;
    return reference.equals(observe.getReference());
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