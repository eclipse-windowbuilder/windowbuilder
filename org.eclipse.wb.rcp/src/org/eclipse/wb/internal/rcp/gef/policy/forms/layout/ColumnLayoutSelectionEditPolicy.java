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
package org.eclipse.wb.internal.rcp.gef.policy.forms.layout;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.figure.TextFeedback;
import org.eclipse.wb.core.gef.policy.PolicyUtils;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.RectangleFigure;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;
import org.eclipse.wb.gef.graphical.handles.ResizeHandle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.gef.graphical.tools.ResizeTracker;
import org.eclipse.wb.internal.rcp.model.forms.layout.column.IColumnLayoutDataInfo;
import org.eclipse.wb.internal.rcp.model.forms.layout.column.IColumnLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link SelectionEditPolicy} for {@link IColumnLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public final class ColumnLayoutSelectionEditPolicy<C extends IControlInfo>
extends
SelectionEditPolicy {
	private final IColumnLayoutInfo<C> m_layout;
	private final C m_control;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ColumnLayoutSelectionEditPolicy(IColumnLayoutInfo<C> layout, C control) {
		m_layout = layout;
		m_control = control;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Handles
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected List<Handle> createSelectionHandles() {
		List<Handle> handles = new ArrayList<>();
		handles.add(new MoveHandle(getHost()));
		handles.add(createHandle(PositionConstants.EAST));
		handles.add(createHandle(PositionConstants.SOUTH));
		handles.add(createHandle(PositionConstants.SOUTH_EAST));
		return handles;
	}

	/**
	 * @return the {@link ResizeHandle} for given direction.
	 */
	private Handle createHandle(int direction) {
		ResizeHandle handle = new ResizeHandle(getHost(), direction);
		handle.setDragTracker(new ResizeTracker(direction, REQ_RESIZE));
		return handle;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Routing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean understandsRequest(Request request) {
		return super.understandsRequest(request) || request.getType() == REQ_RESIZE;
	}

	@Override
	public Command getCommand(final Request request) {
		return getResizeCommand((ChangeBoundsRequest) request);
	}

	@Override
	public void showSourceFeedback(Request request) {
		showResizeFeedback((ChangeBoundsRequest) request);
	}

	@Override
	public void eraseSourceFeedback(Request request) {
		eraseResizeFeedback((ChangeBoundsRequest) request);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Resize
	//
	////////////////////////////////////////////////////////////////////////////
	private Figure m_resizeFeedback;
	private TextFeedback m_textFeedback;

	//
	private Command getResizeCommand(ChangeBoundsRequest request) {
		final int resizeDirection = request.getResizeDirection();
		final Rectangle newBounds = request.getTransformedRectangle(getHost().getFigure().getBounds());
		return new EditCommand(m_control) {
			@Override
			protected void executeEdit() throws Exception {
				IColumnLayoutDataInfo columnData = m_layout.getColumnData2(m_control);
				if (PolicyUtils.hasDirection(resizeDirection, PositionConstants.EAST)) {
					columnData.setWidthHint(newBounds.width);
				}
				if (PolicyUtils.hasDirection(resizeDirection, PositionConstants.SOUTH)) {
					columnData.setHeightHint(newBounds.height);
				}
			}
		};
	}

	private void showResizeFeedback(ChangeBoundsRequest request) {
		if (m_resizeFeedback == null) {
			// create selection feedback
			{
				m_resizeFeedback = new RectangleFigure();
				m_resizeFeedback.setForegroundColor(ColorConstants.red);
				addFeedback(m_resizeFeedback);
			}
			// create text feedback
			{
				m_textFeedback = new TextFeedback(getFeedbackLayer());
				m_textFeedback.add();
			}
		}
		// prepare bounds
		Rectangle bounds;
		{
			Figure hostFigure = getHostFigure();
			bounds = request.getTransformedRectangle(hostFigure.getBounds());
			FigureUtils.translateFigureToAbsolute(hostFigure, bounds);
		}
		// update selection feedback
		m_resizeFeedback.setBounds(bounds);
		// update text feedback
		m_textFeedback.setText(Integer.toString(bounds.width) + " x " + Integer.toString(bounds.height));
		m_textFeedback.setLocation(request.getLocation().getTranslated(10, 10));
	}

	private void eraseResizeFeedback(ChangeBoundsRequest request) {
		// erase selection feedback
		removeFeedback(m_resizeFeedback);
		m_resizeFeedback = null;
		// erase text feedback
		m_textFeedback.remove();
		m_textFeedback = null;
	}
}