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
package org.eclipse.wb.internal.swt.model.layout.grid;

import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.swt.SWT;

/**
 * Model for column in {@link IGridLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swt.model.layout
 */
public final class GridColumnInfo<C extends IControlInfo> extends GridDimensionInfo<C> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public GridColumnInfo(IGridLayoutInfo<C> layout) {
		super(layout);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Grab
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean getGrab(IGridDataInfo gridData) {
		return gridData.getHorizontalGrab();
	}

	@Override
	protected void setGrab(IGridDataInfo gridData, boolean grab) throws Exception {
		gridData.setHorizontalGrab(grab);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Alignment
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getAlignmentTitle(int alignment) {
		if (alignment == SWT.LEFT) {
			return "left";
		} else if (alignment == SWT.CENTER) {
			return "center";
		} else if (alignment == SWT.RIGHT) {
			return "right";
		} else {
			return "fill";
		}
	}

	@Override
	protected int getAlignment(IGridDataInfo gridData) {
		return gridData.getHorizontalAlignment();
	}

	@Override
	protected void setAlignment(IGridDataInfo gridData, int alignment) throws Exception {
		gridData.setHorizontalAlignment(alignment);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Delete
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void delete() throws Exception {
		m_layout.command_deleteColumn(m_index, true);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Processing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean shouldProcessThisControl(IGridDataInfo gridData) {
		return gridData.getX() == m_index;
	}
}
