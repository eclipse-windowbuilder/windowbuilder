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

import com.google.common.collect.ImmutableMap;

import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.association.Association;
import org.eclipse.wb.internal.core.xml.model.association.Associations;
import org.eclipse.wb.internal.core.xml.model.association.IntermediateAssociation;
import org.eclipse.wb.internal.core.xml.model.utils.ElementTarget;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;
import org.eclipse.wb.tests.designer.XML.model.description.AbstractCoreTest;

/**
 * Test for {@link IntermediateAssociation}.
 * 
 * @author scheglov_ke
 */
public class IntermediateAssociationTest extends AbstractCoreTest {
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
    Association association = Associations.name("foo");
    assertEquals("inter foo", association.toString());
  }

  public void test_toString_withAttributes() throws Exception {
    Association association =
        Associations.intermediate("foo", ImmutableMap.of("attrA", "a", "attrB", "b"));
    assertEquals("inter foo {attrA=a, attrB=b}", association.toString());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // add()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link Association#add(XmlObjectInfo, ElementTarget)}.
   */
  public void test_add() throws Exception {
    XmlObjectInfo container = parse("<Shell/>");
    // add
    XmlObjectInfo newObject = createButton();
    Association association = Associations.name("foo");
    XmlObjectUtils.add(newObject, association, container, null);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <foo>",
        "    <Button/>",
        "  </foo>",
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
   * <p>
   * Support for "{parentNS}" in name of tag.
   */
  public void test_add_parentNS() throws Exception {
    XmlObjectInfo container = parse("<z:Shell xmlns:z='http://www.eclipse.org/xwt/presentation'/>");
    // add
    XmlObjectInfo newObject = createButton();
    Association association = Associations.name("{parentNS}foo");
    XmlObjectUtils.add(newObject, association, container, null);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<z:Shell xmlns:z='http://www.eclipse.org/xwt/presentation'>",
        "  <z:foo>",
        "    <z:Button/>",
        "  </z:foo>",
        "</z:Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<z:Shell>",
        "  implicit-layout: absolute",
        "  <z:Button>");
  }

  /**
   * Test for {@link Association#add(XmlObjectInfo, ElementTarget)}.
   */
  public void test_add_attributes() throws Exception {
    XmlObjectInfo container = parse("<Shell/>");
    // add
    XmlObjectInfo newObject = createButton();
    Association association =
        Associations.intermediate("foo", ImmutableMap.of("attrA", "a", "attrB", "b"));
    XmlObjectUtils.add(newObject, association, container, null);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <foo attrA='a' attrB='b'>",
        "    <Button/>",
        "  </foo>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  implicit-layout: absolute",
        "  <Button>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // move()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link Association#move(XmlObjectInfo, ElementTarget, XmlObjectInfo, XmlObjectInfo)}.
   * <p>
   * Inner move in same container, child is already in "name" element, so we move it as is.
   */
  public void test_move_inner_alreadyInName() throws Exception {
    XmlObjectInfo container =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Button wbp:name='buttonA'/>",
            "  <Shell.layout >",
            "    <FillLayout wbp:name='layout'/>",
            "  </Shell.layout>",
            "  <Button wbp:name='buttonB'/>",
            "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <FillLayout wbp:name='layout'>",
        "  <Button wbp:name='buttonA'>",
        "  <Button wbp:name='buttonB'>");
    XmlObjectInfo layout = getObjectByName("layout");
    // move
    Association association = Associations.name("Shell.foo");
    XmlObjectUtils.move(layout, association, container, null);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button wbp:name='buttonA'/>",
        "  <Button wbp:name='buttonB'/>",
        "  <Shell.layout >",
        "    <FillLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button wbp:name='buttonA'>",
        "  <Button wbp:name='buttonB'>",
        "  <FillLayout wbp:name='layout'>");
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
    Association association = Associations.name("foo");
    XmlObjectUtils.move(button, association, composite, null);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Composite wbp:name='composite'>",
        "    <foo>",
        "      <Button wbp:name='button'/>",
        "    </foo>",
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