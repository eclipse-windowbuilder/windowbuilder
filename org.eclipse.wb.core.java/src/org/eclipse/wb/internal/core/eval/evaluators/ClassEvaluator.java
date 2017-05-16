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
package org.eclipse.wb.internal.core.eval.evaluators;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.eval.IExpressionEvaluator;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;

/**
 * Implementation of {@link IExpressionEvaluator} for {@link TypeLiteral} and {@link #getClass()}.
 *
 * @author scheglov_ke
 * @coverage core.evaluation
 */
public final class ClassEvaluator implements IExpressionEvaluator {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IExpressionEvaluator
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object evaluate(EvaluationContext context,
      Expression expression,
      ITypeBinding typeBinding,
      String typeQualifiedName) throws Exception {
    // TypeLiteral
    if (expression instanceof TypeLiteral) {
      TypeLiteral typeLiteral = (TypeLiteral) expression;
      ITypeBinding binding = AstNodeUtils.getTypeBinding(typeLiteral.getType());
      return loadClass(context, binding);
    }
    // getClass()
    if (expression instanceof MethodInvocation) {
      MethodInvocation invocation = (MethodInvocation) expression;
      if (isThisInvocation(invocation) && AstNodeUtils.isMethodInvocation(invocation, "getClass()")) {
        TypeDeclaration typeDeclaration = AstNodeUtils.getEnclosingType(invocation);
        ITypeBinding binding = AstNodeUtils.getTypeBinding(typeDeclaration);
        return loadClass(context, binding);
      }
    }
    // we don't understand given expression
    return AstEvaluationEngine.UNKNOWN;
  }

  /**
   * @return <code>true</code> if target of given {@link MethodInvocation} is "this" class.
   */
  private static boolean isThisInvocation(MethodInvocation invocation) {
    Expression expression = invocation.getExpression();
    return expression == null || expression instanceof ThisExpression;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static Object loadClass(EvaluationContext context, ITypeBinding binding) throws Exception {
    ClassLoader classLoader = context.getClassLoader();
    String className = AstNodeUtils.getFullyQualifiedName(binding, true);
    return ReflectionUtils.getClassByName(classLoader, className);
  }
}
