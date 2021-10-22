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
import org.eclipse.wb.internal.core.model.property.editor.IntegerObjectPropertyEditor;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

/**
 * Test for {@link IntegerObjectPropertyEditor}.
 *
 * @author scheglov_ke
 */
public class IntegerObjectPropertyEditorTest extends AbstractTextPropertyEditorTest {
  private static final IntegerObjectPropertyEditor EDITOR = IntegerObjectPropertyEditor.INSTANCE;

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
   * Test for {@link IntegerObjectPropertyEditor#getText(Property)}.
   */
  public void test_getText() throws Exception {
    assert_getText(null, EDITOR, Property.UNKNOWN_VALUE);
    assert_getText("null", EDITOR, null);
    assert_getText("123", EDITOR, Integer.valueOf(123));
  }

  /**
   * Test for {@link IntegerObjectPropertyEditor#getEditorText(Property)}.
   */
  public void test_getEditorText() throws Exception {
    assert_getEditorText(null, EDITOR, Property.UNKNOWN_VALUE);
    assert_getEditorText("null", EDITOR, null);
    assert_getEditorText("123", EDITOR, Integer.valueOf(123));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setEditorText()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link IntegerObjectPropertyEditor#setEditorText(Property, String)}.
   */
  public void test_setEditorText_value() throws Exception {
    prepareIntegerPanel();
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
        "    setFoo(123);",
        "  }",
        "}");
  }

  /**
   * Test for {@link IntegerObjectPropertyEditor#setEditorText(Property, String)}.
   */
  public void test_setEditorText_null() throws Exception {
    prepareIntegerPanel();
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
        "    setFoo((Integer) null);",
        "  }",
        "}");
  }

  /**
   * Test for {@link IntegerObjectPropertyEditor#setEditorText(Property, String)}.
   */
  public void test_setEditorText_removeValue_emptyString() throws Exception {
    prepareIntegerPanel();
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    setFoo(123);",
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
   * Test for {@link IntegerObjectPropertyEditor#setEditorText(Property, String)}.
   */
  public void test_setEditorText_removeValue_whitespaceString() throws Exception {
    prepareIntegerPanel();
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    setFoo(123);",
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
   * Test for {@link IntegerObjectPropertyEditor#setEditorText(Property, String)}.
   */
  public void test_setEditorText_invalidValue() throws Exception {
    prepareIntegerPanel();
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
      public void run(UiContext context) throws Exception {
        setTextEditorText(property, "notInteger");
      }
    }, new UIRunnable() {
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

  private void prepareIntegerPanel() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public void setFoo(Integer foo) {",
            "  }",
            "}"));
    waitForAutoBuild();
  }
}
