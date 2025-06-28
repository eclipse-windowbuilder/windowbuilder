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
package org.eclipse.wb.internal.swt.gef.policy.layout.form;

import org.eclipse.wb.core.gef.policy.PolicyUtils;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.gef.core.tools.ParentTargetDragEditPartTracker;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.draw2d.Label;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutPreferences;
import org.eclipse.wb.internal.swt.model.layout.form.IFormLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.geometry.Transposer;
import org.eclipse.gef.Request;

/**
 * Header edit part for FormLayout support.
 *
 * @author mitin_aa
 */
public class FormHeaderEditPart<C extends IControlInfo> extends GraphicalEditPart {
	private final boolean isHorizontal;
	private final IFormLayoutInfo<C> layout;
	private final Transposer t;
	private final IFigure containerFigure;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FormHeaderEditPart(IFormLayoutInfo<C> layout,
			Object model,
			boolean isHorizontal,
			IFigure containerFigure) {
		super();
		this.layout = layout;
		this.isHorizontal = isHorizontal;
		this.containerFigure = containerFigure;
		this.t = new Transposer(!isHorizontal);
		setModel(model);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Figure
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Figure createFigure() {
		return new PercentFigure(isHorizontal);
	}

	@Override
	protected void refreshVisuals() {
		int size = t.t(layout.getContainerSize()).width;
		int percent = getPercent(getModel());
		int marginOffset =
				isHorizontal ? FormUtils.getLayoutMarginLeft(layout) : FormUtils.getLayoutMarginTop(layout);
		int position = size * percent / 100 + marginOffset;
		Figure figure = getFigure();
		figure.setToolTip(new Label(percent + "%"));
		int figureSize = t.t(figure.getParent().getSize()).height;
		Rectangle bounds = t.t(new Rectangle(position - figureSize / 2, 0, figureSize, figureSize));
		figure.setBounds(translateModelToFeedback(bounds));
	}

	private int getPercent(Object model) {
		return ((FormLayoutPreferences.PercentsInfo) model).value;
	}

	/**
	 * @return the offset of {@link Figure} with headers relative to the absolute layer.
	 */
	private Point getOffset() {
		return FormHeaderLayoutEditPolicy.getOffset(containerFigure, layout.getComposite());
	}

	/**
	 * Converts "model" {@link Rectangle} into feedback coordinates.
	 */
	private Rectangle translateModelToFeedback(Rectangle r) {
		if (isHorizontal) {
			PolicyUtils.modelToFeedback_rightToLeft(r, layout.getComposite());
		}
		r = t.t(r);
		return t.t(r.getTranslated(t.t(getOffset()).x, 0));
	}

	@Override
	public final Tool getDragTracker(Request request) {
		return new ParentTargetDragEditPartTracker(this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Figure Class
	//
	////////////////////////////////////////////////////////////////////////////
	final static class PercentFigure extends Figure {
		private final Transposer m_t;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public PercentFigure(boolean isHorizontal) {
			m_t = new Transposer(!isHorizontal);
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Figure
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		protected void paintClientArea(Graphics graphics) {
			Dimension size = m_t.t(getSize());
			graphics.setBackgroundColor(ColorConstants.buttonDarker);
			PointList points = new PointList();
			points.addPoint(m_t.t(new Point(3, size.height / 2)));
			points.addPoint(m_t.t(new Point(size.width / 2, size.height)));
			points.addPoint(m_t.t(new Point(11, size.height / 2)));
			graphics.fillPolygon(points);
		}
	}
}
