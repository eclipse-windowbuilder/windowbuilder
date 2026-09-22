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
package org.eclipse.wb.internal.swing.FormLayout.gef.header.selection;

import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.gef.core.requests.KeyRequest;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.internal.swing.FormLayout.gef.header.edit.DimensionHeaderEditPart;
import org.eclipse.wb.internal.swing.FormLayout.model.FormDimensionInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.FormLayoutInfo;
import org.eclipse.wb.internal.swing.gef.policy.layout.header.selection.AbstractDimensionSelectionEditPolicy;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.Handle;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.swt.SWT;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract {@link SelectionEditPolicy} for {@link DimensionHeaderEditPart}.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.header
 */
abstract class DimensionSelectionEditPolicy<T extends FormDimensionInfo> extends AbstractDimensionSelectionEditPolicy {

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
	private ResizeHintFigure m_feedback;
	private ChangeBoundsRequest m_lastResizeRequest;

	@Override
	public boolean understandsRequest(Request request) {
		return super.understandsRequest(request) || request.getType() == REQ_RESIZE;
	}

	@Override
	public Command getCommand(Request request) {
		if (REQ_RESIZE.equals(request.getType())) {
			return getResizeCommand((ChangeBoundsRequest) request);
		}
		return null;
	}

	private Command getResizeCommand(ChangeBoundsRequest request) {
		if (!getLayout().canChangeDimensions()) {
			return null;
		}
		// use such "indirect" command because when we press Ctrl and _don't_ move mouse after
		// this, we will show correct feedback text (without hint), and set correct m_resizeCommand,
		// but GEF already asked command and will not ask it again
		return new Command() {
			@Override
			public void execute() {
				getHost().getViewer().getEditDomain().getCommandStack().execute(m_resizeCommand);
			}
		};
	}

	@Override
	protected void showTextFeedback(ChangeBoundsRequest request, Point location) {
		Layer feedbackLayer = getMainLayer(LayerConstants.FEEDBACK_LAYER);
		// add feedback
		if (m_feedback == null) {
			m_feedback = new ResizeHintFigure();
			feedbackLayer.add(m_feedback);
		}
		// set feedback bounds
		m_feedback.setLocation(location);
		// set text
		m_lastResizeRequest = request;
		updateFeedbackText(request, request.isSnapToEnabled());
		// set hint
		String hintSize;
		if (getDimension().getSize().getComponentSize() == null) {
			hintSize = "minimum";
		} else {
			hintSize = "constant";
		}
		m_feedback.setSizeHint(hintSize);
	}

	/**
	 * Updates the feedback text according to the last resize request and Ctrl state.
	 */
	private void updateFeedbackText(Request request, boolean snapToEnabled) {
		m_feedback.setText(getTextFeedbackText(m_lastResizeRequest, snapToEnabled));
	}

	@Override
	protected void eraseTextFeedback(Request request) {
		FigureUtils.removeFigure(m_feedback);
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
	 * @param inverse
	 *          is <code>true</code> if constant size should be replaced with minimum and reverse.
	 *
	 * @return the size text for text feedback.
	 */
	protected abstract String getTextFeedbackText(ChangeBoundsRequest request, boolean inverse);
}
