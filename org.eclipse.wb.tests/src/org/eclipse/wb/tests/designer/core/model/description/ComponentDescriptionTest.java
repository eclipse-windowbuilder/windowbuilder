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
package org.eclipse.wb.tests.designer.core.model.description;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.ToolkitProvider;
import org.eclipse.wb.internal.core.model.creation.ThisCreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.ConstructorDescription;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ComponentDescriptionHelper}, {@link ComponentDescription}, etc.
 * 
 * @author scheglov_ke
 * @author sablin_aa
 */
public class ComponentDescriptionTest extends SwingModelTest {
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
  /**
   * Test {@link ComponentDescription} of empty object.
   */
  public void test_getDescription_empty() throws Exception {
    setFileContentSrc(
        "test/MyObject.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <methods-exclude signature='setEnabled(boolean)'/>",
            "  </methods>",
            "</component>"));
    setFileContentSrc(
        "test/MyObject.java",
        getSourceDQ(
            "// filler filler filler filler",
            "package test;",
            "public class MyObject {",
            "  public MyObject() {",
            "  }",
            "}"));
    waitForAutoBuild();
    initDesigner();
    // load description
    Class<?> myClass = m_lastLoader.loadClass("test.MyObject");
    ComponentDescription description =
        ComponentDescriptionHelper.getDescription(m_lastEditor, myClass);
    assertFalse(description.isCached());
    assertSame(ToolkitProvider.DESCRIPTION, description.getToolkit());
    assertSame(myClass, description.getComponentClass());
    assertSame(JavaInfo.class, description.getModelClass());
    // toString() should provide at least names of component/model classes
    {
      String string = description.toString();
      assertTrue(string.contains("test.MyObject"));
      assertTrue(string.contains("org.eclipse.wb.core.model.JavaInfo"));
    }
    // same description provided during ASTEditor session
    assertSame(description, ComponentDescriptionHelper.getDescription(m_lastEditor, myClass));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tags
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_methodTag() throws Exception {
    setFileContentSrc(
        "test/MyObject.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='setValue'>",
            "      <parameter type='int'/>",
            "      <tag name='tagName' value='tagValue'/>",
            "    </method>",
            "  </methods>",
            "</component>"));
    setFileContentSrc(
        "test/MyObject.java",
        getSourceDQ(
            "package test;",
            "public class MyObject {",
            "  public void setValue(int value) {",
            "  }",
            "}"));
    waitForAutoBuild();
    initDesigner();
    // load description
    ComponentDescription componentDescription =
        ComponentDescriptionHelper.getDescription(m_lastEditor, "test.MyObject");
    // check method
    MethodDescription methodDescription = componentDescription.getMethod("setValue(int)");
    assertEquals("tagValue", methodDescription.getTag("tagName"));
    assertNull(methodDescription.getTag("no-such-tag"));
  }

  public void test_parameterTag() throws Exception {
    setFileContentSrc(
        "test/MyObject.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <constructors>",
            "    <constructor>",
            "      <parameter type='int'>",
            "        <tag name='tagName' value='tagValue'/>",
            "      </parameter>",
            "    </constructor>",
            "  </constructors>",
            "</component>"));
    setFileContentSrc(
        "test/MyObject.java",
        getSourceDQ(
            "package test;",
            "public class MyObject {",
            "  public MyObject(int value) {",
            "  }",
            "}"));
    waitForAutoBuild();
    initDesigner();
    // load description
    ComponentDescription componentDescription =
        ComponentDescriptionHelper.getDescription(m_lastEditor, "test.MyObject");
    // check constructor
    ConstructorDescription constructorDescription = componentDescription.getConstructors().get(0);
    assertNull(constructorDescription.getTag("tagName"));
    // check constructor's parameter
    ParameterDescription parameterDescription = constructorDescription.getParameter(0);
    assertEquals("tagValue", parameterDescription.getTag("tagName"));
    assertNull(parameterDescription.getTag("no-such-tag"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Evaluation
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_methodEvaluation() throws Exception {
    // MyButton
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends Button {",
            "  public void badMethod(int value) {",
            "    throw new Error();",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='badMethod' executable='false'>",
            "      <parameter type='int'/>",
            "    </method>",
            "  </methods>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    MyButton button = new MyButton();",
            "    button.badMethod(0);",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parameters
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ComponentDescription#hasTrueParameter(String)}.
   */
  public void test_hasTrueParameter() throws Exception {
    setFileContentSrc(
        "test/MyObject.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyObject {",
            "  // filler",
            "}"));
    setFileContentSrc(
        "test/MyObject.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='parameter_1'>true</parameter>",
            "    <parameter name='parameter_2'>false</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    initDesigner();
    //
    ComponentDescription description =
        ComponentDescriptionHelper.getDescription(m_lastEditor, "test.MyObject");
    assertThat(description.hasTrueParameter("parameter_1")).isTrue();
    assertThat(description.hasTrueParameter("parameter_2")).isFalse();
    assertThat(description.hasTrueParameter("parameter_3")).isFalse();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Generics
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_genericConstructorDescription() throws Exception {
    // MyPanel
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <constructors>",
            "    <constructor>",
            "      <parameter type='java.lang.String'/>",
            "      <parameter type='javax.swing.JComponent'/>",
            "    </constructor>",
            "  </constructors>",
            "</component>"));
    setFileContentSrc(
        "test/MyPanel.java",
        getSourceDQ(
            "package test;",
            "import javax.swing.JComponent;",
            "import javax.swing.JPanel;",
            "public class MyPanel extends JPanel {",
            "  public <T extends JComponent> MyPanel(String string, T value) {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    super('test', new JButton());",
            "  }",
            "}");
    // load description
    ComponentDescription componentDescription =
        ComponentDescriptionHelper.getDescription(m_lastEditor, "test.MyPanel");
    // check by signature
    {
      assertNotNull(componentDescription.getConstructor("<init>(java.lang.String,javax.swing.JComponent)"));
      assertNull(componentDescription.getConstructor("<init>(java.lang.Object,javax.swing.JButton)"));
    }
    // check by binding
    {
      SuperConstructorInvocation superConstructorInvocation =
          ((ThisCreationSupport) panel.getCreationSupport()).getInvocation();
      IMethodBinding binding = AstNodeUtils.getSuperBinding(superConstructorInvocation);
      assertNotNull(componentDescription.getConstructor(binding));
      assertNull(componentDescription.getConstructor(AstNodeUtils.getMethodSignature(binding)));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private void initDesigner() throws Exception {
    if (m_testProject != null) {
      parseContainer(
          "// filler filler filler",
          "public class Test extends JPanel {",
          "  public Test() {",
          "  }",
          "}");
    }
  }
}
