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
import org.eclipse.wb.internal.rcp.databinding.ui.providers.TypeImageProvider;

import org.apache.commons.lang.StringUtils;

import java.beans.PropertyDescriptor;

/**
 * Model for <code>Java Beans</code> object properties described over {@link PropertyDescriptor}.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public final class BeanPropertyDescriptorBindableInfo extends BeanPropertyBindableInfo {
  private final PropertyDescriptor m_descriptor;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BeanPropertyDescriptorBindableInfo(BeanSupport beanSupport,
      IObserveInfo parent,
      PropertyDescriptor descriptor) throws Exception {
    super(beanSupport, parent, descriptor.getPropertyType(), createReference(
        parent,
        descriptor.getName()), createPresentation(
        parent,
        descriptor.getName(),
        descriptor.getPropertyType()));
    m_descriptor = descriptor;
  }

  private static String createReference(IObserveInfo parent, String reference) throws Exception {
    if (parent instanceof BeanPropertyDescriptorBindableInfo) {
      BeanPropertyDescriptorBindableInfo bindableParent =
          (BeanPropertyDescriptorBindableInfo) parent;
      return StringUtils.removeEnd(bindableParent.getReference(), "\"") + "." + reference + "\"";
    }
    return "\"" + reference + "\"";
  }

  private static IObservePresentation createPresentation(IObserveInfo parent,
      String reference,
      Class<?> objectType) throws Exception {
    if (parent instanceof BeanPropertyDescriptorBindableInfo) {
      BeanPropertyDescriptorBindableInfo bindableParent =
          (BeanPropertyDescriptorBindableInfo) parent;
      String parentReference = StringUtils.removeStart(bindableParent.getReference(), "\"");
      parentReference = StringUtils.removeEnd(parentReference, "\"");
      //
      final String bindingReference = parentReference + "." + reference;
      return new SimpleObservePresentation(reference,
          bindingReference,
          TypeImageProvider.getImage(objectType));
    }
    return new SimpleObservePresentation(reference, TypeImageProvider.getImage(objectType));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Access to property {@link PropertyDescriptor}.
   */
  public PropertyDescriptor getDescriptor() {
    return m_descriptor;
  }
}