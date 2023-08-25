/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.gef.policy.widgets;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.figure.TextFeedback;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.draw2d.RectangleFigure;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;
import org.eclipse.wb.gef.graphical.handles.SideResizeHandle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.gef.graphical.tools.ResizeTracker;
import org.eclipse.wb.internal.rcp.model.widgets.ITreeColumnInfo;

import org.eclipse.draw2d.geometry.Rectangle;

import java.util.List;

/**
 * {@link SelectionEditPolicy} for {@link ITreeColumnInfo}, that allows resizing without selection.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public final class TreeTreeColumnSelectionEditPolicy extends SelectionEditPolicy {
	private static final String REQ_RESIZE = "resize";
	private final ITreeColumnInfo m_column;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public TreeTreeColumnSelectionEditPolicy(ITreeColumnInfo column) {
		m_column = column;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Handles
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected List<Handle> createSelectionHandles() {
		List<Handle> handles = Lists.newArrayList();
		// create move column handle
		MoveHandle moveHandle = new MoveHandle(getHost());
		moveHandle.setForegroundColor(IColorConstants.red);
		handles.add(moveHandle);
		//
		return handles;
	}

	@Override
	protected List<Handle> createStaticHandles() {
		List<Handle> handles = Lists.newArrayList();
		// create resize column handle
		SideResizeHandle resizeHandle =
				new SideResizeHandle(getHost(), IPositionConstants.RIGHT, 10, true);
		resizeHandle.setDragTrackerTool(new ResizeTracker(getHost(),
				IPositionConstants.EAST,
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
				m_resizeFeedback.setForegroundColor(IColorConstants.red);
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