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

import org.eclipse.wb.core.gef.policy.selection.NonResizableSelectionEditPolicy;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.internal.swt.gef.policy.layout.grid.GridSelectionEditPolicy;
import org.eclipse.wb.internal.swt.model.layout.grid.GridColumnInfo;
import org.eclipse.wb.internal.swt.model.layout.grid.GridLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.grid.GridRowInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.palette.AbsoluteLayoutEntryInfo;
import org.eclipse.wb.tests.designer.rcp.RcpGefTest;
import org.eclipse.wb.tests.gef.GraphicalRobot;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Tests for {@link GridLayout} in GEF.
 * 
 * @author scheglov_ke
 */
public class GridLayoutGefTest extends RcpGefTest {
  private static final int M = 5;
  private static final int S = 5;
  private static final int VS = 25;
  private static final int VG = 5;
  private CompositeInfo composite;
  private GridLayoutInfo layout;
  private GraphicalRobot horizontalRobot;
  private GraphicalRobot verticalRobot;

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
   * When we delete component of expandable/collapsible container, selection {@link Handle} receives
   * ancestor resize event, so tries to update {@link Handle} location. However at this time
   * component may be already deleted, so we can not ask for its cell/bounds.
   */
  public void test_deleteChildAndAncestorResize() throws Exception {
    openPanel(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new GridLayout());",
        "    {",
        "      Composite composite = new Composite(this, SWT.NONE);",
        "      composite.setLayout(new GridLayout());",
        "      {",
        "        Label label = new Label(composite, SWT.NONE);",
        "        label.setText('Label');",
        "      }",
        "      {",
        "        Button button = new Button(composite, SWT.NONE);",
        "        button.setText('Button');",
        "      }",
        "    }",
        "  }",
        "}");
    ControlInfo button = getJavaInfoByName("button");
    // select "button"
    canvas.select(button);
    waitEventLoop(0);
    // delete
    {
      IAction deleteAction = getDeleteAction();
      assertTrue(deleteAction.isEnabled());
      deleteAction.run();
      assertEditor(
          "public class Test extends Shell {",
          "  public Test() {",
          "    setLayout(new GridLayout());",
          "    {",
          "      Composite composite = new Composite(this, SWT.NONE);",
          "      composite.setLayout(new GridLayout());",
          "      {",
          "        Label label = new Label(composite, SWT.NONE);",
          "        label.setText('Label');",
          "      }",
          "    }",
          "  }",
          "}");
    }
  }

  /**
   * There was problem that after replacing {@link GridLayout} with "absolute", column/row headers
   * throw exception.
   */
  public void test_replaceGridLayout_withAbsolute() throws Exception {
    prepareComponent();
    openPanel(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    {",
        "      Button button_1 = new Button(this, SWT.NONE);",
        "    }",
        "    new Label(this, SWT.NONE);",
        "    new Label(this, SWT.NONE);",
        "    {",
        "      Button button_2 = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
    // select "shell", so show headers
    canvas.select(composite);
    waitEventLoop(0);
    // drop "absolute"
    {
      AbsoluteLayoutEntryInfo absoluteEntry = new AbsoluteLayoutEntryInfo();
      absoluteEntry.initialize(m_viewerCanvas, composite);
      absoluteEntry.activate(false);
      canvas.target(composite).in(250, 50).move().click();
      waitEventLoop(0);
    }
    // validate
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(null);",
        "    {",
        "      Button button_1 = new Button(this, SWT.NONE);",
        "      button_1.setBounds(5, 5, 100, 50);",
        "    }",
        "    {",
        "      Button button_2 = new Button(this, SWT.NONE);",
        "      button_2.setBounds(110, 60, 100, 50);",
        "    }",
        "  }",
        "}");
  }

  /**
   * When user externally (not using design canvas) changes "numColumns", we should recalculate
   * positions of controls, in other case we will have incorrect count of column/row headers.
   */
  public void test_change_numColumns() throws Exception {
    openPanel(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(1, false));",
        "    Button button_00 = new Button(this, SWT.NONE);",
        "    Button button_01 = new Button(this, SWT.NONE);",
        "  }",
        "}");
    // select "shell", so show headers
    canvas.select(composite);
    // initially: 1 column, 2 rows
    assertEquals(1, layout.getColumns().size());
    assertEquals(2, layout.getRows().size());
    // set: 2 columns, so 1 row
    // this caused exception in headers refresh
    layout.getPropertyByTitle("numColumns").setValue(2);
    assertNoLoggedExceptions();
    assertEquals(2, layout.getColumns().size());
    assertEquals(1, layout.getRows().size());
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    Button button_00 = new Button(this, SWT.NONE);",
        "    Button button_01 = new Button(this, SWT.NONE);",
        "  }",
        "}");
  }

  /**
   * When user marks {@link Control} as excluded, we should not use {@link GridSelectionEditPolicy}
   * for it.
   */
  public void test_markAsExcluded() throws Exception {
    openPanel(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
    ControlInfo button = getJavaInfoByName("button");
    GraphicalEditPart buttonPart = canvas.getEditPart(button);
    // select "button", so show grid selection
    canvas.select(button);
    assertThat(buttonPart.getEditPolicy(EditPolicy.SELECTION_ROLE)).isInstanceOf(
        GridSelectionEditPolicy.class);
    // set "exclude"
    GridLayoutInfo.getGridData(button).getPropertyByTitle("exclude").setValue(true);
    assertNoLoggedExceptions();
    assertThat(buttonPart.getEditPolicy(EditPolicy.SELECTION_ROLE)).isInstanceOf(
        NonResizableSelectionEditPolicy.class);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      {",
        "        GridData gd_button = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);",
        "        gd_button.exclude = true;",
        "        button.setLayoutData(gd_button);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Indirectly exposed {@link Control} should use simple selection policy.
   */
  public void test_indirectlyExposed() throws Exception {
    openPanel(
        "import org.eclipse.ui.dialogs.FilteredTree;",
        "import org.eclipse.ui.dialogs.PatternFilter;",
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new GridLayout(1, false));",
        "    FilteredTree filteredTree = new FilteredTree(this, SWT.NONE, new PatternFilter());",
        "  }",
        "}");
    CompositeInfo filteredTree = getJavaInfoByName("filteredTree");
    ControlInfo filterControl = filteredTree.getChildrenControls().get(0);
    // select "filterControl" has simple selection policy
    GraphicalEditPart buttonPart = canvas.getEditPart(filterControl);
    assertThat(buttonPart.getEditPolicy(EditPolicy.SELECTION_ROLE)).isInstanceOf(
        NonResizableSelectionEditPolicy.class);
  }

  /**
   * When we move {@link Control} from {@link GridLayout}, this should not cause exception.
   */
  public void test_moveOut() throws Exception {
    openPanel(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Group group = new Group(this, SWT.NONE);",
        "      group.setLayout(new FillLayout());",
        "      group.setText('My Group');",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
    CompositeInfo group = getJavaInfoByName("group");
    ControlInfo button = getJavaInfoByName("button");
    // select "button", check for "grid" selection
    canvas.select(button);
    {
      GraphicalEditPart buttonPart = canvas.getEditPart(button);
      EditPolicy selectionPolicy = buttonPart.getEditPolicy(EditPolicy.SELECTION_ROLE);
      assertThat(selectionPolicy).isInstanceOf(GridSelectionEditPolicy.class);
    }
    // drag "button" to "group"
    canvas.beginDrag(button).dragTo(group, 0.5, 0.5).endDrag();
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Group group = new Group(this, SWT.NONE);",
        "      group.setLayout(new FillLayout());",
        "      group.setText('My Group');",
        "      {",
        "        Button button = new Button(group, SWT.NONE);",
        "      }",
        "    }",
        "  }",
        "}");
    // no "grid" selection
    {
      GraphicalEditPart buttonPart = canvas.getEditPart(button);
      EditPolicy selectionPolicy = buttonPart.getEditPolicy(EditPolicy.SELECTION_ROLE);
      assertThat(selectionPolicy).isInstanceOf(NonResizableSelectionEditPolicy.class);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Size hint
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_setSizeHint_width() throws Exception {
    openPanel(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('New Button');",
        "    }",
        "  }",
        "}");
    ControlInfo button = getJavaInfoByName("button");
    // resize EAST of "button"
    canvas.toResizeHandle(button, "resize_size", IPositionConstants.EAST).beginDrag();
    canvas.target(button).in(200, 0).drag().endDrag();
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      {",
        "        GridData gd_button = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);",
        "        gd_button.widthHint = 200;",
        "        button.setLayoutData(gd_button);",
        "      }",
        "      button.setText('New Button');",
        "    }",
        "  }",
        "}");
  }

  public void test_setSizeHint_height() throws Exception {
    openPanel(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('New Button');",
        "    }",
        "  }",
        "}");
    ControlInfo button = getJavaInfoByName("button");
    // resize SOUTH of "button"
    canvas.toResizeHandle(button, "resize_size", IPositionConstants.SOUTH).beginDrag();
    canvas.target(button).in(0, 50).drag();
    canvas.endDrag();
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      {",
        "        GridData gd_button = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);",
        "        gd_button.heightHint = 50;",
        "        button.setLayoutData(gd_button);",
        "      }",
        "      button.setText('New Button');",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Alignment
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_setAlignment_usingKeyboard() throws Exception {
    openPanel(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('New Button');",
        "    }",
        "  }",
        "}");
    ControlInfo button = getJavaInfoByName("button");
    canvas.select(button);
    // horizontal
    {
      // grabH = true;
      canvas.keyDown(0, 'h');
      assert_setAlignment("SWT.LEFT, SWT.CENTER, true, false");
      // grabH = false;
      canvas.keyDown(0, 'h');
      assert_setAlignment(null);
      // alignmentH = RIGHT;
      canvas.keyDown(0, 'r');
      assert_setAlignment("SWT.RIGHT, SWT.CENTER, false, false");
      // alignmentH = CENTER;
      canvas.keyDown(0, 'c');
      assert_setAlignment("SWT.CENTER, SWT.CENTER, false, false");
      // alignmentH = FILL;
      canvas.keyDown(0, 'f');
      assert_setAlignment("SWT.FILL, SWT.CENTER, false, false");
      // alignmentH = LEFT;
      canvas.keyDown(0, 'l');
      assert_setAlignment(null);
    }
    // vertical
    {
      // grabV = true;
      canvas.keyDown(0, 'v');
      assert_setAlignment("SWT.LEFT, SWT.CENTER, false, true");
      // grabV = false;
      canvas.keyDown(0, 'v');
      assert_setAlignment(null);
      // alignmentV = TOP;
      canvas.keyDown(0, 't');
      assert_setAlignment("SWT.LEFT, SWT.TOP, false, false");
      // alignmentV = BOTTOM;
      canvas.keyDown(0, 'b');
      assert_setAlignment("SWT.LEFT, SWT.BOTTOM, false, false");
      // alignmentV = FILL;
      canvas.keyDown(0, 'F');
      assert_setAlignment("SWT.LEFT, SWT.FILL, false, false");
      // alignmentV = CENTER;
      canvas.keyDown(0, 'm');
      assert_setAlignment(null);
    }
    // grab/fill both
    {
      canvas.keyDown(0, 'o');
      assert_setAlignment("SWT.FILL, SWT.FILL, true, true");
    }
  }

  private void assert_setAlignment(String alignmentString) {
    if (alignmentString != null) {
      assertEditor(
          "class Test extends Shell {",
          "  Test() {",
          "    setLayout(new GridLayout(1, false));",
          "    {",
          "      Button button = new Button(this, SWT.NONE);",
          "      button.setLayoutData(new GridData(" + alignmentString + ", 1, 1));",
          "      button.setText('New Button');",
          "    }",
          "  }",
          "}");
    } else {
      assertEditor(
          "class Test extends Shell {",
          "  Test() {",
          "    setLayout(new GridLayout(1, false));",
          "    {",
          "      Button button = new Button(this, SWT.NONE);",
          "      button.setText('New Button');",
          "    }",
          "  }",
          "}");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE_filled() throws Exception {
    openPanel(
        "public class Test extends Composite {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button existingButton = new Button(this, SWT.NONE);",
        "      existingButton.setText('Existing Button');",
        "    }",
        "  }",
        "}");
    //
    loadButtonWithText();
    canvas.moveTo(composite, M, M);
    canvas.assertCommandNull();
  }

  public void test_CREATE_filledByInherited() throws Exception {
    setFileContentSrc(
        "test/MyShell.java",
        getTestSource2(
            "public class MyShell extends Shell {",
            "  public MyShell() {",
            "    super(SWT.NONE);",
            "    setLayout(new GridLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('Implicit Button');",
            "    }",
            "  }",
            "  protected void checkSubclass() {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    openPanel(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends MyShell {",
        "  public Test() {",
        "  }",
        "}");
    //
    loadButtonWithText();
    {
      Rectangle cell = new Rectangle(0, 0, 1, 1);
      Rectangle cellRectangle = layout.getGridInfo().getCellsRectangle(cell);
      Point cellCenter = cellRectangle.getCenter();
      canvas.moveTo(composite, cellCenter.x, cellCenter.y);
    }
    canvas.assertCommandNull();
  }

  public void test_CREATE_virtual_0x0() throws Exception {
    openPanel(
        "public class Test extends Composite {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setLayout(new GridLayout(1, false));",
        "  }",
        "}");
    //
    loadButtonWithText();
    canvas.moveTo(composite, M, M);
    canvas.click();
    assertEditor(
        "public class Test extends Composite {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('New Button');",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_virtual_0x1() throws Exception {
    openPanel(
        "public class Test extends Composite {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setLayout(new GridLayout(1, false));",
        "  }",
        "}");
    //
    loadButtonWithText();
    canvas.moveTo(composite, M + VS + VG, M);
    canvas.click();
    assertEditor(
        "public class Test extends Composite {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setLayout(new GridLayout(2, false));",
        "    new Label(this, SWT.NONE);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('New Button');",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_appendToColumn_1x0() throws Exception {
    openPanel(
        "public class Test extends Composite {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button existingButton = new Button(this, SWT.NONE);",
        "      existingButton.setText('Existing Button');",
        "    }",
        "  }",
        "}");
    JavaInfo existingButton = getJavaInfoByName("existingButton");
    //
    loadButtonWithText();
    canvas.target(existingButton).inX(0.5).outY(S + 1).move();
    canvas.click();
    assertEditor(
        "public class Test extends Composite {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button existingButton = new Button(this, SWT.NONE);",
        "      existingButton.setText('Existing Button');",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('New Button');",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_appendToRow_0x1() throws Exception {
    openPanel(
        "public class Test extends Composite {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button existingButton = new Button(this, SWT.NONE);",
        "      existingButton.setText('Existing Button');",
        "    }",
        "  }",
        "}");
    JavaInfo existingButton = getJavaInfoByName("existingButton");
    //
    loadButtonWithText();
    canvas.target(existingButton).inY(0.5).outX(S + 1).move();
    canvas.click();
    assertEditor(
        "public class Test extends Composite {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setLayout(new GridLayout(2, false));",
        "    {",
        "      Button existingButton = new Button(this, SWT.NONE);",
        "      existingButton.setText('Existing Button');",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('New Button');",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_beforeFirstRow() throws Exception {
    openPanel(
        "public class Test extends Composite {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button existingButton = new Button(this, SWT.NONE);",
        "      existingButton.setText('Existing Button');",
        "    }",
        "  }",
        "}");
    JavaInfo existingButton = getJavaInfoByName("existingButton");
    //
    loadButtonWithText();
    canvas.target(existingButton).inX(0.5).outY(-2).move();
    canvas.click();
    assertEditor(
        "public class Test extends Composite {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('New Button');",
        "    }",
        "    {",
        "      Button existingButton = new Button(this, SWT.NONE);",
        "      existingButton.setText('Existing Button');",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_beforeFirstColumn() throws Exception {
    openPanel(
        "public class Test extends Composite {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button existingButton = new Button(this, SWT.NONE);",
        "      existingButton.setText('Existing Button');",
        "    }",
        "  }",
        "}");
    JavaInfo existingButton = getJavaInfoByName("existingButton");
    //
    loadButtonWithText();
    canvas.target(existingButton).inY(0.5).outX(-2).move();
    canvas.click();
    assertEditor(
        "public class Test extends Composite {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setLayout(new GridLayout(2, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('New Button');",
        "    }",
        "    {",
        "      Button existingButton = new Button(this, SWT.NONE);",
        "      existingButton.setText('Existing Button');",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_insertColumn() throws Exception {
    openPanel(
        "public class Test extends Composite {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setLayout(new GridLayout(2, false));",
        "    {",
        "      Button button_1 = new Button(this, SWT.NONE);",
        "      button_1.setText('Button 1');",
        "    }",
        "    {",
        "      Button button_2 = new Button(this, SWT.NONE);",
        "      button_2.setText('Button 2');",
        "    }",
        "  }",
        "}");
    JavaInfo button_1 = getJavaInfoByName("button_1");
    //
    loadButtonWithText();
    canvas.target(button_1).inY(0.5).outX(S / 2).move();
    canvas.click();
    assertEditor(
        "public class Test extends Composite {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setLayout(new GridLayout(3, false));",
        "    {",
        "      Button button_1 = new Button(this, SWT.NONE);",
        "      button_1.setText('Button 1');",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('New Button');",
        "    }",
        "    {",
        "      Button button_2 = new Button(this, SWT.NONE);",
        "      button_2.setText('Button 2');",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_insertRow() throws Exception {
    openPanel(
        "public class Test extends Composite {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button button_1 = new Button(this, SWT.NONE);",
        "      button_1.setText('Button 1');",
        "    }",
        "    {",
        "      Button button_2 = new Button(this, SWT.NONE);",
        "      button_2.setText('Button 2');",
        "    }",
        "  }",
        "}");
    JavaInfo button_1 = getJavaInfoByName("button_1");
    //
    loadButtonWithText();
    canvas.target(button_1).inX(0.5).outY(S / 2).move();
    canvas.click();
    assertEditor(
        "public class Test extends Composite {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button button_1 = new Button(this, SWT.NONE);",
        "      button_1.setText('Button 1');",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('New Button');",
        "    }",
        "    {",
        "      Button button_2 = new Button(this, SWT.NONE);",
        "      button_2.setText('Button 2');",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE and inherited layout (can not change dimensions)
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When {@link GridLayoutInfo} is inherited, we can not change its columns.
   */
  public void test_CREATE_inherited_columnOperations() throws Exception {
    setFileContentSrc(
        "test/MyShell.java",
        getTestSource2(
            "public class MyShell extends Shell {",
            "  public MyShell() {",
            "    super(SWT.NONE);",
            "    setLayout(new GridLayout(2, false));",
            "    {",
            "      Button button_1 = new Button(this, SWT.NONE);",
            "      button_1.setText('Implicit #1');",
            "    }",
            "    {",
            "      Button button_2 = new Button(this, SWT.NONE);",
            "      button_2.setText('Implicit #2');",
            "    }",
            "  }",
            "  protected void checkSubclass() {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    openPanel(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends MyShell {",
        "  public Test() {",
        "    {",
        "      Button button_1 = new Button(this, SWT.NONE);",
        "      button_1.setText('Explicit #1');",
        "    }",
        "    {",
        "      Button button_2 = new Button(this, SWT.NONE);",
        "      button_2.setText('Explicit #2');",
        "    }",
        "  }",
        "}");
    loadButton();
    // can not insert column
    {
      ControlInfo button_1 = getJavaInfoByName("button_1");
      canvas.target(button_1).inY(0.5).inX(-2).move();
      canvas.assertCommandNull();
    }
    // can not append column
    {
      ControlInfo button_2 = getJavaInfoByName("button_2");
      canvas.target(button_2).inY(0.5).outX(S + 1).move();
      canvas.assertCommandNull();
    }
  }

  /**
   * When {@link GridLayoutInfo} is inherited, but no inherited {@link Control}s, so we can change
   * columns.
   */
  public void test_CREATE_inheritedEmpty_columnOperations() throws Exception {
    setFileContentSrc(
        "test/MyShell.java",
        getTestSource2(
            "public class MyShell extends Shell {",
            "  public MyShell() {",
            "    super(SWT.NONE);",
            "    setLayout(new GridLayout(2, false));",
            "  }",
            "  protected void checkSubclass() {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    openPanel(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends MyShell {",
        "  public Test() {",
        "    {",
        "      Button button_1 = new Button(this, SWT.NONE);",
        "      button_1.setText('Explicit #1');",
        "    }",
        "    {",
        "      Button button_2 = new Button(this, SWT.NONE);",
        "      button_2.setText('Explicit #2');",
        "    }",
        "  }",
        "}");
    loadButton();
    // can insert column
    {
      ControlInfo button_1 = getJavaInfoByName("button_1");
      canvas.target(button_1).inY(0.5).inX(-2).move();
      canvas.assertCommandNotNull();
    }
    // can append column
    {
      ControlInfo button_2 = getJavaInfoByName("button_2");
      canvas.target(button_2).inY(0.5).outX(S + 1).move();
      canvas.assertCommandNotNull();
    }
  }

  /**
   * When {@link GridLayoutInfo} is inherited, we can not change only explicit rows.
   */
  public void test_CREATE_inherited_rowOperations() throws Exception {
    setFileContentSrc(
        "test/MyShell.java",
        getTestSource2(
            "public class MyShell extends Shell {",
            "  public MyShell() {",
            "    super(SWT.NONE);",
            "    setLayout(new GridLayout(2, false));",
            "    {",
            "      Button button_1 = new Button(this, SWT.NONE);",
            "      button_1.setText('Implicit #1');",
            "    }",
            "    {",
            "      Button button_2 = new Button(this, SWT.NONE);",
            "      button_2.setText('Implicit #2');",
            "    }",
            "  }",
            "  protected void checkSubclass() {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    openPanel(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends MyShell {",
        "  public Test() {",
        "    {",
        "      Button button_1 = new Button(this, SWT.NONE);",
        "      button_1.setText('Explicit #1');",
        "    }",
        "    {",
        "      Button button_2 = new Button(this, SWT.NONE);",
        "      button_2.setText('Explicit #2');",
        "    }",
        "  }",
        "}");
    loadButton();
    // can not insert before "implicit" row
    {
      canvas.target(composite).inX(M + 5).inY(0).move();
      canvas.assertCommandNull();
    }
    // can insert before "explicit" row
    {
      ControlInfo button_1 = getJavaInfoByName("button_1");
      canvas.target(button_1).inX(0.5).outY(-1).move();
      canvas.assertCommandNotNull();
    }
    // can append row
    {
      ControlInfo button_1 = getJavaInfoByName("button_1");
      canvas.target(button_1).inX(0.5).outY(S + 1).move();
      canvas.assertCommandNotNull();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PASTE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_PASTE_virtual_1x0() throws Exception {
    openPanel(
        "public class Test extends Composite {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button existingButton = new Button(this, SWT.NONE);",
        "      existingButton.setText('My Button');",
        "    }",
        "  }",
        "}");
    JavaInfo existingButton = getJavaInfoByName("existingButton");
    //
    doCopyPaste(existingButton);
    canvas.target(existingButton).inX(0.5).outY(S + 1).move();
    canvas.click();
    assertEditor(
        "public class Test extends Composite {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button existingButton = new Button(this, SWT.NONE);",
        "      existingButton.setText('My Button');",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('My Button');",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_MOVE_virtual_1x0() throws Exception {
    openPanel(
        "public class Test extends Composite {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('Existing Button');",
        "    }",
        "  }",
        "}");
    JavaInfo button = getJavaInfoByName("button");
    //
    canvas.beginDrag(button);
    canvas.target(button).inX(0.5).outY(S + 1).drag();
    canvas.endDrag();
    assertEditor(
        "public class Test extends Composite {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setLayout(new GridLayout(1, false));",
        "    new Label(this, SWT.NONE);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('Existing Button');",
        "    }",
        "  }",
        "}");
  }

  public void test_ADD_virtual_0x0() throws Exception {
    openPanel(
        "public class Test extends Composite {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setLayout(new FillLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('Existing Button');",
        "    }",
        "    {",
        "      Composite target = new Composite(this, SWT.NONE);",
        "      target.setLayout(new GridLayout(1, false));",
        "    }",
        "  }",
        "}");
    JavaInfo button = getJavaInfoByName("button");
    JavaInfo target = getJavaInfoByName("target");
    //
    canvas.beginDrag(button);
    canvas.dragTo(target, M + VS / 2, M + VS / 2);
    canvas.endDrag();
    assertEditor(
        "public class Test extends Composite {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setLayout(new FillLayout());",
        "    {",
        "      Composite target = new Composite(this, SWT.NONE);",
        "      target.setLayout(new GridLayout(1, false));",
        "      {",
        "        Button button = new Button(target, SWT.NONE);",
        "        button.setText('Existing Button');",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Column headers
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When {@link GridLayoutInfo} is inherited, we can not move its columns.
   */
  public void test_headerColumn_MOVE_inherited() throws Exception {
    setFileContentSrc(
        "test/MyShell.java",
        getTestSource2(
            "public class MyShell extends Shell {",
            "  public MyShell() {",
            "    super(SWT.NONE);",
            "    setLayout(new GridLayout(2, false));",
            "    {",
            "      Button button_1 = new Button(this, SWT.NONE);",
            "      button_1.setText('Implicit #1');",
            "    }",
            "    {",
            "      Button button_2 = new Button(this, SWT.NONE);",
            "      button_2.setText('Implicit #2');",
            "    }",
            "  }",
            "  protected void checkSubclass() {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    openPanel(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends MyShell {",
        "  public Test() {",
        "  }",
        "}");
    // select "composite" to show headers
    canvas.select(composite);
    // animate headers
    {
      List<GridColumnInfo<ControlInfo>> columns = layout.getColumns();
      GridColumnInfo<ControlInfo> sourceColumn = columns.get(0);
      GridColumnInfo<ControlInfo> relativeColumn = columns.get(1);
      horizontalRobot.beginDrag(sourceColumn).dragTo(relativeColumn, -5, 0.5);
      horizontalRobot.assertCommandNull();
    }
  }

  public void test_headerColumn_MOVE_beforeFirst() throws Exception {
    openPanel(
        "public class Test extends Shell {",
        "  public Test() {",
        "    super(SWT.NONE);",
        "    setLayout(new GridLayout(2, false));",
        "    {",
        "      Button button_1 = new Button(this, SWT.NONE);",
        "      button_1.setText('Implicit #1');",
        "    }",
        "    {",
        "      Button button_2 = new Button(this, SWT.NONE);",
        "      button_2.setText('Implicit #2');",
        "    }",
        "  }",
        "}");
    // select "composite" to show headers
    canvas.select(composite);
    // animate headers
    {
      List<GridColumnInfo<ControlInfo>> columns = layout.getColumns();
      GridColumnInfo<ControlInfo> sourceColumn = columns.get(1);
      GridColumnInfo<ControlInfo> relativeColumn = columns.get(0);
      horizontalRobot.beginDrag(sourceColumn).dragTo(relativeColumn, 5, 0.5);
      horizontalRobot.assertCommandNotNull();
      horizontalRobot.endDrag();
    }
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    super(SWT.NONE);",
        "    setLayout(new GridLayout(2, false));",
        "    {",
        "      Button button_2 = new Button(this, SWT.NONE);",
        "      button_2.setText('Implicit #2');",
        "    }",
        "    {",
        "      Button button_1 = new Button(this, SWT.NONE);",
        "      button_1.setText('Implicit #1');",
        "    }",
        "  }",
        "}");
  }

  public void test_headerColumn_MOVE_afterLast() throws Exception {
    openPanel(
        "public class Test extends Shell {",
        "  public Test() {",
        "    super(SWT.NONE);",
        "    setLayout(new GridLayout(3, false));",
        "    {",
        "      Button button_1 = new Button(this, SWT.NONE);",
        "      button_1.setText('Button #1');",
        "    }",
        "    {",
        "      Button button_2 = new Button(this, SWT.NONE);",
        "      button_2.setText('Button #2');",
        "    }",
        "    {",
        "      Button button_3 = new Button(this, SWT.NONE);",
        "      button_3.setText('Button #3');",
        "    }",
        "  }",
        "}");
    // select "composite" to show headers
    canvas.select(composite);
    // animate headers
    {
      List<GridColumnInfo<ControlInfo>> columns = layout.getColumns();
      GridColumnInfo<ControlInfo> sourceColumn = columns.get(0);
      GridColumnInfo<ControlInfo> relativeColumn = columns.get(2);
      horizontalRobot.beginDrag(sourceColumn).dragTo(relativeColumn, -5, 0.5);
      horizontalRobot.assertCommandNotNull();
      horizontalRobot.endDrag();
    }
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    super(SWT.NONE);",
        "    setLayout(new GridLayout(3, false));",
        "    {",
        "      Button button_2 = new Button(this, SWT.NONE);",
        "      button_2.setText('Button #2');",
        "    }",
        "    {",
        "      Button button_3 = new Button(this, SWT.NONE);",
        "      button_3.setText('Button #3');",
        "    }",
        "    {",
        "      Button button_1 = new Button(this, SWT.NONE);",
        "      button_1.setText('Button #1');",
        "    }",
        "  }",
        "}");
  }

  public void test_headerColumn_MOVE_beforeOther() throws Exception {
    openPanel(
        "public class Test extends Shell {",
        "  public Test() {",
        "    super(SWT.NONE);",
        "    setLayout(new GridLayout(3, false));",
        "    {",
        "      Button button_1 = new Button(this, SWT.NONE);",
        "      button_1.setText('Button #1');",
        "    }",
        "    {",
        "      Button button_2 = new Button(this, SWT.NONE);",
        "      button_2.setText('Button #2');",
        "    }",
        "    {",
        "      Button button_3 = new Button(this, SWT.NONE);",
        "      button_3.setText('Button #3');",
        "    }",
        "  }",
        "}");
    // select "composite" to show headers
    canvas.select(composite);
    // animate headers
    {
      List<GridColumnInfo<ControlInfo>> columns = layout.getColumns();
      GridColumnInfo<ControlInfo> sourceColumn = columns.get(0);
      GridColumnInfo<ControlInfo> relativeColumn = columns.get(2);
      horizontalRobot.beginDrag(sourceColumn).dragTo(relativeColumn, +5, 0.5);
      horizontalRobot.assertCommandNotNull();
      horizontalRobot.endDrag();
    }
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    super(SWT.NONE);",
        "    setLayout(new GridLayout(3, false));",
        "    {",
        "      Button button_2 = new Button(this, SWT.NONE);",
        "      button_2.setText('Button #2');",
        "    }",
        "    {",
        "      Button button_1 = new Button(this, SWT.NONE);",
        "      button_1.setText('Button #1');",
        "    }",
        "    {",
        "      Button button_3 = new Button(this, SWT.NONE);",
        "      button_3.setText('Button #3');",
        "    }",
        "  }",
        "}");
  }

  public void test_headerColumn_MOVE_beforeOther_RTL() throws Exception {
    openPanel(
        "public class Test extends Shell {",
        "  public Test() {",
        "    super(SWT.RIGHT_TO_LEFT);",
        "    setLayout(new GridLayout(3, false));",
        "    {",
        "      Button button_1 = new Button(this, SWT.NONE);",
        "      button_1.setText('Button #1');",
        "    }",
        "    {",
        "      Button button_2 = new Button(this, SWT.NONE);",
        "      button_2.setText('Button #2');",
        "    }",
        "    {",
        "      Button button_3 = new Button(this, SWT.NONE);",
        "      button_3.setText('Button #3');",
        "    }",
        "  }",
        "}");
    // select "composite" to show headers
    canvas.select(composite);
    // animate headers
    {
      List<GridColumnInfo<ControlInfo>> columns = layout.getColumns();
      GridColumnInfo<ControlInfo> sourceColumn = columns.get(0);
      GridColumnInfo<ControlInfo> relativeColumn = columns.get(2);
      horizontalRobot.beginDrag(sourceColumn).dragTo(relativeColumn, -5, 0.5);
      horizontalRobot.assertCommandNotNull();
      horizontalRobot.endDrag();
    }
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    super(SWT.RIGHT_TO_LEFT);",
        "    setLayout(new GridLayout(3, false));",
        "    {",
        "      Button button_2 = new Button(this, SWT.NONE);",
        "      button_2.setText('Button #2');",
        "    }",
        "    {",
        "      Button button_1 = new Button(this, SWT.NONE);",
        "      button_1.setText('Button #1');",
        "    }",
        "    {",
        "      Button button_3 = new Button(this, SWT.NONE);",
        "      button_3.setText('Button #3');",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Row headers
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When {@link GridLayoutInfo} is inherited, we can not move its "implicit" row.
   */
  public void test_headerRow_MOVE_inherited_moveImplicitRow() throws Exception {
    setFileContentSrc(
        "test/MyShell.java",
        getTestSource2(
            "public class MyShell extends Shell {",
            "  public MyShell() {",
            "    super(SWT.NONE);",
            "    setLayout(new GridLayout(1, false));",
            "    {",
            "      Button button_1 = new Button(this, SWT.NONE);",
            "      button_1.setText('Implicit #1');",
            "    }",
            "    {",
            "      Button button_2 = new Button(this, SWT.NONE);",
            "      button_2.setText('Implicit #2');",
            "    }",
            "  }",
            "  protected void checkSubclass() {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    openPanel(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends MyShell {",
        "  public Test() {",
        "  }",
        "}");
    // select "composite" to show headers
    canvas.select(composite);
    // animate headers
    {
      List<GridRowInfo<ControlInfo>> rows = layout.getRows();
      GridRowInfo<ControlInfo> sourceRow = rows.get(1);
      GridRowInfo<ControlInfo> relativeRow = rows.get(0);
      verticalRobot.beginDrag(sourceRow).dragTo(relativeRow, 0.5, +1);
      verticalRobot.assertCommandNull();
    }
  }

  /**
   * When {@link GridLayoutInfo} is inherited, we can not move "explicit" row before "implicit" one.
   */
  public void test_headerRow_MOVE_inherited_moveBeforeImplicitRow() throws Exception {
    setFileContentSrc(
        "test/MyShell.java",
        getTestSource2(
            "public class MyShell extends Shell {",
            "  public MyShell() {",
            "    super(SWT.NONE);",
            "    setLayout(new GridLayout(1, false));",
            "    {",
            "      Button button_1 = new Button(this, SWT.NONE);",
            "      button_1.setText('Implicit #1');",
            "    }",
            "    {",
            "      Button button_2 = new Button(this, SWT.NONE);",
            "      button_2.setText('Implicit #2');",
            "    }",
            "  }",
            "  protected void checkSubclass() {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    openPanel(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends MyShell {",
        "  public Test() {",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('Explicit Button');",
        "    }",
        "  }",
        "}");
    // select "composite" to show headers
    canvas.select(composite);
    // animate headers
    {
      List<GridRowInfo<ControlInfo>> rows = layout.getRows();
      GridRowInfo<ControlInfo> sourceRow = rows.get(2);
      GridRowInfo<ControlInfo> relativeRow = rows.get(1);
      verticalRobot.beginDrag(sourceRow).dragTo(relativeRow, 0.5, +1);
      verticalRobot.assertCommandNull();
    }
  }

  public void test_headerRow_MOVE_beforeOther() throws Exception {
    openPanel(
        "public class Test extends Shell {",
        "  public Test() {",
        "    super(SWT.NONE);",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button button_1 = new Button(this, SWT.NONE);",
        "      button_1.setText('Button #1');",
        "    }",
        "    {",
        "      Button button_2 = new Button(this, SWT.NONE);",
        "      button_2.setText('Button #2');",
        "    }",
        "    {",
        "      Button button_3 = new Button(this, SWT.NONE);",
        "      button_3.setText('Button #3');",
        "    }",
        "  }",
        "}");
    // select "composite" to show headers
    canvas.select(composite);
    // animate headers
    {
      List<GridRowInfo<ControlInfo>> rows = layout.getRows();
      GridRowInfo<ControlInfo> sourceRow = rows.get(2);
      GridRowInfo<ControlInfo> relativeRow = rows.get(0);
      verticalRobot.beginDrag(sourceRow).dragTo(relativeRow, 0.5, +1);
      verticalRobot.assertCommandNotNull();
      verticalRobot.endDrag();
    }
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    super(SWT.NONE);",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button button_3 = new Button(this, SWT.NONE);",
        "      button_3.setText('Button #3');",
        "    }",
        "    {",
        "      Button button_1 = new Button(this, SWT.NONE);",
        "      button_1.setText('Button #1');",
        "    }",
        "    {",
        "      Button button_2 = new Button(this, SWT.NONE);",
        "      button_2.setText('Button #2');",
        "    }",
        "  }",
        "}");
  }

  public void test_headerRow_MOVE_afterLast() throws Exception {
    openPanel(
        "public class Test extends Shell {",
        "  public Test() {",
        "    super(SWT.NONE);",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button button_1 = new Button(this, SWT.NONE);",
        "      button_1.setText('Button #1');",
        "    }",
        "    {",
        "      Button button_2 = new Button(this, SWT.NONE);",
        "      button_2.setText('Button #2');",
        "    }",
        "    {",
        "      Button button_3 = new Button(this, SWT.NONE);",
        "      button_3.setText('Button #3');",
        "    }",
        "  }",
        "}");
    // select "composite" to show headers
    canvas.select(composite);
    // animate headers
    {
      List<GridRowInfo<ControlInfo>> rows = layout.getRows();
      GridRowInfo<ControlInfo> sourceRow = rows.get(0);
      GridRowInfo<ControlInfo> relativeRow = rows.get(2);
      verticalRobot.beginDrag(sourceRow).dragTo(relativeRow, 0.5, -1);
      verticalRobot.assertCommandNotNull();
      verticalRobot.endDrag();
    }
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    super(SWT.NONE);",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button button_2 = new Button(this, SWT.NONE);",
        "      button_2.setText('Button #2');",
        "    }",
        "    {",
        "      Button button_3 = new Button(this, SWT.NONE);",
        "      button_3.setText('Button #3');",
        "    }",
        "    {",
        "      Button button_1 = new Button(this, SWT.NONE);",
        "      button_1.setText('Button #1');",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private void openPanel(String... lines) throws Exception {
    composite = openComposite(lines);
    if (composite.getLayout() instanceof GridLayoutInfo) {
      layout = (GridLayoutInfo) composite.getLayout();
    }
    horizontalRobot = new GraphicalRobot(m_headerHorizontal);
    verticalRobot = new GraphicalRobot(m_headerVertical);
  }
}
