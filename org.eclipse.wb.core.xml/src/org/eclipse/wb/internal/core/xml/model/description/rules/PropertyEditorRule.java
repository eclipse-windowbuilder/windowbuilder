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
package org.eclipse.wb.internal.core.xml.model.description.rules;

import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.description.DescriptionPropertiesHelper;
import org.eclipse.wb.internal.core.xml.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.xml.model.description.internal.PropertyEditorDescription;

import org.apache.commons.digester3.Rule;
import org.xml.sax.Attributes;

/**
 * The {@link Rule} that sets {@link PropertyEditor} of current {@link GenericPropertyDescription}.
 *
 * @author scheglov_ke
 * @coverage XML.model.description
 */
public final class PropertyEditorRule extends Rule {
  private final EditorContext m_context;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PropertyEditorRule(EditorContext context) {
    m_context = context;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rule
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void begin(String namespace, String name, Attributes attributes) throws Exception {
    String id = attributes.getValue("id");
    PropertyEditor editor = DescriptionPropertiesHelper.getConfigurableEditor(id);
    getDigester().push(new PropertyEditorDescription(m_context, editor));
  }

  @Override
  public void end(String namespace, String name) throws Exception {
    // prepare editor
    PropertyEditor editor;
    {
      PropertyEditorDescription editorDescription = (PropertyEditorDescription) getDigester().pop();
      editor = editorDescription.getConfiguredEditor();
    }
    // set editor for current property
    GenericPropertyDescription propertyDescription =
        (GenericPropertyDescription) getDigester().peek();
    if (propertyDescription != null) {
      propertyDescription.setEditor(editor);
    }
  }
}
