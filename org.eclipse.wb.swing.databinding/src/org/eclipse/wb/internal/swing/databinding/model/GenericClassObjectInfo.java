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

import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.swing.databinding.model.generic.IGenericType;

import org.apache.commons.lang.ArrayUtils;

/**
 * Model for objects with generic parameters.
 * 
 * @author lobas_av
 * @coverage bindings.swing.model
 */
public abstract class GenericClassObjectInfo extends SimpleClassObjectInfo {
  private boolean m_generic;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GenericClassObjectInfo(String abstractClassName) {
    super(abstractClassName, null);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  protected boolean isGeneric() {
    return m_generic;
  }

  public void setClass(IGenericType type) {
    setClassName(CoreUtils.getClassName(type.getRawType()));
    m_generic = !(type.isEmpty() && ArrayUtils.isEmpty(type.getRawType().getTypeParameters()));
  }

  /**
   * @return the array of generic type arguments.
   */
  @Override
  protected abstract IGenericType[] getTypeArguments();
}