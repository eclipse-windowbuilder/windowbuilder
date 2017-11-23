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
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.description.rules.MethodPropertyRule;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.BooleanPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link MethodPropertyRule}.
 * 
 * @author scheglov_ke
 */
public class MethodPropertyRuleTest extends SwingModelTest {
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
  public void test_subPropertiesAndAccessors() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public void setText(String someText, boolean html) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <method-property title='text' method='setText(java.lang.String,boolean)'/>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    setText('some text', true);",
            "  }",
            "}");
    // method setText(String,boolean) should be executable
    assertNotNull(panel.getDescription().getMethod("setText(java.lang.String,boolean)"));
    // prepare "text" property
    GenericProperty methodProperty = (GenericProperty) panel.getPropertyByTitle("text");
    assertNotNull(methodProperty);
    assertEquals("(properties)", getPropertyText(methodProperty));
    // check sub-properties
    Property[] subProperties = getSubProperties(methodProperty);
    assertEquals(2, subProperties.length);
    {
      GenericPropertyImpl subProperty = (GenericPropertyImpl) subProperties[0];
      assertEquals("someText", subProperty.getTitle());
      assertEquals("some text", subProperty.getValue());
    }
    {
      GenericPropertyImpl subProperty = (GenericPropertyImpl) subProperties[1];
      assertEquals("html", subProperty.getTitle());
      assertEquals(Boolean.TRUE, subProperty.getValue());
    }
  }

  public void test_useNamesFromParameterDescription() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public void setText(String text, boolean html) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='setText'>",
            "      <parameter type='java.lang.String' name='myArg_0'/>",
            "      <parameter type='boolean' name='myArg_1'/>",
            "    </method>",
            "  </methods>",
            "  <method-property title='text' method='setText(java.lang.String,boolean)'/>",
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
    GenericProperty methodProperty = (GenericProperty) panel.getPropertyByTitle("text");
    Property[] subProperties = getSubProperties(methodProperty);
    assertEquals("myArg_0", subProperties[0].getTitle());
    assertEquals("myArg_1", subProperties[1].getTitle());
  }

  public void test_noExtraTopLevelProperties() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public void setText(String someText, boolean html) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <method-property title='text' method='setText(java.lang.String,boolean)'/>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    setText('some text', true);",
            "  }",
            "}");
    // check sub-properties
    assertNotNull(PropertyUtils.getByPath(panel, "text/someText"));
    assertNotNull(PropertyUtils.getByPath(panel, "text/html"));
    // ensure that there are no extra "someText" or "html" properties
    assertNull(panel.getPropertyByTitle("someText"));
    assertNull(panel.getPropertyByTitle("html"));
  }

  public void test_registeredSubProperties() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public void setText(String someText, boolean html) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <method-property title='text' method='setText(java.lang.String,boolean)'/>",
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
    // check sub-properties
    {
      String id = "setText(java.lang.String,boolean) 0";
      GenericPropertyDescription property = description.getProperty(id);
      assertNotNull(property);
      assertThat(property.getEditor()).isSameAs(StringPropertyEditor.INSTANCE);
    }
    {
      String id = "setText(java.lang.String,boolean) 1";
      GenericPropertyDescription property = description.getProperty(id);
      assertNotNull(property);
      assertThat(property.getEditor()).isSameAs(BooleanPropertyEditor.INSTANCE);
    }
  }
}
