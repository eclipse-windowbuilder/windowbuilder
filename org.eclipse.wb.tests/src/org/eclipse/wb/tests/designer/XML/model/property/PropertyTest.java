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
package org.eclipse.wb.tests.designer.XML.model.property;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipProvider;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.GenericPropertyGetValue;
import org.eclipse.wb.internal.core.xml.model.broadcast.GenericPropertySetExpression;
import org.eclipse.wb.internal.core.xml.model.broadcast.GenericPropertySetValue;
import org.eclipse.wb.internal.core.xml.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.xml.model.property.GenericProperty;
import org.eclipse.wb.internal.core.xml.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.xml.model.property.IExpressionPropertyEditor;
import org.eclipse.wb.internal.core.xml.model.property.accessor.ContentExpressionAccessor;
import org.eclipse.wb.internal.core.xml.model.property.accessor.EmptyExpressionAccessor;
import org.eclipse.wb.internal.core.xml.model.property.accessor.FieldExpressionAccessor;
import org.eclipse.wb.internal.core.xml.model.property.accessor.MethodExpressionAccessor;
import org.eclipse.wb.internal.core.xml.model.property.converter.StringConverter;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XML.model.description.AbstractCoreTest;

import org.eclipse.swt.SWT;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.TypeVariable;

/**
 * Test for {@link GenericProperty} and its components.
 * 
 * @author scheglov_ke
 */
public class PropertyTest extends AbstractCoreTest {
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
   * Test for {@link GenericPropertyImpl#getDescription()}.
   */
  public void test_getDescription() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    refresh();
    XmlObjectInfo button = getObjectByName("button");
    //
    GenericPropertyImpl textProperty = (GenericPropertyImpl) button.getPropertyByTitle("text");
    assertNotNull(textProperty.getDescription());
  }

  public void test_makeCopy() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    refresh();
    XmlObjectInfo button = getObjectByName("button");
    //
    GenericPropertyImpl property = (GenericPropertyImpl) button.getPropertyByTitle("text");
    GenericPropertyImpl copy = new GenericPropertyImpl(property, "otherName");
    assertEquals("text", property.getTitle());
    assertEquals("otherName", copy.getTitle());
  }

  /**
   * Test for {@link GenericPropertyImpl#getType()}.
   */
  public void test_getType() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    refresh();
    XmlObjectInfo button = getObjectByName("button");
    //
    GenericProperty textProperty = (GenericProperty) button.getPropertyByTitle("text");
    assertSame(String.class, textProperty.getType());
  }

  /**
   * Property for type which is {@link TypeVariable}.
   */
  public void test_typeVariableProperty() throws Exception {
    setFileContentSrc(
        "test/MySuper.java",
        getSourceDQ(
            "package test;",
            "import org.eclipse.swt.SWT;",
            "import org.eclipse.swt.widgets.*;",
            "public class MySuper<T> extends Composite {",
            "  public MySuper(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "  public void setTest(T value) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyComponent.java",
        getSourceDQ(
            "package test;",
            "import org.eclipse.swt.SWT;",
            "import org.eclipse.swt.widgets.*;",
            "public class MyComponent extends MySuper<Boolean> {",
            "  public MyComponent(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <t:MyComponent wbp:name='component'/>",
        "</Shell>");
    refresh();
    XmlObjectInfo component = getObjectByName("component");
    //
    Property property = component.getPropertyByTitle("test");
    assertNotNull(property);
    GenericProperty genericProperty = (GenericProperty) property;
    assertSame(Boolean.class, genericProperty.getType());
  }

  /**
   * Test for {@link GenericProperty#hasTrueTag(String)}.
   */
  public void test_hasTrueTag() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    refresh();
    XmlObjectInfo button = getObjectByName("button");
    //
    GenericPropertyImpl textProperty = (GenericPropertyImpl) button.getPropertyByTitle("text");
    assertTrue(textProperty.hasTrueTag("isText"));
    assertFalse(textProperty.hasTrueTag("noSuchTag"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PropertyTooltipProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link PropertyTooltipProvider} implementation.
   */
  public void test_getAdapter_PropertyTooltipProvider() throws Exception {
    XmlObjectInfo shell =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Button wbp:name='button'/>",
            "</Shell>");
    GenericPropertyImpl property = (GenericPropertyImpl) shell.getPropertyByTitle("enabled");
    // property has tooltip provider
    PropertyTooltipProvider tooltipProvider = property.getAdapter(PropertyTooltipProvider.class);
    String tooltip =
        (String) ReflectionUtils.invokeMethod(
            tooltipProvider,
            "getText(org.eclipse.wb.internal.core.model.property.Property)",
            property);
    assertThat(tooltip).contains("Enables the receiver if the argument").contains(
        "and disables it otherwise.");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getValue_modified() throws Exception {
    prepareMyComponent(new String[]{
        "// filler filler filler filler filler",
        "public void setTest(int value) {",
        "}"});
    ControlInfo shell =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <t:MyComponent wbp:name='component' test='123'/>",
            "</Shell>");
    shell.refresh();
    XmlObjectInfo component = getObjectByName("component");
    GenericProperty property = (GenericProperty) component.getPropertyByTitle("test");
    // has value
    assertTrue(property.isModified());
    assertEquals(123, property.getValue());
  }

  /**
   * Test for using tag "x-rawValue".
   */
  public void test_getValue_rawValue_modified() throws Exception {
    prepareMyComponent(new String[]{
        "// filler filler filler filler filler",
        "public void setTest(int value) {",
        "}"}, new String[]{
        "<property id='setTest(int)'>",
        "  <tag name='x-rawValue' value='true'/>",
        "</property>"});
    ControlInfo shell =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <t:MyComponent wbp:name='component' test='SWT.PUSH'/>",
            "</Shell>");
    shell.refresh();
    XmlObjectInfo component = getObjectByName("component");
    GenericProperty property = (GenericProperty) component.getPropertyByTitle("test");
    // has value
    assertTrue(property.isModified());
    assertEquals("SWT.PUSH", property.getValue());
  }

  /**
   * Test for using tag "x-rawValue".
   */
  public void test_getValue_rawValue_default() throws Exception {
    prepareMyComponent(new String[]{
        "// filler filler filler filler filler",
        "private String m_test = '555';",
        "public String getTest() {",
        "  return m_test;",
        "}",
        "public void setTest(String value) {",
        "}"}, new String[]{
        "<property id='setTest(java.lang.String)'>",
        "  <tag name='x-rawValue' value='true'/>",
        "</property>"});
    ControlInfo shell =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <t:MyComponent wbp:name='component'/>",
            "</Shell>");
    shell.refresh();
    XmlObjectInfo component = getObjectByName("component");
    GenericProperty property = (GenericProperty) component.getPropertyByTitle("test");
    // has value
    assertFalse(property.isModified());
    assertEquals("555", property.getValue());
  }

  public void test_getValue_notModified_defaultInDescription() throws Exception {
    prepareMyComponent(new String[]{
        "// filler filler filler filler filler",
        "public void setTest(int value) {",
        "}"}, new String[]{
        "<property id='setTest(int)'>",
        "  <defaultValue value='555'/>",
        "</property>"});
    ControlInfo shell =
        parse(
            "<Shell>",
            "  <Shell.layout>",
            "    <RowLayout/>",
            "  </Shell.layout>",
            "  <t:MyComponent wbp:name='component'/>",
            "</Shell>");
    shell.refresh();
    XmlObjectInfo component = getObjectByName("component");
    GenericProperty property = (GenericProperty) component.getPropertyByTitle("test");
    // not modified, but has value
    assertFalse(property.isModified());
    assertEquals(555, property.getValue());
  }

  public void test_getValue_notModified_defaultFromGetter() throws Exception {
    prepareMyComponent(new String[]{
        "// filler filler filler filler filler",
        "private int m_test = 555;",
        "public int getTest() {",
        "  return m_test;",
        "}",
        "public void setTest(int value) {",
        "  m_test = value;",
        "}"});
    ControlInfo shell =
        parse(
            "<Shell>",
            "  <Shell.layout>",
            "    <RowLayout/>",
            "  </Shell.layout>",
            "  <t:MyComponent wbp:name='component'/>",
            "</Shell>");
    shell.refresh();
    XmlObjectInfo component = getObjectByName("component");
    GenericProperty property = (GenericProperty) component.getPropertyByTitle("test");
    // not modified, but has value
    assertFalse(property.isModified());
    assertEquals(555, property.getValue());
  }

  public void test_getValue_notModified_defaultFromField() throws Exception {
    prepareMyComponent(new String[]{
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public int test = 555;"});
    ControlInfo shell =
        parse(
            "<Shell>",
            "  <Shell.layout>",
            "    <RowLayout/>",
            "  </Shell.layout>",
            "  <t:MyComponent wbp:name='component'/>",
            "</Shell>");
    shell.refresh();
    XmlObjectInfo component = getObjectByName("component");
    GenericProperty property = (GenericProperty) component.getPropertyByTitle("test");
    // not modified, but has value
    assertFalse(property.isModified());
    assertEquals(555, property.getValue());
  }

  /**
   * Test for using {@link MethodExpressionAccessor} in {@link GenericProperty#setValue(Object)}.
   */
  public void test_setValue_method_noGetter() throws Exception {
    prepareMyComponent(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "private int m_test;",
        "public void setTest(int value) {",
        "  m_test = value;",
        "}");
    ControlInfo shell =
        parse(
            "<Shell>",
            "  <Shell.layout>",
            "    <RowLayout/>",
            "  </Shell.layout>",
            "  <t:MyComponent wbp:name='component'/>",
            "</Shell>");
    shell.refresh();
    XmlObjectInfo component = getObjectByName("component");
    GenericProperty property = (GenericProperty) component.getPropertyByTitle("test");
    // it has object reference
    assertSame(component, property.getObject());
    // initially no value
    assertFalse(property.isModified());
    assertSame(Property.UNKNOWN_VALUE, property.getValue());
    // set value
    {
      property.setValue(123);
      assertXML(
          "<Shell>",
          "  <Shell.layout>",
          "    <RowLayout/>",
          "  </Shell.layout>",
          "  <t:MyComponent wbp:name='component' test='123'/>",
          "</Shell>");
      // value is in object
      assertEquals(123, ReflectionUtils.getFieldInt(component.getObject(), "m_test"));
      // value is in property
      assertTrue(property.isModified());
      assertEquals(123, property.getValue());
    }
    // reset to default
    {
      property.setValue(Property.UNKNOWN_VALUE);
      assertXML(
          "<Shell>",
          "  <Shell.layout>",
          "    <RowLayout/>",
          "  </Shell.layout>",
          "  <t:MyComponent wbp:name='component'/>",
          "</Shell>");
      // no value in property
      assertFalse(property.isModified());
      assertSame(Property.UNKNOWN_VALUE, property.getValue());
    }
  }

  /**
   * Test for using {@link MethodExpressionAccessor} in {@link GenericProperty#setValue(Object)}.
   */
  public void test_setValue_method_hasGetter() throws Exception {
    prepareMyComponent(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "private int m_test = 555;",
        "public int getTest() {",
        "  return m_test;",
        "}",
        "public void setTest(int value) {",
        "  m_test = value;",
        "}");
    ControlInfo shell =
        parse(
            "<Shell>",
            "  <Shell.layout>",
            "    <RowLayout/>",
            "  </Shell.layout>",
            "  <t:MyComponent wbp:name='component'/>",
            "</Shell>");
    shell.refresh();
    XmlObjectInfo component = getObjectByName("component");
    GenericProperty property = (GenericProperty) component.getPropertyByTitle("test");
    // has default value
    assertFalse(property.isModified());
    assertEquals(555, property.getValue());
    // set value
    {
      property.setValue(123);
      assertXML(
          "<Shell>",
          "  <Shell.layout>",
          "    <RowLayout/>",
          "  </Shell.layout>",
          "  <t:MyComponent wbp:name='component' test='123'/>",
          "</Shell>");
      // value is in object
      assertEquals(123, ReflectionUtils.getFieldInt(component.getObject(), "m_test"));
      // value is in property
      assertTrue(property.isModified());
      assertEquals(123, property.getValue());
    }
    // set default, so remove
    {
      property.setValue(555);
      assertXML(
          "<Shell>",
          "  <Shell.layout>",
          "    <RowLayout/>",
          "  </Shell.layout>",
          "  <t:MyComponent wbp:name='component'/>",
          "</Shell>");
      // not modified, but has default value
      assertFalse(property.isModified());
      assertEquals(555, property.getValue());
    }
  }

  /**
   * Test for using {@link FieldExpressionAccessor} in {@link GenericProperty#setValue(Object)}.
   */
  public void test_setValue_field() throws Exception {
    prepareMyComponent(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public int test = 555;");
    ControlInfo shell =
        parse(
            "<Shell>",
            "  <Shell.layout>",
            "    <RowLayout/>",
            "  </Shell.layout>",
            "  <t:MyComponent wbp:name='component'/>",
            "</Shell>");
    shell.refresh();
    XmlObjectInfo component = getObjectByName("component");
    GenericProperty property = (GenericProperty) component.getPropertyByTitle("test");
    // has default value
    assertFalse(property.isModified());
    assertEquals(555, property.getValue());
    // set value
    {
      property.setValue(123);
      assertXML(
          "<Shell>",
          "  <Shell.layout>",
          "    <RowLayout/>",
          "  </Shell.layout>",
          "  <t:MyComponent wbp:name='component' test='123'/>",
          "</Shell>");
      // value is in object
      assertEquals(123, ReflectionUtils.getFieldInt(component.getObject(), "test"));
      // value is in property
      assertTrue(property.isModified());
      assertEquals(123, property.getValue());
    }
    // set default, so remove
    {
      property.setValue(555);
      assertXML(
          "<Shell>",
          "  <Shell.layout>",
          "    <RowLayout/>",
          "  </Shell.layout>",
          "  <t:MyComponent wbp:name='component'/>",
          "</Shell>");
      // not modified, but has default value
      assertFalse(property.isModified());
      assertEquals(555, property.getValue());
    }
  }

  public void test_setValue_keepDefault_setDefault() throws Exception {
    prepareMyComponent(new String[]{
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public int test = 555;"}, new String[]{
        "<property id='test'>",
        "  <tag name='x-keepDefault' value='true'/>",
        "</property>"});
    ControlInfo shell =
        parse(
            "<Shell>",
            "  <Shell.layout>",
            "    <RowLayout/>",
            "  </Shell.layout>",
            "  <t:MyComponent wbp:name='component' test='123'/>",
            "</Shell>");
    shell.refresh();
    XmlObjectInfo component = getObjectByName("component");
    GenericProperty property = (GenericProperty) component.getPropertyByTitle("test");
    // has value
    assertTrue(property.isModified());
    assertEquals(123, property.getValue());
    // set default, but keep it
    {
      property.setValue(555);
      assertXML(
          "<Shell>",
          "  <Shell.layout>",
          "    <RowLayout/>",
          "  </Shell.layout>",
          "  <t:MyComponent wbp:name='component' test='555'/>",
          "</Shell>");
      // always modified, has default value
      assertTrue(property.isModified());
      assertEquals(555, property.getValue());
    }
  }

  public void test_setValue_keepDefault_askRemove() throws Exception {
    prepareMyComponent(new String[]{
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public int test = 555;"}, new String[]{
        "<property id='test'>",
        "  <tag name='x-keepDefault' value='true'/>",
        "</property>"});
    ControlInfo shell =
        parse(
            "<Shell>",
            "  <Shell.layout>",
            "    <RowLayout/>",
            "  </Shell.layout>",
            "  <t:MyComponent wbp:name='component' test='123'/>",
            "</Shell>");
    shell.refresh();
    XmlObjectInfo component = getObjectByName("component");
    GenericProperty property = (GenericProperty) component.getPropertyByTitle("test");
    // has value
    assertTrue(property.isModified());
    assertEquals(123, property.getValue());
    // ask to remove, but keep it
    {
      property.setValue(Property.UNKNOWN_VALUE);
      assertXML(
          "<Shell>",
          "  <Shell.layout>",
          "    <RowLayout/>",
          "  </Shell.layout>",
          "  <t:MyComponent wbp:name='component' test='555'/>",
          "</Shell>");
      // always modified, has default value
      assertTrue(property.isModified());
      assertEquals(555, property.getValue());
    }
  }

  /**
   * Test for using {@link IExpressionPropertyEditor}.
   */
  public void test_setValue_forStaticField() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    refresh();
    XmlObjectInfo button = getObjectByName("button");
    //
    Property property = button.getPropertyByTitle("alignment");
    property.setValue(SWT.LEFT);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button wbp:name='button' alignment='LEFT'/>",
        "</Shell>");
  }

  /**
   * Test for using {@link ContentExpressionAccessor}.
   */
  public void test_isContent() throws Exception {
    prepareMyComponent(
        new String[]{
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public int test = 555;"},
        new String[]{"<property-tag name='test' tag='isContent' value='true'/>"});
    ControlInfo shell =
        parse(
            "<Shell>",
            "  <Shell.layout>",
            "    <RowLayout/>",
            "  </Shell.layout>",
            "  <t:MyComponent wbp:name='component'/>",
            "</Shell>");
    shell.refresh();
    XmlObjectInfo component = getObjectByName("component");
    GenericProperty property = (GenericProperty) component.getPropertyByTitle("test");
    // has default value
    assertFalse(property.isModified());
    assertEquals(555, property.getValue());
    assertEquals(null, property.getExpression());
    // set value
    {
      property.setValue(123);
      assertXML(
          "<Shell>",
          "  <Shell.layout>",
          "    <RowLayout/>",
          "  </Shell.layout>",
          "  <t:MyComponent wbp:name='component'>123</t:MyComponent>",
          "</Shell>");
      // value is in property
      assertTrue(property.isModified());
      assertEquals("123", property.getExpression());
    }
    // set default, so remove
    {
      property.setValue(555);
      assertXML(
          "<Shell>",
          "  <Shell.layout>",
          "    <RowLayout/>",
          "  </Shell.layout>",
          "  <t:MyComponent wbp:name='component'/>",
          "</Shell>");
      // not modified, but has default value
      assertFalse(property.isModified());
      assertEquals(555, property.getValue());
      assertEquals(null, property.getExpression());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Expression
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link GenericProperty#getExpression()}.
   */
  public void test_getExpression() throws Exception {
    prepareMyComponent(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public int test;");
    ControlInfo shell =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <t:MyComponent wbp:name='component' test='123'/>",
            "</Shell>");
    shell.refresh();
    XmlObjectInfo component = getObjectByName("component");
    GenericProperty property = (GenericProperty) component.getPropertyByTitle("test");
    //
    assertEquals("123", property.getExpression());
  }

  /**
   * Test for {@link GenericProperty#setExpression(String)}.
   */
  public void test_setExpression() throws Exception {
    prepareMyComponent(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public int test = 5;");
    ControlInfo shell =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <t:MyComponent wbp:name='component'/>",
            "</Shell>");
    shell.refresh();
    XmlObjectInfo component = getObjectByName("component");
    GenericProperty property = (GenericProperty) component.getPropertyByTitle("test");
    // no value
    property.setExpression("1", Property.UNKNOWN_VALUE);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <t:MyComponent wbp:name='component' test='1'/>",
        "</Shell>");
    // non-default value 
    property.setExpression("2", Integer.valueOf(2));
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <t:MyComponent wbp:name='component' test='2'/>",
        "</Shell>");
    // default value 
    property.setExpression("5", Integer.valueOf(5));
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <t:MyComponent wbp:name='component'/>",
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for using {@link GenericPropertySetValue} for validation.
   */
  public void test_GenericProperty_valueValidation_1() throws Exception {
    XmlObjectInfo panel = parse("<Shell/>");
    Property enabledProperty = panel.getPropertyByTitle("enabled");
    // add listener that prevents "enabled" modification
    panel.addBroadcastListener(new GenericPropertySetValue() {
      public void invoke(GenericPropertyImpl property, Object[] value, boolean[] shouldSetValue)
          throws Exception {
        if ("enabled".equals(property.getTitle())) {
          shouldSetValue[0] = false;
        }
      }
    });
    // try to set value
    enabledProperty.setValue(false);
    assertXML("<Shell/>");
  }

  /**
   * Test for using {@link GenericPropertySetValue} for validation.
   */
  public void test_GenericProperty_valueValidation_2() throws Exception {
    XmlObjectInfo panel = parse("<Shell/>");
    Property enabledProperty = panel.getPropertyByTitle("enabled");
    // add listener that on "enabled" modification modifies also "visible"
    panel.addBroadcastListener(new GenericPropertySetValue() {
      public void invoke(GenericPropertyImpl property, Object[] value, boolean[] shouldSetValue)
          throws Exception {
        if ("enabled".equals(property.getTitle())) {
          property.getObject().getPropertyByTitle("modified").setValue(true);
        }
      }
    });
    // try to set value
    enabledProperty.setValue(false);
    assertXML("<Shell modified='true' enabled='false'/>");
  }

  /**
   * Test for using {@link GenericPropertySetExpression} for validation.
   */
  public void test_GenericProperty_expressionValidation_1() throws Exception {
    XmlObjectInfo panel = parse("<Shell/>");
    GenericProperty enabledProperty = (GenericProperty) panel.getPropertyByTitle("enabled");
    // add listener that prevents "enabled" modification
    panel.addBroadcastListener(new GenericPropertySetExpression() {
      public void invoke(GenericPropertyImpl property,
          String[] epxression,
          Object[] value,
          boolean[] shouldSet) throws Exception {
        if ("enabled".equals(property.getTitle())) {
          shouldSet[0] = false;
        }
      }
    });
    // try to set value
    enabledProperty.setExpression("false", false);
    assertXML("<Shell/>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GenericProperty_getPropertyValue
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for using {@link GenericPropertyGetValue}.
   * <p>
   * Return {@link String} instead of <code>boolean</code>.
   */
  public void test_GenericProperty_getPropertyValue_String() throws Exception {
    XmlObjectInfo panel = parse("<Shell/>");
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

  ////////////////////////////////////////////////////////////////////////////
  //
  // getClipboardSource()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link GenericPropertyImpl#getClipboardSource()}.
   */
  public void test_getClipboardSource_notModified() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    refresh();
    XmlObjectInfo button = getObjectByName("button");
    //
    GenericPropertyImpl textProperty = (GenericPropertyImpl) button.getPropertyByTitle("text");
    assertFalse(textProperty.isModified());
    assertSame(null, textProperty.getClipboardSource());
  }

  /**
   * Test for {@link GenericPropertyImpl#getClipboardSource()}.
   */
  public void test_getClipboardSource_noConverter() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button wbp:name='button' text='abc'/>",
        "</Shell>");
    refresh();
    XmlObjectInfo button = getObjectByName("button");
    //
    GenericPropertyDescription description =
        new GenericPropertyDescription("id", "name", null, new EmptyExpressionAccessor() {
          @Override
          public boolean isModified(XmlObjectInfo object) throws Exception {
            return true;
          }
        });
    GenericPropertyImpl property = new GenericPropertyImpl(button, description);
    assertEquals(null, property.getClipboardSource());
  }

  /**
   * Test for {@link GenericPropertyImpl#getClipboardSource()}.
   */
  public void test_getClipboardSource_useConverter() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button wbp:name='button' text='abc'/>",
        "</Shell>");
    refresh();
    XmlObjectInfo button = getObjectByName("button");
    //
    GenericPropertyImpl textProperty = (GenericPropertyImpl) button.getPropertyByTitle("text");
    assertTrue(textProperty.isModified());
    assertEquals("abc", textProperty.getClipboardSource());
  }

  /**
   * Test for {@link GenericPropertyImpl#getClipboardSource()}.
   */
  public void test_getClipboardSource_useEditor() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button wbp:name='button' alignment='CENTER'/>",
        "</Shell>");
    refresh();
    XmlObjectInfo button = getObjectByName("button");
    //
    GenericPropertyImpl alignmentProperty =
        (GenericPropertyImpl) button.getPropertyByTitle("alignment");
    assertTrue(alignmentProperty.isModified());
    assertEquals("CENTER", alignmentProperty.getClipboardSource());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GenericPropertyComposite
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for implementation of {@link Property#getComposite(Property[])}.
   */
  public void test_GenericPropertyComposite() throws Exception {
    XmlObjectInfo shell =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Button wbp:name='button_1' text='111'/>",
            "  <Button wbp:name='button_2' text='222'/>",
            "</Shell>");
    refresh();
    XmlObjectInfo button_1 = getObjectByName("button_1");
    XmlObjectInfo button_2 = getObjectByName("button_2");
    // "enabled" property
    {
      GenericProperty enabledProperty_1 = (GenericProperty) button_1.getPropertyByTitle("enabled");
      GenericProperty enabledProperty_2 = (GenericProperty) button_2.getPropertyByTitle("enabled");
      assertFalse(enabledProperty_1.isModified());
      assertFalse(enabledProperty_2.isModified());
      assertEquals(true, enabledProperty_1.getValue());
      assertEquals(true, enabledProperty_2.getValue());
      // create complex property
      Property[] properties = new Property[]{enabledProperty_1, enabledProperty_2};
      GenericProperty compositeProperty =
          (GenericProperty) enabledProperty_1.getComposite(properties);
      // check complex property
      assertEquals("enabled", compositeProperty.getTitle());
      assertFalse(compositeProperty.isModified());
      assertEquals(true, compositeProperty.getValue());
      assertEquals(null, compositeProperty.getExpression());
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
      // hasTag()
      assertTrue(compositeProperty.hasTrueTag("isText"));
      assertFalse(compositeProperty.hasTrueTag("noSuchTag"));
      // hashCode()
      assertEquals(2, compositeProperty.hashCode());
      // equals()
      {
        assertTrue(compositeProperty.equals(compositeProperty));
        assertFalse(compositeProperty.equals(this));
        //
        Property composite2 = textProperty_2.getComposite(properties);
        assertTrue(compositeProperty.equals(composite2));
      }
      // check different values
      assertSame(Property.UNKNOWN_VALUE, compositeProperty.getValue());
      assertEquals(null, compositeProperty.getExpression());
      // setValue()
      {
        compositeProperty.setValue("333");
        assertEquals("333", compositeProperty.getValue());
        assertEquals("333", compositeProperty.getExpression());
        assertXML(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Button wbp:name='button_1' text='333'/>",
            "  <Button wbp:name='button_2' text='333'/>",
            "</Shell>");
      }
      // setExpression()
      {
        String value = "444";
        compositeProperty.setExpression(StringConverter.INSTANCE.toSource(shell, value), value);
        assertEquals("444", compositeProperty.getValue());
        assertEquals("444", compositeProperty.getExpression());
        assertXML(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Button wbp:name='button_1' text='444'/>",
            "  <Button wbp:name='button_2' text='444'/>",
            "</Shell>");
      }
    }
  }

  /**
   * Test for implementation of {@link Property#getComposite(Property[])}.
   */
  public void test_GenericPropertyComposite_getType_sameType() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button wbp:name='button_1' text='111'/>",
        "  <Button wbp:name='button_2' text='222'/>",
        "</Shell>");
    refresh();
    XmlObjectInfo button_1 = getObjectByName("button_1");
    XmlObjectInfo button_2 = getObjectByName("button_2");
    // "enabled" property
    {
      GenericProperty enabledProperty_1 = (GenericProperty) button_1.getPropertyByTitle("enabled");
      GenericProperty enabledProperty_2 = (GenericProperty) button_2.getPropertyByTitle("enabled");
      // create complex property
      Property[] properties = new Property[]{enabledProperty_1, enabledProperty_2};
      GenericProperty compositeProperty =
          (GenericProperty) enabledProperty_1.getComposite(properties);
      // check
      assertSame(boolean.class, compositeProperty.getType());
    }
  }

  /**
   * Test for implementation of {@link Property#getComposite(Property[])}.
   */
  public void test_GenericPropertyComposite_getType_differentType() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button wbp:name='button' text='111'/>",
        "</Shell>");
    refresh();
    XmlObjectInfo button = getObjectByName("button");
    //
    GenericProperty property_1 = (GenericProperty) button.getPropertyByTitle("enabled");
    GenericProperty property_2 = (GenericProperty) button.getPropertyByTitle("text");
    // create complex property
    Property[] properties = new Property[]{property_1, property_2};
    GenericProperty compositeProperty = (GenericProperty) property_1.getComposite(properties);
    // check
    assertSame(null, compositeProperty.getType());
  }
}