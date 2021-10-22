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
package org.eclipse.wb.internal.xwt.model.property.editor.style;

import org.eclipse.wb.internal.core.model.property.Property;

/**
 * Sub-property for {@link StylePropertyEditor}.
 *
 * @author lobas_av
 * @coverage XWT.model.property.editor
 */
final class SubStyleProperty extends Property {
  private final Property m_mainProperty;
  private final SubStylePropertyImpl m_propertyImpl;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SubStyleProperty(Property mainProperty, SubStylePropertyImpl propertyImpl) {
    super(propertyImpl.createEditor());
    m_mainProperty = mainProperty;
    m_propertyImpl = propertyImpl;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getTitle() {
    return m_propertyImpl.getTitle();
  }

  @Override
  public boolean isModified() throws Exception {
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Object getValue() throws Exception {
    return m_propertyImpl.getValue(m_mainProperty);
  }

  @Override
  public void setValue(Object value) throws Exception {
    m_propertyImpl.setValue(m_mainProperty, value);
  }
}