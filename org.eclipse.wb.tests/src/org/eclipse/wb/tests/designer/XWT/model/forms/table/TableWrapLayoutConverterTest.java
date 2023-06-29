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
package org.eclipse.wb.tests.designer.XWT.model.forms.table;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.TableWrapLayoutConverter;
import org.eclipse.wb.internal.xwt.model.forms.layout.table.TableWrapDataInfo;
import org.eclipse.wb.internal.xwt.model.forms.layout.table.TableWrapLayoutInfo;
import org.eclipse.wb.internal.xwt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/**
 * Tests for {@link TableWrapLayoutConverter}.
 *
 * @author scheglov_ke
 */
public class TableWrapLayoutConverterTest extends XwtModelTest {
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
	// Source
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getTestSource_namespaces() {
		return super.getTestSource_namespaces()
				+ " xmlns:f='clr-namespace:org.eclipse.ui.forms.widgets'";
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * No controls.
	 */
	public void test_empty() throws Exception {
		CompositeInfo shell = parse("<Shell/>");
		setGridLayout(shell, new String[]{
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <f:TableWrapLayout/>",
				"  </Shell.layout>",
		"</Shell>"}, new Rectangle[]{});
	}

	/**
	 * Control in single column, in normal order.
	 */
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
				"    <f:TableWrapLayout/>",
				"  </Shell.layout>",
				"  <Button text='0'/>",
				"  <Button text='1'/>",
		"</Shell>"}, new Rectangle[]{new Rectangle(0, 0, 1, 1), new Rectangle(0, 1, 1, 1)});
	}

	/**
	 * Control in single column, in reverse order.
	 */
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
				"    <f:TableWrapLayout/>",
				"  </Shell.layout>",
				"  <Button text='0'/>",
				"  <Button text='1'/>",
		"</Shell>"}, new Rectangle[]{new Rectangle(0, 0, 1, 1), new Rectangle(0, 1, 1, 1)});
	}

	/**
	 * Control in two columns, no fillers.
	 */
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
				"    <f:TableWrapLayout numColumns='2'/>",
				"  </Shell.layout>",
				"  <Button text='0'/>",
				"  <Button text='1'/>",
		"</Shell>"}, new Rectangle[]{new Rectangle(0, 0, 1, 1), new Rectangle(1, 0, 1, 1)});
	}

	/**
	 * Control in two columns/rows, on diagonal, with fillers.
	 */
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
				"    <f:TableWrapLayout numColumns='2'/>",
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
				"    <f:TableWrapLayout numColumns='2'/>",
				"  </Shell.layout>",
				"  <Button text='0'/>",
				"  <Button text='1'/>",
				"  <Button text='2'>",
				"    <Button.layoutData>",
				"      <f:TableWrapData colspan='2'/>",
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
				"    <f:TableWrapLayout numColumns='2'/>",
				"  </Shell.layout>",
				"  <Button text='0'/>",
				"  <Button text='1'>",
				"    <Button.layoutData>",
				"      <f:TableWrapData rowspan='2'/>",
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
	// Switching layouts
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test switching layouts from {@link GridLayout} to {@link TableWrapLayout}, and restore
	 * component positions & alignments.
	 */
	public void _test_Switching_fromGridLayout() throws Exception {
		CompositeInfo composite =
				parse(
						"// filler filler filler filler filler",
						"<Shell>",
						"  <Shell.layout>",
						"    <GridLayout wbp:name='layout' numColumns='3'/>",
						"  </Shell.layout>",
						"  <Button wbp:name='button_0'>",
						"    <Button.layoutData>",
						"      <GridData verticalAlignment='BOTTOM'/>",
						"    </Button.layoutData>",
						"  </Button>",
						"  <Label/>",
						"  <Label/>",
						"  <Button wbp:name='button_1'>",
						"    <Button.layoutData>",
						"      <GridData horizontalSpan='2'/>",
						"    </Button.layoutData>",
						"  </Button>",
						"  <Label/>",
						"  <Label/>",
						"  <Label/>",
						"  <Button wbp:name='button_2'/>",
						"</Shell>");
		refresh();
		// set TableWrapLayout
		TableWrapLayoutInfo tableWrapLayout =
				createObject("org.eclipse.ui.forms.widgets.TableWrapLayout");
		composite.setLayout(tableWrapLayout);
		printEditorLinesSource();
		assertXML();
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
		TableWrapLayoutInfo layout = setGridLayout(composite, expectedLines);
		// check cells for control's
		int cellIndex = 0;
		for (ControlInfo control : composite.getChildrenControls()) {
			if (!layout.isFiller(control)) {
				TableWrapDataInfo gridData = (TableWrapDataInfo) LayoutInfo.getLayoutData(control);
				int x = getInt(gridData, "x");
				int y = getInt(gridData, "y");
				int width = getInt(gridData, "width");
				int height = getInt(gridData, "height");
				assertEquals(cells[cellIndex++], new Rectangle(x, y, width, height));
			}
		}
	}

	private TableWrapLayoutInfo setGridLayout(CompositeInfo composite, String[] expectedLines)
			throws Exception {
		composite.getRoot().refresh();
		// set GridLayout
		TableWrapLayoutInfo gridLayout = createObject("org.eclipse.ui.forms.widgets.TableWrapLayout");
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
	private static int getInt(TableWrapDataInfo layoutData, String fieldName) throws Exception {
		return ReflectionUtils.getFieldInt(layoutData, fieldName);
	}
}
