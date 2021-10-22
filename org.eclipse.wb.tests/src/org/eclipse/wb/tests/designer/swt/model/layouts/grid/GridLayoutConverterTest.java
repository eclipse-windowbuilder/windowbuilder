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

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.model.layout.grid.GridDataInfo;
import org.eclipse.wb.internal.swt.model.layout.grid.GridLayoutConverter;
import org.eclipse.wb.internal.swt.model.layout.grid.GridLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.swt.layout.GridLayout;

/**
 * Tests for {@link GridLayoutConverter}.
 *
 * @author scheglov_ke
 */
public class GridLayoutConverterTest extends RcpModelTest {
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
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * No controls.
   */
  public void test_empty() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "// filler filler filler",
            "public class Test extends Shell {",
            "  public Test() {",
            "  }",
            "}");
    setGridLayout(shell, new String[]{
        "// filler filler filler",
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new GridLayout(1, false));",
        "  }",
        "}"}, new Rectangle[]{});
  }

  /**
   * Control in single column, in normal order.
   */
  public void test_singleColumn_normalOrder() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    {",
            "      Button button_0 = new Button(this, SWT.NONE);",
            "      button_0.setBounds(10, 10, 100, 20);",
            "    }",
            "    {",
            "      Button button_1 = new Button(this, SWT.NONE);",
            "      button_1.setBounds(20, 40, 100, 20);",
            "    }",
            "  }",
            "}");
    setGridLayout(shell, new String[]{
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button button_0 = new Button(this, SWT.NONE);",
        "    }",
        "    {",
        "      Button button_1 = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}"}, new Rectangle[]{new Rectangle(0, 0, 1, 1), new Rectangle(0, 1, 1, 1)});
  }

  /**
   * We should not move control if it is already in right order.
   */
  public void test_noReorderIfRightOrder() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    {",
            "      Button button_0 = new Button(this, SWT.NONE);",
            "      button_0.setBounds(10, 10, 100, 20);",
            "    }",
            "    int marker;",
            "    {",
            "      Button button_1 = new Button(this, SWT.NONE);",
            "      button_1.setBounds(10, 50, 100, 20);",
            "    }",
            "  }",
            "}");
    setGridLayout(shell, new String[]{
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button button_0 = new Button(this, SWT.NONE);",
        "    }",
        "    int marker;",
        "    {",
        "      Button button_1 = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}"});
  }

  /**
   * Control in single column, in reverse order.
   */
  public void test_singleColumn_reverseOrder() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    {",
            "      Button button_1 = new Button(this, SWT.NONE);",
            "      button_1.setBounds(20, 40, 100, 20);",
            "    }",
            "    {",
            "      Button button_0 = new Button(this, SWT.NONE);",
            "      button_0.setBounds(10, 10, 100, 20);",
            "    }",
            "  }",
            "}");
    setGridLayout(shell, new String[]{
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button button_0 = new Button(this, SWT.NONE);",
        "    }",
        "    {",
        "      Button button_1 = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}"}, new Rectangle[]{new Rectangle(0, 0, 1, 1), new Rectangle(0, 1, 1, 1)});
  }

  /**
   * Control in two rows, no fillers.
   */
  public void test_twoRows_noFillers() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    {",
            "      Button button_0 = new Button(this, SWT.NONE);",
            "      button_0.setBounds(10, 10, 100, 20);",
            "    }",
            "    {",
            "      Button button_1 = new Button(this, SWT.NONE);",
            "      button_1.setBounds(120, 15, 100, 20);",
            "    }",
            "  }",
            "}");
    setGridLayout(shell, new String[]{
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    {",
        "      Button button_0 = new Button(this, SWT.NONE);",
        "    }",
        "    {",
        "      Button button_1 = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}"}, new Rectangle[]{new Rectangle(0, 0, 1, 1), new Rectangle(1, 0, 1, 1)});
  }

  /**
   * Control in two rows, on diagonal, with fillers.
   */
  public void test_twoRows_withFillers() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    {",
            "      Button button_0 = new Button(this, SWT.NONE);",
            "      button_0.setBounds(10, 10, 100, 20);",
            "    }",
            "    {",
            "      Button button_1 = new Button(this, SWT.NONE);",
            "      button_1.setBounds(120, 40, 100, 20);",
            "    }",
            "  }",
            "}");
    setGridLayout(shell, new String[]{
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    {",
        "      Button button_0 = new Button(this, SWT.NONE);",
        "    }",
        "    new Label(this, SWT.NONE);",
        "    new Label(this, SWT.NONE);",
        "    {",
        "      Button button_1 = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}"}, new Rectangle[]{new Rectangle(0, 0, 1, 1), new Rectangle(1, 1, 1, 1)});
  }

  /**
   * Three controls, one spanned horizontally.
   */
  public void test_spanHorizontal() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    {",
            "      Button button_0 = new Button(this, SWT.NONE);",
            "      button_0.setBounds(10, 10, 10, 10);",
            "    }",
            "    {",
            "      Button button_1 = new Button(this, SWT.NONE);",
            "      button_1.setBounds(30, 10, 10, 10);",
            "    }",
            "    {",
            "      Button button_2 = new Button(this, SWT.NONE);",
            "      button_2.setBounds(10, 30, 30, 10);",
            "    }",
            "  }",
            "}");
    setGridLayout(shell, new String[]{
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    {",
        "      Button button_0 = new Button(this, SWT.NONE);",
        "    }",
        "    {",
        "      Button button_1 = new Button(this, SWT.NONE);",
        "    }",
        "    {",
        "      Button button_2 = new Button(this, SWT.NONE);",
        "      button_2.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));",
        "    }",
        "  }",
        "}"}, new Rectangle[]{
        new Rectangle(0, 0, 1, 1),
        new Rectangle(1, 0, 1, 1),
        new Rectangle(0, 1, 2, 1)});
  }

  /**
   * Three controls, one spanned vertically.
   */
  public void test_spanVertical() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    {",
            "      Button button_0 = new Button(this, SWT.NONE);",
            "      button_0.setBounds(10, 10, 10, 10);",
            "    }",
            "    {",
            "      Button button_1 = new Button(this, SWT.NONE);",
            "      button_1.setBounds(30, 10, 10, 30);",
            "    }",
            "    {",
            "      Button button_2 = new Button(this, SWT.NONE);",
            "      button_2.setBounds(10, 30, 10, 10);",
            "    }",
            "  }",
            "}");
    setGridLayout(shell, new String[]{
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    {",
        "      Button button_0 = new Button(this, SWT.NONE);",
        "    }",
        "    {",
        "      Button button_1 = new Button(this, SWT.NONE);",
        "      button_1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 2));",
        "    }",
        "    {",
        "      Button button_2 = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}"}, new Rectangle[]{
        new Rectangle(0, 0, 1, 1),
        new Rectangle(1, 0, 1, 2),
        new Rectangle(0, 1, 1, 1)});
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the {@link GridLayout} for given {@link CompositeInfo}.
   */
  private void setGridLayout(CompositeInfo composite, String[] expectedLines, Rectangle[] cells)
      throws Exception {
    GridLayoutInfo layout = setGridLayout(composite, expectedLines);
    // check cells for control's
    int cellIndex = 0;
    for (ControlInfo control : composite.getChildrenControls()) {
      if (!layout.isFiller(control)) {
        GridDataInfo gridData = GridLayoutInfo.getGridData(control);
        assertEquals(cells[cellIndex++], new Rectangle(ReflectionUtils.getFieldInt(gridData, "x"),
            ReflectionUtils.getFieldInt(gridData, "y"),
            ReflectionUtils.getFieldInt(gridData, "width"),
            ReflectionUtils.getFieldInt(gridData, "height")));
      }
    }
  }

  private GridLayoutInfo setGridLayout(CompositeInfo composite, String[] expectedLines)
      throws Exception {
    composite.getRoot().refresh();
    // set GridLayout
    GridLayoutInfo gridLayout = createJavaInfo("org.eclipse.swt.layout.GridLayout");
    composite.setLayout(gridLayout);
    // check source
    assertEditor(expectedLines);
    return gridLayout;
  }
}
