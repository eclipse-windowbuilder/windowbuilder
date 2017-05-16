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
package org.eclipse.wb.internal.core.model.description;

import org.eclipse.wb.internal.core.model.order.MethodOrder;

import java.lang.reflect.Method;

/**
 * Description for single method of {@link ComponentDescription}.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public class MethodDescription extends AbstractInvocationDescription {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public MethodDescription(Class<?> declaringClass) {
    super(declaringClass);
  }

  public MethodDescription(Method method) throws Exception {
    super(method.getDeclaringClass());
    setName(method.getName());
    setReturnClass(method.getReturnType());
    // add parameters
    for (Class<?> parameterType : method.getParameterTypes()) {
      ParameterDescription parameterDescription = new ParameterDescription();
      parameterDescription.setType(parameterType);
      addParameter(parameterDescription);
    }
    // finalize
    postProcess();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Join
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void join(AbstractInvocationDescription description) {
    super.join(description);
    // order
    MethodDescription methodDescription = (MethodDescription) description;
    if (methodDescription.m_order != MethodOrder.DEFAULT) {
      m_order = methodDescription.m_order;
    }
    // executable
    if (methodDescription.m_executable != null) {
      m_executable = methodDescription.m_executable;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Return class
  //
  ////////////////////////////////////////////////////////////////////////////
  private Class<?> m_returnClass;

  /**
   * @return the {@link Class} of component returned by this factory method;
   */
  public Class<?> getReturnClass() {
    return m_returnClass;
  }

  /**
   * Sets the {@link Class} of component returned by this factory method;
   */
  public void setReturnClass(Class<?> componentClass) {
    m_returnClass = componentClass;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Order
  //
  ////////////////////////////////////////////////////////////////////////////
  private MethodOrder m_order = MethodOrder.DEFAULT;

  /**
   * @return the {@link MethodOrder}, i.e. specification where invocations of this method should be
   *         located relative to invocations of other methods.
   */
  public final MethodOrder getOrder() {
    return m_order;
  }

  /**
   * Sets the {@link MethodOrder}.
   */
  public void setOrder(MethodOrder order) {
    m_order = order;
  }

  /**
   * Sets the {@link MethodOrder} using its specification.
   */
  public void setOrderSpecification(String specification) {
    m_order = MethodOrder.parse(specification);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Executable
  //
  ////////////////////////////////////////////////////////////////////////////
  private Boolean m_executable = null;

  /**
   * @return <code>true</code> if evaluation this method is allowed.
   */
  public final boolean isExecutable() {
    if (m_executable == null) {
      return true;
    }
    return m_executable;
  }

  /**
   * Specifies if this method can be evaluated.
   */
  public final void setExecutable(boolean executable) {
    m_executable = executable ? Boolean.TRUE : Boolean.FALSE;
  }
}
