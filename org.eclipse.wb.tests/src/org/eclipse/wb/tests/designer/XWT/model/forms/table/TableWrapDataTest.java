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

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.TableWrapLayoutImages;
import org.eclipse.wb.internal.xwt.model.forms.layout.table.TableWrapDataInfo;
import org.eclipse.wb.internal.xwt.model.forms.layout.table.TableWrapLayoutInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.forms.widgets.TableWrapData;

import org.junit.Test;

/**
 * Test for {@link TableWrapDataInfo}.
 *
 * @author scheglov_ke
 */
public class TableWrapDataTest extends XwtModelTest {
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
	// Images
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_getSmallAlignmentImage() throws Exception {
		parse(
				"<Shell>",
				"  <Shell.layout>",
				"    <f:TableWrapLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button'/>",
				"</Shell>");
		refresh();
		TableWrapLayoutInfo layout = getObjectByName("layout");
		ControlInfo button = getObjectByName("button");
		//
		TableWrapDataInfo layoutData = layout.getTableWrapData(button);
		check_getSmallAlignmentImage(layoutData, true, new int[]{
				TableWrapData.LEFT,
				TableWrapData.CENTER,
				TableWrapData.RIGHT,
				TableWrapData.FILL}, new String[]{"left.gif", "center.gif", "right.gif", "fill.gif"});
		check_getSmallAlignmentImage(layoutData, false, new int[]{
				TableWrapData.TOP,
				TableWrapData.MIDDLE,
				TableWrapData.BOTTOM,
				TableWrapData.FILL}, new String[]{"top.gif", "middle.gif", "bottom.gif", "fill.gif"});
	}

	private static void check_getSmallAlignmentImage(TableWrapDataInfo layoutData,
			boolean horizontal,
			int[] alignments,
			String[] paths) throws Exception {
		for (int i = 0; i < alignments.length; i++) {
			int alignment = alignments[i];
			Image expectedImage = TableWrapLayoutImages.getImage((horizontal ? "/h/" : "/v/") + paths[i]);
			if (horizontal) {
				layoutData.setHorizontalAlignment(alignment);
			} else {
				layoutData.setVerticalAlignment(alignment);
			}
			assertSame(expectedImage, layoutData.getSmallAlignmentImage(horizontal));
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Horizontal
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link TableWrapDataInfo#setHorizontalAlignment(int)}.
	 */
	@Test
	public void test_horizontalAlignment() throws Exception {
		parse(
				"<Shell>",
				"  <Shell.layout>",
				"    <f:TableWrapLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button'/>",
				"</Shell>");
		refresh();
		TableWrapLayoutInfo layout = getObjectByName("layout");
		ControlInfo button = getObjectByName("button");
		TableWrapDataInfo tableWrapData = layout.getTableWrapData(button);
		// initial state
		assertEquals(TableWrapData.LEFT, tableWrapData.getHorizontalAlignment());
		assertFalse(tableWrapData.getHorizontalGrab());
		// set CENTER
		tableWrapData.setHorizontalAlignment(TableWrapData.CENTER);
		assertEquals(TableWrapData.CENTER, tableWrapData.getHorizontalAlignment());
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <f:TableWrapLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button'>",
				"    <Button.layoutData>",
				"      <f:TableWrapData align='(org.eclipse.ui.forms.widgets.TableWrapData).CENTER'/>",
				"    </Button.layoutData>",
				"  </Button>",
				"</Shell>");
		// set RIGHT
		tableWrapData.setHorizontalAlignment(TableWrapData.RIGHT);
		assertEquals(TableWrapData.RIGHT, tableWrapData.getHorizontalAlignment());
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <f:TableWrapLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button'>",
				"    <Button.layoutData>",
				"      <f:TableWrapData align='(org.eclipse.ui.forms.widgets.TableWrapData).RIGHT'/>",
				"    </Button.layoutData>",
				"  </Button>",
				"</Shell>");
		// set FILL
		tableWrapData.setHorizontalAlignment(TableWrapData.FILL);
		assertEquals(TableWrapData.FILL, tableWrapData.getHorizontalAlignment());
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <f:TableWrapLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button'>",
				"    <Button.layoutData>",
				"      <f:TableWrapData align='(org.eclipse.ui.forms.widgets.TableWrapData).FILL'/>",
				"    </Button.layoutData>",
				"  </Button>",
				"</Shell>");
		// set LEFT
		tableWrapData.setHorizontalAlignment(TableWrapData.LEFT);
		assertEquals(TableWrapData.LEFT, tableWrapData.getHorizontalAlignment());
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <f:TableWrapLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button'/>",
				"</Shell>");
	}

	/**
	 * Sets horizontal grab in {@link TableWrapData#grabHorizontal} field.
	 */
	@Test
	public void test_horizontalGrab() throws Exception {
		parse(
				"<Shell>",
				"  <Shell.layout>",
				"    <f:TableWrapLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button'/>",
				"</Shell>");
		refresh();
		TableWrapLayoutInfo layout = getObjectByName("layout");
		ControlInfo button = getObjectByName("button");
		TableWrapDataInfo tableWrapData = layout.getTableWrapData(button);
		// initial state
		assertFalse(tableWrapData.getHorizontalGrab());
		// grab := true
		tableWrapData.setHorizontalGrab(true);
		assertTrue(tableWrapData.getHorizontalGrab());
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <f:TableWrapLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button'>",
				"    <Button.layoutData>",
				"      <f:TableWrapData grabHorizontal='true'/>",
				"    </Button.layoutData>",
				"  </Button>",
				"</Shell>");
		// grab := false
		tableWrapData.setHorizontalGrab(false);
		assertFalse(tableWrapData.getHorizontalGrab());
		assertXML(
				"<Shell>",
				"  <Shell.layout>",
				"    <f:TableWrapLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button'/>",
				"</Shell>");
	}

	/**
	 * Sets horizontal span in {@link TableWrapData} constructor.
	 */
	@Test
	public void test_horizontalSpan() throws Exception {
		parse(
				"<Shell>",
				"  <Shell.layout>",
				"    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button'/>",
				"</Shell>");
		refresh();
		TableWrapLayoutInfo layout = getObjectByName("layout");
		ControlInfo button = getObjectByName("button");
		TableWrapDataInfo tableWrapData = layout.getTableWrapData(button);
		// initial state
		assertEquals(1, tableWrapData.getHorizontalSpan());
		// horizontalSpan := 2
		tableWrapData.setHorizontalSpan(2);
		assertEquals(2, tableWrapData.getHorizontalSpan());
		assertXML(
				"<Shell>",
				"  <Shell.layout>",
				"    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button'>",
				"    <Button.layoutData>",
				"      <f:TableWrapData colspan='2'/>",
				"    </Button.layoutData>",
				"  </Button>",
				"</Shell>");
		// horizontalSpan := default
		tableWrapData.getPropertyByTitle("colspan").setValue(Property.UNKNOWN_VALUE);
		assertXML(
				"<Shell>",
				"  <Shell.layout>",
				"    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button'/>",
				"</Shell>");
		{
			tableWrapData = layout.getTableWrapData(button);
			assertEquals(1, tableWrapData.getHorizontalSpan());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Vertical
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets vertical alignment in {@link TableWrapData} constructor.
	 */
	@Test
	public void test_verticalAlignment_1() throws Exception {
		parse(
				"<Shell>",
				"  <Shell.layout>",
				"    <f:TableWrapLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button'/>",
				"</Shell>");
		refresh();
		TableWrapLayoutInfo layout = getObjectByName("layout");
		ControlInfo button = getObjectByName("button");
		TableWrapDataInfo tableWrapData = layout.getTableWrapData(button);
		// initial state
		assertEquals(TableWrapData.TOP, tableWrapData.getVerticalAlignment());
		assertFalse(tableWrapData.getVerticalGrab());
		// set MIDDLE
		tableWrapData.setVerticalAlignment(TableWrapData.MIDDLE);
		assertEquals(TableWrapData.MIDDLE, tableWrapData.getVerticalAlignment());
		assertXML(
				"<Shell>",
				"  <Shell.layout>",
				"    <f:TableWrapLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button'>",
				"    <Button.layoutData>",
				"      <f:TableWrapData valign='(org.eclipse.ui.forms.widgets.TableWrapData).MIDDLE'/>",
				"    </Button.layoutData>",
				"  </Button>",
				"</Shell>");
		// set BOTTOM
		tableWrapData.setVerticalAlignment(TableWrapData.BOTTOM);
		assertEquals(TableWrapData.BOTTOM, tableWrapData.getVerticalAlignment());
		assertXML(
				"<Shell>",
				"  <Shell.layout>",
				"    <f:TableWrapLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button'>",
				"    <Button.layoutData>",
				"      <f:TableWrapData valign='(org.eclipse.ui.forms.widgets.TableWrapData).BOTTOM'/>",
				"    </Button.layoutData>",
				"  </Button>",
				"</Shell>");
		// set FILL
		tableWrapData.setVerticalAlignment(TableWrapData.FILL);
		assertEquals(TableWrapData.FILL, tableWrapData.getVerticalAlignment());
		assertXML(
				"<Shell>",
				"  <Shell.layout>",
				"    <f:TableWrapLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button'>",
				"    <Button.layoutData>",
				"      <f:TableWrapData valign='(org.eclipse.ui.forms.widgets.TableWrapData).FILL'/>",
				"    </Button.layoutData>",
				"  </Button>",
				"</Shell>");
		// set TOP
		tableWrapData.setVerticalAlignment(TableWrapData.TOP);
		assertEquals(TableWrapData.TOP, tableWrapData.getVerticalAlignment());
		assertXML(
				"<Shell>",
				"  <Shell.layout>",
				"    <f:TableWrapLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button'/>",
				"</Shell>");
	}

	/**
	 * Sets vertical grab in {@link TableWrapData#grabVertical} field.
	 */
	@Test
	public void test_verticalGrab() throws Exception {
		parse(
				"<Shell>",
				"  <Shell.layout>",
				"    <f:TableWrapLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button'/>",
				"</Shell>");
		refresh();
		TableWrapLayoutInfo layout = getObjectByName("layout");
		ControlInfo button = getObjectByName("button");
		TableWrapDataInfo tableWrapData = layout.getTableWrapData(button);
		// initial state
		assertFalse(tableWrapData.getVerticalGrab());
		// grab := true
		tableWrapData.setVerticalGrab(true);
		assertTrue(tableWrapData.getVerticalGrab());
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <f:TableWrapLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button'>",
				"    <Button.layoutData>",
				"      <f:TableWrapData grabVertical='true'/>",
				"    </Button.layoutData>",
				"  </Button>",
				"</Shell>");
		// grab := false
		tableWrapData.setVerticalGrab(false);
		assertFalse(tableWrapData.getVerticalGrab());
		assertXML(
				"<Shell>",
				"  <Shell.layout>",
				"    <f:TableWrapLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button'/>",
				"</Shell>");
	}

	/**
	 * Sets vertical span in {@link TableWrapData} constructor.
	 */
	@Test
	public void test_verticalSpan() throws Exception {
		parse(
				"<Shell>",
				"  <Shell.layout>",
				"    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button'/>",
				"  <Button/>",
				"  <Button/>",
				"</Shell>");
		refresh();
		TableWrapLayoutInfo layout = getObjectByName("layout");
		ControlInfo button = getObjectByName("button");
		TableWrapDataInfo tableWrapData = layout.getTableWrapData(button);
		// initial state
		assertEquals(1, tableWrapData.getVerticalSpan());
		// verticalSpan := 2
		tableWrapData.setVerticalSpan(2);
		assertEquals(2, tableWrapData.getVerticalSpan());
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button'>",
				"    <Button.layoutData>",
				"      <f:TableWrapData rowspan='2'/>",
				"    </Button.layoutData>",
				"  </Button>",
				"  <Button/>",
				"  <Button/>",
				"</Shell>");
		// verticalSpan := default
		tableWrapData.getPropertyByTitle("rowspan").setValue(Property.UNKNOWN_VALUE);
		assertXML(
				"<Shell>",
				"  <Shell.layout>",
				"    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button'/>",
				"  <Button/>",
				"  <Button/>",
				"</Shell>");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Context menu
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_contextMenu_horizontal() throws Exception {
		parse(
				"<Shell>",
				"  <Shell.layout>",
				"    <f:TableWrapLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button'/>",
				"</Shell>");
		refresh();
		ControlInfo button = getObjectByName("button");
		// prepare context menu
		IMenuManager manager = getContextMenu(button);
		// check actions
		IMenuManager manager2 = findChildMenuManager(manager, "Horizontal alignment");
		assertNotNull(manager2);
		assertNotNull(findChildAction(manager2, "&Grab excess space"));
		assertNotNull(findChildAction(manager2, "&Left"));
		assertNotNull(findChildAction(manager2, "&Center"));
		assertNotNull(findChildAction(manager2, "&Right"));
		assertNotNull(findChildAction(manager2, "&Fill"));
		// check "check" state
		assertTrue(findChildAction(manager2, "&Left").isChecked());
		assertFalse(findChildAction(manager2, "&Right").isChecked());
		// use "Right" action
		{
			IAction action = findChildAction(manager2, "&Right");
			action.setChecked(true);
			action.run();
			assertXML(
					"// filler filler filler filler filler",
					"// filler filler filler filler filler",
					"<Shell>",
					"  <Shell.layout>",
					"    <f:TableWrapLayout wbp:name='layout'/>",
					"  </Shell.layout>",
					"  <Button wbp:name='button'>",
					"    <Button.layoutData>",
					"      <f:TableWrapData align='(org.eclipse.ui.forms.widgets.TableWrapData).RIGHT'/>",
					"    </Button.layoutData>",
					"  </Button>",
					"</Shell>");
		}
		// use "Grab action"
		{
			IAction action = findChildAction(manager2, "&Grab excess space");
			action.run();
			assertXML(
					"// filler filler filler filler filler",
					"// filler filler filler filler filler",
					"<Shell>",
					"  <Shell.layout>",
					"    <f:TableWrapLayout wbp:name='layout'/>",
					"  </Shell.layout>",
					"  <Button wbp:name='button'>",
					"    <Button.layoutData>",
					"      <f:TableWrapData align='(org.eclipse.ui.forms.widgets.TableWrapData).RIGHT'"
							+ " grabHorizontal='true'/>",
							"    </Button.layoutData>",
							"  </Button>",
					"</Shell>");
		}
	}

	@Test
	public void test_contextMenu_vertical() throws Exception {
		parse(
				"<Shell>",
				"  <Shell.layout>",
				"    <f:TableWrapLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button'/>",
				"</Shell>");
		refresh();
		ControlInfo button = getObjectByName("button");
		// prepare context menu
		IMenuManager manager = getContextMenu(button);
		// check actions
		IMenuManager manager2 = findChildMenuManager(manager, "Vertical alignment");
		assertNotNull(manager2);
		assertNotNull(findChildAction(manager2, "&Grab excess space"));
		assertNotNull(findChildAction(manager2, "&Top"));
		assertNotNull(findChildAction(manager2, "&Middle"));
		assertNotNull(findChildAction(manager2, "&Bottom"));
		assertNotNull(findChildAction(manager2, "&Fill"));
		// use "Bottom" action
		{
			IAction action = findChildAction(manager2, "&Bottom");
			action.setChecked(true);
			action.run();
			assertXML(
					"// filler filler filler filler filler",
					"// filler filler filler filler filler",
					"<Shell>",
					"  <Shell.layout>",
					"    <f:TableWrapLayout wbp:name='layout'/>",
					"  </Shell.layout>",
					"  <Button wbp:name='button'>",
					"    <Button.layoutData>",
					"      <f:TableWrapData valign='(org.eclipse.ui.forms.widgets.TableWrapData).BOTTOM'/>",
					"    </Button.layoutData>",
					"  </Button>",
					"</Shell>");
		}
		// use "Grab action"
		{
			IAction action = findChildAction(manager2, "&Grab excess space");
			action.run();
			assertXML(
					"// filler filler filler filler filler",
					"// filler filler filler filler filler",
					"<Shell>",
					"  <Shell.layout>",
					"    <f:TableWrapLayout wbp:name='layout'/>",
					"  </Shell.layout>",
					"  <Button wbp:name='button'>",
					"    <Button.layoutData>",
					"      <f:TableWrapData valign='(org.eclipse.ui.forms.widgets.TableWrapData).BOTTOM'"
							+ " grabVertical='true'/>",
							"    </Button.layoutData>",
							"  </Button>",
					"</Shell>");
		}
	}
}