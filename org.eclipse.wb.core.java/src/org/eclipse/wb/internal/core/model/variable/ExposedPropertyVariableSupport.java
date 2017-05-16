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
package org.eclipse.wb.internal.core.model.variable;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;

import org.eclipse.jdt.core.dom.Statement;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Method;

/**
 * {@link VariableSupport} implementation for sub-components exposed using property.
 *
 * @author scheglov_ke
 * @coverage core.model.variable
 */
public final class ExposedPropertyVariableSupport extends AbstractNoNameVariableSupport {
  private final JavaInfo m_hostJavaInfo;
  private final Method m_method;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ExposedPropertyVariableSupport(JavaInfo javaInfo, JavaInfo hostJavaInfo, Method method) {
    super(javaInfo);
    m_hostJavaInfo = hostJavaInfo;
    m_method = method;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "property";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isValidStatementForChild(Statement statement) {
    return m_hostJavaInfo.getVariableSupport().isValidStatementForChild(statement);
  }

  @Override
  public String getTitle() throws Exception {
    return m_method.getName() + "()";
  }

  @Override
  public String getComponentName() {
    String methodName = m_method.getName();
    if (methodName.startsWith("get")) {
      methodName = methodName.substring("get".length());
    }
    return m_hostJavaInfo.getVariableSupport().getComponentName()
        + StringUtils.capitalize(methodName);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Expressions
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean hasExpression(NodeTarget target) {
    return m_hostJavaInfo.getVariableSupport().hasExpression(target);
  }

  @Override
  public String getReferenceExpression(NodeTarget target) throws Exception {
    String parentAccess = m_hostJavaInfo.getVariableSupport().getAccessExpression(target);
    return parentAccess + m_method.getName() + "()";
  }

  @Override
  public String getAccessExpression(NodeTarget target) throws Exception {
    return getReferenceExpression(target) + ".";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Target
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public StatementTarget getStatementTarget() throws Exception {
    return m_hostJavaInfo.getVariableSupport().getStatementTarget();
  }

  @Override
  public StatementTarget getChildTarget() throws Exception {
    return JavaInfoUtils.getTarget(m_hostJavaInfo);
  }
}
