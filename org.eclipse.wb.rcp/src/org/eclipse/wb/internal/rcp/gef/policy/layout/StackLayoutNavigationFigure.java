/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.internal.rcp.gef.policy.layout;

import org.eclipse.wb.draw2d.Figure;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;

/**
 * {@link Figure} for selecting previous/next {@link Control} on {@link StackLayout}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public final class StackLayoutNavigationFigure extends Figure {
	public static final int WIDTH = 10;
	public static final int HEIGHT = 14;
	private static Image m_prevImage = new Image(null,
			StackLayoutNavigationFigure.class.getResourceAsStream("prev.png"));
	private static Image m_nextImage = new Image(null,
			StackLayoutNavigationFigure.class.getResourceAsStream("next.png"));
	private final StackLayoutSelectionEditPolicy<?> m_policy;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public StackLayoutNavigationFigure(StackLayoutSelectionEditPolicy<?> policy) {
		m_policy = policy;
		addMouseListener(new MouseListener.Stub() {
			@Override
			public void mousePressed(MouseEvent event) {
				event.consume();
				if (event.x < WIDTH) {
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