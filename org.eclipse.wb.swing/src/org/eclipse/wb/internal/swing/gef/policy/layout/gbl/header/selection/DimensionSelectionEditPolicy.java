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
package org.eclipse.wb.internal.swing.gef.policy.layout.gbl.header.selection;

import org.eclipse.wb.core.gef.figure.TextFeedback;
import org.eclipse.wb.core.gef.header.AbstractHeaderSelectionEditPolicy;
import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.draw2d.border.LineBorder;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.internal.swing.gef.policy.layout.gbl.header.edit.DimensionHeaderEditPart;
import org.eclipse.wb.internal.swing.model.layout.gbl.AbstractGridBagLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.DimensionInfo;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Locator;
import org.eclipse.draw2d.geometry.Interval;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract {@link SelectionEditPolicy} for {@link DimensionHeaderEditPart}.
 *
 * @author scheglov_ke
 * @coverage swing.gef.policy
 */
abstract class DimensionSelectionEditPolicy<T extends DimensionInfo>
extends
AbstractHeaderSelectionEditPolicy {

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DimensionSelectionEditPolicy(LayoutEditPolicy mainPolicy) {
		super(mainPolicy);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Handles
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected List<Handle> createSelectionHandles() {
		List<Handle> handles = new ArrayList<>();
		// move handle
		{
			MoveHandle moveHandle = new MoveHandle(getHost(), new HeaderMoveHandleLocator());
			moveHandle.setForegroundColor(ColorConstants.red);
			handles.add(moveHandle);
		}
		//
		return handles;
	}

	@Override
	protected List<Handle> createStaticHandles() {
		List<Handle> handles = new ArrayList<>();
		handles.add(createResizeHandle());
		return handles;
	}

	/**
	 * @return the {@link Handle} for resizing.
	 */
	protected abstract Handle createResizeHandle();

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the host {@link DimensionHeaderEditPart}.
	 */
	@SuppressWarnings("unchecked")
	private DimensionHeaderEditPart<T> getHostHeader() {
		return (DimensionHeaderEditPart<T>) getHost();
	}

	/**
	 * @return the host {@link AbstractGridBagLayoutInfo}.
	 */
	protected final AbstractGridBagLayoutInfo getLayout() {
		return getHostHeader().getLayout();
	}

	/**
	 * @return the host {@link DimensionInfo}.
	 */
	protected final T getDimension() {
		return getHostHeader().getDimension();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Resize
	//
	////////////////////////////////////////////////////////////////////////////
	private IFigure m_lineFeedback;
	private TextFeedback m_feedback;
	protected Command m_resizeCommand;

	@Override
	public boolean understandsRequest(Request request) {
		return super.understandsRequest(request) || request.getType() == REQ_RESIZE;
	}

	@Override
	public Command getCommand(Request request) {
		return m_resizeCommand;
	}

	@Override
	public void showSourceFeedback(Request request) {
		ChangeBoundsRequest changeBoundsRequest = (ChangeBoundsRequest) request;
		m_resizeCommand = null;
		// line feedback
		{
			// create feedback
			if (m_lineFeedback == null) {
				m_lineFeedback = new Figure();
				LineBorder border = new LineBorder(ColorConstants.red, 2);
				m_lineFeedback.setBorder(border);
				addFeedback(m_lineFeedback);
			}
			// prepare feedback bounds
			Rectangle bounds;
			{
				IFigure hostFigure = getHostFigure();
				bounds = changeBoundsRequest.getTransformedRectangle(hostFigure.getBounds());
				FigureUtils.translateFigureToAbsolute(hostFigure, bounds);
			}
			// show feedback
			m_lineFeedback.setBounds(bounds);
		}
		// text feedback
		{
			Layer feedbackLayer = getMainLayer(IEditPartViewer.FEEDBACK_LAYER);
			// add feedback
			if (m_feedback == null) {
				m_feedback = new TextFeedback(feedbackLayer);
				m_feedback.add();
			}
			// set feedback bounds
			{
				Point mouseLocation = changeBoundsRequest.getLocation().getCopy();
				Point feedbackLocation = getTextFeedbackLocation(mouseLocation);
				FigureUtils.translateAbsoluteToFigure(feedbackLayer, feedbackLocation);
				m_feedback.setLocation(feedbackLocation);
			}
			// set text
			m_feedback.setText(getFeedbackText(changeBoundsRequest));
		}
	}

	@Override
	public void eraseSourceFeedback(Request request) {
		removeFeedback(m_lineFeedback);
		m_lineFeedback = null;
		m_feedback.remove();
		m_feedback = null;
	}

	/**
	 * @return the size of {@link DimensionInfo} in pixels (based on {@link IGridInfo} information).
	 */
	protected final int getDimensionSize(Interval[] intervals) {
		int index = getDimension().getIndex();
		if (index < intervals.length - 1) {
			return intervals[index + 1].begin() - intervals[index].begin();
		} else {
			return intervals[index].length();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Resize: abstract feedback
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the location of text feedback (with size hint).
	 */
	protected abstract Point getTextFeedbackLocation(Point mouseLocation);

	/**
	 * @return the text for feedback.
	 */
	protected abstract String getFeedbackText(ChangeBoundsRequest request);

	////////////////////////////////////////////////////////////////////////////
	//
	// Move location
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Implementation of {@link Locator} to place handle directly on header.
	 */
	private class HeaderMoveHandleLocator implements Locator {
		@Override
		public void relocate(IFigure target) {
			IFigure reference = getHostFigure();
			Rectangle bounds = reference.getBounds().getCopy();
			FigureUtils.translateFigureToFigure(reference, target, bounds);
			target.setBounds(bounds);
		}
	}
}
