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
package org.eclipse.wb.tests.designer.core.model.parser;

import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.utils.reflect.CompositeClassLoader;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.util.Iterator;
import java.util.Map;

import javax.swing.UIManager;

public class EditorStateTest extends SwingModelTest {
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
  public void test_getEditor() throws Exception {
    parseContainer(
        "// filler filler filler",
        "class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    assertSame(m_lastEditor, m_lastState.getEditor());
  }

  /**
   * Test for {@link EditorState#isDisposed()}.
   */
  public void test_isDisposed() throws Exception {
    parseContainer(
        "// filler filler filler",
        "class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // remember to use after dispose
    EditorState editorState = m_lastState;
    // not disposed initially
    assertFalse(editorState.isDisposed());
    // do dispose
    disposeLastModel();
    assertTrue(editorState.isDisposed());
  }

  /**
   * We should clear {@link PropertyEditor}'s registered in {@link PropertyEditorManager} because we
   * load them using {@link ClassLoader} from {@link EditorState}, so they are short lived.
   */
  @SuppressWarnings("rawtypes")
  public void test_clearRegisteredEditors() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends JPanel {",
            "}"));
    setFileContentSrc(
        "test/MyObject.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyObject {",
            "  // filler",
            "}"));
    setFileContentSrc(
        "test/MyPropertyEditor.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPropertyEditor extends java.beans.PropertyEditorSupport {",
            "}"));
    setFileContentSrc(
        "test/MyPanelBeanInfo.java",
        getTestSource(
            "public class MyPanelBeanInfo extends java.beans.SimpleBeanInfo {",
            "  public MyPanelBeanInfo() {",
            "    java.beans.PropertyEditorManager.registerEditor(MyObject.class, MyPropertyEditor.class);",
            "  }",
            "}"));
    waitForAutoBuild();
    // does not work for Java 7
    if (EnvironmentUtils.getJavaVersion() >= 1.7) {
      return;
    }
    // prepare editor's registry
    Map registry = (Map) ReflectionUtils.invokeMethod(PropertyEditorManager.class, "getRegistry()");
    int initialEditors = registry.size();
    // parse, one more editor in registry expected
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    // just couple checks for CompositeClassLoader
    {
      assertInstanceOf(CompositeClassLoader.class, m_lastLoader);
      assertNull(m_lastLoader.getResource("test/NoSuchClass.class"));
      assertNotNull(m_lastLoader.getResource("test/MyObject.class"));
    }
    // check for our editor
    assertEquals(initialEditors + 1, registry.size());
    {
      boolean found = false;
      for (Iterator I = registry.entrySet().iterator(); I.hasNext();) {
        Map.Entry entry = (Map.Entry) I.next();
        Class valueClass = (Class) entry.getKey();
        Class editorClass = (Class) entry.getValue();
        if (valueClass.getName().equals("test.MyObject")
            && editorClass.getName().equals("test.MyPropertyEditor")) {
          found = true;
          break;
        }
      }
      assertTrue(found);
    }
    // clear and check registry again
    panel.getBroadcastObject().dispose();
    assertEquals(initialEditors, registry.size());
  }

  /**
   * We should remove cached {@link Class}-s to prevent memory leak and {@link ClassCastException} 
   * 's.
   */
  public void test_clearUIManager() throws Exception {
    String key = "wbp.EditorStateTest";
    setFileContentSrc(
        "test/MyObject.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyObject {",
            "  // filler filler filler",
            "}"));
    waitForAutoBuild();
    String[] lines = {"public class Test extends JPanel {", "  public Test() {", "  }", "}"};
    // parse, one more editor in registry expected
    ContainerInfo panel = parseContainer(lines);
    // put "test.MyObject" into UIManager
    Class<?> myObjectClass = m_lastLoader.loadClass("test.MyObject");
    UIManager.put(key, myObjectClass);
    assertNotNull(UIManager.get(key));
    // dispose and check that UIManager has no "test.MyObject"
    panel.getBroadcastObject().dispose();
    assertNull(UIManager.get(key));
  }
}
