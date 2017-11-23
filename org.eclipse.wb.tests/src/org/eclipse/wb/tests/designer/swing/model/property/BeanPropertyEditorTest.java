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
package org.eclipse.wb.tests.designer.swing.model.property;

import org.eclipse.wb.internal.core.model.description.helpers.DescriptionPropertiesHelper;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.IValueSourcePropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.ObjectPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.property.editor.beans.ComboPropertyEditor;
import org.eclipse.wb.internal.swing.model.property.editor.beans.TextPropertyEditor;
import org.eclipse.wb.internal.swing.model.property.editor.color.ColorPropertyEditor;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import static org.assertj.core.api.Assertions.assertThat;

import java.beans.PropertyEditorSupport;
import java.lang.reflect.Constructor;

/**
 * Tests for property wrapper over {@link java.beans.PropertyEditor}.
 * 
 * @author lobas_av
 * @author scheglov_ke
 */
public class BeanPropertyEditorTest extends SwingModelTest {
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
  public void test_ignore_SunBeansEditors() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new JButton());",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // check java.awt.Color type property "background"
    Property property = button.getPropertyByTitle("background");
    assertNotNull(property);
    assertNotInstanceOf(TextPropertyEditor.class, property.getEditor());
    assertNotInstanceOf(ComboPropertyEditor.class, property.getEditor());
    assertInstanceOf(ColorPropertyEditor.class, property.getEditor());
  }

  /**
   * Test for {@link DescriptionPropertiesHelper#getEditorForEditorType(Class)}.
   */
  public void test_editorForEditor() throws Exception {
    setFileContentSrc(
        "test/MyEditor.java",
        getTestSource(
            "import java.beans.PropertyEditorSupport;",
            "public class MyEditor extends PropertyEditorSupport {",
            "}"));
    waitForAutoBuild();
    // create panel
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    waitForAutoBuild();
    // check wrapper for bean editor
    Class<?> editorType = m_lastLoader.loadClass("test.MyEditor");
    PropertyEditor propertyEditor = DescriptionPropertiesHelper.getEditorForEditorType(editorType);
    assertNotNull(propertyEditor);
  }

  public void test_editorForEditor_beanInfo() throws Exception {
    // create "value" editor
    setFileContentSrc(
        "test/MyIntEditor.java",
        getTestSource(
            "import java.beans.PropertyEditorSupport;",
            "public class MyIntEditor extends PropertyEditorSupport {",
            "}"));
    // create "value2" editor
    setFileContentSrc(
        "test/MyComboEditor.java",
        getTestSource(
            "import java.beans.PropertyEditorSupport;",
            "public class MyComboEditor extends PropertyEditorSupport {",
            "  private String[] m_items = {'111', '222', '333'};",
            "  public String[] getTags() {",
            "    return m_items;",
            "  }",
            "  public boolean supportsCustomEditor() {",
            "    return true;",
            "  }",
            "}"));
    // create test bean
    setFileContentSrc(
        "test/MyBean.java",
        getTestSource(
            "public class MyBean extends JButton {",
            "  private Object m_value;",
            "  private String m_value2;",
            "  public Object getValue() {",
            "    return m_value;",
            "  }",
            "  public void setValue(Object value) {",
            "    m_value = value;",
            "  }",
            "  public String getValue2() {",
            "    return m_value2;",
            "  }",
            "  public void setValue2(String value) {",
            "    m_value2 = value;",
            "  }",
            "}"));
    waitForAutoBuild();
    // create test bean info
    setFileContentSrc(
        "test/MyBeanBeanInfo.java",
        getTestSource(
            "import java.beans.BeanInfo;",
            "import java.beans.Introspector;",
            "import java.beans.SimpleBeanInfo;",
            "import java.beans.PropertyDescriptor;",
            "public class MyBeanBeanInfo extends SimpleBeanInfo {",
            "  private PropertyDescriptor[] m_descriptors;",
            "  public MyBeanBeanInfo() {",
            "    try {",
            "      BeanInfo info = Introspector.getBeanInfo(JButton.class);",
            "      PropertyDescriptor[] descriptors = info.getPropertyDescriptors();",
            "      m_descriptors = new PropertyDescriptor[descriptors.length + 2];",
            "      System.arraycopy(descriptors, 0, m_descriptors, 0, descriptors.length);",
            "      m_descriptors[descriptors.length] = new PropertyDescriptor('value', MyBean.class, 'getValue', 'setValue');",
            "      m_descriptors[descriptors.length].setPropertyEditorClass(MyIntEditor.class);",
            "      m_descriptors[descriptors.length + 1] = new PropertyDescriptor('value2', MyBean.class, 'getValue2', 'setValue2');",
            "      m_descriptors[descriptors.length + 1].setPropertyEditorClass(MyComboEditor.class);",
            "    } catch (Throwable e) {",
            "    }",
            "  }",
            "  public PropertyDescriptor[] getPropertyDescriptors() {",
            "    return m_descriptors;",
            "  }",
            "}"));
    waitForAutoBuild();
    // create panel
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    MyBean bean = new MyBean();",
            "    add(bean);",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo bean = panel.getChildrenComponents().get(0);
    // check text property "value"
    {
      Property property = bean.getPropertyByTitle("value");
      assertNotNull(property);
      PropertyEditor textEditor = property.getEditor();
      assertInstanceOf(TextPropertyEditor.class, textEditor);
      assertNull(textEditor.getPresentation());
    }
    // check combo property "value2"
    {
      Property property = bean.getPropertyByTitle("value2");
      assertNotNull(property);
      PropertyEditor comboEditor = property.getEditor();
      assertInstanceOf(ComboPropertyEditor.class, comboEditor);
      assertNotNull(comboEditor.getPresentation());
    }
  }

  /**
   * Test for {@link DescriptionPropertiesHelper#getEditorForType(Class)}.
   */
  public void test_EditorForType_exceptionDuringLoadingEditor() throws Exception {
    setFileContentSrc(
        "test/MyEditor.java",
        getTestSource(
            "import java.beans.PropertyEditorSupport;",
            "public class MyEditor extends PropertyEditorSupport {",
            "  public MyEditor() {",
            "    throw new IllegalStateException('actual');",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyBean.java",
        getTestSource(
            "public class MyBean extends JButton {",
            "  public void setValue(Object value) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyBeanBeanInfo.java",
        getTestSource(
            "import java.beans.*;",
            "public class MyBeanBeanInfo extends SimpleBeanInfo {",
            "  private PropertyDescriptor[] m_descriptors;",
            "  public MyBeanBeanInfo() {",
            "    try {",
            "      BeanInfo info = Introspector.getBeanInfo(JButton.class);",
            "      m_descriptors = new PropertyDescriptor[1];",
            "      m_descriptors[0] = new PropertyDescriptor('value', MyBean.class, null, 'setValue');",
            "      m_descriptors[0].setPropertyEditorClass(MyEditor.class);",
            "    } catch (Throwable e) {",
            "    }",
            "  }",
            "  public PropertyDescriptor[] getPropertyDescriptors() {",
            "    return m_descriptors;",
            "  }",
            "}"));
    waitForAutoBuild();
    // create panel
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    MyBean bean = new MyBean();",
            "    add(bean);",
            "  }",
            "}");
    ComponentInfo bean = panel.getChildrenComponents().get(0);
    // "MyEditor" throws exception, so it is ignored and default "Object" editor used
    Property property = bean.getPropertyByTitle("value");
    assertNotNull(property);
    assertInstanceOf(ObjectPropertyEditor.class, property.getEditor());
  }

  /**
   * Test for {@link DescriptionPropertiesHelper#getEditorForType(Class)}.
   */
  public void test_getEditorForType() throws Exception {
    setFileContentSrc(
        "test/MyBean.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyBean {",
            "  // filler",
            "}"));
    setFileContentSrc(
        "test/MyBeanEditor.java",
        getTestSource(
            "import java.beans.PropertyEditorSupport;",
            "public class MyBeanEditor extends PropertyEditorSupport {",
            "}"));
    waitForAutoBuild();
    // parse to access class loader
    parseContainer(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // check property editor
    Class<?> beanType = m_lastLoader.loadClass("test.MyBean");
    PropertyEditor propertyEditor = DescriptionPropertiesHelper.getEditorForType(beanType);
    assertNotNull(propertyEditor);
  }

  /**
   * Test for {@link DescriptionPropertiesHelper#getEditorForType(Class)}.
   */
  public void test_getEditorForType_whenParsing() throws Exception {
    setFileContentSrc(
        "test/MyBean.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "// filler filler filler",
            "public class MyBean {",
            "}"));
    setFileContentSrc(
        "test/MyBeanEditor.java",
        getTestSource(
            "import java.beans.PropertyEditorSupport;",
            "public class MyBeanEditor extends PropertyEditorSupport {",
            "}"));
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public void setMyBean(MyBean bean) {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse to access class loader
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    // check "myBean" property
    Property property = panel.getPropertyByTitle("myBean");
    assertNotNull(property);
    assertThat(property.getEditor()).isInstanceOf(TextPropertyEditor.class);
  }

  /**
   * Test for using {@link TextPropertyEditor}, that uses
   * {@link java.beans.PropertyEditor#getAsText()} and
   * {@link java.beans.PropertyEditor#setAsText(String)}.
   */
  public void test_getAsText_setAsText() throws Exception {
    prepare_TextPropertyEditor();
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    MyBean button = new MyBean();",
            "    add(button);",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // check text property "value"
    Property property = button.getPropertyByTitle("value");
    assertNotNull(property);
    TextPropertyEditor propertyEditor = (TextPropertyEditor) property.getEditor();
    assertNull(propertyEditor.getPresentation());
    // initially no value
    assertEquals(null, getPropertyText(property));
    // set new text, allow editor to set expression
    ReflectionUtils.invokeMethod2(
        propertyEditor,
        "setEditorText",
        Property.class,
        String.class,
        property,
        "abc");
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    MyBean button = new MyBean();",
        "    button.setValue(new MyWrapper('abc'));",
        "    add(button);",
        "  }",
        "}");
    assertEquals("abc", getPropertyText(property));
    // set empty text, so remove value
    ReflectionUtils.invokeMethod2(
        propertyEditor,
        "setEditorText",
        Property.class,
        String.class,
        property,
        "");
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    MyBean button = new MyBean();",
        "    add(button);",
        "  }",
        "}");
    assertEquals(null, getPropertyText(property));
  }

  /**
   * Prepares <code>MyWrapper</code> and <code>MyBean</code> with property "value" for testing
   * {@link TextPropertyEditor}.
   */
  private void prepare_TextPropertyEditor() throws Exception {
    setFileContentSrc(
        "test/MyEditor.java",
        getTestSource(
            "import java.beans.PropertyEditorSupport;",
            "public class MyEditor extends PropertyEditorSupport {",
            "  public String getAsText() {",
            "    if (getValue() == null) {",
            "      return null;",
            "    }",
            "    return ((MyWrapper)getValue()).getText();",
            "  }",
            "  public void setAsText(String value) {",
            "    setValue(new MyWrapper(value));",
            "  }",
            "  public String getJavaInitializationString() {",
            "    if (this.getAsText().length() > 0) {",
            "      return 'new test.MyWrapper(\\'' + getAsText() + '\\')';",
            "    }",
            "    return null;",
            "  }",
            "}"));
    prepare_MyWrapper_MyBean();
  }

  /**
   * Prepares <code>MyWrapper</code> and <code>MyBean</code> with property "value" for testing
   * {@link ComboPropertyEditor}, with items "111", "222", "333".
   */
  private void prepare_ComboPropertyEditor() throws Exception {
    setFileContentSrc(
        "test/MyEditor.java",
        getTestSource(
            "import java.beans.PropertyEditorSupport;",
            "public class MyEditor extends PropertyEditorSupport {",
            "  public String getAsText() {",
            "    if (getValue() == null) {",
            "      return null;",
            "    }",
            "    return ((MyWrapper)getValue()).getText();",
            "  }",
            "  public void setAsText(String value) {",
            "    setValue(new MyWrapper(value));",
            "  }",
            "  public String getJavaInitializationString() {",
            "    if (this.getAsText().length() > 0) {",
            "      return 'new test.MyWrapper(\\'' + getAsText() + '\\')';",
            "    }",
            "    return null;",
            "  }",
            "  private String[] m_items = {'111', '222', '333'};",
            "  public String[] getTags() {",
            "    return m_items;",
            "  }",
            "}"));
    prepare_MyWrapper_MyBean();
  }

  private void prepare_MyWrapper_MyBean() throws Exception {
    setFileContentSrc(
        "test/MyWrapper.java",
        getTestSource(
            "public class MyWrapper {",
            "  private final String m_text;",
            "  public MyWrapper(String text) {",
            "    m_text = text;",
            "  }",
            "  public String getText() {",
            "    return m_text;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyBean.java",
        getTestSource(
            "public class MyBean extends JButton {",
            "  private Object m_value;",
            "  public Object getValue() {",
            "    return m_value;",
            "  }",
            "  public void setValue(Object value) {",
            "    m_value = value;",
            "  }",
            "}"));
    // create test BeanInfo
    setFileContentSrc(
        "test/MyBeanBeanInfo.java",
        getTestSource(
            "import java.beans.BeanInfo;",
            "import java.beans.Introspector;",
            "import java.beans.SimpleBeanInfo;",
            "import java.beans.PropertyDescriptor;",
            "public class MyBeanBeanInfo extends SimpleBeanInfo {",
            "  private PropertyDescriptor[] m_descriptors;",
            "  public MyBeanBeanInfo() {",
            "    try {",
            "      BeanInfo info = Introspector.getBeanInfo(JButton.class);",
            "      PropertyDescriptor[] descriptors = info.getPropertyDescriptors();",
            "      m_descriptors = new PropertyDescriptor[descriptors.length + 1];",
            "      System.arraycopy(descriptors, 0, m_descriptors, 0, descriptors.length);",
            "      m_descriptors[descriptors.length] = new PropertyDescriptor('value', MyBean.class, 'getValue', 'setValue');",
            "      m_descriptors[descriptors.length].setPropertyEditorClass(MyEditor.class);",
            "    } catch (Throwable e) {",
            "    }",
            "  }",
            "  public PropertyDescriptor[] getPropertyDescriptors() {",
            "    return m_descriptors;",
            "  }",
            "}"));
    waitForAutoBuild();
  }

  private Object createMyWrapper(String value) throws Exception {
    Class<?> wrapperClass = m_lastLoader.loadClass("test.MyWrapper");
    Constructor<?> wrapperConstructor = ReflectionUtils.getConstructor(wrapperClass, String.class);
    return wrapperConstructor.newInstance(value);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IValueSourcePropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that {@link TextPropertyEditor} implements {@link IValueSourcePropertyEditor}, so we can
   * use {@link GenericProperty#setValue(Object)}. In particular - after using customizer.
   */
  public void test_IValueSourcePropertyEditor_TextPropertyEditor() throws Exception {
    prepare_TextPropertyEditor();
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    MyBean button = new MyBean();",
            "    add(button);",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button = panel.getChildrenComponents().get(0);
    Property property = button.getPropertyByTitle("value");
    // prepare MyWrapper value
    Object wrapper = createMyWrapper("myValue");
    // check for IValueSourcePropertyEditor
    TextPropertyEditor propertyEditor = (TextPropertyEditor) property.getEditor();
    assertEquals("new test.MyWrapper(\"myValue\")", propertyEditor.getValueSource(wrapper));
    // set value, IValueSourcePropertyEditor should be used
    property.setValue(wrapper);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    MyBean button = new MyBean();",
        "    button.setValue(new MyWrapper('myValue'));",
        "    add(button);",
        "  }",
        "}");
    assertEquals("myValue", getPropertyText(property));
  }

  /**
   * Test that {@link ComboPropertyEditor} implements {@link IValueSourcePropertyEditor}, so we can
   * use {@link GenericProperty#setValue(Object)}. In particular - after using customizer.
   */
  public void test_IValueSourcePropertyEditor_ComboPropertyEditor() throws Exception {
    prepare_ComboPropertyEditor();
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    MyBean button = new MyBean();",
            "    add(button);",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button = panel.getChildrenComponents().get(0);
    Property property = button.getPropertyByTitle("value");
    // prepare MyWrapper value
    Object wrapper = createMyWrapper("222");
    // check for IValueSourcePropertyEditor
    ComboPropertyEditor propertyEditor = (ComboPropertyEditor) property.getEditor();
    assertEquals("new test.MyWrapper(\"222\")", propertyEditor.getValueSource(wrapper));
    // set value, IValueSourcePropertyEditor should be used
    property.setValue(wrapper);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    MyBean button = new MyBean();",
        "    button.setValue(new MyWrapper('222'));",
        "    add(button);",
        "  }",
        "}");
    assertEquals("222", getPropertyText(property));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PropertyEditorSupport.setSource()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that if {@link java.beans.PropertyEditor} is {@link PropertyEditorSupport}, then its
   * {@link PropertyEditorSupport#setSource(Object)} is called.
   */
  public void test_PropertyEditorSupport_setSource_1() throws Exception {
    configure_PropertyEditorSupport_setSource_forText();
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      MyBean bean = new MyBean(1);",
            "      add(bean);",
            "    }",
            "    {",
            "      MyBean bean = new MyBean(2);",
            "      add(bean);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // "foo" for button "1"
    {
      ComponentInfo button = panel.getChildrenComponents().get(0);
      Property property = button.getPropertyByTitle("foo");
      assertEquals("1", getPropertyText(property));
    }
    // "foo" for button "2"
    {
      ComponentInfo button = panel.getChildrenComponents().get(1);
      Property property = button.getPropertyByTitle("foo");
      assertEquals("2", getPropertyText(property));
    }
  }

  /**
   * Test that if {@link java.beans.PropertyEditor} is {@link PropertyEditorSupport}, then its
   * {@link PropertyEditorSupport#setSource(Object)} is called.
   */
  public void test_PropertyEditorSupport_setSource_2() throws Exception {
    configure_PropertyEditorSupport_setSource_forText();
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      MyBean bean = new MyBean(5);",
            "      add(bean);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button = panel.getChildrenComponents().get(0);
    Property property = button.getPropertyByTitle("foo");
    // set new text, allow editor to set expression
    ReflectionUtils.invokeMethod2(
        property.getEditor(),
        "setEditorText",
        Property.class,
        String.class,
        property,
        "000");
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      MyBean bean = new MyBean(5);",
        "      bean.setFoo(5);",
        "      add(bean);",
        "    }",
        "  }",
        "}");
    assertEquals("5", getPropertyText(property));
  }

  /**
   * Test that if {@link java.beans.PropertyEditor} is {@link PropertyEditorSupport}, then its
   * {@link PropertyEditorSupport#setSource(Object)} is called.
   */
  public void test_PropertyEditorSupport_setSource_forCombo() throws Exception {
    setFileContentSrc(
        "test/MyEditor.java",
        getTestSource(
            "import java.beans.PropertyEditorSupport;",
            "public class MyEditor extends PropertyEditorSupport {",
            "  public String[] getTags() {",
            "    if (getSource() instanceof MyBean) {",
            "      int value = ((MyBean) getSource()).m_value;",
            "      return new String[] {'' + (value + 0), '' + (value + 1)};",
            "    }",
            "    return new String[] {};",
            "  }",
            "}"));
    configure_PropertyEditorSupport_setSource_justBean();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      MyBean bean = new MyBean(5);",
            "      add(bean);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button = panel.getChildrenComponents().get(0);
    Property property = button.getPropertyByTitle("foo");
    PropertyEditor propertyEditor = property.getEditor();
    //
    String[] tags =
        (String[]) ReflectionUtils.invokeMethod2(
            propertyEditor,
            "getTags",
            Property.class,
            property);
    assertThat(tags).isEqualTo(new String[]{"5", "6"});
  }

  private void configure_PropertyEditorSupport_setSource_forText() throws Exception {
    setFileContentSrc(
        "test/MyEditor.java",
        getTestSource(
            "import java.beans.PropertyEditorSupport;",
            "public class MyEditor extends PropertyEditorSupport {",
            "  public String getAsText() {",
            "    return '' + ((MyBean) getSource()).m_value;",
            "  }",
            "  public void setAsText(String text) {",
            "    // do nothing",
            "  }",
            "  public String getJavaInitializationString() {",
            "    return '' + ((MyBean) getSource()).m_value;",
            "  }",
            "}"));
    configure_PropertyEditorSupport_setSource_justBean();
  }

  private void configure_PropertyEditorSupport_setSource_justBean() throws Exception {
    setFileContentSrc(
        "test/MyBean.java",
        getTestSource(
            "public class MyBean extends JButton {",
            "  int m_value;",
            "  private int m_foo;",
            "  public MyBean(int value) {",
            "    m_value = value;",
            "  }",
            "  public int getFoo() {",
            "    return m_foo;",
            "  }",
            "  public void setFoo(int foo) {",
            "    m_foo = foo;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyBeanBeanInfo.java",
        getTestSource(
            "import java.beans.*;",
            "public class MyBeanBeanInfo extends SimpleBeanInfo {",
            "  private PropertyDescriptor[] m_descriptors;",
            "  public MyBeanBeanInfo() {",
            "    try {",
            "      BeanInfo info = Introspector.getBeanInfo(JButton.class);",
            "      PropertyDescriptor[] descriptors = info.getPropertyDescriptors();",
            "      m_descriptors = new PropertyDescriptor[descriptors.length + 1];",
            "      System.arraycopy(descriptors, 0, m_descriptors, 0, descriptors.length);",
            "      m_descriptors[descriptors.length] = new PropertyDescriptor('foo', MyBean.class, 'getFoo', 'setFoo');",
            "      m_descriptors[descriptors.length].setPropertyEditorClass(MyEditor.class);",
            "    } catch (Throwable e) {",
            "    }",
            "  }",
            "  public PropertyDescriptor[] getPropertyDescriptors() {",
            "    return m_descriptors;",
            "  }",
            "}"));
    waitForAutoBuild();
  }
}