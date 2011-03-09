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
package org.eclipse.wb.tests.designer.core.model.property;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.util.ExposePropertyAction;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jdt.core.dom.VariableDeclaration;

/**
 * Tests for {@link ExposePropertyAction}.
 * 
 * @author scheglov_ke
 */
public class ExposePropertyActionTest extends SwingModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_validOrInvalidProperty() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    ExposePropertyAction action = new ExposePropertyAction();
    //
    {
      action.setProperty(panel.getPropertyByTitle("Class"));
      assertFalse(action.isEnabled());
    }
    {
      action.setProperty(panel.getPropertyByTitle("enabled"));
      assertTrue(action.isEnabled());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validate
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_validate() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "  private int getFoo() {return 0;}",
            "  private void setBar(boolean bar) {}",
            "}");
    ExposePropertyAction action = new ExposePropertyAction();
    action.setProperty(panel.getPropertyByTitle("enabled"));
    // invalid identifier
    {
      String message = call_validate(action, "bad-name");
      assertTrue(message.contains("identifier"));
    }
    // getter already exists
    {
      String message = call_validate(action, "foo");
      assertTrue(message.contains("getFoo()"));
    }
    // setter already exists
    {
      String message = call_validate(action, "bar");
      assertTrue(message.contains("setBar(boolean)"));
    }
    // OK
    assertNull(call_validate(action, "someUniqueProperty"));
  }

  private static String call_validate(ExposePropertyAction action, String exposedName)
      throws Exception {
    return (String) ReflectionUtils.invokeMethod(action, "validate(java.lang.String)", exposedName);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Preview
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Property with primitive type.
   */
  public void test_getPreviewSource_primitive() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    //
    assertEquals(
        getSourceDQ(
            "...",
            "  public float getButtonAlignmentX() {",
            "    return button.getAlignmentX();",
            "  }",
            "  public void setButtonAlignmentX(float alignmentX) {",
            "    button.setAlignmentX(alignmentX);",
            "  }",
            "..."),
        call_getPreview(button, "alignmentX", "buttonAlignmentX", true));
  }

  /**
   * Test case when parameter of setter conflicts with existing {@link VariableDeclaration}.
   */
  public void test_getPreviewSource_parameter() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private int alignmentX;",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    //
    assertEquals(
        getSourceDQ(
            "...",
            "  public float getButtonAlignmentX() {",
            "    return button.getAlignmentX();",
            "  }",
            "  public void setButtonAlignmentX(float alignmentX_1) {",
            "    button.setAlignmentX(alignmentX_1);",
            "  }",
            "..."),
        call_getPreview(button, "alignmentX", "buttonAlignmentX", true));
  }

  /**
   * Property with qualified type name.
   */
  public void test_getPreviewSource_qualified() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    //
    assertEquals(
        getSourceDQ(
            "...",
            "  public String getButtonName() {",
            "    return button.getName();",
            "  }",
            "  public void setButtonName(String name) {",
            "    button.setName(name);",
            "  }",
            "..."),
        call_getPreview(button, "name", "buttonName", true));
  }

  /**
   * Property with array of objects type name.
   */
  public void test_getPreviewSource_qualifiedArray() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends JButton {",
            "  public String[] getItems() {",
            "    return null;",
            "  }",
            "  public void setItems(String[] items) {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    MyButton button = new MyButton();",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // check preview
    assertEquals(
        getSourceDQ(
            "...",
            "  public String[] getButtonItems() {",
            "    return button.getItems();",
            "  }",
            "  public void setButtonItems(String[] items) {",
            "    button.setItems(items);",
            "  }",
            "..."),
        call_getPreview(button, "items", "buttonItems", true));
  }

  /**
   * <code>protected</code> modifier for exposed.
   */
  public void test_getPreviewSource_protected() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    //
    assertEquals(
        getSourceDQ(
            "...",
            "  protected String getButtonName() {",
            "    return button.getName();",
            "  }",
            "  protected void setButtonName(String name) {",
            "    button.setName(name);",
            "  }",
            "..."),
        call_getPreview(button, "name", "buttonName", false));
  }

  private static String call_getPreview(JavaInfo component,
      String propertyName,
      String exposedName,
      boolean isPublic) throws Exception {
    String initialSource = component.getEditor().getSource();
    // prepare action
    ExposePropertyAction action;
    {
      action = new ExposePropertyAction();
      action.setProperty(component.getPropertyByTitle(propertyName));
      assertTrue(action.isEnabled());
    }
    // get preview
    String previewSource;
    {
      assertNull(call_validate(action, exposedName));
      previewSource =
          (String) ReflectionUtils.invokeMethod2(
              action,
              "getPreviewSource",
              boolean.class,
              isPublic);
    }
    // assert that source is not changed
    assertEquals(initialSource, component.getEditor().getSource());
    // OK
    return previewSource;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // expose()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Expose <code>String</code> property.
   */
  public void test_expose_String() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    //
    call_expose(button, "name", "buttonName", true);
    assertEditor(
        "public class Test extends JPanel {",
        "  private JButton button;",
        "  public Test() {",
        "    button = new JButton();",
        "    add(button);",
        "  }",
        "  public String getButtonName() {",
        "    return button.getName();",
        "  }",
        "  public void setButtonName(String name) {",
        "    button.setName(name);",
        "  }",
        "}");
  }

  /**
   * Expose <code>String[]</code> property.
   */
  public void test_expose_StringArray() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends JButton {",
            "  public String[] getItems() {",
            "    return null;",
            "  }",
            "  public void setItems(String[] items) {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    MyButton button = new MyButton();",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    //
    call_expose(button, "items", "buttonItems", true);
    assertEditor(
        "public class Test extends JPanel {",
        "  private MyButton button;",
        "  public Test() {",
        "    button = new MyButton();",
        "    add(button);",
        "  }",
        "  public String[] getButtonItems() {",
        "    return button.getItems();",
        "  }",
        "  public void setButtonItems(String[] items) {",
        "    button.setItems(items);",
        "  }",
        "}");
  }

  private static void call_expose(JavaInfo component,
      String propertyName,
      String exposedName,
      boolean isPublic) throws Exception {
    // prepare action
    ExposePropertyAction action;
    {
      action = new ExposePropertyAction();
      action.setProperty(component.getPropertyByTitle(propertyName));
    }
    // do expose
    assertNull(call_validate(action, exposedName));
    ReflectionUtils.invokeMethod2(action, "expose", boolean.class, isPublic);
  }
}
