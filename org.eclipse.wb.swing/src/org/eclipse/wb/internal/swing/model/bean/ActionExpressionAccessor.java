/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swing.model.bean;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.description.ConstructorDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;

import java.util.List;

import javax.swing.Action;

/**
 * Implementation of {@link ExpressionAccessor} for accessing {@link Expression}'s from
 * {@link Action#putValue(String, Object)} in some constructor.
 *
 * @author scheglov_ke
 * @coverage swing.model
 */
public final class ActionExpressionAccessor extends ExpressionAccessor {
	private final IActionSupport m_actionInfo;
	private final String m_keyName;
	private final String m_keyValue;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ActionExpressionAccessor(IActionSupport actionInfo, String keyName) throws Exception {
		Assert.isTrue(!actionInfo.getInitializationBlocks().isEmpty());
		m_actionInfo = actionInfo;
		m_keyName = keyName;
		m_keyValue = (String) ReflectionUtils.getFieldObject(Action.class, m_keyName);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ExpressionAccessor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Expression getExpression(JavaInfo javaInfo) {
		for (Block block : m_actionInfo.getInitializationBlocks()) {
			for (Statement statement : DomGenerics.statements(block)) {
				Expression expression = null;
				if (statement instanceof SuperConstructorInvocation invocation) {
					expression = getExpression_SuperConstructorInvocation(invocation);
				}
				if (statement instanceof ExpressionStatement expressionStatement) {
					expression = getExpression_ExpressionStatement(expressionStatement);
				}
				// check for result
				if (expression != null) {
					return expression;
				}
			}
		}
		// no Expression
		return null;
	}

	private Expression getExpression_SuperConstructorInvocation(SuperConstructorInvocation invocation) {
		// prepare description
		ConstructorDescription constructor = m_actionInfo.getConstructorDescription();
		// analyze arguments
		if (constructor != null) {
			List<Expression> arguments = DomGenerics.arguments(invocation);
			for (ParameterDescription parameter : constructor.getParameters()) {
				String key = parameter.getTag("actionKey");
				if (m_keyValue.equals(key)) {
					return arguments.get(parameter.getIndex());
				}
			}
		}
		// no expression
		return null;
	}

	private Expression getExpression_ExpressionStatement(ExpressionStatement expressionStatement) {
		if (expressionStatement.getExpression() instanceof MethodInvocation) {
			MethodInvocation invocation = (MethodInvocation) expressionStatement.getExpression();
			if (invocation.getExpression() == null
					&& AstNodeUtils.getMethodSignature(invocation).equals(
							"putValue(java.lang.String,java.lang.Object)")) {
				List<Expression> arguments = DomGenerics.arguments(invocation);
				Expression keyExpression = arguments.get(0);
				Expression valueExpression = arguments.get(1);
				if (m_keyValue.equals(getKeyValue(keyExpression))) {
					return valueExpression;
				}
			}
		}
		return null;
	}

	@Override
	public boolean setExpression(final JavaInfo javaInfo, final String source) throws Exception {
		final Expression expression = getExpression(javaInfo);
		if (expression != null) {
			final AstEditor editor = javaInfo.getEditor();
			if (source == null) {
				if (expression.getLocationInParent() == MethodInvocation.ARGUMENTS_PROPERTY) {
					ExecutionUtils.run(javaInfo, new RunnableEx() {
						@Override
						public void run() throws Exception {
							editor.removeEnclosingStatement(expression);
						}
					});
				}
			} else if (!editor.getSource(expression).equals(source)) {
				ExecutionUtils.run(javaInfo, new RunnableEx() {
					@Override
					public void run() throws Exception {
						editor.replaceExpression(expression, source);
					}
				});
			}
		} else if (source != null) {
			ExecutionUtils.run(javaInfo, new RunnableEx() {
				@Override
				public void run() throws Exception {
					String statementSource = "putValue(" + m_keyName + ", " + source + ");";
					javaInfo.getEditor().addStatement(statementSource, getTarget());
				}

				private StatementTarget getTarget() throws Exception {
					// if first statement in constructor is "super", add after it
					Block block = m_actionInfo.getInitializationBlocks().get(0);
					List<Statement> statements = DomGenerics.statements(block);
					if (!statements.isEmpty()) {
						Statement statement = statements.get(0);
						if (statement instanceof SuperConstructorInvocation) {
							return new StatementTarget(statement, false);
						}
					}
					// in other case add as first statement in constructor
					return new StatementTarget(block, true);
				}
			});
		}
		// success
		return true;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @param keyExpression
	 *          the {@link Expression} for {@link Action#putValue(String, Object)} key. Only
	 *          {@link SimpleName} and {@link StringLiteral} are supported.
	 *
	 * @return the value of "key" {@link Expression}.
	 */
	static String getKeyValue(Expression keyExpression) {
		return (String) JavaInfoEvaluationHelper.getValue(keyExpression);
		/*if (keyExpression instanceof SimpleName) {
    	String keyName = ((SimpleName) keyExpression).getIdentifier();
    	return (String) ReflectionUtils.getFieldObject(Action.class, keyName);
    } else {
    	return ((StringLiteral) keyExpression).getLiteralValue();
    }*/
	}
}
