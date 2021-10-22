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
package org.eclipse.wb.tests.designer.swing;

import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.widgets.Button;

/**
 * Support "Customize" tests.
 *
 * @author lobas_av
 * @author scheglov_ke
 */
public class CustomizeTest extends SwingModelTest {
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
  public void test_noCustomizer() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton('button');",
            "    add(button);",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // check action
    IMenuManager manager = getContextMenu(button);
    IAction action = findChildAction(manager, "&Customize...");
    assertNull(action);
  }

  public void test_customizer() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends JButton {",
            "}"));
    setFileContentSrc(
        "test/MyCustomizer.java",
        getTestSource(
            "import java.beans.Customizer;",
            "import java.beans.PropertyChangeListener;",
            "public class MyCustomizer extends JPanel implements Customizer {",
            "  public MyCustomizer() {",
            "    System.setProperty('wbp.test.isDesignTime',"
                + " Boolean.toString(java.beans.Beans.isDesignTime()));",
            "  }",
            "  public void setObject(Object bean) {",
            "  }",
            "  public void addPropertyChangeListener(PropertyChangeListener listener) {",
            "  }",
            "  public void removePropertyChangeListener(PropertyChangeListener listener) {",
            "  }",
            "}"));
    waitForAutoBuild();
    // create bean info
    setFileContentSrc(
        "test/MyButtonBeanInfo.java",
        getTestSource(
            "import java.beans.BeanInfo;",
            "import java.beans.BeanDescriptor;",
            "import java.beans.Introspector;",
            "import java.beans.SimpleBeanInfo;",
            "public class MyButtonBeanInfo extends SimpleBeanInfo {",
            "  private BeanDescriptor m_descriptor;",
            "  public MyButtonBeanInfo() {",
            "    m_descriptor = new BeanDescriptor(MyButton.class, MyCustomizer.class);",
            "  }",
            "  public BeanDescriptor getBeanDescriptor() {",
            "    return m_descriptor;",
            "  }",
            "  public BeanInfo[] getAdditionalBeanInfo() {",
            "    try {",
            "      BeanInfo info = Introspector.getBeanInfo(JButton.class);",
            "      return new BeanInfo[] {info};",
            "    } catch (Throwable e) {",
            "    }",
            "    return null;",
            "  }",
            "}"));
    waitForAutoBuild();
    // create panel
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    MyButton button = new MyButton();",
            "    add(button);",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // check action
    IMenuManager manager = getContextMenu(button);
    final IAction action = findChildAction(manager, "&Customize...");
    assertNotNull(action);
    // open customize dialog
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        action.run();
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Customize");
        context.clickButton("OK");
      }
    });
    // check for isDesignTime()
    {
      String value = System.clearProperty("wbp.test.isDesignTime");
      assertEquals("true", value);
    }
    // check no changes
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    MyButton button = new MyButton();",
        "    add(button);",
        "  }",
        "}");
  }

  // XXX
  public void test_customizer_chageProperties_OK() throws Exception {
    prepare_customizer_changeProperties();
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    MyButton button = new MyButton();",
            "    add(button);",
            "  }",
            "}");
    panel.refresh();
    final ComponentInfo button = panel.getChildrenComponents().get(0);
    // check action
    IMenuManager manager = getContextMenu(button);
    final IAction action = findChildAction(manager, "&Customize...");
    assertNotNull(action);
    // open customize dialog
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        action.run();
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Customize");
        // change properties
        Object object = button.getObject();
        Object customizer = ReflectionUtils.getFieldObject(object, "customizer");
        ReflectionUtils.invokeMethod(customizer, "doBeanChanges()");
        // commit changes
        context.clickButton("OK");
      }
    });
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    MyButton button = new MyButton();",
        "    button.setTitle('New title');",
        "    button.setFreeze(true);",
        "    add(button);",
        "  }",
        "}");
  }

  /**
   * Open customizer, change bean properties, but "Cancel" customizer. We check that after "Cancel"
   * properties have old values, as before customizing.
   */
  public void test_customizer_chageProperties_Cancel() throws Exception {
    prepare_customizer_changeProperties();
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    MyButton button = new MyButton();",
            "    button.setTitle('Old title');",
            "    add(button);",
            "  }",
            "}");
    panel.refresh();
    final ComponentInfo button = panel.getChildrenComponents().get(0);
    final Object buttonObject = button.getObject();
    // initial property values
    {
      assertEquals("Old title", ScriptUtils.evaluate("getTitle()", buttonObject));
      assertEquals(false, ScriptUtils.evaluate("isFreeze()", buttonObject));
    }
    // check action
    IMenuManager manager = getContextMenu(button);
    final IAction action = findChildAction(manager, "&Customize...");
    assertNotNull(action);
    // open customize dialog
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        action.run();
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Customize");
        // change properties
        Object customizer = ReflectionUtils.getFieldObject(buttonObject, "customizer");
        ReflectionUtils.invokeMethod(customizer, "doBeanChanges()");
        // cancel changes
        context.clickButton("Cancel");
      }
    });
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    MyButton button = new MyButton();",
        "    button.setTitle('Old title');",
        "    add(button);",
        "  }",
        "}");
    // object properties are not changed
    {
      assertEquals("Old title", ScriptUtils.evaluate("getTitle()", buttonObject));
      assertEquals(false, ScriptUtils.evaluate("isFreeze()", buttonObject));
    }
  }

  private void prepare_customizer_changeProperties() throws Exception {
    setFileContentSrc(
        "test/MyCustomizer.java",
        getTestSource(
            "import java.beans.Customizer;",
            "import java.beans.PropertyChangeListener;",
            "public class MyCustomizer extends JPanel implements Customizer {",
            "  private MyButton button;",
            "  public void setObject(Object bean) {",
            "    button = (MyButton) bean;",
            "    button.customizer = this;",
            "  }",
            "  public void doBeanChanges() {",
            "    button.setTitle('New title');",
            "    firePropertyChange('title', null, 'New title');",
            "    button.setFreeze(true);",
            "    firePropertyChange('freeze', null, true);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends JButton {",
            "  public Object customizer;",
            "  private String m_title;",
            "  public String getTitle() {",
            "    return m_title;",
            "  }",
            "  public void setTitle(String title) {",
            "    m_title = title;",
            "  }",
            "  private boolean m_freeze;",
            "  public boolean isFreeze() {",
            "    return m_freeze;",
            "  }",
            "  public void setFreeze(boolean freeze) {",
            "    m_freeze = freeze;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyButtonBeanInfo.java",
        getTestSource(
            "import java.beans.BeanInfo;",
            "import java.beans.BeanDescriptor;",
            "import java.beans.Introspector;",
            "import java.beans.SimpleBeanInfo;",
            "import java.beans.PropertyDescriptor;",
            "public class MyButtonBeanInfo extends SimpleBeanInfo {",
            "  private BeanDescriptor m_descriptor;",
            "  private PropertyDescriptor[] m_properties;",
            "  public MyButtonBeanInfo() {",
            "    m_descriptor = new BeanDescriptor(MyButton.class, MyCustomizer.class);",
            "    try {",
            "      m_properties = new PropertyDescriptor[2];",
            "      m_properties[0] = new PropertyDescriptor('title', MyButton.class, 'getTitle', 'setTitle');",
            "      m_properties[1] = new PropertyDescriptor('freeze', MyButton.class, 'isFreeze', 'setFreeze');",
            "    } catch(Throwable e) {",
            "    }",
            "  }",
            "  public BeanDescriptor getBeanDescriptor() {",
            "    return m_descriptor;",
            "  }",
            "  public PropertyDescriptor[] getPropertyDescriptors() {",
            "    return m_properties;",
            "  }",
            "  public BeanInfo[] getAdditionalBeanInfo() {",
            "    try {",
            "      BeanInfo info = Introspector.getBeanInfo(JButton.class);",
            "      return new BeanInfo[] {info};",
            "    } catch (Throwable e) {",
            "    }",
            "    return null;",
            "  }",
            "}"));
    waitForAutoBuild();
  }

  public void test_customizer_EXPLICIT_PROPERTY_CHANGE() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends JButton {",
            "  private String m_title;",
            "  public String getTitle() {",
            "    return m_title;",
            "  }",
            "  public void setTitle(String title) {",
            "    m_title = title;",
            "  }",
            "  private boolean m_freeze;",
            "  public boolean isFreeze() {",
            "    return m_freeze;",
            "  }",
            "  public void setFreeze(boolean freeze) {",
            "    m_freeze = freeze;",
            "  }",
            "  public Object customizer;",
            "}"));
    setFileContentSrc(
        "test/MyCustomizer.java",
        getTestSource(
            "import java.beans.Customizer;",
            "import java.beans.PropertyChangeListener;",
            "public class MyCustomizer extends JPanel implements Customizer {",
            "  public void setObject(Object bean) {",
            "    MyButton button = (MyButton)bean;",
            "    button.customizer = this;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyButtonBeanInfo.java",
        getTestSource(
            "import java.beans.BeanInfo;",
            "import java.beans.BeanDescriptor;",
            "import java.beans.Introspector;",
            "import java.beans.SimpleBeanInfo;",
            "import java.beans.PropertyDescriptor;",
            "public class MyButtonBeanInfo extends SimpleBeanInfo {",
            "  private BeanDescriptor m_descriptor;",
            "  private PropertyDescriptor[] m_properties;",
            "  public MyButtonBeanInfo() {",
            "    m_descriptor = new BeanDescriptor(MyButton.class, MyCustomizer.class);",
            "    m_descriptor.setValue('EXPLICIT_PROPERTY_CHANGE', Boolean.TRUE);",
            "    try {",
            "      m_properties = new PropertyDescriptor[2];",
            "      m_properties[0] = new PropertyDescriptor('title', MyButton.class, 'getTitle', 'setTitle');",
            "      m_properties[1] = new PropertyDescriptor('freeze', MyButton.class, 'isFreeze', 'setFreeze');",
            "    } catch(Throwable e) {",
            "    }",
            "  }",
            "  public BeanDescriptor getBeanDescriptor() {",
            "    return m_descriptor;",
            "  }",
            "  public PropertyDescriptor[] getPropertyDescriptors() {",
            "    return m_properties;",
            "  }",
            "  public BeanInfo[] getAdditionalBeanInfo() {",
            "    try {",
            "      BeanInfo info = Introspector.getBeanInfo(JButton.class);",
            "      return new BeanInfo[] {info};",
            "    } catch (Throwable e) {",
            "    }",
            "    return null;",
            "  }",
            "}"));
    waitForAutoBuild();
    // create panel
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    MyButton button = new MyButton();",
            "    add(button);",
            "  }",
            "}");
    panel.refresh();
    final ComponentInfo button = panel.getChildrenComponents().get(0);
    // check action
    IMenuManager manager = getContextMenu(button);
    final IAction action = findChildAction(manager, "&Customize...");
    assertNotNull(action);
    // open customize dialog
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        action.run();
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Customize");
        // change properties
        Object object = button.getObject();
        ReflectionUtils.invokeMethod(object, "setTitle(java.lang.String)", "test");
        ReflectionUtils.invokeMethod(object, "setFreeze(boolean)", true);
        // fire property changes
        Object customizer = ReflectionUtils.getFieldObject(object, "customizer");
        ReflectionUtils.invokeMethod(
            customizer,
            "firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)",
            "title",
            null,
            "test");
        // press "OK" button
        Button okButton = context.getButtonByText("OK");
        assertNotNull(okButton);
        context.click(okButton);
      }
    });
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    MyButton button = new MyButton();",
        "    button.setTitle('test');",
        "    add(button);",
        "  }",
        "}");
  }

  /**
   * Even if some component has no <code>BeanInfo</code>, we should use information from
   * <code>BeanInfo</code> of super classes.
   * <p>
   * 39897: Feature Request: Customizing JavaBean w/o BeanInfo
   */
  public void test_useInheritedCustomizer() throws Exception {
    setFileContentSrc(
        "test/BeanA.java",
        getTestSource(
            "public class BeanA extends JButton {",
            "  private static final long serialVersionUID = 0L;",
            "}"));
    setFileContentSrc(
        "test/BeanABeanInfo.java",
        getTestSource(
            "import java.beans.*;",
            "public class BeanABeanInfo extends SimpleBeanInfo {",
            "  private final static Class<?> beanClass = BeanA.class;",
            "  private final static Class<?> customizerClass = BeanCustomizer.class;",
            "  @Override",
            "  public BeanDescriptor getBeanDescriptor() {",
            "    return new BeanDescriptor(beanClass, customizerClass);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/BeanCustomizer.java",
        getTestSource(
            "import java.beans.*;",
            "public class BeanCustomizer extends JPanel implements Customizer {",
            "  private static final long serialVersionUID = 0L;",
            "  public void setObject(Object bean) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/BeanB.java",
        getTestSource(
            "public class BeanB extends BeanA {",
            "  private static final long serialVersionUID = 0L;",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    BeanB bean = new BeanB();",
            "    add(bean);",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // check action
    IMenuManager manager = getContextMenu(button);
    IAction action = findChildAction(manager, "&Customize...");
    assertNotNull(action);
  }
}