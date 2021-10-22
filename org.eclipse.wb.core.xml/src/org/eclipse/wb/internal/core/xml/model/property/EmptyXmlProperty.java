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

import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;

/**
 * Empty {@link XmlProperty}, that has no title or value.
 *
 * @author scheglov_ke
 * @coverage XML.model.property
 */
public class EmptyXmlProperty extends XmlProperty {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EmptyXmlProperty(XmlObjectInfo object) {
    super(object, null, StringPropertyEditor.INSTANCE);
  }

  public EmptyXmlProperty(XmlObjectInfo object, PropertyCategory category) {
    super(object, null, category, StringPropertyEditor.INSTANCE);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isModified() throws Exception {
    return false;
  }

  @Override
  public Object getValue() throws Exception {
    return null;
  }

  @Override
  public void setValue(Object value) throws Exception {
  }
}
