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
package org.eclipse.wb.internal.core.gefTree.policy.dblclick;

import org.eclipse.wb.core.editor.IDesignPageSite;
import org.eclipse.wb.core.model.JavaInfo;

/**
 * An {@link DoubleClickLayoutEditPolicy} instance responsible to open the editor at widget creation
 * position.
 *
 * @author mitin_aa
 * @coverage core.gefTree.policy
 */
final class OpenEditorLayoutEditPolicy extends DoubleClickLayoutEditPolicy {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public OpenEditorLayoutEditPolicy(JavaInfo javaInfo) {
		super(javaInfo);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// DoubleClickLayoutEditPolicy
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void performDoubleClick() {
		IDesignPageSite site = IDesignPageSite.Helper.getSite(m_javaInfo);
		int position = m_javaInfo.getCreationSupport().getNode().getStartPosition();
		site.openSourcePosition(position);
	}
}
