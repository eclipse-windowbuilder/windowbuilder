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
package org.eclipse.wb.tests.designer.swt.model.widgets;

import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.ComponentDescriptionKey;
import org.eclipse.wb.internal.core.model.description.ConstructorDescription;
import org.eclipse.wb.internal.core.model.description.CreationDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.property.editor.DisplayExpressionPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.style.StylePropertyEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.DescriptionProcessor;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.swt.widgets.Composite;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DescriptionProcessor} for SWT.
 * 
 * @author scheglov_ke
 */
public class DescriptionProcessorTest extends RcpModelTest {
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
   * Test that "parent" tag with value "false" disables automatic marking first
   * {@link ParameterDescription} as parent.
   */
  public void test_decriptionForCustomComponent_disableParent() throws Exception {
    setFileContentSrc(
        "test/MyComposite.java",
        getTestSource(
            "public class MyComposite extends Composite {",
            "  public MyComposite(Composite fakeParent, int style, Composite realParent) {",
            "    super(realParent, style);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyComposite.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <constructors>",
            "    <constructor>",
            "      <parameter type='org.eclipse.swt.widgets.Composite'>",
            "        <tag name='parent' value='false'/>",
            "      </parameter>",
            "      <parameter type='int'/>",
            "      <parameter type='org.eclipse.swt.widgets.Composite' parent='true'/>",
            "    </constructor>",
            "  </constructors>",
            "</component>"));
    waitForAutoBuild();
    // parse
    parseComposite(
        "// filler filler filler",
        "public class Test extends Shell {",
        "  public Test() {",
        "  }",
        "}");
    // prepare descriptions
    ComponentDescription description =
        ComponentDescriptionHelper.getDescription(m_lastEditor, "test.MyComposite");
    ConstructorDescription constructorDescription = description.getConstructors().get(0);
    // parameter[0] should NOT be parent
    {
      ParameterDescription parameter_0 = constructorDescription.getParameter(0);
      assertFalse(parameter_0.isParent());
    }
    // parameter[1] should be style
    {
      ParameterDescription styleParameter = constructorDescription.getParameter(1);
      assertThat(styleParameter.getEditor()).isInstanceOf(StylePropertyEditor.class);
    }
    // parameter[2] was marked as parent in description
    {
      ParameterDescription parameter_2 = constructorDescription.getParameter(2);
      assertTrue(parameter_2.isParent());
    }
  }

  /**
   * Constructor with {@link Composite} and "style" parameters.
   */
  public void test_decriptionForCustomComponent_parentStyle() throws Exception {
    setFileContentSrc(
        "test/MyComposite.java",
        getTestSource(
            "public class MyComposite extends Composite {",
            "  public MyComposite(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parseComposite(
        "// filler filler filler",
        "public class Test extends Shell {",
        "  public Test() {",
        "  }",
        "}");
    // prepare descriptions
    ComponentDescription description =
        ComponentDescriptionHelper.getDescription(m_lastEditor, "test.MyComposite");
    ConstructorDescription constructorDescription = description.getConstructors().get(0);
    // parameter[0] should be parent
    {
      ParameterDescription parameter_0 = constructorDescription.getParameter(0);
      assertTrue(parameter_0.isParent());
    }
    // parameter[1] should be style
    {
      ParameterDescription styleParameter = constructorDescription.getParameter(1);
      assertThat(styleParameter.getEditor()).isInstanceOf(StylePropertyEditor.class);
    }
  }

  /**
   * Constructor only with "parent" {@link Composite}.
   */
  public void test_decriptionForCustomComponent_onlyParent() throws Exception {
    setFileContentSrc(
        "test/MyComposite.java",
        getTestSource(
            "public class MyComposite extends Composite {",
            "  public MyComposite(Composite parent) {",
            "    super(parent, SWT.NONE);",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parseComposite(
        "// filler filler filler",
        "public class Test extends Shell {",
        "  public Test() {",
        "  }",
        "}");
    // prepare descriptions
    ComponentDescription description =
        ComponentDescriptionHelper.getDescription(m_lastEditor, "test.MyComposite");
    ConstructorDescription constructorDescription = description.getConstructors().get(0);
    // parameter[0] should be parent
    {
      ParameterDescription parameter_0 = constructorDescription.getParameter(0);
      assertTrue(parameter_0.isParent());
    }
  }

  /**
   * Test that {@link ComponentDescription} for custom component has "parent" flag in constructor,
   * and configured editor for "style" property.
   */
  public void test_decriptionForCustomComponent_1() throws Exception {
    setFileContentSrc(
        "test/MyComposite.java",
        getTestSource(
            "public class MyComposite extends Composite {",
            "  public MyComposite(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    new MyComposite(this, SWT.NONE);",
            "  }",
            "}");
    ControlInfo custom = shell.getChildrenControls().get(0);
    // check custom description
    ComponentDescription description = custom.getDescription();
    ConstructorDescription constructorDescription = description.getConstructors().get(0);
    // first parameter should be parent
    assertTrue(constructorDescription.getParameter(0).isParent());
    // second parameter should be style
    {
      ParameterDescription styleParameter = constructorDescription.getParameter(1);
      StylePropertyEditor editor = (StylePropertyEditor) styleParameter.getEditor();
      assertEquals("org.eclipse.swt.SWT", ReflectionUtils.getFieldObject(editor, "m_className"));
      assertEquals("org.eclipse.swt.SWT.NONE", styleParameter.getDefaultSource());
    }
  }

  /**
   * Parameter "style" should inherit default source from superclass.
   */
  public void test_decriptionForCustomComponent_2() throws Exception {
    setFileContentSrc(
        "test/MyComposite.java",
        getTestSource(
            "public class MyComposite extends Composite {",
            "  public MyComposite(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyComposite.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <constructors>",
            "    <constructor>",
            "      <parameter type='org.eclipse.swt.widgets.Composite'/>",
            "      <parameter type='int' defaultSource='org.eclipse.swt.SWT.BORDER'>",
            "        <editor id='style'>",
            "          <parameter name='class'>org.eclipse.swt.SWT</parameter>",
            "          <parameter name='set'>BORDER</parameter>",
            "        </editor>",
            "      </parameter>",
            "    </constructor>",
            "  </constructors>",
            "</component>"));
    setFileContentSrc(
        "test/MyComposite2.java",
        getTestSource(
            "public class MyComposite2 extends MyComposite {",
            "  public MyComposite2(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    new MyComposite2(this, SWT.NONE);",
            "  }",
            "}");
    ControlInfo composite = shell.getChildrenControls().get(0);
    ComponentDescription description = composite.getDescription();
    ConstructorDescription constructorDescription = description.getConstructors().get(0);
    ParameterDescription styleParameter = constructorDescription.getParameter(1);
    assertEquals("org.eclipse.swt.SWT.BORDER", styleParameter.getDefaultSource());
  }

  public void test_noStyleProperty_whenDisplayExpressionEditor() throws Exception {
    setFileContentSrc(
        "test/MyComposite.java",
        getTestSource(
            "public class MyComposite extends Composite {",
            "  public MyComposite(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyComposite.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <constructors>",
            "    <constructor>",
            "      <parameter type='org.eclipse.swt.widgets.Composite'/>",
            "      <parameter type='int'>",
            "        <editor id='displayExpression'/>",
            "      </parameter>",
            "    </constructor>",
            "  </constructors>",
            "</component>"));
    waitForAutoBuild();
    // parse
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    new MyComposite(this, SWT.NONE);",
            "  }",
            "}");
    ControlInfo composite = shell.getChildrenControls().get(0);
    // still DisplayExpressionPropertyEditor, not replaced by "style" from superclass
    {
      ComponentDescription description = composite.getDescription();
      ConstructorDescription constructorDescription = description.getConstructors().get(0);
      ParameterDescription styleParameter = constructorDescription.getParameter(1);
      assertSame(DisplayExpressionPropertyEditor.INSTANCE, styleParameter.getEditor());
    }
    // so, no top level "Style" property
    assertNull(composite.getPropertyByTitle("Style"));
  }

  public void test_forInterface() throws Exception {
    setFileContentSrc(
        "test/MyInterface.java",
        getSource("package test;", "public interface MyInterface {", "  // filler", "}"));
    waitForAutoBuild();
    // parse
    parseComposite(
        "// filler filler filler",
        "public class Test extends Shell {",
        "  public Test() {",
        "  }",
        "}");
    // ensure that process() does not throw any exception
    Class<?> myInterface = m_lastLoader.loadClass("test.MyInterface");
    ComponentDescription componentDescription =
        new ComponentDescription(new ComponentDescriptionKey(myInterface));
    DescriptionProcessor.INSTANCE.process(m_lastEditor, componentDescription);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Default CreationDescription
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_defaultCreation_onlyParent() throws Exception {
    String additionalParameters = "";
    String source = "new test.MyComposite(%parent%)";
    check_defaultCreation(additionalParameters, source);
  }

  public void test_defaultCreation_parentStyle() throws Exception {
    String additionalParameters = ", int style";
    String source = "new test.MyComposite(%parent%, org.eclipse.swt.SWT.NONE)";
    check_defaultCreation(additionalParameters, source);
  }

  public void test_defaultCreation_secondBoolean() throws Exception {
    String additionalParameters = ", boolean a";
    String source = "new test.MyComposite(%parent%, false)";
    check_defaultCreation(additionalParameters, source);
  }

  public void test_defaultCreation_secondDouble() throws Exception {
    String additionalParameters = ", double a";
    String source = "new test.MyComposite(%parent%, 0.0)";
    check_defaultCreation(additionalParameters, source);
  }

  public void test_defaultCreation_secondObject() throws Exception {
    String additionalParameters = ", Object a";
    String source = "new test.MyComposite(%parent%, (java.lang.Object) null)";
    check_defaultCreation(additionalParameters, source);
  }

  private void check_defaultCreation(String additionalParameters, String expected) throws Exception {
    setFileContentSrc(
        "test/MyComposite.java",
        getTestSource(
            "public class MyComposite extends Composite {",
            "  public MyComposite(Composite parent" + additionalParameters + ") {",
            "    super(parent, SWT.NONE);",
            "  }",
            "}"));
    waitForAutoBuild();
    // prepare context
    parseComposite(
        "// filler filler filler",
        "public class Test extends Shell {",
        "  public Test() {",
        "  }",
        "}");
    //
    ComponentDescription componentDescription =
        ComponentDescriptionHelper.getDescription(m_lastEditor, "test.MyComposite");
    CreationDescription creationDescription = componentDescription.getCreation(null);
    assertEquals(expected, creationDescription.getSource());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Style in BeanInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * We should support feature from D1: style description in BeanInfo.
   */
  public void test_styleBeanInfo() throws Exception {
    setFileContentSrc(
        "test/MyComposite.java",
        getTestSource(
            "public class MyComposite extends Composite {",
            "  public MyComposite(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyCompositeBeanInfo.java",
        getSourceDQ(
            "package test;",
            "import java.beans.*;",
            "public class MyCompositeBeanInfo extends SimpleBeanInfo {",
            "  private static final String SWT_STYLE = 'org.eclipse.wb.swt.style';",
            "  public BeanDescriptor getBeanDescriptor() {",
            "    BeanDescriptor luo_descriptor = new BeanDescriptor( MyComposite.class );",
            "    luo_descriptor.setValue(SWT_STYLE, new String[][]{",
            "      new String[]{'set', null, 'border', 'flat'},",
            "      new String[]{'select', 'type', 'push', null, 'push', 'check', 'radio'},",
            "    });",
            "    return luo_descriptor;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse for context
    parseComposite(
        "// filler filler filler",
        "public class Test extends Shell {",
        "  public Test() {",
        "  }",
        "}");
    //
    ComponentDescription componentDescription =
        ComponentDescriptionHelper.getDescription(m_lastEditor, "test.MyComposite");
    ConstructorDescription constructor = componentDescription.getConstructors().get(0);
    {
      ParameterDescription parameter = constructor.getParameter(0);
      assertTrue(parameter.isParent());
    }
    {
      ParameterDescription parameter = constructor.getParameter(1);
      assertSame(int.class, parameter.getType());
      StylePropertyEditor editor = (StylePropertyEditor) parameter.getEditor();
      assertEquals(
          getSource(
              "org.eclipse.swt.SWT",
              "  border boolean: BORDER",
              "  flat boolean: FLAT",
              "  type select: 0 PUSH CHECK RADIO"),
          editor.getAsString());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Other
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * We should automatically enable execution of <code>createX(Composite)</code> methods of super
   * class.
   */
  public void test_executeCreateMethods() throws Exception {
    setFileContentSrc(
        "test/MyComposite.java",
        getTestSource(
            "public class MyComposite extends Composite {",
            "  public MyComposite(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "  public void createFoo(Composite parent) {",
            "    setEnabled(false);",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    CompositeInfo composite =
        parseComposite(
            "public class Test extends MyComposite {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "    createFoo(this);",
            "  }",
            "}");
    composite.refresh();
    // if "createFoo()" was executed, then it changed "enabled"
    assertEquals(false, ReflectionUtils.invokeMethod(composite.getObject(), "isEnabled()"));
  }
}