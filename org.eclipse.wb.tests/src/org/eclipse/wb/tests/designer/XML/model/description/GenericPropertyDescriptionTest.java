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
package org.eclipse.wb.tests.designer.XML.model.description;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.BooleanPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.IntegerPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.xml.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.xml.model.property.accessor.FieldExpressionAccessor;
import org.eclipse.wb.internal.core.xml.model.property.accessor.MethodExpressionAccessor;
import org.eclipse.wb.internal.core.xml.model.property.converter.BooleanConverter;
import org.eclipse.wb.internal.core.xml.model.property.converter.EnumConverter;
import org.eclipse.wb.internal.core.xml.model.property.converter.IntegerConverter;
import org.eclipse.wb.internal.core.xml.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.xml.model.property.editor.EnumPropertyEditor;
import org.eclipse.wb.internal.core.xml.model.property.editor.StaticFieldPropertyEditor;
import org.eclipse.wb.tests.designer.XML.NoopConfigurablePropertyEditor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link GenericPropertyDescription} loading.
 * 
 * @author scheglov_ke
 */
public class GenericPropertyDescriptionTest extends AbstractCoreTest {
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
   * We should ignore case when description tries to reference not existing property.
   */
  public void test_ignoreNoSuchProperty() throws Exception {
    prepareMyComponent(new String[]{}, new String[]{
        "<property id='setNoSuchProperty(boolean)'>",
        "  <defaultValue value='false'/>",
        "</property>"});
    ComponentDescription description = getMyDescription();
    String id = "setNoSuchProperty(boolean)";
    // no such property, but description loaded
    GenericPropertyDescription property = description.getProperty(id);
    assertNull(property);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Types
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_boolean() throws Exception {
    prepareMyComponent(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public void setTest(boolean value) {",
        "}");
    ComponentDescription description = getMyDescription();
    // "test" property
    {
      String id = "setTest(boolean)";
      GenericPropertyDescription property = description.getProperty(id);
      assertNotNull(property);
      assertEquals(id, property.getId());
      assertEquals("test", property.getName());
      assertEquals("test", property.getTitle());
      assertSame(boolean.class, property.getType());
      assertSame(BooleanPropertyEditor.INSTANCE, property.getEditor());
      assertSame(BooleanConverter.INSTANCE, property.getConverter());
      assertInstanceOf(MethodExpressionAccessor.class, property.getAccessor());
      assertSame(PropertyCategory.NORMAL, property.getCategory());
      assertSame(Property.UNKNOWN_VALUE, property.getDefaultValue());
    }
  }

  public void test_int() throws Exception {
    prepareMyComponent(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public void setTest(int value) {",
        "}");
    ComponentDescription description = getMyDescription();
    // "test" property
    {
      String id = "setTest(int)";
      GenericPropertyDescription property = description.getProperty(id);
      assertNotNull(property);
      assertEquals(id, property.getId());
      assertEquals("test", property.getName());
      assertEquals("test", property.getTitle());
      assertSame(int.class, property.getType());
      assertSame(IntegerPropertyEditor.INSTANCE, property.getEditor());
      assertSame(IntegerConverter.INSTANCE, property.getConverter());
      assertInstanceOf(MethodExpressionAccessor.class, property.getAccessor());
      assertSame(PropertyCategory.NORMAL, property.getCategory());
      assertSame(Property.UNKNOWN_VALUE, property.getDefaultValue());
    }
  }

  public void test_String() throws Exception {
    prepareMyComponent(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public void setTest(String value) {",
        "}");
    ComponentDescription description = getMyDescription();
    // "test" property
    {
      String id = "setTest(java.lang.String)";
      GenericPropertyDescription property = description.getProperty(id);
      assertNotNull(property);
      assertEquals(id, property.getId());
      assertEquals("test", property.getName());
      assertEquals("test", property.getTitle());
      assertSame(String.class, property.getType());
      assertSame(StringPropertyEditor.INSTANCE, property.getEditor());
      assertSame(StringConverter.INSTANCE, property.getConverter());
      assertInstanceOf(MethodExpressionAccessor.class, property.getAccessor());
      assertSame(PropertyCategory.NORMAL, property.getCategory());
      assertSame(Property.UNKNOWN_VALUE, property.getDefaultValue());
    }
  }

  public void test_Enum() throws Exception {
    setFileContentSrc(
        "test/MyEnum.java",
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public enum MyEnum {",
            "  A, B, C",
            "}"));
    prepareMyComponent(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public void setTest(MyEnum value) {",
        "}");
    ComponentDescription description = getMyDescription();
    // "test" property
    {
      String id = "setTest(test.MyEnum)";
      GenericPropertyDescription property = description.getProperty(id);
      assertNotNull(property);
      assertEquals(id, property.getId());
      assertEquals("test", property.getName());
      assertEquals("test", property.getTitle());
      assertEquals("test.MyEnum", property.getType().getName());
      assertSame(EnumPropertyEditor.INSTANCE, property.getEditor());
      assertSame(EnumConverter.INSTANCE, property.getConverter());
      assertInstanceOf(MethodExpressionAccessor.class, property.getAccessor());
      assertSame(PropertyCategory.NORMAL, property.getCategory());
      assertSame(Property.UNKNOWN_VALUE, property.getDefaultValue());
    }
  }

  /**
   * If "setter" has capitalized name, then first letter should be "uncapitalized".
   */
  public void test_attribute_setHTML() throws Exception {
    prepareMyComponent(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public void setHTML(boolean value) {",
        "}");
    ComponentDescription description = getMyDescription();
    // "test" property
    {
      String id = "setHTML(boolean)";
      GenericPropertyDescription property = description.getProperty(id);
      assertEquals("HTML", property.getTitle());
      assertEquals("hTML", property.getAccessor().getAttribute());
    }
  }

  /**
   * In XML we see only public methods.
   */
  public void test_method_protected() throws Exception {
    prepareMyComponent(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "protected void setTest(boolean value) {",
        "}");
    ComponentDescription description = getMyDescription();
    // no "test" property
    {
      String id = "setTest(boolean)";
      GenericPropertyDescription property = description.getProperty(id);
      assertNull(property);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Field
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_field() throws Exception {
    prepareMyComponent(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public boolean test;");
    ComponentDescription description = getMyDescription();
    // "test" property
    {
      String id = "test";
      GenericPropertyDescription property = description.getProperty(id);
      assertNotNull(property);
      assertEquals(id, property.getId());
      assertEquals("test", property.getName());
      assertEquals("test", property.getTitle());
      assertSame(boolean.class, property.getType());
      assertSame(BooleanPropertyEditor.INSTANCE, property.getEditor());
      assertSame(BooleanConverter.INSTANCE, property.getConverter());
      assertInstanceOf(FieldExpressionAccessor.class, property.getAccessor());
      assertSame(PropertyCategory.NORMAL, property.getCategory());
      assertSame(Property.UNKNOWN_VALUE, property.getDefaultValue());
    }
  }

  /**
   * No editor for type {@link Object} and no configurable property editor set. So, no property.
   */
  public void test_field_no_editor() throws Exception {
    prepareMyComponent(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public Object test;");
    ComponentDescription description = getMyDescription();
    // "test" property
    {
      String id = "test";
      GenericPropertyDescription property = description.getProperty(id);
      assertNull(property);
    }
  }

  /**
   * Configurable property editor set. The property should exist.
   */
  public void test_field_has_configurableEditor() throws Exception {
    prepareMyComponent(new String[]{
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public Object test;"}, new String[]{
        "<property id='test'>",
        "<editor id='testEditor'/>",
        "</property>"});
    ComponentDescription description = getMyDescription();
    // "test" property
    {
      String id = "test";
      GenericPropertyDescription property = description.getProperty(id);
      assertNotNull(property);
      assertEquals(id, property.getId());
      assertEquals("test", property.getName());
      assertEquals("test", property.getTitle());
      assertSame(Object.class, property.getType());
      assertSame(NoopConfigurablePropertyEditor.class, property.getEditor().getClass());
    }
  }

  /**
   * In XML we see only public fields.
   */
  public void test_field_protected() throws Exception {
    prepareMyComponent(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "protected boolean test;");
    ComponentDescription description = getMyDescription();
    // no "test" property
    {
      String id = "test";
      GenericPropertyDescription property = description.getProperty(id);
      assertNull(property);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Setter
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * No editor for type {@link Object} and no configurable property editor set. So, no property.
   */
  public void test_setter_no_editor() throws Exception {
    prepareMyComponent(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public void setTest(Object value) {",
        "}");
    ComponentDescription description = getMyDescription();
    // "test" property
    {
      String id = "setTest(java.lang.Object)";
      GenericPropertyDescription property = description.getProperty(id);
      assertNull(property);
    }
  }

  /**
   * Configurable property editor set. The property should exist.
   */
  public void test_setter_has_configurableEditor() throws Exception {
    prepareMyComponent(new String[]{
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public void setTest(Object value) {",
        "}"}, new String[]{
        "<property id='setTest(java.lang.Object)'>",
        "<editor id='testEditor'/>",
        "</property>"});
    ComponentDescription description = getMyDescription();
    // "test" property
    {
      String id = "setTest(java.lang.Object)";
      GenericPropertyDescription property = description.getProperty(id);
      assertNotNull(property);
      assertEquals(id, property.getId());
      assertEquals("test", property.getName());
      assertEquals("test", property.getTitle());
      assertSame(Object.class, property.getType());
      assertSame(NoopConfigurablePropertyEditor.class, property.getEditor().getClass());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Category
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_category_usingNames_preferred() throws Exception {
    prepareMyComponent(new String[]{
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public void setTest(boolean value) {",
        "}"}, new String[]{"<properties-preferred names='test'/>"});
    ComponentDescription description = getMyDescription();
    // "test" property
    {
      String id = "setTest(boolean)";
      GenericPropertyDescription property = description.getProperty(id);
      assertSame(PropertyCategory.PREFERRED, property.getCategory());
    }
  }

  public void test_category_usingNames_normal() throws Exception {
    prepareMyComponent(new String[]{
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public void setTest(boolean value) {",
        "}"}, new String[]{
        "<properties-preferred names='test'/>",
        "<properties-normal names='test'/>"});
    ComponentDescription description = getMyDescription();
    // "test" property
    {
      String id = "setTest(boolean)";
      GenericPropertyDescription property = description.getProperty(id);
      assertSame(PropertyCategory.NORMAL, property.getCategory());
    }
  }

  public void test_category_usingNames_advanced() throws Exception {
    prepareMyComponent(new String[]{
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public void setTest(boolean value) {",
        "}"}, new String[]{"<properties-advanced names='test'/>"});
    ComponentDescription description = getMyDescription();
    // "test" property
    {
      String id = "setTest(boolean)";
      GenericPropertyDescription property = description.getProperty(id);
      assertSame(PropertyCategory.ADVANCED, property.getCategory());
    }
  }

  public void test_category_usingNames_hidden() throws Exception {
    prepareMyComponent(new String[]{
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public void setTest(boolean value) {",
        "}"}, new String[]{"<properties-hidden names='test'/>"});
    ComponentDescription description = getMyDescription();
    // "test" property
    {
      String id = "setTest(boolean)";
      GenericPropertyDescription property = description.getProperty(id);
      assertSame(PropertyCategory.HIDDEN, property.getCategory());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection by ID
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_selectionByID_useMethodSignature() throws Exception {
    prepareMyComponent(new String[]{
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public boolean test;",
        "public void setTest(boolean value) {",
        "}"}, new String[]{"<properties-preferred names='setTest(boolean)'/>"});
    ComponentDescription description = getMyDescription();
    // "test" field
    {
      String id = "test";
      GenericPropertyDescription property = description.getProperty(id);
      assertSame(PropertyCategory.NORMAL, property.getCategory());
    }
    // "setTest()" method
    {
      String id = "setTest(boolean)";
      GenericPropertyDescription property = description.getProperty(id);
      assertSame(PropertyCategory.PREFERRED, property.getCategory());
    }
  }

  public void test_selectionByID_useFieldName() throws Exception {
    prepareMyComponent(new String[]{
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public boolean test;"}, new String[]{"<properties-preferred names='test'/>"});
    ComponentDescription description = getMyDescription();
    // "test" field
    {
      String id = "test";
      GenericPropertyDescription property = description.getProperty(id);
      assertSame(PropertyCategory.PREFERRED, property.getCategory());
    }
  }

  public void test_selectionByID_forceMethod() throws Exception {
    prepareMyComponent(new String[]{
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public boolean test;",
        "public void setTest(boolean value) {",
        "}"}, new String[]{"<properties-preferred names='m:test'/>"});
    ComponentDescription description = getMyDescription();
    // "test" field
    {
      String id = "test";
      GenericPropertyDescription property = description.getProperty(id);
      assertSame(PropertyCategory.NORMAL, property.getCategory());
    }
    // "setTest()" method
    {
      String id = "setTest(boolean)";
      GenericPropertyDescription property = description.getProperty(id);
      assertSame(PropertyCategory.PREFERRED, property.getCategory());
    }
  }

  public void test_selectionByID_forceField() throws Exception {
    prepareMyComponent(new String[]{
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public boolean test;",
        "public void setTest(boolean value) {",
        "}"}, new String[]{"<properties-preferred names='f:test'/>"});
    ComponentDescription description = getMyDescription();
    // "test" field
    {
      String id = "test";
      GenericPropertyDescription property = description.getProperty(id);
      assertSame(PropertyCategory.PREFERRED, property.getCategory());
    }
    // "setTest()" method
    {
      String id = "setTest(boolean)";
      GenericPropertyDescription property = description.getProperty(id);
      assertSame(PropertyCategory.NORMAL, property.getCategory());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Flags
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_tag() throws Exception {
    prepareMyComponent(new String[]{
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public void setTest(boolean value) {",
        "}"}, new String[]{
        "<property-tag name='test' tag='tag_1' value='value_1'/>",
        "<property-tag name='test' tag='tag_2' value='true'/>"});
    ComponentDescription description = getMyDescription();
    // "test" property
    {
      String id = "setTest(boolean)";
      GenericPropertyDescription property = description.getProperty(id);
      assertEquals(null, property.getTag("tag_noSuch"));
      assertEquals("value_1", property.getTag("tag_1"));
      assertEquals("true", property.getTag("tag_2"));
      assertTrue(property.hasTrueTag("tag_2"));
    }
  }

  /**
   * Test for {@link GenericPropertyDescription#getTitle()} and "title" property tag.
   */
  public void test_tag_title() throws Exception {
    prepareMyComponent(new String[]{
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public void setA(boolean value) {",
        "}",
        "public void setB(boolean value) {",
        "}",}, new String[]{"<property-tag name='setA(boolean)' tag='title' value='myTitle'/>"});
    ComponentDescription description = getMyDescription();
    // "A" property
    {
      String id = "setA(boolean)";
      GenericPropertyDescription property = description.getProperty(id);
      assertEquals("myTitle", property.getTitle());
      assertEquals("a", property.getAccessor().getAttribute());
    }
    // "B" property
    {
      String id = "setB(boolean)";
      GenericPropertyDescription property = description.getProperty(id);
      assertEquals("b", property.getTitle());
      assertEquals("b", property.getAccessor().getAttribute());
    }
  }

  public void test_tag_noDefaultValue_noTag() throws Exception {
    prepareMyComponent(new String[]{
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public void setTest(boolean value) {",
        "}"}, new String[]{});
    ComponentDescription description = getMyDescription();
    // "test" property
    {
      String id = "setTest(boolean)";
      GenericPropertyDescription property = description.getProperty(id);
      assertFalse(property.hasTrueTag(ExpressionAccessor.NO_DEFAULT_VALUE_TAG));
    }
  }

  public void test_tag_noDefaultValue_hasTag() throws Exception {
    prepareMyComponent(new String[]{
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public void setTest(boolean value) {",
        "}"}, new String[]{"<properties-noDefaultValue names='test'/>"});
    ComponentDescription description = getMyDescription();
    // "test" property
    {
      String id = "setTest(boolean)";
      GenericPropertyDescription property = description.getProperty(id);
      assertTrue(property.hasTrueTag(ExpressionAccessor.NO_DEFAULT_VALUE_TAG));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Configure property
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_configureProperty_category() throws Exception {
    prepareMyComponent(new String[]{
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public void setTest(boolean value) {",
        "}"}, new String[]{
        "<!-- ===================== -->",
        "<property id='setTest(boolean)'>",
        "  <category value='preferred'/>",
        "</property>"});
    ComponentDescription description = getMyDescription();
    // "test" property
    {
      String id = "setTest(boolean)";
      GenericPropertyDescription property = description.getProperty(id);
      assertSame(PropertyCategory.PREFERRED, property.getCategory());
    }
  }

  public void test_configureProperty_defaultValue() throws Exception {
    prepareMyComponent(new String[]{
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public void setTest(int value) {",
        "}"}, new String[]{
        "<!-- ===================== -->",
        "<property id='setTest(int)'>",
        "  <defaultValue value='123'/>",
        "</property>"});
    ComponentDescription description = getMyDescription();
    // "test" property
    {
      String id = "setTest(int)";
      GenericPropertyDescription property = description.getProperty(id);
      assertEquals(123, property.getDefaultValue());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Configurable editor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Attempt to reference ID for which no configurable editor, causes loading failure.
   */
  public void test_configureProperty_editorWithID_noSuchID() throws Exception {
    prepareMyComponent(new String[]{
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public void setTest(int value) {",
        "}"}, new String[]{
        "<!-- ===================== -->",
        "<property id='setTest(int)'>",
        "  <editor id='noEditorWithSuchID'/>",
        "</property>"});
    ComponentDescription description = getMyDescription();
    // no editor, so no property
    String id = "setTest(int)";
    GenericPropertyDescription property = description.getProperty(id);
    assertNull(property);
  }

  /**
   * Test for configurable editor, use "parameter" tag.
   */
  public void test_configureProperty_editorWithID_parameter() throws Exception {
    prepareMyComponent(new String[]{
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public void setTest(int value) {",
        "}"}, new String[]{
        "<!-- ===================== -->",
        "<property id='setTest(int)'>",
        "  <editor id='staticField'>",
        "    <parameter name='class'>org.eclipse.swt.SWT</parameter>",
        "    <parameter name='fields'>LEFT CENTER RIGHT</parameter>",
        "  </editor>",
        "</property>"});
    ComponentDescription description = getMyDescription();
    // "test" property
    String id = "setTest(int)";
    GenericPropertyDescription property = description.getProperty(id);
    StaticFieldPropertyEditor editor = (StaticFieldPropertyEditor) property.getEditor();
    assertStaticFieldEditor_fields(editor, "LEFT", "CENTER", "RIGHT");
  }

  /**
   * Test for configurable editor, use "parameter-list" tag.
   */
  public void test_configureProperty_editorWithID_parameterList() throws Exception {
    prepareMyComponent(new String[]{
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public void setTest(int value) {",
        "}"}, new String[]{
        "<!-- ===================== -->",
        "<property id='setTest(int)'>",
        "  <editor id='staticField'>",
        "    <parameter name='class'>org.eclipse.swt.SWT</parameter>",
        "    <parameter-list name='fields'>LEFT</parameter-list>",
        "    <parameter-list name='fields'>CENTER</parameter-list>",
        "    <parameter-list name='fields'>RIGHT</parameter-list>",
        "  </editor>",
        "</property>"});
    ComponentDescription description = getMyDescription();
    // "test" property
    String id = "setTest(int)";
    GenericPropertyDescription property = description.getProperty(id);
    StaticFieldPropertyEditor editor = (StaticFieldPropertyEditor) property.getEditor();
    assertStaticFieldEditor_fields(editor, "LEFT", "CENTER", "RIGHT");
  }

  private static void assertStaticFieldEditor_fields(StaticFieldPropertyEditor editor,
      String... expected) {
    String[] actual = (String[]) ReflectionUtils.getFieldObject(editor, "m_names");
    assertThat(actual).isEqualTo(expected);
  }
}