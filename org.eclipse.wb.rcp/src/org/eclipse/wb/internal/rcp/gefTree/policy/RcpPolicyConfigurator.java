/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
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
package org.eclipse.wb.internal.rcp.gefTree.policy;

import org.eclipse.wb.core.gef.IEditPartConfigurator;
import org.eclipse.wb.gef.tree.DesignTreeEditPart;
import org.eclipse.wb.internal.rcp.gef.policy.jface.action.ActionDropRequestProcessor;
import org.eclipse.wb.internal.rcp.gefTree.policy.jface.ControlDecorationDropLayoutEditPolicy;
import org.eclipse.wb.internal.rcp.model.jface.action.MenuManagerInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;

/**
 * Configures RCP related {@link EditPart}'s.
 *
 * @author scheglov_ke
 * @coverage rcp.gefTree.policy
 */
public final class RcpPolicyConfigurator implements IEditPartConfigurator {
	@Override
	public void configure(EditPart context, EditPart editPart) {
		// allow drop Action on MenuManager
		if (editPart.getModel() instanceof MenuManagerInfo) {
			((DesignTreeEditPart) editPart).addRequestProcessor(ActionDropRequestProcessor.INSTANCE);
		}
		// allow drop ControlDecoration on ControlInfo
		if (editPart.getModel() instanceof ControlInfo control) {
			EditPolicy editPolicy = new ControlDecorationDropLayoutEditPolicy(control);
			editPart.installEditPolicy(editPolicy.getClass(), editPolicy);
		}
	}
}
