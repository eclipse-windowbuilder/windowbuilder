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
package org.eclipse.wb.core.controls;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

/**
 * Horizontal or vertical line.
 *
 * @author scheglov_ke
 * @coverage core.control
 */
public final class LineControl extends Canvas {
	private final boolean m_horizontal;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public LineControl(Composite parent, int style) {
		super(parent, style);
		m_horizontal = (style & SWT.HORIZONTAL) != 0;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Size
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Point computeSize(int hint, int hint2, boolean changed) {
		return new Point(m_horizontal ? hint : 1, m_horizontal ? 1 : hint2);
	}
}
