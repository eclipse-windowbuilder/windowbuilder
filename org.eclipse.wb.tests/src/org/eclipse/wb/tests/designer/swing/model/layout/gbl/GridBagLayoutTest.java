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

import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Interval;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.generation.statement.lazy.LazyStatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.variable.LazyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.description.LazyVariableDescription;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swing.Activator;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.component.menu.JPopupMenuInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.AbstractGridBagLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.GridBagConstraintsInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.GridBagLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.IPreferenceConstants;
import org.eclipse.wb.tests.designer.swing.SwingTestUtils;

import org.eclipse.jface.preference.IPreferenceStore;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Test for {@link GridBagLayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class GridBagLayoutTest extends AbstractGridBagLayoutTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void tearDown() throws Exception {
    {
      IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
      preferences.setToDefault(IPreferenceConstants.P_CHANGE_INSETS_FOR_GAPS);
      preferences.setToDefault(IPreferenceConstants.P_GAP_COLUMN);
      preferences.setToDefault(IPreferenceConstants.P_GAP_ROW);
    }
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
  // Images
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_images() throws Exception {
    assertNotNull(AbstractGridBagLayoutInfo.getImage("headers/h/menu/left.gif"));
    assertNotNull(AbstractGridBagLayoutInfo.getImageDescriptor("headers/h/menu/left.gif"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Simple test with {@link GridBagLayoutInfo} and single component with
   * {@link GridBagConstraintsInfo}.
   */
  public void test_fieldAssignment() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new GridBagLayout());",
            "    {",
            "      JButton button = new JButton('button');",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 1;",
            "      gbc.gridy = 2;",
            "      add(button, gbc);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // prepare models
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // check GridBagConstraintsInfo
    GridBagConstraintsInfo gbc = GridBagLayoutInfo.getConstraintsFor(button);
    assertRelatedNodes(gbc, new String[]{
        "new GridBagConstraints()",
        "gbc.gridx = 1",
        "gbc.gridy = 2",
        "add(button, gbc)"});
    assertVisible(gbc, false);
    // check execution - assignment to field
    {
      GridBagConstraints gbcObject = (GridBagConstraints) gbc.getObject();
      assertEquals(1, gbcObject.gridx);
      assertEquals(2, gbcObject.gridy);
    }
  }

  /**
   * Test for support of "parent2/child2" flags for {@link ParameterDescription}.
   */
  public void test_extraParentChild() throws Exception {
    setFileContentSrc(
        "test/AFrame.java",
        getTestSource(
            "public class AFrame extends JFrame {",
            "  protected void addGB(Container parent, Component child, int x, int y) {",
            "    GridBagConstraints constraints = new GridBagConstraints();",
            "    constraints.gridx = x;",
            "    constraints.gridy = y;",
            "    parent.add(child, constraints);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/AFrame.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='addGB'>",
            "      <parameter type='java.awt.Container' parent2='true'/>",
            "      <parameter type='java.awt.Component' child2='true'/>",
            "      <parameter type='int'/>",
            "      <parameter type='int'/>",
            "    </method>",
            "  </methods>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo frame =
        parseContainer(
            "public class Test extends AFrame {",
            "  public Test() {",
            "    getContentPane().setLayout(new GridBagLayout());",
            "    addGB(getContentPane(), new JButton('1 x 1'), 1, 1);",
            "    addGB(getContentPane(), new JButton('2 x 2'), 2, 2);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    {
      ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
      GridBagLayout gblObject = (GridBagLayout) contentPane.getContainer().getLayout();
      // check children of contentPane
      assertEquals(2, contentPane.getChildrenComponents().size());
      {
        Container container = contentPane.getContainer();
        Component[] components = container.getComponents();
        assertEquals(2, components.length);
      }
      // check "button 1 x 1"
      {
        ComponentInfo button = contentPane.getChildrenComponents().get(0);
        GridBagConstraints gbcObject = gblObject.getConstraints(button.getComponent());
        assertEquals(1, gbcObject.gridx);
        assertEquals(1, gbcObject.gridy);
      }
      // check "button 2 x 2"
      {
        ComponentInfo button = contentPane.getChildrenComponents().get(1);
        GridBagConstraints gbcObject = gblObject.getConstraints(button.getComponent());
        assertEquals(2, gbcObject.gridx);
        assertEquals(2, gbcObject.gridy);
      }
    }
  }

  /**
   * {@link JComponent#getBaseline(int, int)} does not like when size of component is zero. Such
   * zero size happens because we try to {@link Container#doLayout()} before applying top bounds.
   * So, we should set some reasonable size for {@link Container} and hope that it will be enough to
   * prevent zero size for components.
   */
  public void test_ComboBox_andBaseline() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout layout = new GridBagLayout();",
            "    layout.rowWeights = new double[]{1.0, Double.MIN_VALUE};",
            "    setLayout(layout);",
            "    {",
            "      JComboBox combo = new JComboBox();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.anchor = GridBagConstraints.BASELINE;",
            "      add(combo, gbc);",
            "    }",
            "  }",
            "}");
    refresh();
    assertNoErrors(panel);
  }

  /**
   * Same as {@link #test_ComboBox_andBaseline()} but test also that size of {@link JPanel} is same
   * as set using source.
   */
  public void test_keepSize() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    setSize(600, 250);",
            "    GridBagLayout layout = new GridBagLayout();",
            "    setLayout(layout);",
            "  }",
            "}");
    refresh();
    // verify that size of top level JPanel was not damaged
    assertEquals(new Dimension(600, 250), panel.getBounds().getSize());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Grid
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link IGridInfo} implementation.
   */
  public void test_grid() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout layout = new GridBagLayout();",
            "    setLayout(layout);",
            "    layout.columnWidths = new int[]{0, 0, 0, 0};",
            "    layout.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};",
            "    layout.rowHeights = new int[]{0, 0, 0, 0};",
            "    layout.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};",
            "    {",
            "      JButton button = new JButton('button 0 0');",
            "      add(button, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,",
            "          GridBagConstraints.CENTER, GridBagConstraints.BOTH,",
            "          new Insets(0, 0, 5, 5), 0, 0));",
            "    }",
            "    {",
            "      JButton button = new JButton('button 1 0 2 1');",
            "      add(button, new GridBagConstraints(1, 0, 2, 1, 0.0, 0.0,",
            "          GridBagConstraints.CENTER, GridBagConstraints.BOTH,",
            "          new Insets(0, 0, 5, 5), 0, 0));",
            "    }",
            "    {",
            "      JButton button = new JButton('button 1 1');",
            "      add(button, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,",
            "          GridBagConstraints.CENTER, GridBagConstraints.BOTH,",
            "          new Insets(0, 0, 5, 5), 0, 0));",
            "    }",
            "    {",
            "      JButton button = new JButton('button 1 2');",
            "      add(button, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,",
            "          GridBagConstraints.CENTER, GridBagConstraints.BOTH,",
            "          new Insets(0, 0, 5, 15), 0, 0));",
            "    }",
            "    {",
            "      JButton button = new JButton('button 2 2');",
            "      add(button, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,",
            "          GridBagConstraints.CENTER, GridBagConstraints.BOTH,",
            "          new Insets(0, 0, 5, 5), 0, 0));",
            "    }",
            "  }",
            "}");
    GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
    // prepare components
    ComponentInfo[] components;
    {
      List<ComponentInfo> childrenComponents = panel.getChildrenComponents();
      components = childrenComponents.toArray(new ComponentInfo[childrenComponents.size()]);
      assertEquals(5, components.length);
    }
    // prepare grid
    panel.refresh();
    IGridInfo gridInfo = layout.getGridInfo();
    // column/row count
    assertEquals(3, gridInfo.getColumnCount());
    assertEquals(3, gridInfo.getRowCount());
    // intervals
    {
      // columns
      {
        Interval[] intervals = gridInfo.getColumnIntervals();
        assertEquals(3, intervals.length);
        assertTrue(intervals[0].length > 20);
        assertTrue(intervals[1].length > 20);
        assertTrue(intervals[2].length > 20);
      }
      // rows
      {
        Interval[] intervals = gridInfo.getRowIntervals();
        assertEquals(3, intervals.length);
        assertTrue(intervals[0].length > 20);
        assertTrue(intervals[1].length > 20);
        assertTrue(intervals[2].length > 20);
      }
    }
    // cells
    {
      assertEquals(new Rectangle(0, 0, 1, 1), gridInfo.getComponentCells(components[0]));
      assertEquals(new Rectangle(1, 0, 2, 1), gridInfo.getComponentCells(components[1]));
      assertEquals(new Rectangle(1, 1, 1, 1), gridInfo.getComponentCells(components[2]));
      assertEquals(new Rectangle(1, 2, 1, 1), gridInfo.getComponentCells(components[3]));
      assertEquals(new Rectangle(2, 2, 1, 1), gridInfo.getComponentCells(components[4]));
      // just ask cells rectangle without check
      gridInfo.getCellsRectangle(new Rectangle(0, 0, 1, 1));
    }
    // insets
    assertEquals(new Insets(0, 0, 0, 0), gridInfo.getInsets());
    // default sizes
    {
      assertEquals(25, gridInfo.getVirtualColumnSize());
      assertEquals(5, gridInfo.getVirtualColumnGap());
      assertEquals(25, gridInfo.getVirtualRowSize());
      assertEquals(5, gridInfo.getVirtualRowGap());
    }
    // occupied
    {
      assertSame(components[0], gridInfo.getOccupied(0, 0));
      assertSame(components[1], gridInfo.getOccupied(1, 0));
      assertSame(components[1], gridInfo.getOccupied(2, 0));
      assertNull(gridInfo.getOccupied(0, 1));
      assertSame(components[2], gridInfo.getOccupied(1, 1));
    }
  }

  /**
   * Test for {@link IGridInfo} when container has no components, so no intervals.
   */
  public void test_grid_noComponents() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout layout = new GridBagLayout();",
            "    setLayout(layout);",
            "  }",
            "}");
    GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
    // prepare grid
    panel.refresh();
    IGridInfo gridInfo = layout.getGridInfo();
    // column/row count
    assertEquals(0, gridInfo.getColumnCount());
    assertEquals(0, gridInfo.getRowCount());
  }

  /**
   * Test for case when one of the components is spanned to the filler column. This was not expected
   * before, but seems legitimate and some users do this.
   */
  public void test_grid_spannedColumn_includeFiller() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout layout = new GridBagLayout();",
            "    layout.columnWidths = new int[]{0, 0};",
            "    layout.columnWeights = new double[]{0.0, Double.MIN_VALUE};",
            "    setLayout(layout);",
            "    {",
            "      JButton button = new JButton();",
            "      GridBagConstraints constraints = new GridBagConstraints();",
            "      constraints.gridx = 0;",
            "      constraints.gridwidth = 2;",
            "      add(button, constraints);",
            "    }",
            "  }",
            "}");
    refresh();
    GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
    ComponentInfo button = getJavaInfoByName("button");
    // prepare grid
    IGridInfo gridInfo = layout.getGridInfo();
    // validate grid
    assertEquals(2, gridInfo.getColumnCount());
    Rectangle cells = gridInfo.getComponentCells(button);
    Rectangle cellsRectangle = gridInfo.getCellsRectangle(cells);
    assertEquals(new Rectangle(0, 0, 2, 1), cells);
    assertThat(cellsRectangle.x).isEqualTo(0);
    assertThat(cellsRectangle.width).isGreaterThan(400);
  }

  /**
   * Test for case when one of the components is spanned to the filler row. This was not expected
   * before, but seems legitimate and some users do this.
   */
  public void test_grid_spannedRow_includeFiller() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout layout = new GridBagLayout();",
            "    layout.rowHeights = new int[]{0, 0};",
            "    layout.rowWeights = new double[]{0.0, Double.MIN_VALUE};",
            "    setLayout(layout);",
            "    {",
            "      JButton button = new JButton();",
            "      GridBagConstraints constraints = new GridBagConstraints();",
            "      constraints.gridy = 0;",
            "      constraints.gridheight = 2;",
            "      add(button, constraints);",
            "    }",
            "  }",
            "}");
    refresh();
    GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
    ComponentInfo button = getJavaInfoByName("button");
    // prepare grid
    IGridInfo gridInfo = layout.getGridInfo();
    // validate grid
    assertEquals(2, gridInfo.getRowCount());
    Rectangle cells = gridInfo.getComponentCells(button);
    Rectangle cellsRectangle = gridInfo.getCellsRectangle(cells);
    assertEquals(new Rectangle(0, 0, 1, 2), cells);
    assertThat(cellsRectangle.y).isEqualTo(0);
    assertThat(cellsRectangle.height).isGreaterThan(250);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Empty column
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link IGridInfo} when one column has no components.<br>
   * However it should still have not zero size, so that we could drop component into it.
   */
  public void test_grid_emptyColumn() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout layout = new GridBagLayout();",
            "    setLayout(layout);",
            "    {",
            "      JButton button = new JButton('button 1 0');",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 1;",
            "      gbc.gridy = 0;",
            "      add(button, gbc);",
            "    }",
            "  }",
            "}");
    check_grid_emptyColumn(panel);
  }

  /**
   * Test for {@link IGridInfo} when one column has no components.<br>
   * However it should still have not zero size, so that we could drop component into it.<br>
   * Here we test that it works for "contentPane" on {@link JFrame}.
   */
  public void test_grid_emptyColumn_JFrame() throws Exception {
    ContainerInfo frame =
        parseContainer(
            "class Test extends JFrame {",
            "  public Test() {",
            "    GridBagLayout layout = new GridBagLayout();",
            "    getContentPane().setLayout(layout);",
            "    {",
            "      JButton button = new JButton('button 1 0');",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 1;",
            "      gbc.gridy = 0;",
            "      getContentPane().add(button, gbc);",
            "    }",
            "  }",
            "}");
    ContainerInfo panel = (ContainerInfo) frame.getChildrenComponents().get(0);
    check_grid_emptyColumn(panel);
  }

  /**
   * Test for {@link IGridInfo} when one column has no components.<br>
   * However it should still have not zero size, so that we could drop component into it.<br>
   * Here we have "columnWidths" field not <code>null</code>, but with not enough elements.
   */
  public void test_grid_emptyColumn2() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout layout = new GridBagLayout();",
            "    layout.columnWidths = new int[]{};",
            "    setLayout(layout);",
            "    {",
            "      JButton button = new JButton('button 1 0');",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 1;",
            "      gbc.gridy = 0;",
            "      add(button, gbc);",
            "    }",
            "  }",
            "}");
    check_grid_emptyColumn(panel);
  }

  /**
   * Test for {@link IGridInfo} when one column has no components.<br>
   * However it should still have not zero size, so that we could drop component into it.<br>
   * Here we have "columnWeights" with last surrogate column.
   */
  public void test_grid_emptyColumn3() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout layout = new GridBagLayout();",
            "    layout.columnWidths = new int[]{0, 0, 0};",
            "    layout.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};",
            "    setLayout(layout);",
            "    {",
            "      JButton button = new JButton('button 1 0');",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 1;",
            "      gbc.gridy = 0;",
            "      add(button, gbc);",
            "    }",
            "  }",
            "}");
    check_grid_emptyColumn(panel);
  }

  /**
   * Test for {@link IGridInfo} when one column has no components.<br>
   * However it should still have not zero size, so that we could drop component into it.
   * <p>
   * Column "0" has component, but it is snapped on two columns, so "0" is too small.
   */
  public void test_grid_emptyColumn_spanned() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout layout = new GridBagLayout();",
            "    setLayout(layout);",
            "    {",
            "      JButton button = new JButton('button 0 0 2 1');",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 0;",
            "      gbc.gridwidth = 2;",
            "      gbc.gridy = 0;",
            "      add(button, gbc);",
            "    }",
            "  }",
            "}");
    check_grid_emptyColumn(panel);
  }

  private void check_grid_emptyColumn(ContainerInfo panel) throws Exception {
    GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
    // prepare grid
    panel.getRoot().refresh();
    IGridInfo gridInfo = layout.getGridInfo();
    // check column intervals
    {
      Interval[] intervals = gridInfo.getColumnIntervals();
      assertEquals(2, intervals.length);
      // interval with component is fairly wide
      assertTrue(intervals[1].length > 50);
      // interval without components is as wide, as "virtual"
      assertEquals(gridInfo.getVirtualColumnSize(), intervals[0].length);
      // there is "virtual" gap between two intervals, even if one is "virtual"
      assertEquals(gridInfo.getVirtualColumnGap(), intervals[1].begin - intervals[0].end());
    }
  }

  /**
   * Test for {@link IGridInfo} when one column has no components.<br>
   * However it should still have not zero size, so that we could drop component into it.<br>
   * Here we have "columnWidths" that is big enough, so no need to drop it to smaller (but bigger
   * than zero) value.
   */
  public void test_grid_emptyColumn4() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout layout = new GridBagLayout();",
            "    layout.columnWidths = new int[]{150};",
            "    layout.columnWeights = new double[]{0.0};",
            "    setLayout(layout);",
            "  }",
            "}");
    panel.refresh();
    GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
    // check grid
    IGridInfo gridInfo = layout.getGridInfo();
    Interval[] intervals = gridInfo.getColumnIntervals();
    assertEquals(1, intervals.length);
    assertEquals(150 - 5, intervals[0].length);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Empty row
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link IGridInfo} when one row has no components.<br>
   * However it should still have not zero size, so that we could drop component into it.
   */
  public void test_grid_emptyRow() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout layout = new GridBagLayout();",
            "    setLayout(layout);",
            "    {",
            "      JButton button = new JButton('button 0 1');",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 0;",
            "      gbc.gridy = 1;",
            "      add(button, gbc);",
            "    }",
            "  }",
            "}");
    check_grid_emptyRow(panel);
  }

  /**
   * Test for {@link IGridInfo} when one row has no components.<br>
   * However it should still have not zero size, so that we could drop component into it.
   * <p>
   * Row "0" has component, but it is spanned two rows, so "0" has zero size.
   */
  public void test_grid_emptyRow_spanned() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout layout = new GridBagLayout();",
            "    setLayout(layout);",
            "    {",
            "      JButton button = new JButton('button 0 1');",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 0;",
            "      gbc.gridy = 0;",
            "      gbc.gridheight = 2;",
            "      add(button, gbc);",
            "    }",
            "  }",
            "}");
    check_grid_emptyRow(panel);
  }

  private void check_grid_emptyRow(ContainerInfo panel) throws Exception {
    panel.refresh();
    GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
    // prepare grid
    IGridInfo gridInfo = layout.getGridInfo();
    // check column intervals
    {
      Interval[] intervals = gridInfo.getRowIntervals();
      assertEquals(2, intervals.length);
      // interval with component is fairly tall
      assertTrue(intervals[1].length > 20);
      // interval without components is as tall, as "virtual"
      assertEquals(gridInfo.getVirtualRowSize(), intervals[0].length);
      // there is "virtual" gap between two intervals, even if one is "virtual"
      assertEquals(gridInfo.getVirtualRowGap(), intervals[1].begin - intervals[0].end());
    }
  }

  /**
   * Test for case when columnWeights/rowWeights have less elements than required by dimensions.
   */
  public void test_grid_lessWeightThanDimensions() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout layout = new GridBagLayout();",
            "    layout.columnWidths = new int[]{200};",
            "    layout.columnWeights = new double[]{0.0};",
            "    layout.rowHeights = new int[]{30};",
            "    layout.rowWeights = new double[]{0.0};",
            "    setLayout(layout);",
            "    {",
            "      JButton button = new JButton();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 1;",
            "      gbc.gridy = 1;",
            "      add(button, gbc);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
    // check grid (it should not crash)
    IGridInfo gridInfo = layout.getGridInfo();
    {
      Interval[] intervals = gridInfo.getColumnIntervals();
      assertThat(intervals).hasSize(2);
    }
    {
      Interval[] intervals = gridInfo.getRowIntervals();
      assertThat(intervals).hasSize(2);
    }
  }

  /**
   * We should ensure that empty row has some reasonable size. However this may conflict with
   * "topBounds.path" feature.
   */
  public void test_grid_emptyRow_whenPack() throws Exception {
    setFileContentSrc(
        "test/MyFrame.java",
        getTestSource(
            "public class MyFrame extends JFrame {",
            "  protected void finishInit() {",
            "    pack();",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyFrame.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='finishInit'/>",
            "  </methods>",
            "  <parameters>",
            "    <parameter name='topBounds.pack'>true</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    //
    ContainerInfo frame =
        parseContainer(
            "class Test extends MyFrame {",
            "  public Test() {",
            "    GridBagLayout layout = new GridBagLayout();",
            "    getContentPane().setLayout(layout);",
            "    {",
            "      JButton button = new JButton('button 0 1');",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 0;",
            "      gbc.gridy = 1;",
            "      getContentPane().add(button, gbc);",
            "    }",
            "    finishInit();",
            "  }",
            "}");
    refresh();
    ComponentInfo button = getJavaInfoByName("button");
    // "button" should be fully visible
    Rectangle frameBounds = frame.getAbsoluteBounds();
    Rectangle buttonBounds = button.getAbsoluteBounds();
    assertTrue(frameBounds.contains(buttonBounds.getBottomRight()));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Set
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that replacing {@link GridBagLayoutInfo} with different layout manager does not cause any
   * problem.
   */
  public void test_replaceGBL() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout layout = new GridBagLayout();",
            "    setLayout(layout);",
            "    {",
            "      JButton button = new JButton();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 1;",
            "      gbc.gridy = 1;",
            "      add(button, gbc);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/setLayout(layout)/ /add(button, gbc)/}",
        "  {new: java.awt.GridBagLayout} {local-unique: layout} {/new GridBagLayout()/ /setLayout(layout)/}",
        "  {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /add(button, gbc)/}",
        "    {new: java.awt.GridBagConstraints} {local-unique: gbc} {/new GridBagConstraints()/ /gbc.gridx = 1/ /gbc.gridy = 1/ /add(button, gbc)/}");
    panel.refresh();
    // replace with java.awt.FlowLayout
    LayoutInfo flowLayout = createJavaInfo("java.awt.FlowLayout");
    panel.setLayout(flowLayout);
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));",
        "    {",
        "      JButton button = new JButton();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(button)/ /setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5))/}",
        "  {new: java.awt.FlowLayout} {empty} {/setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5))/}",
        "  {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /add(button)/}");
  }

  /**
   * Test that replacing {@link GridBagLayoutInfo} with different layout manager does not cause any
   * problem.
   */
  public void test_replaceGBL_whenVirtualConstraints() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout layout = new GridBagLayout();",
            "    setLayout(layout);",
            "    {",
            "      JButton button = new JButton();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/setLayout(layout)/ /add(button)/}",
        "  {new: java.awt.GridBagLayout} {local-unique: layout} {/new GridBagLayout()/ /setLayout(layout)/}",
        "  {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /add(button)/}",
        "    {virtual-GBL-constraints} {virtual-GBL-constraints} {}");
    panel.refresh();
    // replace with java.awt.FlowLayout
    LayoutInfo flowLayout = createJavaInfo("java.awt.FlowLayout");
    panel.setLayout(flowLayout);
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));",
        "    {",
        "      JButton button = new JButton();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(button)/ /setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5))/}",
        "  {new: java.awt.FlowLayout} {empty} {/setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5))/}",
        "  {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /add(button)/}");
  }

  /**
   * When we set {@link GridBagLayoutInfo}, it should be created in "normal grid" mode, i.e. with
   * "end-of-grid" fillers.
   */
  public void test_setLayout() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    // set layout
    GridBagLayoutInfo gbl = createJavaInfo("java.awt.GridBagLayout");
    panel.setLayout(gbl);
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    GridBagLayout gridBagLayout = new GridBagLayout();",
        "    gridBagLayout.columnWidths = new int[]{0};",
        "    gridBagLayout.rowHeights = new int[]{0};",
        "    gridBagLayout.columnWeights = new double[]{Double.MIN_VALUE};",
        "    gridBagLayout.rowWeights = new double[]{Double.MIN_VALUE};",
        "    setLayout(gridBagLayout);",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Add component into empty {@link GridBagLayoutInfo}.
   */
  public void test_CREATE_empty_0() throws Exception {
    final ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new GridBagLayout());",
            "  }",
            "}");
    panel.refresh();
    // add new component
    Activator.getDefault().getPreferenceStore().setValue(IPreferenceConstants.P_GBC_LONG, true);
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
        ComponentInfo button = createJButton();
        layout.command_CREATE(button, 0, false, 0, false);
      }
    });
    // check result
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new GridBagLayout());",
        "    {",
        "      JButton button = new JButton();",
        "      add(button, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));",
        "    }",
        "  }",
        "}");
  }

  /**
   * Add component into empty {@link GridBagLayoutInfo} (but with end-of-grid fillers), with
   * column/row size/weight.
   */
  public void test_CREATE_empty_1() throws Exception {
    final ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout layout = new GridBagLayout();",
            "    layout.columnWidths = new int[] {0};",
            "    layout.rowHeights = new int[] {0};",
            "    layout.columnWeights = new double[] {Double.MIN_VALUE};",
            "    layout.rowWeights = new double[] {Double.MIN_VALUE};",
            "    setLayout(layout);",
            "  }",
            "}");
    panel.refresh();
    // add new component
    Activator.getDefault().getPreferenceStore().setValue(IPreferenceConstants.P_GBC_LONG, true);
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
        ComponentInfo button = createJButton();
        layout.command_CREATE(button, 0, false, 0, false);
      }
    });
    // check result
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
        "      JButton button = new JButton();",
        "      add(button, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));",
        "    }",
        "  }",
        "}");
  }

  /**
   * Add component into empty {@link GridBagLayoutInfo}, with column/row size/weight.<br>
   * New columns/rows should be appended.
   */
  public void test_CREATE_empty_2() throws Exception {
    final ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout layout = new GridBagLayout();",
            "    layout.columnWidths = new int[] {0};",
            "    layout.rowHeights = new int[] {0};",
            "    layout.columnWeights = new double[] {Double.MIN_VALUE};",
            "    layout.rowWeights = new double[] {Double.MIN_VALUE};",
            "    setLayout(layout);",
            "  }",
            "}");
    panel.refresh();
    // add new component
    Activator.getDefault().getPreferenceStore().setValue(IPreferenceConstants.P_GBC_LONG, true);
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
        ComponentInfo button = createJButton();
        layout.command_CREATE(button, 2, false, 1, false);
      }
    });
    // check result
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    GridBagLayout layout = new GridBagLayout();",
        "    layout.columnWidths = new int[] {0, 0, 0, 0};",
        "    layout.rowHeights = new int[] {0, 0, 0};",
        "    layout.columnWeights = new double[] {0.0, 0.0, 0.0, Double.MIN_VALUE};",
        "    layout.rowWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};",
        "    setLayout(layout);",
        "    {",
        "      JButton button = new JButton();",
        "      add(button, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));",
        "    }",
        "  }",
        "}");
  }

  /**
   * Insert new component into new column/row.<br>
   * Existing component should be moved.
   */
  public void test_CREATE_insertColumn_insertRow() throws Exception {
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
            "    {",
            "      JButton button = new JButton('button 0 0');",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 0;",
            "      gbc.gridy = 0;",
            "      add(button, gbc);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // add new component
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
        ComponentInfo button = createJButton();
        layout.command_CREATE(button, 0, true, 0, true);
      }
    });
    // check result
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    GridBagLayout layout = new GridBagLayout();",
        "    layout.columnWidths = new int[] {0, 0, 0};",
        "    layout.rowHeights = new int[] {0, 0, 0};",
        "    layout.columnWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};",
        "    layout.rowWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};",
        "    setLayout(layout);",
        "    {",
        "      JButton button = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.insets = new Insets(0, 0, 5, 5);",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 0;",
        "      add(button, gbc);",
        "    }",
        "    {",
        "      JButton button = new JButton('button 0 0');",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.gridx = 1;",
        "      gbc.gridy = 1;",
        "      add(button, gbc);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Append new component into new column/row.
   */
  public void test_CREATE_appendColumn_appendRow() throws Exception {
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
            "    {",
            "      JButton button = new JButton('button 0 0');",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 0;",
            "      gbc.gridy = 0;",
            "      add(button, gbc);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // add new component
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
        ComponentInfo button = createJButton();
        layout.command_CREATE(button, 1, false, 1, false);
      }
    });
    // check result
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    GridBagLayout layout = new GridBagLayout();",
        "    layout.columnWidths = new int[] {0, 0, 0};",
        "    layout.rowHeights = new int[] {0, 0, 0};",
        "    layout.columnWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};",
        "    layout.rowWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};",
        "    setLayout(layout);",
        "    {",
        "      JButton button = new JButton('button 0 0');",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.insets = new Insets(0, 0, 5, 5);",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 0;",
        "      add(button, gbc);",
        "    }",
        "    {",
        "      JButton button = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.gridx = 1;",
        "      gbc.gridy = 1;",
        "      add(button, gbc);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Append new component into new column.
   */
  public void test_CREATE_appendColumn() throws Exception {
    final ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout layout = new GridBagLayout();",
            "    setLayout(layout);",
            "    {",
            "      JButton button = new JButton('button 0 0');",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 0;",
            "      gbc.gridy = 0;",
            "      add(button, gbc);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // add new component
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
        ComponentInfo button = createJButton();
        layout.command_CREATE(button, 1, false, 0, false);
      }
    });
    // check result
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    GridBagLayout layout = new GridBagLayout();",
        "    setLayout(layout);",
        "    {",
        "      JButton button = new JButton('button 0 0');",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.insets = new Insets(0, 0, 0, 5);",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 0;",
        "      add(button, gbc);",
        "    }",
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

  /**
   * Insert new component into new column/row.<br>
   * Existing component (spanned on two columns) should be expanded.
   */
  public void test_CREATE_insertColumn_appendRow_expandSpanColumn() throws Exception {
    final ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout layout = new GridBagLayout();",
            "    setLayout(layout);",
            "    {",
            "      JButton button = new JButton('button 0 0 2 1');",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 0;",
            "      gbc.gridy = 0;",
            "      gbc.gridwidth = 2;",
            "      add(button, gbc);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // add new component
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
        ComponentInfo button = createJButton();
        layout.command_CREATE(button, 1, true, 1, false);
      }
    });
    // check result
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    GridBagLayout layout = new GridBagLayout();",
        "    setLayout(layout);",
        "    {",
        "      JButton button = new JButton('button 0 0 2 1');",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.insets = new Insets(0, 0, 5, 0);",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 0;",
        "      gbc.gridwidth = 3;",
        "      add(button, gbc);",
        "    }",
        "    {",
        "      JButton button = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.insets = new Insets(0, 0, 0, 5);",
        "      gbc.gridx = 1;",
        "      gbc.gridy = 1;",
        "      add(button, gbc);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Insert new component into new column/row.<br>
   * Existing component (spanned on two rows) should be expanded.
   */
  public void test_CREATE_appendColumn_insertRow_expandSpanRow() throws Exception {
    final ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout layout = new GridBagLayout();",
            "    setLayout(layout);",
            "    {",
            "      JButton button = new JButton('button 0 0 1 2');",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 0;",
            "      gbc.gridy = 0;",
            "      gbc.gridheight = 2;",
            "      add(button, gbc);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // add new component
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
        ComponentInfo button = createJButton();
        layout.command_CREATE(button, 1, false, 1, true);
      }
    });
    // check result
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    GridBagLayout layout = new GridBagLayout();",
        "    setLayout(layout);",
        "    {",
        "      JButton button = new JButton('button 0 0 1 2');",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.insets = new Insets(0, 0, 0, 5);",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 0;",
        "      gbc.gridheight = 3;",
        "      add(button, gbc);",
        "    }",
        "    {",
        "      JButton button = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.insets = new Insets(0, 0, 5, 0);",
        "      gbc.gridx = 1;",
        "      gbc.gridy = 1;",
        "      add(button, gbc);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test that column gap can be configured.
   */
  public void test_CREATE_appendColumn_differentGap() throws Exception {
    final ContainerInfo panel =
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
            "      add(button, gbc);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // add new component
    {
      IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
      preferences.setValue(IPreferenceConstants.P_GAP_COLUMN, 10);
    }
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
        ComponentInfo button = createJButton();
        layout.command_CREATE(button, 1, false, 0, false);
      }
    });
    // check result
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    GridBagLayout layout = new GridBagLayout();",
        "    setLayout(layout);",
        "    {",
        "      JButton button = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.insets = new Insets(0, 0, 0, 10);",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 0;",
        "      add(button, gbc);",
        "    }",
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

  /**
   * Test that row gap can be configured.
   */
  public void test_CREATE_appendRow_differentGap() throws Exception {
    final ContainerInfo panel =
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
            "      add(button, gbc);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // add new component
    {
      IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
      preferences.setValue(IPreferenceConstants.P_GAP_ROW, 10);
    }
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
        ComponentInfo button = createJButton();
        layout.command_CREATE(button, 0, false, 1, false);
      }
    });
    // check result
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    GridBagLayout layout = new GridBagLayout();",
        "    setLayout(layout);",
        "    {",
        "      JButton button = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.insets = new Insets(0, 0, 10, 0);",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 0;",
        "      add(button, gbc);",
        "    }",
        "    {",
        "      JButton button = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 1;",
        "      add(button, gbc);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test that we can disable changing "insets" for generating gaps.
   */
  public void test_CREATE_appendRow_dontChangeInsets_soNoGap() throws Exception {
    final ContainerInfo panel =
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
            "      add(button, gbc);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // add new component
    {
      IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
      preferences.setValue(IPreferenceConstants.P_CHANGE_INSETS_FOR_GAPS, false);
    }
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
        ComponentInfo button = createJButton();
        layout.command_CREATE(button, 0, false, 1, false);
      }
    });
    // check result
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
        "      add(button, gbc);",
        "    }",
        "    {",
        "      JButton button = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 1;",
        "      add(button, gbc);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE_last
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link GridBagLayoutInfo#command_CREATE_last(ComponentInfo)}.
   */
  public void test_CREATE_last_hasEmptyCell() throws Exception {
    final ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new GridBagLayout());",
            "    {",
            "      JButton button_1 = new JButton();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 1;",
            "      gbc.gridy = 0;",
            "      add(button_1, gbc);",
            "    }",
            "    {",
            "      JButton button_2 = new JButton();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 0;",
            "      gbc.gridy = 1;",
            "      add(button_2, gbc);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // add new component
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
        ComponentInfo button = createJButton();
        layout.command_CREATE_last(button);
      }
    });
    // check result
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new GridBagLayout());",
        "    {",
        "      JButton button_1 = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.insets = new Insets(0, 0, 5, 0);",
        "      gbc.gridx = 1;",
        "      gbc.gridy = 0;",
        "      add(button_1, gbc);",
        "    }",
        "    {",
        "      JButton button_2 = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.insets = new Insets(0, 0, 0, 5);",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 1;",
        "      add(button_2, gbc);",
        "    }",
        "    {",
        "      JButton button = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.gridx = 1;",
        "      gbc.gridy = 1;",
        "      add(button, gbc);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link GridBagLayoutInfo#command_CREATE_last(ComponentInfo)}.
   */
  public void test_CREATE_last_noEmptyCell() throws Exception {
    final ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new GridBagLayout());",
            "    {",
            "      JButton button_1 = new JButton();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 0;",
            "      gbc.gridy = 0;",
            "      add(button_1, gbc);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // add new component
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
        ComponentInfo button = createJButton();
        layout.command_CREATE_last(button);
      }
    });
    // check result
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new GridBagLayout());",
        "    {",
        "      JButton button_1 = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.insets = new Insets(0, 0, 5, 0);",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 0;",
        "      add(button_1, gbc);",
        "    }",
        "    {",
        "      JButton button = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 1;",
        "      add(button, gbc);",
        "    }",
        "  }",
        "}");
  }

  /**
   * We should handle correctly case when "rowHeights" has not enough elements.
   */
  public void test_CREATE_tooLittleElements_rowHeight() throws Exception {
    final ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout layout = new GridBagLayout();",
            "    layout.rowHeights = new int[] {};",
            "    layout.rowWeights = new double[] {};",
            "    setLayout(layout);",
            "    {",
            "      JButton button = new JButton('existing');",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 0;",
            "      gbc.gridy = 0;",
            "      add(button, gbc);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // add new component
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
        ComponentInfo button = createJButton();
        layout.command_CREATE(button, 0, false, 1, false);
      }
    });
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    GridBagLayout layout = new GridBagLayout();",
        "    layout.rowHeights = new int[] {0, 0};",
        "    layout.rowWeights = new double[] {0.0, 0.0};",
        "    setLayout(layout);",
        "    {",
        "      JButton button = new JButton('existing');",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.insets = new Insets(0, 0, 5, 0);",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 0;",
        "      add(button, gbc);",
        "    }",
        "    {",
        "      JButton button = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 1;",
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
   * When we move, we update insets.
   */
  public void test_MOVE_0() throws Exception {
    final ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout layout = new GridBagLayout();",
            "    layout.columnWidths = new int[] {0, 0, 0};",
            "    layout.rowHeights = new int[] {0, 0, 0};",
            "    layout.columnWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};",
            "    layout.rowWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};",
            "    setLayout(layout);",
            "    {",
            "      JButton button = new JButton('button 0 0');",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.insets = new Insets(0, 0, 5, 5);",
            "      gbc.gridx = 0;",
            "      gbc.gridy = 0;",
            "      add(button, gbc);",
            "    }",
            "    {",
            "      JButton button = new JButton('button 1 1');",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 1;",
            "      gbc.gridy = 1;",
            "      add(button, gbc);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // do move
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
        ComponentInfo button_00 = panel.getChildrenComponents().get(0);
        layout.command_MOVE(button_00, 1, false, 0, false);
      }
    });
    // check result
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    GridBagLayout layout = new GridBagLayout();",
        "    layout.columnWidths = new int[] {0, 0, 0};",
        "    layout.rowHeights = new int[] {0, 0, 0};",
        "    layout.columnWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};",
        "    layout.rowWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};",
        "    setLayout(layout);",
        "    {",
        "      JButton button = new JButton('button 0 0');",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.insets = new Insets(0, 0, 5, 0);",
        "      gbc.gridx = 1;",
        "      gbc.gridy = 0;",
        "      add(button, gbc);",
        "    }",
        "    {",
        "      JButton button = new JButton('button 1 1');",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.gridx = 1;",
        "      gbc.gridy = 1;",
        "      add(button, gbc);",
        "    }",
        "  }",
        "}");
  }

  /**
   * When we move we may reorder components.
   */
  public void test_MOVE_1() throws Exception {
    final ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout layout = new GridBagLayout();",
            "    layout.columnWidths = new int[] {0, 0, 0};",
            "    layout.rowHeights = new int[] {0, 0, 0};",
            "    layout.columnWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};",
            "    layout.rowWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};",
            "    setLayout(layout);",
            "    {",
            "      JButton button = new JButton('button 0 1');",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.insets = new Insets(0, 0, 0, 5);",
            "      gbc.gridx = 0;",
            "      gbc.gridy = 1;",
            "      add(button, gbc);",
            "    }",
            "    {",
            "      JButton button = new JButton('button 1 1');",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 1;",
            "      gbc.gridy = 1;",
            "      add(button, gbc);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // do move
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
        ComponentInfo button_11 = panel.getChildrenComponents().get(1);
        layout.command_MOVE(button_11, 0, false, 0, false);
      }
    });
    // check result
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    GridBagLayout layout = new GridBagLayout();",
        "    layout.columnWidths = new int[] {0, 0, 0};",
        "    layout.rowHeights = new int[] {0, 0, 0};",
        "    layout.columnWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};",
        "    layout.rowWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};",
        "    setLayout(layout);",
        "    {",
        "      JButton button = new JButton('button 1 1');",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.insets = new Insets(0, 0, 5, 5);",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 0;",
        "      add(button, gbc);",
        "    }",
        "    {",
        "      JButton button = new JButton('button 0 1');",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.insets = new Insets(0, 0, 0, 5);",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 1;",
        "      add(button, gbc);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for moving from inner {@link JPanel} to {@link GridBagLayout}, two times.
   */
  public void test_MOVE_2() throws Exception {
    final ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout layout = new GridBagLayout();",
            "    layout.columnWidths = new int[] {0, 0};",
            "    layout.rowHeights = new int[] {0, 0};",
            "    layout.columnWeights = new double[] {0.0, 1.0};",
            "    layout.rowWeights = new double[] {0.0, 1.0};",
            "    setLayout(layout);",
            "    {",
            "      JPanel innerPanel = new JPanel();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 1;",
            "      gbc.gridy = 1;",
            "      add(innerPanel, gbc);",
            "      {",
            "        JButton button = new JButton('button');",
            "        innerPanel.add(button);",
            "      }",
            "    }",
            "  }",
            "}");
    panel.refresh();
    final GridBagLayoutInfo gridLayout = (GridBagLayoutInfo) panel.getLayout();
    final ContainerInfo innerPanel = (ContainerInfo) panel.getChildrenComponents().get(0);
    final FlowLayoutInfo flowLayout = (FlowLayoutInfo) innerPanel.getLayout();
    final ComponentInfo button = innerPanel.getChildrenComponents().get(0);
    // move from "innerPanel"
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        gridLayout.command_MOVE(button, 0, false, 0, false);
      }
    });
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    GridBagLayout layout = new GridBagLayout();",
        "    layout.columnWidths = new int[] {0, 0};",
        "    layout.rowHeights = new int[] {0, 0};",
        "    layout.columnWeights = new double[] {0.0, 1.0};",
        "    layout.rowWeights = new double[] {0.0, 1.0};",
        "    setLayout(layout);",
        "    {",
        "      JButton button = new JButton('button');",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.insets = new Insets(0, 0, 5, 5);",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 0;",
        "      add(button, gbc);",
        "    }",
        "    {",
        "      JPanel innerPanel = new JPanel();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.gridx = 1;",
        "      gbc.gridy = 1;",
        "      add(innerPanel, gbc);",
        "    }",
        "  }",
        "}");
    // move to "innerPanel"
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        flowLayout.move(button, null);
      }
    });
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    GridBagLayout layout = new GridBagLayout();",
        "    layout.columnWidths = new int[] {0, 0};",
        "    layout.rowHeights = new int[] {0, 0};",
        "    layout.columnWeights = new double[] {0.0, 1.0};",
        "    layout.rowWeights = new double[] {0.0, 1.0};",
        "    setLayout(layout);",
        "    {",
        "      JPanel innerPanel = new JPanel();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.gridx = 1;",
        "      gbc.gridy = 1;",
        "      add(innerPanel, gbc);",
        "      {",
        "        JButton button = new JButton('button');",
        "        innerPanel.add(button);",
        "      }",
        "    }",
        "  }",
        "}");
    // move again from "innerPanel"
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        gridLayout.command_MOVE(button, 0, false, 0, false);
      }
    });
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    GridBagLayout layout = new GridBagLayout();",
        "    layout.columnWidths = new int[] {0, 0};",
        "    layout.rowHeights = new int[] {0, 0};",
        "    layout.columnWeights = new double[] {0.0, 1.0};",
        "    layout.rowWeights = new double[] {0.0, 1.0};",
        "    setLayout(layout);",
        "    {",
        "      JButton button = new JButton('button');",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.insets = new Insets(0, 0, 5, 5);",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 0;",
        "      add(button, gbc);",
        "    }",
        "    {",
        "      JPanel innerPanel = new JPanel();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.gridx = 1;",
        "      gbc.gridy = 1;",
        "      add(innerPanel, gbc);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for moving and "virtual" constraints.<br>
   * Move component below.
   */
  public void test_MOVE_4() throws Exception {
    final ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout layout = new GridBagLayout();",
            "    setLayout(layout);",
            "    {",
            "      JButton button_1 = new JButton('button 1');",
            "      add(button_1);",
            "    }",
            "    {",
            "      JButton button_2 = new JButton('button 2');",
            "      add(button_2);",
            "    }",
            "    {",
            "      JButton button_3 = new JButton('button 3');",
            "      add(button_3);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // do move
    Activator.getDefault().getPreferenceStore().setValue(IPreferenceConstants.P_GBC_LONG, true);
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
        ComponentInfo button_2 = panel.getChildrenComponents().get(2);
        layout.command_MOVE(button_2, 2, false, 1, false);
      }
    });
    // check result
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    GridBagLayout layout = new GridBagLayout();",
        "    setLayout(layout);",
        "    {",
        "      JButton button_1 = new JButton('button 1');",
        "      add(button_1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));",
        "    }",
        "    {",
        "      JButton button_2 = new JButton('button 2');",
        "      add(button_2, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));",
        "    }",
        "    {",
        "      JButton button_3 = new JButton('button 3');",
        "      add(button_3, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for moving and "virtual" constraints.<br>
   * Move component above.
   */
  public void test_MOVE_5() throws Exception {
    final ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout layout = new GridBagLayout();",
            "    setLayout(layout);",
            "    {",
            "      JButton button_1 = new JButton('button 1');",
            "      add(button_1);",
            "    }",
            "    {",
            "      JButton button_2 = new JButton('button 2');",
            "      add(button_2);",
            "    }",
            "    {",
            "      JButton button_3 = new JButton('button 3');",
            "      add(button_3);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // do move
    Activator.getDefault().getPreferenceStore().setValue(IPreferenceConstants.P_GBC_LONG, true);
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
        ComponentInfo button_2 = panel.getChildrenComponents().get(2);
        layout.command_MOVE(button_2, 0, false, 0, true);
      }
    });
    // check result
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    GridBagLayout layout = new GridBagLayout();",
        "    setLayout(layout);",
        "    {",
        "      JButton button_3 = new JButton('button 3');",
        "      add(button_3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));",
        "    }",
        "    {",
        "      JButton button_1 = new JButton('button 1');",
        "      add(button_1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));",
        "    }",
        "    {",
        "      JButton button_2 = new JButton('button 2');",
        "      add(button_2, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));",
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
   * Test that we can delete component from {@link GridBagLayoutInfo}.<br>
   * There are no minimum size arrays for columns/rows, so when we delete component, last empty
   * columns/rows are also deleted.
   */
  public void test_DELETE_1() throws Exception {
    final ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new GridBagLayout());",
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
    assertEquals(1, layout.getColumns().size());
    assertEquals(1, layout.getRows().size());
    // do delete
    panel.getChildrenComponents().get(0).delete();
    // check result
    assertEquals(0, layout.getColumns().size());
    assertEquals(0, layout.getRows().size());
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new GridBagLayout());",
        "  }",
        "}");
  }

  /**
   * Test that we can delete component from {@link GridBagLayoutInfo}.<br>
   * There is minimum size arrays for columns/rows, so we keep column/row even when they become
   * empty.
   */
  public void test_DELETE_2() throws Exception {
    final ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout layout = new GridBagLayout();",
            "    layout.columnWidths = new int[] {0};",
            "    layout.rowHeights = new int[] {0};",
            "    layout.columnWeights = new double[] {0.0};",
            "    layout.rowWeights = new double[] {0.0};",
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
    assertEquals(1, layout.getColumns().size());
    assertEquals(1, layout.getRows().size());
    // do delete
    panel.getChildrenComponents().get(0).delete();
    // check result
    assertEquals(1, layout.getColumns().size());
    assertEquals(1, layout.getRows().size());
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    GridBagLayout layout = new GridBagLayout();",
        "    layout.columnWidths = new int[] {0};",
        "    layout.rowHeights = new int[] {0};",
        "    layout.columnWeights = new double[] {0.0};",
        "    layout.rowWeights = new double[] {0.0};",
        "    setLayout(layout);",
        "  }",
        "}");
  }

  /**
   * When we delete empty last columns/rows we should also cut "weight" arrays.
   */
  public void test_DELETE_3() throws Exception {
    final ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout layout = new GridBagLayout();",
            "    layout.columnWeights = new double[] {1.0};",
            "    layout.rowWeights = new double[] {1.0};",
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
    assertEquals(1, layout.getColumns().size());
    assertEquals(1, layout.getRows().size());
    // do delete
    panel.getChildrenComponents().get(0).delete();
    // check result
    assertEquals(0, layout.getColumns().size());
    assertEquals(0, layout.getRows().size());
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    GridBagLayout layout = new GridBagLayout();",
        "    layout.columnWeights = new double[] {};",
        "    layout.rowWeights = new double[] {};",
        "    setLayout(layout);",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setCells()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link GridBagLayoutInfo#command_setCells(ComponentInfo, Rectangle)}.<br>
   * Span single component.
   */
  public void test_setCells_0() throws Exception {
    final ContainerInfo panel =
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
    // set cells
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
        ComponentInfo button_0 = panel.getChildrenComponents().get(0);
        layout.command_setCells(button_0, new Rectangle(0, 0, 2, 1));
      }
    });
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

  /**
   * Test for {@link GridBagLayoutInfo#command_setCells(ComponentInfo, Rectangle)}.<br>
   * Span component that causes move.
   */
  public void test_setCells_1() throws Exception {
    final ContainerInfo panel =
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
            "      gbc.gridy = 0;",
            "      gbc.insets = new Insets(0, 0, 5, 0);",
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
    // prepare layout
    final GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
    assertEquals(2, layout.getColumns().size());
    assertEquals(2, layout.getRows().size());
    // set cells
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        ComponentInfo button_0 = panel.getChildrenComponents().get(0);
        layout.command_setCells(button_0, new Rectangle(0, 1, 1, 1));
      }
    });
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new GridBagLayout());",
        "    {",
        "      JButton button_1 = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.gridx = 1;",
        "      gbc.gridy = 0;",
        "      gbc.insets = new Insets(0, 0, 5, 0);",
        "      add(button_1, gbc);",
        "    }",
        "    {",
        "      JButton button_0 = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 1;",
        "      gbc.insets = new Insets(0, 0, 0, 5);",
        "      add(button_0, gbc);",
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
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for copy/paste {@link JPanel} with {@link GridBagLayout} and children.
   */
  public void test_clipboard() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JPanel inner = new JPanel();",
            "      add(inner);",
            "      GridBagLayout layout = new GridBagLayout();",
            "      layout.columnWidths = new int[]{10, 20, 0};",
            "      layout.columnWeights = new double[]{1.0, 2.0, Double.MIN_VALUE};",
            "      layout.rowHeights = new int[]{10, 20, 30, 0};",
            "      layout.rowWeights = new double[]{1.0, 2.0, 3.0, Double.MIN_VALUE};",
            "      inner.setLayout(layout);",
            "      {",
            "        JButton button = new JButton();",
            "        GridBagConstraints gbc = new GridBagConstraints();",
            "        gbc.gridx = 1;",
            "        gbc.gridy = 2;",
            "        inner.add(button, gbc);",
            "      }",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // prepare memento
    JavaInfoMemento memento;
    {
      ContainerInfo inner = getJavaInfoByName("inner");
      memento = JavaInfoMemento.createMemento(inner);
    }
    //
    ContainerInfo newInner = (ContainerInfo) memento.create(panel);
    ((FlowLayoutInfo) panel.getLayout()).add(newInner, null);
    memento.apply();
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JPanel inner = new JPanel();",
        "      add(inner);",
        "      GridBagLayout layout = new GridBagLayout();",
        "      layout.columnWidths = new int[]{10, 20, 0};",
        "      layout.columnWeights = new double[]{1.0, 2.0, Double.MIN_VALUE};",
        "      layout.rowHeights = new int[]{10, 20, 30, 0};",
        "      layout.rowWeights = new double[]{1.0, 2.0, 3.0, Double.MIN_VALUE};",
        "      inner.setLayout(layout);",
        "      {",
        "        JButton button = new JButton();",
        "        GridBagConstraints gbc = new GridBagConstraints();",
        "        gbc.gridx = 1;",
        "        gbc.gridy = 2;",
        "        inner.add(button, gbc);",
        "      }",
        "    }",
        "    {",
        "      JPanel panel = new JPanel();",
        "      add(panel);",
        "      GridBagLayout gridBagLayout = new GridBagLayout();",
        "      gridBagLayout.columnWidths = new int[]{10, 20, 0};",
        "      gridBagLayout.rowHeights = new int[]{10, 20, 30, 0};",
        "      gridBagLayout.columnWeights = new double[]{1.0, 2.0, Double.MIN_VALUE};",
        "      gridBagLayout.rowWeights = new double[]{1.0, 2.0, 3.0, Double.MIN_VALUE};",
        "      panel.setLayout(gridBagLayout);",
        "      {",
        "        JButton button = new JButton();",
        "        GridBagConstraints gbc = new GridBagConstraints();",
        "        gbc.gridx = 1;",
        "        gbc.gridy = 2;",
        "        panel.add(button, gbc);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * We should not change alignments when paste existing panel.
   */
  public void test_clipboard_disableAutoAlignment() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JPanel inner = new JPanel();",
            "      add(inner);",
            "      inner.setLayout(new GridBagLayout());",
            "      {",
            "        JLabel label = new JLabel();",
            "        GridBagConstraints gbc = new GridBagConstraints();",
            "        gbc.gridx = 0;",
            "        gbc.gridy = 0;",
            "        inner.add(label, gbc);",
            "      }",
            "      {",
            "        JTextField textField = new JTextField();",
            "        GridBagConstraints gbc = new GridBagConstraints();",
            "        gbc.gridx = 1;",
            "        gbc.gridy = 0;",
            "        inner.add(textField, gbc);",
            "      }",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // prepare memento
    JavaInfoMemento memento;
    {
      ContainerInfo inner = getJavaInfoByName("inner");
      memento = JavaInfoMemento.createMemento(inner);
    }
    //
    ContainerInfo newInner = (ContainerInfo) memento.create(panel);
    ((FlowLayoutInfo) panel.getLayout()).add(newInner, null);
    memento.apply();
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JPanel inner = new JPanel();",
        "      add(inner);",
        "      inner.setLayout(new GridBagLayout());",
        "      {",
        "        JLabel label = new JLabel();",
        "        GridBagConstraints gbc = new GridBagConstraints();",
        "        gbc.gridx = 0;",
        "        gbc.gridy = 0;",
        "        inner.add(label, gbc);",
        "      }",
        "      {",
        "        JTextField textField = new JTextField();",
        "        GridBagConstraints gbc = new GridBagConstraints();",
        "        gbc.gridx = 1;",
        "        gbc.gridy = 0;",
        "        inner.add(textField, gbc);",
        "      }",
        "    }",
        "    {",
        "      JPanel panel = new JPanel();",
        "      add(panel);",
        "      GridBagLayout gridBagLayout = new GridBagLayout();",
        "      gridBagLayout.columnWidths = new int[]{0, 0, 0};",
        "      gridBagLayout.rowHeights = new int[]{0, 0};",
        "      gridBagLayout.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};",
        "      gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};",
        "      panel.setLayout(gridBagLayout);",
        "      {",
        "        JLabel label = new JLabel();",
        "        GridBagConstraints gbc = new GridBagConstraints();",
        "        gbc.insets = new Insets(0, 0, 0, 5);",
        "        gbc.gridx = 0;",
        "        gbc.gridy = 0;",
        "        panel.add(label, gbc);",
        "      }",
        "      {",
        "        JTextField textField = new JTextField();",
        "        GridBagConstraints gbc = new GridBagConstraints();",
        "        gbc.gridx = 1;",
        "        gbc.gridy = 0;",
        "        panel.add(textField, gbc);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bugs
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * <p>
   * 40334: NPE by relocation of the component in GridBagLayout
   */
  public void test_autoRename_lazyVariable() throws Exception {
    final ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new GridBagLayout());",
            "  }",
            "}");
    panel.refresh();
    // add new component
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
        // use "lazy"
        SwingTestUtils.setGenerations(
            LazyVariableDescription.INSTANCE,
            LazyStatementGeneratorDescription.INSTANCE);
        SwingTestUtils.setLazyMethodModifier(LazyVariableSupport.V_METHOD_MODIFIER_PRIVATE);
        try {
          ComponentInfo button_1 = createJButton();
          layout.command_CREATE(button_1, 0, false, 0, false);
          assertEditor(
              "class Test extends JPanel {",
              "  private JButton button;",
              "  public Test() {",
              "    setLayout(new GridBagLayout());",
              "    GridBagConstraints gbc = new GridBagConstraints();",
              "    gbc.gridx = 0;",
              "    gbc.gridy = 0;",
              "    add(getButton(), gbc);",
              "  }",
              "  private JButton getButton() {",
              "    if (button == null) {",
              "      button = new JButton();",
              "    }",
              "    return button;",
              "  }",
              "}");
          // set text, will rename
          setText_withAlwaysRename(button_1, "aaa");
          assertEditor(
              "class Test extends JPanel {",
              "  private JButton aaaButton;",
              "  public Test() {",
              "    setLayout(new GridBagLayout());",
              "    GridBagConstraints gbc = new GridBagConstraints();",
              "    gbc.gridx = 0;",
              "    gbc.gridy = 0;",
              "    add(getAaaButton(), gbc);",
              "  }",
              "  private JButton getAaaButton() {",
              "    if (aaaButton == null) {",
              "      aaaButton = new JButton();",
              "      aaaButton.setText('aaa');",
              "    }",
              "    return aaaButton;",
              "  }",
              "}");
          // add one more JButton, crash happens
          ComponentInfo button_2 = createJButton();
          layout.command_CREATE(button_2, 1, false, 0, false);
          assertEditor(
              "class Test extends JPanel {",
              "  private JButton aaaButton;",
              "  private JButton button;",
              "  public Test() {",
              "    setLayout(new GridBagLayout());",
              "    GridBagConstraints gbc = new GridBagConstraints();",
              "    gbc.insets = new Insets(0, 0, 0, 5);",
              "    gbc.gridx = 0;",
              "    gbc.gridy = 0;",
              "    add(getAaaButton(), gbc);",
              "    GridBagConstraints gbc_1 = new GridBagConstraints();",
              "    gbc_1.gridx = 1;",
              "    gbc_1.gridy = 0;",
              "    add(getButton(), gbc_1);",
              "  }",
              "  private JButton getAaaButton() {",
              "    if (aaaButton == null) {",
              "      aaaButton = new JButton();",
              "      aaaButton.setText('aaa');",
              "    }",
              "    return aaaButton;",
              "  }",
              "  private JButton getButton() {",
              "    if (button == null) {",
              "      button = new JButton();",
              "    }",
              "    return button;",
              "  }",
              "}");
        } finally {
          SwingTestUtils.setGenerationDefaults();
        }
      }
    });
  }

  public void test_useJPanelWithGBL() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public MyPanel() {",
            "    GridBagLayout layout = new GridBagLayout();",
            "    setLayout(layout);",
            "    layout.columnWidths = new int[]{0, 0};",
            "    layout.columnWeights = new double[]{0.0, Double.MIN_VALUE};",
            "    layout.rowHeights = new int[]{0, 0};",
            "    layout.rowWeights = new double[]{0.0, Double.MIN_VALUE};",
            "    {",
            "      JButton button = new JButton();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 0;",
            "      gbc.gridy = 0;",
            "      add(button, gbc);",
            "    }",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "    add(new MyPanel());",
            "  }",
            "}");
    panel.refresh();
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/setLayout(new BorderLayout())/ /add(new MyPanel())/}",
        "  {new: java.awt.BorderLayout} {empty} {/setLayout(new BorderLayout())/}",
        "  {new: test.MyPanel} {empty} {/add(new MyPanel())/}",
        "    {implicit-layout: java.awt.GridBagLayout} {implicit-layout} {}");
    ContainerInfo inner = (ContainerInfo) panel.getChildrenComponents().get(0);
    GridBagLayoutInfo layout = (GridBagLayoutInfo) inner.getLayout();
    //
    IGridInfo gridInfo = layout.getGridInfo();
    assertEquals(1, gridInfo.getColumnCount());
    assertEquals(1, layout.getColumns().size());
  }

  /**
   * We considered mistakenly {@link GridBagLayoutInfo} not attached to {@link ContainerInfo} as
   * layout for container of "this" {@link ContainerInfo}, because both have same "null" parent. We
   * should use better check.
   */
  public void test_danglingGBL() throws Exception {
    parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    GridBagLayout danglingLayout = new GridBagLayout();",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
  }

  /**
   * If will column/row has component, then consider it as normal column/row, not as artificial, so
   * don't remove it.
   */
  public void test_componentInFiller() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout layout = new GridBagLayout();",
            "    setLayout(layout);",
            "    layout.columnWidths = new int[]{0, 0};",
            "    layout.columnWeights = new double[]{0.0, Double.MIN_VALUE};",
            "    layout.rowHeights = new int[]{0, 0};",
            "    layout.rowWeights = new double[]{0.0, Double.MIN_VALUE};",
            "    {",
            "      JButton button = new JButton();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 1;",
            "      gbc.gridy = 1;",
            "      add(button, gbc);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
    // IGridInfo
    {
      IGridInfo gridInfo = layout.getGridInfo();
      assertEquals(2, gridInfo.getColumnCount());
      assertEquals(2, gridInfo.getRowCount());
    }
    // dimensions
    assertThat(layout.getColumns()).hasSize(2);
    assertThat(layout.getRows()).hasSize(2);
  }

  /**
   * We should add {@link GridBagLayoutInfo} before components. But there was problem that we added
   * it also before {@link GridBagConstraintsInfo}, so in lazy mode - in "parent" block, not in
   * container "accessor" method.
   */
  public void test_setLayout_whenContainerOnGBL() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  private JPanel inner;",
            "  public Test() {",
            "    setLayout(new GridBagLayout());",
            "    GridBagConstraints gbc = new GridBagConstraints();",
            "    gbc.gridx = 0;",
            "    gbc.gridy = 0;",
            "    add(getInner(), gbc);",
            "  }",
            "  private JPanel getInner() {",
            "    if (inner == null) {",
            "      inner = new JPanel();",
            "    }",
            "    return inner;",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/setLayout(new GridBagLayout())/ /add(getInner(), gbc)/}",
        "  {new: java.awt.GridBagLayout} {empty} {/setLayout(new GridBagLayout())/}",
        "  {new: javax.swing.JPanel} {lazy: inner getInner()} {/new JPanel()/ /inner/ /add(getInner(), gbc)/}",
        "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "    {new: java.awt.GridBagConstraints} {local-unique: gbc} {/new GridBagConstraints()/ /gbc.gridx = 0/ /gbc.gridy = 0/ /add(getInner(), gbc)/}");
    panel.refresh();
    ContainerInfo inner = getJavaInfoByName("inner");
    //
    // prepare new Layout
    LayoutInfo newLayout = createJavaInfo("java.awt.GridLayout");
    inner.setLayout(newLayout);
    assertEditor(
        "class Test extends JPanel {",
        "  private JPanel inner;",
        "  public Test() {",
        "    setLayout(new GridBagLayout());",
        "    GridBagConstraints gbc = new GridBagConstraints();",
        "    gbc.gridx = 0;",
        "    gbc.gridy = 0;",
        "    add(getInner(), gbc);",
        "  }",
        "  private JPanel getInner() {",
        "    if (inner == null) {",
        "      inner = new JPanel();",
        "      inner.setLayout(new GridLayout(1, 0, 0, 0));",
        "    }",
        "    return inner;",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/setLayout(new GridBagLayout())/ /add(getInner(), gbc)/}",
        "  {new: java.awt.GridBagLayout} {empty} {/setLayout(new GridBagLayout())/}",
        "  {new: javax.swing.JPanel} {lazy: inner getInner()} {/new JPanel()/ /inner/ /add(getInner(), gbc)/ /inner.setLayout(new GridLayout(1, 0, 0, 0))/}",
        "    {new: java.awt.GridBagConstraints} {local-unique: gbc} {/new GridBagConstraints()/ /gbc.gridx = 0/ /gbc.gridy = 0/ /add(getInner(), gbc)/}",
        "    {new: java.awt.GridLayout} {empty} {/inner.setLayout(new GridLayout(1, 0, 0, 0))/}");
  }

  /**
   * {@link JPopupMenuInfo} is not managed by {@link LayoutInfo}.
   */
  public void test_JPopupMenu() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new GridBagLayout());",
            "    {",
            "      JButton button = new JButton('button');",
            "      add(button, new GridBagConstraints());",
            "    }",
            "    {",
            "      JPopupMenu popupMenu = new JPopupMenu();",
            "      addPopup(this, popupMenu);",
            "    }",
            "  }",
            "  private static void addPopup(Component component, JPopupMenu popup) {",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/setLayout(new GridBagLayout())/ /add(button, new GridBagConstraints())/ /addPopup(this, popupMenu)/}",
        "  {new: java.awt.GridBagLayout} {empty} {/setLayout(new GridBagLayout())/}",
        "  {new: javax.swing.JButton} {local-unique: button} {/new JButton('button')/ /add(button, new GridBagConstraints())/}",
        "    {new: java.awt.GridBagConstraints} {empty} {/add(button, new GridBagConstraints())/}",
        "  {new: javax.swing.JPopupMenu} {local-unique: popupMenu} {/new JPopupMenu()/ /addPopup(this, popupMenu)/}");
    GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
    ComponentInfo button = getJavaInfoByName("button");
    //
    assertThat(layout.getComponents()).containsExactly(button);
  }
}
