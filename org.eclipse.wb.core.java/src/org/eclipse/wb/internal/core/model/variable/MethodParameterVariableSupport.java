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

import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.eval.ExecutionFlowUtils;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;

import java.text.MessageFormat;
import java.util.List;

/**
 * Implementation of {@link VariableSupport} for {@link SingleVariableDeclaration} parameter of
 * {@link MethodDeclaration}.
 *
 * @author scheglov_ke
 * @coverage core.model.variable
 */
public final class MethodParameterVariableSupport extends AbstractNamedVariableSupport {
  private final SingleVariableDeclaration m_parameter;
  private final MethodDeclaration m_method;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MethodParameterVariableSupport(JavaInfo javaInfo, SingleVariableDeclaration parameter) {
    super(javaInfo, parameter.getName());
    m_parameter = parameter;
    m_method = (MethodDeclaration) m_parameter.getParent();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return m_parameter.getName().getIdentifier();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getTitle() throws Exception {
    return m_parameter.getName().getIdentifier()
        + " in "
        + m_method.getName().getIdentifier()
        + "(...)";
  }

  @Override
  public boolean isJavaInfo(ASTNode node) {
    if (node instanceof SimpleName && AstNodeUtils.isVariable(node)) {
      return ExecutionFlowUtils.getLastAssignment(new ExecutionFlowDescription(m_method), node) == m_parameter;
    }
    return false;
  }

  @Override
  public StatementTarget getStatementTarget() throws Exception {
    // if first statement is SuperConstructorInvocation, then target = after super()
    {
      List<Statement> statements = DomGenerics.statements(m_method.getBody());
      if (!statements.isEmpty() && statements.get(0) instanceof SuperConstructorInvocation) {
        return new StatementTarget(statements.get(0), false);
      }
    }
    // target = in beginning of method
    return new StatementTarget(m_method, true);
  }

  @Override
  public boolean isValidStatementForChild(Statement statement) {
    return AstNodeUtils.getEnclosingMethod(statement) == m_method;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Name
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setName(String newName) throws Exception {
    modifyName(newName);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Expressions
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean hasExpression(NodeTarget target) {
    return getReferenceExpression0(target) != null;
  }

  @Override
  public String getReferenceExpression(NodeTarget target) throws Exception {
    String expression = getReferenceExpression0(target);
    if (expression != null) {
      return expression;
    }
    throw new IllegalArgumentException(MessageFormat.format(
        "Can not access {0} of {1} at target {2}.",
        m_parameter,
        m_method,
        target));
  }

  private String getReferenceExpression0(NodeTarget target) {
    StatementTarget statementTarget = target.getStatementTarget();
    if (statementTarget != null) {
      ASTNode targetNode = statementTarget.getNode();
      // directly in declaring method
      if (AstNodeUtils.contains(m_method, targetNode)) {
        return getName();
      }
      // in other method
      {
        MethodDeclaration targetMethod = AstNodeUtils.getEnclosingMethod(targetNode);
        for (SingleVariableDeclaration parameter : DomGenerics.parameters(targetMethod)) {
          SimpleName parameterName = parameter.getName();
          if (m_javaInfo.isRepresentedBy(parameterName)) {
            return parameterName.getIdentifier();
          }
        }
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Conversion
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canConvertLocalToField() {
    return false;
  }

  @Override
  public void convertLocalToField() throws Exception {
    throw new IllegalStateException();
  }

  @Override
  public boolean canConvertFieldToLocal() {
    return false;
  }

  @Override
  public void convertFieldToLocal() throws Exception {
    throw new IllegalStateException();
  }
}
