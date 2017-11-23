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
import org.eclipse.wb.internal.core.xml.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.xml.model.property.accessor.FieldExpressionAccessor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link FieldExpressionAccessor}.
 * 
 * @author scheglov_ke
 */
public class FieldExpressionAccessorTest extends AbstractCoreTest {
  private XmlObjectInfo myComponent;
  private GenericPropertyImpl property;
  private ExpressionAccessor accessor;

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
        "public int test;");
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
        "public int test;");
    prepareProperty();
    // not modified
    assertFalse(accessor.isModified(myComponent));
    assertEquals(null, accessor.getExpression(myComponent));
    // no default value
    assertEquals(0, accessor.getDefaultValue(myComponent));
    assertEquals(Property.UNKNOWN_VALUE, accessor.getValue(myComponent));
  }

  public void test_getX_hasValue() throws Exception {
    prepareMyComponent(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public int test;");
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
    assertEquals(0, accessor.getDefaultValue(myComponent));
    assertEquals(5, accessor.getValue(myComponent));
  }

  public void test_getX_hasDefaultValue_noValue() throws Exception {
    prepareMyComponent(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public int test = 123;");
    prepareProperty();
    // not modified
    assertFalse(accessor.isModified(myComponent));
    assertEquals(null, accessor.getExpression(myComponent));
    // no default value
    assertEquals(123, accessor.getDefaultValue(myComponent));
    assertEquals(Property.UNKNOWN_VALUE, accessor.getValue(myComponent));
  }

  /**
   * Test for {@link FieldExpressionAccessor#setExpression(XmlObjectInfo, String)}.
   */
  public void test_setExpression() throws Exception {
    prepareMyComponent(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public int test;");
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
    accessor = property.getDescription().getAccessor();
  }
}