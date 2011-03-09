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

import static org.eclipse.wb.internal.xwt.model.layout.grid.GridLayoutInfo.getGridData;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.swt.model.layout.grid.GridImages;
import org.eclipse.wb.internal.xwt.model.layout.grid.GridDataInfo;
import org.eclipse.wb.internal.xwt.model.layout.grid.GridLayoutInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;

/**
 * Test for {@link GridDataInfo}.
 * 
 * @author scheglov_ke
 */
public class GridDataTest extends XwtModelTest {
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
  // Set alignment
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link GridDataInfo#setHorizontalAlignment(int)}.
   */
  public void test_setHorizontalAlignment() throws Exception {
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
    GridDataInfo gridData = getGridData(button);
    // initial state
    assertEquals(SWT.LEFT, gridData.getHorizontalAlignment());
    // SWT.LEFT is default alignment, so nothing should be changed
    gridData.setHorizontalAlignment(SWT.LEFT);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    // SWT.RIGHT
    gridData.setHorizontalAlignment(SWT.RIGHT);
    assertEquals(SWT.RIGHT, gridData.getHorizontalAlignment());
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'>",
        "    <Button.layoutData>",
        "      <GridData horizontalAlignment='RIGHT'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
    // SWT.LEFT is default alignment, so GridData removed
    gridData.setHorizontalAlignment(SWT.LEFT);
    assertEquals(SWT.LEFT, gridData.getHorizontalAlignment());
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
  }

  /**
   * Test for {@link GridDataInfo#setVerticalAlignment(int)}.
   */
  public void test_setVerticalAlignment() throws Exception {
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
    GridDataInfo gridData = getGridData(button);
    // initial state
    assertEquals(SWT.CENTER, gridData.getVerticalAlignment());
    // SWT.CENTER is default alignment, so nothing should be changed
    gridData.setVerticalAlignment(SWT.CENTER);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    // SWT.BOTTOM
    gridData.setVerticalAlignment(SWT.BOTTOM);
    assertEquals(SWT.BOTTOM, gridData.getVerticalAlignment());
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'>",
        "    <Button.layoutData>",
        "      <GridData verticalAlignment='BOTTOM'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
    // SWT.CENTER is default alignment, so GridData removed
    gridData.setVerticalAlignment(SWT.CENTER);
    assertEquals(SWT.CENTER, gridData.getVerticalAlignment());
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Images
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getSmallAlignmentImage() throws Exception {
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
    GridDataInfo gridData = getGridData(button);
    //
    check_getSmallAlignmentImage(gridData, true, new int[]{
        SWT.LEFT,
        SWT.CENTER,
        SWT.RIGHT,
        SWT.FILL}, new String[]{"left.gif", "center.gif", "right.gif", "fill.gif"});
    check_getSmallAlignmentImage(gridData, false, new int[]{
        SWT.TOP,
        SWT.CENTER,
        SWT.BOTTOM,
        SWT.FILL}, new String[]{"top.gif", "center.gif", "bottom.gif", "fill.gif"});
  }

  private static void check_getSmallAlignmentImage(GridDataInfo gridData,
      boolean horizontal,
      int[] alignments,
      String[] paths) throws Exception {
    for (int i = 0; i < alignments.length; i++) {
      int alignment = alignments[i];
      Image expectedImage = GridImages.getImage((horizontal ? "h/" : "v/") + paths[i]);
      if (horizontal) {
        gridData.setHorizontalAlignment(alignment);
      } else {
        gridData.setVerticalAlignment(alignment);
      }
      assertSame(expectedImage, gridData.getSmallAlignmentImage(horizontal));
    }
  }

  /**
   * Set invalid alignment and ask image.
   */
  public void test_getSmallAlignmentImage_invalid() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'>",
        "    <Button.layoutData>",
        "      <GridData horizontalAlignment='-1' verticalAlignment='-1'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
    refresh();
    ControlInfo button = getObjectByName("button");
    //
    GridDataInfo gridData = getGridData(button);
    assertSame(null, gridData.getSmallAlignmentImage(true));
    assertSame(null, gridData.getSmallAlignmentImage(false));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Size hint
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link GridDataInfo#getWidthHint()} and {@link GridDataInfo#setWidthHint(int)}.
   */
  public void test_sizeHint_width() throws Exception {
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
    GridDataInfo gridData = GridLayoutInfo.getGridData(button);
    // no hint initially
    assertEquals(-1, gridData.getWidthHint());
    // set hint
    gridData.setWidthHint(200);
    assertEquals(200, gridData.getWidthHint());
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'>",
        "    <Button.layoutData>",
        "      <GridData widthHint='200'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
    // remove hint
    gridData.setWidthHint(-1);
    assertEquals(-1, gridData.getWidthHint());
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
  }

  /**
   * Test for {@link GridDataInfo#getHeightHint()} and {@link GridDataInfo#setHeightHint(int)}.
   */
  public void test_sizeHint_height() throws Exception {
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
    GridDataInfo gridData = GridLayoutInfo.getGridData(button);
    // no hint initially
    assertEquals(-1, gridData.getHeightHint());
    // set hint
    gridData.setHeightHint(200);
    assertEquals(200, gridData.getHeightHint());
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'>",
        "    <Button.layoutData>",
        "      <GridData heightHint='200'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
    // remove hint
    gridData.setHeightHint(-1);
    assertEquals(-1, gridData.getHeightHint());
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Grab
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_grabHorizontal() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'>",
        "    <Button.layoutData>",
        "      <GridData grabExcessHorizontalSpace='true'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
    refresh();
    ControlInfo button = getObjectByName("button");
    // initial state, set new
    {
      GridDataInfo gridData = getGridData(button);
      assertTrue(gridData.getHorizontalGrab());
      gridData.setHorizontalGrab(false);
    }
    // check new state
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    {
      GridDataInfo gridData = getGridData(button);
      assertFalse(gridData.getHorizontalGrab());
    }
  }

  public void test_grabVertical() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'>",
        "    <Button.layoutData>",
        "      <GridData grabExcessVerticalSpace='true'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
    refresh();
    ControlInfo button = getObjectByName("button");
    // initial state, set new
    {
      GridDataInfo gridData = getGridData(button);
      assertTrue(gridData.getVerticalGrab());
      gridData.setVerticalGrab(false);
    }
    // check new state
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    {
      GridDataInfo gridData = getGridData(button);
      assertFalse(gridData.getVerticalGrab());
    }
  }

  public void test_grab_usingProperty() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'>",
        "    <Button.layoutData>",
        "      <GridData grabExcessHorizontalSpace='true'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
    refresh();
    ControlInfo button = getObjectByName("button");
    // initial state, set new
    {
      GridDataInfo gridData = getGridData(button);
      assertTrue(gridData.getHorizontalGrab());
      gridData.getPropertyByTitle("grabExcessHorizontalSpace").setValue(false);
    }
    // check new state
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    {
      GridDataInfo gridData = getGridData(button);
      assertFalse(gridData.getHorizontalGrab());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Span
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link GridDataInfo#setHorizontalSpan(int)}.
   */
  public void test_setHorizontalSpan() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_2'/>",
        "  <Button wbp:name='button_3'/>",
        "</Shell>");
    refresh();
    ControlInfo button = getObjectByName("button_3");
    //
    GridDataInfo gridData = getGridData(button);
    gridData.setHorizontalSpan(2);
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_2'/>",
        "  <Button wbp:name='button_3'>",
        "    <Button.layoutData>",
        "      <GridData horizontalSpan='2'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
  }

  /**
   * Test for using "horizontalSpan" property.
   */
  public void test_setProperty_horizontalSpan() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_2'/>",
        "  <Button wbp:name='button_3'/>",
        "</Shell>");
    refresh();
    ControlInfo button = getObjectByName("button_3");
    Property property = getGridData(button).getPropertyByTitle("horizontalSpan");
    // ignore <= 0
    {
      String source = m_lastContext.getContent();
      property.setValue(0);
      assertEquals(source, m_lastContext.getContent());
    }
    // ignore if "x + span > numColumns"
    {
      String source = m_lastContext.getContent();
      property.setValue(3);
      assertEquals(source, m_lastContext.getContent());
    }
    // set "2"
    property.setValue(2);
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_2'/>",
        "  <Button wbp:name='button_3'>",
        "    <Button.layoutData>",
        "      <GridData horizontalSpan='2'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
  }

  /**
   * Test for {@link GridDataInfo#setVerticalSpan(int)}.
   */
  public void test_setVerticalSpan() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_2'/>",
        "  <Button wbp:name='button_3'/>",
        "</Shell>");
    refresh();
    ControlInfo button = getObjectByName("button_2");
    //
    GridDataInfo gridData = getGridData(button);
    gridData.setVerticalSpan(2);
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_2'>",
        "    <Button.layoutData>",
        "      <GridData verticalSpan='2'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Button wbp:name='button_3'/>",
        "</Shell>");
  }

  /**
   * Test for using "verticalSpan" property.
   */
  public void test_setProperty_verticalSpan() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_2'/>",
        "  <Button wbp:name='button_3'/>",
        "</Shell>");
    refresh();
    ControlInfo button = getObjectByName("button_2");
    Property property = getGridData(button).getPropertyByTitle("verticalSpan");
    // ignore <= 0
    {
      String source = m_lastContext.getContent();
      property.setValue(0);
      assertEquals(source, m_lastContext.getContent());
    }
    // ignore if "y + span > numRows"
    {
      String source = m_lastContext.getContent();
      property.setValue(3);
      assertEquals(source, m_lastContext.getContent());
    }
    // set "2"
    property.setValue(2);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_2'>",
        "    <Button.layoutData>",
        "      <GridData verticalSpan='2'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "  <Button wbp:name='button_3'/>",
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_contextMenu_horizontal() throws Exception {
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
    // prepare context menu
    IMenuManager manager = getContextMenu(button);
    IMenuManager manager2 = findChildMenuManager(manager, "Horizontal alignment");
    assertNotNull(manager2);
    assertNotNull(findChildAction(manager2, "&Grab excess space"));
    assertNotNull(findChildAction(manager2, "&Left"));
    assertNotNull(findChildAction(manager2, "&Center"));
    assertNotNull(findChildAction(manager2, "&Right"));
    assertNotNull(findChildAction(manager2, "&Fill"));
    // check "check" state
    assertTrue(findChildAction(manager2, "&Left").isChecked());
    assertFalse(findChildAction(manager2, "&Right").isChecked());
    // use "Right" action
    {
      IAction action = findChildAction(manager2, "&Right");
      action.setChecked(true);
      action.run();
      assertXML(
          "<Shell>",
          "  <Shell.layout>",
          "    <GridLayout/>",
          "  </Shell.layout>",
          "  <Button wbp:name='button'>",
          "    <Button.layoutData>",
          "      <GridData horizontalAlignment='RIGHT'/>",
          "    </Button.layoutData>",
          "  </Button>",
          "</Shell>");
    }
    // use "Grab action"
    {
      IAction action = findChildAction(manager2, "&Grab excess space");
      action.run();
      assertXML(
          "<Shell>",
          "  <Shell.layout>",
          "    <GridLayout/>",
          "  </Shell.layout>",
          "  <Button wbp:name='button'>",
          "    <Button.layoutData>",
          "      <GridData horizontalAlignment='RIGHT' grabExcessHorizontalSpace='true'/>",
          "    </Button.layoutData>",
          "  </Button>",
          "</Shell>");
    }
  }

  public void test_contextMenu_vertical() throws Exception {
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
    // prepare context menu
    IMenuManager manager = getContextMenu(button);
    IMenuManager manager2 = findChildMenuManager(manager, "Vertical alignment");
    assertNotNull(manager2);
    assertNotNull(findChildAction(manager2, "&Grab excess space"));
    assertNotNull(findChildAction(manager2, "&Top"));
    assertNotNull(findChildAction(manager2, "&Center"));
    assertNotNull(findChildAction(manager2, "&Bottom"));
    assertNotNull(findChildAction(manager2, "&Fill"));
    // use "Bottom" action
    {
      IAction action = findChildAction(manager2, "&Bottom");
      action.setChecked(true);
      action.run();
      assertXML(
          "<Shell>",
          "  <Shell.layout>",
          "    <GridLayout/>",
          "  </Shell.layout>",
          "  <Button wbp:name='button'>",
          "    <Button.layoutData>",
          "      <GridData verticalAlignment='BOTTOM'/>",
          "    </Button.layoutData>",
          "  </Button>",
          "</Shell>");
    }
    // use "Grab action"
    {
      IAction action = findChildAction(manager2, "&Grab excess space");
      action.run();
      assertXML(
          "<Shell>",
          "  <Shell.layout>",
          "    <GridLayout/>",
          "  </Shell.layout>",
          "  <Button wbp:name='button'>",
          "    <Button.layoutData>",
          "      <GridData verticalAlignment='BOTTOM' grabExcessVerticalSpace='true'/>",
          "    </Button.layoutData>",
          "  </Button>",
          "</Shell>");
    }
  }

  public void test_contextMenu_horizontalHint() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'>",
        "    <Button.layoutData>",
        "      <GridData widthHint='200'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
    refresh();
    ControlInfo button = getObjectByName("button");
    // clear "widthHint"
    {
      // prepare action
      IAction clearHintAction = getClearHintAction(button, true);
      assertNotNull(clearHintAction);
      // use action
      clearHintAction.run();
      assertXML(
          "// filler filler filler filler filler",
          "<Shell>",
          "  <Shell.layout>",
          "    <GridLayout/>",
          "  </Shell.layout>",
          "  <Button wbp:name='button'/>",
          "</Shell>");
    }
    // no "widthHint" value, so no action
    {
      IAction clearHintAction = getClearHintAction(button, true);
      assertNull(clearHintAction);
    }
  }

  public void test_contextMenu_verticalHint() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'>",
        "    <Button.layoutData>",
        "      <GridData heightHint='200'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
    refresh();
    ControlInfo button = getObjectByName("button");
    // clear "heightHint"
    {
      // prepare action
      IAction clearHintAction = getClearHintAction(button, false);
      assertNotNull(clearHintAction);
      // use action
      clearHintAction.run();
      assertXML(
          "// filler filler filler filler filler",
          "<Shell>",
          "  <Shell.layout>",
          "    <GridLayout/>",
          "  </Shell.layout>",
          "  <Button wbp:name='button'/>",
          "</Shell>");
    }
    // no "heightHint" value, so no action
    {
      IAction clearHintAction = getClearHintAction(button, false);
      assertNull(clearHintAction);
    }
  }

  private IAction getClearHintAction(ControlInfo button, boolean horizontal) throws Exception {
    IMenuManager manager = getContextMenu(button);
    String managerName = horizontal ? "Horizontal alignment" : "Vertical alignment";
    IMenuManager alignmentManager = findChildMenuManager(manager, managerName);
    return findChildAction(alignmentManager, "Clear hint");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Default values
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link GridData} does not use "modern" values as default values for fields, for example it uses
   * {@link GridData#BEGINNING}, not {@link SWT#LEFT} as we would like. So, we need to check and fix
   * this.
   */
  public void test_defaultValues() throws Exception {
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
    GridDataInfo gridData = getGridData(button);
    // ask using methods
    assertEquals(SWT.LEFT, gridData.getHorizontalAlignment());
    assertEquals(SWT.CENTER, gridData.getVerticalAlignment());
    assertEquals(false, gridData.getHorizontalGrab());
    assertEquals(false, gridData.getVerticalGrab());
    // ask using properties
    {
      assertEquals(SWT.LEFT, gridData.getPropertyByTitle("horizontalAlignment").getValue());
      assertEquals(SWT.CENTER, gridData.getPropertyByTitle("verticalAlignment").getValue());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Modern alignment constants
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_modernHorizontalAlignment() throws Exception {
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
    button.startEdit();
    //
    check_modernHorizontalAlignment(button, SWT.LEFT, SWT.LEFT, null);
    check_modernHorizontalAlignment(button, SWT.BEGINNING, SWT.LEFT, null);
    check_modernHorizontalAlignment(button, GridData.BEGINNING, SWT.LEFT, null);
    //
    check_modernHorizontalAlignment(button, SWT.CENTER, SWT.CENTER, "CENTER");
    check_modernHorizontalAlignment(button, GridData.CENTER, SWT.CENTER, "CENTER");
    //
    check_modernHorizontalAlignment(button, SWT.RIGHT, SWT.RIGHT, "RIGHT");
    check_modernHorizontalAlignment(button, SWT.END, SWT.RIGHT, "RIGHT");
    check_modernHorizontalAlignment(button, GridData.END, SWT.RIGHT, "RIGHT");
    //
    check_modernHorizontalAlignment(button, SWT.FILL, SWT.FILL, "FILL");
    check_modernHorizontalAlignment(button, GridData.FILL, SWT.FILL, "FILL");
  }

  public void test_modernVerticalAlignment() throws Exception {
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
    button.startEdit();
    //
    check_modernVerticalAlignment(button, SWT.TOP, SWT.TOP, "TOP");
    check_modernVerticalAlignment(button, SWT.BEGINNING, SWT.TOP, "TOP");
    check_modernVerticalAlignment(button, GridData.BEGINNING, SWT.TOP, "TOP");
    //
    check_modernVerticalAlignment(button, SWT.CENTER, SWT.CENTER, null);
    check_modernVerticalAlignment(button, GridData.CENTER, SWT.CENTER, null);
    //
    check_modernVerticalAlignment(button, SWT.BOTTOM, SWT.BOTTOM, "BOTTOM");
    check_modernVerticalAlignment(button, SWT.END, SWT.BOTTOM, "BOTTOM");
    check_modernVerticalAlignment(button, GridData.END, SWT.BOTTOM, "BOTTOM");
    //
    check_modernVerticalAlignment(button, SWT.FILL, SWT.FILL, "FILL");
    check_modernVerticalAlignment(button, GridData.FILL, SWT.FILL, "FILL");
  }

  private void check_modernHorizontalAlignment(ControlInfo button,
      int horizontalAlignment,
      int horizontalAlignmentEx,
      String horizontalSourceEx) throws Exception {
    String sourceEx;
    {
      sourceEx = "<GridData place=\"holder\"";
      if (horizontalSourceEx != null) {
        sourceEx += " horizontalAlignment=\"" + horizontalSourceEx + "\"";
      }
      sourceEx += ">";
    }
    check_modernAlignments(button, horizontalAlignment, horizontalAlignmentEx, -1, -1, sourceEx);
  }

  private void check_modernVerticalAlignment(ControlInfo button,
      int verticalAlignment,
      int verticalAlignmentEx,
      String verticalSourceEx) throws Exception {
    String sourceEx;
    {
      sourceEx = "<GridData place=\"holder\"";
      if (verticalSourceEx != null) {
        sourceEx += " verticalAlignment=\"" + verticalSourceEx + "\"";
      }
      sourceEx += ">";
    }
    check_modernAlignments(button, -1, -1, verticalAlignment, verticalAlignmentEx, sourceEx);
  }

  private void check_modernAlignments(ControlInfo button,
      int horizontalAlignment,
      int horizontalAlignmentEx,
      int verticalAlignment,
      int verticalAlignmentEx,
      String sourceEx) throws Exception {
    GridDataInfo gridData = getGridData(button);
    // set some attribute to ensure that it will be not removed
    gridData.getCreationSupport().getElement().setAttribute("place", "holder");
    // set/check alignments
    if (horizontalAlignment != -1) {
      gridData.setHorizontalAlignment(horizontalAlignment);
      assertEquals(horizontalAlignmentEx, gridData.getHorizontalAlignment());
    }
    if (verticalAlignment != -1) {
      gridData.setVerticalAlignment(verticalAlignment);
      assertEquals(verticalAlignmentEx, gridData.getVerticalAlignment());
    }
    // check source for GridData
    String gridDataSource = gridData.toString();
    assertEquals(sourceEx, gridDataSource);
  }
}