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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer;

import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.ObservableFactoryInfo;

import java.util.List;

/**
 * Abstract model for Designer observable factory for tree viewer input.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public abstract class BeansObservableFactoryInfo extends ObservableFactoryInfo {
  protected Class<?> m_elementType;
  protected String m_propertyName;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BeansObservableFactoryInfo(String className) {
    super(className);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public final Class<?> getElementType() {
    return m_elementType;
  }

  public final void setElementType(Class<?> elementType) {
    m_elementType = elementType;
  }

  public final String getPropertyName() {
    return m_propertyName;
  }

  public void setPropertyName(String propertyName) throws Exception {
    m_propertyName = propertyName;
  }

  public boolean isDesignerMode() {
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addSourceCode(List<String> lines) throws Exception {
    lines.add(m_className
        + " "
        + getVariableIdentifier()
        + " = new "
        + m_className
        + "("
        + CoreUtils.getClassName(m_elementType)
        + ".class, "
        + CoreUtils.getDefaultString(m_propertyName, "\"", "null")
        + ");");
  }
}