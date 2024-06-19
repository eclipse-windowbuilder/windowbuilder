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
package org.eclipse.wb.internal.core.model.property.accessor;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;

/**
 * The implementation of {@link ExpressionAccessor} for argument of
 * {@link SuperConstructorInvocation}.
 *
 * @author scheglov_ke
 * @coverage core.model.property.accessor
 */
public final class SuperConstructorAccessor extends ExpressionAccessor {
	private final SuperConstructorInvocation m_invocation;
	private final int m_index;
	private final String m_defaultSource;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SuperConstructorAccessor(SuperConstructorInvocation invocation,
			int index,
			String defaultSource) {
		m_invocation = invocation;
		m_index = index;
		m_defaultSource = defaultSource;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ExpressionAccessor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Expression getExpression(JavaInfo javaInfo) {
		return DomGenerics.arguments(m_invocation).get(m_index);
	}

	@Override
	public boolean setExpression(JavaInfo javaInfo, String source) throws Exception {
		// if given source is "null", use default source (but it also can be "null")
		final String newSource;
		if (source != null) {
			newSource = source;
		} else {
			newSource = m_defaultSource;
		}
		// if we have source to replace current, do this
		if (newSource != null) {
			final AstEditor editor = javaInfo.getEditor();
			final Expression oldExpression = getExpression(javaInfo);
			if (!editor.getSource(oldExpression).equals(source)) {
				ExecutionUtils.run(javaInfo, new RunnableEx() {
					@Override
					public void run() throws Exception {
						editor.replaceExpression(oldExpression, newSource);
					}
				});
				return true;
			}
		}
		// no changes
		return false;
	}
}
