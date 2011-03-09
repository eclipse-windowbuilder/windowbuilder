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
package org.eclipse.wb.internal.rcp.databinding.model.beans.direct;

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.reference.IReferenceProvider;
import org.eclipse.wb.internal.core.databinding.ui.decorate.IObserveDecorator;
import org.eclipse.wb.internal.rcp.databinding.model.IObservableFactory;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.PropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.ui.providers.TypeImageProvider;

import java.beans.PropertyDescriptor;
import java.util.Collections;
import java.util.List;

/**
 * {@link PropertyBindableInfo} model for properties with observable types.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public final class DirectPropertyBindableInfo extends PropertyBindableInfo {
  private final IObservableFactory m_observableFactory;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public DirectPropertyBindableInfo(BeanSupport beanSupport,
      IObserveInfo parent,
      PropertyDescriptor descriptor) {
    super(beanSupport,
        parent,
        descriptor.getReadMethod().getName() + "()",
        TypeImageProvider.OBJECT_IMAGE,
        descriptor.getPropertyType(),
        descriptor.getReadMethod().getName() + "()");
    m_observableFactory = null;
  }

  public DirectPropertyBindableInfo(BeanSupport beanSupport,
      IObserveInfo parent,
      PropertyDescriptor descriptor,
      IObservableFactory observableFactory) {
    super(beanSupport,
        parent,
        descriptor.getReadMethod().getName() + "()",
        TypeImageProvider.DIRECT_IMAGE,
        descriptor.getPropertyType(),
        descriptor.getReadMethod().getName() + "()");
    m_observableFactory = observableFactory;
  }

  public DirectPropertyBindableInfo(BeanSupport beanSupport,
      IObserveInfo parent,
      String text,
      Class<?> objectType,
      IReferenceProvider referenceProvider,
      IObservableFactory observableFactory) {
    super(beanSupport, parent, text, TypeImageProvider.DIRECT_IMAGE, objectType, referenceProvider);
    m_observableFactory = observableFactory;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Hierarchy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public List<IObserveInfo> getChildren(ChildrenContext context) {
    return Collections.emptyList();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public IObservableFactory getObservableFactory() throws Exception {
    return m_observableFactory;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  public IObserveDecorator getDecorator() {
    return IObserveDecorator.BOLD_ITALIC;
  }
}