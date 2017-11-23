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
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.GenericPropertyGetValue;
import org.eclipse.wb.core.model.broadcast.GenericPropertyGetValueEx;
import org.eclipse.wb.core.model.broadcast.GenericPropertySetValue;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.GenericPropertyComposite;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.JavaProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.accessor.ConstructorAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.FieldAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.SetterAccessor;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipProvider;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipTextProvider;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;
import org.eclipse.wb.tests.designer.tests.common.GenericPropertyNoValue;
import org.eclipse.wb.tests.designer.tests.common.PropertyNoValue;

import org.eclipse.jdt.core.dom.Expression;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author scheglov_ke
 */
public class PropertyTest extends SwingModelTest {
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
  // UNKNOWN_VALUE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_unknown() throws Exception {
    assertEquals("UNKNOWN_VALUE", Property.UNKNOWN_VALUE.toString());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // JavaProperty
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link JavaProperty#getJavaInfo()}.
   */
  public void test_JavaProperty_getJavaInfo() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    JavaProperty property = (JavaProperty) panel.getPropertyByTitle("enabled");
    //
    assertSame(panel, property.getObjectInfo());
    assertSame(panel, property.getJavaInfo());
    assertSame(panel, property.getAdapter(ObjectInfo.class));
  }

  /**
   * Test for {@link JavaProperty#getTitle()}.
   */
  public void test_JavaProperty_getTitle() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    JavaProperty property = (JavaProperty) panel.getPropertyByTitle("enabled");
    // default title
    assertEquals("enabled", property.getTitle());
    // update title
    property.setTitle("newTitle");
    assertEquals("newTitle", property.getTitle());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GenericPropertyComposite
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link GenericPropertyComposite}, implementation for
   * {@link Property#getComposite(Property[])} .
   */
  public void test_GenericPropertyComposite() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JButton button_1 = new JButton('111');",
            "    add(button_1);",
            "    //",
            "    JButton button_2 = new JButton('222');",
            "    add(button_2);",
            "  }",
            "}");
    ComponentInfo button_1 = panel.getChildrenComponents().get(0);
    ComponentInfo button_2 = panel.getChildrenComponents().get(1);
    // "icon" property
    {
      GenericProperty iconProperty_1 = (GenericProperty) button_1.getPropertyByTitle("icon");
      GenericProperty iconProperty_2 = (GenericProperty) button_2.getPropertyByTitle("icon");
      assertFalse(iconProperty_1.isModified());
      assertFalse(iconProperty_2.isModified());
      assertSame(null, iconProperty_1.getValue());
      assertSame(null, iconProperty_2.getValue());
      // create complex property
      Property[] properties = new Property[]{iconProperty_1, iconProperty_2};
      GenericProperty compositeProperty = (GenericProperty) iconProperty_1.getComposite(properties);
      // check complex property
      assertEquals("icon", compositeProperty.getTitle());
      assertFalse(compositeProperty.isModified());
      assertSame(null, compositeProperty.getValue());
      assertNull(compositeProperty.getExpression());
    }
    // "text" property
    {
      GenericProperty textProperty_1 = (GenericProperty) button_1.getPropertyByTitle("text");
      GenericProperty textProperty_2 = (GenericProperty) button_2.getPropertyByTitle("text");
      assertTrue(textProperty_1.isModified());
      assertTrue(textProperty_2.isModified());
      assertEquals("111", textProperty_1.getValue());
      assertEquals("222", textProperty_2.getValue());
      // create complex property
      Property[] properties = new Property[]{textProperty_1, textProperty_2};
      GenericProperty compositeProperty = (GenericProperty) textProperty_1.getComposite(properties);
      // check complex property
      assertEquals("text", compositeProperty.getTitle());
      assertTrue(compositeProperty.isModified());
      assertEquals(textProperty_1.getCategory(), compositeProperty.getCategory());
      assertEquals(textProperty_2.getCategory(), compositeProperty.getCategory());
      // equals
      {
        assertTrue(compositeProperty.equals(compositeProperty));
        assertFalse(compositeProperty.equals(this));
        //
        Property composite2 = textProperty_2.getComposite(properties);
        assertTrue(compositeProperty.equals(composite2));
      }
      // check different values
      assertSame(Property.UNKNOWN_VALUE, compositeProperty.getValue());
      assertNull(compositeProperty.getExpression());
      // setValue()
      {
        compositeProperty.setValue("333");
        assertEquals("333", compositeProperty.getValue());
        assertEquals("\"333\"", m_lastEditor.getSource(compositeProperty.getExpression()));
        assertEditor(
            "class Test extends JPanel {",
            "  Test() {",
            "    JButton button_1 = new JButton('333');",
            "    add(button_1);",
            "    //",
            "    JButton button_2 = new JButton('333');",
            "    add(button_2);",
            "  }",
            "}");
      }
      // setExpression()
      {
        String value = "444";
        compositeProperty.setExpression(StringConverter.INSTANCE.toJavaSource(panel, value), value);
        assertEquals("444", compositeProperty.getValue());
        assertEquals("\"444\"", m_lastEditor.getSource(compositeProperty.getExpression()));
        assertEditor(
            "class Test extends JPanel {",
            "  Test() {",
            "    JButton button_1 = new JButton('444');",
            "    add(button_1);",
            "    //",
            "    JButton button_2 = new JButton('444');",
            "    add(button_2);",
            "  }",
            "}");
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GenericPropertyComposite.getDefaultValue()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link GenericPropertyComposite#getDefaultValue()}.
   */
  public void test_GenericPropertyComposite_getDefaultValue_sameValue() throws Exception {
    GenericProperty property_1 = create_GenericProperty_defaultValue("A");
    GenericProperty property_2 = create_GenericProperty_defaultValue("A");
    GenericPropertyComposite composite = GenericPropertyComposite.create(property_1, property_2);
    assertEquals("A", composite.getDefaultValue());
  }

  /**
   * Test for {@link GenericPropertyComposite#getDefaultValue()}.
   */
  public void test_GenericPropertyComposite_getDefaultValue_differentValues() throws Exception {
    GenericProperty property_1 = create_GenericProperty_defaultValue("A");
    GenericProperty property_2 = create_GenericProperty_defaultValue("B");
    GenericPropertyComposite composite = GenericPropertyComposite.create(property_1, property_2);
    assertSame(Property.UNKNOWN_VALUE, composite.getDefaultValue());
  }

  private static GenericPropertyNoValue create_GenericProperty_defaultValue(final String defaultValue) {
    return new GenericPropertyNoValue(null, "title", StringPropertyEditor.INSTANCE) {
      @Override
      public Object getDefaultValue() throws Exception {
        return defaultValue;
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GenericPropertyComposite.getType()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link GenericPropertyComposite#getType()}.
   */
  public void test_GenericPropertyComposite_getType_sameTypes() throws Exception {
    GenericProperty property_1 = createGenericProperty_withType(String.class);
    GenericProperty property_2 = createGenericProperty_withType(String.class);
    GenericProperty composite = GenericPropertyComposite.create(property_1, property_2);
    assertSame(String.class, composite.getType());
  }

  /**
   * Test for {@link GenericPropertyComposite#getType()}.
   */
  public void test_GenericPropertyComposite_getType_differentTypes() throws Exception {
    GenericProperty property_1 = createGenericProperty_withType(String.class);
    GenericProperty property_2 = createGenericProperty_withType(Object.class);
    GenericProperty composite = GenericPropertyComposite.create(property_1, property_2);
    assertSame(null, composite.getType());
  }

  /**
   * Test for {@link GenericPropertyComposite#getType()}.
   */
  public void test_GenericPropertyComposite_getType_oneNull() throws Exception {
    GenericProperty property_1 = createGenericProperty_withType(null);
    GenericProperty property_2 = createGenericProperty_withType(String.class);
    GenericProperty composite = GenericPropertyComposite.create(property_1, property_2);
    assertSame(null, composite.getType());
  }

  private static GenericPropertyNoValue createGenericProperty_withType(final Class<?> type) {
    return new GenericPropertyNoValue(null, "title", StringPropertyEditor.INSTANCE) {
      @Override
      public Class<?> getType() {
        return type;
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GenericPropertyImpl constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for creating copy of {@link GenericPropertyImpl} using
   * {@link GenericPropertyImpl#GenericPropertyImpl(GenericPropertyImpl)} constructor.
   */
  public void test_GenericPropertyImpl_copyConstructor() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    // prepare "enabled" property
    GenericPropertyImpl enabledProperty = (GenericPropertyImpl) panel.getPropertyByTitle("enabled");
    assertNotNull(enabledProperty);
    assertEquals("enabled", enabledProperty.getTitle());
    assertEquals(boolean.class, enabledProperty.getType());
    // check copy
    GenericPropertyImpl enabledCopy = new GenericPropertyImpl(enabledProperty);
    assertNotSame(enabledProperty, enabledCopy);
    assertEquals("enabled", enabledCopy.getTitle());
    assertSame(enabledProperty.getCategory(), enabledCopy.getCategory());
    assertEquals(
        getGenericPropertyAccessors(enabledProperty),
        getGenericPropertyAccessors(enabledCopy));
    assertSame(enabledProperty.getEditor(), enabledCopy.getEditor());
    assertSame(enabledProperty.getDescription(), enabledCopy.getDescription());
    assertEquals(boolean.class, enabledCopy.getType());
  }

  /**
   * Test for creating copy of {@link GenericPropertyImpl} using
   * {@link GenericPropertyImpl#GenericPropertyImpl(GenericPropertyImpl, String)} constructor.
   */
  public void test_GenericPropertyImpl_copyConstructor2() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    // prepare "enabled" property
    GenericPropertyImpl enabledProperty = (GenericPropertyImpl) panel.getPropertyByTitle("enabled");
    assertNotNull(enabledProperty);
    assertEquals("enabled", enabledProperty.getTitle());
    // check copy
    GenericPropertyImpl enabledCopy = new GenericPropertyImpl(enabledProperty, "enabledCopy");
    assertNotSame(enabledProperty, enabledCopy);
    assertEquals("enabledCopy", enabledCopy.getTitle());
    assertSame(enabledProperty.getCategory(), enabledCopy.getCategory());
    assertEquals(
        getGenericPropertyAccessors(enabledProperty),
        getGenericPropertyAccessors(enabledCopy));
    assertSame(enabledProperty.getEditor(), enabledCopy.getEditor());
    assertSame(enabledProperty.getDescription(), enabledCopy.getDescription());
  }

  /**
   * Test for creating copy of {@link GenericPropertyImpl}.
   * <p>
   * "Constructor/text" has no {@link GenericPropertyDescription}, but we still want to know type in
   * copy.
   */
  public void test_GenericPropertyImpl_copyConstructor_copyType() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new JButton('a'));",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // prepare property
    GenericPropertyImpl textProperty =
        (GenericPropertyImpl) PropertyUtils.getByPath(button, "Constructor/text");
    assertNotNull(textProperty);
    assertSame(String.class, textProperty.getType());
    // check copy
    GenericPropertyImpl textCopy = new GenericPropertyImpl(textProperty, "Text");
    assertSame(String.class, textCopy.getType());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GenericPropertyImpl value
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that if value was provided into {@link GenericProperty#setExpression(String, Object)},
   * then this value can be accessed using {@link GenericProperty#getValue()} even without
   * refresh().
   */
  public void test_GenericPropertyImpl_setValue_getValue_withoutRefresh() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    panel.startEdit();
    // use "name" property
    GenericProperty property = (GenericProperty) panel.getPropertyByTitle("name");
    // no value initially
    assertSame(null, property.getValue());
    // set value (new Expression)
    {
      String value = "myName";
      property.setExpression(StringConverter.INSTANCE.toJavaSource(null, value), value);
      assertSame(value, property.getValue());
    }
    // set value again (update existing Expression) {
    {
      String value = "newName";
      property.setExpression(StringConverter.INSTANCE.toJavaSource(null, value), value);
      assertSame(value, property.getValue());
    }
  }

  /**
   * When we use {@link Property#UNKNOWN_VALUE} for
   * {@link GenericProperty#setExpression(String, Object)} and <b>do</b> refresh, we should not
   * break existing value.
   */
  public void test_GenericPropertyImpl_setValueUNKNOWN_getValue_withRefresh() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    // use "name" property
    GenericProperty property = (GenericProperty) panel.getPropertyByTitle("name");
    // no value initially
    assertSame(null, property.getValue());
    // set value
    {
      String value = "myName";
      property.setExpression(
          StringConverter.INSTANCE.toJavaSource(null, value),
          Property.UNKNOWN_VALUE);
      assertEquals(value, property.getValue());
    }
    // set "null" value
    {
      property.setExpression("null", Property.UNKNOWN_VALUE);
      assertEquals(null, property.getValue());
    }
  }

  /**
   * Test that {@link GenericProperty#setExpression(String, Object)} accepts source with "%this%"
   * pattern.
   */
  public void test_GenericPropertyImpl_setExpression() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    // use "toolTipText" property
    GenericProperty property = (GenericProperty) panel.getPropertyByTitle("toolTipText");
    // set expression
    property.setExpression("\"name: \" + %this%.getName()", Property.UNKNOWN_VALUE);
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setToolTipText('name: ' + getName());",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that we can use
   * {@link GenericPropertySetValue#invoke(GenericPropertyImpl, Object[], boolean[])} for
   * validation.
   */
  public void test_GenericProperty_valueValidation_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    Property enabledProperty = panel.getPropertyByTitle("enabled");
    // add listener that prevents "enabled" modification
    panel.addBroadcastListener(new GenericPropertySetValue() {
      public void invoke(GenericPropertyImpl property, Object[] value, boolean[] shouldSetValue)
          throws Exception {
        shouldSetValue[0] &= !"enabled".equals(property.getTitle());
      }
    });
    // try to set value
    enabledProperty.setValue(Boolean.FALSE);
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
  }

  /**
   * Test that we can use
   * {@link GenericPropertySetValue#invoke(GenericPropertyImpl, Object[], boolean[])} for
   * participation.
   */
  public void test_GenericProperty_valueValidation_2() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    Property enabledProperty = panel.getPropertyByTitle("enabled");
    // add listener that on "enabled" modification modifies also "visible"
    panel.addBroadcastListener(new GenericPropertySetValue() {
      public void invoke(GenericPropertyImpl property, Object[] value, boolean[] shouldSetValue)
          throws Exception {
        if ("enabled".equals(property.getTitle())) {
          property.getJavaInfo().getPropertyByTitle("visible").setValue(value[0]);
        }
      }
    });
    // try to set value
    enabledProperty.setValue(Boolean.FALSE);
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setEnabled(false);",
        "    setVisible(false);",
        "  }",
        "}");
  }

  /**
   * Test that we can use
   * {@link JavaEventListener#setPropertyExpression(GenericPropertyImpl, String[], boolean[])} for
   * validation.
   */
  public void test_GenericProperty_expressionValidation_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    final GenericProperty enabledProperty = (GenericProperty) panel.getPropertyByTitle("enabled");
    final GenericProperty visibleProperty = (GenericProperty) panel.getPropertyByTitle("visible");
    // add listener that prevents "enabled" modification
    panel.addBroadcastListener(new JavaEventListener() {
      @Override
      public void setPropertyExpression(GenericPropertyImpl property,
          String[] source,
          Object[] value,
          boolean[] shouldSet) throws Exception {
        shouldSet[0] &= property != enabledProperty;
      }
    });
    // try to set value for "enabled", ignored
    enabledProperty.setExpression("false", Property.UNKNOWN_VALUE);
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // set value for "visible", works
    visibleProperty.setExpression("false", Property.UNKNOWN_VALUE);
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setVisible(false);",
        "  }",
        "}");
  }

  /**
   * Test that {@link JavaEventListener#propertyValueWasSet(GenericPropertyImpl)} event is fired.
   */
  public void test_GenericProperty_propertyValueWasSet() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    final GenericProperty enabledProperty = (GenericProperty) panel.getPropertyByTitle("enabled");
    final AtomicBoolean wasFired = new AtomicBoolean();
    // add listener
    panel.addBroadcastListener(new JavaEventListener() {
      @Override
      public void propertyValueWasSet(GenericPropertyImpl property) throws Exception {
        if (property == enabledProperty) {
          wasFired.set(true);
        }
      }
    });
    // set value for "enabled" to "false"
    wasFired.set(false);
    enabledProperty.setValue(false);
    assertTrue(wasFired.get());
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setEnabled(false);",
        "  }",
        "}");
    // set value for "enabled" to "true"
    wasFired.set(false);
    enabledProperty.setValue(true);
    assertTrue(wasFired.get());
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // set value for "enabled" to "false"
    wasFired.set(false);
    enabledProperty.setExpression("false", Property.UNKNOWN_VALUE);
    assertTrue(wasFired.get());
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setEnabled(false);",
        "  }",
        "}");
  }

  /**
   * Test that we can use
   * {@link JavaEventListener#invoke(GenericPropertyImpl, Expression, Object[])}.
   */
  public void test_GenericProperty_getValue_expressionListener() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setEnabled(true);",
            "  }",
            "}");
    panel.refresh();
    final GenericProperty enabledProperty = (GenericProperty) panel.getPropertyByTitle("enabled");
    // initially normal, boolean value
    assertEquals(Boolean.TRUE, enabledProperty.getValue());
    // add listener that forces "enabled" value
    panel.addBroadcastListener(new GenericPropertyGetValueEx() {
      public void invoke(GenericPropertyImpl property, Expression expression, Object[] value)
          throws Exception {
        if (property == enabledProperty) {
          value[0] = "String, not boolean";
        }
      }
    });
    // ask for value
    assertEquals("String, not boolean", enabledProperty.getValue());
  }

  /**
   * Test that we can use {@link JavaEventListener#invoke(GenericPropertyImpl, Object[])}.
   * <p>
   * Return {@link String} instead of <code>boolean</code>.
   */
  public void test_GenericProperty_getValue_unconditionaListener_String() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setEnabled(true);",
            "  }",
            "}");
    panel.refresh();
    final GenericProperty enabledProperty = (GenericProperty) panel.getPropertyByTitle("enabled");
    // initially normal, boolean value
    assertEquals(Boolean.TRUE, enabledProperty.getValue());
    // add listener that forces "enabled" value
    panel.addBroadcastListener(new GenericPropertyGetValue() {
      public void invoke(GenericPropertyImpl property, Object[] value) throws Exception {
        if (property == enabledProperty) {
          assertSame(Property.UNKNOWN_VALUE, value[0]);
          value[0] = "String, not boolean";
        }
      }
    });
    // ask for value
    assertEquals("String, not boolean", enabledProperty.getValue());
  }

  /**
   * Test that we can use {@link JavaEventListener#invoke(GenericPropertyImpl, Object[])}.
   * <p>
   * Check that <code>null</code> is considered as valid value.
   */
  public void test_GenericProperty_getValue_unconditionaListener_null() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setEnabled(true);",
            "  }",
            "}");
    panel.refresh();
    final GenericProperty enabledProperty = (GenericProperty) panel.getPropertyByTitle("enabled");
    // initially normal, boolean value
    assertEquals(Boolean.TRUE, enabledProperty.getValue());
    // add listener that forces "enabled" value
    panel.addBroadcastListener(new GenericPropertyGetValue() {
      public void invoke(GenericPropertyImpl property, Object[] value) throws Exception {
        if (property == enabledProperty) {
          assertSame(Property.UNKNOWN_VALUE, value[0]);
          value[0] = null;
        }
      }
    });
    // ask for value
    assertEquals(null, enabledProperty.getValue());
  }

  /**
   * When default value specified in description, value of getter is ignored.
   */
  public void test_defaultPropertyValue_ignoreAccessor() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public int getFoo() {",
            "    return 5;",
            "  }",
            "  public void setFoo(int foo) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <property id='setFoo(int)'>",
            "    <defaultValue value='2'/>",
            "  </property>",
            "</component>"));
    waitForAutoBuild();
    //
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    Property property = panel.getPropertyByTitle("foo");
    assertEquals(2, property.getValue());
    // set new value (ignoring that it is default value of accessor)
    property.setValue(5);
    assertEquals(5, property.getValue());
    assertEditor(
        "// filler filler filler",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    setFoo(5);",
        "  }",
        "}");
  }

  /**
   * There was bug: when we set same value as it was, {@link FieldAccessor} was used to ask default
   * value, and it returns also same value. So, we consider that default value is set and try to
   * remove source. This is not valid, because we trying to set value using "constructor" accessor.
   */
  public void test_defaultPropertyValue_noDefaultValue_forConstructor() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public int foo;",
            "  public MyPanel(int foo) {",
            "    this.foo = foo;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <constructors>",
            "    <constructor>",
            "      <parameter type='int' property='foo'/>",
            "    </constructor>",
            "  </constructors>",
            "</component>"));
    waitForAutoBuild();
    //
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    super(2);",
            "  }",
            "}");
    Property property = panel.getPropertyByTitle("foo");
    assertEquals(2, property.getValue());
    // set same value
    String expectedSource = m_lastEditor.getSource();
    property.setValue(2);
    assertEquals(2, property.getValue());
    assertEditor(expectedSource, m_lastEditor);
  }

  /**
   * No getter, no forced default value.
   */
  public void test_defaultPropertyValue_noDefaultValue() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public void setFoo(int foo) {",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertSame(Property.UNKNOWN_VALUE, panel.getPropertyByTitle("foo").getValue());
  }

  /**
   * Test for {@link GenericProperty#getType()}.
   */
  public void test_GenericProperty_getType() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    GenericProperty property = (GenericProperty) panel.getPropertyByTitle("enabled");
    assertSame(boolean.class, property.getType());
  }

  /**
   * Test for {@link GenericProperty#getType()}.
   * <p>
   * Properties created for constructor parameter also should provide type.
   */
  public void test_GenericProperty_getType_forConstructor_subProperty() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new JButton('a'));",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    //
    GenericProperty property =
        (GenericProperty) PropertyUtils.getByPath(button, "Constructor/text");
    assertNotNull(property);
    assertSame(String.class, property.getType());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // SetterAccessor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link SetterAccessor}.
   */
  public void test_setterAccessor() throws Exception {
    JavaInfo panel =
        parseSource(
            "test",
            "Test.java",
            getSourceDQ(
                "package test;",
                "import java.awt.*;",
                "import javax.swing.*;",
                "class Test {",
                "  public static void main(String args[]) {",
                "    JPanel panel = new JPanel();",
                "    panel.setEnabled(false);",
                "  }",
                "}"));
    GenericProperty property = (GenericProperty) panel.getPropertyByTitle("enabled");
    assertNotNull(property);
    assertSame(panel, property.getJavaInfo());
    // check for tooltip
    assertNotNull(property.getAdapter(PropertyTooltipProvider.class));
    assertNull(property.getAdapter(Object.class)); // we don't have such adapter, just to cover code
    // check accessor
    {
      List<ExpressionAccessor> accessors = getGenericPropertyAccessors(property);
      assertEquals(1, accessors.size());
      //
      ExpressionAccessor accessor = accessors.get(0);
      assertEquals(Boolean.TRUE, accessor.getDefaultValue(panel));
      // tooltip
      {
        assertNull(accessor.getAdapter(Object.class)); // we don't have such adapter, just to cover code
        PropertyTooltipTextProvider tooltipProvider =
            (PropertyTooltipTextProvider) accessor.getAdapter(PropertyTooltipProvider.class);
        Method getText_method =
            PropertyTooltipTextProvider.class.getDeclaredMethod(
                "getText",
                new Class[]{Property.class});
        getText_method.setAccessible(true);
        String text = (String) getText_method.invoke(tooltipProvider, new Object[]{property});
        assertThat(text).contains("this component");
      }
    }
    // initial check
    assertRelatedNodes(panel, new String[]{"new JPanel()", "panel.setEnabled(false)"});
    //
    // start edit and never finish it to avoid refresh()
    panel.startEdit();
    // use setExpression()
    {
      // set "true" without default check
      {
        property.setExpression("true", Property.UNKNOWN_VALUE);
        check_setterAccessor_true(panel, property);
      }
      // set "false" without default check
      {
        property.setExpression("false", Property.UNKNOWN_VALUE);
        check_setterAccessor_false(panel, property);
      }
      // set "true" with default check, will remove invocation
      {
        property.setExpression("true", Boolean.TRUE);
        check_setterAccessor_empty(panel, property);
        assertEquals(Boolean.TRUE, property.getValue());
      }
      // set "true" with default check, will be ignored
      {
        property.setExpression("true", Boolean.TRUE);
        check_setterAccessor_empty(panel, property);
      }
      // set "false" with default check, will add invocation
      {
        property.setExpression("false", Boolean.FALSE);
        check_setterAccessor_false(panel, property);
      }
      // again set "true" with default check, will remove invocation
      {
        property.setExpression("true", Boolean.TRUE);
        check_setterAccessor_empty(panel, property);
      }
    }
    // use setValue()
    {
      // set "false"
      {
        property.setValue(Boolean.FALSE);
        check_setterAccessor_false(panel, property);
      }
      // set "true"
      {
        property.setValue(Boolean.TRUE);
        check_setterAccessor_empty(panel, property);
      }
      // set Property.UNKNOWN_VALUE
      {
        property.setValue(Boolean.FALSE);
        check_setterAccessor_false(panel, property);
        property.setValue(Property.UNKNOWN_VALUE);
        check_setterAccessor_empty(panel, property);
      }
    }
  }

  private static void check_setterAccessor_false(JavaInfo panel, Property property)
      throws Exception {
    check_setterAccessor(panel, "panel.setEnabled(false);", new String[]{
        "new JPanel()",
        "panel.setEnabled(false)"}, property, true);
  }

  private static void check_setterAccessor_true(JavaInfo panel, Property property) throws Exception {
    check_setterAccessor(panel, "panel.setEnabled(true);", new String[]{
        "new JPanel()",
        "panel.setEnabled(true)"}, property, true);
  }

  private static void check_setterAccessor_empty(JavaInfo panel, Property property)
      throws Exception {
    check_setterAccessor(panel, null, new String[]{"new JPanel()"}, property, false);
  }

  private static void check_setterAccessor(JavaInfo panel,
      String setSource,
      String[] relatedNodes,
      Property property,
      boolean modified) throws Exception {
    AstEditor editor = panel.getEditor();
    assertAST(editor);
    //
    String[] innerLines = setSource != null ? new String[]{"    " + setSource} : null;
    String expectedSource =
        getSource3(new String[]{
            "package test;",
            "import java.awt.*;",
            "import javax.swing.*;",
            "class Test {",
            "  public static void main(String args[]) {",
            "    JPanel panel = new JPanel();"}, innerLines, new String[]{"  }", "}"});
    assertEquals(expectedSource, editor.getSource());
    assertRelatedNodes(panel, relatedNodes);
    // check property
    assertEquals(modified, property.isModified());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor accessor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for property that has {@link ConstructorAccessor} but it is not used.
   */
  public void test_constructorAccessor_1() throws Exception {
    JavaInfo button =
        parseSource(
            "test",
            "Test.java",
            getSourceDQ(
                "package test;",
                "import java.awt.*;",
                "import javax.swing.*;",
                "class Test {",
                "  public static void main(String args[]) {",
                "    JButton button = new JButton();",
                "    button.setText('abc');",
                "  }",
                "}"));
    GenericProperty property = (GenericProperty) button.getPropertyByTitle("text");
    assertNotNull(property);
    // initial checks
    assertRelatedNodes(button, new String[]{"new JButton()", "button.setText(\"abc\")"});
    assertEquals("abc", property.getValue());
    // check accessors
    {
      List<ExpressionAccessor> accessors = getGenericPropertyAccessors(property);
      assertEquals(1, accessors.size());
      assertInstanceOf(SetterAccessor.class, accessors.get(0));
      //
      assertEquals("", accessors.get(0).getDefaultValue(button));
    }
    // start edit and never finish it to avoid refresh()
    button.startEdit();
    // set value, should be changed using setText()
    {
      property.setValue("12345");
      check_constructorAccessor(button, new String[]{
          "    JButton button = new JButton();",
          "    button.setText(\"12345\");"}, new String[]{
          "new JButton()",
          "button.setText(\"12345\")"});
    }
    // remove value, setText() should be removed
    {
      property.setValue(Property.UNKNOWN_VALUE);
      check_constructorAccessor(
          button,
          new String[]{"    JButton button = new JButton();"},
          new String[]{"new JButton()"});
    }
    // set value, setText() should be added
    {
      property.setValue("12345");
      check_constructorAccessor(button, new String[]{
          "    JButton button = new JButton();",
          "    button.setText(\"12345\");"}, new String[]{
          "new JButton()",
          "button.setText(\"12345\")"});
    }
  }

  /**
   * Test for property that has active {@link ConstructorAccessor}.
   */
  public void test_constructorAccessor_2() throws Exception {
    JavaInfo button =
        parseSource(
            "test",
            "Test.java",
            getSourceDQ(
                "package test;",
                "import java.awt.*;",
                "import javax.swing.*;",
                "class Test {",
                "  public static void main(String args[]) {",
                "    JButton button = new JButton('abc');",
                "  }",
                "}"));
    GenericProperty property = (GenericProperty) button.getPropertyByTitle("text");
    assertNotNull(property);
    // check accessors
    {
      List<ExpressionAccessor> accessors = getGenericPropertyAccessors(property);
      assertEquals(2, accessors.size());
      assertInstanceOf(SetterAccessor.class, accessors.get(0));
      assertInstanceOf(ConstructorAccessor.class, accessors.get(1));
      //
      assertEquals("abc", accessors.get(0).getDefaultValue(button));
      assertSame(Property.UNKNOWN_VALUE, accessors.get(1).getDefaultValue(button));
    }
    // initial checks
    assertRelatedNodes(button, new String[]{"new JButton(\"abc\")"});
    assertEquals("abc", property.getValue());
    // start edit and never finish it to avoid refresh()
    button.startEdit();
    // simple value
    {
      property.setValue("12345");
      check_constructorAccessor(
          button,
          new String[]{"    JButton button = new JButton(\"12345\");"},
          new String[]{"new JButton(\"12345\")"});
    }
    // reset to default
    {
      property.setValue("ABC");
      property.setValue(Property.UNKNOWN_VALUE);
      check_constructorAccessor(
          button,
          new String[]{"    JButton button = new JButton((String) null);"},
          new String[]{"new JButton((String) null)"});
    }
  }

  private static void check_constructorAccessor(JavaInfo button,
      String[] lines,
      String[] relatedNodes) throws Exception {
    String[] lines_prefix =
        new String[]{
            "package test;",
            "import java.awt.*;",
            "import javax.swing.*;",
            "class Test {",
            "  public static void main(String args[]) {"};
    String[] lines_suffix = new String[]{"  }", "}"};
    //
    AstEditor editor = button.getEditor();
    assertAST(editor);
    assertEquals(getSource3(lines_prefix, lines, lines_suffix), editor.getSource());
    assertRelatedNodes(button, relatedNodes);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // StandardBeanPropertiesRule
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for setters with same name, but different types. We should have properties for both
   * setters.
   */
  public void test_StandardBeanPropertiesRule_twoSettersWithSameName() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends JButton {",
            "  public void setFoo(String text) {",
            "  }",
            "  public void setFoo(String[] text) {",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new MyButton());",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    //
    assertNotNull(button.getPropertyByTitle("foo(java.lang.String)"));
    assertNotNull(button.getPropertyByTitle("foo(java.lang.String[])"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Arbitrary values map
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link Property#getArbitraryValue(Object)}, etc.
   */
  public void test_arbitraryValuesMap() throws Exception {
    Property property = new PropertyNoValue(StringPropertyEditor.INSTANCE);
    String key = "myKey";
    // initially no any value
    assertNull(property.getArbitraryValue(key));
    // put value and get it
    property.putArbitraryValue(key, this);
    assertSame(this, property.getArbitraryValue(key));
    // remove value
    property.removeArbitraryValue(key);
    assertNull(property.getArbitraryValue(key));
  }
}
