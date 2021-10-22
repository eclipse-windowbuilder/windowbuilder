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
package org.eclipse.wb.internal.rcp.databinding.xwt.ui.property;

import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo.ChildrenContext;
import org.eclipse.wb.internal.core.databinding.model.SynchronizeManager;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.databinding.ui.property.AbstractBindingsProperty;
import org.eclipse.wb.internal.core.databinding.ui.property.Context;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.rcp.databinding.xwt.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.xwt.Messages;
import org.eclipse.wb.internal.rcp.databinding.xwt.model.widgets.WidgetsObserveTypeContainer;

import org.eclipse.jface.action.IMenuManager;

import java.text.MessageFormat;
import java.util.List;

/**
 *
 * @author lobas_av
 *
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
  //
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Property[] createProperties() throws Exception {
    DatabindingsProvider provider = (DatabindingsProvider) m_context.provider;
    WidgetsObserveTypeContainer container =
        (WidgetsObserveTypeContainer) provider.getContainer(ObserveType.WIDGETS);
    m_context.observeObject = container.resolve((XmlObjectInfo) m_context.objectInfo);
    //
    Assert.isNotNull(
        m_context.observeObject,
        MessageFormat.format(
            Messages.BindingsProperty_syncDoesNotWork,
            SynchronizeManager.class.getName(),
            m_context.objectInfo));
    List<IObserveInfo> observes =
        m_context.observeObject.getChildren(ChildrenContext.ChildrenForPropertiesTable);
    //
    Property[] properties = new Property[observes.size()];
    for (int i = 0; i < properties.length; i++) {
      properties[i] = new ObserveProperty(m_context, observes.get(i));
    }
    return properties;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  //
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addBindingAction(IMenuManager menu,
      IBindingInfo binding,
      IObserveInfo observeProperty,
      boolean isTarget) throws Exception {
    // TODO Auto-generated method stub
  }

  @Override
  protected boolean checkEquals(IObserveInfo observe) throws Exception {
    // TODO Auto-generated method stub
    return false;
  }
}