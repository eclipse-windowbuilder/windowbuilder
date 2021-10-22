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
package org.eclipse.wb.tests.designer.swing.model.layout.spring;

import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.internal.core.model.layout.absolute.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.swing.Activator;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.IntValue;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;

import org.eclipse.jface.preference.IPreferenceStore;

import java.lang.reflect.Field;
import java.util.List;

import javax.swing.SpringLayout;

/**
 * GEF tests for {@link SpringLayout} and absolute framework in general.
 *
 * @author scheglov_ke
 */
public class SpringLayoutGefTest extends SwingGefTest {
  private ContainerInfo panel;
  private ComponentInfo box;
  private ComponentInfo anchor;
  private ComponentInfo boxA;
  private ComponentInfo boxB;
  private ComponentInfo boxC;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    if (m_testProject == null) {
      do_projectCreate();
    }
    // preferences
    {
      IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
      preferenceStore.setValue(IPreferenceConstants.P_COMPONENT_GAP_LEFT, 5);
      preferenceStore.setValue(IPreferenceConstants.P_COMPONENT_GAP_RIGHT, 5);
      preferenceStore.setValue(IPreferenceConstants.P_COMPONENT_GAP_TOP, 5);
      preferenceStore.setValue(IPreferenceConstants.P_COMPONENT_GAP_BOTTOM, 5);
    }
  }

  @Override
  protected void tearDown() throws Exception {
    for (Field field : getClass().getDeclaredFields()) {
      field.set(this, null);
    }
    // preferences
    {
      IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
      preferenceStore.setToDefault(IPreferenceConstants.P_COMPONENT_GAP_LEFT);
      preferenceStore.setToDefault(IPreferenceConstants.P_COMPONENT_GAP_RIGHT);
      preferenceStore.setToDefault(IPreferenceConstants.P_COMPONENT_GAP_TOP);
      preferenceStore.setToDefault(IPreferenceConstants.P_COMPONENT_GAP_BOTTOM);
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
  // CREATE, absolute
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE_absoluteNoSnap_topLeft() throws Exception {
    prepare_CREATE_emptyPanel();
    canvas.target(panel).inX(30).inY(50).move().click();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    SpringLayout layout = new SpringLayout();",
        "    setLayout(layout);",
        "    {",
        "      Box box = new Box();",
        "      layout.putConstraint(SpringLayout.NORTH, box, 50, SpringLayout.NORTH, this);",
        "      layout.putConstraint(SpringLayout.WEST, box, 30, SpringLayout.WEST, this);",
        "      add(box);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_absoluteNoSnap_bottomRight() throws Exception {
    prepare_CREATE_emptyPanel();
    canvas.target(panel).inX(250).inY(200).move().click();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    SpringLayout layout = new SpringLayout();",
        "    setLayout(layout);",
        "    {",
        "      Box box = new Box();",
        "      layout.putConstraint(SpringLayout.SOUTH, box, -50, SpringLayout.SOUTH, this);",
        "      layout.putConstraint(SpringLayout.EAST, box, -100, SpringLayout.EAST, this);",
        "      add(box);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_absoluteSnap_leftOffset() throws Exception {
    prepare_CREATE_emptyPanel();
    canvas.target(panel).inX(2).inY(50).move().click();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    SpringLayout layout = new SpringLayout();",
        "    setLayout(layout);",
        "    {",
        "      Box box = new Box();",
        "      layout.putConstraint(SpringLayout.NORTH, box, 50, SpringLayout.NORTH, this);",
        "      layout.putConstraint(SpringLayout.WEST, box, 10, SpringLayout.WEST, this);",
        "      add(box);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_absoluteSnap_leftZero() throws Exception {
    prepare_CREATE_emptyPanel();
    canvas.target(panel).outX(-2).inY(50).move().click();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    SpringLayout layout = new SpringLayout();",
        "    setLayout(layout);",
        "    {",
        "      Box box = new Box();",
        "      layout.putConstraint(SpringLayout.NORTH, box, 50, SpringLayout.NORTH, this);",
        "      layout.putConstraint(SpringLayout.WEST, box, 0, SpringLayout.WEST, this);",
        "      add(box);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_absoluteSnap_rightOffset() throws Exception {
    prepare_CREATE_emptyPanel();
    canvas.target(panel).rightSide().inX(-2).inY(50).move().click();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    SpringLayout layout = new SpringLayout();",
        "    setLayout(layout);",
        "    {",
        "      Box box = new Box();",
        "      layout.putConstraint(SpringLayout.NORTH, box, 50, SpringLayout.NORTH, this);",
        "      layout.putConstraint(SpringLayout.EAST, box, -10, SpringLayout.EAST, this);",
        "      add(box);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_absoluteSnap_rightZero() throws Exception {
    prepare_CREATE_emptyPanel();
    canvas.target(panel).rightSide().outX(2).inY(50).move().click();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    SpringLayout layout = new SpringLayout();",
        "    setLayout(layout);",
        "    {",
        "      Box box = new Box();",
        "      layout.putConstraint(SpringLayout.NORTH, box, 50, SpringLayout.NORTH, this);",
        "      layout.putConstraint(SpringLayout.EAST, box, 0, SpringLayout.EAST, this);",
        "      add(box);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_absoluteSnap_topOffset() throws Exception {
    prepare_CREATE_emptyPanel();
    canvas.target(panel).inX(30).inY(2).move().click();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    SpringLayout layout = new SpringLayout();",
        "    setLayout(layout);",
        "    {",
        "      Box box = new Box();",
        "      layout.putConstraint(SpringLayout.NORTH, box, 10, SpringLayout.NORTH, this);",
        "      layout.putConstraint(SpringLayout.WEST, box, 30, SpringLayout.WEST, this);",
        "      add(box);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_absoluteSnap_topZero() throws Exception {
    prepare_CREATE_emptyPanel();
    canvas.target(panel).inX(30).outY(-2).move().click();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    SpringLayout layout = new SpringLayout();",
        "    setLayout(layout);",
        "    {",
        "      Box box = new Box();",
        "      layout.putConstraint(SpringLayout.NORTH, box, 0, SpringLayout.NORTH, this);",
        "      layout.putConstraint(SpringLayout.WEST, box, 30, SpringLayout.WEST, this);",
        "      add(box);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_absoluteSnap_bottomOffset() throws Exception {
    prepare_CREATE_emptyPanel();
    canvas.target(panel).inX(30).bottomSide().inY(-2).move().click();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    SpringLayout layout = new SpringLayout();",
        "    setLayout(layout);",
        "    {",
        "      Box box = new Box();",
        "      layout.putConstraint(SpringLayout.WEST, box, 30, SpringLayout.WEST, this);",
        "      layout.putConstraint(SpringLayout.SOUTH, box, -10, SpringLayout.SOUTH, this);",
        "      add(box);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_absoluteSnap_bottomZero() throws Exception {
    prepare_CREATE_emptyPanel();
    canvas.target(panel).inX(30).bottomSide().outY(2).move().click();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    SpringLayout layout = new SpringLayout();",
        "    setLayout(layout);",
        "    {",
        "      Box box = new Box();",
        "      layout.putConstraint(SpringLayout.WEST, box, 30, SpringLayout.WEST, this);",
        "      layout.putConstraint(SpringLayout.SOUTH, box, 0, SpringLayout.SOUTH, this);",
        "      add(box);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_ansoluteNoSnap_andResize_absoluteNoSnap() throws Exception {
    prepare_CREATE_emptyPanel();
    canvas.target(panel).inX(30).inY(50).move();
    canvas.beginDrag().dragOn(100, 50).endDrag();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    SpringLayout layout = new SpringLayout();",
        "    setLayout(layout);",
        "    {",
        "      Box box = new Box();",
        "      layout.putConstraint(SpringLayout.NORTH, box, 50, SpringLayout.NORTH, this);",
        "      layout.putConstraint(SpringLayout.WEST, box, 30, SpringLayout.WEST, this);",
        "      layout.putConstraint(SpringLayout.SOUTH, box, 125, SpringLayout.NORTH, this);",
        "      layout.putConstraint(SpringLayout.EAST, box, 180, SpringLayout.WEST, this);",
        "      add(box);",
        "    }",
        "  }",
        "}");
  }

  private void prepare_CREATE_emptyPanel() throws Exception {
    prepareBox();
    parse_forCREATE(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    SpringLayout layout = new SpringLayout();",
        "    setLayout(layout);",
        "  }",
        "}");
    // begin CREATE
    loadCreationBox();
  }

  private void parse_forCREATE(String... lines) throws Exception {
    panel = openContainer(lines);
    // configure for CREATE
    canvas.create(100, 50).sideMode();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE, anchor
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE_anchor_TL2TR() throws Exception {
    prepare_CREATE_singlePanel();
    canvas.mouseMode().target(panel).inX(-1).inY(-1).move();
    canvas.sideMode().target(anchor).outX(2).inY(2).move().click();
    assert_CREATE_singlePanel(
        "      layout.putConstraint(SpringLayout.NORTH, box, 0, SpringLayout.NORTH, anchor);",
        "      layout.putConstraint(SpringLayout.WEST, box, 5, SpringLayout.EAST, anchor);");
  }

  public void test_CREATE_anchor_TL2BL() throws Exception {
    prepare_CREATE_singlePanel();
    canvas.mouseMode().target(panel).inX(-1).inY(-1).move();
    canvas.sideMode().target(anchor).outX(-2).outY(2).move().click();
    assert_CREATE_singlePanel(
        "      layout.putConstraint(SpringLayout.NORTH, box, 5, SpringLayout.SOUTH, anchor);",
        "      layout.putConstraint(SpringLayout.WEST, box, 0, SpringLayout.WEST, anchor);");
  }

  public void test_CREATE_anchor_TL2BL_indent() throws Exception {
    prepare_CREATE_singlePanel();
    canvas.mouseMode().target(panel).inX(-1).inY(-1).move();
    canvas.sideMode().target(anchor).inX(10).outY(2).move().click();
    assert_CREATE_singlePanel(
        "      layout.putConstraint(SpringLayout.NORTH, box, 5, SpringLayout.SOUTH, anchor);",
        "      layout.putConstraint(SpringLayout.WEST, box, 10, SpringLayout.WEST, anchor);");
  }

  public void test_CREATE_anchor_TL2BL_indent2() throws Exception {
    prepare_CREATE_singlePanel();
    // move right/below
    canvas.target(anchor).outX(50).outY(50).move();
    // attach top-left with indent
    canvas.target(anchor).inX(12).outY(2).move();
    // finish
    canvas.click();
    assert_CREATE_singlePanel(
        "      layout.putConstraint(SpringLayout.NORTH, box, 5, SpringLayout.SOUTH, anchor);",
        "      layout.putConstraint(SpringLayout.WEST, box, 10, SpringLayout.WEST, anchor);");
  }

  public void test_CREATE_anchor_TR2TL() throws Exception {
    prepare_CREATE_singlePanel();
    canvas.mouseMode().target(panel).inX(1).inY(-1).move();
    canvas.sideMode().target(anchor).rightSide().outX(-2).inY(2).move().click();
    assert_CREATE_singlePanel(
        "      layout.putConstraint(SpringLayout.NORTH, box, 0, SpringLayout.NORTH, anchor);",
        "      layout.putConstraint(SpringLayout.EAST, box, -5, SpringLayout.WEST, anchor);");
  }

  public void test_CREATE_anchor_BL2BR() throws Exception {
    prepare_CREATE_singlePanel();
    canvas.mouseMode().target(panel).inX(-1).inY(1).move();
    canvas.sideMode().target(anchor).outX(2).bottomSide().inY(-2).move().click();
    assert_CREATE_singlePanel(
        "      layout.putConstraint(SpringLayout.WEST, box, 5, SpringLayout.EAST, anchor);",
        "      layout.putConstraint(SpringLayout.SOUTH, box, 0, SpringLayout.SOUTH, anchor);");
  }

  public void test_CREATE_anchor_BL2TL() throws Exception {
    prepare_CREATE_singlePanel();
    canvas.mouseMode().target(panel).inX(-1).inY(1).move();
    canvas.sideMode().target(anchor).inX(2).bottomSide().outY(-2).move().click();
    assert_CREATE_singlePanel(
        "      layout.putConstraint(SpringLayout.WEST, box, 0, SpringLayout.WEST, anchor);",
        "      layout.putConstraint(SpringLayout.SOUTH, box, -5, SpringLayout.NORTH, anchor);");
  }

  public void test_CREATE_anchor_BR2BL() throws Exception {
    prepare_CREATE_singlePanel();
    canvas.mouseMode().target(panel).inX(1).inY(1).move();
    canvas.sideMode().target(anchor).rightSide().outX(-2).bottomSide().inY(-2).move().click();
    assert_CREATE_singlePanel(
        "      layout.putConstraint(SpringLayout.SOUTH, box, 0, SpringLayout.SOUTH, anchor);",
        "      layout.putConstraint(SpringLayout.EAST, box, -5, SpringLayout.WEST, anchor);");
  }

  public void test_CREATE_anchor_BR2TR() throws Exception {
    prepare_CREATE_singlePanel();
    canvas.target(panel).inX(10).inY(10).move();
    canvas.target(anchor).rightSide().inX(-2).bottomSide().outY(-2).move().click();
    assert_CREATE_singlePanel(
        "      layout.putConstraint(SpringLayout.SOUTH, box, -5, SpringLayout.NORTH, anchor);",
        "      layout.putConstraint(SpringLayout.EAST, box, 0, SpringLayout.EAST, anchor);");
  }

  private void prepare_CREATE_singlePanel() throws Exception {
    prepareBox();
    // open editor
    parse_forCREATE(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    SpringLayout layout = new SpringLayout();",
        "    setLayout(layout);",
        "    //",
        "    Box anchor = new Box();",
        "    layout.putConstraint(SpringLayout.NORTH, anchor, 120, SpringLayout.NORTH, this);",
        "    layout.putConstraint(SpringLayout.WEST, anchor, 170, SpringLayout.WEST, this);",
        "    add(anchor);",
        "  }",
        "}");
    anchor = panel.getChildrenComponents().get(0);
    // begin CREATE
    loadCreationBox();
  }

  private void assert_CREATE_singlePanel(String... constraints) throws Exception {
    String[] lines =
        CodeUtils.join(new String[]{
            "public class Test extends JPanel {",
            "  public Test() {",
            "    SpringLayout layout = new SpringLayout();",
            "    setLayout(layout);",
            "    //",
            "    Box anchor = new Box();",
            "    layout.putConstraint(SpringLayout.NORTH, anchor, 120, SpringLayout.NORTH, this);",
            "    layout.putConstraint(SpringLayout.WEST, anchor, 170, SpringLayout.WEST, this);",
            "    add(anchor);",
            "    {",
            "      Box box = new Box();"}, constraints);
    lines = CodeUtils.join(lines, new String[]{"      add(box);", "    }", "  }", "}"});
    assertEditor(lines);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE, with border
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE_withBorder() throws Exception {
    {
      prepareBox();
      parse_forCREATE(
          "public class Test extends JPanel {",
          "  public Test() {",
          "    setBorder(new EmptyBorder(20, 30, 5, 5));",
          "    SpringLayout layout = new SpringLayout();",
          "    setLayout(layout);",
          "  }",
          "}");
      loadCreationBox();
      canvas.create(100, 50).sideMode();
    }
    canvas.target(panel).inX(80).inY(80).move().click();
    int expectedNorth =
        Expectations.get(60, new IntValue[]{
            new IntValue("sablin-aa", 60),
            new IntValue("flanker-windows", 60)});
    int expectedWest =
        Expectations.get(50, new IntValue[]{
            new IntValue("sablin-aa", 50),
            new IntValue("flanker-windows", 50)});
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setBorder(new EmptyBorder(20, 30, 5, 5));",
        "    SpringLayout layout = new SpringLayout();",
        "    setLayout(layout);",
        "    {",
        "      Box box = new Box();",
        "      layout.putConstraint(SpringLayout.NORTH, box, "
            + expectedNorth
            + ", SpringLayout.NORTH, this);",
        "      layout.putConstraint(SpringLayout.WEST, box, "
            + expectedWest
            + ", SpringLayout.WEST, this);",
        "      add(box);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_MOVE_topLeft() throws Exception {
    prepare_MOVE(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    SpringLayout layout = new SpringLayout();",
        "    setLayout(layout);",
        "    {",
        "      Box box = new Box();",
        "      layout.putConstraint(SpringLayout.NORTH, box, 20, SpringLayout.NORTH, this);",
        "      layout.putConstraint(SpringLayout.WEST, box, 10, SpringLayout.WEST, this);",
        "      add(box);",
        "    }",
        "  }",
        "}");
    ComponentInfo source = panel.getChildrenComponents().get(0);
    canvas.beginMove(source);
    canvas.target(panel).inX(20).inY(40).drag();
    canvas.endDrag();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    SpringLayout layout = new SpringLayout();",
        "    setLayout(layout);",
        "    {",
        "      Box box = new Box();",
        "      layout.putConstraint(SpringLayout.NORTH, box, 40, SpringLayout.NORTH, this);",
        "      layout.putConstraint(SpringLayout.WEST, box, 20, SpringLayout.WEST, this);",
        "      add(box);",
        "    }",
        "  }",
        "}");
  }

  public void test_MOVE_topRight() throws Exception {
    prepare_MOVE(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    SpringLayout layout = new SpringLayout();",
        "    setLayout(layout);",
        "    {",
        "      Box box = new Box();",
        "      layout.putConstraint(SpringLayout.NORTH, box, 20, SpringLayout.NORTH, this);",
        "      layout.putConstraint(SpringLayout.WEST, box, 10, SpringLayout.WEST, this);",
        "      add(box);",
        "    }",
        "  }",
        "}");
    ComponentInfo source = panel.getChildrenComponents().get(0);
    canvas.beginMove(source);
    canvas.target(panel).rightSide().inX(-30).inY(40).drag();
    canvas.endDrag();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    SpringLayout layout = new SpringLayout();",
        "    setLayout(layout);",
        "    {",
        "      Box box = new Box();",
        "      layout.putConstraint(SpringLayout.NORTH, box, 40, SpringLayout.NORTH, this);",
        "      layout.putConstraint(SpringLayout.EAST, box, -30, SpringLayout.EAST, this);",
        "      add(box);",
        "    }",
        "  }",
        "}");
  }

  public void test_MOVE_bottomLeft() throws Exception {
    prepare_MOVE(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    SpringLayout layout = new SpringLayout();",
        "    setLayout(layout);",
        "    {",
        "      Box box = new Box();",
        "      layout.putConstraint(SpringLayout.NORTH, box, 20, SpringLayout.NORTH, this);",
        "      layout.putConstraint(SpringLayout.WEST, box, 10, SpringLayout.WEST, this);",
        "      add(box);",
        "    }",
        "  }",
        "}");
    ComponentInfo source = panel.getChildrenComponents().get(0);
    canvas.beginMove(source);
    canvas.target(panel).inX(20).bottomSide().inY(-40).drag();
    canvas.endDrag();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    SpringLayout layout = new SpringLayout();",
        "    setLayout(layout);",
        "    {",
        "      Box box = new Box();",
        "      layout.putConstraint(SpringLayout.WEST, box, 20, SpringLayout.WEST, this);",
        "      layout.putConstraint(SpringLayout.SOUTH, box, -40, SpringLayout.SOUTH, this);",
        "      add(box);",
        "    }",
        "  }",
        "}");
  }

  private void prepare_MOVE(String... lines) throws Exception {
    prepareBox();
    panel = openContainer(lines);
    canvas.sideMode();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE and cycles
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_MOVE_andCycles_A() throws Exception {
    prepare_MOVE_andCycles();
    canvas.beginMove(boxA);
    canvas.target(boxC).outX(30).inY(30).drag();
    canvas.target(boxC).outX(3).inY(3).drag();
    canvas.endDrag();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    SpringLayout layout = new SpringLayout();",
        "    setLayout(layout);",
        "    ",
        "    Box boxA = new Box();",
        "    add(boxA);",
        "    ",
        "    Box boxB = new Box();",
        "    layout.putConstraint(SpringLayout.WEST, boxB, 75, SpringLayout.WEST, this);",
        "    layout.putConstraint(SpringLayout.NORTH, boxA, 0, SpringLayout.NORTH, boxB);",
        "    add(boxB);",
        "    ",
        "    Box boxC = new Box();",
        "    layout.putConstraint(SpringLayout.WEST, boxA, 5, SpringLayout.EAST, boxC);",
        "    layout.putConstraint(SpringLayout.WEST, boxC, 5, SpringLayout.EAST, boxB);",
        "    add(boxC);",
        "  }",
        "}");
  }

  public void test_MOVE_andCycles_B() throws Exception {
    prepare_MOVE_andCycles();
    canvas.beginMove(boxB);
    canvas.rightSide().bottomSide().target(panel).inX(-5).inY(-5).drag();
    canvas.endDrag();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    SpringLayout layout = new SpringLayout();",
        "    setLayout(layout);",
        "    ",
        "    Box boxA = new Box();",
        "    layout.putConstraint(SpringLayout.WEST, boxA, 20, SpringLayout.WEST, this);",
        "    add(boxA);",
        "    ",
        "    Box boxB = new Box();",
        "    layout.putConstraint(SpringLayout.SOUTH, boxB, -10, SpringLayout.SOUTH, this);",
        "    layout.putConstraint(SpringLayout.EAST, boxB, -10, SpringLayout.EAST, this);",
        "    add(boxB);",
        "    ",
        "    Box boxC = new Box();",
        "    layout.putConstraint(SpringLayout.WEST, boxC, 60, SpringLayout.EAST, boxA);",
        "    add(boxC);",
        "  }",
        "}");
  }

  private void prepare_MOVE_andCycles() throws Exception {
    prepareBox(50, 20);
    panel =
        openContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    SpringLayout layout = new SpringLayout();",
            "    setLayout(layout);",
            "    ",
            "    Box boxA = new Box();",
            "    layout.putConstraint(SpringLayout.WEST, boxA, 20, SpringLayout.WEST, this);",
            "    add(boxA);",
            "    ",
            "    Box boxB = new Box();",
            "    layout.putConstraint(SpringLayout.WEST, boxB, 5, SpringLayout.EAST, boxA);",
            "    add(boxB);",
            "    ",
            "    Box boxC = new Box();",
            "    layout.putConstraint(SpringLayout.WEST, boxC, 5, SpringLayout.EAST, boxB);",
            "    add(boxC);",
            "  }",
            "}");
    {
      List<ComponentInfo> components = panel.getChildrenComponents();
      boxA = components.get(0);
      boxB = components.get(1);
      boxC = components.get(2);
    }
    canvas.sideMode();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Resize, single component, attached top-left
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_RESIZE_singleTL_absoluteLeft() throws Exception {
    prepare_RESIZE_singleTL();
    canvas.beginResize(box, IPositionConstants.WEST).dragOn(-50, 0).endDrag();
    assert_RESIZE_singleTL(
        "      layout.putConstraint(SpringLayout.NORTH, box, 100, SpringLayout.NORTH, this);",
        "      layout.putConstraint(SpringLayout.WEST, box, 50, SpringLayout.WEST, this);",
        "      layout.putConstraint(SpringLayout.EAST, box, 200, SpringLayout.WEST, this);");
  }

  public void test_RESIZE_singleTL_snapLeftOffset() throws Exception {
    prepare_RESIZE_singleTL();
    canvas.beginResize(box, IPositionConstants.WEST);
    canvas.target(panel).inX(12).drag().endDrag();
    assert_RESIZE_singleTL(
        "      layout.putConstraint(SpringLayout.NORTH, box, 100, SpringLayout.NORTH, this);",
        "      layout.putConstraint(SpringLayout.WEST, box, 10, SpringLayout.WEST, this);",
        "      layout.putConstraint(SpringLayout.EAST, box, 200, SpringLayout.WEST, this);");
  }

  public void test_RESIZE_singleTL_absoluteRight() throws Exception {
    prepare_RESIZE_singleTL();
    canvas.beginResize(box, IPositionConstants.EAST).dragOn(50, 0).endDrag();
    assert_RESIZE_singleTL(
        "      layout.putConstraint(SpringLayout.NORTH, box, 100, SpringLayout.NORTH, this);",
        "      layout.putConstraint(SpringLayout.WEST, box, 100, SpringLayout.WEST, this);",
        "      layout.putConstraint(SpringLayout.EAST, box, 250, SpringLayout.WEST, this);");
  }

  public void test_RESIZE_singleTL_snapRightOffset() throws Exception {
    prepare_RESIZE_singleTL();
    canvas.beginResize(box, IPositionConstants.EAST);
    canvas.target(panel).rightSide().inX(-12).drag().endDrag();
    assert_RESIZE_singleTL(
        "      layout.putConstraint(SpringLayout.NORTH, box, 100, SpringLayout.NORTH, this);",
        "      layout.putConstraint(SpringLayout.WEST, box, 100, SpringLayout.WEST, this);",
        "      layout.putConstraint(SpringLayout.EAST, box, -10, SpringLayout.EAST, this);");
  }

  public void test_RESIZE_singleTL_absoluteTop() throws Exception {
    prepare_RESIZE_singleTL();
    canvas.beginResize(box, IPositionConstants.NORTH).dragOn(0, -50).endDrag();
    assert_RESIZE_singleTL(
        "      layout.putConstraint(SpringLayout.NORTH, box, 50, SpringLayout.NORTH, this);",
        "      layout.putConstraint(SpringLayout.WEST, box, 100, SpringLayout.WEST, this);",
        "      layout.putConstraint(SpringLayout.SOUTH, box, 150, SpringLayout.NORTH, this);");
  }

  public void test_RESIZE_singleTL_snapTopOffset() throws Exception {
    prepare_RESIZE_singleTL();
    canvas.beginResize(box, IPositionConstants.NORTH);
    canvas.target(panel).inY(12).drag().endDrag();
    assert_RESIZE_singleTL(
        "      layout.putConstraint(SpringLayout.NORTH, box, 10, SpringLayout.NORTH, this);",
        "      layout.putConstraint(SpringLayout.WEST, box, 100, SpringLayout.WEST, this);",
        "      layout.putConstraint(SpringLayout.SOUTH, box, 150, SpringLayout.NORTH, this);");
  }

  public void test_RESIZE_singleTL_absoluteBottom() throws Exception {
    prepare_RESIZE_singleTL();
    canvas.beginResize(box, IPositionConstants.SOUTH).dragOn(0, 50).endDrag();
    assert_RESIZE_singleTL(
        "      layout.putConstraint(SpringLayout.NORTH, box, 100, SpringLayout.NORTH, this);",
        "      layout.putConstraint(SpringLayout.WEST, box, 100, SpringLayout.WEST, this);",
        "      layout.putConstraint(SpringLayout.SOUTH, box, 200, SpringLayout.NORTH, this);");
  }

  public void test_RESIZE_singleTL_snapBottomOffset() throws Exception {
    prepare_RESIZE_singleTL();
    canvas.beginResize(box, IPositionConstants.SOUTH);
    canvas.target(panel).bottomSide().inY(-12).drag().endDrag();
    assert_RESIZE_singleTL(
        "      layout.putConstraint(SpringLayout.NORTH, box, 100, SpringLayout.NORTH, this);",
        "      layout.putConstraint(SpringLayout.WEST, box, 100, SpringLayout.WEST, this);",
        "      layout.putConstraint(SpringLayout.SOUTH, box, -10, SpringLayout.SOUTH, this);");
  }

  private void prepare_RESIZE_singleTL() throws Exception {
    prepareBox();
    panel =
        openContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    SpringLayout layout = new SpringLayout();",
            "    setLayout(layout);",
            "    {",
            "      Box box = new Box();",
            "      layout.putConstraint(SpringLayout.NORTH, box, 100, SpringLayout.NORTH, this);",
            "      layout.putConstraint(SpringLayout.WEST, box, 100, SpringLayout.WEST, this);",
            "      add(box);",
            "    }",
            "  }",
            "}");
    box = panel.getChildrenComponents().get(0);
  }

  private void assert_RESIZE_singleTL(String... constraints) throws Exception {
    String[] lines =
        CodeUtils.join(new String[]{
            "public class Test extends JPanel {",
            "  public Test() {",
            "    SpringLayout layout = new SpringLayout();",
            "    setLayout(layout);",
            "    {",
            "      Box box = new Box();"}, constraints);
    lines = CodeUtils.join(lines, new String[]{"      add(box);", "    }", "  }", "}"});
    assertEditor(lines);
  }
}
