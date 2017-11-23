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
package org.eclipse.wb.tests.designer.XML.model.description;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipProvider;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.xml.model.property.accessor.MethodExpressionAccessor;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

/**
 * Test for {@link MethodExpressionAccessor}.
 * 
 * @author scheglov_ke
 */
public class MethodExpressionAccessorTest extends AbstractCoreTest {
  private XmlObjectInfo myComponent;
  private GenericPropertyImpl property;
  private MethodExpressionAccessor accessor;

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
  public void test_getAdapter() throws Exception {
    prepareMyComponent(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "/**",
        "* My JavaDoc text.",
        "*/",
        "public void setTest(int value) {",
        "}");
    prepareProperty();
    // ignore arbitrary adapter
    assertNull(accessor.getAdapter(null));
    // get provider
    PropertyTooltipProvider tooltipProvider = accessor.getAdapter(PropertyTooltipProvider.class);
    assertNotNull(tooltipProvider);
    String tooltip =
        (String) ReflectionUtils.invokeMethod(
            tooltipProvider,
            "getText(org.eclipse.wb.internal.core.model.property.Property)",
            property);
    assertThat(tooltip).isEqualTo("My JavaDoc text.");
  }

  public void test_getX_noValue() throws Exception {
    prepareMyComponent(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public void setTest(int value) {",
        "}");
    prepareProperty();
    // not modified
    assertFalse(accessor.isModified(myComponent));
    assertEquals(null, accessor.getExpression(myComponent));
    // no default value
    assertSame(Property.UNKNOWN_VALUE, accessor.getDefaultValue(myComponent));
    assertSame(Property.UNKNOWN_VALUE, accessor.getValue(myComponent));
  }

  public void test_getX_hasValue() throws Exception {
    prepareMyComponent(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public void setTest(int value) {",
        "}");
    prepareProperty(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <t:MyComponent wbp:name='myComponent' test='5'/>",
        "</Shell>");
    // modified
    assertTrue(accessor.isModified(myComponent));
    assertEquals("5", accessor.getExpression(myComponent));
    // no default value
    assertSame(Property.UNKNOWN_VALUE, accessor.getDefaultValue(myComponent));
    assertEquals(5, accessor.getValue(myComponent));
  }

  public void test_getX_hasDefaultValue_noValue() throws Exception {
    prepareMyComponent(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public int getTest() {",
        "  return 123;",
        "}",
        "public void setTest(int value) {",
        "}");
    prepareProperty();
    // not modified
    assertFalse(accessor.isModified(myComponent));
    assertEquals(null, accessor.getExpression(myComponent));
    // no default value
    assertEquals(123, accessor.getDefaultValue(myComponent));
    assertEquals(Property.UNKNOWN_VALUE, accessor.getValue(myComponent));
  }

  public void test_getX_propertyElement() throws Exception {
    prepareMyComponent(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "private String[] m_items;",
        "public void setTest(String[] items) {",
        "  m_items = items;",
        "}");
    prepareProperty(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell xmlns:p1='clr-namespace:java.lang'>",
        "  <t:MyComponent wbp:name='myComponent'>",
        "    <t:MyComponent.test>",
        "      <p1:String>some</p1:String>",
        "      <p1:String>items</p1:String>",
        "    </t:MyComponent.test>",
        "  </t:MyComponent>",
        "</Shell>");
    // modified - has element
    assertTrue(accessor.isModified(myComponent));
    // ...but no String expression
    assertEquals(null, accessor.getExpression(myComponent));
  }

  /**
   * Test for {@link MethodExpressionAccessor#setExpression(XmlObjectInfo, String)}.
   */
  public void test_setExpression() throws Exception {
    prepareMyComponent(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public void setTest(int value) {",
        "}");
    prepareProperty();
    // not modified
    assertFalse(accessor.isModified(myComponent));
    assertEquals(null, accessor.getExpression(myComponent));
    // set expression
    accessor.setExpression(myComponent, "123");
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <t:MyComponent wbp:name='myComponent' test='123'/>",
        "</Shell>");
    // modified
    assertTrue(accessor.isModified(myComponent));
    assertEquals("123", accessor.getExpression(myComponent));
    assertEquals(123, accessor.getValue(myComponent));
  }

  public void test_getSetter_getGetter() throws Exception {
    prepareMyComponent(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public int getTest() {",
        "  return 123;",
        "}",
        "public void setTest(int value) {",
        "}");
    prepareProperty();
    // has setter
    {
      Method setter = accessor.getSetter();
      assertNotNull(setter);
      assertEquals("setTest", setter.getName());
    }
    // has getter
    {
      Method getter = accessor.getGetter();
      assertNotNull(getter);
      assertEquals("getTest", getter.getName());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private void prepareProperty() throws Exception {
    prepareProperty(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <t:MyComponent wbp:name='myComponent'/>",
        "</Shell>");
  }

  private void prepareProperty(String... lines) throws Exception {
    parse(lines);
    refresh();
    // prepare
    myComponent = getObjectByName("myComponent");
    property = (GenericPropertyImpl) myComponent.getPropertyByTitle("test");
    assertNotNull(property);
    accessor = (MethodExpressionAccessor) property.getDescription().getAccessor();
  }
}