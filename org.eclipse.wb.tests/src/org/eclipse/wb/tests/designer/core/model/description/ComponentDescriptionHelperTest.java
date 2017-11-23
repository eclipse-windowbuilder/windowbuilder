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
import org.eclipse.wb.core.model.association.UnknownAssociation;
import org.eclipse.wb.internal.core.model.creation.ExposedPropertyCreationSupport;
import org.eclipse.wb.internal.core.model.creation.MethodParameterCreationSupport;
import org.eclipse.wb.internal.core.model.description.AbstractDescription;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.description.helpers.DescriptionHelper;
import org.eclipse.wb.internal.core.model.description.rules.StandardBeanPropertiesRule;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.variable.MethodParameterVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.swing.SwingToolkitDescription;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.core.TestBundle;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.swt.graphics.Image;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;

import org.assertj.core.api.Assertions;
import org.osgi.framework.Bundle;
import org.xml.sax.SAXParseException;

import java.awt.Container;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Tests for {@link ComponentDescriptionHelper}.
 *
 * @author scheglov_ke
 */
public class ComponentDescriptionHelperTest extends SwingModelTest {
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
  // ComponentDescriptionHelper
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * For some reason description for {@link Object} is not marked as cached, so we reload it, and
   * icon for it, each time. However there is bug, so icon become disposed. We test this here.
   */
  public void test_objectIcon() throws Exception {
    setFileContentSrc(
        "test/MyObject.java",
        getSourceDQ("package test;", "public class MyObject {", "}"));
    waitForAutoBuild();
    // step: 1
    {
      JavaInfo panel =
          parseSource(
              "test",
              "Test_1.java",
              getTestSource(
                  "public class Test_1 extends JPanel {",
                  "  public Test_1() {",
                  "  }",
                  "}"));
      // check description
      {
        ComponentDescription description =
            ComponentDescriptionHelper.getDescription(m_lastEditor, Object.class);
        Image icon = description.getIcon();
        assertFalse(icon.isDisposed());
      }
      // refresh and dispose
      try {
        panel.refresh();
      } finally {
        panel.refresh_dispose();
        panel.getBroadcastObject().dispose();
      }
    }
    // step: 2
    {
      JavaInfo panel =
          parseSource(
              "test",
              "Test_2.java",
              getTestSource(
                  "public class Test_2 extends JPanel {",
                  "  public Test_2() {",
                  "  }",
                  "}"));
      // check description
      {
        ComponentDescription description =
            ComponentDescriptionHelper.getDescription(m_lastEditor, Object.class);
        Image icon = description.getIcon();
        assertFalse(icon.isDisposed());
      }
      // refresh and dispose
      try {
        panel.refresh();
      } finally {
        panel.refresh_dispose();
        panel.getBroadcastObject().dispose();
      }
    }
  }

  public void test_iconsForInterfaces() throws Exception {
    setFileContentSrc(
        "test/IComponent.java",
        getSourceDQ("package test;", "public interface IComponent {", "}"));
    setFileContentSrc(
        "test/IComponent.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <toolkit id='org.eclipse.wb.swing'/>",
            "  <model class='org.eclipse.wb.core.model.JavaInfo'/>",
            "</component>"));
    TestUtils.createImagePNG(m_testProject, "src/test/IComponent.png", 1, 1);
    //
    setFileContentSrc(
        "test/Component.java",
        getSourceDQ("package test;", "public class Component implements IComponent {", "}"));
    TestUtils.createImagePNG(m_testProject, "src/test/Component.png", 2, 2);
    //
    setFileContentSrc(
        "test/Component_2.java",
        getSourceDQ("package test;", "public class Component_2 implements IComponent {", "}"));
    //
    setFileContentSrc(
        "test/MyComponent_1.java",
        getSourceDQ("package test;", "public class MyComponent_1 extends Component {", "}"));
    TestUtils.createImagePNG(m_testProject, "src/test/MyComponent_1.png", 3, 3);
    //
    setFileContentSrc(
        "test/MyComponent_2.java",
        getSourceDQ("package test;", "public class MyComponent_2 extends Component {", "}"));
    waitForAutoBuild();
    // parse for context
    parseContainer(
        "// filler filler filler",
        "public final class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // IComponent: 1x1 icon
    {
      ComponentDescription description =
          ComponentDescriptionHelper.getDescription(m_lastEditor, "test.IComponent");
      assertThat(description.getIcon().getBounds().width).isEqualTo(1);
    }
    // Component: 2x2 icon
    {
      ComponentDescription description =
          ComponentDescriptionHelper.getDescription(m_lastEditor, "test.Component");
      assertThat(description.getIcon().getBounds().width).isEqualTo(2);
    }
    // Component_2: no special icon, but it implements IComponent, so 1x1 icon
    {
      ComponentDescription description =
          ComponentDescriptionHelper.getDescription(m_lastEditor, "test.Component_2");
      assertThat(description.getIcon().getBounds().width).isEqualTo(1);
    }
    // MyComponent_1: special 3x3 icon
    {
      ComponentDescription description =
          ComponentDescriptionHelper.getDescription(m_lastEditor, "test.MyComponent_1");
      assertThat(description.getIcon().getBounds().width).isEqualTo(3);
    }
    // MyComponent_2: no special icon, so use 2x2 from Component
    {
      ComponentDescription description =
          ComponentDescriptionHelper.getDescription(m_lastEditor, "test.MyComponent_2");
      assertThat(description.getIcon().getBounds().width).isEqualTo(2);
    }
  }

  /**
   * Test that access of property editor with unknown id throws exception.
   */
  public void test_unknow_propertyEditor() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends JPanel {",
            "  public void setQ(int q) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <property id='setQ(int)'>",
            "    <editor id='no-such-editor'/>",
            "  </property>",
            "</component>"));
    waitForAutoBuild();
    //
    try {
      parseContainer(
          "// filler filler filler",
          "public class Test extends MyPanel {",
          "  public Test() {",
          "  }",
          "}");
      fail();
    } catch (DesignerException e) {
      assertInstanceOf(IllegalArgumentException.class, DesignerExceptionUtils.getRootCause(e));
    }
  }

  public void test_BeanInfo_icon() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends JPanel {",
            "}"));
    setFileContentSrc(
        "test/MyPanelBeanInfo.java",
        getTestSource(
            "import java.awt.image.*;",
            "public class MyPanelBeanInfo extends java.beans.SimpleBeanInfo {",
            "  public Image getIcon(int iconKind) {",
            "    return new BufferedImage(10, 15, BufferedImage.TYPE_INT_RGB);",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    // check ComponentDescription
    ComponentDescription description = panel.getDescription();
    Assertions.assertThat(description.getBeanInfo()).isNotNull();
    Image icon = description.getIcon();
    assertEquals(10, icon.getBounds().width);
    assertEquals(15, icon.getBounds().height);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Caching
  //
  ////////////////////////////////////////////////////////////////////////////
  public static class NotCachedButton extends JButton {
    private static final long serialVersionUID = 0L;
  }

  /**
   * Test {@link ComponentDescription}'s are cached.
   * <p>
   * Here we know, that {@link JPanel} should be cached, but our {@link JButton} subclass - not.
   */
  public void test_cachedComponentDescriptions() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends JButton {",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new MyButton());",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // check caching
    assertThat(panel.getDescription().isCached()).isTrue();
    assertThat(button.getDescription().isCached()).isFalse();
  }

  /**
   * Test {@link ComponentDescription}'s are cached.
   * <p>
   * If component has <code>dontCacheDescription</code> parameter with <code>true</code> value, then
   * it should not be cached.
   * <p>
   * We use separate {@link Bundle} because only components that described in {@link Bundle} may be
   * cached. No big reason - just such implementation done. Practically components from project are
   * always reloaded, so have different {@link Class} and can not be cached.
   */
  public void test_cachedComponentDescriptions_noCacheParameter() throws Exception {
    TestBundle testBundle = new TestBundle();
    try {
      String className = NotCachedButton.class.getName();
      String descriptionsPath =
          "wbp-meta/" + CodeUtils.getPackage(className).replace('.', '/') + "/";
      testBundle.addClass(NotCachedButton.class);
      testBundle.setFile(
          descriptionsPath + ".wbp-cache-descriptions",
          "Please, cache this package.");
      testBundle.setFile(
          descriptionsPath + CodeUtils.getShortClass(className) + ".wbp-component.xml",
          getSourceDQ(
              "<?xml version='1.0' encoding='UTF-8'?>",
              "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
              "  <parameters>",
              "    <parameter name='dontCacheDescription'>true</parameter>",
              "  </parameters>",
              "</component>"));
      testBundle.addExtension("org.eclipse.wb.core.toolkits", new String[]{
          "<toolkit id='org.eclipse.wb.swing'>",
          "  <classLoader-bundle bundle='" + testBundle.getId() + "'/>",
          "</toolkit>"});
      testBundle.install(true);
      try {
        parseContainer(
            "public class Test extends JFrame {",
            "  public Test() {",
            "      // filler",
            "  }",
            "}");
        // load description for "test2.MyButton"
        ComponentDescription description =
            ComponentDescriptionHelper.getDescription(m_lastEditor, className);
        assertFalse(description.isCached());
      } finally {
        testBundle.uninstall();
      }
    } finally {
      testBundle.dispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation caching
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * We should cache presentations for standard Swing components.
   */
  public void test_presentationCaching_use_forStandardComponents() throws Exception {
    // parse for context
    String[] lines =
        {
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}"};
    parseContainer(lines);
    // prepare component description
    ComponentDescription description =
        ComponentDescriptionHelper.getDescription(m_lastEditor, "javax.swing.JButton");
    assertTrue(description.isPresentationCached());
  }

  /**
   * If icon is in project, then user can change it, so we should not cache presentation.
   */
  public void test_presentationCaching_disable_forComponentInProject() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends JButton {",
            "  public MyButton() {",
            "  }",
            "}"));
    TestUtils.createImagePNG(m_testProject, "src/test/MyButton.png", 10, 10);
    waitForAutoBuild();
    String[] lines =
        {
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}"};
    // parse for context
    parseContainer(lines);
    // prepare component description
    ComponentDescription description =
        ComponentDescriptionHelper.getDescription(m_lastEditor, "test.MyButton");
    assertFalse(description.isPresentationCached());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Sources
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that *.wbp-component.xml file can be loaded from "wbp-meta" of {@link IProject}.
   */
  public void test_source_fromProjectMeta() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "// filler filler filler",
            "public class MyPanel extends JPanel {",
            "}"));
    setFileContent(
        "wbp-meta/test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <description>My description</description>",
            "  <parameters>",
            "    <parameter name='parameter_1'>AAA</parameter>",
            "  </parameters>",
            "</component>"));
    createImagePNG("wbp-meta/test/MyPanel.png", 10, 10);
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    ComponentDescription description = panel.getDescription();
    // check parameter to validate that description was loaded
    assertEquals("AAA", description.getParameter("parameter_1"));
    // check description
    assertEquals("My description", description.getDescription());
    // check icon
    {
      Image icon = description.getIcon();
      assertThat(icon.getBounds().width).isEqualTo(10);
      assertThat(icon.getBounds().height).isEqualTo(10);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ComponentDescription.getDescription()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for textual description, i.e. method {@link ComponentDescription#getDescription()}.
   */
  public void test_textualDescription_plainText() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "// filler filler filler",
            "public class MyPanel extends JPanel {",
            "}"));
    setFileContent(
        "wbp-meta/test/MyPanel.wbp-component.xml",
        getSource(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <description>My description</description>",
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
    //
    String description = panel.getDescription().getDescription();
    assertEquals("My description", description);
  }

  /**
   * Test for textual description, i.e. method {@link ComponentDescription#getDescription()}.
   * <p>
   * We should be able to use XHTML in description.
   */
  public void test_textualDescription_HTML() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "// filler filler filler",
            "public class MyPanel extends JPanel {",
            "}"));
    setFileContent(
        "wbp-meta/test/MyPanel.wbp-component.xml",
        getSource(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <description>My <b>cool</b> description</description>",
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
    //
    String description = panel.getDescription().getDescription();
    assertEquals("My <b>cool</b> description", description);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Property: default value
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_defaultPropertyValue_true() throws Exception {
    assert_defaultPropertyValue("boolean", "true", Boolean.TRUE);
  }

  public void test_defaultPropertyValue_false() throws Exception {
    assert_defaultPropertyValue("boolean", "false", Boolean.FALSE);
  }

  /**
   * Positive integer value.
   */
  public void test_defaultPropertyValue_intPositive() throws Exception {
    assert_defaultPropertyValue("int", "10", 10);
  }

  /**
   * Negative integer value.
   */
  public void test_defaultPropertyValue_intNegative() throws Exception {
    assert_defaultPropertyValue("int", "-10", -10);
  }

  /**
   * Integer expression.
   */
  public void test_defaultPropertyValue_intExpression() throws Exception {
    assert_defaultPropertyValue("int", "1 + 2 * 3", 7);
  }

  /**
   * Static field access.
   */
  public void test_defaultPropertyValue_intStaticField() throws Exception {
    assert_defaultPropertyValue(
        "int",
        "javax.swing.SwingConstants.RIGHT",
        javax.swing.SwingConstants.RIGHT);
  }

  /**
   * String value.
   */
  public void test_defaultPropertyValue_stringValue() throws Exception {
    assert_defaultPropertyValue("java.lang.String", "\"str\"", "str");
  }

  /**
   * Object value, two times to check that correct {@link ClassLoader} is used.
   */
  public void test_defaultPropertyValue_objectValue() throws Exception {
    setFileContentSrc(
        "test/Position.java",
        getSourceDQ(
            "package test;",
            "public class Position {",
            "  public static Position LEFT = new Position('left');",
            "  public static Position RIGHT = new Position('right');",
            "  public static Position CENTER = new Position('center');",
            "  private String position;",
            "  private Position(String position) {",
            "    this.position = position;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public void setValue(test.Position value) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <property id='setValue(test.Position)'>",
            "    <editor id='staticField'>",
            "      <parameter name='class'>test.Position</parameter>",
            "      <parameter name='fields'>LEFT RIGHT CENTER</parameter>",
            "    </editor>",
            "    <defaultValue value='test.Position.LEFT'/>",
            "  </property>",
            "</component>"));
    waitForAutoBuild();
    // parse 1
    {
      ContainerInfo panel =
          parseContainer(
              "// filler filler filler",
              "public class Test extends MyPanel {",
              "  public Test() {",
              "  }",
              "}");
      assertEquals("LEFT", getPropertyText(panel.getPropertyByTitle("value")));
      // clean up to allow second parsing
      disposeLastModel();
      getFileSrc("test/Test.java").delete(true, null);
    }
    // parse 2
    {
      ContainerInfo panel =
          parseContainer(
              "// filler filler filler",
              "public class Test extends MyPanel {",
              "  public Test() {",
              "  }",
              "}");
      assertEquals("LEFT", getPropertyText(panel.getPropertyByTitle("value")));
    }
  }

  /**
   * Exception will happen, because of invalid expression.
   */
  public void test_defaultPropertyValue_bad() throws Exception {
    try {
      assert_defaultPropertyValue("boolean", "bad-string", null);
      fail();
    } catch (DesignerException e) {
    }
  }

  /**
   * When default value specified in description, getter should not be used to ask default value.
   */
  public void test_defaultPropertyValue_ignoreAccessor() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public int getFoo() {",
            "    return 5;",
            "  }",
            "  public void setFoo(int foo) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <property id='setFoo(int)'>",
            "    <defaultValue value='2'/>",
            "  </property>",
            "</component>"));
    waitForAutoBuild();
    //
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertEquals(2, panel.getPropertyByTitle("foo").getValue());
  }

  /**
   * Prepares "MyPanel" class with default value of property in description.
   */
  private void prepare_defaultPropertyValue(String propertyType, String defaultValueString)
      throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends JPanel {",
            "  public void setQ(" + propertyType + " q) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSource(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <property id='setQ(" + propertyType + ")'>",
            "    <defaultValue value='" + defaultValueString + "'/>",
            "  </property>",
            "</component>"));
    waitForAutoBuild();
  }

  private void assert_defaultPropertyValue(String propertyType,
      String defaultValueString,
      Object expectedValue) throws Exception {
    prepare_defaultPropertyValue(propertyType, defaultValueString);
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertEquals(expectedValue, panel.getPropertyByTitle("q").getValue());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Property: category
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * We can use tag "<category value='normal/preferred/advanced/hidden'>" to change
   * {@link PropertyCategory} for {@link Property}.
   */
  public void test_propertyCategory() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public void setA(int value) {",
            "  }",
            "  public void setB(int value) {",
            "  }",
            "  public void setC(int value) {",
            "  }",
            "  public void setD(int value) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <property id='setA(int)'>",
            "    <category value='normal'/>",
            "  </property>",
            "  <property id='setB(int)'>",
            "    <category value='preferred'/>",
            "  </property>",
            "  <property id='setC(int)'>",
            "    <category value='advanced'/>",
            "  </property>",
            "  <property id='setD(int)'>",
            "    <category value='hidden'/>",
            "  </property>",
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
    ComponentDescription description = panel.getDescription();
    assertSame(PropertyCategory.NORMAL, description.getProperty("setA(int)").getCategory());
    assertSame(PropertyCategory.PREFERRED, description.getProperty("setB(int)").getCategory());
    assertSame(PropertyCategory.ADVANCED, description.getProperty("setC(int)").getCategory());
    assertSame(PropertyCategory.HIDDEN, description.getProperty("setD(int)").getCategory());
  }

  /**
   * Attempt to use "<category>" with unsupported value.
   * <p>
   * {@link SAXParseException} will happen, because of XSD validation.
   */
  public void test_propertyCategory_bad() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public void setA(int value) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <property id='setA(int)'>",
            "    <category value='no-such-category'/>",
            "  </property>",
            "</component>"));
    waitForAutoBuild();
    // parse
    try {
      parseContainer(
          "// filler filler filler",
          "public class Test extends MyPanel {",
          "  public Test() {",
          "  }",
          "}");
    } catch (Throwable e) {
      e = DesignerExceptionUtils.getRootCause(e);
      assertInstanceOf(SAXParseException.class, e);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MethodRule
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * "method" tag should add {@link MethodDescription} into {@link ComponentDescription} and also
   * fill returnType/declaringClass.
   */
  public void test_MethodRule() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public String foo(int value) {",
            "    return null;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='foo'>",
            "      <parameter type='int'/>",
            "      <tag name='myTagName' value='myTagValue'/>",
            "    </method>",
            "  </methods>",
            "</component>"));
    waitForAutoBuild();
    //
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    // MethodDescription
    MethodDescription methodDescription = panel.getDescription().getMethod("foo(int)");
    assertNotNull(methodDescription);
    assertEquals("test.MyPanel", methodDescription.getDeclaringClass().getName());
    assertEquals("java.lang.String", methodDescription.getReturnClass().getName());
    // tag
    assertEquals("myTagValue", methodDescription.getTag("myTagName"));
    assertNull(methodDescription.getTag("no-such-tag"));
  }

  /**
   * Test for {@link AbstractDescription#hasTrueTag(String)}.
   */
  public void test_AbstractDescription_hasTrueTag() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends JPanel {",
            "  public void foo() {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='foo'>",
            "      <tag name='myTrueTag' value='true'/>",
            "      <tag name='myFalseTag' value='false'/>",
            "    </method>",
            "  </methods>",
            "</component>"));
    waitForAutoBuild();
    //
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    // MethodDescription
    MethodDescription methodDescription = panel.getDescription().getMethod("foo()");
    assertTrue(methodDescription.hasTrueTag("myTrueTag"));
    assertFalse(methodDescription.hasTrueTag("myFalseTag"));
    assertFalse(methodDescription.hasTrueTag("noSuchTag"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MethodsOperationRule
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_MethodsOperationRule_signatureInclude() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends JPanel {",
            "  public void foo() {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <methods-include signature='foo()'/>",
            "  </methods>",
            "</component>"));
    waitForAutoBuild();
    //
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertNotNull(panel.getDescription().getMethod("foo()"));
  }

  public void test_MethodsOperationRule_signatureExclude() throws Exception {
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
            "    <methods-exclude signature='setEnabled(boolean)'/>",
            "  </methods>",
            "</component>"));
    waitForAutoBuild();
    //
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertNull(panel.getDescription().getMethod("setEnabled(boolean)"));
    assertNotNull(panel.getDescription().getMethod("setAutoscrolls(boolean)"));
  }

  public void test_MethodsOperationRule_regexpInclude() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends JPanel {",
            "  public void foo() {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <methods-include signature='/foo.+/'/>",
            "  </methods>",
            "</component>"));
    waitForAutoBuild();
    //
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertNotNull(panel.getDescription().getMethod("foo()"));
  }

  public void test_MethodsOperationRule_regexpExclude() throws Exception {
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
            "    <methods-exclude signature='/setEnabled.+/'/>",
            "  </methods>",
            "</component>"));
    waitForAutoBuild();
    //
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertNull(panel.getDescription().getMethod("setEnabled(boolean)"));
    assertNotNull(panel.getDescription().getMethod("setAutoscrolls(boolean)"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Exposed children
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for custom components and using "exposing-rules".
   */
  public void test_exposedChildren_rules() throws Exception {
    setFileContentSrc(
        "test_1/MyPanel_1.java",
        getSourceDQ(
            "package test_1;",
            "import javax.swing.*;",
            "public class MyPanel_1 extends JPanel {",
            "  public MyPanel_1() {",
            "    add(getButton_1());",
            "  }",
            "  private JButton m_button_1 = new JButton();",
            "  public JButton getButton_1() {",
            "    return m_button_1;",
            "  }",
            "}"));
    setFileContentSrc(
        "test_2/MyPanel_2.java",
        getSourceDQ(
            "package test_2;",
            "import javax.swing.*;",
            "public class MyPanel_2 extends test_1.MyPanel_1 {",
            "  public MyPanel_2() {",
            "    add(getButton_2());",
            "  }",
            "  private JButton m_button_2 = new JButton();",
            "  public JButton getButton_2() {",
            "    return m_button_2;",
            "  }",
            "}"));
    setFileContentSrc(
        "test_2/MyPanel_2.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <exposing-rules>",
            "    <exclude package='test_1'/>",
            "  </exposing-rules>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends test_2.MyPanel_2 {",
            "  public Test() {",
            "  }",
            "}");
    List<ComponentInfo> components = panel.getChildrenComponents();
    assertEquals(1, components.size());
    ExposedPropertyCreationSupport exposedCreationSupport =
        (ExposedPropertyCreationSupport) components.get(0).getCreationSupport();
    assertThat(exposedCreationSupport.toString()).contains("getButton_2()");
  }

  /**
   * We should have ability to specify tweaks for descriptions of exposed components.<br>
   * In this case we expose {@link JButton} as {@link Container}. The {@link Container} type in
   * description tells that it <em>has</em> layout, but we as developers of <code>MyPanel</code>
   * know, that we expose {@link JButton} that should not be used with layout. So, we need to add
   * tweak.
   */
  public void test_exposedChildren_specificDescriptions() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public MyPanel() {",
            "    add(getButton());",
            "  }",
            "  private Container m_button = new JButton();",
            "  public Container getButton() {",
            "    return m_button;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.getButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='layout.has'>false</parameter>",
            "  </parameters>",
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
    ContainerInfo exposedButton = (ContainerInfo) panel.getChildrenComponents().get(0);
    assertThat(exposedButton.toString()).contains("getButton()");
    assertFalse(exposedButton.hasLayout());
  }

  /**
   * When no method-specific description resource, pure type-based {@link ComponentDescription}
   * should be returned.
   */
  public void test_exposedChildren_noSpecificDescription() throws Exception {
    ContainerInfo frame =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JFrame {",
            "  public Test() {",
            "  }",
            "}");
    ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
    assertThat(contentPane.toString()).contains("getContentPane()");
    // description is same as for Container
    assertSame(
        contentPane.getDescription(),
        ComponentDescriptionHelper.getDescription(m_lastEditor, Container.class));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parameter based children
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Helper method for preparing further testing.
   */
  private void prepare_parameterChildren_specificDescription(boolean withDescription)
      throws Exception {
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
    if (withDescription) {
      setFileContentSrc(
          "test/MyDialog.createDialogArea_java.awt.Container_.0.wbp-component.xml",
          getSourceDQ(
              "<?xml version='1.0' encoding='UTF-8'?>",
              "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
              "  <parameters>",
              "    <parameter name='parameter_1'>value_1</parameter>",
              "  </parameters>",
              "</component>"));
    }
    waitForAutoBuild();
  }

  /**
   * Don't use special description for {@link SingleVariableDeclaration}, so no
   * <code>parameter_1</code>. We should have ability to specify tweaks for descriptions of
   * parameter components.<br>
   * In this case parameter "parent" in <code>createDialogArea</code> should be marked as not having
   * layout.
   */
  public void test_parameterChildren_specificDescription_0() throws Exception {
    prepare_parameterChildren_specificDescription(false);
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyDialog {",
            "  public Test() {",
            "  }",
            "  protected void createDialogArea(Container parent) {",
            "  }",
            "}");
    // check hierarchy
    assertHierarchy(
        "{this: test.MyDialog} {this} {}",
        "  {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}",
        "  {parameter} {parent} {}",
        "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
    // prepare "parent" ContainerInfo
    ContainerInfo parent;
    {
      parent = (ContainerInfo) panel.getChildrenComponents().get(0);
      assertInstanceOf(UnknownAssociation.class, parent.getAssociation());
      assertInstanceOf(MethodParameterCreationSupport.class, parent.getCreationSupport());
      assertInstanceOf(MethodParameterVariableSupport.class, parent.getVariableSupport());
      // no "parameter_1"
      assertNull(parent.getDescription().getParameter("parameter_1"));
      // and at all, this is same description as just for Container
      assertSame(
          ComponentDescriptionHelper.getDescription(m_lastEditor, Container.class),
          parent.getDescription());
    }
  }

  /**
   * We should have ability to specify tweaks for descriptions of parameter components.<br>
   * In this case parameter "parent" in <code>createDialogArea</code> should be marked as not having
   * layout.
   */
  public void test_parameterChildren_specificDescription_1() throws Exception {
    prepare_parameterChildren_specificDescription(true);
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyDialog {",
            "  public Test() {",
            "  }",
            "  protected void createDialogArea(Container parent) {",
            "  }",
            "}");
    // check hierarchy
    assertHierarchy(
        "{this: test.MyDialog} {this} {}",
        "  {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}",
        "  {parameter} {parent} {}",
        "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
    // prepare "parent" ContainerInfo
    ContainerInfo parent;
    {
      parent = (ContainerInfo) panel.getChildrenComponents().get(0);
      assertInstanceOf(UnknownAssociation.class, parent.getAssociation());
      assertInstanceOf(MethodParameterCreationSupport.class, parent.getCreationSupport());
      assertInstanceOf(MethodParameterVariableSupport.class, parent.getVariableSupport());
      assertEquals("value_1", parent.getDescription().getParameter("parameter_1"));
    }
  }

  /**
   * Same as {@link #test_parameterChildren_specificDescription_1()} but tests that specific
   * {@link ComponentDescription}'s are used also in subclasses.
   */
  public void test_parameterChildren_specificDescription_2() throws Exception {
    prepare_parameterChildren_specificDescription(true);
    setFileContentSrc(
        "test/MyDialog2.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public abstract class MyDialog2 extends MyDialog {",
            "}"));
    setFileContentSrc(
        "test/MyDialog2.createDialogArea_java.awt.Container_.0.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='parameter_2'>value_2</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyDialog2 {",
            "  public Test() {",
            "  }",
            "  protected void createDialogArea(Container parent) {",
            "  }",
            "}");
    // check hierarchy
    assertHierarchy(
        "{this: test.MyDialog2} {this} {}",
        "  {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}",
        "  {parameter} {parent} {}",
        "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
    // prepare "parent" ContainerInfo
    ContainerInfo parent;
    {
      parent = (ContainerInfo) panel.getChildrenComponents().get(0);
      assertInstanceOf(UnknownAssociation.class, parent.getAssociation());
      assertInstanceOf(MethodParameterCreationSupport.class, parent.getCreationSupport());
      assertInstanceOf(MethodParameterVariableSupport.class, parent.getVariableSupport());
      assertEquals("value_1", parent.getDescription().getParameter("parameter_1"));
      assertEquals("value_2", parent.getDescription().getParameter("parameter_2"));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parameters
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ComponentDescription#getParameters()}.
   */
  public void test_parameters() throws Exception {
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
            "  <parameters>",
            "    <parameter name='parameter_1'>AAA</parameter>",
            "    <parameter name='parameter_2'>BBB</parameter>",
            "    <parameter name='parameter_3'/>",
            "  </parameters>",
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
    ComponentDescription description = panel.getDescription();
    // single parameters
    {
      assertEquals("AAA", description.getParameter("parameter_1"));
      assertEquals("BBB", description.getParameter("parameter_2"));
      assertEquals("", description.getParameter("parameter_3"));
      assertNull(description.getParameter("parameter_No"));
    }
    // parameters as Map
    {
      Map<String, String> parameters = description.getParameters();
      assertThat(parameters).contains(
          entry("parameter_1", "AAA"),
          entry("parameter_2", "BBB"),
          entry("parameter_3", ""));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Configurable property
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * In GWT <code>ListBox</code> has only <code>addItem(String)</code> methods, and no method to set
   * all items as single invocation, like <code>setItems(String[])</code>. So, to edit items, we
   * need some artificial {@link Property}.
   */
  public void test_configurableProperty() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public void clear() {",
            "  }",
            "  public void addItem(String item) {",
            "  }",
            "  public void insertItem(String item, int index) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <add-property id='stringsAdd' title='items'>",
            "    <parameter name='addMethod'>addItem</parameter>",
            "    <parameter name='removeMethods'>clear() insertItem(java.lang.String,int)</parameter>",
            "  </add-property>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    addItem('aaa');",
            "    addItem('bbb');",
            "  }",
            "}");
    // prepare "items" property
    Property itemsProperty = panel.getPropertyByTitle("items");
    assertNotNull(itemsProperty);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ParameterDescription
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_ParameterDescription_flags() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public void foo_1(Component component) {",
            "  }",
            "  public void foo_2(Component component) {",
            "  }",
            "  public void foo_3(Component component_1, Component component_2) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='foo_1'>",
            "      <parameter type='java.awt.Component' child='true'/>",
            "    </method>",
            "    <method name='foo_2'>",
            "      <parameter type='java.awt.Component' parent='true'/>",
            "    </method>",
            "    <method name='foo_3'>",
            "      <parameter type='java.awt.Component' parent2='true'/>",
            "      <parameter type='java.awt.Component' child2='true'/>",
            "    </method>",
            "  </methods>",
            "</component>"));
    waitForAutoBuild();
    //
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    ComponentDescription componentDescription = panel.getDescription();
    {
      MethodDescription method = componentDescription.getMethod("foo_1(java.awt.Component)");
      assertEquals("foo_1({java.awt.Component,child})", method.toString());
      assertTrue(method.getParameter(0).isChild());
      assertFalse(method.getParameter(0).isParent());
      assertFalse(method.getParameter(0).isParent2());
      assertFalse(method.getParameter(0).isChild2());
    }
    {
      MethodDescription method = componentDescription.getMethod("foo_2(java.awt.Component)");
      assertEquals("foo_2({java.awt.Component,parent})", method.toString());
      assertTrue(method.getParameter(0).isParent());
    }
    {
      MethodDescription method =
          componentDescription.getMethod("foo_3(java.awt.Component,java.awt.Component)");
      assertEquals(
          "foo_3({java.awt.Component,parent2},{java.awt.Component,child2})",
          method.toString());
      assertTrue(method.getParameter(0).isParent2());
      assertTrue(method.getParameter(1).isChild2());
    }
  }

  /**
   * Test that for <code>setXXX()</code> methods that are form the property created by
   * {@link StandardBeanPropertiesRule}, have <code>name</code> for {@link ParameterDescription}.
   */
  public void test_ParameterDescription_name() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    String methodSignature = "setEnabled(boolean)";
    MethodDescription methodDescription = panel.getDescription().getMethod(methodSignature);
    assertEquals("enabled", methodDescription.getParameter(0).getName());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // StandardBeanPropertiesRule
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that {@link StandardBeanPropertiesRule} does not create property for static method.
   */
  public void test_StandardBeanPropertiesRule_ignoreStaticMethods() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public static void setFoo(int value) {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertNull(panel.getPropertyByTitle("foo"));
  }

  /**
   * Sometimes we need to tweak "getter" for property.
   */
  public void test_StandardBeanPropertiesRule_nonStandardGetter() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public void setFoo(int foo) {",
            "  }",
            "  public int getMyFoo() {",
            "    return 555;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <property id='setFoo(int)'>",
            "    <getter name='getMyFoo'/>",
            "  </property>",
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
    panel.refresh();
    // default value is "555" from getMyFoo()
    Property property = panel.getPropertyByTitle("foo");
    assertEquals(555, property.getValue());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ComponentDescription.getConstructor()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_ComponentDescription_getConstructor() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setEnabled(false);",
            "  }",
            "}");
    MethodInvocation methodInvocation = panel.getMethodInvocation("setEnabled(boolean)");
    IMethodBinding methodBinding = AstNodeUtils.getMethodBinding(methodInvocation);
    // use wrong method binding to access constructor, "null" expected
    assertNull(panel.getDescription().getConstructor(methodBinding));
  }

  /**
   * We saw more than one time that description of component referenced no existing constructor.
   * This caused wonders why some feature does not work when it seems that we have all required
   * descriptions.
   * <p>
   * So, we have to add stronger check for constructor.
   */
  public void test_badConstructor() throws Exception {
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <constructors>",
            "    <constructor>",
            "      <parameter type='int'/>",
            "    </constructor>",
            "  </constructors>",
            "</component>"));
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends JPanel{",
            "  public MyPanel() {",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    try {
      parseContainer(
          "// filler filler filler",
          "public class Test extends MyPanel {",
          "  public Test() {",
          "  }",
          "}");
    } catch (Throwable e) {
      Throwable root = DesignerExceptionUtils.getRootCause(e);
      // check that exception message gives enough information:
      // a) name of class;
      // b) signature of constructor.
      assertThat(root.getMessage()).contains("test.MyPanel").contains("<init>(int)");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractInvocationDescription
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_AbstractInvocationDescription() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    String methodSignature = "setEnabled(boolean)";
    MethodDescription methodDescription = panel.getDescription().getMethod(methodSignature);
    // equals
    assertTrue(methodDescription.equals(methodDescription));
    assertFalse(methodDescription.equals(this));
    // hashCode
    assertEquals(methodSignature.hashCode(), methodDescription.hashCode());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Factory
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link ComponentDescriptionHelper} should use {@link ComponentInfo} as model for {@link Box},
   * no matter if it has factory methods.
   */
  public void test_instanceFactory_model_onlyStatic() throws Exception {
    parseContainer(
        "// filler filler filler",
        "public final class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // prepare ComponentDescription
    ComponentDescription componentDescription =
        ComponentDescriptionHelper.getDescription(m_lastEditor, Box.class);
    assertTrue(ComponentInfo.class.isAssignableFrom(componentDescription.getModelClass()));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // loadModelClass()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ComponentDescriptionHelper#loadModelClass(String)}.
   */
  public void test_loadModelClass() throws Exception {
    parseContainer(
        "// filler filler filler",
        "public final class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // do checks
    DescriptionHelper.loadModelClass("org.eclipse.wb.core.model.JavaInfo");
    DescriptionHelper.loadModelClass("org.eclipse.wb.internal.swt.model.widgets.ControlInfo");
    try {
      DescriptionHelper.loadModelClass("no.such.Class");
      fail();
    } catch (ClassNotFoundException e) {
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getDescription() variants
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that {@link ComponentDescriptionHelper#getDescription(AstEditor, String)} and
   * {@link ComponentDescriptionHelper#getDescription(AstEditor, Class)} return same result.
   */
  public void test_getDescription_variants() throws Exception {
    parseContainer(
        "// filler filler filler",
        "public final class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // do checks
    assertSame(
        ComponentDescriptionHelper.getDescription(m_lastEditor, JPanel.class),
        ComponentDescriptionHelper.getDescription(m_lastEditor, "javax.swing.JPanel"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // has*
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ComponentDescriptionHelper#hasComponentDescriptionResource(EditorState, Class)}
   * .
   */
  public void test_hasComponentDescriptionResource() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends JButton {",
            "  public MyButton() {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "// filler filler filler",
        "public final class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    assertTrue(DescriptionHelper.hasComponentDescriptionResource(
        m_lastLoadingContext,
        Container.class));
    assertFalse(DescriptionHelper.hasComponentDescriptionResource(
        m_lastLoadingContext,
        m_lastLoader.loadClass("test.MyButton")));
  }

  /**
   * Test for
   * {@link ComponentDescriptionHelper#hasForcedToolkitForComponent(EditorState, String, String)}.
   */
  public void test_hasForcedToolkitForComponent() throws Exception {
    String swingToolkitId = SwingToolkitDescription.INSTANCE.getId();
    setFileContentSrc(
        "test/NoForced.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class NoForced extends JButton {",
            "  public NoForced() {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/HasForced.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class HasForced extends JButton {",
            "  public HasForced() {",
            "  }",
            "}"));
    setFileContentSrc("test/HasForced.wbp-forced-toolkit.txt", swingToolkitId);
    waitForAutoBuild();
    // parse
    parseContainer(
        "// filler filler filler",
        "public final class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    //
    assertFalse(DescriptionHelper.hasForcedToolkitForComponent(
        m_lastLoadingContext,
        "no.matter",
        "test.NoForced"));
    assertTrue(DescriptionHelper.hasForcedToolkitForComponent(
        m_lastLoadingContext,
        swingToolkitId,
        "test.HasForced"));
    assertFalse(DescriptionHelper.hasForcedToolkitForComponent(
        m_lastLoadingContext,
        "not.required.toolkit",
        "test.HasForced"));
  }
}
