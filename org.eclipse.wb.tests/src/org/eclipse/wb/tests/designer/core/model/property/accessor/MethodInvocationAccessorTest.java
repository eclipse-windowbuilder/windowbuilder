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
package org.eclipse.wb.tests.designer.core.model.property.accessor;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.accessor.IAccessibleExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.MethodInvocationAccessor;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipProvider;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Tests for {@link MethodInvocationAccessor}.
 * 
 * @author scheglov_ke
 */
public class MethodInvocationAccessorTest extends SwingModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Project creation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    // prepare MyPanel
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public void setText(String text, boolean html) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='setText'>",
            "      <parameter type='java.lang.String' name='text' defaultSource='null'/>",
            "      <parameter type='boolean' name='html'/>",
            "    </method>",
            "  </methods>",
            "  <method-property title='text' method='setText(java.lang.String,boolean)'/>",
            "</component>"));
    waitForAutoBuild();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_access() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    GenericProperty property = (GenericProperty) panel.getPropertyByTitle("text");
    MethodInvocationAccessor accessor =
        (MethodInvocationAccessor) getGenericPropertyAccessors(property).get(0);
    // do checks
    assertNull(accessor.getAdapter(null));
    assertNotNull(accessor.getAdapter(IAccessibleExpressionAccessor.class));
    assertNotNull(accessor.getAdapter(PropertyTooltipProvider.class));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getExpression()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link MethodInvocationAccessor#getExpression(JavaInfo)}.<br>
   * No existing invocation.
   */
  public void test_getExpression_noInvocation() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    GenericProperty property = (GenericProperty) panel.getPropertyByTitle("text");
    MethodInvocationAccessor accessor =
        (MethodInvocationAccessor) getGenericPropertyAccessors(property).get(0);
    // do check
    assertNull(accessor.getExpression(panel));
  }

  /**
   * Test for {@link MethodInvocationAccessor#getExpression(JavaInfo)}.<br>
   * Has existing invocation.
   */
  public void test_getExpression_hasInvocation() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    setText('text', false);",
            "  }",
            "}");
    GenericProperty property = (GenericProperty) panel.getPropertyByTitle("text");
    MethodInvocationAccessor accessor =
        (MethodInvocationAccessor) getGenericPropertyAccessors(property).get(0);
    // do check
    MethodInvocation invocation = (MethodInvocation) accessor.getExpression(panel);
    assertEquals("setText(\"text\", false)", m_lastEditor.getSource(invocation));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setExpression()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link MethodInvocationAccessor#setExpression(JavaInfo, String)}.
   */
  public void test_setExpression_addNew() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    GenericProperty property = (GenericProperty) panel.getPropertyByTitle("text");
    MethodInvocationAccessor accessor =
        (MethodInvocationAccessor) getGenericPropertyAccessors(property).get(0);
    // do check
    accessor.setExpression(panel, "\"new text\", true");
    assertEditor(
        "// filler filler filler",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    setText('new text', true);",
        "  }",
        "}");
  }

  /**
   * Test for {@link MethodInvocationAccessor#setExpression(JavaInfo, String)}.
   */
  public void test_setExpression_removeExisting() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    setText('text', false);",
            "  }",
            "}");
    GenericProperty property = (GenericProperty) panel.getPropertyByTitle("text");
    MethodInvocationAccessor accessor =
        (MethodInvocationAccessor) getGenericPropertyAccessors(property).get(0);
    // do check
    accessor.setExpression(panel, null);
    assertEditor(
        "// filler filler filler",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "  }",
        "}");
  }

  /**
   * Test for {@link MethodInvocationAccessor#setExpression(JavaInfo, String)}.
   */
  public void test_setExpression_replaceExisting() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    setText('text', false);",
            "  }",
            "}");
    GenericProperty property = (GenericProperty) panel.getPropertyByTitle("text");
    MethodInvocationAccessor accessor =
        (MethodInvocationAccessor) getGenericPropertyAccessors(property).get(0);
    // do check
    accessor.setExpression(panel, "\"new text\", true");
    assertEditor(
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    setText('new text', true);",
        "  }",
        "}");
  }
}
