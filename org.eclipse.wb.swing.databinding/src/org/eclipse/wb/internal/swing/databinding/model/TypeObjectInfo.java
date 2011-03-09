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

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.swing.databinding.model.generic.IGenericType;

/**
 * {@link AstObjectInfo} model for objects with generic parameters.
 * 
 * @author lobas_av
 * @coverage bindings.swing.model
 */
public final class TypeObjectInfo extends AstObjectInfo {
  private final IGenericType m_objectType;
  private final String m_parameters;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TypeObjectInfo(IGenericType objectType, String parameters) {
    m_objectType = objectType;
    m_parameters = parameters;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public IGenericType getObjectType() {
    return m_objectType;
  }

  public String getParameters() {
    return m_parameters;
  }
}