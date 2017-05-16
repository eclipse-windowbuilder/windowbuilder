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
import org.eclipse.wb.core.eval.ExecutionFlowUtils2;
import org.eclipse.wb.core.eval.ExpressionValue;
import org.eclipse.wb.core.eval.IExpressionEvaluator;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.ICoreExceptionConstants;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.lang.reflect.Field;

/**
 * Implementation of {@link IExpressionEvaluator} for {@link QualifiedName}.
 *
 * @author scheglov_ke
 * @coverage core.evaluation
 */
public final class SimpleNameEvaluator implements IExpressionEvaluator {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IExpressionEvaluator
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object evaluate(EvaluationContext context,
      Expression expression,
      ITypeBinding typeBinding,
      String typeQualifiedName) throws Exception {
    if (expression instanceof SimpleName) {
      SimpleName simpleName = (SimpleName) expression;
      // try to find field in super Class or implemented interface
      {
        Object value = evaluateAsConstant(context, simpleName);
        if (value != AstEvaluationEngine.UNKNOWN) {
          return value;
        }
      }
      // try to find assignment
      return evaluateAsAssignment(context, simpleName);
    }
    // we don't understand given expression
    return AstEvaluationEngine.UNKNOWN;
  }

  private Object evaluateAsConstant(EvaluationContext context, SimpleName simpleName)
      throws Exception {
    IVariableBinding variableBinding = AstNodeUtils.getVariableBinding(simpleName);
    if (variableBinding != null) {
      ITypeBinding declaringClassBinding = variableBinding.getDeclaringClass();
      if (declaringClassBinding != null) {
        String declaringClassName = AstNodeUtils.getFullyQualifiedName(declaringClassBinding, true);
        // check for local TypeDeclaration, ignore it
        if (isNameOfTopTypeDeclaration(simpleName, declaringClassName)) {
          return AstEvaluationEngine.UNKNOWN;
        }
        // prepare declaring class/interface
        Class<?> declaringClass = context.getClassLoader().loadClass(declaringClassName);
        // check Field
        Field field = ReflectionUtils.getFieldByName(declaringClass, simpleName.getIdentifier());
        if (field != null && java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
          return field.get(null);
        }
      }
    }
    return AstEvaluationEngine.UNKNOWN;
  }

  private static boolean isNameOfTopTypeDeclaration(ASTNode node, String typeName) {
    TypeDeclaration topTypeDeclaration = AstNodeUtils.getEnclosingTypeTop(node);
    return AstNodeUtils.getFullyQualifiedName(topTypeDeclaration, false).equals(typeName);
  }

  private Object evaluateAsAssignment(EvaluationContext context, SimpleName simpleName)
      throws Exception {
    ExpressionValue value = ExecutionFlowUtils2.getValue(context.getFlowDescription(), simpleName);
    if (value != null) {
      Expression expression = value.getExpression();
      // field declaration without initializer
      if (expression.getLocationInParent() == VariableDeclarationFragment.NAME_PROPERTY
          && expression.getParent().getLocationInParent() == FieldDeclaration.FRAGMENTS_PROPERTY) {
        return null;
      }
      // parameter of method without invocation
      if (expression.getLocationInParent() == SingleVariableDeclaration.NAME_PROPERTY) {
        SingleVariableDeclaration parameter = (SingleVariableDeclaration) expression.getParent();
        MethodDeclaration methodDeclaration = AstNodeUtils.getEnclosingMethod(parameter);
        return context.evaluateUnknownParameter(methodDeclaration, parameter);
      }
      // normal Expression
      return AstEvaluationEngine.evaluate(context, expression);
    }
    // no value
    throw new DesignerException(ICoreExceptionConstants.EVAL_NO_SIMPLE_NAME_FOUND,
        simpleName.getIdentifier());
  }
}
