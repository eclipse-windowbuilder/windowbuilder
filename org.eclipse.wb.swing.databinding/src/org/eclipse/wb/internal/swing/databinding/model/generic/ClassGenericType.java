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
package org.eclipse.wb.internal.swing.databinding.model.generic;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * {@link IGenericType} for describe single class.
 *
 * @author lobas_av
 * @coverage bindings.swing.model.generic
 */
public final class ClassGenericType implements IGenericType {
  public static final IGenericType OBJECT_CLASS = new ClassGenericType(Object.class, null, null);
  public static final IGenericType STRING_CLASS = new ClassGenericType(String.class, null, null);
  public static final IGenericType INT_CLASS = new ClassGenericType(int.class, null, null);
  public static final IGenericType LIST_CLASS = new ClassGenericType(List.class,
      "java.util.List<java.lang.Object>",
      null);
  public static final IGenericType WILDCARD = new ClassGenericType(null, "?", "?");
  //
  private final Class<?> m_rawType;
  private final String m_fullName;
  private final String m_simpleName;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ClassGenericType(Class<?> rawType, String fullName, String simpleName) {
    m_rawType = rawType;
    m_fullName =
        fullName == null ? GenericUtils.convertPrimitiveType(ReflectionUtils.getFullyQualifiedName(
            m_rawType,
            false)) : fullName;
    m_simpleName = simpleName == null ? m_rawType.getSimpleName() : simpleName;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IGenericType
  //
  ////////////////////////////////////////////////////////////////////////////
  public Class<?> getRawType() {
    return m_rawType;
  }

  public String getFullTypeName() {
    return m_fullName;
  }

  public String getSimpleTypeName() {
    return m_simpleName;
  }

  public List<IGenericType> getSubTypes() {
    return Collections.emptyList();
  }

  public IGenericType getSubType(int index) {
    return OBJECT_CLASS;
  }

  public boolean isEmpty() {
    return true;
  }
}