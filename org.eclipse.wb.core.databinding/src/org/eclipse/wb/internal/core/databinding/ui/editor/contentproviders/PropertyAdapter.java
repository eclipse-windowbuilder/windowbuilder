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
package org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders;

import java.beans.PropertyDescriptor;

/**
 * Independent adapter over any property object. Represented as name ant type.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public class PropertyAdapter {
  protected final String m_name;
  protected final Class<?> m_type;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public PropertyAdapter(String name, Class<?> type) {
    m_name = name;
    m_type = type == null ? Object.class : type;
  }

  public PropertyAdapter(PropertyDescriptor descriptor) {
    this(descriptor.getName(), descriptor.getPropertyType());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the property name.
   */
  public String getName() {
    return m_name;
  }

  /**
   * @return the property {@link Class} type.
   */
  public Class<?> getType() {
    return m_type;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public int hashCode() {
    return m_name.hashCode() ^ m_type.hashCode();
  }

  @Override
  public boolean equals(Object object) {
    // self check
    if (object == this) {
      return true;
    }
    // compare with other adapter
    if (object instanceof PropertyAdapter) {
      PropertyAdapter adapter = (PropertyAdapter) object;
      return m_name.equals(adapter.m_name) && m_type == adapter.m_type;
    }
    // default
    return false;
  }
}