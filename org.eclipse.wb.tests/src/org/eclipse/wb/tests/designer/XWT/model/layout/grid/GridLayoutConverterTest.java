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
package org.eclipse.wb.tests.designer.XWT.model.layout.grid;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.model.layout.grid.GridLayoutConverter;
import org.eclipse.wb.internal.xwt.model.layout.grid.GridDataInfo;
import org.eclipse.wb.internal.xwt.model.layout.grid.GridLayoutInfo;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.layout.GridLayout;

import org.junit.Test;

/**
 * Tests for {@link GridLayoutConverter}.
 *
 * @author scheglov_ke
 */
public class GridLayoutConverterTest extends XwtModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Exit zone :-) XXX
	//
	////////////////////////////////////////////////////////////////////////////
	public void _test_exit() throws Exception {
		System.exit(0);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * No controls.
	 */
	@Test
	public void test_empty() throws Exception {
		CompositeInfo shell = parse("<Shell/>");
		setGridLayout(shell, new String[]{
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <GridLayout/>",
				"  </Shell.layout>",
		"</Shell>"}, new Rectangle[]{});
	}

	/**
	 * Control in single column, in normal order.
	 */
	@Test
	public void test_singleColumn_normalOrder() throws Exception {
		CompositeInfo shell =
				parse(
						"<Shell>",
						"  <Button text='0' bounds='10, 10, 100, 20'/>",
						"  <Button text='1' bounds='20, 40, 100, 20'/>",
						"</Shell>");
		setGridLayout(shell, new String[]{
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <GridLayout/>",
				"  </Shell.layout>",
				"  <Button text='0'/>",
				"  <Button text='1'/>",
		"</Shell>"}, new Rectangle[]{new Rectangle(0, 0, 1, 1), new Rectangle(0, 1, 1, 1)});
	}

	/**
	 * Control in single column, in reverse order.
	 */
	@Test
	public void test_singleColumn_reverseOrder() throws Exception {
		CompositeInfo shell =
				parse(
						"<Shell>",
						"  <Button text='1' bounds='20, 40, 100, 20'/>",
						"  <Button text='0' bounds='10, 10, 100, 20'/>",
						"</Shell>");
		setGridLayout(shell, new String[]{
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <GridLayout/>",
				"  </Shell.layout>",
				"  <Button text='0'/>",
				"  <Button text='1'/>",
		"</Shell>"}, new Rectangle[]{new Rectangle(0, 0, 1, 1), new Rectangle(0, 1, 1, 1)});
	}

	/**
	 * Control in two columns, no fillers.
	 */
	@Test
	public void test_twoRows_noFillers() throws Exception {
		CompositeInfo shell =
				parse(
						"<Shell>",
						"  <Button text='0' bounds='10, 10, 100, 20'/>",
						"  <Button text='1' bounds='120, 15, 100, 20'/>",
						"</Shell>");
		setGridLayout(shell, new String[]{
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <GridLayout numColumns='2'/>",
				"  </Shell.layout>",
				"  <Button text='0'/>",
				"  <Button text='1'/>",
		"</Shell>"}, new Rectangle[]{new Rectangle(0, 0, 1, 1), new Rectangle(1, 0, 1, 1)});
	}

	/**
	 * Control in two columns/rows, on diagonal, with fillers.
	 */
	@Test
	public void test_twoRows_withFillers() throws Exception {
		CompositeInfo shell =
				parse(
						"<Shell>",
						"  <Button text='0' bounds='10, 10, 100, 20'/>",
						"  <Button text='1' bounds='120, 40, 100, 20'/>",
						"</Shell>");
		setGridLayout(shell, new String[]{
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <GridLayout numColumns='2'/>",
				"  </Shell.layout>",
				"  <Button text='0'/>",
				"  <Label/>",
				"  <Label/>",
				"  <Button text='1'/>",
		"</Shell>"}, new Rectangle[]{new Rectangle(0, 0, 1, 1), new Rectangle(1, 1, 1, 1)});
	}

	/**
	 * Three controls, one spanned horizontally.
	 */
	@Test
	public void test_spanHorizontal() throws Exception {
		CompositeInfo shell =
				parse(
						"<Shell>",
						"  <Button text='0' bounds='10, 10, 10, 10'/>",
						"  <Button text='1' bounds='30, 10, 10, 10'/>",
						"  <Button text='2' bounds='10, 30, 30, 10'/>",
						"</Shell>");
		setGridLayout(shell, new String[]{
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <GridLayout numColumns='2'/>",
				"  </Shell.layout>",
				"  <Button text='0'/>",
				"  <Button text='1'/>",
				"  <Button text='2'>",
				"    <Button.layoutData>",
				"      <GridData horizontalSpan='2'/>",
				"    </Button.layoutData>",
				"  </Button>",
		"</Shell>"}, new Rectangle[]{
				new Rectangle(0, 0, 1, 1),
				new Rectangle(1, 0, 1, 1),
				new Rectangle(0, 1, 2, 1)});
	}

	/**
	 * Three controls, one spanned vertically.
	 */
	@Test
	public void test_spanVertical() throws Exception {
		CompositeInfo shell =
				parse(
						"<Shell>",
						"  <Button text='0' bounds='10, 10, 10, 10'/>",
						"  <Button text='1' bounds='30, 10, 10, 30'/>",
						"  <Button text='2' bounds='10, 30, 10, 10'/>",
						"</Shell>");
		setGridLayout(shell, new String[]{
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <GridLayout numColumns='2'/>",
				"  </Shell.layout>",
				"  <Button text='0'/>",
				"  <Button text='1'>",
				"    <Button.layoutData>",
				"      <GridData verticalSpan='2'/>",
				"    </Button.layoutData>",
				"  </Button>",
				"  <Button text='2'/>",
		"</Shell>"}, new Rectangle[]{
				new Rectangle(0, 0, 1, 1),
				new Rectangle(1, 0, 1, 2),
				new Rectangle(0, 1, 1, 1)});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets the {@link GridLayout} for given {@link CompositeInfo}.
	 */
	private void setGridLayout(CompositeInfo composite, String[] expectedLines, Rectangle[] cells)
			throws Exception {
		GridLayoutInfo layout = setGridLayout(composite, expectedLines);
		// check cells for control's
		int cellIndex = 0;
		for (ControlInfo control : composite.getChildrenControls()) {
			if (!layout.isFiller(control)) {
				GridDataInfo gridData = GridLayoutInfo.getGridData(control);
				int x = getInt(gridData, "x");
				int y = getInt(gridData, "y");
				int width = getInt(gridData, "width");
				int height = getInt(gridData, "height");
				assertEquals(cells[cellIndex++], new Rectangle(x, y, width, height));
			}
		}
	}

	private GridLayoutInfo setGridLayout(CompositeInfo composite, String[] expectedLines)
			throws Exception {
		composite.getRoot().refresh();
		// set GridLayout
		GridLayoutInfo gridLayout = createObject("org.eclipse.swt.layout.GridLayout");
		composite.setLayout(gridLayout);
		// check source
		assertXML(expectedLines);
		return gridLayout;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the <code>int</code> value of field with given name.
	 */
	private static int getInt(GridDataInfo gridData, String fieldName) throws Exception {
		return ReflectionUtils.getFieldInt(gridData, fieldName);
	}
}
