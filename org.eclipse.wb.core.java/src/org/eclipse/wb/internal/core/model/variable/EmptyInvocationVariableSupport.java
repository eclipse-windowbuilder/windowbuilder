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
package org.eclipse.wb.internal.core.model.variable;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.AssociationUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;

import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;

/**
 * Specific sub-class of {@link EmptyVariableSupport} that adds new {@link JavaInfo} using just
 * {@link MethodInvocation} in {@link ExpressionStatement}. For example in can be used to set
 * <code>Layout</code> using single {@link MethodInvocation} without any variables:
 *
 * <pre>
 * 	composite.setLayout(new GridLayout(1, false));
 * </pre>
 *
 * @author scheglov_ke
 * @coverage core.model.variable
 */
public final class EmptyInvocationVariableSupport extends EmptyVariableSupport {
	private final String m_invocationSource;
	private final int m_argumentIndex;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public EmptyInvocationVariableSupport(JavaInfo javaInfo,
			String invocationSource,
			int argumentIndex) {
		super(javaInfo);
		m_invocationSource = invocationSource;
		m_argumentIndex = argumentIndex;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Adding
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String add_getVariableStatementSource(StatementTarget associationTarget) throws Exception {
		String source = m_invocationSource;
		// use child creation source
		{
			NodeTarget creationTarget = new NodeTarget(associationTarget);
			String childSource = m_javaInfo.getCreationSupport().add_getSource(creationTarget);
			source = source.replace("%child%", childSource);
		}
		// replace parent expressions
		source = AssociationUtils.replaceTemplates(m_javaInfo, source, associationTarget);
		{
			/*VariableSupport parentVariable = m_javaInfo.getParentJava().getVariableSupport();
      source = StringUtils.replace(source, "%parent%", parentVariable.getReferenceExpression(true));
      source = StringUtils.replace(source, "%parent%.", parentVariable.getAccessExpression(true));*/
		}
		// return as Statement source
		return source + ";";
	}

	@Override
	public void add_setVariableStatement(Statement statement) throws Exception {
		MethodInvocation invocation =
				(MethodInvocation) ((ExpressionStatement) statement).getExpression();
		add_setInitializer(DomGenerics.arguments(invocation).get(m_argumentIndex));
	}
}
