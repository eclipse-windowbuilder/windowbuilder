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
package org.eclipse.wb.internal.swing.gef.policy.layout.header.selection;

import org.eclipse.wb.core.gef.header.AbstractHeaderSelectionEditPolicy;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.ChangeBoundsRequest;

/**
 * Abstract {@link SelectionEditPolicy} for {@code DimensionHeaderEditPart}.
 *
 * @author scheglov_ke
 * @coverage swing.gef.policy
 */
public abstract class AbstractDimensionSelectionEditPolicy extends AbstractHeaderSelectionEditPolicy {
	private IFigure m_lineFeedback;
	protected Command m_resizeCommand;

	public AbstractDimensionSelectionEditPolicy(LayoutEditPolicy mainPolicy) {
		super(mainPolicy);
	}

	@Override
	public final void showSourceFeedback(Request request) {
		if (!understandsRequest(request)) {
			return;
		}
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
		Layer feedbackLayer = getMainLayer(LayerConstants.FEEDBACK_LAYER);

		Point mouseLocation = changeBoundsRequest.getLocation().getCopy();
		Point feedbackLocation = getTextFeedbackLocation(mouseLocation);
		// translate relative mouse location to layer => absolute mouse location
		feedbackLayer.translateToRelative(feedbackLocation);

		showTextFeedback(changeBoundsRequest, feedbackLocation);
	}

	@Override
	public final void eraseSourceFeedback(Request request) {
		if (!understandsRequest(request)) {
			return;
		}
		removeFeedback(m_lineFeedback);
		m_lineFeedback = null;
		// text feedback
		eraseTextFeedback(request);
	}

	/**
	 * @return the location of text feedback (with size hint).
	 */
	protected abstract Point getTextFeedbackLocation(Point mouseLocation);

	/**
	 * Called within {@link #showSourceFeedback(Request)} to show the text feedback.
	 *
	 * @param request  the Request
	 * @param location the absolute location of the text feedback.
	 */
	protected abstract void showTextFeedback(ChangeBoundsRequest request, Point location);

	/**
	 * Called within {@link #eraseSourceFeedback(Request)} to erase the text
	 * feedback.
	 *
	 * @param request the Request
	 */
	protected abstract void eraseTextFeedback(Request request);
}
