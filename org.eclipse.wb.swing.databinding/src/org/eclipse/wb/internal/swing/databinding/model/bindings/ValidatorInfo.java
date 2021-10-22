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
package org.eclipse.wb.internal.swing.databinding.model.bindings;

import org.eclipse.wb.internal.swing.databinding.model.GenericClassObjectInfo;
import org.eclipse.wb.internal.swing.databinding.model.generic.GenericUtils;
import org.eclipse.wb.internal.swing.databinding.model.generic.IGenericType;

/**
 * Model for {@link org.jdesktop.beansbinding.Validator}.
 *
 * @author lobas_av
 * @coverage bindings.swing.model.bindings
 */
public final class ValidatorInfo extends GenericClassObjectInfo {
  private final BindingInfo m_binding;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ValidatorInfo(IGenericType objectType, BindingInfo binding) {
    super("org.jdesktop.beansbinding.Validator");
    m_binding = binding;
    setClass(objectType);
    if (isGeneric()) {
      GenericUtils.assertEquals(m_binding.getModelPropertyType(), objectType.getSubType(0));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected IGenericType[] getTypeArguments() {
    return isGeneric() ? new IGenericType[]{m_binding.getModelPropertyType()} : null;
  }
}