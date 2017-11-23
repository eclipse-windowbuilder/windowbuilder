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
package org.eclipse.wb.tests.designer.core.model.variables;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.VoidInvocationCreationSupport;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.model.variable.VoidInvocationVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.component.JToolBarInfo;
import org.eclipse.wb.internal.swing.model.component.JToolBarSeparatorCreationSupport;
import org.eclipse.wb.internal.swing.model.component.JToolBarSeparatorInfo;

import org.eclipse.jdt.core.dom.MethodInvocation;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang.NotImplementedException;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JToolBar;

/**
 * Test for {@link VoidInvocationVariableSupport}.
 * 
 * @author scheglov_ke
 */
public class VoidInvocationTest extends AbstractVariableTest {
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
  public void test_object() throws Exception {
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
    VoidInvocationVariableSupport variableSupport =
        (VoidInvocationVariableSupport) separator.getVariableSupport();
    // basic checks
    assertEquals("void", variableSupport.toString());
    assertEquals("addSeparator()", variableSupport.getTitle());
    assertFalse(variableSupport.isDefault());
    // name
    assertFalse(variableSupport.hasName());
    try {
      variableSupport.getName();
      fail();
    } catch (IllegalStateException e) {
    }
    // conversion
    assertFalse(variableSupport.canConvertLocalToField());
    assertFalse(variableSupport.canConvertFieldToLocal());
    try {
      variableSupport.convertLocalToField();
      fail();
    } catch (IllegalStateException e) {
    }
    try {
      variableSupport.convertFieldToLocal();
      fail();
    } catch (IllegalStateException e) {
    }
    // reference/access expressions
    try {
      variableSupport.getReferenceExpression((NodeTarget) null);
      fail();
    } catch (IllegalStateException e) {
    }
    try {
      variableSupport.getAccessExpression((NodeTarget) null);
      fail();
    } catch (IllegalStateException e) {
    }
    // getStatementTarget()
    try {
      variableSupport.getStatementTarget();
      fail();
    } catch (IllegalStateException e) {
    }
  }

  /**
   * {@link VoidInvocationCreationSupport} does not return value, so has only factory properties, no
   * method/field based ones.
   */
  public void test_noProperties() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JToolBar bar = new JToolBar();",
            "    add(bar);",
            "    bar.addSeparator(new Dimension(100, 50));",
            "  }",
            "}");
    JToolBarInfo bar = (JToolBarInfo) panel.getChildrenComponents().get(0);
    JToolBarSeparatorInfo separator = (JToolBarSeparatorInfo) bar.getChildrenComponents().get(0);
    //
    Property[] properties = separator.getProperties();
    assertThat(properties).hasSize(1);
    assertNotNull(PropertyUtils.getByPath(properties, "Factory"));
    assertNotNull(PropertyUtils.getByPath(properties, "Factory/size"));
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
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JToolBar bar = new JToolBar();",
        "    add(bar);",
        "    bar.addSeparator();",
        "  }",
        "}");
    assertEquals("addSeparator()", separator.getVariableSupport().getTitle());
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
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Method with Component, as JavaInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Good case - argument of <code>addItem()</code> is existing {@link JavaInfo}.
   */
  public void test_parseComponent_hasComponent() throws Exception {
    prepare_parseComponent();
    String[] lines =
        {
            "class Test extends JPanel {",
            "  Test() {",
            "    MyBar bar = new MyBar();",
            "    add(bar);",
            "    //",
            "    JButton button = new JButton();",
            "    bar.addItem(button);",
            "  }",
            "}"};
    parseContainer(lines);
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(bar)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: test.MyBar} {local-unique: bar} {/new MyBar()/ /add(bar)/ /bar.addItem(button)/}",
        "    {void} {void} {/bar.addItem(button)/}",
        "      {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /bar.addItem(button)/}");
  }

  /**
   * We specify that <code>addItem(Component)</code> requires {@link JavaInfo}, so if it is not
   * present (by any reason), we don't create "voidFactory" component.
   */
  public void test_parseComponent_invalidComponent() throws Exception {
    prepare_parseComponent();
    String[] lines =
        {
            "class Test extends JPanel {",
            "  Test() {",
            "    MyBar bar = new MyBar();",
            "    add(bar);",
            "    //",
            "    bar.addItem(null);",
            "  }",
            "}"};
    parseContainer(lines);
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(bar)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: test.MyBar} {local-unique: bar} {/new MyBar()/ /add(bar)/ /bar.addItem(null)/}");
  }

  private void prepare_parseComponent() throws Exception {
    setFileContentSrc(
        "test/MyBar.java",
        getTestSource(
            "public class MyBar extends JPanel {",
            "  public void addItem(Component content) {",
            "    JPanel wrapper = new JPanel();",
            "    add(wrapper);",
            "    wrapper.add(content);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyBar.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='addItem'>",
            "      <parameter type='java.awt.Component'>",
            "        <tag name='voidFactory.requiredJavaInfo' value='true'/>",
            "      </parameter>",
            "      <tag name='voidFactory.creationSupport' value='"
                + Test_CreationSupport.class.getName()
                + "'/>",
            "      <tag name='voidFactory.componentClass' value='java.awt.Component'/>",
            "    </method>",
            "  </methods>",
            "  <parameters>",
            "    <parameter name='layout.has'>false</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
  }

  public static class Test_CreationSupport extends VoidInvocationCreationSupport {
    public Test_CreationSupport(JavaInfo hostJavaInfo,
        MethodDescription description,
        MethodInvocation invocation,
        JavaInfo[] argumentInfos) {
      super(hostJavaInfo, description, invocation);
      assertNotNull(argumentInfos[0]);
    }

    @Override
    protected Object getObject(Object toolbar) throws Exception {
      Component[] components = ((Container) toolbar).getComponents();
      return components[components.length - 1];
    }

    @Override
    protected String add_getMethodSource() throws Exception {
      throw new NotImplementedException();
    }
  }
}
