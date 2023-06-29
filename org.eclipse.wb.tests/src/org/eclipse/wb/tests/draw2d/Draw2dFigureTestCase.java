/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.draw2d;

import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.ICursorConstants;

import org.eclipse.swt.widgets.Display;

import junit.framework.TestCase;

/**
 * @author lobas_av
 *
 */
public abstract class Draw2dFigureTestCase extends TestCase
implements
IColorConstants,
ICursorConstants {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public Draw2dFigureTestCase(Class<?> _class) {
		super(_class.getName());
	}

	@Override
	protected void setUp() throws Exception {
		// check create display for initialize figure's colors
		Display.getDefault();
	}
}