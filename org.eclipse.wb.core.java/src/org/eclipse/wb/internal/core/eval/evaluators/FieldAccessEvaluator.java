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
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.lang.reflect.Field;

/**
 * Implementation of {@link IExpressionEvaluator} for {@link FieldAccess}.
 *
 * @author scheglov_ke
 * @coverage core.evaluation
 */
public final class FieldAccessEvaluator implements IExpressionEvaluator {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IExpressionEvaluator
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object evaluate(EvaluationContext context,
      Expression expression,
      ITypeBinding typeBinding,
      String typeQualifiedName) throws Exception {
    if (expression instanceof FieldAccess) {
      FieldAccess fieldAccess = (FieldAccess) expression;
      String fieldName = fieldAccess.getName().getIdentifier();
      // check "this" expression
      Expression fieldAccessExpression = fieldAccess.getExpression();
      if (fieldAccessExpression instanceof ThisExpression) {
        // prepare field declaration
        TypeDeclaration typeDeclaration = AstNodeUtils.getEnclosingType(expression);
        VariableDeclarationFragment fragment =
            AstNodeUtils.getFieldFragmentByName(typeDeclaration, fieldName);
        Assert.isNotNull(fragment);
        // calculate field value
        Expression fieldInitializer = fragment.getInitializer();
        if (fieldInitializer == null) {
          FieldDeclaration fieldDeclaration =
              AstNodeUtils.getEnclosingNode(fragment, FieldDeclaration.class);
          String className = AstNodeUtils.getFullyQualifiedName(fieldDeclaration.getType(), true);
          return ReflectionUtils.getDefaultValue(className);
        }
        return AstEvaluationEngine.evaluate(context, fieldInitializer);
      }
      // prepare expression
      String expressionClassName = AstNodeUtils.getFullyQualifiedName(fieldAccessExpression, true);
      Class<?> expressionClass = context.getClassLoader().loadClass(expressionClassName);
      Object expressionValue = AstEvaluationEngine.evaluate(context, fieldAccessExpression);
      // prepare field
      Field field = ReflectionUtils.getFieldByName(expressionClass, fieldName);
      Assert.isNotNull(field);
      // return static value
      return field.get(expressionValue);
    }
    // we don't understand given expression
    return AstEvaluationEngine.UNKNOWN;
  }
}
