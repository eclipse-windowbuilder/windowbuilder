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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.input;

import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;

import java.util.List;

/**
 * Model for field {@code Java Bean} object that sets as input for tree viewer.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public class BeanFieldInputObservableInfo extends ObservableInfo {
  private final BindableInfo m_object;
  private final BindableInfo m_property;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BeanFieldInputObservableInfo(BindableInfo object, BindableInfo property) {
    m_object = object;
    m_property = property;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ObservableInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public BindableInfo getBindableObject() {
    return m_object;
  }

  @Override
  public BindableInfo getBindableProperty() {
    return m_property;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Source code
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void addSourceCode(List<String> lines, CodeGenerationSupport generationSupport)
      throws Exception {
    setVariableIdentifier(m_object.getReference());
  }
}