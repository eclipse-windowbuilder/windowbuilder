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

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Implementations of this interface are used during evaluating of {@link Expression} by
 * {@link AstEvaluationEngine}.
 *
 * They are contributed using extension point, so it is possible to extend set of supported
 * expressions.
 *
 * @author scheglov_ke
 */
public interface IExpressionEvaluator {
  /**
   * @return value of given {@link Expression} or {@link AstEvaluationEngine#UNKNOWN}.
   */
  Object evaluate(EvaluationContext context,
      Expression expression,
      ITypeBinding typeBinding,
      String typeQualifiedName) throws Exception;
}
