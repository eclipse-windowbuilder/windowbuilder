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
package org.eclipse.wb.internal.swing.databinding.model.beans;

import org.eclipse.wb.internal.core.databinding.model.reference.IReferenceProvider;
import org.eclipse.wb.internal.core.databinding.ui.decorate.IObserveDecorator;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.SeparatorUiContentProvider;
import org.eclipse.wb.internal.core.utils.ui.SwtResourceManager;
import org.eclipse.wb.internal.swing.databinding.model.ObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.generic.IGenericType;
import org.eclipse.wb.internal.swing.databinding.model.properties.BeanPropertyInfo;
import org.eclipse.wb.internal.swing.databinding.model.properties.PropertyInfo;
import org.eclipse.wb.internal.swing.databinding.ui.contentproviders.PropertiesUiContentProvider;

import org.apache.commons.lang.ArrayUtils;

import java.util.List;

/**
 * Model for <code>Java Beans</code> object properties.
 *
 * @author lobas_av
 * @coverage bindings.swing.model.beans
 */
public final class PropertiesObserveInfo extends BeanPropertyObserveInfo {
  private final String[] m_properties;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PropertiesObserveInfo(BeanSupport beanSupport,
      ObserveInfo parent,
      String text,
      IGenericType objectType,
      IReferenceProvider referenceProvider,
      IObserveDecorator decorator,
      String[] properties) throws Exception {
    super(beanSupport, parent, text, objectType, referenceProvider, decorator);
    setBindingDecoration(SwtResourceManager.TOP_LEFT);
    m_properties = properties;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canShared() {
    return false;
  }

  @Override
  public boolean isRepresentedBy(String reference) throws Exception {
    return ArrayUtils.contains(m_properties, reference);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void createContentProviders(List<IUiContentProvider> providers,
      ObserveInfo observeObject,
      PropertyInfo observeAstProperty) throws Exception {
    providers.add(new PropertiesUiContentProvider((BeanPropertyInfo) observeAstProperty,
        m_properties));
    providers.add(new SeparatorUiContentProvider());
  }
}