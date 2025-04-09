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
package org.eclipse.wb.internal.rcp.gef.policy.forms.layout.grid.header.edit;

import org.eclipse.wb.core.gef.header.Headers;
import org.eclipse.wb.core.gef.header.IHeaderMenuProvider;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.tools.ParentTargetDragEditPartTracker;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.ITableWrapLayoutInfo;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.TableWrapDimensionInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

/**
 * {@link EditPart} for column/row header of {@link ITableWrapLayout_Info<C>}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public abstract class DimensionHeaderEditPart<C extends IControlInfo> extends GraphicalEditPart
implements
IHeaderMenuProvider {
	protected static final Color COLOR_NORMAL = Headers.COLOR_HEADER;
	protected static final Font DEFAULT_FONT = new Font(null, "Arial", 7, SWT.NONE);
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance fields
	//
	////////////////////////////////////////////////////////////////////////////
	protected final ITableWrapLayoutInfo<C> m_layout;
	protected final TableWrapDimensionInfo<C> m_dimension;
	private final Figure m_containerFigure;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DimensionHeaderEditPart(ITableWrapLayoutInfo<C> layout,
			TableWrapDimensionInfo<C> dimension,
			Figure containerFigure) {
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
	 * @return the host {@link ITableWrapLayout_Info<C>}.
	 */
	public final ITableWrapLayoutInfo<C> getLayout() {
		return m_layout;
	}

	/**
	 * @return the {@link TableWrapDimensionInfo} model.
	 */
	public final TableWrapDimensionInfo<C> getDimension() {
		return m_dimension;
	}

	/**
	 * @return the offset of {@link Figure} with headers relative to the absolute layer.
	 */
	public final Point getOffset() {
		Point offset = new Point(0, 0);
		FigureUtils.translateFigureToAbsolute2(m_containerFigure, offset);
		offset.performTranslate(m_layout.getComposite().getClientAreaInsets());
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
		ExecutionUtils.runLog(new RunnableEx() {
			@Override
			public void run() throws Exception {
				getFigure().setToolTipText(m_dimension.getTitle());
				getFigure().setBackgroundColor(COLOR_NORMAL);
			}
		});
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
			ExecutionUtils.run(m_layout.getUnderlyingModel(), new RunnableEx() {
				@Override
				public void run() throws Exception {
					m_dimension.flipGrab();
				}
			});
		}
	}
}
