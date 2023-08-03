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
package org.eclipse.wb.tests.draw2d;

import org.eclipse.wb.draw2d.border.LineBorder;

import org.eclipse.draw2d.geometry.Insets;

import org.junit.Test;

/**
 * @author lobas_av
 *
 */
public class LineBorderTest extends Draw2dFigureTestCase {

	////////////////////////////////////////////////////////////////////////////
	//
	// LineBorder test's
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_constructor() throws Exception {
		LineBorder border = new LineBorder();
		// check init state properties for new border
		assertNull(border.getColor());
		assertEquals(1, border.getWidth());
		assertEquals(new Insets(1), border.getInsets(null));
	}

	@Test
	public void test_constructor_int() throws Exception {
		LineBorder border = new LineBorder(3);
		// check init state properties for border constructor(int)
		assertNull(border.getColor());
		assertEquals(3, border.getWidth());
		assertEquals(new Insets(3), border.getInsets(null));
	}

	@Test
	public void test_constructor_Color() throws Exception {
		LineBorder border = new LineBorder(red);
		// check init state properties for border constructor(Color)
		assertSame(red, border.getColor());
		assertEquals(1, border.getWidth());
		assertEquals(new Insets(1), border.getInsets(null));
	}

	@Test
	public void test_constructor_Color_int() throws Exception {
		// check init state properties for border constructor(int, Color)
		LineBorder border = new LineBorder(blue, 7);
		assertSame(blue, border.getColor());
		assertEquals(7, border.getWidth());
		assertEquals(new Insets(7), border.getInsets(null));
	}
}