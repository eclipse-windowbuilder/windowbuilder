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
package org.eclipse.wb.internal.core.model.property.editor;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.description.rules.MethodPropertyRule;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.complex.IComplexPropertyEditor;
import org.eclipse.wb.internal.core.model.util.PropertyUtils2;

import java.util.List;

/**
 * The {@link PropertyEditor} for complex method-based property, created by
 * {@link MethodPropertyRule}.
 *
 * @author scheglov_ke
 * @coverage core.model.property.editor
 */
public final class MethodPropertyPropertyEditor extends TextDisplayPropertyEditor
    implements
      IComplexPropertyEditor {
  private final String m_text;
  private final List<GenericPropertyDescription> m_descriptions;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MethodPropertyPropertyEditor(String text, List<GenericPropertyDescription> descriptions) {
    m_text = text;
    m_descriptions = descriptions;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText(Property property) throws Exception {
    return m_text;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IComplexPropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  public Property[] getProperties(Property _methodProperty) throws Exception {
    GenericProperty methodProperty = (GenericProperty) _methodProperty;
    // lazily create sub-properties
    Property[] properties = (Property[]) methodProperty.getArbitraryValue(this);
    if (properties == null) {
      properties = createProperties(methodProperty);
      methodProperty.putArbitraryValue(this, properties);
    }
    // OK, we have sub-properties
    return properties;
  }

  private Property[] createProperties(GenericProperty methodProperty) {
    Property[] properties = new Property[m_descriptions.size()];
    int propertyIndex = 0;
    for (GenericPropertyDescription description : m_descriptions) {
      JavaInfo javaInfo = methodProperty.getJavaInfo();
      properties[propertyIndex++] = PropertyUtils2.createGenericPropertyImpl(javaInfo, description);
    }
    return properties;
  }
}
