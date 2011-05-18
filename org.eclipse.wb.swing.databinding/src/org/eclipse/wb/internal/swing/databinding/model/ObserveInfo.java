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
package org.eclipse.wb.internal.swing.databinding.model;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.presentation.IObservePresentationDecorator;
import org.eclipse.wb.internal.core.databinding.model.reference.IReferenceProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.swing.databinding.model.bindings.BindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.generic.IGenericType;
import org.eclipse.wb.internal.swing.databinding.model.properties.PropertyInfo;

import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * Abstract model for any object and property that may be use for binding.
 * 
 * @author lobas_av
 * @coverage bindings.swing.model
 */
public abstract class ObserveInfo implements IObserveInfo {
  private IGenericType m_objectType;
  private final IReferenceProvider m_referenceProvider;
  private int m_bindingDecorationCorner;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ObserveInfo(IGenericType objectType, IReferenceProvider referenceProvider) {
    m_objectType = objectType;
    m_referenceProvider = referenceProvider;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link ObserveCreationType} for this observe object.
   */
  public abstract ObserveCreationType getCreationType();

  /**
   * @return {@link IGenericType} for this observe object.
   */
  public final IGenericType getObjectType() {
    return m_objectType;
  }

  protected final void setObjectType(IGenericType objectType) {
    m_objectType = objectType;
  }

  /**
   * @return {@link Class} type of observe object or property.
   */
  public final Class<?> getObjectClass() {
    return m_objectType.getRawType();
  }

  /**
   * @return {@link IReferenceProvider} reference provider on observe object or property.
   */
  public final IReferenceProvider getReferenceProvider() {
    return m_referenceProvider;
  }

  /**
   * @return the reference on observe object or property.
   */
  public final String getReference() throws Exception {
    return m_referenceProvider.getReference();
  }

  public boolean isRepresentedBy(String reference) throws Exception {
    return reference.equals(m_referenceProvider.getReference());
  }

  public boolean canShared() {
    return false;
  }

  public PropertyInfo createProperty(ObserveInfo observeObject) throws Exception {
    throw new UnsupportedOperationException();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Binding
  //
  ////////////////////////////////////////////////////////////////////////////
  private List<BindingInfo> m_bindings;

  public void createBinding(BindingInfo binding) throws Exception {
    if (m_bindings == null) {
      m_bindings = Lists.newArrayList();
    }
    m_bindings.add(binding);
    updateBindingDecoration();
  }

  public void deleteBinding(BindingInfo binding) throws Exception {
    m_bindings.remove(binding);
    if (m_bindings.isEmpty()) {
      m_bindings = null;
    }
    updateBindingDecoration();
  }

  public List<BindingInfo> getBindings() {
    if (m_bindings == null) {
      return Collections.emptyList();
    }
    return m_bindings;
  }

  protected final void setBindingDecoration(int decorationCorner) {
    m_bindingDecorationCorner = decorationCorner;
  }

  private void updateBindingDecoration() throws Exception {
    if (m_bindingDecorationCorner != 0) {
      IObservePresentation presentation = getPresentation();
      if (presentation instanceof IObservePresentationDecorator) {
        IObservePresentationDecorator presentationDecorator =
            (IObservePresentationDecorator) presentation;
        presentationDecorator.setBindingDecorator(CollectionUtils.isEmpty(m_bindings)
            ? 0
            : m_bindingDecorationCorner);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Create {@link IUiContentProvider} content providers for edit this model.
   */
  public void createContentProviders(List<IUiContentProvider> providers,
      ObserveInfo observeObject,
      PropertyInfo observeAstProperty) throws Exception {
  }
}