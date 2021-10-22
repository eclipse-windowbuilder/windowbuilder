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

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.reference.IReferenceProvider;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.swing.databinding.model.ObserveCreationType;
import org.eclipse.wb.internal.swing.databinding.model.ObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.generic.IGenericType;

import java.util.Collections;
import java.util.List;

/**
 * Model for any <code>Java Beans</code> objects.
 *
 * @author lobas_av
 * @coverage bindings.swing.model.beans
 */
public abstract class BeanObserveInfo extends ObserveInfo {
  private final BeanSupport m_beanSupport;
  private final ObserveInfo m_parent;
  private List<ObserveInfo> m_properties;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BeanObserveInfo(BeanSupport beanSupport,
      ObserveInfo parent,
      IGenericType objectType,
      IReferenceProvider referenceProvider) {
    super(objectType, referenceProvider);
    m_beanSupport = beanSupport;
    m_parent = parent;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Type
  //
  ////////////////////////////////////////////////////////////////////////////
  public ObserveType getType() {
    return ObserveType.BEANS;
  }

  @Override
  public ObserveCreationType getCreationType() {
    return ObserveCreationType.AutoBinding;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Hierarchy
  //
  ////////////////////////////////////////////////////////////////////////////
  public final IObserveInfo getParent() {
    return m_parent;
  }

  public final List<IObserveInfo> getChildren(ChildrenContext context) {
    if (context == ChildrenContext.ChildrenForPropertiesTable) {
      if (m_properties == null) {
        m_properties = m_beanSupport.createProperties(this, getObjectType());
      }
      return CoreUtils.cast(m_properties);
    }
    return Collections.emptyList();
  }

  protected final void setProperties(List<ObserveInfo> properties) {
    m_properties = properties;
  }
}