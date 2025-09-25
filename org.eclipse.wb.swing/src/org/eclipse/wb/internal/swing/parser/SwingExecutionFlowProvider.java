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
package org.eclipse.wb.internal.swing.parser;

import org.eclipse.wb.internal.core.eval.ExecutionFlowProvider;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.dom.ASTNode;
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
	public boolean shouldVisit(ASTNode node) {
		if (AstNodeUtils.isSuccessorOf(getTypeBinding(node), "java.lang.Runnable")) {
			MethodInvocation invocation = getMethodInvocation(node);
			if (invocation != null) {
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
		return super.shouldVisit(node);
	}
}
