/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.internal.core.databinding.model;

import org.eclipse.wb.internal.core.databinding.parser.IModelResolver;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.List;

/**
 * Abstract model for any bindings model object. It has some presentation in AST.
 *
 * @author lobas_av
 * @coverage bindings.model
 */
public abstract class AstObjectInfo {
	private String m_variable;

	////////////////////////////////////////////////////////////////////////////
	//
	// Variable
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the name of variable to which this object is assigned first time or <code>null</code>
	 *         if there are no any variable.
	 */
	public String getVariableIdentifier() throws Exception {
		return m_variable;
	}

	/**
	 * Sets the name of variable of object.
	 */
	public void setVariableIdentifier(String variable) {
		m_variable = variable;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Parser
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Parse given {@link MethodInvocation} for this object (this object is expression for invocation)
	 * and create {@link AstObjectInfo} model otherwise return <code>null</code>.
	 */
	public AstObjectInfo parseExpression(AstEditor editor,
			String signature,
			MethodInvocation invocation,
			Expression[] arguments,
			IModelResolver resolver,
			IDatabindingsProvider provider) throws Exception {
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the text for visual presentation of this object.
	 */
	public String getPresentationText() throws Exception {
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Code generation
	//
	////////////////////////////////////////////////////////////////////////////
	public boolean addSourceCodeSeparator() {
		return true;
	}

	/**
	 * Generate source code association with this object and add to <code>lines</code>.
	 */
	public void addSourceCode(List<String> lines, CodeGenerationSupport generationSupport)
			throws Exception {
		throw new UnsupportedOperationException();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Visiting
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Visits this {@link AstObjectInfo} using given {@link AstObjectInfoVisitor}. This method must be
	 * implemented in all subclasses that contains specific children.
	 */
	public void accept(AstObjectInfoVisitor visitor) throws Exception {
		visitor.visit(this);
	}
}