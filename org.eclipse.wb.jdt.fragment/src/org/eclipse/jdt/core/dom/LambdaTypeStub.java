/*******************************************************************************
 * Copyright (c) 2023 Patrick Ziegler and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

import java.util.List;

/**
 * Wrapper class to handle the anonymous types that are implicitly created by
 * lambda expressions.
 */
public class LambdaTypeStub extends TypeDeclaration {
	private final Expression m_expression;
	private final MethodDeclaration m_method;

	public LambdaTypeStub(Expression expression, IMethodBinding method) {
		this(expression, new LambdaMethodStub(expression, method));
	}

	private LambdaTypeStub(Expression expression, MethodDeclaration lambdaMethod) {
		super(expression.getAST());
		m_expression = expression;
		m_method = lambdaMethod;
		setSourceRange(expression.getStartPosition(), expression.getLength());
		setParent(expression, expression.getLocationInParent());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// TypeDeclaration
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	void accept0(ASTVisitor visitor) {
		m_expression.accept0(visitor);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public List bodyDeclarations() {
		return List.of(m_method);
	}

	@Override
	void appendDebugString(StringBuilder buffer) {
		m_expression.appendDebugString(buffer);
	}

	@Override
	ITypeBinding internalResolveBinding() {
		return m_expression.resolveTypeBinding();
	}

}
