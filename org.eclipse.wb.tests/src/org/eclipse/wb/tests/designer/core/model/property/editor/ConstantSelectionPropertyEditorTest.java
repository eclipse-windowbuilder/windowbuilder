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
package org.eclipse.wb.tests.designer.core.model.property.editor;

import com.google.common.collect.Sets;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.ConstantSelectionPropertyEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.swt.widgets.Shell;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

/**
 * Test for {@link ConstantSelectionPropertyEditor}.
 * 
 * @author scheglov_ke
 */
public class ConstantSelectionPropertyEditorTest extends SwingModelTest {
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
    // prepare JDT elements
    setFileContentSrc(
        "test/PrefConstants.java",
        getTestSource(
            "public interface PrefConstants {",
            "  String ID_1 = 'id 1';",
            "  int ID_2 = 2;",
            "  int ID_3 = 3;",
            "  String ID_4 = 'id 4';",
            "}"));
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public MyPanel() {",
            "  }",
            "  public void setStringId(String id) {",
            "  }",
            "  public void setStringId2(String id) {",
            "  }",
            "  public void setIntId(int id) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <property id='setStringId(java.lang.String)'>",
            "    <editor id='constantSelection'>",
            "      <parameter name='type'>java.lang.String</parameter>",
            "    </editor>",
            "  </property>",
            "  <property id='setStringId2(java.lang.String)'>",
            "    <editor id='constantSelection'>",
            "      <parameter name='type'>java.lang.String</parameter>",
            "    </editor>",
            "  </property>",
            "  <property id='setIntId(int)'>",
            "    <editor id='constantSelection'>",
            "      <parameter name='type'>int</parameter>",
            "    </editor>",
            "  </property>",
            "</component>"));
    waitForAutoBuild();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    if (m_shell != null) {
      m_shell.dispose();
      m_shell = null;
    }
    // clean-up fields
    m_property = null;
    m_propertyEditor = null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Helpers
  //
  ////////////////////////////////////////////////////////////////////////////
  private final String m_propertyName = "stringId";
  private Property m_property;
  private ConstantSelectionPropertyEditor m_propertyEditor;

  private void prepareProperty() throws Exception {
    m_property = m_lastParseInfo.getPropertyByTitle(m_propertyName);
    m_propertyEditor = (ConstantSelectionPropertyEditor) m_property.getEditor();
  }

  /**
   * Invokes {@link ConstantSelectionPropertyEditor#getText(Property)}.
   */
  private String getText() throws Exception {
    prepareProperty();
    return (String) ReflectionUtils.invokeMethod2(
        m_propertyEditor,
        "getText",
        Property.class,
        m_property);
  }

  /**
   * Invokes {@link ConstantSelectionPropertyEditor#getField(GenericProperty)}.
   */
  private IField getField() throws Exception {
    prepareProperty();
    return (IField) ReflectionUtils.invokeMethod2(
        m_propertyEditor,
        "getField",
        GenericProperty.class,
        m_property);
  }

  /**
   * Invokes {@link ConstantSelectionPropertyEditor#getType(GenericProperty)}.
   */
  private IType getType() throws Exception {
    prepareProperty();
    return (IType) ReflectionUtils.invokeMethod2(
        m_propertyEditor,
        "getType",
        GenericProperty.class,
        m_property);
  }

  /**
   * Invokes {@link ConstantSelectionPropertyEditor#getFields(IType)}.
   */
  @SuppressWarnings("unchecked")
  private List<IField> getFields() throws Exception {
    prepareProperty();
    return (List<IField>) ReflectionUtils.invokeMethod2(
        m_propertyEditor,
        "getFields",
        IType.class,
        getType());
  }

  /**
   * Invokes {@link ConstantSelectionPropertyEditor#setField(GenericProperty, IField)}.
   */
  private void setField(IField field) throws Exception {
    prepareProperty();
    ReflectionUtils.invokeMethod2(
        m_propertyEditor,
        "setField",
        GenericProperty.class,
        IField.class,
        m_property,
        field);
  }

  /**
   * Invokes {@link ConstantSelectionPropertyEditor#getUsedTypes(JavaInfo)}.
   */
  @SuppressWarnings("unchecked")
  private Set<IType> getUsedTypes() throws Exception {
    prepareProperty();
    return (Set<IType>) ReflectionUtils.invokeMethod2(
        m_propertyEditor,
        "getUsedTypes",
        JavaInfo.class,
        m_lastParseInfo);
  }

  /**
   * Invokes {@link ConstantSelectionPropertyEditor#getLocalTypes(JavaInfo)}.
   */
  @SuppressWarnings("unchecked")
  private List<IType> getLocalTypes() throws Exception {
    prepareProperty();
    return (List<IType>) ReflectionUtils.invokeMethod2(
        m_propertyEditor,
        "getLocalTypes",
        JavaInfo.class,
        m_lastParseInfo);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getField()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Property has no value.
   */
  public void test_utils_noValue() throws Exception {
    parseContainer(
        "// filler filler filler",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "  }",
        "}");
    assertNull(getField());
    assertNull(getType());
    assertNull(getText());
    assertThat(getFields()).isEmpty();
  }

  /**
   * Property does not use {@link QualifiedName} as expression.
   */
  public void test_utils_notQualifiedName() throws Exception {
    parseContainer(
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    setStringId('constant');",
        "  }",
        "}");
    assertNull(getField());
    assertNull(getType());
    assertNull(getText());
    assertThat(getFields()).isEmpty();
  }

  /**
   * Normal {@link QualifiedName} as value.
   */
  public void test_utils_qualifiedName() throws Exception {
    parseContainer(
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    setStringId(PrefConstants.ID_1);",
        "  }",
        "}");
    {
      IField field = getField();
      assertEquals("ID_1", field.getElementName());
    }
    {
      IType type = getType();
      assertEquals("test.PrefConstants", type.getFullyQualifiedName());
    }
    assertEquals("ID_1", getText());
    // check possible fields
    {
      List<IField> fields = getFields();
      assertThat(fields).hasSize(2);
      assertEquals("ID_1", fields.get(0).getElementName());
      assertEquals("ID_4", fields.get(1).getElementName());
    }
    // set new IField
    {
      IType constantsType = m_testProject.getJavaProject().findType("test.PrefConstants");
      IField field = constantsType.getField("ID_4");
      assertTrue(field.exists());
      setField(field);
      assertEditor(
          "public class Test extends MyPanel {",
          "  public Test() {",
          "    setStringId(PrefConstants.ID_4);",
          "  }",
          "}");
    }
    // after setField() we should understand Expression
    assertEquals("ID_4", getText());
  }

  /**
   * {@link SimpleName} as value, from implemented interface.
   */
  public void test_utils_simpleName_interface() throws Exception {
    parseContainer(
        "public class Test extends MyPanel implements PrefConstants {",
        "  public Test() {",
        "    setStringId(ID_1);",
        "  }",
        "}");
    {
      IField field = getField();
      assertEquals("ID_1", field.getElementName());
    }
    {
      IType type = getType();
      assertEquals("test.PrefConstants", type.getFullyQualifiedName());
    }
    assertEquals("ID_1", getText());
    // check possible fields
    {
      List<IField> fields = getFields();
      assertThat(fields).hasSize(2);
      assertEquals("ID_1", fields.get(0).getElementName());
      assertEquals("ID_4", fields.get(1).getElementName());
    }
    // set new IField
    {
      IType constantsType = m_testProject.getJavaProject().findType("test.PrefConstants");
      IField field = constantsType.getField("ID_4");
      assertTrue(field.exists());
      setField(field);
      assertEditor(
          "public class Test extends MyPanel implements PrefConstants {",
          "  public Test() {",
          "    setStringId(ID_4);",
          "  }",
          "}");
    }
    // after setField() we should understand Expression
    assertEquals("ID_4", getText());
  }

  /**
   * Test for {@link #setField(IField)}.
   */
  public void test_setField_local() throws Exception {
    parseContainer(
        "public class Test extends MyPanel {",
        "  public static final String LOCAL_ID = 'value';",
        "  public Test() {",
        "  }",
        "}");
    // set new IField
    {
      IType constantsType = m_testProject.getJavaProject().findType("test.Test");
      IField field = constantsType.getField("LOCAL_ID");
      assertTrue(field.exists());
      setField(field);
      assertEditor(
          "public class Test extends MyPanel {",
          "  public static final String LOCAL_ID = 'value';",
          "  public Test() {",
          "    setStringId(LOCAL_ID);",
          "  }",
          "}");
    }
    // after setField() we should understand Expression
    assertEquals("LOCAL_ID", getText());
  }

  /**
   * {@link SimpleName} as value, from local {@link IField}.
   */
  public void test_utils_simpleName_field() throws Exception {
    parseContainer(
        "public class Test extends MyPanel {",
        "  public static final String LOCAL_ID = 'value';",
        "  public static final String LOCAL_ID2 = 'value2';",
        "  private static final String LOCAL_ID3 = 'value3';",
        "  public static final int INT_VALUE = 0;",
        "  public Test() {",
        "    setStringId(LOCAL_ID);",
        "  }",
        "}");
    {
      IField field = getField();
      assertEquals("LOCAL_ID", field.getElementName());
    }
    {
      IType type = getType();
      assertEquals("test.Test", type.getFullyQualifiedName());
    }
    assertEquals("LOCAL_ID", getText());
    // only "String" fields should be returned
    {
      List<IField> fields = getFields();
      assertThat(fields).hasSize(2);
      assertEquals("LOCAL_ID", fields.get(0).getElementName());
      assertEquals("LOCAL_ID2", fields.get(1).getElementName());
    }
  }

  /**
   * {@link SimpleName} as value, from local variable, so BAD case.
   */
  public void test_utils_simpleName_variable() throws Exception {
    parseContainer(
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    String id = 'value';",
        "    setStringId(id);",
        "  }",
        "}");
    assertNull(getField());
    assertNull(getType());
    assertNull(getText());
  }

  /**
   * Test for {@link ConstantSelectionPropertyEditor#getUsedTypes(JavaInfo)}.
   */
  public void test_getUsedTypes() throws Exception {
    parseContainer(
        "public class Test extends MyPanel {",
        "  public static final String LOCAL_ID = 'value';",
        "  public Test() {",
        "    setStringId(PrefConstants.ID_1);",
        "    setStringId2(LOCAL_ID);",
        "  }",
        "}");
    Set<IType> types = getUsedTypes();
    // convert IType's into their names
    Set<String> typeNames = Sets.newHashSet();
    for (IType type : types) {
      typeNames.add(type.getFullyQualifiedName());
    }
    // validate IType's names
    assertThat(typeNames).containsOnly("test.PrefConstants", "test.Test");
  }

  /**
   * Test for {@link ConstantSelectionPropertyEditor#getUsedInterfaces(JavaInfo)}.<br>
   * This {@link TypeDeclaration} has no constants.
   */
  public void test_getLocalTypes_0() throws Exception {
    parseContainer(
        "public class Test extends MyPanel implements PrefConstants {",
        "  public Test() {",
        "  }",
        "}");
    List<IType> types = getLocalTypes();
    // convert IType's into their names
    Set<String> typeNames = Sets.newHashSet();
    for (IType type : types) {
      typeNames.add(type.getFullyQualifiedName());
    }
    // validate IType's names
    assertThat(typeNames).containsOnly("test.PrefConstants");
  }

  /**
   * Test for {@link ConstantSelectionPropertyEditor#getUsedInterfaces(JavaInfo)}.<br>
   * This {@link TypeDeclaration} has constants.
   */
  public void test_getLocalTypes_1() throws Exception {
    // declare interface without valid (String) constants
    setFileContentSrc(
        "test/BadConstants.java",
        getSourceDQ("package test;", "public interface BadConstants {", "  int ID = 0;", "}"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "public class Test extends MyPanel implements PrefConstants, BadConstants {",
        "  public static final String LOCAL_ID = 'value';",
        "  public Test() {",
        "  }",
        "}");
    List<IType> types = getLocalTypes();
    // convert IType's into their names
    Set<String> typeNames = Sets.newHashSet();
    for (IType type : types) {
      typeNames.add(type.getFullyQualifiedName());
    }
    // validate IType's names
    assertThat(typeNames).containsOnly("test.PrefConstants", "test.Test");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Combo
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Check for items.
   */
  public void test_combo_items() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    setStringId(PrefConstants.ID_1);",
            "  }",
            "}");
    Property property = panel.getPropertyByTitle("stringId");
    //
    addComboPropertyItems(property);
    // check items
    {
      List<String> items = getComboPropertyItems();
      assertThat(items).containsExactly("ID_1", "ID_4");
    }
    // select current item
    {
      setComboPropertySelection(1);
      setComboPropertySelection(property);
      assertEquals(0, getComboPropertySelection());
    }
    // set new item
    {
      setComboPropertyValue(property, 1);
      assertEditor(
          "public class Test extends MyPanel {",
          "  public Test() {",
          "    setStringId(PrefConstants.ID_4);",
          "  }",
          "}");
    }
  }
}
