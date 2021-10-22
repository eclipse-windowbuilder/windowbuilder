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
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.xwt.model.jface.ViewerInfo;
import org.eclipse.wb.internal.xwt.model.util.NamePropertySupport;

/**
 * Test for {@link NamePropertySupport}.
 *
 * @author scheglov_ke
 */
public class NamePropertySupportTest extends XwtModelTest {
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
  public void test_Control() throws Exception {
    XmlObjectInfo shell = parse("<Shell/>");
    // prepare "Name" property
    Property property = shell.getPropertyByTitle("Name");
    assertNotNull(property);
    assertTrue(property.getCategory().isSystem());
    // cached
    assertSame(property, shell.getPropertyByTitle("Name"));
    // no value yet
    assertFalse(property.isModified());
    assertEquals(Property.UNKNOWN_VALUE, property.getValue());
    // set value
    property.setValue("shell");
    assertXML("<Shell x:Name='shell'/>");
    assertTrue(property.isModified());
    assertEquals("shell", property.getValue());
    // remove value
    property.setValue(Property.UNKNOWN_VALUE);
    assertXML("<Shell/>");
    assertFalse(property.isModified());
    assertEquals(Property.UNKNOWN_VALUE, property.getValue());
  }

  public void test_Viewer() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <TableViewer wbp:name='viewer'/>",
        "</Shell>");
    ViewerInfo viewer = getObjectByName("viewer");
    // prepare "Name" property
    Property property = viewer.getPropertyByTitle("Name");
    assertNotNull(property);
    assertTrue(property.getCategory().isSystem());
    // cached
    assertSame(property, viewer.getPropertyByTitle("Name"));
    // no value yet
    assertFalse(property.isModified());
    assertEquals(Property.UNKNOWN_VALUE, property.getValue());
    // set value
    property.setValue("myViewer");
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <TableViewer wbp:name='viewer' x:Name='myViewer'/>",
        "</Shell>");
    assertTrue(property.isModified());
    assertEquals("myViewer", property.getValue());
    // remove value
    property.setValue(Property.UNKNOWN_VALUE);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <TableViewer wbp:name='viewer'/>",
        "</Shell>");
    assertFalse(property.isModified());
    assertEquals(Property.UNKNOWN_VALUE, property.getValue());
  }
}