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
package org.eclipse.wb.tests.designer.core.model.util;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.order.ComponentOrder;
import org.eclipse.wb.internal.core.model.order.ComponentOrderBeforeSibling;
import org.eclipse.wb.internal.core.model.order.ComponentOrderDefault;
import org.eclipse.wb.internal.core.model.order.ComponentOrderFirst;
import org.eclipse.wb.internal.core.model.order.ComponentOrderLast;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link ComponentOrder}.
 * 
 * @author scheglov_ke
 */
public class ComponentOrderTest extends SwingModelTest {
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
  // Parsing
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_parseBad() throws Exception {
    try {
      ComponentOrder.parse("noSuchComponentOrder");
      fail();
    } catch (IllegalArgumentException e) {
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Default
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ComponentOrderDefault}.
   */
  public void test_default() throws Exception {
    ComponentOrder order = ComponentOrder.parse("default");
    String[] lines =
        {
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new JButton());",
            "    add(new JCheckBox());",
            "  }",
            "}"};
    ContainerInfo container = parseContainer(lines);
    ComponentInfo component = createJButton();
    //
    assertSame(null, order.getNextComponent_whenLast(component, container));
    {
      ComponentInfo otherComponent = container.getChildrenComponents().get(0);
      assertTrue(order.canBeBefore(otherComponent));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // First
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ComponentOrderFirst}.
   */
  public void test_first() throws Exception {
    String[] lines =
        {
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new JButton());",
            "    add(new JCheckBox());",
            "  }",
            "}"};
    ContainerInfo container = parseContainer(lines);
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(new JButton())/ /add(new JCheckBox())/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {empty} {/add(new JButton())/}",
        "  {new: javax.swing.JCheckBox} {empty} {/add(new JCheckBox())/}");
    // prepare component
    ComponentInfo component = create_firstComponent();
    ComponentOrder order = component.getDescription().getOrder();
    // component: before first JavaInfo
    {
      ComponentInfo firstChild = container.getChildrenComponents().get(0);
      assertSame(firstChild, order.getNextComponent_whenLast(component, container));
    }
    // yes, "first" should be before any other
    {
      ComponentInfo otherComponent = container.getChildrenComponents().get(0);
      assertTrue(order.canBeBefore(otherComponent));
    }
  }

  /**
   * Test for {@link ComponentOrderFirst}.
   */
  public void test_first_add() throws Exception {
    String[] lines =
        {
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new JButton());",
            "  }",
            "}"};
    ContainerInfo container = parseContainer(lines);
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(new JButton())/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {empty} {/add(new JButton())/}");
    // prepare component
    ComponentInfo component = create_firstComponent();
    // add
    ((FlowLayoutInfo) container.getLayout()).add(component, null);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      MyButton myButton = new MyButton();",
        "      add(myButton);",
        "    }",
        "    add(new JButton());",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(new JButton())/ /add(myButton)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: test.MyButton} {local-unique: myButton} {/new MyButton()/ /add(myButton)/}",
        "  {new: javax.swing.JButton} {empty} {/add(new JButton())/}");
  }

  /**
   * Test for {@link ComponentOrderFirst}.
   */
  public void test_first_add_allImplicit() throws Exception {
    String[] lines =
        {
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "  // filler filler filler",
            "}"};
    ContainerInfo container = parseContainer(lines);
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
    // prepare component
    ComponentInfo component = create_firstComponent();
    // add
    ((FlowLayoutInfo) container.getLayout()).add(component, null);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      MyButton myButton = new MyButton();",
        "      add(myButton);",
        "    }",
        "  }",
        "  // filler filler filler",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(myButton)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: test.MyButton} {local-unique: myButton} {/new MyButton()/ /add(myButton)/}");
  }

  private ComponentInfo create_firstComponent() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends JButton {",
            "  // filler filler filler",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSource(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <order>first</order>",
            "</component>"));
    waitForAutoBuild();
    //
    ComponentInfo component = createComponent("test.MyButton");
    assertSame(ComponentOrderFirst.INSTANCE, component.getDescription().getOrder());
    return component;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Last
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ComponentOrderLast}.
   */
  public void test_last() throws Exception {
    String[] lines =
        {
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new JButton());",
            "  }",
            "}"};
    ContainerInfo container = parseContainer(lines);
    // prepare component
    ComponentInfo component = create_lastComponent();
    ComponentOrder order = component.getDescription().getOrder();
    // component: after all
    JavaInfo expectedNextComponent = null;
    assertSame(expectedNextComponent, order.getNextComponent_whenLast(component, container));
    // no, this component should be last
    {
      ComponentInfo otherComponent = container.getChildrenComponents().get(0);
      assertFalse(order.canBeBefore(otherComponent));
    }
  }

  /**
   * Test for {@link ComponentOrderLast}.
   */
  public void test_last_addIt() throws Exception {
    String[] lines =
        {
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new JButton());",
            "  }",
            "}"};
    ContainerInfo container = parseContainer(lines);
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(new JButton())/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {empty} {/add(new JButton())/}");
    // prepare component
    ComponentInfo component = create_lastComponent();
    // add
    ((FlowLayoutInfo) container.getLayout()).add(component, null);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    add(new JButton());",
        "    {",
        "      MyButton myButton = new MyButton();",
        "      add(myButton);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(new JButton())/ /add(myButton)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {empty} {/add(new JButton())/}",
        "  {new: test.MyButton} {local-unique: myButton} {/new MyButton()/ /add(myButton)/}");
  }

  /**
   * Test for {@link ComponentOrderLast}.
   * <p>
   * New sibling should be added before "last", even if we don't give "next".
   */
  public void test_last_addOther() throws Exception {
    prepare_lastComponent();
    String[] lines =
        {
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new MyButton());",
            "  }",
            "}"};
    ContainerInfo container = parseContainer(lines);
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(new MyButton())/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: test.MyButton} {empty} {/add(new MyButton())/}");
    container.refresh();
    // prepare new JButton
    ComponentInfo newButton = createJButton();
    // add
    ((FlowLayoutInfo) container.getLayout()).add(newButton, null);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JButton button = new JButton();",
        "      add(button);",
        "    }",
        "    add(new MyButton());",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(new MyButton())/ /add(button)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton empty} {local-unique: button} {/new JButton()/ /add(button)/}",
        "  {new: test.MyButton} {empty} {/add(new MyButton())/}");
  }

  /**
   * Test for {@link ComponentOrderLast}.
   * <p>
   * New sibling should be added before "last", even if we don't give "next".
   */
  public void test_last_reparentOther() throws Exception {
    prepare_lastComponent();
    String[] lines =
        {
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JPanel inner = new JPanel();",
            "      add(inner);",
            "      {",
            "        JButton button = new JButton();",
            "        inner.add(button);",
            "      }",
            "    }",
            "    add(new MyButton());",
            "  }",
            "}"};
    ContainerInfo container = parseContainer(lines);
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(inner)/ /add(new MyButton())/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JPanel} {local-unique: inner} {/new JPanel()/ /add(inner)/ /inner.add(button)/}",
        "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "    {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /inner.add(button)/}",
        "  {new: test.MyButton} {empty} {/add(new MyButton())/}");
    container.refresh();
    ContainerInfo inner = (ContainerInfo) container.getChildrenComponents().get(0);
    ComponentInfo button = inner.getChildrenComponents().get(0);
    // move
    ((FlowLayoutInfo) container.getLayout()).move(button, null);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JPanel inner = new JPanel();",
        "      add(inner);",
        "    }",
        "    {",
        "      JButton button = new JButton();",
        "      add(button);",
        "    }",
        "    add(new MyButton());",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(inner)/ /add(new MyButton())/ /add(button)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JPanel} {local-unique: inner} {/new JPanel()/ /add(inner)/}",
        "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /add(button)/}",
        "  {new: test.MyButton} {empty} {/add(new MyButton())/}");
  }

  private ComponentInfo create_lastComponent() throws Exception {
    prepare_lastComponent();
    //
    ComponentInfo component = createComponent("test.MyButton");
    assertSame(ComponentOrderLast.INSTANCE, component.getDescription().getOrder());
    return component;
  }

  private void prepare_lastComponent() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends JButton {",
            "  // filler filler filler",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSource(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <order>last</order>",
            "</component>"));
    waitForAutoBuild();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // beforeSibling
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ComponentOrderBeforeSibling}.
   */
  public void test_beforeSibling() throws Exception {
    String[] lines =
        {
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new JTextField());",
            "    add(new JButton());",
            "  }",
            "}"};
    ContainerInfo container = parseContainer(lines);
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(new JTextField())/ /add(new JButton())/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JTextField} {empty} {/add(new JTextField())/}",
        "  {new: javax.swing.JButton} {empty} {/add(new JButton())/}");
    // prepare component
    ComponentInfo component = create_beforeSibling();
    ComponentOrder order = component.getDescription().getOrder();
    // component: before JButton
    {
      ComponentInfo button = container.getChildrenComponents().get(1);
      assertSame(button, order.getNextComponent_whenLast(component, container));
    }
    // no limit
    {
      ComponentInfo otherComponent = container.getChildrenComponents().get(0);
      assertTrue(order.canBeBefore(otherComponent));
    }
  }

  /**
   * Test for {@link ComponentOrderBeforeSibling}.
   */
  public void test_beforeSibling_noComponentForBefore() throws Exception {
    String[] lines =
        {
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new JTextField());",
            "  }",
            "}"};
    ContainerInfo container = parseContainer(lines);
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(new JTextField())/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JTextField} {empty} {/add(new JTextField())/}");
    // prepare component
    ComponentInfo component = create_beforeSibling();
    ComponentOrder order = component.getDescription().getOrder();
    // component: no JButton
    assertSame(null, order.getNextComponent_whenLast(component, container));
  }

  /**
   * Test for {@link ComponentOrderBeforeSibling}.
   */
  public void test_beforeSibling_add_it() throws Exception {
    String[] lines =
        {
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new JTextField());",
            "    add(new JButton());",
            "  }",
            "}"};
    ContainerInfo container = parseContainer(lines);
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(new JTextField())/ /add(new JButton())/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JTextField} {empty} {/add(new JTextField())/}",
        "  {new: javax.swing.JButton} {empty} {/add(new JButton())/}");
    // prepare component
    ComponentInfo component = create_beforeSibling();
    // add
    ((FlowLayoutInfo) container.getLayout()).add(component, null);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    add(new JTextField());",
        "    {",
        "      MyButton myButton = new MyButton();",
        "      add(myButton);",
        "    }",
        "    add(new JButton());",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(new JTextField())/ /add(new JButton())/ /add(myButton)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JTextField} {empty} {/add(new JTextField())/}",
        "  {new: test.MyButton} {local-unique: myButton} {/new MyButton()/ /add(myButton)/}",
        "  {new: javax.swing.JButton} {empty} {/add(new JButton())/}");
  }

  private ComponentInfo create_beforeSibling() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends JButton {",
            "  // filler filler filler",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSource(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <order>beforeSibling javax.swing.JButton</order>",
            "</component>"));
    waitForAutoBuild();
    //
    ComponentInfo component = createComponent("test.MyButton");
    assertThat(component.getDescription().getOrder()).isInstanceOf(
        ComponentOrderBeforeSibling.class);
    return component;
  }
}
