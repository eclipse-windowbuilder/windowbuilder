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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.ObjectPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.model.variable.LazyVariableSupport;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.TreeItem;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * Tests for {@link ObjectPropertyEditor}.
 * 
 * @author scheglov_ke
 */
public class ObjectPropertyEditorTest extends SwingModelTest {
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
   * {@link ObjectPropertyEditor} can be used to select object of any type, as non-visual bean.
   */
  public void test_nonVisualBean() throws Exception {
    setJavaContentSrc("test", "MyObject", new String[]{
        "public class MyObject {",
        "  // filler filler filler filler filler",
        "}"}, null);
    setJavaContentSrc("test", "MyComponent", new String[]{
        "public class MyComponent extends JLabel {",
        "  public void setValue(MyObject object) {",
        "  }",
        "}"}, null);
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private final MyComponent component = new MyComponent();",
            "  /**",
            "  * @wbp.nonvisual location=0,0",
            "  */",
            "  private final MyObject object_1 = new MyObject();",
            "  /**",
            "  * @wbp.nonvisual location=0,0",
            "  */",
            "  private final Object object_2 = new Object();",
            "  public Test() {",
            "    add(component);",
            "  }",
            "}");
    panel.refresh();
    // no "Object" properties for standard Swing components
    {
      assertNull(panel.getPropertyByTitle("dropTarget"));
    }
    // hierarchy 
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(component)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: test.MyComponent} {field-initializer: component} {/new MyComponent()/ /add(component)/}",
        "  {NonVisualBeans}",
        "    {new: test.MyObject} {field-initializer: object_1} {/new MyObject()/}",
        "    {new: java.lang.Object} {field-initializer: object_2} {/new Object()/}");
    ComponentInfo component = panel.getChildrenComponents().get(0);
    // prepare property
    final Property property = component.getPropertyByTitle("value");
    assertNotNull(property);
    assertFalse(property.isModified());
    assertSame(PropertyCategory.ADVANCED, property.getCategory());
    // prepare editor
    final PropertyEditor propertyEditor = property.getEditor();
    assertThat(propertyEditor).isSameAs(ObjectPropertyEditor.INSTANCE);
    // animate
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        openPropertyDialog(property);
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("value");
        TreeItem panelItem = context.getTreeItem("(javax.swing.JPanel)");
        Button okButton = context.getButtonByText("OK");
        // initially "panel" selected, so invalid
        assertFalse(okButton.isEnabled());
        // prepare "non-visual beans" item
        TreeItem beansContainer;
        {
          TreeItem[] childItems = panelItem.getItems();
          assertThat(childItems).hasSize(1);
          assertEquals("(non-visual beans)", childItems[0].getText());
          beansContainer = childItems[0];
        }
        // prepare "object_1"
        TreeItem myObjectItem;
        {
          TreeItem[] beanItems = beansContainer.getItems();
          assertThat(beanItems).hasSize(1);
          assertEquals("object_1", beanItems[0].getText());
          myObjectItem = beanItems[0];
        }
        // container - invalid
        UiContext.setSelection(beansContainer);
        assertFalse(okButton.isEnabled());
        // "object_1" - valid
        UiContext.setSelection(myObjectItem);
        assertTrue(okButton.isEnabled());
        // click OK
        context.clickButton(okButton);
      }
    });
    // check
    assertEditor(
        "public class Test extends JPanel {",
        "  private final MyComponent component = new MyComponent();",
        "  /**",
        "  * @wbp.nonvisual location=0,0",
        "  */",
        "  private final MyObject object_1 = new MyObject();",
        "  /**",
        "  * @wbp.nonvisual location=0,0",
        "  */",
        "  private final Object object_2 = new Object();",
        "  public Test() {",
        "    component.setValue(object_1);",
        "    add(component);",
        "  }",
        "}");
    assertNoErrors(panel);
    assertEquals("object_1", getPropertyText(property));
  }

  /**
   * {@link ObjectPropertyEditor} can be used to select subclass {@link Component}.
   */
  public void test_subclassOfComponent() throws Exception {
    setJavaContentSrc("test", "MyComponent", new String[]{
        "public class MyComponent extends JLabel {",
        "  public void setButton(JButton button) {",
        "  }",
        "}"}, null);
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private final MyComponent component = new MyComponent();",
            "  private final JButton button = new JButton();",
            "  private final JTextField textField = new JTextField();",
            "  public Test() {",
            "    add(component);",
            "    add(button);",
            "    add(textField);",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo component = panel.getChildrenComponents().get(0);
    // prepare property
    final Property property = component.getPropertyByTitle("button");
    assertNotNull(property);
    assertFalse(property.isModified());
    // prepare editor
    final PropertyEditor propertyEditor = property.getEditor();
    assertThat(propertyEditor).isSameAs(ObjectPropertyEditor.INSTANCE);
    // animate
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        openPropertyDialog(property);
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("button");
        TreeItem panelItem = context.getTreeItem("(javax.swing.JPanel)");
        Button okButton = context.getButtonByText("OK");
        // initially "panel" selected, so invalid
        assertFalse(okButton.isEnabled());
        // prepare items
        TreeItem[] childItems = panelItem.getItems();
        assertThat(childItems).hasSize(1);
        assertEquals("button", childItems[0].getText());
        // JButton - valid
        UiContext.setSelection(childItems[0]);
        assertTrue(okButton.isEnabled());
        // click OK
        context.clickButton(okButton);
      }
    });
    // check
    assertEditor(
        "public class Test extends JPanel {",
        "  private final MyComponent component = new MyComponent();",
        "  private final JButton button = new JButton();",
        "  private final JTextField textField = new JTextField();",
        "  public Test() {",
        "    component.setButton(button);",
        "    add(component);",
        "    add(button);",
        "    add(textField);",
        "  }",
        "}");
    assertNoErrors(panel);
    assertEquals("button", getPropertyText(property));
  }

  /**
   * {@link ObjectPropertyEditor} should select current value in dialog.
   */
  public void test_initialSelection() throws Exception {
    setJavaContentSrc("test", "MyComponent", new String[]{
        "public class MyComponent extends JLabel {",
        "  public void setButton(JButton button) {",
        "  }",
        "}"}, null);
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private final MyComponent component = new MyComponent();",
            "  private final JButton button_1 = new JButton();",
            "  private final JButton button_2 = new JButton();",
            "  public Test() {",
            "    component.setButton(button_2);",
            "    add(component);",
            "    add(button_1);",
            "    add(button_2);",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo component = getJavaInfoByName("component");
    // prepare property
    final Property property = component.getPropertyByTitle("button");
    assertNotNull(property);
    // prepare editor
    final PropertyEditor propertyEditor = property.getEditor();
    assertThat(propertyEditor).isSameAs(ObjectPropertyEditor.INSTANCE);
    // animate
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        openPropertyDialog(property);
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("button");
        // "button_2" is selected
        try {
          TreeItem itemButton_2 = context.getTreeItem("button_2");
          TreeItem[] selection = itemButton_2.getParent().getSelection();
          assertThat(selection).containsOnly(itemButton_2);
        } finally {
          context.clickButton("Cancel");
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // JLabel.setLabelFor(java.awt.Component)
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * No invocation for this method.
   */
  public void test_getText_noInvocation() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JLabel label = new JLabel();",
            "      add(label);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo label = panel.getChildrenComponents().get(0);
    // check "labelFor" property
    Property labelForProperty = label.getPropertyByTitle("labelFor");
    assertNotNull(labelForProperty);
    assertSame(PropertyCategory.PREFERRED, labelForProperty.getCategory());
    // no "setLabelFor()" invocation, so no text
    assertFalse(labelForProperty.isModified());
    assertNull(getPropertyText(labelForProperty));
    // check sub-properties
    {
      ObjectPropertyEditor opEditor = (ObjectPropertyEditor) labelForProperty.getEditor();
      assertThat(opEditor.getProperties(labelForProperty).length).isEqualTo(0);
    }
  }

  /**
   * We have invocation for this method.
   */
  public void test_getText_hasInvocation() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private final JLabel label = new JLabel();",
            "  private final JTextField textField = new JTextField();",
            "  public Test() {",
            "    add(label);",
            "    label.setLabelFor(textField);",
            "    add(textField);",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo label = panel.getChildrenComponents().get(0);
    // check "labelFor" property
    Property labelForProperty = label.getPropertyByTitle("labelFor");
    assertTrue(labelForProperty.isModified());
    assertEquals("textField", getPropertyText(labelForProperty));
    // check sub-properties
    {
      ObjectPropertyEditor opEditor = (ObjectPropertyEditor) labelForProperty.getEditor();
      assertThat(opEditor.getProperties(labelForProperty).length).isGreaterThan(0);
    }
  }

  /**
   * Absolute layout has <code>null</code> component class, should be handled correctly.
   */
  public void test_withAbsoluteLayout() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(null);",
            "    add(new JLabel());",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo label = panel.getChildrenComponents().get(0);
    // prepare property
    final Property property = label.getPropertyByTitle("labelFor");
    final PropertyEditor propertyEditor = property.getEditor();
    assertThat(propertyEditor).isSameAs(ObjectPropertyEditor.INSTANCE);
    // animate - just open and ensure that dialog opened (no exception during this)
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        openPropertyDialog(property);
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("labelFor");
        context.clickButton("Cancel");
      }
    });
  }

  /**
   * {@link JLabel} is before {@link JTextField}.
   */
  public void test_setComponent_labelBefore() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JLabel label = new JLabel();",
            "      add(label);",
            "    }",
            "    {",
            "      JTextField textField = new JTextField();",
            "      add(textField);",
            "    }",
            "  }",
            "}");
    ComponentInfo label = panel.getChildrenComponents().get(0);
    ComponentInfo textField = panel.getChildrenComponents().get(1);
    // set "labelFor" property
    Property labelForProperty = label.getPropertyByTitle("labelFor");
    setComponent(labelForProperty, textField);
    assertEditor(
        "public class Test extends JPanel {",
        "  private JLabel label;",
        "  public Test() {",
        "    {",
        "      label = new JLabel();",
        "      add(label);",
        "    }",
        "    {",
        "      JTextField textField = new JTextField();",
        "      label.setLabelFor(textField);",
        "      add(textField);",
        "    }",
        "  }",
        "}");
  }

  /**
   * {@link JLabel} is after {@link JTextField}.
   */
  public void test_setComponent_labelAfter() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JTextField textField = new JTextField();",
            "      add(textField);",
            "    }",
            "    {",
            "      JLabel label = new JLabel();",
            "      add(label);",
            "    }",
            "  }",
            "}");
    ComponentInfo textField = panel.getChildrenComponents().get(0);
    ComponentInfo label = panel.getChildrenComponents().get(1);
    // set "labelFor" property
    Property labelForProperty = label.getPropertyByTitle("labelFor");
    setComponent(labelForProperty, textField);
    assertEditor(
        "public class Test extends JPanel {",
        "  private JTextField textField;",
        "  public Test() {",
        "    {",
        "      textField = new JTextField();",
        "      add(textField);",
        "    }",
        "    {",
        "      JLabel label = new JLabel();",
        "      label.setLabelFor(textField);",
        "      add(label);",
        "    }",
        "  }",
        "}");
  }

  /**
   * {@link LazyVariableSupport} for {@link JLabel} and {@link JTextField}. We can put
   * {@link JLabel#setLabelFor(Component)} in any place, but prefer {@link JLabel} method.
   */
  public void test_setComponent_lazy() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private JLabel m_label;",
            "  private JTextField m_textField;",
            "  public Test() {",
            "    add(getLabel());",
            "    add(getTextField());",
            "  }",
            "  private JLabel getLabel() {",
            "    if (m_label == null) {",
            "      m_label = new JLabel();",
            "    }",
            "    return m_label;",
            "  }",
            "  private JTextField getTextField() {",
            "    if (m_textField == null) {",
            "      m_textField = new JTextField();",
            "    }",
            "    return m_textField;",
            "  }",
            "}");
    ComponentInfo label = panel.getChildrenComponents().get(0);
    ComponentInfo textField = panel.getChildrenComponents().get(1);
    // set "labelFor" property
    Property labelForProperty = label.getPropertyByTitle("labelFor");
    setComponent(labelForProperty, textField);
    assertEditor(
        "public class Test extends JPanel {",
        "  private JLabel m_label;",
        "  private JTextField m_textField;",
        "  public Test() {",
        "    add(getLabel());",
        "    add(getTextField());",
        "  }",
        "  private JLabel getLabel() {",
        "    if (m_label == null) {",
        "      m_label = new JLabel();",
        "      m_label.setLabelFor(getTextField());",
        "    }",
        "    return m_label;",
        "  }",
        "  private JTextField getTextField() {",
        "    if (m_textField == null) {",
        "      m_textField = new JTextField();",
        "    }",
        "    return m_textField;",
        "  }",
        "}");
  }

  /**
   * Set to different {@link JTextField}.
   */
  public void test_setComponent_newComponent() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private JLabel label;",
            "  public Test() {",
            "    {",
            "      label = new JLabel();",
            "      add(label);",
            "    }",
            "    {",
            "      JTextField textField_1 = new JTextField();",
            "      label.setLabelFor(textField_1);",
            "      add(textField_1);",
            "    }",
            "    {",
            "      JTextField textField_2 = new JTextField();",
            "      add(textField_2);",
            "    }",
            "  }",
            "}");
    ComponentInfo label = panel.getChildrenComponents().get(0);
    ComponentInfo textField_2 = panel.getChildrenComponents().get(2);
    // set "labelFor" property
    Property labelForProperty = label.getPropertyByTitle("labelFor");
    setComponent(labelForProperty, textField_2);
    assertEditor(
        "public class Test extends JPanel {",
        "  private JLabel label;",
        "  public Test() {",
        "    {",
        "      label = new JLabel();",
        "      add(label);",
        "    }",
        "    {",
        "      JTextField textField_1 = new JTextField();",
        "      add(textField_1);",
        "    }",
        "    {",
        "      JTextField textField_2 = new JTextField();",
        "      label.setLabelFor(textField_2);",
        "      add(textField_2);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Use <code>null</code> to remove existing value.
   */
  public void test_setComponent_noComponent() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private JLabel label;",
            "  public Test() {",
            "    {",
            "      label = new JLabel();",
            "      add(label);",
            "    }",
            "    {",
            "      JTextField textField_1 = new JTextField();",
            "      label.setLabelFor(textField_1);",
            "      add(textField_1);",
            "    }",
            "  }",
            "}");
    ComponentInfo label = panel.getChildrenComponents().get(0);
    // set "labelFor" property
    Property labelForProperty = label.getPropertyByTitle("labelFor");
    setComponent(labelForProperty, null);
    assertEditor(
        "public class Test extends JPanel {",
        "  private JLabel label;",
        "  public Test() {",
        "    {",
        "      label = new JLabel();",
        "      add(label);",
        "    }",
        "    {",
        "      JTextField textField_1 = new JTextField();",
        "      add(textField_1);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Use {@link ObjectPropertyEditor} for constructor argument.
   */
  public void test_setComponent_constructor() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public MyPanel(JButton button) {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "public class Test extends JPanel {",
        "  private final JButton button_1 = new JButton();",
        "  private final JButton button_2 = new JButton();",
        "  private final MyPanel myPanel = new MyPanel(button_1);",
        "  public Test() {",
        "    add(button_1);",
        "    add(button_2);",
        "    add(myPanel);",
        "  }",
        "}");
    refresh();
    ContainerInfo myPanel = getJavaInfoByName("myPanel");
    ComponentInfo button_2 = getJavaInfoByName("button_2");
    // prepare Property
    Property property = PropertyUtils.getByPath(myPanel, "Constructor/button");
    assertNotNull(property);
    // initially "button_1"
    assertTrue(property.isModified());
    assertEquals("button_1", getPropertyText(property));
    // set "button_2"
    setComponent(property, button_2);
    assertEditor(
        "public class Test extends JPanel {",
        "  private final JButton button_1 = new JButton();",
        "  private final JButton button_2 = new JButton();",
        "  private final MyPanel myPanel = new MyPanel(button_2);",
        "  public Test() {",
        "    add(button_1);",
        "    add(button_2);",
        "    add(myPanel);",
        "  }",
        "}");
  }

  private static void setComponent(Property property, JavaInfo component) throws Exception {
    ReflectionUtils.invokeMethod2(
        property.getEditor(),
        "setComponent",
        GenericProperty.class,
        JavaInfo.class,
        property,
        component);
  }
}
