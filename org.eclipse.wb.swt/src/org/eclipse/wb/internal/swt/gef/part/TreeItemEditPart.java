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
package org.eclipse.wb.internal.swt.gef.part;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.swt.model.widgets.TreeItemInfo;

import org.eclipse.draw2d.IFigure;

/**
 * {@link EditPart} for {@link TreeItemInfo}.
 *
 * @author scheglov_ke
 * @coverage swt.gef.part
 */
public final class TreeItemEditPart extends ItemEditPart {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public TreeItemEditPart(TreeItemInfo item) {
		super(item);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Children
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IFigure getContentPane() {
		EditPart parent = this;
		while (parent instanceof TreeItemEditPart) {
			parent = parent.getParent();
		}
		return ((GraphicalEditPart) parent).getContentPane();
	}
}