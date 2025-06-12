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
package org.eclipse.wb.tests.designer.swing.model;

import org.eclipse.wb.internal.swing.model.CoordinateUtils;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link CoordinateUtils}.
 *
 * @author scheglov_ke
 */
public class CoordinateUtilsTest extends DesignerTestCase {
	////////////////////////////////////////////////////////////////////////////
	//
	// AWT -> draw2d
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link CoordinateUtils#get(java.awt.Point)}.
	 */
	@Test
	public void test_toDraw2d_Point() throws Exception {
		assertEquals(
				new org.eclipse.draw2d.geometry.Point(1, 2),
				CoordinateUtils.get(new java.awt.Point(1, 2)));
	}

	/**
	 * Test for {@link CoordinateUtils#get(java.awt.Rectangle)}.
	 */
	@Test
	public void test_toDraw2d_Rectangle() throws Exception {
		assertEquals(
				new org.eclipse.draw2d.geometry.Rectangle(1, 2, 3, 4),
				CoordinateUtils.get(new java.awt.Rectangle(1, 2, 3, 4)));
	}

	/**
	 * Test for {@link CoordinateUtils#get(java.awt.Dimension)}.
	 */
	@Test
	public void test_toDraw2d_Dimension() throws Exception {
		assertEquals(
				new org.eclipse.draw2d.geometry.Dimension(1, 2),
				CoordinateUtils.get(new java.awt.Dimension(1, 2)));
	}

	/**
	 * Test for {@link CoordinateUtils#get(java.awt.Insets)}.
	 */
	@Test
	public void test_toDraw2d_Insets() throws Exception {
		assertEquals(
				new org.eclipse.draw2d.geometry.Insets(1, 2, 3, 4),
				CoordinateUtils.get(new java.awt.Insets(1, 2, 3, 4)));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// draw2d -> AWT
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link CoordinateUtils#get(org.eclipse.draw2d.geometry.Insets)}.
	 */
	@Test
	public void test_toAWT_Insets() throws Exception {
		assertEquals(
				new java.awt.Insets(1, 2, 3, 4),
				CoordinateUtils.get(new org.eclipse.draw2d.geometry.Insets(1, 2, 3, 4)));
	}
}
