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
package org.eclipse.wb.internal.rcp.parser;

import org.eclipse.wb.core.eval.ExecutionFlowProvider;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.List;

/**
 * {@link ExecutionFlowProvider} for RCP.
 * 
 * @author scheglov_ke
 * @coverage rcp.parser
 */
public class RcpExecutionFlowProvider extends ExecutionFlowProvider {
  @Override
  public MethodDeclaration getDefaultConstructor(TypeDeclaration typeDeclaration) {
    ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(typeDeclaration);
    List<MethodDeclaration> constructors = AstNodeUtils.getConstructors(typeDeclaration);
    // Forms API FormPage+ <init>(*org.eclipse.ui.forms.editor.FormEditor*)
    if (AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.ui.forms.editor.FormPage")) {
      for (MethodDeclaration constructor : constructors) {
        if (AstNodeUtils.getMethodSignature(constructor).contains(
            "org.eclipse.ui.forms.editor.FormEditor")) {
          return constructor;
        }
      }
    }
    // SWT Dialog+ <init>(Shell,style)
    if (AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.swt.widgets.Dialog")) {
      for (MethodDeclaration constructor : constructors) {
        if (AstNodeUtils.getMethodSignature(constructor).equals(
            "<init>(org.eclipse.swt.widgets.Shell,int)")) {
          return constructor;
        }
      }
    }
    // Shell+ <init>()
    if (AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.swt.widgets.Shell")) {
      for (MethodDeclaration constructor : constructors) {
        if (constructor.parameters().isEmpty()) {
          return constructor;
        }
      }
    }
    // Composite+ <init>(Composite,style)
    if (AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.swt.widgets.Composite")) {
      for (MethodDeclaration constructor : constructors) {
        if (AstNodeUtils.getMethodSignature(constructor).equals(
            "<init>(org.eclipse.swt.widgets.Composite,int)")) {
          return constructor;
        }
      }
    }
    // super
    return super.getDefaultConstructor(typeDeclaration);
  }

  @Override
  public boolean shouldVisit(AnonymousClassDeclaration anonymous) throws Exception {
    // Realm.runWithDefault()
    if (AstNodeUtils.isSuccessorOf(anonymous, "java.lang.Runnable")) {
      ClassInstanceCreation creation = (ClassInstanceCreation) anonymous.getParent();
      if (creation.getLocationInParent() == MethodInvocation.ARGUMENTS_PROPERTY) {
        MethodInvocation invocation = (MethodInvocation) creation.getParent();
        return AstNodeUtils.isMethodInvocation(
            invocation,
            "org.eclipse.core.databinding.observable.Realm",
            "runWithDefault(org.eclipse.core.databinding.observable.Realm,java.lang.Runnable)");
      }
    }
    // unknown pattern
    return false;
  }
}
