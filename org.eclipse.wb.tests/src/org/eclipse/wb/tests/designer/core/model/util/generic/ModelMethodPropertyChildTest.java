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
package org.eclipse.wb.tests.designer.core.model.util.generic;

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;
import org.eclipse.wb.internal.core.model.util.generic.ModelMethodPropertyChildSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.state.EditorWarning;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

/**
 * Test for {@link ModelMethodPropertyChildSupport}.
 * 
 * @author scheglov_ke
 */
public class ModelMethodPropertyChildTest extends SwingModelTest {
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
  public static class MyModel extends ContainerInfo {
    public MyModel(AstEditor editor,
        ComponentDescription description,
        CreationSupport creationSupport) throws Exception {
      super(editor, description, creationSupport);
    }

    private final Map<Object, String> m_values = Maps.newHashMap();

    public Object getValue(ComponentInfo component) {
      String value = m_values.get(component);
      return value != null ? value : Property.UNKNOWN_VALUE;
    }

    public void setValue(ComponentInfo component, Object value) {
      m_values.put(component, (String) value);
    }
  }

  private void prepareMyPanel(String parameters) throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends JPanel {",
            "  // filler",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <model class='" + MyModel.class.getName() + "'/>",
            "  <parameters>",
            "    <parameter name='modelMethodChildProperty " + parameters + "'/>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
  }

  public void test_valueProperty() throws Exception {
    prepareMyPanel("getter=getValue setter=setValue title=value category=normal type=java.lang.String"
        + " child="
        + ComponentInfo.class.getName());
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    add(new JButton());",
            "    add(new JTextField());",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
    ComponentInfo button = panel.getChildrenComponents().get(0);
    ComponentInfo text = panel.getChildrenComponents().get(1);
    // "value" property for "button"
    {
      Property property = button.getPropertyByTitle("value");
      assertNotNull(property);
      assertSame(PropertyCategory.NORMAL, property.getCategory());
      assertSame(StringPropertyEditor.INSTANCE, property.getEditor());
      // no initial value
      assertFalse(property.isModified());
      assertSame(Property.UNKNOWN_VALUE, property.getValue());
      // new value
      property.setValue("123");
      assertEquals("123", property.getValue());
      // same property always
      assertSame(property, button.getPropertyByTitle("value"));
    }
    // "value" property for "text"
    {
      Property property = text.getPropertyByTitle("value");
      assertNotNull(property);
      assertSame(PropertyCategory.NORMAL, property.getCategory());
      // no initial value
      assertFalse(property.isModified());
      assertSame(Property.UNKNOWN_VALUE, property.getValue());
      // new value
      property.setValue("123");
      assertEquals("123", property.getValue());
      // same property always
      assertSame(property, text.getPropertyByTitle("value"));
      assertNotSame(property, button.getPropertyByTitle("value"));
    }
    // no "value" property for "layout"
    {
      Property property = panel.getLayout().getPropertyByTitle("value");
      assertNull(property);
    }
  }

  public void test_noRequiredParameters() throws Exception {
    prepareMyPanel("");
    parseContainer(
        "// filler filler filler",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "  }",
        "}");
    // check warnings
    List<EditorWarning> warnings = m_lastState.getWarnings();
    assertThat(warnings).hasSize(1);
    assertThat(warnings.get(0).getMessage()).contains("'getter'").contains("'setter'").contains(
        "'title'");
  }

  public void test_noParameter_getter() throws Exception {
    prepareMyPanel("getter=noSuchMethod setter=foo title=bar type=java.lang.String");
    parseContainer(
        "// filler filler filler",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "  }",
        "}");
    // check warnings
    List<EditorWarning> warnings = m_lastState.getWarnings();
    assertThat(warnings).hasSize(1);
    assertThat(warnings.get(0).getMessage()).contains("Invalid").contains("getter");
  }

  public void test_noParameter_setter() throws Exception {
    prepareMyPanel("getter=getValue setter=noSuchMethod title=bar type=java.lang.String");
    parseContainer(
        "// filler filler filler",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "  }",
        "}");
    // check warnings
    List<EditorWarning> warnings = m_lastState.getWarnings();
    assertThat(warnings).hasSize(1);
    assertThat(warnings.get(0).getMessage()).contains("Invalid").contains("setter");
  }
}