/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swt.model.layout.grid;

import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.swt.SWT;

/**
 * Model for row in {@link IGridLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swt.model.layout
 */
public final class GridRowInfo<C extends IControlInfo> extends GridDimensionInfo<C> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public GridRowInfo(IGridLayoutInfo<C> layout) {
		super(layout);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Grab
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean getGrab(IGridDataInfo gridData) {
		return gridData.getVerticalGrab();
	}

	@Override
	protected void setGrab(IGridDataInfo gridData, boolean grab) throws Exception {
		gridData.setVerticalGrab(grab);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Alignment
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getAlignmentTitle(int alignment) {
		if (alignment == SWT.TOP) {
			return "top";
		} else if (alignment == SWT.CENTER) {
			return "center";
		} else if (alignment == SWT.BOTTOM) {
			return "bottom";
		} else {
			return "fill";
		}
	}

	@Override
	protected int getAlignment(IGridDataInfo gridData) {
		return gridData.getVerticalAlignment();
	}

	@Override
	protected void setAlignment(IGridDataInfo gridData, int alignment) throws Exception {
		gridData.setVerticalAlignment(alignment);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Delete
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void delete() throws Exception {
		m_layout.command_deleteRow(m_index, true);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Processing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean shouldProcessThisControl(IGridDataInfo gridData) {
		return gridData.getY() == getIndex();
	}
}
