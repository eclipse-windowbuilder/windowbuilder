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
package org.eclipse.wb.gef.graphical.tools;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.gef.core.tools.Tool;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.LayerManager;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;

import java.util.ArrayList;
import java.util.List;

/**
 * A Tool which selects multiple objects inside a rectangular area of a Graphical Viewer. If the
 * SHIFT key is pressed at the beginning of the drag, the enclosed items will be appended to the
 * current selection. If the CONTROL key is pressed at the beginning of the drag, the enclosed items
 * will have their selection state inverted.
 * <P>
 * By default, only {@link EditPart}'s whose figure's are on the primary layer will be considered
 * within the enclosed rectangle.
 *
 * @author lobas_av
 * @coverage gef.graphical
 */
public class MarqueeSelectionTool extends Tool {
	private static final int TOGGLE_MODE = 1;
	private static final int APPEND_MODE = 2;
	private static final Request REQUEST = new Request(RequestConstants.REQ_SELECTION);
	//
	private int m_selectionMode;
	private List<EditPart> m_allChildren;
	private List<EditPart> m_selectedEditParts;
	private IFigure m_marqueeFeedbackFigure;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MarqueeSelectionTool() {
		setDefaultCursor(Cursors.CROSS);
		setDisabledCursor(Cursors.NO);
		setUnloadWhenFinished(false);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tool
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void deactivate() {
		if (m_state == STATE_DRAG_IN_PROGRESS) {
			eraseMarqueeFeedback();
			eraseTargetFeedback();
		}
		super.deactivate();
		m_allChildren = null;
		m_selectedEditParts = null;
		m_state = STATE_TERMINAL;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Cursor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Cursor calculateCursor() {
		switch (m_state) {
		case STATE_INITIAL :
		case STATE_DRAG :
		case STATE_DRAG_IN_PROGRESS :
			return getDefaultCursor();
		case STATE_INVALID :
			return getDisabledCursor();
		default :
			return super.calculateCursor();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// High-Level handle MouseEvent
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean handleButtonDown(int button) {
		if (button == 1) {
			if (m_state == STATE_INITIAL) {
				m_state = STATE_DRAG_IN_PROGRESS;
				if ((m_stateMask & SWT.CONTROL) != 0) {
					m_selectionMode = TOGGLE_MODE;
				} else if ((m_stateMask & SWT.SHIFT) != 0) {
					m_selectionMode = APPEND_MODE;
				}
			}
		} else {
			m_state = STATE_INVALID;
			eraseTargetFeedback();
			eraseMarqueeFeedback();
		}
		refreshCursor();
		return true;
	}

	@Override
	protected boolean handleButtonUp(int button) {
		if (m_state == STATE_DRAG_IN_PROGRESS) {
			m_state = STATE_TERMINAL;
			eraseTargetFeedback();
			eraseMarqueeFeedback();
			performMarqueeSelect();
		}
		//
		//setUnloadWhenFinished(false);
		handleFinished();
		return true;
	}

	@Override
	protected boolean handleDragInProgress() {
		if (m_state == STATE_DRAG || m_state == STATE_DRAG_IN_PROGRESS) {
			showMarqueeFeedback();
			eraseTargetFeedback();
			m_selectedEditParts = calculateNewSelection();
			showTargetFeedback();
		}
		return true;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Selection
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * If in multi select mode, add the new selections to the already selected group; otherwise, clear
	 * the selection and select the new group
	 */
	private void performMarqueeSelect() {
		EditPartViewer viewer = getCurrentViewer();
		List<EditPart> newSelections = calculateNewSelection();
		//
		if (m_selectionMode == APPEND_MODE) {
			// append all new selection elements
			for (EditPart editPart : newSelections) {
				viewer.appendSelection(editPart);
			}
		} else if (m_selectionMode == TOGGLE_MODE) {
			List<EditPart> selected = new ArrayList<>(viewer.getSelectedEditParts());
			//
			for (EditPart editPart : newSelections) {
				if (editPart.getSelected() == EditPart.SELECTED_NONE) {
					selected.add(editPart);
				} else {
					selected.remove(editPart);
				}
			}
			//
			viewer.setSelection(new StructuredSelection(selected));
		} else {
			// replace selection to all new selection elements
			viewer.setSelection(new StructuredSelection(newSelections));
		}
	}

	/**
	 * Calculate new selections based on which children fall inside the marquee selection rectangle.
	 * Do not select children who are not visible.
	 */
	private List<EditPart> calculateNewSelection() {
		return getAllChildren().stream() //
				.filter(this::isPrimaryMarqueeSelectable) //
				.filter(this::isMarqueeSelectable) //
				.toList();
	}

	/**
	 * Decides whether the given edit part may potentially be included in the
	 * current marquee selection.
	 *
	 * @param editPart the {@link EditPart} of interest
	 * @return {@code true} if the given edit part may be included into the
	 *         marquee selection, {@code false} otherwise
	 */
	private boolean isMarqueeSelectable(EditPart editPart) {
		// IMPORTANT: MarqueeSelectionTool is not a TargetingTool, thus the
		// pre-selection does not depend on hit-testing. Therefore, the visible
		// state of the edit part's figure has to be taken into consideration as
		// well.
		return editPart.isSelectable() && editPart.getTargetEditPart(REQUEST) == editPart
				&& FigureUtilities.isNotFullyClipped(((GraphicalEditPart) editPart).getFigure());
	}

	/**
	 * Determines which edit parts are directly affected by the current marquee
	 * selection. Calculation is performed by regarding the current marquee
	 * selection rectangle ( {@link #getCurrentMarqueeSelectionRectangle()}).
	 *
	 * @param editPart the {@link EditPart} whose state is to be determined
	 * @return {@code true} if the {@link EditPart} should be regarded as being
	 *         included in the current marquee selection, {@code false} otherwise
	 */
	private boolean isPrimaryMarqueeSelectable(EditPart editPart) {
		// figure bounds are used to determine if edit part is included in selection
		IFigure figure = ((GraphicalEditPart) editPart).getFigure();
		Rectangle r = figure.getBounds().getCopy();
		figure.translateToAbsolute(r);

		Rectangle marqueeSelectionRectangle = getCurrentMarqueeSelectionRectangle();
		return marqueeSelectionRectangle.contains(r);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Target Feedback
	//
	////////////////////////////////////////////////////////////////////////////
	private void showTargetFeedback() {
		for (EditPart editPart : m_selectedEditParts) {
			editPart.showTargetFeedback(REQUEST);
		}
	}

	private void eraseTargetFeedback() {
		if (m_selectedEditParts != null) {
			for (EditPart editPart : m_selectedEditParts) {
				editPart.eraseTargetFeedback(REQUEST);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Marquee Feedback
	//
	////////////////////////////////////////////////////////////////////////////
	private void showMarqueeFeedback() {
		// check create feedback figure
		if (m_marqueeFeedbackFigure == null) {
			m_marqueeFeedbackFigure = new Figure() {
				@Override
				protected void paintClientArea(Graphics graphics) {
					graphics.setLineStyle(SWT.LINE_DASHDOT);
					graphics.setXORMode(true);
					Rectangle r = getClientArea();
					graphics.drawRectangle(0, 0, r.width - 1, r.height - 1);
				}
			};
			m_marqueeFeedbackFigure.setForegroundColor(ColorConstants.white);
			m_marqueeFeedbackFigure.setBackgroundColor(ColorConstants.black);
			getFeedbackPane().add(m_marqueeFeedbackFigure);
		}
		// update feedback figure
		Rectangle bounds = getCurrentMarqueeSelectionRectangle();
		m_marqueeFeedbackFigure.translateToRelative(bounds);
		m_marqueeFeedbackFigure.setBounds(bounds);
	}

	private void eraseMarqueeFeedback() {
		if (m_marqueeFeedbackFigure != null) {
			getFeedbackPane().remove(m_marqueeFeedbackFigure);
			m_marqueeFeedbackFigure = null;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns feedback layer.
	 */
	private IFigure getFeedbackPane() {
		return LayerManager.Helper.find(getCurrentViewer()).getLayer(LayerConstants.FEEDBACK_LAYER);
	}

	/**
	 * Returns rectangular area between left-top corner <code>(m_startX, m_startY)</code> and
	 * bottom-right corner <code>(m_currentX, m_currentY)</code>.
	 */
	private Rectangle getCurrentMarqueeSelectionRectangle() {
		return new Rectangle(getStartLocation(), getLocation());
	}

	/**
	 * Return a {@link List} including all of the children of the root {@link EditPart}.
	 */
	private List<EditPart> getAllChildren() {
		if (m_allChildren == null || m_allChildren.isEmpty()) {
			m_allChildren = new ArrayList<>();
			getAllChildren(m_allChildren, getCurrentViewer().getRootEditPart());
		}
		return m_allChildren;
	}

	/**
	 * Returns a {@link List} including all of the children of the {@link EditPart} passed in.
	 */
	private static void getAllChildren(List<EditPart> children, EditPart editPart) {
		for (EditPart childEditPart : editPart.getChildren()) {
			children.add(childEditPart);
			getAllChildren(children, childEditPart);
		}
	}
}