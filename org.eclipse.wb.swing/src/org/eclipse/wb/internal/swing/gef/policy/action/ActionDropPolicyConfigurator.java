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
package org.eclipse.wb.internal.swing.gef.policy.action;

import org.eclipse.wb.core.gef.IEditPartConfigurator;
import org.eclipse.wb.internal.swing.model.bean.ActionInfo;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;

import javax.swing.AbstractButton;

/**
 * Contributes {@link ActionInfo} related {@link EditPolicy}'s.
 *
 * @author scheglov_ke
 * @coverage swing.gef.policy
 */
public final class ActionDropPolicyConfigurator implements IEditPartConfigurator {
	@Override
	public void configure(EditPart context, org.eclipse.wb.gef.core.EditPart editPart) {
		// drop ActionInfo on javax.swing.AbstractButton
		if (editPart.getModel() instanceof ComponentInfo) {
			ComponentInfo component = (ComponentInfo) editPart.getModel();
			if (AbstractButton.class.isAssignableFrom(component.getDescription().getComponentClass())) {
				editPart.installEditPolicy(new ActionDropButtonLayoutEditPolicy(component));
			}
		}
	}
}
