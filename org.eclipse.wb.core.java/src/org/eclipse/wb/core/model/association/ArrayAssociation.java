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
package org.eclipse.wb.core.model.association;

import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link Association} for {@link ArrayCreation} item.
 *
 * @author sablin_aa
 * @coverage core.model.association
 */
public final class ArrayAssociation extends Association {
	private final ArrayCreation m_arrayCreation;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public ArrayAssociation(ArrayCreation arrayCreation) {
		m_arrayCreation = arrayCreation;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the underlying {@link ArrayCreation}.
	 */
	public ArrayCreation getCreation() {
		return m_arrayCreation;
	}

	@Override
	public Statement getStatement() {
		return AstNodeUtils.getEnclosingStatement(m_arrayCreation);
	}

	@Override
	public String getSource() {
		return m_editor.getSource(m_arrayCreation);
	}

	@Override
	public boolean remove() throws Exception {
		ArrayInitializer initializer = m_arrayCreation.getInitializer();
		List<Expression> expressionsToRemove = new ArrayList<>();
		// locate items
		{
			@SuppressWarnings("unchecked")
			List<Expression> expressions = initializer.expressions();
			for (Expression expression : expressions) {
				if (m_javaInfo.isRepresentedBy(expression)) {
					expressionsToRemove.add(expression);
				}
			}
		}
		// remove items
		for (Expression expression : expressionsToRemove) {
			int elementIndex = initializer.expressions().indexOf(expression);
			m_editor.removeArrayElement(initializer, elementIndex);
		}
		// yes, association removed
		return super.remove();
	}
}
