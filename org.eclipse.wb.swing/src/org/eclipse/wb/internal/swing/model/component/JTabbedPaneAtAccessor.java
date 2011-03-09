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
package org.eclipse.wb.internal.swing.model.component;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.Statement;

import org.apache.commons.lang.StringUtils;

import java.util.List;

import javax.swing.JTabbedPane;

/**
 * The implementation of {@link ExpressionAccessor} for {@link JTabbedPane} "set*At(index, value)"
 * methods.
 * 
 * @author scheglov_ke
 * @coverage swing.model
 */
public final class JTabbedPaneAtAccessor extends ExpressionAccessor {
  private final String m_signature;
  private final JTabbedPaneInfo m_pane;
  private final ComponentInfo m_component;
  private final String m_defaultSource;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JTabbedPaneAtAccessor(String signature,
      JTabbedPaneInfo pane,
      ComponentInfo component,
      String defaultSource) {
    m_signature = signature;
    m_pane = pane;
    m_component = component;
    m_defaultSource = defaultSource;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ExpressionAccessor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Expression getExpression(JavaInfo javaInfo) throws Exception {
    MethodInvocation invocation = getMethodInvocation();
    // check if valid creation found
    if (invocation != null) {
      return (Expression) invocation.arguments().get(1);
    }
    // not found
    return null;
  }

  @Override
  public boolean setExpression(JavaInfo javaInfo, String source) throws Exception {
    MethodInvocation invocation = getMethodInvocation();
    if (invocation != null) {
      boolean remove = source == null || source.equals(m_defaultSource);
      if (remove) {
        javaInfo.startEdit();
        try {
          m_pane.getEditor().removeEnclosingStatement(invocation);
        } finally {
          javaInfo.endEdit();
        }
      } else {
        Expression oldExpression = (Expression) invocation.arguments().get(1);
        if (!javaInfo.getEditor().getSource(oldExpression).equals(source)) {
          javaInfo.startEdit();
          try {
            javaInfo.getEditor().replaceExpression(oldExpression, source);
          } finally {
            javaInfo.endEdit();
          }
        }
      }
    } else if (source != null) {
      javaInfo.startEdit();
      try {
        // prepare invocation source
        String invocationSource;
        {
          String methodName = StringUtils.substringBefore(m_signature, "(");
          int componentIndex = getComponentIndex();
          invocationSource =
              TemplateUtils.format("{0}.{1}({2}, {3})", m_pane, methodName, componentIndex, source);
        }
        // prepare target
        StatementTarget target = getAtTarget(m_component);
        // add invocation
        Expression expression = m_pane.addExpressionStatement(target, invocationSource);
        m_component.addRelatedNode(expression);
      } finally {
        javaInfo.endEdit();
      }
    }
    // success
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link MethodInvocation} for this {@link JTabbedPaneInfo} and {@link ComponentInfo}
   *         .
   */
  private MethodInvocation getMethodInvocation() throws Exception {
    int componentIndex = getComponentIndex();
    Assert.isTrue(componentIndex >= 0);
    //
    List<MethodInvocation> invocations = m_pane.getMethodInvocations(m_signature);
    for (MethodInvocation invocation : invocations) {
      if (isAtIndexInvocation(invocation, componentIndex)) {
        return invocation;
      }
    }
    // not found
    return null;
  }

  /**
   * @return the {@link StatementTarget} for adding "set*At()" {@link MethodInvocation}.
   */
  static StatementTarget getAtTarget(ComponentInfo component) throws Exception {
    Statement associationStatement = component.getAssociation().getStatement();
    return new StatementTarget(associationStatement, false);
  }

  /**
   * @return the index from given "set*At()" {@link MethodInvocation}.
   */
  static int getAtIndex(MethodInvocation invocation) {
    Expression indexExpression = (Expression) invocation.arguments().get(0);
    // we generate only NumberLiteral
    if (indexExpression instanceof NumberLiteral) {
      String token = ((NumberLiteral) indexExpression).getToken();
      return Integer.parseInt(token);
    }
    // if not NumberLiteral, then we parsed it, so we know its value
    return (Integer) JavaInfoEvaluationHelper.getValue(indexExpression);
  }

  /**
   * Sets the new value for index in given "set*At()" {@link MethodInvocation}.
   */
  static void setAtIndex(AstEditor editor, MethodInvocation invocation, int index) throws Exception {
    Expression indexExpression = (Expression) invocation.arguments().get(0);
    editor.replaceExpression(indexExpression, Integer.toString(index));
  }

  /**
   * @return <code>true</code> if given {@link MethodInvocation} has required index.
   */
  static boolean isAtIndexInvocation(MethodInvocation invocation, int componentIndex) {
    return getAtIndex(invocation) == componentIndex;
  }

  /**
   * @return the index of this {@link ComponentInfo} of {@link JTabbedPaneInfo}.
   */
  private int getComponentIndex() {
    return m_pane.getChildrenComponents().indexOf(m_component);
  }
}
