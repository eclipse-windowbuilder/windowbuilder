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
package org.eclipse.wb.internal.rcp.model.forms;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.variable.AbstractNoNameVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.model.ModelMessages;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * {@link VariableSupport} for {@link FormToolkit} described by {@link FormToolkitAccess}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public final class FormToolkitVariableSupport extends AbstractNoNameVariableSupport {
	private final JavaInfo m_hostJavaInfo;
	private final FormToolkitAccess m_toolkitAccess;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FormToolkitVariableSupport(JavaInfo javaInfo,
			JavaInfo hostJavaInfo,
			FormToolkitAccess toolkitAccess) {
		super(javaInfo);
		m_hostJavaInfo = hostJavaInfo;
		m_toolkitAccess = toolkitAccess;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		return "toolkitAccess";
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getTitle() throws Exception {
		return "FormToolkit instance";
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Expressions
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getReferenceExpression(NodeTarget target) throws Exception {
		return m_toolkitAccess.getReferenceExpression();
	}

	@Override
	public String getAccessExpression(NodeTarget target) throws Exception {
		return getReferenceExpression(target) + ".";
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Target
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public StatementTarget getStatementTarget() throws Exception {
		MethodDeclaration configureMethod;
		{
			String configureMethodSignature =
					JavaInfoUtils.getParameter(m_hostJavaInfo, "FormToolkit.configureMethod");
			Assert.isNotNull(
					configureMethodSignature,
					ModelMessages.FormToolkitVariableSupport_noFormToolkit_configureMethod,
					m_hostJavaInfo);
			configureMethod =
					AstNodeUtils.getMethodBySignature(
							JavaInfoUtils.getTypeDeclaration(m_hostJavaInfo),
							configureMethodSignature);
		}
		// target = beginning of "configure" method
		return new StatementTarget(configureMethod, true);
	}
}
