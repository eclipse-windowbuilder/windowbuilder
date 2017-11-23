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

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.ITypedProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.IntegerPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.StringListPropertyEditor;
import org.eclipse.wb.internal.core.model.util.generic.ModelMethodPropertySupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorWarning;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link ModelMethodPropertySupport}.
 * 
 * @author scheglov_ke
 */
public class ModelMethodPropertyTest extends SwingModelTest {
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

    private String m_value = "abc";

    public Object getValue() {
      return m_value;
    }

    public void setValue(Object value) {
      m_value = (String) value;
    }
  }

  private void prepareMyPanel(String parameters) throws Exception {
    prepareMyPanel0(parameters + " type=java.lang.String");
  }

  private void prepareMyPanel0(String parameters) throws Exception {
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
            "    <parameter name='modelMethodProperty " + parameters + "'/>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
  }

  public void test_valueProperty() throws Exception {
    prepareMyPanel("getter=getValue setter=setValue title=value category=normal");
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
    // test "value" property
    Property property = panel.getPropertyByTitle("value");
    assertNotNull(property);
    assertSame(PropertyCategory.NORMAL, property.getCategory());
    assertSame(String.class, ((ITypedProperty) property).getType());
    // current value
    assertTrue(property.isModified());
    assertEquals("abc", property.getValue());
    // set new value
    property.setValue("123");
    assertEquals("123", property.getValue());
    // next time same Property should be returned
    assertSame(property, panel.getPropertyByTitle("value"));
  }

  public void test_primitiveType() throws Exception {
    prepareMyPanel0("getter=getValue setter=setValue title=value type=int");
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
    // test "value" property
    Property property = panel.getPropertyByTitle("value");
    assertNotNull(property);
    assertSame(IntegerPropertyEditor.INSTANCE, property.getEditor());
    assertSame(int.class, ((ITypedProperty) property).getType());
  }

  public void test_propertyEditor_StringList() throws Exception {
    prepareMyPanel("getter=getValue setter=setValue title=value editor=strings(A,B,C)");
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
    // test "value" property
    Property property = panel.getPropertyByTitle("value");
    assertNotNull(property);
    // test its editor
    {
      PropertyEditor propertyEditor = property.getEditor();
      assertInstanceOf(StringListPropertyEditor.class, propertyEditor);
      String[] strings = (String[]) ReflectionUtils.getFieldObject(propertyEditor, "m_strings");
      assertThat(strings).isEqualTo(new String[]{"A", "B", "C"});
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // No required parameter
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_noRequiredParameters_getter() throws Exception {
    prepareMyPanel("_getter=getValue setter=setValue title=value");
    parseContainer(
        "// filler filler filler",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "  }",
        "}");
    // check warnings
    List<EditorWarning> warnings = m_lastState.getWarnings();
    assertThat(warnings).hasSize(1);
    assertThat(warnings.get(0).getMessage()).contains("'getter'");
  }

  public void test_noRequiredParameters_setter() throws Exception {
    prepareMyPanel("getter=getValue _setter=setValue title=value");
    parseContainer(
        "// filler filler filler",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "  }",
        "}");
    // check warnings
    List<EditorWarning> warnings = m_lastState.getWarnings();
    assertThat(warnings).hasSize(1);
    assertThat(warnings.get(0).getMessage()).contains("'setter'");
  }

  public void test_noRequiredParameters_title() throws Exception {
    prepareMyPanel("getter=getValue setter=setValue _title=value");
    parseContainer(
        "// filler filler filler",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "  }",
        "}");
    // check warnings
    List<EditorWarning> warnings = m_lastState.getWarnings();
    assertThat(warnings).hasSize(1);
    assertThat(warnings.get(0).getMessage()).contains("'title'");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Invalid value for parameter
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_invalidParameter_getter() throws Exception {
    prepareMyPanel("getter=noSuchMethod setter=foo title=bar");
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

  public void test_invalidParameter_setter() throws Exception {
    prepareMyPanel("getter=getValue setter=noSuchMethod title=bar");
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