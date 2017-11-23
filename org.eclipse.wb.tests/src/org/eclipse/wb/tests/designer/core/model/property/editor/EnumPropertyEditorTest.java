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
import org.eclipse.wb.internal.core.model.property.editor.EnumPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.ITextValuePropertyEditor;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link EnumPropertyEditor}.
 * 
 * @author scheglov_ke
 */
public class EnumPropertyEditorTest extends SwingModelTest {
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
   * Test using {@link EnumPropertyEditor}.
   */
  public void test_externalEnum() throws Exception {
    prepare_Foo_MyPanel();
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    setFoo(Foo.B);",
            "  }",
            "}");
    // prepare property
    GenericProperty property = (GenericProperty) panel.getPropertyByTitle("foo");
    EnumPropertyEditor editor = (EnumPropertyEditor) property.getEditor();
    // text
    assertEquals("B", editor.getText(property));
    // IValueSourcePropertyEditor
    {
      Class<?> classFoo = m_lastLoader.loadClass("test.Foo");
      assertEquals("test.Foo.B", editor.getValueSource(classFoo.getEnumConstants()[1]));
      assertNull(editor.getValueSource(this));
    }
    // IClipboardSourceProvider
    assertEquals("test.Foo.B", editor.getClipboardSource(property));
    // elements
    {
      Enum<?>[] enums =
          (Enum<?>[]) ReflectionUtils.invokeMethod(
              editor,
              "getElements(org.eclipse.wb.internal.core.model.property.Property)",
              property);
      String[] enumStrings = GenericsUtils.getEnumStrings(enums);
      assertThat(enumStrings).isEqualTo(new String[]{"A", "B", "C"});
    }
  }

  public void test_getText_noValue() throws Exception {
    prepare_Foo_MyPanel();
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    // prepare property
    GenericProperty property = (GenericProperty) panel.getPropertyByTitle("foo");
    EnumPropertyEditor editor = (EnumPropertyEditor) property.getEditor();
    // text
    assertNull(editor.getText(property));
    assertNull(editor.getClipboardSource(property));
  }

  /**
   * Test for {@link ITextValuePropertyEditor}.
   */
  public void test_setText() throws Exception {
    prepare_Foo_MyPanel();
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    setFoo(Foo.B);",
            "  }",
            "}");
    // prepare property
    GenericProperty property = (GenericProperty) panel.getPropertyByTitle("foo");
    // set value
    setPropertyText(property, "C");
    assertEditor(
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    setFoo(Foo.C);",
        "  }",
        "}");
  }

  /**
   * Enum that is internal class <code>test.MyPanel.Foo</code>.
   */
  public void test_innerEnum() throws Exception {
    prepare_Foo();
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public enum Foo {A, B, C}",
            "  public void setFoo(Foo foo) {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    setFoo(Foo.B);",
            "  }",
            "}");
    // prepare property
    GenericProperty property = (GenericProperty) panel.getPropertyByTitle("foo");
    EnumPropertyEditor editor = (EnumPropertyEditor) property.getEditor();
    // text
    assertEquals("B", editor.getText(property));
    // IValueSourcePropertyEditor
    {
      Class<?> classFoo = m_lastLoader.loadClass("test.MyPanel$Foo");
      assertEquals("test.MyPanel.Foo.B", editor.getValueSource(classFoo.getEnumConstants()[1]));
    }
    // IClipboardSourceProvider
    assertEquals("test.MyPanel.Foo.B", editor.getClipboardSource(property));
  }

  /**
   * We should be able to get elements of {@link Enum} even when it is constructor parameter.
   */
  public void test_constructorParameter_enum() throws Exception {
    prepare_Foo();
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends JButton {",
            "  public MyButton(Foo foo) {",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      MyButton button = new MyButton(Foo.B);",
            "      add(button);",
            "    }",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // prepare property
    GenericProperty property = (GenericProperty) PropertyUtils.getByPath(button, "Constructor/foo");
    EnumPropertyEditor editor = (EnumPropertyEditor) property.getEditor();
    // text
    assertEquals("B", editor.getText(property));
    // elements
    {
      Enum<?>[] enums =
          (Enum<?>[]) ReflectionUtils.invokeMethod(
              editor,
              "getElements(org.eclipse.wb.internal.core.model.property.Property)",
              property);
      String[] enumStrings = GenericsUtils.getEnumStrings(enums);
      assertThat(enumStrings).isEqualTo(new String[]{"A", "B", "C"});
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Configures project for standalone <code>test.Foo</code> enum.
   */
  private void prepare_Foo_MyPanel() throws Exception {
    prepare_Foo();
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public void setFoo(Foo foo) {",
            "  }",
            "}"));
    waitForAutoBuild();
  }

  private void prepare_Foo() throws Exception {
    setFileContentSrc(
        "test/Foo.java",
        getSourceDQ(
            "package test;",
            "// filler filler filler",
            "// filler filler filler",
            "public enum Foo {",
            "  A, B, C",
            "}"));
  }
}
