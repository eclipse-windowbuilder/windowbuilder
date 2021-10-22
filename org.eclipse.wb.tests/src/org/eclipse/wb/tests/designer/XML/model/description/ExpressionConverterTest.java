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

import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.description.DescriptionPropertiesHelper;
import org.eclipse.wb.internal.core.xml.model.property.converter.BooleanConverter;
import org.eclipse.wb.internal.core.xml.model.property.converter.DoubleConverter;
import org.eclipse.wb.internal.core.xml.model.property.converter.EnumConverter;
import org.eclipse.wb.internal.core.xml.model.property.converter.ExpressionConverter;
import org.eclipse.wb.internal.core.xml.model.property.converter.IntegerConverter;
import org.eclipse.wb.internal.core.xml.model.property.converter.StringConverter;

/**
 * Tests for standard {@link ExpressionConverter}'s.
 *
 * @author scheglov_ke
 */
public class ExpressionConverterTest extends AbstractCoreTest {
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
  public void test_boolean() throws Exception {
    assertConverterEditor(boolean.class);
    //
    XmlObjectInfo object = parse("<Shell/>");
    ExpressionConverter converter = BooleanConverter.INSTANCE;
    assertEquals(null, converter.toSource(object, null));
    assertEquals("true", converter.toSource(object, true));
    assertEquals("false", converter.toSource(object, false));
  }

  public void test_Boolean() throws Exception {
    assertConverterEditor(Boolean.class);
    //
    XmlObjectInfo object = parse("<Shell/>");
    ExpressionConverter converter = BooleanConverter.INSTANCE;
    assertEquals(null, converter.toSource(object, null));
    assertEquals("true", converter.toSource(object, true));
    assertEquals("false", converter.toSource(object, false));
  }

  public void test_int() throws Exception {
    assertConverterEditor(int.class);
    //
    XmlObjectInfo object = parse("<Shell/>");
    ExpressionConverter converter = IntegerConverter.INSTANCE;
    assertEquals(null, converter.toSource(object, null));
    assertEquals("12", converter.toSource(object, 12));
    assertEquals("-12", converter.toSource(object, -12));
    assertEquals("0", converter.toSource(object, 0));
  }

  public void test_Integer() throws Exception {
    assertConverterEditor(Integer.class);
    //
    XmlObjectInfo object = parse("<Shell/>");
    ExpressionConverter converter = IntegerConverter.INSTANCE;
    assertEquals(null, converter.toSource(object, null));
    assertEquals("12", converter.toSource(object, 12));
    assertEquals("-12", converter.toSource(object, -12));
    assertEquals("0", converter.toSource(object, 0));
  }

  public void test_double() throws Exception {
    assertConverterEditor(double.class);
    //
    XmlObjectInfo object = parse("<Shell/>");
    ExpressionConverter converter = DoubleConverter.INSTANCE;
    assertEquals(null, converter.toSource(object, null));
    assertEquals("12.5", converter.toSource(object, 12.5));
    assertEquals("-12.7", converter.toSource(object, -12.7));
    assertEquals("10", converter.toSource(object, 10.0));
    assertEquals("0", converter.toSource(object, 0.0));
  }

  public void test_String() throws Exception {
    assertConverterEditor(String.class);
    //
    XmlObjectInfo object = parse("<Shell/>");
    ExpressionConverter converter = StringConverter.INSTANCE;
    // simple variants
    assertEquals(null, converter.toSource(object, null));
    assertEquals("text", converter.toSource(object, "text"));
    // no encoding here, because XML model itself does it
    assertEquals("a & b", converter.toSource(object, "a & b"));
  }

  private static enum MyEnum {
    A, B, C
  }

  public void test_Enum() throws Exception {
    assertConverterEditor(MyEnum.class);
    //
    XmlObjectInfo object = parse("<Shell/>");
    ExpressionConverter converter = EnumConverter.INSTANCE;
    assertEquals(null, converter.toSource(object, null));
    assertEquals("A", converter.toSource(object, MyEnum.A));
    assertEquals("B", converter.toSource(object, MyEnum.B));
    assertEquals("C", converter.toSource(object, MyEnum.C));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Asserts that there are {@link ExpressionConverter} and {@link PropertyEditor} registered for
   * given type, usually in plugin.xml.
   */
  private static void assertConverterEditor(Class<?> type) throws Exception {
    assertNotNull(DescriptionPropertiesHelper.getConverterForType(type));
    assertNotNull(DescriptionPropertiesHelper.getEditorForType(type));
  }
}
