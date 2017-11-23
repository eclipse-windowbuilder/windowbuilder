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
package org.eclipse.wb.tests.designer.rcp.model.forms.table;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.TableWrapDataInfo;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.TableWrapLayoutImages;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.TableWrapLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.model.forms.AbstractFormsTest;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/**
 * Test for {@link TableWrapDataInfo}.
 * 
 * @author scheglov_ke
 */
public class TableWrapDataTest extends AbstractFormsTest {
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
  public void test_getSmallAlignmentImage() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new TableWrapLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
    ControlInfo button = shell.getChildrenControls().get(0);
    //
    shell.refresh();
    try {
      TableWrapDataInfo layoutData = layout.getTableWrapData(button);
      check_getSmallAlignmentImage(layoutData, true, new int[]{
          TableWrapData.LEFT,
          TableWrapData.CENTER,
          TableWrapData.RIGHT,
          TableWrapData.FILL}, new String[]{"left.gif", "center.gif", "right.gif", "fill.gif"});
      check_getSmallAlignmentImage(layoutData, false, new int[]{
          TableWrapData.TOP,
          TableWrapData.MIDDLE,
          TableWrapData.BOTTOM,
          TableWrapData.FILL}, new String[]{"top.gif", "middle.gif", "bottom.gif", "fill.gif"});
    } finally {
      shell.refresh_dispose();
    }
  }

  private static void check_getSmallAlignmentImage(TableWrapDataInfo layoutData,
      boolean horizontal,
      int[] alignments,
      String[] paths) throws Exception {
    for (int i = 0; i < alignments.length; i++) {
      int alignment = alignments[i];
      Image expectedImage = TableWrapLayoutImages.getImage((horizontal ? "/h/" : "/v/") + paths[i]);
      if (horizontal) {
        layoutData.setHorizontalAlignment(alignment);
      } else {
        layoutData.setVerticalAlignment(alignment);
      }
      assertSame(expectedImage, layoutData.getSmallAlignmentImage(horizontal));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Horizontal
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets horizontal alignment in {@link TableWrapData} constructor.
   */
  public void test_horizontalAlignment_1() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new TableWrapLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      {",
            "        TableWrapData tableWrapData = new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP);",
            "        button.setLayoutData(tableWrapData);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
    ControlInfo button = shell.getChildrenControls().get(0);
    TableWrapDataInfo tableWrapData = layout.getTableWrapData(button);
    // initial state
    assertEquals(TableWrapData.LEFT, tableWrapData.getHorizontalAlignment());
    assertFalse(tableWrapData.getHorizontalGrab());
    // set CENTER
    tableWrapData.setHorizontalAlignment(TableWrapData.CENTER);
    assertEquals(TableWrapData.CENTER, tableWrapData.getHorizontalAlignment());
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new TableWrapLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setLayoutData(new TableWrapData(TableWrapData.CENTER, TableWrapData.TOP));",
        "    }",
        "  }",
        "}");
    // set RIGHT
    tableWrapData.setHorizontalAlignment(TableWrapData.RIGHT);
    assertEquals(TableWrapData.RIGHT, tableWrapData.getHorizontalAlignment());
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new TableWrapLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setLayoutData(new TableWrapData(TableWrapData.RIGHT, TableWrapData.TOP));",
        "    }",
        "  }",
        "}");
    // set FILL
    tableWrapData.setHorizontalAlignment(TableWrapData.FILL);
    assertEquals(TableWrapData.FILL, tableWrapData.getHorizontalAlignment());
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new TableWrapLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setLayoutData(new TableWrapData(TableWrapData.FILL, TableWrapData.TOP));",
        "    }",
        "  }",
        "}");
    // set LEFT
    tableWrapData.setHorizontalAlignment(TableWrapData.LEFT);
    assertEquals(TableWrapData.LEFT, tableWrapData.getHorizontalAlignment());
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new TableWrapLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Sets horizontal alignment in {@link TableWrapData#align} field.
   */
  public void test_horizontalAlignment_2() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new TableWrapLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      {",
            "        TableWrapData tableWrapData = new TableWrapData();",
            "        tableWrapData.align = TableWrapData.LEFT;",
            "        button.setLayoutData(tableWrapData);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
    ControlInfo button = shell.getChildrenControls().get(0);
    TableWrapDataInfo tableWrapData = layout.getTableWrapData(button);
    // initial state
    assertEquals(TableWrapData.LEFT, tableWrapData.getHorizontalAlignment());
    assertFalse(tableWrapData.getHorizontalGrab());
    // set CENTER
    tableWrapData.setHorizontalAlignment(TableWrapData.CENTER);
    assertEquals(TableWrapData.CENTER, tableWrapData.getHorizontalAlignment());
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new TableWrapLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      {",
        "        TableWrapData tableWrapData = new TableWrapData();",
        "        tableWrapData.align = TableWrapData.CENTER;",
        "        button.setLayoutData(tableWrapData);",
        "      }",
        "    }",
        "  }",
        "}");
    // set RIGHT
    tableWrapData.setHorizontalAlignment(TableWrapData.RIGHT);
    assertEquals(TableWrapData.RIGHT, tableWrapData.getHorizontalAlignment());
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new TableWrapLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      {",
        "        TableWrapData tableWrapData = new TableWrapData();",
        "        tableWrapData.align = TableWrapData.RIGHT;",
        "        button.setLayoutData(tableWrapData);",
        "      }",
        "    }",
        "  }",
        "}");
    // set FILL
    tableWrapData.setHorizontalAlignment(TableWrapData.FILL);
    assertEquals(TableWrapData.FILL, tableWrapData.getHorizontalAlignment());
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new TableWrapLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      {",
        "        TableWrapData tableWrapData = new TableWrapData();",
        "        tableWrapData.align = TableWrapData.FILL;",
        "        button.setLayoutData(tableWrapData);",
        "      }",
        "    }",
        "  }",
        "}");
    // set LEFT
    tableWrapData.setHorizontalAlignment(TableWrapData.LEFT);
    assertEquals(TableWrapData.LEFT, tableWrapData.getHorizontalAlignment());
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new TableWrapLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Sets horizontal grab in {@link TableWrapData#grabHorizontal} field.
   */
  public void test_horizontalGrab() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new TableWrapLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
    ControlInfo button = shell.getChildrenControls().get(0);
    TableWrapDataInfo tableWrapData = layout.getTableWrapData(button);
    // initial state
    assertFalse(tableWrapData.getHorizontalGrab());
    // grab := true
    tableWrapData.setHorizontalGrab(true);
    assertTrue(tableWrapData.getHorizontalGrab());
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new TableWrapLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      {",
        "        TableWrapData tableWrapData = new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP, 1, 1);",
        "        tableWrapData.grabHorizontal = true;",
        "        button.setLayoutData(tableWrapData);",
        "      }",
        "    }",
        "  }",
        "}");
    // grab := false
    tableWrapData.setHorizontalGrab(false);
    assertFalse(tableWrapData.getHorizontalGrab());
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new TableWrapLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Precondition: {@link TableWrapData#FILL} horizontal alignment in constructor.<br>
   * Action: turn grab to true/false.<br>
   * Result: use/not {@link TableWrapData#FILL_GRAB} in constructor.
   */
  public void test_horizontalFillGrab_1() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new TableWrapLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      {",
            "        TableWrapData tableWrapData = new TableWrapData(TableWrapData.FILL, TableWrapData.TOP);",
            "        button.setLayoutData(tableWrapData);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
    ControlInfo button = shell.getChildrenControls().get(0);
    TableWrapDataInfo tableWrapData = layout.getTableWrapData(button);
    // initial state
    assertEquals(TableWrapData.FILL, tableWrapData.getHorizontalAlignment());
    assertFalse(tableWrapData.getHorizontalGrab());
    // grab := true
    tableWrapData.setHorizontalGrab(true);
    assertEquals(TableWrapData.FILL, tableWrapData.getHorizontalAlignment());
    assertTrue(tableWrapData.getHorizontalGrab());
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new TableWrapLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));",
        "    }",
        "  }",
        "}");
    // grab := false
    tableWrapData.setHorizontalGrab(false);
    assertEquals(TableWrapData.FILL, tableWrapData.getHorizontalAlignment());
    assertFalse(tableWrapData.getHorizontalGrab());
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new TableWrapLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setLayoutData(new TableWrapData(TableWrapData.FILL, TableWrapData.TOP));",
        "    }",
        "  }",
        "}");
  }

  /**
   * Precondition: constructor with alignment, horizontal grab is <code>true</code> in field.<br>
   * Action: change horizontal alignment to {@link TableWrapData#FILL} and back to
   * {@link TableWrapData#LEFT
	 * }.<br>
   * Result: use/not {@link TableWrapData#FILL_GRAB} in constructor.
   */
  public void test_horizontalFillGrab_2() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new TableWrapLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      {",
            "        TableWrapData tableWrapData = new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP);",
            "        tableWrapData.grabHorizontal = true;",
            "        button.setLayoutData(tableWrapData);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
    ControlInfo button = shell.getChildrenControls().get(0);
    final TableWrapDataInfo tableWrapData = layout.getTableWrapData(button);
    // initial state
    assertEquals(TableWrapData.LEFT, tableWrapData.getHorizontalAlignment());
    assertTrue(tableWrapData.getHorizontalGrab());
    // alignment := FILL
    ExecutionUtils.run(shell, new RunnableEx() {
      public void run() throws Exception {
        tableWrapData.setHorizontalAlignment(TableWrapData.FILL);
      }
    });
    assertEquals(TableWrapData.FILL, tableWrapData.getHorizontalAlignment());
    assertTrue(tableWrapData.getHorizontalGrab());
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new TableWrapLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));",
        "    }",
        "  }",
        "}");
    // alignment := LEFT
    ExecutionUtils.run(shell, new RunnableEx() {
      public void run() throws Exception {
        tableWrapData.setHorizontalAlignment(TableWrapData.LEFT);
      }
    });
    assertEquals(TableWrapData.LEFT, tableWrapData.getHorizontalAlignment());
    assertTrue(tableWrapData.getHorizontalGrab());
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new TableWrapLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      {",
        "        TableWrapData tableWrapData = new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP);",
        "        tableWrapData.grabHorizontal = true;",
        "        button.setLayoutData(tableWrapData);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Precondition: {@link TableWrapData#LEFT} horizontal alignment in constructor.<br>
   * Action: turn grab to <code>true</code>.<br>
   * Result: just set grab in field.
   */
  public void test_horizontalFillGrab_3() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new TableWrapLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      {",
            "        TableWrapData tableWrapData = new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP);",
            "        button.setLayoutData(tableWrapData);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
    ControlInfo button = shell.getChildrenControls().get(0);
    TableWrapDataInfo tableWrapData = layout.getTableWrapData(button);
    // initial state
    assertEquals(TableWrapData.LEFT, tableWrapData.getHorizontalAlignment());
    assertFalse(tableWrapData.getHorizontalGrab());
    // grab := true
    tableWrapData.setHorizontalGrab(true);
    assertEquals(TableWrapData.LEFT, tableWrapData.getHorizontalAlignment());
    assertTrue(tableWrapData.getHorizontalGrab());
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new TableWrapLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      {",
        "        TableWrapData tableWrapData = new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP);",
        "        tableWrapData.grabHorizontal = true;",
        "        button.setLayoutData(tableWrapData);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Sets horizontal span in {@link TableWrapData} constructor.
   */
  public void test_horizontalSpan_1() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    {",
            "      TableWrapLayout layout = new TableWrapLayout();",
            "      layout.numColumns = 2;",
            "      setLayout(layout);",
            "    }",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      {",
            "        TableWrapData tableWrapData = new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP, 1, 1);",
            "        button.setLayoutData(tableWrapData);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
    ControlInfo button = shell.getChildrenControls().get(0);
    TableWrapDataInfo tableWrapData = layout.getTableWrapData(button);
    // initial state
    assertEquals(1, tableWrapData.getHorizontalSpan());
    // horizontalSpan := 2
    tableWrapData.setHorizontalSpan(2);
    assertEquals(2, tableWrapData.getHorizontalSpan());
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    {",
        "      TableWrapLayout layout = new TableWrapLayout();",
        "      layout.numColumns = 2;",
        "      setLayout(layout);",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP, 1, 2));",
        "    }",
        "  }",
        "}");
    // horizontalSpan := default
    tableWrapData.getPropertyByTitle("colspan").setValue(Property.UNKNOWN_VALUE);
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    {",
        "      TableWrapLayout layout = new TableWrapLayout();",
        "      layout.numColumns = 2;",
        "      setLayout(layout);",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
    {
      tableWrapData = layout.getTableWrapData(button);
      assertEquals(1, tableWrapData.getHorizontalSpan());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Vertical
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets vertical alignment in {@link TableWrapData} constructor.
   */
  public void test_verticalAlignment_1() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new TableWrapLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      {",
            "        TableWrapData tableWrapData = new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP);",
            "        button.setLayoutData(tableWrapData);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
    ControlInfo button = shell.getChildrenControls().get(0);
    TableWrapDataInfo tableWrapData = layout.getTableWrapData(button);
    // initial state
    assertEquals(TableWrapData.TOP, tableWrapData.getVerticalAlignment());
    assertFalse(tableWrapData.getVerticalGrab());
    // set MIDDLE
    tableWrapData.setVerticalAlignment(TableWrapData.MIDDLE);
    assertEquals(TableWrapData.MIDDLE, tableWrapData.getVerticalAlignment());
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new TableWrapLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.MIDDLE));",
        "    }",
        "  }",
        "}");
    // set BOTTOM
    tableWrapData.setVerticalAlignment(TableWrapData.BOTTOM);
    assertEquals(TableWrapData.BOTTOM, tableWrapData.getVerticalAlignment());
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new TableWrapLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.BOTTOM));",
        "    }",
        "  }",
        "}");
    // set FILL
    tableWrapData.setVerticalAlignment(TableWrapData.FILL);
    assertEquals(TableWrapData.FILL, tableWrapData.getVerticalAlignment());
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new TableWrapLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.FILL));",
        "    }",
        "  }",
        "}");
    // set TOP
    tableWrapData.setVerticalAlignment(TableWrapData.TOP);
    assertEquals(TableWrapData.TOP, tableWrapData.getVerticalAlignment());
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new TableWrapLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Sets vertical alignment in {@link TableWrapData#align} field.
   */
  public void test_verticalAlignment_2() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new TableWrapLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      {",
            "        TableWrapData tableWrapData = new TableWrapData();",
            "        tableWrapData.valign = TableWrapData.TOP;",
            "        button.setLayoutData(tableWrapData);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
    ControlInfo button = shell.getChildrenControls().get(0);
    TableWrapDataInfo tableWrapData = layout.getTableWrapData(button);
    // initial state
    assertEquals(TableWrapData.TOP, tableWrapData.getVerticalAlignment());
    assertFalse(tableWrapData.getVerticalGrab());
    // set MIDDLE
    tableWrapData.setVerticalAlignment(TableWrapData.MIDDLE);
    assertEquals(TableWrapData.MIDDLE, tableWrapData.getVerticalAlignment());
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new TableWrapLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      {",
        "        TableWrapData tableWrapData = new TableWrapData();",
        "        tableWrapData.valign = TableWrapData.MIDDLE;",
        "        button.setLayoutData(tableWrapData);",
        "      }",
        "    }",
        "  }",
        "}");
    // set BOTTOM
    tableWrapData.setVerticalAlignment(TableWrapData.BOTTOM);
    assertEquals(TableWrapData.BOTTOM, tableWrapData.getVerticalAlignment());
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new TableWrapLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      {",
        "        TableWrapData tableWrapData = new TableWrapData();",
        "        tableWrapData.valign = TableWrapData.BOTTOM;",
        "        button.setLayoutData(tableWrapData);",
        "      }",
        "    }",
        "  }",
        "}");
    // set FILL
    tableWrapData.setVerticalAlignment(TableWrapData.FILL);
    assertEquals(TableWrapData.FILL, tableWrapData.getVerticalAlignment());
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new TableWrapLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      {",
        "        TableWrapData tableWrapData = new TableWrapData();",
        "        tableWrapData.valign = TableWrapData.FILL;",
        "        button.setLayoutData(tableWrapData);",
        "      }",
        "    }",
        "  }",
        "}");
    // set TOP
    tableWrapData.setVerticalAlignment(TableWrapData.TOP);
    assertEquals(TableWrapData.TOP, tableWrapData.getVerticalAlignment());
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new TableWrapLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Sets vertical alignment in {@link TableWrapData} constructor.
   */
  public void test_verticalAlignment_3() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new TableWrapLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      {",
            "        TableWrapData tableWrapData = new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP);",
            "        tableWrapData.grabVertical = true;",
            "        button.setLayoutData(tableWrapData);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
    ControlInfo button = shell.getChildrenControls().get(0);
    TableWrapDataInfo tableWrapData = layout.getTableWrapData(button);
    // initial state
    assertEquals(TableWrapData.TOP, tableWrapData.getVerticalAlignment());
    assertTrue(tableWrapData.getVerticalGrab());
    // set MIDDLE
    tableWrapData.setVerticalAlignment(TableWrapData.MIDDLE);
    assertEquals(TableWrapData.MIDDLE, tableWrapData.getVerticalAlignment());
    assertTrue(tableWrapData.getVerticalGrab());
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new TableWrapLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      {",
        "        TableWrapData tableWrapData = new TableWrapData(TableWrapData.LEFT, TableWrapData.MIDDLE);",
        "        tableWrapData.grabVertical = true;",
        "        button.setLayoutData(tableWrapData);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Sets vertical grab in {@link TableWrapData#grabVertical} field.
   */
  public void test_verticalGrab() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new TableWrapLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
    ControlInfo button = shell.getChildrenControls().get(0);
    TableWrapDataInfo tableWrapData = layout.getTableWrapData(button);
    // initial state
    assertFalse(tableWrapData.getVerticalGrab());
    // grab := true
    tableWrapData.setVerticalGrab(true);
    assertTrue(tableWrapData.getVerticalGrab());
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new TableWrapLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      {",
        "        TableWrapData tableWrapData = new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP, 1, 1);",
        "        tableWrapData.grabVertical = true;",
        "        button.setLayoutData(tableWrapData);",
        "      }",
        "    }",
        "  }",
        "}");
    // grab := false
    tableWrapData.setVerticalGrab(false);
    assertFalse(tableWrapData.getVerticalGrab());
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new TableWrapLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Precondition: {@link TableWrapData#FILL} vertical alignment in constructor.<br>
   * Action: turn grab to true/false.<br>
   * Result: use/not {@link TableWrapData#FILL_GRAB} in constructor.
   */
  public void test_verticalFillGrab_1() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new TableWrapLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.FILL));",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
    ControlInfo button = shell.getChildrenControls().get(0);
    TableWrapDataInfo tableWrapData = layout.getTableWrapData(button);
    // initial state
    assertEquals(TableWrapData.FILL, tableWrapData.getVerticalAlignment());
    assertFalse(tableWrapData.getVerticalGrab());
    // grab := true
    tableWrapData.setVerticalGrab(true);
    assertEquals(TableWrapData.FILL, tableWrapData.getVerticalAlignment());
    assertTrue(tableWrapData.getVerticalGrab());
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new TableWrapLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.FILL_GRAB));",
        "    }",
        "  }",
        "}");
    // grab := false
    tableWrapData.setVerticalGrab(false);
    assertEquals(TableWrapData.FILL, tableWrapData.getVerticalAlignment());
    assertFalse(tableWrapData.getVerticalGrab());
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new TableWrapLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.FILL));",
        "    }",
        "  }",
        "}");
  }

  /**
   * Precondition: constructor with alignment, vertical grab is <code>true</code> in field.<br>
   * Action: change vertical alignment to {@link TableWrapData#FILL} and back to
   * {@link TableWrapData#TOP}. <br>
   * Result: use/not {@link TableWrapData#FILL_GRAB} in constructor.
   */
  public void test_verticalFillGrab_2() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new TableWrapLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      {",
            "        TableWrapData tableWrapData = new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP);",
            "        tableWrapData.grabVertical = true;",
            "        button.setLayoutData(tableWrapData);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
    ControlInfo button = shell.getChildrenControls().get(0);
    final TableWrapDataInfo tableWrapData = layout.getTableWrapData(button);
    // initial state
    assertEquals(TableWrapData.TOP, tableWrapData.getVerticalAlignment());
    assertTrue(tableWrapData.getVerticalGrab());
    // alignment := FILL
    ExecutionUtils.run(shell, new RunnableEx() {
      public void run() throws Exception {
        tableWrapData.setVerticalAlignment(TableWrapData.FILL);
      }
    });
    assertEquals(TableWrapData.FILL, tableWrapData.getVerticalAlignment());
    assertTrue(tableWrapData.getVerticalGrab());
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new TableWrapLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.FILL_GRAB));",
        "    }",
        "  }",
        "}");
    // alignment := TOP
    ExecutionUtils.run(shell, new RunnableEx() {
      public void run() throws Exception {
        tableWrapData.setVerticalAlignment(TableWrapData.TOP);
      }
    });
    assertEquals(TableWrapData.TOP, tableWrapData.getVerticalAlignment());
    assertTrue(tableWrapData.getVerticalGrab());
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new TableWrapLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      {",
        "        TableWrapData tableWrapData = new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP);",
        "        tableWrapData.grabVertical = true;",
        "        button.setLayoutData(tableWrapData);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Precondition: {@link TableWrapData#TOP} vertical alignment in constructor.<br>
   * Action: turn grab to <code>true</code>.<br>
   * Result: just set grab in field.
   */
  public void test_verticalFillGrab_3() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new TableWrapLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      {",
            "        TableWrapData tableWrapData = new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP);",
            "        button.setLayoutData(tableWrapData);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
    ControlInfo button = shell.getChildrenControls().get(0);
    TableWrapDataInfo tableWrapData = layout.getTableWrapData(button);
    // initial state
    assertEquals(TableWrapData.TOP, tableWrapData.getVerticalAlignment());
    assertFalse(tableWrapData.getVerticalGrab());
    // grab := true
    tableWrapData.setVerticalGrab(true);
    assertEquals(TableWrapData.TOP, tableWrapData.getVerticalAlignment());
    assertTrue(tableWrapData.getVerticalGrab());
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new TableWrapLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      {",
        "        TableWrapData tableWrapData = new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP);",
        "        tableWrapData.grabVertical = true;",
        "        button.setLayoutData(tableWrapData);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Sets vertical span in {@link TableWrapData} constructor.
   */
  public void test_verticalSpan_1() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    {",
            "      TableWrapLayout layout = new TableWrapLayout();",
            "      layout.numColumns = 2;",
            "      setLayout(layout);",
            "    }",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP, 1, 1));",
            "    }",
            "    new Button(this, SWT.NONE);",
            "    new Button(this, SWT.NONE);",
            "  }",
            "}");
    shell.refresh();
    TableWrapLayoutInfo layout = (TableWrapLayoutInfo) shell.getLayout();
    ControlInfo button = shell.getChildrenControls().get(0);
    TableWrapDataInfo tableWrapData = layout.getTableWrapData(button);
    // initial state
    assertEquals(1, tableWrapData.getVerticalSpan());
    // verticalSpan := 2
    tableWrapData.setVerticalSpan(2);
    assertEquals(2, tableWrapData.getVerticalSpan());
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    {",
        "      TableWrapLayout layout = new TableWrapLayout();",
        "      layout.numColumns = 2;",
        "      setLayout(layout);",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP, 2, 1));",
        "    }",
        "    new Button(this, SWT.NONE);",
        "    new Button(this, SWT.NONE);",
        "  }",
        "}");
    // verticalSpan := default
    tableWrapData.getPropertyByTitle("rowspan").setValue(Property.UNKNOWN_VALUE);
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    {",
        "      TableWrapLayout layout = new TableWrapLayout();",
        "      layout.numColumns = 2;",
        "      setLayout(layout);",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "    new Button(this, SWT.NONE);",
        "    new Button(this, SWT.NONE);",
        "  }",
        "}");
    {
      tableWrapData = layout.getTableWrapData(button);
      assertEquals(1, tableWrapData.getVerticalSpan());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_contextMenu_horizontal() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new TableWrapLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ControlInfo button = shell.getChildrenControls().get(0);
    //
    TableWrapDataInfo layoutData = (TableWrapDataInfo) button.getChildrenJava().get(0);
    assertNotNull(layoutData);
    //
    // prepare context menu
    IMenuManager manager;
    {
      manager = getDesignerMenuManager();
      shell.getBroadcastObject().addContextMenu(null, button, manager);
    }
    // check actions
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
      assertEditor(
          "class Test extends Shell {",
          "  Test() {",
          "    setLayout(new TableWrapLayout());",
          "    {",
          "      Button button = new Button(this, SWT.NONE);",
          "      button.setLayoutData(new TableWrapData(TableWrapData.RIGHT, TableWrapData.TOP, 1, 1));",
          "    }",
          "  }",
          "}");
    }
    // use "Grab action"
    {
      IAction action = findChildAction(manager2, "&Grab excess space");
      action.run();
      assertEditor(
          "class Test extends Shell {",
          "  Test() {",
          "    setLayout(new TableWrapLayout());",
          "    {",
          "      Button button = new Button(this, SWT.NONE);",
          "      {",
          "        TableWrapData tableWrapData = new TableWrapData(TableWrapData.RIGHT, TableWrapData.TOP, 1, 1);",
          "        tableWrapData.grabHorizontal = true;",
          "        button.setLayoutData(tableWrapData);",
          "      }",
          "    }",
          "  }",
          "}");
    }
  }

  public void test_contextMenu_vertical() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new TableWrapLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    ControlInfo button = shell.getChildrenControls().get(0);
    //
    TableWrapDataInfo layoutData = (TableWrapDataInfo) button.getChildrenJava().get(0);
    assertNotNull(layoutData);
    //
    shell.refresh();
    try {
      // prepare context menu
      IMenuManager manager;
      {
        manager = getDesignerMenuManager();
        shell.getBroadcastObject().addContextMenu(null, button, manager);
      }
      // check actions
      IMenuManager manager2 = findChildMenuManager(manager, "Vertical alignment");
      assertNotNull(manager2);
      assertNotNull(findChildAction(manager2, "&Grab excess space"));
      assertNotNull(findChildAction(manager2, "&Top"));
      assertNotNull(findChildAction(manager2, "&Middle"));
      assertNotNull(findChildAction(manager2, "&Bottom"));
      assertNotNull(findChildAction(manager2, "&Fill"));
      // use "Bottom" action
      {
        IAction action = findChildAction(manager2, "&Bottom");
        action.setChecked(true);
        action.run();
        assertEditor(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new TableWrapLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.BOTTOM, 1, 1));",
            "    }",
            "  }",
            "}");
      }
      // use "Grab action"
      {
        IAction action = findChildAction(manager2, "&Grab excess space");
        action.run();
        assertEditor(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new TableWrapLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      {",
            "        TableWrapData tableWrapData = new TableWrapData(TableWrapData.LEFT, TableWrapData.BOTTOM, 1, 1);",
            "        tableWrapData.grabVertical = true;",
            "        button.setLayoutData(tableWrapData);",
            "      }",
            "    }",
            "  }",
            "}");
      }
    } finally {
      shell.refresh_dispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Default
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_deleteIfDefault_constructor0() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new TableWrapLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setLayoutData(new TableWrapData());",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // refresh(), force check
    ExecutionUtils.refresh(shell);
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new TableWrapLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
  }

  public void test_deleteIfDefault_constructor1_yes() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new TableWrapLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setLayoutData(new TableWrapData(TableWrapData.LEFT));",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // refresh(), force check
    ExecutionUtils.refresh(shell);
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new TableWrapLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
  }

  public void test_deleteIfDefault_constructor1_no() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new TableWrapLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setLayoutData(new TableWrapData(TableWrapData.RIGHT));",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // refresh(), force check
    ExecutionUtils.refresh(shell);
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new TableWrapLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setLayoutData(new TableWrapData(TableWrapData.RIGHT));",
        "    }",
        "  }",
        "}");
  }

  public void test_deleteIfDefault_constructor2_yes() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new TableWrapLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP));",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // refresh(), force check
    ExecutionUtils.refresh(shell);
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new TableWrapLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
  }

  public void test_deleteIfDefault_constructor2_no() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new TableWrapLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.BOTTOM));",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // refresh(), force check
    ExecutionUtils.refresh(shell);
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new TableWrapLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.BOTTOM));",
        "    }",
        "  }",
        "}");
  }

  public void test_deleteIfDefault_constructor4_yes() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new TableWrapLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP, 1, 1));",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // refresh(), force check
    ExecutionUtils.refresh(shell);
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new TableWrapLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
  }

  public void test_deleteIfDefault_constructor4_no() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new TableWrapLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP, 2, 1));",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // refresh(), force check
    ExecutionUtils.refresh(shell);
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new TableWrapLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP, 2, 1));",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dangling
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link TableWrapData} can be used only if parent {@link Composite} has {@link TableWrapLayout}.
   */
  public void test_hasParentLayout_notCompatible() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    Button button = new Button(this, SWT.NONE);",
            "    button.setLayoutData(new TableWrapData());",
            "  }",
            "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/new Button(this, SWT.NONE)/}",
        "  {implicit-layout: absolute} {implicit-layout} {}",
        "  {new: org.eclipse.swt.widgets.Button} {local-unique: button} {/new Button(this, SWT.NONE)/ /button.setLayoutData(new TableWrapData())/}");
    //
    shell.refresh();
    assertNoErrors(shell);
  }
}