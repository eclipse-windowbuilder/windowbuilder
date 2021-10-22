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
package org.eclipse.wb.internal.rcp.model.rcp;

import com.google.common.base.Function;
import com.google.common.base.Functions;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.core.resources.IProject;
import org.eclipse.pde.core.plugin.IPluginElement;

import org.apache.commons.lang.ObjectUtils;

/**
 * {@link Property} that updates PDE model.
 *
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public final class ExtensionElementProperty<T> extends Property {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Converters
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final Function<String, String> IDENTITY = Functions.identity();
  public static final Function<Boolean, String> FROM_BOOLEAN = new Function<Boolean, String>() {
    public String apply(Boolean from) {
      return from.toString();
    }
  };
  public static final Function<String, Boolean> TO_BOOLEAN = new Function<String, Boolean>() {
    public Boolean apply(String from) {
      return Boolean.parseBoolean(from);
    }
  };
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final RunnableEx m_setValueListener;
  private final String m_title;
  private final IProject m_project;
  private final PdeUtils m_utils;
  private final String m_pointID;
  private final String m_elementName;
  private final String m_className;
  private final String m_attributeName;
  private final Function<T, String> m_fromValueConverter;
  private final Function<String, T> m_toValueConverter;
  private final Object m_defaultValue;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ExtensionElementProperty(RunnableEx setValueListener,
      PropertyEditor editor,
      String title,
      IProject project,
      String pointID,
      String elementName,
      String extensionID,
      String attributeName,
      Function<T, String> fromValueConverter,
      Function<String, T> toValueConverter,
      Object defaultValue) {
    super(editor);
    m_setValueListener = setValueListener;
    m_title = title;
    m_project = project;
    m_defaultValue = defaultValue;
    m_utils = PdeUtils.get(project);
    m_pointID = pointID;
    m_elementName = elementName;
    m_className = extensionID;
    m_attributeName = attributeName;
    m_fromValueConverter = fromValueConverter;
    m_toValueConverter = toValueConverter;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getTitle() {
    return m_title;
  }

  @Override
  public boolean isModified() throws Exception {
    Object value = getValue();
    return value != UNKNOWN_VALUE && !ObjectUtils.equals(value, m_defaultValue);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Object getValue() throws Exception {
    IPluginElement element = getElement();
    if (element != null) {
      String attributeValue = PdeUtils.getAttribute(element, m_attributeName);
      if (attributeValue != null) {
        return m_toValueConverter.apply(attributeValue);
      }
    }
    return m_defaultValue;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void setValue(Object value) throws Exception {
    IPluginElement element = getElement();
    if (element != null && !ObjectUtils.equals(value, getValue())) {
      String attributeValue;
      if (value == UNKNOWN_VALUE || ObjectUtils.equals(value, m_defaultValue)) {
        attributeValue = null;
      } else {
        attributeValue = m_fromValueConverter.apply((T) value);
      }
      // set new attribute value
      m_utils.setAttribute(element, m_attributeName, attributeValue);
      // notify listener
      if (m_setValueListener != null) {
        m_setValueListener.run();
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link PdeUtils} for accessing/editing <code>plugin.xml</code> file.
   */
  public PdeUtils getUtils() {
    return m_utils;
  }

  /**
   * @return the {@link IProject} of this {@link ExtensionElementProperty}.
   */
  public IProject getProject() {
    return m_project;
  }

  /**
   * @return <code>true</code> if this {@link ExtensionElementProperty} has corresponding
   *         {@link IPluginElement}, so can show/change something.
   */
  public boolean hasElement() {
    return getElement() != null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link IPluginElement} corresponding to given extension parameters.
   */
  private IPluginElement getElement() {
    return m_utils.getExtensionElementByClass(m_pointID, m_elementName, m_className);
  }
}
