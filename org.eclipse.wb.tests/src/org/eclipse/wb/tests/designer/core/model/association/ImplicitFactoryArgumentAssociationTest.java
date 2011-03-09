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

import org.eclipse.wb.core.model.association.ImplicitFactoryArgumentAssociation;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

/**
 * Tests for {@link ImplicitFactoryArgumentAssociation}.
 * 
 * @author scheglov_ke
 */
public class ImplicitFactoryArgumentAssociationTest extends SwingModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_permissions() throws Exception {
    ContainerInfo panel = parseTestCase();
    ContainerInfo bar = (ContainerInfo) panel.getChildrenComponents().get(0);
    ComponentInfo component = (ComponentInfo) bar.getChildrenJava().get(0);
    ComponentInfo button = (ComponentInfo) component.getChildrenJava().get(0);
    // association
    {
      ImplicitFactoryArgumentAssociation association =
          (ImplicitFactoryArgumentAssociation) button.getAssociation();
      assertTrue(association.canDelete());
    }
    // check operations
    {
      assertTrue(component.canDelete());
      assertTrue(JavaInfoUtils.canMove(component));
      assertFalse(JavaInfoUtils.canReparent(component));
    }
    {
      assertTrue(button.canDelete());
      assertFalse(JavaInfoUtils.canMove(button));
      assertTrue(JavaInfoUtils.canReparent(button));
    }
  }

  public void test_deleteButton() throws Exception {
    ContainerInfo panel = parseTestCase();
    ContainerInfo bar = (ContainerInfo) panel.getChildrenComponents().get(0);
    ComponentInfo component = (ComponentInfo) bar.getChildrenJava().get(0);
    ComponentInfo button = (ComponentInfo) component.getChildrenJava().get(0);
    // delete "button", so "component" also should be deleted
    button.delete();
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    MyBar bar = new MyBar();",
        "    add(bar);",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(bar)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: test.MyBar} {local-unique: bar} {/new MyBar()/ /add(bar)/}");
  }

  public void test_deleteFactoryComponent() throws Exception {
    ContainerInfo panel = parseTestCase();
    ContainerInfo bar = (ContainerInfo) panel.getChildrenComponents().get(0);
    ComponentInfo component = (ComponentInfo) bar.getChildrenJava().get(0);
    // delete "component", so "button" also should be deleted
    component.delete();
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    MyBar bar = new MyBar();",
        "    add(bar);",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(bar)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: test.MyBar} {local-unique: bar} {/new MyBar()/ /add(bar)/}");
  }

  public void test_reparentButton() throws Exception {
    ContainerInfo panel = parseTestCase();
    ContainerInfo bar = (ContainerInfo) panel.getChildrenComponents().get(0);
    ComponentInfo component = (ComponentInfo) bar.getChildrenJava().get(0);
    ComponentInfo button = (ComponentInfo) component.getChildrenJava().get(0);
    // move "button" directly on "panel"
    FlowLayoutInfo flowLayout = (FlowLayoutInfo) panel.getLayout();
    flowLayout.move(button, null);
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    MyBar bar = new MyBar();",
        "    add(bar);",
        "    //",
        "    JButton button = new JButton('my JButton');",
        "    add(button);",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(bar)/ /add(button)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: test.MyBar} {local-unique: bar} {/new MyBar()/ /add(bar)/}",
        "  {new: javax.swing.JButton} {local-unique: button} {/new JButton('my JButton')/ /add(button)/}");
  }

  private ContainerInfo parseTestCase() throws Exception {
    setFileContentSrc(
        "test/MyBar.java",
        getTestSource(
            "public class MyBar extends JPanel {",
            "  public JComponent addItem(Component component) {",
            "    JPanel wrapper = new JPanel();",
            "    add(wrapper);",
            "    wrapper.add(component);",
            "    return wrapper;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyBar.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='addItem'>",
            "      <parameter type='java.awt.Component' name='component'>",
            "        <tag name='implicitFactory.child' value='true'/>",
            "      </parameter>",
            "      <tag name='implicitFactory' value='true'/>",
            "    </method>",
            "  </methods>",
            "  <parameters>",
            "    <parameter name='layout.has'>false</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    MyBar bar = new MyBar();",
            "    add(bar);",
            "    //",
            "    JButton button = new JButton('my JButton');",
            "    JComponent component = bar.addItem(button);",
            "    component.setEnabled(false);",
            "  }",
            "}");
    // check hierarchy
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(bar)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: test.MyBar} {local-unique: bar} {/new MyBar()/ /add(bar)/ /bar.addItem(button)/}",
        "    {implicit-factory} {local-unique: component} {/bar.addItem(button)/ /component.setEnabled(false)/}",
        "      {new: javax.swing.JButton} {local-unique: button} {/new JButton('my JButton')/ /bar.addItem(button)/}");
    return panel;
  }
}
