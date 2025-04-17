/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.internal.rcp.gef.part.widgets;

import org.eclipse.wb.core.gef.part.ComponentIconEditPart;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.rcp.model.widgets.IDropTargetInfo;

import org.eclipse.draw2d.geometry.Rectangle;

/**
 * {@link EditPart} for {@link IDropTargetInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.part
 */
public final class DropTargetEditPart extends ComponentIconEditPart {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DropTargetEditPart(IDropTargetInfo component) {
		super(component);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Figure
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Rectangle getFigureBounds(int width, int height) {
		return new Rectangle(45, 5, width, height);
	}
}
