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
package org.eclipse.wb.internal.swing.FormLayout.gef.header.selection;

import org.eclipse.wb.core.gef.header.AbstractHeaderSelectionEditPolicy;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.ILocator;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.draw2d.border.LineBorder;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.KeyRequest;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.internal.swing.FormLayout.gef.header.edit.DimensionHeaderEditPart;
import org.eclipse.wb.internal.swing.FormLayout.model.FormDimensionInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.FormLayoutInfo;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.swt.SWT;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract {@link SelectionEditPolicy} for {@link DimensionHeaderEditPart}.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.header
 */
abstract class DimensionSelectionEditPolicy<T extends FormDimensionInfo>
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
			moveHandle.setForegroundColor(IColorConstants.red);
			handles.add(moveHandle);
		}
		//
		return handles;
	}

	@Override
	protected List<Handle> createStaticHandles() {
		List<Handle> handles = new ArrayList<>();
		//
		DimensionHeaderEditPart<T> headerEditPart = getHostHeader();
		if (!headerEditPart.getDimension().isGap()) {
			handles.add(createResizeHandle());
		}
		//
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
	 * @return the host {@link FormLayoutInfo}.
	 */
	protected final FormLayoutInfo getLayout() {
		return getHostHeader().getLayout();
	}

	/**
	 * @return the host {@link FormDimensionInfo}.
	 */
	protected final T getDimension() {
		return getHostHeader().getDimension();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Resize
	//
	////////////////////////////////////////////////////////////////////////////
	private Figure m_lineFeedback;
	private ResizeHintFigure m_feedback;
	private ChangeBoundsRequest m_lastResizeRequest;
	protected Command m_resizeCommand;

	@Override
	public boolean understandsRequest(Request request) {
		return super.understandsRequest(request) || request.getType() == REQ_RESIZE;
	}

	@Override
	public Command getCommand(Request request) {
		if (!getLayout().canChangeDimensions()) {
			return null;
		}
		// use such "indirect" command because when we press Ctrl and _don't_ move mouse after
		// this, we will show correct feedback text (without hint), and set correct m_resizeCommand,
		// but GEF already asked command and will not ask it again
		return new Command() {
			@Override
			public void execute() {
				getHost().getViewer().getEditDomain().executeCommand(m_resizeCommand);
			}
		};
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
				LineBorder border = new LineBorder(IColorConstants.red, 2);
				m_lineFeedback.setBorder(border);
				addFeedback(m_lineFeedback);
			}
			// prepare feedback bounds
			Rectangle bounds;
			{
				Figure hostFigure = getHostFigure();
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
				m_feedback = new ResizeHintFigure();
				feedbackLayer.add(m_feedback);
			}
			// set feedback bounds
			{
				Point mouseLocation = changeBoundsRequest.getLocation().getCopy();
				Point feedbackLocation = getTextFeedbackLocation(mouseLocation);
				FigureUtils.translateAbsoluteToFigure(feedbackLayer, feedbackLocation);
				m_feedback.setLocation(feedbackLocation);
			}
			// set text
			m_lastResizeRequest = changeBoundsRequest;
			updateFeedbackText(changeBoundsRequest, changeBoundsRequest.isSnapToEnabled());
			// set hint
			{
				String hintSize;
				if (getDimension().getSize().getComponentSize() == null) {
					hintSize = "minimum";
				} else {
					hintSize = "constant";
				}
				m_feedback.setSizeHint(hintSize);
			}
		}
	}

	/**
	 * Updates the feedback text according to the last resize request and Ctrl state.
	 */
	private void updateFeedbackText(Request request, boolean snapToEnabled) {
		m_feedback.setText(getTextFeedbackText(m_lastResizeRequest, snapToEnabled));
	}

	@Override
	public void eraseSourceFeedback(Request request) {
		removeFeedback(m_lineFeedback);
		m_lineFeedback = null;
		//
		m_feedback.getParent().remove(m_feedback);
		m_feedback = null;
	}

	@Override
	public void performRequest(Request request) {
		if (m_feedback != null && request instanceof KeyRequest keyRequest) {
			if (keyRequest.getKeyCode() == SWT.CTRL) {
				m_feedback.setShowSizeHint(!keyRequest.isPressed());
				updateFeedbackText(request, true);
			}
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
	 * @param inverse
	 *          is <code>true</code> if constant size should be replaced with minimum and reverse.
	 *
	 * @return the size text for text feedback.
	 */
	protected abstract String getTextFeedbackText(ChangeBoundsRequest request, boolean inverse);

	////////////////////////////////////////////////////////////////////////////
	//
	// Move location
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Implementation of {@link ILocator} to place handle directly on header.
	 */
	private class HeaderMoveHandleLocator implements ILocator {
		@Override
		public void relocate(Figure target) {
			Figure reference = getHostFigure();
			Rectangle bounds = reference.getBounds().getCopy();
			FigureUtils.translateFigureToFigure(reference, target, bounds);
			target.setBounds(bounds);
		}
	}
}
