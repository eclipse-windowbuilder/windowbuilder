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
package org.eclipse.wb.internal.swing.databinding.model.bindings;

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.parser.AstModelSupport;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lobas_av
 * @coverage bindings.swing.model.bindings
 */
public final class ColumnBindingModelSupport extends AstModelSupport {
	private final List<Expression> m_invocations = new ArrayList<>();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ColumnBindingModelSupport(AstObjectInfo model, MethodInvocation invocation) {
		super(model);
		addInvocation(invocation);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public void addInvocation(MethodInvocation invocation) {
		m_invocations.add(invocation);
		calculateNameReference(invocation);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IModelSupport
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean isRepresentedBy(Expression expression) throws Exception {
		for (Expression invocation : m_invocations) {
			if (invocation == expression) {
				return true;
			}
		}
		return isRepresentedOverReference(expression);
	}
}