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
package org.eclipse.wb.internal.swt.gef.policy.layout;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.figure.TextFeedback;
import org.eclipse.wb.core.gef.policy.PolicyUtils;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.RectangleFigure;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;
import org.eclipse.wb.gef.graphical.handles.ResizeHandle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.gef.graphical.tools.ResizeTracker;
import org.eclipse.wb.internal.swt.model.layout.IRowDataInfo;
import org.eclipse.wb.internal.swt.model.layout.IRowLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.RowLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link SelectionEditPolicy} for {@link RowLayoutInfo}.
 *
 * @author lobas_av
 * @coverage swt.gef.policy
 */
public final class RowLayoutSelectionEditPolicy<C extends IControlInfo> extends SelectionEditPolicy {
	private final IRowLayoutInfo<C> m_layout;
	private final C m_control;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public RowLayoutSelectionEditPolicy(IRowLayoutInfo<C> layout, C control) {
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
	public Command getCommand(Request request) {
		if (request instanceof ChangeBoundsRequest) {
			return getResizeCommand((ChangeBoundsRequest) request);
		}
		return null;
	}

	@Override
	public void showSourceFeedback(Request request) {
		if (request instanceof ChangeBoundsRequest) {
			showResizeFeedback((ChangeBoundsRequest) request);
		}
	}

	@Override
	public void eraseSourceFeedback(Request request) {
		if (request instanceof ChangeBoundsRequest) {
			eraseResizeFeedback((ChangeBoundsRequest) request);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Resize
	//
	////////////////////////////////////////////////////////////////////////////
	private IFigure m_resizeFeedback;
	private TextFeedback m_textFeedback;

	private Command getResizeCommand(ChangeBoundsRequest request) {
		final int resizeDirection = request.getResizeDirection();
		final Rectangle newBounds = request.getTransformedRectangle(getHost().getFigure().getBounds());
		return new EditCommand(m_control) {
			@Override
			protected void executeEdit() throws Exception {
				IRowDataInfo rowData = m_layout.getRowData2(m_control);
				if (PolicyUtils.hasDirection(resizeDirection, PositionConstants.EAST)) {
					rowData.setWidth(newBounds.width);
				}
				if (PolicyUtils.hasDirection(resizeDirection, PositionConstants.SOUTH)) {
					rowData.setHeight(newBounds.height);
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
			IFigure hostFigure = getHostFigure();
			bounds = request.getTransformedRectangle(hostFigure.getBounds());
			FigureUtils.translateFigureToAbsolute(hostFigure, bounds);
		}
		// update selection feedback
		m_resizeFeedback.setBounds(bounds);
		// update text feedback
		m_textFeedback.setText(bounds.width + " x " + bounds.height);
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