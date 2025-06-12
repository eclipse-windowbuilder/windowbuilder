/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.tests.designer.swt.model.property;

import org.eclipse.wb.internal.core.model.property.converter.ExpressionConverter;
import org.eclipse.wb.internal.swt.model.property.converter.PointConverter;
import org.eclipse.wb.internal.swt.model.property.converter.RectangleConverter;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Point;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for SWT converters.
 *
 * @author scheglov_ke
 */
public class ConvertersTest extends RcpModelTest {
	private CompositeInfo shell;

	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();
		shell =
				parseComposite(
						"// filler filler filler",
						"public class Test extends Shell {",
						"  public Test() {",
						"  }",
						"}");
	}

	@Override
	@AfterEach
	public void tearDown() throws Exception {
		shell = null;
		super.tearDown();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_RectangleConverter() throws Exception {
		ExpressionConverter converter = RectangleConverter.INSTANCE;
		assertEquals("(org.eclipse.swt.graphics.Rectangle) null", converter.toJavaSource(shell, null));
		assertEquals(
				"new org.eclipse.swt.graphics.Rectangle(1, 2, 3, 4)",
				converter.toJavaSource(shell, new Rectangle(1, 2, 3, 4)));
	}

	@Test
	public void test_PointConverter() throws Exception {
		ExpressionConverter converter = PointConverter.INSTANCE;
		assertEquals("(org.eclipse.swt.graphics.Point) null", converter.toJavaSource(shell, null));
		assertEquals(
				"new org.eclipse.swt.graphics.Point(1, 2)",
				converter.toJavaSource(shell, new Point(1, 2)));
	}
}