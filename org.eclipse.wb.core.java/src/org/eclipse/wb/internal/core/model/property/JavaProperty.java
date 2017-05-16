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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;

import org.eclipse.jdt.core.IJavaProject;

/**
 * Abstract {@link Property} for {@link JavaInfo}.
 *
 * @author scheglov_ke
 * @coverage core.model.property
 */
public abstract class JavaProperty extends ObjectProperty {
  protected final JavaInfo m_javaInfo;
  private String m_title;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JavaProperty(JavaInfo javaInfo, String title, PropertyEditor propertyEditor) {
    super(propertyEditor);
    m_javaInfo = javaInfo;
    m_title = title;
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

  /**
   * Sets the title, used to rename properties in special toolkits.
   */
  public final void setTitle(String title) {
    m_title = title;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public ObjectInfo getObjectInfo() {
    return m_javaInfo;
  }

  /**
   * @return the {@link JavaInfo} of this {@link Property}.
   */
  public final JavaInfo getJavaInfo() {
    return m_javaInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IAdapter
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public <T> T getAdapter(Class<T> adapter) {
    if (adapter == IJavaProject.class) {
      return adapter.cast(m_javaInfo.getEditor().getJavaProject());
    }
    if (adapter == ObjectInfo.class) {
      return adapter.cast(m_javaInfo);
    }
    return super.getAdapter(adapter);
  }
}
