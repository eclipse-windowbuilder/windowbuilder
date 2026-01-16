/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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

import org.eclipse.wb.internal.swing.MigLayout.model.MigLayoutConverter;
import org.eclipse.wb.internal.swing.MigLayout.model.MigLayoutInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import net.miginfocom.swing.MigLayout;

import org.junit.jupiter.api.Test;

import javax.swing.JTable;

/**
 * Test for {@link MigLayoutConverter}.
 *
 * @author scheglov_ke
 */
public class MigLayoutConverterTest extends AbstractMigLayoutTest {
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
	@Test
	public void test_noComponents() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setSize(450, 300);",
						"    setLayout(null);",
						"  }",
						"}");
		panel.refresh();
		//
		setLayout(panel, MigLayout.class);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setSize(450, 300);",
				"    setLayout(new MigLayout('', '[]', '[]'));",
				"  }",
				"}");
	}

	/**
	 * {@link JTable} has zero preferred size, so when we convert it into {@link MigLayoutInfo}, it
	 * does not fit into any column/row.
	 */
	@Test
	public void test_zeroSizeComponent() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      JTable table = new JTable();",
						"      add(table);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		//
		setLayout(panel, MigLayout.class);
		assertEditor(
				"// filler filler filler filler filler",
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new MigLayout('', '[1px]', '[1px]'));",
				"    {",
				"      JTable table = new JTable();",
				"      add(table, 'cell 0 0,alignx left,aligny top');",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Horizontal alignment
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_oneColumn_LEFT() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setSize(450, 300);",
						"    setLayout(null);",
						"    {",
						"      JButton button = new JButton(C_1);",
						"      button.setBounds(10, 4, 50, 100);",
						"      add(button);",
						"    }",
						"    {",
						"      JButton button = new JButton(C_2);",
						"      button.setBounds(11, 120, 20, 80);",
						"      add(button);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		//
		setLayout(panel, MigLayout.class);
		assertEditor(
				"public class Test extends JPanel implements IConstants {",
				"  public Test() {",
				"    setSize(450, 300);",
				"    setLayout(new MigLayout('', '[50px]', '[100px][80px]'));",
				"    {",
				"      JButton button = new JButton(C_1);",
				"      add(button, 'cell 0 0,grow');",
				"    }",
				"    {",
				"      JButton button = new JButton(C_2);",
				"      add(button, 'cell 0 1,alignx left,growy');",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_oneColumn_CENTER() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setSize(450, 300);",
						"    setLayout(null);",
						"    {",
						"      JButton button = new JButton(C_1);",
						"      button.setBounds(10, 4, 80, 100);",
						"      add(button);",
						"    }",
						"    {",
						"      JButton button = new JButton(C_2);",
						"      button.setBounds(40, 120, 20, 80);",
						"      add(button);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		//
		setLayout(panel, MigLayout.class);
		assertEditor(
				"public class Test extends JPanel implements IConstants {",
				"  public Test() {",
				"    setSize(450, 300);",
				"    setLayout(new MigLayout('', '[80px]', '[100px][80px]'));",
				"    {",
				"      JButton button = new JButton(C_1);",
				"      add(button, 'cell 0 0,grow');",
				"    }",
				"    {",
				"      JButton button = new JButton(C_2);",
				"      add(button, 'cell 0 1,alignx center,growy');",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_oneColumn_RIGHT() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setLayout(null);",
						"    setSize(450, 300);",
						"    {",
						"      JButton button = new JButton(C_1);",
						"      button.setBounds(10, 4, 50, 100);",
						"      add(button);",
						"    }",
						"    {",
						"      JButton button = new JButton(C_2);",
						"      button.setBounds(39, 120, 20, 80);",
						"      add(button);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		//
		setLayout(panel, MigLayout.class);
		assertEditor(
				"public class Test extends JPanel implements IConstants {",
				"  public Test() {",
				"    setSize(450, 300);",
				"    setLayout(new MigLayout('', '[50px]', '[100px][80px]'));",
				"    {",
				"      JButton button = new JButton(C_1);",
				"      add(button, 'cell 0 0,grow');",
				"    }",
				"    {",
				"      JButton button = new JButton(C_2);",
				"      add(button, 'cell 0 1,alignx right,growy');",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_oneColumn_FILL() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setSize(450, 300);",
						"    setLayout(null);",
						"    {",
						"      JButton button = new JButton(C_1);",
						"      button.setBounds(10, 4, 50, 100);",
						"      add(button);",
						"    }",
						"    {",
						"      JButton button = new JButton(C_2);",
						"      button.setBounds(11, 120, 49, 80);",
						"      add(button);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		//
		setLayout(panel, MigLayout.class);
		assertEditor(
				"public class Test extends JPanel implements IConstants {",
				"  public Test() {",
				"    setSize(450, 300);",
				"    setLayout(new MigLayout('', '[50px]', '[100px][80px]'));",
				"    {",
				"      JButton button = new JButton(C_1);",
				"      add(button, 'cell 0 0,grow');",
				"    }",
				"    {",
				"      JButton button = new JButton(C_2);",
				"      add(button, 'cell 0 1,grow');",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Vertical alignment
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_oneRow_top() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setSize(450, 300);",
						"    setLayout(null);",
						"    {",
						"      JButton button = new JButton(C_1);",
						"      button.setBounds(4, 10, 100, 50);",
						"      add(button);",
						"    }",
						"    {",
						"      JButton button = new JButton(C_2);",
						"      button.setBounds(120, 11, 80, 20);",
						"      add(button);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		//
		setLayout(panel, MigLayout.class);
		assertEditor(
				"public class Test extends JPanel implements IConstants {",
				"  public Test() {",
				"    setSize(450, 300);",
				"    setLayout(new MigLayout('', '[100px][80px]', '[50px]'));",
				"    {",
				"      JButton button = new JButton(C_1);",
				"      add(button, 'cell 0 0,grow');",
				"    }",
				"    {",
				"      JButton button = new JButton(C_2);",
				"      add(button, 'cell 1 0,growx,aligny top');",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_oneRow_center() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setSize(450, 300);",
						"    setLayout(null);",
						"    {",
						"      JButton button = new JButton(C_1);",
						"      button.setBounds(4, 10, 100, 50);",
						"      add(button);",
						"    }",
						"    {",
						"      JButton button = new JButton(C_2);",
						"      button.setBounds(120, 25, 80, 20);",
						"      add(button);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		//
		setLayout(panel, MigLayout.class);
		assertEditor(
				"public class Test extends JPanel implements IConstants {",
				"  public Test() {",
				"    setSize(450, 300);",
				"    setLayout(new MigLayout('', '[100px][80px]', '[50px]'));",
				"    {",
				"      JButton button = new JButton(C_1);",
				"      add(button, 'cell 0 0,grow');",
				"    }",
				"    {",
				"      JButton button = new JButton(C_2);",
				"      add(button, 'cell 1 0,growx,aligny center');",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_oneRow_bottom() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setSize(450, 300);",
						"    setLayout(null);",
						"    {",
						"      JButton button = new JButton(C_1);",
						"      button.setBounds(4, 10, 100, 50);",
						"      add(button);",
						"    }",
						"    {",
						"      JButton button = new JButton(C_2);",
						"      button.setBounds(120, 39, 80, 20);",
						"      add(button);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		//
		setLayout(panel, MigLayout.class);
		assertEditor(
				"public class Test extends JPanel implements IConstants {",
				"  public Test() {",
				"    setSize(450, 300);",
				"    setLayout(new MigLayout('', '[100px][80px]', '[50px]'));",
				"    {",
				"      JButton button = new JButton(C_1);",
				"      add(button, 'cell 0 0,grow');",
				"    }",
				"    {",
				"      JButton button = new JButton(C_2);",
				"      add(button, 'cell 1 0,growx,aligny bottom');",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_oneRow_fill() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setSize(450, 300);",
						"    setLayout(null);",
						"    {",
						"      JButton button = new JButton(C_1);",
						"      button.setBounds(4, 10, 100, 50);",
						"      add(button);",
						"    }",
						"    {",
						"      JButton button = new JButton(C_2);",
						"      button.setBounds(120, 11, 80, 39);",
						"      add(button);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		//
		setLayout(panel, MigLayout.class);
		assertEditor(
				"public class Test extends JPanel implements IConstants {",
				"  public Test() {",
				"    setSize(450, 300);",
				"    setLayout(new MigLayout('', '[100px][80px]', '[50px]'));",
				"    {",
				"      JButton button = new JButton(C_1);",
				"      add(button, 'cell 0 0,grow');",
				"    }",
				"    {",
				"      JButton button = new JButton(C_2);",
				"      add(button, 'cell 1 0,grow');",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Spanning
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_twoRows_spanColumns() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setSize(450, 300);",
						"    setLayout(null);",
						"    {",
						"      JButton button = new JButton(C_1);",
						"      button.setBounds(0, 10, 100, 40);",
						"      add(button);",
						"    }",
						"    {",
						"      JButton button = new JButton(C_2);",
						"      button.setBounds(110, 10, 80, 20);",
						"      add(button);",
						"    }",
						"    {",
						"      JButton button = new JButton(C_3);",
						"      button.setBounds(45, 60, 90, 40);",
						"      add(button);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		//
		setLayout(panel, MigLayout.class);
		assertEditor(
				"public class Test extends JPanel implements IConstants {",
				"  public Test() {",
				"    setSize(450, 300);",
				"    setLayout(new MigLayout('', '[100px][10px][80px]', '[40px][40px]'));",
				"    {",
				"      JButton button = new JButton(C_1);",
				"      add(button, 'cell 0 0,grow');",
				"    }",
				"    {",
				"      JButton button = new JButton(C_2);",
				"      add(button, 'cell 2 0,growx,aligny top');",
				"    }",
				"    {",
				"      JButton button = new JButton(C_3);",
				"      add(button, 'cell 0 1 3 1,alignx center,growy');",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_Switching_fromGridBagLayout() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    GridBagLayout gridBagLayout = new GridBagLayout();",
						"    gridBagLayout.columnWidths = new int[] { 0, 0, 0, 0 };",
						"    gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0 };",
						"    gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };",
						"    gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };",
						"    setLayout(gridBagLayout);",
						"    {",
						"      JComboBox comboBox = new JComboBox();",
						"      GridBagConstraints gbc = new GridBagConstraints();",
						"      gbc.insets = new Insets(0, 0, 5, 5);",
						"      gbc.fill = GridBagConstraints.HORIZONTAL;",
						"      gbc.gridx = 1;",
						"      gbc.gridy = 0;",
						"      add(comboBox, gbc);",
						"    }",
						"    {",
						"      JLabel label = new JLabel('New label');",
						"      GridBagConstraints gbc = new GridBagConstraints();",
						"      gbc.insets = new Insets(0, 0, 5, 5);",
						"      gbc.anchor = GridBagConstraints.EAST;",
						"      gbc.gridx = 0;",
						"      gbc.gridy = 1;",
						"      add(label, gbc);",
						"    }",
						"    {",
						"      JTextField textField = new JTextField();",
						"      GridBagConstraints gbc = new GridBagConstraints();",
						"      gbc.gridwidth = 2;",
						"      gbc.insets = new Insets(0, 0, 5, 5);",
						"      gbc.fill = GridBagConstraints.HORIZONTAL;",
						"      gbc.gridx = 1;",
						"      gbc.gridy = 1;",
						"      add(textField, gbc);",
						"      textField.setColumns(10);",
						"    }",
						"    {",
						"      JButton button = new JButton('New button');",
						"      GridBagConstraints gbc = new GridBagConstraints();",
						"      gbc.gridx = 2;",
						"      gbc.gridy = 2;",
						"      add(button, gbc);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		// set FormLayout
		try {
			setLayout(panel, MigLayout.class);
		} finally {
			panel.refresh_dispose();
		}
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new MigLayout('', '[60px][275px][5px][105px]', '[26px][21px][27px]'));",
				"    {",
				"      JComboBox comboBox = new JComboBox();",
				"      add(comboBox, 'cell 1 0,growx,aligny center');",
				"    }",
				"    {",
				"      JLabel label = new JLabel('New label');",
				"      add(label, 'cell 0 1,alignx right,aligny center');",
				"    }",
				"    {",
				"      JTextField textField = new JTextField();",
				"      add(textField, 'cell 1 1 3 1,growx,aligny center');",
				"      textField.setColumns(10);",
				"    }",
				"    {",
				"      JButton button = new JButton('New button');",
				"      add(button, 'cell 3 2,alignx center,aligny center');",
				"    }",
				"  }",
				"}");
	}
}
