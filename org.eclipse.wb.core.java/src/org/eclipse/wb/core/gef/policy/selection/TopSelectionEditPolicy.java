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
package org.eclipse.wb.core.gef.policy.selection;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.core.model.ITopBoundsSupport;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;
import org.eclipse.wb.gef.graphical.handles.ResizeHandle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.gef.graphical.tools.ResizeTracker;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link SelectionEditPolicy} for top level {@link AbstractComponentInfo}.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public final class TopSelectionEditPolicy extends SelectionEditPolicy {
	private final IAbstractComponentInfo m_component;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public TopSelectionEditPolicy(IAbstractComponentInfo component) {
		m_component = component;
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
		handles.add(createResizeHandle(PositionConstants.EAST));
		handles.add(createResizeHandle(PositionConstants.SOUTH_EAST));
		handles.add(createResizeHandle(PositionConstants.SOUTH));
		return handles;
	}

	private Handle createResizeHandle(int direction) {
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
	private TopResizeFigure m_resizeFeedback;

	/**
	 * @return the {@link Command} for resize.
	 */
	private Command getResizeCommand(ChangeBoundsRequest request) {
		final Rectangle oldBounds = getHost().getFigure().getBounds();
		final Rectangle newBounds = request.getTransformedRectangle(oldBounds);
		sanitizeBounds(newBounds);
		return new EditCommand(m_component) {
			@Override
			protected void executeEdit() throws Exception {
				ITopBoundsSupport topBoundsSupport = m_component.getTopBoundsSupport();
				topBoundsSupport.setSize(newBounds.width, newBounds.height);
			}
		};
	}

	/**
	 * Shows resize feedback.
	 */
	protected void showResizeFeedback(ChangeBoundsRequest request) {
		if (m_resizeFeedback == null) {
			// create feedback
			{
				m_resizeFeedback = new TopResizeFigure();
				addFeedback(m_resizeFeedback);
			}
		}
		// update feedback
		{
			// prepare feedback bounds
			Rectangle bounds;
			{
				IFigure hostFigure = getHostFigure();
				bounds = request.getTransformedRectangle(hostFigure.getBounds());
				sanitizeBounds(bounds);
				FigureUtils.translateFigureToAbsolute(hostFigure, bounds);
			}
			String sizeText =
					"[" + Integer.toString(bounds.width) + " x " + Integer.toString(bounds.height) + "]";
			m_resizeFeedback.setSizeText(sizeText);
			// set bounds for feedback
			m_resizeFeedback.setBounds(bounds);
		}
	}

	/**
	 * Erases resize feedback.
	 */
	private void eraseResizeFeedback(ChangeBoundsRequest request) {
		if (m_resizeFeedback != null) {
			removeFeedback(m_resizeFeedback);
			m_resizeFeedback = null;
		}
	}

	/**
	 * Ensure that given {@link Rectangle} has reasonable width/height.
	 */
	private static void sanitizeBounds(Rectangle bounds) {
		bounds.width = Math.max(bounds.width, 10);
		bounds.height = Math.max(bounds.height, 10);
	}
}