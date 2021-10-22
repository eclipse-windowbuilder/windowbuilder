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
package org.eclipse.wb.internal.rcp.databinding.model.beans.observables;

import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.CollectionPropertyBindableInfo;

import org.apache.commons.lang.ClassUtils;

/**
 * Abstract model for collection observable objects.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public abstract class CollectionObservableInfo extends ObservableInfo {
  protected final BeanBindableInfo m_bindableObject;
  protected final CollectionPropertyBindableInfo m_bindableProperty;
  protected Class<?> m_elementType;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CollectionObservableInfo(BeanBindableInfo bindableObject,
      CollectionPropertyBindableInfo bindableProperty,
      Class<?> elementType) {
    m_bindableObject = bindableObject;
    m_bindableProperty = bindableProperty;
    m_elementType = elementType;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public final Class<?> getElementType() {
    return m_elementType;
  }

  public final void setElementType(Class<?> elementType) {
    m_elementType = elementType;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ObservableInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final BindableInfo getBindableObject() {
    return m_bindableObject;
  }

  @Override
  public final BindableInfo getBindableProperty() {
    return m_bindableProperty;
  }

  @Override
  public final boolean canShared() {
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final String getPresentationText() throws Exception {
    String elementTypeName =
        m_elementType == null ? "?????" : ClassUtils.getShortClassName(m_elementType);
    return getPresentationPrefix()
        + "("
        + getBindableObject().getPresentation().getTextForBinding()
        + ", "
        + elementTypeName
        + ".class)";
  }

  protected abstract String getPresentationPrefix();
}