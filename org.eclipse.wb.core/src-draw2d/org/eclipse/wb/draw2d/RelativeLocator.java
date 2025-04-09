/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.draw2d;

import org.eclipse.draw2d.geometry.Rectangle;

/**
 * Implementation of {@link AbstractRelativeLocator} that uses some {@link Figure} as reference.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public final class RelativeLocator extends AbstractRelativeLocator {
	private final Figure m_reference;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public RelativeLocator(Figure reference, double relativeX, double relativeY) {
		super(relativeX, relativeY);
		m_reference = reference;
	}

	public RelativeLocator(Figure reference, int location) {
		super(location);
		m_reference = reference;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// AbstractRelativeLocator
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Rectangle getReferenceRectangle() {
		Rectangle bounds = m_reference.getBounds().getCopy();
		FigureUtils.translateFigureToAbsolute(m_reference, bounds);
		return bounds;
	}
}