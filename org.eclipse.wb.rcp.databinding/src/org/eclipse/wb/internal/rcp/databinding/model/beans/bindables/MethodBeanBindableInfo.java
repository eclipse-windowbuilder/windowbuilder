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

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.presentation.SimpleObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.reference.IReferenceProvider;
import org.eclipse.wb.internal.core.databinding.model.reference.StringReferenceProvider;
import org.eclipse.wb.internal.core.utils.ui.SwtResourceManager;
import org.eclipse.wb.internal.rcp.databinding.ui.providers.TypeImageProvider;

/**
 * Model for method based <code>Java Beans</code> objects.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public final class MethodBeanBindableInfo extends BeanBindableInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public MethodBeanBindableInfo(BeanSupport beanSupport,
      IObserveInfo parent,
      Class<?> objectType,
      String reference) throws Exception {
    this(beanSupport, parent, objectType, new StringReferenceProvider(reference), null);
  }

  public MethodBeanBindableInfo(BeanSupport beanSupport,
      IObserveInfo parent,
      Class<?> objectType,
      IReferenceProvider referenceProvider,
      IReferenceProvider presentationReferenceProvider) throws Exception {
    super(beanSupport, parent, objectType, referenceProvider, createPresentation(
        referenceProvider,
        presentationReferenceProvider));
    setBindingDecoration(SwtResourceManager.TOP_RIGHT);
  }

  private static IObservePresentation createPresentation(IReferenceProvider referenceProvider,
      IReferenceProvider presentationReferenceProvider) throws Exception {
    IReferenceProvider provider =
        presentationReferenceProvider == null ? referenceProvider : presentationReferenceProvider;
    return new SimpleObservePresentation(provider.getReference(), TypeImageProvider.METHOD_IMAGE);
  }
}