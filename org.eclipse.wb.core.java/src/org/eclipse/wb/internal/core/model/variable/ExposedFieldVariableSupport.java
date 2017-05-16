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

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.Statement;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Field;

/**
 * {@link VariableSupport} implementation for sub-component exposed using {@link Field}.
 *
 * @author scheglov_ke
 * @coverage core.model.variable
 */
public final class ExposedFieldVariableSupport extends AbstractNoNameVariableSupport {
  private final JavaInfo m_hostJavaInfo;
  private final Field m_field;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ExposedFieldVariableSupport(JavaInfo javaInfo, JavaInfo hostJavaInfo, Field field) {
    super(javaInfo);
    m_hostJavaInfo = hostJavaInfo;
    m_field = field;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return m_field.getName();
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
    return m_field.getName();
  }

  @Override
  public String getComponentName() {
    String name = m_field.getName();
    name =
        new VariableUtils(m_javaInfo).stripPrefixSuffix(
            name,
            JavaCore.CODEASSIST_FIELD_PREFIXES,
            JavaCore.CODEASSIST_FIELD_SUFFIXES);
    name = StringUtils.capitalize(name);
    return m_hostJavaInfo.getVariableSupport().getComponentName() + name;
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
    return parentAccess + m_field.getName();
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
