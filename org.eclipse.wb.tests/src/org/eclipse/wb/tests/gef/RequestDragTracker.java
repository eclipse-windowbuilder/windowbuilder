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
package org.eclipse.wb.tests.gef;

import org.eclipse.wb.gef.core.tools.DragEditPartTracker;

import org.eclipse.gef.EditPart;

public class RequestDragTracker extends DragEditPartTracker {
	public RequestDragTracker(EditPart sourceEditPart) {
		super(sourceEditPart);
	}

	@Override
	public boolean isActive() {
		return super.isActive();
	}
}
