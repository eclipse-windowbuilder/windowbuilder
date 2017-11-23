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

import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.CreationDescription;
import org.eclipse.wb.internal.core.model.description.CreationDescription.TypeParameterDescription;
import org.eclipse.wb.internal.core.model.description.CreationInvocationDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.swt.graphics.Image;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;

import java.util.Map;

/**
 * Tests for loading {@link CreationDescription} from *.wbp-component.xml files.
 *
 * @author scheglov_ke
 */
public class CreationDescriptionLoadingTest extends SwingModelTest {
  private Class<?> m_myButtonClass;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void tearDown() throws Exception {
    m_myButtonClass = null;
    super.tearDown();
  }

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
  // Loading from descriptions
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Load {@link CreationDescription} without id.
   */
  public void test_load_noId() throws Exception {
    prepareMyButton();
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <creation>",
            "    <source><![CDATA[new test.MyButton()]]></source>",
            "  </creation>",
            "</component>"));
    waitForAutoBuild();
    // prepare component description
    ComponentDescription description =
        ComponentDescriptionHelper.getDescription(m_lastEditor, m_myButtonClass);
    // check
    CreationDescription creation = description.getCreation(null);
    assertNull(creation.getId());
    assertEquals("MyButton", creation.getName());
    assertEquals("new test.MyButton()", creation.getSource());
    assertEquals(0, creation.getInvocations().size());
  }

  /**
   * Load {@link CreationDescription} with explicit "name" attribute.
   */
  public void test_load_withName() throws Exception {
    prepareMyButton();
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <creation id='withName' name='my unique name'>",
            "    <source><![CDATA[new test.MyButton()]]></source>",
            "  </creation>",
            "</component>"));
    waitForAutoBuild();
    // prepare component description
    ComponentDescription description =
        ComponentDescriptionHelper.getDescription(m_lastEditor, m_myButtonClass);
    // check
    String id = "withName";
    CreationDescription creation = description.getCreation(id);
    assertEquals(id, creation.getId());
    assertEquals("my unique name", creation.getName());
    assertEquals("new test.MyButton()", creation.getSource());
    assertEquals(0, creation.getInvocations().size());
  }

  /**
   * Load {@link CreationDescription} without/with explicit "description" sub-element.
   */
  public void test_load_forDescription() throws Exception {
    prepareMyButton();
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <description>type level description</description>",
            "  <creation id='noDescription'>",
            "    <source><![CDATA[new test.MyButton()]]></source>",
            "  </creation>",
            "  <creation id='withDescription'>",
            "    <source><![CDATA[new test.MyButton()]]></source>",
            "    <description>creation level description</description>",
            "  </creation>",
            "</component>"));
    waitForAutoBuild();
    // prepare component description
    ComponentDescription description;
    {
      description = ComponentDescriptionHelper.getDescription(m_lastEditor, m_myButtonClass);
      assertEquals("type level description", description.getDescription());
    }
    // check
    {
      String id = "noDescription";
      CreationDescription creation = description.getCreation(id);
      assertEquals(id, creation.getId());
      assertEquals("type level description", creation.getDescription());
    }
    {
      String id = "withDescription";
      CreationDescription creation = description.getCreation(id);
      assertEquals(id, creation.getId());
      assertEquals("creation level description", creation.getDescription());
    }
  }

  /**
   * Load {@link CreationDescription} when text in "description" sub-element is wrapped.<br>
   * We should remove any EOL's and normalize spaces.
   */
  public void test_load_wrappedDescription() throws Exception {
    prepareMyButton();
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <description>type",
            "    level    description</description>",
            "  <creation id='withDescription'>",
            "    <source><![CDATA[new test.MyButton()]]></source>",
            "    <description>creation",
            "        level    description</description>",
            "  </creation>",
            "</component>"));
    waitForAutoBuild();
    // prepare component description
    ComponentDescription description =
        ComponentDescriptionHelper.getDescription(m_lastEditor, m_myButtonClass);
    // description for "component"
    {
      CreationDescription creation = description.getCreation(null);
      assertEquals("type level description", creation.getDescription());
    }
    // description for "creation"
    {
      CreationDescription creation = description.getCreation("withDescription");
      assertEquals("creation level description", creation.getDescription());
    }
  }

  /**
   * Load {@link CreationDescription}, use "id" for loading creation specific icon.
   */
  public void test_load_forIcon() throws Exception {
    prepareMyButton();
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <creation>",
            "    <source><![CDATA[new test.MyButton()]]></source>",
            "  </creation>",
            "  <creation id='withIcon'>",
            "    <source><![CDATA[new test.MyButton()]]></source>",
            "  </creation>",
            "</component>"));
    TestUtils.createImagePNG(m_testProject, "src/test/MyButton_withIcon.png", 10, 10);
    waitForAutoBuild();
    // prepare component description
    ComponentDescription description =
        ComponentDescriptionHelper.getDescription(m_lastEditor, m_myButtonClass);
    // no "id", so use type icon
    {
      String id = null;
      CreationDescription creation = description.getCreation(id);
      assertEquals(id, creation.getId());
      // check icon
      Image creationIcon = creation.getIcon();
      assertNotNull(creationIcon);
      assertSame(description.getIcon(), creationIcon);
      assertEquals(16, creationIcon.getBounds().width);
      assertEquals(16, creationIcon.getBounds().height);
    }
    // has "id", and we create icon for this "id", so load it
    {
      String id = "withIcon";
      CreationDescription creation = description.getCreation(id);
      assertEquals(id, creation.getId());
      // check icon
      Image creationIcon = creation.getIcon();
      assertNotNull(creationIcon);
      assertNotSame(description.getIcon(), creationIcon);
      assertEquals(10, creationIcon.getBounds().width);
      assertEquals(10, creationIcon.getBounds().height);
    }
  }

  /**
   * Load {@link CreationDescription} that uses <code>"%component.class%"</code> pattern and fills
   * it during {@link CreationDescription#getSource()}.
   */
  public void test_load_withPattern() throws Exception {
    prepareMyButton();
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <creation id='withPattern'>",
            "    <source><![CDATA[new %component.class%()]]></source>",
            "  </creation>",
            "</component>"));
    waitForAutoBuild();
    // prepare component description
    ComponentDescription description =
        ComponentDescriptionHelper.getDescription(m_lastEditor, m_myButtonClass);
    // check
    String id = "withPattern";
    CreationDescription creation = description.getCreation(id);
    assertEquals(id, creation.getId());
    assertEquals("MyButton", creation.getName());
    assertEquals("new test.MyButton()", creation.getSource());
    assertEquals(0, creation.getInvocations().size());
  }

  /**
   * Load {@link CreationDescription} with specified <code>invocation</code> elements.
   */
  public void test_load_withInvocation() throws Exception {
    prepareMyButton();
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <creation id='withInvocation'>",
            "    <source><![CDATA[new test.MyButton()]]></source>",
            "    <invocation signature='setText(java.lang.String)'><![CDATA['some text']]></invocation>",
            "  </creation>",
            "</component>"));
    waitForAutoBuild();
    // prepare component description
    ComponentDescription description =
        ComponentDescriptionHelper.getDescription(m_lastEditor, m_myButtonClass);
    // check
    String id = "withInvocation";
    CreationDescription creation = description.getCreation(id);
    assertEquals(id, creation.getId());
    assertEquals("MyButton", creation.getName());
    assertEquals("new test.MyButton()", creation.getSource());
    assertEquals(1, creation.getInvocations().size());
    {
      CreationInvocationDescription invocation = creation.getInvocations().get(0);
      assertEquals("setText(java.lang.String)", invocation.getSignature());
      assertEquals("\"some text\"", invocation.getArguments());
    }
  }

  /**
   * Load {@link CreationDescription} with specified <code>parameter</code> elements.
   */
  public void test_load_withParameters() throws Exception {
    prepareMyButton();
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <creation id='withParameters'>",
            "    <source><![CDATA[new test.MyButton()]]></source>",
            "    <parameter name='name_1'>value_1</parameter>",
            "    <parameter name='name_2'>value_2</parameter>",
            "  </creation>",
            "</component>"));
    waitForAutoBuild();
    // prepare component description
    ComponentDescription description =
        ComponentDescriptionHelper.getDescription(m_lastEditor, m_myButtonClass);
    // check
    String id = "withParameters";
    CreationDescription creation = description.getCreation(id);
    assertEquals(id, creation.getId());
    assertEquals("MyButton", creation.getName());
    assertEquals("new test.MyButton()", creation.getSource());
    assertThat(creation.getParameters()).contains(
        entry("name_1", "value_1"),
        entry("name_2", "value_2"));
  }

  /**
   * Load {@link CreationDescription} with tag.
   */
  public void test_load_withTag() throws Exception {
    prepareMyButton();
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <creation>",
            "    <source><![CDATA[new test.MyButton()]]></source>",
            "    <tag name='testTag1' value='true'/>",
            "    <tag name='testTag2' value='tag2value'/>",
            "  </creation>",
            "</component>"));
    waitForAutoBuild();
    // prepare component description
    ComponentDescription description =
        ComponentDescriptionHelper.getDescription(m_lastEditor, m_myButtonClass);
    // check
    CreationDescription creation = description.getCreation(null);
    assertTrue(creation.hasTrueTag("testTag1"));
    assertEquals(creation.getTag("testTag2"), "tag2value");
    assertNull(creation.getTag("testTag3"));
    assertFalse(creation.hasTrueTag("testTag3"));
  }

  /**
   * Load {@link CreationDescription} with type parameters (generics).
   */
  public void test_load_withTypeParameters() throws Exception {
    prepareContext();
    // prepare component to test on
    {
      setFileContentSrc(
          "test/MyButton.java",
          getTestSource(
              "// filler filler filler filler filler",
              "// filler filler filler filler filler",
              "public class MyButton<T extends java.lang.Number> extends JButton {",
              "  public MyButton() {",
              "  }",
              "}"));
      setFileContentSrc(
          "test/MyButton.wbp-component.xml",
          getSourceDQ(
              "<?xml version='1.0' encoding='UTF-8'?>",
              "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
              "  <creation>",
              "    <source><![CDATA[new test.MyButton<%T%>()]]></source>",
              "    <typeParameters>",
              "      <typeParameter name='T' type='java.lang.Number' title='Generic type &lt;T&gt;'/>",
              "    </typeParameters>",
              "  </creation>",
              "</component>"));
      waitForAutoBuild();
      m_myButtonClass = m_lastLoader.loadClass("test.MyButton");
    }
    // prepare component description
    ComponentDescription description =
        ComponentDescriptionHelper.getDescription(m_lastEditor, m_myButtonClass);
    // check
    CreationDescription creation = description.getCreation(null);
    Map<String, TypeParameterDescription> typeParameters = creation.getTypeParameters();
    assertNotNull(typeParameters);
    TypeParameterDescription parameter = typeParameters.get("T");
    assertNotNull(parameter);
    assertEquals("java.lang.Number", parameter.getTypeName());
    assertEquals("Generic type <T>", parameter.getTitle());
  }

  /**
   * Default {@link CreationDescription} should exist even for standard {@link Object}.
   */
  public void test_defaultCreation_forObject() throws Exception {
    class MyPanel {
    }
    prepareContext();
    // prepare component description
    ComponentDescription description =
        ComponentDescriptionHelper.getDescription(m_lastEditor, MyPanel.class);
    // check
    CreationDescription creation = description.getCreation(null);
    assertNotNull(creation);
    assertEquals(
        "new " + ReflectionUtils.getCanonicalName(MyPanel.class) + "()",
        creation.getSource());
  }

  /**
   * Default {@link CreationDescription} should have default values for arguments.
   */
  public void test_defaultCreation_forComponent() throws Exception {
    prepareContext();
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends JButton {",
            "  public MyButton(int a, String b, java.util.List c) {",
            "  }",
            "}"));
    waitForAutoBuild();
    m_myButtonClass = m_lastLoader.loadClass("test.MyButton");
    // prepare component description
    ComponentDescription description =
        ComponentDescriptionHelper.getDescription(m_lastEditor, m_myButtonClass);
    // check
    CreationDescription creation = description.getCreation(null);
    assertEquals(
        "new test.MyButton(0, (java.lang.String) null, (java.util.List) null)",
        creation.getSource());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private void prepareMyButton() throws Exception {
    prepareContext();
    // prepare component to test on
    {
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
      m_myButtonClass = m_lastLoader.loadClass("test.MyButton");
    }
  }

  private void prepareContext() throws Exception {
    parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    // filler filler filler",
        "  }",
        "}");
  }
}
