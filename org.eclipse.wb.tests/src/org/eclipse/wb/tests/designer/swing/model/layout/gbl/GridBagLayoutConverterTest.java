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
package org.eclipse.wb.tests.designer.swing.model.layout.gbl;

import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.GridBagLayoutConverter;
import org.eclipse.wb.internal.swing.model.layout.gbl.GridBagLayoutInfo;

import org.junit.jupiter.api.Test;

import java.awt.GridBagLayout;

/**
 * Tests for {@link GridBagLayoutConverter}.
 *
 * @author scheglov_ke
 */
public class GridBagLayoutConverterTest extends AbstractGridBagLayoutTest {
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
	public void test_empty() throws Exception {
		ContainerInfo panel = parseContainer("""
				public class Test extends JPanel {
					public Test() {
						setSize(450, 300);
						setLayout(null);
					}
				}""");
		panel.refresh();
		// set GridBagLayout
		setLayout(panel, GridBagLayout.class);
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setSize(450, 300);
						GridBagLayout gridBagLayout = new GridBagLayout();
						gridBagLayout.columnWidths = new int[]{0};
						gridBagLayout.rowHeights = new int[]{0};
						gridBagLayout.columnWeights = new double[]{Double.MIN_VALUE};
						gridBagLayout.rowWeights = new double[]{Double.MIN_VALUE};
						setLayout(gridBagLayout);
					}
				}""");
	}

	@Test
	public void test_oneColumn() throws Exception {
		ContainerInfo panel = parseContainer("""
				public class Test extends JPanel {
					public Test() {
						setSize(450, 300);
						setLayout(null);
						{
							JButton button_0 = new JButton();
							button_0.setBounds(4, 10, 100, 30);
							add(button_0);
						}
						{
							JButton button_1 = new JButton();
							button_1.setBounds(10, 50, 80, 20);
							add(button_1);
						}
					}
				}""");
		panel.refresh();
		// set GridBagLayout
		setLayout(panel, GridBagLayout.class);
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setSize(450, 300);
						GridBagLayout gridBagLayout = new GridBagLayout();
						gridBagLayout.columnWidths = new int[]{100, 0};
						gridBagLayout.rowHeights = new int[]{30, 20, 0};
						gridBagLayout.columnWeights = new double[]{0.0, Double.MIN_VALUE};
						gridBagLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
						setLayout(gridBagLayout);
						{
							JButton button_0 = new JButton();
							GridBagConstraints gbc = new GridBagConstraints();
							gbc.fill = GridBagConstraints.BOTH;
							gbc.insets = new Insets(0, 0, 5, 0);
							gbc.gridx = 0;
							gbc.gridy = 0;
							add(button_0, gbc);
						}
						{
							JButton button_1 = new JButton();
							GridBagConstraints gbc = new GridBagConstraints();
							gbc.fill = GridBagConstraints.BOTH;
							gbc.gridx = 0;
							gbc.gridy = 1;
							add(button_1, gbc);
						}
					}
				}""");
		{
			GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
			assertEquals(1, layout.getColumns().size());
			assertEquals(2, layout.getRows().size());
		}
	}

	@Test
	public void test_oneColumn_left() throws Exception {
		ContainerInfo panel = parseContainer("""
				public class Test extends JPanel {
					public Test() {
						setSize(450, 300);
						setLayout(null);
						{
							JButton button_0 = new JButton();
							button_0.setBounds(4, 10, 100, 30);
							add(button_0);
						}
						{
							JButton button_1 = new JButton();
							button_1.setBounds(5, 50, 60, 20);
							add(button_1);
						}
					}
				}""");
		panel.refresh();
		// set GridBagLayout
		setLayout(panel, GridBagLayout.class);
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setSize(450, 300);
						GridBagLayout gridBagLayout = new GridBagLayout();
						gridBagLayout.columnWidths = new int[]{100, 0};
						gridBagLayout.rowHeights = new int[]{30, 20, 0};
						gridBagLayout.columnWeights = new double[]{0.0, Double.MIN_VALUE};
						gridBagLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
						setLayout(gridBagLayout);
						{
							JButton button_0 = new JButton();
							GridBagConstraints gbc = new GridBagConstraints();
							gbc.fill = GridBagConstraints.BOTH;
							gbc.insets = new Insets(0, 0, 5, 0);
							gbc.gridx = 0;
							gbc.gridy = 0;
							add(button_0, gbc);
						}
						{
							JButton button_1 = new JButton();
							GridBagConstraints gbc = new GridBagConstraints();
							gbc.anchor = GridBagConstraints.WEST;
							gbc.fill = GridBagConstraints.VERTICAL;
							gbc.gridx = 0;
							gbc.gridy = 1;
							add(button_1, gbc);
						}
					}
				}""");
		{
			GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
			assertEquals(1, layout.getColumns().size());
			assertEquals(2, layout.getRows().size());
		}
	}

	@Test
	public void test_oneRow() throws Exception {
		ContainerInfo panel = parseContainer("""
				public class Test extends JPanel {
					public Test() {
						setSize(450, 300);
						setLayout(null);
						{
							JButton button_0 = new JButton();
							button_0.setBounds(4, 10, 100, 30);
							add(button_0);
						}
						{
							JButton button_1 = new JButton();
							button_1.setBounds(120, 13, 80, 15);
							add(button_1);
						}
					}
				}""");
		panel.refresh();
		// set GridBagLayout
		setLayout(panel, GridBagLayout.class);
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setSize(450, 300);
						GridBagLayout gridBagLayout = new GridBagLayout();
						gridBagLayout.columnWidths = new int[]{100, 80, 0};
						gridBagLayout.rowHeights = new int[]{30, 0};
						gridBagLayout.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
						gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
						setLayout(gridBagLayout);
						{
							JButton button_0 = new JButton();
							GridBagConstraints gbc = new GridBagConstraints();
							gbc.fill = GridBagConstraints.BOTH;
							gbc.insets = new Insets(0, 0, 0, 5);
							gbc.gridx = 0;
							gbc.gridy = 0;
							add(button_0, gbc);
						}
						{
							JButton button_1 = new JButton();
							GridBagConstraints gbc = new GridBagConstraints();
							gbc.fill = GridBagConstraints.HORIZONTAL;
							gbc.gridx = 1;
							gbc.gridy = 0;
							add(button_1, gbc);
						}
					}
				}""");
		{
			GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
			assertEquals(2, layout.getColumns().size());
			assertEquals(1, layout.getRows().size());
		}
	}

	@Test
	public void test_oneRow_top() throws Exception {
		ContainerInfo panel = parseContainer("""
				public class Test extends JPanel {
					public Test() {
						setSize(450, 300);
						setLayout(null);
						{
							JButton button_0 = new JButton();
							button_0.setBounds(4, 10, 100, 50);
							add(button_0);
						}
						{
							JButton button_1 = new JButton();
							button_1.setBounds(120, 11, 80, 20);
							add(button_1);
						}
					}
				}""");
		panel.refresh();
		// set GridBagLayout
		setLayout(panel, GridBagLayout.class);
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setSize(450, 300);
						GridBagLayout gridBagLayout = new GridBagLayout();
						gridBagLayout.columnWidths = new int[]{100, 80, 0};
						gridBagLayout.rowHeights = new int[]{50, 0};
						gridBagLayout.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
						gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
						setLayout(gridBagLayout);
						{
							JButton button_0 = new JButton();
							GridBagConstraints gbc = new GridBagConstraints();
							gbc.fill = GridBagConstraints.BOTH;
							gbc.insets = new Insets(0, 0, 0, 5);
							gbc.gridx = 0;
							gbc.gridy = 0;
							add(button_0, gbc);
						}
						{
							JButton button_1 = new JButton();
							GridBagConstraints gbc = new GridBagConstraints();
							gbc.anchor = GridBagConstraints.NORTH;
							gbc.fill = GridBagConstraints.HORIZONTAL;
							gbc.gridx = 1;
							gbc.gridy = 0;
							add(button_1, gbc);
						}
					}
				}""");
		{
			GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
			assertEquals(2, layout.getColumns().size());
			assertEquals(1, layout.getRows().size());
		}
	}

	@Test
	public void test_oneRow_bottom() throws Exception {
		ContainerInfo panel = parseContainer("""
				public class Test extends JPanel {
					public Test() {
						setSize(450, 300);
						setLayout(null);
						{
							JButton button_0 = new JButton();
							button_0.setBounds(4, 10, 100, 50);
							add(button_0);
						}
						{
							JButton button_1 = new JButton();
							button_1.setBounds(120, 38, 80, 20);
							add(button_1);
						}
					}
				}""");
		panel.refresh();
		// set GridBagLayout
		setLayout(panel, GridBagLayout.class);
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setSize(450, 300);
						GridBagLayout gridBagLayout = new GridBagLayout();
						gridBagLayout.columnWidths = new int[]{100, 80, 0};
						gridBagLayout.rowHeights = new int[]{50, 0};
						gridBagLayout.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
						gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
						setLayout(gridBagLayout);
						{
							JButton button_0 = new JButton();
							GridBagConstraints gbc = new GridBagConstraints();
							gbc.fill = GridBagConstraints.BOTH;
							gbc.insets = new Insets(0, 0, 0, 5);
							gbc.gridx = 0;
							gbc.gridy = 0;
							add(button_0, gbc);
						}
						{
							JButton button_1 = new JButton();
							GridBagConstraints gbc = new GridBagConstraints();
							gbc.anchor = GridBagConstraints.SOUTH;
							gbc.fill = GridBagConstraints.HORIZONTAL;
							gbc.gridx = 1;
							gbc.gridy = 0;
							add(button_1, gbc);
						}
					}
				}""");
		{
			GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
			assertEquals(2, layout.getColumns().size());
			assertEquals(1, layout.getRows().size());
		}
	}

	@Test
	public void test_oneRow_reorder() throws Exception {
		ContainerInfo panel = parseContainer("""
				public class Test extends JPanel {
					public Test() {
						setSize(450, 300);
						setLayout(null);
						{
							JButton button_1 = new JButton();
							button_1.setBounds(120, 13, 80, 18);
							add(button_1);
						}
						{
							JButton button_0 = new JButton();
							button_0.setBounds(4, 10, 100, 30);
							add(button_0);
						}
					}
				}""");
		panel.refresh();
		// set GridBagLayout
		setLayout(panel, GridBagLayout.class);
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setSize(450, 300);
						GridBagLayout gridBagLayout = new GridBagLayout();
						gridBagLayout.columnWidths = new int[]{100, 80, 0};
						gridBagLayout.rowHeights = new int[]{30, 0};
						gridBagLayout.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
						gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
						setLayout(gridBagLayout);
						{
							JButton button_0 = new JButton();
							GridBagConstraints gbc = new GridBagConstraints();
							gbc.fill = GridBagConstraints.BOTH;
							gbc.insets = new Insets(0, 0, 0, 5);
							gbc.gridx = 0;
							gbc.gridy = 0;
							add(button_0, gbc);
						}
						{
							JButton button_1 = new JButton();
							GridBagConstraints gbc = new GridBagConstraints();
							gbc.fill = GridBagConstraints.HORIZONTAL;
							gbc.gridx = 1;
							gbc.gridy = 0;
							add(button_1, gbc);
						}
					}
				}""");
		{
			GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
			assertEquals(2, layout.getColumns().size());
			assertEquals(1, layout.getRows().size());
		}
	}

	@Test
	public void test_Switching_fromGridLayout() throws Exception {
		ContainerInfo panel = parseContainer("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new GridLayout(3, 3, 0, 0));
						{
							JLabel label = new JLabel("New label");
							add(label);
						}
						{
							JTextField textField = new JTextField();
							add(textField);
							textField.setColumns(10);
						}
						{
							JTextArea textArea = new JTextArea();
							add(textArea);
						}
						{
							JCheckBox checkBox = new JCheckBox("New check box");
							add(checkBox);
						}
						{
							JButton button = new JButton("New button");
							add(button);
						}
					}
				}""");
		panel.refresh();
		// set GridBagLayout
		setLayout(panel, GridBagLayout.class);
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						GridBagLayout gridBagLayout = new GridBagLayout();
						gridBagLayout.columnWidths = new int[]{225, 225, 0};
						gridBagLayout.rowHeights = new int[]{100, 100, 100, 0};
						gridBagLayout.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
						gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
						setLayout(gridBagLayout);
						{
							JLabel label = new JLabel("New label");
							GridBagConstraints gbc = new GridBagConstraints();
							gbc.fill = GridBagConstraints.BOTH;
							gbc.insets = new Insets(0, 0, 5, 5);
							gbc.gridx = 0;
							gbc.gridy = 0;
							add(label, gbc);
						}
						{
							JTextField textField = new JTextField();
							GridBagConstraints gbc = new GridBagConstraints();
							gbc.fill = GridBagConstraints.BOTH;
							gbc.insets = new Insets(0, 0, 5, 0);
							gbc.gridx = 1;
							gbc.gridy = 0;
							add(textField, gbc);
							textField.setColumns(10);
						}
						{
							JTextArea textArea = new JTextArea();
							GridBagConstraints gbc = new GridBagConstraints();
							gbc.fill = GridBagConstraints.BOTH;
							gbc.insets = new Insets(0, 0, 5, 5);
							gbc.gridx = 0;
							gbc.gridy = 1;
							add(textArea, gbc);
						}
						{
							JCheckBox checkBox = new JCheckBox("New check box");
							GridBagConstraints gbc = new GridBagConstraints();
							gbc.fill = GridBagConstraints.BOTH;
							gbc.insets = new Insets(0, 0, 5, 0);
							gbc.gridx = 1;
							gbc.gridy = 1;
							add(checkBox, gbc);
						}
						{
							JButton button = new JButton("New button");
							GridBagConstraints gbc = new GridBagConstraints();
							gbc.fill = GridBagConstraints.BOTH;
							gbc.insets = new Insets(0, 0, 0, 5);
							gbc.gridx = 0;
							gbc.gridy = 2;
							add(button, gbc);
						}
					}
				}""");
	}

	@Test
	public void test_reorderOnConvert_lazy() throws Exception {
		ContainerInfo panel = parseContainer("""
				class Test extends JPanel {
					private JButton buttonA;
					private JButton buttonB;
					public Test() {
						setLayout(null);
						add(getButtonA());
						add(getButtonB());
					}
					private JButton getButtonA() {
						if (buttonA == null) {
							buttonA = new JButton();
							buttonA.setBounds(200, 200, 50, 25);
						}
						return buttonA;
					}
					private JButton getButtonB() {
						if (buttonB == null) {
							buttonB = new JButton();
							buttonB.setBounds(0, 0, 50, 25);
						}
						return buttonB;
					}
				}""");
		panel.refresh();
		// set GridBagLayout
		setLayout(panel, GridBagLayout.class);
		assertEditor("""
				class Test extends JPanel {
					private JButton buttonA;
					private JButton buttonB;
					public Test() {
						GridBagLayout gridBagLayout = new GridBagLayout();
						gridBagLayout.columnWidths = new int[]{50, 150, 50, 0};
						gridBagLayout.rowHeights = new int[]{25, 175, 25, 0};
						gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
						gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
						setLayout(gridBagLayout);
						GridBagConstraints gbc_1 = new GridBagConstraints();
						gbc_1.fill = GridBagConstraints.BOTH;
						gbc_1.insets = new Insets(0, 0, 5, 5);
						gbc_1.gridx = 0;
						gbc_1.gridy = 0;
						add(getButtonB(), gbc_1);
						GridBagConstraints gbc = new GridBagConstraints();
						gbc.fill = GridBagConstraints.BOTH;
						gbc.gridx = 2;
						gbc.gridy = 2;
						add(getButtonA(), gbc);
					}
					private JButton getButtonA() {
						if (buttonA == null) {
							buttonA = new JButton();
						}
						return buttonA;
					}
					private JButton getButtonB() {
						if (buttonB == null) {
							buttonB = new JButton();
						}
						return buttonB;
					}
				}""");
	}
}
