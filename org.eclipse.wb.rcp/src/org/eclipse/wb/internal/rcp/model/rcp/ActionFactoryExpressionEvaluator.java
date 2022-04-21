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
package org.eclipse.wb.internal.rcp.model.rcp;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.eval.IExpressionEvaluator;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.actions.ActionFactory;

/**
 * Implementation of {@link IExpressionEvaluator} for evaluating {@link Action}'s from
 * {@link ActionFactory}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public class ActionFactoryExpressionEvaluator implements IExpressionEvaluator {
  @Override
  public Object evaluate(EvaluationContext context,
      Expression expression,
      ITypeBinding typeBinding,
      String typeQualifiedName) throws Exception {
    if (expression instanceof QualifiedName) {
      QualifiedName qualifiedName = (QualifiedName) expression;
      if (AstNodeUtils.getFullyQualifiedName(qualifiedName.getQualifier(), false).equals(
          "org.eclipse.ui.actions.ActionFactory")) {
      }
    }
    // we don't understand given expression
    return AstEvaluationEngine.UNKNOWN;
  }
}
