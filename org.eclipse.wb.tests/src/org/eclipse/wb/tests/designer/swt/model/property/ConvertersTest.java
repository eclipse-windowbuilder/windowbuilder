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
package org.eclipse.wb.tests.designer.swt.model.property;

import org.eclipse.wb.internal.core.model.property.converter.ExpressionConverter;
import org.eclipse.wb.internal.swt.model.property.converter.PointConverter;
import org.eclipse.wb.internal.swt.model.property.converter.RectangleConverter;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.support.PointSupport;
import org.eclipse.wb.internal.swt.support.RectangleSupport;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

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
	protected void setUp() throws Exception {
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
	protected void tearDown() throws Exception {
		shell = null;
		super.tearDown();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_RectangleConverter() throws Exception {
		ExpressionConverter converter = RectangleConverter.INSTANCE;
		assertEquals("(org.eclipse.swt.graphics.Rectangle) null", converter.toJavaSource(shell, null));
		assertEquals(
				"new org.eclipse.swt.graphics.Rectangle(1, 2, 3, 4)",
				converter.toJavaSource(shell, RectangleSupport.newRectangle(1, 2, 3, 4)));
	}

	public void test_PointConverter() throws Exception {
		ExpressionConverter converter = PointConverter.INSTANCE;
		assertEquals("(org.eclipse.swt.graphics.Point) null", converter.toJavaSource(shell, null));
		assertEquals(
				"new org.eclipse.swt.graphics.Point(1, 2)",
				converter.toJavaSource(shell, PointSupport.newPoint(1, 2)));
	}
}