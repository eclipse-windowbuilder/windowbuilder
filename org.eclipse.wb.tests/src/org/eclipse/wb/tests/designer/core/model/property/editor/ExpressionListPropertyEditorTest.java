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

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.model.property.editor.ExpressionListPropertyEditor;
import org.eclipse.wb.internal.core.utils.check.AssertionFailedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test for {@link ExpressionListPropertyEditor}.
 *
 * @author sablin_aa
 */
public class ExpressionListPropertyEditorTest extends AbstractTextPropertyEditorTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Exit zone :-)
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
  /**
   * Top level class, all fields are valid. Parameters also containing special code
   * <code>null</code>.
   */
  public void test_configure_valid() throws Exception {
    Map<String, Object> parameters = getEditorParameters();
    ExpressionListPropertyEditor editor =
        createEditor(ExpressionListPropertyEditor.class, parameters);
    assertEditorConfiguration(editor, parameters);
  }

  /**
   * No titles test.
   */
  public void test_configure_titles() throws Exception {
    Map<String, Object> parameters = getEditorParameters();
    parameters.remove("titles");
    ExpressionListPropertyEditor editor =
        createEditor(ExpressionListPropertyEditor.class, parameters);
    parameters.put("titles", parameters.get("expressions"));
    assertEditorConfiguration(editor, parameters);
  }

  /**
   * Count fail test.
   */
  @SuppressWarnings("unchecked")
  public void test_configure_count() throws Exception {
    Map<String, Object> parameters = getEditorParameters();
    // remove first condition from conditions list
    List<String> conditions = (List<String>) parameters.get("conditions");
    conditions.remove(0);
    parameters.put("conditions", conditions);
    // test
    try {
      createEditor(ExpressionListPropertyEditor.class, parameters);
      fail();
    } catch (AssertionFailedException e) {
      // OK
    }
  }

  /**
   * Parameter fail test.
   */
  public void test_configure_parameters() throws Exception {
    Map<String, Object> parameters = getEditorParameters();
    // remove conditions from parameters
    parameters.remove("conditions");
    // test
    try {
      createEditor(ExpressionListPropertyEditor.class, parameters);
      fail();
    } catch (AssertionFailedException e) {
      // OK
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ExpressionListPropertyEditor#getValueSource(Object)}.
   */
  public void test_getValueSource() throws Exception {
    ExpressionListPropertyEditor editor =
        createEditor(ExpressionListPropertyEditor.class, getEditorParameters());
    assertEquals("new java.lang.String()", editor.getValueSource(new String()));
    assertEquals("null", editor.getValueSource(null));
  }

  /**
   * Test for {@link ExpressionListPropertyEditor#getClipboardSource(Object)}.
   */
  public void test_getClipboardSource() throws Exception {
    ExpressionListPropertyEditor editor =
        createEditor(ExpressionListPropertyEditor.class, getEditorParameters());
    assert_getClipboardSource("new java.lang.String()", editor, new String());
    assert_getClipboardSource(null, editor, new StringBuffer());
  }

  /**
   * Test for {@link ExpressionListPropertyEditor#getText(Object)}.
   */
  public void test_getText() throws Exception {
    ExpressionListPropertyEditor editor =
        createEditor(ExpressionListPropertyEditor.class, getEditorParameters());
    assert_getText("STR", editor, new String());
    assert_getText("NIL", editor, null);
    assert_getText(null, editor, new StringBuffer());
  }

  /**
   * Test for MVEL imports.
   */
  public void test_imports() throws Exception {
    // prepare parameters
    HashMap<String, Object> parameters = Maps.newHashMap();
    parameters.put("functions", getSourceDQ("import java.util.ArrayList;"));
    parameters.put(
        "expressions",
        Arrays.asList("new java.util.ArrayList()", "new java.lang.String()", "null"));
    parameters.put(
        "conditions",
        Arrays.asList("value is ArrayList", "value is String", "value == null"));
    parameters.put("titles", Arrays.asList("AL", "STR", "NIL"));
    //
    ExpressionListPropertyEditor editor =
        createEditor(ExpressionListPropertyEditor.class, parameters);
    assert_getText("AL", editor, new ArrayList<Object>());
    assert_getText("STR", editor, new String());
    assert_getText("NIL", editor, null);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Assertions
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Asserts that given {@link ExpressionListPropertyEditor} has expected configuration.
   */
  @SuppressWarnings("unchecked")
  private void assertEditorConfiguration(ExpressionListPropertyEditor editor,
      Map<String, Object> parameters) throws Exception {
    assertContainsOnly(editor, "m_expressions", (List<String>) parameters.get("expressions"));
    assertContainsOnly(editor, "m_conditions", (List<String>) parameters.get("conditions"));
    assertContainsOnly(editor, "m_titles", (List<String>) parameters.get("titles"));
    assertEquals(getFieldValue(editor, "m_functions"), parameters.get("functions"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private Map<String, Object> getEditorParameters() {
    //<editor id="expressionList">
    //	<parameter name="functions">def valueType(c) { if (value == null) { return false; } else { return isType(value.getClass(),c); } };</parameter>
    //	<parameter-list name="expressions">new Integer()</parameter-list>
    //	<parameter-list name="expressions">new String()</parameter-list>
    //	<parameter-list name="expressions">null</parameter-list>
    //	<parameter-list name="conditions">valueType("java.lang.Integer")</parameter-list>
    //	<parameter-list name="conditions">valueType("java.lang.String")</parameter-list>
    //	<parameter-list name="conditions">value==null</parameter-list>
    //	<parameter-list name="titles">INT</parameter-list>
    //	<parameter-list name="titles">STR</parameter-list>
    //	<parameter-list name="titles">NIL</parameter-list>
    //</editor>
    HashMap<String, Object> params = Maps.newHashMap();
    params.put(
        "functions",
        getSourceDQ(
            "def valueType(c) {",
            "  if (value == null) {",
            "    return false; ",
            "  } else { ",
            "    return isType(value.getClass(),c); ",
            "  } ",
            "};"));
    params.put(
        "expressions",
        Arrays.asList("new java.lang.Integer()", "new java.lang.String()", "null"));
    params.put(
        "conditions",
        Arrays.asList(
            "valueType(\"java.lang.Integer\")",
            "valueType(\"java.lang.String\")",
            "value == null"));
    params.put("titles", Arrays.asList("INT", "STR", "NIL"));
    params.put("imports", Arrays.asList("com.google.common.collect.Lists"));
    return params;
  }
}
