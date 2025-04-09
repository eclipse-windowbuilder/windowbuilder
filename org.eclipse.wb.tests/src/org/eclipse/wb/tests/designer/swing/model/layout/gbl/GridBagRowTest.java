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
package org.eclipse.wb.tests.designer.swing.model.layout.gbl;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.GridBagLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.RowInfo;

import org.junit.Test;

import java.awt.GridBagConstraints;

/**
 * Test for {@link RowInfo}.
 *
 * @author scheglov_ke
 */
public class GridBagRowTest extends AbstractGridBagLayoutTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// getAlignment(), setAlignment()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link RowInfo#getAlignment()}.<br>
	 * No components, so unknown alignment.
	 */
	@Test
	public void test_getAlignment_1() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  public Test() {",
						"    GridBagLayout layout = new GridBagLayout();",
						"    layout.rowHeights = new int[]{0};",
						"    setLayout(layout);",
						"  }",
						"}");
		panel.refresh();
		GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
		assertEquals(1, layout.getRows().size());
		assertSame(RowInfo.Alignment.UNKNOWN, layout.getRows().get(0).getAlignment());
	}

	/**
	 * Test for {@link RowInfo#getAlignment()}.<br>
	 * Single component with {@link GridBagConstraints#NORTH}, so "top" alignment.
	 */
	@Test
	public void test_getAlignment_2() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  public Test() {",
						"    GridBagLayout layout = new GridBagLayout();",
						"    setLayout(layout);",
						"    {",
						"      JButton button = new JButton();",
						"      GridBagConstraints gbc = new GridBagConstraints();",
						"      gbc.gridx = 0;",
						"      gbc.gridy = 0;",
						"      gbc.anchor = GridBagConstraints.NORTH;",
						"      add(button, gbc);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
		assertEquals(1, layout.getRows().size());
		assertSame(RowInfo.Alignment.TOP, layout.getRows().get(0).getAlignment());
	}

	/**
	 * Test for {@link RowInfo#getAlignment()}.<br>
	 * Two components with different alignments, so "unknown" alignment for {@link RowInfo}.
	 */
	@Test
	public void test_getAlignment_3() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  public Test() {",
						"    GridBagLayout layout = new GridBagLayout();",
						"    setLayout(layout);",
						"    {",
						"      JButton button = new JButton();",
						"      GridBagConstraints gbc = new GridBagConstraints();",
						"      gbc.gridx = 0;",
						"      gbc.gridy = 0;",
						"      gbc.anchor = GridBagConstraints.NORTH;",
						"      add(button, gbc);",
						"    }",
						"    {",
						"      JButton button = new JButton();",
						"      GridBagConstraints gbc = new GridBagConstraints();",
						"      gbc.gridx = 1;",
						"      gbc.gridy = 0;",
						"      gbc.anchor = GridBagConstraints.SOUTH;",
						"      add(button, gbc);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
		// prepare row
		RowInfo row;
		{
			assertEquals(1, layout.getRows().size());
			row = layout.getRows().get(0);
		}
		// no common alignment
		assertSame(RowInfo.Alignment.UNKNOWN, row.getAlignment());
		// set alignment
		row.setAlignment(RowInfo.Alignment.BOTTOM);
		assertSame(RowInfo.Alignment.BOTTOM, row.getAlignment());
		assertEditor(
				"class Test extends JPanel {",
				"  public Test() {",
				"    GridBagLayout layout = new GridBagLayout();",
				"    setLayout(layout);",
				"    {",
				"      JButton button = new JButton();",
				"      GridBagConstraints gbc = new GridBagConstraints();",
				"      gbc.gridx = 0;",
				"      gbc.gridy = 0;",
				"      gbc.anchor = GridBagConstraints.SOUTH;",
				"      add(button, gbc);",
				"    }",
				"    {",
				"      JButton button = new JButton();",
				"      GridBagConstraints gbc = new GridBagConstraints();",
				"      gbc.gridx = 1;",
				"      gbc.gridy = 0;",
				"      gbc.anchor = GridBagConstraints.SOUTH;",
				"      add(button, gbc);",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// DELETE
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Delete first row, component in next row moved.
	 */
	@Test
	public void test_DELETE_1() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  public Test() {",
						"    GridBagLayout layout = new GridBagLayout();",
						"    layout.columnWidths = new int[] {1, 2, 3};",
						"    layout.rowHeights = new int[] {1, 2, 3};",
						"    layout.columnWeights = new double[] {0.1, 0.2, Double.MIN_VALUE};",
						"    layout.rowWeights = new double[] {0.1, 0.2, Double.MIN_VALUE};",
						"    setLayout(layout);",
						"    {",
						"      JButton button_1 = new JButton();",
						"      GridBagConstraints gbc = new GridBagConstraints();",
						"      gbc.gridx = 0;",
						"      gbc.gridy = 0;",
						"      add(button_1, gbc);",
						"    }",
						"    {",
						"      JButton button_2 = new JButton();",
						"      GridBagConstraints gbc = new GridBagConstraints();",
						"      gbc.gridx = 1;",
						"      gbc.gridy = 1;",
						"      add(button_2, gbc);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
		assertEquals(2, layout.getColumns().size());
		assertEquals(2, layout.getRows().size());
		// do delete
		layout.getRowOperations().delete(0);
		assertEquals(2, layout.getColumns().size());
		assertEquals(1, layout.getRows().size());
		assertEditor(
				"class Test extends JPanel {",
				"  public Test() {",
				"    GridBagLayout layout = new GridBagLayout();",
				"    layout.columnWidths = new int[] {1, 2, 3};",
				"    layout.rowHeights = new int[] {2, 3};",
				"    layout.columnWeights = new double[] {0.1, 0.2, Double.MIN_VALUE};",
				"    layout.rowWeights = new double[] {0.2, Double.MIN_VALUE};",
				"    setLayout(layout);",
				"    {",
				"      JButton button_2 = new JButton();",
				"      GridBagConstraints gbc = new GridBagConstraints();",
				"      gbc.gridx = 1;",
				"      gbc.gridy = 0;",
				"      add(button_2, gbc);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * When delete row, height of spanned components may be decreased.
	 */
	@Test
	public void test_DELETE_2() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  public Test() {",
						"    GridBagLayout layout = new GridBagLayout();",
						"    layout.columnWidths = new int[] {1, 2, 3};",
						"    layout.rowHeights = new int[] {1, 2, 3};",
						"    layout.columnWeights = new double[] {0.1, 0.2, Double.MIN_VALUE};",
						"    layout.rowWeights = new double[] {0.1, 0.2, Double.MIN_VALUE};",
						"    setLayout(layout);",
						"    {",
						"      JButton button_0 = new JButton();",
						"      GridBagConstraints gbc = new GridBagConstraints();",
						"      gbc.insets = new Insets(0, 0, 5, 5);",
						"      gbc.gridx = 0;",
						"      gbc.gridy = 0;",
						"      gbc.gridheight = 2;",
						"      add(button_0, gbc);",
						"    }",
						"    {",
						"      JButton button_1 = new JButton();",
						"      GridBagConstraints gbc = new GridBagConstraints();",
						"      gbc.gridx = 1;",
						"      gbc.gridy = 1;",
						"      add(button_1, gbc);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
		assertEquals(2, layout.getColumns().size());
		assertEquals(2, layout.getRows().size());
		// do delete
		layout.getRowOperations().delete(1);
		assertEquals(2, layout.getColumns().size());
		assertEquals(1, layout.getRows().size());
		assertEditor(
				"class Test extends JPanel {",
				"  public Test() {",
				"    GridBagLayout layout = new GridBagLayout();",
				"    layout.columnWidths = new int[] {1, 2, 3};",
				"    layout.rowHeights = new int[] {1, 3};",
				"    layout.columnWeights = new double[] {0.1, 0.2, Double.MIN_VALUE};",
				"    layout.rowWeights = new double[] {0.1, Double.MIN_VALUE};",
				"    setLayout(layout);",
				"    {",
				"      JButton button_0 = new JButton();",
				"      GridBagConstraints gbc = new GridBagConstraints();",
				"      gbc.insets = new Insets(0, 0, 0, 5);",
				"      gbc.gridx = 0;",
				"      gbc.gridy = 0;",
				"      add(button_0, gbc);",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// MOVE
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Move row backward.
	 */
	@Test
	public void test_MOVE_backward() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  public Test() {",
						"    GridBagLayout layout = new GridBagLayout();",
						"    layout.columnWidths = new int[] {1, 2, 3, 4};",
						"    layout.rowHeights = new int[] {1, 2, 3, 4};",
						"    layout.columnWeights = new double[] {0.1, 0.2, 0.3, Double.MIN_VALUE};",
						"    layout.rowWeights = new double[] {0.1, 0.2, 0.3, Double.MIN_VALUE};",
						"    setLayout(layout);",
						"    {",
						"      JButton button_0 = new JButton();",
						"      GridBagConstraints gbc = new GridBagConstraints();",
						"      gbc.insets = new Insets(0, 0, 5, 5);",
						"      gbc.gridx = 0;",
						"      gbc.gridy = 0;",
						"      add(button_0, gbc);",
						"    }",
						"    {",
						"      JButton button_1 = new JButton();",
						"      GridBagConstraints gbc = new GridBagConstraints();",
						"      gbc.insets = new Insets(0, 0, 5, 5);",
						"      gbc.gridx = 1;",
						"      gbc.gridy = 1;",
						"      add(button_1, gbc);",
						"    }",
						"    {",
						"      JButton button_2 = new JButton();",
						"      GridBagConstraints gbc = new GridBagConstraints();",
						"      gbc.gridx = 2;",
						"      gbc.gridy = 2;",
						"      add(button_2, gbc);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		final GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
		assertEquals(3, layout.getRows().size());
		// do move
		ExecutionUtils.run(panel, new RunnableEx() {
			@Override
			public void run() throws Exception {
				layout.getRowOperations().move(2, 0);
			}
		});
		assertEquals(3, layout.getRows().size());
		assertEditor(
				"class Test extends JPanel {",
				"  public Test() {",
				"    GridBagLayout layout = new GridBagLayout();",
				"    layout.columnWidths = new int[] {1, 2, 3, 4};",
				"    layout.rowHeights = new int[] {3, 1, 2, 4};",
				"    layout.columnWeights = new double[] {0.1, 0.2, 0.3, Double.MIN_VALUE};",
				"    layout.rowWeights = new double[] {0.3, 0.1, 0.2, Double.MIN_VALUE};",
				"    setLayout(layout);",
				"    {",
				"      JButton button_2 = new JButton();",
				"      GridBagConstraints gbc = new GridBagConstraints();",
				"      gbc.insets = new Insets(0, 0, 5, 0);",
				"      gbc.gridx = 2;",
				"      gbc.gridy = 0;",
				"      add(button_2, gbc);",
				"    }",
				"    {",
				"      JButton button_0 = new JButton();",
				"      GridBagConstraints gbc = new GridBagConstraints();",
				"      gbc.insets = new Insets(0, 0, 5, 5);",
				"      gbc.gridx = 0;",
				"      gbc.gridy = 1;",
				"      add(button_0, gbc);",
				"    }",
				"    {",
				"      JButton button_1 = new JButton();",
				"      GridBagConstraints gbc = new GridBagConstraints();",
				"      gbc.insets = new Insets(0, 0, 0, 5);",
				"      gbc.gridx = 1;",
				"      gbc.gridy = 2;",
				"      add(button_1, gbc);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Move row forward.
	 */
	@Test
	public void test_MOVE_forward() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  public Test() {",
						"    GridBagLayout layout = new GridBagLayout();",
						"    layout.columnWidths = new int[] {1, 2, 3, 4};",
						"    layout.rowHeights = new int[] {1, 2, 3, 4};",
						"    layout.columnWeights = new double[] {0.1, 0.2, 0.3, Double.MIN_VALUE};",
						"    layout.rowWeights = new double[] {0.1, 0.2, 0.3, Double.MIN_VALUE};",
						"    setLayout(layout);",
						"    {",
						"      JButton button_0 = new JButton();",
						"      GridBagConstraints gbc = new GridBagConstraints();",
						"      gbc.insets = new Insets(0, 0, 5, 5);",
						"      gbc.gridx = 0;",
						"      gbc.gridy = 0;",
						"      add(button_0, gbc);",
						"    }",
						"    {",
						"      JButton button_1 = new JButton();",
						"      GridBagConstraints gbc = new GridBagConstraints();",
						"      gbc.insets = new Insets(0, 0, 5, 5);",
						"      gbc.gridx = 1;",
						"      gbc.gridy = 1;",
						"      add(button_1, gbc);",
						"    }",
						"    {",
						"      JButton button_2 = new JButton();",
						"      GridBagConstraints gbc = new GridBagConstraints();",
						"      gbc.gridx = 2;",
						"      gbc.gridy = 2;",
						"      add(button_2, gbc);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		final GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
		assertEquals(3, layout.getRows().size());
		// do move
		ExecutionUtils.run(panel, new RunnableEx() {
			@Override
			public void run() throws Exception {
				layout.getRowOperations().move(0, 3);
			}
		});
		assertEquals(3, layout.getRows().size());
		assertEditor(
				"class Test extends JPanel {",
				"  public Test() {",
				"    GridBagLayout layout = new GridBagLayout();",
				"    layout.columnWidths = new int[] {1, 2, 3, 4};",
				"    layout.rowHeights = new int[] {2, 3, 1, 4};",
				"    layout.columnWeights = new double[] {0.1, 0.2, 0.3, Double.MIN_VALUE};",
				"    layout.rowWeights = new double[] {0.2, 0.3, 0.1, Double.MIN_VALUE};",
				"    setLayout(layout);",
				"    {",
				"      JButton button_1 = new JButton();",
				"      GridBagConstraints gbc = new GridBagConstraints();",
				"      gbc.insets = new Insets(0, 0, 5, 5);",
				"      gbc.gridx = 1;",
				"      gbc.gridy = 0;",
				"      add(button_1, gbc);",
				"    }",
				"    {",
				"      JButton button_2 = new JButton();",
				"      GridBagConstraints gbc = new GridBagConstraints();",
				"      gbc.insets = new Insets(0, 0, 5, 0);",
				"      gbc.gridx = 2;",
				"      gbc.gridy = 1;",
				"      add(button_2, gbc);",
				"    }",
				"    {",
				"      JButton button_0 = new JButton();",
				"      GridBagConstraints gbc = new GridBagConstraints();",
				"      gbc.insets = new Insets(0, 0, 0, 5);",
				"      gbc.gridx = 0;",
				"      gbc.gridy = 2;",
				"      add(button_0, gbc);",
				"    }",
				"  }",
				"}");
	}
}
