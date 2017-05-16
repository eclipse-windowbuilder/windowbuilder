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

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Sometimes we don't want to evaluate {@link ClassInstanceCreation} as is.
 *
 * @author scheglov_ke
 * @coverage core.evaluation
 */
public class InvocationEvaluatorInterceptor {
  /**
   * Evaluates {@link ClassInstanceCreation}.
   *
   * @return some value to use instead of real, or {@link AstEvaluationEngine#UNKNOWN} if
   *         {@link ClassInstanceCreation} should be evaluated as is.
   */
  public Object evaluate(EvaluationContext context,
      ClassInstanceCreation expression,
      ITypeBinding typeBinding,
      Class<?> clazz,
      Constructor<?> actualConstructor,
      Object[] arguments) throws Exception {
    return AstEvaluationEngine.UNKNOWN;
  }

  /**
   * Evaluates anonymous {@link ClassInstanceCreation}.
   *
   * @return some value to use instead of real, or {@link AstEvaluationEngine#UNKNOWN} if
   *         {@link ClassInstanceCreation} should be evaluated as is.
   */
  public Object evaluateAnonymous(EvaluationContext context,
      ClassInstanceCreation expression,
      ITypeBinding typeBinding,
      ITypeBinding typeBindingConcrete,
      IMethodBinding methodBinding,
      Object[] arguments) throws Exception {
    return AstEvaluationEngine.UNKNOWN;
  }

  /**
   * Evaluates {@link MethodInvocation}.
   *
   * @return some value to use instead of real, or {@link AstEvaluationEngine#UNKNOWN} if
   *         {@link MethodInvocation} should be evaluated as is.
   */
  public Object evaluate(EvaluationContext context,
      MethodInvocation invocation,
      IMethodBinding methodBinding,
      Class<?> clazz,
      Method method,
      Object[] argumentValues) {
    return AstEvaluationEngine.UNKNOWN;
  }

  /**
   * Allows to return different {@link Method} to use.
   * <p>
   * For example in GWT subclasses of <code>JavaScriptObject</code> are rewritten by GWT hosted mode
   * {@link ClassLoader} so that all their methods are moved into <code>ClassName$</code>. So, we
   * can not find {@link Method} using normal way.
   *
   * @return the {@link Method} of {@link Class}, with required signature, or <code>null</code> if
   *         default resolving should be used.
   */
  public Method resolveMethod(Class<?> clazz, String signature) throws Exception {
    return null;
  }

  /**
   * Sometimes we known that some pieces of code in Internet or samples are not compatible with
   * WindowBuilder and we want to show specific exception/message for them.
   *
   * @return the {@link Throwable} to use instead of original one, or <code>null</code> if original
   *         one should be used.
   */
  public Throwable rewriteException(Throwable e) {
    return null;
  }
}
