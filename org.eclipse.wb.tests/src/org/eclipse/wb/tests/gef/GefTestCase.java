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
package org.eclipse.wb.tests.gef;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.gef.EditPart;
import org.eclipse.swt.widgets.Display;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

/**
 * @author lobas_av
 *
 */
public abstract class GefTestCase extends Assertions {

	@BeforeEach
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