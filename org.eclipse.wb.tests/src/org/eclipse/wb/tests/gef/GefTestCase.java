/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.gef;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.gef.EditPart;
import org.eclipse.swt.widgets.Display;

import org.junit.Assert;
import org.junit.Before;

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
		ReflectionUtils.invokeMethod(parent, "addChild(org.eclipse.gef.EditPart,int)", child, -1);
	}
}