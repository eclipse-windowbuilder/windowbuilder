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
package org.eclipse.wb.tests.designer.swing.model.layout.gef;

import org.eclipse.wb.internal.core.utils.StringUtilities;
import org.eclipse.wb.internal.swing.gef.policy.layout.BorderLayoutEditPolicy;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

/**
 * Test for {@link BorderLayoutEditPolicy}.
 * 
 * @author scheglov_ke
 */
public class BorderLayoutPolicyTest extends AbstractLayoutPolicyTest {
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
  // setLayout
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for setting layout.
   */
  public void test_canvas_setLayout() throws Exception {
    String[] source =
        new String[]{
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}"};
    String[] source2 =
        new String[]{
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new BorderLayout(0, 0));",
            "  }",
            "}"};
    check_setLayout(source, "java.awt.BorderLayout", source2, 10, 10);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for dropping new component.
   */
  public void test_canvas_CREATE() throws Exception {
    openContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new BorderLayout());",
        "  }",
        "}");
    //
    loadCreationTool("javax.swing.JButton", "empty");
    canvas.moveTo(m_contentEditPart, 10, 10);
    canvas.assertFeedbackFigures(6);
    waitEventLoop(10);
    //
    canvas.click();
    canvas.assertNoFeedbackFigures();
    waitEventLoop(10);
    //
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new BorderLayout());",
        "    {",
        "      JButton button = new JButton();",
        "      add(button, BorderLayout.NORTH);",
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
   * Test for moving component with "normal" variable.
   */
  public void test_canvas_MOVE_1() throws Exception {
    check_MOVE(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new BorderLayout());",
        "    {",
        "      JButton button = new JButton('Button');",
        "      add(button, BorderLayout.NORTH);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for moving with "lazy" variable.
   */
  public void test_canvas_MOVE_2() throws Exception {
    check_MOVE(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new BorderLayout());",
        "    add(getButton(), BorderLayout.NORTH);",
        "  }",
        "  private JButton button;",
        "  private JButton getButton() {",
        "    if (button == null) {",
        "      button = new JButton();",
        "    }",
        "    return button;",
        "  }",
        "}");
  }

  /**
   * Test: when no constraints (i.e. implicit CENTER), we should add argument, not just replace it.
   */
  public void test_canvas_MOVE_3() throws Exception {
    String[] source =
        new String[]{
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "    {",
            "      final JButton button = new JButton();",
            "      add(button);",
            "    }",
            "  }",
            "}"};
    String[] expectedSource =
        StringUtilities.replace(source, "add(button);", "add(button, BorderLayout.SOUTH);");
    check_MOVE(source, expectedSource);
  }

  /**
   * Test for moving component from NORTH to SOUTH.
   */
  private void check_MOVE(String... lines) throws Exception {
    String[] expectedSource = StringUtilities.replace(lines, "NORTH", "SOUTH");
    check_MOVE(lines, expectedSource);
  }

  /**
   * Test for moving component at (10,10) - usually NORTH, may be CENTER/WEST; to SOUTH.
   */
  private void check_MOVE(String[] source, String[] expectedSource) throws Exception,
      InterruptedException {
    ContainerInfo panel = openContainer(source);
    ComponentInfo button = panel.getChildrenComponents().get(0);
    //
    canvas.beginDrag(button);
    canvas.assertNoFeedbackFigures();
    //
    canvas.dragTo(panel, 10, -10);
    canvas.assertFeedbackFigures(5);
    //
    canvas.endDrag();
    canvas.assertNoFeedbackFigures();
    //
    assertEditor(expectedSource);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ADD
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for ADD (reparenting) for "normal" variable.
   */
  public void test_canvas_ADD_1() throws Exception {
    String[] source =
        new String[]{
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "    {",
            "      JPanel panel = new JPanel();",
            "      panel.setBackground(Color.PINK);",
            "      panel.setPreferredSize(new Dimension(0, 150));",
            "      add(panel, BorderLayout.SOUTH);",
            "      {",
            "        JButton button_1 = new JButton('Button 1');",
            "        panel.add(button_1);",
            "      }",
            "      {",
            "        JButton button_2 = new JButton('Button 2');",
            "        panel.add(button_2);",
            "      }",
            "    }",
            "  }",
            "}"};
    String[] source2 =
        new String[]{
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "    {",
            "      JPanel panel = new JPanel();",
            "      panel.setBackground(Color.PINK);",
            "      panel.setPreferredSize(new Dimension(0, 150));",
            "      add(panel, BorderLayout.SOUTH);",
            "      {",
            "        JButton button_2 = new JButton('Button 2');",
            "        panel.add(button_2);",
            "      }",
            "    }",
            "    {",
            "      JButton button_1 = new JButton('Button 1');",
            "      add(button_1, BorderLayout.NORTH);",
            "    }",
            "  }",
            "}"};
    check_ADD(source, source2);
  }

  /**
   * Test for ADD (reparenting) for "lazy" variable.
   */
  public void test_canvas_ADD_2() throws Exception {
    String[] source =
        new String[]{
            "public class Test extends JPanel {",
            "  private JPanel panel;",
            "  private JButton button_1;",
            "  private JButton button_2;",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "    add(getPanel(), BorderLayout.SOUTH);",
            "  }",
            "  private JPanel getPanel() {",
            "    if (panel == null) {",
            "      panel = new JPanel();",
            "      panel.setBackground(Color.PINK);",
            "      panel.setPreferredSize(new Dimension(0, 150));",
            "      panel.add(getButton_1());",
            "      panel.add(getButton_2());",
            "    }",
            "    return panel;",
            "  }",
            "  private JButton getButton_1() {",
            "    if (button_1 == null) {",
            "      button_1 = new JButton('Button 1');",
            "    }",
            "    return button_1;",
            "  }",
            "  private JButton getButton_2() {",
            "    if (button_2 == null) {",
            "      button_2 = new JButton('button 2');",
            "    }",
            "    return button_2;",
            "  }",
            "}"};
    String[] source2 =
        new String[]{
            "public class Test extends JPanel {",
            "  private JPanel panel;",
            "  private JButton button_1;",
            "  private JButton button_2;",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "    add(getPanel(), BorderLayout.SOUTH);",
            "    add(getButton_1(), BorderLayout.NORTH);",
            "  }",
            "  private JPanel getPanel() {",
            "    if (panel == null) {",
            "      panel = new JPanel();",
            "      panel.setBackground(Color.PINK);",
            "      panel.setPreferredSize(new Dimension(0, 150));",
            "      panel.add(getButton_2());",
            "    }",
            "    return panel;",
            "  }",
            "  private JButton getButton_1() {",
            "    if (button_1 == null) {",
            "      button_1 = new JButton('Button 1');",
            "    }",
            "    return button_1;",
            "  }",
            "  private JButton getButton_2() {",
            "    if (button_2 == null) {",
            "      button_2 = new JButton('button 2');",
            "    }",
            "    return button_2;",
            "  }",
            "}"};
    check_ADD(source, source2);
  }

  /**
   * Test for ADD (reparenting) of "first component of first child" to root container.
   */
  private void check_ADD(String[] source, String[] source2) throws Exception {
    ContainerInfo panel = openContainer(source);
    ContainerInfo inner = (ContainerInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button = inner.getChildrenComponents().get(0);
    //
    canvas.beginDrag(button);
    canvas.assertNoFeedbackFigures();
    //
    canvas.dragTo(panel, 10, 10);
    canvas.assertFeedbackFigures(5);
    waitEventLoop(10);
    //
    canvas.endDrag();
    canvas.assertNoFeedbackFigures();
    waitEventLoop(10);
    //
    assertEditor(source2);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tree
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_tree_CREATE_hasEmptyRegion() throws Exception {
    ContainerInfo panel =
        openContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "  }",
            "}");
    //
    loadCreationTool("javax.swing.JButton", "empty");
    tree.moveOn(panel);
    tree.assertFeedback_on(panel);
    tree.assertCommandNotNull();
    tree.click();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new BorderLayout());",
        "    {",
        "      JButton button = new JButton();",
        "      add(button, BorderLayout.NORTH);",
        "    }",
        "  }",
        "}");
  }

  public void test_tree_CREATE_noEmptyRegion() throws Exception {
    ContainerInfo panel =
        openContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "    add(new JButton(), BorderLayout.NORTH);",
            "    add(new JButton(), BorderLayout.SOUTH);",
            "    add(new JButton(), BorderLayout.WEST);",
            "    add(new JButton(), BorderLayout.EAST);",
            "    add(new JButton(), BorderLayout.CENTER);",
            "  }",
            "}");
    //
    loadCreationTool("javax.swing.JButton", "empty");
    tree.moveOn(panel);
    tree.assertFeedback_on(panel);
    tree.assertCommandNull();
  }

  public void test_tree_MOVE() throws Exception {
    ContainerInfo panel =
        openContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "    {",
            "      JButton button_1 = new JButton();",
            "      add(button_1, BorderLayout.NORTH);",
            "    }",
            "    {",
            "      JButton button_2 = new JButton();",
            "      add(button_2, BorderLayout.SOUTH);",
            "    }",
            "  }",
            "}");
    ComponentInfo button_1 = panel.getChildrenComponents().get(0);
    ComponentInfo button_2 = panel.getChildrenComponents().get(1);
    //
    tree.startDrag(button_2).dragBefore(button_1);
    tree.assertCommandNotNull();
    tree.endDrag();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new BorderLayout());",
        "    {",
        "      JButton button_2 = new JButton();",
        "      add(button_2, BorderLayout.SOUTH);",
        "    }",
        "    {",
        "      JButton button_1 = new JButton();",
        "      add(button_1, BorderLayout.NORTH);",
        "    }",
        "  }",
        "}");
  }

  public void test_tree_ADD() throws Exception {
    ContainerInfo panel =
        openContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "    {",
            "      JPanel inner = new JPanel();",
            "      add(inner, BorderLayout.CENTER);",
            "      {",
            "        JButton button = new JButton();",
            "        inner.add(button);",
            "      }",
            "    }",
            "  }",
            "}");
    ContainerInfo inner = (ContainerInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button = inner.getChildrenComponents().get(0);
    //
    tree.startDrag(button).dragBefore(inner);
    tree.assertCommandNotNull();
    tree.endDrag();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new BorderLayout());",
        "    {",
        "      JButton button = new JButton();",
        "      add(button, BorderLayout.NORTH);",
        "    }",
        "    {",
        "      JPanel inner = new JPanel();",
        "      add(inner, BorderLayout.CENTER);",
        "    }",
        "  }",
        "}");
  }
}
