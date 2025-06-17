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
package org.eclipse.wb.internal.rcp.gef.policy.widgets;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.figure.TextFeedback;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.RectangleFigure;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;
import org.eclipse.wb.gef.graphical.handles.SideResizeHandle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.gef.graphical.tools.ResizeTracker;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.rcp.model.widgets.ISashFormInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link SelectionEditPolicy} for {@link IControlInfo} in {@link ISashFormInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public final class SashFormSelectionEditPolicy<C extends IControlInfo> extends SelectionEditPolicy {
	private final ISashFormInfo<C> m_composite;
	private final C m_control;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SashFormSelectionEditPolicy(ISashFormInfo<C> composite, C control) {
		m_composite = composite;
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
		// create move handle
		MoveHandle moveHandle = new MoveHandle(getHost());
		moveHandle.setForegroundColor(ColorConstants.red);
		handles.add(moveHandle);
		//
		return handles;
	}

	@Override
	protected List<Handle> createStaticHandles() {
		List<Handle> handles = new ArrayList<>();
		// create resize handle (exclude last ControlInfo)
		boolean isLast = GenericsUtils.getLastOrNull(m_composite.getChildrenControls()) == m_control;
		if (!isLast) {
			SideResizeHandle resizeHandle;
			if (m_composite.isHorizontal()) {
				resizeHandle = new SideResizeHandle(getHost(), PositionConstants.RIGHT, 10, true);
				resizeHandle.setDragTracker(new ResizeTracker(getHost(),
						PositionConstants.EAST,
						REQ_RESIZE));
			} else {
				resizeHandle = new SideResizeHandle(getHost(), PositionConstants.BOTTOM, 10, true);
				resizeHandle.setDragTracker(new ResizeTracker(getHost(),
						PositionConstants.SOUTH,
						REQ_RESIZE));
			}
			handles.add(resizeHandle);
		}
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

	/**
	 * @return the {@link Command} for {@link #REQ_RESIZE} request.
	 */
	private Command getResizeCommand(ChangeBoundsRequest request) {
		final Rectangle newBounds = request.getTransformedRectangle(getHostFigure().getBounds());
		return new EditCommand(m_composite) {
			@Override
			protected void executeEdit() throws Exception {
				int size = m_composite.isHorizontal() ? newBounds.width : newBounds.height;
				m_composite.command_RESIZE(m_control, size);
			}
		};
	}

	/**
	 * Shows {@link #REQ_RESIZE} feedback.
	 */
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
			FigureUtils.translateFigureToAbsolute(hostFigure, bounds.shrink(-1, -1));
		}
		// update selection feedback
		m_resizeFeedback.setBounds(bounds);
		// update text feedback
		int size = m_composite.isHorizontal() ? bounds.width : bounds.height;
		m_textFeedback.setText(Integer.toString(size - 2));
		m_textFeedback.setLocation(request.getLocation().getTranslated(10, 10));
	}

	/**
	 * Erases {@link #REQ_RESIZE} feedback.
	 */
	private void eraseResizeFeedback(ChangeBoundsRequest request) {
		// erase selection feedback
		removeFeedback(m_resizeFeedback);
		m_resizeFeedback = null;
		// erase text feedback
		m_textFeedback.remove();
		m_textFeedback = null;
	}
}
