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
package org.eclipse.wb.internal.rcp.model;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.eval.InvocationEvaluatorInterceptor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * For custom RCP objects.
 *
 * @author scheglov_ke
 * @coverage rcp.model
 */
public final class RcpInvocationEvaluatorInterceptor extends InvocationEvaluatorInterceptor {
  @Override
  public Object evaluateAnonymous(EvaluationContext context,
      ClassInstanceCreation expression,
      ITypeBinding typeBinding,
      ITypeBinding typeBindingConcrete,
      IMethodBinding methodBinding,
      Object[] arguments) throws Exception {
    if (AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.jface.action.Action")) {
      return AstEvaluationEngine.createAnonymousInstance(context, methodBinding, arguments);
    }
    return AstEvaluationEngine.UNKNOWN;
  }
}
