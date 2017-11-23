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
package org.eclipse.wb.tests.designer.core.model.creation;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.creation.factory.StaticFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.description.CreationInvocationDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.description.factory.FactoryMethodDescription;
import org.eclipse.wb.internal.core.model.description.helpers.FactoryDescriptionHelper;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorWarning;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;
import org.eclipse.wb.tests.designer.tests.Activator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.swt.graphics.Image;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import javax.swing.JButton;

/**
 * Tests for {@link FactoryDescriptionHelper}.
 * 
 * @author scheglov_ke
 */
public class FactoryDescriptionHelperTest extends SwingModelTest {
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
  // Factory method checking
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link FactoryDescriptionHelper#isFactoryMethod(MethodDeclaration)}.
   */
  public void test_isFactoryMethod() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration(
            "test",
            "Test.java",
            getTestSource(
                "public class Test {",
                "  /**",
                "  * @wbp.factory",
                "  */",
                "  public static JButton createButton() {",
                "    return new JButton();",
                "  }",
                "  public static JButton createButton2() {",
                "    return new JButton();",
                "  }",
                "}"));
    MethodDeclaration[] methods = typeDeclaration.getMethods();
    assertTrue(FactoryDescriptionHelper.isFactoryMethod(methods[0]));
    assertFalse(FactoryDescriptionHelper.isFactoryMethod(methods[1]));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Descriptions
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Check that only "public static" methods with "@wbp.factory" tag are included.
   */
  public void test_descriptions_javaDoc() throws Exception {
    // prepare factory with one good and several bad methods
    setFileContentSrc(
        "test/StaticFactory_.java",
        getTestSource(
            "public final class StaticFactory_ {",
            "  /**",
            "  * @wbp.factory",
            "  */",
            "  public static JButton create_static_withFactory(String text) {",
            "    return new JButton(text);",
            "  }",
            "  public static JButton create_static_noFactory(String text) {",
            "    return new JButton(text);",
            "  }",
            "  /**",
            "  * @wbp.factory",
            "  */",
            "  public JButton create_notStatic_withFactory(String text) {",
            "    return new JButton(text);",
            "  }",
            "  /**",
            "  * @wbp.factory",
            "  */",
            "  private static JButton create_static_private_withFactory(String text) {",
            "    return new JButton(text);",
            "  }",
            "}"));
    waitForAutoBuild();
    // prepare model context
    parseContainer(
        "// filler filler filler",
        "public final class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // check static factory descriptions
    Map<String, FactoryMethodDescription> descriptionsMap =
        getDescriptionsMap("test.StaticFactory_", true);
    assertThat(descriptionsMap).hasSize(1);
    assertTrue(descriptionsMap.containsKey("create_static_withFactory(java.lang.String)"));
    // check for parameter name
    FactoryMethodDescription description = descriptionsMap.values().iterator().next();
    assertEquals("text", description.getParameter(0).getName());
  }

  /**
   * Check for "@wbp.factory.parameter.source" tag.
   */
  public void test_descriptions_javaDoc_defaultSource() throws Exception {
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  /**",
            "  * @wbp.factory.parameter.source text 'my text'",
            "  */",
            "  public static JButton create_1(String text) {",
            "    return new JButton(text);",
            "  }",
            "  public static JButton create_2(String text) {",
            "    return new JButton(text);",
            "  }",
            "}"));
    waitForAutoBuild();
    // prepare model context
    parseContainer(
        "// filler filler filler",
        "public final class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // check static factory descriptions
    Map<String, FactoryMethodDescription> descriptionsMap =
        getDescriptionsMap("test.StaticFactory", true);
    assertThat(descriptionsMap).hasSize(2);
    // create_1
    {
      FactoryMethodDescription description = descriptionsMap.get("create_1(java.lang.String)");
      assertEquals("text", description.getParameter(0).getName());
      assertEquals("\"my text\"", description.getParameter(0).getDefaultSource());
    }
    // create_2
    {
      FactoryMethodDescription description = descriptionsMap.get("create_2(java.lang.String)");
      assertEquals("text", description.getParameter(0).getName());
      assertEquals("(java.lang.String) null", description.getParameter(0).getDefaultSource());
    }
  }

  /**
   * Check that parameter with name "parent" automatically marked as parent.
   */
  public void test_descriptions_javaDoc_parent() throws Exception {
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  public static JButton createButton(Container parent) {",
            "    JButton button = new JButton();",
            "    parent.add(button);",
            "    return button;",
            "  }",
            "}"));
    waitForAutoBuild();
    // prepare model context
    parseContainer(
        "// filler filler filler",
        "public final class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // check description
    FactoryMethodDescription description =
        getDescription("test.StaticFactory", "createButton(java.awt.Container)", true);
    {
      ParameterDescription parameter = description.getParameter(0);
      assertEquals("parent", parameter.getName());
      assertTrue(parameter.isParent());
    }
  }

  /**
   * Check that "Factory" suffix in class name makes all "public static" methods a factory methods.
   */
  public void test_descriptions_suffixFactory() throws Exception {
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  public static JButton create_1(String text) {",
            "    return new JButton(text);",
            "  }",
            "  public static JButton create_2(String text) {",
            "    return new JButton(text);",
            "  }",
            "}"));
    waitForAutoBuild();
    // prepare model context
    parseContainer(
        "// filler filler filler",
        "public final class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // check static factory descriptions
    Map<String, FactoryMethodDescription> descriptionsMap =
        getDescriptionsMap("test.StaticFactory", true);
    assertThat(descriptionsMap).hasSize(2);
    assertTrue(descriptionsMap.containsKey("create_1(java.lang.String)"));
    assertTrue(descriptionsMap.containsKey("create_2(java.lang.String)"));
  }

  /**
   * Check that single "@wbp.factory" tag for class makes all "public static" methods a factory
   * methods.
   */
  public void test_descriptions_javaDoc_forClass() throws Exception {
    // prepare factory
    setFileContentSrc(
        "test/StaticFactory_.java",
        getTestSource(
            "/**",
            "* @wbp.factory",
            "*/",
            "public final class StaticFactory_ {",
            "  public static JButton create_1(String text) {",
            "    return new JButton(text);",
            "  }",
            "  public static JButton create_2(String text) {",
            "    return new JButton(text);",
            "  }",
            "}"));
    waitForAutoBuild();
    // prepare model context
    parseContainer(
        "// filler filler filler",
        "public final class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // check static factory descriptions
    Map<String, FactoryMethodDescription> descriptionsMap =
        getDescriptionsMap("test.StaticFactory_", true);
    assertThat(descriptionsMap).hasSize(2);
    assertTrue(descriptionsMap.containsKey("create_1(java.lang.String)"));
    assertTrue(descriptionsMap.containsKey("create_2(java.lang.String)"));
  }

  /**
   * Check that "void" methods are ignored.
   */
  public void test_descriptions_javaDoc_forClass_withSetters() throws Exception {
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  public static void setText(String text) {",
            "  }",
            "  public static String getText() {",
            "    return null;",
            "  }",
            "}"));
    waitForAutoBuild();
    // prepare model context
    parseContainer(
        "// filler filler filler",
        "public final class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // check static factory descriptions
    Map<String, FactoryMethodDescription> descriptionsMap =
        getDescriptionsMap("test.StaticFactory", true);
    assertThat(descriptionsMap).isEmpty();
  }

  /**
   * Check that parameters are automatically bound to the properties by name/title.
   */
  public void test_descriptions_javaDoc_autoBinding() throws Exception {
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  public static JButton createButton(String text, Icon icon, int noSuchProperty) {",
            "    return new JButton(text);",
            "  }",
            "}"));
    waitForAutoBuild();
    // prepare model context
    parseContainer(
        "// filler filler filler",
        "public final class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // prepare factory description
    FactoryMethodDescription description =
        getDescription(
            "test.StaticFactory",
            "createButton(java.lang.String,javax.swing.Icon,int)",
            true);
    // check that two parameters are bound and third is not bound
    assertEquals("setText(java.lang.String)", description.getParameter(0).getProperty());
    assertEquals("setIcon(javax.swing.Icon)", description.getParameter(1).getProperty());
    assertNull(description.getParameter(2).getProperty());
  }

  /**
   * Check that with "@wbp.factory.parameters.noBinding" no parameters auto-binding done.
   */
  public void test_descriptions_javaDoc_noBinding() throws Exception {
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  /**",
            "  * @wbp.factory.parameters.noBinding",
            "  */",
            "  public static JButton createButton(String text) {",
            "    return new JButton(text);",
            "  }",
            "}"));
    waitForAutoBuild();
    // prepare model context
    parseContainer(
        "// filler filler filler",
        "public final class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // prepare factory descriptions
    FactoryMethodDescription description =
        getDescription("test.StaticFactory", "createButton(java.lang.String)", true);
    // check that parameter is not bound
    assertNull(description.getParameter(0).getProperty());
  }

  /**
   * Check for factory methods described in XML.
   */
  public void test_descriptions_XML() throws Exception {
    // prepare factory
    setFileContentSrc(
        "test/StaticFactory_.java",
        getTestSource(
            "public final class StaticFactory_ {",
            "  public static JButton create_xml(String text) {",
            "    return new JButton(text);",
            "  }",
            "  public static JButton create_noFactory(String text) {",
            "    return new JButton(text);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/StaticFactory_.wbp-factory.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<factory>",
            "  <method name='create_xml'>",
            "    <parameter type='java.lang.String' property='setText(java.lang.String)'/>",
            "  </method>",
            "</factory>"));
    waitForAutoBuild();
    // prepare model context
    parseContainer(
        "// filler filler filler",
        "public final class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // check static factory descriptions
    Map<String, FactoryMethodDescription> descriptionsMap =
        getDescriptionsMap("test.StaticFactory_", true);
    assertThat(descriptionsMap).hasSize(1);
    assertTrue(descriptionsMap.containsKey("create_xml(java.lang.String)"));
    // check for parameter name
    FactoryMethodDescription description = descriptionsMap.values().iterator().next();
    assertEquals("text", description.getParameter(0).getName());
  }

  /**
   * Factory has instance methods, described in XML, but we ask static methods, so empty map.
   */
  public void test_descriptions_XML_instanceMethods_askStatic() throws Exception {
    // prepare factory
    setFileContentSrc(
        "test/InstanceFactory.java",
        getTestSource(
            "public final class InstanceFactory {",
            "  public JButton createButton() {",
            "    return new JButton();",
            "  }",
            "}"));
    setFileContentSrc(
        "test/InstanceFactory.wbp-factory.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<factory>",
            "  <method name='createButton'/>",
            "</factory>"));
    waitForAutoBuild();
    // prepare model context
    parseContainer(
        "// filler filler filler",
        "public final class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // check static factory descriptions, no entries expected
    Map<String, FactoryMethodDescription> descriptionsMap =
        getDescriptionsMap("test.InstanceFactory", true);
    assertThat(descriptionsMap).isEmpty();
  }

  /**
   * Factory has static methods, described in XML, but we ask instance methods, so empty map.
   */
  public void test_descriptions_XML_staticMethods_askInstance() throws Exception {
    // prepare factory
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  public static JButton createButton() {",
            "    return new JButton();",
            "  }",
            "}"));
    setFileContentSrc(
        "test/StaticFactory.wbp-factory.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<factory>",
            "  <method name='createButton'/>",
            "</factory>"));
    waitForAutoBuild();
    // prepare model context
    parseContainer(
        "// filler filler filler",
        "public final class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // check instance factory descriptions, no entries expected
    Map<String, FactoryMethodDescription> descriptionsMap =
        getDescriptionsMap("test.StaticFactory", false);
    assertThat(descriptionsMap).isEmpty();
  }

  /**
   * If method is described in as factory in some class, it is also factory in sub-classes.
   */
  public void test_descriptions_XML_instanceFactory_inheritance() throws Exception {
    setFileContentSrc(
        "test/SuperToolkit.java",
        getTestSource(
            "public class SuperToolkit {",
            "  public JButton createButton() {",
            "    return new JButton();",
            "  }",
            "}"));
    setFileContentSrc(
        "test/SuperToolkit.wbp-factory.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<factory>",
            "  <method name='createButton'/>",
            "</factory>"));
    setFileContentSrc(
        "test/SubToolkit.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public final class SubToolkit extends SuperToolkit {",
            "  // filler",
            "}"));
    waitForAutoBuild();
    // prepare context
    parseContainer(
        "// filler filler filler",
        "public final class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // createButton() is factory method
    {
      Map<String, FactoryMethodDescription> descriptionsMap =
          getDescriptionsMap("test.SubToolkit", false);
      assertThat(descriptionsMap).hasSize(1);
      assertThat(descriptionsMap.keySet()).contains("createButton()");
    }
  }

  /**
   * Check for factory methods described in XML, with plain text "description" for method.
   */
  public void test_descriptions_XML_textualDescription_plainText() throws Exception {
    // prepare factory
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  public static JButton createButton() {",
            "    return new JButton();",
            "  }",
            "}"));
    setFileContentSrc(
        "test/StaticFactory.wbp-factory.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<factory>",
            "  <method name='createButton'>",
            "    <description>Some textual description of method.</description>",
            "  </method>",
            "</factory>"));
    waitForAutoBuild();
    // prepare model context
    parseContainer(
        "// filler filler filler",
        "public final class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // get FactoryMethodDescription
    FactoryMethodDescription description =
        getDescription("test.StaticFactory", "createButton()", true);
    assertEquals("Some textual description of method.", description.getDescription());
  }

  /**
   * Check for factory methods described in XML, with HTML "description" for method.
   */
  public void test_descriptions_XML_textualDescription_HTML_1() throws Exception {
    // prepare factory
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  public static JButton createButton() {",
            "    return new JButton();",
            "  }",
            "}"));
    setFileContentSrc(
        "test/StaticFactory.wbp-factory.xml",
        getSource(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<factory>",
            "  <method name='createButton'>",
            "    <description>Some <b>HTML</b> description.</description>",
            "  </method>",
            "</factory>"));
    waitForAutoBuild();
    // prepare model context
    parseContainer(
        "// filler filler filler",
        "public final class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // get FactoryMethodDescription
    FactoryMethodDescription description =
        getDescription("test.StaticFactory", "createButton()", true);
    assertEquals("Some <b>HTML</b> description.", description.getDescription());
  }

  /**
   * Check for factory methods described in XML, with HTML "description" for method.
   */
  public void test_descriptions_XML_textualDescription_HTML_2() throws Exception {
    // prepare factory
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  public static JButton createButton() {",
            "    return new JButton();",
            "  }",
            "}"));
    setFileContentSrc(
        "test/StaticFactory.wbp-factory.xml",
        getSource(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<factory>",
            "  <method name='createButton'>",
            "    <description>First. <p attr='value'/> Second.</description>",
            "  </method>",
            "</factory>"));
    waitForAutoBuild();
    // prepare model context
    parseContainer(
        "// filler filler filler",
        "public final class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // get FactoryMethodDescription
    FactoryMethodDescription description =
        getDescription("test.StaticFactory", "createButton()", true);
    assertEquals("First. <p attr='value'/> Second.", description.getDescription());
  }

  /**
   * Check for factory methods described in XML, with HTML "description" for method.
   * <p>
   * We should support "entities", in decimal and hexadecimal forms.
   */
  public void test_descriptions_XML_textualDescription_HTML_3() throws Exception {
    // prepare factory
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  public static JButton createButton() {",
            "    return new JButton();",
            "  }",
            "}"));
    setFileContentSrc(
        "test/StaticFactory.wbp-factory.xml",
        getSource(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<factory>",
            "  <method name='createButton'>",
            "    <description>&#48;&#x31;</description>",
            "  </method>",
            "</factory>"));
    waitForAutoBuild();
    // prepare model context
    parseContainer(
        "// filler filler filler",
        "public final class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // get FactoryMethodDescription
    FactoryMethodDescription description =
        getDescription("test.StaticFactory", "createButton()", true);
    assertEquals("&#48;&#x31;", description.getDescription());
  }

  /**
   * Check for factory methods described in XML, with text "name" for method.
   */
  public void test_descriptions_XML_presentationName() throws Exception {
    // prepare factory
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  public static JButton createButton() {",
            "    return new JButton();",
            "  }",
            "}"));
    setFileContentSrc(
        "test/StaticFactory.wbp-factory.xml",
        getSource(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<factory>",
            "  <method name='createButton'>",
            "    <name>Some name.</name>",
            "  </method>",
            "</factory>"));
    waitForAutoBuild();
    // prepare model context
    parseContainer(
        "// filler filler filler",
        "public final class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // get FactoryMethodDescription
    FactoryMethodDescription description =
        getDescription("test.StaticFactory", "createButton()", true);
    assertEquals("Some name.", description.getPresentationName());
  }

  /**
   * Check for factory methods described in XML, with text "parameter" tags for method.
   */
  public void test_descriptions_XML_parameters() throws Exception {
    // prepare factory
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  public static JButton createButton() {",
            "    return new JButton();",
            "  }",
            "}"));
    setFileContentSrc(
        "test/StaticFactory.wbp-factory.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<factory>",
            "  <method name='createButton'>",
            "    <parameters>",
            "      <parameter name='parameter.1'>some value</parameter>",
            "    </parameters>",
            "  </method>",
            "</factory>"));
    waitForAutoBuild();
    // prepare model context
    parseContainer(
        "// filler filler filler",
        "public final class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // get FactoryMethodDescription
    FactoryMethodDescription description =
        getDescription("test.StaticFactory", "createButton()", true);
    assertEquals("some value", description.getParameter("parameter.1"));
    assertNull(description.getParameter("noSuchParameter"));
  }

  /**
   * Check for cached descriptions.
   */
  public void test_descriptions_cached() throws Exception {
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  public static JButton createButton(String text) {",
            "    return new JButton(text);",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse source
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    add(StaticFactory.createButton('button 1'));",
            "    add(StaticFactory.createButton('button 2'));",
            "  }",
            "}");
    assertThat(panel.getChildrenComponents()).hasSize(2);
    // check that descriptions are same, i.e. cached
    ComponentInfo button_1 = panel.getChildrenComponents().get(0);
    ComponentInfo button_2 = panel.getChildrenComponents().get(1);
    StaticFactoryCreationSupport creation_1 =
        (StaticFactoryCreationSupport) button_1.getCreationSupport();
    StaticFactoryCreationSupport creation_2 =
        (StaticFactoryCreationSupport) button_2.getCreationSupport();
    assertSame(creation_1.getDescription(), creation_2.getDescription());
  }

  /**
   * We don't support member classes as factories.
   */
  public void test_descriptions_memberClass() throws Exception {
    setFileContentSrc(
        "test/SomeObject.java",
        getTestSource(
            "public class SomeObject {",
            "  SomeObject() {",
            "  }",
            "  public static class StaticFactory {",
            "    public static JButton createButton() {",
            "      return new JButton();",
            "    }",
            "  }",
            "}"));
    waitForAutoBuild();
    String[] lines = {"public class Test extends JPanel {", "  public Test() {", "  }", "}"};
    // prepare context
    parseContainer(lines);
    // get factories
    Map<String, FactoryMethodDescription> descriptionsMap =
        getDescriptionsMap("test.SomeObject$StaticFactory", true);
    assertThat(descriptionsMap).isEmpty();
  }

  /**
   * If something, in this case {@link ClassNotFoundException}, causes exception during loading
   * factory, we should just ignore it.
   */
  public void test_descriptions_exceptionInJavaInternals() throws Exception {
    // create MyFactory that references not existing class
    setFileContentSrc(
        "test/NoSuchClass.java",
        getSourceDQ("package test;", "public class NoSuchClass {", "}"));
    setFileContentSrc(
        "test/MyFactory.java",
        getSourceDQ(
            "package test;",
            "public class MyFactory {",
            "  public static NoSuchClass getBad() {",
            "    return null;",
            "  }",
            "}"));
    waitForAutoBuild();
    getFile("bin/test/NoSuchClass.class").delete(true, null);
    String[] lines = {"public class Test extends JPanel {", "  public Test() {", "  }", "}"};
    // prepare context
    parseContainer(lines);
    // get factories, causes exception, so no descriptions
    Map<String, FactoryMethodDescription> descriptionsMap =
        getDescriptionsMap0("test.MyFactory", true);
    assertThat(descriptionsMap).isEmpty();
    // exception logged as warning
    {
      List<EditorWarning> warnings = m_lastState.getWarnings();
      assertThat(warnings).hasSize(1);
      EditorWarning warning = warnings.get(0);
      Throwable exception = warning.getException();
      Throwable rootException = DesignerExceptionUtils.getRootCause(exception);
      assertThat(rootException).isExactlyInstanceOf(ClassNotFoundException.class);
    }
  }

  /**
   * Check for parameters binding using index.
   */
  public void test_descriptions_bindByIndex() throws Exception {
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  /**",
            "  * @wbp.factory.parameters.noBinding",
            "  * @wbp.factory.parameter.property 0 text",
            "  */",
            "  public static JButton createButton(String text) {",
            "    return new JButton(text);",
            "  }",
            "}"));
    waitForAutoBuild();
    // prepare model context
    ContainerInfo panel =
        parseContainer(
            "public final class Test extends JPanel {",
            "  Test() {",
            "    add(StaticFactory.createButton(null));",
            "  }",
            "}");
    assertThat(panel.getChildrenComponents()).hasSize(1);
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // check for property binding
    FactoryMethodDescription description =
        ((StaticFactoryCreationSupport) button.getCreationSupport()).getDescription();
    assertEquals("setText(java.lang.String)", description.getParameter(0).getProperty());
  }

  /**
   * Check for parameters binding using index, but with invalid value.
   */
  public void test_descriptions_bindByIndex_invalid() throws Exception {
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  /**",
            "  * @wbp.factory.parameter.property -1 text",
            "  */",
            "  public static JButton createButton(String text) {",
            "    return new JButton(text);",
            "  }",
            "}"));
    waitForAutoBuild();
    // bad factory, so it is ignored
    parseContainer(
        "public final class Test extends JPanel {",
        "  Test() {",
        "    add(StaticFactory.createButton(null));",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(StaticFactory.createButton(null))/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
  }

  /**
   * Check for parameters binding using name, but with invalid value.
   */
  public void test_descriptions_bindByName_invalid() throws Exception {
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  /**",
            "  * @wbp.factory.parameter.property noSuchParameter text",
            "  */",
            "  public static JButton createButton(String text) {",
            "    return new JButton(text);",
            "  }",
            "}"));
    waitForAutoBuild();
    // bad factory, so it is ignored
    parseContainer(
        "public final class Test extends JPanel {",
        "  Test() {",
        "    add(StaticFactory.createButton(null));",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(StaticFactory.createButton(null))/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
  }

  /**
   * Check that for instance factory both - static and instance methods are considered as factories.
   * <p>
   * Kosta.20080407: I consider this not correct anymore, so disable this test.
   */
  public void _test_descriptionsInstanceStatic() throws Exception {
    setFileContentSrc(
        "test/InstanceFactory.java",
        getTestSource(
            "public final class InstanceFactory {",
            "  public static JButton create_static(String text) {",
            "    return new JButton(text);",
            "  }",
            "  public JButton create_instance(String text) {",
            "    return new JButton(text);",
            "  }",
            "}"));
    waitForAutoBuild();
    // prepare model context
    parseContainer(
        "// filler filler filler",
        "public final class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // check factory descriptions
    String factoryClassName = "test.InstanceFactory";
    Map<String, FactoryMethodDescription> descriptionsMap =
        getDescriptionsMap(factoryClassName, false);
    assertThat(descriptionsMap).hasSize(2);
    assertTrue(descriptionsMap.containsKey("create_static(java.lang.String)"));
    assertTrue(descriptionsMap.containsKey("create_instance(java.lang.String)"));
    // check that instance factory not considered as static
    {
      FactoryMethodDescription description =
          getDescription(factoryClassName, "create_instance(java.lang.String)", true);
      assertNull(description);
    }
  }

  /**
   * Test icons in {@link FactoryMethodDescription}.
   */
  public void test_descriptions_icon() throws Exception {
    // prepare factory
    {
      setFileContentSrc(
          "test/StaticFactory.java",
          getTestSource(
              "public final class StaticFactory {",
              "  public static JButton createButton(String text) {",
              "    return new JButton(text);",
              "  }",
              "  public static JButton createButton() {",
              "    return new JButton();",
              "  }",
              "}"));
      // create icon for "createButton(java.lang.String)"
      {
        IFile iconFile = getFileSrc("test", "StaticFactory.createButton_java.lang.String_.png");
        iconFile.create(Activator.getFile("icons/test.png"), true, null);
      }
      // build
      waitForAutoBuild();
    }
    // prepare model context
    parseContainer(
        "// filler filler filler",
        "public final class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    Class<?> factoryClass = m_lastLoader.loadClass("test.StaticFactory");
    // check "createButton()"
    {
      FactoryMethodDescription factoryDescription =
          getDescription("test.StaticFactory", "createButton()", true);
      assertTrue(factoryDescription.isFactory());
      assertSame(factoryClass, factoryDescription.getDeclaringClass());
      assertSame(JButton.class, factoryDescription.getReturnClass());
      assertNull(factoryDescription.getIcon());
    }
    // check "createButton(java.lang.String)"
    Image factoryMethodIcon;
    {
      FactoryMethodDescription factoryDescription =
          getDescription("test.StaticFactory", "createButton(java.lang.String)", true);
      assertTrue(factoryDescription.isFactory());
      assertSame(factoryClass, factoryDescription.getDeclaringClass());
      assertSame(JButton.class, factoryDescription.getReturnClass());
      // icon
      factoryMethodIcon = factoryDescription.getIcon();
      assertNotNull(factoryMethodIcon);
      assertFalse(factoryMethodIcon.isDisposed());
      assertTrue(UiUtils.equals(factoryMethodIcon, Activator.getImage("test.png")));
    }
    // here we checked for disposing icon, but now it is disposed when description GC'ed, so skip this
  }

  /**
   * Test for {@link FactoryMethodDescription#getInvocations()}.
   */
  public void test_descriptions_getInvocations() throws Exception {
    // prepare factory
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  public static JButton createButton() {",
            "    return new JButton();",
            "  }",
            "}"));
    setFileContentSrc(
        "test/StaticFactory.wbp-factory.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<factory>",
            "  <method name='createButton'>",
            "    <invocation signature='setText(java.lang.String)'><![CDATA['Static Button']]></invocation>",
            "  </method>",
            "</factory>"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // check description
    FactoryMethodDescription factoryDescription =
        getDescription("test.StaticFactory", "createButton()", true);
    {
      List<CreationInvocationDescription> invocations = factoryDescription.getInvocations();
      assertThat(invocations).hasSize(1);
      CreationInvocationDescription invocationDescription = invocations.get(0);
      assertEquals("setText(java.lang.String)", invocationDescription.getSignature());
      assertEquals("\"Static Button\"", invocationDescription.getArguments());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getFactoryUnits()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the first {@link ICompilationUnit} from
   *         {@link FactoryDescriptionHelper#getFactoryUnits(AstEditor, IPackageFragment)}, or
   *         <code>null</code> if no factory units.
   */
  private ICompilationUnit getFactoryUnit(JavaInfo component) throws Exception {
    List<ICompilationUnit> factoryUnits = getFactoryUnits();
    return !factoryUnits.isEmpty() ? factoryUnits.get(0) : null;
  }

  /**
   * @return the {@link ICompilationUnit}'s from
   *         {@link FactoryDescriptionHelper#getFactoryUnits(AstEditor, IPackageFragment)}, may be
   *         empty, but not <code>null</code>.
   */
  private List<ICompilationUnit> getFactoryUnits() throws Exception {
    IPackageFragment currentPackage = (IPackageFragment) m_lastEditor.getModelUnit().getParent();
    return FactoryDescriptionHelper.getFactoryUnits(m_lastEditor, currentPackage);
  }

  /**
   * No factory units.
   */
  public void test_getFactoryUnits_noUnits() throws Exception {
    m_waitForAutoBuild = true;
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertNull(getFactoryUnit(panel));
  }

  /**
   * Test for case when {@link ICompilationUnit#findPrimaryType()} return <code>null</code> when no
   * primary type, i.e. type with name of unit.
   */
  public void test_getFactoryUnits_noPrimaryType() throws Exception {
    setFileContentSrc("test/NoPrimaryType.java", getSourceDQ("package test;", "class Foo {", "}"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    assertThat(getFactoryUnits()).isEmpty();
  }

  /**
   * We have factory class, however it is not marked with tag or <code>*.wbp-factory.xml</code>.
   */
  public void test_getFactoryUnits_noTagOrDescription_notFactorySuffix() throws Exception {
    setFileContentSrc(
        "test/StaticFactory_.java",
        getTestSource(
            "public final class StaticFactory_ {",
            "  public static JButton createButton(String text) {",
            "    return new JButton(text);",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse, just for context
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertFalse(FactoryDescriptionHelper.isFactoryClass(m_lastEditor, "test.StaticFactory_"));
    assertNull(getFactoryUnit(panel));
  }

  /**
   * We have factory class, that is not marked with tag or <code>*.wbp-factory.xml</code>. However
   * if has suffix "Factory" , so considered as factory.
   */
  public void test_getFactoryUnits_noTagOrDescription_hasFactorySuffix() throws Exception {
    ICompilationUnit factoryUnit =
        createModelCompilationUnit(
            "test",
            "StaticFactory.java",
            getTestSource(
                "public final class StaticFactory {",
                "  public static JButton createButton(String text) {",
                "    return new JButton(text);",
                "  }",
                "}"));
    waitForAutoBuild();
    // parse, just for context
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertTrue(FactoryDescriptionHelper.isFactoryClass(m_lastEditor, "test.StaticFactory"));
    assertEquals(factoryUnit, getFactoryUnit(panel));
  }

  /**
   * Test for factory with <code>@wbp.factory</code> in source.
   */
  public void test_getFactoryUnits_tag() throws Exception {
    ICompilationUnit factoryUnit =
        createModelCompilationUnit(
            "test",
            "StaticFactory_.java",
            getTestSource(
                "public final class StaticFactory_ {",
                "  /**",
                "  * @wbp.factory",
                "  */",
                "  public static JButton createButton(String text) {",
                "    return new JButton(text);",
                "  }",
                "}"));
    waitForAutoBuild();
    // parse, just for context
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertTrue(FactoryDescriptionHelper.isFactoryClass(m_lastEditor, "test.StaticFactory_"));
    assertEquals(factoryUnit, getFactoryUnit(panel));
  }

  /**
   * Test for factory with <code>@wbp.factory</code> in source, but not active.
   */
  public void test_getFactoryUnits_tagInComment() throws Exception {
    setFileContentSrc(
        "test/StaticFactory_.java",
        getTestSource(
            "public final class StaticFactory_ {",
            "  public static JButton createButton(String text) {",
            "    // @wbp.factory  -  here this tag means nothing",
            "    return new JButton(text);",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse, just for context
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertFalse(FactoryDescriptionHelper.isFactoryClass(m_lastEditor, "test.StaticFactory_"));
    assertNull(getFactoryUnit(panel));
  }

  /**
   * We have factory class and <code>*.wbp-factory.xml</code>.
   */
  public void test_getFactoryUnits_description() throws Exception {
    ICompilationUnit factoryUnit =
        createModelCompilationUnit(
            "test",
            "StaticFactory_.java",
            getTestSource(
                "public final class StaticFactory_ {",
                "  public static JButton createButton(String text) {",
                "    return new JButton(text);",
                "  }",
                "}"));
    setFileContentSrc(
        "test/StaticFactory_.wbp-factory.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<factory>",
            "  <method name='createButton'>",
            "    <parameter type='java.lang.String'/>",
            "  </method>",
            "</factory>"));
    waitForAutoBuild();
    // parse, just for context
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertTrue(FactoryDescriptionHelper.isFactoryClass(m_lastEditor, "test.StaticFactory_"));
    assertEquals(factoryUnit, getFactoryUnit(panel));
  }

  /**
   * We have factory class and <code>*.wbp-factory.xml</code>, but not factory methods.
   */
  public void test_getFactoryUnits_descriptionNoMethods() throws Exception {
    setFileContentSrc(
        "test/StaticFactory_.java",
        getTestSource(
            "public final class StaticFactory_ {",
            "  public static JButton createButton(String text) {",
            "    return new JButton(text);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/StaticFactory_.wbp-factory.xml",
        getSourceDQ("<?xml version='1.0' encoding='UTF-8'?>", "<factory>", "</factory>"));
    waitForAutoBuild();
    // parse, just for context
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertFalse(FactoryDescriptionHelper.isFactoryClass(m_lastEditor, "test.StaticFactory_"));
    assertNull(getFactoryUnit(panel));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // isFactoryInvocation()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link FactoryDescriptionHelper#isFactoryInvocation(AstEditor, MethodInvocation)}.
   */
  public void test_isFactoryInvocation_static() throws Exception {
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "/**",
            "* @wbp.factory",
            "*/",
            "public final class StaticFactory {",
            "  public static JButton createButton() {",
            "    return new JButton();",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "public final class Test extends JPanel {",
        "  public Test() {",
        "    System.out.println(-1);",
        "    add(StaticFactory.createButton());",
        "  }",
        "}");
    {
      MethodInvocation invocation = (MethodInvocation) m_lastEditor.getEnclosingNode(".println");
      assertFalse(FactoryDescriptionHelper.isFactoryInvocation(m_lastEditor, invocation));
    }
    {
      MethodInvocation invocation =
          (MethodInvocation) m_lastEditor.getEnclosingNode(".createButton");
      assertTrue(FactoryDescriptionHelper.isFactoryInvocation(m_lastEditor, invocation));
    }
  }

  /**
   * Test for {@link FactoryDescriptionHelper#isFactoryInvocation(AstEditor, MethodInvocation)}.
   */
  public void test_isFactoryInvocation_instance() throws Exception {
    setFileContentSrc(
        "test/InstanceFactory.java",
        getTestSource(
            "/**",
            "* @wbp.factory",
            "*/",
            "public final class InstanceFactory {",
            "  public JButton createButton() {",
            "    return new JButton();",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "public final class Test extends JPanel {",
        "  private final InstanceFactory factory = new InstanceFactory();",
        "  public Test() {",
        "    System.out.println(-1);",
        "    add(factory.createButton());",
        "  }",
        "}");
    {
      MethodInvocation invocation = (MethodInvocation) m_lastEditor.getEnclosingNode(".println");
      assertFalse(FactoryDescriptionHelper.isFactoryInvocation(m_lastEditor, invocation));
    }
    {
      MethodInvocation invocation =
          (MethodInvocation) m_lastEditor.getEnclosingNode(".createButton");
      assertTrue(FactoryDescriptionHelper.isFactoryInvocation(m_lastEditor, invocation));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // isFactory()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Description by default.
   */
  public void test_isFactory_descriptions_default_1() throws Exception {
    // prepare factory
    setFileContentSrc(
        "test/TestFactory.java",
        getTestSource(
            "public final class TestFactory {",
            "  public static TestFactory create() {",
            "    return new TestFactory();",
            "  }",
            "  public JButton createButton() {",
            "    return new JButton();",
            "  }",
            "  public Object createNo(Object value) {",
            "    return value;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/TestFactory.wbp-factory.xml",
        getSourceDQ("<?xml version='1.0' encoding='UTF-8'?>", "<factory>", "</factory>"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // check descriptions
    assertThat(getDescriptionsMap("test.TestFactory", true).size()).isEqualTo(1);
    assertThat(getDescriptionsMap("test.TestFactory", false).size()).isEqualTo(2);
    // for 'create()'
    {
      FactoryMethodDescription factoryDescription =
          getDescription("test.TestFactory", "create()", true);
      assertThat(factoryDescription.isFactory()).isTrue();
    }
    // for 'createButton()'
    {
      FactoryMethodDescription factoryDescription =
          getDescription("test.TestFactory", "createButton()", false);
      assertThat(factoryDescription.isFactory()).isTrue();
    }
    // for 'setProperty()'
    {
      FactoryMethodDescription factoryDescription =
          getDescription("test.TestFactory", "createNo(java.lang.Object)", false);
      assertThat(factoryDescription.isFactory()).isTrue();
    }
  }

  public void test_isFactory_descriptions_default_2() throws Exception {
    // prepare factory
    setFileContentSrc(
        "test/TestFactory_.java",
        getTestSource(
            "public final class TestFactory_ {",
            "  public static TestFactory_ create() {",
            "    return new TestFactory_();",
            "  }",
            "  public JButton createButton() {",
            "    return new JButton();",
            "  }",
            "  public Object createNo(Object value) {",
            "    return value;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/TestFactory_.wbp-factory.xml",
        getSourceDQ("<?xml version='1.0' encoding='UTF-8'?>", "<factory>", "</factory>"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // for 'create()'
    {
      FactoryMethodDescription factoryDescription =
          getDescription("test.TestFactory_", "create()", true);
      assertThat(factoryDescription).isNull();
    }
    // for 'createButton()'
    {
      FactoryMethodDescription factoryDescription =
          getDescription("test.TestFactory_", "createButton()", false);
      assertThat(factoryDescription).isNull();
    }
    // for 'setProperty()'
    {
      FactoryMethodDescription factoryDescription =
          getDescription("test.TestFactory_", "createNo(java.lang.Object)", false);
      assertThat(factoryDescription).isNull();
    }
  }

  /**
   * Description with use 'factory=false'.
   */
  public void test_isFactory_descriptions_1() throws Exception {
    // prepare factory
    setFileContentSrc(
        "test/TestFactory.java",
        getTestSource(
            "public final class TestFactory {",
            "  public static TestFactory create() {",
            "    return new TestFactory();",
            "  }",
            "  public JButton createButton() {",
            "    return new JButton();",
            "  }",
            "  public Object createNo(Object value) {",
            "    return value;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/TestFactory.wbp-factory.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<factory>",
            "  <method name='createNo' factory='false'>",
            "    <parameter type='java.lang.Object'/>",
            "  </method>",
            "</factory>"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // for 'create()'
    {
      FactoryMethodDescription factoryDescription =
          getDescription("test.TestFactory", "create()", true);
      assertThat(factoryDescription.isFactory()).isTrue();
    }
    // for 'createButton()'
    {
      FactoryMethodDescription factoryDescription =
          getDescription("test.TestFactory", "createButton()", false);
      assertThat(factoryDescription.isFactory()).isTrue();
    }
    // for 'setProperty()'
    {
      FactoryMethodDescription factoryDescription =
          getDescription("test.TestFactory", "createNo(java.lang.Object)", false);
      assertThat(factoryDescription).isNull();
    }
  }

  /**
   * Description with use 'allMethodsAreFactories'.
   */
  public void test_isFactory_descriptions_2() throws Exception {
    // prepare factory
    setFileContentSrc(
        "test/TestFactory.java",
        getTestSource(
            "public final class TestFactory {",
            "  public static TestFactory create() {",
            "    return new TestFactory();",
            "  }",
            "  public JButton createButton() {",
            "    return new JButton();",
            "  }",
            "  public Object createNo(Object value) {",
            "    return value;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/TestFactory.wbp-factory.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<factory>",
            "  <allMethodsAreFactories>false</allMethodsAreFactories>",
            "</factory>"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // for 'create()'
    {
      FactoryMethodDescription factoryDescription =
          getDescription("test.TestFactory", "create()", true);
      assertThat(factoryDescription).isNull();
    }
    // for 'createButton()'
    {
      FactoryMethodDescription factoryDescription =
          getDescription("test.TestFactory", "createButton()", false);
      assertThat(factoryDescription).isNull();
    }
    // for 'setProperty()'
    {
      FactoryMethodDescription factoryDescription =
          getDescription("test.TestFactory", "createNo(java.lang.Object)", false);
      assertThat(factoryDescription).isNull();
    }
  }

  /**
   * Description with various options.
   */
  public void test_isFactory_descriptions_mixed() throws Exception {
    // prepare factory
    setFileContentSrc(
        "test/TestFactory.java",
        getTestSource(
            "public final class TestFactory {",
            "  public static TestFactory create() {",
            "    return new TestFactory();",
            "  }",
            "  public JButton createButton() {",
            "    return new JButton();",
            "  }",
            "  public Object createNo(Object value) {",
            "    return value;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/TestFactory.wbp-factory.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<factory>",
            "  <allMethodsAreFactories>false</allMethodsAreFactories>",
            "  <method name='create' factory='true'>",
            "  </method>",
            "  <method name='createButton' factory='true'>",
            "  </method>",
            "</factory>"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // for 'create()'
    {
      FactoryMethodDescription factoryDescription =
          getDescription("test.TestFactory", "create()", true);
      assertThat(factoryDescription.isFactory()).isTrue();
    }
    // for 'createButton()'
    {
      FactoryMethodDescription factoryDescription =
          getDescription("test.TestFactory", "createButton()", false);
      assertThat(factoryDescription.isFactory()).isTrue();
    }
    // for 'setProperty()'
    {
      FactoryMethodDescription factoryDescription =
          getDescription("test.TestFactory", "createNo(java.lang.Object)", false);
      assertThat(factoryDescription).isNull();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link FactoryMethodDescription}'s, fails if errors or warnings.
   */
  public Map<String, FactoryMethodDescription> getDescriptionsMap(String factoryClassName,
      boolean forStatic) throws Exception {
    Map<String, FactoryMethodDescription> descriptions =
        getDescriptionsMap0(factoryClassName, forStatic);
    assertNoErrors(m_lastParseInfo);
    return descriptions;
  }

  /**
   * @return the {@link FactoryMethodDescription}'s, may be some warnings are logged.
   */
  private Map<String, FactoryMethodDescription> getDescriptionsMap0(String factoryClassName,
      boolean forStatic) throws Exception {
    Class<?> factoryClass = m_lastLoader.loadClass(factoryClassName);
    return FactoryDescriptionHelper.getDescriptionsMap(m_lastEditor, factoryClass, forStatic);
  }

  /**
   * @return single {@link FactoryMethodDescription}.
   */
  private FactoryMethodDescription getDescription(String factoryClassName,
      String signature,
      boolean forStatic) throws Exception {
    Class<?> factoryClass = m_lastLoader.loadClass(factoryClassName);
    return FactoryDescriptionHelper.getDescription(m_lastEditor, factoryClass, signature, forStatic);
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // GC
  //
  ////////////////////////////////////////////////////////////////////////////
  /*public void test_waitGC() throws Exception {
  	for (int i = 0; i < 10; i++) {
  		System.gc();
  		Thread.sleep(300);
  	}
  	//Thread.sleep(1000 * 100000);
  }*/
}
