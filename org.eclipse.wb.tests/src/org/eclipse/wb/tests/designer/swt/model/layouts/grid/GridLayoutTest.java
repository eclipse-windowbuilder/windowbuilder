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
package org.eclipse.wb.tests.designer.swt.model.layouts.grid;

import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Interval;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.model.jface.viewer.ViewerInfo;
import org.eclipse.wb.internal.swt.model.layout.RowLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.grid.GridColumnInfo;
import org.eclipse.wb.internal.swt.model.layout.grid.GridDataInfo;
import org.eclipse.wb.internal.swt.model.layout.grid.GridImages;
import org.eclipse.wb.internal.swt.model.layout.grid.GridLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.grid.GridRowInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.model.widgets.TableInfo;
import org.eclipse.wb.tests.designer.rcp.BTestUtils;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;
import org.eclipse.wb.tests.designer.swt.model.jface.ViewerTest;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link GridLayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class GridLayoutTest extends RcpModelTest {
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
    assertNotNull(GridImages.getImage("h/left.gif"));
    assertNotNull(GridImages.getImageDescriptor("v/top.gif"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for parsing empty {@link GridLayoutInfo}.
   */
  public void test_parseEmpty() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(1, false));",
            "  }",
            "}");
    shell.refresh();
  }

  /**
   * There was problem that sometimes we try to access <code>GridData2</code> and expect at this
   * time only {@link GridData} from {@link Control#getLayoutData()}. We should also check if it was
   * not already replaced with <code>GridData2</code>.
   */
  public void test_doubleConvertTo_GridData2() throws Exception {
    parseComposite(
        "class Test extends Composite {",
        "  Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setLayout(new GridLayout());",
        "    new Button(this, SWT.NONE);",
        "  }",
        "  public Point computeSize(int wHint, int hHint, boolean changed) {",
        "    return super.computeSize(600, 490, false);",
        "  }",
        "}");
    refresh();
    assertNoErrors(m_lastParseInfo);
  }

  /**
   * Fillers should be filtered out from presentation children.
   */
  public void test_excludeFillersFromPresentationChildren_1() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(2, false));",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "    }",
            "    new Label(this, SWT.NONE);",
            "  }",
            "}");
    shell.refresh();
    ControlInfo button = shell.getChildrenControls().get(0);
    ControlInfo filler = shell.getChildrenControls().get(1);
    //
    IObjectPresentation presentation = shell.getPresentation();
    {
      List<ObjectInfo> presentationChildren = presentation.getChildrenTree();
      assertThat(presentationChildren).contains(button).doesNotContain(filler);
    }
    {
      List<ObjectInfo> presentationChildren = presentation.getChildrenGraphical();
      assertThat(presentationChildren).contains(button).doesNotContain(filler);
    }
  }

  /**
   * When we create {@link Label} using instance factory, it is not filler.
   */
  public void test_excludeFillersFromPresentationChildren_2() throws Exception {
    setFileContentSrc(
        "test/MyFactory.java",
        getTestSource(
            "public class MyFactory {",
            "  public Label createLabel(Composite parent) {",
            "    return new Label(parent, SWT.NONE);",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  private final MyFactory factory = new MyFactory();",
            "  Test() {",
            "    setLayout(new GridLayout());",
            "    factory.createLabel(this);",
            "  }",
            "}");
    shell.refresh();
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(new GridLayout())/ /factory.createLabel(this)/}",
        "  {new: org.eclipse.swt.layout.GridLayout} {empty} {/setLayout(new GridLayout())/}",
        "  {instance factory: {field-initializer: factory} createLabel(org.eclipse.swt.widgets.Composite)} {empty} {/factory.createLabel(this)/}",
        "    {virtual-layout_data: org.eclipse.swt.layout.GridData} {virtual-layout-data} {}",
        "  {instance factory container}",
        "    {new: test.MyFactory} {field-initializer: factory} {/new MyFactory()/ /factory.createLabel(this)/}");
    ControlInfo label = shell.getChildrenControls().get(0);
    // factory created "label" is visible
    IObjectPresentation presentation = shell.getPresentation();
    {
      List<ObjectInfo> presentationChildren = presentation.getChildrenTree();
      assertThat(presentationChildren).contains(label);
    }
  }

  /**
   * <code>Label</code> with <code>setText()</code> is not filler, it is normal control.
   */
  public void test_fillersWith_setText() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(1, false));",
            "    new Label(this, SWT.NONE).setText('txt');",
            "  }",
            "}");
    shell.refresh();
    ControlInfo label = shell.getChildrenControls().get(0);
    //
    IObjectPresentation presentation = shell.getPresentation();
    {
      List<ObjectInfo> presentationChildren = presentation.getChildrenTree();
      assertThat(presentationChildren).contains(label);
    }
    {
      List<ObjectInfo> presentationChildren = presentation.getChildrenGraphical();
      assertThat(presentationChildren).contains(label);
    }
  }

  public void test_fillersWithout_setText() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(1, false));",
            "    new Label(this, SWT.NONE);",
            "  }",
            "}");
    shell.refresh();
    ControlInfo label = shell.getChildrenControls().get(0);
    //
    IObjectPresentation presentation = shell.getPresentation();
    {
      List<ObjectInfo> presentationChildren = presentation.getChildrenTree();
      assertThat(presentationChildren).doesNotContain(label);
    }
    {
      List<ObjectInfo> presentationChildren = presentation.getChildrenGraphical();
      assertThat(presentationChildren).doesNotContain(label);
    }
  }

  /**
   * No controls, no changes expected.
   */
  public void test_fixGrid_noControls() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new GridLayout());",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    // do "fix"
    ReflectionUtils.invokeMethod(layout, "fixGrid()");
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new GridLayout());",
        "  }",
        "}");
  }

  /**
   * It is possible that there are existing {@link Control}-s created in superclass. We should not
   * try to "fix" this by adding fillers.
   */
  public void test_fixGrid_leadingImplicitControls() throws Exception {
    setFileContentSrc(
        "test/MyShell.java",
        getTestSource(
            "public class MyShell extends Shell {",
            "  public MyShell() {",
            "    setLayout(new GridLayout());",
            "    new Text(this, SWT.NONE);",
            "  }",
            "  protected void checkSubclass() {",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    CompositeInfo shell =
        parseComposite(
            "public class Test extends MyShell {",
            "  public Test() {",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    // do "fix"
    ReflectionUtils.invokeMethod(layout, "fixGrid()");
    assertEditor(
        "public class Test extends MyShell {",
        "  public Test() {",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link IGridInfo}.
   */
  public void test_gridInfo() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(2, false));",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('000');",
            "    }",
            "    new Label(this, SWT.NONE);",
            "    new Label(this, SWT.NONE);",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('111');",
            "    }",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      {",
            "        GridData gridData = new GridData();",
            "        gridData.horizontalSpan = 2;",
            "        button.setLayoutData(gridData);",
            "      }",
            "      button.setText('222');",
            "    }",
            "  }",
            "}");
    refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    ControlInfo button_0 = shell.getChildrenControls().get(0);
    ControlInfo button_1 = shell.getChildrenControls().get(3);
    ControlInfo button_2 = shell.getChildrenControls().get(4);
    //
    IGridInfo gridInfo = layout.getGridInfo();
    assertNotNull(gridInfo);
    // check count of column/row
    assertEquals(2, gridInfo.getColumnCount());
    assertEquals(3, gridInfo.getRowCount());
    // check column intervals
    {
      int[] columnOrigins = layout.getColumnOrigins();
      Interval[] columnIntervals = gridInfo.getColumnIntervals();
      assertEquals(columnOrigins.length, columnIntervals.length);
      for (int i = 0; i < columnOrigins.length; i++) {
        int origin = columnOrigins[i];
        Interval interval = columnIntervals[i];
        assertEquals(origin, interval.begin);
      }
    }
    // check row intervals
    {
      int[] rowOrigins = layout.getRowOrigins();
      Interval[] rowIntervals = gridInfo.getRowIntervals();
      assertEquals(rowOrigins.length, rowIntervals.length);
    }
    // check component cells
    {
      assertEquals(new Rectangle(0, 0, 1, 1), gridInfo.getComponentCells(button_0));
      assertEquals(new Rectangle(1, 1, 1, 1), gridInfo.getComponentCells(button_1));
      assertEquals(new Rectangle(0, 2, 2, 1), gridInfo.getComponentCells(button_2));
    }
    // check cells rectangle
    {
      Rectangle rectangle = gridInfo.getCellsRectangle(new Rectangle(0, 0, 1, 1));
      assertEquals(5, rectangle.x);
      assertEquals(5, rectangle.y);
    }
    // not RTL
    assertFalse(gridInfo.isRTL());
    // insets
    assertEquals(new Insets(0, 0, 0, 0), gridInfo.getInsets());
    // check "virtual" feedback sizes
    {
      assertTrue(gridInfo.hasVirtualColumns());
      assertTrue(gridInfo.hasVirtualRows());
      assertEquals(25, gridInfo.getVirtualColumnSize());
      assertEquals(5, gridInfo.getVirtualColumnGap());
      assertEquals(25, gridInfo.getVirtualRowSize());
      assertEquals(5, gridInfo.getVirtualRowGap());
    }
    // check occupied cells
    {
      assertSame(button_0, gridInfo.getOccupied(0, 0));
      assertSame(button_1, gridInfo.getOccupied(1, 1));
      assertSame(button_2, gridInfo.getOccupied(0, 2));
      assertSame(button_2, gridInfo.getOccupied(1, 2));
      assertNull(gridInfo.getOccupied(1, 0));
      assertNull(gridInfo.getOccupied(0, 1));
    }
  }

  /**
   * Test cells when {@link Shell#setSize(int, int)} is used.
   */
  public void test_gridInfo2() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setSize(300, 200);",
            "    setLayout(new GridLayout());",
            "    new Button(this, SWT.NONE);",
            "    new Button(this, SWT.NONE);",
            "  }",
            "}");
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    ControlInfo button_0 = shell.getChildrenControls().get(0);
    ControlInfo button_1 = shell.getChildrenControls().get(1);
    //
    shell.refresh();
    IGridInfo gridInfo = layout.getGridInfo();
    assertEquals(new Rectangle(0, 0, 1, 1), gridInfo.getComponentCells(button_0));
    assertEquals(new Rectangle(0, 1, 1, 1), gridInfo.getComponentCells(button_1));
  }

  /**
   * Test for {@link IGridInfo} when there are not controls.
   */
  public void test_gridInfo_empty() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(2, false));",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    //
    IGridInfo gridInfo = layout.getGridInfo();
    assertEquals(0, gridInfo.getRowIntervals().length);
    assertEquals(0, gridInfo.getColumnIntervals().length);
  }

  public void test_gridInfo_tooBigHorizontalSpan() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(1, false));",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      {",
            "        GridData gridData = new GridData();",
            "        gridData.horizontalSpan = 10;",
            "        button.setLayoutData(gridData);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    ControlInfo button = shell.getChildrenControls().get(0);
    //
    IGridInfo gridInfo = layout.getGridInfo();
    // check count of column/row
    assertEquals(1, gridInfo.getColumnCount());
    assertEquals(1, gridInfo.getRowCount());
    // check cells
    assertEquals(new Rectangle(0, 0, 1, 1), gridInfo.getComponentCells(button));
  }

  /**
   * Implicit controls also occupy cells.
   */
  public void test_gridInfo_implicitControls() throws Exception {
    prepareShell_withImplicit();
    CompositeInfo shell =
        parseComposite(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends MyShell {",
            "  public Test() {",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    ControlInfo button = getJavaInfoByName("button");
    IGridInfo gridInfo = layout.getGridInfo();
    // 2x2 grid
    assertEquals(2, gridInfo.getColumnCount());
    assertEquals(2, gridInfo.getRowCount());
    // occupied by implicit and explicit controls
    assertSame(shell, gridInfo.getOccupied(0, 0));
    assertSame(shell, gridInfo.getOccupied(1, 0));
    assertSame(button, gridInfo.getOccupied(0, 1));
    assertSame(null, gridInfo.getOccupied(1, 1));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // canManageDimensions()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link GridLayoutInfo#canChangeDimensions()}.
   */
  public void test_canChangeDimensions_explicit() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new GridLayout(2, false));",
            "  }",
            "}");
    refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    // is explicit
    assertTrue(layout.canChangeDimensions());
  }

  /**
   * Test for {@link GridLayoutInfo#canChangeDimensions()}.
   */
  public void test_canChangeDimensions_implicit() throws Exception {
    prepareShell_withImplicit();
    CompositeInfo shell =
        parseComposite(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends MyShell {",
            "  public Test() {",
            "  }",
            "}");
    refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    // is implicit
    assertFalse(layout.canChangeDimensions());
  }

  /**
   * Test for {@link GridLayoutInfo#canChangeDimensions()}.
   * <p>
   * {@link GridLayout} is implicit, but there are no implicit {@link Control}s, so it is safe to
   * change number of columns.
   */
  public void test_canChangeDimensions_implicit_withoutControls() throws Exception {
    prepareShell_withImplicitEmpty();
    CompositeInfo shell =
        parseComposite(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends MyShell {",
            "  public Test() {",
            "  }",
            "}");
    refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    // is implicit, but no implicit controls
    assertTrue(layout.canChangeDimensions());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setCells()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_setCells_horizontalSpan_inc() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(2, false));",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('000');",
            "    }",
            "    new Label(this, SWT.LEFT);",
            "    new Label(this, SWT.RIGHT);",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('111');",
            "    }",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    ControlInfo button = shell.getChildrenControls().get(0);
    GridDataInfo gridData = GridLayoutInfo.getGridData(button);
    // check initial GridData
    {
      assertEquals(0, getInt(gridData, "x"));
      assertEquals(0, getInt(gridData, "y"));
      assertEquals(1, getInt(gridData, "width"));
      assertEquals(1, getInt(gridData, "height"));
    }
    // set horizontal span
    layout.command_setCells(button, new Rectangle(0, 0, 2, 1), true);
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));",
        "      button.setText('000');",
        "    }",
        "    new Label(this, SWT.RIGHT);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('111');",
        "    }",
        "  }",
        "}");
    // check GridData
    {
      assertEquals(0, getInt(gridData, "x"));
      assertEquals(0, getInt(gridData, "y"));
      assertEquals(2, getInt(gridData, "width"));
      assertEquals(1, getInt(gridData, "height"));
    }
  }

  public void test_setCells_horizontalSpan_dec() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(2, false));",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      {",
            "        GridData gridData = new GridData();",
            "        gridData.horizontalSpan = 2;",
            "        button.setLayoutData(gridData);",
            "      }",
            "      button.setText('000');",
            "    }",
            "    new Label(this, SWT.RIGHT);",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('111');",
            "    }",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    ControlInfo button = shell.getChildrenControls().get(0);
    //
    layout.command_setCells(button, new Rectangle(0, 0, 1, 1), true);
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('000');",
        "    }",
        "    new Label(this, SWT.NONE);",
        "    new Label(this, SWT.RIGHT);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('111');",
        "    }",
        "  }",
        "}");
  }

  /**
   * There is possible situation when {@link GridLayout} has columns which are not filled with
   * {@link Control}s and we consider that {@link GridLayout} has less columns than it really has.
   * So, when we later build "control grid" we try to access array item which is out of bounds.
   */
  public void test_setCells_horizontalSpan_incToEmptyColumns() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(5, false));",
            "    Button button_1 = new Button(this, SWT.NONE);",
            "    Button button_2 = new Button(this, SWT.NONE);",
            "    Button button_3 = new Button(this, SWT.NONE);",
            "    {",
            "      Button toResize = new Button(this, SWT.NONE);",
            "      {",
            "        GridData gridData = new GridData();",
            "        gridData.horizontalSpan = 3;",
            "        toResize.setLayoutData(gridData);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    ControlInfo toResize = getJavaInfoByName("toResize");
    // grid was fixed
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(5, false));",
        "    Button button_1 = new Button(this, SWT.NONE);",
        "    Button button_2 = new Button(this, SWT.NONE);",
        "    Button button_3 = new Button(this, SWT.NONE);",
        "    new Label(this, SWT.NONE);",
        "    new Label(this, SWT.NONE);",
        "    {",
        "      Button toResize = new Button(this, SWT.NONE);",
        "      {",
        "        GridData gridData = new GridData();",
        "        gridData.horizontalSpan = 3;",
        "        toResize.setLayoutData(gridData);",
        "      }",
        "    }",
        "    new Label(this, SWT.NONE);",
        "    new Label(this, SWT.NONE);",
        "  }",
        "}");
    assertEquals(5, layout.getColumns().size());
    assertEquals(2, layout.getRows().size());
    // set cells
    layout.command_setCells(toResize, new Rectangle(0, 1, 5, 1), true);
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(5, false));",
        "    Button button_1 = new Button(this, SWT.NONE);",
        "    Button button_2 = new Button(this, SWT.NONE);",
        "    Button button_3 = new Button(this, SWT.NONE);",
        "    new Label(this, SWT.NONE);",
        "    new Label(this, SWT.NONE);",
        "    {",
        "      Button toResize = new Button(this, SWT.NONE);",
        "      {",
        "        GridData gridData = new GridData();",
        "        gridData.horizontalSpan = 5;",
        "        toResize.setLayoutData(gridData);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  public void test_setCells_verticalSpan_inc() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(2, false));",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('000');",
            "    }",
            "    new Label(this, SWT.LEFT);",
            "    new Label(this, SWT.RIGHT);",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('111');",
            "    }",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    ControlInfo button = shell.getChildrenControls().get(0);
    GridDataInfo gridData = GridLayoutInfo.getGridData(button);
    // check initial GridData
    {
      assertEquals(0, getInt(gridData, "x"));
      assertEquals(0, getInt(gridData, "y"));
      assertEquals(1, getInt(gridData, "width"));
      assertEquals(1, getInt(gridData, "height"));
    }
    // set vertical span
    layout.command_setCells(button, new Rectangle(0, 0, 1, 2), true);
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 2));",
        "      button.setText('000');",
        "    }",
        "    new Label(this, SWT.LEFT);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('111');",
        "    }",
        "  }",
        "}");
    // check GridData
    {
      assertEquals(0, getInt(gridData, "x"));
      assertEquals(0, getInt(gridData, "y"));
      assertEquals(1, getInt(gridData, "width"));
      assertEquals(2, getInt(gridData, "height"));
    }
  }

  public void test_setCells_verticalSpan_dec() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(2, false));",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('000');",
            "    }",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      {",
            "        GridData gridData = new GridData();",
            "        gridData.verticalSpan = 2;",
            "        button.setLayoutData(gridData);",
            "      }",
            "      button.setText('111');",
            "    }",
            "    new Label(this, SWT.RIGHT);",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    ControlInfo button = shell.getChildrenControls().get(1);
    //
    layout.command_setCells(button, new Rectangle(1, 1, 1, 1), true);
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('000');",
        "    }",
        "    new Label(this, SWT.NONE);",
        "    new Label(this, SWT.RIGHT);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('111');",
        "    }",
        "  }",
        "}");
  }

  public void test_setCells_move() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(2, false));",
            "    Button button_00 = new Button(this, SWT.NONE);",
            "    new Label(this, SWT.NONE);",
            "    Button button_01 = new Button(this, SWT.NONE);",
            "    Button button_11 = new Button(this, SWT.NONE);",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    ControlInfo button_01 = shell.getChildrenControls().get(2);
    //
    layout.command_setCells(button_01, new Rectangle(1, 0, 1, 1), true);
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    Button button_00 = new Button(this, SWT.NONE);",
        "    Button button_01 = new Button(this, SWT.NONE);",
        "    new Label(this, SWT.NONE);",
        "    Button button_11 = new Button(this, SWT.NONE);",
        "  }",
        "}");
    // check x/y for new filler
    {
      ControlInfo filler = shell.getChildrenControls().get(2);
      GridDataInfo gridData = GridLayoutInfo.getGridData(filler);
      assertEquals(0, getInt(gridData, "x"));
      assertEquals(1, getInt(gridData, "y"));
      assertEquals(1, getInt(gridData, "width"));
      assertEquals(1, getInt(gridData, "height"));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // command_setSizeHint()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link GridLayoutInfo#command_setSizeHint(ControlInfo, boolean, Dimension)}.
   */
  public void test_setSizeHint_width() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(1, false));",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    ControlInfo button = shell.getChildrenControls().get(0);
    // set hint
    layout.command_setSizeHint(button, true, new Dimension(200, -1));
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      {",
        "        GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);",
        "        gridData.widthHint = 200;",
        "        button.setLayoutData(gridData);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link GridLayoutInfo#command_setSizeHint(ControlInfo, boolean, Dimension)}.
   */
  public void test_setSizeHint_height() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(1, false));",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    ControlInfo button = shell.getChildrenControls().get(0);
    // set hint
    layout.command_setSizeHint(button, false, new Dimension(-1, 50));
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      {",
        "        GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);",
        "        gridData.heightHint = 50;",
        "        button.setLayoutData(gridData);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When we delete {@link ControlInfo}, it should be replaced with filler.
   */
  public void test_delete_replaceWithFillers() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(2, false));",
            "    Button button_00 = new Button(this, SWT.NONE);",
            "    Button button_10 = new Button(this, SWT.NONE);",
            "    Button button_01 = new Button(this, SWT.NONE);",
            "    Button button_11 = new Button(this, SWT.NONE);",
            "  }",
            "}");
    shell.refresh();
    ControlInfo button_01 = shell.getChildrenControls().get(2);
    //
    button_01.delete();
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    Button button_00 = new Button(this, SWT.NONE);",
        "    Button button_10 = new Button(this, SWT.NONE);",
        "    new Label(this, SWT.NONE);",
        "    Button button_11 = new Button(this, SWT.NONE);",
        "  }",
        "}");
  }

  /**
   * When we delete column, we should keep at least one column.
   */
  public void test_delete_keepOneColumn() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(1, false));",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ControlInfo button = shell.getChildrenControls().get(0);
    //
    button.delete();
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(1, false));",
        "  }",
        "}");
  }

  public void test_delete_removeEmptyDimensions() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(2, false));",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('000');",
            "    }",
            "    new Label(this, SWT.NONE);",
            "    new Label(this, SWT.NONE);",
            "    new Label(this, SWT.NONE);",
            "    new Label(this, SWT.NONE);",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('111');",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ControlInfo button = shell.getChildrenControls().get(5);
    //
    {
      GridDataInfo gridData = GridLayoutInfo.getGridData(button);
      assertEquals(1, getInt(gridData, "x"));
      assertEquals(2, getInt(gridData, "y"));
    }
    //
    button.delete();
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('000');",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE_inEmptyCell() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(2, false));",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('000');",
            "    }",
            "    new Label(this, SWT.LEFT);",
            "    new Label(this, SWT.RIGHT);",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('111');",
            "    }",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    //
    ControlInfo newButton = BTestUtils.createButton();
    layout.command_CREATE(newButton, 1, false, 0, false);
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('000');",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "    new Label(this, SWT.RIGHT);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('111');",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_insertRow() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(2, false));",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('000');",
            "    }",
            "    new Label(this, SWT.LEFT);",
            "    new Label(this, SWT.RIGHT);",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('111');",
            "    }",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    //
    ControlInfo newButton = BTestUtils.createButton();
    layout.command_CREATE(newButton, 1, false, 1, true);
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('000');",
        "    }",
        "    new Label(this, SWT.LEFT);",
        "    new Label(this, SWT.NONE);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "    new Label(this, SWT.RIGHT);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('111');",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_insertColumn() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(2, false));",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('000');",
            "    }",
            "    new Label(this, SWT.LEFT);",
            "    new Label(this, SWT.RIGHT);",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('111');",
            "    }",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    //
    ControlInfo newButton = BTestUtils.createButton();
    layout.command_CREATE(newButton, 1, true, 0, false);
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(3, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('000');",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "    new Label(this, SWT.LEFT);",
        "    new Label(this, SWT.RIGHT);",
        "    new Label(this, SWT.NONE);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('111');",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_insertColumnRow() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(1, false));",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('000');",
            "    }",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    //
    ControlInfo newButton = BTestUtils.createButton();
    layout.command_CREATE(newButton, 0, true, 0, true);
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "    new Label(this, SWT.NONE);",
        "    new Label(this, SWT.NONE);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('000');",
        "    }",
        "  }",
        "}");
    // delete - should return in initial state
    newButton.delete();
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('000');",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_appendRow() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(1, false));",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('000');",
            "    }",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    //
    ControlInfo newButton = BTestUtils.createButton();
    layout.command_CREATE(newButton, 0, false, 2, false);
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('000');",
        "    }",
        "    new Label(this, SWT.NONE);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_appendColumn() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(1, false));",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('000');",
            "    }",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    //
    ControlInfo newButton = BTestUtils.createButton();
    layout.command_CREATE(newButton, 2, false, 0, false);
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(3, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('000');",
        "    }",
        "    new Label(this, SWT.NONE);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_appendColumnRow() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(1, false));",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('000');",
            "    }",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    //
    ControlInfo newButton = BTestUtils.createButton();
    layout.command_CREATE(newButton, 1, false, 1, false);
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('000');",
        "    }",
        "    new Label(this, SWT.NONE);",
        "    new Label(this, SWT.NONE);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_insertColumnHorizontalSpan() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(2, false));",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      {",
            "        GridData gridData = new GridData();",
            "        gridData.horizontalSpan = 2;",
            "        button.setLayoutData(gridData);",
            "      }",
            "      button.setText('000');",
            "    }",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('111');",
            "    }",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('222');",
            "    }",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    //
    ControlInfo newButton = BTestUtils.createButton();
    layout.command_CREATE(newButton, 1, true, 1, false);
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(3, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      {",
        "        GridData gridData = new GridData();",
        "        gridData.horizontalSpan = 3;",
        "        button.setLayoutData(gridData);",
        "      }",
        "      button.setText('000');",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('111');",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('222');",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_insertRowVerticalSpan() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(2, false));",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      {",
            "        GridData gridData = new GridData();",
            "        gridData.verticalSpan = 2;",
            "        button.setLayoutData(gridData);",
            "      }",
            "      button.setText('000');",
            "    }",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('111');",
            "    }",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('222');",
            "    }",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    //
    ControlInfo newButton = BTestUtils.createButton();
    layout.command_CREATE(newButton, 1, false, 1, true);
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      {",
        "        GridData gridData = new GridData();",
        "        gridData.verticalSpan = 3;",
        "        button.setLayoutData(gridData);",
        "      }",
        "      button.setText('000');",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('111');",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('222');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for parsing "not balanced" {@link GridLayoutInfo} and adding into <code>null</code> cell.
   */
  public void test_CREATE_notBalanced() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(2, false));",
            "    new Label(this, SWT.NONE);",
            "    new Label(this, SWT.NONE);",
            "    new Label(this, SWT.NONE);",
            "  }",
            "}");
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    shell.refresh();
    //
    ControlInfo newButton = BTestUtils.createButton();
    layout.command_CREATE(newButton, 1, false, 1, false);
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    new Label(this, SWT.NONE);",
        "    new Label(this, SWT.NONE);",
        "    new Label(this, SWT.NONE);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE special cases
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE_Shell_open() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test {",
            "  public static void main(String[] args) {",
            "    Shell shell = new Shell();",
            "    shell.setLayout(new GridLayout(1, false));",
            "    shell.open();",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    //
    ControlInfo newButton = BTestUtils.createButton();
    layout.command_CREATE(newButton, 1, false, 0, false);
    assertEditor(
        "public class Test {",
        "  public static void main(String[] args) {",
        "    Shell shell = new Shell();",
        "    shell.setLayout(new GridLayout(2, false));",
        "    new Label(shell, SWT.NONE);",
        "    {",
        "      Button button = new Button(shell, SWT.NONE);",
        "    }",
        "    shell.open();",
        "  }",
        "}");
  }

  public void test_CREATE_Shell_layout() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test {",
            "  public static void main(String[] args) {",
            "    Shell shell = new Shell();",
            "    shell.setLayout(new GridLayout(1, false));",
            "    shell.layout();",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    //
    ControlInfo newButton = BTestUtils.createButton();
    layout.command_CREATE(newButton, 1, false, 0, false);
    assertEditor(
        "public class Test {",
        "  public static void main(String[] args) {",
        "    Shell shell = new Shell();",
        "    shell.setLayout(new GridLayout(2, false));",
        "    new Label(shell, SWT.NONE);",
        "    {",
        "      Button button = new Button(shell, SWT.NONE);",
        "    }",
        "    shell.layout();",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands for implicit GridLayout
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link GridLayoutInfo#isExplicitRow(int)}.
   */
  public void test_implicitLayout_isExplicitRow() throws Exception {
    prepareShell_withImplicit();
    CompositeInfo shell =
        parseComposite(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends MyShell {",
            "  public Test() {",
            "    Button button = new Button(this, SWT.NONE);",
            "  }",
            "}");
    refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    // has 1 implicit and 1 explicit row
    assertEquals(2, layout.getRows().size());
    assertFalse(layout.isExplicitRow(0));
    assertTrue(layout.isExplicitRow(1));
  }

  /**
   * We should not break existing {@link GridLayout} based design by changing number of columns.
   */
  public void test_implicitLayout_CREATE() throws Exception {
    prepareShell_withImplicit();
    CompositeInfo shell =
        parseComposite(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends MyShell {",
            "  public Test() {",
            "  }",
            "}");
    refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    // initial state
    assertEquals(2, layout.getColumns().size());
    assertEquals(1, layout.getRows().size());
    // add new Button
    ControlInfo newButton = BTestUtils.createButton();
    layout.command_CREATE(newButton, 1, false, 1, false);
    assertEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends MyShell {",
        "  public Test() {",
        "    new Label(this, SWT.NONE);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Using implicit {@link GridLayout} should not cause problems during moving {@link Control}.
   */
  public void test_implicitLayout_MOVE() throws Exception {
    prepareShell_withImplicit();
    CompositeInfo shell =
        parseComposite(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends MyShell {",
            "  public Test() {",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "    }",
            "    new Label(this, SWT.NONE);",
            "  }",
            "}");
    refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    ControlInfo button = getJavaInfoByName("button");
    // move "button"
    layout.command_MOVE(button, 1, false, 1, false);
    assertEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends MyShell {",
        "  public Test() {",
        "    new Label(this, SWT.NONE);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
  }

  private void prepareShell_withImplicit() throws Exception {
    setFileContentSrc(
        "test/MyShell.java",
        getTestSource(
            "public class MyShell extends Shell {",
            "  public MyShell() {",
            "    setLayout(new GridLayout(2, false));",
            "    new Button(this, SWT.NONE);",
            "    new Button(this, SWT.NONE);",
            "  }",
            "  protected void checkSubclass() {",
            "  }",
            "}"));
    waitForAutoBuild();
  }

  private void prepareShell_withImplicitEmpty() throws Exception {
    setFileContentSrc(
        "test/MyShell.java",
        getTestSource(
            "public class MyShell extends Shell {",
            "  public MyShell() {",
            "    setLayout(new GridLayout(1, false));",
            "  }",
            "  protected void checkSubclass() {",
            "  }",
            "}"));
    waitForAutoBuild();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dimension operations
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_columnAccess() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(2, false));",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('0 x 0');",
            "    }",
            "    new Label(this, SWT.NONE);",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('0 x 1');",
            "    }",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('1 x 1');",
            "    }",
            "  }",
            "}");
    shell.refresh();
    final GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    final GridColumnInfo<ControlInfo> column = layout.getColumns().get(0);
    // check initial values
    assertEquals(0, column.getIndex());
    assertEquals("left", column.getTitle());
    assertFalse(column.getGrab());
    assertEquals(SWT.LEFT, column.getAlignment().intValue());
    // flip grab
    column.flipGrab();
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));",
        "      button.setText('0 x 0');",
        "    }",
        "    new Label(this, SWT.NONE);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));",
        "      button.setText('0 x 1');",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('1 x 1');",
        "    }",
        "  }",
        "}");
    assertEquals("left, grab", column.getTitle());
    // set alignment
    column.setAlignment(SWT.FILL);
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));",
        "      button.setText('0 x 0');",
        "    }",
        "    new Label(this, SWT.NONE);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));",
        "      button.setText('0 x 1');",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('1 x 1');",
        "    }",
        "  }",
        "}");
    assertEquals("fill, grab", column.getTitle());
    // set different alignment for "0 x 1" button
    {
      ControlInfo button = shell.getChildrenControls().get(2);
      GridLayoutInfo.getGridData(button).setHorizontalAlignment(SWT.RIGHT);
      assertNull(column.getAlignment());
    }
    // check other alignments
    {
      column.setAlignment(SWT.CENTER);
      assertEquals("center, grab", column.getTitle());
      //
      column.setAlignment(SWT.RIGHT);
      assertEquals("right, grab", column.getTitle());
    }
    // delete
    ExecutionUtils.run(shell, new RunnableEx() {
      public void run() throws Exception {
        column.delete();
      }
    });
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(1, false));",
        "    new Label(this, SWT.NONE);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('1 x 1');",
        "    }",
        "  }",
        "}");
  }

  public void test_rowAccess() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(2, false));",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('0 x 0');",
            "    }",
            "    new Label(this, SWT.NONE);",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('0 x 1');",
            "    }",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('1 x 1');",
            "    }",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    //
    GridRowInfo<ControlInfo> row = layout.getRows().get(1);
    // check initial values
    assertEquals(1, row.getIndex());
    assertEquals("center", row.getTitle());
    assertFalse(row.getGrab());
    assertEquals(SWT.CENTER, row.getAlignment().intValue());
    // flip grab
    row.flipGrab();
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('0 x 0');",
        "    }",
        "    new Label(this, SWT.NONE);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));",
        "      button.setText('0 x 1');",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));",
        "      button.setText('1 x 1');",
        "    }",
        "  }",
        "}");
    assertEquals("center, grab", row.getTitle());
    // set alignment
    row.setAlignment(SWT.FILL);
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('0 x 0');",
        "    }",
        "    new Label(this, SWT.NONE);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1));",
        "      button.setText('0 x 1');",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1));",
        "      button.setText('1 x 1');",
        "    }",
        "  }",
        "}");
    assertEquals("fill, grab", row.getTitle());
    // set different alignment for "0 x 1" button
    {
      ControlInfo button = shell.getChildrenControls().get(2);
      GridLayoutInfo.getGridData(button).setVerticalAlignment(SWT.BOTTOM);
      assertNull(row.getAlignment());
    }
    // check other alignments
    {
      row.setAlignment(SWT.TOP);
      assertEquals("top, grab", row.getTitle());
      //
      row.setAlignment(SWT.BOTTOM);
      assertEquals("bottom, grab", row.getTitle());
    }
    // delete
    row.delete();
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('0 x 0');",
        "    }",
        "  }",
        "}");
  }

  public void test_deleteColumn() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(3, false));",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      {",
            "        GridData gridData = new GridData();",
            "        gridData.horizontalSpan = 3;",
            "        button.setLayoutData(gridData);",
            "      }",
            "      button.setText('000');",
            "    }",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('111');",
            "    }",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('New Button');",
            "    }",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('222');",
            "    }",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    //
    try {
      shell.startEdit();
      layout.command_deleteColumn(1, true);
    } finally {
      shell.endEdit();
    }
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      {",
        "        GridData gridData = new GridData();",
        "        gridData.horizontalSpan = 2;",
        "        button.setLayoutData(gridData);",
        "      }",
        "      button.setText('000');",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('111');",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('222');",
        "    }",
        "  }",
        "}");
  }

  public void test_deleteColumn_deleteAlsoEmptyRows() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(2, false));",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('000');",
            "    }",
            "    new Label(this, SWT.NONE);",
            "    new Label(this, SWT.NONE);",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('111');",
            "    }",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    //
    try {
      shell.startEdit();
      layout.command_deleteColumn(1, true);
    } finally {
      shell.endEdit();
    }
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('000');",
        "    }",
        "  }",
        "}");
  }

  /**
   * When grid has empty cells (at the ends of columns/rows), this caused
   * {@link NullPointerException}.
   */
  public void test_delete_missingFillers() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(2, false));",
            "    Button button_00 = new Button(this, SWT.NONE);",
            "    Button button_10 = new Button(this, SWT.NONE);",
            "    Button button_01 = new Button(this, SWT.NONE);",
            "  }",
            "}");
    shell.refresh();
    // grid was fixed
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    Button button_00 = new Button(this, SWT.NONE);",
        "    Button button_10 = new Button(this, SWT.NONE);",
        "    Button button_01 = new Button(this, SWT.NONE);",
        "    new Label(this, SWT.NONE);",
        "  }",
        "}");
    // prepare all Control's
    List<ControlInfo> controls = shell.getChildrenControls();
    assertThat(controls).hasSize(4);
    // delete "button_01"
    ControlInfo button_01 = controls.get(2);
    button_01.delete();
    // ..."row 1" should be removed
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    Button button_00 = new Button(this, SWT.NONE);",
        "    Button button_10 = new Button(this, SWT.NONE);",
        "  }",
        "}");
  }

  public void test_deleteRow() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(2, false));",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      {",
            "        GridData gridData = new GridData();",
            "        gridData.verticalSpan = 3;",
            "        button.setLayoutData(gridData);",
            "      }",
            "      button.setText('000');",
            "    }",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('111');",
            "    }",
            "    {",
            "      Button button_1 = new Button(this, SWT.NONE);",
            "      button_1.setText('New Button');",
            "    }",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('222');",
            "    }",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    //
    try {
      shell.startEdit();
      layout.command_deleteRow(1, true);
    } finally {
      shell.endEdit();
    }
    //
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      {",
        "        GridData gridData = new GridData();",
        "        gridData.verticalSpan = 2;",
        "        button.setLayoutData(gridData);",
        "      }",
        "      button.setText('000');",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('111');",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('222');",
        "    }",
        "  }",
        "}");
  }

  public void test_deleteRow_deleteAlsoEmptyColumns() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(2, false));",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('000');",
            "    }",
            "    new Label(this, SWT.NONE);",
            "    new Label(this, SWT.NONE);",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('111');",
            "    }",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    //
    try {
      shell.startEdit();
      layout.command_deleteRow(1, true);
    } finally {
      shell.endEdit();
    }
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('000');",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE COLUMN
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_MOVE_COLUMN_before() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(2, false));",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('000');",
            "    }",
            "    new Label(this, SWT.NONE);",
            "    new Label(this, SWT.NONE);",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('111');",
            "    }",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    //
    try {
      layout.startEdit();
      layout.command_MOVE_COLUMN(1, 0);
    } finally {
      layout.endEdit();
    }
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    new Label(this, SWT.NONE);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('000');",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('111');",
        "    }",
        "    new Label(this, SWT.NONE);",
        "  }",
        "}");
  }

  public void test_MOVE_COLUMN_after() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(2, false));",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('000');",
            "    }",
            "    new Label(this, SWT.NONE);",
            "    new Label(this, SWT.NONE);",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('111');",
            "    }",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    //
    try {
      layout.startEdit();
      layout.command_MOVE_COLUMN(0, 2);
    } finally {
      layout.endEdit();
    }
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    new Label(this, SWT.NONE);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('000');",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('111');",
        "    }",
        "    new Label(this, SWT.NONE);",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE ROW
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_MOVE_ROW_before() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(2, false));",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('000');",
            "    }",
            "    new Label(this, SWT.NONE);",
            "    new Label(this, SWT.NONE);",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('111');",
            "    }",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    //
    try {
      layout.startEdit();
      layout.command_MOVE_ROW(1, 0);
    } finally {
      layout.endEdit();
    }
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    new Label(this, SWT.NONE);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('111');",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('000');",
        "    }",
        "    new Label(this, SWT.NONE);",
        "  }",
        "}");
  }

  public void test_MOVE_ROW_after() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(2, false));",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('000');",
            "    }",
            "    new Label(this, SWT.NONE);",
            "    new Label(this, SWT.NONE);",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('111');",
            "    }",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    //
    try {
      layout.startEdit();
      layout.command_MOVE_ROW(0, 2);
    } finally {
      layout.endEdit();
    }
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    new Label(this, SWT.NONE);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('111');",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('000');",
        "    }",
        "    new Label(this, SWT.NONE);",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // command_normalizeSpanning()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link GridLayoutInfo#command_normalizeSpanning()}.<br>
   * Single control spanned on two columns.
   */
  public void test_normalizeSpanning_1() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(2, false));",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      {",
            "        GridData gridData = new GridData();",
            "        gridData.horizontalSpan = 2;",
            "        button.setLayoutData(gridData);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    //
    layout.command_normalizeSpanning();
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link GridLayoutInfo#command_normalizeSpanning()}.<br>
   * Single control spanned on two rows.
   */
  public void test_normalizeSpanning_2() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(1, false));",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      {",
            "        GridData gridData = new GridData();",
            "        gridData.verticalSpan = 2;",
            "        button.setLayoutData(gridData);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    //
    layout.command_normalizeSpanning();
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link GridLayoutInfo#command_normalizeSpanning()}.<br>
   * No normalize: each column/row has control.
   */
  public void test_normalizeSpanning_3() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(2, false));",
            "    {",
            "      Button button_1 = new Button(this, SWT.NONE);",
            "      {",
            "        GridData gridData = new GridData();",
            "        gridData.horizontalSpan = 2;",
            "        button_1.setLayoutData(gridData);",
            "      }",
            "    }",
            "    new Label(this, SWT.NONE);",
            "    {",
            "      Button button_2 = new Button(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    //
    layout.command_normalizeSpanning();
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    {",
        "      Button button_1 = new Button(this, SWT.NONE);",
        "      {",
        "        GridData gridData = new GridData();",
        "        gridData.horizontalSpan = 2;",
        "        button_1.setLayoutData(gridData);",
        "      }",
        "    }",
        "    new Label(this, SWT.NONE);",
        "    {",
        "      Button button_2 = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link GridLayoutInfo#command_normalizeSpanning()}.<br>
   * Do normalize: no control for second column.
   */
  public void test_normalizeSpanning_4() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(2, false));",
            "    {",
            "      Button button_1 = new Button(this, SWT.NONE);",
            "      {",
            "        GridData gridData = new GridData();",
            "        gridData.horizontalSpan = 2;",
            "        button_1.setLayoutData(gridData);",
            "      }",
            "    }",
            "    {",
            "      Button button_2 = new Button(this, SWT.NONE);",
            "    }",
            "    new Label(this, SWT.NONE);",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    //
    layout.command_normalizeSpanning();
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button button_1 = new Button(this, SWT.NONE);",
        "    }",
        "    {",
        "      Button button_2 = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_MOVE() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(2, false));",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('000');",
            "    }",
            "    new Label(this, SWT.NONE);",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('111');",
            "    }",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('222');",
            "    }",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    ControlInfo button = shell.getChildrenControls().get(2);
    //
    layout.command_MOVE(button, 1, false, 0, false);
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('000');",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('111');",
        "    }",
        "    new Label(this, SWT.NONE);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('222');",
        "    }",
        "  }",
        "}");
  }

  public void test_MOVE_out() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(1, false));",
            "    {",
            "      Composite composite = new Composite(this, SWT.NONE);",
            "      composite.setLayout(new RowLayout());",
            "    }",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    CompositeInfo composite = (CompositeInfo) shell.getChildrenControls().get(0);
    RowLayoutInfo layout = (RowLayoutInfo) composite.getLayout();
    ControlInfo button = shell.getChildrenControls().get(1);
    //
    layout.command_MOVE(button, null);
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Composite composite = new Composite(this, SWT.NONE);",
        "      composite.setLayout(new RowLayout());",
        "      {",
        "        Button button = new Button(composite, SWT.NONE);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  public void test_MOVE_error_1() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(4, false));",
            "    new Label(this, SWT.NONE);",
            "    new Label(this, SWT.NONE);",
            "    new Label(this, SWT.NONE);",
            "    new Label(this, SWT.NONE);",
            "    new Label(this, SWT.NONE);",
            "    new Label(this, SWT.NONE);",
            "    new Label(this, SWT.NONE);",
            "    Button button = new Button(this, SWT.NONE);",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo gridLayout = (GridLayoutInfo) shell.getLayout();
    ControlInfo button = shell.getChildrenControls().get(7);
    //
    gridLayout.command_MOVE(button, 1, false, 0, false);
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    new Label(this, SWT.NONE);",
        "    Button button = new Button(this, SWT.NONE);",
        "  }",
        "}");
  }

  public void test_MOVE_error_2() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(3, false));",
            "    new Label(this, SWT.NONE);",
            "    new Label(this, SWT.NONE);",
            "    new Label(this, SWT.NONE);",
            "    new Label(this, SWT.NONE);",
            "    new Label(this, SWT.NONE);",
            "    Button button = new Button(this, SWT.NONE);",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo gridLayout = (GridLayoutInfo) shell.getLayout();
    ControlInfo button = shell.getChildrenControls().get(5);
    //
    gridLayout.command_MOVE(button, 0, false, 0, false);
    gridLayout.getGridInfo();
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(1, false));",
        "    Button button = new Button(this, SWT.NONE);",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ADD
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_ADD() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(1, false));",
            "    {",
            "      Composite composite = new Composite(this, SWT.NONE);",
            "      composite.setLayout(new RowLayout());",
            "      {",
            "        Button button = new Button(composite, SWT.NONE);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    CompositeInfo composite = (CompositeInfo) shell.getChildrenControls().get(0);
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    ControlInfo button = composite.getChildrenControls().get(0);
    //
    layout.command_ADD(button, 0, false, 1, false);
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Composite composite = new Composite(this, SWT.NONE);",
        "      composite.setLayout(new RowLayout());",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Special cases
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE_noReference() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(1, false));",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    //
    ControlInfo newButton = BTestUtils.createButton();
    layout.command_CREATE(newButton, 0, false, 0, false);
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_viewer() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(1, false));",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    //
    ViewerInfo viewer = ViewerTest.createTableViewer(m_lastEditor);
    TableInfo table = (TableInfo) JavaInfoUtils.getWrapped(viewer);
    //
    layout.command_CREATE(table, 0, false, 0, false);
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      TableViewer tableViewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION);",
        "      Table table = tableViewer.getTable();",
        "      table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "numColumns" property
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * There were problems when we increase/decrease number of columns using "numColumns" property.
   */
  public void test_numColumns_inc() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(2, false));",
            "    Button button_1 = new Button(this, SWT.NONE);",
            "    Button button_2 = new Button(this, SWT.NONE);",
            "    Button button_3 = new Button(this, SWT.NONE);",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    // grid was fixed
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    Button button_1 = new Button(this, SWT.NONE);",
        "    Button button_2 = new Button(this, SWT.NONE);",
        "    Button button_3 = new Button(this, SWT.NONE);",
        "    new Label(this, SWT.NONE);",
        "  }",
        "}");
    // initial state
    assertEquals(2, layout.getColumns().size());
    assertEquals(2, layout.getRows().size());
    // numColumn := 3
    layout.getPropertyByTitle("numColumns").setValue(3);
    assertEquals(3, layout.getColumns().size());
    assertEquals(2, layout.getRows().size());
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(3, false));",
        "    Button button_1 = new Button(this, SWT.NONE);",
        "    Button button_2 = new Button(this, SWT.NONE);",
        "    Button button_3 = new Button(this, SWT.NONE);",
        "    new Label(this, SWT.NONE);",
        "    new Label(this, SWT.NONE);",
        "    new Label(this, SWT.NONE);",
        "  }",
        "}");
  }

  /**
   * There were problems when we increase/decrease number of columns using "numColumns" property.
   */
  public void test_numColumns_dec() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(3, false));",
            "    Button button_1 = new Button(this, SWT.NONE);",
            "    Button button_2 = new Button(this, SWT.NONE);",
            "    Button button_3 = new Button(this, SWT.NONE);",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    // initial state
    assertEquals(3, layout.getColumns().size());
    assertEquals(1, layout.getRows().size());
    // numColumn := 2
    layout.getPropertyByTitle("numColumns").setValue(2);
    assertEquals(2, layout.getColumns().size());
    assertEquals(2, layout.getRows().size());
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    Button button_1 = new Button(this, SWT.NONE);",
        "    Button button_2 = new Button(this, SWT.NONE);",
        "    Button button_3 = new Button(this, SWT.NONE);",
        "    new Label(this, SWT.NONE);",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "exclude"
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When we mark {@link Control} as excluded, this should not cause problems.
   */
  public void test_excludeFlag() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(1, false));",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    ControlInfo button = shell.getChildrenControls().get(0);
    // initially "button" is managed
    assertThat(layout.getControls()).containsOnly(button);
    // exclude "button"
    GridLayoutInfo.getGridData(button).getPropertyByTitle("exclude").setValue(true);
    assertThat(layout.getControls()).isEmpty();
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      {",
        "        GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);",
        "        gridData.exclude = true;",
        "        button.setLayoutData(gridData);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * When we mark {@link Control} as excluded, this should not cause problems.
   */
  public void test_excludeFlag_forImplicit() throws Exception {
    setFileContentSrc(
        "test/MyComposite.java",
        getTestSource(
            "class MyComposite extends Composite {",
            "  MyComposite(Composite parent, int style) {",
            "    super(parent, style);",
            "    setLayout(new GridLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      {",
            "        GridData gridData = new GridData();",
            "        gridData.exclude = true;",
            "        button.setLayoutData(gridData);",
            "      }",
            "    }",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    CompositeInfo shell =
        parseComposite(
            "// filler filler filler filler filler",
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(1, false));",
            "    new MyComposite(this, SWT.NONE);",
            "  }",
            "}");
    shell.refresh();
    assertNoErrors(shell);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete layout
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that when delete {@link GridLayoutInfo}, fillers are also removed, because there are not
   * controls that user wants.
   */
  public void test_DELETE_removeFillers() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(1, false));",
            "    new Label(this, SWT.NONE);",
            "    new Button(this, SWT.NONE);",
            "  }",
            "}");
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    shell.refresh();
    // initially 2 controls - filler and Button
    assertEquals(2, shell.getChildrenControls().size());
    // after delete - only Button
    layout.delete();
    assertEquals(1, shell.getChildrenControls().size());
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    new Button(this, SWT.NONE);",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_clipboard() throws Exception {
    final CompositeInfo shell =
        parseJavaInfo(
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Composite composite = new Composite(this, SWT.NONE);",
            "      composite.setLayout(new GridLayout(2, false));",
            "      {",
            "        Button button = new Button(composite, SWT.NONE);",
            "        GridData gridData = new GridData();",
            "        gridData.horizontalAlignment = GridData.FILL;",
            "        button.setLayoutData(gridData);",
            "      }",
            "      new Label(composite, SWT.NONE);",
            "      new Label(composite, SWT.NONE);",
            "      {",
            "        Button button = new Button(composite, SWT.CHECK);",
            "      }",
            "    }",
            "  }",
            "}");
    refresh();
    //
    ControlInfo composite = getJavaInfoByName("composite");
    doCopyPaste(composite, new PasteProcedure<ControlInfo>() {
      public void run(ControlInfo copy) throws Exception {
        shell.getLayout().command_CREATE(copy, null);
      }
    });
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    {",
        "      Composite composite = new Composite(this, SWT.NONE);",
        "      composite.setLayout(new GridLayout(2, false));",
        "      {",
        "        Button button = new Button(composite, SWT.NONE);",
        "        GridData gridData = new GridData();",
        "        gridData.horizontalAlignment = GridData.FILL;",
        "        button.setLayoutData(gridData);",
        "      }",
        "      new Label(composite, SWT.NONE);",
        "      new Label(composite, SWT.NONE);",
        "      {",
        "        Button button = new Button(composite, SWT.CHECK);",
        "      }",
        "    }",
        "    {",
        "      Composite composite = new Composite(this, SWT.NONE);",
        "      composite.setLayout(new GridLayout(2, false));",
        "      {",
        "        Button button = new Button(composite, SWT.NONE);",
        "        button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));",
        "      }",
        "      new Label(composite, SWT.NONE);",
        "      new Label(composite, SWT.NONE);",
        "      {",
        "        Button button = new Button(composite, SWT.CHECK);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the <code>int</code> value of field with given name.
   */
  private static int getInt(GridDataInfo gridData, String fieldName) throws Exception {
    return ReflectionUtils.getFieldInt(gridData, fieldName);
  }
}