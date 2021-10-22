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

import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.association.Association;
import org.eclipse.wb.internal.core.xml.model.association.Associations;
import org.eclipse.wb.internal.core.xml.model.association.PropertyAssociation;
import org.eclipse.wb.internal.core.xml.model.utils.ElementTarget;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;
import org.eclipse.wb.tests.designer.XML.model.description.AbstractCoreTest;

/**
 * Test for {@link PropertyAssociation}.
 *
 * @author scheglov_ke
 */
public class PropertyAssociationTest extends AbstractCoreTest {
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
    Association association = Associations.property("foo");
    assertEquals("property foo", association.toString());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // add()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link Association#add(XmlObjectInfo, ElementTarget)}.
   */
  public void test_add_noNext() throws Exception {
    XmlObjectInfo container = parse("<Shell/>");
    // add
    XmlObjectInfo newObject = createButton();
    Association association = Associations.property("foo");
    XmlObjectUtils.add(newObject, association, container, null);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.foo>",
        "    <Button/>",
        "  </Shell.foo>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  implicit-layout: absolute",
        "  <Button>");
  }

  /**
   * Test for {@link Association#add(XmlObjectInfo, ElementTarget)}.
   */
  public void test_add_hasNext() throws Exception {
    XmlObjectInfo container =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Button wbp:name='other'/>",
            "  <Button wbp:name='reference'/>",
            "</Shell>");
    XmlObjectInfo reference = getObjectByName("reference");
    // add
    XmlObjectInfo newObject = createButton();
    Association association = Associations.property("foo");
    XmlObjectUtils.add(newObject, association, container, reference);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button wbp:name='other'/>",
        "  <Shell.foo>",
        "    <Button/>",
        "  </Shell.foo>",
        "  <Button wbp:name='reference'/>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  implicit-layout: absolute",
        "  <Button wbp:name='other'>",
        "  <Button>",
        "  <Button wbp:name='reference'>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // move()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link Association#move(XmlObjectInfo, ElementTarget, XmlObjectInfo, XmlObjectInfo)}.
   */
  public void test_move_noNext() throws Exception {
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
    Association association = Associations.property("foo");
    XmlObjectUtils.move(button, association, container, null);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button wbp:name='buttonA'/>",
        "  <Button wbp:name='buttonB'/>",
        "  <Shell.foo>",
        "    <Button wbp:name='button'/>",
        "  </Shell.foo>",
        "</Shell>");
    assertHierarchy(
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
  public void test_move_hasNext() throws Exception {
    XmlObjectInfo container =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Button wbp:name='buttonA'/>",
            "  <Button wbp:name='button'/>",
            "  <Button wbp:name='buttonB'/>",
            "</Shell>");
    XmlObjectInfo button = getObjectByName("button");
    XmlObjectInfo nextComponent = getObjectByName("buttonA");
    // move
    Association association = Associations.property("foo");
    XmlObjectUtils.move(button, association, container, nextComponent);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.foo>",
        "    <Button wbp:name='button'/>",
        "  </Shell.foo>",
        "  <Button wbp:name='buttonA'/>",
        "  <Button wbp:name='buttonB'/>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "<Shell>",
        "  implicit-layout: absolute",
        "  <Button wbp:name='button'>",
        "  <Button wbp:name='buttonA'>",
        "  <Button wbp:name='buttonB'>");
  }

  /**
   * Test for {@link Association#move(XmlObjectInfo, ElementTarget, XmlObjectInfo, XmlObjectInfo)}.
   */
  public void test_move_reparent() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Composite wbp:name='composite'/>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    XmlObjectInfo composite = getObjectByName("composite");
    XmlObjectInfo button = getObjectByName("button");
    // move
    Association association = Associations.property("foo");
    XmlObjectUtils.move(button, association, composite, null);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Composite wbp:name='composite'>",
        "    <Composite.foo>",
        "      <Button wbp:name='button'/>",
        "    </Composite.foo>",
        "  </Composite>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  implicit-layout: absolute",
        "  <Composite wbp:name='composite'>",
        "    implicit-layout: absolute",
        "    <Button wbp:name='button'>");
  }
}