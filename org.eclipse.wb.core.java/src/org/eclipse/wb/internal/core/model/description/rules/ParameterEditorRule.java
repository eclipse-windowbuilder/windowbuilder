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
package org.eclipse.wb.internal.core.model.description.rules;

import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.description.helpers.DescriptionPropertiesHelper;
import org.eclipse.wb.internal.core.model.description.internal.PropertyEditorDescription;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;

/**
 * The {@link Rule} that sets {@link PropertyEditor} of current {@link ParameterDescription}.
 *
 * @author lobas_av
 * @coverage core.model.description
 */
public final class ParameterEditorRule extends Rule {
  private final EditorState m_state;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ParameterEditorRule(EditorState state) {
    m_state = state;
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
    digester.push(new PropertyEditorDescription(m_state, editor));
  }

  @Override
  public void end(String namespace, String name) throws Exception {
    // prepare editor
    PropertyEditor editor;
    {
      PropertyEditorDescription editorDescription = (PropertyEditorDescription) digester.pop();
      editor = editorDescription.getConfiguredEditor();
    }
    // set editor for current property
    ParameterDescription propertyDescription = (ParameterDescription) digester.peek();
    propertyDescription.setEditor(editor);
  }
}