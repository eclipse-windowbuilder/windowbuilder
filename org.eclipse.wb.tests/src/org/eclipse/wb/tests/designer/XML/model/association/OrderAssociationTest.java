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
package org.eclipse.wb.tests.designer.XML.model.association;

import org.eclipse.wb.internal.core.utils.check.AssertionFailedException;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.association.Association;
import org.eclipse.wb.internal.core.xml.model.association.OrderAssociation;
import org.eclipse.wb.internal.core.xml.model.utils.ElementTarget;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;
import org.eclipse.wb.tests.designer.XML.model.description.AbstractCoreTest;

import org.apache.commons.lang.NotImplementedException;

/**
 * Test for {@link OrderAssociation}.
 *
 * @author scheglov_ke
 */
public class OrderAssociationTest extends AbstractCoreTest {
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
  public void test_toString() throws Exception {
    Association association = OrderAssociation.INSTANCE;
    assertEquals("order", association.toString());
  }

  /**
   * Test for {@link Association#add(XmlObjectInfo, ElementTarget)}.
   */
  public void test_add_notImplemented() throws Exception {
    XmlObjectInfo container = parse("<Shell/>");
    // add
    XmlObjectInfo newObject = createButton();
    Association association = OrderAssociation.INSTANCE;
    try {
      XmlObjectUtils.add(newObject, association, container, null);
      fail();
    } catch (NotImplementedException e) {
    }
  }

  /**
   * Test for {@link Association#move(XmlObjectInfo, ElementTarget, XmlObjectInfo, XmlObjectInfo)}.
   */
  public void test_reorder() throws Exception {
    XmlObjectInfo container =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Button wbp:name='buttonA'/>",
            "  <Button wbp:name='button'/>",
            "  <Button wbp:name='buttonB'/>",
            "</Shell>");
    XmlObjectInfo button = getObjectByName("button");
    // move
    Association association = OrderAssociation.INSTANCE;
    XmlObjectUtils.move(button, association, container, null);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button wbp:name='buttonA'/>",
        "  <Button wbp:name='buttonB'/>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  implicit-layout: absolute",
        "  <Button wbp:name='buttonA'>",
        "  <Button wbp:name='buttonB'>",
        "  <Button wbp:name='button'>");
  }

  /**
   * Test for {@link Association#move(XmlObjectInfo, ElementTarget, XmlObjectInfo, XmlObjectInfo)}.
   */
  public void test_reparent_notImplemented() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Composite wbp:name='composite'/>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    XmlObjectInfo composite = getObjectByName("composite");
    XmlObjectInfo button = getObjectByName("button");
    // move
    Association association = OrderAssociation.INSTANCE;
    try {
      XmlObjectUtils.move(button, association, composite, null);
      fail();
    } catch (AssertionFailedException e) {
    }
  }
}