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
import org.eclipse.wb.internal.core.model.description.rules.MethodSinglePropertyRule;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.DisplayExpressionPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.IntegerPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.StringArrayPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link MethodSinglePropertyRule}.
 * 
 * @author scheglov_ke
 */
public class MethodSinglePropertyRuleTest extends SwingModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Even without any special descriptions we support single parameter methods with same name, but
   * different parameter type.
   */
  public void test_supportForMultipleMethodsByDefault() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public void setText(String text) {",
            "  }",
            "  public void setText(String[] items) {",
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
    ComponentDescription description = panel.getDescription();
    // setText(java.lang.String)
    {
      String id = "setText(java.lang.String)";
      GenericPropertyDescription property = description.getProperty(id);
      assertEquals("text(java.lang.String)", property.getTitle());
      assertThat(property.getEditor()).isSameAs(StringPropertyEditor.INSTANCE);
    }
    // setText(java.lang.String[])
    {
      String id = "setText(java.lang.String[])";
      GenericPropertyDescription property = description.getProperty(id);
      assertEquals("text(java.lang.String[])", property.getTitle());
      assertThat(property.getEditor()).isSameAs(StringArrayPropertyEditor.INSTANCE);
    }
  }

  /**
   * If method is not "setter", we can force create property from it using
   * <code>method-single-property</code> rule.
   */
  public void test_forNonStandardMethod() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public void foo(int value) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <method-single-property title='myFoo' method='foo(int)'>",
            "    <category value='preferred'/>",
            "    <defaultValue value='123'/>",
            "  </method-single-property>",
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
    // method foo(int) should be executable
    assertNotNull(description.getMethod("foo(int)"));
    // foo(int)
    {
      String id = "foo(int)";
      GenericPropertyDescription property = description.getProperty(id);
      assertEquals("myFoo", property.getTitle());
      assertSame(PropertyCategory.PREFERRED, property.getCategory());
      assertEquals(123, property.getDefaultValue());
      assertThat(property.getEditor()).isSameAs(IntegerPropertyEditor.INSTANCE);
    }
  }

  /**
   * You can specify {@link PropertyEditor} for parameter and it will be used for {@link Property}.
   */
  public void test_propertyEditorAsParameterEditor() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends JPanel {",
            "  public void foo(int foo) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "      <method name='foo'>",
            "        <parameter type='int'>",
            "          <editor id='displayExpression'/>",
            "        </parameter>",
            "      </method>",
            "  </methods>",
            "  <method-single-property title='myFoo' method='foo(int)'/>",
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
    // foo(int)
    {
      String id = "foo(int)";
      GenericPropertyDescription property = description.getProperty(id);
      assertEquals("myFoo", property.getTitle());
      assertThat(property.getEditor()).isSameAs(DisplayExpressionPropertyEditor.INSTANCE);
    }
  }
}
