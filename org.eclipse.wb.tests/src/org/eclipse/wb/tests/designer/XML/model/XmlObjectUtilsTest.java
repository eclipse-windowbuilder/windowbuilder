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
package org.eclipse.wb.tests.designer.XML.model;

import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.association.Association;
import org.eclipse.wb.internal.core.xml.model.association.Associations;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.creation.ElementCreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.XML.model.description.AbstractCoreTest;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang.ArrayUtils;

import java.util.Map;

/**
 * Test for {@link XmlObjectUtils}.
 * 
 * @author scheglov_ke
 */
public class XmlObjectUtilsTest extends AbstractCoreTest {
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
  // Test
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link XmlObjectUtils#getParameter(XmlObjectInfo, String)} and
   * {@link XmlObjectUtils#setParameter(XmlObjectInfo, String, String)}.
   */
  public void test_getSetParameter() throws Exception {
    XmlObjectInfo shell = parse("<Shell/>");
    String name = "testParameter";
    // initially no such parameter
    assertSame(null, XmlObjectUtils.getParameter(shell, name));
    // set parameter
    String value = "foo";
    XmlObjectUtils.setParameter(shell, name, value);
    // has value now
    assertSame(value, XmlObjectUtils.getParameter(shell, name));
    // remove value
    XmlObjectUtils.setParameter(shell, name, null);
    assertSame(null, XmlObjectUtils.getParameter(shell, name));
  }

  /**
   * Test for {@link XmlObjectUtils#hasTrueParameter(XmlObjectInfo, String)}.
   */
  public void test_hasTrueParameter() throws Exception {
    XmlObjectInfo shell = parse("<Shell/>");
    String name = "testParameter";
    // no such parameter
    assertFalse(XmlObjectUtils.hasTrueParameter(shell, name));
    // "false"
    XmlObjectUtils.setParameter(shell, name, "false");
    assertFalse(XmlObjectUtils.hasTrueParameter(shell, name));
    // not "true"
    XmlObjectUtils.setParameter(shell, name, "someString");
    assertFalse(XmlObjectUtils.hasTrueParameter(shell, name));
    // "true"
    XmlObjectUtils.setParameter(shell, name, "true");
    assertTrue(XmlObjectUtils.hasTrueParameter(shell, name));
  }

  public void test_getParameters() throws Exception {
    prepareMyComponent(ArrayUtils.EMPTY_STRING_ARRAY, new String[]{
        "  <parameters>",
        "    <parameter name='test.parameter.1'>value_1</parameter>",
        "    <parameter name='test.parameter.2'>1000</parameter>",
        "  </parameters>"});
    // parse
    XmlObjectInfo panel = parse("<t:MyComponent/>");
    // check single parameters
    {
      assertThat(XmlObjectUtils.getParameter(panel, "test.parameter.1")).isEqualTo("value_1");
      assertThat(XmlObjectUtils.getParameter(panel, "test.parameter.2")).isEqualTo("1000");
      assertThat(XmlObjectUtils.getParameter(panel, "test.parameter.3")).isNull();
    }
    // check parameters map
    {
      Map<String, String> parameters = XmlObjectUtils.getParameters(panel);
      assertThat(parameters.get("test.parameter.1")).isEqualTo("value_1");
      assertThat(parameters.get("test.parameter.2")).isEqualTo("1000");
      assertThat(parameters.get("test.parameter.3")).isNull();
    }
    // set new parameter
    XmlObjectUtils.setParameter(panel, "test.parameter.3", "true");
    // check parameters map
    {
      // check mapped values
      Map<String, String> parameters = XmlObjectUtils.getParameters(panel);
      assertThat(parameters.get("test.parameter.1")).isEqualTo("value_1");
      assertThat(parameters.get("test.parameter.2")).isEqualTo("1000");
      assertThat(parameters.get("test.parameter.3")).isEqualTo("true");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Script
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link XmlObjectUtils#executeScriptParameter(XmlObjectInfo, String)}.
   */
  public void test_executeScriptParameter() throws Exception {
    prepareMyComponent(new String[]{}, new String[]{
        "// filler filler filler filler filler",
        "<parameters>",
        "  <parameter name='myScript'>return 5;</parameter>",
        "</parameters>"});
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <t:MyComponent wbp:name='myComponent'/>",
        "</Shell>");
    CompositeInfo myComponent = getObjectByName("myComponent");
    // existing script
    assertEquals(5, XmlObjectUtils.executeScriptParameter(myComponent, "myScript"));
    // no script
    assertEquals(null, XmlObjectUtils.executeScriptParameter(myComponent, "moSuchScript"));
  }

  /**
   * Test for {@link XmlObjectUtils#executeScript(XmlObjectInfo, String)}.
   */
  public void test_executeScript() throws Exception {
    prepareMyComponent(new String[]{
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public String myField = '_myField';"}, new String[]{});
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <t:MyComponent wbp:name='myComponent' enabled='false'/>",
        "</Shell>");
    refresh();
    CompositeInfo myComponent = getObjectByName("myComponent");
    // existing script
    assertEquals("false_myField", XmlObjectUtils.executeScript(
        myComponent,
        "return model.getAttributeValue('enabled') + object.myField"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // createObject()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link XmlObjectUtils#createObject(EditorContext, String, CreationSupport)}.
   */
  public void test_createObject_stringClass() throws Exception {
    parse("<Shell/>");
    // create SWT Button
    XmlObjectInfo newObject =
        XmlObjectUtils.createObject(
            m_lastContext,
            "org.eclipse.swt.widgets.Button",
            new ElementCreationSupport());
    assertSame(m_lastContext, newObject.getContext());
    {
      ComponentDescription description = newObject.getDescription();
      assertEquals("org.eclipse.swt.widgets.Button", description.getComponentClass().getName());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // add()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link XmlObjectUtils#add(XmlObjectInfo, Association, XmlObjectInfo, XmlObjectInfo)}.
   */
  public void test_add_noNext() throws Exception {
    XmlObjectInfo container = parse("<Shell/>");
    // add
    XmlObjectInfo newObject = createButtonWithText();
    XmlObjectUtils.add(newObject, Associations.direct(), container, null);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button text='New Button'/>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  implicit-layout: absolute",
        "  <Button text='New Button'>");
  }

  /**
   * Test for {@link XmlObjectUtils#add(XmlObjectInfo, Association, XmlObjectInfo, XmlObjectInfo)}.
   */
  public void test_add_hasNext() throws Exception {
    XmlObjectInfo container =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Button wbp:name='reference'/>",
            "</Shell>");
    XmlObjectInfo reference = getObjectByName("reference");
    // add
    XmlObjectInfo newObject = createButtonWithText();
    XmlObjectUtils.add(newObject, Associations.direct(), container, reference);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button text='New Button'/>",
        "  <Button wbp:name='reference'/>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  implicit-layout: absolute",
        "  <Button text='New Button'>",
        "  <Button wbp:name='reference'>");
  }

  /**
   * Test for {@link XmlObjectUtils#addFirst(XmlObjectInfo, Association, XmlObjectInfo)}.
   */
  public void test_addFirst() throws Exception {
    XmlObjectInfo container =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Button wbp:name='reference'/>",
            "</Shell>");
    // add
    XmlObjectInfo newObject = createButtonWithText();
    XmlObjectUtils.addFirst(newObject, Associations.direct(), container);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button text='New Button'/>",
        "  <Button wbp:name='reference'/>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button text='New Button'>",
        "  implicit-layout: absolute",
        "  <Button wbp:name='reference'>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // move()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link XmlObjectUtils#move(XmlObjectInfo, Association, XmlObjectInfo, XmlObjectInfo)}.
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
    XmlObjectUtils.move(button, Associations.direct(), container, null);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button wbp:name='buttonA'/>",
        "  <Button wbp:name='buttonB'/>",
        "  <Button wbp:name='button'/>",
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
   * Test for {@link XmlObjectUtils#move(XmlObjectInfo, Association, XmlObjectInfo, XmlObjectInfo)}.
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
    XmlObjectUtils.move(button, Associations.direct(), container, nextComponent);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button wbp:name='button'/>",
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
   * Test for {@link XmlObjectUtils#move(XmlObjectInfo, Association, XmlObjectInfo, XmlObjectInfo)}.
   * <p>
   * Test for using without {@link Association}, allowed during reorder.
   */
  public void test_move_noAssociation() throws Exception {
    XmlObjectInfo container =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Button wbp:name='buttonA'/>",
            "  <Button wbp:name='button'/>",
            "  <Button wbp:name='buttonB'/>",
            "</Shell>");
    XmlObjectInfo component = getObjectByName("button");
    XmlObjectInfo nextComponent = getObjectByName("buttonA");
    // move
    XmlObjectUtils.move(component, null, container, nextComponent);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button wbp:name='button'/>",
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
   * Test for {@link XmlObjectUtils#move(XmlObjectInfo, Association, XmlObjectInfo, XmlObjectInfo)}.
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
    XmlObjectUtils.move(button, Associations.direct(), composite, null);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Composite wbp:name='composite'>",
        "    <Button wbp:name='button'/>",
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

  /**
   * Test for {@link XmlObjectUtils#move(XmlObjectInfo, Association, XmlObjectInfo, XmlObjectInfo)}.
   * <p>
   * When we move out some {@link DocumentElement} we should remove intermediate elements between
   * moved {@link DocumentElement} and parent {@link DocumentElement}.
   */
  public void test_move_whenInSubElement_ofOldParent_() throws Exception {
    XmlObjectInfo shell =
        parse(
            "<Shell>",
            "  <TabFolder>",
            "    <TabItem>",
            "      <TabItem.control>",
            "        <Button wbp:name='button'/>",
            "      </TabItem.control>",
            "    </TabItem>",
            "  </TabFolder>",
            "</Shell>");
    XmlObjectInfo button = getObjectByName("button");
    // move
    XmlObjectUtils.move(button, Associations.direct(), shell, null);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <TabFolder>",
        "    <TabItem/>",
        "  </TabFolder>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "<Shell>",
        "  implicit-layout: absolute",
        "  <TabFolder>",
        "    <TabItem>",
        "  <Button wbp:name='button'>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getTagForClass()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link XmlObjectUtils#getTagForClass(XmlObjectInfo, Class)}.
   */
  public void test_getTagForClass() throws Exception {
    XmlObjectInfo shell = parse("<Shell/>");
    // no namespace
    {
      assertEquals(
          "Button",
          XmlObjectUtils.getTagForClass(
              shell,
              m_lastLoader.loadClass("org.eclipse.swt.widgets.Button")));
      assertXML("<Shell/>");
    }
    // has namespace
    {
      assertEquals(
          "p1:String",
          XmlObjectUtils.getTagForClass(shell, m_lastLoader.loadClass("java.lang.String")));
      assertXML("<Shell xmlns:p1='clr-namespace:java.lang'/>");
    }
  }
}