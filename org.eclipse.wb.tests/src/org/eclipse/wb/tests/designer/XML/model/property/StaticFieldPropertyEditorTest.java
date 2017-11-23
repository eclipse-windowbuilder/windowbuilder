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
package org.eclipse.wb.tests.designer.XML.model.property;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.IExceptionConstants;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.xml.model.property.GenericProperty;
import org.eclipse.wb.internal.core.xml.model.property.editor.StaticFieldPropertyEditor;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XML.model.description.AbstractCoreTest;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.swing.SwingConstants;

/**
 * Test for {@link StaticFieldPropertyEditor}.
 * 
 * @author scheglov_ke
 */
public class StaticFieldPropertyEditorTest extends AbstractCoreTest {
  private Shell m_shell;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    // prepare test Shell
    m_shell = new Shell();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    if (m_shell != null) {
      m_shell.dispose();
      m_shell = null;
    }
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
  // Configure
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_configure_noClass() throws Exception {
    prepareMyComponent(new String[]{
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public void setTest(int value) {",
        "}"}, new String[]{
        "<!-- ===================== -->",
        "<property id='setTest(int)'>",
        "  <editor id='staticField'>",
        "  </editor>",
        "</property>"});
    //
    try {
      getMyDescription();
    } catch (Throwable e) {
      DesignerException de = (DesignerException) DesignerExceptionUtils.getRootCause(e);
      assertEquals(IExceptionConstants.DESCRIPTION_EDITOR_STATIC_FIELD, de.getCode());
    }
  }

  public void test_configure_noFields() throws Exception {
    prepareMyComponent(new String[]{
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public void setTest(int value) {",
        "}"}, new String[]{
        "<!-- ===================== -->",
        "<property id='setTest(int)'>",
        "  <editor id='staticField'>",
        "    <parameter name='class'>org.eclipse.swt.SWT</parameter>",
        "  </editor>",
        "</property>"});
    //
    try {
      getMyDescription();
    } catch (Throwable e) {
      DesignerException de = (DesignerException) DesignerExceptionUtils.getRootCause(e);
      assertEquals(IExceptionConstants.DESCRIPTION_EDITOR_STATIC_FIELD, de.getCode());
    }
  }

  /**
   * Use single "parameter" with name "fields".
   */
  public void test_configure_singleFieldsParameter() throws Exception {
    prepareComponent_withField();
    ComponentDescription description = getMyDescription();
    // "test" property
    GenericPropertyDescription property = description.getProperty("test");
    StaticFieldPropertyEditor editor = (StaticFieldPropertyEditor) property.getEditor();
    assertConfiguration(
        editor,
        "org.eclipse.swt.SWT",
        new String[]{"LEFT", "CENTER", "RIGHT"},
        new String[]{"LEFT", "CENTER", "RIGHT"},
        new Object[]{SWT.LEFT, SWT.CENTER, SWT.RIGHT});
  }

  /**
   * Use single "parameter-list" with name "fields".
   */
  public void test_configure_parameterList() throws Exception {
    prepareComponent_withoutDefaultValue();
    ComponentDescription description = getMyDescription();
    // "test" property
    GenericPropertyDescription property = description.getProperty("test");
    StaticFieldPropertyEditor editor = (StaticFieldPropertyEditor) property.getEditor();
    assertConfiguration(
        editor,
        "org.eclipse.swt.SWT",
        new String[]{"LEFT", "CENTER", "RIGHT"},
        new String[]{"LEFT", "CENTER", "RIGHT"},
        new Object[]{SWT.LEFT, SWT.CENTER, SWT.RIGHT});
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Configure
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Top level class, all fields are valid.
   */
  public void test_configure_1() throws Exception {
    StaticFieldPropertyEditor editor = new StaticFieldPropertyEditor();
    editor.configure(SwingConstants.class, new String[]{"LEFT", "RIGHT"});
    //
    String e_classSourceName = "javax.swing.SwingConstants";
    String[] e_names = new String[]{"LEFT", "RIGHT"};
    String[] e_titles = new String[]{"LEFT", "RIGHT"};
    Object[] e_values = new Object[]{SwingConstants.LEFT, SwingConstants.RIGHT};
    assertConfiguration(editor, e_classSourceName, e_names, e_titles, e_values);
  }

  /**
   * Top level class, one field does not exist, should be skipped.
   */
  public void test_configure_2() throws Exception {
    StaticFieldPropertyEditor editor = new StaticFieldPropertyEditor();
    editor.configure(SwingConstants.class, new String[]{"LEFT", "noSuchField", "RIGHT"});
    //
    String e_classSourceName = "javax.swing.SwingConstants";
    String[] e_names = new String[]{"LEFT", "RIGHT"};
    String[] e_titles = new String[]{"LEFT", "RIGHT"};
    Object[] e_values = new Object[]{SwingConstants.LEFT, SwingConstants.RIGHT};
    assertConfiguration(editor, e_classSourceName, e_names, e_titles, e_values);
  }

  /**
   * Top level class, all fields are valid. Specify title in field description.
   */
  public void test_configure_3() throws Exception {
    StaticFieldPropertyEditor editor = new StaticFieldPropertyEditor();
    editor.configure(SwingConstants.class, new String[]{"LEFT:asLeft", "RIGHT:asRight"});
    //
    String e_classSourceName = "javax.swing.SwingConstants";
    String[] e_names = new String[]{"LEFT", "RIGHT"};
    String[] e_titles = new String[]{"asLeft", "asRight"};
    Object[] e_values = new Object[]{SwingConstants.LEFT, SwingConstants.RIGHT};
    assertConfiguration(editor, e_classSourceName, e_names, e_titles, e_values);
  }

  /**
   * Special <code>*remove</code> field.
   */
  public void test_configure_4() throws Exception {
    StaticFieldPropertyEditor editor = new StaticFieldPropertyEditor();
    editor.configure(SwingConstants.class, new String[]{"LEFT", "*remove", "RIGHT"});
    //
    String e_classSourceName = "javax.swing.SwingConstants";
    String[] e_names = new String[]{"LEFT", null, "RIGHT"};
    String[] e_titles = new String[]{"LEFT", "", "RIGHT"};
    Object[] e_values = new Object[]{SwingConstants.LEFT, null, SwingConstants.RIGHT};
    assertConfiguration(editor, e_classSourceName, e_names, e_titles, e_values);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Configuration assertions
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Asserts that given {@link StaticFieldPropertyEditor} has expected configuration.
   */
  private static void assertConfiguration(StaticFieldPropertyEditor editor,
      String e_className,
      String[] e_names,
      String[] e_titles,
      Object[] e_values) throws Exception {
    {
      Class<?> actialClass = (Class<?>) ReflectionUtils.getFieldObject(editor, "m_class");
      assertEquals(e_className, actialClass.getName());
    }
    assertArrayField_equals(editor, "m_names", e_names);
    assertArrayField_equals(editor, "m_titles", e_titles);
    assertArrayField_equals(editor, "m_values", e_values);
  }

  private static void assertArrayField_equals(Object editor, String fieldName, Object[] expected) {
    Object[] actual = (Object[]) ReflectionUtils.getFieldObject(editor, fieldName);
    assertThat(actual).isEqualTo(expected);
  }

  private void prepareComponent_withField() throws Exception {
    prepareMyComponent(new String[]{
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public int test = SWT.LEFT;"}, new String[]{
        "<!-- ===================== -->",
        "<property id='test'>",
        "  <editor id='staticField'>",
        "    <parameter name='class'>org.eclipse.swt.SWT</parameter>",
        "    <parameter name='fields'>LEFT CENTER RIGHT</parameter>",
        "  </editor>",
        "</property>"});
  }

  private void prepareComponent_withoutDefaultValue() throws Exception {
    prepareMyComponent(new String[]{
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public int test;"}, new String[]{
        "<!-- ===================== -->",
        "<property id='test'>",
        "  <editor id='staticField'>",
        "    <parameter name='class'>org.eclipse.swt.SWT</parameter>",
        "    <parameter-list name='fields'>LEFT</parameter-list>",
        "    <parameter-list name='fields'>CENTER</parameter-list>",
        "    <parameter-list name='fields'>RIGHT</parameter-list>",
        "  </editor>",
        "</property>"});
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getClipboardSource()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getClipboardSource_noValue() throws Exception {
    prepareComponent_withoutDefaultValue();
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <t:MyComponent wbp:name='component'/>",
        "</Shell>");
    refresh();
    XmlObjectInfo component = getObjectByName("component");
    GenericProperty property = (GenericProperty) component.getPropertyByTitle("test");
    StaticFieldPropertyEditor propertyEditor = (StaticFieldPropertyEditor) property.getEditor();
    // no value
    assertEquals(null, propertyEditor.getClipboardSource(property));
  }

  public void test_getClipboardSource_hasValue() throws Exception {
    prepareComponent_withField();
    ControlInfo shell =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <t:MyComponent wbp:name='component' test='LEFT'/>",
            "</Shell>");
    shell.refresh();
    XmlObjectInfo component = getObjectByName("component");
    GenericProperty property = (GenericProperty) component.getPropertyByTitle("test");
    StaticFieldPropertyEditor propertyEditor = (StaticFieldPropertyEditor) property.getEditor();
    // has text
    assertEquals("LEFT", propertyEditor.getClipboardSource(property));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IExpressionPropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link StaticFieldPropertyEditor#setValue(Property, Object)}.
   */
  public void test_setValue_GenericProperty() throws Exception {
    prepareComponent_withField();
    ControlInfo shell =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <t:MyComponent wbp:name='component' test='LEFT'/>",
            "</Shell>");
    shell.refresh();
    XmlObjectInfo component = getObjectByName("component");
    GenericProperty property = (GenericProperty) component.getPropertyByTitle("test");
    //
    property.setValue(SWT.CENTER);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <t:MyComponent wbp:name='component' test='CENTER'/>",
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getText()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getText_noValue() throws Exception {
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
    ControlInfo shell =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <t:MyComponent wbp:name='component'/>",
            "</Shell>");
    shell.refresh();
    XmlObjectInfo component = getObjectByName("component");
    GenericProperty property = (GenericProperty) component.getPropertyByTitle("test");
    // no value
    assertEquals(null, getPropertyText(property));
  }

  public void test_getText_hasValue() throws Exception {
    prepareComponent_withField();
    ControlInfo shell =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <t:MyComponent wbp:name='component' test='LEFT'/>",
            "</Shell>");
    shell.refresh();
    XmlObjectInfo component = getObjectByName("component");
    GenericProperty property = (GenericProperty) component.getPropertyByTitle("test");
    // has text
    assertEquals("LEFT", getPropertyText(property));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Combo
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_comboMethods() throws Exception {
    prepareComponent_withField();
    ControlInfo shell =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <t:MyComponent wbp:name='component' test='LEFT'/>",
            "</Shell>");
    shell.refresh();
    XmlObjectInfo component = getObjectByName("component");
    GenericProperty property = (GenericProperty) component.getPropertyByTitle("test");
    // add items
    addComboPropertyItems(property);
    // check items
    {
      List<String> items = getComboPropertyItems();
      assertThat(items).containsExactly("LEFT", "CENTER", "RIGHT");
    }
    // select current item
    {
      setComboPropertySelection(1);
      setComboPropertySelection(property);
      assertEquals(0, getComboPropertySelection());
    }
    // set non-default value
    {
      setComboPropertyValue(property, 1);
      assertXML(
          "// filler filler filler filler filler",
          "<Shell>",
          "  <t:MyComponent wbp:name='component' test='CENTER'/>",
          "</Shell>");
    }
    // set default value
    {
      setComboPropertyValue(property, 0);
      assertXML(
          "// filler filler filler filler filler",
          "<Shell>",
          "  <t:MyComponent wbp:name='component'/>",
          "</Shell>");
    }
  }

  public void test_comboMethods_removeValue() throws Exception {
    prepareMyComponent(new String[]{
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public int test = SWT.LEFT;"}, new String[]{
        "<!-- ===================== -->",
        "<property id='test'>",
        "  <editor id='staticField'>",
        "    <parameter name='class'>org.eclipse.swt.SWT</parameter>",
        "    <parameter name='fields'>*remove LEFT RIGHT</parameter>",
        "  </editor>",
        "</property>"});
    ControlInfo shell =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <t:MyComponent wbp:name='component' test='LEFT'/>",
            "</Shell>");
    shell.refresh();
    XmlObjectInfo component = getObjectByName("component");
    GenericProperty property = (GenericProperty) component.getPropertyByTitle("test");
    // add items
    addComboPropertyItems(property);
    // check items
    {
      List<String> items = getComboPropertyItems();
      assertThat(items).containsExactly("", "LEFT", "RIGHT");
    }
    // set "*remove"
    {
      setComboPropertyValue(property, 0);
      assertXML(
          "// filler filler filler filler filler",
          "<Shell>",
          "  <t:MyComponent wbp:name='component'/>",
          "</Shell>");
    }
  }
}