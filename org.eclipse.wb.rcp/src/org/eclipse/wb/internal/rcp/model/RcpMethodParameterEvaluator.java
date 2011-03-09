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
import org.eclipse.wb.internal.core.model.creation.IMethodParameterEvaluator;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * {@link IMethodParameterEvaluator} for RCP objects.
 * 
 * @author scheglov_ke
 * @coverage rcp.model
 */
public final class RcpMethodParameterEvaluator implements IMethodParameterEvaluator {
  public static final IMethodParameterEvaluator INSTANCE = new RcpMethodParameterEvaluator();
  public static final FormToolkit FORM_TOOLKIT = new FormToolkit(Display.getDefault());

  ////////////////////////////////////////////////////////////////////////////
  //
  // IMethodParameterEvaluator
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object evaluateParameter(EvaluationContext context,
      MethodDeclaration methodDeclaration,
      String methodSignature,
      SingleVariableDeclaration parameter,
      int index) throws Exception {
    // FormToolkit parameter may be used before creating "this", passed in "super", so provide it
    if (AstNodeUtils.isSuccessorOf(parameter, "org.eclipse.ui.forms.widgets.FormToolkit")) {
      return FORM_TOOLKIT;
    }
    // provide empty, but not null, value for ISelection
    if (AstNodeUtils.isSuccessorOf(parameter, "org.eclipse.jface.viewers.ISelection")) {
      return new StructuredSelection();
    }
    // unknown parameter
    return AstEvaluationEngine.UNKNOWN;
  }
}