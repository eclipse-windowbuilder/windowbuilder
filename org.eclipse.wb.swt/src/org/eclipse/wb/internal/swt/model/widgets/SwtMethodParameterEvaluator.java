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
package org.eclipse.wb.internal.swt.model.widgets;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.internal.core.model.creation.IMethodParameterEvaluator;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.support.ControlSupport;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.swt.widgets.Shell;

/**
 * {@link IMethodParameterEvaluator} for SWT objects.
 * 
 * @author scheglov_ke
 * @coverage swt.model.widgets
 */
public final class SwtMethodParameterEvaluator implements IMethodParameterEvaluator {
  public static final IMethodParameterEvaluator INSTANCE = new SwtMethodParameterEvaluator();

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
    ClassLoader classLoader = context.getClassLoader();
    String parameterName = parameter.getName().getIdentifier();
    ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(parameter);
    // parent
    if (AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.swt.widgets.Composite")) {
      if (index == 0 || parameterName.equals("parent")) {
        return getDefaultShell(parameter, classLoader);
      }
    }
    // Display
    if (index == 0) {
      if (AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.swt.widgets.Display")) {
        Class<?> c_Display = classLoader.loadClass("org.eclipse.swt.widgets.Display");
        return ReflectionUtils.invokeMethod(c_Display, "getDefault()");
      }
    }
    // unknown parameter
    return AstEvaluationEngine.UNKNOWN;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  static final String SHELL_KEY =
      "org.eclipse.wb.internal.swt.model.widgets.SWT_MethodParameterEvaluator Shell";

  /**
   * @return the {@link Shell} instance for given {@link ASTNode}, existing or new.
   */
  public static Object getDefaultShell(ASTNode node, ClassLoader classLoader) throws Exception {
    Object shell = node.getProperty(SHELL_KEY);
    if (shell == null || ControlSupport.isDisposed(shell)) {
      Class<?> c_Shell = classLoader.loadClass("org.eclipse.swt.widgets.Shell");
      shell = c_Shell.newInstance();
      node.setProperty(SHELL_KEY, shell);
    }
    return shell;
  }
}