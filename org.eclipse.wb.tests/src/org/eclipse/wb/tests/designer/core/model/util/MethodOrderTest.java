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
import org.eclipse.wb.core.model.WrapperMethodInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.model.order.MethodOrder;
import org.eclipse.wb.internal.core.model.order.MethodOrderAfterAssociation;
import org.eclipse.wb.internal.core.model.order.MethodOrderAfterCreation;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.component.JPanelInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jface.dialogs.TitleAreaDialog;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Tests for {@link MethodDescription} and {@link MethodOrder}.
 * 
 * @author scheglov_ke
 */
public class MethodOrderTest extends SwingModelTest {
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
  public void test_MethodOrderDescription_parse_bad() throws Exception {
    try {
      MethodOrder.parse("noSuchMethodOrder");
      fail();
    } catch (IllegalArgumentException e) {
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // normal
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * No special {@link MethodOrder}, so {@link MethodOrderAfterCreation}.
   */
  public void test_addMethodInvocation_default_default() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setAutoscrolls(true);",
            "  }",
            "}");
    // check
    panel.addMethodInvocation("setEnabled(boolean)", "false");
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setEnabled(false);",
        "    setAutoscrolls(true);",
        "  }",
        "}");
    assertInstanceOf(MethodOrderAfterCreation.class, panel.getDescription().getDefaultMethodOrder());
  }

  /**
   * Add {@link MethodInvocation} for method without {@link MethodOrder}, so default
   * {@link MethodOrder} of component is used, and default is set to "afterCreation".
   */
  public void test_this_addMethodInvocation_default_afterCreation() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends JButton {",
            "  public void foo(int value) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <method-order>",
            "    <default order='afterCreation'/>",
            "  </method-order>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      MyButton button = new MyButton();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    assertInstanceOf(
        MethodOrderAfterCreation.class,
        button.getDescription().getDefaultMethodOrder());
    // add invocation
    button.addMethodInvocation("foo(int)", "555");
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      MyButton button = new MyButton();",
        "      button.foo(555);",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Add {@link MethodInvocation} for method without {@link MethodOrder}, so default
   * {@link MethodOrder} of component is used, and default is set to "afterAssociation".
   */
  public void test_this_addMethodInvocation_default_afterAssociation() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends JButton {",
            "  public void foo(int value) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <method-order>",
            "    <default order='afterAssociation'/>",
            "  </method-order>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      MyButton button = new MyButton();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    assertInstanceOf(
        MethodOrderAfterAssociation.class,
        button.getDescription().getDefaultMethodOrder());
    // add invocation
    button.addMethodInvocation("foo(int)", "555");
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      MyButton button = new MyButton();",
        "      add(button);",
        "      button.foo(555);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // first
  //
  ////////////////////////////////////////////////////////////////////////////
  private void prepare_first_setEnabled() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "// filler filler filler",
            "public class MyPanel extends JPanel {",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='setEnabled' order='first'>",
            "      <parameter type='boolean'/>",
            "    </method>",
            "  </methods>",
            "</component>"));
    waitForAutoBuild();
  }

  /**
   * Add <code>setEnabled()</code> as first {@link Statement}.
   */
  public void test_first_addMethodInvocation() throws Exception {
    prepare_first_setEnabled();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    setAutoscrolls(true);",
            "  }",
            "}");
    // check
    panel.addMethodInvocation("setEnabled(boolean)", "false");
    assertEditor(
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    setEnabled(false);",
        "    setAutoscrolls(true);",
        "  }",
        "}");
  }

  /**
   * Add <code>setEnabled()</code> as first {@link Statement}.
   * <p>
   * Case when {@link JavaInfo} is "this" component.
   */
  public void test_first_addMethodInvocation_inversionThis() throws Exception {
    prepare_first_setEnabled();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    setEnabled(false);",
            "  }",
            "}");
    // check
    panel.addMethodInvocation("setAutoscrolls(boolean)", "true");
    assertEditor(
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    setEnabled(false);",
        "    setAutoscrolls(true);",
        "  }",
        "}");
  }

  /**
   * Add <code>setEnabled()</code> as first {@link Statement}.
   * <p>
   * Case when {@link JavaInfo} is "local" component.
   */
  public void test_first_addMethodInvocation_inversion() throws Exception {
    prepare_first_setEnabled();
    // parse
    parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    MyPanel myPanel = new MyPanel();",
        "    myPanel.setEnabled(false);",
        "    add(myPanel);",
        "  }",
        "}");
    ContainerInfo panel = getJavaInfoByName("myPanel");
    // check
    panel.addMethodInvocation("setAutoscrolls(boolean)", "true");
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    MyPanel myPanel = new MyPanel();",
        "    myPanel.setEnabled(false);",
        "    myPanel.setAutoscrolls(true);",
        "    add(myPanel);",
        "  }",
        "}");
  }

  /**
   * New components should be added <em>after</em> the <code>setEnabled()</code> invocation.
   */
  public void test_first_getTarget() throws Exception {
    prepare_first_setEnabled();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    setEnabled(false);",
            "  }",
            "}");
    // check
    ComponentInfo newButton = createJButton();
    ((FlowLayoutInfo) panel.getLayout()).add(newButton, null);
    assertEditor(
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    setEnabled(false);",
        "    {",
        "      JButton button = new JButton();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // after (i.e. after some other method invocation)
  //
  ////////////////////////////////////////////////////////////////////////////
  private void prepare_setEnabled_after_setAutoscrolls() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends JPanel {",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='setEnabled' order='after setAutoscrolls(boolean)'>",
            "      <parameter type='boolean'/>",
            "    </method>",
            "  </methods>",
            "</component>"));
    waitForAutoBuild();
  }

  /**
   * Add <code>setEnabled()</code> after <code>setAutoscrolls()</code>.<br>
   * Target method invocation exists.
   */
  public void test_addMethodInvocation_after_canReference() throws Exception {
    prepare_setEnabled_after_setAutoscrolls();
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    setEnabled(false);",
            "  }",
            "}");
    // check target
    {
      StatementTarget target = JavaInfoUtils.getTarget(panel, null);
      assertTarget(target, null, getStatement(panel, 0), false);
    }
  }

  /**
   * Add <code>setEnabled()</code> after <code>setAutoscrolls()</code>.<br>
   * Target method invocation exists.
   */
  public void test_addMethodInvocation_after_withTarget() throws Exception {
    prepare_setEnabled_after_setAutoscrolls();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    setAutoscrolls(true);",
            "  }",
            "}");
    // check
    panel.addMethodInvocation("setEnabled(boolean)", "false");
    assertEditor(
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    setAutoscrolls(true);",
        "    setEnabled(false);",
        "  }",
        "}");
  }

  /**
   * Add <code>setEnabled()</code> after <code>setAutoscrolls()</code>.<br>
   * Target method invocation exists (two times).
   */
  public void test_addMethodInvocation_after_withTarget2() throws Exception {
    prepare_setEnabled_after_setAutoscrolls();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    setAutoscrolls(true);",
            "    setAutoscrolls(true);",
            "  }",
            "}");
    // check
    panel.addMethodInvocation("setEnabled(boolean)", "false");
    assertEditor(
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    setAutoscrolls(true);",
        "    setAutoscrolls(true);",
        "    setEnabled(false);",
        "  }",
        "}");
  }

  /**
   * Add <code>setEnabled()</code> after <code>setAutoscrolls()</code>.<br>
   * Target method invocation does not exist.
   */
  public void test_addMethodInvocation_after_noTarget() throws Exception {
    prepare_setEnabled_after_setAutoscrolls();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    setFont(null);",
            "  }",
            "}");
    // check
    panel.addMethodInvocation("setEnabled(boolean)", "false");
    assertEditor(
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    setEnabled(false);",
        "    setFont(null);",
        "  }",
        "}");
  }

  /**
   * Add <code>setEnabled()</code> after <code>setAutoscrolls()</code>.
   * <p>
   * Target method invocation does not exist, but exists invocation of other method, after which
   * target should be.
   */
  public void test_addMethodInvocation_after_transitivity() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends JPanel {",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='setAutoscrolls' order='after setOpaque(boolean)'>",
            "      <parameter type='boolean'/>",
            "    </method>",
            "    <method name='setEnabled' order='after setAutoscrolls(boolean)'>",
            "      <parameter type='boolean'/>",
            "    </method>",
            "  </methods>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    setOpaque(false);",
            "  }",
            "}");
    // check
    panel.addMethodInvocation("setEnabled(boolean)", "false");
    assertEditor(
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    setOpaque(false);",
        "    setEnabled(false);",
        "  }",
        "}");
  }

  /**
   * Add <code>setAutoscrolls()</code> before <code>setEnabled()</code>.<br>
   */
  public void test_addMethodInvocation_after_inversion() throws Exception {
    prepare_setEnabled_after_setAutoscrolls();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    setFont(null);",
            "    setEnabled(false);",
            "  }",
            "}");
    // check
    panel.addMethodInvocation("setAutoscrolls(boolean)", "true");
    assertEditor(
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    setFont(null);",
        "    setAutoscrolls(true);",
        "    setEnabled(false);",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // last
  //
  ////////////////////////////////////////////////////////////////////////////
  private void prepare_setEnabled_last() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "// filler filler filler",
            "public class MyPanel extends JPanel {",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='setEnabled' order='last'>",
            "      <parameter type='boolean'/>",
            "    </method>",
            "  </methods>",
            "</component>"));
    waitForAutoBuild();
  }

  /**
   * Add <code>setEnabled()</code> as last {@link Statement}.
   */
  public void test_addMethodInvocation_last() throws Exception {
    prepare_setEnabled_last();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    setAutoscrolls(true);",
            "    {",
            "      JButton button = new JButton();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    // check
    panel.addMethodInvocation("setEnabled(boolean)", "false");
    assertEditor(
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    setAutoscrolls(true);",
        "    {",
        "      JButton button = new JButton();",
        "      add(button);",
        "    }",
        "    setEnabled(false);",
        "  }",
        "}");
  }

  /**
   * New components should be added <em>before</em> the <code>setEnabled()</code> invocation.
   */
  public void test_getTarget_last() throws Exception {
    prepare_setEnabled_last();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    setEnabled(false);",
            "  }",
            "}");
    // check
    ComponentInfo newButton = createJButton();
    ((FlowLayoutInfo) panel.getLayout()).add(newButton, null);
    assertEditor(
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    {",
        "      JButton button = new JButton();",
        "      add(button);",
        "    }",
        "    setEnabled(false);",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // last: wrapper
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_addMethodInvocation_last_whenWrapper() throws Exception {
    prepare_setEnabled_last_whenWrapper();
    parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    TestWrapper wrapper = new TestWrapper(this);",
        "    JButton wrapped = wrapper.getControl();",
        "  }",
        "}");
    JavaInfo wrapper = getJavaInfoByName("wrapper");
    // check
    wrapper.addMethodInvocation("setEnabled(boolean)", "false");
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    TestWrapper wrapper = new TestWrapper(this);",
        "    JButton wrapped = wrapper.getControl();",
        "    wrapper.setEnabled(false);",
        "  }",
        "}");
  }

  public void test_getTarget_last_whenWrapper_andNonExecutableInvocation() throws Exception {
    prepare_setEnabled_last_whenWrapper();
    parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    TestWrapper wrapper = new TestWrapper(this);",
        "    wrapper.hashCode();",
        "    JButton wrapped = wrapper.getControl();",
        "  }",
        "}");
    JavaInfo wrapper = getJavaInfoByName("wrapper");
    // check
    wrapper.addMethodInvocation("setEnabled(boolean)", "false");
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    TestWrapper wrapper = new TestWrapper(this);",
        "    wrapper.hashCode();",
        "    JButton wrapped = wrapper.getControl();",
        "    wrapper.setEnabled(false);",
        "  }",
        "}");
  }

  private void prepare_setEnabled_last_whenWrapper() throws Exception {
    setFileContentSrc(
        "test/TestWrapper.java",
        getTestSource(
            "public class TestWrapper {",
            "  JButton m_control;",
            "  public TestWrapper(Container parent){",
            "    m_control = new JButton();",
            "    m_control.setBounds(10, 10, 10, 10);",
            "    parent.add(m_control);",
            "  }",
            "  public JButton getControl(){",
            "    return m_control;",
            "  }",
            "  public void setEnabled(boolean enabled){",
            "  }",
            "}"));
    setFileContentSrc(
        "test/TestWrapper.wbp-component.xml",
        getSource(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <toolkit id='org.eclipse.wb.swing'/>",
            "  <model class='" + WrapperMethodInfo.class.getName() + "'/>",
            "  <creation>",
            "    <source><![CDATA[new test.TestWrapper(%parent%)]]></source>",
            "  </creation>",
            "  <constructors>",
            "    <constructor>",
            "      <parameter type='java.awt.Container' parent='true'/>",
            "    </constructor>",
            "  </constructors>",
            "  <methods>",
            "    <method name='setEnabled' order='last'>",
            "      <parameter type='boolean'/>",
            "    </method>",
            "  </methods>",
            "  <parameters>",
            "    <parameter name='Wrapper.method'>getControl</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // beforeAssociation
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_beforeAssociation_addInvocation() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends JButton {",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <method-order>",
            "    <default order='beforeAssociation'/>",
            "  </method-order>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      MyButton button = new MyButton();",
            "      button.setAutoscrolls(true);",
            "      add(button);",
            "      button.setAutoscrolls(true);",
            "    }",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // add invocation
    button.getPropertyByTitle("enabled").setValue(false);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      MyButton button = new MyButton();",
        "      button.setAutoscrolls(true);",
        "      button.setEnabled(false);",
        "      add(button);",
        "      button.setAutoscrolls(true);",
        "    }",
        "  }",
        "}");
    // check target
    {
      StatementTarget target = JavaInfoUtils.getTarget(button, null);
      assertTarget(target, null, getStatement(button, 4), false);
    }
  }

  public void test_beforeAssociation_forThis_addInvocation() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends JPanel {",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <method-order>",
            "    <default order='beforeAssociation'/>",
            "  </method-order>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    // add invocation
    panel.getPropertyByTitle("enabled").setValue(false);
    assertEditor(
        "// filler filler filler",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    setEnabled(false);",
        "  }",
        "}");
    // check target
    {
      StatementTarget target = JavaInfoUtils.getTarget(panel, null);
      assertTarget(target, null, getStatement(panel, 0), false);
    }
  }

  public void test_beforeAssociation_forThis_exposed_addInvocation() throws Exception {
    setFileContentSrc(
        "test/MyFrame.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyFrame extends JFrame {",
            "}"));
    setFileContentSrc(
        "test/MyFrame.getContentPane__.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <method-order>",
            "    <default order='beforeAssociation'/>",
            "  </method-order>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyFrame {",
            "  public Test() {",
            "    setEnabled(false);",
            "  }",
            "}");
    ComponentInfo contentPane = panel.getChildrenComponents().get(0);
    // add invocation
    contentPane.getPropertyByTitle("enabled").setValue(false);
    assertEditor(
        "public class Test extends MyFrame {",
        "  public Test() {",
        "    getContentPane().setEnabled(false);",
        "    setEnabled(false);",
        "  }",
        "}");
  }

  public void test_beforeAssociation_lazy() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends JButton {",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <method-order>",
            "    <default order='beforeAssociation'/>",
            "  </method-order>",
            "</component>"));
    waitForAutoBuild();
    String[] lines =
        {
            "public class Test extends JPanel {",
            "  private MyButton button;",
            "  public Test() {",
            "    add(getButton());",
            "  }",
            "  private MyButton getButton() {",
            "    if (button == null) {",
            "      button = new MyButton();",
            "      button.setDefaultCapable(true);",
            "    }",
            "    return button;",
            "  }",
            "}"};
    // parse
    ContainerInfo panel = parseContainer(lines);
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // add invocation
    button.getPropertyByTitle("enabled").setValue(false);
    assertEditor(
        "public class Test extends JPanel {",
        "  private MyButton button;",
        "  public Test() {",
        "    add(getButton());",
        "  }",
        "  private MyButton getButton() {",
        "    if (button == null) {",
        "      button = new MyButton();",
        "      button.setEnabled(false);",
        "      button.setDefaultCapable(true);",
        "    }",
        "    return button;",
        "  }",
        "}");
  }

  public void test_beforeAssociation_forRoot() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test {",
            "  public static void main(String[] args) {",
            "    JPanel panel = new JPanel();",
            "  }",
            "}");
    // ask target
    MethodOrder methodOrder = new MethodOrderAfterAssociation();
    StatementTarget target = methodOrder.getTarget(panel, "noMatter()");
    assertTarget(target, null, getStatement(panel, 0), false);
  }

  public void test_beforeAssociation_CREATE() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends JButton {",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <creation>",
            "    <source><![CDATA[new test.MyButton()]]></source>",
            "    <invocation signature='setText(java.lang.String)'><![CDATA['New Button']]></invocation>",
            "  </creation>",
            "  <method-order>",
            "    <default order='beforeAssociation'/>",
            "  </method-order>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    // add new MyButton
    ComponentInfo button = createJavaInfo("test.MyButton");
    ((FlowLayoutInfo) panel.getLayout()).add(button, null);
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      MyButton myButton = new MyButton();",
        "      myButton.setText('New Button');",
        "      add(myButton);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // afterAssociation
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_afterAssociation_forChild_addInvocation() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends JButton {",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <method-order>",
            "    <default order='afterAssociation'/>",
            "  </method-order>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      MyButton button = new MyButton();",
            "      button.setAutoscrolls(true);",
            "      add(button);",
            "      button.setAutoscrolls(true);",
            "    }",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // add invocation
    button.getPropertyByTitle("enabled").setValue(false);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      MyButton button = new MyButton();",
        "      button.setAutoscrolls(true);",
        "      add(button);",
        "      button.setEnabled(false);",
        "      button.setAutoscrolls(true);",
        "    }",
        "  }",
        "}");
    // check target
    {
      StatementTarget target = JavaInfoUtils.getTarget(button, null);
      assertTarget(target, null, getStatement(button, 4), false);
    }
  }

  /**
   * If {@link Association} has no {@link Statement}, then we should fall to default association.
   */
  public void test_afterAssociation_implicitAssociation() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public abstract class MyPanel extends JPanel {",
            "  public MyPanel() {",
            "    add(getContent());",
            "  }",
            "  protected abstract Component getContent();",
            "}"));
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends JButton {",
            "  public MyButton() {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSource(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <method-order>",
            "    <default order='afterAssociation'/>",
            "  </method-order>",
            "</component>"));
    waitForAutoBuild();
    String[] lines =
        {
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "  protected Component getContent() {",
            "    MyButton myButton = new MyButton();",
            "    myButton.setDefaultCapable(true);",
            "    return myButton;",
            "  }",
            "}"};
    // parse
    ContainerInfo panel = parseContainer(lines);
    assertHierarchy(
        "{this: test.MyPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: test.MyButton} {local-unique: myButton} {/new MyButton()/ /myButton.setDefaultCapable(true)/ /myButton/}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // add invocation
    button.getPropertyByTitle("enabled").setValue(false);
    assertEditor(
        "public class Test extends MyPanel {",
        "  public Test() {",
        "  }",
        "  protected Component getContent() {",
        "    MyButton myButton = new MyButton();",
        "    myButton.setEnabled(false);",
        "    myButton.setDefaultCapable(true);",
        "    return myButton;",
        "  }",
        "}");
  }

  public void test_afterAssociation_forThis_addInvocation() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends JPanel {",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <method-order>",
            "    <default order='afterAssociation'/>",
            "  </method-order>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    // add invocation
    panel.getPropertyByTitle("enabled").setValue(false);
    assertEditor(
        "// filler filler filler",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    setEnabled(false);",
        "  }",
        "}");
    // check target
    {
      StatementTarget target = JavaInfoUtils.getTarget(panel, null);
      assertTarget(target, null, getStatement(panel, 0), false);
    }
  }

  public void test_afterAssociation_forThis_exposed_addInvocation() throws Exception {
    setFileContentSrc(
        "test/MyFrame.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyFrame extends JFrame {",
            "}"));
    setFileContentSrc(
        "test/MyFrame.getContentPane__.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <method-order>",
            "    <default order='afterAssociation'/>",
            "  </method-order>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyFrame {",
            "  public Test() {",
            "    setEnabled(false);",
            "  }",
            "}");
    ComponentInfo contentPane = panel.getChildrenComponents().get(0);
    // add invocation
    contentPane.getPropertyByTitle("enabled").setValue(false);
    assertEditor(
        "public class Test extends MyFrame {",
        "  public Test() {",
        "    getContentPane().setEnabled(false);",
        "    setEnabled(false);",
        "  }",
        "}");
  }

  public void test_afterAssociation_forRoot() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test {",
            "  public static void main(String[] args) {",
            "    JPanel panel = new JPanel();",
            "  }",
            "}");
    // ask target
    MethodOrder methodOrder = new MethodOrderAfterAssociation();
    StatementTarget target = methodOrder.getTarget(panel, "noMatter()");
    assertTarget(target, null, getStatement(panel, 0), false);
  }

  public void test_afterAssociation_CREATE() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends JButton {",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <creation>",
            "    <source><![CDATA[new test.MyButton()]]></source>",
            "    <invocation signature='setText(java.lang.String)'><![CDATA['New Button']]></invocation>",
            "  </creation>",
            "  <method-order>",
            "    <default order='afterAssociation'/>",
            "  </method-order>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    // add new MyButton
    ComponentInfo button = createJavaInfo("test.MyButton");
    ((FlowLayoutInfo) panel.getLayout()).add(button, null);
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      MyButton myButton = new MyButton();",
        "      add(myButton);",
        "      myButton.setText('New Button');",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // This specific
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Tag <code>thisTargetMethod</code> for {@link MethodDescription} allows to specify signature of
   * method to which new {@link MethodInvocation} should be added. This is required for example for
   * {@link TitleAreaDialog} and its "title area" properties.
   */
  public void test_this_addMethodInvocation_thisSpecific_0() throws Exception {
    setFileContentSrc(
        "test/MyDialog.java",
        getTestSource(
            "public abstract class MyDialog extends JPanel {",
            "  public MyDialog() {",
            "    setLayout(new BorderLayout());",
            "    JPanel contentArea = new JPanel();",
            "    add(contentArea);",
            "    createDialogArea(contentArea);",
            "  }",
            "  protected abstract void createDialogArea(Container parent);",
            "}"));
    setFileContentSrc(
        "test/MyDialog.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='setEnabled'>",
            "      <parameter type='boolean'/>",
            "      <tag name='thisTargetMethod' value='createDialogArea(java.awt.Container)'/>",
            "    </method>",
            "  </methods>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyDialog {",
            "  public Test() {",
            "  }",
            "  protected void createDialogArea(Container parent) {",
            "  }",
            "}");
    panel.getPropertyByTitle("enabled").setValue(false);
    assertEditor(
        "public class Test extends MyDialog {",
        "  public Test() {",
        "  }",
        "  protected void createDialogArea(Container parent) {",
        "    setEnabled(false);",
        "  }",
        "}");
  }

  /**
   * Same as {@link #test_this_addMethodInvocation_thisSpecific_0()}, but first {@link Statement}
   * has {@link SuperMethodInvocation}.
   */
  public void test_this_addMethodInvocation_thisSpecific_1() throws Exception {
    setFileContentSrc(
        "test/MyDialog.java",
        getTestSource(
            "public abstract class MyDialog extends JPanel {",
            "  public MyDialog() {",
            "    setLayout(new BorderLayout());",
            "    JPanel contentArea = new JPanel();",
            "    add(contentArea);",
            "    createDialogArea(contentArea);",
            "  }",
            "  protected void createDialogArea(Container parent) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyDialog.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='setEnabled'>",
            "      <parameter type='boolean'/>",
            "      <tag name='thisTargetMethod' value='createDialogArea(java.awt.Container)'/>",
            "    </method>",
            "  </methods>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyDialog {",
            "  public Test() {",
            "  }",
            "  protected void createDialogArea(Container parent) {",
            "    super.createDialogArea(parent);",
            "    int value;",
            "  }",
            "}");
    panel.getPropertyByTitle("enabled").setValue(false);
    assertEditor(
        "public class Test extends MyDialog {",
        "  public Test() {",
        "  }",
        "  protected void createDialogArea(Container parent) {",
        "    super.createDialogArea(parent);",
        "    setEnabled(false);",
        "    int value;",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "afterCreation"
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_afterCreation() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends JButton {",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <method-order>",
            "    <default order='afterCreation'/>",
            "  </method-order>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      MyButton button = new MyButton();",
            "      button.setAutoscrolls(true);",
            "      add(button);",
            "      button.setAutoscrolls(true);",
            "    }",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // add invocation
    button.getPropertyByTitle("enabled").setValue(false);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      MyButton button = new MyButton();",
        "      button.setEnabled(false);",
        "      button.setAutoscrolls(true);",
        "      add(button);",
        "      button.setAutoscrolls(true);",
        "    }",
        "  }",
        "}");
    // check target
    {
      StatementTarget target = JavaInfoUtils.getTarget(button, null);
      assertTarget(target, null, getStatement(button, 4), false);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rules
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_rules_separateOrderForMethod() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends JButton {",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <method-order>",
            "    <method signature='setSelected(boolean)' order='beforeAssociation'/>",
            "    <method signature='setEnabled(boolean)' order='afterAssociation'/>",
            "  </method-order>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      MyButton button = new MyButton();",
            "      button.setAutoscrolls(true);",
            "      add(button);",
            "      button.setAutoscrolls(true);",
            "    }",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    {
      MethodDescription method = button.getDescription().getMethod("setSelected(boolean)");
      String orderClassName = method.getOrder().getClass().getName();
      assertThat(orderClassName).endsWith("MethodOrderBeforeAssociation");
    }
    {
      MethodDescription method = button.getDescription().getMethod("setEnabled(boolean)");
      String orderClassName = method.getOrder().getClass().getName();
      assertThat(orderClassName).endsWith("MethodOrderAfterAssociation");
    }
  }

  public void test_rules_severalMethods() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends JButton {",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <method-order>",
            "    <methods order='beforeAssociation'>",
            "      <s>setEnabled(boolean)</s>",
            "      <s>setSelected(boolean)</s>",
            "    </methods>",
            "    <methods order='afterAssociation'>",
            "      <s>setAutoscrolls(boolean)</s>",
            "    </methods>",
            "  </method-order>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      MyButton button = new MyButton();",
            "      button.setAutoscrolls(true);",
            "      add(button);",
            "      button.setAutoscrolls(true);",
            "    }",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // setEnabled() and setSelected() should be "beforeAssociation"
    {
      MethodDescription method = button.getDescription().getMethod("setEnabled(boolean)");
      String orderClassName = method.getOrder().getClass().getName();
      assertThat(orderClassName).endsWith("MethodOrderBeforeAssociation");
    }
    {
      MethodDescription method = button.getDescription().getMethod("setSelected(boolean)");
      String orderClassName = method.getOrder().getClass().getName();
      assertThat(orderClassName).endsWith("MethodOrderBeforeAssociation");
    }
    // setAutoscrolls() should be "afterAssociation"
    {
      MethodDescription method = button.getDescription().getMethod("setAutoscrolls(boolean)");
      String orderClassName = method.getOrder().getClass().getName();
      assertThat(orderClassName).endsWith("MethodOrderAfterAssociation");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "afterChild" (i.e. after last specified children)
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_afterChildren_invocations() throws Exception {
    configureProject_afterChildren();
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends ContainerPanel {",
            "  public Test() {",
            "  }",
            "}");
    {
      Property property = panel.getPropertyByTitle("property_2");
      assertNotNull(property);
      property.setValue(new Integer(7));
      assertEditor(
          "// filler filler filler",
          "public class Test extends ContainerPanel {",
          "  public Test() {",
          "    setProperty_2(7);",
          "  }",
          "}");
    }
    {
      Property property = panel.getPropertyByTitle("property_1");
      assertNotNull(property);
      property.setValue(new Integer(112));
      assertEditor(
          "// filler filler filler",
          "public class Test extends ContainerPanel {",
          "  public Test() {",
          "    setProperty_2(7);",
          "    setProperty_1(112);",
          "  }",
          "}");
    }
  }

  public void test_afterChildren_betweenAddedChildren() throws Exception {
    configureProject_afterChildren();
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends ContainerPanel {",
            "  public Test() {",
            "  }",
            "}");
    {
      // create new ItemPanel1
      JavaInfo item1 =
          JavaInfoUtils.createJavaInfo(
              panel.getEditor(),
              "test.ItemPanel1",
              new ConstructorCreationSupport());
      JavaInfoUtils.add(item1, null, panel, null);
      assertEditor(
          "// filler filler filler",
          "public class Test extends ContainerPanel {",
          "  public Test() {",
          "    {",
          "      ItemPanel1 itemPanel1 = new ItemPanel1(this);",
          "    }",
          "  }",
          "}");
    }
    {
      // create new ItemPanel2
      JavaInfo item2 =
          JavaInfoUtils.createJavaInfo(
              panel.getEditor(),
              "test.ItemPanel2",
              new ConstructorCreationSupport());
      JavaInfoUtils.add(item2, null, panel, null);
      assertEditor(
          "// filler filler filler",
          "public class Test extends ContainerPanel {",
          "  public Test() {",
          "    {",
          "      ItemPanel1 itemPanel1 = new ItemPanel1(this);",
          "    }",
          "    {",
          "      ItemPanel2 itemPanel2 = new ItemPanel2(this);",
          "    }",
          "  }",
          "}");
    }
    {
      Property property = panel.getPropertyByTitle("property_2");
      assertNotNull(property);
      property.setValue(new Integer(7));
      assertEditor(
          "// filler filler filler",
          "public class Test extends ContainerPanel {",
          "  public Test() {",
          "    {",
          "      ItemPanel1 itemPanel1 = new ItemPanel1(this);",
          "    }",
          "    setProperty_2(7);",
          "    {",
          "      ItemPanel2 itemPanel2 = new ItemPanel2(this);",
          "    }",
          "  }",
          "}");
    }
    {
      Property property = panel.getPropertyByTitle("property_1");
      assertNotNull(property);
      property.setValue(new Integer(112));
      assertEditor(
          "// filler filler filler",
          "public class Test extends ContainerPanel {",
          "  public Test() {",
          "    {",
          "      ItemPanel1 itemPanel1 = new ItemPanel1(this);",
          "    }",
          "    setProperty_2(7);",
          "    {",
          "      ItemPanel2 itemPanel2 = new ItemPanel2(this);",
          "    }",
          "    setProperty_1(112);",
          "  }",
          "}");
    }
  }

  public void test_afterChildren_betweenChildren() throws Exception {
    configureProject_afterChildren();
    ContainerInfo panel =
        parseContainer(
            "public class Test extends ContainerPanel {",
            "  public Test() {",
            "    {",
            "      ItemPanel1 itemPanel1 = new ItemPanel1(this);",
            "    }",
            "    {",
            "      ItemPanel2 itemPanel2 = new ItemPanel2(this);",
            "    }",
            "  }",
            "}");
    {
      Property property = panel.getPropertyByTitle("property_2");
      assertNotNull(property);
      property.setValue(new Integer(7));
      assertEditor(
          "public class Test extends ContainerPanel {",
          "  public Test() {",
          "    {",
          "      ItemPanel1 itemPanel1 = new ItemPanel1(this);",
          "    }",
          "    setProperty_2(7);",
          "    {",
          "      ItemPanel2 itemPanel2 = new ItemPanel2(this);",
          "    }",
          "  }",
          "}");
    }
    {
      Property property = panel.getPropertyByTitle("property_1");
      assertNotNull(property);
      property.setValue(new Integer(112));
      assertEditor(
          "public class Test extends ContainerPanel {",
          "  public Test() {",
          "    {",
          "      ItemPanel1 itemPanel1 = new ItemPanel1(this);",
          "    }",
          "    setProperty_2(7);",
          "    {",
          "      ItemPanel2 itemPanel2 = new ItemPanel2(this);",
          "    }",
          "    setProperty_1(112);",
          "  }",
          "}");
    }
  }

  public void test_afterChildren_addChild() throws Exception {
    configureProject_afterChildren();
    ContainerInfo panel =
        parseContainer(
            "public class Test extends ContainerPanel {",
            "  public Test() {",
            "    setProperty_2(7);",
            "    setProperty_1(112);",
            "  }",
            "}");
    {
      // create new ItemPanel2
      JavaInfo item2 =
          JavaInfoUtils.createJavaInfo(
              panel.getEditor(),
              "test.ItemPanel2",
              new ConstructorCreationSupport());
      JavaInfoUtils.add(item2, null, panel, null);
      assertEditor(
          "public class Test extends ContainerPanel {",
          "  public Test() {",
          "    setProperty_2(7);",
          "    {",
          "      ItemPanel2 itemPanel2 = new ItemPanel2(this);",
          "    }",
          "    setProperty_1(112);",
          "  }",
          "}");
    }
    {
      // create new ItemPanel1
      JavaInfo item1 =
          JavaInfoUtils.createJavaInfo(
              panel.getEditor(),
              "test.ItemPanel1",
              new ConstructorCreationSupport());
      JavaInfoUtils.add(item1, null, panel, null);
      assertEditor(
          "public class Test extends ContainerPanel {",
          "  public Test() {",
          "    {",
          "      ItemPanel1 itemPanel1 = new ItemPanel1(this);",
          "    }",
          "    setProperty_2(7);",
          "    {",
          "      ItemPanel2 itemPanel2 = new ItemPanel2(this);",
          "    }",
          "    setProperty_1(112);",
          "  }",
          "}");
    }
  }

  private void configureProject_afterChildren() throws Exception {
    {
      // ItemPanel1
      setJavaContentSrc("test", "ItemPanel1", new String[]{
          "public class ItemPanel1 extends JPanel {",
          "  public ItemPanel1(JPanel parent){",
          "    parent.add(this);",
          "  }",
          "}"}, new String[]{
          "<?xml version='1.0' encoding='UTF-8'?>",
          "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
          "  <creation>",
          "    <source><![CDATA[new test.ItemPanel1(%parent%)]]></source>",
          "  </creation>",
          "  <constructors>",
          "    <constructor>",
          "      <parameter type='javax.swing.JPanel' parent='true'/>",
          "    </constructor>",
          "  </constructors>",
          "</component>"});
    }
    {
      // ItemPanel2
      setJavaContentSrc("test", "ItemPanel2", new String[]{
          "public class ItemPanel2 extends JPanel {",
          "  public ItemPanel2(JPanel parent){",
          "    parent.add(this);",
          "  }",
          "}"}, new String[]{
          "<?xml version='1.0' encoding='UTF-8'?>",
          "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
          "  <creation>",
          "    <source><![CDATA[new test.ItemPanel2(%parent%)]]></source>",
          "  </creation>",
          "  <constructors>",
          "    <constructor>",
          "      <parameter type='javax.swing.JPanel' parent='true'/>",
          "    </constructor>",
          "  </constructors>",
          "</component>"});
    }
    {
      // ContainerPanel
      setJavaContentSrc("test", "ContainerPanel", new String[]{
          "public class ContainerPanel extends JPanel {",
          "  public void setProperty_1(int value){",
          "  }",
          "  public void setProperty_2(int value){",
          "  }",
          "}"}, new String[]{
          "<?xml version='1.0' encoding='UTF-8'?>",
          "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
          "  <method-order>",
          "    <method signature='setProperty_1(int)' order='last'/>",
          "    <method signature='setProperty_2(int)' order='afterChildren test.ItemPanel1'/>",
          "  </method-order>",
          "</component>"});
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "afterParentChild" (i.e. after parent last specified children)
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_afterParentChildren_invocation() throws Exception {
    configureProject_afterParentChildren();
    ContainerInfo panel =
        parseContainer(
            "public class Test extends ContainerPanel {",
            "  public Test() {",
            "    setProperty(new PropertyItem());",
            "    {",
            "      ItemPanel itemPanel = new ItemPanel(this);",
            "    }",
            "  }",
            "}");
    List<ComponentInfo> childrenComponents = panel.getChildrenComponents();
    // property item
    JavaInfo propertyItemInfo = childrenComponents.get(0);
    assertThat(propertyItemInfo.getDescription().getComponentClass().getCanonicalName()).isEqualTo(
        "test.PropertyItem");
    // panel item
    JavaInfo panelItemInfo = childrenComponents.get(1);
    assertThat(panelItemInfo).isInstanceOf(JPanelInfo.class);
    // change property
    Property property = propertyItemInfo.getPropertyByTitle("value");
    property.setValue(10);
    // check source
    assertEditor(
        "public class Test extends ContainerPanel {",
        "  public Test() {",
        "    PropertyItem propertyItem = new PropertyItem();",
        "    setProperty(propertyItem);",
        "    {",
        "      ItemPanel itemPanel = new ItemPanel(this);",
        "    }",
        "    propertyItem.setValue(10);",
        "  }",
        "}");
  }

  public void test_afterParentChildren_invocationAsLast() throws Exception {
    configureProject_afterParentChildren();
    ContainerInfo panel =
        parseContainer(
            "public class Test extends ContainerPanel {",
            "  public Test() {",
            "    {",
            "      ItemPanel itemPanel = new ItemPanel(this);",
            "    }",
            "    setProperty(new PropertyItem());",
            "  }",
            "}");
    List<ComponentInfo> childrenComponents = panel.getChildrenComponents();
    // panel item
    JavaInfo panelItemInfo = childrenComponents.get(0);
    assertThat(panelItemInfo).isInstanceOf(JPanelInfo.class);
    // property item
    JavaInfo propertyItemInfo = childrenComponents.get(1);
    assertThat(propertyItemInfo.getDescription().getComponentClass().getCanonicalName()).isEqualTo(
        "test.PropertyItem");
    // change property
    Property property = propertyItemInfo.getPropertyByTitle("value");
    property.setValue(50);
    // check source
    assertEditor(
        "public class Test extends ContainerPanel {",
        "  public Test() {",
        "    {",
        "      ItemPanel itemPanel = new ItemPanel(this);",
        "    }",
        "    PropertyItem propertyItem = new PropertyItem();",
        "    setProperty(propertyItem);",
        "    propertyItem.setValue(50);",
        "  }",
        "}");
  }

  public void test_afterParentChildren_addChild() throws Exception {
    configureProject_afterParentChildren();
    ContainerInfo panel =
        parseContainer(
            "public class Test extends ContainerPanel {",
            "  public Test() {",
            "    setProperty(new PropertyItem());",
            "  }",
            "}");
    List<ComponentInfo> childrenComponents = panel.getChildrenComponents();
    // property item
    JavaInfo propertyItemInfo = childrenComponents.get(0);
    assertThat(propertyItemInfo.getDescription().getComponentClass().getCanonicalName()).isEqualTo(
        "test.PropertyItem");
    // change property
    Property property = propertyItemInfo.getPropertyByTitle("value");
    property.setValue(100);
    // check source
    assertEditor(
        "public class Test extends ContainerPanel {",
        "  public Test() {",
        "    PropertyItem propertyItem = new PropertyItem();",
        "    setProperty(propertyItem);",
        "    propertyItem.setValue(100);",
        "  }",
        "}");
    // create new child panel item
    JavaInfo item =
        JavaInfoUtils.createJavaInfo(
            panel.getEditor(),
            "test.ItemPanel",
            new ConstructorCreationSupport());
    JavaInfoUtils.add(item, null, panel, null);
    // check source
    assertEditor(
        "public class Test extends ContainerPanel {",
        "  public Test() {",
        "    PropertyItem propertyItem = new PropertyItem();",
        "    setProperty(propertyItem);",
        "    {",
        "      ItemPanel itemPanel = new ItemPanel(this);",
        "    }",
        "    propertyItem.setValue(100);",
        "  }",
        "}");
  }

  public void test_afterParentChildren_invocationBlockFirst() throws Exception {
    configureProject_afterParentChildren();
    ContainerInfo panel =
        parseContainer(
            "public class Test extends ContainerPanel {",
            "  public Test() {",
            "    {",
            "      setProperty(new PropertyItem());",
            "    }",
            "    {",
            "      ItemPanel itemPanel = new ItemPanel(this);",
            "    }",
            "  }",
            "}");
    List<ComponentInfo> childrenComponents = panel.getChildrenComponents();
    // property item
    JavaInfo propertyInfo = childrenComponents.get(0);
    /*JavaInfo itemPanelInfo = */childrenComponents.get(1);
    propertyInfo.getPropertyByTitle("value").setValue(10);
    // check source
    assertEditor(
        "public class Test extends ContainerPanel {",
        "  private PropertyItem propertyItem;",
        "  public Test() {",
        "    {",
        "      propertyItem = new PropertyItem();",
        "      setProperty(propertyItem);",
        "    }",
        "    {",
        "      ItemPanel itemPanel = new ItemPanel(this);",
        "    }",
        "    propertyItem.setValue(10);",
        "  }",
        "}");
  }

  public void test_afterParentChildren_invocationBlockLast() throws Exception {
    configureProject_afterParentChildren();
    ContainerInfo panel =
        parseContainer(
            "public class Test extends ContainerPanel {",
            "  public Test() {",
            "    {",
            "      ItemPanel itemPanel = new ItemPanel(this);",
            "    }",
            "    {",
            "      setProperty(new PropertyItem());",
            "    }",
            "  }",
            "}");
    List<ComponentInfo> childrenComponents = panel.getChildrenComponents();
    // property item
    /*JavaInfo itemPanelInfo = */childrenComponents.get(0);
    JavaInfo propertyInfo = childrenComponents.get(1);
    propertyInfo.getPropertyByTitle("value").setValue(10);
    // check source
    assertEditor(
        "public class Test extends ContainerPanel {",
        "  public Test() {",
        "    {",
        "      ItemPanel itemPanel = new ItemPanel(this);",
        "    }",
        "    {",
        "      PropertyItem propertyItem = new PropertyItem();",
        "      setProperty(propertyItem);",
        "      propertyItem.setValue(10);",
        "    }",
        "  }",
        "}");
    // create new child
    JavaInfo item =
        JavaInfoUtils.createJavaInfo(
            panel.getEditor(),
            "test.ItemPanel",
            new ConstructorCreationSupport());
    JavaInfoUtils.add(item, null, panel, null);
    // check source
    assertEditor(
        "public class Test extends ContainerPanel {",
        "  public Test() {",
        "    {",
        "      ItemPanel itemPanel = new ItemPanel(this);",
        "    }",
        "    {",
        "      PropertyItem propertyItem = new PropertyItem();",
        "      setProperty(propertyItem);",
        "      {",
        "        ItemPanel itemPanel = new ItemPanel(this);",
        "      }",
        "      propertyItem.setValue(10);",
        "    }",
        "  }",
        "}");
  }

  private void configureProject_afterParentChildren() throws Exception {
    {
      // ItemPanel
      setJavaContentSrc("test", "ItemPanel", new String[]{
          "public class ItemPanel extends JPanel {",
          "  public ItemPanel(JPanel parent){",
          "    parent.add(this);",
          "  }",
          "}"}, new String[]{
          "<?xml version='1.0' encoding='UTF-8'?>",
          "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
          "  <creation>",
          "    <source><![CDATA[new test.ItemPanel(%parent%)]]></source>",
          "  </creation>",
          "  <constructors>",
          "    <constructor>",
          "      <parameter type='javax.swing.JPanel' parent='true'/>",
          "    </constructor>",
          "  </constructors>",
          "</component>"});
    }
    {
      // PropertyItem
      setJavaContentSrc("test", "PropertyItem", new String[]{
          "public class PropertyItem extends Component {",
          "  public PropertyItem(){",
          "  }",
          "  public void setValue(int value){",
          "  }",
          "}"}, new String[]{
          "<?xml version='1.0' encoding='UTF-8'?>",
          "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
          "  <method-order>",
          "    <method signature='setValue(int)' order='afterParentChildren test.ItemPanel'/>",
          "  </method-order>",
          "</component>"});
    }
    {
      // ContainerPanel
      setJavaContentSrc("test", "ContainerPanel", new String[]{
          "public class ContainerPanel extends JPanel {",
          "  public void setProperty(test.PropertyItem value){",
          "  }",
          "}"}, new String[]{
          "<?xml version='1.0' encoding='UTF-8'?>",
          "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
          "  <methods>",
          "    <method name='setProperty'>",
          "      <parameter type='test.PropertyItem' child='true'/>",
          "    </method>",
          "  </methods>",
          "</component>"});
    }
    waitForAutoBuild();
  }
}
