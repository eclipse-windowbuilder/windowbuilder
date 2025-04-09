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
package org.eclipse.wb.tests.draw2d;

import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.widgets.Display;

import org.junit.Before;



/**
 * @author lobas_av
 *
 */
public abstract class Draw2dFigureTestCase extends DesignerTestCase implements ColorConstants {
	@Override
	@Before
	public void setUp() throws Exception {
		// check create display for initialize figure's colors
		Display.getDefault();
	}
}