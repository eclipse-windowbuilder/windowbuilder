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
package org.eclipse.wb.internal.swing.databinding.wizards.autobindings;

import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.PropertyAdapter;
import org.eclipse.wb.internal.core.databinding.wizards.autobindings.AbstractDescriptor;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Swing component descriptor.
 *
 * @author lobas_av
 * @coverage bindings.swing.wizard.auto
 */
public final class SwingComponentDescriptor extends AbstractDescriptor {
  private String m_componentClassName;
  private String[] m_propertyClasses;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getComponentClass() {
    return m_componentClassName;
  }

  public void setComponentClass(String className) {
    m_componentClassName = className;
  }

  public String getPropertyClass() {
    return m_propertyClasses[m_propertyClasses.length - 1];
  }

  public void setPropertyClass(String classes) {
    m_propertyClasses = StringUtils.split(classes);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Default
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isDefault(Object property) {
    PropertyAdapter propertyAdapter = (PropertyAdapter) property;
    Class<?> propertyType = propertyAdapter.getType();
    if (propertyType != null) {
      return ArrayUtils.contains(m_propertyClasses, propertyType.getName());
    }
    return false;
  }
}