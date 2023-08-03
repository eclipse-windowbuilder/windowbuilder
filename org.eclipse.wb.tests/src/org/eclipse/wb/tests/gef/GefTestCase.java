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
package org.eclipse.wb.tests.gef;

import org.eclipse.wb.gef.core.EditPart;

import org.eclipse.swt.widgets.Display;

import org.junit.Assert;
import org.junit.Before;

import java.lang.reflect.Method;

/**
 * @author lobas_av
 *
 */
public abstract class GefTestCase extends Assert {

	@Before
	public void setUp() throws Exception {
		// check create display for initialize figure's colors
		Display.getDefault();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	protected static final void addChildEditPart(EditPart parent, EditPart child) throws Exception {
		Method method =
				EditPart.class.getDeclaredMethod("addChild", new Class[]{EditPart.class, int.class});
		method.setAccessible(true);
		method.invoke(parent, new Object[]{child, -1});
	}
}