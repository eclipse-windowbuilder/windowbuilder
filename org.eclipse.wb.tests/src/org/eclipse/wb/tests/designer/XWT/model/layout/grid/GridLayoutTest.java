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
package org.eclipse.wb.tests.designer.XWT.model.layout.grid;

import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Interval;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.model.layout.grid.GridColumnInfo;
import org.eclipse.wb.internal.swt.model.layout.grid.GridImages;
import org.eclipse.wb.internal.swt.model.layout.grid.GridRowInfo;
import org.eclipse.wb.internal.xwt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.xwt.model.layout.grid.GridDataInfo;
import org.eclipse.wb.internal.xwt.model.layout.grid.GridLayoutInfo;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link GridLayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class GridLayoutTest extends XwtModelTest {
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
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <GridLayout/>",
            "  </Shell.layout>",
            "</Shell>");
    shell.refresh();
    shell.refresh_dispose();
  }

  /**
   * Fillers should be filtered out from presentation children.
   */
  public void test_excludeFillersFromPresentationChildren_1() throws Exception {
    CompositeInfo shell =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <GridLayout/>",
            "  </Shell.layout>",
            "  <Button wbp:name='button'/>",
            "  <Label/>",
            "</Shell>");
    refresh();
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
   * <code>Label</code> with <code>setText()</code> is not filler, it is normal control.
   */
  public void test_fillersWith_setText() throws Exception {
    CompositeInfo shell =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <GridLayout/>",
            "  </Shell.layout>",
            "  <Label wbp:name='label' text='txt'/>",
            "</Shell>");
    refresh();
    ControlInfo label = getObjectByName("label");
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

  ////////////////////////////////////////////////////////////////////////////
  //
  // fixGrid()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * No controls, no changes expected.
   */
  public void test_fixGrid_noControls() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "</Shell>");
    refresh();
    GridLayoutInfo layout = getObjectByName("layout");
    // do "fix"
    ReflectionUtils.invokeMethod(layout, "fixGrid()");
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "</Shell>");
  }

  /**
   * It is possible that there are existing {@link Control}-s created in superclass. We should not
   * try to "fix" this by adding fillers.
   */
  public void test_fixGrid_leadingImplicitControls() throws Exception {
    setFileContentSrc(
        "test/MyShell.java",
        getJavaSource(
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
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<t:MyShell>",
            "  <Button/>",
            "</t:MyShell>");
    refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    // do "fix"
    ReflectionUtils.invokeMethod(layout, "fixGrid()");
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<t:MyShell>",
        "  <Button/>",
        "</t:MyShell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IGridInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link IGridInfo}.
   */
  public void test_gridInfo() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'/>",
        "  <Label/>",
        "  <Label/>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_2'>",
        "    <Button.layoutData>",
        "      <GridData horizontalSpan='2'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
    refresh();
    GridLayoutInfo layout = getObjectByName("layout");
    ControlInfo button_0 = getObjectByName("button_0");
    ControlInfo button_1 = getObjectByName("button_1");
    ControlInfo button_2 = getObjectByName("button_2");
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
    parse(
        "// filler filler filler filler filler",
        "<Shell size='300, 200'>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'/>",
        "  <Button wbp:name='button_1'/>",
        "</Shell>");
    refresh();
    GridLayoutInfo layout = getObjectByName("layout");
    ControlInfo button_0 = getObjectByName("button_0");
    ControlInfo button_1 = getObjectByName("button_1");
    //
    IGridInfo gridInfo = layout.getGridInfo();
    assertEquals(new Rectangle(0, 0, 1, 1), gridInfo.getComponentCells(button_0));
    assertEquals(new Rectangle(0, 1, 1, 1), gridInfo.getComponentCells(button_1));
  }

  /**
   * Test for {@link IGridInfo} when there are no controls.
   */
  public void test_gridInfo_empty() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "</Shell>");
    refresh();
    GridLayoutInfo layout = getObjectByName("layout");
    //
    IGridInfo gridInfo = layout.getGridInfo();
    assertEquals(0, gridInfo.getRowIntervals().length);
    assertEquals(0, gridInfo.getColumnIntervals().length);
  }

  public void test_gridInfo_tooBigHorizontalSpan() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'>",
        "    <Button.layoutData>",
        "      <GridData horizontalSpan='10'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
    refresh();
    GridLayoutInfo layout = getObjectByName("layout");
    ControlInfo button = getObjectByName("button");
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
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<t:MyShell>",
            "  <Button wbp:name='button'/>",
            "</t:MyShell>");
    refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    ControlInfo button = getObjectByName("button");
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
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <GridLayout/>",
            "  </Shell.layout>",
            "</Shell>");
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
    CompositeInfo shell = parse("<t:MyShell/>");
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
    CompositeInfo shell = parse("<t:MyShell/>");
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
  public void test_setCells_horizontalSpan() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'/>",
        "  <Label/>",
        "  <Button/>",
        "  <Button/>",
        "</Shell>");
    refresh();
    GridLayoutInfo layout = getObjectByName("layout");
    ControlInfo button = getObjectByName("button");
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
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'>",
        "    <Button.layoutData>",
        "      <GridData horizontalSpan='2'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Button/>",
        "  <Button/>",
        "</Shell>");
    // check GridData
    {
      assertEquals(0, getInt(gridData, "x"));
      assertEquals(0, getInt(gridData, "y"));
      assertEquals(2, getInt(gridData, "width"));
      assertEquals(1, getInt(gridData, "height"));
    }
  }

  public void test_setCells_horizontalSpan2() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'>",
        "    <Button.layoutData>",
        "      <GridData horizontalSpan='2'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Button/>",
        "  <Button/>",
        "</Shell>");
    refresh();
    GridLayoutInfo layout = getObjectByName("layout");
    ControlInfo button = getObjectByName("button");
    //
    layout.command_setCells(button, new Rectangle(0, 0, 1, 1), true);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'/>",
        "  <Label/>",
        "  <Button/>",
        "  <Button/>",
        "</Shell>");
  }

  public void test_setCells_verticalSpan() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'/>",
        "  <Button text='0'/>",
        "  <Label/>",
        "  <Button text='1'/>",
        "</Shell>");
    refresh();
    GridLayoutInfo layout = getObjectByName("layout");
    ControlInfo button = getObjectByName("button");
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
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'>",
        "    <Button.layoutData>",
        "      <GridData verticalSpan='2'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Button text='0'/>",
        "  <Button text='1'/>",
        "</Shell>");
    // check GridData
    {
      assertEquals(0, getInt(gridData, "x"));
      assertEquals(0, getInt(gridData, "y"));
      assertEquals(1, getInt(gridData, "width"));
      assertEquals(2, getInt(gridData, "height"));
    }
  }

  public void test_setCells_verticalSpan2() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'>",
        "    <Button.layoutData>",
        "      <GridData verticalSpan='2'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Button text='0'/>",
        "  <Button text='1'/>",
        "</Shell>");
    refresh();
    GridLayoutInfo layout = getObjectByName("layout");
    ControlInfo button = getObjectByName("button");
    //
    layout.command_setCells(button, new Rectangle(0, 0, 1, 1), true);
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'/>",
        "  <Button text='0'/>",
        "  <Label/>",
        "  <Button text='1'/>",
        "</Shell>");
  }

  public void test_setCells_move() throws Exception {
    CompositeInfo shell =
        parse(
            "<Shell>",
            "  <Shell.layout>",
            "    <GridLayout wbp:name='layout' numColumns='2'/>",
            "  </Shell.layout>",
            "  <Button wbp:name='button_00'/>",
            "  <Label/>",
            "  <Button wbp:name='button_01'/>",
            "  <Button wbp:name='button_11'/>",
            "</Shell>");
    refresh();
    GridLayoutInfo layout = getObjectByName("layout");
    ControlInfo button_01 = getObjectByName("button_01");
    //
    layout.command_setCells(button_01, new Rectangle(1, 0, 1, 1), true);
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_00'/>",
        "  <Button wbp:name='button_01'/>",
        "  <Label/>",
        "  <Button wbp:name='button_11'/>",
        "</Shell>");
    // check x/y for new filler
    {
      ControlInfo filler = shell.getChildrenControls().get(2);
      assertTrue(layout.isFiller(filler));
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
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    refresh();
    GridLayoutInfo layout = getObjectByName("layout");
    ControlInfo button = getObjectByName("button");
    // set hint
    layout.command_setSizeHint(button, true, new Dimension(200, -1));
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'>",
        "    <Button.layoutData>",
        "      <GridData widthHint='200'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
  }

  /**
   * Test for {@link GridLayoutInfo#command_setSizeHint(ControlInfo, boolean, Dimension)}.
   */
  public void test_setSizeHint_height() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    refresh();
    GridLayoutInfo layout = getObjectByName("layout");
    ControlInfo button = getObjectByName("button");
    // set hint
    layout.command_setSizeHint(button, false, new Dimension(-1, 50));
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'>",
        "    <Button.layoutData>",
        "      <GridData heightHint='50'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
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
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_00'/>",
        "  <Button wbp:name='button_10'/>",
        "  <Button wbp:name='button_01'/>",
        "  <Button wbp:name='button_11'/>",
        "</Shell>");
    refresh();
    ControlInfo button_01 = getObjectByName("button_01");
    //
    button_01.delete();
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_00'/>",
        "  <Button wbp:name='button_10'/>",
        "  <Label/>",
        "  <Button wbp:name='button_11'/>",
        "</Shell>");
  }

  /**
   * When we delete column, we should keep at least one column.
   */
  public void test_delete_keepOneColumn() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    refresh();
    ControlInfo button = getObjectByName("button");
    //
    button.delete();
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout/>",
        "  </Shell.layout>",
        "</Shell>");
  }

  public void test_delete_removeEmptyDimensions() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button/>",
        "  <Label/>",
        "  <Label/>",
        "  <Label/>",
        "  <Label/>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    refresh();
    //
    ControlInfo button = getObjectByName("button");
    {
      GridDataInfo gridData = GridLayoutInfo.getGridData(button);
      assertEquals(1, getInt(gridData, "x"));
      assertEquals(2, getInt(gridData, "y"));
    }
    //
    button.delete();
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout/>",
        "  </Shell.layout>",
        "  <Button/>",
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE_inEmptyCell() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button text='0'/>",
        "  <Label/>",
        "  <Label />",
        "  <Button text='1'/>",
        "</Shell>");
    refresh();
    GridLayoutInfo layout = getObjectByName("layout");
    //
    ControlInfo newButton = createButton();
    layout.command_CREATE(newButton, 1, false, 0, false);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button text='0'/>",
        "  <Button/>",
        "  <Label />",
        "  <Button text='1'/>",
        "</Shell>");
  }

  public void test_CREATE_insertRow() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button text='0'/>",
        "  <Label />",
        "  <Label  />",
        "  <Button text='1'/>",
        "</Shell>");
    refresh();
    GridLayoutInfo layout = getObjectByName("layout");
    //
    ControlInfo newButton = createButton();
    layout.command_CREATE(newButton, 1, false, 1, true);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button text='0'/>",
        "  <Label />",
        "  <Label/>",
        "  <Button/>",
        "  <Label  />",
        "  <Button text='1'/>",
        "</Shell>");
  }

  public void test_CREATE_insertColumn() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button text='0'/>",
        "  <Label />",
        "  <Label  />",
        "  <Button text='1'/>",
        "</Shell>");
    refresh();
    GridLayoutInfo layout = getObjectByName("layout");
    //
    ControlInfo newButton = createButton();
    layout.command_CREATE(newButton, 1, true, 0, false);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='3'/>",
        "  </Shell.layout>",
        "  <Button text='0'/>",
        "  <Button/>",
        "  <Label />",
        "  <Label  />",
        "  <Label/>",
        "  <Button text='1'/>",
        "</Shell>");
  }

  public void test_CREATE_insertColumnRow() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button text='0'/>",
        "</Shell>");
    refresh();
    GridLayoutInfo layout = getObjectByName("layout");
    //
    ControlInfo newButton = createButton();
    layout.command_CREATE(newButton, 0, true, 0, true);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button/>",
        "  <Label/>",
        "  <Label/>",
        "  <Button text='0'/>",
        "</Shell>");
    // delete - should return in initial state
    newButton.delete();
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button text='0'/>",
        "</Shell>");
  }

  public void test_CREATE_appendRow() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button text='0'/>",
        "</Shell>");
    refresh();
    GridLayoutInfo layout = getObjectByName("layout");
    //
    ControlInfo newButton = createButton();
    layout.command_CREATE(newButton, 0, false, 2, false);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button text='0'/>",
        "  <Label/>",
        "  <Button/>",
        "</Shell>");
  }

  public void test_CREATE_appendColumn() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button text='0'/>",
        "</Shell>");
    refresh();
    GridLayoutInfo layout = getObjectByName("layout");
    //
    ControlInfo newButton = createButton();
    layout.command_CREATE(newButton, 2, false, 0, false);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='3'/>",
        "  </Shell.layout>",
        "  <Button text='0'/>",
        "  <Label/>",
        "  <Button/>",
        "</Shell>");
  }

  public void test_CREATE_appendColumnRow() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button text='0'/>",
        "</Shell>");
    refresh();
    GridLayoutInfo layout = getObjectByName("layout");
    //
    ControlInfo newButton = createButton();
    layout.command_CREATE(newButton, 1, false, 1, false);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button text='0'/>",
        "  <Label/>",
        "  <Label/>",
        "  <Button/>",
        "</Shell>");
  }

  public void test_CREATE_insertColumnHorizontalSpan() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'>",
        "    <Button.layoutData>",
        "      <GridData horizontalSpan='2'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Button text='1'/>",
        "  <Button text='2'/>",
        "</Shell>");
    refresh();
    GridLayoutInfo layout = getObjectByName("layout");
    //
    ControlInfo newButton = createButton();
    layout.command_CREATE(newButton, 1, true, 1, false);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='3'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'>",
        "    <Button.layoutData>",
        "      <GridData horizontalSpan='3'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Button text='1'/>",
        "  <Button/>",
        "  <Button text='2'/>",
        "</Shell>");
  }

  public void test_CREATE_insertRowVerticalSpan() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'>",
        "    <Button.layoutData>",
        "      <GridData verticalSpan='2'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Button text='1'/>",
        "  <Button text='2'/>",
        "</Shell>");
    refresh();
    GridLayoutInfo layout = getObjectByName("layout");
    //
    ControlInfo newButton = createButton();
    layout.command_CREATE(newButton, 1, false, 1, true);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'>",
        "    <Button.layoutData>",
        "      <GridData verticalSpan='3'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Button text='1'/>",
        "  <Button/>",
        "  <Button text='2'/>",
        "</Shell>");
  }

  /**
   * Test for parsing "not balanced" {@link GridLayoutInfo} and adding into <code>null</code> cell.
   */
  public void test_CREATE_notBalanced() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Label/>",
        "  <Label/>",
        "  <Label/>",
        "</Shell>");
    refresh();
    GridLayoutInfo layout = getObjectByName("layout");
    //
    ControlInfo newButton = createButton();
    layout.command_CREATE(newButton, 1, false, 1, false);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Label/>",
        "  <Label/>",
        "  <Label/>",
        "  <Button/>",
        "</Shell>");
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
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<t:MyShell>",
            "  <Button wbp:name='button'/>",
            "</t:MyShell>");
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
  public void test_implicitLayout_CREATE_hasInheritedControls() throws Exception {
    prepareShell_withImplicit();
    CompositeInfo shell = parse("<t:MyShell/>");
    refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    // initial state
    assertEquals(2, layout.getColumns().size());
    assertEquals(1, layout.getRows().size());
    // add new Button
    ControlInfo newButton = createButton();
    layout.command_CREATE(newButton, 1, false, 1, false);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<t:MyShell>",
        "  <Label/>",
        "  <Button/>",
        "</t:MyShell>");
  }

  /**
   * In XWT, if {@link GridLayout} is implicit and there are no inherited {@link Control}s, we
   * should be able to add new columns and rows.
   */
  public void test_implicitLayout_CREATE_noInheritedControls() throws Exception {
    prepareShell_withImplicitEmpty();
    CompositeInfo shell = parse("<t:MyShell/>");
    refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    // initial state
    assertEquals(0, layout.getColumns().size());
    assertEquals(0, layout.getRows().size());
    // add new Button
    ControlInfo newButton = createButton();
    layout.command_CREATE(newButton, 1, false, 0, false);
    // new state
    assertEquals(2, layout.getColumns().size());
    assertEquals(1, layout.getRows().size());
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<t:MyShell numColumns='2'>",
        "  <Label/>",
        "  <Button/>",
        "</t:MyShell>");
  }

  /**
   * Using implicit {@link GridLayout} should not cause problems during moving {@link Control}.
   */
  public void test_implicitLayout_MOVE() throws Exception {
    prepareShell_withImplicit();
    CompositeInfo shell =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<t:MyShell>",
            "  <Button wbp:name='button'/>",
            "  <Label/>",
            "</t:MyShell>");
    refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    ControlInfo button = getObjectByName("button");
    // move "button"
    layout.command_MOVE(button, 1, false, 1, false);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<t:MyShell>",
        "  <Label/>",
        "  <Button wbp:name='button'/>",
        "</t:MyShell>");
  }

  private void prepareShell_withImplicit() throws Exception {
    setFileContentSrc(
        "test/MyShell.java",
        getJavaSource(
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
        getJavaSource(
            "public class MyShell extends Shell {",
            "  private final GridLayout gridLayout = new GridLayout(1, false);",
            "  public MyShell() {",
            "    setLayout(gridLayout);",
            "  }",
            "  protected void checkSubclass() {",
            "  }",
            "  public void setNumColumns(int numColumns) {",
            "    gridLayout.numColumns = numColumns;",
            "    layout();",
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
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_00'/>",
        "  <Label/>",
        "  <Button wbp:name='button_01'/>",
        "  <Button wbp:name='button_11'/>",
        "</Shell>");
    refresh();
    GridLayoutInfo layout = getObjectByName("layout");
    final GridColumnInfo<ControlInfo> column = layout.getColumns().get(0);
    // check initial values
    assertEquals(0, column.getIndex());
    assertEquals("left", column.getTitle());
    assertFalse(column.getGrab());
    assertEquals(SWT.LEFT, column.getAlignment().intValue());
    // flip grab
    column.flipGrab();
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_00'>",
        "    <Button.layoutData>",
        "      <GridData grabExcessHorizontalSpace='true'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Label/>",
        "  <Button wbp:name='button_01'>",
        "    <Button.layoutData>",
        "      <GridData grabExcessHorizontalSpace='true'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Button wbp:name='button_11'/>",
        "</Shell>");
    assertEquals("left, grab", column.getTitle());
    // set alignment
    column.setAlignment(SWT.FILL);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_00'>",
        "    <Button.layoutData>",
        "      <GridData grabExcessHorizontalSpace='true' horizontalAlignment='FILL'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Label/>",
        "  <Button wbp:name='button_01'>",
        "    <Button.layoutData>",
        "      <GridData grabExcessHorizontalSpace='true' horizontalAlignment='FILL'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Button wbp:name='button_11'/>",
        "</Shell>");
    assertEquals("fill, grab", column.getTitle());
    // set different alignment for "button_01"
    {
      ControlInfo button = getObjectByName("button_01");
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
    ExecutionUtils.run(layout, new RunnableEx() {
      public void run() throws Exception {
        column.delete();
      }
    });
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Label/>",
        "  <Button wbp:name='button_11'/>",
        "</Shell>");
  }

  public void test_rowAccess() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_00'/>",
        "  <Label/>",
        "  <Button wbp:name='button_01'/>",
        "  <Button wbp:name='button_11'/>",
        "</Shell>");
    refresh();
    GridLayoutInfo layout = getObjectByName("layout");
    GridRowInfo<ControlInfo> row = layout.getRows().get(1);
    // check initial values
    assertEquals(1, row.getIndex());
    assertEquals("center", row.getTitle());
    assertFalse(row.getGrab());
    assertEquals(SWT.CENTER, row.getAlignment().intValue());
    // flip grab
    row.flipGrab();
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_00'/>",
        "  <Label/>",
        "  <Button wbp:name='button_01'>",
        "    <Button.layoutData>",
        "      <GridData grabExcessVerticalSpace='true'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Button wbp:name='button_11'>",
        "    <Button.layoutData>",
        "      <GridData grabExcessVerticalSpace='true'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
    assertEquals("center, grab", row.getTitle());
    // set alignment
    row.setAlignment(SWT.FILL);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_00'/>",
        "  <Label/>",
        "  <Button wbp:name='button_01'>",
        "    <Button.layoutData>",
        "      <GridData grabExcessVerticalSpace='true' verticalAlignment='FILL'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Button wbp:name='button_11'>",
        "    <Button.layoutData>",
        "      <GridData grabExcessVerticalSpace='true' verticalAlignment='FILL'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
    assertEquals("fill, grab", row.getTitle());
    // set different alignment for "button_01"
    {
      ControlInfo button = getObjectByName("button_01");
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
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_00'/>",
        "</Shell>");
  }

  public void test_deleteColumn() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='3'/>",
        "  </Shell.layout>",
        "  <Button>",
        "    <Button.layoutData>",
        "      <GridData horizontalSpan='3'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Button text='0'/>",
        "  <Button text='1'/>",
        "  <Button text='2'/>",
        "</Shell>");
    refresh();
    //
    ExecutionUtils.run(m_lastObject, new RunnableEx() {
      public void run() throws Exception {
        GridLayoutInfo layout = getObjectByName("layout");
        layout.command_deleteColumn(1, true);
      }
    });
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button>",
        "    <Button.layoutData>",
        "      <GridData horizontalSpan='2'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Button text='0'/>",
        "  <Button text='2'/>",
        "</Shell>");
  }

  public void test_deleteColumn_deleteAlsoEmptyRows() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_00'/>",
        "  <Label/>",
        "  <Label/>",
        "  <Button wbp:name='button_11'/>",
        "</Shell>");
    refresh();
    //
    ExecutionUtils.run(m_lastObject, new RunnableEx() {
      public void run() throws Exception {
        GridLayoutInfo layout = getObjectByName("layout");
        layout.command_deleteColumn(1, true);
      }
    });
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_00'/>",
        "</Shell>");
  }

  /**
   * When grid has empty cells (at the ends of columns/rows), this caused
   * {@link NullPointerException}.
   */
  public void test_delete_missingFillers() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_00'/>",
        "  <Button wbp:name='button_01'/>",
        "  <Button wbp:name='button_10'/>",
        "</Shell>");
    refresh();
    // delete "button_10"
    ControlInfo button_10 = getObjectByName("button_10");
    button_10.delete();
    // ..."row 1" should be removed
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_00'/>",
        "  <Button wbp:name='button_01'/>",
        "</Shell>");
  }

  public void test_deleteRow() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button>",
        "    <Button.layoutData>",
        "      <GridData verticalSpan='3'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Button text='0'/>",
        "  <Button text='1'/>",
        "  <Button text='2'/>",
        "</Shell>");
    refresh();
    //
    ExecutionUtils.run(m_lastObject, new RunnableEx() {
      public void run() throws Exception {
        GridLayoutInfo layout = getObjectByName("layout");
        layout.command_deleteRow(1, true);
      }
    });
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button>",
        "    <Button.layoutData>",
        "      <GridData verticalSpan='2'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Button text='0'/>",
        "  <Button text='2'/>",
        "</Shell>");
  }

  public void test_deleteRow_deleteAlsoEmptyColumns() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_00'/>",
        "  <Label/>",
        "  <Label/>",
        "  <Button wbp:name='button_11'/>",
        "</Shell>");
    refresh();
    //
    ExecutionUtils.run(m_lastObject, new RunnableEx() {
      public void run() throws Exception {
        GridLayoutInfo layout = getObjectByName("layout");
        layout.command_deleteRow(1, true);
      }
    });
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_00'/>",
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE COLUMN
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_MOVE_COLUMN_before() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_00'/>",
        "  <Label/>",
        "  <Label/>",
        "  <Button wbp:name='button_11'/>",
        "</Shell>");
    refresh();
    //
    ExecutionUtils.run(m_lastObject, new RunnableEx() {
      public void run() throws Exception {
        GridLayoutInfo layout = getObjectByName("layout");
        layout.command_MOVE_COLUMN(1, 0);
      }
    });
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Label/>",
        "  <Button wbp:name='button_00'/>",
        "  <Button wbp:name='button_11'/>",
        "  <Label/>",
        "</Shell>");
  }

  public void test_MOVE_COLUMN_after() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_00'/>",
        "  <Label/>",
        "  <Label/>",
        "  <Button wbp:name='button_11'/>",
        "</Shell>");
    refresh();
    //
    ExecutionUtils.run(m_lastObject, new RunnableEx() {
      public void run() throws Exception {
        GridLayoutInfo layout = getObjectByName("layout");
        layout.command_MOVE_COLUMN(0, 2);
      }
    });
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Label/>",
        "  <Button wbp:name='button_00'/>",
        "  <Button wbp:name='button_11'/>",
        "  <Label/>",
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE ROW
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_MOVE_ROW_before() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_00'/>",
        "  <Label/>",
        "  <Label/>",
        "  <Button wbp:name='button_11'/>",
        "</Shell>");
    refresh();
    //
    ExecutionUtils.run(m_lastObject, new RunnableEx() {
      public void run() throws Exception {
        GridLayoutInfo layout = getObjectByName("layout");
        layout.command_MOVE_ROW(1, 0);
      }
    });
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Label/>",
        "  <Button wbp:name='button_11'/>",
        "  <Button wbp:name='button_00'/>",
        "  <Label/>",
        "</Shell>");
  }

  public void test_MOVE_ROW_after() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_00'/>",
        "  <Label/>",
        "  <Label/>",
        "  <Button wbp:name='button_11'/>",
        "</Shell>");
    refresh();
    //
    ExecutionUtils.run(m_lastObject, new RunnableEx() {
      public void run() throws Exception {
        GridLayoutInfo layout = getObjectByName("layout");
        layout.command_MOVE_ROW(0, 2);
      }
    });
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Label/>",
        "  <Button wbp:name='button_11'/>",
        "  <Button wbp:name='button_00'/>",
        "  <Label/>",
        "</Shell>");
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
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button>",
        "    <Button.layoutData>",
        "      <GridData horizontalSpan='2'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
    refresh();
    GridLayoutInfo layout = getObjectByName("layout");
    //
    layout.command_normalizeSpanning();
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button/>",
        "</Shell>");
  }

  /**
   * Test for {@link GridLayoutInfo#command_normalizeSpanning()}.<br>
   * Single control spanned on two rows.
   */
  public void test_normalizeSpanning_2() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button>",
        "    <Button.layoutData>",
        "      <GridData verticalSpan='2'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
    refresh();
    GridLayoutInfo layout = getObjectByName("layout");
    //
    layout.command_normalizeSpanning();
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button/>",
        "</Shell>");
  }

  /**
   * Test for {@link GridLayoutInfo#command_normalizeSpanning()}.<br>
   * No normalize: each column/row has control.
   */
  public void test_normalizeSpanning_3() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button>",
        "    <Button.layoutData>",
        "      <GridData horizontalSpan='2'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Label/>",
        "  <Button/>",
        "</Shell>");
    refresh();
    GridLayoutInfo layout = getObjectByName("layout");
    //
    layout.command_normalizeSpanning();
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button>",
        "    <Button.layoutData>",
        "      <GridData horizontalSpan='2'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Label/>",
        "  <Button/>",
        "</Shell>");
  }

  /**
   * Test for {@link GridLayoutInfo#command_normalizeSpanning()}.<br>
   * Do normalize: no control for second column.
   */
  public void test_normalizeSpanning_4() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_1'>",
        "    <Button.layoutData>",
        "      <GridData horizontalSpan='2'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Button wbp:name='button_2'/>",
        "  <Label/>",
        "</Shell>");
    refresh();
    GridLayoutInfo layout = getObjectByName("layout");
    //
    layout.command_normalizeSpanning();
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_2'/>",
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_MOVE() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_00'/>",
        "  <Label/>",
        "  <Button wbp:name='button_10'/>",
        "  <Button wbp:name='button_11'/>",
        "</Shell>");
    refresh();
    GridLayoutInfo layout = getObjectByName("layout");
    ControlInfo button = getObjectByName("button_10");
    //
    layout.command_MOVE(button, 1, false, 0, false);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_00'/>",
        "  <Button wbp:name='button_10'/>",
        "  <Label/>",
        "  <Button wbp:name='button_11'/>",
        "</Shell>");
  }

  public void test_MOVE_out() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout/>",
        "  </Shell.layout>",
        "  <Composite>",
        "    <Composite.layout>",
        "      <FillLayout wbp:name='layout'/>",
        "    </Composite.layout>",
        "  </Composite>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    refresh();
    LayoutInfo layout = getObjectByName("layout");
    ControlInfo button = getObjectByName("button");
    //
    layout.command_MOVE(button, null);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout/>",
        "  </Shell.layout>",
        "  <Composite>",
        "    <Composite.layout>",
        "      <FillLayout wbp:name='layout'/>",
        "    </Composite.layout>",
        "    <Button wbp:name='button'/>",
        "  </Composite>",
        "</Shell>");
  }

  public void test_MOVE_error_1() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='4'/>",
        "  </Shell.layout>",
        "  <Label/>",
        "  <Label/>",
        "  <Label/>",
        "  <Label/>",
        "  <Label/>",
        "  <Label/>",
        "  <Label/>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    refresh();
    GridLayoutInfo layout = getObjectByName("layout");
    ControlInfo button = getObjectByName("button");
    //
    layout.command_MOVE(button, 1, false, 0, false);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Label/>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
  }

  public void test_MOVE_error_2() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='3'/>",
        "  </Shell.layout>",
        "  <Label/>",
        "  <Label/>",
        "  <Label/>",
        "  <Label/>",
        "  <Label/>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    refresh();
    GridLayoutInfo layout = getObjectByName("layout");
    ControlInfo button = getObjectByName("button");
    //
    layout.command_MOVE(button, 0, false, 0, false);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ADD
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_ADD() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Composite>",
        "    <Composite.layout>",
        "      <RowLayout/>",
        "    </Composite.layout>",
        "    <Button wbp:name='button'/>",
        "  </Composite>",
        "</Shell>");
    refresh();
    GridLayoutInfo layout = getObjectByName("layout");
    ControlInfo button = getObjectByName("button");
    //
    layout.command_ADD(button, 0, false, 1, false);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Composite>",
        "    <Composite.layout>",
        "      <RowLayout/>",
        "    </Composite.layout>",
        "  </Composite>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Special cases
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE_noReference() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "</Shell>");
    refresh();
    GridLayoutInfo layout = getObjectByName("layout");
    //
    ControlInfo newButton = createButton();
    layout.command_CREATE(newButton, 0, false, 0, false);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button/>",
        "</Shell>");
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
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_2'/>",
        "  <Button wbp:name='button_3'/>",
        "</Shell>");
    refresh();
    GridLayoutInfo layout = getObjectByName("layout");
    // initial state
    assertEquals(2, layout.getColumns().size());
    assertEquals(2, layout.getRows().size());
    // numColumn = 3
    layout.getPropertyByTitle("numColumns").setValue(3);
    assertEquals(3, layout.getColumns().size());
    assertEquals(1, layout.getRows().size());
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='3'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_2'/>",
        "  <Button wbp:name='button_3'/>",
        "</Shell>");
  }

  /**
   * There were problems when we increase/decrease number of columns using "numColumns" property.
   */
  public void test_numColumns_dec() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='3'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_2'/>",
        "  <Button wbp:name='button_3'/>",
        "</Shell>");
    refresh();
    GridLayoutInfo layout = getObjectByName("layout");
    // initial state
    assertEquals(3, layout.getColumns().size());
    assertEquals(1, layout.getRows().size());
    // numColumn = 2
    layout.getPropertyByTitle("numColumns").setValue(2);
    assertEquals(2, layout.getColumns().size());
    assertEquals(2, layout.getRows().size());
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_2'/>",
        "  <Button wbp:name='button_3'/>",
        "</Shell>");
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
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    refresh();
    GridLayoutInfo layout = getObjectByName("layout");
    ControlInfo button = getObjectByName("button");
    // initially "button" is managed
    assertThat(layout.getControls()).containsOnly(button);
    // exclude "button"
    GridLayoutInfo.getGridData(button).getPropertyByTitle("exclude").setValue(true);
    assertThat(layout.getControls()).isEmpty();
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'>",
        "    <Button.layoutData>",
        "      <GridData exclude='true'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
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
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <GridLayout wbp:name='layout'/>",
            "  </Shell.layout>",
            "  <Label/>",
            "  <Button/>",
            "</Shell>");
    refresh();
    GridLayoutInfo layout = getObjectByName("layout");
    // initially 2 controls - filler and Button
    assertThat(shell.getChildrenControls()).hasSize(2);
    // after delete - only Button
    layout.delete();
    assertThat(shell.getChildrenControls()).hasSize(1);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button/>",
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_clipboard() throws Exception {
    final CompositeInfo shell =
        parse(
            "<Shell>",
            "  <Shell.layout>",
            "    <FillLayout wbp:name='layout'/>",
            "  </Shell.layout>",
            "  <Composite wbp:name='composite'>",
            "    <Composite.layout>",
            "      <GridLayout numColumns='2'/>",
            "    </Composite.layout>",
            "    <Button text='1'>",
            "      <Button.layoutData>",
            "        <GridData horizontalAlignment='FILL'/>",
            "      </Button.layoutData>",
            "    </Button>",
            "    <Label/>",
            "    <Label/>",
            "    <Button text='2'/>",
            "  </Composite>",
            "</Shell>");
    refresh();
    //
    ControlInfo composite = getObjectByName("composite");
    doCopyPaste(composite, new PasteProcedure<ControlInfo>() {
      public void run(ControlInfo copy) throws Exception {
        shell.getLayout().command_CREATE(copy, null);
      }
    });
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Composite wbp:name='composite'>",
        "    <Composite.layout>",
        "      <GridLayout numColumns='2'/>",
        "    </Composite.layout>",
        "    <Button text='1'>",
        "      <Button.layoutData>",
        "        <GridData horizontalAlignment='FILL'/>",
        "      </Button.layoutData>",
        "    </Button>",
        "    <Label/>",
        "    <Label/>",
        "    <Button text='2'/>",
        "  </Composite>",
        "  <Composite>",
        "    <Composite.layout>",
        "      <GridLayout numColumns='2'/>",
        "    </Composite.layout>",
        "    <Button text='1'>",
        "      <Button.layoutData>",
        "        <GridData horizontalAlignment='FILL'/>",
        "      </Button.layoutData>",
        "    </Button>",
        "    <Label/>",
        "    <Label/>",
        "    <Button text='2'/>",
        "  </Composite>",
        "</Shell>");
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