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
package org.eclipse.wb.tests.designer.swing.model.layout.MigLayout;

import org.eclipse.wb.internal.swing.MigLayout.Activator;
import org.eclipse.wb.internal.swing.MigLayout.model.IPreferenceConstants;
import org.eclipse.wb.internal.swing.MigLayout.model.MigLayoutInfo;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.eclipse.jface.preference.IPreferenceStore;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * Tests for {@link MigLayoutInfo} and automatic alignment.
 *
 * @author scheglov_ke
 */
public class MigLayoutAutoAlignmentTest extends AbstractMigLayoutTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@AfterEach
	public void tearDown() throws Exception {
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setToDefault(IPreferenceConstants.P_ENABLE_GRAB);
		preferences.setToDefault(IPreferenceConstants.P_ENABLE_RIGHT_ALIGNMENT);
		super.tearDown();
	}

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
	 * {@link JTextField} marked as required horizontal grab/fill.
	 */
	@Test
	public void test_CREATE_Text() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new MigLayout());",
						"  }",
						"}");
		panel.refresh();
		MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
		// create
		{
			ComponentInfo newComponent = createComponent(JTextField.class);
			layout.command_CREATE(newComponent, 0, false, 0, false);
		}
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new MigLayout('', '[grow]', '[]'));",
				"    {",
				"      JTextField textField = new JTextField();",
				"      add(textField, 'cell 0 0,growx');",
				"      textField.setColumns(10);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test that horizontal grab/fill for {@link JTextField} can be disabled.
	 */
	@Test
	public void test_CREATE_Text_disabled() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new MigLayout());",
						"  }",
						"}");
		panel.refresh();
		MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
		// create
		Activator.getDefault().getPreferenceStore().setValue(IPreferenceConstants.P_ENABLE_GRAB, false);
		{
			ComponentInfo newComponent = createComponent(JTextField.class);
			layout.command_CREATE(newComponent, 0, false, 0, false);
		}
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new MigLayout());",
				"    {",
				"      JTextField textField = new JTextField();",
				"      add(textField, 'cell 0 0');",
				"      textField.setColumns(10);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * For {@link JTable} marked as required horizontal/vertical grab/fill.
	 */
	@Test
	public void test_CREATE_Table() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new MigLayout());",
						"  }",
						"}");
		panel.refresh();
		MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
		// create
		{
			ComponentInfo newComponent = createComponent(JTable.class);
			layout.command_CREATE(newComponent, 0, false, 0, false);
		}
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new MigLayout('', '[grow]', '[grow]'));",
				"    {",
				"      JTable table = new JTable();",
				"      add(table, 'cell 0 0,grow');",
				"    }",
				"  }",
				"}");
	}

	/**
	 * {@link JLabel} is marked as "right" aligned and next widget is {@link JTextField}, so when add
	 * {@link JLabel} before {@link JTextField}, use "right" alignment.
	 */
	@Test
	public void test_CREATE_LabelBeforeText() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new MigLayout('', '[][]', '[]'));",
						"    {",
						"      JTextField textField = new JTextField();",
						"      add(textField, 'cell 1 0, growx');",
						"    }",
						"  }",
						"}");
		panel.refresh();
		MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
		// create
		{
			ComponentInfo newComponent = createComponent(JLabel.class);
			layout.command_CREATE(newComponent, 0, false, 0, false);
		}
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new MigLayout('', '[][]', '[]'));",
				"    {",
				"      JLabel label = new JLabel('New label');",
				"      add(label, 'cell 0 0,alignx trailing');",
				"    }",
				"    {",
				"      JTextField textField = new JTextField();",
				"      add(textField, 'cell 1 0, growx');",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Check that automatic "right alignment" feature for {@link JLabel} can be disabled.
	 */
	@Test
	public void test_CREATE_LabelBeforeText_disabled() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new MigLayout('', '[][]', '[]'));",
						"    {",
						"      JTextField textField = new JTextField();",
						"      add(textField, 'cell 1 0, growx');",
						"    }",
						"  }",
						"}");
		panel.refresh();
		MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
		// create
		Activator.getDefault().getPreferenceStore().setValue(
				IPreferenceConstants.P_ENABLE_RIGHT_ALIGNMENT,
				false);
		{
			ComponentInfo newComponent = createComponent(JLabel.class);
			layout.command_CREATE(newComponent, 0, false, 0, false);
		}
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new MigLayout('', '[][]', '[]'));",
				"    {",
				"      JLabel label = new JLabel('New label');",
				"      add(label, 'cell 0 0');",
				"    }",
				"    {",
				"      JTextField textField = new JTextField();",
				"      add(textField, 'cell 1 0, growx');",
				"    }",
				"  }",
				"}");
	}

	/**
	 * {@link JLabel} is marked as "right" aligned and next widget is {@link JTextField}, so when add
	 * {@link JTextField} after {@link JLabel} , use "right" alignment for {@link JLabel}.
	 */
	@Test
	public void test_CREATE_TextAfterLabel() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new MigLayout('', '[][]', '[]'));",
						"    {",
						"      JLabel label = new JLabel('New label');",
						"      add(label, 'cell 0 0');",
						"    }",
						"  }",
						"}");
		panel.refresh();
		MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
		// create
		{
			ComponentInfo newComponent = createComponent(JTextField.class);
			layout.command_CREATE(newComponent, 1, false, 0, false);
		}
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new MigLayout('', '[][grow]', '[]'));",
				"    {",
				"      JLabel label = new JLabel('New label');",
				"      add(label, 'cell 0 0,alignx trailing');",
				"    }",
				"    {",
				"      JTextField textField = new JTextField();",
				"      add(textField, 'cell 1 0,growx');",
				"      textField.setColumns(10);",
				"    }",
				"  }",
				"}");
	}
}
