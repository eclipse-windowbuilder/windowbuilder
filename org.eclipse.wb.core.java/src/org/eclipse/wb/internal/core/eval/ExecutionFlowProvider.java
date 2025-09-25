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
package org.eclipse.wb.internal.core.eval;

import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.awt.EventQueue;

/**
 * This interface may be contributed by toolkit plugins to provide tweaks for execution flow.
 * <p>
 * For example in Swing we should visit {@link EventQueue#invokeAndWait(Runnable)}, even if
 * {@link Runnable} is {@link AnonymousClassDeclaration}.
 *
 * @author scheglov_ke
 * @coverage core.evaluation
 */
public class ExecutionFlowProvider {
	/**
	 * @return the constructor to start execution flow from, may be <code>null</code> if no any
	 *         constructor or just none of constructors can be chosen as default.
	 */
	public MethodDeclaration getDefaultConstructor(TypeDeclaration typeDeclaration) {
		return null;
	}

	public boolean shouldVisit(ASTNode node) {
		return false;
	}

	protected MethodInvocation getMethodInvocation(ASTNode node) {
		return switch (node) {
		case AnonymousClassDeclaration declaration -> {
			ClassInstanceCreation creation = (ClassInstanceCreation) declaration.getParent();
			if (creation.getLocationInParent() == MethodInvocation.ARGUMENTS_PROPERTY) {
				yield (MethodInvocation) creation.getParent();
			}
			yield null;
		}
		case LambdaExpression expression -> {
			if (expression.getLocationInParent() == MethodInvocation.ARGUMENTS_PROPERTY) {
				yield (MethodInvocation) expression.getParent();
			}
			yield null;
		}
		default -> null;
		};
	}

	protected ITypeBinding getTypeBinding(ASTNode node) {
		return switch (node) {
		case AnonymousClassDeclaration declaration -> AstNodeUtils.getTypeBinding(declaration);
		case LambdaExpression expression -> AstNodeUtils.getTypeBinding(expression);
		default -> null;
		};
	}
}
