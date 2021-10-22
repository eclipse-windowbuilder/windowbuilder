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
package org.eclipse.wb.tests.designer.swing.model.layout;

import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.LayoutInfo;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;
import org.eclipse.wb.tests.designer.swing.model.component.ContainerTest;

import java.awt.LayoutManager;

import javax.swing.JPanel;

/**
 * Tests {@link LayoutInfo} and GEF.
 *
 * @author scheglov_ke
 */
public class LayoutGefTest extends SwingGefTest {
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
  // Drop layout
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_dropLayout_canvas() throws Exception {
    ContainerInfo panel =
        openContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(null);",
            "  }",
            "}");
    // create BorderLayout
    LayoutInfo newLayout = loadCreationTool("java.awt.BorderLayout");
    // use canvas
    canvas.create();
    canvas.target(panel).in(100, 100).move();
    canvas.assertFeedbacks(canvas.getTargetPredicate(panel));
    canvas.assertCommandNotNull();
    canvas.click();
    // assert
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new BorderLayout(0, 0));",
        "  }",
        "}");
    assertSame(newLayout, panel.getLayout());
  }

  public void test_dropLayout_tree() throws Exception {
    ContainerInfo panel =
        openContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(null);",
            "  }",
            "}");
    // create BorderLayout
    LayoutInfo newLayout = loadCreationTool("java.awt.BorderLayout");
    // use canvas
    tree.moveOn(panel);
    tree.assertFeedback_on(panel);
    tree.assertCommandNotNull();
    tree.click();
    // assert
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new BorderLayout(0, 0));",
        "  }",
        "}");
    assertSame(newLayout, panel.getLayout());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Can not drop Layout
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_dropLayout_disabledSetLayout_canvas() throws Exception {
    ContainerTest.prepareMyPanel_disabledSetLayout();
    ContainerInfo panel =
        openContainer(
            "// filler filler filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    // load BorderLayout
    loadCreationTool("java.awt.BorderLayout");
    // use canvas
    canvas.create();
    canvas.target(panel).in(100, 100).move();
    canvas.assertCommandNull();
  }

  public void test_dropLayout_disabledSetLayout_tree() throws Exception {
    ContainerTest.prepareMyPanel_disabledSetLayout();
    ContainerInfo panel =
        openContainer(
            "// filler filler filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    // load BorderLayout
    loadCreationTool("java.awt.BorderLayout");
    // use canvas
    tree.moveOn(panel);
    tree.assertCommandNull();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When {@link JPanel} has initially one of the layouts supported in tree, and we drop unknown
   * {@link LayoutManager} (no support in tree), we should not be able to drop any components on
   * this {@link JPanel}. This means that we should remove old {@link LayoutEditPolicy}.
   */
  public void test_dropUnknownLayout_noTreeLayout() throws Exception {
    setFileContentSrc(
        "test/MyLayout.java",
        getTestSource(
            "public class MyLayout implements LayoutManager {",
            "  public void addLayoutComponent(String name, Component comp) {",
            "  }",
            "  public  void removeLayoutComponent(Component comp) {",
            "  }",
            "  public Dimension preferredLayoutSize(Container parent) {",
            "    return new Dimension(200, 100);",
            "  }",
            "  public Dimension minimumLayoutSize(Container parent) {",
            "    return new Dimension(200, 100);",
            "  }",
            "  public void layoutContainer(Container parent) {",
            "  }",
            "}"));
    waitForAutoBuild();
    // open editor
    ContainerInfo panel =
        openContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(null);",
            "  }",
            "}");
    // FlowLayout has command
    {
      loadCreationTool("javax.swing.JButton");
      tree.moveOn(panel);
      tree.assertFeedback_on(panel);
      tree.assertCommandNotNull();
      tree.cancel();
    }
    // drop MyLayout
    {
      loadCreationTool("test.MyLayout");
      tree.moveOn(panel);
      tree.click();
    }
    // MyLayout is not supported in tree, so no command
    {
      loadCreationTool("javax.swing.JButton");
      tree.moveOn(panel);
      tree.assertFeedback_on(panel);
      tree.assertCommandNull();
    }
  }
}
