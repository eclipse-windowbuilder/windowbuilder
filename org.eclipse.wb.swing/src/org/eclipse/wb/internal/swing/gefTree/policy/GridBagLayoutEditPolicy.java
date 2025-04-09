/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
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
package org.eclipse.wb.internal.swing.gefTree.policy;

import org.eclipse.wb.core.gefTree.policy.ObjectLayoutEditPolicy;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.swing.gef.ComponentsLayoutRequestValidator;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.GridBagLayoutInfo;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;

import java.util.List;

/**
 * Implementation of {@link LayoutEditPolicy} for {@link GridBagLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.gefTree.policy
 */
public final class GridBagLayoutEditPolicy extends ObjectLayoutEditPolicy<ComponentInfo> {
	private final GridBagLayoutInfo m_layout;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public GridBagLayoutEditPolicy(GridBagLayoutInfo layout) {
		super(layout);
		m_layout = layout;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Requests
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean isGoodReferenceChild(Request request, EditPart editPart) {
		return editPart.getModel() instanceof ComponentInfo;
	}

	@Override
	protected ILayoutRequestValidator getRequestValidator() {
		return ComponentsLayoutRequestValidator.INSTANCE;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void command_CREATE(ComponentInfo component, ComponentInfo reference) throws Exception {
		m_layout.command_CREATE_last(component);
	}

	@Override
	protected Command getMoveCommand(List<? extends EditPart> moveParts, Object referenceObject) {
		return null;
	}

	@Override
	protected Command getAddCommand(List<? extends EditPart> addParts, Object referenceObject) {
		return null;
	}
}