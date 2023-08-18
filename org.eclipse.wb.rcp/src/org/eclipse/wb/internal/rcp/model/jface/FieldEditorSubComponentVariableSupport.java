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
package org.eclipse.wb.internal.rcp.model.jface;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.variable.AbstractNoNameVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Method;

/**
 * Implementation of {@link VariableSupport} for {@link ControlInfo} exposed from
 * {@link FieldEditorInfo} using <code>getXXXControl(Composite)</code>.
 * <p>
 * For example {@link StringFieldEditor#getTextControl(org.eclipse.swt.widgets.Composite)}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.jface
 */
public final class FieldEditorSubComponentVariableSupport extends AbstractNoNameVariableSupport {
	private final FieldEditorInfo m_fieldEditor;
	private final Method m_method;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FieldEditorSubComponentVariableSupport(JavaInfo javaInfo,
			FieldEditorInfo fieldEditor,
			Method method) {
		super(javaInfo);
		m_fieldEditor = fieldEditor;
		m_method = method;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		return "subComponent";
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean isValidStatementForChild(Statement statement) {
		return m_fieldEditor.getVariableSupport().isValidStatementForChild(statement);
	}

	@Override
	public String getTitle() throws Exception {
		return m_method.getName() + "()";
	}

	@Override
	public String getComponentName() {
		String methodName = m_method.getName();
		if (methodName.startsWith("get")) {
			methodName = methodName.substring("get".length());
		}
		return m_fieldEditor.getVariableSupport().getComponentName()
				+ StringUtils.capitalize(methodName);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Target
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public StatementTarget getStatementTarget() throws Exception {
		return m_fieldEditor.getVariableSupport().getStatementTarget();
	}

	@Override
	public StatementTarget getChildTarget() throws Exception {
		return JavaInfoUtils.getTarget(m_fieldEditor);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Expressions
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getReferenceExpression(NodeTarget target) throws Exception {
		String parentName = ensureParentComposite();
		String parentAccess = m_fieldEditor.getVariableSupport().getAccessExpression(target);
		return parentAccess + m_method.getName() + "(" + parentName + ")";
	}

	@Override
	public String getAccessExpression(NodeTarget target) throws Exception {
		return getReferenceExpression(target) + ".";
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Ensures that "parentComposite" is assigned into variable. This is important because in
	 * {@link FieldEditorPreferencePage#FLAT} mode each invocation of
	 * {@link FieldEditorPreferencePage#getFieldEditorParent()} creates new instance of
	 * {@link Composite}. But sub-component access methods require same instance as
	 * {@link FieldEditor} itself.
	 */
	private String ensureParentComposite() throws Exception {
		String parentName = null;
		// find "parentComposite" in ClassInstanceCreation
		{
			Expression parentExpression = getParentExpression(m_fieldEditor);
			if (parentExpression instanceof MethodInvocation) {
				Statement editorCreationStatement = AstNodeUtils.getEnclosingStatement(parentExpression);
				AstEditor astEditor = m_fieldEditor.getEditor();
				parentName =
						astEditor.getUniqueVariableName(
								editorCreationStatement.getStartPosition(),
								"composite",
								null);
				astEditor.addStatement("org.eclipse.swt.widgets.Composite "
						+ parentName
						+ " = "
						+ astEditor.getSource(parentExpression)
						+ ";", new StatementTarget(editorCreationStatement, true));
				astEditor.replaceExpression(parentExpression, parentName);
			} else if (parentExpression instanceof SimpleName parentSimpleName) {
				parentName = parentSimpleName.getIdentifier();
			}
		}
		// final check
		Assert.isNotNull(parentName, "Unable to find name of 'parentComposite' for %s.", m_fieldEditor);
		return parentName;
	}

	/**
	 * @return the {@link Expression} for "parentComposite" in creation of given
	 *         {@link FieldEditorInfo}.
	 */
	static Expression getParentExpression(FieldEditorInfo m_fieldEditor) {
		Expression parentExpression = null;
		// find "parentComposite" in ClassInstanceCreation
		Assert.instanceOf(ConstructorCreationSupport.class, m_fieldEditor.getCreationSupport());
		ConstructorCreationSupport creationSupport =
				(ConstructorCreationSupport) m_fieldEditor.getCreationSupport();
		ClassInstanceCreation creation = creationSupport.getCreation();
		for (ParameterDescription parameter : creationSupport.getDescription().getParameters()) {
			if (FieldEditorInfo.isParameterComposite(parameter)) {
				parentExpression = DomGenerics.arguments(creation).get(parameter.getIndex());
			}
		}
		// final check
		Assert.isNotNull(parentExpression, "Unable to find 'parentComposite' for %s.", m_fieldEditor);
		return parentExpression;
	}
}
