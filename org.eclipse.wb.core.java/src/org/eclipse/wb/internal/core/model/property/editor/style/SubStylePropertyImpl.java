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
package org.eclipse.wb.internal.core.model.property.editor.style;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;

import org.eclipse.jface.action.IMenuManager;

/**
 * Abstract sub-property implementation for {@link StylePropertyEditor}.
 *
 * @author lobas_av
 * @coverage core.model.property.editor
 */
public abstract class SubStylePropertyImpl {
  private final AbstractStylePropertyEditor m_editor;
  private final String m_title;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SubStylePropertyImpl(AbstractStylePropertyEditor editor, String title) {
    m_editor = editor;
    m_title = title;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * The title for sub-property.
   */
  public final String getTitle() {
    return m_title;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // As string
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Appends style presentation of this style editor part, for tests.
   */
  public void getAsString(StringBuilder builder) {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link PropertyEditor} for sub-property.
   */
  public abstract PropertyEditor createEditor();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Style
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the current style value.
   */
  protected final long getStyleValue(Property property) throws Exception {
    return m_editor.getStyleValue(property);
  }

  /**
   * Sets the new value of given {@link SubStylePropertyImpl}.
   */
  protected final void setStyleValue(Property property, long newValue) throws Exception {
    m_editor.setStyleValue(property, newValue);
  }

  /**
   * @return int value of given flag.
   */
  public abstract long getFlag(String sFlag);

  /**
   * @return the string/source presentation of this flag.
   */
  public abstract String getFlagValue(Property property) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the current value of this sub-property.
   */
  public abstract Object getValue(Property property) throws Exception;

  /**
   * Sets the new value of this sub-property.
   */
  public abstract void setValue(Property property, Object value) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Popup menu
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Contributes actions into {@link Property} context menu.
   */
  public abstract void contributeActions(Property property, IMenuManager manager) throws Exception;
}