/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.internal.rcp.gef.part.rcp.perspective;

import org.eclipse.wb.core.gef.part.AbstractComponentEditPart;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.PageLayoutInfo;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;

import java.util.List;

/**
 * {@link EditPart} for {@link PageLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.part
 */
public final class PageLayoutEditPart extends AbstractComponentEditPart {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PageLayoutEditPart(PageLayoutInfo page) {
		super(page);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Children
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected List<?> getModelChildren() {
		List<?> modelChildren = super.getModelChildren();
		return modelChildren;
	}

	@Override
	protected EditPart createChild(Object model) {
		return super.createChild(model);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Figure
	//
	////////////////////////////////////////////////////////////////////////////
	private static final Point LOCATION = new Point(20, 40);

	@Override
	protected Point getRootLocation() {
		return LOCATION;
	}
}
