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
package org.eclipse.wb.internal.core.model.property;

import com.google.common.collect.Maps;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.utils.IAdaptable;

import java.util.Map;

/**
 * {@link Property} is used to display/change properties of {@link ObjectInfo}'s.
 *
 * @author scheglov_ke
 * @coverage core.model.property
 */
public abstract class Property implements IAdaptable {
  /**
   * The value that should be used when we don't know real value of {@link Property}. We can not use
   * <code>null</code> because <code>null</code> can be valid value.
   */
  public static final Object UNKNOWN_VALUE = new Object() {
    @Override
    public String toString() {
      return "UNKNOWN_VALUE";
    }
  };
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final PropertyEditor m_editor;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public Property(PropertyEditor editor) {
    m_category = PropertyCategory.NORMAL;
    m_editor = editor;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the title displayed to the user to identify the property.
   */
  public abstract String getTitle();

  /**
   * @return <code>true</code> if this property has a non-default value
   */
  public abstract boolean isModified() throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Category
  //
  ////////////////////////////////////////////////////////////////////////////
  private PropertyCategory m_category;

  /**
   * @return current {@link PropertyCategory}.
   */
  public final PropertyCategory getCategory() {
    return m_category;
  }

  /**
   * Sets the {@link PropertyCategory} for this {@link Property}.
   */
  public final void setCategory(PropertyCategory category) {
    m_category = category;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the current value of this {@link Property} or {@link #UNKNOWN_VALUE}.
   */
  public abstract Object getValue() throws Exception;

  /**
   * Sets the new value of this {@link Property}.
   *
   * @param the
   *          new value of {@link Property} or {@link #UNKNOWN_VALUE} if {@link Property}
   *          modification should be removed.
   */
  public abstract void setValue(Object value) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link PropertyEditor}.
   */
  public final PropertyEditor getEditor() {
    return m_editor;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Composite
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the composite {@link Property} for given array of {@link Property}'s or
   *         <code>null</code> if no composite {@link Property} can be created.
   */
  public Property getComposite(Property[] properties) {
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IAdaptable
  //
  ////////////////////////////////////////////////////////////////////////////
  public <T> T getAdapter(Class<T> adapter) {
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Arbitrary values map
  //
  ////////////////////////////////////////////////////////////////////////////
  private Map<Object, Object> m_arbitraryMap;

  /**
   * Associates the given value with the given key.
   */
  public final void putArbitraryValue(Object key, Object value) {
    if (m_arbitraryMap == null) {
      m_arbitraryMap = Maps.newHashMap();
    }
    m_arbitraryMap.put(key, value);
  }

  /**
   * @return the value to which the given key is mapped, or <code>null</code>.
   */
  public final Object getArbitraryValue(Object key) {
    if (m_arbitraryMap != null) {
      return m_arbitraryMap.get(key);
    }
    return null;
  }

  /**
   * Removes the mapping for a key.
   */
  public final void removeArbitraryValue(Object key) {
    if (m_arbitraryMap != null) {
      m_arbitraryMap.remove(key);
    }
  }
}
