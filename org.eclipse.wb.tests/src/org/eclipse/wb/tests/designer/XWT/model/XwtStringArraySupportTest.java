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
package org.eclipse.wb.tests.designer.XWT.model;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.xwt.model.util.XwtStringArraySupport;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link XwtStringArraySupport}.
 * 
 * @author scheglov_ke
 */
public class XwtStringArraySupportTest extends XwtModelTest {
  private XmlObjectInfo myComponent;
  private GenericPropertyImpl property;

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
  public void test_setNew() throws Exception {
    prepareMyComponent();
    prepareProperty(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <t:MyComponent wbp:name='myComponent'/>",
        "</Shell>");
    assertFalse(property.isModified());
    // set value
    String[] value = new String[]{"aaa", "bbb", "ccc"};
    property.setValue(value);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell xmlns:p1='clr-namespace:java.lang'>",
        "  <t:MyComponent wbp:name='myComponent'>",
        "    <t:MyComponent.test>",
        "      <p1:String>aaa</p1:String>",
        "      <p1:String>bbb</p1:String>",
        "      <p1:String>ccc</p1:String>",
        "    </t:MyComponent.test>",
        "  </t:MyComponent>",
        "</Shell>");
    // modified
    assertTrue(property.isModified());
    {
      Object[] actual =
          (Object[]) ReflectionUtils.getFieldObject(myComponent.getObject(), "m_items");
      assertThat(actual).isEqualTo(value);
    }
    {
      Object[] actual = (Object[]) property.getValue();
      assertThat(actual).isEqualTo(value);
    }
  }

  public void test_updateExisting() throws Exception {
    prepareMyComponent();
    prepareProperty(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell xmlns:p1='clr-namespace:java.lang'>",
        "  <t:MyComponent wbp:name='myComponent'>",
        "    <t:MyComponent.test>",
        "      <p1:String>old</p1:String>",
        "      <p1:String>items</p1:String>",
        "    </t:MyComponent.test>",
        "  </t:MyComponent>",
        "</Shell>");
    assertTrue(property.isModified());
    // set value
    String[] value = new String[]{"aaa", "bbb", "ccc"};
    property.setValue(value);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell xmlns:p1='clr-namespace:java.lang'>",
        "  <t:MyComponent wbp:name='myComponent'>",
        "    <t:MyComponent.test>",
        "      <p1:String>aaa</p1:String>",
        "      <p1:String>bbb</p1:String>",
        "      <p1:String>ccc</p1:String>",
        "    </t:MyComponent.test>",
        "  </t:MyComponent>",
        "</Shell>");
    // modified
    assertTrue(property.isModified());
    {
      Object[] actual =
          (Object[]) ReflectionUtils.getFieldObject(myComponent.getObject(), "m_items");
      assertThat(actual).isEqualTo(value);
    }
    {
      Object[] actual = (Object[]) property.getValue();
      assertThat(actual).isEqualTo(value);
    }
  }

  public void test_removeExisting() throws Exception {
    prepareMyComponent();
    prepareProperty(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell xmlns:p1='clr-namespace:java.lang'>",
        "  <t:MyComponent wbp:name='myComponent'>",
        "    <t:MyComponent.test>",
        "      <p1:String>old</p1:String>",
        "      <p1:String>items</p1:String>",
        "    </t:MyComponent.test>",
        "  </t:MyComponent>",
        "</Shell>");
    assertTrue(property.isModified());
    // set value
    property.setValue(Property.UNKNOWN_VALUE);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell xmlns:p1='clr-namespace:java.lang'>",
        "  <t:MyComponent wbp:name='myComponent'/>",
        "</Shell>");
    // modified
    assertFalse(property.isModified());
    {
      Object actual = ReflectionUtils.getFieldObject(myComponent.getObject(), "m_items");
      assertThat(actual).isNull();
    }
    {
      Object actual = property.getValue();
      assertThat(actual).isSameAs(Property.UNKNOWN_VALUE);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private void prepareMyComponent() throws Exception {
    prepareMyComponent(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "private String[] m_items;",
        "public void setTest(String[] items) {",
        "  m_items = items;",
        "}");
  }

  private void prepareProperty(String... lines) throws Exception {
    parse(lines);
    refresh();
    // prepare
    myComponent = getObjectByName("myComponent");
    property = (GenericPropertyImpl) myComponent.getPropertyByTitle("test");
    assertNotNull(property);
  }
}