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

/**
 * Wrapper method to handle listeners that are expressed via lambda
 * expressions.<br>
 * Example: <code>event -&gt; { ... }</code>
 */
public class LambdaMethodStub extends MethodDeclaration {
	private final Expression m_lambdaExpression;
	private final IMethodBinding m_lambdaMethod;
	private final Block m_lambdaBody;

	public LambdaMethodStub(Expression lambdaExpression, IMethodBinding lambdaMethod) {
		super(lambdaExpression.getAST());
		m_lambdaExpression = lambdaExpression;
		m_lambdaMethod = lambdaMethod;
		// stub, not relevant for listener hook
		m_lambdaBody = new Block(lambdaExpression.getAST());
		setSourceRange(m_lambdaExpression.getStartPosition(), m_lambdaExpression.getLength());
		setParent(m_lambdaExpression, m_lambdaExpression.getLocationInParent());
	}

	@Override
	void accept0(ASTVisitor visitor) {
		m_lambdaExpression.accept0(visitor);
	}

	@Override
	void appendDebugString(StringBuilder buffer) {
		m_lambdaExpression.appendDebugString(buffer);
	}

	@Override
	public IMethodBinding resolveBinding() {
		return m_lambdaMethod;
	}

	@Override
	public Block getBody() {
		return m_lambdaBody;
	}
}