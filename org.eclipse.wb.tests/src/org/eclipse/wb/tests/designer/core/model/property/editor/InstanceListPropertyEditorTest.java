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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.model.property.editor.InstanceListPropertyEditor;
import org.eclipse.wb.internal.core.utils.check.AssertionFailedException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test for {@link InstanceListPropertyEditor}.
 *
 * @author sablin_aa
 */
public class InstanceListPropertyEditorTest extends AbstractTextPropertyEditorTest {
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
    InstanceListPropertyEditor editor = createEditor(InstanceListPropertyEditor.class, parameters);
    assertEditorConfiguration(editor, parameters);
  }

  /**
   * Parameters sanity check
   */
  public void test_configure_check() throws Exception {
    Map<String, Object> parameters = getEditorParameters();
    parameters.put("titles", Lists.newArrayList("STR", "INT"));
    // test
    try {
      createEditor(InstanceListPropertyEditor.class, parameters);
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
    parameters.remove("types");
    // test
    try {
      createEditor(InstanceListPropertyEditor.class, parameters);
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
   * Test for {@link InstanceListPropertyEditor#getValueSource(Object)}.
   */
  public void test_getValueSource() throws Exception {
    Map<String, Object> parameters = getEditorParameters();
    InstanceListPropertyEditor editor = createEditor(InstanceListPropertyEditor.class, parameters);
    assertEquals("new java.lang.String()", editor.getValueSource(new String()));
  }

  /**
   * Test for {@link InstanceListPropertyEditor#getClipboardSource(Object)}.
   */
  public void test_getClipboardSource() throws Exception {
    Map<String, Object> parameters = getEditorParameters();
    InstanceListPropertyEditor editor = createEditor(InstanceListPropertyEditor.class, parameters);
    assert_getClipboardSource("new java.lang.Integer()", editor, new Integer(3));
    assert_getClipboardSource(null, editor, new Boolean(true));
  }

  /**
   * Test for {@link InstanceListPropertyEditor#getText(Object)}.
   */
  public void test_getText() throws Exception {
    Map<String, Object> parameters = getEditorParameters();
    InstanceListPropertyEditor editor = createEditor(InstanceListPropertyEditor.class, parameters);
    assert_getText("String", editor, new String("String"));
    assert_getText("null", editor, null);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private Map<String, Object> getEditorParameters() {
    //<editor id="instanceList">
    //	<parameter-list name="types">java.lang.String</parameter-list>
    //	<parameter-list name="types">java.lang.Integer</parameter-list>
    //	<parameter-list name="types">null</parameter-list>
    //</editor>
    HashMap<String, Object> params = Maps.newHashMap();
    params.put("types", Lists.newArrayList("java.lang.String", "java.lang.Integer", "null"));
    return params;
  }

  /**
   * Asserts that given {@link InstanceListPropertyEditor} has expected configuration.
   */
  @SuppressWarnings("unchecked")
  private void assertEditorConfiguration(InstanceListPropertyEditor editor,
      Map<String, Object> parameters) throws Exception {
    assertContainsOnly(editor, "m_types", (List<String>) parameters.get("types"));
    assertEquals(((Object[]) getFieldValue(editor, "m_classes")).length, 3);
  }
}
