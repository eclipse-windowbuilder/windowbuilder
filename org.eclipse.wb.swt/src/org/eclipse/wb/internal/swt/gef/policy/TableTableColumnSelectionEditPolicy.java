/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
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
package org.eclipse.wb.internal.swt.gef.policy;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.figure.TextFeedback;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.RectangleFigure;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;
import org.eclipse.wb.gef.graphical.handles.SideResizeHandle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.gef.graphical.tools.ResizeTracker;
import org.eclipse.wb.internal.swt.model.widgets.ITableColumnInfo;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link SelectionEditPolicy} that shows simple rectangle selection around {@link EditPart} and one
 * column resize {@link Handle}.
 *
 * @author lobas_av
 * @coverage swt.gef.policy
 */
public final class TableTableColumnSelectionEditPolicy extends SelectionEditPolicy {
	private final ITableColumnInfo m_column;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public TableTableColumnSelectionEditPolicy(ITableColumnInfo column) {
		m_column = column;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Handles
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected List<Handle> createSelectionHandles() {
		List<Handle> handles = new ArrayList<>();
		// create move column handle
		MoveHandle moveHandle = new MoveHandle(getHost());
		moveHandle.setForegroundColor(ColorConstants.red);
		handles.add(moveHandle);
		//
		return handles;
	}

	@Override
	protected List<Handle> createStaticHandles() {
		List<Handle> handles = new ArrayList<>();
		// create resize column handle
		SideResizeHandle resizeHandle =
				new SideResizeHandle(getHost(), PositionConstants.RIGHT, 10, true);
		resizeHandle.setDragTracker(new ResizeTracker(getHost(),
				PositionConstants.EAST,
				REQ_RESIZE));
		handles.add(resizeHandle);
		//
		return handles;
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
	//@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "BC_UNCONFIRMED_CAST")
	public Command getCommand(Request request) {
		return getResizeCommand((ChangeBoundsRequest) request);
	}

	@Override
	//@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "BC_UNCONFIRMED_CAST")
	public void showSourceFeedback(Request request) {
		showResizeFeedback((ChangeBoundsRequest) request);
	}

	@Override
	//@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "BC_UNCONFIRMED_CAST")
	public void eraseSourceFeedback(Request request) {
		eraseResizeFeedback((ChangeBoundsRequest) request);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Resize
	//
	////////////////////////////////////////////////////////////////////////////
	private IFigure m_resizeFeedback;
	private TextFeedback m_textFeedback;

	private Command getResizeCommand(ChangeBoundsRequest request) {
		final Rectangle newBounds = request.getTransformedRectangle(getHostFigure().getBounds());
		return new EditCommand(m_column) {
			@Override
			protected void executeEdit() throws Exception {
				m_column.setWidth(newBounds.width);
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
			FigureUtils.translateFigureToAbsolute(hostFigure, bounds.shrink(-1, -1));
		}
		// update selection feedback
		m_resizeFeedback.setBounds(bounds);
		// update text feedback
		m_textFeedback.setText(Integer.toString(bounds.width - 2));
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