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
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.ColumnInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.DimensionInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.DimensionOperations;
import org.eclipse.wb.internal.swing.model.layout.gbl.GridBagLayoutInfo;

import org.eclipse.draw2d.geometry.Rectangle;

import java.awt.GridBagConstraints;
import java.util.List;

/**
 * Test for {@link ColumnInfo}.
 *
 * @author scheglov_ke
 */
public class GridBagColumnTest extends AbstractGridBagLayoutTest {
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
  // size/weight
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link DimensionInfo#setSize(int)}.<br>
   * Assignment to <code>columnWidths</code> exists, so just replace element.
   */
  public void test_setSize_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout layout = new GridBagLayout();",
            "    layout.columnWidths = new int[]{0};",
            "    setLayout(layout);",
            "    {",
            "      JButton button = new JButton();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 0;",
            "      gbc.gridy = 0;",
            "      add(button, gbc);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
    // prepare column
    assertEquals(1, layout.getColumns().size());
    ColumnInfo column = layout.getColumns().get(0);
    assertEquals(0, column.getSize());
    // set size
    column.setSize(100);
    assertEquals(100, column.getSize());
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    GridBagLayout layout = new GridBagLayout();",
        "    layout.columnWidths = new int[]{100};",
        "    setLayout(layout);",
        "    {",
        "      JButton button = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 0;",
        "      add(button, gbc);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link DimensionInfo#setSize(int)}.<br>
   * Assignment to <code>columnWidths</code> does not exist, so should be added first.
   */
  public void test_setSize_2() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout layout = new GridBagLayout();",
            "    setLayout(layout);",
            "    {",
            "      JButton button_0 = new JButton();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 0;",
            "      gbc.gridy = 0;",
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
    // prepare column
    assertEquals(2, layout.getColumns().size());
    ColumnInfo column = layout.getColumns().get(0);
    assertEquals(0, column.getSize());
    // set size
    column.setSize(100);
    assertEquals(100, column.getSize());
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    GridBagLayout layout = new GridBagLayout();",
        "    layout.columnWidths = new int[]{100, 0};",
        "    setLayout(layout);",
        "    {",
        "      JButton button_0 = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 0;",
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
  }

  /**
   * Test for {@link DimensionInfo#setWeight(double)}.<br>
   * Assignment to <code>columnWeights</code> exists, so just replace element.
   */
  public void test_setWeight_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout layout = new GridBagLayout();",
            "    layout.columnWeights = new double[]{0.0};",
            "    setLayout(layout);",
            "    {",
            "      JButton button = new JButton();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 0;",
            "      gbc.gridy = 0;",
            "      add(button, gbc);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
    // prepare column
    assertEquals(1, layout.getColumns().size());
    ColumnInfo column = layout.getColumns().get(0);
    assertEquals(0.0, column.getWeight(), 1.0E-6);
    // set size
    column.setWeight(2.0);
    assertEquals(2.0, column.getWeight(), 1.0E-6);
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    GridBagLayout layout = new GridBagLayout();",
        "    layout.columnWeights = new double[]{2.0};",
        "    setLayout(layout);",
        "    {",
        "      JButton button = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 0;",
        "      add(button, gbc);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link DimensionInfo#setWeight(double)}.<br>
   * Assignment to <code>columnWeights</code> does not exist, so should be added first.
   */
  public void test_setWeight_2() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout layout = new GridBagLayout();",
            "    setLayout(layout);",
            "    {",
            "      JButton button_0 = new JButton();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 0;",
            "      gbc.gridy = 0;",
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
    // prepare column
    assertEquals(2, layout.getColumns().size());
    ColumnInfo column = layout.getColumns().get(0);
    assertEquals(0.0, column.getWeight(), 1.0E-6);
    // set size
    column.setWeight(2.0);
    assertEquals(2.0, column.getWeight(), 1.0E-6);
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    GridBagLayout layout = new GridBagLayout();",
        "    layout.columnWeights = new double[]{2.0, 0.0};",
        "    setLayout(layout);",
        "    {",
        "      JButton button_0 = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 0;",
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
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getAlignment(), setAlignment()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ColumnInfo#getAlignment()}.<br>
   * No components, so unknown alignment.
   */
  public void test_getAlignment_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout layout = new GridBagLayout();",
            "    layout.columnWidths = new int[]{0};",
            "    setLayout(layout);",
            "  }",
            "}");
    panel.refresh();
    GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
    assertEquals(1, layout.getColumns().size());
    assertSame(ColumnInfo.Alignment.UNKNOWN, layout.getColumns().get(0).getAlignment());
  }

  /**
   * Test for {@link ColumnInfo#getAlignment()}.<br>
   * Single component with {@link GridBagConstraints#WEST}, so "left" alignment.
   */
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
            "      gbc.anchor = GridBagConstraints.WEST;",
            "      add(button, gbc);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
    assertEquals(1, layout.getColumns().size());
    assertSame(ColumnInfo.Alignment.LEFT, layout.getColumns().get(0).getAlignment());
  }

  /**
   * Test for {@link ColumnInfo#getAlignment()}.<br>
   * Two components with different alignments, so "unknown" alignment for {@link ColumnInfo}.
   */
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
            "      gbc.anchor = GridBagConstraints.WEST;",
            "      add(button, gbc);",
            "    }",
            "    {",
            "      JButton button = new JButton();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 0;",
            "      gbc.gridy = 1;",
            "      gbc.anchor = GridBagConstraints.EAST;",
            "      add(button, gbc);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
    // prepare column
    ColumnInfo column;
    {
      assertEquals(1, layout.getColumns().size());
      column = layout.getColumns().get(0);
    }
    // no common alignment
    assertSame(ColumnInfo.Alignment.UNKNOWN, column.getAlignment());
    // set alignment
    column.setAlignment(ColumnInfo.Alignment.RIGHT);
    assertSame(ColumnInfo.Alignment.RIGHT, column.getAlignment());
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
        "      gbc.anchor = GridBagConstraints.EAST;",
        "      add(button, gbc);",
        "    }",
        "    {",
        "      JButton button = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 1;",
        "      gbc.anchor = GridBagConstraints.EAST;",
        "      add(button, gbc);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // INSERT
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Simple test for {@link DimensionOperations#insert(int)}.<br>
   * When we append new column, this causes gaps in previously last column.
   */
  public void test_insert() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout layout = new GridBagLayout();",
            "    layout.columnWidths = new int[] {1, 2};",
            "    layout.rowHeights = new int[] {0, 0};",
            "    layout.columnWeights = new double[] {0.1, Double.MIN_VALUE};",
            "    layout.rowWeights = new double[] {0.0, Double.MIN_VALUE};",
            "    setLayout(layout);",
            "    {",
            "      JButton button_0 = new JButton();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 0;",
            "      gbc.gridy = 0;",
            "      add(button_0, gbc);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    final GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
    assertEquals(1, layout.getColumns().size());
    assertEquals(1, layout.getRows().size());
    // append column
    ExecutionUtils.run(panel, new RunnableEx() {
      @Override
      public void run() throws Exception {
        layout.getColumnOperations().insert(1);
      }
    });
    assertEquals(2, layout.getColumns().size());
    assertEquals(1, layout.getRows().size());
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    GridBagLayout layout = new GridBagLayout();",
        "    layout.columnWidths = new int[] {1, 0, 2};",
        "    layout.rowHeights = new int[] {0, 0};",
        "    layout.columnWeights = new double[] {0.1, 0.0, Double.MIN_VALUE};",
        "    layout.rowWeights = new double[] {0.0, Double.MIN_VALUE};",
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
  // DELETE
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Delete first column, component in next column moved.
   */
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
    layout.getColumnOperations().delete(0);
    assertEquals(1, layout.getColumns().size());
    assertEquals(2, layout.getRows().size());
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    GridBagLayout layout = new GridBagLayout();",
        "    layout.columnWidths = new int[] {2, 3};",
        "    layout.rowHeights = new int[] {1, 2, 3};",
        "    layout.columnWeights = new double[] {0.2, Double.MIN_VALUE};",
        "    layout.rowWeights = new double[] {0.1, 0.2, Double.MIN_VALUE};",
        "    setLayout(layout);",
        "    {",
        "      JButton button_2 = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 1;",
        "      add(button_2, gbc);",
        "    }",
        "  }",
        "}");
  }

  /**
   * When delete column, width of spanned components may be decreased.<br>
   * When delete column, gaps should be fixed.
   */
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
            "      gbc.gridwidth = 2;",
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
    layout.getColumnOperations().delete(1);
    assertEquals(1, layout.getColumns().size());
    assertEquals(2, layout.getRows().size());
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    GridBagLayout layout = new GridBagLayout();",
        "    layout.columnWidths = new int[] {1, 3};",
        "    layout.rowHeights = new int[] {1, 2, 3};",
        "    layout.columnWeights = new double[] {0.1, Double.MIN_VALUE};",
        "    layout.rowWeights = new double[] {0.1, 0.2, Double.MIN_VALUE};",
        "    setLayout(layout);",
        "    {",
        "      JButton button_0 = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.insets = new Insets(0, 0, 5, 0);",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 0;",
        "      add(button_0, gbc);",
        "    }",
        "  }",
        "}");
  }

  /**
   * We should ignore "columnWidths" if there are not enough elements.
   */
  public void test_DELETE_whenNotEnoughWidthElements() throws Exception {
    parseContainer(
        "class Test extends JPanel {",
        "  public Test() {",
        "    GridBagLayout layout = new GridBagLayout();",
        "    layout.columnWidths = new int[] {1};",
        "    layout.columnWeights = new double[] {0.1, 0.2, 0.3};",
        "    setLayout(layout);",
        "    {",
        "      JButton button = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.gridx = 2;",
        "      gbc.gridy = 0;",
        "      add(button, gbc);",
        "    }",
        "  }",
        "}");
    refresh();
    final GridBagLayoutInfo layout = getJavaInfoByName("layout");
    assertEquals(3, layout.getColumns().size());
    // do delete
    ExecutionUtils.run(layout, new RunnableEx() {
      @Override
      public void run() throws Exception {
        layout.getColumnOperations().delete(1);
      }
    });
    assertEquals(2, layout.getColumns().size());
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    GridBagLayout layout = new GridBagLayout();",
        "    layout.columnWidths = new int[] {1};",
        "    layout.columnWeights = new double[] {0.1, 0.3};",
        "    setLayout(layout);",
        "    {",
        "      JButton button = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.gridx = 1;",
        "      gbc.gridy = 0;",
        "      add(button, gbc);",
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
   * Move column backward.
   */
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
    List<ColumnInfo> columns = layout.getColumns();
    // initial columns
    assertEquals(3, columns.size());
    ColumnInfo column_0 = columns.get(0);
    ColumnInfo column_1 = columns.get(1);
    ColumnInfo column_2 = columns.get(2);
    // do move
    ExecutionUtils.run(panel, new RunnableEx() {
      @Override
      public void run() throws Exception {
        layout.getColumnOperations().move(2, 0);
      }
    });
    assertEquals(3, columns.size());
    assertSame(column_2, columns.get(0));
    assertSame(column_0, columns.get(1));
    assertSame(column_1, columns.get(2));
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    GridBagLayout layout = new GridBagLayout();",
        "    layout.columnWidths = new int[] {3, 1, 2, 4};",
        "    layout.rowHeights = new int[] {1, 2, 3, 4};",
        "    layout.columnWeights = new double[] {0.3, 0.1, 0.2, Double.MIN_VALUE};",
        "    layout.rowWeights = new double[] {0.1, 0.2, 0.3, Double.MIN_VALUE};",
        "    setLayout(layout);",
        "    {",
        "      JButton button_0 = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.insets = new Insets(0, 0, 5, 5);",
        "      gbc.gridx = 1;",
        "      gbc.gridy = 0;",
        "      add(button_0, gbc);",
        "    }",
        "    {",
        "      JButton button_1 = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.insets = new Insets(0, 0, 5, 0);",
        "      gbc.gridx = 2;",
        "      gbc.gridy = 1;",
        "      add(button_1, gbc);",
        "    }",
        "    {",
        "      JButton button_2 = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.insets = new Insets(0, 0, 0, 5);",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 2;",
        "      add(button_2, gbc);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Move column forward.
   */
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
    List<ColumnInfo> columns = layout.getColumns();
    // initial columns
    assertEquals(3, columns.size());
    ColumnInfo column_0 = columns.get(0);
    ColumnInfo column_1 = columns.get(1);
    ColumnInfo column_2 = columns.get(2);
    // do move
    ExecutionUtils.run(panel, new RunnableEx() {
      @Override
      public void run() throws Exception {
        layout.getColumnOperations().move(0, 3);
      }
    });
    assertEquals(3, columns.size());
    assertSame(column_1, columns.get(0));
    assertSame(column_2, columns.get(1));
    assertSame(column_0, columns.get(2));
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    GridBagLayout layout = new GridBagLayout();",
        "    layout.columnWidths = new int[] {2, 3, 1, 4};",
        "    layout.rowHeights = new int[] {1, 2, 3, 4};",
        "    layout.columnWeights = new double[] {0.2, 0.3, 0.1, Double.MIN_VALUE};",
        "    layout.rowWeights = new double[] {0.1, 0.2, 0.3, Double.MIN_VALUE};",
        "    setLayout(layout);",
        "    {",
        "      JButton button_0 = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.insets = new Insets(0, 0, 5, 0);",
        "      gbc.gridx = 2;",
        "      gbc.gridy = 0;",
        "      add(button_0, gbc);",
        "    }",
        "    {",
        "      JButton button_1 = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.insets = new Insets(0, 0, 5, 5);",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 1;",
        "      add(button_1, gbc);",
        "    }",
        "    {",
        "      JButton button_2 = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.insets = new Insets(0, 0, 0, 5);",
        "      gbc.gridx = 1;",
        "      gbc.gridy = 2;",
        "      add(button_2, gbc);",
        "    }",
        "  }",
        "}");
  }

  /**
   * There was bug in moving column inside of horizontally spanned component.<br>
   * So, we need this test to reproduce and never repeat this problem again.
   */
  public void test_MOVE_inside() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new GridBagLayout());",
            "    {",
            "      JButton button_0 = new JButton();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 0;",
            "      gbc.gridy = 0;",
            "      gbc.insets = new Insets(0, 0, 5, 5);",
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
    final GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
    assertEquals(2, layout.getColumns().size());
    // set horizontal span for "button_0"
    {
      ComponentInfo button_0 = panel.getChildrenComponents().get(0);
      layout.command_setCells(button_0, new Rectangle(0, 0, 2, 1));
      assertEditor(
          "class Test extends JPanel {",
          "  public Test() {",
          "    setLayout(new GridBagLayout());",
          "    {",
          "      JButton button_0 = new JButton();",
          "      GridBagConstraints gbc = new GridBagConstraints();",
          "      gbc.gridwidth = 2;",
          "      gbc.gridx = 0;",
          "      gbc.gridy = 0;",
          "      gbc.insets = new Insets(0, 0, 5, 0);",
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
    }
    // add new component
    ComponentInfo button_2;
    {
      button_2 = createJButton();
      layout.command_CREATE(button_2, 2, false, 1, false);
      assertEquals(3, layout.getColumns().size());
      assertEditor(
          "class Test extends JPanel {",
          "  public Test() {",
          "    setLayout(new GridBagLayout());",
          "    {",
          "      JButton button_0 = new JButton();",
          "      GridBagConstraints gbc = new GridBagConstraints();",
          "      gbc.gridwidth = 2;",
          "      gbc.gridx = 0;",
          "      gbc.gridy = 0;",
          "      gbc.insets = new Insets(0, 0, 5, 5);",
          "      add(button_0, gbc);",
          "    }",
          "    {",
          "      JButton button_1 = new JButton();",
          "      GridBagConstraints gbc = new GridBagConstraints();",
          "      gbc.insets = new Insets(0, 0, 0, 5);",
          "      gbc.gridx = 1;",
          "      gbc.gridy = 1;",
          "      add(button_1, gbc);",
          "    }",
          "    {",
          "      JButton button = new JButton();",
          "      GridBagConstraints gbc = new GridBagConstraints();",
          "      gbc.gridx = 2;",
          "      gbc.gridy = 1;",
          "      add(button, gbc);",
          "    }",
          "  }",
          "}");
    }
    // move column
    ExecutionUtils.run(panel, new RunnableEx() {
      @Override
      public void run() throws Exception {
        layout.getColumnOperations().move(2, 1);
      }
    });
    assertEquals(3, layout.getColumns().size());
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new GridBagLayout());",
        "    {",
        "      JButton button_0 = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.gridwidth = 3;",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 0;",
        "      gbc.insets = new Insets(0, 0, 5, 0);",
        "      add(button_0, gbc);",
        "    }",
        "    {",
        "      JButton button = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.insets = new Insets(0, 0, 0, 5);",
        "      gbc.gridx = 1;",
        "      gbc.gridy = 1;",
        "      add(button, gbc);",
        "    }",
        "    {",
        "      JButton button_1 = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.gridx = 2;",
        "      gbc.gridy = 1;",
        "      add(button_1, gbc);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // normalizeSpanning()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link DimensionOperations#normalizeSpanning()}.<br>
   * Single component, so no changes.
   */
  public void test_normalizeSpanning_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new GridBagLayout());",
            "    {",
            "      JButton button_0 = new JButton();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 0;",
            "      gbc.gridy = 0;",
            "      add(button_0, gbc);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    final GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
    //
    ExecutionUtils.run(panel, new RunnableEx() {
      @Override
      public void run() throws Exception {
        layout.getColumnOperations().normalizeSpanning();
      }
    });
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new GridBagLayout());",
        "    {",
        "      JButton button_0 = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 0;",
        "      add(button_0, gbc);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link DimensionOperations#normalizeSpanning()}.<br>
   * Button is spanned on 2 columns, and both required, so no changes.
   */
  public void test_normalizeSpanning_2() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new GridBagLayout());",
            "    {",
            "      JButton button_00 = new JButton();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 0;",
            "      gbc.gridy = 0;",
            "      gbc.gridwidth = 2;",
            "      add(button_00, gbc);",
            "    }",
            "    {",
            "      JButton button_01 = new JButton();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 0;",
            "      gbc.gridy = 1;",
            "      add(button_01, gbc);",
            "    }",
            "    {",
            "      JButton button_11 = new JButton();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 1;",
            "      gbc.gridy = 1;",
            "      add(button_11, gbc);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    final GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
    //
    ExecutionUtils.run(panel, new RunnableEx() {
      @Override
      public void run() throws Exception {
        layout.getColumnOperations().normalizeSpanning();
      }
    });
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new GridBagLayout());",
        "    {",
        "      JButton button_00 = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 0;",
        "      gbc.gridwidth = 2;",
        "      add(button_00, gbc);",
        "    }",
        "    {",
        "      JButton button_01 = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 1;",
        "      add(button_01, gbc);",
        "    }",
        "    {",
        "      JButton button_11 = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.gridx = 1;",
        "      gbc.gridy = 1;",
        "      add(button_11, gbc);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link DimensionOperations#normalizeSpanning()}.<br>
   * Button is spanned on 2 columns, but only one required.
   */
  public void test_normalizeSpanning_3() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new GridBagLayout());",
            "    {",
            "      JButton button_0 = new JButton();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 0;",
            "      gbc.gridy = 0;",
            "      gbc.gridwidth = 2;",
            "      add(button_0, gbc);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    final GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
    //
    ExecutionUtils.run(panel, new RunnableEx() {
      @Override
      public void run() throws Exception {
        layout.getColumnOperations().normalizeSpanning();
      }
    });
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new GridBagLayout());",
        "    {",
        "      JButton button_0 = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 0;",
        "      add(button_0, gbc);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // clear()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link DimensionOperations#clear(int)}.<br>
   * Clear column "0".
   */
  public void test_clear_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new GridBagLayout());",
            "    {",
            "      JButton button_0 = new JButton();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 0;",
            "      gbc.gridy = 0;",
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
    final GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
    assertEquals(2, layout.getColumns().size());
    assertEquals(2, layout.getRows().size());
    // do clear
    ExecutionUtils.run(panel, new RunnableEx() {
      @Override
      public void run() throws Exception {
        layout.getColumnOperations().clear(0);
      }
    });
    assertEquals(2, layout.getColumns().size());
    assertEquals(2, layout.getRows().size());
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new GridBagLayout());",
        "    {",
        "      JButton button_1 = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.gridx = 1;",
        "      gbc.gridy = 1;",
        "      add(button_1, gbc);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link DimensionOperations#clear(int)}.<br>
   * Clear column "1", "button_1" should be deleted, but not "button_0", even if "button_0" spanned
   * on column "1".
   */
  public void test_clear_2() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new GridBagLayout());",
            "    {",
            "      JButton button_0 = new JButton();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 0;",
            "      gbc.gridy = 0;",
            "      gbc.gridwidth = 2;",
            "      add(button_0, gbc);",
            "    }",
            "    {",
            "      JButton button_1 = new JButton();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
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
    assertEquals(3, layout.getColumns().size());
    assertEquals(3, layout.getRows().size());
    // do clear
    ExecutionUtils.run(panel, new RunnableEx() {
      @Override
      public void run() throws Exception {
        layout.getColumnOperations().clear(1);
      }
    });
    assertEquals(3, layout.getColumns().size());
    assertEquals(3, layout.getRows().size());
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new GridBagLayout());",
        "    {",
        "      JButton button_0 = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 0;",
        "      gbc.gridwidth = 2;",
        "      add(button_0, gbc);",
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
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // split()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link DimensionOperations#split(int)}.
   */
  public void test_span_1() throws Exception {
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
            "      gbc.gridx = 0;",
            "      gbc.gridy = 0;",
            "      gbc.insets = new Insets(0, 0, 5, 5);",
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
    final GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
    // check columns
    {
      assertEquals(2, layout.getRows().size());
      List<ColumnInfo> columns = layout.getColumns();
      assertEquals(2, columns.size());
      assertEquals(1, columns.get(0).getSize());
      assertEquals(2, columns.get(1).getSize());
    }
    // do split
    ExecutionUtils.run(panel, new RunnableEx() {
      @Override
      public void run() throws Exception {
        layout.getColumnOperations().split(0);
      }
    });
    // check columns
    {
      assertEquals(2, layout.getRows().size());
      List<ColumnInfo> columns = layout.getColumns();
      assertEquals(3, columns.size());
      assertEquals(1, columns.get(0).getSize());
      assertEquals(1, columns.get(1).getSize());
      assertEquals(2, columns.get(2).getSize());
    }
    // check source
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    GridBagLayout layout = new GridBagLayout();",
        "    layout.columnWidths = new int[] {1, 1, 2, 3};",
        "    layout.rowHeights = new int[] {1, 2, 3};",
        "    layout.columnWeights = new double[] {0.1, 0.1, 0.2, Double.MIN_VALUE};",
        "    layout.rowWeights = new double[] {0.1, 0.2, Double.MIN_VALUE};",
        "    setLayout(layout);",
        "    {",
        "      JButton button_0 = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.gridwidth = 2;",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 0;",
        "      gbc.insets = new Insets(0, 0, 5, 5);",
        "      add(button_0, gbc);",
        "    }",
        "    {",
        "      JButton button_1 = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.gridx = 2;",
        "      gbc.gridy = 1;",
        "      add(button_1, gbc);",
        "    }",
        "  }",
        "}");
  }
}
