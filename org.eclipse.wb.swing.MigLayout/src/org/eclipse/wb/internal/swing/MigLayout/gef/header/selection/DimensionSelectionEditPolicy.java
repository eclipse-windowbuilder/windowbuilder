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
package org.eclipse.wb.internal.swing.MigLayout.gef.header.selection;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.gef.core.requests.KeyRequest;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.swing.MigLayout.gef.header.edit.DimensionHeaderEditPart;
import org.eclipse.wb.internal.swing.MigLayout.gef.header.selection.ResizeHintFigure.SizeElement;
import org.eclipse.wb.internal.swing.MigLayout.model.MigDimensionInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.MigLayoutInfo;
import org.eclipse.wb.internal.swing.gef.policy.layout.header.selection.AbstractDimensionSelectionEditPolicy;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Handle;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.ChangeBoundsRequest;

import net.miginfocom.layout.UnitValue;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract {@link SelectionEditPolicy} for {@link DimensionHeaderEditPart}.
 *
 * @author scheglov_ke
 * @coverage swing.MigLayout.header
 */
abstract class DimensionSelectionEditPolicy<T extends MigDimensionInfo> extends AbstractDimensionSelectionEditPolicy {

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
	 * @return the host {@link MigLayoutInfo}.
	 */
	protected final MigLayoutInfo getLayout() {
		return getHostHeader().getLayout();
	}

	/**
	 * @return the host {@link MigDimensionInfo}.
	 */
	protected final T getDimension() {
		return getHostHeader().getDimension();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Resize
	//
	////////////////////////////////////////////////////////////////////////////
	private SizeElement m_resizeSizeElement;
	private String m_resizeSizeUnit;
	private ResizeHintFigure m_feedback;
	private ChangeBoundsRequest m_lastResizeRequest;

	@Override
	public boolean understandsRequest(Request request) {
		return super.understandsRequest(request) || request.getType() == REQ_RESIZE;
	}

	@Override
	public Command getCommand(Request request) {
		if (REQ_RESIZE.equals(request.getType())) {
			return getResizeCommand(request);
		}
		return null;
	}

	private Command getResizeCommand(Request request) {
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
			// get initial values
			prepareDefaultResizeElements();
		}
		// set feedback bounds
		m_feedback.setLocation(location);
		// set text
		m_lastResizeRequest = request;
		updateFeedbackText(request);
	}

	/**
	 * Initializes {@link #m_resizeSizeElement} and {@link #m_resizeSizeUnit}.
	 */
	private void prepareDefaultResizeElements() {
		m_resizeSizeElement = SizeElement.PREF;
		m_resizeSizeUnit = StringUtils.EMPTY;
		// prepare default size element to resize
		UnitValue resizeValue;
		{
			T dimension = getDimension();
			{
				resizeValue = dimension.getPreferredSize();
				m_resizeSizeElement = SizeElement.PREF;
			}
			if (resizeValue == null) {
				resizeValue = dimension.getMinimumSize();
				if (resizeValue != null) {
					m_resizeSizeElement = SizeElement.MIN;
				}
			}
			if (resizeValue == null) {
				resizeValue = dimension.getMaximumSize();
				if (resizeValue != null) {
					m_resizeSizeElement = SizeElement.MAX;
				}
			}
		}
		// use existing unit
		if (resizeValue != null) {
			m_resizeSizeUnit = resizeValue.getUnitString();
		}
	}

	/**
	 * Updates the feedback text according to the {@link #m_lastResizeRequest} and size element/unit.
	 */
	private void updateFeedbackText(Request request) {
		// prepare size
		final String sizeString;
		{
			int pixels = getPixelSize(m_lastResizeRequest.getSizeDelta());
			sizeString = getDimension().toUnitString(pixels, m_resizeSizeUnit);
		}
		// show text
		{
			String text;
			switch (m_resizeSizeElement) {
			case MIN :
				text = "min := ";
				break;
			case PREF :
				text = "pref := ";
				break;
			case MAX :
				text = "max := ";
				break;
			default :
				text = "";
			}
			m_feedback.setText(text + sizeString);
		}
		// set command
		final MigLayoutInfo layout = getLayout();
		m_resizeCommand = new EditCommand(layout) {
			@Override
			protected void executeEdit() throws Exception {
				T dimension = getDimension();
				switch (m_resizeSizeElement) {
				case MIN :
					dimension.setMinimumSize(sizeString);
					break;
				case PREF :
					dimension.setPreferredSize(sizeString);
					break;
				case MAX :
					dimension.setMaximumSize(sizeString);
					break;
				default :
					return;
				}
				layout.writeDimensions();
			}
		};
	}

	@Override
	protected void eraseTextFeedback(Request request) {
		FigureUtils.removeFigure(m_feedback);
		m_feedback = null;
	}

	@Override
	public void performRequest(Request request) {
		if (request instanceof KeyRequest keyRequest) {
			// special key for resize feedback
			if (m_feedback != null) {
				char c = keyRequest.getCharacter();
				{
					SizeElement newSizeElement = ResizeHintFigure.getNewSizeElement(c);
					if (newSizeElement != null) {
						m_resizeSizeElement = newSizeElement;
						updateFeedbackText(request);
					}
				}
				{
					String newSizeUnit = ResizeHintFigure.getNewSizeUnit(c);
					if (newSizeUnit != null) {
						m_resizeSizeUnit = newSizeUnit;
						updateFeedbackText(request);
					}
				}
			}
			// keyboard based alignment
			if (keyRequest.isPressed()) {
				char c = keyRequest.getCharacter();
				if (c == 'g') {
					flipGrow();
				}
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Resize: abstract feedback
	//
	////////////////////////////////////////////////////////////////////////////

	/**
	 * @return the size of host {@link EditPart} in pixels, taking into account given resize delta.
	 */
	protected abstract int getPixelSize(Dimension resizeDelta);

	////////////////////////////////////////////////////////////////////////////
	//
	// Keyboard
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Set/unset the "grow" flag.
	 */
	private void flipGrow() {
		final MigLayoutInfo layout = getLayout();
		ExecutionUtils.run(layout, () -> {
			getDimension().flipGrow();
			layout.writeDimensions();
		});
	}
}
