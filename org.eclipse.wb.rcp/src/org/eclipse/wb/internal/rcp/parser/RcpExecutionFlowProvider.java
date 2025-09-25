/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.parser;

import org.eclipse.wb.internal.core.eval.ExecutionFlowProvider;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.dom.ASTNode;
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
	public boolean shouldVisit(ASTNode node) {
		if (AstNodeUtils.isSuccessorOf(getTypeBinding(node), "java.lang.Runnable")) {
			MethodInvocation invocation = getMethodInvocation(node);
			if (invocation != null) {
				return AstNodeUtils.isMethodInvocation(
						invocation,
						"org.eclipse.core.databinding.observable.Realm",
						"runWithDefault(org.eclipse.core.databinding.observable.Realm,java.lang.Runnable)");
			}
		}
		// unknown pattern
		return super.shouldVisit(node);
	}
}
