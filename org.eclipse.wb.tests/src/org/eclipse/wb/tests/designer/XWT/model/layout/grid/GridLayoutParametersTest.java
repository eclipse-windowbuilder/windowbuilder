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

import org.eclipse.wb.internal.rcp.ToolkitProvider;
import org.eclipse.wb.internal.swt.model.layout.grid.IPreferenceConstants;
import org.eclipse.wb.internal.xwt.model.layout.grid.GridLayoutInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;
import org.eclipse.wb.tests.designer.core.PreferencesRepairer;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import org.junit.Test;

/**
 * Test for {@link GridLayoutInfo} and special parameters for grab/alignment.
 *
 * @author scheglov_ke
 */
public class GridLayoutParametersTest extends XwtModelTest {
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
	 * {@link Text} widget is marked as required horizontal grab/fill.
	 */
	@Test
	public void test_CREATE_Text() throws Exception {
		parse(
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <GridLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"</Shell>");
		refresh();
		GridLayoutInfo layout = getObjectByName("layout");
		//
		ControlInfo newText = createObject("org.eclipse.swt.widgets.Text");
		layout.command_CREATE(newText, 0, false, 0, false);
		assertXML(
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <GridLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"  <Text x:Style='BORDER'>",
				"    <Text.layoutData>",
				"      <GridData grabExcessHorizontalSpace='true' horizontalAlignment='FILL'/>",
				"    </Text.layoutData>",
				"  </Text>",
				"</Shell>");
	}

	/**
	 * Test that horizontal grab/fill {@link Text} can be disabled.
	 */
	@Test
	public void test_CREATE_Text_disabled() throws Exception {
		parse(
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <GridLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"</Shell>");
		refresh();
		GridLayoutInfo layout = getObjectByName("layout");
		//
		PreferencesRepairer preferences =
				new PreferencesRepairer(ToolkitProvider.DESCRIPTION.getPreferences());
		try {
			preferences.setValue(IPreferenceConstants.P_ENABLE_GRAB, false);
			ControlInfo newText = createObject("org.eclipse.swt.widgets.Text");
			layout.command_CREATE(newText, 0, false, 0, false);
			assertXML(
					"// filler filler filler filler filler",
					"<Shell>",
					"  <Shell.layout>",
					"    <GridLayout wbp:name='layout'/>",
					"  </Shell.layout>",
					"  <Text x:Style='BORDER'/>",
					"</Shell>");
		} finally {
			preferences.restore();
		}
	}

	/**
	 * {@link Table} widget is marked as required horizontal/vertical grab/fill.
	 */
	@Test
	public void test_CREATE_Table() throws Exception {
		parse(
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <GridLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"</Shell>");
		refresh();
		GridLayoutInfo layout = getObjectByName("layout");
		//
		ControlInfo newTable = createObject("org.eclipse.swt.widgets.Table");
		layout.command_CREATE(newTable, 0, false, 0, false);
		assertXML(
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <GridLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"  <Table x:Style='BORDER | FULL_SELECTION' linesVisible='true' headerVisible='true'>",
				"    <Table.layoutData>",
				"      <GridData grabExcessHorizontalSpace='true' horizontalAlignment='FILL'"
						+ " grabExcessVerticalSpace='true' verticalAlignment='FILL'/>",
						"    </Table.layoutData>",
						"  </Table>",
				"</Shell>");
	}

	/**
	 * {@link Label} widget is marked as "right" aligned and next widget is {@link Text}, so when add
	 * {@link Label} before {@link Text}, use {@link GridData#END} alignment.
	 */
	@Test
	public void test_CREATE_LabelBeforeText() throws Exception {
		parse(
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <GridLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"  <Text/>",
				"</Shell>");
		refresh();
		GridLayoutInfo layout = getObjectByName("layout");
		//
		ControlInfo newLabel = createObject("org.eclipse.swt.widgets.Label");
		layout.command_CREATE(newLabel, 0, true, 0, false);
		assertXML(
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <GridLayout wbp:name='layout' numColumns='2'/>",
				"  </Shell.layout>",
				"  <Label text='New Label'>",
				"    <Label.layoutData>",
				"      <GridData horizontalAlignment='RIGHT'/>",
				"    </Label.layoutData>",
				"  </Label>",
				"  <Text/>",
				"</Shell>");
	}

	/**
	 * Check that automatic "right alignment" feature for {@link Label} can be disabled.
	 */
	@Test
	public void test_CREATE_LabelBeforeText_disabled() throws Exception {
		parse(
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <GridLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"  <Text/>",
				"</Shell>");
		refresh();
		GridLayoutInfo layout = getObjectByName("layout");
		//
		PreferencesRepairer preferences =
				new PreferencesRepairer(ToolkitProvider.DESCRIPTION.getPreferences());
		try {
			preferences.setValue(IPreferenceConstants.P_ENABLE_RIGHT_ALIGNMENT, false);
			ControlInfo newLabel = createObject("org.eclipse.swt.widgets.Label");
			layout.command_CREATE(newLabel, 0, true, 0, false);
			assertXML(
					"// filler filler filler filler filler",
					"<Shell>",
					"  <Shell.layout>",
					"    <GridLayout wbp:name='layout' numColumns='2'/>",
					"  </Shell.layout>",
					"  <Label text='New Label'/>",
					"  <Text/>",
					"</Shell>");
		} finally {
			preferences.restore();
		}
	}

	/**
	 * {@link Label} widget is marked as "right" aligned and next widget is {@link Text}, so when add
	 * {@link Text} after {@link Label}, use {@link GridData#END} alignment for {@link Label}.
	 */
	@Test
	public void test_CREATE_Text_afterLabel() throws Exception {
		parse(
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <GridLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"  <Label text='New Label'/>",
				"</Shell>");
		refresh();
		GridLayoutInfo layout = getObjectByName("layout");
		//
		ControlInfo newText = createObject("org.eclipse.swt.widgets.Text");
		layout.command_CREATE(newText, 1, false, 0, false);
		assertXML(
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <GridLayout wbp:name='layout' numColumns='2'/>",
				"  </Shell.layout>",
				"  <Label text='New Label'>",
				"    <Label.layoutData>",
				"      <GridData horizontalAlignment='RIGHT'/>",
				"    </Label.layoutData>",
				"  </Label>",
				"  <Text x:Style='BORDER'>",
				"    <Text.layoutData>",
				"      <GridData grabExcessHorizontalSpace='true' horizontalAlignment='FILL'/>",
				"    </Text.layoutData>",
				"  </Text>",
				"</Shell>");
	}

	/**
	 * When we add {@link Text} after "filler" {@link Label}, we should not change its alignment.
	 */
	@Test
	public void test_CREATE_Text_afterFiller() throws Exception {
		parse(
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <GridLayout wbp:name='layout' numColumns='2'/>",
				"  </Shell.layout>",
				"  <Label/>",
				"  <Label/>",
				"</Shell>");
		refresh();
		GridLayoutInfo layout = getObjectByName("layout");
		//
		ControlInfo newText = createObject("org.eclipse.swt.widgets.Text");
		layout.command_CREATE(newText, 1, false, 0, false);
		assertXML(
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <GridLayout wbp:name='layout' numColumns='2'/>",
				"  </Shell.layout>",
				"  <Label/>",
				"  <Text x:Style='BORDER'>",
				"    <Text.layoutData>",
				"      <GridData grabExcessHorizontalSpace='true' horizontalAlignment='FILL'/>",
				"    </Text.layoutData>",
				"  </Text>",
				"</Shell>");
	}
}