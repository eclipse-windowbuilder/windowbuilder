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
package org.eclipse.wb.internal.swt.gef.policy.layout.form;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.gef.policy.PolicyUtils;
import org.eclipse.wb.core.gef.policy.layout.LayoutPolicyUtils;
import org.eclipse.wb.core.gef.policy.layout.generic.AbstractPopupFigure;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.internal.core.gef.policy.snapping.PlacementUtils;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutInfoImplClassic;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.resource.ImageDescriptor;

import org.apache.commons.lang.exception.NestableError;

import java.util.Collection;
import java.util.List;

/**
 * Maintains alignment menus around control selection.
 *
 * @author mitin_aa
 */
public final class AnchorFiguresClassic<C extends IControlInfo> {
	// fields
	private List<Figure> m_alignmentFigures;
	private final SelectionEditPolicy m_policy;
	private final FormLayoutInfoImplClassic<C> m_layoutImpl;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AnchorFiguresClassic(SelectionEditPolicy policy, FormLayoutInfoImplClassic<C> impl) {
		m_policy = policy;
		m_layoutImpl = impl;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Shows alignment figures for host {@link EditPart} and its siblings.
	 */
	public final void show() {
		if (m_alignmentFigures == null) {
			m_alignmentFigures = Lists.newArrayList();
			// show alignment figures for all of the children of the host's parent
			{
				Collection<EditPart> editParts = m_policy.getHost().getParent().getChildren();
				for (EditPart editPart : editParts) {
					showAlignmentFigures(editPart);
				}
			}
		}
	}

	/**
	 * Hides alignment figures for this host and its siblings.
	 */
	public final void hide() {
		if (m_alignmentFigures != null) {
			for (Figure figure : m_alignmentFigures) {
				figure.getParent().remove(figure);
			}
			m_alignmentFigures = null;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Figures
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the alignment figure for given component and axis.
	 */
	private Figure createAlignmentFigure(C widget, int side) {
		IEditPartViewer viewer = m_policy.getHost().getViewer();
		return PlacementUtils.isHorizontalSide(side)
				? new HorizontalPopupFigure(viewer, widget, side)
						: new VerticalPopupFigure(viewer, widget, side);
	}

	/**
	 * Shows all possible alignment figures for given edit part.
	 */
	@SuppressWarnings("unchecked")
	private void showAlignmentFigures(EditPart editPart) {
		// check model
		C widget;
		{
			Object model = editPart.getModel();
			if (!(model instanceof IControlInfo)) {
				return;
			}
			widget = (C) model;
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
			{
				Figure figure = createAlignmentFigure(widget, IPositionConstants.LEFT);
				if (figure != null) {
					addAlignmentFigure(widget, figure, true, true);
				}
			}
			{
				Figure figure = createAlignmentFigure(widget, IPositionConstants.RIGHT);
				if (figure != null) {
					addAlignmentFigure(widget, figure, false, true);
				}
			}
			{
				Figure figure = createAlignmentFigure(widget, IPositionConstants.TOP);
				if (figure != null) {
					addAlignmentFigure(widget, figure, true, false);
				}
			}
			{
				Figure figure = createAlignmentFigure(widget, IPositionConstants.BOTTOM);
				if (figure != null) {
					addAlignmentFigure(widget, figure, false, false);
				}
			}
		}
	}

	/**
	 * Adds alignment figure at given offset from right side of component's cells.
	 */
	private void addAlignmentFigure(C component,
			Figure figure,
			boolean isLeading,
			boolean isHorisontal) {
		Figure layer = m_policy.getHost().getViewer().getLayer(IEditPartViewer.CLICKABLE_LAYER);
		// prepare rectangle for cells used by component (in layer coordinates)
		Rectangle widgetRect;
		{
			widgetRect = component.getModelBounds().getCopy();
			PolicyUtils.translateModelToFeedback(m_policy, widgetRect);
		}
		// prepare location and size
		Point figureLocation;
		{
			Dimension figureSize = figure.getSize();
			figureLocation = new Point();
			if (isHorisontal) {
				if (isLeading) {
					figureLocation.x = widgetRect.x - figureSize.width - 3;
					figureLocation.y = widgetRect.y + widgetRect.height / 2 - figureSize.height / 2;
				} else {
					figureLocation.x = widgetRect.right() + 3;
					figureLocation.y = widgetRect.y + widgetRect.height / 2 - figureSize.height / 2;
				}
			} else {
				if (isLeading) {
					figureLocation.x = widgetRect.x + widgetRect.width / 2 - figureSize.width / 2;
					figureLocation.y = widgetRect.y - figureSize.height - 3;
				} else {
					figureLocation.x = widgetRect.x + widgetRect.width / 2 - figureSize.width / 2;
					figureLocation.y = widgetRect.bottom() + 3;
				}
			}
			// don't draw if the widget is too small
			if (widgetRect.width < figureSize.width + 6 || widgetRect.height < figureSize.height + 4) {
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
	// Inner classes
	//
	////////////////////////////////////////////////////////////////////////////
	private final class HorizontalPopupFigure extends AbstractPopupFigure {
		private final C m_widget;
		private final int m_side;

		protected HorizontalPopupFigure(IEditPartViewer viewer, C widget, int side) {
			super(viewer, 9, 5);
			m_widget = widget;
			m_side = side;
		}

		@Override
		protected ImageDescriptor getImageDescriptor() {
			return m_layoutImpl.getAnchorActions().getImageHorizontal(m_widget, m_side);
		}

		@Override
		protected void fillMenu(IMenuManager manager) {
			m_layoutImpl.getAnchorActions().fillMenuHorizontal(m_widget, m_side, manager);
		}
	}
	private final class VerticalPopupFigure extends AbstractPopupFigure {
		private final C m_widget;
		private final int m_side;

		protected VerticalPopupFigure(IEditPartViewer viewer, C widget, int side) {
			super(viewer, 5, 9);
			m_widget = widget;
			m_side = side;
		}

		@Override
		protected ImageDescriptor getImageDescriptor() {
			try {
				return m_layoutImpl.getAnchorActions().getImageVertical(m_widget, m_side);
			} catch (Throwable e) {
				throw new NestableError(e);
			}
		}

		@Override
		protected void fillMenu(IMenuManager manager) {
			m_layoutImpl.getAnchorActions().fillMenuVertical(m_widget, m_side, manager);
		}
	}
}
