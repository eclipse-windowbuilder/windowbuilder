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
package org.eclipse.wb.internal.swing.gef.policy.layout;

import org.eclipse.wb.draw2d.Figure;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Image;

/**
 * Figure for change current card selection component.
 *
 * @author lobas_av
 * @coverage swing.gef.policy
 */
public final class CardNavigationFigure extends Figure {
	public static final int WIDTH = 10;
	public static final int HEIGHT = 14;
	private static Image m_prevImage = new Image(null,
			CardNavigationFigure.class.getResourceAsStream("prev.png"));
	private static Image m_nextImage = new Image(null,
			CardNavigationFigure.class.getResourceAsStream("next.png"));
	private final CardLayoutSelectionEditPolicy m_policy;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public CardNavigationFigure(CardLayoutSelectionEditPolicy policy) {
		m_policy = policy;
		addMouseListener(new MouseListener.Stub() {
			@Override
			public void mousePressed(MouseEvent event) {
				event.consume();
				//
				Point location = Point.SINGLETON;
				location.setLocation(event.x, event.y);
				translateFromParent(location);
				//
				if (location.x < WIDTH) {
					m_policy.showPrevComponent();
				} else {
					m_policy.showNextComponent();
				}
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Figure
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void paintClientArea(Graphics graphics) {
		Rectangle r = getClientArea();
		graphics.drawImage(m_prevImage, r.x, r.y);
		graphics.drawImage(m_nextImage, r.x + WIDTH, r.y);
	}
}