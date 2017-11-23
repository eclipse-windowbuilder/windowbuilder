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
package org.eclipse.wb.tests.designer.rcp.model.jface;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.model.jface.FieldEditorLabelsConstantsPropertyEditor;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.swt.widgets.Text;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Test for {@link FieldEditorLabelsConstantsPropertyEditor}.
 * 
 * @author scheglov_ke
 */
public class FieldEditorLabelsConstantsPropertyEditorTest extends SwingModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    // prepare JDT elements
    setFileContentSrc(
        "test/PrefConstants.java",
        getTestSource(
            "public interface PrefConstants {",
            "  String ID_1 = 'id 1';",
            "  String ID_2 = 'id 2';",
            "}"));
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public MyPanel() {",
            "  }",
            "  public void setLabelsAndValues(String [][] labelsAndValues) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <property id='setLabelsAndValues(java.lang.String[][])'>",
            "    <editor id='FieldEditor_LabelsConstants'/>",
            "  </property>",
            "</component>"));
    waitForAutoBuild();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    // clean-up fields
    m_property = null;
    m_propertyEditor = null;
    m_resultLabels = null;
    m_resultFields = null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Helpers
  //
  ////////////////////////////////////////////////////////////////////////////
  private final String m_propertyName = "labelsAndValues";
  private Property m_property;
  private FieldEditorLabelsConstantsPropertyEditor m_propertyEditor;
  private List<String> m_resultLabels = Lists.newArrayList();
  private List<IField> m_resultFields = Lists.newArrayList();

  private void prepareProperty() throws Exception {
    m_property = m_lastParseInfo.getPropertyByTitle(m_propertyName);
    m_propertyEditor = (FieldEditorLabelsConstantsPropertyEditor) m_property.getEditor();
  }

  /**
   * Invokes {@link FieldEditorLabelsConstantsPropertyEditor#getTextForEditing(GenericProperty)}.
   */
  private String getTextForEditing() throws Exception {
    prepareProperty();
    String text =
        (String) ReflectionUtils.invokeMethod2(
            m_propertyEditor,
            "getTextForEditing",
            GenericProperty.class,
            m_property);
    return StringUtils.replace(text, Text.DELIMITER, "\n");
  }

  /**
   * Invokes
   * {@link FieldEditorLabelsConstantsPropertyEditor#prepareLabelsFields(List, List, GenericProperty, String)}
   * .
   */
  private String prepareLabelsFields(String text) throws Exception {
    prepareProperty();
    return (String) ReflectionUtils.invokeMethod2(
        m_propertyEditor,
        "prepareLabelsFields",
        List.class,
        List.class,
        GenericProperty.class,
        String.class,
        m_resultLabels,
        m_resultFields,
        m_property,
        text);
  }

  /**
   * Invokes
   * {@link FieldEditorLabelsConstantsPropertyEditor#setLabelsFields(List, List, GenericProperty)} .
   */
  private String setLabelsFields() throws Exception {
    prepareProperty();
    return (String) ReflectionUtils.invokeMethod2(
        m_propertyEditor,
        "setLabelsFields",
        List.class,
        List.class,
        GenericProperty.class,
        m_resultLabels,
        m_resultFields,
        m_property);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getTextForEditing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * No expression.
   */
  public void test_getTextForEditing_0() throws Exception {
    parseContainer(
        "// filler filler filler",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "  }",
        "}");
    assertEquals("", getTextForEditing());
  }

  /**
   * Expression with both {@link ArrayInitializer} and {@link ArrayCreation} as elements.
   */
  public void test_getTextForEditing_1() throws Exception {
    parseContainer(
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    setLabelsAndValues(new String[][]{{'1', PrefConstants.ID_1},"
            + " new String[]{'2', PrefConstants.ID_2}});",
        "  }",
        "}");
    assertEquals(
        getSourceDQ("1 test.PrefConstants.ID_1", "2 test.PrefConstants.ID_2"),
        getTextForEditing());
  }

  /**
   * Ignore element with not {@link IField}.
   */
  public void test_getTextForEditing_2() throws Exception {
    parseContainer(
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    setLabelsAndValues(new String[][]{{'1', PrefConstants.ID_1},"
            + " new String[]{'2', 'Ignored literal'}});",
        "  }",
        "}");
    assertEquals("1 test.PrefConstants.ID_1\n", getTextForEditing());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // prepareLabelsFields
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_prepareLabelsFields() throws Exception {
    parseContainer(
        "public class Test extends MyPanel {",
        "  public static final String LOCAL_ID = 'value';",
        "  public Test() {",
        "  }",
        "}");
    // all valid, but empty
    {
      String errorMessage = prepareLabelsFields("");
      assertThat(errorMessage).isNull();
      assertThat(m_resultLabels).isEmpty();
      assertThat(m_resultFields).isEmpty();
    }
    // invalid: no field name
    {
      String errorMessage = prepareLabelsFields("one_word");
      assertThat(errorMessage).contains("label").contains("field");
    }
    // invalid: no field with such name
    {
      String errorMessage = prepareLabelsFields("label no.such.field");
      assertThat(errorMessage).contains("Invalid field");
    }
    // use local field
    {
      String errorMessage = prepareLabelsFields(getSourceDQ("some label LOCAL_ID"));
      assertThat(errorMessage).isNull();
      assertThat(m_resultLabels).containsOnly("some label");
      {
        assertThat(m_resultFields).hasSize(1);
        assertEquals("LOCAL_ID", m_resultFields.get(0).getElementName());
      }
    }
    // two label/field pairs
    {
      String errorMessage =
          prepareLabelsFields(getSourceDQ(
              "first label test.PrefConstants.ID_1",
              "second label test.PrefConstants.ID_2"));
      assertThat(errorMessage).isNull();
      assertThat(m_resultLabels).containsOnly("first label", "second label");
      {
        assertThat(m_resultFields).hasSize(2);
        assertEquals("ID_1", m_resultFields.get(0).getElementName());
        assertEquals("ID_2", m_resultFields.get(1).getElementName());
      }
    }
    // set labels/fields
    setLabelsFields();
    assertEditor(
        "public class Test extends MyPanel {",
        "  public static final String LOCAL_ID = 'value';",
        "  public Test() {",
        "    setLabelsAndValues(new String[][]{{'first label', PrefConstants.ID_1},"
            + " {'second label', PrefConstants.ID_2}});",
        "  }",
        "}");
  }
}