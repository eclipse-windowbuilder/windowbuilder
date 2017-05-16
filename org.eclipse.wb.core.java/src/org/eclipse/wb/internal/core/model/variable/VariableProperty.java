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
package org.eclipse.wb.internal.core.model.variable;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.AbstractTextPropertyEditor;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;

/**
 * Implementation of {@link Property} for editing name of variable.
 *
 * @author scheglov_ke
 * @coverage core.model.variable
 */
public final class VariableProperty extends Property {
  private final AbstractNamedVariableSupport m_variableSupport;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public VariableProperty(AbstractNamedVariableSupport variableSupport) {
    super(new VariablePropertyEditor());
    m_variableSupport = variableSupport;
    setCategory(PropertyCategory.system(0));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Property
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getTitle() {
    return "Variable";
  }

  @Override
  public Object getValue() throws Exception {
    return m_variableSupport.getName();
  }

  @Override
  public boolean isModified() throws Exception {
    return true;
  }

  @Override
  public void setValue(Object value) throws Exception {
    if (!(value instanceof String)) {
      return;
    }
    String text = ((String) value).trim();
    // set name
    JavaInfo javaInfo = m_variableSupport.getJavaInfo();
    try {
      javaInfo.startEdit();
      m_variableSupport.setName(text);
    } finally {
      javaInfo.endEdit();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // VariablePropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final class VariablePropertyEditor extends AbstractTextPropertyEditor {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Text
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected String getText(Property property) throws Exception {
      return (String) property.getValue();
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // AbstractTextPropertyEditor
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected String getEditorText(Property property) throws Exception {
      return getText(property);
    }

    @Override
    protected boolean setEditorText(Property property, String text) throws Exception {
      text = text.trim();
      // validate name
      {
        VariableProperty variableProperty = (VariableProperty) property;
        String errorMessage = variableProperty.m_variableSupport.validateName(text);
        if (errorMessage != null) {
          UiUtils.openWarning(DesignerPlugin.getShell(), property.getTitle(), errorMessage);
          return false;
        }
      }
      // OK
      property.setValue(text);
      return true;
    }
  }
}
