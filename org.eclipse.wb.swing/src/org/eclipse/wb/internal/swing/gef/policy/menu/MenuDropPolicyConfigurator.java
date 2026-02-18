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
package org.eclipse.wb.internal.swing.gef.policy.menu;

import org.eclipse.wb.core.gef.IEditPartConfigurator;
import org.eclipse.wb.internal.swing.Activator;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;

/**
 * Contributes menu related {@link EditPolicy}'s.
 *
 * @author scheglov_ke
 * @coverage swing.gef.policy
 */
public final class MenuDropPolicyConfigurator implements IEditPartConfigurator {
	@Override
	public void configure(EditPart context, EditPart editPart) {
		// drop JMenuBar on JFrame, JDialog and JApply
		if (editPart.getModel() instanceof ContainerInfo) {
			ContainerInfo container = (ContainerInfo) editPart.getModel();
			Class<?> componentClass = container.getDescription().getComponentClass();
			if (JFrame.class.isAssignableFrom(componentClass)
					|| JInternalFrame.class.isAssignableFrom(componentClass)
					|| JDialog.class.isAssignableFrom(componentClass)
					|| Activator.isAssignableFromJApplet(componentClass)) {
				EditPolicy editPolicy = new MenuBarDropLayoutEditPolicy(container);
				editPart.installEditPolicy(editPolicy.getClass(), editPolicy);
			}
		}
		// drop JPopupMenu on any java.awt.Component
		if (editPart.getModel() instanceof ComponentInfo) {
			ComponentInfo component = (ComponentInfo) editPart.getModel();
			EditPolicy editPolicy = new MenuPopupDropLayoutEditPolicy(component);
			editPart.installEditPolicy(editPolicy.getClass(), editPolicy);
		}
	}
}
