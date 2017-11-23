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
package org.eclipse.wb.tests.designer.XWT.model.forms;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.xwt.model.forms.FormInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link FormInfo}.
 * 
 * @author scheglov_ke
 */
public class FormTest extends XwtModelTest {
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
  // Body
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_noBodyElement() throws Exception {
    parse(
        "<!-- Forms API -->",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <Form wbp:name='form'/>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <FillLayout>",
        "  <Form wbp:name='form'>",
        "    <Form.body?>",
        "      implicit-layout: absolute");
    refresh();
    //
    FormInfo form = getObjectByName("form");
    XmlObjectInfo body = form.getChildrenXML().get(0);
    {
      CreationSupport creationSupport = body.getCreationSupport();
      assertEquals("<Form.body?>", creationSupport.toString());
      assertEquals("Form.body", creationSupport.getTitle());
      assertTrue(creationSupport.canDelete());
    }
  }

  public void test_hasBodyElement() throws Exception {
    parse(
        "<!-- Forms API -->",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <Form wbp:name='form'>",
        "    <Form.body enabled='false'/>",
        "  </Form>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <FillLayout>",
        "  <Form wbp:name='form'>",
        "    <Form.body enabled='false'>",
        "      implicit-layout: absolute");
    refresh();
    //
    FormInfo form = getObjectByName("form");
    XmlObjectInfo body = form.getChildrenXML().get(0);
    {
      CreationSupport creationSupport = body.getCreationSupport();
      assertEquals("<Form.body enabled=\"false\">", creationSupport.toString());
      assertEquals("Form.body", creationSupport.getTitle());
      assertTrue(creationSupport.canDelete());
    }
  }

  public void test_generateBodyElement() throws Exception {
    parse(
        "<!-- Forms API -->",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <Form wbp:name='form'/>",
        "</Shell>");
    refresh();
    FormInfo form = getObjectByName("form");
    //
    XmlObjectInfo body = form.getChildrenXML().get(0);
    body.getPropertyByTitle("enabled").setValue(false);
    assertXML(
        "<!-- Forms API -->",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <Form wbp:name='form'>",
        "    <Form.body enabled='false'/>",
        "  </Form>",
        "</Shell>");
  }

  /**
   * Test that property values are intercepted and displayed.
   */
  public void test_body_propertyValue() throws Exception {
    parse(
        "<!-- Forms API -->",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <Form wbp:name='form'>",
        "    <Form.body enabled='false'/>",
        "  </Form>",
        "</Shell>");
    refresh();
    FormInfo form = getObjectByName("form");
    XmlObjectInfo body = form.getChildrenXML().get(0);
    //
    Property property = body.getPropertyByTitle("enabled");
    assertEquals(Boolean.FALSE, property.getValue());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that "background" property is applied and not overridden.
   * <p>
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=310239
   */
  public void test_background() throws Exception {
    parse(
        "<!-- Forms API -->",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <Form wbp:name='form' background='yellow'/>",
        "</Shell>");
    refresh();
    FormInfo form = getObjectByName("form");
    //
    Color background = form.getControl().getBackground();
    assertEquals(new RGB(255, 255, 0), background.getRGB());
  }

  /**
   * Test that "headClient" control is rendered correctly.
   * <p>
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=310238
   */
  public void test_headClient() throws Exception {
    parse(
        "<!-- Forms API -->",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <Form>",
        "    <Form.headClient>",
        "      <Button wbp:name='button' text='New Button'/>",
        "    </Form.headClient>",
        "  </Form>",
        "</Shell>");
    refresh();
    ControlInfo button = getObjectByName("button");
    {
      Rectangle bounds = button.getBounds();
      assertThat(bounds.width).isGreaterThan(400);
      assertThat(bounds.height).isGreaterThan(20);
    }
  }
}
