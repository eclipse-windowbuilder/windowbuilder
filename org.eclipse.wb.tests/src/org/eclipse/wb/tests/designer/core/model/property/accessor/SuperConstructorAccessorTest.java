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

import org.eclipse.wb.internal.core.model.creation.ThisCreationSupport;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.accessor.SuperConstructorAccessor;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;

/**
 * Tests for {@link SuperConstructorAccessor}.
 *
 * @author scheglov_ke
 */
public class SuperConstructorAccessorTest extends SwingModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public MyPanel(boolean enabled) {",
            "    setEnabled(enabled);",
            "  }",
            "}"));
    waitForAutoBuild();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_0() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    super(false);",
            "  }",
            "}");
    // check
    SuperConstructorAccessor accessor = getAccessor(panel, null);
    assertSame(Property.UNKNOWN_VALUE, accessor.getDefaultValue(panel));
    assertEquals(false, ((BooleanLiteral) accessor.getExpression(panel)).booleanValue());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setExpression
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_setExpression_newValue() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    super(false);",
            "  }",
            "}");
    // check
    SuperConstructorAccessor accessor = getAccessor(panel, null);
    assertTrue(accessor.setExpression(panel, "true"));
    assertEditor(
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    super(true);",
        "  }",
        "}");
  }

  /**
   * Use <code>null</code> to clear value.
   */
  public void test_setExpression_nullValue_noDefault() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    super(true);",
            "  }",
            "}");
    // check
    SuperConstructorAccessor accessor = getAccessor(panel, null);
    assertFalse(accessor.setExpression(panel, null));
    assertEditor(
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    super(true);",
        "  }",
        "}");
  }

  /**
   * Use <code>null</code> to clear value.
   */
  public void test_setExpression_nullValue_withDefault() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    super(true);",
            "  }",
            "}");
    // check
    SuperConstructorAccessor accessor = getAccessor(panel, "new java.lang.String().equals(null)");
    assertTrue(accessor.setExpression(panel, null));
    assertEditor(
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    super(new String().equals(null));",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static SuperConstructorAccessor getAccessor(ContainerInfo panel, String defaultSource) {
    MethodDeclaration constructor =
        ((ThisCreationSupport) panel.getCreationSupport()).getConstructor();
    SuperConstructorInvocation superConstructorInvocation =
        (SuperConstructorInvocation) constructor.getBody().statements().get(0);
    return new SuperConstructorAccessor(superConstructorInvocation, 0, defaultSource);
  }
}
