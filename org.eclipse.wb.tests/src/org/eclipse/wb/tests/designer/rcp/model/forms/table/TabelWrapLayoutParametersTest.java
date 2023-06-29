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
package org.eclipse.wb.tests.designer.rcp.model.forms.table;

import org.eclipse.wb.internal.rcp.ToolkitProvider;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.IPreferenceConstants;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.TableWrapLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.core.PreferencesRepairer;
import org.eclipse.wb.tests.designer.rcp.BTestUtils;
import org.eclipse.wb.tests.designer.rcp.model.forms.AbstractFormsTest;

import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/**
 * Test for {@link TableWrapLayout} and special parameters for grab/alignment.
 *
 * @author scheglov_ke
 */
public class TabelWrapLayoutParametersTest extends AbstractFormsTest {
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
	public void test_CREATE_Text() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      setLayout(layout);",
						"    }",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		//
		shell.refresh();
		ControlInfo newText = BTestUtils.createControl("org.eclipse.swt.widgets.Text");
		layout.command_CREATE(newText, 0, false, 0, false);
		assertEditor(
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      TableWrapLayout layout = new TableWrapLayout();",
				"      setLayout(layout);",
				"    }",
				"    {",
				"      Text text = new Text(this, SWT.BORDER);",
				"      text.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP, 1, 1));",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test that horizontal grab/fill {@link Text} can be disabled.
	 */
	public void test_CREATE_Text_disabled() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      setLayout(layout);",
						"    }",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		shell.refresh();
		//
		PreferencesRepairer preferences =
				new PreferencesRepairer(ToolkitProvider.DESCRIPTION.getPreferences());
		try {
			preferences.setValue(IPreferenceConstants.P_ENABLE_GRAB, false);
			ControlInfo newText = BTestUtils.createControl("org.eclipse.swt.widgets.Text");
			layout.command_CREATE(newText, 0, false, 0, false);
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    {",
					"      TableWrapLayout layout = new TableWrapLayout();",
					"      setLayout(layout);",
					"    }",
					"    {",
					"      Text text = new Text(this, SWT.BORDER);",
					"    }",
					"  }",
					"}");
		} finally {
			preferences.restore();
		}
	}

	/**
	 * {@link Table} widget is marked as required horizontal/vertical grab/fill.
	 */
	public void test_CREATE_Table() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      setLayout(layout);",
						"    }",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		//
		shell.refresh();
		ControlInfo newTable = BTestUtils.createControl("org.eclipse.swt.widgets.Table");
		layout.command_CREATE(newTable, 0, false, 0, false);
		assertEditor(
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      TableWrapLayout layout = new TableWrapLayout();",
				"      setLayout(layout);",
				"    }",
				"    {",
				"      Table table = new Table(this, SWT.BORDER | SWT.FULL_SELECTION);",
				"      table.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB, 1, 1));",
				"      table.setHeaderVisible(true);",
				"      table.setLinesVisible(true);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * {@link Label} widget is marked as "right" aligned and next widget is {@link Text}, so when add
	 * {@link Label} before {@link Text}, use {@link TableWrapData#RIGHT} alignment.
	 */
	public void test_CREATE_LabelBeforeText() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      layout.numColumns = 2;",
						"      setLayout(layout);",
						"    }",
						"    new Label(this, SWT.NONE);",
						"    {",
						"      Text text = new Text(this, SWT.BORDER);",
						"    }",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		//
		shell.refresh();
		ControlInfo newLabel = BTestUtils.createControl("org.eclipse.swt.widgets.Label");
		layout.command_CREATE(newLabel, 0, false, 0, false);
		assertEditor(
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      TableWrapLayout layout = new TableWrapLayout();",
				"      layout.numColumns = 2;",
				"      setLayout(layout);",
				"    }",
				"    {",
				"      Label label = new Label(this, SWT.NONE);",
				"      label.setLayoutData(new TableWrapData(TableWrapData.RIGHT, TableWrapData.TOP, 1, 1));",
				"      label.setText('New Label');",
				"    }",
				"    {",
				"      Text text = new Text(this, SWT.BORDER);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Check that automatic "right alignment" feature for {@link Label} can be disabled.
	 */
	public void test_CREATE_LabelBeforeText_disabled() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      layout.numColumns = 2;",
						"      setLayout(layout);",
						"    }",
						"    new Label(this, SWT.NONE);",
						"    {",
						"      Text text = new Text(this, SWT.BORDER);",
						"    }",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		//
		shell.refresh();
		PreferencesRepairer preferences =
				new PreferencesRepairer(ToolkitProvider.DESCRIPTION.getPreferences());
		try {
			preferences.setValue(IPreferenceConstants.P_ENABLE_RIGHT_ALIGNMENT, false);
			ControlInfo newLabel = BTestUtils.createControl("org.eclipse.swt.widgets.Label");
			layout.command_CREATE(newLabel, 0, false, 0, false);
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    {",
					"      TableWrapLayout layout = new TableWrapLayout();",
					"      layout.numColumns = 2;",
					"      setLayout(layout);",
					"    }",
					"    {",
					"      Label label = new Label(this, SWT.NONE);",
					"      label.setText('New Label');",
					"    }",
					"    {",
					"      Text text = new Text(this, SWT.BORDER);",
					"    }",
					"  }",
					"}");
		} finally {
			preferences.restore();
		}
	}

	/**
	 * {@link Label} widget is marked as "right" aligned and next widget is {@link Text}, so when add
	 * {@link Text} after {@link Label}, use {@link TableWrapData#RIGHT} alignment for {@link Label}.
	 */
	public void test_CREATE_TextAfterLabel() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    {",
						"      TableWrapLayout layout = new TableWrapLayout();",
						"      layout.numColumns = 2;",
						"      setLayout(layout);",
						"    }",
						"    {",
						"      Label label = new Label(this, SWT.NONE);",
						"    }",
						"    new Label(this, SWT.NONE);",
						"  }",
						"}");
		TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
		//
		shell.refresh();
		ControlInfo newText = BTestUtils.createControl("org.eclipse.swt.widgets.Text");
		layout.command_CREATE(newText, 1, false, 0, false);
		assertEditor(
				"class Test extends Shell {",
				"  Test() {",
				"    {",
				"      TableWrapLayout layout = new TableWrapLayout();",
				"      layout.numColumns = 2;",
				"      setLayout(layout);",
				"    }",
				"    {",
				"      Label label = new Label(this, SWT.NONE);",
				"      label.setLayoutData(new TableWrapData(TableWrapData.RIGHT, TableWrapData.TOP, 1, 1));",
				"    }",
				"    {",
				"      Text text = new Text(this, SWT.BORDER);",
				"      text.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP, 1, 1));",
				"    }",
				"  }",
				"}");
	}
}