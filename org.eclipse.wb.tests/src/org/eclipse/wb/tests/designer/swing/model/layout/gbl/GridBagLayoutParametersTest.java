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
package org.eclipse.wb.tests.designer.swing.model.layout.gbl;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swing.Activator;
import org.eclipse.wb.internal.swing.ToolkitProvider;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.GridBagLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.IPreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;

import org.junit.After;
import org.junit.Test;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * Tests for {@link GridBagLayoutInfo} and automatic alignment.
 *
 * @author scheglov_ke
 */
public class GridBagLayoutParametersTest extends AbstractGridBagLayoutTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@After
	public void tearDown() throws Exception {
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setToDefault(IPreferenceConstants.P_ENABLE_GRAB);
		preferences.setToDefault(IPreferenceConstants.P_ENABLE_RIGHT_ALIGNMENT);
		super.tearDown();
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
		final ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  public Test() {",
						"    GridBagLayout layout = new GridBagLayout();",
						"    layout.columnWidths = new int[] {0, 0};",
						"    layout.rowHeights = new int[] {0, 0};",
						"    layout.columnWeights = new double[] {0.0, Double.MIN_VALUE};",
						"    layout.rowWeights = new double[] {0.0, Double.MIN_VALUE};",
						"    setLayout(layout);",
						"  }",
						"}");
		panel.refresh();
		// add new component
		ExecutionUtils.run(panel, new RunnableEx() {
			@Override
			public void run() throws Exception {
				GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
				ComponentInfo newComponent = createComponent(JTextField.class);
				layout.command_CREATE(newComponent, 0, false, 0, false);
			}
		});
		// check result
		assertEditor(
				"class Test extends JPanel {",
				"  public Test() {",
				"    GridBagLayout layout = new GridBagLayout();",
				"    layout.columnWidths = new int[] {0, 0};",
				"    layout.rowHeights = new int[] {0, 0};",
				"    layout.columnWeights = new double[] {1.0, Double.MIN_VALUE};",
				"    layout.rowWeights = new double[] {0.0, Double.MIN_VALUE};",
				"    setLayout(layout);",
				"    {",
				"      JTextField textField = new JTextField();",
				"      GridBagConstraints gbc = new GridBagConstraints();",
				"      gbc.fill = GridBagConstraints.HORIZONTAL;",
				"      gbc.gridx = 0;",
				"      gbc.gridy = 0;",
				"      add(textField, gbc);",
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
		final ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  public Test() {",
						"    GridBagLayout layout = new GridBagLayout();",
						"    layout.columnWidths = new int[] {0, 0};",
						"    layout.rowHeights = new int[] {0, 0};",
						"    layout.columnWeights = new double[] {0.0, Double.MIN_VALUE};",
						"    layout.rowWeights = new double[] {0.0, Double.MIN_VALUE};",
						"    setLayout(layout);",
						"  }",
						"}");
		panel.refresh();
		// add new component
		ToolkitProvider.DESCRIPTION.getPreferences().setValue(IPreferenceConstants.P_ENABLE_GRAB, false);
		ExecutionUtils.run(panel, new RunnableEx() {
			@Override
			public void run() throws Exception {
				GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
				ComponentInfo newComponent = createComponent(JTextField.class);
				layout.command_CREATE(newComponent, 0, false, 0, false);
			}
		}); // check result
		assertEditor(
				"class Test extends JPanel {",
				"  public Test() {",
				"    GridBagLayout layout = new GridBagLayout();",
				"    layout.columnWidths = new int[] {0, 0};",
				"    layout.rowHeights = new int[] {0, 0};",
				"    layout.columnWeights = new double[] {0.0, Double.MIN_VALUE};",
				"    layout.rowWeights = new double[] {0.0, Double.MIN_VALUE};",
				"    setLayout(layout);",
				"    {",
				"      JTextField textField = new JTextField();",
				"      GridBagConstraints gbc = new GridBagConstraints();",
				"      gbc.gridx = 0;",
				"      gbc.gridy = 0;",
				"      add(textField, gbc);",
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
		final ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  public Test() {",
						"    GridBagLayout layout = new GridBagLayout();",
						"    layout.columnWidths = new int[] {0, 0};",
						"    layout.rowHeights = new int[] {0, 0};",
						"    layout.columnWeights = new double[] {0.0, Double.MIN_VALUE};",
						"    layout.rowWeights = new double[] {0.0, Double.MIN_VALUE};",
						"    setLayout(layout);",
						"  }",
						"}");
		panel.refresh();
		// add new component
		ExecutionUtils.run(panel, new RunnableEx() {
			@Override
			public void run() throws Exception {
				GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
				ComponentInfo newComponent = createComponent(JTable.class);
				layout.command_CREATE(newComponent, 0, false, 0, false);
			}
		});
		// check result
		assertEditor(
				"class Test extends JPanel {",
				"  public Test() {",
				"    GridBagLayout layout = new GridBagLayout();",
				"    layout.columnWidths = new int[] {0, 0};",
				"    layout.rowHeights = new int[] {0, 0};",
				"    layout.columnWeights = new double[] {1.0, Double.MIN_VALUE};",
				"    layout.rowWeights = new double[] {1.0, Double.MIN_VALUE};",
				"    setLayout(layout);",
				"    {",
				"      JTable table = new JTable();",
				"      GridBagConstraints gbc = new GridBagConstraints();",
				"      gbc.fill = GridBagConstraints.BOTH;",
				"      gbc.gridx = 0;",
				"      gbc.gridy = 0;",
				"      add(table, gbc);",
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
		final ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  public Test() {",
						"    GridBagLayout layout = new GridBagLayout();",
						"    layout.columnWidths = new int[] {0, 0, 0};",
						"    layout.rowHeights = new int[] {0, 0};",
						"    layout.columnWeights = new double[] {0.0, 1.0, Double.MIN_VALUE};",
						"    layout.rowWeights = new double[] {0.0, Double.MIN_VALUE};",
						"    setLayout(layout);",
						"    {",
						"      JTextField textField = new JTextField();",
						"      GridBagConstraints gbc = new GridBagConstraints();",
						"      gbc.fill = GridBagConstraints.HORIZONTAL;",
						"      gbc.gridx = 1;",
						"      gbc.gridy = 0;",
						"      add(textField, gbc);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		// add new component
		ExecutionUtils.run(panel, new RunnableEx() {
			@Override
			public void run() throws Exception {
				GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
				ComponentInfo newComponent = createComponent(JLabel.class);
				layout.command_CREATE(newComponent, 0, false, 0, false);
			}
		});
		// check result
		assertEditor(
				"class Test extends JPanel {",
				"  public Test() {",
				"    GridBagLayout layout = new GridBagLayout();",
				"    layout.columnWidths = new int[] {0, 0, 0};",
				"    layout.rowHeights = new int[] {0, 0};",
				"    layout.columnWeights = new double[] {0.0, 1.0, Double.MIN_VALUE};",
				"    layout.rowWeights = new double[] {0.0, Double.MIN_VALUE};",
				"    setLayout(layout);",
				"    {",
				"      JLabel label = new JLabel('New label');",
				"      GridBagConstraints gbc = new GridBagConstraints();",
				"      gbc.insets = new Insets(0, 0, 0, 5);",
				"      gbc.anchor = GridBagConstraints.EAST;",
				"      gbc.gridx = 0;",
				"      gbc.gridy = 0;",
				"      add(label, gbc);",
				"    }",
				"    {",
				"      JTextField textField = new JTextField();",
				"      GridBagConstraints gbc = new GridBagConstraints();",
				"      gbc.fill = GridBagConstraints.HORIZONTAL;",
				"      gbc.gridx = 1;",
				"      gbc.gridy = 0;",
				"      add(textField, gbc);",
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
		final ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  public Test() {",
						"    GridBagLayout layout = new GridBagLayout();",
						"    layout.columnWidths = new int[] {0, 0, 0};",
						"    layout.rowHeights = new int[] {0, 0};",
						"    layout.columnWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};",
						"    layout.rowWeights = new double[] {0.0, Double.MIN_VALUE};",
						"    setLayout(layout);",
						"    {",
						"      JLabel label = new JLabel('New label');",
						"      GridBagConstraints gbc = new GridBagConstraints();",
						"      gbc.insets = new Insets(0, 0, 0, 5);",
						"      gbc.gridx = 0;",
						"      gbc.gridy = 0;",
						"      add(label, gbc);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		// add new component
		ExecutionUtils.run(panel, new RunnableEx() {
			@Override
			public void run() throws Exception {
				GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
				ComponentInfo newComponent = createComponent(JTextField.class);
				layout.command_CREATE(newComponent, 1, false, 0, false);
			}
		});
		// check result
		assertEditor(
				"class Test extends JPanel {",
				"  public Test() {",
				"    GridBagLayout layout = new GridBagLayout();",
				"    layout.columnWidths = new int[] {0, 0, 0};",
				"    layout.rowHeights = new int[] {0, 0};",
				"    layout.columnWeights = new double[] {0.0, 1.0, Double.MIN_VALUE};",
				"    layout.rowWeights = new double[] {0.0, Double.MIN_VALUE};",
				"    setLayout(layout);",
				"    {",
				"      JLabel label = new JLabel('New label');",
				"      GridBagConstraints gbc = new GridBagConstraints();",
				"      gbc.anchor = GridBagConstraints.EAST;",
				"      gbc.insets = new Insets(0, 0, 0, 5);",
				"      gbc.gridx = 0;",
				"      gbc.gridy = 0;",
				"      add(label, gbc);",
				"    }",
				"    {",
				"      JTextField textField = new JTextField();",
				"      GridBagConstraints gbc = new GridBagConstraints();",
				"      gbc.fill = GridBagConstraints.HORIZONTAL;",
				"      gbc.gridx = 1;",
				"      gbc.gridy = 0;",
				"      add(textField, gbc);",
				"      textField.setColumns(10);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Check that automatic "right alignment" feature for {@link JLabel} can be disabled.
	 */
	@Test
	public void test_CREATE_LabelBeforeText_disabled() throws Exception {
		final ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  public Test() {",
						"    GridBagLayout layout = new GridBagLayout();",
						"    layout.columnWidths = new int[] {0, 0, 0};",
						"    layout.rowHeights = new int[] {0, 0};",
						"    layout.columnWeights = new double[] {0.0, 1.0, Double.MIN_VALUE};",
						"    layout.rowWeights = new double[] {0.0, Double.MIN_VALUE};",
						"    setLayout(layout);",
						"    {",
						"      JTextField textField = new JTextField();",
						"      GridBagConstraints gbc = new GridBagConstraints();",
						"      gbc.fill = GridBagConstraints.HORIZONTAL;",
						"      gbc.gridx = 1;",
						"      gbc.gridy = 0;",
						"      add(textField, gbc);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		// add new component
		ToolkitProvider.DESCRIPTION.getPreferences().setValue(
				IPreferenceConstants.P_ENABLE_RIGHT_ALIGNMENT,
				false);
		ExecutionUtils.run(panel, new RunnableEx() {
			@Override
			public void run() throws Exception {
				GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
				ComponentInfo newComponent = createComponent(JLabel.class);
				layout.command_CREATE(newComponent, 0, false, 0, false);
			}
		});
		// check result
		assertEditor(
				"class Test extends JPanel {",
				"  public Test() {",
				"    GridBagLayout layout = new GridBagLayout();",
				"    layout.columnWidths = new int[] {0, 0, 0};",
				"    layout.rowHeights = new int[] {0, 0};",
				"    layout.columnWeights = new double[] {0.0, 1.0, Double.MIN_VALUE};",
				"    layout.rowWeights = new double[] {0.0, Double.MIN_VALUE};",
				"    setLayout(layout);",
				"    {",
				"      JLabel label = new JLabel('New label');",
				"      GridBagConstraints gbc = new GridBagConstraints();",
				"      gbc.insets = new Insets(0, 0, 0, 5);",
				"      gbc.gridx = 0;",
				"      gbc.gridy = 0;",
				"      add(label, gbc);",
				"    }",
				"    {",
				"      JTextField textField = new JTextField();",
				"      GridBagConstraints gbc = new GridBagConstraints();",
				"      gbc.fill = GridBagConstraints.HORIZONTAL;",
				"      gbc.gridx = 1;",
				"      gbc.gridy = 0;",
				"      add(textField, gbc);",
				"    }",
				"  }",
				"}");
	}
}
