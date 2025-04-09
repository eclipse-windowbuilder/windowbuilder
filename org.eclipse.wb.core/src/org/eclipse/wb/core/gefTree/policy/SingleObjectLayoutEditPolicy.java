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
package org.eclipse.wb.core.gefTree.policy;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;

import java.util.List;

/**
 * {@link LayoutEditPolicy} for {@link ObjectInfo} container which accepts only one child.
 *
 * @author scheglov_ke
 * @coverage core.gefTree.policy
 */
public abstract class SingleObjectLayoutEditPolicy<C> extends ObjectLayoutEditPolicy<C> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SingleObjectLayoutEditPolicy(ObjectInfo host) {
		super(host);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Routing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected final boolean isGoodReferenceChild(Request request, EditPart editPart) {
		return false;
	}

	/**
	 * @return <code>true</code> if this container is empty, so we can drop new component.
	 */
	protected abstract boolean isEmpty();

	////////////////////////////////////////////////////////////////////////////
	//
	// Command
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final Command getCommand(Request request) {
		if (!isEmpty()) {
			return null;
		}
		return super.getCommand(request);
	}

	@Override
	protected final Command getMoveCommand(List<? extends EditPart> moveParts, Object referenceObject) {
		return null;
	}

	@Override
	protected final Command getAddCommand(List<? extends EditPart> addParts, Object referenceObject) {
		if (addParts.size() != 1) {
			return null;
		}
		return super.getAddCommand(addParts, referenceObject);
	}

	@Override
	protected final void command_CREATE(C component, C reference) throws Exception {
		command_CREATE(component);
	}

	@Override
	protected final void command_ADD(C component, C reference) throws Exception {
		command_ADD(component);
	}

	protected abstract void command_CREATE(C component) throws Exception;

	protected abstract void command_ADD(C component) throws Exception;
}