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

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.ByteObjectPropertyEditor;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

/**
 * Test for {@link ByteObjectPropertyEditor}.
 *
 * @author scheglov_ke
 */
public class ByteObjectPropertyEditorTest extends AbstractTextPropertyEditorTest {
  private static final ByteObjectPropertyEditor EDITOR = ByteObjectPropertyEditor.INSTANCE;

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
   * Test for {@link ByteObjectPropertyEditor#getText(Property)}.
   */
  public void test_getText() throws Exception {
    assert_getText(null, EDITOR, Property.UNKNOWN_VALUE);
    assert_getText("null", EDITOR, null);
    assert_getText("123", EDITOR, Byte.valueOf((byte) 123));
  }

  /**
   * Test for {@link ByteObjectPropertyEditor#getEditorText(Property)}.
   */
  public void test_getEditorText() throws Exception {
    assert_getEditorText(null, EDITOR, Property.UNKNOWN_VALUE);
    assert_getEditorText("null", EDITOR, null);
    assert_getEditorText("123", EDITOR, Byte.valueOf((byte) 123));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setEditorText()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ByteObjectPropertyEditor#setEditorText(Property, String)}.
   */
  public void test_setEditorText_value() throws Exception {
    prepareBytePanel();
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    //
    Property property = panel.getPropertyByTitle("foo");
    setTextEditorText(property, "123");
    assertEditor(
        "// filler filler filler",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    setFoo((byte) 123);",
        "  }",
        "}");
  }

  /**
   * Test for {@link ByteObjectPropertyEditor#setEditorText(Property, String)}.
   */
  public void test_setEditorText_null() throws Exception {
    prepareBytePanel();
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    //
    Property property = panel.getPropertyByTitle("foo");
    setTextEditorText(property, "null");
    assertEditor(
        "// filler filler filler",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    setFoo((Byte) null);",
        "  }",
        "}");
  }

  /**
   * Test for {@link ByteObjectPropertyEditor#setEditorText(Property, String)}.
   */
  public void test_setEditorText_removeValue_emptyString() throws Exception {
    prepareBytePanel();
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    setFoo((byte) 123);",
            "  }",
            "}");
    panel.refresh();
    //
    Property property = panel.getPropertyByTitle("foo");
    setTextEditorText(property, "");
    assertEditor(
        "// filler filler filler",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "  }",
        "}");
  }

  /**
   * Test for {@link ByteObjectPropertyEditor#setEditorText(Property, String)}.
   */
  public void test_setEditorText_removeValue_whitespaceString() throws Exception {
    prepareBytePanel();
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    setFoo((byte) 123);",
            "  }",
            "}");
    panel.refresh();
    //
    Property property = panel.getPropertyByTitle("foo");
    setTextEditorText(property, " ");
    assertEditor(
        "// filler filler filler",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "  }",
        "}");
  }

  /**
   * Test for {@link ByteObjectPropertyEditor#setEditorText(Property, String)}.
   */
  public void test_setEditorText_invalidValue() throws Exception {
    prepareBytePanel();
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    //
    final Property property = panel.getPropertyByTitle("foo");
    new UiContext().executeAndCheck(new UIRunnable() {
      @Override
      public void run(UiContext context) throws Exception {
        setTextEditorText(property, "notByte");
      }
    }, new UIRunnable() {
      @Override
      public void run(UiContext context) throws Exception {
        context.useShell("foo");
        context.clickButton("OK");
      }
    });
    assertEditor(
        "// filler filler filler",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "  }",
        "}");
  }

  private void prepareBytePanel() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public void setFoo(Byte foo) {",
            "  }",
            "}"));
    waitForAutoBuild();
  }
}
