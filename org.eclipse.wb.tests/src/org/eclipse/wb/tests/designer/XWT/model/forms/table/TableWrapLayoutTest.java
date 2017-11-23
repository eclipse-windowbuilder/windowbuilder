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
package org.eclipse.wb.tests.designer.XWT.model.forms.table;

import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Interval;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.TableWrapColumnInfo;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.TableWrapLayoutImages;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.TableWrapRowInfo;
import org.eclipse.wb.internal.xwt.model.forms.layout.table.TableWrapDataInfo;
import org.eclipse.wb.internal.xwt.model.forms.layout.table.TableWrapLayoutInfo;
import org.eclipse.wb.internal.xwt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

import org.eclipse.ui.forms.widgets.TableWrapData;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link TableWrapLayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class TableWrapLayoutTest extends XwtModelTest {
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
  // Source
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getTestSource_namespaces() {
    return super.getTestSource_namespaces()
        + " xmlns:f='clr-namespace:org.eclipse.ui.forms.widgets'";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Images
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_images() throws Exception {
    assertNotNull(TableWrapLayoutImages.getImage("h/left.gif"));
    assertNotNull(TableWrapLayoutImages.getImageDescriptor("v/top.gif"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for parsing empty {@link TableWrapLayoutInfo}.
   */
  public void test_parseEmpty() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "</Shell>");
    refresh();
  }

  /**
   * Fillers should be filtered out from presentation children.
   */
  public void test_excludeFillersFromPresentationChildren() throws Exception {
    CompositeInfo shell =
        parse(
            "<Shell>",
            "  <Shell.layout>",
            "    <f:TableWrapLayout wbp:name='layout'/>",
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
   * Test for {@link IGridInfo}.
   */
  public void test_gridInfo() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'/>",
        "  <Label/>",
        "  <Label/>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_2'>",
        "    <Button.layoutData>",
        "      <f:TableWrapData colspan='2'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
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
    // insets
    assertEquals(new Insets(0, 0, 0, 0), gridInfo.getInsets());
    // check "virtual" feedback sizes
    {
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
   * Test for {@link IGridInfo} when there are no controls.
   */
  public void test_gridInfo_empty() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
    //
    IGridInfo gridInfo = layout.getGridInfo();
    assertEquals(0, gridInfo.getRowIntervals().length);
    assertEquals(0, gridInfo.getColumnIntervals().length);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setCells()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_setCells_horizontalSpan() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'/>",
        "  <Label/>",
        "  <Label/>",
        "  <Button wbp:name='button_1'/>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
    ControlInfo button = getObjectByName("button_0");
    TableWrapDataInfo layoutData = layout.getTableWrapData(button);
    // check initial TableWrapData
    {
      assertEquals(0, getInt(layoutData, "x"));
      assertEquals(0, getInt(layoutData, "y"));
      assertEquals(1, getInt(layoutData, "width"));
      assertEquals(1, getInt(layoutData, "height"));
    }
    // set horizontal span
    layout.command_setCells(button, new Rectangle(0, 0, 2, 1), true);
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'>",
        "    <Button.layoutData>",
        "      <f:TableWrapData colspan='2'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Label/>",
        "  <Button wbp:name='button_1'/>",
        "</Shell>");
    // check TableWrapData
    {
      assertEquals(0, getInt(layoutData, "x"));
      assertEquals(0, getInt(layoutData, "y"));
      assertEquals(2, getInt(layoutData, "width"));
      assertEquals(1, getInt(layoutData, "height"));
    }
  }

  public void test_setCells_horizontalSpan2() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'>",
        "    <Button.layoutData>",
        "      <f:TableWrapData colspan='2'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Label/>",
        "  <Button wbp:name='button_1'/>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
    ControlInfo button = getObjectByName("button_0");
    //
    layout.command_setCells(button, new Rectangle(0, 0, 1, 1), true);
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'/>",
        "  <Label/>",
        "  <Label/>",
        "  <Button wbp:name='button_1'/>",
        "</Shell>");
  }

  public void test_setCells_verticalSpan() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'/>",
        "  <Label/>",
        "  <Label/>",
        "  <Button wbp:name='button_1'/>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
    ControlInfo button = getObjectByName("button_0");
    TableWrapDataInfo layoutData = layout.getTableWrapData(button);
    // check initial TableWrapData
    {
      assertEquals(0, getInt(layoutData, "x"));
      assertEquals(0, getInt(layoutData, "y"));
      assertEquals(1, getInt(layoutData, "width"));
      assertEquals(1, getInt(layoutData, "height"));
    }
    // set vertical span
    layout.command_setCells(button, new Rectangle(0, 0, 1, 2), true);
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'>",
        "    <Button.layoutData>",
        "      <f:TableWrapData rowspan='2'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Label/>",
        "  <Button wbp:name='button_1'/>",
        "</Shell>");
    // check TableWrapData
    {
      assertEquals(0, getInt(layoutData, "x"));
      assertEquals(0, getInt(layoutData, "y"));
      assertEquals(1, getInt(layoutData, "width"));
      assertEquals(2, getInt(layoutData, "height"));
    }
  }

  public void test_setCells_verticalSpan2() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'>",
        "    <Button.layoutData>",
        "      <f:TableWrapData rowspan='2'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Label/>",
        "  <Button wbp:name='button_1'/>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
    ControlInfo button = getObjectByName("button_0");
    //
    layout.command_setCells(button, new Rectangle(0, 0, 1, 1), true);
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'/>",
        "  <Label/>",
        "  <Label/>",
        "  <Button wbp:name='button_1'/>",
        "</Shell>");
  }

  public void test_setCells_move() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'/>",
        "  <Label/>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_2'/>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
    ControlInfo button = getObjectByName("button_0");
    //
    layout.command_setCells(button, new Rectangle(1, 0, 1, 1), true);
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Label/>",
        "  <Button wbp:name='button_0'/>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_2'/>",
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
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'/>",
        "  <Label/>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_2'/>",
        "</Shell>");
    refresh();
    ControlInfo button = getObjectByName("button_1");
    //
    button.delete();
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'/>",
        "  <Label/>",
        "  <Label/>",
        "  <Button wbp:name='button_2'/>",
        "</Shell>");
  }

  /**
   * When we delete column, we should keep at least one column.
   */
  public void test_delete_keepOneColumn() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout/>",
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
        "    <f:TableWrapLayout/>",
        "  </Shell.layout>",
        "</Shell>");
  }

  public void test_delete_removeEmptyDimensions() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'/>",
        "  <Label/>",
        "  <Label/>",
        "  <Label/>",
        "  <Label/>",
        "  <Button wbp:name='button_1'/>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
    ControlInfo button = getObjectByName("button_1");
    //
    {
      TableWrapDataInfo layoutData = layout.getTableWrapData(button);
      assertEquals(1, getInt(layoutData, "x"));
      assertEquals(2, getInt(layoutData, "y"));
    }
    //
    button.delete();
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'/>",
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE_inEmptyCell() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button text='0'/>",
        "  <Label/>",
        "  <Label />",
        "  <Button text='1'/>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
    //
    ControlInfo newButton = createButton();
    layout.command_CREATE(newButton, 1, false, 0, false);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button text='0'/>",
        "  <Button/>",
        "  <Label />",
        "  <Button text='1'/>",
        "</Shell>");
  }

  public void test_CREATE_insertRow() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button text='0'/>",
        "  <Label />",
        "  <Label  />",
        "  <Button text='1'/>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
    //
    ControlInfo newButton = createButton();
    layout.command_CREATE(newButton, 1, false, 1, true);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
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
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button text='0'/>",
        "  <Label />",
        "  <Label  />",
        "  <Button text='1'/>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
    //
    ControlInfo newButton = createButton();
    layout.command_CREATE(newButton, 1, true, 0, false);
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='3'/>",
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
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button text='0'/>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
    //
    ControlInfo newButton = createButton();
    layout.command_CREATE(newButton, 0, true, 0, true);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button/>",
        "  <Label/>",
        "  <Label/>",
        "  <Button text='0'/>",
        "</Shell>");
    // delete - should return in initial state
    newButton.delete();
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button text='0'/>",
        "</Shell>");
  }

  public void test_CREATE_appendRow() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button text='0'/>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
    //
    ControlInfo newButton = createButton();
    layout.command_CREATE(newButton, 0, false, 2, false);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button text='0'/>",
        "  <Label/>",
        "  <Button/>",
        "</Shell>");
  }

  public void test_CREATE_appendColumn() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button text='0'/>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
    //
    ControlInfo newButton = createButton();
    layout.command_CREATE(newButton, 2, false, 0, false);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='3'/>",
        "  </Shell.layout>",
        "  <Button text='0'/>",
        "  <Label/>",
        "  <Button/>",
        "</Shell>");
  }

  public void test_CREATE_appendColumnRow() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button text='0'/>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
    //
    ControlInfo newButton = createButton();
    layout.command_CREATE(newButton, 1, false, 1, false);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button text='0'/>",
        "  <Label/>",
        "  <Label/>",
        "  <Button/>",
        "</Shell>");
  }

  public void test_CREATE_insertColumnHorizontalSpan() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'>",
        "    <Button.layoutData>",
        "      <f:TableWrapData colspan='2'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_2'/>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
    //
    ControlInfo newButton = createButton();
    layout.command_CREATE(newButton, 1, true, 1, false);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='3'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'>",
        "    <Button.layoutData>",
        "      <f:TableWrapData colspan='3'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Button wbp:name='button_1'/>",
        "  <Button/>",
        "  <Button wbp:name='button_2'/>",
        "</Shell>");
  }

  public void test_CREATE_insertRowVerticalSpan() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'>",
        "    <Button.layoutData>",
        "      <f:TableWrapData rowspan='2'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_2'/>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
    //
    ControlInfo newButton = createButton();
    layout.command_CREATE(newButton, 1, false, 1, true);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'>",
        "    <Button.layoutData>",
        "      <f:TableWrapData rowspan='3'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Button wbp:name='button_1'/>",
        "  <Button/>",
        "  <Button wbp:name='button_2'/>",
        "</Shell>");
  }

  /**
   * Test for parsing "not balanced" {@link TableWrapLayoutInfo} and adding into <code>null</code>
   * cell.
   */
  public void test_CREATE_notBalanced() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Label/>",
        "  <Label/>",
        "  <Label/>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
    //
    ControlInfo newButton = createButton();
    layout.command_CREATE(newButton, 1, false, 1, false);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Label/>",
        "  <Label/>",
        "  <Label/>",
        "  <Button/>",
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dimension operations
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_columnAccess() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'/>",
        "  <Label/>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_2'/>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
    ControlInfo button_1 = getObjectByName("button_1");
    final TableWrapColumnInfo<?> column = layout.getColumns().get(0);
    // check initial values
    assertEquals(0, column.getIndex());
    assertEquals("left", column.getTitle());
    assertFalse(column.getGrab());
    assertEquals(TableWrapData.LEFT, column.getAlignment().intValue());
    // flip grab
    column.flipGrab();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'>",
        "    <Button.layoutData>",
        "      <f:TableWrapData grabHorizontal='true'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Label/>",
        "  <Button wbp:name='button_1'>",
        "    <Button.layoutData>",
        "      <f:TableWrapData grabHorizontal='true'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Button wbp:name='button_2'/>",
        "</Shell>");
    assertEquals("left, grab", column.getTitle());
    // set alignment
    ExecutionUtils.run(m_lastObject, new RunnableEx() {
      public void run() throws Exception {
        column.setAlignment(TableWrapData.FILL);
      }
    });
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'>",
        "    <Button.layoutData>",
        "      <f:TableWrapData grabHorizontal='true'"
            + " align='(org.eclipse.ui.forms.widgets.TableWrapData).FILL'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Label/>",
        "  <Button wbp:name='button_1'>",
        "    <Button.layoutData>",
        "      <f:TableWrapData grabHorizontal='true'"
            + " align='(org.eclipse.ui.forms.widgets.TableWrapData).FILL'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Button wbp:name='button_2'/>",
        "</Shell>");
    assertEquals("fill, grab", column.getTitle());
    // set different alignment for "0 x 1" button
    {
      layout.getTableWrapData(button_1).setHorizontalAlignment(TableWrapData.RIGHT);
      assertNull(column.getAlignment());
    }
    // check other alignments
    {
      column.setAlignment(TableWrapData.CENTER);
      assertEquals("center, grab", column.getTitle());
      //
      column.setAlignment(TableWrapData.RIGHT);
      assertEquals("right, grab", column.getTitle());
    }
    // delete
    column.delete();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_2'/>",
        "</Shell>");
  }

  public void test_rowAccess() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'/>",
        "  <Label/>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_2'/>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
    ControlInfo button_1 = getObjectByName("button_1");
    final TableWrapRowInfo<?> row = layout.getRows().get(1);
    // check initial values
    assertEquals(1, row.getIndex());
    assertEquals("top", row.getTitle());
    assertFalse(row.getGrab());
    assertEquals(TableWrapData.TOP, row.getAlignment().intValue());
    // flip grab
    row.flipGrab();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'/>",
        "  <Label/>",
        "  <Button wbp:name='button_1'>",
        "    <Button.layoutData>",
        "      <f:TableWrapData grabVertical='true'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Button wbp:name='button_2'>",
        "    <Button.layoutData>",
        "      <f:TableWrapData grabVertical='true'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
    assertEquals("top, grab", row.getTitle());
    // set alignment
    ExecutionUtils.run(m_lastObject, new RunnableEx() {
      public void run() throws Exception {
        row.setAlignment(TableWrapData.FILL);
      }
    });
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'/>",
        "  <Label/>",
        "  <Button wbp:name='button_1'>",
        "    <Button.layoutData>",
        "      <f:TableWrapData grabVertical='true'"
            + " valign='(org.eclipse.ui.forms.widgets.TableWrapData).FILL'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Button wbp:name='button_2'>",
        "    <Button.layoutData>",
        "      <f:TableWrapData grabVertical='true'"
            + " valign='(org.eclipse.ui.forms.widgets.TableWrapData).FILL'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
    assertEquals("fill, grab", row.getTitle());
    // set different alignment for "0 x 1" button
    {
      layout.getTableWrapData(button_1).setVerticalAlignment(TableWrapData.BOTTOM);
      assertNull(row.getAlignment());
    }
    // check other alignments
    {
      row.setAlignment(TableWrapData.TOP);
      assertEquals("top, grab", row.getTitle());
      //
      row.setAlignment(TableWrapData.MIDDLE);
      assertEquals("middle, grab", row.getTitle());
      //
      row.setAlignment(TableWrapData.BOTTOM);
      assertEquals("bottom, grab", row.getTitle());
    }
    // delete
    row.delete();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'/>",
        "</Shell>");
  }

  public void test_deleteColumn() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='3'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'>",
        "    <Button.layoutData>",
        "      <f:TableWrapData colspan='3'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_2'/>",
        "  <Button wbp:name='button_3'/>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
    //
    try {
      m_lastObject.startEdit();
      layout.command_deleteColumn(1, true);
    } finally {
      m_lastObject.endEdit();
    }
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'>",
        "    <Button.layoutData>",
        "      <f:TableWrapData colspan='2'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_3'/>",
        "</Shell>");
  }

  public void test_deleteColumn_deleteAlsoEmptyRows() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'/>",
        "  <Label/>",
        "  <Label/>",
        "  <Button wbp:name='button_1'/>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
    //
    try {
      m_lastObject.startEdit();
      layout.command_deleteColumn(1, true);
    } finally {
      m_lastObject.endEdit();
    }
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'/>",
        "</Shell>");
  }

  public void test_deleteRow() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'>",
        "    <Button.layoutData>",
        "      <f:TableWrapData rowspan='3'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_2'/>",
        "  <Button wbp:name='button_3'/>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
    //
    try {
      m_lastObject.startEdit();
      layout.command_deleteRow(1, true);
    } finally {
      m_lastObject.endEdit();
    }
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'>",
        "    <Button.layoutData>",
        "      <f:TableWrapData rowspan='2'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_3'/>",
        "</Shell>");
  }

  public void test_deleteRow_deleteAlsoEmptyColumns() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'/>",
        "  <Label/>",
        "  <Label/>",
        "  <Button wbp:name='button_1'/>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
    //
    try {
      m_lastObject.startEdit();
      layout.command_deleteRow(1, true);
    } finally {
      m_lastObject.endEdit();
    }
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'/>",
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE COLUMN
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_MOVE_COLUMN_before() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'/>",
        "  <Label/>",
        "  <Label/>",
        "  <Button wbp:name='button_1'/>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
    //
    try {
      layout.startEdit();
      layout.command_MOVE_COLUMN(1, 0);
    } finally {
      layout.endEdit();
    }
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Label/>",
        "  <Button wbp:name='button_0'/>",
        "  <Button wbp:name='button_1'/>",
        "  <Label/>",
        "</Shell>");
  }

  public void test_MOVE_COLUMN_after() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'/>",
        "  <Label/>",
        "  <Label/>",
        "  <Button wbp:name='button_1'/>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
    //
    try {
      layout.startEdit();
      layout.command_MOVE_COLUMN(0, 2);
    } finally {
      layout.endEdit();
    }
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Label/>",
        "  <Button wbp:name='button_0'/>",
        "  <Button wbp:name='button_1'/>",
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
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'/>",
        "  <Label/>",
        "  <Label/>",
        "  <Button wbp:name='button_1'/>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
    //
    try {
      layout.startEdit();
      layout.command_MOVE_ROW(1, 0);
    } finally {
      layout.endEdit();
    }
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Label/>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_0'/>",
        "  <Label/>",
        "</Shell>");
  }

  public void test_MOVE_ROW_after() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'/>",
        "  <Label/>",
        "  <Label/>",
        "  <Button wbp:name='button_1'/>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
    //
    try {
      layout.startEdit();
      layout.command_MOVE_ROW(0, 2);
    } finally {
      layout.endEdit();
    }
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Label/>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_0'/>",
        "  <Label/>",
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_MOVE() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'/>",
        "  <Label/>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_2'/>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
    ControlInfo button = getObjectByName("button_1");
    //
    layout.command_MOVE(button, 1, false, 0, false);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_0'/>",
        "  <Button wbp:name='button_1'/>",
        "  <Label/>",
        "  <Button wbp:name='button_2'/>",
        "</Shell>");
  }

  public void test_MOVE_out() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout/>",
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
        "    <f:TableWrapLayout/>",
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
        "    <f:TableWrapLayout wbp:name='layout' numColumns='4'/>",
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
    TableWrapLayoutInfo layout = getObjectByName("layout");
    ControlInfo button = getObjectByName("button");
    //
    layout.command_MOVE(button, 1, false, 0, false);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
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
        "    <f:TableWrapLayout wbp:name='layout' numColumns='3'/>",
        "  </Shell.layout>",
        "  <Label/>",
        "  <Label/>",
        "  <Label/>",
        "  <Label/>",
        "  <Label/>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
    ControlInfo button = getObjectByName("button");
    //
    layout.command_MOVE(button, 0, false, 0, false);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout'/>",
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
        "    <f:TableWrapLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Composite>",
        "    <Composite.layout>",
        "      <RowLayout/>",
        "    </Composite.layout>",
        "    <Button wbp:name='button'/>",
        "  </Composite>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
    ControlInfo button = getObjectByName("button");
    //
    layout.command_ADD(button, 0, false, 1, false);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout'/>",
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
        "    <f:TableWrapLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
    //
    ControlInfo newButton = createButton();
    layout.command_CREATE(newButton, 0, false, 0, false);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout'/>",
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
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_2'/>",
        "  <Button wbp:name='button_3'/>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
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
        "    <f:TableWrapLayout wbp:name='layout' numColumns='3'/>",
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
        "    <f:TableWrapLayout wbp:name='layout' numColumns='3'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_2'/>",
        "  <Button wbp:name='button_3'/>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
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
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_2'/>",
        "  <Button wbp:name='button_3'/>",
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete layout
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that when delete {@link TableWrapLayoutInfo}, fillers are also removed, because there are
   * not controls that user wants.
   */
  public void test_DELETE_removeFillers() throws Exception {
    CompositeInfo shell =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <f:TableWrapLayout wbp:name='layout'/>",
            "  </Shell.layout>",
            "  <Label/>",
            "  <Button/>",
            "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
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
            "      <f:TableWrapLayout numColumns='2'/>",
            "    </Composite.layout>",
            "    <Button text='1'>",
            "      <Button.layoutData>",
            "        <f:TableWrapData align='(org.eclipse.ui.forms.widgets.TableWrapData).FILL'/>",
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
        "      <f:TableWrapLayout numColumns='2'/>",
        "    </Composite.layout>",
        "    <Button text='1'>",
        "      <Button.layoutData>",
        "        <f:TableWrapData align='(org.eclipse.ui.forms.widgets.TableWrapData).FILL'/>",
        "      </Button.layoutData>",
        "    </Button>",
        "    <Label/>",
        "    <Label/>",
        "    <Button text='2'/>",
        "  </Composite>",
        "  <Composite>",
        "    <Composite.layout>",
        "      <f:TableWrapLayout numColumns='2'/>",
        "    </Composite.layout>",
        "    <Button text='1'>",
        "      <Button.layoutData>",
        "        <f:TableWrapData align='(org.eclipse.ui.forms.widgets.TableWrapData).FILL'/>",
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
  private static int getInt(TableWrapDataInfo layoutData, String fieldName) throws Exception {
    return ReflectionUtils.getFieldInt(layoutData, fieldName);
  }
}