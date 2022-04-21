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
package org.eclipse.wb.internal.swt.support;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.eval.IExpressionEvaluator;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.swt.widgets.Display;

/**
 * Implementation of {@link IExpressionEvaluator} for evaluating <code>new Display()</code> as
 * <code>Display.getCurrent()</code>. We need this because all SWT forms use same shared, already
 * existing instance of {@link Display}.
 *
 * @author scheglov_ke
 * @coverage swt.support
 */
public class DisplayEvaluator implements IExpressionEvaluator {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IExpressionEvaluator
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Object evaluate(EvaluationContext context,
      Expression expression,
      ITypeBinding typeBinding,
      String typeQualifiedName) throws Exception {
    if (expression instanceof ClassInstanceCreation
        && AstNodeUtils.getFullyQualifiedName(typeBinding, false).equals(
            "org.eclipse.swt.widgets.Display")) {
      return DisplaySupport.getCurrent();
    }
    // we don't understand given expression
    return AstEvaluationEngine.UNKNOWN;
  }
}