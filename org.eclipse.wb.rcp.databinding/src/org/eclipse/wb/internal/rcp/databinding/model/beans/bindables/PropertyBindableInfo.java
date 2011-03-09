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
package org.eclipse.wb.internal.rcp.databinding.model.beans.bindables;

import org.eclipse.wb.internal.core.databinding.model.IObserveDecoration;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.reference.IReferenceProvider;
import org.eclipse.wb.internal.core.databinding.model.reference.StringReferenceProvider;
import org.eclipse.wb.internal.rcp.databinding.model.IObservableFactory;
import org.eclipse.wb.internal.rcp.databinding.model.SimpleObservePresentation;

import org.eclipse.swt.graphics.Image;

/**
 * Abstract model for <code>Java Beans</code> object properties.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public abstract class PropertyBindableInfo extends BeanBindableInfo implements IObserveDecoration {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public PropertyBindableInfo(BeanSupport beanSupport,
      IObserveInfo parent,
      String text,
      Class<?> objectType,
      String reference) {
    super(beanSupport,
        parent,
        objectType,
        new StringReferenceProvider(reference),
        new SimpleObservePresentation(text, objectType));
  }

  public PropertyBindableInfo(BeanSupport beanSupport,
      IObserveInfo parent,
      Class<?> objectType,
      String reference,
      IObservePresentation presentation) {
    super(beanSupport, parent, objectType, new StringReferenceProvider(reference), presentation);
  }

  public PropertyBindableInfo(BeanSupport beanSupport,
      IObserveInfo parent,
      String text,
      Image image,
      Class<?> objectType,
      String reference) {
    super(beanSupport,
        parent,
        objectType,
        new StringReferenceProvider(reference),
        new SimpleObservePresentation(text, image));
  }

  public PropertyBindableInfo(BeanSupport beanSupport,
      IObserveInfo parent,
      String text,
      Class<?> objectType,
      IReferenceProvider referenceProvider) {
    super(beanSupport, parent, objectType, referenceProvider, new SimpleObservePresentation(text,
        objectType));
  }

  public PropertyBindableInfo(BeanSupport beanSupport,
      IObserveInfo parent,
      String text,
      Image image,
      Class<?> objectType,
      IReferenceProvider referenceProvider) {
    super(beanSupport, parent, objectType, referenceProvider, new SimpleObservePresentation(text,
        image));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link IObservableFactory} for create observables with this property.
   */
  @Override
  public abstract IObservableFactory getObservableFactory() throws Exception;
}