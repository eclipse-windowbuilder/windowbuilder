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
package org.eclipse.wb.tests.designer.swing.model.layout.MigLayout;

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.draw2d.geometry.Interval;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.swing.MigLayout.Activator;
import org.eclipse.wb.internal.swing.MigLayout.model.CellConstraintsSupport;
import org.eclipse.wb.internal.swing.MigLayout.model.DimensionsProperty;
import org.eclipse.wb.internal.swing.MigLayout.model.MigColumnInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.MigDimensionInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.MigLayoutInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.MigRowInfo;
import org.eclipse.wb.internal.swing.model.CoordinateUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.RectValue;
import org.eclipse.wb.tests.designer.Expectations.StrValue;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Display;

import junit.framework.AssertionFailedError;

import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.swing.JButton;

/**
 * Test for {@link MigLayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class MigLayoutTest extends AbstractMigLayoutTest {
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
  // setLayout() from context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Tests that {@link ContainerInfo} has item <code>"MigLayout"</code> and it works on clean
   * project, without added {@link MigLayout} jar.
   */
  @DisposeProjectAfter
  public void test_setLayoutFromContextMenu() throws Exception {
    do_projectDispose();
    do_projectCreate();
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertTrue(panel.hasLayout());
    // prepare "Set Layout" menu manager
    IMenuManager layoutManager;
    {
      MenuManager menuManager = getDesignerMenuManager();
      panel.getBroadcastObject().addContextMenu(ImmutableList.of(panel), panel, menuManager);
      layoutManager = findChildMenuManager(menuManager, "Set layout");
      assertNotNull(layoutManager);
    }
    // use one of the actions to set new layout
    {
      IAction action = findChildAction(layoutManager, "MigLayout");
      action.run();
      m_includeMigImports = false;
      assertEditor(
          "import net.miginfocom.swing.MigLayout;",
          "// filler filler filler",
          "public class Test extends JPanel {",
          "  public Test() {",
          "    setLayout(new MigLayout('', '[]', '[]'));",
          "  }",
          "}");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MigLayout_Info
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Dangling {@link MigLayoutInfo}, not connected to {@link ContainerInfo} causes problems.
   */
  public void test_dangling() throws Exception {
    parseContainer(
        "public class Test extends JPanel {",
        "  private MigLayout m_layout = new MigLayout();",
        "  public Test() {",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
  }

  /**
   * If container is NVO, its layout was not performed. But we should handle this correctly.
   */
  public void test_nonVisual() throws Exception {
    parseContainer(
        "import java.util.ArrayList;",
        "class Test extends JPanel {",
        "  /**",
        "  * @wbp.nonvisual location=10,20",
        "  */",
        "  private JPanel inner = new JPanel();",
        "  Test() {",
        "    inner.setLayout(new MigLayout());",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {NonVisualBeans}",
        "    {new: javax.swing.JPanel} {field-initializer: inner} {/new JPanel()/ /inner.setLayout(new MigLayout())/}",
        "      {new: net.miginfocom.swing.MigLayout} {empty} {/inner.setLayout(new MigLayout())/}");
    refresh();
    assertNoErrors(m_lastParseInfo);
  }

  /**
   * Test that {@link MigLayoutInfo#writeDimensions()} not only updates
   * {@link ClassInstanceCreation}, but also {@link ConstructorCreationSupport}.
   */
  public void test_writeDimensions_constructorProperties() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    // do write
    layout.writeDimensions();
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[]', '[]'));",
        "  }",
        "}");
    {
      ConstructorCreationSupport creationSupport =
          (ConstructorCreationSupport) layout.getCreationSupport();
      assertEquals(
          "<init>(java.lang.String,java.lang.String,java.lang.String)",
          creationSupport.getDescription().getSignature());
    }
  }

  /**
   * Test for using {@link LC} for layout constraints.
   */
  public void test_writeDimensions_LC() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout(new LC().insets('10 20 30 40')));",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    // do write
    layout.writeDimensions();
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout('insets 10 20 30 40', '[]', '[]'));",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IGridInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link IGridInfo}.
   */
  public void test_IGridInfo() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "    add(new JButton(C_1), 'cell 0 0, width 100px, height 40px');",
            "    add(new JButton(C_2), 'cell 1 1, width 150px');",
            "    add(new JButton(C_3), 'cell 2 1, width 50px');",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    ComponentInfo button_1 = panel.getChildrenComponents().get(0);
    ComponentInfo button_2 = panel.getChildrenComponents().get(1);
    ComponentInfo button_3 = panel.getChildrenComponents().get(2);
    IGridInfo gridInfo = layout.getGridInfo();
    // columns
    {
      assertEquals(3, gridInfo.getColumnCount());
      Interval[] columnIntervals = gridInfo.getColumnIntervals();
      assertThat(columnIntervals).hasSize(3);
      assertEquals("Interval(7, 100)", columnIntervals[0].toString());
      assertEquals("Interval(111, 150)", columnIntervals[1].toString());
      assertEquals("Interval(265, 50)", columnIntervals[2].toString());
    }
    // rows
    {
      assertEquals(2, gridInfo.getRowCount());
      Interval[] rowIntervals = gridInfo.getRowIntervals();
      assertThat(rowIntervals).hasSize(2);
      assertEquals("Interval(7, 40)", rowIntervals[0].toString());
      assertEquals(
          Expectations.get("Interval(51, 23)", new StrValue[]{
              new StrValue("kosta-home", "Interval(51, 25)"),
              new StrValue("scheglov-win", "Interval(51, 23)")}),
          rowIntervals[1].toString());
    }
    // cells
    {
      assertEquals(new Rectangle(0, 0, 1, 1), gridInfo.getComponentCells(button_1));
      assertEquals(new Rectangle(1, 1, 1, 1), gridInfo.getComponentCells(button_2));
      assertEquals(new Rectangle(2, 1, 1, 1), gridInfo.getComponentCells(button_3));
      //
      {
        Rectangle cells = new Rectangle(0, 0, 1, 1);
        Rectangle expected = new Rectangle(7, 7, 100 + 1, 40 + 1);
        assertEquals(expected, gridInfo.getCellsRectangle(cells));
      }
      {
        Rectangle cells = new Rectangle(2, 0, 1, 1);
        Rectangle expected = new Rectangle(265, 7, 50 + 1, 40 + 1);
        assertEquals(expected, gridInfo.getCellsRectangle(cells));
      }
      {
        Rectangle cells = new Rectangle(0, 0, 2, 2);
        Rectangle expected =
            Expectations.get(
                new Rectangle(7, 7, 100 + 4 + 150 + 1, 40 + 4 + 23 + 1),
                new RectValue[]{
                    new RectValue("kosta-home", new Rectangle(7,
                        7,
                        100 + 4 + 150 + 1,
                        40 + 4 + 25 + 1)),
                    new RectValue("scheglov-win", new Rectangle(7,
                        7,
                        100 + 4 + 150 + 1,
                        40 + 4 + 23 + 1))});
        assertEquals(expected, gridInfo.getCellsRectangle(cells));
      }
    }
    // occupied
    {
      assertSame(button_1, gridInfo.getOccupied(0, 0));
      assertSame(button_2, gridInfo.getOccupied(1, 1));
      assertSame(button_3, gridInfo.getOccupied(2, 1));
      assertNull(gridInfo.getOccupied(1, 0));
      assertNull(gridInfo.getOccupied(0, 1));
      assertNull(gridInfo.getOccupied(2, 0));
    }
    // insets, default sizes
    {
      assertEquals(CoordinateUtils.get(panel.getContainer().getInsets()), gridInfo.getInsets());
      assertEquals(4, gridInfo.getVirtualColumnGap());
      assertEquals(25, gridInfo.getVirtualColumnSize());
      assertEquals(4, gridInfo.getVirtualRowGap());
      assertEquals(25, gridInfo.getVirtualRowSize());
    }
  }

  /**
   * Test for {@link IGridInfo}.
   */
  public void test_IGridInfo_emptyColumnRow() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    IGridInfo gridInfo = layout.getGridInfo();
    // check that even when column/row is empty, it still has some size
    {
      // columns
      {
        assertEquals(1, gridInfo.getColumnCount());
        // intervals
        Interval[] columnIntervals = gridInfo.getColumnIntervals();
        assertThat(columnIntervals).hasSize(1);
        // interval[0]
        assertThat(columnIntervals[0].length).isGreaterThan(18);
        assertFalse(columnIntervals[0].isEmpty());
      }
      // rows
      {
        assertEquals(1, gridInfo.getRowCount());
        // intervals
        Interval[] rowIntervals = gridInfo.getRowIntervals();
        assertThat(rowIntervals).hasSize(1);
        // interval[0]
        assertThat(rowIntervals[0].length).isGreaterThan(18);
        assertFalse(rowIntervals[0].isEmpty());
      }
    }
  }

  /**
   * If component is invisible and excluded from layout, its cells are empty.
   */
  public void test_IGridInfo_getCellsRectangle_empty() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout('', '[][]'));",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    IGridInfo gridInfo = layout.getGridInfo();
    // check empty cells rectangle
    Rectangle cells = new Rectangle(0, 0, 0, 0);
    Rectangle expected = new Rectangle(7, 7, 0, 0);
    assertEquals(expected, gridInfo.getCellsRectangle(cells));
  }

  /**
   * Test for {@link IGridInfo}.
   */
  public void test_IGridInfo_withContainerInsets() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setBorder(new EmptyBorder(10, 20, 30, 40));",
            "    setLayout(new MigLayout());",
            "    add(new JButton(C_1), 'cell 0 0');",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    IGridInfo gridInfo = layout.getGridInfo();
    //
    ComponentInfo buttonInfo = panel.getChildrenComponents().get(0);
    JButton buttonObject = (JButton) buttonInfo.getComponent();
    // simple checks
    assertEquals(1, gridInfo.getColumnCount());
    assertEquals(1, gridInfo.getRowCount());
    {
      Interval[] intervals = gridInfo.getColumnIntervals();
      assertEquals(1, intervals.length);
      assertEquals(new Interval(buttonObject.getX(), buttonObject.getWidth()), intervals[0]);
    }
    {
      Interval[] intervals = gridInfo.getRowIntervals();
      assertEquals(1, intervals.length);
      assertEquals(new Interval(buttonObject.getY(), buttonObject.getHeight()), intervals[0]);
    }
  }

  /**
   * Test for {@link IGridInfo}.<br>
   * First component of spanned cell can specify span, so same cells should be returned for all
   * components in spanned cell(s).
   */
  public void test_IGridInfo_withSpanSplit() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "    add(new JButton(C_1), 'cell 0 0 2 1');",
            "    add(new JButton(C_2), 'cell 0 0');",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    ComponentInfo button_1 = panel.getChildrenComponents().get(0);
    ComponentInfo button_2 = panel.getChildrenComponents().get(1);
    //
    IGridInfo gridInfo = layout.getGridInfo();
    assertEquals(new Rectangle(0, 0, 2, 1), gridInfo.getComponentCells(button_1));
    assertEquals(new Rectangle(0, 0, 2, 1), gridInfo.getComponentCells(button_2));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MigDimension_Info
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_dimensions_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "    add(new JButton(C_1), 'cell 0 0, width 100px, height 40px');",
            "    add(new JButton(C_2), 'cell 1 1, width 150px');",
            "    add(new JButton(C_3), 'cell 2 1, width 50px');",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    {
      List<MigColumnInfo> columns = layout.getColumns();
      assertThat(columns).hasSize(3);
    }
    {
      List<MigRowInfo> rows = layout.getRows();
      assertThat(rows).hasSize(2);
    }
  }

  public void test_dimensions_2() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout('', '[][]', '[][][][]'));",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    {
      List<MigColumnInfo> columns = layout.getColumns();
      assertThat(columns).hasSize(2);
    }
    {
      List<MigRowInfo> rows = layout.getRows();
      assertThat(rows).hasSize(4);
    }
  }

  /**
   * Test for {@link MigDimensionInfo#getString()}.
   */
  public void test_dimensions_getString() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout('', '[10mm:3cm:3in][left]', '[100px,top][]'));",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    {
      List<MigColumnInfo> columns = layout.getColumns();
      {
        MigColumnInfo column = columns.get(0);
        assertEquals("[10mm:3cm:3in]", column.getString(true));
        assertEquals("10mm:3cm:3in", column.getString(false));
      }
      {
        MigColumnInfo column = columns.get(1);
        assertEquals("[left]", column.getString(true));
      }
    }
    {
      List<MigRowInfo> rows = layout.getRows();
      {
        MigRowInfo row = rows.get(0);
        assertEquals("[100px,top]", row.getString(true));
      }
      {
        MigRowInfo row = rows.get(1);
        assertEquals("[]", row.getString(true));
      }
    }
  }

  /**
   * Test for {@link MigDimensionInfo#setString(String)}.
   */
  public void test_dimensions_setString() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout('', '[]', '[]'));",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    // column := [100px:null,grow]
    {
      MigColumnInfo column = layout.getColumns().get(0);
      column.setString("[100px:null,grow]");
      assertEquals("[100px:null,grow]", column.getString(true));
      layout.writeDimensions();
      assertEditor(
          "public class Test extends JPanel implements IConstants {",
          "  public Test() {",
          "    setLayout(new MigLayout('', '[100px:null,grow]', '[]'));",
          "  }",
          "}");
    }
    // column := []
    {
      MigColumnInfo column = layout.getColumns().get(0);
      column.setString("[]");
      assertEquals("[]", column.getString(true));
      layout.writeDimensions();
      assertEditor(
          "public class Test extends JPanel implements IConstants {",
          "  public Test() {",
          "    setLayout(new MigLayout('', '[]', '[]'));",
          "  }",
          "}");
    }
    // row := [20mm,fill]
    {
      MigRowInfo row = layout.getRows().get(0);
      row.setString("[20mm,fill]");
      assertEquals("[20mm,fill]", row.getString(true));
      layout.writeDimensions();
      assertEditor(
          "public class Test extends JPanel implements IConstants {",
          "  public Test() {",
          "    setLayout(new MigLayout('', '[]', '[20mm,fill]'));",
          "  }",
          "}");
    }
  }

  /**
   * Test for {@link MigDimensionInfo#getTooltip()}.
   */
  public void test_dimensions_getTooltip() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout('', '[10mm:3cm:3in][left]', '[100px,top][]'));",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    {
      List<MigColumnInfo> columns = layout.getColumns();
      {
        MigColumnInfo column = columns.get(0);
        assertEquals("[10mm:3cm:3in]", column.getTooltip());
      }
      {
        MigColumnInfo column = columns.get(1);
        assertEquals("[left]", column.getTooltip());
      }
    }
    {
      List<MigRowInfo> rows = layout.getRows();
      {
        MigRowInfo row = rows.get(0);
        assertEquals("[100px,top]", row.getTooltip());
      }
      {
        MigRowInfo row = rows.get(1);
        assertEquals("[]", row.getTooltip());
      }
    }
  }

  /**
   * Test for {@link MigDimensionInfo#hasGrow()} and {@link MigDimensionInfo#flipGrow()}.
   */
  public void test_dimensions_isGrab() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout('', '[]', '[]'));",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    MigColumnInfo column = layout.getColumns().get(0);
    // initially not a grab
    assertFalse(column.hasGrow());
    // flip to "true"
    column.flipGrow();
    assertTrue(column.hasGrow());
    layout.writeDimensions();
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[grow]', '[]'));",
        "  }",
        "}");
    // flip to "false"
    column.flipGrow();
    assertFalse(column.hasGrow());
    layout.writeDimensions();
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[]', '[]'));",
        "  }",
        "}");
  }

  /**
   * Test for grow weight/priority.
   */
  public void test_dimensions_growWeightPriority() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout('', '[]', '[]'));",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    MigColumnInfo column = layout.getColumns().get(0);
    // no grow
    assertEquals(100, column.getGrowPriority());
    assertEquals(null, column.getGrow());
    // weight := 150
    {
      column.setGrow(150f);
      layout.writeDimensions();
      assertEditor(
          "public class Test extends JPanel implements IConstants {",
          "  public Test() {",
          "    setLayout(new MigLayout('', '[grow 150]', '[]'));",
          "  }",
          "}");
    }
    // priority := 200
    {
      column.setGrowPriority(200);
      layout.writeDimensions();
      assertEditor(
          "public class Test extends JPanel implements IConstants {",
          "  public Test() {",
          "    setLayout(new MigLayout('', '[growprio 200,grow 150]', '[]'));",
          "  }",
          "}");
    }
    // weight := 100, i.e. default
    {
      column.setGrow(100f);
      layout.writeDimensions();
      assertEditor(
          "public class Test extends JPanel implements IConstants {",
          "  public Test() {",
          "    setLayout(new MigLayout('', '[growprio 200,grow]', '[]'));",
          "  }",
          "}");
    }
    // weight := 0, i.e. no grow
    {
      column.setGrow(0f);
      layout.writeDimensions();
      assertEditor(
          "public class Test extends JPanel implements IConstants {",
          "  public Test() {",
          "    setLayout(new MigLayout('', '[growprio 200,grow 0]', '[]'));",
          "  }",
          "}");
    }
    // weight := null, i.e. no grow
    {
      column.setGrow(null);
      layout.writeDimensions();
      assertEditor(
          "public class Test extends JPanel implements IConstants {",
          "  public Test() {",
          "    setLayout(new MigLayout('', '[growprio 200]', '[]'));",
          "  }",
          "}");
    }
    // priority := 100, i.e. default
    {
      column.setGrowPriority(100);
      layout.writeDimensions();
      assertEditor(
          "public class Test extends JPanel implements IConstants {",
          "  public Test() {",
          "    setLayout(new MigLayout('', '[]', '[]'));",
          "  }",
          "}");
    }
  }

  /**
   * Test for shrink weight/priority.
   */
  public void test_dimensions_shrinkWeightPriority() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout('', '[]', '[]'));",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    MigColumnInfo column = layout.getColumns().get(0);
    // no shrink
    assertEquals(100, column.getShrinkPriority());
    assertEquals(100f, column.getShrink());
    // weight := 150
    {
      column.setShrink(150f);
      layout.writeDimensions();
      assertEditor(
          "public class Test extends JPanel implements IConstants {",
          "  public Test() {",
          "    setLayout(new MigLayout('', '[shrink 150]', '[]'));",
          "  }",
          "}");
    }
    // priority := 200
    {
      column.setShrinkPriority(200);
      layout.writeDimensions();
      assertEditor(
          "public class Test extends JPanel implements IConstants {",
          "  public Test() {",
          "    setLayout(new MigLayout('', '[shrinkprio 200,shrink 150]', '[]'));",
          "  }",
          "}");
    }
    // weight := 100, i.e. default
    {
      column.setShrink(100f);
      layout.writeDimensions();
      assertEditor(
          "public class Test extends JPanel implements IConstants {",
          "  public Test() {",
          "    setLayout(new MigLayout('', '[shrinkprio 200]', '[]'));",
          "  }",
          "}");
    }
    // weight := 0, i.e. no shrink
    {
      column.setShrink(0f);
      layout.writeDimensions();
      assertEditor(
          "public class Test extends JPanel implements IConstants {",
          "  public Test() {",
          "    setLayout(new MigLayout('', '[shrinkprio 200,shrink 0]', '[]'));",
          "  }",
          "}");
    }
    // weight := null, i.e. default shrink
    {
      column.setShrink(null);
      layout.writeDimensions();
      assertEditor(
          "public class Test extends JPanel implements IConstants {",
          "  public Test() {",
          "    setLayout(new MigLayout('', '[shrinkprio 200]', '[]'));",
          "  }",
          "}");
    }
    // priority := 100, i.e. default
    {
      column.setShrinkPriority(100);
      layout.writeDimensions();
      assertEditor(
          "public class Test extends JPanel implements IConstants {",
          "  public Test() {",
          "    setLayout(new MigLayout('', '[]', '[]'));",
          "  }",
          "}");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Column: getAlignment()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Default alignments, for single existing column and created implicitly by adding component.
   */
  public void test_ColumnInfo_getAlignment_0() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "    add(new JButton(C_1), 'cell 0 0');",
            "    add(new JButton(C_2), 'cell 1 0');",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    //
    List<MigColumnInfo> columns = layout.getColumns();
    assertThat(columns).hasSize(2);
    // by default MigLayout has single column
    {
      MigColumnInfo column = columns.get(0);
      assertSame(MigColumnInfo.Alignment.DEFAULT, column.getAlignment(false));
      assertSame(MigColumnInfo.Alignment.LEADING, column.getAlignment(true));
    }
    // when we add component, it can create new default column
    {
      MigColumnInfo column = columns.get(1);
      assertSame(MigColumnInfo.Alignment.DEFAULT, column.getAlignment(false));
      assertSame(MigColumnInfo.Alignment.LEADING, column.getAlignment(true));
    }
  }

  public void test_ColumnInfo_getAlignment_LEFT() throws Exception {
    check_ColumnInfo_getAlignment("left", MigColumnInfo.Alignment.LEFT);
  }

  public void test_ColumnInfo_getAlignment_CENTER() throws Exception {
    check_ColumnInfo_getAlignment("center", MigColumnInfo.Alignment.CENTER);
  }

  public void test_ColumnInfo_getAlignment_RIGHT() throws Exception {
    check_ColumnInfo_getAlignment("right", MigColumnInfo.Alignment.RIGHT);
  }

  public void test_ColumnInfo_getAlignment_FILL() throws Exception {
    check_ColumnInfo_getAlignment("fill", MigColumnInfo.Alignment.FILL);
  }

  public void test_ColumnInfo_getAlignment_LEADING() throws Exception {
    check_ColumnInfo_getAlignment("leading", MigColumnInfo.Alignment.LEADING);
  }

  public void test_ColumnInfo_getAlignment_TRAILING() throws Exception {
    check_ColumnInfo_getAlignment("trailing", MigColumnInfo.Alignment.TRAILING);
  }

  public void test_ColumnInfo_getAlignment_UNKNOWN() throws Exception {
    check_ColumnInfo_getAlignment("align 30%", MigColumnInfo.Alignment.UNKNOWN);
  }

  /**
   * Test for {@link MigColumnInfo#getAlignment()}.
   */
  private void check_ColumnInfo_getAlignment(String alignmentString,
      MigColumnInfo.Alignment expectedAlignment) throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout('', '" + alignmentString + "'));",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    // check alignment
    MigColumnInfo column = layout.getColumns().get(0);
    assertSame(expectedAlignment, column.getAlignment(true));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Column: setAlignment()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_ColumnInfo_setAlignment_LEFT() throws Exception {
    check_ColumnInfo_setAlignment(MigColumnInfo.Alignment.LEFT, "'', '[left]', '[]'");
  }

  public void test_ColumnInfo_setAlignment_CENTER() throws Exception {
    check_ColumnInfo_setAlignment(MigColumnInfo.Alignment.CENTER, "'', '[center]', '[]'");
  }

  public void test_ColumnInfo_setAlignment_RIGHT() throws Exception {
    check_ColumnInfo_setAlignment(MigColumnInfo.Alignment.RIGHT, "'', '[right]', '[]'");
  }

  public void test_ColumnInfo_setAlignment_FILL() throws Exception {
    check_ColumnInfo_setAlignment(MigColumnInfo.Alignment.FILL, "'', '[fill]', '[]'");
  }

  public void test_ColumnInfo_setAlignment_LEADING() throws Exception {
    check_ColumnInfo_setAlignment(MigColumnInfo.Alignment.LEADING, "'', '[leading]', '[]'");
  }

  public void test_ColumnInfo_setAlignment_TRAILING() throws Exception {
    check_ColumnInfo_setAlignment(MigColumnInfo.Alignment.TRAILING, "'', '[trailing]', '[]'");
  }

  public void test_ColumnInfo_setAlignment_toDefault_1() throws Exception {
    check_ColumnInfo_setAlignment(
        "'', 'left', ''",
        MigColumnInfo.Alignment.DEFAULT,
        "'', '[]', '[]'");
  }

  public void test_ColumnInfo_setAlignment_toDefault_2() throws Exception {
    check_ColumnInfo_setAlignment(
        "'', 'fill', ''",
        MigColumnInfo.Alignment.DEFAULT,
        "'', '[]', '[]'");
  }

  public void test_ColumnInfo_setAlignment_withGrow_RIGHT() throws Exception {
    check_ColumnInfo_setAlignment(
        "'', '[grow]', ''",
        MigColumnInfo.Alignment.RIGHT,
        "'', '[grow,right]', '[]'");
  }

  public void test_ColumnInfo_setAlignment_withFill_RIGHT() throws Exception {
    check_ColumnInfo_setAlignment(
        "'', '[fill]', ''",
        MigColumnInfo.Alignment.RIGHT,
        "'', '[right]', '[]'");
  }

  public void test_ColumnInfo_setAlignment_UNKNOWN() throws Exception {
    try {
      check_ColumnInfo_setAlignment(MigColumnInfo.Alignment.UNKNOWN, "not used");
      fail();
    } catch (IllegalArgumentException e) {
    }
  }

  /**
   * Test for {@link MigColumnInfo#setAlignment(MigColumnInfo.Alignment)}.
   */
  private void check_ColumnInfo_setAlignment(MigColumnInfo.Alignment alignment,
      String expectedLayoutArgs) throws Exception {
    check_ColumnInfo_setAlignment("", alignment, expectedLayoutArgs);
  }

  /**
   * Test for {@link MigColumnInfo#setAlignment(MigColumnInfo.Alignment)}.
   */
  private void check_ColumnInfo_setAlignment(String initialArgs,
      MigColumnInfo.Alignment alignment,
      String expectedArgs) throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout(" + initialArgs + "));",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    // set alignment
    MigColumnInfo column = layout.getColumns().get(0);
    column.setAlignment(alignment);
    layout.writeDimensions();
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout(" + expectedArgs + "));",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Row: getAlignment()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Default alignments, for single existing row and created implicitly by adding component.
   */
  public void test_RowInfo_getAlignment_0() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "    add(new JButton(C_1), 'cell 0 0');",
            "    add(new JButton(C_2), 'cell 0 1');",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    //
    List<MigRowInfo> rows = layout.getRows();
    assertThat(rows).hasSize(2);
    // by default MigLayout has single row
    {
      MigRowInfo row = rows.get(0);
      assertSame(MigRowInfo.Alignment.DEFAULT, row.getAlignment(false));
      assertSame(MigRowInfo.Alignment.BASELINE, row.getAlignment(true));
    }
    // when we add component, it can create new default row
    {
      MigRowInfo row = rows.get(1);
      assertSame(MigRowInfo.Alignment.DEFAULT, row.getAlignment(false));
      assertSame(MigRowInfo.Alignment.BASELINE, row.getAlignment(true));
    }
  }

  public void test_RowInfo_getAlignment_TOP() throws Exception {
    check_RowInfo_getAlignment("top", MigRowInfo.Alignment.TOP);
  }

  public void test_RowInfo_getAlignment_CENTER() throws Exception {
    check_RowInfo_getAlignment("center", MigRowInfo.Alignment.CENTER);
  }

  public void test_RowInfo_getAlignment_BOTTOM() throws Exception {
    check_RowInfo_getAlignment("bottom", MigRowInfo.Alignment.BOTTOM);
  }

  public void test_RowInfo_getAlignment_FILL() throws Exception {
    check_RowInfo_getAlignment("fill", MigRowInfo.Alignment.FILL);
  }

  public void test_RowInfo_getAlignment_BASELINE() throws Exception {
    check_RowInfo_getAlignment("baseline", MigRowInfo.Alignment.BASELINE);
  }

  public void test_RowInfo_getAlignment_UNKNOWN() throws Exception {
    check_RowInfo_getAlignment("align 30%", MigRowInfo.Alignment.UNKNOWN);
  }

  /**
   * Test for {@link MigRowInfo#getAlignment()}.
   */
  private void check_RowInfo_getAlignment(String alignmentString,
      MigRowInfo.Alignment expectedAlignment) throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout('', '', '" + alignmentString + "'));",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    // check alignment
    MigRowInfo row = layout.getRows().get(0);
    assertSame(expectedAlignment, row.getAlignment(true));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Row: setAlignment()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_RowInfo_setAlignment_TOP() throws Exception {
    check_RowInfo_setAlignment(MigRowInfo.Alignment.TOP, "'', '[]', '[top]'");
  }

  public void test_RowInfo_setAlignment_CENTER() throws Exception {
    check_RowInfo_setAlignment(MigRowInfo.Alignment.CENTER, "'', '[]', '[center]'");
  }

  public void test_RowInfo_setAlignment_BOTTOM() throws Exception {
    check_RowInfo_setAlignment(MigRowInfo.Alignment.BOTTOM, "'', '[]', '[bottom]'");
  }

  public void test_RowInfo_setAlignment_FILL() throws Exception {
    check_RowInfo_setAlignment(MigRowInfo.Alignment.FILL, "'', '[]', '[fill]'");
  }

  public void test_RowInfo_setAlignment_BASELINE() throws Exception {
    check_RowInfo_setAlignment(MigRowInfo.Alignment.BASELINE, "'', '[]', '[baseline]'");
  }

  public void test_RowInfo_setAlignment_UNKNOWN() throws Exception {
    try {
      check_RowInfo_setAlignment(MigRowInfo.Alignment.UNKNOWN, "not used");
      fail();
    } catch (IllegalArgumentException e) {
    }
  }

  public void test_RowInfo_setAlignment_toDefault_1() throws Exception {
    check_RowInfo_setAlignment("'', '', 'top'", MigRowInfo.Alignment.DEFAULT, "'', '[]', '[]'");
  }

  public void test_RowInfo_setAlignment_toDefault_2() throws Exception {
    check_RowInfo_setAlignment("'', '', 'fill'", MigRowInfo.Alignment.DEFAULT, "'', '[]', '[]'");
  }

  public void test_RowInfo_setAlignment_withGrow_TOP() throws Exception {
    check_RowInfo_setAlignment("'', '', 'grow'", MigRowInfo.Alignment.TOP, "'', '[]', '[grow,top]'");
  }

  public void test_RowInfo_setAlignment_withFill_TOP() throws Exception {
    check_RowInfo_setAlignment("'', '', 'fill'", MigRowInfo.Alignment.TOP, "'', '[]', '[top]'");
  }

  /**
   * Test for {@link MigRowInfo#setAlignment(MigRowInfo.Alignment)}.
   */
  private void check_RowInfo_setAlignment(MigRowInfo.Alignment alignment, String expectedLayoutArgs)
      throws Exception {
    check_RowInfo_setAlignment("", alignment, expectedLayoutArgs);
  }

  /**
   * Test for {@link MigRowInfo#setAlignment(MigRowInfo.Alignment)}.
   */
  private void check_RowInfo_setAlignment(String initialArgs,
      MigRowInfo.Alignment alignment,
      String expectedArgs) throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout(" + initialArgs + "));",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    // set alignment
    MigRowInfo row = layout.getRows().get(0);
    row.setAlignment(alignment);
    layout.writeDimensions();
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout(" + expectedArgs + "));",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dimension: get*Size() and set*Size()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_dimensionSize_setCheck() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout('', '[100px]', '[]'));",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    MigColumnInfo column = layout.getColumns().get(0);
    // initially no "min" or "max"
    assertNull(column.getMinimumSize());
    assertNotNull(column.getPreferredSize());
    assertNull(column.getMaximumSize());
    // min := 1cm
    {
      column.setMinimumSize("1cm");
      assertNotNull(column.getMinimumSize());
      assertEquals("1cm", column.getString(column.getMinimumSize()));
      layout.writeDimensions();
      assertEditor(
          "public class Test extends JPanel implements IConstants {",
          "  public Test() {",
          "    setLayout(new MigLayout('', '[1cm:100px]', '[]'));",
          "  }",
          "}");
    }
    // min := 10%
    {
      column.setMinimumSize("10%");
      assertNotNull(column.getMinimumSize());
      assertEquals("10%", column.getString(column.getMinimumSize()));
      layout.writeDimensions();
      assertEditor(
          "public class Test extends JPanel implements IConstants {",
          "  public Test() {",
          "    setLayout(new MigLayout('', '[10%:100px]', '[]'));",
          "  }",
          "}");
    }
    // min := null
    {
      column.setMinimumSize(null);
      assertNull(column.getMinimumSize());
      assertEquals(null, column.getString(column.getMinimumSize()));
      layout.writeDimensions();
      assertEditor(
          "public class Test extends JPanel implements IConstants {",
          "  public Test() {",
          "    setLayout(new MigLayout('', '[100px]', '[]'));",
          "  }",
          "}");
    }
    // max := 5cm
    {
      column.setMaximumSize("5cm");
      assertNotNull(column.getMaximumSize());
      layout.writeDimensions();
      assertEditor(
          "public class Test extends JPanel implements IConstants {",
          "  public Test() {",
          "    setLayout(new MigLayout('', '[:100px:5cm]', '[]'));",
          "  }",
          "}");
    }
    // pref := 20mm
    {
      column.setMinimumSize("1cm");
      column.setPreferredSize("20mm");
      column.setMaximumSize("5cm");
      assertNotNull(column.getMinimumSize());
      assertNotNull(column.getPreferredSize());
      assertNotNull(column.getMaximumSize());
      layout.writeDimensions();
      assertEditor(
          "public class Test extends JPanel implements IConstants {",
          "  public Test() {",
          "    setLayout(new MigLayout('', '[1cm:20mm:5cm]', '[]'));",
          "  }",
          "}");
    }
    // size := 1in:2in:3in
    {
      column.setSize("1in:2in:3in");
      layout.writeDimensions();
      assertEditor(
          "public class Test extends JPanel implements IConstants {",
          "  public Test() {",
          "    setLayout(new MigLayout('', '[1in:2in:3in]', '[]'));",
          "  }",
          "}");
    }
    // size := default
    {
      column.setSize(null);
      layout.writeDimensions();
      assertEditor(
          "public class Test extends JPanel implements IConstants {",
          "  public Test() {",
          "    setLayout(new MigLayout('', '[]', '[]'));",
          "  }",
          "}");
    }
  }

  /**
   * Test for {@link MigDimensionInfo#toUnitString(int, String)}.
   */
  public void test_dimensionSize_toUnitString() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    {
      MigColumnInfo column = layout.getColumns().get(0);
      assertEquals("2.65cm", column.toUnitString(100, "cm"));
      assertEquals("22.22%", column.toUnitString(100, "%"));
      // check "sp" unit
      String expected_sp;
      int displayWidth = Display.getDefault().getBounds().width;
      if (displayWidth == 1920) {
        expected_sp = "5.21sp";
      } else if (displayWidth == 1680) {
        expected_sp = "5.95sp";
      } else {
        throw new AssertionFailedError("Unknown display width: " + displayWidth);
      }
      assertEquals(expected_sp, column.toUnitString(100, "sp"));
    }
    {
      MigRowInfo row = layout.getRows().get(0);
      assertEquals("2.65cm", row.toUnitString(100, "cm"));
      assertEquals("33.33%", row.toUnitString(100, "%"));
      // check "sp" unit
      int displayHeight = Display.getDefault().getBounds().height;
      String expected_sp;
      if (displayHeight == 1200) {
        expected_sp = "8.33sp";
      } else if (displayHeight == 1050) {
        expected_sp = "9.52sp";
      } else {
        throw new AssertionFailedError("Unknown display width: " + displayHeight);
      }
      assertEquals(expected_sp, row.toUnitString(100, "sp"));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // normalizeSpanning()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Each column/row has component, so no change.
   */
  public void test_normalizeSpanning_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "    add(new JButton(), 'cell 0 0');",
            "    add(new JButton(), 'cell 1 1');",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    //
    layout.normalizeSpanning();
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[][]', '[][]'));",
        "    add(new JButton(), 'cell 0 0');",
        "    add(new JButton(), 'cell 1 1');",
        "  }",
        "}");
  }

  /**
   * Last column/row has no components, so delete them.
   */
  public void test_normalizeSpanning_2() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout('', '[10][20]', '[30][40]'));",
            "    add(new JButton(), 'cell 0 0');",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    //
    layout.normalizeSpanning();
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[10]', '[30]'));",
        "    add(new JButton(), 'cell 0 0');",
        "  }",
        "}");
  }

  /**
   * First column/row has no components, so delete them.
   */
  public void test_normalizeSpanning_3() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout('', '[10][20]', '[30][40]'));",
            "    add(new JButton(), 'cell 1 1');",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    //
    layout.normalizeSpanning();
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[20]', '[40]'));",
        "    add(new JButton(), 'cell 0 0');",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // command_setCells
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_setCells() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "    add(new JButton(C_1), 'cell 0 0');",
            "    add(new JButton(C_2), 'cell 1 1');",
            "    add(new JButton(C_3), 'cell 1 2');",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    ComponentInfo button_3 = panel.getChildrenComponents().get(2);
    //
    layout.command_setCells(button_3, new Rectangle(0, 1, 1, 2));
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[][]', '[][][]'));",
        "    add(new JButton(C_1), 'cell 0 0');",
        "    add(new JButton(C_2), 'cell 1 1');",
        "    add(new JButton(C_3), 'cell 0 1 1 2');",
        "  }",
        "}");
    assertCellBounds(button_3, 0, 1, 1, 2);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE_existingCell() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    //
    ComponentInfo newButton = createJButton();
    layout.command_CREATE(newButton, 0, false, 0, false);
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout());",
        "    {",
        "      JButton button = new JButton();",
        "      add(button, 'cell 0 0');",
        "    }",
        "  }",
        "}");
    assertCellBounds(newButton, 0, 0, 1, 1);
  }

  public void test_CREATE_appendColumnRow() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    //
    ComponentInfo newButton = createJButton();
    layout.command_CREATE(newButton, 1, false, 2, false);
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[][]', '[][][]'));",
        "    {",
        "      JButton button = new JButton();",
        "      add(button, 'cell 1 2');",
        "    }",
        "  }",
        "}");
    assertCellBounds(newButton, 1, 2, 1, 1);
  }

  /**
   * Insert new column/row: move existing component.
   */
  public void test_CREATE_insertColumnRow_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "    add(new JButton(C_1), 'cell 0 1');",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    //
    ComponentInfo newButton = createJButton();
    layout.command_CREATE(newButton, 0, true, 0, true);
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[][]', '[][][]'));",
        "    {",
        "      JButton button = new JButton();",
        "      add(button, 'cell 0 0');",
        "    }",
        "    add(new JButton(C_1), 'cell 1 2');",
        "  }",
        "}");
    assertCellBounds(newButton, 0, 0, 1, 1);
  }

  /**
   * Insert new column/row: horizontally spanned component.
   */
  public void test_CREATE_insertColumnRow_2() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "    add(new JButton(C_1), 'cell 0 0');",
            "    add(new JButton(C_2), 'cell 1 1');",
            "    add(new JButton(C_3), 'cell 0 2 2 1');",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    //
    ComponentInfo newButton = createJButton();
    layout.command_CREATE(newButton, 1, true, 3, false);
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[][][]', '[][][][]'));",
        "    add(new JButton(C_1), 'cell 0 0');",
        "    add(new JButton(C_2), 'cell 2 1');",
        "    add(new JButton(C_3), 'cell 0 2 3 1');",
        "    {",
        "      JButton button = new JButton();",
        "      add(button, 'cell 1 3');",
        "    }",
        "  }",
        "}");
    assertCellBounds(newButton, 1, 3, 1, 1);
  }

  /**
   * Insert new column/row: vertically spanned component.
   */
  public void test_CREATE_insertColumnRow_3() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "    add(new JButton(C_1), 'cell 0 0');",
            "    add(new JButton(C_2), 'cell 1 1');",
            "    add(new JButton(C_3), 'cell 2 0 1 2');",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    //
    ComponentInfo newButton = createJButton();
    layout.command_CREATE(newButton, 3, false, 1, true);
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[][][][]', '[][][]'));",
        "    add(new JButton(C_1), 'cell 0 0');",
        "    {",
        "      JButton button = new JButton();",
        "      add(button, 'cell 3 1');",
        "    }",
        "    add(new JButton(C_2), 'cell 1 2');",
        "    add(new JButton(C_3), 'cell 2 0 1 3');",
        "  }",
        "}");
    assertCellBounds(newButton, 3, 1, 1, 1);
  }

  /**
   * Insert new column/row: don't touch docked components.
   */
  public void test_CREATE_insertColumnRow_4() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "    add(new JButton(C_1), 'north');",
            "    add(new JButton(C_2), 'cell 0 0');",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    //
    ComponentInfo newButton = createJButton();
    layout.command_CREATE(newButton, 0, true, 0, true);
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[][]', '[][]'));",
        "    add(new JButton(C_1), 'north');",
        "    {",
        "      JButton button = new JButton();",
        "      add(button, 'cell 0 0');",
        "    }",
        "    add(new JButton(C_2), 'cell 1 1');",
        "  }",
        "}");
    assertCellBounds(newButton, 0, 0, 1, 1);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE/ADD
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link MigLayoutInfo#command_MOVE(ComponentInfo, int, boolean, int, boolean)}.<br>
   * Just move single not spanned component.
   */
  public void test_MOVE_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "    {",
            "      JButton button = new JButton(C_1);",
            "      add(button, 'cell 0 0');",
            "    }",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    ComponentInfo button = panel.getChildrenComponents().get(0);
    //
    layout.command_MOVE(button, 1, false, 1, false);
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[][]', '[][]'));",
        "    {",
        "      JButton button = new JButton(C_1);",
        "      add(button, 'cell 1 1');",
        "    }",
        "  }",
        "}");
    assertCellBounds(button, 1, 1, 1, 1);
  }

  /**
   * Test for {@link MigLayoutInfo#command_MOVE(ComponentInfo, int, boolean, int, boolean)}.<br>
   * Move spanned component - size set to 1x1.
   */
  public void test_MOVE_2() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "    add(new JButton(C_1), 'cell 0 0');",
            "    add(new JButton(C_2), 'cell 1 1');",
            "    {",
            "      JButton button = new JButton(C_3);",
            "      add(button, 'cell 0 2 2 1');",
            "    }",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    ComponentInfo button_3 = panel.getChildrenComponents().get(2);
    //
    layout.command_MOVE(button_3, 0, false, 1, false);
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout());",
        "    add(new JButton(C_1), 'cell 0 0');",
        "    {",
        "      JButton button = new JButton(C_3);",
        "      add(button, 'cell 0 1');",
        "    }",
        "    add(new JButton(C_2), 'cell 1 1');",
        "  }",
        "}");
    assertCellBounds(button_3, 0, 1, 1, 1);
  }

  /**
   * Test for {@link MigLayoutInfo#command_MOVE(ComponentInfo, int, boolean, int, boolean)}.<br>
   * Move should force <code>cell</code> tag.
   */
  public void test_MOVE_3() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "    {",
            "      JButton button = new JButton(C_1);",
            "      add(button, 'wrap');",
            "    }",
            "    {",
            "      JButton button = new JButton(C_2);",
            "      add(button, 'skip');",
            "    }",
            "    {",
            "      JButton button = new JButton(C_3);",
            "      add(button, '');",
            "    }",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    ComponentInfo button_1 = panel.getChildrenComponents().get(0);
    ComponentInfo button_2 = panel.getChildrenComponents().get(1);
    ComponentInfo button_3 = panel.getChildrenComponents().get(2);
    // check initial bounds
    assertCellBounds(button_1, 0, 0, 1, 1);
    assertCellBounds(button_2, 1, 1, 1, 1);
    assertCellBounds(button_3, 2, 1, 1, 1);
    //
    layout.command_MOVE(button_2, 1, false, 2, false);
    assertCellBounds(button_1, 0, 0, 1, 1);
    assertCellBounds(button_2, 1, 2, 1, 1);
    assertCellBounds(button_3, 2, 1, 1, 1);
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[][][]', '[][][]'));",
        "    {",
        "      JButton button = new JButton(C_1);",
        "      add(button, 'cell 0 0');",
        "    }",
        "    {",
        "      JButton button = new JButton(C_3);",
        "      add(button, 'cell 2 1');",
        "    }",
        "    {",
        "      JButton button = new JButton(C_2);",
        "      add(button, 'cell 1 2');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link MigLayoutInfo#command_MOVE(ComponentInfo, int, boolean, int, boolean)}.<br>
   * Move docked component, should remove dock.
   */
  public void test_MOVE_4() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "    {",
            "      JButton button = new JButton(C_1);",
            "      add(button, 'north');",
            "    }",
            "    {",
            "      JButton button = new JButton(C_2);",
            "      add(button, 'cell 0 0');",
            "    }",
            "    {",
            "      JButton button = new JButton(C_3);",
            "      add(button, 'cell 1 1');",
            "    }",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    // prepare button
    ComponentInfo button = panel.getChildrenComponents().get(0);
    CellConstraintsSupport constraints = MigLayoutInfo.getConstraints(button);
    // initially docked
    assertSame(CellConstraintsSupport.DockSide.NORTH, constraints.getDockSide());
    // move to (0,1)
    layout.command_MOVE(button, 0, false, 1, false);
    assertSame(null, constraints.getDockSide());
    assertCellBounds(button, 0, 1, 1, 1);
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout());",
        "    {",
        "      JButton button = new JButton(C_2);",
        "      add(button, 'cell 0 0');",
        "    }",
        "    {",
        "      JButton button = new JButton(C_1);",
        "      add(button, 'cell 0 1');",
        "    }",
        "    {",
        "      JButton button = new JButton(C_3);",
        "      add(button, 'cell 1 1');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link MigLayoutInfo#command_ADD(ComponentInfo, int, boolean, int, boolean)}.<br>
   * Move single component.
   */
  public void test_ADD() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "    {",
            "      JPanel panel = new JPanel();",
            "      add(panel, 'cell 0 0');",
            "      {",
            "        JButton button = new JButton(C_1);",
            "        panel.add(button);",
            "      }",
            "    }",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    ContainerInfo innerPanel = (ContainerInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button = innerPanel.getChildrenComponents().get(0);
    //
    layout.command_MOVE(button, 0, true, 0, true);
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[][]', '[][]'));",
        "    {",
        "      JButton button = new JButton(C_1);",
        "      add(button, 'cell 0 0');",
        "    }",
        "    {",
        "      JPanel panel = new JPanel();",
        "      add(panel, 'cell 1 1');",
        "    }",
        "  }",
        "}");
    assertCellBounds(button, 0, 0, 1, 1);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Split support
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link MigLayoutInfo#getCellComponents(int, int)}.
   */
  public void test_getCellComponents() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "    add(new JButton(C_1), 'cell 0 0');",
            "    add(new JButton(C_2), 'cell 0 0');",
            "    add(new JButton(C_3), 'cell 1 0');",
            "    add(new JButton(C_4), 'cell 1 1');",
            "    add(new JButton(C_5), 'cell 0 2 2 1');",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    // prepare buttons
    ComponentInfo button_1 = panel.getChildrenComponents().get(0);
    ComponentInfo button_2 = panel.getChildrenComponents().get(1);
    ComponentInfo button_3 = panel.getChildrenComponents().get(2);
    ComponentInfo button_4 = panel.getChildrenComponents().get(3);
    ComponentInfo button_5 = panel.getChildrenComponents().get(4);
    //
    assertThat(layout.getCellComponents(0, 0)).isEqualTo(ImmutableList.of(button_1, button_2));
    assertThat(layout.getCellComponents(1, 0)).isEqualTo(ImmutableList.of(button_3));
    assertThat(layout.getCellComponents(1, 1)).isEqualTo(ImmutableList.of(button_4));
    assertThat(layout.getCellComponents(0, 2)).isEqualTo(ImmutableList.of(button_5));
    assertThat(layout.getCellComponents(1, 2)).isEqualTo(ImmutableList.of(button_5));
    assertThat(layout.getCellComponents(2, 2)).isEmpty();
  }

  /**
   * Test for {@link CellConstraintsSupport#isHorizontalSplit()}. <br>
   * Explicit "flowx" for component.
   */
  public void test_isHorizontalSplit_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "    add(new JButton(C_1), 'cell 0 0,flowx');",
            "    add(new JButton(C_2), 'cell 0 0');",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button_1 = panel.getChildrenComponents().get(0);
    //
    assertTrue(MigLayoutInfo.getConstraints(button_1).isHorizontalSplit());
  }

  /**
   * Test for {@link CellConstraintsSupport#isHorizontalSplit()}. <br>
   * Explicit "flowy" for component.
   */
  public void test_isHorizontalSplit_2() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "    add(new JButton(C_1), 'cell 0 0,flowy');",
            "    add(new JButton(C_2), 'cell 0 0');",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button_1 = panel.getChildrenComponents().get(0);
    //
    assertFalse(MigLayoutInfo.getConstraints(button_1).isHorizontalSplit());
  }

  /**
   * Test for {@link CellConstraintsSupport#isHorizontalSplit()}. <br>
   * Implicit "flowx" from {@link LC}.
   */
  public void test_isHorizontalSplit_3() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "    add(new JButton(C_1), 'cell 0 0');",
            "    add(new JButton(C_2), 'cell 0 0');",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button_1 = panel.getChildrenComponents().get(0);
    //
    assertTrue(MigLayoutInfo.getConstraints(button_1).isHorizontalSplit());
  }

  /**
   * Test for {@link CellConstraintsSupport#isHorizontalSplit()}.<br>
   * Explicit "flowy" from {@link LC}.
   */
  public void test_isHorizontalSplit_4() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout(new LC().flowY()));",
            "    add(new JButton(C_1), 'cell 0 0');",
            "    add(new JButton(C_2), 'cell 0 0');",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button_1 = panel.getChildrenComponents().get(0);
    //
    assertFalse(MigLayoutInfo.getConstraints(button_1).isHorizontalSplit());
  }

  /**
   * Test for
   * {@link MigLayoutInfo#command_splitCREATE(int, int, boolean, ComponentInfo, ComponentInfo)}.<br>
   * Explicit horizontal split.
   */
  public void test_splitCREATE_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "    add(new JButton(C_1), 'cell 0 0');",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    ComponentInfo button_1 = panel.getChildrenComponents().get(0);
    //
    ComponentInfo newButton = createJButton();
    layout.command_splitCREATE(0, 0, true, newButton, button_1);
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout());",
        "    {",
        "      JButton button = new JButton();",
        "      add(button, 'flowx,cell 0 0');",
        "    }",
        "    add(new JButton(C_1), 'cell 0 0');",
        "  }",
        "}");
  }

  /**
   * Test for
   * {@link MigLayoutInfo#command_splitCREATE(int, int, boolean, ComponentInfo, ComponentInfo)}.<br>
   * Explicit vertical split.
   */
  public void test_splitCREATE_2() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "    add(new JButton(C_1), 'cell 0 0');",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    //
    ComponentInfo newButton = createJButton();
    layout.command_splitCREATE(0, 0, false, newButton, null);
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout());",
        "    add(new JButton(C_1), 'flowy,cell 0 0');",
        "    {",
        "      JButton button = new JButton();",
        "      add(button, 'cell 0 0');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for
   * {@link MigLayoutInfo#command_splitCREATE(int, int, boolean, ComponentInfo, ComponentInfo)}.<br>
   * Existing vertical split, so request for horizontal split is ignored.
   */
  public void test_splitCREATE_3() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "    add(new JButton(C_1), 'flowy,cell 0 0');",
            "    add(new JButton(C_2), 'cell 0 0');",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    //
    ComponentInfo newButton = createJButton();
    layout.command_splitCREATE(0, 0, false, newButton, null);
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout());",
        "    add(new JButton(C_1), 'flowy,cell 0 0');",
        "    add(new JButton(C_2), 'cell 0 0');",
        "    {",
        "      JButton button = new JButton();",
        "      add(button, 'cell 0 0');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for
   * {@link MigLayoutInfo#command_splitMOVE(int, int, boolean, ComponentInfo, ComponentInfo)}.<br>
   * Move inside same cell.
   */
  public void test_splitMOVE_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "    {",
            "      JButton button = new JButton(C_1);",
            "      add(button, 'flowy,cell 0 0');",
            "    }",
            "    {",
            "      JButton button = new JButton(C_2);",
            "      add(button, 'cell 0 0');",
            "    }",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    ComponentInfo button_1 = panel.getChildrenComponents().get(0);
    ComponentInfo button_2 = panel.getChildrenComponents().get(1);
    //
    layout.command_splitMOVE(0, 0, true, button_2, button_1);
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout());",
        "    {",
        "      JButton button = new JButton(C_2);",
        "      add(button, 'flowy,cell 0 0');",
        "    }",
        "    {",
        "      JButton button = new JButton(C_1);",
        "      add(button, 'cell 0 0');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for
   * {@link MigLayoutInfo#command_splitMOVE(int, int, boolean, ComponentInfo, ComponentInfo)}.<br>
   * Move from different cell.
   */
  public void test_splitMOVE_2() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "    {",
            "      JButton button = new JButton(C_1);",
            "      add(button, 'cell 0 0');",
            "    }",
            "    {",
            "      JButton button = new JButton(C_2);",
            "      add(button, 'cell 1 0');",
            "    }",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    ComponentInfo button_2 = panel.getChildrenComponents().get(1);
    //
    layout.command_splitMOVE(0, 0, true, button_2, null);
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout());",
        "    {",
        "      JButton button = new JButton(C_1);",
        "      add(button, 'flowx,cell 0 0');",
        "    }",
        "    {",
        "      JButton button = new JButton(C_2);",
        "      add(button, 'cell 0 0');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for move first component in splitted cell to the different cell.
   */
  public void test_splitMOVE_3() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "    {",
            "      JButton button = new JButton(C_1);",
            "      add(button, 'flowx,cell 0 0');",
            "    }",
            "    {",
            "      JButton button = new JButton(C_2);",
            "      add(button, 'cell 0 0');",
            "    }",
            "    {",
            "      JButton button = new JButton(C_3);",
            "      add(button, 'cell 0 0');",
            "    }",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    ComponentInfo button_1 = panel.getChildrenComponents().get(0);
    //
    layout.command_MOVE(button_1, 1, false, 0, false);
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[][]', '[]'));",
        "    {",
        "      JButton button = new JButton(C_2);",
        "      add(button, 'flowx,cell 0 0');",
        "    }",
        "    {",
        "      JButton button = new JButton(C_3);",
        "      add(button, 'cell 0 0');",
        "    }",
        "    {",
        "      JButton button = new JButton(C_1);",
        "      add(button, 'cell 1 0');",
        "    }",
        "  }",
        "}");
  }

  /**
   * If delete first component, then flow direction specification should be moved to next component.
   */
  public void test_splitDELETE_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "    {",
            "      JButton button = new JButton(C_1);",
            "      add(button, 'flowx,cell 0 0');",
            "    }",
            "    {",
            "      JButton button = new JButton(C_2);",
            "      add(button, 'cell 0 0');",
            "    }",
            "    {",
            "      JButton button = new JButton(C_3);",
            "      add(button, 'cell 0 0');",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button_1 = panel.getChildrenComponents().get(0);
    //
    button_1.delete();
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout());",
        "    {",
        "      JButton button = new JButton(C_2);",
        "      add(button, 'flowx,cell 0 0');",
        "    }",
        "    {",
        "      JButton button = new JButton(C_3);",
        "      add(button, 'cell 0 0');",
        "    }",
        "  }",
        "}");
  }

  /**
   * If delete first component, then flow direction specification should be moved to next component.<br>
   * Only one component left in splitted cell, so remove flow direction at all.
   */
  public void test_splitDELETE_2() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "    {",
            "      JButton button = new JButton(C_1);",
            "      add(button, 'flowx,cell 0 0');",
            "    }",
            "    {",
            "      JButton button = new JButton(C_2);",
            "      add(button, 'cell 0 0');",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button_1 = panel.getChildrenComponents().get(0);
    //
    button_1.delete();
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout());",
        "    {",
        "      JButton button = new JButton(C_2);",
        "      add(button, 'cell 0 0');",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Docking
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_dock_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "    add(new JButton(C_1), 'dock north');",
            "    add(new JButton(C_2), 'west');",
            "    add(new JButton('long text'), '');",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    // check constraints
    {
      ComponentInfo button_3 = panel.getChildrenComponents().get(2);
      assertCellBounds(button_3, 0, 0, 1, 1);
    }
    // check dimensions
    {
      List<MigColumnInfo> columns = layout.getColumns();
      assertEquals(1, columns.size());
    }
    {
      List<MigRowInfo> rows = layout.getRows();
      assertEquals(1, rows.size());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Gaps
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_gaps_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout('', '[]10px[]20px[]', '[]'));",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    // prepare columns
    List<MigColumnInfo> columns = layout.getColumns();
    MigColumnInfo column_0 = columns.get(0);
    MigColumnInfo column_1 = columns.get(1);
    MigColumnInfo column_2 = columns.get(2);
    // getGap[Before,After]
    {
      assertEquals("[]", column_0.getString(true));
      assertEquals(null, column_0.getString(column_0.getGapBefore()));
      assertEquals("10px", column_0.getString(column_0.getGapAfter()));
    }
    {
      assertEquals("[]", column_1.getString(true));
      assertEquals("10px", column_1.getString(column_1.getGapBefore()));
      assertEquals("20px", column_1.getString(column_1.getGapAfter()));
    }
    {
      assertEquals("[]", column_2.getString(true));
      assertEquals("20px", column_2.getString(column_2.getGapBefore()));
      assertEquals(null, column_2.getString(column_2.getGapAfter()));
    }
    // setGapBefore()
    {
      column_1.setGapBefore("5mm");
      assertEquals("5mm", column_1.getString(column_1.getGapBefore()));
      assertEquals("5mm", column_0.getString(column_0.getGapAfter()));
      layout.writeDimensions();
      assertEditor(
          "public class Test extends JPanel implements IConstants {",
          "  public Test() {",
          "    setLayout(new MigLayout('', '[]5mm[]20px[]', '[]'));",
          "  }",
          "}");
    }
    // setGapAfter()
    {
      column_1.setGapAfter("10mm");
      assertEquals("10mm", column_1.getString(column_1.getGapAfter()));
      assertEquals("10mm", column_2.getString(column_2.getGapBefore()));
      layout.writeDimensions();
      assertEditor(
          "public class Test extends JPanel implements IConstants {",
          "  public Test() {",
          "    setLayout(new MigLayout('', '[]5mm[]10mm[]', '[]'));",
          "  }",
          "}");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Column commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link MigLayoutInfo#insertColumn(int)}.
   */
  public void test_column_INSERT() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout('', '[100px][200px]', ''));",
            "    add(new JButton(C_1), 'cell 0 0');",
            "    add(new JButton(C_2), 'cell 0 1 2 1');",
            "    add(new JButton(C_3), 'cell 1 2');",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    //
    layout.insertColumn(1);
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[100px][][200px]', '[][][]'));",
        "    add(new JButton(C_1), 'cell 0 0');",
        "    add(new JButton(C_2), 'cell 0 1 3 1');",
        "    add(new JButton(C_3), 'cell 2 2');",
        "  }",
        "}");
  }

  /**
   * Test for {@link MigLayoutInfo#deleteColumn(int)}.
   */
  public void test_column_DELETE() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout('', '[100px][150px][200px]', '[][][]'));",
            "    add(new JButton(C_1), 'cell 0 0');",
            "    add(new JButton(C_2), 'cell 0 1 3 1');",
            "    add(new JButton(C_3), 'cell 2 2');",
            "    add(new JButton(C_4), 'cell 1 0');",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    //
    layout.deleteColumn(1);
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[100px][200px]', '[][][]'));",
        "    add(new JButton(C_1), 'cell 0 0');",
        "    add(new JButton(C_2), 'cell 0 1 2 1');",
        "    add(new JButton(C_3), 'cell 1 2');",
        "  }",
        "}");
  }

  /**
   * Test for {@link MigLayoutInfo#clearColumn(int)}.
   */
  public void test_column_CLEAR() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout('', '[100px][150px][200px]', '[][][]'));",
            "    add(new JButton(C_1), 'cell 0 0');",
            "    add(new JButton(C_2), 'cell 0 1 3 1');",
            "    add(new JButton(C_3), 'cell 2 2');",
            "    add(new JButton(C_4), 'cell 1 0');",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    //
    layout.clearColumn(1);
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[100px][150px][200px]', '[][][]'));",
        "    add(new JButton(C_1), 'cell 0 0');",
        "    add(new JButton(C_2), 'cell 0 1 3 1');",
        "    add(new JButton(C_3), 'cell 2 2');",
        "  }",
        "}");
  }

  /**
   * Test for {@link MigLayoutInfo#splitColumn(int)}.
   */
  public void test_column_SPLIT() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout('', '[100px][200px][300px]', '[][][]'));",
            "    add(new JButton(C_1), 'cell 0 0');",
            "    add(new JButton(C_2), 'cell 1 1');",
            "    add(new JButton(C_3), 'cell 2 2');",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    //
    layout.splitColumn(1);
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[100px][200px][200px][300px]', '[][][]'));",
        "    add(new JButton(C_1), 'cell 0 0');",
        "    add(new JButton(C_2), 'cell 1 1 2 1');",
        "    add(new JButton(C_3), 'cell 3 2');",
        "  }",
        "}");
  }

  /**
   * Test for {@link MigLayoutInfo#moveColumn(int, int)}.
   * 
   * <pre>
	 *   1....
	 *   22...
	 *   ...3.
	 *   ..444
	 *   ....5
	 * </pre>
   * 
   * into
   * 
   * <pre>
	 *   1....
	 *   222...
	 *   .3...
	 *   ...44
	 *   ....5
	 * </pre>
   */
  public void test_column_MOVE_backward() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout('', '[10px][20px][30px][40px][50px]', '[][][][][]'));",
            "    add(new JButton(C_1), 'cell 0 0');",
            "    add(new JButton(C_2), 'cell 0 1 2 1');",
            "    add(new JButton(C_3), 'cell 3 2');",
            "    add(new JButton(C_4), 'cell 2 3 3 1');",
            "    add(new JButton(C_5), 'cell 4 4');",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    //
    layout.moveColumn(3, 1);
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[10px][40px][20px][30px][50px]', '[][][][][]'));",
        "    add(new JButton(C_1), 'cell 0 0');",
        "    add(new JButton(C_2), 'cell 0 1 3 1');",
        "    add(new JButton(C_3), 'cell 1 2');",
        "    add(new JButton(C_4), 'cell 3 3 2 1');",
        "    add(new JButton(C_5), 'cell 4 4');",
        "  }",
        "}");
  }

  /**
   * Test for {@link MigLayoutInfo#moveColumn(int, int)}.
   * 
   * <pre>
	 *   1....
	 *   222...
	 *   .3...
	 *   ...44
	 *   ....5
	 * </pre>
   * 
   * into
   * 
   * <pre>
	 *   1....
	 *   22...
	 *   ...3.
	 *   ..444
	 *   ....5
	 * </pre>
   */
  public void test_column_MOVE_forward() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout('', '[10px][40px][20px][30px][50px]', '[][][][][]'));",
            "    add(new JButton(C_1), 'cell 0 0');",
            "    add(new JButton(C_2), 'cell 0 1 3 1');",
            "    add(new JButton(C_3), 'cell 1 2');",
            "    add(new JButton(C_4), 'cell 3 3 2 1');",
            "    add(new JButton(C_5), 'cell 4 4');",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    //
    layout.moveColumn(1, 4);
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[10px][20px][30px][40px][50px]', '[][][][][]'));",
        "    add(new JButton(C_1), 'cell 0 0');",
        "    add(new JButton(C_2), 'cell 0 1 2 1');",
        "    add(new JButton(C_3), 'cell 3 2');",
        "    add(new JButton(C_4), 'cell 2 3 3 1');",
        "    add(new JButton(C_5), 'cell 4 4');",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Row commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link MigLayoutInfo#insertRow(int)}.
   * 
   * <pre>
	 *   12.
	 *   .23
	 * </pre>
   * 
   * <pre>
	 *   12.
	 *   *2*
	 *   .23
	 * </pre>
   */
  public void test_row_INSERT() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout('', '[][][]', '[100px][200px]'));",
            "    add(new JButton(C_1), 'cell 0 0');",
            "    add(new JButton(C_2), 'cell 1 0 1 2');",
            "    add(new JButton(C_3), 'cell 2 1');",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    //
    layout.insertRow(1);
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[][][]', '[100px][][200px]'));",
        "    add(new JButton(C_1), 'cell 0 0');",
        "    add(new JButton(C_2), 'cell 1 0 1 3');",
        "    add(new JButton(C_3), 'cell 2 2');",
        "  }",
        "}");
  }

  /**
   * Test for {@link MigLayoutInfo#deleteRow(int)}.
   * 
   * <pre>
	 *   12.
	 *   .24
	 *   .23
	 * </pre>
   * 
   * <pre>
	 *   12.
	 *   .23
	 * </pre>
   */
  public void test_row_DELETE() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout('', '[][][]', '[100px][][200px]'));",
            "    add(new JButton(C_1), 'cell 0 0');",
            "    add(new JButton(C_2), 'cell 1 0 1 3');",
            "    add(new JButton(C_3), 'cell 2 2');",
            "    add(new JButton(C_4), 'cell 2 1');",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    //
    layout.deleteRow(1);
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[][][]', '[100px][200px]'));",
        "    add(new JButton(C_1), 'cell 0 0');",
        "    add(new JButton(C_2), 'cell 1 0 1 2');",
        "    add(new JButton(C_3), 'cell 2 1');",
        "  }",
        "}");
  }

  /**
   * Test for {@link MigLayoutInfo#clearRow(int)}.
   * 
   * <pre>
	 *   12.
	 *   .24
	 *   .23
	 * </pre>
   * 
   * <pre>
	 *   12.
	 *   .2.
	 *   .23
	 * </pre>
   */
  public void test_row_CLEAR() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout('', '[][][]', '[100px][200px][300px]'));",
            "    add(new JButton(C_1), 'cell 0 0');",
            "    add(new JButton(C_2), 'cell 1 0 1 3');",
            "    add(new JButton(C_3), 'cell 2 2');",
            "    add(new JButton(C_4), 'cell 2 1');",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    //
    layout.clearRow(1);
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[][][]', '[100px][200px][300px]'));",
        "    add(new JButton(C_1), 'cell 0 0');",
        "    add(new JButton(C_2), 'cell 1 0 1 3');",
        "    add(new JButton(C_3), 'cell 2 2');",
        "  }",
        "}");
  }

  /**
   * Test for {@link MigLayoutInfo#splitRow(int)}.
   * 
   * <pre>
	 *   1..
	 *   .2.
	 *   ..3
	 * </pre>
   * 
   * <pre>
	 *   1..
	 *   .2.
	 *   .2.
	 *   ..3
	 * </pre>
   */
  public void test_row_SPLIT() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout('', '[][][]', '[100px][200px][300px]'));",
            "    add(new JButton(C_1), 'cell 0 0');",
            "    add(new JButton(C_2), 'cell 1 1');",
            "    add(new JButton(C_3), 'cell 2 2');",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    //
    layout.splitRow(1);
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[][][]', '[100px][200px][200px][300px]'));",
        "    add(new JButton(C_1), 'cell 0 0');",
        "    add(new JButton(C_2), 'cell 1 1 1 2');",
        "    add(new JButton(C_3), 'cell 2 3');",
        "  }",
        "}");
  }

  /**
   * Test for {@link MigLayoutInfo#moveRow(int, int)}.
   * 
   * <pre>
	 *   12...
	 *   .2...
	 *   ...4.
	 *   ..34.
	 *   ...45
	 * </pre>
   * 
   * <pre>
	 *   1....
	 *   222...
	 *   .3...
	 *   ...44
	 *   ....5
	 * </pre>
   */
  public void test_row_MOVE_backward() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout('', '[][][][][]', '[10px][20px][30px][40px][50px]'));",
            "    add(new JButton(C_1), 'cell 0 0');",
            "    add(new JButton(C_2), 'cell 1 0 1 2');",
            "    add(new JButton(C_3), 'cell 2 3');",
            "    add(new JButton(C_4), 'cell 3 2 1 3');",
            "    add(new JButton(C_5), 'cell 4 4');",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    //
    layout.moveRow(3, 1);
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[][][][][]', '[10px][40px][20px][30px][50px]'));",
        "    add(new JButton(C_1), 'cell 0 0');",
        "    add(new JButton(C_2), 'cell 1 0 1 3');",
        "    add(new JButton(C_3), 'cell 2 1');",
        "    add(new JButton(C_4), 'cell 3 3 1 2');",
        "    add(new JButton(C_5), 'cell 4 4');",
        "  }",
        "}");
  }

  /**
   * Test for {@link MigLayoutInfo#moveRow(int, int)}.
   * 
   * <pre>
	 *   1....
	 *   222...
	 *   .3...
	 *   ...44
	 *   ....5
	 * </pre>
   * 
   * <pre>
	 *   12...
	 *   .2...
	 *   ...4.
	 *   ..34.
	 *   ...45
	 * </pre>
   */
  public void test_row_MOVE_forward() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout('', '[][][][][]', '[10px][40px][20px][30px][50px]'));",
            "    add(new JButton(C_1), 'cell 0 0');",
            "    add(new JButton(C_2), 'cell 1 0 1 3');",
            "    add(new JButton(C_3), 'cell 2 1');",
            "    add(new JButton(C_4), 'cell 3 3 1 2');",
            "    add(new JButton(C_5), 'cell 4 4');",
            "  }",
            "}");
    panel.refresh();
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    //
    layout.moveRow(1, 4);
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[][][][][]', '[10px][20px][30px][40px][50px]'));",
        "    add(new JButton(C_1), 'cell 0 0');",
        "    add(new JButton(C_2), 'cell 1 0 1 2');",
        "    add(new JButton(C_3), 'cell 2 3');",
        "    add(new JButton(C_4), 'cell 3 2 1 3');",
        "    add(new JButton(C_5), 'cell 4 4');",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // canChangeDimensions()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link MigLayoutInfo#canChangeDimensions()}.
   */
  public void test_canChangeDimensions_constructor() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "  }",
            "}");
    //
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    assertTrue(layout.canChangeDimensions());
  }

  /**
   * Test for {@link MigLayoutInfo#canChangeDimensions()}.
   */
  public void test_canChangeDimensions_implicit() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends JPanel {",
            "  public MyPanel() {",
            "    setLayout(new MigLayout());",
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
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    assertFalse(layout.canChangeDimensions());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // UI
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test {@link DimensionsProperty} for columns/rows.
   */
  public void test_DimensionsProperty() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new MigLayout('', '[10mm:3cm:3in][left]', '[100px,top][]'));",
            "  }",
            "}");
    MigLayoutInfo layout = (MigLayoutInfo) panel.getLayout();
    panel.refresh();
    // columnSpecs
    {
      Property property = layout.getPropertyByTitle("columnSpecs");
      assertSame(Property.UNKNOWN_VALUE, property.getValue());
      assertTrue(property.isModified());
      property.setValue(this); // ignored
      // check text in TextDisplayPropertyEditor
      assertEquals("[10mm:3cm:3in][left]", getPropertyText(property));
    }
    // rowSpecs
    {
      Property property = layout.getPropertyByTitle("rowSpecs");
      assertSame(Property.UNKNOWN_VALUE, property.getValue());
      assertTrue(property.isModified());
      property.setValue(this); // ignored
      // check text in TextDisplayPropertyEditor
      assertEquals("[100px,top][]", getPropertyText(property));
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
            "    setLayout(new MigLayout('', '[10mm:3cm:3in][left]', '[100px,top][]'));",
            "  }",
            "}");
    // check for actions
    MenuManager menuManager = getDesignerMenuManager();
    panel.getBroadcastObject().addContextMenu(ImmutableList.of(panel), panel, menuManager);
    assertNotNull(findChildAction(menuManager, "Edit c&olumns..."));
    assertNotNull(findChildAction(menuManager, "Edit &rows..."));
  }
}
