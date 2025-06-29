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
package org.eclipse.wb.internal.rcp.gef.part.widgets;

import org.eclipse.wb.core.gef.part.AbstractComponentEditPart;
import org.eclipse.wb.core.gef.policy.selection.TopSelectionEditPolicy;
import org.eclipse.wb.internal.rcp.model.widgets.DialogInfo;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;

/**
 * {@link EditPart} for {@link DialogInfo}.
 *
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage rcp.gef.part
 */
public class DialogEditPart extends AbstractComponentEditPart {
	private final DialogInfo m_dialog;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DialogEditPart(DialogInfo dialog) {
		super(dialog);
		m_dialog = dialog;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Policies
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refreshEditPolicies() {
		super.refreshEditPolicies();
		// injecting into main {@link ShellEditPart} a {@link TopSelectionEditPolicy}.
		for (EditPart child : getChildren()) {
			if (child.getModel() == m_dialog.getShellInfo()) {
				child.installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, new TopSelectionEditPolicy(m_dialog));
			}
		}
	}
}
