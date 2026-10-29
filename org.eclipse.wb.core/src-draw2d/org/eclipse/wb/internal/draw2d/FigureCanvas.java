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
package org.eclipse.wb.internal.draw2d;

import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * A Canvas that contains {@link IFigure Figures}.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public class FigureCanvas extends org.eclipse.draw2d.FigureCanvas {

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FigureCanvas(Composite parent, int style) {
		super(parent, style | SWT.DOUBLE_BUFFERED);
		setDefaultEventManager();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// FigureCanvas
	//
	////////////////////////////////////////////////////////////////////////////

	protected void setDefaultEventManager() {
		getLightweightSystem().setEventDispatcher(new EventManager(this));
	}
}