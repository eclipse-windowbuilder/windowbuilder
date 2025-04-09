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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.AbstractWrapper;

import org.eclipse.jdt.core.dom.Statement;

/**
 * {@link Association} for {@link JavaInfo} wrapped in {@link AbstractWrapper}. It delegates all
 * operations to the {@link Association} of {@link AbstractWrapper}.
 *
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage core.model.association
 */
public class WrappedObjectAssociation extends Association {
	private final AbstractWrapper m_wrapper;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public WrappedObjectAssociation(AbstractWrapper wrapper) {
		m_wrapper = wrapper;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Statement getStatement() {
		return m_wrapper.getWrapperInfo().getAssociation().getStatement();
	}

	@Override
	public String getSource() {
		return m_wrapper.getWrapperInfo().getAssociation().getSource();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Operations
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean remove() throws Exception {
		// Control association with Viewer can not be broken
		return false;
	}

	@Override
	public void setParent(JavaInfo parent) throws Exception {
		m_wrapper.getWrapperInfo().getAssociation().setParent(parent);
	}
}
