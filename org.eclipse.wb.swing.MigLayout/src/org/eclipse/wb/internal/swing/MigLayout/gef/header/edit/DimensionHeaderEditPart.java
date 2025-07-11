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
package org.eclipse.wb.internal.swing.MigLayout.gef.header.edit;

import org.eclipse.wb.core.gef.header.Headers;
import org.eclipse.wb.core.gef.header.IHeaderMenuProvider;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.tools.ParentTargetDragEditPartTracker;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.swing.MigLayout.model.MigDimensionInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.MigLayoutInfo;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

/**
 * {@link EditPart} for column/row header of {@link MigLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.MigLayout.header
 */
public abstract class DimensionHeaderEditPart<T extends MigDimensionInfo> extends GraphicalEditPart
implements
IHeaderMenuProvider {
	private static final String DEFAULT_FONT_NAME = "Arial";
	protected static final Color COLOR_NORMAL = Headers.COLOR_HEADER;
	protected static final Font DEFAULT_FONT = new Font(null, DEFAULT_FONT_NAME, 7, SWT.NONE);
	protected static final Color GROUP_COLORS[] = new Color[]{
			new Color(null, 200, 255, 200),
			new Color(null, 255, 210, 170),
			new Color(null, 180, 255, 255),
			new Color(null, 255, 255, 180),
			new Color(null, 230, 180, 255)};
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance fields
	//
	////////////////////////////////////////////////////////////////////////////
	protected final MigLayoutInfo m_layout;
	protected final T m_dimension;
	private final IFigure m_containerFigure;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DimensionHeaderEditPart(MigLayoutInfo layout, T dimension, IFigure containerFigure) {
		m_layout = layout;
		m_dimension = dimension;
		m_containerFigure = containerFigure;
		setModel(dimension);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the index of this {@link MigDimensionInfo}.
	 */
	public abstract int getIndex();

	/**
	 * @return the host {@link MigLayoutInfo}.
	 */
	public final MigLayoutInfo getLayout() {
		return m_layout;
	}

	/**
	 * @return the {@link MigDimensionInfo} model.
	 */
	public final T getDimension() {
		return m_dimension;
	}

	/**
	 * @return the offset of {@link IFigure} with headers relative to the absolute
	 *         layer.
	 */
	public final Point getOffset() {
		Point offset = new Point(0, 0);
		FigureUtils.translateFigureToAbsolute2(m_containerFigure, offset);
		return offset;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Dragging
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final Tool getDragTracker(Request request) {
		return new ParentTargetDragEditPartTracker(this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Figure support
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refreshVisuals() {
		// update tooltip XXX
		//getFigure().setToolTipText(m_dimension.getToolTip());
		// update background
		{
			getFigure().setBackgroundColor(COLOR_NORMAL);
			// XXX
			/*if (m_dimension.isGap()) {
      	getFigure().setBackground(COLOR_GAP);
      } else {
      	int group = m_layout.getDimensionGroupIndex(m_dimension);
      	if (group != -1) {
      		getFigure().setBackground(GROUP_COLORS[group % GROUP_COLORS.length]);
      	} else {
      		getFigure().setBackground(COLOR_NORMAL);
      	}
      }*/
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Edit
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void performRequest(Request request) {
		super.performRequest(request);
		if (request.getType() == RequestConstants.REQ_OPEN) {
			editDimension();
		}
	}

	/**
	 * Opens the {@link MigDimensionInfo} edit dialog.
	 */
	protected abstract void editDimension();
}
