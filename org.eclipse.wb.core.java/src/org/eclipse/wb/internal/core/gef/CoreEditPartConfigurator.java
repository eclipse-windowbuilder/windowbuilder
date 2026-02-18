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
package org.eclipse.wb.internal.core.gef;

import org.eclipse.wb.core.gef.IEditPartConfigurator;
import org.eclipse.wb.core.gef.policy.DirectTextPropertyEditPolicy;
import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.gef.policy.DblClickRunScriptEditPolicy;
import org.eclipse.wb.internal.core.gef.policy.FlipBooleanPropertyEditPolicy;
import org.eclipse.wb.internal.core.utils.state.GlobalState;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;

import org.apache.commons.lang3.StringUtils;

/**
 * {@link IEditPartConfigurator} for any {@link EditPart}.
 *
 * @author scheglov_ke
 * @coverage core.gef
 */
public final class CoreEditPartConfigurator implements IEditPartConfigurator {
	@Override
	public void configure(EditPart context, EditPart editPart) {
		Object model = editPart.getModel();
		// double click
		if (GlobalState.isComponent(model)) {
			ObjectInfo component = (ObjectInfo) model;
			configureFlipBooleanProperty(editPart, component);
			configure_onDoubleClick_runScript(editPart, component);
		}
		// direct edit
		if (model instanceof AbstractComponentInfo item) {
			DirectTextPropertyEditPolicy.install(editPart, item);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Allows to use <code>double-click.flipBooleanProperty</code> parameter to flip some boolean
	 * property between <code>true/false</code> states, for example "expanded" property.
	 */
	private void configureFlipBooleanProperty(EditPart editPart, ObjectInfo component) {
		String propertyPath =
				GlobalState.getParametersProvider().getParameter(
						component,
						"double-click.flipBooleanProperty");
		if (!StringUtils.isEmpty(propertyPath)) {
			EditPolicy editPolicy = new FlipBooleanPropertyEditPolicy(component, propertyPath);
			editPart.installEditPolicy(editPolicy.getClass(), editPolicy);
		}
	}

	/**
	 * If has "double-click.runScript", then run this MVEL script with component as context.
	 */
	private void configure_onDoubleClick_runScript(EditPart editPart, ObjectInfo component) {
		String propertyPath =
				GlobalState.getParametersProvider().getParameter(component, "double-click.runScript");
		if (!StringUtils.isEmpty(propertyPath)) {
			EditPolicy editPolicy = new DblClickRunScriptEditPolicy(component, propertyPath);
			editPart.installEditPolicy(editPolicy.getClass(), editPolicy);
		}
	}
}
