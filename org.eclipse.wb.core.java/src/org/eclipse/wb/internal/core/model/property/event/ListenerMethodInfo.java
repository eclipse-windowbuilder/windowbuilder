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
package org.eclipse.wb.internal.core.model.property.event;

import org.eclipse.wb.internal.core.utils.GenericTypeResolver;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Information about single method in {@link ListenerInfo}.
 *
 * @author scheglov_ke
 * @coverage core.model.property.events
 */
final class ListenerMethodInfo {
  private final ListenerInfo m_listener;
  private final Method m_method;
  private final String m_signature;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ListenerMethodInfo(ListenerInfo listener, Method method) {
    m_listener = listener;
    m_method = method;
    m_signature = ReflectionUtils.getMethodSignature(method);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the parent {@link ListenerInfo}.
   */
  public ListenerInfo getListener() {
    return m_listener;
  }

  /**
   * @return the reflection {@link Method}.
   */
  public Method getMethod() {
    return m_method;
  }

  /**
   * @return the signature of reflection {@link Method}.
   */
  public String getSignature() {
    return m_signature;
  }

  /**
   * @return the name of this method.
   */
  public String getName() {
    return m_method.getName();
  }

  /**
   * @return <code>true</code> if this {@link Method} is abstract.
   */
  public boolean isAbstract() {
    return ReflectionUtils.isAbstract(m_method);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Generics utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the signature for method in AST. It may be different from {@link #getSignature()} if
   *         listener type is generic.
   */
  public String getSignatureAST() {
    String name = m_method.getName();
    StringBuilder buffer = new StringBuilder();
    buffer.append(name);
    // types
    buffer.append('(');
    boolean firstParameter = true;
    for (String parameterType : getActualParameterTypes()) {
      // separator
      if (firstParameter) {
        firstParameter = false;
      } else {
        buffer.append(',');
      }
      // in AST signatures we don't use generics, so remove them
      parameterType = StringUtils.substringBefore(parameterType, "<");
      // append
      buffer.append(parameterType);
    }
    buffer.append(')');
    // done
    return buffer.toString();
  }

  /**
   * @return the actual parameter types of this method, as required by type arguments of listener.
   */
  public String[] getActualParameterTypes() {
    Type[] genericTypes = m_method.getGenericParameterTypes();
    GenericTypeResolver resolver = m_listener.getResolver();
    return GenericsUtils.getTypeNames(resolver, genericTypes);
  }
}
