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
package org.eclipse.wb.tests.designer.swing.model.layout.model;

import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.tests.designer.swing.model.layout.gef.AbstractLayoutPolicyTest;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

/**
 * Test for {@link FlowLayoutInfo} in GEF.
 * 
 * @author scheglov_ke
 */
public class FlowLayoutGefTest extends AbstractLayoutPolicyTest {
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
   * Set {@link FlowLayout} instead of explicit {@link BorderLayout}.
   */
  public void test_setLayout() throws Exception {
    String[] source =
        new String[]{
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "    {",
            "      final JButton button = new JButton('New button');",
            "      add(button, BorderLayout.SOUTH);",
            "    }",
            "  }",
            "}"};
    String[] source2 =
        new String[]{
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));",
            "    {",
            "      final JButton button = new JButton('New button');",
            "      add(button);",
            "    }",
            "  }",
            "}"};
    check_setLayout(source, "java.awt.FlowLayout", source2, 10, 10);
  }

  /**
   * Set {@link FlowLayout} instead of implicit {@link BorderLayout}.
   */
  public void test_setLayout2() throws Exception {
    String[] source =
        new String[]{
            "public class Test extends JFrame {",
            "  public Test() {",
            "    {",
            "      final JButton button = new JButton('New button');",
            "      getContentPane().add(button, BorderLayout.SOUTH);",
            "    }",
            "  }",
            "}"};
    String[] source2 =
        new String[]{
            "public class Test extends JFrame {",
            "  public Test() {",
            "    getContentPane().setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));",
            "    {",
            "      final JButton button = new JButton('New button');",
            "      getContentPane().add(button);",
            "    }",
            "  }",
            "}"};
    check_setLayout(source, "java.awt.FlowLayout", source2, 10, 50);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE_onEmpty() throws Exception {
    ContainerInfo panel =
        openContainer(
            "// filler filler filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    //
    loadCreationTool("javax.swing.JButton");
    canvas.moveTo(panel, 10, 10);
    canvas.click();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_beforeExisting() throws Exception {
    ContainerInfo panel =
        openContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JButton button = new JButton('Button');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    //
    loadCreationTool("javax.swing.JButton");
    canvas.moveTo(panel, 10, 10);
    canvas.click();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button);",
        "    }",
        "    {",
        "      JButton button = new JButton('Button');",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_afterExisting() throws Exception {
    ContainerInfo panel =
        openContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JButton button = new JButton('Button');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    //
    loadCreationTool("javax.swing.JButton");
    canvas.moveTo(panel, -10, 10);
    canvas.click();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JButton button = new JButton('Button');",
        "      add(button);",
        "    }",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * We should now be able to use exposed component as reference.
   */
  public void test_CREATE_hasExposed() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  protected final JButton m_button;",
            "  public MyPanel() {",
            "    {",
            "      m_button = new JButton('Button');",
            "      add(m_button);",
            "    }",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        openContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    //
    loadCreationTool("javax.swing.JButton");
    canvas.moveTo(panel, 10, 10);
    canvas.click();
    assertEditor(
        "// filler filler filler",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE RTL
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE_RTL_onEmpty() throws Exception {
    ContainerInfo panel =
        openContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);",
            "  }",
            "}");
    //
    loadCreationTool("javax.swing.JButton");
    canvas.moveTo(panel, 10, 10);
    canvas.click();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_RTL_last() throws Exception {
    openContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);",
        "    {",
        "      JButton existing = new JButton('Button');",
        "      add(existing);",
        "    }",
        "  }",
        "}");
    ComponentInfo existing = getJavaInfoByName("existing");
    //
    loadCreationTool("javax.swing.JButton");
    canvas.target(existing).outX(-5).inY(5).move();
    canvas.click();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);",
        "    {",
        "      JButton existing = new JButton('Button');",
        "      add(existing);",
        "    }",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_RTL_beforeExisting() throws Exception {
    openContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);",
        "    {",
        "      JButton existing = new JButton('Button');",
        "      add(existing);",
        "    }",
        "  }",
        "}");
    ComponentInfo existing = getJavaInfoByName("existing");
    //
    loadCreationTool("javax.swing.JButton");
    canvas.target(existing).in(-5, 5).move();
    canvas.click();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button);",
        "    }",
        "    {",
        "      JButton existing = new JButton('Button');",
        "      add(existing);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_RTL_betweenExisting() throws Exception {
    openContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);",
        "    {",
        "      JButton buttonA = new JButton('ButtonA');",
        "      add(buttonA);",
        "    }",
        "    {",
        "      JButton buttonB = new JButton('ButtonB');",
        "      add(buttonB);",
        "    }",
        "  }",
        "}");
    ComponentInfo button = getJavaInfoByName("buttonA");
    //
    loadCreationTool("javax.swing.JButton");
    canvas.target(button).outX(-5).inY(5).move();
    canvas.click();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);",
        "    {",
        "      JButton buttonA = new JButton('ButtonA');",
        "      add(buttonA);",
        "    }",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button);",
        "    }",
        "    {",
        "      JButton buttonB = new JButton('ButtonB');",
        "      add(buttonB);",
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
   * Flow container supports moving only single component.
   */
  public void test_MOVE_twoComponents() throws Exception {
    String[] lines =
        new String[]{
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JButton buttonA = new JButton('Button A');",
            "      add(buttonA);",
            "    }",
            "    {",
            "      JButton buttonB = new JButton('Button B');",
            "      add(buttonB);",
            "    }",
            "  }",
            "}"};
    openContainer(lines);
    ComponentInfo buttonA = getJavaInfoByName("buttonA");
    ComponentInfo buttonB = getJavaInfoByName("buttonB");
    // select two buttons
    canvas.select(buttonA, buttonB);
    assertEquals(2, m_viewerCanvas.getSelectedEditParts().size());
    // drag
    {
      canvas.beginDrag(buttonB, 10, 5);
      canvas.dragTo(buttonA, -10, 0);
      //canvas.dragTo(panel, 10, 10);
      canvas.endDrag();
    }
    // no changes expected
    assertEditor(lines);
  }

  public void test_MOVE_localVariable() throws Exception {
    String[] source =
        new String[]{
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JButton button = new JButton('Button 1');",
            "      add(button);",
            "    }",
            "    {",
            "      JButton button = new JButton('Button 2');",
            "      add(button);",
            "    }",
            "  }",
            "}"};
    String[] source2 =
        new String[]{
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JButton button = new JButton('Button 2');",
            "      add(button);",
            "    }",
            "    {",
            "      JButton button = new JButton('Button 1');",
            "      add(button);",
            "    }",
            "  }",
            "}"};
    check_MOVE(source, source2);
  }

  public void test_MOVE_lazy() throws Exception {
    String[] source =
        new String[]{
            "public class Test extends JPanel {",
            "  private JButton button_1;",
            "  private JButton button_2;",
            "  public Test() {",
            "    add(getButton_1());",
            "    add(getButton_2());",
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
            "  private JButton button_1;",
            "  private JButton button_2;",
            "  public Test() {",
            "    add(getButton_2());",
            "    add(getButton_1());",
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
    check_MOVE(source, source2);
  }

  private void check_MOVE(String[] source, String[] source2) throws Exception {
    ContainerInfo panel = openContainer(source);
    ComponentInfo button_1 = panel.getChildrenComponents().get(0);
    ComponentInfo button_2 = panel.getChildrenComponents().get(1);
    // ignored move: last to last
    {
      canvas.beginDrag(button_2);
      canvas.dragTo(button_2, -10, 0);
      canvas.endDrag();
      canvas.assertNoFeedbackFigures();
      assertEditor(source);
    }
    // ignored move: before already next
    {
      canvas.beginDrag(button_1);
      canvas.dragTo(button_2, 10, 0);
      canvas.endDrag();
      canvas.assertNoFeedbackFigures();
      assertEditor(source);
    }
    // real move
    {
      canvas.beginDrag(button_2);
      canvas.dragTo(button_1, 10, 0);
      canvas.endDrag();
      canvas.assertNoFeedbackFigures();
      assertEditor(source2);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ADD
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_ADD() throws Exception {
    openContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new BorderLayout());",
        "    {",
        "      JButton button = new JButton('Button');",
        "      add(button, BorderLayout.NORTH);",
        "    }",
        "    {",
        "      JPanel panel = new JPanel();",
        "      panel.setBackground(Color.PINK);",
        "      panel.setPreferredSize(new Dimension(0, 150));",
        "      add(panel, BorderLayout.SOUTH);",
        "    }",
        "  }",
        "}");
    ComponentInfo button = getJavaInfoByName("button");
    ComponentInfo inner = getJavaInfoByName("panel");
    //
    canvas.beginDrag(button);
    canvas.dragTo(inner, 10, 10);
    canvas.endDrag();
    canvas.assertFeedbackFigures(0);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new BorderLayout());",
        "    {",
        "      JPanel panel = new JPanel();",
        "      panel.setBackground(Color.PINK);",
        "      panel.setPreferredSize(new Dimension(0, 150));",
        "      add(panel, BorderLayout.SOUTH);",
        "      {",
        "        JButton button = new JButton('Button');",
        "        panel.add(button);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  public void test_ADD_twoComponents() throws Exception {
    String[] lines =
        {
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "    {",
            "      JButton buttonA = new JButton('ButtonA');",
            "      add(buttonA, BorderLayout.NORTH);",
            "    }",
            "    {",
            "      JButton buttonB = new JButton('ButtonB');",
            "      add(buttonB, BorderLayout.SOUTH);",
            "    }",
            "    {",
            "      JPanel panel = new JPanel();",
            "      panel.setBackground(Color.PINK);",
            "      panel.setPreferredSize(new Dimension(0, 150));",
            "      add(panel, BorderLayout.CENTER);",
            "    }",
            "  }",
            "}"};
    openContainer(lines);
    ComponentInfo buttonA = getJavaInfoByName("buttonA");
    ComponentInfo buttonB = getJavaInfoByName("buttonB");
    ComponentInfo inner = getJavaInfoByName("panel");
    // select two buttons
    canvas.select(buttonA, buttonB);
    assertEquals(2, m_viewerCanvas.getSelectedEditParts().size());
    // drag
    canvas.beginDrag(buttonB, 10, 5);
    canvas.dragTo(inner, 10, 10);
    canvas.endDrag();
    canvas.assertFeedbackFigures(0);
    // no changes
    assertEditor(lines);
  }
}
