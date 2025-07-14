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
package org.eclipse.wb.internal.core.gef.policy.layout.absolute;

import org.eclipse.wb.core.editor.constants.CoreImages;
import org.eclipse.wb.core.gef.policy.PolicyUtils;
import org.eclipse.wb.core.gef.policy.layout.LayoutPolicyUtils;
import org.eclipse.wb.core.gef.policy.layout.generic.AbstractPopupFigure;
import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.Polyline;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.actions.AnchorsActionsSupport;
import org.eclipse.wb.internal.core.gef.policy.snapping.ComponentAttachmentInfo;
import org.eclipse.wb.internal.core.gef.policy.snapping.IAbsoluteLayoutCommands;
import org.eclipse.wb.internal.core.gef.policy.snapping.PlacementUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.geometry.Transposer;
import org.eclipse.gef.EditPart;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Selection policy for complex absolute layouts: SWT FormLayout, Swing SpringLayout.
 *
 * @author mitin_aa
 */
public abstract class AbsoluteComplexSelectionEditPolicy<C extends IAbstractComponentInfo>
		extends AbsoluteBasedSelectionEditPolicy<C> {
	// constants
	private static final int MIN_LEFT_SPACE = 10;
	private static final int INITIAL_RIGHT_SPACE = 10;
	private static final int FIGURES_SPACE = 10;
	// fields
	private List<IFigure> m_feedbacks;
	private List<IFigure> m_alignmentFigures;
	private final IAbsoluteLayoutCommands m_layout;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbsoluteComplexSelectionEditPolicy(IAbsoluteLayoutCommands layoutCommands) {
		super();
		m_layout = layoutCommands;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	//	Selection
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void showSelection() {
		super.showSelection();
		ExecutionUtils.runRethrow(() -> {
			IAbstractComponentInfo widget = (IAbstractComponentInfo) getHostModel();
			drawFeedbacks(widget, PositionConstants.LEFT);
			drawFeedbacks(widget, PositionConstants.RIGHT);
			drawFeedbacks(widget, PositionConstants.TOP);
			drawFeedbacks(widget, PositionConstants.BOTTOM);
		});
		if (getHost().getSelected() == EditPart.SELECTED_PRIMARY) {
			showAlignmentFigures();
		}
	}

	private void drawFeedbacks(IAbstractComponentInfo widget, int side) throws Exception {
		ComponentAttachmentInfo neighborAttachment;
		while ((neighborAttachment = getComponentAttachmentInfo(widget, side)) != null) {
			addFeedbackToComponent(
					widget,
					neighborAttachment.getTarget(),
					side,
					neighborAttachment.getAlignment());
			widget = neighborAttachment.getTarget();
		}
		if (isAttached(widget, side)) {
			addFeedbackToParent(widget, side);
		}
	}

	@Override
	protected void hideSelection() {
		super.hideSelection();
		for (IFigure figure : getFeedbacks()) {
			FigureUtils.removeFigure(figure);
		}
		m_feedbacks = new ArrayList<>();
		hideAlignmentFigures();
	}

	private Dimension getParentSize(IAbstractComponentInfo parent) {
		Rectangle compositeBounds = parent.getModelBounds().getCopy();
		Insets clientAreaInsets = parent.getClientAreaInsets();
		return compositeBounds.shrink(clientAreaInsets).getSize().expand(-1, -1);
	}

	private void addFeedbackToComponent(IAbstractComponentInfo widget,
			IAbstractComponentInfo neighborWidget,
			int side,
			int neighborSide) {
		Assert.isTrue((side == PositionConstants.LEFT || side == PositionConstants.RIGHT)
				&& (neighborSide == PositionConstants.LEFT || neighborSide == PositionConstants.RIGHT)
				|| (side == PositionConstants.TOP || side == PositionConstants.BOTTOM)
				&& (neighborSide == PositionConstants.TOP || neighborSide == PositionConstants.BOTTOM)
				|| side == PositionConstants.CENTER
				|| neighborSide == PositionConstants.CENTER);
		// bounds, transposed
		Transposer t = new Transposer(!PlacementUtils.isHorizontalSide(side));
		Rectangle widgetBounds = t.t(widget.getModelBounds().getCopy());
		Rectangle neighborBounds = t.t(neighborWidget.getModelBounds().getCopy());
		// points
		int x1 =
				PlacementUtils.isTrailingSide(neighborSide) ? neighborBounds.right() : neighborBounds.x;
		int x2 = PlacementUtils.isTrailingSide(side) ? widgetBounds.right() : widgetBounds.x;
		int y = widgetBounds.getLeft().y;
		Point p2 = t.t(new Point(x1, y));
		Point p1 = t.t(new Point(x2, y));
		// draw the main line
		addLineFeedback(p1, p2, PlacementUtils.isHorizontalSide(side));
		// draw the helper line
		int lineStartY = widgetBounds.y > neighborBounds.y ? neighborBounds.y : widgetBounds.y;
		int lineEndY =
				widgetBounds.bottom() < neighborBounds.bottom()
				? neighborBounds.bottom()
						: widgetBounds.bottom();
		Point p3 = t.t(new Point(x1, lineStartY));
		Point p4 = t.t(new Point(x1, lineEndY));
		// convert to feedback
		PolicyUtils.translateModelToFeedback(this, p3);
		PolicyUtils.translateModelToFeedback(this, p4);
		addSimpleLineFeedback(p3, p4);
	}

	private void addFeedbackToParent(IAbstractComponentInfo widget, int side) {
		IAbstractComponentInfo parent = (IAbstractComponentInfo) widget.getParent();
		// bounds, transposed
		Transposer t = new Transposer(!PlacementUtils.isHorizontalSide(side));
		Dimension parentSize = t.t(getParentSize(parent));
		Rectangle widgetBounds = t.t(widget.getModelBounds().getCopy());
		// points
		int x1 = PlacementUtils.isTrailingSide(side) ? parentSize.width : 0;
		int x2 = PlacementUtils.isTrailingSide(side) ? widgetBounds.right() : widgetBounds.x;
		int y = widgetBounds.getLeft().y;
		Point p2 = t.t(new Point(x1, y));
		Point p1 = t.t(new Point(x2, y));
		// draw
		addLineFeedback(p1, p2, PlacementUtils.isHorizontalSide(side));
	}

	private void addLineFeedback(Point p1, Point p2, boolean isHorizontal) {
		// convert to feedback
		PolicyUtils.translateModelToFeedback(this, p1);
		PolicyUtils.translateModelToFeedback(this, p2);
		addSimpleLineFeedback(p1, p2);
		// don't draw line end while line length is less than 3 (default LineEndFigure radius).
		Transposer t = new Transposer(!isHorizontal);
		p1 = t.t(p1);
		p2 = t.t(p2);
		if (Math.abs(p2.x - p1.x) > LineEndFigure.RADIUS) {
			addLineEndFeedback(t.t(p2), isHorizontal, p2.x > p1.x);
		}
	}

	private void addSimpleLineFeedback(Point begin, Point end) {
		Polyline line = new Polyline();
		line.addPoint(begin);
		line.addPoint(end);
		line.setForegroundColor(AbsolutePolicyUtils.COLOR_FEEDBACK);
		line.setLineStyle(SWT.LINE_DASH);
		addMyFeedback(line);
	}

	private void addLineEndFeedback(Point point, boolean isHorizontal, boolean isTrailing) {
		int alignment;
		if (isHorizontal) {
			alignment = isTrailing ? PositionConstants.RIGHT : PositionConstants.LEFT;
		} else {
			alignment = isTrailing ? PositionConstants.BOTTOM : PositionConstants.TOP;
		}
		LineEndFigure lineEndFigure = new LineEndFigure(alignment, AbsolutePolicyUtils.COLOR_FEEDBACK);
		addMyFeedback(lineEndFigure);
		Dimension size = lineEndFigure.getSize();
		lineEndFigure.setLocation(new Point(point.x - size.width / 2, point.y - size.height / 2));
	}

	private void addMyFeedback(IFigure figure) {
		getFeedbacks().add(figure);
		// add feedback
		getLayer(IEditPartViewer.HANDLE_LAYER_SUB_2).add(figure);
	}

	private List<IFigure> getFeedbacks() {
		if (m_feedbacks == null) {
			m_feedbacks = new ArrayList<>();
		}
		return m_feedbacks;
	}

	/**
	 * @return the alignment figure for given component and axis.
	 */
	protected IFigure createAlignmentFigure(final IAbstractComponentInfo widget,
			final boolean isHorizontal) {
		IEditPartViewer viewer = getHost().getViewer();
		return isHorizontal
				? new HorizontalPopupFigure(viewer, widget)
						: new VerticalPopupFigure(viewer, widget);
	}

	/**
	 * Shows alignment figures for host {@link EditPart} and its siblings.
	 */
	public final void showAlignmentFigures() {
		if (m_alignmentFigures == null) {
			m_alignmentFigures = new ArrayList<>();
			// show cell figures for all children of host's parent
			{
				Collection<? extends EditPart> editParts = getHost().getParent().getChildren();
				for (EditPart editPart : editParts) {
					showAlignmentFigures(editPart);
				}
			}
		}
	}

	/**
	 * Hides alignment figures for this host and its siblings.
	 */
	public final void hideAlignmentFigures() {
		if (m_alignmentFigures != null) {
			for (IFigure figure : m_alignmentFigures) {
				figure.getParent().remove(figure);
			}
			m_alignmentFigures = null;
		}
	}

	/**
	 * Shows all possible alignment figures for given edit part.
	 */
	private void showAlignmentFigures(EditPart editPart) {
		// check model
		IAbstractComponentInfo widget;
		{
			Object model = editPart.getModel();
			if (!(model instanceof IAbstractComponentInfo)) {
				return;
			}
			widget = (IAbstractComponentInfo) model;
		}
		// check if we can show alignment figures for this control
		{
			String showFiguresString = null;
			if (!LayoutPolicyUtils.shouldShowSideFigures(showFiguresString, editPart)) {
				return;
			}
		}
		// show alignment figures
		{
			int offset = INITIAL_RIGHT_SPACE;
			{
				IFigure horizontalFigure = createAlignmentFigure(widget, true);
				if (horizontalFigure != null) {
					offset += horizontalFigure.getSize().width;
					addAlignmentFigure(widget, horizontalFigure, offset);
					offset += FIGURES_SPACE;
				}
			}
			{
				IFigure verticalFigure = createAlignmentFigure(widget, false);
				if (verticalFigure != null) {
					offset += verticalFigure.getSize().width;
					addAlignmentFigure(widget, verticalFigure, offset);
					offset += FIGURES_SPACE;
				}
			}
		}
	}

	/**
	 * Adds alignment figure at given offset from right side of component's cells.
	 */
	private void addAlignmentFigure(IAbstractComponentInfo component, IFigure figure, int offset) {
		IFigure layer = getLayer(IEditPartViewer.CLICKABLE_LAYER);
		// prepare rectangle for cells used by component (in layer coordinates)
		Rectangle cellRect;
		{
			cellRect = component.getModelBounds().getCopy();
			PolicyUtils.translateModelToFeedback(this, cellRect);
		}
		// prepare location and size
		Point figureLocation;
		{
			Dimension figureSize = figure.getSize();
			figureLocation = new Point(cellRect.right() - offset, cellRect.y - figureSize.height / 2);
			if (figureLocation.x < cellRect.x + MIN_LEFT_SPACE) {
				return;
			}
		}
		// add alignment figure
		layer.add(figure);
		figure.setLocation(figureLocation);
		m_alignmentFigures.add(figure);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Helper
	//
	////////////////////////////////////////////////////////////////////////////
	protected boolean isAttached(final IAbstractComponentInfo widget, final int side) {
		return ExecutionUtils.runObject(() -> m_layout.isAttached(widget, side));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Popup figures
	//
	////////////////////////////////////////////////////////////////////////////
	protected class HorizontalPopupFigure extends AbstractPopupFigure {
		private final IAbstractComponentInfo m_widget;

		protected HorizontalPopupFigure(IEditPartViewer viewer, IAbstractComponentInfo widget) {
			super(viewer, 9, 5);
			m_widget = widget;
		}

		@Override
		protected ImageDescriptor getImageDescriptor() {
			boolean isLeftAttached = isAttached(m_widget, PositionConstants.LEFT);
			boolean isRightAttached = isAttached(m_widget, PositionConstants.RIGHT);
			if (isLeftAttached && isRightAttached) {
				return CoreImages.ALIGNMENT_H_SMALL_FILL;
			} else if (isRightAttached) {
				return CoreImages.ALIGNMENT_H_SMALL_RIGHT;
			} else {
				return CoreImages.ALIGNMENT_H_SMALL_LEFT;
			}
		}

		@Override
		protected void fillMenu(IMenuManager manager) {
			new AnchorsActionsSupport(getPlacementsSupport()).fillAnchorsActions(
					manager,
					m_widget,
					true);
		}
	}
	protected class VerticalPopupFigure extends AbstractPopupFigure {
		private final IAbstractComponentInfo m_widget;

		protected VerticalPopupFigure(IEditPartViewer viewer, IAbstractComponentInfo widget) {
			super(viewer, 5, 9);
			m_widget = widget;
		}

		@Override
		protected ImageDescriptor getImageDescriptor() {
			boolean isTopAttached = isAttached(m_widget, PositionConstants.TOP);
			boolean isBottomAttached = isAttached(m_widget, PositionConstants.BOTTOM);
			if (isTopAttached && isBottomAttached) {
				return CoreImages.ALIGNMENT_V_SMALL_FILL;
			} else if (isBottomAttached) {
				return CoreImages.ALIGNMENT_V_SMALL_BOTTOM;
			} else {
				return CoreImages.ALIGNMENT_V_SMALL_TOP;
			}
		}

		@Override
		protected void fillMenu(IMenuManager manager) {
			new AnchorsActionsSupport(getPlacementsSupport()).fillAnchorsActions(
					manager,
					m_widget,
					false);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Abstract
	//
	////////////////////////////////////////////////////////////////////////////
	protected abstract ComponentAttachmentInfo getComponentAttachmentInfo(IAbstractComponentInfo widget,
			int side) throws Exception;
}