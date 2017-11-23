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
package org.eclipse.wb.tests.designer.XML.model.generic;

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;
import org.eclipse.wb.internal.core.model.util.generic.ModelMethodPropertyChildSupport;
import org.eclipse.wb.internal.core.utils.state.EditorWarning;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XML.model.description.AbstractCoreTest;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang.ArrayUtils;

import java.util.List;
import java.util.Map;

/**
 * Test for {@link ModelMethodPropertyChildSupport}.
 * 
 * @author scheglov_ke
 */
public class ModelMethodPropertyChildTest extends AbstractCoreTest {
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
  public static class MyModel extends CompositeInfo {
    public MyModel(EditorContext context,
        ComponentDescription description,
        CreationSupport creationSupport) throws Exception {
      super(context, description, creationSupport);
    }

    private final Map<Object, String> m_values = Maps.newHashMap();

    public Object getValue(ControlInfo component) {
      String value = m_values.get(component);
      return value != null ? value : Property.UNKNOWN_VALUE;
    }

    public void setValue(ControlInfo component, Object value) {
      m_values.put(component, (String) value);
    }
  }

  private void prepareMyPanel(String parameters) throws Exception {
    prepareMyComponent(ArrayUtils.EMPTY_STRING_ARRAY, new String[]{
        "  <x-model class='" + MyModel.class.getName() + "'/>",
        "  <parameters>",
        "    <parameter name='x-modelMethodChildProperty " + parameters + "'/>",
        "  </parameters>"});
  }

  public void test_valueProperty() throws Exception {
    prepareMyPanel("getter=getValue setter=setValue title=value category=normal type=java.lang.String"
        + " child="
        + ControlInfo.class.getName());
    CompositeInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<t:MyComponent>",
            "  <Button wbp:name='button'/>",
            "  <Text wbp:name='text'/>",
            "</t:MyComponent>");
    refresh();
    ControlInfo button = getObjectByName("button");
    ControlInfo text = getObjectByName("text");
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
    parse("<t:MyComponent/>");
    // check warnings
    List<EditorWarning> warnings = m_lastContext.getWarnings();
    assertThat(warnings).hasSize(1);
    assertThat(warnings.get(0).getMessage()).contains("'getter'").contains("'setter'").contains(
        "'title'");
  }

  public void test_noParameter_getter() throws Exception {
    prepareMyPanel("getter=noSuchMethod setter=foo title=bar type=java.lang.String");
    parse("<t:MyComponent/>");
    // check warnings
    List<EditorWarning> warnings = m_lastContext.getWarnings();
    assertThat(warnings).hasSize(1);
    assertThat(warnings.get(0).getMessage()).contains("Invalid").contains("getter");
  }

  public void test_noParameter_setter() throws Exception {
    prepareMyPanel("getter=getValue setter=noSuchMethod title=bar type=java.lang.String");
    parse("<t:MyComponent/>");
    // check warnings
    List<EditorWarning> warnings = m_lastContext.getWarnings();
    assertThat(warnings).hasSize(1);
    assertThat(warnings.get(0).getMessage()).contains("Invalid").contains("setter");
  }
}