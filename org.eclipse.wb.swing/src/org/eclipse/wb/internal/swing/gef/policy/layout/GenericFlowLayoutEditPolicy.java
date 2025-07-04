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
package org.eclipse.wb.internal.swing.gef.policy.layout;

import org.eclipse.wb.core.gef.policy.selection.NonResizableSelectionEditPolicy;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.swing.gef.policy.ComponentFlowLayoutEditPolicy;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.layout.GenericFlowLayoutInfo;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;

/**
 * Implementation of {@link LayoutEditPolicy} for {@link GenericFlowLayoutInfo}.
 *
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage swing.gef.policy
 */
public abstract class GenericFlowLayoutEditPolicy extends ComponentFlowLayoutEditPolicy {
	private final GenericFlowLayoutInfo m_layout;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public GenericFlowLayoutEditPolicy(GenericFlowLayoutInfo layout) {
		super(layout);
		m_layout = layout;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean isRtl(Request request) {
		return m_layout.getContainer().isRTL();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Decoration
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void decorateChild(EditPart child) {
		if (child.getModel() instanceof ComponentInfo) {
			child.installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, new NonResizableSelectionEditPolicy());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// AbstractFlowLayoutEditPolicy
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected final boolean isGoodReferenceChild(Request request, EditPart editPart) {
		Object model = editPart.getModel();
		return model instanceof ComponentInfo && GlobalState.getValidatorHelper().canReference(model);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void command_CREATE(ComponentInfo newObject, ComponentInfo referenceObject)
			throws Exception {
		m_layout.add(newObject, referenceObject);
	}

	@Override
	protected void command_MOVE(ComponentInfo object, ComponentInfo referenceObject) throws Exception {
		m_layout.move(object, referenceObject);
	}
}
