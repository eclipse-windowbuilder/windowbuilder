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

import org.eclipse.wb.draw2d.border.MarginBorder;

import org.eclipse.draw2d.geometry.Insets;

import org.junit.Test;

/**
 * @author lobas_av
 *
 */
public class MarginBorderTest extends Draw2dFigureTestCase {

	////////////////////////////////////////////////////////////////////////////
	//
	// Test's
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_constructor() throws Exception {
		// check work constructor (int, int, int, int)
		assertEquals(new Insets(1, 2, 3, 4), new MarginBorder(new Insets(1, 2, 3, 4)).getInsets(null));
		//
		// check work constructor (int)
		assertEquals(new Insets(7, 7, 7, 7), new MarginBorder(7).getInsets(null));
	}
}