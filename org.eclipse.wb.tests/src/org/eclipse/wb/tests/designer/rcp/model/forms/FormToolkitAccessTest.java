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
package org.eclipse.wb.tests.designer.rcp.model.forms;

import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.InstanceFactoryInfo;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.rcp.IExceptionConstants;
import org.eclipse.wb.internal.rcp.model.forms.FormToolkitAccess;
import org.eclipse.wb.internal.rcp.model.forms.FormToolkitCreationSupport;
import org.eclipse.wb.internal.rcp.model.forms.FormToolkitVariableSupport;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link FormToolkitAccess}.
 * 
 * @author scheglov_ke
 */
public class FormToolkitAccessTest extends AbstractFormsTest {
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
  // FormToolkit_Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_invalid() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "import org.eclipse.ui.forms.*;",
            "import org.eclipse.ui.forms.widgets.*;",
            "public class Test {",
            "}");
    assertSame(null, FormToolkitAccess.get(typeDeclaration));
    try {
      FormToolkitAccess.getOrFail(typeDeclaration);
      fail();
    } catch (DesignerException e) {
      assertEquals(IExceptionConstants.NO_FORM_TOOLKIT, e.getCode());
    }
  }

  public void test_toolkitMethod_public() throws Exception {
    setFileContentSrc(
        "test/MyForm.java",
        getSource(
            "package test;",
            "import org.eclipse.ui.forms.*;",
            "import org.eclipse.ui.forms.widgets.*;",
            "public class MyForm {",
            "  public FormToolkit getPublicToolkit() {",
            "    return null;",
            "  }",
            "}"));
    waitForAutoBuild();
    TypeDeclaration typeDeclaration =
        createTypeDeclaration(
            "test",
            "Test.java",
            getSource(
                "package test;",
                "import org.eclipse.ui.forms.*;",
                "import org.eclipse.ui.forms.widgets.*;",
                "public class Test extends MyForm {",
                "  public void isToolkit_1() {",
                "    int a;",
                "  }",
                "  public void isToolkit_2() {",
                "    System.out.println();",
                "  }",
                "  public void isToolkit_3() {",
                "    getPublicToolkit();",
                "  }",
                "}"));
    FormToolkitAccess toolkitAccess = FormToolkitAccess.getOrFail(typeDeclaration);
    assertEquals("getPublicToolkit()", toolkitAccess.getReferenceExpression());
    assertToolkitNode(false, toolkitAccess, typeDeclaration, "isToolkit_1");
    assertToolkitNode(false, toolkitAccess, typeDeclaration, "isToolkit_2");
    assertToolkitNode(true, toolkitAccess, typeDeclaration, "isToolkit_3");
  }

  public void test_toolkitMethod_protected() throws Exception {
    setFileContentSrc(
        "test/MyForm.java",
        getSource(
            "package test;",
            "import org.eclipse.ui.forms.*;",
            "import org.eclipse.ui.forms.widgets.*;",
            "public class MyForm {",
            "  public FormToolkit getProtectedToolkit() {",
            "    return null;",
            "  }",
            "}"));
    waitForAutoBuild();
    TypeDeclaration typeDeclaration =
        createTypeDeclaration(
            "test",
            "Test.java",
            getSource(
                "package test;",
                "import org.eclipse.ui.forms.*;",
                "import org.eclipse.ui.forms.widgets.*;",
                "public class Test extends MyForm {",
                "}"));
    FormToolkitAccess toolkitAccess = FormToolkitAccess.getOrFail(typeDeclaration);
    assertEquals("getProtectedToolkit()", toolkitAccess.getReferenceExpression());
  }

  public void test_toolkitField() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "import org.eclipse.ui.forms.*;",
            "import org.eclipse.ui.forms.widgets.*;",
            "public class Test {",
            "  private FormToolkit m_toolkit;",
            "  //",
            "  private int field;",
            "  public void isToolkit_1() {",
            "    int a;",
            "  }",
            "  public int isToolkit_2() {",
            "    return field;",
            "  }",
            "  public Object isToolkit_3() {",
            "    return m_toolkit;",
            "  }",
            "}");
    FormToolkitAccess toolkitAccess = FormToolkitAccess.getOrFail(typeDeclaration);
    assertEquals("m_toolkit", toolkitAccess.getReferenceExpression());
    assertToolkitNode(false, toolkitAccess, typeDeclaration, "isToolkit_1");
    assertToolkitNode(false, toolkitAccess, typeDeclaration, "isToolkit_2");
    assertToolkitNode(true, toolkitAccess, typeDeclaration, "isToolkit_3");
  }

  public void test_toolkitLocal() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "import org.eclipse.ui.forms.*;",
            "import org.eclipse.ui.forms.widgets.*;",
            "import org.eclipse.swt.widgets.*;",
            "public class Test {",
            "  public void createMasterPart(int a, double b) {",
            "    System.out.println(1);",
            "    FormToolkit toolkit = new FormToolkit((Display) null);",
            "    System.out.println(2);",
            "    System.out.println(toolkit);",
            "  }",
            "  public void foo() {",
            "    System.out.println(3);",
            "  }",
            "}");
    FormToolkitAccess toolkitAccess = FormToolkitAccess.getOrFail(typeDeclaration);
    assertEquals("toolkit", toolkitAccess.getReferenceExpression());
    {
      ASTNode node = m_lastEditor.getEnclosingNode("toolkit)");
      assertTrue(toolkitAccess.isToolkit(node));
    }
    {
      ASTNode node = m_lastEditor.getEnclosingNode("3)");
      assertFalse(toolkitAccess.isToolkit(node));
    }
  }

  public void test_formMethod() throws Exception {
    setFileContentSrc(
        "test/MyForm.java",
        getSource(
            "package test;",
            "import org.eclipse.ui.forms.*;",
            "import org.eclipse.ui.forms.widgets.*;",
            "public class MyForm {",
            "  public IManagedForm getMyForm() {",
            "    return null;",
            "  }",
            "}"));
    waitForAutoBuild();
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "import org.eclipse.ui.forms.*;",
            "import org.eclipse.ui.forms.widgets.*;",
            "public class Test extends MyForm {",
            "  public void isToolkit_1() {",
            "    int a;",
            "  }",
            "  public void isToolkit_2() {",
            "    System.out.println();",
            "  }",
            "  public void isToolkit_3() {",
            "    getMyForm().getToolkit();",
            "  }",
            "}");
    FormToolkitAccess toolkitAccess = FormToolkitAccess.getOrFail(typeDeclaration);
    assertEquals("getMyForm().getToolkit()", toolkitAccess.getReferenceExpression());
    assertToolkitNode(false, toolkitAccess, typeDeclaration, "isToolkit_1");
    assertToolkitNode(false, toolkitAccess, typeDeclaration, "isToolkit_2");
    assertToolkitNode(true, toolkitAccess, typeDeclaration, "isToolkit_3");
  }

  public void test_formField() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "import org.eclipse.ui.forms.*;",
            "import org.eclipse.ui.forms.widgets.*;",
            "public class Test {",
            "  private IManagedForm m_form;",
            "  //",
            "  private int field;",
            "  public void isToolkit_1() {",
            "    int a;",
            "  }",
            "  public int isToolkit_2() {",
            "    return field;",
            "  }",
            "  public Object isToolkit_3() {",
            "    return m_form.getToolkit();",
            "  }",
            "}");
    FormToolkitAccess toolkitAccess = FormToolkitAccess.getOrFail(typeDeclaration);
    assertEquals("m_form.getToolkit()", toolkitAccess.getReferenceExpression());
    assertToolkitNode(false, toolkitAccess, typeDeclaration, "isToolkit_1");
    assertToolkitNode(false, toolkitAccess, typeDeclaration, "isToolkit_2");
    assertToolkitNode(true, toolkitAccess, typeDeclaration, "isToolkit_3");
  }

  public void test_formMethod_prefer_toolkitLocal() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "import org.eclipse.swt.widgets.*;",
            "import org.eclipse.ui.forms.*;",
            "import org.eclipse.ui.forms.widgets.*;",
            "public class Test {",
            "  public void createMasterPart(int a, double b) {",
            "    FormToolkit toolkit = new FormToolkit((Display) null);",
            "  }",
            "  public IManagedForm getMyForm() {",
            "    return null;",
            "  }",
            "}");
    FormToolkitAccess toolkitAccess = FormToolkitAccess.getOrFail(typeDeclaration);
    assertEquals("toolkit", toolkitAccess.getReferenceExpression());
  }

  /**
   * Assert that {@link Expression} of first {@link Statement} in method with given name is/not
   * {@link FormToolkitAccess#isToolkit(ASTNode)}.
   */
  private static void assertToolkitNode(boolean expectedResult,
      FormToolkitAccess toolkitAccess,
      TypeDeclaration typeDeclaration,
      String methodName) {
    ASTNode node;
    {
      MethodDeclaration methodDeclaration =
          AstNodeUtils.getMethodBySignature(typeDeclaration, methodName + "()");
      Statement statement = DomGenerics.statements(methodDeclaration.getBody()).get(0);
      if (statement instanceof ExpressionStatement) {
        node = ((ExpressionStatement) statement).getExpression();
      } else if (statement instanceof ReturnStatement) {
        node = ((ReturnStatement) statement).getExpression();
      } else {
        node = statement;
      }
    }
    assertThat(toolkitAccess.isToolkit(node)).isEqualTo(expectedResult);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // FormToolkit_*Support
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link FormToolkitCreationSupport} and {@link FormToolkitVariableSupport}.
   */
  public void test_toolkitAccessSupports() throws Exception {
    setFileContentSrc(
        "test/MyShell.java",
        getTestSource(
            "public abstract class MyShell extends Shell {",
            "  protected IManagedForm m_managedForm;",
            "  public void initialize(IManagedForm form) {",
            "    m_managedForm = form;",
            "  }",
            "  public abstract void createContents(Composite parent);",
            "  protected void checkSubclass () {}",
            "}"));
    setFileContentSrc(
        "test/MyShell.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='FormToolkit.configureMethod'>createContents(org.eclipse.swt.widgets.Composite)</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    CompositeInfo shell =
        parseComposite(
            "public class Test extends MyShell {",
            "  public Test() {",
            "    createContents(this);",
            "  }",
            "  public void createContents(Composite parent) {",
            "  }",
            "  private void isToolkit_1() {",
            "    m_managedForm.getToolkit();",
            "  }",
            "}");
    TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(shell);
    // prepare toolkit, its creation/variable supports
    FormToolkitAccess toolkitAccess = FormToolkitAccess.getOrFail(typeDeclaration);
    CreationSupport creationSupport = new FormToolkitCreationSupport(shell, toolkitAccess);
    InstanceFactoryInfo toolkit =
        InstanceFactoryInfo.createFactory(
            m_lastEditor,
            m_lastLoader.loadClass("org.eclipse.ui.forms.widgets.FormToolkit"),
            creationSupport);
    VariableSupport variableSupport = new FormToolkitVariableSupport(toolkit, shell, toolkitAccess);
    toolkit.setVariableSupport(variableSupport);
    // check CreationSupport
    {
      assertEquals("toolkitAccess: m_managedForm.getToolkit()", creationSupport.toString());
      assertSame(shell.getCreationSupport().getNode(), creationSupport.getNode());
      // check for isJavaInfo()
      assertFalse(creationSupport.isJavaInfo(null));
      {
        MethodDeclaration methodDeclaration =
            AstNodeUtils.getMethodBySignature(typeDeclaration, "isToolkit_1()");
        Statement statement = DomGenerics.statements(methodDeclaration.getBody()).get(0);
        Expression toolkitExpression = ((ExpressionStatement) statement).getExpression();
        assertTrue(creationSupport.isJavaInfo(toolkitExpression));
      }
      // validation
      assertFalse(creationSupport.canDelete());
      assertFalse(creationSupport.canReorder());
      assertFalse(creationSupport.canReparent());
    }
    // check VariableSupport
    {
      assertEquals("toolkitAccess", variableSupport.toString());
      assertEquals("FormToolkit instance", variableSupport.getTitle());
      // expressions
      {
        NodeTarget nodeTarget = getNodeStatementTarget(shell, false, 0);
        assertEquals(
            "m_managedForm.getToolkit()",
            variableSupport.getReferenceExpression(nodeTarget));
        assertEquals("m_managedForm.getToolkit().", variableSupport.getAccessExpression(nodeTarget));
      }
      // target
      {
        MethodDeclaration expectedMethod =
            AstNodeUtils.getMethodBySignature(
                typeDeclaration,
                "createContents(org.eclipse.swt.widgets.Composite)");
        assertTarget(variableSupport.getStatementTarget(), expectedMethod.getBody(), null, true);
      }
    }
  }
}