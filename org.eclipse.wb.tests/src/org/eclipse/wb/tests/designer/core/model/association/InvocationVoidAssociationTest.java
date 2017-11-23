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
package org.eclipse.wb.tests.designer.core.model.association;

import org.eclipse.wb.core.model.association.InvocationVoidAssociation;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.component.JToolBarInfo;
import org.eclipse.wb.internal.swing.model.component.JToolBarSeparatorCreationSupport;
import org.eclipse.wb.internal.swing.model.component.JToolBarSeparatorInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import static org.assertj.core.api.Assertions.assertThat;

import javax.swing.JToolBar;

/**
 * Tests for {@link InvocationVoidAssociation}, for example {@link JToolBarSeparatorInfo}.
 * 
 * @author scheglov_ke
 */
public class InvocationVoidAssociationTest extends SwingModelTest {
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
  public void test_parse() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JToolBar bar = new JToolBar();",
            "    add(bar);",
            "    bar.addSeparator();",
            "  }",
            "}");
    JToolBarInfo bar = (JToolBarInfo) panel.getChildrenComponents().get(0);
    JToolBarSeparatorInfo separator = (JToolBarSeparatorInfo) bar.getChildrenComponents().get(0);
    // check association
    InvocationVoidAssociation association = (InvocationVoidAssociation) separator.getAssociation();
    assertSame(separator, association.getJavaInfo());
    assertEquals("bar.addSeparator()", association.getSource());
    assertEquals("bar.addSeparator()", m_lastEditor.getSource(association.getInvocation()));
    assertEquals("bar.addSeparator();", m_lastEditor.getSource(association.getStatement()));
  }

  public void test_delete() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JToolBar bar = new JToolBar();",
            "    add(bar);",
            "    bar.addSeparator();",
            "  }",
            "}");
    JToolBarInfo bar = (JToolBarInfo) panel.getChildrenComponents().get(0);
    JToolBarSeparatorInfo separator = (JToolBarSeparatorInfo) bar.getChildrenComponents().get(0);
    // do delete
    assertTrue(separator.canDelete());
    separator.delete();
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JToolBar bar = new JToolBar();",
        "    add(bar);",
        "  }",
        "}");
  }

  public void test_add() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JToolBar bar = new JToolBar();",
            "    add(bar);",
            "  }",
            "}");
    JToolBarInfo bar = (JToolBarInfo) panel.getChildrenComponents().get(0);
    // create separator
    JToolBarSeparatorCreationSupport creationSupport = new JToolBarSeparatorCreationSupport(bar);
    JToolBarSeparatorInfo separator =
        (JToolBarSeparatorInfo) JavaInfoUtils.createJavaInfo(
            m_lastEditor,
            JToolBar.Separator.class,
            creationSupport);
    // add separator
    bar.command_CREATE(separator, null);
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JToolBar bar = new JToolBar();",
        "    add(bar);",
        "    bar.addSeparator();",
        "  }",
        "}");
    // check VoidInvocationCreationSupport.toString()
    assertEquals("void", creationSupport.toString());
    // check association
    InvocationVoidAssociation association = (InvocationVoidAssociation) separator.getAssociation();
    assertSame(separator, association.getJavaInfo());
    assertEquals("bar.addSeparator()", association.getSource());
  }

  public void test_moveInner() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JToolBar bar = new JToolBar();",
            "    add(bar);",
            "    {",
            "      JButton button = new JButton();",
            "      bar.add(button);",
            "    }",
            "    bar.addSeparator();",
            "  }",
            "}");
    JToolBarInfo bar = (JToolBarInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button = bar.getChildrenComponents().get(0);
    JToolBarSeparatorInfo separator = (JToolBarSeparatorInfo) bar.getChildrenComponents().get(1);
    // move separator
    bar.command_MOVE(separator, button);
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    JToolBar bar = new JToolBar();",
        "    add(bar);",
        "    bar.addSeparator();",
        "    {",
        "      JButton button = new JButton();",
        "      bar.add(button);",
        "    }",
        "  }",
        "}");
    // check association
    InvocationVoidAssociation association = (InvocationVoidAssociation) separator.getAssociation();
    assertSame(separator, association.getJavaInfo());
    assertEquals("bar.addSeparator()", association.getSource());
  }

  /**
   * We need special support for moving <code>getX()</code> lazy accessor method inside of parent
   * block.
   */
  public void test_moveInner_lazy() throws Exception {
    setFileContentSrc(
        "test/MyBar.java",
        getTestSource(
            "public class MyBar extends JPanel {",
            "  public JButton addButton() {",
            "    JButton button = new JButton();",
            "    add(button);",
            "    return button;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyBar.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='addButton'>",
            "      <tag name='implicitFactory' value='true'/>",
            "    </method>",
            "  </methods>",
            "</component>"));
    waitForAutoBuild();
    String[] lines =
        {
            "public class Test extends JPanel {",
            "  private MyBar bar;",
            "  private JButton button_1;",
            "  private JButton button_2;",
            "  public Test() {",
            "    add(getBar());",
            "  }",
            "  private MyBar getBar() {",
            "    if (bar == null) {",
            "      bar = new MyBar();",
            "      getButton_1();",
            "      getButton_2();",
            "    }",
            "    return bar;",
            "  }",
            "  private JButton getButton_1() {",
            "    if (button_1 == null) {",
            "      button_1 = bar.addButton();",
            "    }",
            "    return button_1;",
            "  }",
            "  private JButton getButton_2() {",
            "    if (button_2 == null) {",
            "      button_2 = bar.addButton();",
            "    }",
            "    return button_2;",
            "  }",
            "}"};
    // parse
    ContainerInfo panel = parseContainer(lines);
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(getBar())/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: test.MyBar} {lazy: bar getBar()} {/new MyBar()/ /bar.addButton()/ /bar.addButton()/ /bar/ /add(getBar())/}",
        "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "    {implicit-factory} {lazy: button_1 getButton_1()} {/bar.addButton()/ /button_1/ /getButton_1()/}",
        "    {implicit-factory} {lazy: button_2 getButton_2()} {/bar.addButton()/ /button_2/ /getButton_2()/}");
    ContainerInfo bar = (ContainerInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button_1 = bar.getChildrenComponents().get(0);
    ComponentInfo button_2 = bar.getChildrenComponents().get(1);
    assertThat(button_2.getAssociation()).isInstanceOf(InvocationVoidAssociation.class);
    // move "button_2" before "button_1"
    JavaInfoUtils.move(button_2, null, bar, button_1);
    assertEditor(
        "public class Test extends JPanel {",
        "  private MyBar bar;",
        "  private JButton button_1;",
        "  private JButton button_2;",
        "  public Test() {",
        "    add(getBar());",
        "  }",
        "  private MyBar getBar() {",
        "    if (bar == null) {",
        "      bar = new MyBar();",
        "      getButton_2();",
        "      getButton_1();",
        "    }",
        "    return bar;",
        "  }",
        "  private JButton getButton_1() {",
        "    if (button_1 == null) {",
        "      button_1 = bar.addButton();",
        "    }",
        "    return button_1;",
        "  }",
        "  private JButton getButton_2() {",
        "    if (button_2 == null) {",
        "      button_2 = bar.addButton();",
        "    }",
        "    return button_2;",
        "  }",
        "}");
  }
}
