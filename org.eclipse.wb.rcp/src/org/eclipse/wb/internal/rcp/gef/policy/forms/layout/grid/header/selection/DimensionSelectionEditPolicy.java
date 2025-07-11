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
package org.eclipse.wb.internal.rcp.gef.policy.forms.layout.grid.header.selection;

import org.eclipse.wb.core.gef.header.AbstractHeaderSelectionEditPolicy;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.internal.rcp.gef.policy.forms.layout.grid.header.edit.DimensionHeaderEditPart;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.TableWrapDimensionInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Locator;
import org.eclipse.draw2d.geometry.Rectangle;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract {@link SelectionEditPolicy} for {@link DimensionHeaderEditPart}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
abstract class DimensionSelectionEditPolicy<C extends IControlInfo>
extends
AbstractHeaderSelectionEditPolicy {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DimensionSelectionEditPolicy(LayoutEditPolicy mainPolicy) {
		super(mainPolicy);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Handles
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected List<Handle> createSelectionHandles() {
		List<Handle> handles = new ArrayList<>();
		// move handle
		{
			MoveHandle moveHandle = new MoveHandle(getHost(), new HeaderMoveHandleLocator());
			moveHandle.setForegroundColor(ColorConstants.red);
			handles.add(moveHandle);
		}
		//
		return handles;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the host {@link TableWrapDimensionInfo}.
	 */
	@SuppressWarnings("unchecked")
	protected final TableWrapDimensionInfo<C> getDimension() {
		return ((DimensionHeaderEditPart<C>) getHost()).getDimension();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Move location
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Implementation of {@link Locator} to place handle directly on header.
	 */
	private class HeaderMoveHandleLocator implements Locator {
		@Override
		public void relocate(IFigure target) {
			IFigure reference = getHostFigure();
			Rectangle bounds = reference.getBounds().getCopy();
			FigureUtils.translateFigureToFigure(reference, target, bounds);
			target.setBounds(bounds);
		}
	}
}
