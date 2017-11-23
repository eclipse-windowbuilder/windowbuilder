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
package org.eclipse.wb.tests.designer.swing.model.layout.FormLayout;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Interval;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.check.AssertionFailedException;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.FormLayout.Activator;
import org.eclipse.wb.internal.swing.FormLayout.model.CellConstraintsSupport;
import org.eclipse.wb.internal.swing.FormLayout.model.DimensionsProperty;
import org.eclipse.wb.internal.swing.FormLayout.model.FormColumnInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.FormLayoutInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.FormRowInfo;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;

import org.eclipse.jface.action.MenuManager;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang.ArrayUtils;

import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.Border;

/**
 * Test for {@link FormLayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class FormLayoutTest extends AbstractFormLayoutTest {
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
  // Activator
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link Activator}.
   */
  public void test_Activator() throws Exception {
    assertNotNull(Activator.getDefault());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for columns/rows.
   */
  public void test_object() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.RELATED_GAP_COLSPEC,",
            "        ColumnSpec.decode('70dlu'),",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.DEFAULT_ROWSPEC,}));",
            "  }",
            "}");
    panel.refresh();
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    // check columns
    {
      List<FormColumnInfo> columns = layout.getColumns();
      assertEquals(3, columns.size());
    }
    // check rows
    {
      List<FormRowInfo> rows = layout.getRows();
      assertEquals(1, rows.size());
    }
    // check IGridInfo
    {
      IGridInfo gridInfo = layout.getGridInfo();
      assertEquals(3, gridInfo.getColumnCount());
      assertEquals(1, gridInfo.getRowCount());
      // prepare intervals
      Interval[] columnIntervals = gridInfo.getColumnIntervals();
      Interval[] rowIntervals = gridInfo.getRowIntervals();
      assertEquals(3, columnIntervals.length);
      assertEquals(1, rowIntervals.length);
      // check that even when column/row is empty, it still has some size
      {
        assertThat(columnIntervals[2].length).isGreaterThan(18);
        assertFalse(columnIntervals[0].isEmpty());
        //
        assertThat(rowIntervals[0].length).isGreaterThan(18);
        assertFalse(rowIntervals[0].isEmpty());
      }
    }
  }

  /**
   * There was {@link ClassCastException} when use {@link JPanel} with {@link FormLayout}.
   */
  public void test_useJPannel_withFormLayout() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public MyPanel() {",
            "    setLayout(new FormLayout());",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new MyPanel());",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(new MyPanel())/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: test.MyPanel} {empty} {/add(new MyPanel())/}",
        "    {implicit-layout: com.jgoodies.forms.layout.FormLayout} {implicit-layout} {}");
    // refresh
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * Test that on {@link JFrame} the {@link JTable} with grab/fill has big size.
   */
  public void test_withJTable() throws Exception {
    ContainerInfo frame =
        parseContainer(
            "public class Test extends JFrame {",
            "  public Test() {",
            "    getContentPane().setLayout(new FormLayout(new ColumnSpec[] {",
            "        ColumnSpec.decode('default:grow'),},",
            "      new RowSpec[] {",
            "        RowSpec.decode('default:grow'),}));",
            "    {",
            "      JTable table = new JTable();",
            "      getContentPane().add(table, '1, 1, fill, fill');",
            "    }",
            "  }",
            "}");
    frame.refresh();
    ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
    ComponentInfo table = contentPane.getChildrenComponents().get(0);
    assertTrue("Actual size: " + table.getBounds().getSize(), table.getBounds().width > 300);
    assertTrue("Actual size: " + table.getBounds().getSize(), table.getBounds().height > 200);
  }

  /**
   * Test {@link DimensionsProperty} for columns/rows.
   */
  public void test_DimensionsProperty() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.RELATED_GAP_COLSPEC,",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.DEFAULT_ROWSPEC,}));",
            "  }",
            "}");
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    panel.refresh();
    // columnSpecs
    {
      Property property = layout.getPropertyByTitle("columnSpecs");
      assertSame(Property.UNKNOWN_VALUE, property.getValue());
      assertTrue(property.isModified());
      property.setValue(this); // ignored
      // check text in TextDisplayPropertyEditor
      assertEquals("related gap, default", getPropertyText(property));
    }
    // rowSpecs
    {
      Property property = layout.getPropertyByTitle("rowSpecs");
      assertSame(Property.UNKNOWN_VALUE, property.getValue());
      assertTrue(property.isModified());
      property.setValue(this); // ignored
      // check text in TextDisplayPropertyEditor
      assertEquals("default", getPropertyText(property));
    }
  }

  /**
   * Test for "Edit columns" and "Edit rows" actions.
   */
  public void test_editColumnsRowsActions() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {},",
            "      new RowSpec[] {}));",
            "  }",
            "}");
    // check for actions
    MenuManager menuManager = getDesignerMenuManager();
    panel.getBroadcastObject().addContextMenu(ImmutableList.of(panel), panel, menuManager);
    assertNotNull(findChildAction(menuManager, "Edit c&olumns..."));
    assertNotNull(findChildAction(menuManager, "Edit &rows..."));
  }

  /**
   * Test {@link FormLayoutInfo#setColumns(List)}.
   */
  public void test_setColumns() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.RELATED_GAP_COLSPEC,",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.DEFAULT_ROWSPEC,}));",
            "  }",
            "}");
    panel.refresh();
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    //
    assertEquals(2, layout.getColumns().size());
    layout.setColumns(Lists.<FormColumnInfo>newArrayList());
    assertEquals(0, layout.getColumns().size());
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {},",
        "      new RowSpec[] {",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "  }",
        "}");
  }

  /**
   * Test {@link FormLayoutInfo#setRows(List)}.
   */
  public void test_setRows() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.RELATED_GAP_COLSPEC,",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.DEFAULT_ROWSPEC,}));",
            "  }",
            "}");
    panel.refresh();
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    //
    assertEquals(1, layout.getRows().size());
    //
    layout.setRows(Lists.<FormRowInfo>newArrayList());
    assertEquals(0, layout.getRows().size());
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {}));",
        "  }",
        "}");
  }

  public void test_getMinimumSize() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,",
            "        FormSpecs.DEFAULT_COLSPEC,",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.DEFAULT_ROWSPEC,",
            "        FormSpecs.DEFAULT_ROWSPEC,}));",
            "    add(new JButton(), '1,1,2,1');",
            "  }",
            "}");
    panel.refresh();
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    //
    Dimension minimumSize = layout.getMinimumSize();
    assertEquals(2, minimumSize.width);
    assertEquals(1, minimumSize.height);
  }

  /**
   * More tests for {@link IGridInfo}.
   */
  public void test_gridInfo() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        ColumnSpec.decode('120px'),",
            "        ColumnSpec.decode('50px'),",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        RowSpec.decode('40px'),",
            "        FormSpecs.DEFAULT_ROWSPEC,}));",
            "    add(new JButton(), '1,1,2,1');",
            "  }",
            "}");
    panel.refresh();
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    ComponentInfo button = panel.getChildrenComponents().get(0);
    //
    IGridInfo gridInfo = layout.getGridInfo();
    assertEquals(3, gridInfo.getColumnCount());
    assertEquals(2, gridInfo.getRowCount());
    // insets
    assertEquals(new Insets(0, 0, 0, 0), gridInfo.getInsets());
    // check virtual for columns/rows
    {
      assertThat(gridInfo.getVirtualColumnSize()).isGreaterThan(18);
      assertThat(gridInfo.getVirtualColumnGap()).isGreaterThan(3);
      assertThat(gridInfo.getVirtualRowSize()).isGreaterThan(18);
      assertThat(gridInfo.getVirtualRowGap()).isGreaterThan(3);
    }
    // component cells
    Rectangle cells = gridInfo.getComponentCells(button);
    assertEquals(new Rectangle(0, 0, 2, 1), cells);
    // cells -> pixels
    Rectangle cellsRectangle = gridInfo.getCellsRectangle(cells);
    assertEquals(0, cellsRectangle.x);
    assertEquals(0, cellsRectangle.y);
    assertEquals(171, cellsRectangle.width);
    assertEquals(41, cellsRectangle.height);
    // occupied
    assertSame(button, gridInfo.getOccupied(0, 0));
    assertSame(button, gridInfo.getOccupied(1, 0));
    assertNull(gridInfo.getOccupied(0, 1));
    assertNull(gridInfo.getOccupied(1, 1));
  }

  /**
   * {@link IGridInfo#getInsets()} should return insets of {@link Border}.
   */
  public void test_gridInfo_insets() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setBorder(new EmptyBorder(10, 20, 30, 40));",
            "    setLayout(new FormLayout());",
            "  }",
            "}");
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    panel.refresh();
    //
    IGridInfo gridInfo = layout.getGridInfo();
    assertEquals(new Insets(10, 20, 30, 40), gridInfo.getInsets());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // normalizeSpanning()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * No gaps, each column/row has component, so no change.
   */
  public void test_normalizeSpanning_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.DEFAULT_ROWSPEC,",
            "        FormSpecs.DEFAULT_ROWSPEC,}));",
            "    add(new JButton(), '1, 1');",
            "    add(new JButton(), '2, 2');",
            "  }",
            "}");
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    //
    layout.normalizeSpanning();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    add(new JButton(), '1, 1');",
        "    add(new JButton(), '2, 2');",
        "  }",
        "}");
  }

  /**
   * With gaps, each column/row has component, so no change.
   */
  public void test_normalizeSpanning_2() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.RELATED_GAP_COLSPEC,",
            "        FormSpecs.DEFAULT_COLSPEC,",
            "        FormSpecs.RELATED_GAP_COLSPEC,",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.RELATED_GAP_ROWSPEC,",
            "        FormSpecs.DEFAULT_ROWSPEC,",
            "        FormSpecs.RELATED_GAP_ROWSPEC,",
            "        FormSpecs.DEFAULT_ROWSPEC,}));",
            "    add(new JButton(), '2, 2');",
            "    add(new JButton(), '4, 4');",
            "  }",
            "}");
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    //
    layout.normalizeSpanning();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    add(new JButton(), '2, 2');",
        "    add(new JButton(), '4, 4');",
        "  }",
        "}");
  }

  /**
   * With gaps, last column/row has no components, so delete them.
   */
  public void test_normalizeSpanning_3() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.UNRELATED_GAP_COLSPEC,",
            "        FormSpecs.PREF_COLSPEC,",
            "        FormSpecs.RELATED_GAP_COLSPEC,",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.UNRELATED_GAP_ROWSPEC,",
            "        FormSpecs.PREF_ROWSPEC,",
            "        FormSpecs.RELATED_GAP_ROWSPEC,",
            "        FormSpecs.DEFAULT_ROWSPEC,}));",
            "    add(new JButton(), '2, 2');",
            "  }",
            "}");
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    //
    layout.normalizeSpanning();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.UNRELATED_GAP_COLSPEC,",
        "        FormSpecs.PREF_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.UNRELATED_GAP_ROWSPEC,",
        "        FormSpecs.PREF_ROWSPEC,}));",
        "    add(new JButton(), '2, 2');",
        "  }",
        "}");
  }

  /**
   * With gaps, first column/row has no components, so delete them.
   */
  public void test_normalizeSpanning_4() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.UNRELATED_GAP_COLSPEC,",
            "        FormSpecs.PREF_COLSPEC,",
            "        FormSpecs.RELATED_GAP_COLSPEC,",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.UNRELATED_GAP_ROWSPEC,",
            "        FormSpecs.PREF_ROWSPEC,",
            "        FormSpecs.RELATED_GAP_ROWSPEC,",
            "        FormSpecs.DEFAULT_ROWSPEC,}));",
            "    add(new JButton(), '4, 4');",
            "  }",
            "}");
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    //
    layout.normalizeSpanning();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    add(new JButton(), '2, 2');",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // get*ComponentsCount()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getColumnComponentsCounts() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,",
            "        FormSpecs.DEFAULT_COLSPEC,",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.DEFAULT_ROWSPEC,",
            "        FormSpecs.DEFAULT_ROWSPEC,}));",
            "    add(new JButton(), '1,1,2,1');",
            "    add(new JButton(), '1,2,1,1');",
            "    add(new JButton(), '3,2,1,1');",
            "  }",
            "}");
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    assertTrue(ArrayUtils.isEquals(new int[]{2, 0, 1}, layout.getColumnComponentsCounts()));
    assertTrue(ArrayUtils.isEquals(new int[]{1, 2}, layout.getRowComponentsCounts()));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dimension manipulations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link FormLayoutInfo#writeDimensions()} when there are no columns.
   */
  public void test_writeEmpty() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {}));",
            "  }",
            "}");
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    // remove columns
    layout.getColumns().clear();
    layout.writeDimensions();
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {},",
        "      new RowSpec[] {}));",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setLayout
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for installing.
   */
  public void test_setLayout() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    setLayout(panel, FormLayout.class);
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {},",
        "      new RowSpec[] {}));",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Create: empty cell.
   */
  public void test_CREATE_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.DEFAULT_ROWSPEC,}));",
            "  }",
            "}");
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    // create
    ComponentInfo newComponent = createJButton();
    layout.command_CREATE(newComponent, 1, false, 1, false);
    assertInstanceOf(InvocationChildAssociation.class, newComponent.getAssociation());
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    {",
        "      JButton button = new JButton();",
        "      add(button, '1, 1');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Create: insert cell and replace unsupported arguments on {@link FormLayout}.
   */
  public void test_CREATE_2() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(''));",
            "  }",
            "}");
    panel.refresh();
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    // initial state
    assertEquals(0, layout.getColumns().size());
    assertEquals(0, layout.getRows().size());
    // create
    ComponentInfo newComponent = createJButton();
    layout.command_CREATE(newComponent, 1, true, 1, true);
    assertInstanceOf(InvocationChildAssociation.class, newComponent.getAssociation());
    //
    CellConstraintsSupport constraints = FormLayoutInfo.getConstraints(newComponent);
    assertEquals(2, ReflectionUtils.getFieldInt(constraints, "x"));
    assertEquals(2, ReflectionUtils.getFieldInt(constraints, "y"));
    //
    assertEquals(2, layout.getColumns().size());
    assertEquals(2, layout.getRows().size());
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    {",
        "      JButton button = new JButton();",
        "      add(button, '2, 2');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Create: insert cell and move/resize existing children.<br>
   * Also, new component added before others, because it is in row/column before.
   */
  public void test_CREATE_3() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.RELATED_GAP_COLSPEC,",
            "        FormSpecs.DEFAULT_COLSPEC,",
            "        FormSpecs.RELATED_GAP_COLSPEC,",
            "        FormSpecs.DEFAULT_COLSPEC,",
            "        FormSpecs.RELATED_GAP_COLSPEC,",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.RELATED_GAP_ROWSPEC,",
            "        FormSpecs.DEFAULT_ROWSPEC,",
            "        FormSpecs.RELATED_GAP_ROWSPEC,",
            "        FormSpecs.DEFAULT_ROWSPEC,",
            "        FormSpecs.RELATED_GAP_ROWSPEC,",
            "        FormSpecs.DEFAULT_ROWSPEC,}));",
            "    {",
            "      JButton button = new JButton('111');",
            "      add(button, '2, 2');",
            "    }",
            "    {",
            "      JButton button = new JButton('222');",
            "      add(button, '4, 4');",
            "    }",
            "    {",
            "      JButton button = new JButton('col span');",
            "      add(button, '1, 6, 6, 1');",
            "    }",
            "    {",
            "      JButton button = new JButton('row span');",
            "      add(button, '6, 1, 1, 6');",
            "    }",
            "  }",
            "}");
    panel.refresh();
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    // initial state
    assertEquals(6, layout.getColumns().size());
    assertEquals(6, layout.getRows().size());
    // create
    ComponentInfo newComponent = createJButton();
    layout.command_CREATE(newComponent, 3, true, 3, true);
    assertInstanceOf(InvocationChildAssociation.class, newComponent.getAssociation());
    //
    assertEquals(8, layout.getColumns().size());
    assertEquals(8, layout.getRows().size());
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    {",
        "      JButton button = new JButton('111');",
        "      add(button, '2, 2');",
        "    }",
        "    {",
        "      JButton button = new JButton();",
        "      add(button, '4, 4');",
        "    }",
        "    {",
        "      JButton button = new JButton('222');",
        "      add(button, '6, 6');",
        "    }",
        "    {",
        "      JButton button = new JButton('col span');",
        "      add(button, '1, 8, 8, 1');",
        "    }",
        "    {",
        "      JButton button = new JButton('row span');",
        "      add(button, '8, 1, 1, 8');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Create: add more columns/rows.
   */
  public void test_CREATE_4() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout());",
            "  }",
            "}");
    panel.refresh();
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    // create
    ComponentInfo newComponent = createJButton();
    layout.command_CREATE(newComponent, 4, false, 4, false);
    assertInstanceOf(InvocationChildAssociation.class, newComponent.getAssociation());
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    {",
        "      JButton button = new JButton();",
        "      add(button, '4, 4');",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PASTE
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for copy/paste in {@link FormLayoutInfo}.
   */
  public void test_PASTE() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.DEFAULT_ROWSPEC,}));",
            "    {",
            "      JButton button = new JButton();",
            "      button.setEnabled(false);",
            "      add(button, '1, 1');",
            "    }",
            "  }",
            "}");
    panel.refresh();
    final FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    // prepare memento
    final JavaInfoMemento memento;
    {
      ComponentInfo button = panel.getChildrenComponents().get(0);
      memento = JavaInfoMemento.createMemento(button);
    }
    // do paste
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        ComponentInfo newComponent = (ComponentInfo) memento.create(layout);
        layout.command_CREATE(newComponent, 3, false, 3, false);
        memento.apply();
      }
    });
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    {",
        "      JButton button = new JButton();",
        "      button.setEnabled(false);",
        "      add(button, '1, 1');",
        "    }",
        "    {",
        "      JButton button = new JButton();",
        "      button.setEnabled(false);",
        "      add(button, '3, 3');",
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
   * Move component into different cell.
   */
  public void test_MOVE() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.RELATED_GAP_COLSPEC,",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.RELATED_GAP_ROWSPEC,",
            "        FormSpecs.DEFAULT_ROWSPEC,}));",
            "    {",
            "      JButton button = new JButton();",
            "      add(button, '2, 2');",
            "    }",
            "  }",
            "}");
    panel.refresh();
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // move
    layout.command_MOVE(button, 1, false, 1, false);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    {",
        "      JButton button = new JButton();",
        "      add(button, '1, 1');",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ADD
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Add component from other container.
   */
  public void test_ADD() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.DEFAULT_ROWSPEC,",
            "        FormSpecs.DEFAULT_ROWSPEC,}));",
            "    {",
            "      JPanel panel = new JPanel();",
            "      add(panel, '1, 1');",
            "      {",
            "        JButton button = new JButton();",
            "        panel.add(button);",
            "      }",
            "    }",
            "  }",
            "}");
    panel.refresh();
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    ContainerInfo innerPanel = (ContainerInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button = innerPanel.getChildrenComponents().get(0);
    // add
    panel.startEdit();
    layout.command_ADD(button, 2, false, 2, false);
    {
      CellConstraintsSupport constraints = FormLayoutInfo.getConstraints(button);
      assertEquals(2, ReflectionUtils.getFieldInt(constraints, "x"));
      assertEquals(2, ReflectionUtils.getFieldInt(constraints, "y"));
      assertEquals(1, ReflectionUtils.getFieldInt(constraints, "width"));
      assertEquals(1, ReflectionUtils.getFieldInt(constraints, "height"));
      assertEquals(CellConstraints.DEFAULT, ReflectionUtils.getFieldObject(constraints, "alignH"));
      assertEquals(CellConstraints.DEFAULT, ReflectionUtils.getFieldObject(constraints, "alignV"));
    }
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    {",
        "      JPanel panel = new JPanel();",
        "      add(panel, '1, 1');",
        "    }",
        "    {",
        "      JButton button = new JButton();",
        "      add(button, '2, 2');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Move button from {@link FormLayoutInfo}.
   */
  public void test_ADD_OUT() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.DEFAULT_ROWSPEC,",
            "        FormSpecs.DEFAULT_ROWSPEC,}));",
            "    {",
            "      JPanel panel = new JPanel();",
            "      add(panel, '1, 1');",
            "    }",
            "    {",
            "      JButton button = new JButton();",
            "      add(button, '2, 2');",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ContainerInfo innerPanel = (ContainerInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button = panel.getChildrenComponents().get(1);
    // move
    FlowLayoutInfo flowLayout = (FlowLayoutInfo) innerPanel.getLayout();
    flowLayout.move(button, null);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    {",
        "      JPanel panel = new JPanel();",
        "      add(panel, '1, 1');",
        "      {",
        "        JButton button = new JButton();",
        "        panel.add(button);",
        "      }",
        "    }",
        "  }",
        "}");
    // check that layout does not have cached CellConstraintsSupport
    try {
      FormLayoutInfo.getConstraints(button);
      fail();
    } catch (AssertionFailedException e) {
    }
  }

  /**
   * Delete button from {@link FormLayoutInfo}.
   */
  public void test_DELETE() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.DEFAULT_ROWSPEC,}));",
            "    {",
            "      JButton button = new JButton();",
            "      add(button, '1, 1');",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // delete
    button.delete();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "  }",
        "}");
    // check that layout does not have cached CellConstraintsSupport
    try {
      FormLayoutInfo.getConstraints(button);
      fail();
    } catch (AssertionFailedException e) {
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // canChangeDimensions()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link FormLayoutInfo#canChangeDimensions()}.
   */
  public void test_canChangeDimensions_constructor() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout());",
            "  }",
            "}");
    //
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    assertTrue(layout.canChangeDimensions());
  }

  /**
   * Test for {@link FormLayoutInfo#canChangeDimensions()}.
   */
  public void test_canChangeDimensions_implicit() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends JPanel {",
            "  public MyPanel() {",
            "    setLayout(new FormLayout());",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    //
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    assertFalse(layout.canChangeDimensions());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Problems
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link FormLayoutInfo} should contribute "Constraints" property only it is active on container.
   */
  public void test_checkForActive_whenContributeProperties() throws Exception {
    String[] lines =
        {
            "public class MyPanel extends JPanel {",
            "  public MyPanel() {",
            "    setLayout(new FormLayout());",
            "  }",
            "}"};
    setFileContentSrc("test/MyPanel.java", getTestSource(lines));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    setLayout(new GridLayout());",
            "    add(new JButton());",
            "  }",
            "}");
    assertHierarchy(
        "{this: test.MyPanel} {this} {/setLayout(new GridLayout())/ /add(new JButton())/}",
        "  {new: java.awt.GridLayout} {empty} {/setLayout(new GridLayout())/}",
        "  {new: javax.swing.JButton} {empty} {/add(new JButton())/}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    assertNull(button.getPropertyByTitle("Constraints"));
  }
}
