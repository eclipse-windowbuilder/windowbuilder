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

import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.apache.commons.lang.ClassUtils;

import java.util.Map;

/**
 * The {@link PropertyEditor} for selecting single class instance from given set.
 *
 * @author sablin_aa
 * @coverage core.model.property.editor
 */
public final class InstanceListPropertyEditor extends AbstractListPropertyEditor {
  private String[] m_types;
  private String[] m_titles;
  private Class<?>[] m_classes;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access to list items
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected int getCount() {
    return m_classes.length;
  }

  @Override
  protected int getValueIndex(Object value) {
    for (int i = 0; i < getCount(); i++) {
      if (value == null ? m_classes[i] == null : value.getClass() == m_classes[i]) {
        return i;
      }
    }
    return -1;
  }

  @Override
  protected String getTitle(int index) {
    return m_titles[index];
  }

  @Override
  protected String getExpression(int index) throws Exception {
    return m_classes[index] == null ? "null" : "new " + m_types[index] + "()";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IConfigurablePropertyObject
  //
  ////////////////////////////////////////////////////////////////////////////
  public void configure(EditorState state, Map<String, Object> parameters) throws Exception {
    // prepare m_classes
    m_types = getParameterAsArray(parameters, "types");
    // prepare m_types
    m_classes = new Class<?>[m_types.length];
    for (int i = 0; i < m_types.length; i++) {
      // special "null" expression
      if (m_types[i].equalsIgnoreCase("null")) {
        m_classes[i] = null;
      } else {
        m_classes[i] = state.getEditorLoader().loadClass(m_types[i]);
      }
    }
    // prepare titles
    m_titles = getParameterAsArray(parameters, "titles", true);
    if (m_titles == null || m_titles.length < 1) {
      m_titles = new String[m_classes.length];
      for (int i = 0; i < m_classes.length; i++) {
        ClassUtils.getShortClassName(m_classes[i]);
        if (m_classes[i] == null) {
          m_titles[i] = "null";
        } else {
          m_titles[i] = ClassUtils.getShortClassName(m_classes[i]);
        }
      }
    } else {
      // sanity check
      Assert.isTrue(
          m_classes.length == m_titles.length,
          "Count of types/titles should be same in %s",
          parameters);
    }
  }
}
