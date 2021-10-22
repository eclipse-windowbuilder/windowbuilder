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
package org.eclipse.wb.internal.xwt.model.property.event;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDisplayPropertyEditor;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;

import org.eclipse.swt.graphics.Point;

/**
 * {@link PropertyEditor} for {@link XwtListenerProperty}.
 *
 * @author scheglov_ke
 * @coverage XWT.model.property
 */
final class XwtListenerPropertyEditor extends TextDisplayPropertyEditor {
  private final String m_attribute;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public XwtListenerPropertyEditor(String name) {
    m_attribute = name + "Event";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText(Property property) throws Exception {
    XmlObjectInfo object = ((XwtListenerProperty) property).getObject();
    return object.getAttribute(m_attribute);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void doubleClick(Property property, Point location) throws Exception {
    ((XwtListenerProperty) property).openListener();
  }
}
