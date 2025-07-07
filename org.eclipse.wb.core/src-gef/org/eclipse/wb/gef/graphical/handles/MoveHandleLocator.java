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
package org.eclipse.wb.gef.graphical.handles;

import org.eclipse.wb.draw2d.FigureUtils;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Locator;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * A Locator used to place {@link MoveHandle}s. By default, a MoveHandle's bounds are equal to its
 * owner figure's bounds, expanded by the handle's {@link Insets}.
 *
 * @author lobas_av
 * @coverage gef.graphical
 */
public class MoveHandleLocator implements Locator {
	private final IFigure m_reference;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates a new {@link MoveHandleLocator} and sets its reference figure to <code>ref</code>. The
	 * reference figure should be the handle's owner figure.
	 */
	public MoveHandleLocator(IFigure reference) {
		m_reference = reference;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Locator
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets the handle's bounds to that of its owner figure's bounds, expanded by the handle's
	 * {@link Insets}.
	 */
	@Override
	public void relocate(IFigure target) {
		Rectangle bounds = m_reference.getBounds().getResized(-1, -1);
		FigureUtils.translateFigureToFigure(m_reference, target, bounds);
		//
		bounds.expand(target.getInsets());
		bounds.resize(1, 1);
		target.setBounds(bounds);
	}
}