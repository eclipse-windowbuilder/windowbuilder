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
package org.eclipse.wb.internal.core.xml.model.property;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;

/**
 * Simple {@link Property} for direct {@link String} attribute of {@link XmlObjectInfo}.
 *
 * @author scheglov_ke
 * @coverage XML.model.property
 */
public class XmlAttributeProperty extends XmlProperty {
  private final String m_attribute;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public XmlAttributeProperty(XmlObjectInfo object,
      String title,
      PropertyEditor propertyEditor,
      String attribute) {
    super(object, title, propertyEditor);
    m_attribute = attribute;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Property
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isModified() throws Exception {
    return getValue() != null;
  }

  @Override
  public Object getValue() throws Exception {
    return m_object.getAttribute(m_attribute);
  }

  @Override
  protected void setValueEx(Object value) throws Exception {
    if (value == UNKNOWN_VALUE) {
      m_object.removeAttribute(m_attribute);
    }
    if (value instanceof String) {
      m_object.setAttribute(m_attribute, (String) value);
    }
  }
}
