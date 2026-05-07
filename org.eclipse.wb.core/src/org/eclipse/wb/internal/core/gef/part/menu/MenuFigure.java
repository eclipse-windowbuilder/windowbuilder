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
package org.eclipse.wb.internal.core.gef.part.menu;

import org.eclipse.draw2d.Figure;
import org.eclipse.gef.EditPart;

/**
 * Generic menu {@link Figure} used for visualization by all menu
 * {@link EditPart}s. The top-left corner of a menu will always be
 * {@code (0, 0)} and thus local coordinates must be used.
 */
class MenuFigure extends Figure {
	@Override
	protected boolean useLocalCoordinates() {
		// model bounds of menu items are relative to the top-left corner of a menu
		return true;
	}
}
