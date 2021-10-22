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
package org.eclipse.wb.internal.core.xml.model.property;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.property.ObjectProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;

import org.eclipse.jdt.core.IJavaProject;

/**
 * {@link Property} for {@link XmlObjectInfo}.
 *
 * @author scheglov_ke
 * @coverage XML.model.property
 */
public abstract class XmlProperty extends ObjectProperty {
  protected final XmlObjectInfo m_object;
  private final String m_title;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public XmlProperty(XmlObjectInfo object, String title, PropertyEditor propertyEditor) {
    this(object, title, PropertyCategory.NORMAL, propertyEditor);
  }

  public XmlProperty(XmlObjectInfo object,
      String title,
      PropertyCategory category,
      PropertyEditor propertyEditor) {
    super(propertyEditor);
    m_object = object;
    m_title = title;
    setCategory(category);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final String getTitle() {
    return m_title;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final ObjectInfo getObjectInfo() {
    return m_object;
  }

  /**
   * @return the {@link XmlObjectInfo} of this property.
   */
  public final XmlObjectInfo getObject() {
    return m_object;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setValue(final Object value) throws Exception {
    ExecutionUtils.run(m_object, new RunnableEx() {
      public void run() throws Exception {
        setValueEx(value);
      }
    });
  }

  /**
   * Implementation of {@link #setValue(Object)} executed in edit operation.
   */
  protected void setValueEx(Object value) throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IAdaptable
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public <T> T getAdapter(Class<T> adapter) {
    if (adapter == IJavaProject.class) {
      return adapter.cast(m_object.getContext().getJavaProject());
    }
    if (adapter == ObjectInfo.class) {
      return adapter.cast(m_object);
    }
    return super.getAdapter(adapter);
  }
}
