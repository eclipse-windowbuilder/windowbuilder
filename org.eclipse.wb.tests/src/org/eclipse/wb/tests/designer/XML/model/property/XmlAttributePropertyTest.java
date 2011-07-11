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
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.property.XmlAttributeProperty;
import org.eclipse.wb.internal.core.xml.model.property.XmlProperty;
import org.eclipse.wb.tests.designer.XML.model.description.AbstractCoreTest;

/**
 * Test for {@link XmlAttributeProperty}.
 * 
 * @author scheglov_ke
 */
public class XmlAttributePropertyTest extends AbstractCoreTest {
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
  public void test_0() throws Exception {
    XmlObjectInfo shell = parse("<Shell/>");
    // create XMLProperty
    XmlProperty property =
        new XmlAttributeProperty(shell, "title", StringPropertyEditor.INSTANCE, "attr");
    assertEquals("title", property.getTitle());
    assertSame(PropertyCategory.NORMAL, property.getCategory());
    // object
    assertSame(shell, property.getObjectInfo());
    assertSame(shell, property.getObject());
    // no value initially
    assertEquals(null, property.getValue());
    assertFalse(property.isModified());
    // set value
    property.setValue("abc");
    assertXML("<Shell attr='abc'/>");
    assertEquals("abc", property.getValue());
    assertTrue(property.isModified());
    // remove value
    property.setValue(Property.UNKNOWN_VALUE);
    assertXML("<Shell/>");
    assertEquals(null, property.getValue());
    assertFalse(property.isModified());
  }
}