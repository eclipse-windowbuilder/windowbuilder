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
package org.eclipse.wb.core.editor.palette.model;

import org.eclipse.wb.internal.core.editor.palette.model.entry.AttributesProvider;
import org.eclipse.wb.internal.core.utils.check.Assert;

/**
 * Abstract element of palette.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette
 */
public abstract class AbstractElementInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final int hashCode() {
    return m_id.hashCode();
  }

  @Override
  public final boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof AbstractElementInfo) {
      AbstractElementInfo element = (AbstractElementInfo) obj;
      return m_id.equals(element.m_id);
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Id
  //
  ////////////////////////////////////////////////////////////////////////////
  private String m_id;

  /**
   * Sets the id of this element, can be used only one time.
   */
  public final void setId(String id) {
    Assert.isNull(m_id);
    Assert.isNotNull(id);
    m_id = id;
  }

  /**
   * @return the id of this element.
   */
  public final String getId() {
    return m_id;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visible
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_visible = true;

  /**
   * @return <code>true</code> if this element is visible.
   */
  public final boolean isVisible() {
    return m_visible;
  }

  /**
   * Sets visibility flag.
   */
  public final void setVisible(boolean visible) {
    m_visible = visible;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Name
  //
  ////////////////////////////////////////////////////////////////////////////
  private String m_name;

  /**
   * @return the name of this element.
   */
  public final String getName() {
    if (m_name == null) {
      return getNameDefault();
    }
    return m_name;
  }

  /**
   * @return the "raw" name, without default.
   */
  protected String getNameRaw() {
    return m_name;
  }

  /**
   * @return the default name, instead of <code>null</code>.
   */
  protected String getNameDefault() {
    return "(unknown)";
  }

  /**
   * Sets the name of this element.
   */
  public final void setName(String name) {
    m_name = name;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Description
  //
  ////////////////////////////////////////////////////////////////////////////
  private String m_description;

  /**
   * @return the description of this element.
   */
  public final String getDescription() {
    return m_description;
  }

  /**
   * Sets the description of this element.
   */
  public final void setDescription(String description) {
    m_description = description;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if given {@link AttributesProvider} has attribute with value "true",
   *         or return <code>defaultValue</code> if there are no such attribute.
   */
  protected static boolean getBoolean(AttributesProvider attributes,
      String attribute,
      boolean defaultValue) {
    String text = attributes.getAttribute(attribute);
    if (text != null) {
      return "true".equals(text);
    }
    return defaultValue;
  }
}
