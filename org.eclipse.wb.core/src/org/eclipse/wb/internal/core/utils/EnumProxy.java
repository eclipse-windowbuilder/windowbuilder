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
package org.eclipse.wb.internal.core.utils;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import java.util.Map;

/**
 * Class for working with {@link Enum} using reflection. Ex., for cases of enum class not exists in
 * JDK.
 *
 * @author mitin_aa
 * @coverage core.util
 */
public class EnumProxy {
  private Class<?> m_enumClass;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EnumProxy(String enumClass) {
    try {
      m_enumClass = Class.forName(enumClass);
    } catch (Throwable e) {
      // ignore errors, if we can't create instances then we don't need it
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Class} of this {@link Enum}.
   */
  public Class<?> getEnumClass() throws Exception {
    return m_enumClass;
  }

  /**
   * Returns the constant of {@link Enum} by given <code>ordinal</code> value or <code>null</code>
   * if no such constant.
   *
   * @param ordinal
   *          the ordinal to search constant.
   * @return the constant of {@link Enum} by given <code>ordinal</code> value or <code>null</code>
   *         if no such constant.
   * @throws Exception
   */
  public Object getEnumConstant(int ordinal) throws Exception {
    Map<?, ?> map = (Map<?, ?>) ReflectionUtils.invokeMethod2(m_enumClass, "enumConstantDirectory");
    for (Object constant : map.values()) {
      if (getOrdinal(constant) == ordinal) {
        return constant;
      }
    }
    return null;
  }

  /**
   * Returns the ordinal of this {@link Enum} by given constant name string.
   *
   * @param name
   *          the name of constant to search.
   * @return the ordinal of this {@link Enum} by given constant name string.
   * @throws Exception
   */
  public int getOrdinal(String name) throws Exception {
    Object enumConstant =
        ReflectionUtils.invokeMethod2(null, "valueOf", Class.class, String.class, m_enumClass, name);
    return getOrdinal(enumConstant);
  }

  /**
   * Returns the ordinal of this {@link Enum} by given constant object.
   *
   * @param name
   *          the constant object.
   * @return the ordinal of this {@link Enum} by given constant object.
   * @throws Exception
   */
  private int getOrdinal(Object enumConstant) throws Exception {
    return (Integer) ReflectionUtils.invokeMethod2(enumConstant, "ordinal");
  }
}
