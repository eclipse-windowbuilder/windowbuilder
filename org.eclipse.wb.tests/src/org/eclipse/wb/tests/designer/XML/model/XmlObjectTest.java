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

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectInfoDelete;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.creation.ElementCreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XML.model.description.AbstractCoreTest;

import org.eclipse.swt.widgets.Shell;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang.ArrayUtils;

import java.util.List;

/**
 * Test for {@link XmlObjectInfo}.
 * 
 * @author scheglov_ke
 */
public class XmlObjectTest extends AbstractCoreTest {
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
  public void test_getX() throws Exception {
    XmlObjectInfo shell = parse("<Shell/>");
    // no XML parent
    {
      assertSame(null, shell.getParent());
      assertSame(null, shell.getParentXML());
    }
    // has EditorContext
    {
      EditorContext context = shell.getContext();
      assertNotNull(context);
      {
        String filePath = context.getFile().getFullPath().toPortableString();
        assertEquals("/TestProject/src/test/Test.xwt", filePath);
      }
    }
    // has ComponentDescription
    {
      ComponentDescription description = shell.getDescription();
      assertNotNull(description);
      assertSame(Shell.class, description.getComponentClass());
    }
    // has CreationSupport
    {
      CreationSupport creationSupport = shell.getCreationSupport();
      assertNotNull(creationSupport);
      {
        DocumentElement element = creationSupport.getElement();
        assertNotNull(element);
        assertEquals("Shell", element.getTag());
      }
    }
  }

  /**
   * Test for {@link XmlObjectInfo#setCreationSupport(CreationSupport)}
   */
  public void test_getCreationSupport() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    XmlObjectInfo button = getObjectByName("button");
    //
    CreationSupport oldCreationSupport = button.getCreationSupport();
    CreationSupport newCreationSupport =
        new ElementCreationSupport(oldCreationSupport.getElement());
    button.setCreationSupport(newCreationSupport);
    assertSame(newCreationSupport, button.getCreationSupport());
  }

  /**
   * Test for {@link XmlObjectInfo#getChildByObject(Object)}
   */
  public void test_getChildByObject() throws Exception {
    XmlObjectInfo shell =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Button wbp:name='button'/>",
            "</Shell>");
    refresh();
    XmlObjectInfo button = getObjectByName("button");
    //
    assertSame(shell, shell.getChildByObject(shell.getObject()));
    assertSame(button, shell.getChildByObject(button.getObject()));
    assertSame(null, shell.getChildByObject(this));
    // we don't need to search "null" objects
    assertSame(null, shell.getChildByObject(null));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Attributes raw
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link XmlObjectInfo#getElement()}.
   */
  public void test_getElement() throws Exception {
    XmlObjectInfo shell = parse("<Shell/>");
    assertSame(shell.getCreationSupport().getElement(), shell.getElement());
  }

  /**
   * Test for {@link XmlObjectInfo#getAttribute(String)}.
   */
  public void test_getAttribute() throws Exception {
    XmlObjectInfo shell = parse("<Shell foo='bar'/>");
    assertEquals("bar", shell.getAttribute("foo"));
    assertEquals(null, shell.getAttribute("no-such-attribute"));
  }

  /**
   * Test for {@link XmlObjectInfo#setAttribute(String, String)}.
   */
  public void test_setAttribute() throws Exception {
    XmlObjectInfo shell = parse("<Shell/>");
    shell.setAttribute("foo", "bar");
    assertXML("<Shell foo='bar'/>");
  }

  /**
   * Test for {@link XmlObjectInfo#removeAttribute(String)}.
   */
  public void test_removeAttribute() throws Exception {
    XmlObjectInfo shell = parse("<Shell foo='bar'/>");
    shell.removeAttribute("foo");
    assertXML("<Shell/>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Hierarchy
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link XmlObjectInfo#getParentXML()}
   */
  public void test_getParentXML() throws Exception {
    XmlObjectInfo shell =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Composite wbp:name='composite'>",
            "    <Button wbp:name='button'/>",
            "  </Composite>",
            "</Shell>");
    XmlObjectInfo composite = getObjectByName("composite");
    XmlObjectInfo button = getObjectByName("button");
    //
    assertSame(null, shell.getParentXML());
    assertSame(shell, composite.getParentXML());
    assertSame(composite, button.getParentXML());
  }

  /**
   * Test for {@link XmlObjectInfo#getRootXML()}
   */
  public void test_getRootXML() throws Exception {
    XmlObjectInfo shell =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Composite wbp:name='composite'>",
            "    <Button wbp:name='button'/>",
            "  </Composite>",
            "</Shell>");
    XmlObjectInfo composite = getObjectByName("composite");
    XmlObjectInfo button = getObjectByName("button");
    //
    assertSame(shell, shell.getRootXML());
    assertSame(shell, composite.getRootXML());
    assertSame(shell, button.getRootXML());
  }

  /**
   * Test for {@link XmlObjectInfo#getChildrenXML()}
   */
  public void test_getChildrenXML() throws Exception {
    XmlObjectInfo shell =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Composite wbp:name='composite'>",
            "    <Button wbp:name='button_1'/>",
            "    <Button wbp:name='button_2'/>",
            "  </Composite>",
            "</Shell>");
    XmlObjectInfo composite = getObjectByName("composite");
    XmlObjectInfo button_2 = getObjectByName("button_1");
    XmlObjectInfo button_1 = getObjectByName("button_2");
    // implicit layout + "composite"
    {
      List<XmlObjectInfo> children = shell.getChildrenXML();
      assertThat(children).hasSize(1 + 1).contains(composite);
    }
    // implicit layout + "button_1" + "button_2"
    {
      List<XmlObjectInfo> children = composite.getChildrenXML();
      assertThat(children).hasSize(1 + 2).contains(button_1, button_2);
    }
    // no children for "button_1"
    {
      List<XmlObjectInfo> children = button_1.getChildrenXML();
      assertThat(children).isEmpty();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link XmlObjectInfo#getProperties()}.
   */
  public void test_getProperties() throws Exception {
    XmlObjectInfo shell = parse("<Shell/>");
    Property[] properties = shell.getProperties();
    String[] titles = PropertyUtils.getTitles(properties);
    assertThat(titles).contains("text", "enabled");
  }

  /**
   * Test for {@link XmlObjectInfo#getPropertyByTitle(String)}.
   */
  public void test_getPropertyByTitle() throws Exception {
    XmlObjectInfo shell = parse("<Shell/>");
    // "text"
    {
      Property property = shell.getPropertyByTitle("text");
      assertNotNull(property);
      assertEquals("text", property.getTitle());
    }
    // no such property
    {
      Property property = shell.getPropertyByTitle("noSuchProperty");
      assertNull(property);
    }
  }

  /**
   * Test that value of property is applied into XML.
   */
  public void test_setPropertyValue() throws Exception {
    XmlObjectInfo shell = parse("<Shell text='first'/>");
    shell.refresh();
    Property property = shell.getPropertyByTitle("text");
    // initial value
    assertEquals("first", property.getValue());
    assertEquals("first", shell.getAttributeValue("text"));
    // set new value
    property.setValue("second");
    assertEquals("second", property.getValue());
    assertEquals("second", shell.getAttributeValue("text"));
    assertXML("<Shell text='second'/>");
  }

  /**
   * Test that attribute value applied into {@link XmlObjectInfo} and can be accessed using
   * {@link XmlObjectInfo#getAttributeValue(String)}.
   */
  public void test_getAttributeValue() throws Exception {
    XmlObjectInfo shell =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Shell text='foo'/>");
    shell.refresh();
    //
    assertEquals("foo", shell.getAttributeValue("text"));
    assertSame(Property.UNKNOWN_VALUE, shell.getAttributeValue("noSuchAttribute"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link XmlObjectInfo#toString()}.
   */
  public void test_toString() throws Exception {
    XmlObjectInfo shell =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Shell text='foo'>",
            "  <Button text='foo' wbp:name='button'/>",
            "</Shell>");
    // "shell"
    assertEquals(getSourceDQ("<Shell text='foo'>"), shell.toString());
    // "button"
    {
      XmlObjectInfo button = getObjectByName("button");
      assertEquals(getSourceDQ("<Button text='foo' wbp:name='button'>"), button.toString());
    }
  }

  /**
   * Test for {@link XmlObjectInfo#getPresentation()}.
   */
  public void test_presentation() throws Exception {
    XmlObjectInfo shell = parse("<Shell text='My Shell'/>");
    shell.refresh();
    //
    IObjectPresentation presentation = shell.getPresentation();
    {
      assertNotNull(presentation.getIcon());
      assertSame(shell.getDescription().getIcon(), presentation.getIcon());
    }
    assertEquals("Shell", presentation.getText());
    assertThat(presentation.getChildrenTree()).isEmpty();
    assertThat(presentation.getChildrenGraphical()).isEmpty();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link XmlObjectInfo#delete()}.
   * <p>
   * Delete single {@link XmlObjectInfo}.
   */
  public void test_delete_normalComponent() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  implicit-layout: absolute",
        "  <Button wbp:name='button'>");
    // delete "button"
    XmlObjectInfo button = getObjectByName("button");
    assertTrue(button.canDelete());
    button.delete();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell/>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  implicit-layout: absolute");
  }

  /**
   * Test for {@link XmlObjectInfo#delete()}.
   * <p>
   * Delete root {@link XmlObjectInfo}, clear it instead.
   */
  public void test_delete_rootComponent() throws Exception {
    XmlObjectInfo shell =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Shell text='My text'>",
            "  <Button wbp:name='button'/>",
            "</Shell>");
    // delete "shell"
    assertTrue(shell.canDelete());
    shell.delete();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell text='My text'/>");
  }

  /**
   * Test for {@link XmlObjectInfo#delete()}.
   * <p>
   * Delete {@link XmlObjectInfo} and its children.
   */
  public void test_delete_withChildren() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Composite wbp:name='composite'>",
        "    <Button/>",
        "  </Composite>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "<Shell>",
        "  implicit-layout: absolute",
        "  <Composite wbp:name='composite'>",
        "    implicit-layout: absolute",
        "    <Button>");
    // delete "composite"
    XmlObjectInfo composite = getObjectByName("composite");
    assertTrue(composite.canDelete());
    composite.delete();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell/>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  implicit-layout: absolute");
  }

  /**
   * Test for {@link XmlObjectInfo#delete()}.
   * <p>
   * Test for {@link XMLObject_delete} broadcast.
   */
  public void test_delete_withBroadcast() throws Exception {
    final XmlObjectInfo shell =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Button wbp:name='button'/>",
            "</Shell>");
    final XmlObjectInfo button = getObjectByName("button");
    // delete "button"
    final StringBuilder log = new StringBuilder();
    m_lastObject.addBroadcastListener(new ObjectInfoDelete() {
      @Override
      public void before(ObjectInfo parent, ObjectInfo child) throws Exception {
        super.before(parent, child);
        assertSame(shell, parent);
        assertSame(button, child);
        assertTrue(child.isDeleting());
        assertFalse(child.isDeleted());
        log.append("deleteBefore\n");
      }

      @Override
      public void after(ObjectInfo parent, ObjectInfo child) throws Exception {
        super.after(parent, child);
        assertSame(shell, parent);
        assertSame(button, child);
        assertTrue(child.isDeleting());
        assertTrue(child.isDeleted());
        log.append("deleteAfter\n");
      }
    });
    button.delete();
    assertEquals("deleteBefore\n" + "deleteAfter\n", log.toString());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "refresh_afterCreate" script
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that during "refresh" the script "refresh_afterCreate" is executed.
   */
  public void test_refresh_afterCreate_script() throws Exception {
    prepareMyComponent(ArrayUtils.EMPTY_STRING_ARRAY, new String[]{
        "  <parameters>",
        "    <parameter name='refresh_afterCreate'>object.setEnabled(false)</parameter>",
        "  </parameters>",});
    // parse
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <t:MyComponent wbp:name='myComponent'/>",
        "</Shell>");
    refresh();
    //
    ControlInfo myComponent = getObjectByName("myComponent");
    assertFalse(myComponent.getControl().isEnabled());
  }
}