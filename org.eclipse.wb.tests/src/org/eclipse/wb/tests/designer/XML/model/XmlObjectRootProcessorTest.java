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

import org.eclipse.wb.internal.core.model.util.ObjectsLabelProvider;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.XmlObjectRootProcessor;
import org.eclipse.wb.tests.designer.XML.model.description.AbstractCoreTest;

import org.apache.commons.lang.ArrayUtils;

/**
 * Test for {@link XmlObjectRootProcessor}.
 * 
 * @author scheglov_ke
 */
public class XmlObjectRootProcessorTest extends AbstractCoreTest {
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
  // Visibility
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_visibility_separateTrue() throws Exception {
    prepareMyComponent(ArrayUtils.EMPTY_STRING_ARRAY, new String[]{
        "  <parameters>",
        "    <parameter name='visible.inTree'>true</parameter>",
        "    <parameter name='visible.inGraphical'>true</parameter>",
        "  </parameters>"});
    XmlObjectInfo object = prepareVisibilityObject();
    assertVisibleInGraphical(object, true);
    assertVisibleInTree(object, true);
  }

  public void test_visibility_separateFalse() throws Exception {
    prepareMyComponent(ArrayUtils.EMPTY_STRING_ARRAY, new String[]{
        "  <parameters>",
        "    <parameter name='visible.inTree'>false</parameter>",
        "    <parameter name='visible.inGraphical'>false</parameter>",
        "  </parameters>"});
    XmlObjectInfo object = prepareVisibilityObject();
    assertVisibleInGraphical(object, false);
    assertVisibleInTree(object, false);
  }

  public void test_visibility_different() throws Exception {
    prepareMyComponent(ArrayUtils.EMPTY_STRING_ARRAY, new String[]{
        "  <parameters>",
        "    <parameter name='visible.inTree'>true</parameter>",
        "    <parameter name='visible.inGraphical'>false</parameter>",
        "  </parameters>"});
    XmlObjectInfo object = prepareVisibilityObject();
    assertVisibleInGraphical(object, false);
    assertVisibleInTree(object, true);
  }

  public void test_visibility_bothFalse() throws Exception {
    prepareMyComponent(ArrayUtils.EMPTY_STRING_ARRAY, new String[]{
        "  <parameters>",
        "    <parameter name='visible'>false</parameter>",
        "  </parameters>"});
    XmlObjectInfo object = prepareVisibilityObject();
    assertVisibleInGraphical(object, false);
    assertVisibleInTree(object, false);
  }

  private XmlObjectInfo prepareVisibilityObject() throws Exception {
    parse(
        "<!-- filler filler filler filler filler -->",
        "<!-- filler filler filler filler filler -->",
        "<Shell>",
        "  <t:MyComponent wbp:name='object'/>",
        "</Shell>");
    return getObjectByName("object");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Decoration with "text"
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that "label" of {@link XmlObjectInfo} uses text from its "text" property.
   */
  public void test_decorateWithText_hasText() throws Exception {
    XmlObjectInfo shell =
        parse(
            "<!-- filler filler filler filler filler -->",
            "<!-- filler filler filler filler filler -->",
            "<Shell text='Hello!'/>");
    shell.refresh();
    //
    String text = ObjectsLabelProvider.INSTANCE.getText(shell);
    assertEquals("Shell - \"Hello!\"", text);
  }

  /**
   * Test that "label" of {@link XmlObjectInfo} uses text from its "text" property.
   */
  public void test_decorateWithText_noText() throws Exception {
    XmlObjectInfo shell =
        parse(
            "<!-- filler filler filler filler filler -->",
            "<!-- filler filler filler filler filler -->",
            "<Shell/>");
    shell.refresh();
    //
    String text = ObjectsLabelProvider.INSTANCE.getText(shell);
    assertEquals("Shell", text);
  }
}