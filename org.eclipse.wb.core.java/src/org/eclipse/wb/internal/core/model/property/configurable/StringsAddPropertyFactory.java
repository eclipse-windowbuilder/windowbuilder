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
package org.eclipse.wb.internal.core.model.property.configurable;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.description.ConfigurablePropertyDescription;
import org.eclipse.wb.internal.core.model.property.IConfigurablePropertyFactory;
import org.eclipse.wb.internal.core.model.property.IConfigurablePropertyObject;
import org.eclipse.wb.internal.core.model.property.JavaProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.state.EditorState;

/**
 * Implementation of {@link IConfigurablePropertyFactory} to create property with
 * {@link StringsAddPropertyEditor}.
 *
 * @author scheglov_ke
 * @coverage core.model.property.editor
 */
public final class StringsAddPropertyFactory implements IConfigurablePropertyFactory {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IConfigurablePropertyFactory
  //
  ////////////////////////////////////////////////////////////////////////////
  public Property create(JavaInfo javaInfo, ConfigurablePropertyDescription description)
      throws Exception {
    Property property = new ConfigurableProperty(javaInfo, description.getTitle());
    if (property.getEditor() instanceof IConfigurablePropertyObject) {
      EditorState editorState = EditorState.get(javaInfo.getEditor());
      IConfigurablePropertyObject editor = (IConfigurablePropertyObject) property.getEditor();
      description.configure(editorState, editor);
    }
    return property;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Property
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link ConfigurableProperty} implementation for this {@link IConfigurablePropertyFactory}.
   */
  private static final class ConfigurableProperty extends JavaProperty {
    private final StringsAddPropertyEditor m_stringsEditor;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public ConfigurableProperty(JavaInfo javaInfo, String title) {
      super(javaInfo, title, new StringsAddPropertyEditor());
      m_stringsEditor = (StringsAddPropertyEditor) getEditor();
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Presentation
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean isModified() throws Exception {
      return m_stringsEditor.getItems(this).length != 0;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Value
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public Object getValue() throws Exception {
      return m_stringsEditor.getItems(this);
    }

    @Override
    public void setValue(Object value) throws Exception {
      if (value instanceof String[]) {
        m_stringsEditor.setItems(this, (String[]) value);
      }
    }
  }
}
