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
package org.eclipse.wb.tests.designer.core.model.generic;

import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;

/**
 * Tests for <code>double-click.flipBooleanProperty</code> support.
 *
 * @author scheglov_ke
 */
public class FlipBooleanPropertyGefTest extends SwingGefTest {
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
  public void test_doFlip() throws Exception {
    prepareMyPanel();
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='double-click.flipBooleanProperty'>myExpanded</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // open editor
    ContainerInfo panel = openMyPanel();
    // flip: false -> true
    canvas.doubleClick(panel);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new BorderLayout());",
        "    {",
        "      MyPanel panel = new MyPanel();",
        "      panel.setMyExpanded(true);",
        "      add(panel);",
        "    }",
        "  }",
        "}");
    // flip: true -> false
    canvas.doubleClick(panel);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new BorderLayout());",
        "    {",
        "      MyPanel panel = new MyPanel();",
        "      add(panel);",
        "    }",
        "  }",
        "}");
  }

  /**
   * If no specified property, then ignore.
   */
  public void test_noSuchProperty() throws Exception {
    prepareMyPanel();
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='double-click.flipBooleanProperty'>noSuchProperty</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    ContainerInfo panel = openMyPanel();
    // do double click, but property to flip does not exist, so ignore
    canvas.doubleClick(panel);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new BorderLayout());",
        "    {",
        "      MyPanel panel = new MyPanel();",
        "      add(panel);",
        "    }",
        "  }",
        "}");
  }

  /**
   * If specified property is not boolean, then ignore.
   */
  public void test_notBooleanProperty() throws Exception {
    prepareMyPanel();
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='double-click.flipBooleanProperty'>background</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    ContainerInfo panel = openMyPanel();
    // do double click, but property to flip is not boolean, so ignore
    canvas.doubleClick(panel);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new BorderLayout());",
        "    {",
        "      MyPanel panel = new MyPanel();",
        "      add(panel);",
        "    }",
        "  }",
        "}");
  }

  public void test_noFlipParameter() throws Exception {
    prepareMyPanel();
    waitForAutoBuild();
    ContainerInfo panel = openMyPanel();
    // do double click, but no flip parameter, so ignore
    canvas.doubleClick(panel);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new BorderLayout());",
        "    {",
        "      MyPanel panel = new MyPanel();",
        "      add(panel);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private void prepareMyPanel() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends Container {",
            "  private boolean m_expanded;",
            "  public boolean getMyExpanded() {",
            "    return m_expanded;",
            "  }",
            "  public void setMyExpanded(boolean expanded) {",
            "    m_expanded = expanded;",
            "  }",
            "}"));
  }

  private ContainerInfo openMyPanel() throws Exception {
    ContainerInfo mainPanel =
        openContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "    {",
            "      MyPanel panel = new MyPanel();",
            "      add(panel);",
            "    }",
            "  }",
            "}");
    return (ContainerInfo) mainPanel.getChildrenComponents().get(0);
  }
}
