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
package org.eclipse.wb.tests.draw2d;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Interval;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import org.junit.jupiter.api.Assertions;


/**
 * @author lobas_av
 *
 */
public abstract class Draw2dTestCase extends Assertions {

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Asserts that two objects are equal. Expected object <code>(width, height)</code>. Actual object
	 * <code>{@link Dimension}</code>. If they are not an AssertionError is thrown.
	 */
	public static final void assertEquals(int width, int height, Dimension dimension)
			throws Exception {
		assertEquals(width, dimension.width);
		assertEquals(height, dimension.height);
	}

	/**
	 * Asserts that two objects are equal. Expected object <code>(top, left, bottom, right)</code>.
	 * Actual object <code>{@link Insets}</code>. If they are not an AssertionError is thrown.
	 */
	public static final void assertEquals(int top, int left, int bottom, int right, Insets insets)
			throws Exception {
		assertEquals(top, insets.top);
		assertEquals(left, insets.left);
		assertEquals(bottom, insets.bottom);
		assertEquals(right, insets.right);
	}

	/**
	 * Asserts that two objects are equal. Expected object <code>(x, y)</code>. Actual object
	 * <code>{@link Point}</code>. If they are not an AssertionError is thrown.
	 */
	public static final void assertEquals(int x, int y, Point point) throws Exception {
		assertEquals(x, point.x);
		assertEquals(y, point.y);
	}

	/**
	 * Asserts that two objects are equal. Expected object <code>(begin, length)</code>. Actual object
	 * <code>{@link Interval}</code>. If they are not an AssertionError is thrown.
	 */
	public static final void assertEquals(int begin, int length, Interval interval) throws Exception {
		assertEquals(begin, interval.begin());
		assertEquals(length, interval.length());
	}

	/**
	 * Asserts that two objects are equal. Expected object <code>(x, y, width, height)</code>. Actual
	 * object <code>{@link Rectangle}</code>. If they are not an AssertionError is thrown.
	 */
	public static final void assertEquals(int x, int y, int width, int height, Rectangle rectangle)
			throws Exception {
		assertEquals(x, rectangle.x);
		assertEquals(y, rectangle.y);
		assertEquals(width, rectangle.width);
		assertEquals(height, rectangle.height);
	}
}