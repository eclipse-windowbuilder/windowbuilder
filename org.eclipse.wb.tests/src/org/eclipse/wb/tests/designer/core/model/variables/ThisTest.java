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
package org.eclipse.wb.tests.designer.core.model.variables;

import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.variable.ThisVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * Test for {@link ThisVariableSupport}.
 *
 * @author scheglov_ke
 */
public class ThisTest extends AbstractVariableTest {
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
  public void test_support() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    //",
            "  }",
            "}");
    // check children
    assertEquals(1, panel.getChildrenJava().size());
    assertTrue(panel.getChildrenJava().get(0) instanceof FlowLayoutInfo);
    // check VariableSupport object
    ThisVariableSupport variableSupport = (ThisVariableSupport) panel.getVariableSupport();
    assertNotNull(variableSupport);
    assertEquals("this", variableSupport.toString());
    //
    assertEquals("(javax.swing.JPanel)", variableSupport.getTitle());
    // constructor
    {
      TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(panel);
      MethodDeclaration constructor =
          AstNodeUtils.getMethodBySignature(typeDeclaration, "<init>()");
      assertSame(constructor, variableSupport.getConstructor());
    }
    // check name
    assertEquals(false, variableSupport.hasName());
    try {
      variableSupport.getName();
      fail();
    } catch (IllegalStateException e) {
    }
    try {
      variableSupport.setName("foo");
      fail();
    } catch (IllegalStateException e) {
    }
    // component name
    assertEquals("this", variableSupport.getComponentName());
    // CreationSupport.can*
    {
      assertFalse(panel.getCreationSupport().canReorder());
      assertFalse(panel.getCreationSupport().canReparent());
    }
    // any statement is valid reference for "this"
    assertTrue(variableSupport.isValidStatementForChild(null));
    // we can request expression at any target
    assertTrue(variableSupport.hasExpression(null));
    // check getReferenceExpression/getAccessExpression
    assertEquals("this", variableSupport.getReferenceExpression((NodeTarget) null));
    assertEquals("", variableSupport.getAccessExpression((NodeTarget) null));
    // check conversion
    assertFalse(variableSupport.canConvertLocalToField());
    assertFalse(variableSupport.canConvertFieldToLocal());
    try {
      variableSupport.convertLocalToField();
      fail();
    } catch (IllegalStateException e) {
    }
    try {
      variableSupport.convertFieldToLocal();
      fail();
    } catch (IllegalStateException e) {
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Target
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Empty constructor.
   */
  public void test_target_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    //",
            "  }",
            "}");
    TypeDeclaration typeDeclaration = AstNodeUtils.getTypeByName(m_lastEditor.getAstUnit(), "Test");
    assertStatementTarget(panel, typeDeclaration.getMethods()[0].getBody(), null, true);
  }

  /**
   * Constructor with {@link Statement}.
   */
  public void test_target_2() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setVisible(true);",
            "  }",
            "}");
    TypeDeclaration typeDeclaration = AstNodeUtils.getTypeByName(m_lastEditor.getAstUnit(), "Test");
    assertStatementTarget(panel, typeDeclaration.getMethods()[0].getBody(), null, true);
  }

  /**
   * Constructor with {@link SuperMethodInvocation}.
   */
  public void test_target_3() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    super();",
            "  }",
            "}");
    TypeDeclaration typeDeclaration = AstNodeUtils.getTypeByName(m_lastEditor.getAstUnit(), "Test");
    MethodDeclaration constructor = typeDeclaration.getMethods()[0];
    assertStatementTarget(panel, null, (Statement) constructor.getBody().statements().get(0), false);
  }
}
