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
package org.eclipse.wb.core.eval;

import org.eclipse.wb.internal.core.eval.evaluators.InvocationEvaluator;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.exception.ICoreExceptionConstants;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;

import net.sf.cglib.proxy.Callback;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.List;

/**
 * The engine for interpreting AST {@link Expression}'s into values.
 *
 * @author scheglov_ke
 * @coverage core.evaluation
 */
public final class AstEvaluationEngine {
  /**
   * The value that means that {@link AstEvaluationEngine} can not evaluate given expression.
   */
  public static final Object UNKNOWN = new Object();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return value of given {@link Expression}.
   * @throws DesignerException
   *           if given expression can not be evaluated.
   */
  public static Object evaluate(final EvaluationContext context, final Expression expression)
      throws Exception {
    try {
      return evaluate0(context, expression);
    } catch (final Throwable e) {
      Object result = ExecutionUtils.runObjectLog(new RunnableObjectEx<Object>() {
        public Object runObject() throws Exception {
          return context.evaluationFailed(expression, e);
        }
      }, UNKNOWN);
      if (result != UNKNOWN) {
        return result;
      }
      throw new Error(context.getSource(expression), e);
    }
  }

  /**
   * Evaluates given {@link ClassInstanceCreation} directly, without asking other possible
   * registered {@link IExpressionEvaluator}.
   */
  public static Object createClassInstanceCreationDirectly(EvaluationContext context,
      ClassInstanceCreation creation) throws Exception {
    ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(creation);
    String typeQualifiedName = AstNodeUtils.getFullyQualifiedName(typeBinding, true);
    return new InvocationEvaluator().evaluate(context, creation, typeBinding, typeQualifiedName);
  }

  /**
   * @return the instance of anonymous {@link ClassInstanceCreation}, where abstract methods are
   *         implemented to return default value.
   */
  public static Object createAnonymousInstance(EvaluationContext context,
      IMethodBinding methodBinding,
      Object[] argumentValues) throws Exception {
    return createAnonymousInstance(
        context,
        methodBinding,
        argumentValues,
        DefaultMethodInterceptor.INSTANCE);
  }

  /**
   * @param methodBinding
   *          the {@link IMethodBinding} of constructor.
   *
   * @return the instance of anonymous {@link ClassInstanceCreation}, intercepting methods using
   *         given {@link Callback}.
   */
  public static Object createAnonymousInstance(EvaluationContext context,
      IMethodBinding methodBinding,
      Object[] argumentValues,
      Callback callback) throws Exception {
    return InvocationEvaluator.createAnonymousInstance(
        context,
        methodBinding,
        argumentValues,
        callback);
  }

  /**
   * @return stack trace for exception in user code.
   */
  public static String getUserStackTrace(Throwable e) {
    e = DesignerExceptionUtils.getRootCause(e);
    String stackTrace = ExceptionUtils.getStackTrace(e);
    stackTrace = StringUtils.substringBefore(stackTrace, "at org.eclipse.wb.");
    stackTrace = StringUtils.substringBefore(stackTrace, "at sun.reflect.");
    stackTrace = StringUtils.stripEnd(stackTrace, null);
    return stackTrace;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  private static Object evaluate0(EvaluationContext context, Expression expression)
      throws Exception {
    // try to evaluate "pure" expression
    {
      Object value = context.evaluate(expression);
      if (value != UNKNOWN) {
        context.evaluationSuccessful(expression, value);
        return value;
      }
    }
    // simple expression
    if (expression instanceof NullLiteral) {
      context.evaluationSuccessful(expression, null);
      return null;
    }
    if (expression instanceof ParenthesizedExpression) {
      ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) expression;
      Object value = evaluate(context, parenthesizedExpression.getExpression());
      context.evaluationSuccessful(expression, value);
      return value;
    }
    // use expression evaluators
    {
      // prepare type binding/name
      ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(expression);
      String typeQualifiedName = AstNodeUtils.getFullyQualifiedName(typeBinding, false);
      // temporary evaluators
      for (IExpressionEvaluator evaluator : context.getEvaluators()) {
        Object value = evaluator.evaluate(context, expression, typeBinding, typeQualifiedName);
        if (value != UNKNOWN) {
          context.evaluationSuccessful(expression, value);
          return value;
        }
      }
      // external evaluators
      {
        List<IExpressionEvaluator> evaluators =
            ExternalFactoriesHelper.getElementsInstances(
                IExpressionEvaluator.class,
                "org.eclipse.wb.core.expressionEvaluators",
                "evaluator");
        for (IExpressionEvaluator evaluator : evaluators) {
          Object value = evaluator.evaluate(context, expression, typeBinding, typeQualifiedName);
          if (value != UNKNOWN) {
            context.evaluationSuccessful(expression, value);
            return value;
          }
        }
      }
    }
    // unknown expression
    throw new DesignerException(ICoreExceptionConstants.EVAL_UNKNOWN_EXPRESSION_TYPE,
        context.getSource(expression));
  }
}
