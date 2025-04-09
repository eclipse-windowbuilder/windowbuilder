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
package org.eclipse.wb.core.gefTree.part;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.tree.TreeEditPart;
import org.eclipse.wb.internal.core.gefTree.policy.dblclick.DoubleClickLayoutEditPolicy;

/**
 * {@link TreeEditPart} for {@link JavaInfo}.
 *
 * @author mitin_aa
 * @coverage core.gefTree
 */
public class JavaEditPart extends ObjectEditPart {
	private final JavaInfo m_javaInfo;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public JavaEditPart(JavaInfo javaInfo) {
		super(javaInfo);
		m_javaInfo = javaInfo;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Policies
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		DoubleClickLayoutEditPolicy.install(this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link JavaInfo} model for this {@link JavaInfoEditPart}.
	 */
	public final JavaInfo getJavaInfo() {
		return m_javaInfo;
	}
}
