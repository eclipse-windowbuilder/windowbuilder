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
package org.eclipse.wb.internal.layout.group.gef;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.Polyline;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.AbsolutePolicyUtils;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;

import org.netbeans.modules.form.layoutdesign.IFeedbacksDrawer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link IFeedbacksDrawer}. Draws and maintains feedbacks engaged by GL engine.
 *
 * @author mitin_aa
 */
final class FeedbacksDrawer implements IFeedbacksDrawer {
	private final IFeedbacksHelper m_helper;
	private final List<IFigure> m_feedbacks = new ArrayList<>();
	private static final Image[] m_images = {null, null, null};

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FeedbacksDrawer(IFeedbacksHelper h) {
		m_helper = h;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IFeedbackDrawer
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void drawLine(int x1, int y1, int x2, int y2) {
		Polyline line = createLineFeedback(x1, y1, x2, y2);
		line.setForegroundColor(AbsolutePolicyUtils.COLOR_FEEDBACK);
		m_feedbacks.add(line);
	}

	@Override
	public void fillArc(int x,
			int y,
			final int width,
			final int height,
			final int startAngle,
			final int arcAngle) {
		IFigure figure = new Figure() {
			@Override
			protected void paintClientArea(Graphics graphics) {
				graphics.setBackgroundColor(AbsolutePolicyUtils.COLOR_FEEDBACK);
				graphics.setAntialias(SWT.ON);
				graphics.fillArc(1, 1, width - 1, height - 1, startAngle, arcAngle);
			}
		};
		Point point = new Point(x, y);
		m_helper.translateModelToFeedback(point);
		figure.setBounds(new Rectangle(point.x, point.y, width, height));
		m_helper.addFeedback2(figure);
		m_feedbacks.add(figure);
	}

	@Override
	public void drawLinkBadge(int x, int y, int dimension) {
		final Image image = getImage(dimension);
		IFigure figure = new Figure() {
			@Override
			protected void paintClientArea(Graphics graphics) {
				// draw image
				if (image != null) {
					graphics.drawImage(image, 0, 0);
				}
			}
		};
		org.eclipse.swt.graphics.Rectangle imageBounds = image.getBounds();
		Rectangle bounds =
				new Rectangle(x - imageBounds.width - 2,
						y - imageBounds.height - 2,
						imageBounds.width,
						imageBounds.height);
		m_helper.translateModelToFeedback(bounds);
		figure.setBounds(bounds);
		m_feedbacks.add(figure);
		m_helper.addFeedback2(figure);
	}

	private Image getImage(int dimension) {
		if (m_images[dimension] == null) {
			m_images[dimension] = loadImage("linked" + dimension + ".png");
		}
		return m_images[dimension];
	}

	private Image loadImage(String name) {
		InputStream resourceAsStream = getClass().getResourceAsStream(name);
		return new Image(null, resourceAsStream);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Feedbacks drawing
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates line figure, translates given model coordinates into feedback and adds created line
	 * into feedback layer
	 */
	protected final Polyline createLineFeedback(int x1, int y1, int x2, int y2) {
		// prepare points
		Point begin = new Point(x1, y1);
		Point end = new Point(x2, y2);
		m_helper.translateModelToFeedback(begin);
		m_helper.translateModelToFeedback(end);
		// create feedback
		Polyline line = new Polyline();
		line.addPoint(begin);
		line.addPoint(end);
		line.setLineStyle(SWT.LINE_DOT);
		m_helper.addFeedback2(line);
		return line;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public void removeFeedbacks() {
		for (IFigure figure : m_feedbacks) {
			if (figure != null) {
				FigureUtils.removeFigure(figure);
			}
		}
		m_feedbacks.clear();
	}
}
