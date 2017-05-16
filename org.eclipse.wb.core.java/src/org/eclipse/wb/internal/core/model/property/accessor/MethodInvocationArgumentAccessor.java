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
package org.eclipse.wb.internal.core.model.property.accessor;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.List;

/**
 * The implementation of {@link ExpressionAccessor} for accessing argument of some
 * {@link MethodInvocation}.
 *
 * @author scheglov_ke
 * @coverage core.model.property.accessor
 */
public final class MethodInvocationArgumentAccessor extends ExpressionAccessor {
  private final Method m_method;
  private final String m_methodSignature;
  private final int m_index;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MethodInvocationArgumentAccessor(Method method, int index) {
    m_method = method;
    m_methodSignature = ReflectionUtils.getMethodSignature(m_method);
    m_index = index;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ExpressionAccessor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Expression getExpression(JavaInfo javaInfo) throws Exception {
    MethodInvocation invocation = getMethodInvocation(javaInfo);
    return getArgumentExpression(invocation);
  }

  @Override
  public boolean setExpression(final JavaInfo javaInfo, final String source) throws Exception {
    final List<String> defaultArguments = getMethodDescription(javaInfo).getDefaultArguments();
    // modify expression
    final MethodInvocation invocation = getMethodInvocation(javaInfo);
    if (invocation != null) {
      final AstEditor editor = javaInfo.getEditor();
      final Expression oldArgumentExpression = getArgumentExpression(invocation);
      final String argumentSource = source != null ? source : defaultArguments.get(m_index);
      if (!isSameSource(editor, oldArgumentExpression, argumentSource)) {
        ExecutionUtils.run(javaInfo, new RunnableEx() {
          public void run() throws Exception {
            editor.replaceExpression(oldArgumentExpression, argumentSource);
            if (isDefaultArguments(javaInfo, invocation)) {
              editor.removeEnclosingStatement(invocation);
            }
          }
        });
      }
    } else if (source != null) {
      ExecutionUtils.run(javaInfo, new RunnableEx() {
        public void run() throws Exception {
          String argumentsSource = getNewInvocationArguments();
          javaInfo.addMethodInvocation(m_methodSignature, argumentsSource);
        }

        private String getNewInvocationArguments() {
          List<String> arguments = Lists.newArrayList(defaultArguments);
          arguments.set(m_index, source);
          return StringUtils.join(arguments.iterator(), ", ");
        }
      });
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
   * @return the {@link MethodInvocation} of this setter for given {@link JavaInfo}.
   */
  private MethodInvocation getMethodInvocation(JavaInfo javaInfo) throws Exception {
    return javaInfo.getMethodInvocation(m_methodSignature);
  }

  /**
   * @return the {@link Expression} of given {@link MethodInvocation}.
   */
  private Expression getArgumentExpression(MethodInvocation invocation) {
    return invocation != null ? DomGenerics.arguments(invocation).get(m_index) : null;
  }

  private MethodDescription getMethodDescription(JavaInfo javaInfo) {
    MethodDescription methodDescription = javaInfo.getDescription().getMethod(m_methodSignature);
    Assert.isNotNull(
        methodDescription,
        "Can not find description for method with signature %s",
        m_methodSignature);
    return methodDescription;
  }

  private boolean isDefaultArguments(JavaInfo javaInfo, MethodInvocation invocation) {
    AstEditor editor = javaInfo.getEditor();
    boolean allDefault = true;
    List<ParameterDescription> parameters = getMethodDescription(javaInfo).getParameters();
    List<Expression> arguments = DomGenerics.arguments(invocation);
    for (ParameterDescription parameter : parameters) {
      int index = parameter.getIndex();
      Expression argument = arguments.get(index);
      allDefault &= isDefaultArgument(editor, argument, parameter);
    }
    return allDefault;
  }

  private static boolean isDefaultArgument(AstEditor editor,
      Expression argument,
      ParameterDescription parameter) {
    String defaultArgumentSource = parameter.getDefaultSource();
    return isSameSource(editor, argument, defaultArgumentSource);
  }

  private static boolean isSameSource(AstEditor editor, Expression expression, String expectedSource) {
    String currentArgumentSource = getNormalizedSource(editor, expression);
    return expectedSource.equals(currentArgumentSource);
  }

  private static String getNormalizedSource(AstEditor editor, Expression expression) {
    if (expression instanceof CastExpression) {
      CastExpression castExpression = (CastExpression) expression;
      if (castExpression.getExpression() instanceof NullLiteral) {
        String qualifiedTypeName = AstNodeUtils.getFullyQualifiedName(expression, false);
        return MessageFormat.format("({0}) null", qualifiedTypeName);
      }
    }
    return editor.getSource(expression);
  }
}
