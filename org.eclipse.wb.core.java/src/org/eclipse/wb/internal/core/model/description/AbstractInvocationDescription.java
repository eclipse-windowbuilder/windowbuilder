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

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Abstract description for some invocation (method or constructor).
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public abstract class AbstractInvocationDescription extends AbstractDescription {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractInvocationDescription(Class<?> declaringClass) {
    m_declaringClass = declaringClass;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final String toString() {
    return m_name + "(" + StringUtils.join(m_parameters.iterator(), ",") + ")";
  }

  @Override
  public final boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof AbstractInvocationDescription) {
      AbstractInvocationDescription description = (AbstractInvocationDescription) obj;
      return m_signature.equals(description.m_signature);
    }
    return false;
  }

  @Override
  public final int hashCode() {
    return m_signature.hashCode();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Join
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Joins this {@link AbstractInvocationDescription} with given one.
   */
  public void join(AbstractInvocationDescription description) {
    // parameters
    List<ParameterDescription> newParameters = description.getParameters();
    for (int index = 0; index < m_parameters.size(); index++) {
      ParameterDescription thisParameter = m_parameters.get(index);
      ParameterDescription newParameter = newParameters.get(index);
      thisParameter.join(newParameter);
    }
    // tags
    putTags(description.getTags());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Declaring class
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Class<?> m_declaringClass;

  public final Class<?> getDeclaringClass() {
    return m_declaringClass;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Name
  //
  ////////////////////////////////////////////////////////////////////////////
  private String m_name;

  /**
   * @return the name of this method.
   */
  public final String getName() {
    return m_name;
  }

  /**
   * Sets the name of this method.
   */
  public final void setName(String name) {
    m_name = name;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parameters
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<ParameterDescription> m_parameters = Lists.newArrayList();

  /**
   * @return the list of {@link ParameterDescription}'s of this
   *         {@link AbstractInvocationDescription}.
   */
  public List<ParameterDescription> getParameters() {
    return m_parameters;
  }

  /**
   * @return the {@link ParameterDescription} of this {@link AbstractInvocationDescription}.
   */
  public ParameterDescription getParameter(int index) {
    return m_parameters.get(index);
  }

  /**
   * Adds new {@link ParameterDescription}.
   */
  public final void addParameter(ParameterDescription parameter) {
    m_parameters.add(parameter);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parameter utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link List} of default source for each {@link ParameterDescription}.
   */
  public List<String> getDefaultArguments() {
    List<String> defaultArguments = Lists.newArrayList();
    for (ParameterDescription parameter : getParameters()) {
      String defaultArgument = parameter.getDefaultSource();
      defaultArguments.add(defaultArgument);
    }
    return defaultArguments;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Signature
  //
  ////////////////////////////////////////////////////////////////////////////
  private String m_signature;

  /**
   * @return the signature of this method. This signature should be same as
   *         {@link AstNodeUtils#getMethodSignature(org.eclipse.jdt.core.dom.MethodDeclaration)} .
   */
  public final String getSignature() {
    return m_signature;
  }

  /**
   * This method is invoked as last step, after all parameters added and computes final signature of
   * this method.
   */
  public final void postProcess() {
    // post process parameters
    for (int index = 0; index < m_parameters.size(); index++) {
      ParameterDescription parameter = m_parameters.get(index);
      parameter.setIndex(index);
    }
    // compute signature
    {
      StringBuffer buffer = new StringBuffer();
      // append name
      buffer.append(m_name);
      buffer.append("(");
      // append parameters
      for (int i = 0; i < m_parameters.size(); i++) {
        ParameterDescription parameter = m_parameters.get(i);
        if (i != 0) {
          buffer.append(',');
        }
        buffer.append(ReflectionUtils.getFullyQualifiedName(parameter.getType(), false));
      }
      //
      buffer.append(")");
      // set final signature
      m_signature = buffer.toString();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialization
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_initialized;

  /**
   * @return <code>true</code>, if this {@link AbstractInvocationDescription} is fully initialized.
   */
  public boolean isInitialized() {
    return m_initialized;
  }

  /**
   * Specifies, if this {@link AbstractInvocationDescription} is fully initialized.
   * <p>
   * For {@link AbstractInvocationDescription} we need to know names for
   * {@link ParameterDescription}'s because we show them to user in property table. But getting them
   * requires much time, so we should not load them always when
   * {@link AbstractInvocationDescription} is loaded. So, we delay full initialization.
   */
  public void setInitialized(boolean initialized) {
    m_initialized = initialized;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visiting
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final void visit(JavaInfo javaInfo, int state) throws Exception {
    super.visit(javaInfo, state);
    for (ParameterDescription parameter : m_parameters) {
      parameter.visit(javaInfo, state);
    }
  }
}
