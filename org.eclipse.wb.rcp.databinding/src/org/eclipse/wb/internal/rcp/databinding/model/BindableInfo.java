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
package org.eclipse.wb.internal.rcp.databinding.model;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.presentation.IObservePresentationDecorator;
import org.eclipse.wb.internal.core.databinding.model.reference.IReferenceProvider;
import org.eclipse.wb.internal.core.databinding.model.reference.StringReferenceProvider;

import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * Abstract model for any object and property that may be use for binding.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.model
 */
public abstract class BindableInfo implements IObserveInfo {
  private Class<?> m_objectType;
  private IReferenceProvider m_referenceProvider;
  private int m_bindingDecorationCorner;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public BindableInfo(Class<?> objectType, String reference) {
    this(objectType, new StringReferenceProvider(reference));
  }

  public BindableInfo(Class<?> objectType, IReferenceProvider referenceProvider) {
    m_objectType = objectType;
    m_referenceProvider = referenceProvider;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link Class} type of bindable object or property.
   */
  public final Class<?> getObjectType() {
    return m_objectType;
  }

  /**
   * Sets {@link Class} type of bindable object or property.
   */
  protected final void setObjectType(Class<?> objectType) {
    m_objectType = objectType;
  }

  /**
   * @return {@link IReferenceProvider} reference provider on bindable object or property.
   */
  public final IReferenceProvider getReferenceProvider() {
    return m_referenceProvider;
  }

  /**
   * Sets {@link IReferenceProvider} reference provider on bindable object or property.
   */
  public final void setReferenceProvider(IReferenceProvider referenceProvider) {
    m_referenceProvider = referenceProvider;
  }

  /**
   * @return the reference on bindable object or property.
   */
  public final String getReference() throws Exception {
    return m_referenceProvider.getReference();
  }

  /**
   * @return {@link BindableInfo} object that represented given reference or <code>null</code>.
   */
  public final BindableInfo resolveReference(String reference) throws Exception {
    if (reference.equals(getReference())) {
      return this;
    }
    for (BindableInfo child : getChildren()) {
      BindableInfo result = child.resolveReference(reference);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  /**
   * @return {@link BindableInfo} property that association with given reference or
   *         <code>null</code>.
   */
  public BindableInfo resolvePropertyReference(String reference) throws Exception {
    throw new UnsupportedOperationException();
  }

  /**
   * @return {@link BindableInfo} collection with sub models.
   */
  protected abstract List<BindableInfo> getChildren();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link IObservableFactory} for create observable.
   */
  public IObservableFactory getObservableFactory() throws Exception {
    throw new UnsupportedOperationException();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // 
  //
  ////////////////////////////////////////////////////////////////////////////
  private List<AbstractBindingInfo> m_bindings;

  /**
   * This method is invoked as last step of create new observable.
   */
  public void createBinding(AbstractBindingInfo binding) throws Exception {
    if (m_bindings == null) {
      m_bindings = Lists.newArrayList();
    }
    m_bindings.add(binding);
    updateBindingDecoration();
  }

  public void deleteBinding(AbstractBindingInfo binding) throws Exception {
    m_bindings.remove(binding);
    if (m_bindings.isEmpty()) {
      m_bindings = null;
    }
    updateBindingDecoration();
  }

  public List<AbstractBindingInfo> getBindings() {
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
}