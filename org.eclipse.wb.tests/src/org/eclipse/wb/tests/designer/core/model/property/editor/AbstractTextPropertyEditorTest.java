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

import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.IConfigurablePropertyObject;
import org.eclipse.wb.internal.core.model.property.editor.ExpressionListPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDisplayPropertyEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import static org.easymock.EasyMock.expect;
import static org.assertj.core.api.Assertions.assertThat;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;

import java.util.List;
import java.util.Map;

/**
 * Test for {@link ExpressionListPropertyEditor}.
 * 
 * @author sablin_aa
 */
public class AbstractTextPropertyEditorTest extends SwingModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Assertions
  //
  ////////////////////////////////////////////////////////////////////////////
  protected static void assert_getText(String expectedText,
      TextDisplayPropertyEditor editor,
      Object value) throws Exception {
    String actualText =
        (String) callMethodWithPropertyValue(
            "getText(org.eclipse.wb.internal.core.model.property.Property)",
            editor,
            value);
    assertEquals(expectedText, actualText);
  }

  protected static void assert_getEditorText(String expectedText,
      TextDisplayPropertyEditor editor,
      Object value) throws Exception {
    String actualText =
        (String) callMethodWithPropertyValue(
            "getEditorText(org.eclipse.wb.internal.core.model.property.Property)",
            editor,
            value);
    assertEquals(expectedText, actualText);
  }

  protected static void assert_getClipboardSource(String expectedSource,
      TextDisplayPropertyEditor editor,
      Object value) throws Exception {
    String actualSource =
        (String) callMethodWithPropertyValue(
            "getClipboardSource(org.eclipse.wb.internal.core.model.property.GenericProperty)",
            editor,
            value);
    assertEquals(expectedSource, actualSource);
  }

  private static Object callMethodWithPropertyValue(String signature,
      TextDisplayPropertyEditor editor,
      Object value) throws Exception {
    // prepare for mocking
    IMocksControl mocksControl = EasyMock.createStrictControl();
    GenericProperty property = mocksControl.createMock(GenericProperty.class);
    // configure property
    mocksControl.reset();
    expect(property.getValue()).andReturn(value);
    mocksControl.replay();
    // verify
    Object result = ReflectionUtils.invokeMethod(editor, signature, property);
    mocksControl.verify();
    return result;
  }

  /**
   * 
   * Assert class array field equal for .
   * 
   * @param editor
   * @param fieldName
   * @param expected
   * @throws Exception
   */
  protected static void assertContainsOnly(Object editor, String fieldName, List<?> expected)
      throws Exception {
    assertThat((Object[]) getFieldValue(editor, fieldName)).containsOnly(expected.toArray());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialize environment
  //
  ////////////////////////////////////////////////////////////////////////////
  protected void initTestSourceState() throws Exception {
    parseContainer(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
  }

  protected <T extends TextDisplayPropertyEditor> T createEditor(Class<T> clazz,
      Map<String, Object> parameters) throws Exception {
    initTestSourceState();
    T editor = clazz.newInstance();
    IConfigurablePropertyObject configurableEditor = (IConfigurablePropertyObject) editor;
    configurableEditor.configure(m_lastState, parameters);
    return editor;
  }
}
