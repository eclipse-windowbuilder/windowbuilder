/*******************************************************************************
 * Copyright (c) 2026 Patrick Ziegler and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.model.property.table.editparts;

import org.eclipse.wb.internal.core.model.property.table.PropertyTable;

import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.requests.SelectionRequest;
import org.eclipse.gef.tools.TargetingTool;

/**
 * DragTracker used to select, edit, and open {@link PropertyEditPart}s.
 */
public class PropertyEditPartTracker extends TargetingTool implements DragTracker {
	private EditPart owner;

	/**
	 * Constructs a new {@link PropertyEditPartTracker} with the given edit part as
	 * the source.
	 *
	 * @param owner the source edit part
	 */
	public PropertyEditPartTracker(EditPart owner) {
		this.owner = owner;
	}

	/**
	 * Returns {@code true} if cursor is on splitter.
	 */
	private boolean isLocationSplitter() {
		if (getCurrentViewer() instanceof PropertyTable propertyTable) {
			return Math.abs(propertyTable.getSplitter() - getLocation().x) < 2;
		}
		return false;
	}

	@Override
	protected boolean handleButtonDown(int button) {
		if (isLocationSplitter() || button != 1) {
			return false;
		}

		getCurrentViewer().select(owner);

		SelectionRequest request = new SelectionRequest();
		request.setType(REQ_SELECTION);
		request.setLocation(getLocation());
		owner.performRequest(request);

		return true;
	}

	@Override
	protected String getCommandName() {
		return "Property Tracker";//$NON-NLS-1$
	}
}
