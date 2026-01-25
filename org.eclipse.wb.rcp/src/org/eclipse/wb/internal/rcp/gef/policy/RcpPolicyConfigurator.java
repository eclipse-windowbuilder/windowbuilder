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
package org.eclipse.wb.internal.rcp.gef.policy;

import org.eclipse.wb.core.gef.IEditPartConfigurator;
import org.eclipse.wb.core.gef.part.menu.IMenuObjectEditPart;
import org.eclipse.wb.gef.graphical.DesignEditPart;
import org.eclipse.wb.internal.rcp.gef.policy.jface.ControlDecorationDropLayoutEditPolicy;
import org.eclipse.wb.internal.rcp.gef.policy.jface.FieldEditorDropRequestProcessor;
import org.eclipse.wb.internal.rcp.gef.policy.jface.action.ActionDropRequestProcessor;
import org.eclipse.wb.internal.rcp.model.jface.action.MenuManagerInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.gef.EditPart;

/**
 * Configures RCP related {@link EditPart}'s.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public final class RcpPolicyConfigurator implements IEditPartConfigurator {
	@Override
	public void configure(EditPart context, org.eclipse.wb.gef.core.EditPart editPart) {
		((DesignEditPart) editPart).addRequestProcessor(FieldEditorDropRequestProcessor.INSTANCE);
		// allow drop Action on MenuManager
		if (editPart instanceof IMenuObjectEditPart menuEditPart) {
			if (menuEditPart.getMenuModel().getToolkitModel() instanceof MenuManagerInfo) {
				((DesignEditPart) editPart).addRequestProcessor(ActionDropRequestProcessor.INSTANCE);
			}
		}
		// allow drop ControlDecoration on ControlInfo
		if (editPart.getModel() instanceof ControlInfo) {
			ControlInfo control = (ControlInfo) editPart.getModel();
			editPart.installEditPolicy(new ControlDecorationDropLayoutEditPolicy(control));
		}
	}
}
