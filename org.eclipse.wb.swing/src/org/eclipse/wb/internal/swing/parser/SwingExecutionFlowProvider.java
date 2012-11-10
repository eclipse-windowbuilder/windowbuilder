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
package org.eclipse.wb.internal.swing.parser;

import org.eclipse.wb.internal.core.eval.ExecutionFlowProvider;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * {@link ExecutionFlowProvider} for Swing.
 * 
 * @author scheglov_ke
 * @coverage swing.parser
 */
public class SwingExecutionFlowProvider extends ExecutionFlowProvider {
  @Override
  public MethodDeclaration getDefaultConstructor(TypeDeclaration typeDeclaration) {
    ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(typeDeclaration);
    // find constructor without parameters
    if (AstNodeUtils.isSuccessorOf(typeBinding, "java.awt.Component")) {
      for (MethodDeclaration constructor : AstNodeUtils.getConstructors(typeDeclaration)) {
        if (constructor.parameters().isEmpty()) {
          return constructor;
        }
      }
    }
    // super
    return super.getDefaultConstructor(typeDeclaration);
  }

  @Override
  public boolean shouldVisit(AnonymousClassDeclaration anonymous) throws Exception {
    if (AstNodeUtils.isSuccessorOf(anonymous.resolveBinding(), "java.lang.Runnable")) {
      ClassInstanceCreation creation = (ClassInstanceCreation) anonymous.getParent();
      if (creation.getLocationInParent() == MethodInvocation.ARGUMENTS_PROPERTY) {
        MethodInvocation invocation = (MethodInvocation) creation.getParent();
        return AstNodeUtils.isMethodInvocation(
            invocation,
            "java.awt.EventQueue",
            "invokeLater(java.lang.Runnable)")
            || AstNodeUtils.isMethodInvocation(
                invocation,
                "javax.swing.SwingUtilities",
                "invokeLater(java.lang.Runnable)")
            || AstNodeUtils.isMethodInvocation(
                invocation,
                "java.awt.EventQueue",
                "invokeAndWait(java.lang.Runnable)")
            || AstNodeUtils.isMethodInvocation(
                invocation,
                "javax.swing.SwingUtilities",
                "invokeAndWait(java.lang.Runnable)");
      }
    }
    return super.shouldVisit(anonymous);
  }
}
