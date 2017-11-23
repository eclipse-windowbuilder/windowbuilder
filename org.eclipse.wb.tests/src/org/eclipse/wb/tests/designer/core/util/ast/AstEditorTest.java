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
package org.eclipse.wb.tests.designer.core.util.ast;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.ast.AnonymousTypeDeclaration;
import org.eclipse.wb.internal.core.utils.ast.AstCodeGeneration;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.AstParser;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.IASTEditorCommitListener;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.AssertionFailedException;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.exception.ICoreExceptionConstants;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.StrValue;
import org.eclipse.wb.tests.designer.core.AbstractJavaTest;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.ide.IDE;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.easymock.EasyMock;

import java.util.List;

/**
 * Tests for {@link AstEditor}.
 * 
 * @author scheglov_ke
 */
public class AstEditorTest extends AbstractJavaTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    if (m_testProject == null) {
      do_projectCreate();
    }
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    DesignerPlugin.getActivePage().closeAllEditors(false);
  }

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
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstEditor#getJavaProject()}.
   */
  public void test_getJavaProject() throws Exception {
    createTypeDeclaration_TestC("");
    assertSame(m_testProject.getJavaProject(), m_lastEditor.getJavaProject());
  }

  /**
   * Test for {@link AstEditor#getProject()}.
   */
  public void test_getProject() throws Exception {
    createTypeDeclaration_TestC("");
    assertSame(m_testProject.getProject(), m_lastEditor.getProject());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // hasCompilationErrors()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that {@link AstEditor#hasCompilationErrors()}.
   */
  public void test_hasCompilationErrors_false() throws Exception {
    createTypeDeclaration_TestC("");
    assertFalse(m_lastEditor.hasCompilationErrors());
  }

  /**
   * Test that {@link AstEditor#hasCompilationErrors()}.
   */
  public void test_hasCompilationErrors_true() throws Exception {
    m_ignoreModelCompileProblems = true;
    createTypeDeclaration_TestC("somethingBad");
    assertTrue(m_lastEditor.hasCompilationErrors());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getPrimaryType()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getPrimaryType() throws Exception {
    CompilationUnit compilationUnit =
        createASTCompilationUnit(
            "test",
            "Test.java",
            getSource(
                "package test;",
                "",
                "class Foo {}",
                "",
                "public class Test {",
                "  public Test() {",
                "  }",
                "}",
                "",
                "class Bar {}"));
    assertSame(compilationUnit.types().get(1), m_lastEditor.getPrimaryType());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getModelType()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstEditor#getModelType(TypeDeclaration)}.
   */
  public void test_getModelType() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  public Test() {",
            "    int a;",
            "  }",
            "  private void init() {",
            "  }",
            "}");
    IType expected = m_lastEditor.getModelUnit().getType("Test");
    IType actual = m_lastEditor.getModelType(typeDeclaration);
    assertEquals(expected, actual);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // moveStatement
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_moveStatement_nop_1() throws Exception {
    String[] lines = new String[]{"    int a;", "    int b;"};
    char_moveStatement(lines, 0, 0, true, lines, false);
  }

  public void test_moveStatement_nop_2() throws Exception {
    String[] lines = new String[]{"    int a;", "    int b;"};
    char_moveStatement(lines, 0, 0, false, lines, false);
  }

  public void test_moveStatement_nop_3() throws Exception {
    String[] lines = new String[]{"    int a;", "    int b;"};
    char_moveStatement(lines, 0, 1, true, lines, false);
  }

  public void test_moveStatement_nop_4() throws Exception {
    String[] lines = new String[]{"    int a;", "    int b;"};
    char_moveStatement(lines, 1, 0, false, lines, false);
  }

  public void test_moveStatement_nop_5() throws Exception {
    String[] lines = new String[]{"    int a;", "    int b;"};
    char_moveStatement(lines, 0, -1, true, lines, false);
  }

  public void test_moveStatement_nop_6() throws Exception {
    String[] lines = new String[]{"    int a;", "    int b;"};
    char_moveStatement(lines, 1, -1, false, lines, false);
  }

  public void test_moveStatement_1() throws Exception {
    String[] lines_0 = new String[]{"    int a;", "    int b;", "    int c;"};
    String[] lines_1 = new String[]{"    int b;", "    int a;", "    int c;"};
    char_moveStatement(lines_0, 1, 0, true, lines_1, true);
  }

  public void test_moveStatement_2() throws Exception {
    String[] lines_0 = new String[]{"    int a;", "    int b;", "    int c;"};
    String[] lines_1 = new String[]{"    int b;", "    int a;", "    int c;"};
    char_moveStatement(lines_0, 0, 2, true, lines_1, true);
  }

  public void test_moveStatement_3() throws Exception {
    String[] lines_0 = new String[]{"    int a;", "    int b", "      = ", "      5;"};
    String[] lines_1 = new String[]{"    int b", "      = ", "      5;", "    int a;"};
    char_moveStatement(lines_0, 1, 0, true, lines_1, true);
  }

  public void test_moveStatement_4_before() throws Exception {
    String[] lines_0 =
        new String[]{
            "    {",
            "      int a0123456789__;",
            "    }",
            "    {",
            "      int b0123456789;",
            "    }"};
    String[] lines_1 =
        new String[]{
            "    {",
            "      int b0123456789;",
            "      int a0123456789__;",
            "    }",
            "    {",
            "    }"};
    char_moveStatement(lines_0, new int[]{1, 0}, new int[]{0, 0}, true, lines_1, true);
  }

  public void test_moveStatement_4_after() throws Exception {
    String[] lines_0 =
        new String[]{
            "    {",
            "      int a0123456789__;",
            "    }",
            "    {",
            "      int b0123456789;",
            "    }"};
    String[] lines_1 =
        new String[]{
            "    {",
            "      int a0123456789__;",
            "      int b0123456789;",
            "    }",
            "    {",
            "    }"};
    char_moveStatement(lines_0, new int[]{1, 0}, new int[]{0, 0}, false, lines_1, true);
  }

  public void test_moveStatement_4_inBlockBegin() throws Exception {
    String[] lines_0 =
        new String[]{
            "    {",
            "      int a0123456789__;",
            "    }",
            "    int bbbbbb01234567890123456789;"};
    String[] lines_1 =
        new String[]{
            "    {",
            "      int bbbbbb01234567890123456789;",
            "      int a0123456789__;",
            "    }"};
    char_moveStatement(lines_0, new int[]{1}, new int[]{0, -1}, true, lines_1, true);
  }

  public void test_moveStatement_4_inBlockEnd() throws Exception {
    String[] lines_0 =
        new String[]{
            "    {",
            "      int a0123456789__;",
            "    }",
            "    int bbbbbb01234567890123456789;"};
    String[] lines_1 =
        new String[]{
            "    {",
            "      int a0123456789__;",
            "      int bbbbbb01234567890123456789;",
            "    }"};
    char_moveStatement(lines_0, new int[]{1}, new int[]{0, -1}, false, lines_1, true);
  }

  public void test_moveStatement_5() throws Exception {
    String[] lines_0 =
        new String[]{
            "    {",
            "      int a01234567890123456789;",
            "    }",
            "    int",
            "      b01234567890123456789;"};
    String[] lines_1 =
        new String[]{
            "    {",
            "      int",
            "        b01234567890123456789;",
            "      int a01234567890123456789;",
            "    }"};
    char_moveStatement(lines_0, new int[]{1}, new int[]{0, 0}, true, lines_1, true);
  }

  public void test_moveStatement_6() throws Exception {
    String[] lines_0 = new String[]{"    int a;", "    int b;", "    int c;"};
    String[] lines_1 = new String[]{"    int b;", "    int a;", "    int c;"};
    char_moveStatement(lines_0, 0, 1, false, lines_1, true);
  }

  public void test_moveStatement_7() throws Exception {
    String[] lines_0 =
        new String[]{
            "    {",
            "      int a;",
            "    }",
            "    {",
            "      int b;",
            "    }",
            "    int c;"};
    String[] lines_1 =
        new String[]{
            "    {",
            "    }",
            "    {",
            "      int b;",
            "      int a;",
            "    }",
            "    int c;"};
    char_moveStatement(lines_0, new int[]{0, 0}, new int[]{1, 0}, false, lines_1, true);
  }

  public void test_moveStatement_8() throws Exception {
    String[] lines_0 = new String[]{"    int a;", "    int b;", "    int c;"};
    String[] lines_1 = new String[]{"    int c;", "    int a;", "    int b;"};
    char_moveStatement(lines_0, 2, -1, true, lines_1, true);
  }

  public void test_moveStatement_9() throws Exception {
    String[] lines_0 = new String[]{"    int a;", "    int b;", "    int c;"};
    String[] lines_1 = new String[]{"    int b;", "    int c;", "    int a;"};
    char_moveStatement(lines_0, 0, -1, false, lines_1, true);
  }

  public void test_moveStatement_10() throws Exception {
    String[] lines_0 =
        new String[]{
            "    {",
            "      int a;",
            "    }",
            "    {",
            "      int b;",
            "    }",
            "    int c;"};
    String[] lines_1 =
        new String[]{
            "    {",
            "    }",
            "    {",
            "      int a;",
            "      int b;",
            "    }",
            "    int c;"};
    char_moveStatement(lines_0, new int[]{0, 0}, new int[]{1, -1}, true, lines_1, true);
  }

  public void test_moveStatement_11() throws Exception {
    String[] lines_0 =
        new String[]{
            "    {",
            "      int a0123;",
            "      int a;",
            "    }",
            "    {",
            "      int b;",
            "    }",
            "    int c;"};
    String[] lines_1 =
        new String[]{
            "    {",
            "      int a0123;",
            "    }",
            "    {",
            "      int b;",
            "      int a;",
            "    }",
            "    int c;"};
    char_moveStatement(lines_0, new int[]{0, 1}, new int[]{1, -1}, false, lines_1, true);
  }

  public void test_moveStatement_12() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  public Test() {",
            "    int a;",
            "  }",
            "  private void init() {",
            "  }",
            "}");
    MethodDeclaration method_constructor = typeDeclaration.getMethods()[0];
    MethodDeclaration method_init = typeDeclaration.getMethods()[1];
    // move statement
    Statement statement = DomGenerics.statements(method_constructor.getBody()).get(0);
    m_lastEditor.moveStatement(statement, new StatementTarget(method_init, false));
    // check source
    assertEditor(
        getSourceDQ(
            "package test;",
            "public class Test {",
            "  public Test() {",
            "  }",
            "  private void init() {",
            "    int a;",
            "  }",
            "}"),
        m_lastEditor);
    assertSame(statement.getParent(), method_init.getBody());
  }

  public void test_moveStatement_emptyLine() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration(
            "test",
            "Test.java",
            getSource(
                "package test;",
                "public class Test {",
                "  public Test() {",
                "    {",
                "      int foo;",
                "",
                "      int bar;",
                "    }",
                "    int target;",
                "  }",
                "}"));
    MethodDeclaration constructor = typeDeclaration.getMethods()[0];
    List<Statement> statements = DomGenerics.statements(constructor.getBody());
    // move statement
    Statement statement = statements.get(0);
    m_lastEditor.moveStatement(statement, new StatementTarget(statements.get(1), false));
    // check source
    assertEditor(
        getSource(
            "package test;",
            "public class Test {",
            "  public Test() {",
            "    int target;",
            "    {",
            "      int foo;",
            "",
            "      int bar;",
            "    }",
            "  }",
            "}"),
        m_lastEditor);
  }

  /**
   * When source has lines with mixed indentation - leading tabs and then spaces, this caused
   * shifting lines again and again.
   */
  public void test_moveStatement_mixedIndent() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration(
            "test",
            "Test.java",
            getSource(
                "package test;",
                "public class Test {",
                "  public Test() {",
                "\t\t    int lineToMove;",
                "\t\t    int target;",
                "  }",
                "}"));
    MethodDeclaration constructor = typeDeclaration.getMethods()[0];
    List<Statement> statements = DomGenerics.statements(constructor.getBody());
    // move statement
    Statement statementToMove = statements.get(0);
    Statement targetStatement = statements.get(1);
    m_lastEditor.moveStatement(statementToMove, new StatementTarget(targetStatement, false));
    // check source
    assertEditor(
        getSource(
            "package test;",
            "public class Test {",
            "  public Test() {",
            "\t\t    int target;",
            "\t\t    int lineToMove;",
            "  }",
            "}"),
        m_lastEditor);
  }

  /**
   * When source has lines with mixed indentation - leading tabs and then spaces, this caused
   * shifting lines again and again.
   */
  public void test_moveStatement_mixedIndent_withEmptyLine() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration(
            "test",
            "Test.java",
            getSource(
                "package test;",
                "public class Test {",
                "  public Test() {",
                "\t\t    ",
                "\t\t    int lineToMove;",
                "\t\t    int target;",
                "  }",
                "}"));
    MethodDeclaration constructor = typeDeclaration.getMethods()[0];
    List<Statement> statements = DomGenerics.statements(constructor.getBody());
    // move statement
    Statement statementToMove = statements.get(0);
    Statement targetStatement = statements.get(1);
    m_lastEditor.moveStatement(statementToMove, new StatementTarget(targetStatement, false));
    // check source
    assertEditor(
        getSource(
            "package test;",
            "public class Test {",
            "  public Test() {",
            "\t\t    int target;",
            "\t\t    ",
            "\t\t    int lineToMove;",
            "  }",
            "}"),
        m_lastEditor);
  }

  private void char_moveStatement(String[] initialLines,
      int statementIndex,
      int targetStatementIndex,
      boolean before,
      String[] expectedLines,
      boolean wereModifications) throws Exception {
    char_moveStatement(
        initialLines,
        new int[]{statementIndex},
        new int[]{targetStatementIndex},
        before,
        expectedLines,
        wereModifications);
  }

  /**
   * When we move {@link Statement}, we should update {@link TagElement} positions.
   */
  public void test_moveStatement_withTagElement_forward() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration(
            "test",
            "Test.java",
            getSource(
                "package test;",
                "public class Test {",
                "  public void source() {",
                "    int statement;",
                "  }",
                "  /**",
                "  * @tag",
                "  */",
                "  public void target() {",
                "  }",
                "}"));
    MethodDeclaration source = typeDeclaration.getMethods()[0];
    MethodDeclaration target = typeDeclaration.getMethods()[1];
    // move statement
    Statement statementToMove = DomGenerics.statements(source).get(0);
    m_lastEditor.moveStatement(statementToMove, new StatementTarget(target, false));
    assertEditor(
        getSource(
            "package test;",
            "public class Test {",
            "  public void source() {",
            "  }",
            "  /**",
            "  * @tag",
            "  */",
            "  public void target() {",
            "    int statement;",
            "  }",
            "}"),
        m_lastEditor);
  }

  /**
   * When we move {@link Statement}, we should update {@link TagElement} positions.
   */
  public void test_moveStatement_withTagElement_backward() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration(
            "test",
            "Test.java",
            getSource(
                "package test;",
                "public class Test {",
                "  public void target() {",
                "  }",
                "  /**",
                "  * @tag",
                "  */",
                "  public void source() {",
                "    int statement;",
                "  }",
                "}"));
    MethodDeclaration source = typeDeclaration.getMethods()[1];
    MethodDeclaration target = typeDeclaration.getMethods()[0];
    // move statement
    Statement statementToMove = DomGenerics.statements(source).get(0);
    m_lastEditor.moveStatement(statementToMove, new StatementTarget(target, false));
    assertEditor(
        getSource(
            "package test;",
            "public class Test {",
            "  public void target() {",
            "    int statement;",
            "  }",
            "  /**",
            "  * @tag",
            "  */",
            "  public void source() {",
            "  }",
            "}"),
        m_lastEditor);
  }

  private void char_moveStatement(String[] initialLines,
      int[] statementIndex,
      int[] targetStatementIndex,
      boolean before,
      String[] expectedLines,
      boolean wereModifications) throws Exception {
    String[] lines_0 = new String[]{"package test;", "class Test {", "  Test() {"};
    String[] lines_2 = new String[]{"  }", "}"};
    TypeDeclaration typeDeclaration =
        createTypeDeclaration("test", "Test.java", getSource3(lines_0, initialLines, lines_2));
    long initialModifications = typeDeclaration.getAST().modificationCount();
    Block body = typeDeclaration.getMethods()[0].getBody();
    // prepare target
    StatementTarget target;
    {
      Block targetBlock;
      Statement targetStatement;
      if (targetStatementIndex[targetStatementIndex.length - 1] == -1) {
        targetBlock = (Block) getStatement(body, targetStatementIndex);
        targetStatement = null;
      } else {
        targetBlock = null;
        targetStatement = getStatement(body, targetStatementIndex);
      }
      target = new StatementTarget(targetBlock, targetStatement, before);
    }
    // move
    Statement statement = getStatement(body, statementIndex);
    m_lastEditor.moveStatement(statement, target);
    // check if changes were made
    if (wereModifications) {
      assertFalse(typeDeclaration.getAST().modificationCount() == initialModifications);
    } else {
      assertTrue(typeDeclaration.getAST().modificationCount() == initialModifications);
    }
    // validate
    assertEquals(getSource3(lines_0, expectedLines, lines_2), m_lastEditor.getSource());
    assertAST(m_lastEditor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getEnclosingXXX
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstEditor#getEnclosingNode(int)}.
   */
  public void test_getEnclosingNode() throws Exception {
    TypeDeclaration typeDeclaration = createTypeDeclaration_TestC("void foo(){}");
    MethodDeclaration method = typeDeclaration.getMethods()[0];
    {
      ASTNode node = typeDeclaration.getName();
      assertSame(node, m_lastEditor.getEnclosingNode(node.getStartPosition() + 1));
    }
    {
      ASTNode node = method.getName();
      assertSame(method, m_lastEditor.getEnclosingNode(AstNodeUtils.getSourceEnd(node)));
    }
  }

  /**
   * Tests for {@link AstEditor} "getEnclosing*" methods.
   */
  public void test_getEnclosing_all() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  void foo() {",
            "    System.out.println();",
            "  }",
            "}");
    // prepare testing targets
    MethodDeclaration method = typeDeclaration.getMethods()[0];
    Block block = method.getBody();
    ExpressionStatement statement = (ExpressionStatement) block.statements().get(0);
    Expression expression = statement.getExpression();
    // Statement
    {
      assertSame(statement, m_lastEditor.getEnclosingStatement(statement.getStartPosition()));
      assertSame(statement, m_lastEditor.getEnclosingStatement(statement.getStartPosition() + 5));
      assertSame(statement, m_lastEditor.getEnclosingStatement(expression.getStartPosition()));
      assertNull(m_lastEditor.getEnclosingStatement(typeDeclaration.getStartPosition()));
    }
    // Block
    {
      assertSame(block, m_lastEditor.getEnclosingBlock(block.getStartPosition()));
      assertSame(block, m_lastEditor.getEnclosingBlock(statement.getStartPosition()));
      assertSame(block, m_lastEditor.getEnclosingBlock(statement.getStartPosition() + 5));
      assertNull(m_lastEditor.getEnclosingBlock(typeDeclaration.getStartPosition()));
    }
    // MethodDeclaration
    {
      assertSame(method, m_lastEditor.getEnclosingMethod(method.getStartPosition()));
      assertSame(method, m_lastEditor.getEnclosingMethod(method.getStartPosition() + 5));
      assertSame(method, m_lastEditor.getEnclosingMethod(block.getStartPosition()));
      assertNull(m_lastEditor.getEnclosingMethod(typeDeclaration.getStartPosition()));
    }
    // TypeDeclaration
    {
      assertSame(typeDeclaration, m_lastEditor.getEnclosingType(typeDeclaration.getStartPosition()));
      assertSame(
          typeDeclaration,
          m_lastEditor.getEnclosingType(typeDeclaration.getStartPosition() + 5));
      assertNull(m_lastEditor.getEnclosingType(typeDeclaration.getParent().getStartPosition()));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // putGlobalValue
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_globalValue() throws Exception {
    createTypeDeclaration_TestC("");
    //
    String key = "KEY";
    assertNull(m_lastEditor.getGlobalValue(key));
    m_lastEditor.putGlobalValue(key, this);
    assertSame(this, m_lastEditor.getGlobalValue(key));
    m_lastEditor.removeGlobalValue(key);
    assertNull(m_lastEditor.getGlobalValue(key));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getRootMethods
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getRootMethods() throws Exception {
    TypeDeclaration typeDeclaration = createTypeDeclaration_TestC("void root(){}");
    EditorState state = EditorState.get(m_lastEditor);
    // initially no methods
    assertNull(state.getFlowDescription());
    // try to set "null", should fail
    try {
      state.setFlowDescription(null);
      fail();
    } catch (AssertionFailedException e) {
    }
    // set
    ExecutionFlowDescription flowDescription =
        new ExecutionFlowDescription(typeDeclaration.getMethods());
    state.setFlowDescription(flowDescription);
    assertSame(flowDescription, state.getFlowDescription());
    // try to set again, should be fine (we allow to set root methods several times since 2007-05-16)
    state.setFlowDescription(flowDescription);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getClassLoader
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getClassLoader() throws Exception {
    createTypeDeclaration_TestC("");
    EditorState state = EditorState.get(m_lastEditor);
    // initially no class loader
    assertNull(state.getEditorLoader());
    // try to set "null", should fail
    while (true) {
      try {
        state.initialize(null, null);
      } catch (AssertionFailedException e) {
        break;
      }
      fail();
    }
    // set
    ClassLoader classLoader = getClass().getClassLoader();
    state.initialize(null, classLoader);
    assertSame(classLoader, state.getEditorLoader());
    // try to set again, should fail
    while (true) {
      try {
        state.initialize(null, classLoader);
      } catch (AssertionFailedException e) {
        break;
      }
      fail();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // commitChanges
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that when source in {@link AstEditor} is same as in underlying {@link ICompilationUnit},
   * we don't touch {@link ICompilationUnit}, so don't set "modified" flag.
   */
  public void test_commitChanges_withoutChanges() throws Exception {
    createTypeDeclaration_TestC("");
    // compilation unit is not modified initially
    assertTrue(m_lastModelUnit.isConsistent());
    // commit changes, but source in ASTEditor is same as in compilation unit, so again no changes
    m_lastEditor.commitChanges();
    assertTrue(m_lastModelUnit.isConsistent());
  }

  /**
   * Test that when source in {@link AstEditor} is same as in underlying {@link ICompilationUnit},
   * we don't touch {@link ICompilationUnit}, so don't set "modified" flag.
   */
  public void test_commitChanges_underlayingFileChanged() throws Exception {
    createTypeDeclaration_TestC("");
    // update underlying file
    String newContent = getFileContentSrc("test/Test.java") + "// new comment";
    setFileContentSrc("test/Test.java", newContent);
    // commit changes - no changes made via ASTEditor, so file should not be changed
    m_lastEditor.saveChanges(false);
    assertEquals(newContent, getFileContentSrc("test/Test.java"));
  }

  /**
   * Test set {@link IASTEditorCommitListener} is used during {@link AstEditor#commitChanges()}.
   */
  public void test_commitChanges_listener() throws Exception {
    createTypeDeclaration_TestC("");
    // set listener
    final boolean[] aboutNotified = new boolean[1];
    final boolean[] doneNotified = new boolean[1];
    m_lastEditor.setCommitListener(new IASTEditorCommitListener() {
      public void aboutToCommit() {
        aboutNotified[0] = true;
      }

      public boolean canEditBaseFile() {
        return true;
      }

      public void commitDone() {
        doneNotified[0] = true;
      }
    });
    // commit changes, both notifications expected
    m_lastEditor.commitChanges();
    assertTrue(aboutNotified[0]);
    assertTrue(doneNotified[0]);
  }

  /**
   * Test set {@link IASTEditorCommitListener} is used during {@link AstEditor#commitChanges()}.
   */
  public void test_commitChanges_listener_canEdit() throws Exception {
    TypeDeclaration typeDeclaration = createTypeDeclaration_TestC("");
    // set listener
    final boolean[] canEditValue = {true};
    final boolean[] aboutNotified = new boolean[1];
    final boolean[] canEditNotified = new boolean[1];
    final boolean[] doneNotified = new boolean[1];
    m_lastEditor.setCommitListener(new IASTEditorCommitListener() {
      public void aboutToCommit() {
        aboutNotified[0] = true;
      }

      public boolean canEditBaseFile() {
        canEditNotified[0] = true;
        return canEditValue[0];
      }

      public void commitDone() {
        doneNotified[0] = true;
      }
    });
    // commit changes, both notifications expected
    m_lastEditor.commitChanges();
    //
    assertTrue(aboutNotified[0]);
    assertFalse(canEditNotified[0]);
    assertTrue(doneNotified[0]);
    // clear state
    aboutNotified[0] = false;
    canEditNotified[0] = false;
    doneNotified[0] = false;
    // change editor
    SimpleName nameNode = typeDeclaration.getName();
    m_lastEditor.replaceSubstring(nameNode.getStartPosition(), nameNode.getLength(), "FooBar");
    //
    m_lastEditor.commitChanges();
    assertTrue(aboutNotified[0]);
    assertTrue(canEditNotified[0]);
    assertTrue(doneNotified[0]);
    assertEquals("package test;public class FooBar{}", m_lastEditor.getSource());
    assertEquals("package test;public class FooBar{}", m_lastModelUnit.getBuffer().getContents());
    // set don't edit mode
    canEditValue[0] = false;
    // clear state
    aboutNotified[0] = false;
    canEditNotified[0] = false;
    doneNotified[0] = false;
    // change editor
    nameNode = typeDeclaration.getName();
    m_lastEditor.replaceSubstring(nameNode.getStartPosition(), nameNode.getLength(), "BackToTest");
    //
    m_lastEditor.commitChanges();
    assertTrue(aboutNotified[0]);
    assertTrue(canEditNotified[0]);
    assertTrue(doneNotified[0]);
    assertEquals("package test;public class BackToTest{}", m_lastEditor.getSource());
    assertEquals("package test;public class FooBar{}", m_lastModelUnit.getBuffer().getContents());
  }

  /**
   * Test for real modification.
   */
  public void test_commitChanges() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration(
            "very.very.very.long_.package_.name",
            "Test.java",
            getSourceDQ("package very.very.very.long_.package_.name;", "public class Test {", "}"));
    SimpleName nameNode = typeDeclaration.getName();
    //
    m_lastEditor.replaceSubstring(nameNode.getStartPosition(), nameNode.getLength(), "FooBar");
    String expected =
        getSourceDQ("package very.very.very.long_.package_.name;", "public class FooBar {", "}");
    assertEquals(expected, m_lastEditor.getSource(typeDeclaration.getParent()));
    // is not changed CU until commitChanges()
    assertFalse(expected.equals(m_lastModelUnit.getBuffer().getContents()));
    assertTrue(m_lastModelUnit.isConsistent());
    // now we should have same source in CU
    m_lastEditor.commitChanges();
    assertEquals(expected, m_lastModelUnit.getBuffer().getContents());
    assertFalse(m_lastModelUnit.isConsistent());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // saveChanges()
  //
  ////////////////////////////////////////////////////////////////////////////
  private void saveChanges_assertSaved(String expected, boolean bufferEquals, boolean fileEquals)
      throws JavaModelException {
    String unitContent = m_lastModelUnit.getBuffer().getContents();
    String fileContent = getFileContentSrc("test/Test.java");
    if (bufferEquals) {
      assertThat(unitContent).isEqualTo(expected);
    } else {
      assertThat(unitContent).isNotEqualTo(expected);
    }
    if (fileEquals) {
      assertThat(fileContent).isEqualTo(expected);
    } else {
      assertThat(fileContent).isNotEqualTo(expected);
    }
  }

  /**
   * Test for {@link AstEditor#saveChanges()}.
   */
  public void test_saveChanges_noEditor() throws Exception {
    createASTCompilationUnit(
        "test",
        "Test.java",
        getSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "package test;",
            "public class Test {",
            "}"));
    TypeDeclaration typeDeclaration = m_lastEditor.getPrimaryType();
    //
    m_lastEditor.addFieldDeclaration("int f;", new BodyDeclarationTarget(typeDeclaration, false));
    String expected =
        getSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "package test;",
            "public class Test {",
            "  int f;",
            "}");
    // not saved yet
    saveChanges_assertSaved(expected, false, false);
    // do save
    m_lastEditor.saveChanges(false);
    saveChanges_assertSaved(expected, true, true);
    assertTrue(m_lastModelUnit.isConsistent());
  }

  /**
   * Test for {@link AstEditor#saveChanges()}.
   * <p>
   * This {@link ICompilationUnit} is opened in Java editor, so is not saved.
   */
  public void test_saveChanges_openedEditor() throws Exception {
    createASTCompilationUnit(
        "test",
        "Test.java",
        getSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "package test;",
            "public class Test {",
            "}"));
    TypeDeclaration typeDeclaration = m_lastEditor.getPrimaryType();
    // open in Java editor
    IDE.openEditor(DesignerPlugin.getActivePage(), getFileSrc("test/Test.java"));
    // modify ASTEditor
    m_lastEditor.addFieldDeclaration("int f;", new BodyDeclarationTarget(typeDeclaration, false));
    String expected =
        getSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "package test;",
            "public class Test {",
            "  int f;",
            "}");
    // not saved yet
    saveChanges_assertSaved(expected, false, false);
    // do save - not forced, so ignored
    m_lastEditor.saveChanges(false);
    saveChanges_assertSaved(expected, true, false);
    // force save
    m_lastEditor.saveChanges(true);
    saveChanges_assertSaved(expected, true, true);
    assertTrue(m_lastModelUnit.isConsistent());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // indexOfCharBackward()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstEditor#indexOfCharBackward(char, int)}.
   */
  public void test_indexOfCharBackward() throws Exception {
    createTypeDeclaration_Test(
        "// filler filler filler",
        "public class Test {",
        "  // filler filler filler filler",
        "}");
    assertEquals(1, m_lastEditor.indexOfCharBackward('a', 2));
    assertEquals(4, m_lastEditor.indexOfCharBackward('a', 6));
    try {
      m_lastEditor.indexOfCharBackward('z', 6);
      fail();
    } catch (IllegalArgumentException e) {
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getSource
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getSource() throws Exception {
    TypeDeclaration typeDeclaration;
    {
      String code = "private int m_value = 12345;";
      typeDeclaration = createTypeDeclaration_TestC(code);
    }
    // prepare field
    FieldDeclaration fieldDeclaration = typeDeclaration.getFields()[0];
    VariableDeclarationFragment declarationFragment =
        (VariableDeclarationFragment) fieldDeclaration.fragments().get(0);
    ASTNode nameNode = declarationFragment.getName();
    //
    assertEquals(
        "m_value",
        m_lastEditor.getSource(nameNode.getStartPosition(), nameNode.getLength()));
    assertEquals("m_value", m_lastEditor.getSource(nameNode));
    assertEquals(
        "m_value",
        m_lastEditor.getSourceBeginEnd(
            AstNodeUtils.getSourceBegin(nameNode),
            AstNodeUtils.getSourceEnd(nameNode)));
    // check for bad location
    try {
      m_lastEditor.getSource(-1, 1);
      fail();
    } catch (Throwable e) {
      assertThat(e).isExactlyInstanceOf(BadLocationException.class);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setSource
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstEditor#setSource(String)}.
   */
  public void test_setSource() throws Exception {
    createASTCompilationUnit(
        "test",
        "Test.java",
        getSourceDQ(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "package test;",
            "public class Test {",
            "  private int m_value = 12345;",
            "}"));
    //
    String newSource = m_lastEditor.getSource();
    newSource = StringUtils.replace(newSource, "12345", "23456");
    m_lastEditor.setSource(newSource);
    // has new source
    assertEquals(newSource, m_lastEditor.getSource());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getExternalSource()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstEditor#getExternalSource(ASTNode)}.
   */
  public void test_getExternalSource() throws Exception {
    createTypeDeclaration(
        "test",
        "Constants.java",
        getSourceDQ(
            "package test;",
            "public class Constants {",
            "  public static final int SIZE = 10;",
            "}"));
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "",
            "import java.util.*;",
            "",
            "public class Test {",
            "  int f_0 = 12345;",
            "  Object f_1 = new ArrayList();",
            "  Object f_2 = new ArrayList(Constants.SIZE);",
            "  Object f_3 = Collections.emptyList();",
            "  Object f_4 = List.class;",
            "}");
    waitForAutoBuild();
    // check source for field initializers
    FieldDeclaration[] fields = typeDeclaration.getFields();
    assertEquals("12345", getExternalSource(fields[0], null));
    assertEquals("new java.util.ArrayList()", getExternalSource(fields[1], null));
    assertEquals("new java.util.ArrayList(test.Constants.SIZE)", getExternalSource(fields[2], null));
    assertEquals(
        "new java.util.ArrayList(sizeParameter)",
        getExternalSource(fields[2], new Function<ASTNode, String>() {
          public String apply(ASTNode from) {
            if (m_lastEditor.getSource(from).equals("Constants.SIZE")) {
              return "sizeParameter";
            }
            return null;
          }
        }));
    assertEquals("java.util.Collections.emptyList()", getExternalSource(fields[3], null));
    assertEquals("java.util.List.class", getExternalSource(fields[4], null));
  }

  /**
   * @return the result of {@link AstEditor#getExternalSource(ASTNode)} for sole initializer of
   *         given {@link FieldDeclaration}.
   */
  private String getExternalSource(FieldDeclaration fieldDeclaration,
      Function<ASTNode, String> transformer) {
    Expression initializer = DomGenerics.fragments(fieldDeclaration).get(0).getInitializer();
    return m_lastEditor.getExternalSource(initializer, transformer);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getTypeBindingSource(ITypeBinding)
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstEditor#getTypeBindingSource(ITypeBinding)}.
   */
  public void test_getTypeBindingSource() throws Exception {
    createTypeDeclaration_Test(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "import java.util.List;",
        "import java.util.Map;",
        "public class Test {",
        "  private List<String> field_1;",
        "  private Map<Double, List<String>> field_2;",
        "  public Test() {",
        "  }",
        "}");
    // field_1
    {
      FieldDeclaration field = getNode("field_1", FieldDeclaration.class);
      ITypeBinding typeBinding = field.getType().resolveBinding();
      assertEquals(
          "java.util.List<java.lang.String>",
          m_lastEditor.getTypeBindingSource(typeBinding));
    }
    // field_2
    {
      FieldDeclaration field = getNode("field_2", FieldDeclaration.class);
      ITypeBinding typeBinding = field.getType().resolveBinding();
      assertEquals(
          "java.util.Map<java.lang.Double, java.util.List<java.lang.String>>",
          m_lastEditor.getTypeBindingSource(typeBinding));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // indexOf()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstEditor#indexOf(String)}.
   */
  public void test_indexOf() throws Exception {
    createTypeDeclaration_Test(
        "public class Test {",
        "  public Test() {",
        "    int a;",
        "    int b;",
        "  }",
        "}");
    {
      int index = m_lastEditor.indexOf("int a");
      assertEquals("int a", m_lastEditor.getSource(index, 5));
    }
    {
      int index = m_lastEditor.indexOf("int b");
      assertEquals("int b", m_lastEditor.getSource(index, 5));
    }
    try {
      m_lastEditor.indexOf("noSuchString");
      fail();
    } catch (IllegalArgumentException e) {
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getCharAt()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstEditor#getChar(int)}.
   */
  public void test_getCharAt() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "// filler filler filler",
            "// filler filler filler",
            "class Test {",
            "  int a;",
            "}");
    FieldDeclaration fieldDeclaration = typeDeclaration.getFields()[0];
    int position = AstNodeUtils.getSourceBegin(fieldDeclaration);
    // good positions
    assertEquals('i', m_lastEditor.getChar(position + 0));
    assertEquals('n', m_lastEditor.getChar(position + 1));
    assertEquals('t', m_lastEditor.getChar(position + 2));
    // bad position
    try {
      m_lastEditor.getChar(-1);
      fail();
    } catch (Throwable e) {
      assertThat(e).isExactlyInstanceOf(BadLocationException.class);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getWhitespaceToLeft
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getWhitespaceToLeft() throws Exception {
    TypeDeclaration typeDeclaration;
    {
      String code = "\t\t\r\t\t\n\t\tprivate int m_value = 12345;";
      typeDeclaration = createTypeDeclaration_TestC(code);
    }
    // prepare field and its position
    FieldDeclaration fieldDeclaration = typeDeclaration.getFields()[0];
    int end = fieldDeclaration.getStartPosition();
    // do checks
    assertEquals("", m_lastEditor.getWhitespaceToLeft(end + 1, false));
    assertEquals("\t\t", m_lastEditor.getWhitespaceToLeft(end, false));
    assertEquals("\t\t\r\t\t\n\t\t", m_lastEditor.getWhitespaceToLeft(end, true));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getLineNumber
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getLineNumber() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "// filler filler filler",
            "// filler filler filler",
            "class Test {",
            "  int a;",
            "}");
    FieldDeclaration fieldDeclaration = typeDeclaration.getFields()[0];
    assertEquals(4, m_lastEditor.getLineNumber(fieldDeclaration.getStartPosition()));
  }

  public void test_getLineNumber_badPosition() throws Exception {
    createTypeDeclaration_Test(
        "// filler filler filler",
        "// filler filler filler",
        "class Test {",
        "  int a;",
        "}");
    try {
      m_lastEditor.getLineNumber(-1);
      fail();
    } catch (Throwable e) {
      assertThat(e).isExactlyInstanceOf(BadLocationException.class);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getLineBegin
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstEditor#getLineBegin(int)}.
   */
  public void test_getLineBegin() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "import java.awt.*;",
            "import javax.swing.*;",
            "class Test extends JPanel {",
            "  int a;",
            "}");
    // prepare field and its position
    FieldDeclaration fieldDeclaration = typeDeclaration.getFields()[0];
    int begin = AstNodeUtils.getSourceBegin(fieldDeclaration);
    int end = AstNodeUtils.getSourceEnd(fieldDeclaration);
    int lineBegin = begin - 1;
    // do checks
    assertEquals(lineBegin, m_lastEditor.getLineBegin(begin));
    assertEquals(lineBegin, m_lastEditor.getLineBegin(begin + 1));
    assertEquals(lineBegin, m_lastEditor.getLineBegin(end));
    // bad location
    try {
      m_lastEditor.getLineBegin(-1);
      fail();
    } catch (Throwable e) {
      assertThat(e).isExactlyInstanceOf(BadLocationException.class);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getLineEnd
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstEditor#getLineEnd(int)}
   */
  public void test_getLineEnd() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "import java.awt.*;",
            "import javax.swing.*;",
            "class Test extends JPanel {",
            "  int a;",
            "}");
    // prepare field and its position
    FieldDeclaration fieldDeclaration = typeDeclaration.getFields()[0];
    int begin = AstNodeUtils.getSourceBegin(fieldDeclaration);
    int end = AstNodeUtils.getSourceEnd(fieldDeclaration);
    int lineEnd = end;
    // do checks
    assertEquals(lineEnd, m_lastEditor.getLineEnd(begin));
    assertEquals(lineEnd, m_lastEditor.getLineEnd(begin + 1));
    assertEquals(lineEnd, m_lastEditor.getLineEnd(end - 1));
    assertEquals(lineEnd, m_lastEditor.getLineEnd(end));
    // bad location
    try {
      m_lastEditor.getLineEnd(-1);
      fail();
    } catch (Throwable e) {
      assertThat(e).isExactlyInstanceOf(BadLocationException.class);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // skipWhitespaceAndPureEOLCToLeft
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_skipWhitespaceAndPureEOLCToLeft_1() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "import java.awt.*;",
            "import javax.swing.*;",
            "class Test extends JPanel {",
            "  int a;",
            "  // 111",
            "  ",
            "  // 222",
            "  int b;",
            "  int c;  int d;",
            "}");
    FieldDeclaration[] fields = typeDeclaration.getFields();
    // do checks
    assertEquals(
        AstNodeUtils.getSourceEnd(fields[0]) + "\n".length(),
        m_lastEditor.skipWhitespaceAndPureEOLCToLeft(AstNodeUtils.getSourceBegin(fields[1])));
    assertEquals(
        AstNodeUtils.getSourceEnd(fields[2]),
        m_lastEditor.skipWhitespaceAndPureEOLCToLeft(AstNodeUtils.getSourceBegin(fields[3])));
    assertEquals(
        0,
        m_lastEditor.skipWhitespaceAndPureEOLCToLeft(AstNodeUtils.getSourceBegin(m_lastEditor.getAstUnit().getPackage())));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // skipSingleEOLToLeft
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_skipSingleEOLToLeft_1() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "// filler filler filler",
            "// filler filler filler",
            "class Test {",
            "\r\n\r\nint b;",
            "}");
    FieldDeclaration fieldDeclaration = typeDeclaration.getFields()[0];
    int index = fieldDeclaration.getStartPosition();
    // no EOL to left
    assertEquals(index + 1, m_lastEditor.skipSingleEOLToLeft(index + 1));
    // skip only single EOL, but this is \r\n, so 2 character
    assertEquals(index - 2, m_lastEditor.skipSingleEOLToLeft(index));
  }

  public void test_skipSingleEOLToLeft_2() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "// filler filler filler",
            "// filler filler filler",
            "class Test {",
            "\n\nint b;",
            "}");
    FieldDeclaration fieldDeclaration = typeDeclaration.getFields()[0];
    int index = fieldDeclaration.getStartPosition();
    // skip only single EOL - \n, so 1 character
    assertEquals(index - 1, m_lastEditor.skipSingleEOLToLeft(index));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // addFieldDeclaration
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test bad cases.
   */
  public void test_addFieldDeclaration_0_bad() throws Exception {
    createTypeDeclaration_TestC("");
    // 'null' as type and 'null' as bodyDeclaration
    try {
      m_lastEditor.addFieldDeclaration("", new BodyDeclarationTarget(null, null, false));
      fail();
    } catch (AssertionFailedException e) {
    }
  }

  private static final String FIELD_INIT = getSourceDQ(
      "package test;",
      "  class Test {",
      "    int m_value0123456789;",
      "  }");
  private static final String FIELD_BEFORE = getSourceDQ(
      "package test;",
      "  class Test {",
      "    int abc;",
      "    int m_value0123456789;",
      "  }");
  private static final String FIELD_AFTER = getSourceDQ(
      "package test;",
      "  class Test {",
      "    int m_value0123456789;",
      "    int abc;",
      "  }");

  public void test_addFieldDeclaration_1_body_before() throws Exception {
    check_addFieldDeclaration(FIELD_INIT, false, true, FIELD_BEFORE);
  }

  public void test_addFieldDeclaration_2_body_after() throws Exception {
    check_addFieldDeclaration(FIELD_INIT, false, false, FIELD_AFTER);
  }

  public void test_addFieldDeclaration_3_type_before() throws Exception {
    check_addFieldDeclaration(FIELD_INIT, true, true, FIELD_BEFORE);
  }

  public void test_addFieldDeclaration_4_type_after() throws Exception {
    check_addFieldDeclaration(FIELD_INIT, true, false, FIELD_AFTER);
  }

  public void test_addFieldDeclaration_5_emptyLines_before() throws Exception {
    check_addFieldDeclaration(
        getSourceDQ("package test;", "class Test {", "  ", "  ", "  int m_value0123456789;", "}"),
        false,
        true,
        getSourceDQ(
            "package test;",
            "class Test {",
            "  int abc;",
            "  ",
            "  ",
            "  int m_value0123456789;",
            "}"));
  }

  public void test_addFieldDeclaration_5_emptyLines_after() throws Exception {
    check_addFieldDeclaration(
        getSourceDQ("package test;", "class Test {", "  ", "  ", "  int m_value0123456789;", "}"),
        false,
        false,
        getSourceDQ(
            "package test;",
            "class Test {",
            "  ",
            "  ",
            "  int m_value0123456789;",
            "  int abc;",
            "}"));
  }

  public void test_addFieldDeclaration_6_javaDocComment_before() throws Exception {
    check_addFieldDeclaration(
        getSourceDQ(
            "package test;",
            "class Test {",
            "  /**",
            "  * The comment.",
            "  */",
            "  int m_value0123456789;",
            "}"),
        false,
        true,
        getSourceDQ(
            "package test;",
            "class Test {",
            "  int abc;",
            "  /**",
            "  * The comment.",
            "  */",
            "  int m_value0123456789;",
            "}"));
  }

  public void test_addFieldDeclaration_6_afterWithEOLComment() throws Exception {
    check_addFieldDeclaration(
        getSourceDQ("package test;", "class Test {", "  int m_value0123456789; // comment", "}"),
        false,
        false,
        getSourceDQ(
            "package test;",
            "class Test {",
            "  int m_value0123456789; // comment",
            "  int abc;",
            "}"));
  }

  /**
   * Checks {@link AstEditor#addFieldDeclaration(String, BodyDeclarationTarget)}.
   * 
   * @param relativeToType
   *          is <code>true</code> if new field should be added relative to type, is
   *          <code>false</code> if relative to field.
   */
  public void check_addFieldDeclaration(String initialSource,
      boolean relativeToType,
      boolean before,
      String expectedSource) throws Exception {
    TypeDeclaration typeDeclaration = createTypeDeclaration("test", "Test.java", initialSource);
    FieldDeclaration targetField = typeDeclaration.getFields()[0];
    //
    BodyDeclarationTarget target =
        new BodyDeclarationTarget(relativeToType ? typeDeclaration : null, relativeToType
            ? null
            : targetField, before);
    FieldDeclaration newField = m_lastEditor.addFieldDeclaration("int abc;", target);
    assertAST(m_lastEditor);
    assertNotNull(newField);
    assertNotNull(newField.getType());
    assertNotNull(AstNodeUtils.getTypeBinding(newField.getType()));
    assertEquals(before ? 0 : 1, typeDeclaration.bodyDeclarations().indexOf(newField));
    assertEquals(before ? 1 : 0, typeDeclaration.bodyDeclarations().indexOf(targetField));
    assertEquals(expectedSource, m_lastEditor.getSource());
  }

  public void test_addFieldDeclaration_inAnonymousType() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "import javax.swing.*;",
            "import java.awt.event.*;",
            "class Test extends JPanel {",
            "  Test() {",
            "    addKeyListener(new KeyAdapter() {",
            "    });",
            "  }",
            "}");
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    ExpressionStatement expressionStatement =
        (ExpressionStatement) methodDeclaration.getBody().statements().get(0);
    MethodInvocation invocation = (MethodInvocation) expressionStatement.getExpression();
    ClassInstanceCreation creation = (ClassInstanceCreation) invocation.arguments().get(0);
    TypeDeclaration anonymousTypeDeclaration =
        AnonymousTypeDeclaration.create(creation.getAnonymousClassDeclaration());
    int anonymousTypeLength = anonymousTypeDeclaration.getLength();
    //
    BodyDeclarationTarget target = new BodyDeclarationTarget(anonymousTypeDeclaration, null, false);
    FieldDeclaration newField = m_lastEditor.addFieldDeclaration("int m_value;", target);
    assertNotNull(newField);
    assertEditor(
        getSourceDQ(
            "package test;",
            "import javax.swing.*;",
            "import java.awt.event.*;",
            "class Test extends JPanel {",
            "  Test() {",
            "    addKeyListener(new KeyAdapter() {",
            "      int m_value;",
            "    });",
            "  }",
            "}"),
        m_lastEditor);
    assertTrue(anonymousTypeDeclaration.getLength() > anonymousTypeLength);
  }

  /**
   * If we don't see full context - this should not be problem.
   */
  public void test_addFieldDeclaration_interfaceMethods() throws Exception {
    setFileContentSrc(
        "test/MyInterface.java",
        getSource("package test;", "public interface MyInterface {", "  void someMethod();", "}"));
    waitForAutoBuild();
    //
    m_ignoreModelCompileProblems = true;
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test implements MyInterface {",
            "  //$hide>>$",
            "  public void someMethod() {",
            "  }",
            "  //$hide<<$",
            "}");
    m_lastEditor.addFieldDeclaration("int field;", new BodyDeclarationTarget(typeDeclaration, true));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // addMethodDeclaration
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_addMethodDeclaration_0() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "// filler filler filler",
            "class Test {",
            "  int m_value0123456789;",
            "}");
    FieldDeclaration targetField = typeDeclaration.getFields()[0];
    //
    BodyDeclarationTarget target = new BodyDeclarationTarget(null, targetField, true);
    MethodDeclaration newMethod =
        m_lastEditor.addMethodDeclaration("int foo()", ImmutableList.of("return 0;"), target);
    assertAST(m_lastEditor);
    assertNotNull(newMethod);
    assertEquals(0, typeDeclaration.bodyDeclarations().indexOf(newMethod));
    assertEquals(1, typeDeclaration.bodyDeclarations().indexOf(targetField));
    assertEditor(
        getSourceDQ(
            "package test;",
            "// filler filler filler",
            "class Test {",
            "  int foo() {",
            "    return 0;",
            "  }",
            "  int m_value0123456789;",
            "}"),
        m_lastEditor);
  }

  public void test_addMethodDeclaration_danglingJavadoc() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "// filler filler filler",
            "// filler filler filler",
            "// filler filler filler",
            "class Test {",
            "  /** dangling */",
            "}");
    BodyDeclarationTarget target = new BodyDeclarationTarget(typeDeclaration, null, false);
    //
    m_lastEditor.addMethodDeclaration("int foo()", ImmutableList.of("return 0;"), target);
    assertEditor(
        getSource(
            "package test;",
            "// filler filler filler",
            "// filler filler filler",
            "// filler filler filler",
            "class Test {",
            "  int foo() {",
            "    return 0;",
            "  }",
            "}"),
        m_lastEditor);
  }

  public void test_addMethodDeclaration_withEmptyLine() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "// filler filler filler",
            "class Test {",
            "  int m_value0123456789;",
            "}");
    FieldDeclaration targetField = typeDeclaration.getFields()[0];
    //
    BodyDeclarationTarget target = new BodyDeclarationTarget(null, targetField, true);
    MethodDeclaration newMethod =
        m_lastEditor.addMethodDeclaration("int foo()", ImmutableList.of("\t", "return 0;"), target);
    assertAST(m_lastEditor);
    assertNotNull(newMethod);
    assertEquals(0, typeDeclaration.bodyDeclarations().indexOf(newMethod));
    assertEquals(1, typeDeclaration.bodyDeclarations().indexOf(targetField));
    assertEditor(
        getSourceDQ(
            "package test;",
            "// filler filler filler",
            "class Test {",
            "  int foo() {",
            "      ",
            "    return 0;",
            "  }",
            "  int m_value0123456789;",
            "}"),
        m_lastEditor);
  }

  public void test_addMethodDeclaration_withParameters() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "// filler filler filler",
            "// filler filler filler",
            "class Test {",
            "}");
    BodyDeclarationTarget target = new BodyDeclarationTarget(typeDeclaration, false);
    MethodDeclaration newMethod =
        m_lastEditor.addMethodDeclaration(
            "void foo(int a, String b, String[] c)",
            ImmutableList.<String>of(),
            target);
    assertAST(m_lastEditor);
    assertNotNull(newMethod);
    assertEquals(0, typeDeclaration.bodyDeclarations().indexOf(newMethod));
    assertEditor(
        getSourceDQ(
            "package test;",
            "// filler filler filler",
            "// filler filler filler",
            "class Test {",
            "  void foo(int a, String b, String[] c) {",
            "  }",
            "}"),
        m_lastEditor);
  }

  public void test_addMethodDeclaration_withAnnotations() throws Exception {
    setFileContentSrc(
        "test/Test_0.java",
        getSource("package test;", "public class Test_0 {", "  public void fooBar() {", "  }", "}"));
    TypeDeclaration typeDeclaration =
        createTypeDeclaration(
            "test",
            "Test.java",
            getSource("package test;", "public class Test extends Test_0 {", "  // filler", "}"));
    //
    BodyDeclarationTarget target = new BodyDeclarationTarget(typeDeclaration, true);
    MethodDeclaration newMethod =
        m_lastEditor.addMethodDeclaration(
            ImmutableList.<String>of("@Override"),
            "public void fooBar()",
            ImmutableList.<String>of(),
            target);
    assertNotNull(newMethod);
    assertEquals(0, typeDeclaration.bodyDeclarations().indexOf(newMethod));
    assertEditor(
        getSourceDQ(
            "package test;",
            "public class Test extends Test_0 {",
            "  @Override",
            "  public void fooBar() {",
            "  }",
            "  // filler",
            "}"),
        m_lastEditor);
  }

  /**
   * Test for {@link AstEditor#addInterfaceMethodDeclaration(String, BodyDeclarationTarget)}.
   */
  public void test_addInterfaceMethodDeclaration() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "// filler filler filler",
            "public interface Test {",
            "  int FILLER = 0;",
            "}");
    //
    BodyDeclarationTarget target = new BodyDeclarationTarget(typeDeclaration, false);
    MethodDeclaration newMethod = m_lastEditor.addInterfaceMethodDeclaration("int foo()", target);
    assertAST(m_lastEditor);
    assertNotNull(newMethod);
    assertEquals(1, typeDeclaration.bodyDeclarations().indexOf(newMethod));
    assertEditor(
        getSourceDQ(
            "package test;",
            "// filler filler filler",
            "public interface Test {",
            "  int FILLER = 0;",
            "  int foo();",
            "}"),
        m_lastEditor);
  }

  /**
   * Keep static modifier of inner classes (if any) while adding method declaration to outer type.
   */
  public void test_ASTParser_addMethodDeclaration_innerTypeStatic() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  private static class Inner {",
            "    public static void someMethod() {",
            "    }",
            "  }",
            "}");
    m_lastEditor.addMethodDeclaration(
        "void testMethod()",
        ImmutableList.<String>of(),
        new BodyDeclarationTarget(typeDeclaration, false));
  }

  /**
   * There was problem with removing visibility modifier when target type has {@link Javadoc}.
   */
  public void test_ASTParser_parseBodyDeclaration_targetTypeWithJavadoc() throws Exception {
    createTypeDeclaration_Test(
        "// filler filler filler",
        "/**",
        "  Some comment.",
        "*/",
        "public class Test {",
        "  // marker",
        "}");
    int position = m_lastEditor.indexOf("// marker");
    String source = "void foo() {}";
    check_ASTParser_parseBodyDeclaration(position, source);
  }

  /**
   * We use inner type as context, test that its "private" does not prevent parsing.
   */
  public void test_ASTParser_parseBodyDeclaration_intoPrivateInnerType() throws Exception {
    createTypeDeclaration_Test(
        "public class Test {",
        "  private static class Inner {",
        "    // marker",
        "  }",
        "}");
    int position = m_lastEditor.indexOf("// marker");
    String source = "void foo() {}";
    check_ASTParser_parseBodyDeclaration(position, source);
  }

  /**
   * We use inner type as context, test that its "private" does not prevent parsing.
   */
  public void test_ASTParser_parseBodyDeclaration_intoProtectedInnerType() throws Exception {
    createTypeDeclaration_Test(
        "public class Test {",
        "  protected static class Inner {",
        "    // marker",
        "  }",
        "}");
    int position = m_lastEditor.indexOf("// marker");
    String source = "void foo() {}";
    check_ASTParser_parseBodyDeclaration(position, source);
  }

  /**
   * From Java point of view all is OK - we should add {@link MethodDeclaration} into
   * {@link AnonymousClassDeclaration}. But in the past we in reality parsed it in "main"
   * {@link TypeDeclaration}, so this caused compilation error and no bindings.
   * <p>
   * Solution: parse full {@link CompilationUnit} with inserted {@link MethodDeclaration}. This is
   * slower, but more reliable.
   */
  public void test_ASTParser_parseBodyDeclaration_intoAnonymous_whenTopLevelHasSameMethod()
      throws Exception {
    createTypeDeclaration_Test(
        "public class Test {",
        "  public Test() {",
        "    new Object() {",
        "      // marker",
        "    };",
        "  }",
        "  private void foo() {",
        "  }",
        "}");
    int position = m_lastEditor.indexOf("// marker");
    String source = "void foo() {}";
    check_ASTParser_parseBodyDeclaration(position, source);
  }

  private void check_ASTParser_parseBodyDeclaration(int position, String source) throws Exception {
    BodyDeclaration declaration = m_lastEditor.getParser().parseBodyDeclaration(position, source);
    assertNotNull(declaration);
    assertEquals(position, declaration.getStartPosition());
    assertEquals(source.length(), declaration.getLength());
  }

  /**
   * Add enums declared in current CU and used in methods.
   */
  public void test_ASTParser_addMethodDeclaration_Enums_declared() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  private enum TestEnum {",
            "    C1,",
            "    C2;",
            "    TestEnum(){}",
            "    void someEnumMethod(){}",
            "  }",
            "  private final TestEnum m_testEnum = TestEnum.C1;",
            "  void someTestMethod1(TestEnum testEnum){}",
            "}");
    m_lastEditor.addMethodDeclaration(
        "void someTestMethod2(TestEnum testEnum)",
        ImmutableList.<String>of(),
        new BodyDeclarationTarget(typeDeclaration, false));
  }

  /**
   * Test that {@link ICoreExceptionConstants#AST_PARSE_ERROR} includes source.
   */
  public void test_ASTParser_parseBodyDeclaration_parseError() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration(
            "test",
            "Test.java",
            getSource(
                "// filler filler filler filler filler",
                "// filler filler filler filler filler",
                "package test;",
                "public class Test {",
                "  public Test() {",
                "  }",
                "}"));
    //
    try {
      m_lastEditor.addMethodDeclaration(
          "void foo()",
          ImmutableList.of("somethingBadA();", "somethingBadB();"),
          new BodyDeclarationTarget(typeDeclaration, false));
      fail();
    } catch (Throwable e) {
      // "parse error" with Statement source
      DesignerException methodDE = DesignerExceptionUtils.getDesignerException(e);
      assertEquals(ICoreExceptionConstants.AST_PARSE_ERROR, methodDE.getCode());
      {
        String[] parameters = methodDE.getParameters();
        assertThat(parameters).hasSize(2);
        {
          String source = parameters[0];
          assertThat(source).doesNotContain("class Test");
          assertThat(source).contains("void foo() {");
          assertThat(source).contains("somethingBadA();");
          assertThat(source).contains("somethingBadB();");
        }
        {
          String problems = parameters[1];
          assertThat(problems).contains("The method somethingBadA() is undefined for the type Test");
          assertThat(problems).contains("The method somethingBadB() is undefined for the type Test");
        }
      }
      // ASTNode "parse error" is cause
      DesignerException nodeDE = (DesignerException) methodDE.getCause();
      assertEquals(ICoreExceptionConstants.AST_PARSE_ERROR, nodeDE.getCode());
      {
        String[] parameters = nodeDE.getParameters();
        assertThat(parameters).hasSize(2);
        {
          String source = parameters[0];
          assertThat(source).contains("class Test");
          assertThat(source).contains("void foo() {");
          assertThat(source).contains("somethingBadA();");
          assertThat(source).contains("somethingBadB();");
        }
      }
    }
  }

  /**
   * Add anonymous type instance with initializer into parsed source code.
   */
  public void test_ASTParser_addStatement_anonymousInitializer() throws Exception {
    createTypeDeclaration_Test(
        "import java.util.ArrayList;",
        "public class Test {",
        "  public Test() {",
        "    new ArrayList<Object>() {",
        "      {",
        "        // initializer",
        "      }",
        "    };",
        "  }",
        "}");
    int position = m_lastEditor.indexOf("// initializer");
    String source = "ensureCapacity(77);";
    Statement statement = m_lastEditor.getParser().parseStatement(position, source);
    // check statement
    assertNotNull(statement);
    assertEquals(position, statement.getStartPosition());
    assertEquals(source.length(), statement.getLength());
    // check bindings
    Expression expression = ((ExpressionStatement) statement).getExpression();
    IMethodBinding methodBinding = AstNodeUtils.getMethodBinding((MethodInvocation) expression);
    assertEquals("ensureCapacity", methodBinding.getName());
    ITypeBinding typeBinding = methodBinding.getDeclaringClass();
    assertEquals("java.util.ArrayList", AstNodeUtils.getFullyQualifiedName(typeBinding, false));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getParametersSource/getParameterNames
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getParametersSource() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "class Test {",
            "  int getSum(String s, double d) {",
            "    return 0;",
            "  }",
            "}");
    MethodDeclaration method = typeDeclaration.getMethods()[0];
    assertEquals("String s, double d", m_lastEditor.getParametersSource(method));
  }

  public void test_getParameterNames() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "class Test {",
            "  int getSum(String s, double d) {",
            "    return 0;",
            "  }",
            "}");
    MethodDeclaration method = typeDeclaration.getMethods()[0];
    assertTrue(ArrayUtils.isEquals(new String[]{"s", "d"}, m_lastEditor.getParameterNames(method)));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // replaceMethodName()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstEditor#replaceMethodName(MethodDeclaration, String)}.
   */
  public void test_replaceMethodName_returnVoid() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "// filler filler filler",
            "class Test {",
            "  public void foo(int a) {",
            "  }",
            "}");
    MethodDeclaration method = typeDeclaration.getMethods()[0];
    m_lastEditor.replaceMethodName(method, "barBaz");
    assertEditor(
        getSourceDQ(
            "package test;",
            "// filler filler filler",
            "class Test {",
            "  public void barBaz(int a) {",
            "  }",
            "}"),
        m_lastEditor);
  }

  /**
   * Test for {@link AstEditor#replaceMethodName(MethodDeclaration, String)}.
   */
  public void test_replaceMethodName_returnObject() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "class Test {",
            "  public String foo(int a) {",
            "    return null;",
            "  }",
            "}");
    MethodDeclaration method = typeDeclaration.getMethods()[0];
    m_lastEditor.replaceMethodName(method, "barBaz");
    assertEditor(
        getSourceDQ(
            "package test;",
            "class Test {",
            "  public String barBaz(int a) {",
            "    return null;",
            "  }",
            "}"),
        m_lastEditor);
  }

  /**
   * Test for {@link AstEditor#replaceMethodName(MethodDeclaration, String)}.
   */
  public void test_replaceMethodName_returnPrimitive() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "class Test {",
            "  public int foo(int a) {",
            "    return 0;",
            "  }",
            "}");
    MethodDeclaration method = typeDeclaration.getMethods()[0];
    m_lastEditor.replaceMethodName(method, "barBaz");
    assertEditor(
        getSourceDQ(
            "package test;",
            "class Test {",
            "  public int barBaz(int a) {",
            "    return 0;",
            "  }",
            "}"),
        m_lastEditor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // replaceMethodType()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstEditor#replaceMethodType(MethodDeclaration, String)}.
   */
  public void test_replaceMethodType_int() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "class Test {",
            "  public long foo() {",
            "    return 0;",
            "  }",
            "}");
    MethodDeclaration method = typeDeclaration.getMethods()[0];
    m_lastEditor.replaceMethodType(method, "int");
    assertEditor(
        getSourceDQ(
            "package test;",
            "class Test {",
            "  public int foo() {",
            "    return 0;",
            "  }",
            "}"),
        m_lastEditor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // addTypeDeclaration
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_addTypeDeclaration() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "// filler filler filler",
            "// filler filler filler",
            "class Test {",
            "}");
    //
    BodyDeclarationTarget target = new BodyDeclarationTarget(typeDeclaration, null, true);
    TypeDeclaration newType =
        m_lastEditor.addTypeDeclaration(ImmutableList.of(
            "private class Inner {",
            "\tint a;",
            "\tint getA() {",
            "\t\treturn a;",
            "\t}",
            "}"), target);
    assertAST(m_lastEditor);
    assertNotNull(newType);
    assertEquals(0, typeDeclaration.bodyDeclarations().indexOf(newType));
    assertEquals(
        getSourceDQ(
            "package test;",
            "// filler filler filler",
            "// filler filler filler",
            "class Test {",
            "  private class Inner {",
            "    int a;",
            "    int getA() {",
            "      return a;",
            "    }",
            "  }",
            "}"),
        m_lastEditor.getSource());
    // check ITypeBinding
    {
      ITypeBinding newTypeBinding = AstNodeUtils.getTypeBinding(newType);
      assertEquals("test.Test.Inner", AstNodeUtils.getFullyQualifiedName(newTypeBinding, false));
    }
  }

  /**
   * Test that {@link SuperConstructorInvocation} has {@link IMethodBinding} after parsing.
   */
  public void test_addTypeDeclaration_superCI_binding() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "// filler filler filler",
            "// filler filler filler",
            "import java.util.ArrayList;",
            "class Test {",
            "}");
    //
    BodyDeclarationTarget target = new BodyDeclarationTarget(typeDeclaration, null, true);
    TypeDeclaration newType =
        m_lastEditor.addTypeDeclaration(ImmutableList.of(
            "private class Inner extends ArrayList {",
            "\tInner() {",
            "\t\tsuper(5);",
            "\t}",
            "}"), target);
    assertNotNull(newType);
    assertEquals(0, typeDeclaration.bodyDeclarations().indexOf(newType));
    assertEditor(
        getSourceDQ(
            "package test;",
            "// filler filler filler",
            "// filler filler filler",
            "import java.util.ArrayList;",
            "class Test {",
            "  private class Inner extends ArrayList {",
            "    Inner() {",
            "      super(5);",
            "    }",
            "  }",
            "}"),
        m_lastEditor);
    // check IMethodBinding
    {
      SuperConstructorInvocation invocation = getNode("super(5)", SuperConstructorInvocation.class);
      IMethodBinding binding = AstNodeUtils.getSuperBinding(invocation);
      assertNotNull(binding);
      assertEquals("<init>(int)", AstNodeUtils.getMethodSignature(binding));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ensureInterfaceImplementation
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_ensureInterfaceImplementation_empty() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "// filler filler filler",
            "// filler filler filler",
            "class Test {",
            "}");
    // ensure
    boolean result =
        m_lastEditor.ensureInterfaceImplementation(typeDeclaration, "java.io.Serializable");
    assertTrue(result);
    // check
    assertEditor(
        getSourceDQ(
            "package test;",
            "import java.io.Serializable;",
            "// filler filler filler",
            "// filler filler filler",
            "class Test implements Serializable {",
            "}"),
        m_lastEditor);
    // interface should be included as ASTNode
    {
      List<Type> superInterfaces = DomGenerics.superInterfaces(typeDeclaration);
      assertEquals(1, superInterfaces.size());
      Type superInterface = superInterfaces.get(0);
      assertEquals(
          "java.io.Serializable",
          AstNodeUtils.getFullyQualifiedName(superInterface, false));
    }
    // interface should be included into ITypeBinding of "typeDeclaration"
    {
      ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(typeDeclaration);
      assertTrue(AstNodeUtils.isSuccessorOf(typeBinding, "java.io.Serializable"));
    }
  }

  public void test_ensureInterfaceImplementation_extends() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "// filler filler filler",
            "class Test extends java.util.ArrayList {",
            "}");
    // ensure
    boolean result =
        m_lastEditor.ensureInterfaceImplementation(typeDeclaration, "java.io.Serializable");
    assertTrue(result);
    // check
    assertEditor(
        getSourceDQ(
            "package test;",
            "import java.io.Serializable;",
            "// filler filler filler",
            "class Test extends java.util.ArrayList implements Serializable {",
            "}"),
        m_lastEditor);
    // interface should be included as ASTNode
    {
      List<Type> superInterfaces = DomGenerics.superInterfaces(typeDeclaration);
      assertEquals(1, superInterfaces.size());
      Type superInterface = superInterfaces.get(0);
      assertEquals(
          "java.io.Serializable",
          AstNodeUtils.getFullyQualifiedName(superInterface, false));
    }
    // interface should be included into ITypeBinding of "typeDeclaration"
    {
      ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(typeDeclaration);
      assertTrue(AstNodeUtils.isSuccessorOf(typeBinding, "java.io.Serializable"));
    }
  }

  public void test_ensureInterfaceImplementation_implements() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "class Test implements Comparable {",
            "  public int compareTo(Object o) {",
            "    return 0;",
            "  }",
            "}");
    // ensure
    boolean result =
        m_lastEditor.ensureInterfaceImplementation(typeDeclaration, "java.io.Serializable");
    assertTrue(result);
    // check
    assertEditor(
        getSourceDQ(
            "package test;",
            "import java.io.Serializable;",
            "class Test implements Comparable, Serializable {",
            "  public int compareTo(Object o) {",
            "    return 0;",
            "  }",
            "}"),
        m_lastEditor);
    // interface should be included as ASTNode
    {
      List<Type> superInterfaces = DomGenerics.superInterfaces(typeDeclaration);
      assertEquals(2, superInterfaces.size());
      Type superInterface = superInterfaces.get(1);
      assertEquals(
          "java.io.Serializable",
          AstNodeUtils.getFullyQualifiedName(superInterface, false));
    }
    // interface should be included into ITypeBinding of "typeDeclaration"
    {
      ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(typeDeclaration);
      assertTrue(AstNodeUtils.isSuccessorOf(typeBinding, "java.io.Serializable"));
    }
  }

  public void test_ensureInterfaceImplementation_already() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "// filler filler filler",
            "class Test implements java.io.Serializable {",
            "}");
    // ensure
    boolean result =
        m_lastEditor.ensureInterfaceImplementation(typeDeclaration, "java.io.Serializable");
    assertFalse(result);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ensureThrownException()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstEditor#ensureThrownException(MethodDeclaration, String)}.
   */
  public void test_ensureThrownException_alreadyDeclared_directly() throws Exception {
    createTypeDeclaration_TestD(
        "// filler filler filler filler filler",
        "package test;",
        "public class Test {",
        "  public void foo() throws ClassNotFoundException {",
        "  }",
        "}");
    MethodDeclaration method = getNode("foo()", MethodDeclaration.class);
    //
    m_lastEditor.ensureThrownException(method, "java.lang.ClassNotFoundException");
    assertEditor(
        getSource(
            "// filler filler filler filler filler",
            "package test;",
            "public class Test {",
            "  public void foo() throws ClassNotFoundException {",
            "  }",
            "}"),
        m_lastEditor);
  }

  /**
   * Test for {@link AstEditor#ensureThrownException(MethodDeclaration, String)}.
   */
  public void test_ensureThrownException_alreadyDeclared_super() throws Exception {
    createTypeDeclaration_TestD(
        "// filler filler filler filler filler",
        "package test;",
        "public class Test {",
        "  public void foo() throws Exception {",
        "  }",
        "}");
    MethodDeclaration method = getNode("foo()", MethodDeclaration.class);
    //
    m_lastEditor.ensureThrownException(method, "java.lang.ClassNotFoundException");
    assertEditor(
        getSource(
            "// filler filler filler filler filler",
            "package test;",
            "public class Test {",
            "  public void foo() throws Exception {",
            "  }",
            "}"),
        m_lastEditor);
  }

  /**
   * Test for {@link AstEditor#ensureThrownException(MethodDeclaration, String)}.
   */
  public void test_ensureThrownException_noExisting() throws Exception {
    createTypeDeclaration_TestD(
        "// filler filler filler filler filler",
        "package test;",
        "public class Test {",
        "  public void foo() {",
        "  }",
        "}");
    MethodDeclaration method = getNode("foo()", MethodDeclaration.class);
    //
    m_lastEditor.ensureThrownException(method, "java.lang.ClassNotFoundException");
    assertEditor(
        getSource(
            "// filler filler filler filler filler",
            "package test;",
            "public class Test {",
            "  public void foo() throws ClassNotFoundException {",
            "  }",
            "}"),
        m_lastEditor);
  }

  /**
   * Test for {@link AstEditor#ensureThrownException(MethodDeclaration, String)}.
   */
  public void test_ensureThrownException_afterExisting() throws Exception {
    createTypeDeclaration_TestD(
        "// filler filler filler filler filler",
        "package test;",
        "public class Test {",
        "  public void foo() throws NullPointerException {",
        "  }",
        "}");
    MethodDeclaration method = getNode("foo()", MethodDeclaration.class);
    //
    m_lastEditor.ensureThrownException(method, "java.lang.ClassNotFoundException");
    assertEditor(
        getSource(
            "// filler filler filler filler filler",
            "package test;",
            "public class Test {",
            "  public void foo() throws NullPointerException, ClassNotFoundException {",
            "  }",
            "}"),
        m_lastEditor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // replaceSubstring
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_replaceSubstring_1() throws Exception {
    TypeDeclaration typeDeclaration;
    {
      String code = "private int m_value = 12345;";
      typeDeclaration = createTypeDeclaration_TestC(code);
    }
    // prepare field
    FieldDeclaration fieldDeclaration = typeDeclaration.getFields()[0];
    VariableDeclarationFragment declarationFragment =
        (VariableDeclarationFragment) fieldDeclaration.fragments().get(0);
    ASTNode nameNode = declarationFragment.getName();
    // do change and compare
    String expected =
        "private int m_value2 = 12345; (31 + 29 = 60)\n"
            + "	private (31 + 7 = 38)\n"
            + "	int (39 + 3 = 42)\n"
            + "	m_value2 = 12345 (43 + 16 = 59)\n"
            + "		m_value2 (43 + 8 = 51)\n"
            + "		12345 (54 + 5 = 59)\n";
    m_lastEditor.replaceSubstring(nameNode.getStartPosition(), nameNode.getLength(), "m_value2");
    String actual = getNodesTree(fieldDeclaration);
    assertEquals(expected, actual);
  }

  public void test_replaceSubstring_2() throws Exception {
    TypeDeclaration typeDeclaration;
    {
      String code = "private int m_value = 12345;";
      typeDeclaration = createTypeDeclaration_TestC(code);
    }
    // prepare field
    FieldDeclaration fieldDeclaration = typeDeclaration.getFields()[0];
    VariableDeclarationFragment declarationFragment =
        (VariableDeclarationFragment) fieldDeclaration.fragments().get(0);
    ASTNode nameNode = declarationFragment.getName();
    // replace will fail because we try to start replace inside of node, but end outside
    try {
      m_lastEditor.replaceSubstring(
          nameNode.getStartPosition() + 1,
          nameNode.getLength() + 1,
          "_value2=");
      fail();
    } catch (DesignerException e) {
      assertEquals(ICoreExceptionConstants.AST_EDITOR_REPLACE, e.getCode());
    }
    // replace will fail because we try to start replace outside of node, but end inside
    try {
      m_lastEditor.replaceSubstring(
          nameNode.getStartPosition() - 1,
          nameNode.getLength() - 1,
          "_value3=");
      fail();
    } catch (DesignerException e) {
      assertEquals(ICoreExceptionConstants.AST_EDITOR_REPLACE, e.getCode());
    }
  }

  public void test_replaceSubstring_3() throws Exception {
    TypeDeclaration typeDeclaration;
    {
      String code = "private int m_value = 12345;";
      typeDeclaration = createTypeDeclaration_TestC(code);
    }
    // prepare field
    FieldDeclaration fieldDeclaration = typeDeclaration.getFields()[0];
    VariableDeclarationFragment declarationFragment =
        (VariableDeclarationFragment) fieldDeclaration.fragments().get(0);
    ASTNode nameNode = declarationFragment.getName();
    // do change and compare
    String expected =
        "private int m_value=12345; (31 + 26 = 57)\n"
            + "	private (31 + 7 = 38)\n"
            + "	int (39 + 3 = 42)\n"
            + "	m_value=12345 (43 + 13 = 56)\n"
            + "		m_value (43 + 7 = 50)\n"
            + "		12345 (51 + 5 = 56)\n";
    m_lastEditor.replaceSubstring(nameNode.getStartPosition() + +nameNode.getLength(), 3, "=");
    String actual = getNodesTree(fieldDeclaration);
    assertEquals(expected, actual);
  }

  public void test_replaceSubstring_4() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestD(
            "// filler filler filler",
            "// filler filler filler",
            "class Test {",
            "  int m_value;",
            "}");
    // prepare field
    FieldDeclaration fieldDeclaration = typeDeclaration.getFields()[0];
    VariableDeclarationFragment declarationFragment =
        (VariableDeclarationFragment) fieldDeclaration.fragments().get(0);
    SimpleName nameNode = declarationFragment.getName();
    // do change and compare
    String newName = "myField";
    m_lastEditor.replaceSubstring(nameNode, newName);
    nameNode.setIdentifier(newName);
    assertEditor(
        getSourceDQ(
            "// filler filler filler",
            "// filler filler filler",
            "class Test {",
            "  int myField;",
            "}"),
        m_lastEditor);
  }

  private String getNodesTree(ASTNode root) {
    final StringBuffer buffer = new StringBuffer();
    root.accept(new ASTVisitor() {
      private int m_indent;

      @Override
      public void preVisit(ASTNode node) {
        try {
          buffer.append(StringUtils.repeat("\t", m_indent));
          buffer.append(m_lastEditor.getSource(node));
          buffer.append(" (");
          buffer.append(node.getStartPosition());
          buffer.append(" + ");
          buffer.append(node.getLength());
          buffer.append(" = ");
          buffer.append(node.getStartPosition() + node.getLength());
          buffer.append(")\n");
          m_indent++;
        } catch (Throwable e) {
          throw ReflectionUtils.propagate(e);
        }
      }

      @Override
      public void postVisit(ASTNode node) {
        m_indent--;
      }
    });
    return buffer.toString();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getUniqueVariableName
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Tests for using position, without exclusion.
   */
  public void test_getUniqueVariableName_0() throws Exception {
    createTypeDeclaration_TestC(getSourceDQ(
        "int m_value = 12345;",
        "void foo() {",
        "  int value = 1;",
        "  System.out.println(1);",
        "}",
        "void bar() {",
        "  System.out.println(2);",
        "}"));
    // simple cases
    {
      int position = m_lastEditor.getSource().indexOf("int value");
      assertFalse(position == -1);
      // no "abc" identifier at all
      assertEquals("abc", m_lastEditor.getUniqueVariableName(position, "abc", null));
      // "foo" is not the name of variable
      assertEquals("foo", m_lastEditor.getUniqueVariableName(position, "foo", null));
    }
    // "value" is visible at, so "value_1" should be used
    {
      int position = m_lastEditor.getSource().indexOf("System.out.println(1);");
      assertFalse(position == -1);
      assertEquals("value_1", m_lastEditor.getUniqueVariableName(position, "value", null));
    }
    // at this point "value" is not visible
    {
      int position = m_lastEditor.getSource().indexOf("System.out.println(2);");
      assertFalse(position == -1);
      assertEquals("value", m_lastEditor.getUniqueVariableName(position, "value", null));
    }
    // when ask globally unique variable... 
    {
      // ..."m_value" already visible 
      assertEquals("m_value_1", m_lastEditor.getUniqueVariableName(-1, "m_value", null));
      // ..."value" already visible 
      assertEquals("value_1", m_lastEditor.getUniqueVariableName(-1, "value", null));
    }
  }

  /**
   * Tests for excluding some {@link VariableDeclaration}'s.
   */
  public void test_getUniqueVariableName_1() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestC(getSourceDQ(
            "int m_value;",
            "int m_value2;",
            "void foo() {",
            "  m_value2 = 1;",
            "}"));
    // m_value as base, exclude m_value 
    {
      VariableDeclarationFragment fragment =
          (VariableDeclarationFragment) typeDeclaration.getFields()[0].fragments().get(0);
      assertEquals("m_value", m_lastEditor.getUniqueVariableName(-1, "m_value", fragment));
    }
    // m_value as base, exclude m_value2
    {
      VariableDeclarationFragment fragment =
          (VariableDeclarationFragment) typeDeclaration.getFields()[1].fragments().get(0);
      assertEquals("m_value_1", m_lastEditor.getUniqueVariableName(-1, "m_value", fragment));
    }
  }

  /**
   * Test for shadowing new variable by existing variable in <em>lower</em> nodes.
   */
  public void test_getUniqueVariableName_shadowSameBlock() throws Exception {
    createTypeDeclaration_TestC(getSourceDQ(
        "void foo() {",
        "  System.out.println(1);",
        "  int value;",
        "}"));
    // "value" can not be used because it is already used below
    int position = m_lastEditor.getSource().indexOf("System.out.println(1);");
    assertTrue(position != -1);
    assertEquals("value_1", m_lastEditor.getUniqueVariableName(position, "value", null));
  }

  /**
   * Test for version {@link AstEditor#getUniqueVariableName(List, String)}, i.e. when we give
   * conflicting {@link VariableDeclaration}'s instead of position.
   */
  public void test_getUniqueVariableName_withDeclarations() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  int foo;",
            "  void bar() {",
            "    int baz;",
            "  }",
            "}");
    // if we use "normal" method with position, it uses all variables
    {
      assertEquals("value", m_lastEditor.getUniqueVariableName(-1, "value", null));
      assertEquals("foo_1", m_lastEditor.getUniqueVariableName(-1, "foo", null));
      assertEquals("bar", m_lastEditor.getUniqueVariableName(-1, "bar", null));
      assertEquals("baz_1", m_lastEditor.getUniqueVariableName(-1, "baz", null));
    }
    // if we use "explicit" method with VariableDeclaration's, it uses only given variables
    {
      VariableDeclaration fooDeclaration =
          DomGenerics.fragments(typeDeclaration.getFields()[0]).get(0);
      VariableDeclaration bazDeclaration =
          DomGenerics.fragments(
              (VariableDeclarationStatement) typeDeclaration.getMethods()[0].getBody().statements().get(
                  0)).get(0);
      assertEquals("value", AstEditor.getUniqueVariableName(
          Lists.newArrayList(fooDeclaration, bazDeclaration),
          "value"));
      assertEquals(
          "baz_1",
          AstEditor.getUniqueVariableName(Lists.newArrayList(fooDeclaration, bazDeclaration), "baz"));
      assertEquals(
          "baz",
          AstEditor.getUniqueVariableName(Lists.newArrayList(fooDeclaration), "baz"));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getUniqueMethodName
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getUniqueMethodName() throws Exception {
    createTypeDeclaration(
        "test",
        "Test.java",
        getSource(
            "package test;",
            "public class Test {",
            "  void foo() {",
            "  }",
            "  void bar() {",
            "  }",
            "  void baz(int a) {",
            "  }",
            "}"));
    assertEquals("abc", m_lastEditor.getUniqueMethodName("abc"));
    assertEquals("foo_1", m_lastEditor.getUniqueMethodName("foo"));
    assertEquals("bar_1", m_lastEditor.getUniqueMethodName("bar"));
    assertEquals("baz_1", m_lastEditor.getUniqueMethodName("baz"));
    assertEquals("hashCode_1", m_lastEditor.getUniqueMethodName("hashCode"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getUniqueTypeName
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getUniqueTypeName() throws Exception {
    createTypeDeclaration_TestC("void foo(){} class A {} class B {} class C {}");
    assertEquals("A_1", m_lastEditor.getUniqueTypeName("A"));
    assertEquals("B_1", m_lastEditor.getUniqueTypeName("B"));
    assertEquals("C_1", m_lastEditor.getUniqueTypeName("C"));
    assertEquals("D", m_lastEditor.getUniqueTypeName("D"));
    assertEquals("foo", m_lastEditor.getUniqueTypeName("foo"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getStatementEndIndex
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getStatementEndIndex_1() throws Exception {
    check_getStatementEndIndex("void foo() {int a; int b; int c;}", 0, 7);
  }

  public void test_getStatementEndIndex_2() throws Exception {
    check_getStatementEndIndex("void foo() {int a; \tint b;\t\t int c;}", 1, 9);
  }

  public void test_getStatementEndIndex_3() throws Exception {
    check_getStatementEndIndex("void foo() {int a; \tint b;\t\t// a\n int c;}", 1, 12);
  }

  private void check_getStatementEndIndex(String code, int statementIndex, int expectedIndexShift)
      throws Exception {
    TypeDeclaration typeDeclaration = createTypeDeclaration_TestC(code);
    MethodDeclaration method = typeDeclaration.getMethods()[0];
    assertEquals(code, m_lastEditor.getSource(method));
    // prepare statement to remove
    Statement statement = (Statement) method.getBody().statements().get(statementIndex);
    assertEquals(
        statement.getStartPosition() + expectedIndexShift,
        m_lastEditor.getStatementEndIndex(statement));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // removeStatement
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for removing dangling {@link Statement}.
   */
  public void test_removeStatement_0() throws Exception {
    // test for dangling node
    TypeDeclaration testType =
        createTypeDeclaration(
            "test",
            "Test.java",
            getSource(
                "public class Test{",
                "  public Test() {",
                "    int a = 10;",
                "    {",
                "      int b = 100;",
                "      int c = 1000;",
                "    }",
                "  }",
                "}"));
    MethodDeclaration method = testType.getMethods()[0];
    Block block = (Block) DomGenerics.statements(method.getBody()).get(1);
    List<Statement> blockStatements = DomGenerics.statements(block);
    Statement blockStatement = blockStatements.get(0);
    // remove block
    m_lastEditor.removeStatement(block);
    assertEditor(
        getSourceDQ("public class Test{", "  public Test() {", "    int a = 10;", "  }", "}"),
        m_lastEditor);
    // remove statement from deleted block, should not cause any problem, no changes
    String expectedSource = m_lastEditor.getSource();
    m_lastEditor.removeStatement(blockStatement);
    assertEditor(expectedSource, m_lastEditor);
  }

  public void test_removeStatement_1() throws Exception {
    // simple case - just statement, without any whitespace
    String code = "void foo(){int value = 1;}";
    check_removeStatement(code, 0, "void foo(){}");
  }

  public void test_removeStatement_2() throws Exception {
    // we remove whitespace only to the EOL, but not including EOL
    String code = "void foo(){int value = 1;\t \t\n }";
    check_removeStatement(code, 0, "void foo(){\n }");
  }

  public void test_removeStatement_3() throws Exception {
    // we remove also EOL comments
    String code = "void foo(){int value = 1; // abc \n }";
    check_removeStatement(code, 0, "void foo(){\n }");
  }

  public void test_removeStatement_3_1() throws Exception {
    // we remove also EOL comments
    String code = "void foo(){int value = 1; // abc \r\n }";
    check_removeStatement(code, 0, "void foo(){\r\n }");
  }

  public void test_removeStatement_4() throws Exception {
    // we remove any whitespace before statement
    String code = "void foo(){\t\t\n\t int value = 1;}";
    check_removeStatement(code, 0, "void foo(){}");
  }

  public void test_removeStatement_5() throws Exception {
    // we remove also any comments before statement
    String code = "void foo(){\t\t\n\t // abc\n/*qwe\n\trty*/ int value = 1;}";
    check_removeStatement(code, 0, "void foo(){}");
  }

  public void test_removeStatement_6() throws Exception {
    // we remove from beginning of block or (in this case) from end of previous statement
    String code = "void foo(){\n\tint a;\n\tint value = 1;\n}";
    check_removeStatement(code, 1, "void foo(){\n\tint a;\n}");
  }

  public void test_removeStatement_7() throws Exception {
    // we remove full block if its sole statement is removing
    String code = "void foo(){int a;{int b;}}";
    check_removeStatement(code, new int[]{1, 0}, "void foo(){int a;}");
  }

  public void test_removeStatement_8() throws Exception {
    // ...but only if it is sole statement
    String code = "void foo(){int a;{int b;int c;}}";
    check_removeStatement(code, new int[]{1, 0}, "void foo(){int a;{int c;}}");
  }

  private void check_removeStatement(String code, int indexToRemove, String expectedSource)
      throws Exception {
    check_removeStatement(code, new int[]{indexToRemove}, expectedSource);
  }

  private void check_removeStatement(String code, int indexes[], String expectedSource)
      throws Exception {
    TypeDeclaration typeDeclaration = createTypeDeclaration_TestC(code);
    MethodDeclaration method = typeDeclaration.getMethods()[0];
    assertEquals(code, m_lastEditor.getSource(method));
    // prepare statement to remove
    Statement statement = getStatement(method.getBody(), indexes);
    // remove statement and check result
    m_lastEditor.removeStatement(statement);
    assertEquals(expectedSource, m_lastEditor.getSource(method));
    assertAST(m_lastEditor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Comments
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that {@link LineComment}'s are moved/removed during operations.
   */
  public void test_comments_1_removeLine() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestC(getSourceDQ(
            "  void foo() {",
            "    // comment 0",
            "    int statementToRemove;",
            "  }",
            "  // comment 1"));
    MethodDeclaration method = typeDeclaration.getMethods()[0];
    // prepare comments
    Comment comment_0;
    Comment comment_1;
    {
      List<Comment> commentList = m_lastEditor.getCommentList();
      assertEquals(2, commentList.size());
      comment_0 = commentList.get(0);
      comment_1 = commentList.get(1);
    }
    assertEquals("// comment 0", m_lastEditor.getSource(comment_0));
    assertEquals("// comment 1", m_lastEditor.getSource(comment_1));
    // remove Statement, so line comment before it also should be removed
    Statement statement = getStatement(method.getBody(), new int[]{0});
    m_lastEditor.removeStatement(statement);
    assertAST(m_lastEditor);
    assertEquals("void foo() {\n\t}", m_lastEditor.getSource(method));
    //
    {
      List<Comment> commentList = m_lastEditor.getCommentList();
      assertEquals(1, commentList.size());
      assertFalse(commentList.contains(comment_0));
      assertTrue(commentList.contains(comment_1));
      assertEquals("// comment 1", m_lastEditor.getSource(comment_1));
    }
  }

  /**
   * There was problem that {@link LineComment}'s were not removed from AST and caused range
   * exceptions later.
   */
  public void test_comments_removeComment_whenReplace() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestC(getSource(
            "  void foo() {",
            "    // comment 0",
            "    int statementToRemove;",
            "    // comment 1",
            "  }"));
    MethodDeclaration method = typeDeclaration.getMethods()[0];
    // prepare comments
    Comment comment_0;
    Comment comment_1;
    {
      List<Comment> commentList = m_lastEditor.getCommentList();
      assertThat(commentList).hasSize(2);
      comment_0 = commentList.get(0);
      comment_1 = commentList.get(1);
    }
    assertEquals("// comment 0", m_lastEditor.getSource(comment_0));
    assertEquals("// comment 1", m_lastEditor.getSource(comment_1));
    // remove Statement, so line comment before it also should be removed
    Statement statement = getStatement(method.getBody(), new int[]{0});
    m_lastEditor.removeStatement(statement);
    // check comments
    {
      List<Comment> commentList = m_lastEditor.getCommentList();
      assertThat(commentList).hasSize(1).containsExactly(comment_1);
      assertEquals("// comment 1", m_lastEditor.getSource(comment_1));
    }
  }

  /**
   * Test that {@link LineComment}'s are moved/removed during operations.<br/>
   * Here we use long declarations just to force wrapping of source.
   */
  public void test_comments_2_removeBlock() throws Exception {
    check_comments_2_removeBlock(new String[]{
        "  void foo() {",
        "    /* block comment */",
        "    int longDeclaration_1;",
        "  }"});
  }

  /**
   * Test that {@link LineComment}'s are moved/removed during operations.<br/>
   * We use long declarations at end to force remove because of invalid start.
   */
  public void test_comments_2_removeBlock2() throws Exception {
    check_comments_2_removeBlock(new String[]{
        "  void foo() {",
        "    /* block comment */",
        "  }",
        "  int longDeclaration_1;",
        "  int longDeclaration_2;",
        "  int longDeclaration_3;",
        "  int longDeclaration_4;"});
  }

  private void check_comments_2_removeBlock(String[] lines) throws Exception {
    TypeDeclaration typeDeclaration = createTypeDeclaration_TestC(getSource(lines));
    MethodDeclaration method = typeDeclaration.getMethods()[0];
    // prepare comment
    Comment comment;
    {
      List<Comment> commentList = m_lastEditor.getCommentList();
      assertEquals(1, commentList.size());
      comment = commentList.get(0);
    }
    assertEquals("/* block comment */", m_lastEditor.getSource(comment));
    // remove method (including comment)
    m_lastEditor.removeBodyDeclaration(method);
    assertAST(m_lastEditor);
    //
    {
      List<Comment> commentList = m_lastEditor.getCommentList();
      assertEquals(0, commentList.size());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // removeEnclosingStatement
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_removeEnclosingStatement() throws Exception {
    TypeDeclaration typeDeclaration = createTypeDeclaration_TestC("void foo() {int a;}");
    MethodDeclaration method = typeDeclaration.getMethods()[0];
    //
    VariableDeclarationStatement statement =
        (VariableDeclarationStatement) getStatement(method.getBody(), new int[]{0});
    VariableDeclaration fragment = (VariableDeclaration) statement.fragments().get(0);
    m_lastEditor.removeEnclosingStatement(fragment);
    assertEquals("void foo() {}", m_lastEditor.getSource(method));
    assertAST(m_lastEditor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // removeInvocationArgument
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test: sole argument.
   */
  public void test_removeInvocationArgument_0() throws Exception {
    check_removeInvocationArgument("System.out.println(0)", 0, "System.out.println()");
  }

  /**
   * Test: first argument.
   */
  public void test_removeInvocationArgument_1() throws Exception {
    check_removeInvocationArgument("bar(0, 1, 2)", 0, "bar(1, 2)");
  }

  /**
   * Test: first argument, more spaces.
   */
  public void test_removeInvocationArgument_2() throws Exception {
    check_removeInvocationArgument("bar(0  ,  \r\n 1, 2)", 0, "bar(1, 2)");
  }

  /**
   * Test: inner argument.
   */
  public void test_removeInvocationArgument_3() throws Exception {
    check_removeInvocationArgument("bar(0, 1, 2)", 1, "bar(0, 2)");
  }

  /**
   * Test: last argument.
   */
  public void test_removeInvocationArgument_4() throws Exception {
    check_removeInvocationArgument("bar(0, 1, 2)", 2, "bar(0, 1)");
  }

  /**
   * Test: two arguments.
   */
  public void test_removeInvocationArgument_5() throws Exception {
    MethodInvocation invocation = check_removeInvocationArgument("bar(0, 1, 2)", 0, "bar(1, 2)");
    check_removeInvocationArgument(invocation, 0, "bar(2)");
  }

  private MethodInvocation check_removeInvocationArgument(String source,
      int argumentIndex,
      String expectedSource) throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestC("void foo() {" + source + ";} void bar(int a, int b, int c) {}");
    MethodDeclaration method = typeDeclaration.getMethods()[0];
    ExpressionStatement statement = (ExpressionStatement) method.getBody().statements().get(0);
    MethodInvocation invocation = (MethodInvocation) statement.getExpression();
    //
    return check_removeInvocationArgument(invocation, argumentIndex, expectedSource);
  }

  private MethodInvocation check_removeInvocationArgument(MethodInvocation invocation,
      int argumentIndex,
      String expectedSource) throws Exception {
    m_lastEditor.removeInvocationArgument(invocation, argumentIndex);
    assertEquals(expectedSource, m_lastEditor.getSource(invocation));
    assertAST(m_lastEditor);
    return invocation;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // moveInvocationArgument
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_moveInvocationArgument() throws Exception {
    MethodInvocation invocation =
        check_moveInvocationArgument("bar(0, 1, 2)", 0, 1, "bar(1, 0, 2)");
    check_moveInvocationArgument(invocation, 2, 1, "bar(1, 2, 0)");
  }

  private MethodInvocation check_moveInvocationArgument(String source,
      int oldArgumentIndex,
      int newArgumentIndex,
      String expectedSource) throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestC("void foo() {" + source + ";} void bar(int a, int b, int c) {}");
    MethodDeclaration method = typeDeclaration.getMethods()[0];
    ExpressionStatement statement = (ExpressionStatement) method.getBody().statements().get(0);
    MethodInvocation invocation = (MethodInvocation) statement.getExpression();
    //
    return check_moveInvocationArgument(
        invocation,
        oldArgumentIndex,
        newArgumentIndex,
        expectedSource);
  }

  private MethodInvocation check_moveInvocationArgument(MethodInvocation invocation,
      int oldArgumentIndex,
      int newArgumentIndex,
      String expectedSource) throws Exception {
    m_lastEditor.moveInvocationArgument(invocation, oldArgumentIndex, newArgumentIndex);
    assertEquals(expectedSource, m_lastEditor.getSource(invocation));
    assertAST(m_lastEditor);
    return invocation;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // removeCreationArgument
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_removeCreationArgument() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  private void foo() {",
            "    new Test(1, 2);",
            "  }",
            "  public Test(int a, int b) {",
            "  }",
            "  public Test(int a) {",
            "  }",
            "}");
    // do remove argument
    {
      MethodDeclaration method = typeDeclaration.getMethods()[0];
      ExpressionStatement statement = (ExpressionStatement) method.getBody().statements().get(0);
      ClassInstanceCreation creation = (ClassInstanceCreation) statement.getExpression();
      m_lastEditor.removeCreationArgument(creation, 1);
    }
    // check editor
    assertEditor(
        getSourceDQ(
            "package test;",
            "public class Test {",
            "  private void foo() {",
            "    new Test(1);",
            "  }",
            "  public Test(int a, int b) {",
            "  }",
            "  public Test(int a) {",
            "  }",
            "}"),
        m_lastEditor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // addArrayElement
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_addArrayElement_0() throws Exception {
    check_addArrayElement("new int[]{2, 3}", 0, "1", "new int[]{1, 2, 3}");
  }

  public void test_addArrayElement_1() throws Exception {
    check_addArrayElement("new int[]{1, 3}", 1, "2", "new int[]{1, 2, 3}");
  }

  public void test_addArrayElement_2() throws Exception {
    check_addArrayElement("new int[]{1, 2}", 2, "3", "new int[]{1, 2, 3}");
  }

  public void test_addArrayElement_3() throws Exception {
    check_addArrayElement("new int[]{}", 0, "1", "new int[]{1}");
  }

  private void check_addArrayElement(String source,
      int elementIndex,
      String newElementSource,
      String expectedSource) throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestC("void foo() { int[] x = " + source + ";}");
    MethodDeclaration method = typeDeclaration.getMethods()[0];
    VariableDeclarationStatement statement =
        (VariableDeclarationStatement) method.getBody().statements().get(0);
    VariableDeclarationFragment fragment =
        (VariableDeclarationFragment) statement.fragments().get(0);
    ArrayCreation creation = (ArrayCreation) fragment.getInitializer();
    //
    m_lastEditor.addArrayElement(creation.getInitializer(), elementIndex, newElementSource);
    assertEquals(expectedSource, m_lastEditor.getSource(creation));
    assertAST(m_lastEditor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // removeArrayElement
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_removeArrayElement_0() throws Exception {
    check_removeArrayElement("new int[]{1, 2, 3}", 0, "new int[]{2, 3}");
  }

  public void test_removeArrayElement_1() throws Exception {
    check_removeArrayElement("new int[]{1, 2, 3}", 1, "new int[]{1, 3}");
  }

  public void test_removeArrayElement_2() throws Exception {
    check_removeArrayElement("new int[]{1, 2, 3}", 2, "new int[]{1, 2}");
  }

  public void test_removeArrayElement_3() throws Exception {
    check_removeArrayElement("new int[]{1}", 0, "new int[]{}");
  }

  /**
   * We should be nice and ignore remove request if there are no such element.
   */
  public void test_removeArrayElement_noSuchIndex() throws Exception {
    check_removeArrayElement("new int[]{0, 1}", 2, "new int[]{0, 1}");
  }

  private void check_removeArrayElement(String source, int elementIndex, String expectedSource)
      throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestC("void foo() { int[] x = " + source + ";}");
    MethodDeclaration method = typeDeclaration.getMethods()[0];
    VariableDeclarationStatement statement =
        (VariableDeclarationStatement) method.getBody().statements().get(0);
    VariableDeclarationFragment fragment =
        (VariableDeclarationFragment) statement.fragments().get(0);
    ArrayCreation creation = (ArrayCreation) fragment.getInitializer();
    //
    m_lastEditor.removeArrayElement(creation.getInitializer(), elementIndex);
    assertEquals(expectedSource, m_lastEditor.getSource(creation));
    assertAST(m_lastEditor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // moveArrayElement
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_moveArrayElement() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestC("void foo() { int[] a = new int[]{0}; int[] b = new int[]{1, 2};}");
    MethodDeclaration method = typeDeclaration.getMethods()[0];
    //
    VariableDeclarationStatement statementA =
        (VariableDeclarationStatement) method.getBody().statements().get(0);
    VariableDeclarationFragment fragmentA =
        (VariableDeclarationFragment) statementA.fragments().get(0);
    ArrayCreation oldCreation = (ArrayCreation) fragmentA.getInitializer();
    //
    VariableDeclarationStatement statementB =
        (VariableDeclarationStatement) method.getBody().statements().get(1);
    VariableDeclarationFragment fragmentB =
        (VariableDeclarationFragment) statementB.fragments().get(0);
    ArrayCreation newCreation = (ArrayCreation) fragmentB.getInitializer();
    //
    Expression moveElement =
        m_lastEditor.moveArrayElement(
            oldCreation.getInitializer(),
            newCreation.getInitializer(),
            0,
            1);
    //
    assertNotNull(moveElement);
    assertSame(newCreation.getInitializer(), moveElement.getParent());
    assertEquals("new int[]{}", m_lastEditor.getSource(oldCreation));
    assertEquals("new int[]{1, 0, 2}", m_lastEditor.getSource(newCreation));
    assertAST(m_lastEditor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // exchangeArrayElements()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_exchangeArrayElements_1() throws Exception {
    check_exchangeArrayElements("new int[]{1, 2, 3, 4}", 0, 2, "new int[]{3, 2, 1, 4}");
  }

  public void test_exchangeArrayElements_2() throws Exception {
    check_exchangeArrayElements("new int[]{111, 2, 3, 4}", 0, 2, "new int[]{3, 2, 111, 4}");
  }

  public void test_exchangeArrayElements_3() throws Exception {
    check_exchangeArrayElements("new int[]{1, 2, 333, 4}", 0, 2, "new int[]{333, 2, 1, 4}");
  }

  public void test_exchangeArrayElements_4() throws Exception {
    check_exchangeArrayElements("new int[]{1, 2, 3, 4}", 2, 0, "new int[]{3, 2, 1, 4}");
  }

  public void test_exchangeArrayElements_5() throws Exception {
    check_exchangeArrayElements("new int[]{111, 2, 3, 4}", 2, 0, "new int[]{3, 2, 111, 4}");
  }

  public void test_exchangeArrayElements_6() throws Exception {
    check_exchangeArrayElements("new int[]{1, 2, 333, 4}", 2, 0, "new int[]{333, 2, 1, 4}");
  }

  /**
   * Test for
   * {@link AstEditor#exchangeArrayElements(org.eclipse.jdt.core.dom.ArrayInitializer, int, int)}.
   */
  private void check_exchangeArrayElements(String source,
      int index_1,
      int index_2,
      String expectedSource) throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestC("void foo() { int[] x = " + source + ";}");
    MethodDeclaration method = typeDeclaration.getMethods()[0];
    VariableDeclarationStatement statement =
        (VariableDeclarationStatement) method.getBody().statements().get(0);
    VariableDeclarationFragment fragment =
        (VariableDeclarationFragment) statement.fragments().get(0);
    ArrayCreation creation = (ArrayCreation) fragment.getInitializer();
    // step 1
    {
      m_lastEditor.exchangeArrayElements(creation.getInitializer(), index_1, index_2);
      assertEquals(expectedSource, m_lastEditor.getSource(creation));
      assertAST(m_lastEditor);
    }
    // step 2
    {
      m_lastEditor.exchangeArrayElements(creation.getInitializer(), index_1, index_2);
      assertEquals(source, m_lastEditor.getSource(creation));
      assertAST(m_lastEditor);
    }
    // step 3
    {
      m_lastEditor.exchangeArrayElements(creation.getInitializer(), index_1, index_2);
      assertEquals(expectedSource, m_lastEditor.getSource(creation));
      assertAST(m_lastEditor);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // addInvocationArgument
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test: sole argument.
   */
  public void test_addInvocationArgument_1() throws Exception {
    check_addInvocationArgument("bar()", 0, "0", "bar(0)");
  }

  /**
   * Test: first argument.
   */
  public void test_addInvocationArgument_2() throws Exception {
    check_addInvocationArgument("bar(1)", 0, "0", "bar(0, 1)");
  }

  /**
   * Test: last argument.
   */
  public void test_addInvocationArgument_3() throws Exception {
    check_addInvocationArgument("bar(0)", 1, "1", "bar(0, 1)");
  }

  /**
   * Test: inner argument.
   */
  public void test_addInvocationArgument_4() throws Exception {
    check_addInvocationArgument("bar(0, 2)", 1, "1", "bar(0, 1, 2)");
  }

  /**
   * Test: argument with fully qualified types.
   */
  public void test_addInvocationArgument_5() throws Exception {
    CompilationUnit compilationUnit =
        createASTCompilationUnit(
            "test",
            "Test.java",
            getSourceDQ(
                "package test;",
                "public class Test {",
                "  Test() {",
                "    bar(null);",
                "  }",
                "  void bar(Object a) {}",
                "  void bar(Object a, Object b) {}",
                "}"));
    TypeDeclaration typeDeclaration = (TypeDeclaration) compilationUnit.types().get(0);
    MethodDeclaration method = typeDeclaration.getMethods()[0];
    ExpressionStatement statement = (ExpressionStatement) method.getBody().statements().get(0);
    MethodInvocation invocation = (MethodInvocation) statement.getExpression();
    //
    m_lastEditor.addInvocationArgument(invocation, 1, "new java.util.ArrayList()");
    assertEquals(
        getSourceDQ(
            "package test;",
            "import java.util.ArrayList;",
            "public class Test {",
            "  Test() {",
            "    bar(null, new ArrayList());",
            "  }",
            "  void bar(Object a) {}",
            "  void bar(Object a, Object b) {}",
            "}"),
        m_lastEditor.getSource());
    assertAST(m_lastEditor);
  }

  private MethodInvocation check_addInvocationArgument(String source,
      int argumentIndex,
      String argumentSource,
      String expectedSource) throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestC("void foo() {"
            + source
            + ";} void bar() {} void bar(int a) {}  void bar(int a, int b) {}  void bar(int a, int b, int c) {}");
    MethodDeclaration method = typeDeclaration.getMethods()[0];
    ExpressionStatement statement = (ExpressionStatement) method.getBody().statements().get(0);
    MethodInvocation invocation = (MethodInvocation) statement.getExpression();
    //
    return check_addInvocationArgument(invocation, argumentIndex, argumentSource, expectedSource);
  }

  private MethodInvocation check_addInvocationArgument(MethodInvocation invocation,
      int argumentIndex,
      String argumentSource,
      String expectedSource) throws Exception {
    m_lastEditor.addInvocationArgument(invocation, argumentIndex, argumentSource);
    assertEquals(expectedSource, m_lastEditor.getSource(invocation));
    assertAST(m_lastEditor);
    return invocation;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // addCreationArgument()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstEditor#addCreationArgument(ClassInstanceCreation, int, String)}.
   */
  public void test_addCreationArgument() throws Exception {
    createTypeDeclaration(
        "test",
        "Test.java",
        getSource(
            "package test;",
            "public class Test {",
            "  Test() {",
            "    new java.util.ArrayList();",
            "  }",
            "}"));
    ClassInstanceCreation creation =
        (ClassInstanceCreation) m_lastEditor.getEnclosingNode("new java.util.");
    assertEquals("<init>()", AstNodeUtils.getCreationSignature(creation));
    //
    m_lastEditor.addCreationArgument(creation, 0, "10");
    assertEditor(
        getSource(
            "package test;",
            "public class Test {",
            "  Test() {",
            "    new java.util.ArrayList(10);",
            "  }",
            "}"),
        m_lastEditor);
    assertEquals("<init>(int)", AstNodeUtils.getCreationSignature(creation));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // addAnonymousClassDeclaration
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstEditor#addAnonymousClassDeclaration(ClassInstanceCreation)}.
   */
  public void test_addAnonymousClassDeclaration() throws Exception {
    createTypeDeclaration(
        "test",
        "Test.java",
        getSource(
            "package test;",
            "import java.util.ArrayList;",
            "public class Test {",
            "  Test() {",
            "    ArrayList list = new ArrayList(10);",
            "  }",
            "}"));
    ClassInstanceCreation creation = getNode("new ArrayList");
    assertNull(creation.getAnonymousClassDeclaration());
    assertEquals("<init>(int)", AstNodeUtils.getCreationSignature(creation));
    //
    m_lastEditor.addAnonymousClassDeclaration(creation);
    assertEditor(
        getSource(
            "package test;",
            "import java.util.ArrayList;",
            "public class Test {",
            "  Test() {",
            "    ArrayList list = new ArrayList(10) {",
            "    };",
            "  }",
            "}"),
        m_lastEditor);
    assertNotNull(creation.getAnonymousClassDeclaration());
    assertEquals("<init>(int)", AstNodeUtils.getCreationSignature(creation));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // replaceInvocationName
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_replaceInvocationName() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestD(
            "public class Test {",
            "  void foo() {",
            "    bar(0);",
            "  }",
            "  void bar(int a) {",
            "  }",
            "  void baz(int a) {",
            "  }",
            "}");
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    ExpressionStatement statement =
        (ExpressionStatement) methodDeclaration.getBody().statements().get(0);
    MethodInvocation invocation = (MethodInvocation) statement.getExpression();
    //
    m_lastEditor.replaceInvocationName(invocation, "baz");
    assertEditor(
        getSourceDQ(
            "public class Test {",
            "  void foo() {",
            "    baz(0);",
            "  }",
            "  void bar(int a) {",
            "  }",
            "  void baz(int a) {",
            "  }",
            "}"),
        m_lastEditor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // replaceInvocationExpression
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstEditor#replaceInvocationExpression(MethodInvocation, String)}.
   */
  public void test_replaceInvocationExpression_nullExpression() throws Exception {
    setFileContentSrc(
        "test/Wrapper.java",
        getSource(
            "package test;",
            "public class Wrapper {",
            "  public void print(int value) {",
            "  }",
            "}"));
    createTypeDeclaration_TestD(
        "package test;",
        "public class Test {",
        "  void foo() {",
        "    print(0);",
        "  }",
        "  void print(int value) {",
        "  }",
        "  Wrapper getWrapper() {",
        "    return null;",
        "  }",
        "}");
    MethodInvocation invocation = getNode("print(0)", MethodInvocation.class);
    //
    m_lastEditor.replaceInvocationExpression(invocation, "getWrapper()");
    assertEditor(
        getSource(
            "package test;",
            "public class Test {",
            "  void foo() {",
            "    getWrapper().print(0);",
            "  }",
            "  void print(int value) {",
            "  }",
            "  Wrapper getWrapper() {",
            "    return null;",
            "  }",
            "}"),
        m_lastEditor);
  }

  /**
   * Test for {@link AstEditor#replaceInvocationExpression(MethodInvocation, String)}.
   */
  public void test_replaceInvocationExpression_ThisExpression() throws Exception {
    setFileContentSrc(
        "test/Wrapper.java",
        getSource(
            "package test;",
            "public class Wrapper {",
            "  public void print(int value) {",
            "  }",
            "}"));
    createTypeDeclaration_TestD(
        "package test;",
        "public class Test {",
        "  void foo() {",
        "    this.print(0);",
        "  }",
        "  void print(int value) {",
        "  }",
        "  Wrapper getWrapper() {",
        "    return null;",
        "  }",
        "}");
    MethodInvocation invocation = getNode("print(0)", MethodInvocation.class);
    //
    m_lastEditor.replaceInvocationExpression(invocation, "getWrapper()");
    assertEditor(
        getSource(
            "package test;",
            "public class Test {",
            "  void foo() {",
            "    getWrapper().print(0);",
            "  }",
            "  void print(int value) {",
            "  }",
            "  Wrapper getWrapper() {",
            "    return null;",
            "  }",
            "}"),
        m_lastEditor);
  }

  /**
   * Test for {@link AstEditor#replaceInvocationExpression(MethodInvocation, String)}.
   */
  public void test_replaceInvocationExpression_anyExpression() throws Exception {
    setFileContentSrc(
        "test/Wrapper.java",
        getSource(
            "package test;",
            "public class Wrapper {",
            "  public void print(int value) {",
            "  }",
            "}"));
    createTypeDeclaration_TestD(
        "package test;",
        "public class Test {",
        "  void foo() {",
        "    new Wrapper().print(0);",
        "  }",
        "  Wrapper getWrapper() {",
        "    return null;",
        "  }",
        "}");
    MethodInvocation invocation = getNode("print(0)", MethodInvocation.class);
    //
    m_lastEditor.replaceInvocationExpression(invocation, "getWrapper()");
    assertEditor(
        getSource(
            "package test;",
            "public class Test {",
            "  void foo() {",
            "    getWrapper().print(0);",
            "  }",
            "  Wrapper getWrapper() {",
            "    return null;",
            "  }",
            "}"),
        m_lastEditor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // replaceCreationArguments
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstEditor#replaceCreationArguments(ClassInstanceCreation, String[])}, single
   * arguments line.
   */
  public void test_replaceCreationArguments_1() throws Exception {
    String[] newArgumentsLines = new String[]{"1"};
    String[] expectedSourceLines =
        new String[]{
            "package test;",
            "import java.util.*;",
            "public class Test {",
            "  void foo() {",
            "    Object o = new ArrayList(1);",
            "  }",
            "}"};
    check_replaceCreationArguments(newArgumentsLines, expectedSourceLines, "<init>(int)");
  }

  /**
   * Test for {@link AstEditor#replaceCreationArguments(ClassInstanceCreation, String[])}, several
   * arguments line.
   */
  public void test_replaceCreationArguments_2() throws Exception {
    String[] newArgumentsLines = new String[]{"1", "\t+", "\t2"};
    String[] expectedSourceLines =
        new String[]{
            "package test;",
            "import java.util.*;",
            "public class Test {",
            "  void foo() {",
            "    Object o = new ArrayList(1",
            "      +",
            "      2);",
            "  }",
            "}"};
    check_replaceCreationArguments(newArgumentsLines, expectedSourceLines, "<init>(int)");
  }

  /**
   * Test for {@link AstEditor#replaceCreationArguments(ClassInstanceCreation, String[])}, imports.
   */
  public void test_replaceCreationArguments_3() throws Exception {
    String[] newArgumentsLines = new String[]{"java.util.Collections.EMPTY_LIST"};
    String[] expectedSourceLines =
        new String[]{
            "package test;",
            "import java.util.*;",
            "public class Test {",
            "  void foo() {",
            "    Object o = new ArrayList(Collections.EMPTY_LIST);",
            "  }",
            "}"};
    check_replaceCreationArguments(
        newArgumentsLines,
        expectedSourceLines,
        "<init>(java.util.Collection)");
  }

  private void check_replaceCreationArguments(String[] newArgumentsLines,
      String[] expectedSourceLines,
      String expectedSignature) throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "import java.util.*;",
            "public class Test {",
            "  void foo() {",
            "    Object o = new ArrayList();",
            "  }",
            "}");
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    VariableDeclarationStatement statement =
        (VariableDeclarationStatement) methodDeclaration.getBody().statements().get(0);
    VariableDeclarationFragment fragment =
        (VariableDeclarationFragment) statement.fragments().get(0);
    ClassInstanceCreation creation = (ClassInstanceCreation) fragment.getInitializer();
    // do replace
    m_lastEditor.replaceCreationArguments(creation, ImmutableList.copyOf(newArgumentsLines));
    // check source
    assertEditor(getSource(expectedSourceLines), m_lastEditor);
    // check signature
    {
      String actualSignature = AstNodeUtils.getCreationSignature(creation);
      assertEquals(expectedSignature, actualSignature);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // replaceInvocationArguments
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstEditor#replaceInvocationArguments(MethodInvocation, List)}.
   */
  public void test_replaceInvocationArguments() throws Exception {
    setFileContentSrc(
        "MyObject.java",
        getSourceDQ(
            "public class MyObject {",
            "  public void setText(int i, boolean b) {",
            "  }",
            "}"));
    createTypeDeclaration_TestD(
        "public class Test {",
        "  public Test() {",
        "    MyObject obj = new MyObject();",
        "    obj.setText(1, false);",
        "  }",
        "}");
    MethodInvocation invocation = (MethodInvocation) m_lastEditor.getEnclosingNode(", ");
    // do replace
    m_lastEditor.replaceInvocationArguments(invocation, ImmutableList.of("2, true"));
    assertEditor(
        getSourceDQ(
            "public class Test {",
            "  public Test() {",
            "    MyObject obj = new MyObject();",
            "    obj.setText(2, true);",
            "  }",
            "}"),
        m_lastEditor);
  }

  /**
   * Test for {@link AstEditor#replaceInvocationArguments(MethodInvocation, List)}.
   */
  public void test_replaceInvocationArguments_whenChained() throws Exception {
    setFileContentSrc(
        "MyObject.java",
        getSourceDQ("public class MyObject {", "  public void foo(int i, boolean b) {", "  }", "}"));
    createTypeDeclaration_TestD(
        "public class Test {",
        "  public Test() {",
        "    getObj().foo(1, false);",
        "  }",
        "  private MyObject getObj() {",
        "    return null;",
        "  }",
        "}");
    MethodInvocation invocation = (MethodInvocation) m_lastEditor.getEnclosingNode(", ");
    // do replace
    m_lastEditor.replaceInvocationArguments(invocation, ImmutableList.of("2, true"));
    assertEditor(
        getSourceDQ(
            "public class Test {",
            "  public Test() {",
            "    getObj().foo(2, true);",
            "  }",
            "  private MyObject getObj() {",
            "    return null;",
            "  }",
            "}"),
        m_lastEditor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ParenthesizedExpression
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstEditor#inlineParenthesizedExpression(Expression)}.
   */
  public void test_inlineParenthesizedExpression() throws Exception {
    createTypeDeclaration_Test(
        "public class Test {",
        "  public Test() {",
        "    Object obj = new Object();",
        "    ((obj)).hashCode();",
        "  }",
        "}");
    Expression objNode = (Expression) m_lastEditor.getEnclosingNode("obj)");
    // do inline
    m_lastEditor.inlineParenthesizedExpression(objNode);
    assertEditor(
        getSourceDQ(
            "package test;",
            "public class Test {",
            "  public Test() {",
            "    Object obj = new Object();",
            "    obj.hashCode();",
            "  }",
            "}"),
        m_lastEditor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // hasEnclosingTryStatement()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstEditor#hasEnclosingTryStatement(Statement, String)}.
   */
  public void test_hasEnclosingTryStatement_noTryStatement() throws Exception {
    createTypeDeclaration_Test(
        "public class Test {",
        "  public Test() {",
        "    int value;",
        "  }",
        "}");
    Statement statement = getNode("int value", Statement.class);
    //
    assertFalse(m_lastEditor.hasEnclosingTryStatement(statement, "java.lang.Throwable"));
  }

  /**
   * Test for {@link AstEditor#hasEnclosingTryStatement(Statement, String)}.
   */
  public void test_hasEnclosingTryStatement_noSuchCatch() throws Exception {
    createTypeDeclaration_Test(
        "public class Test {",
        "  public Test() {",
        "    try {",
        "      int value;",
        "    } catch (java.lang.Exception e) {",
        "    }",
        "  }",
        "}");
    Statement statement = getNode("int value", Statement.class);
    //
    assertFalse(m_lastEditor.hasEnclosingTryStatement(statement, "java.lang.Throwable"));
  }

  /**
   * Test for {@link AstEditor#hasEnclosingTryStatement(Statement, String)}.
   */
  public void test_hasEnclosingTryStatement_hasExactCatch() throws Exception {
    createTypeDeclaration_Test(
        "public class Test {",
        "  public Test() {",
        "    try {",
        "      int value;",
        "    } catch (java.lang.Exception e) {",
        "    }",
        "  }",
        "}");
    Statement statement = getNode("int value", Statement.class);
    //
    assertTrue(m_lastEditor.hasEnclosingTryStatement(statement, "java.lang.Exception"));
  }

  /**
   * Test for {@link AstEditor#hasEnclosingTryStatement(Statement, String)}.
   */
  public void test_hasEnclosingTryStatement_hasSuperCatch() throws Exception {
    createTypeDeclaration_Test(
        "public class Test {",
        "  public Test() {",
        "    try {",
        "      int value;",
        "    } catch (java.lang.Throwable e) {",
        "    }",
        "  }",
        "}");
    Statement statement = getNode("int value", Statement.class);
    //
    assertTrue(m_lastEditor.hasEnclosingTryStatement(statement, "java.lang.Exception"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Statement => TryStatement
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstEditor#encloseInTryStatement(Statement, String)}.
   */
  public void test_encloseInTryStatement() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  public Test() {",
            "    int value;",
            "  }",
            "}");
    Block sourceBlock = typeDeclaration.getMethods()[0].getBody();
    Statement statement = DomGenerics.statements(sourceBlock).get(0);
    // do enclose
    m_lastEditor.encloseInTryStatement(statement, "java.lang.Throwable");
    assertEditor(
        getSourceDQ(
            "package test;",
            "public class Test {",
            "  public Test() {",
            "    try {",
            "      int value;",
            "    } catch (Throwable e) {",
            "    }",
            "  }",
            "}"),
        m_lastEditor);
  }

  /**
   * Test for {@link AstEditor#removeEmptyTryStatements()}.
   */
  public void test_removeEmptyTryStatements() throws Exception {
    createTypeDeclaration_Test(
        "public class Test {",
        "  public Test() {",
        "    try{",
        "    } finally {",
        "    }",
        "    try {",
        "      int value;",
        "    } finally {",
        "    }",
        "  }",
        "}");
    // do remove
    m_lastEditor.removeEmptyTryStatements();
    assertEditor(
        getSourceDQ(
            "package test;",
            "public class Test {",
            "  public Test() {",
            "    try {",
            "      int value;",
            "    } finally {",
            "    }",
            "  }",
            "}"),
        m_lastEditor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Statement <=> Block
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstEditor#encloseInBlock(Statement)}.
   */
  public void test_encloseInBlock() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  public Test() {",
            "    int value;",
            "  }",
            "}");
    Block sourceBlock = typeDeclaration.getMethods()[0].getBody();
    Statement statement = DomGenerics.statements(sourceBlock).get(0);
    // do enclose
    m_lastEditor.encloseInBlock(statement);
    assertEditor(
        getSourceDQ(
            "package test;",
            "public class Test {",
            "  public Test() {",
            "    {",
            "      int value;",
            "    }",
            "  }",
            "}"),
        m_lastEditor);
  }

  /**
   * Test for {@link AstEditor#inlineBlock(Block)}.
   */
  public void test_inlineBlock() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  public Test() {",
            "    int a;",
            "    {",
            "      int b;",
            "      int c;",
            "      int d;",
            "    }",
            "    int e;",
            "  }",
            "}");
    Block mainBlock = typeDeclaration.getMethods()[0].getBody();
    Block blockToInline = (Block) DomGenerics.statements(mainBlock).get(1);
    // do inline
    m_lastEditor.inlineBlock(blockToInline);
    assertEditor(
        getSourceDQ(
            "package test;",
            "public class Test {",
            "  public Test() {",
            "    int a;",
            "    int b;",
            "    int c;",
            "    int d;",
            "    int e;",
            "  }",
            "}"),
        m_lastEditor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // addStatement
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_addStatement_1() throws Exception {
    // simple case - before statement
    String methodCode = "int f; void foo() {\n\tint a; // test \n}";
    String statementCode = "System.out.println(f);";
    String expectedMethodCode = "{\n\t" + statementCode + "\n\tint a; // test \n}";
    check_addStatement(methodCode, 0, true, statementCode, expectedMethodCode);
  }

  public void test_addStatement_2() throws Exception {
    // more complex case - after statement
    String methodCode = "void foo() {\n\tint a; // test \n}";
    String statementCode = "System.out.println(a);";
    String expectedMethodCode = "{\n\tint a; // test \n\t" + statementCode + "\n}";
    check_addStatement(methodCode, 0, false, statementCode, expectedMethodCode);
  }

  public void test_addStatement_2a() throws Exception {
    // after statement without prefix
    String methodCode = "\nvoid foo() {int a;}";
    String statementCode = "System.out.println(a);";
    String expectedMethodCode = "{int a;\n" + statementCode + "}";
    check_addStatement(methodCode, 0, false, statementCode, expectedMethodCode);
  }

  public void test_addStatement_2b() throws Exception {
    // for parameter
    String methodCode = "\nvoid foo(int p) {int a;}";
    String statementCode = "System.out.println(p);";
    String expectedMethodCode = "{int a;\n" + statementCode + "}";
    check_addStatement(methodCode, 0, false, statementCode, expectedMethodCode);
  }

  public void test_addStatement_2c() throws Exception {
    // check default values for different types
    String methodCode =
        "boolean z;byte b;char c;short s;int i;long l;float f;double d;Object o;String str; \nvoid foo() {int a;}";
    String statementCode = "System.out.println(a);";
    String expectedMethodCode = "{int a;\n" + statementCode + "}";
    check_addStatement(methodCode, 0, false, statementCode, expectedMethodCode);
  }

  public void test_addStatement_3() throws Exception {
    // in beginning of method
    String methodCode = "\n\tvoid foo() {\n\t\tint a;\n\t}";
    String statementCode = "System.out.println();";
    String expectedMethodCode = "{\n\t\t" + statementCode + "\n\t\tint a;\n\t}";
    check_addStatement(methodCode, -1, true, statementCode, expectedMethodCode);
  }

  public void test_addStatement_4() throws Exception {
    // in end of method
    String methodCode = "\n\tvoid foo() {\n\t\tint a;\n\t}";
    String statementCode = "System.out.println(a);";
    String expectedMethodCode = "{\n\t\tint a;\n\t\t" + statementCode + "\n\t}";
    check_addStatement(methodCode, -1, false, statementCode, expectedMethodCode);
  }

  private void check_addStatement(String methodCode,
      int targetStatementIndex,
      boolean before,
      String statementCode,
      String expectedMethodCode) throws Exception {
    TypeDeclaration typeDeclaration = createTypeDeclaration_TestC(methodCode);
    MethodDeclaration targetMethod = typeDeclaration.getMethods()[0];
    Statement targetStatement =
        targetStatementIndex != -1 ? (Statement) targetMethod.getBody().statements().get(
            targetStatementIndex) : null;
    //
    StatementTarget target = new StatementTarget(targetMethod.getBody(), targetStatement, before);
    Statement newStatement = m_lastEditor.addStatement(statementCode, target);
    assertNotNull(newStatement);
    assertEquals(expectedMethodCode, m_lastEditor.getSource(targetMethod.getBody()));
    assertAST(m_lastEditor);
  }

  /**
   * Test that we can add relative to inner {@link Block}.
   */
  public void test_addStatement_5() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "import java.awt.*;",
            "import javax.swing.*;",
            "class Test extends JPanel {",
            "  Test() {",
            "    {",
            "    }",
            "  }",
            "}");
    MethodDeclaration targetMethod = typeDeclaration.getMethods()[0];
    Block targetBlock = (Block) getStatement(targetMethod.getBody(), new int[]{0});
    //
    String source = "setVisible(false);";
    m_lastEditor.addStatement(source, new StatementTarget(targetBlock, true));
    assertAST(m_lastEditor);
    assertEquals(
        getSourceDQ(
            "package test;",
            "import java.awt.*;",
            "import javax.swing.*;",
            "class Test extends JPanel {",
            "  Test() {",
            "    {",
            "      " + source,
            "    }",
            "  }",
            "}"),
        m_lastEditor.getSource());
  }

  /**
   * Test that we can add statement with leading comment lines.
   */
  public void test_addStatement_6() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "import java.awt.*;",
            "import javax.swing.*;",
            "class Test extends JPanel {",
            "  Test() {",
            "    System.out.println(1);",
            "    // existing comment",
            "    System.out.println(2);",
            "  }",
            "}");
    MethodDeclaration targetMethod = typeDeclaration.getMethods()[0];
    Statement targetStatement = (Statement) targetMethod.getBody().statements().get(1);
    //
    m_lastEditor.addStatement(
        ImmutableList.of("// first comment", "// second comment", "setVisible(false);"),
        new StatementTarget(targetStatement, true));
    assertEquals(
        getSourceDQ(
            "package test;",
            "import java.awt.*;",
            "import javax.swing.*;",
            "class Test extends JPanel {",
            "  Test() {",
            "    System.out.println(1);",
            "    // first comment",
            "    // second comment",
            "    setVisible(false);",
            "    // existing comment",
            "    System.out.println(2);",
            "  }",
            "}"),
        m_lastEditor.getSource());
    assertAST(m_lastEditor);
  }

  /**
   * Test that we can add after statement with trailing end of line comment.
   */
  public void test_addStatement_7() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "class Test {",
            "  Test() {",
            "    System.out.println(1); // end of line comment",
            "    System.out.println(2);",
            "  }",
            "}");
    MethodDeclaration targetMethod = typeDeclaration.getMethods()[0];
    Statement targetStatement = (Statement) targetMethod.getBody().statements().get(0);
    //
    m_lastEditor.addStatement("System.out.println(false);", new StatementTarget(targetStatement,
        false));
    assertEquals(
        getSourceDQ(
            "package test;",
            "class Test {",
            "  Test() {",
            "    System.out.println(1); // end of line comment",
            "    System.out.println(false);",
            "    System.out.println(2);",
            "  }",
            "}"),
        m_lastEditor.getSource());
    assertAST(m_lastEditor);
  }

  /**
   * Test that when we use statement source with fully qualified names, they are imported.
   */
  public void test_addStatement_8() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "// filler filler filler",
            "class Test {",
            "  Test() {",
            "  }",
            "}");
    MethodDeclaration targetMethod = typeDeclaration.getMethods()[0];
    Block targetBlock = targetMethod.getBody();
    //
    m_lastEditor.addStatement(
        "java.util.List myList = new java.util.ArrayList();",
        new StatementTarget(targetBlock, true));
    assertEquals(
        getSourceDQ(
            "package test;",
            "import java.util.List;",
            "import java.util.ArrayList;",
            "// filler filler filler",
            "class Test {",
            "  Test() {",
            "    List myList = new ArrayList();",
            "  }",
            "}"),
        m_lastEditor.getSource());
    assertAST(m_lastEditor);
  }

  /**
   * We should be able to add new {@link Statement} relative to child in {@link IfStatement}.
   */
  public void test_addStatement_beforeStatement_inIfStatement_thenWithElse() throws Exception {
    createTypeDeclaration_Test(
        "public class Test {",
        "  public Test() {",
        "    if (true)",
        "      System.out.println(0);",
        "    else",
        "      System.out.println(1);",
        "  }",
        "}");
    Statement targetStatement = getNode("System.out.println(0)", Statement.class);
    //
    m_lastEditor.addStatement("int a;", new StatementTarget(targetStatement, true));
    assertEditor(
        getSource(
            "package test;",
            "public class Test {",
            "  public Test() {",
            "    if (true) {",
            "      int a;",
            "      System.out.println(0);",
            "    }",
            "    else",
            "      System.out.println(1);",
            "  }",
            "}"),
        m_lastEditor);
  }

  /**
   * We should be able to add new {@link Statement} relative to child in {@link IfStatement}.
   */
  public void test_addStatement_beforeStatement_inIfStatement_onlyThen() throws Exception {
    createTypeDeclaration_Test(
        "public class Test {",
        "  public Test() {",
        "    if (true)",
        "      System.out.println();",
        "  }",
        "}");
    Statement targetStatement = getNode("System.out.println()", Statement.class);
    //
    m_lastEditor.addStatement("int a;", new StatementTarget(targetStatement, true));
    assertEditor(
        getSource(
            "package test;",
            "public class Test {",
            "  public Test() {",
            "    if (true) {",
            "      int a;",
            "      System.out.println();",
            "    }",
            "  }",
            "}"),
        m_lastEditor);
  }

  public void test_addStatement_x_1() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration("test", "Test.java", "package test;\n"
            + "import java.awt.*; import javax.swing.*;"
            + "class Test extends JPanel {Test() {}}");
    MethodDeclaration targetMethod = typeDeclaration.getMethods()[0];
    // check that we see method of super-class
    m_lastEditor.addStatement("setVisible(false);", new StatementTarget(targetMethod, true));
    assertAST(m_lastEditor);
    assertEquals("package test;\n"
        + "import java.awt.*; import javax.swing.*;"
        + "class Test extends JPanel {Test() {\n\tsetVisible(false);}}", m_lastEditor.getSource());
  }

  public void test_addStatement_x_2() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration("test", "Test.java", "package test;\n"
            + "import java.awt.*; import javax.swing.*;"
            + "class Test extends JPanel {Test() {} int foo(){return 1;}}");
    MethodDeclaration targetMethod = typeDeclaration.getMethods()[0];
    // check that we see method of same class
    m_lastEditor.addStatement("foo();", new StatementTarget(targetMethod, true));
    assertAST(m_lastEditor);
    assertEquals(
        "package test;\n"
            + "import java.awt.*; import javax.swing.*;"
            + "class Test extends JPanel {Test() {\n\tfoo();} int foo(){return 1;}}",
        m_lastEditor.getSource());
  }

  /**
   * Test for {@link ICoreExceptionConstants#AST_PARSE_ERROR} that includes source.
   */
  public void test_addStatement_parseError() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration(
            "test",
            "Test.java",
            getSource("package test;", "public class Test {", "  public Test() {", "  }", "}"));
    MethodDeclaration targetMethod = typeDeclaration.getMethods()[0];
    //
    try {
      m_lastEditor.addStatement("somethingBad();", new StatementTarget(targetMethod, true));
      fail();
    } catch (Throwable e) {
      // "parse error" with Statement source
      DesignerException statementDE = DesignerExceptionUtils.getDesignerException(e);
      assertEquals(ICoreExceptionConstants.AST_PARSE_ERROR, statementDE.getCode());
      {
        String[] parameters = statementDE.getParameters();
        assertThat(parameters).hasSize(2);
        assertThat(parameters[0]).contains("somethingBad();");
        assertThat(parameters[1]).contains("The method somethingBad() is undefined");
      }
      // ASTNode "parse error" is cause
      DesignerException nodeDE = (DesignerException) statementDE.getCause();
      assertEquals(ICoreExceptionConstants.AST_PARSE_ERROR, nodeDE.getCode());
      {
        String[] parameters = nodeDE.getParameters();
        assertThat(parameters).hasSize(2);
        assertThat(parameters[0]).contains("somethingBad();");
        assertThat(parameters[1]).contains("The method somethingBad() is undefined");
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // removeBodyDeclaration
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_removeBodyDeclaration_1() throws Exception {
    // remove inner, it removes trailing spaces
    String code = "int aaa;\tint bbb; int ccc;";
    check_removeBodyDeclaration(code, 1, "int aaa;\tint ccc;");
  }

  public void test_removeBodyDeclaration_2() throws Exception {
    // remove inner, it removes trailing spaces to the EOL
    String code = "int aaa;\n\t\tint bbb;\n\tint ccc;";
    check_removeBodyDeclaration(code, 1, "int aaa;\n\tint ccc;");
  }

  public void test_removeBodyDeclaration_3() throws Exception {
    // remove first
    String code = "int aaa; int bbb; int ccc;";
    check_removeBodyDeclaration(code, 0, "int bbb; int ccc;");
  }

  public void test_removeBodyDeclaration_4() throws Exception {
    // remove last
    String code = "int aaa; int bbb; int ccc;";
    check_removeBodyDeclaration(code, 2, "int aaa; int bbb; ");
  }

  public void test_removeBodyDeclaration_4_2() throws Exception {
    // remove last
    String code = "int aaa;\nint bbb;\nint ccc;\n";
    check_removeBodyDeclaration(code, 2, "int aaa;\nint bbb;\n");
  }

  public void test_removeBodyDeclaration_5() throws Exception {
    // spaces before inner, keep only spaces before \n
    String code = "int aaa; \n\t\t\nint bbb; int ccc;";
    check_removeBodyDeclaration(code, 1, "int aaa; int ccc;");
  }

  public void test_removeBodyDeclaration_6() throws Exception {
    // spaces before first
    String code = "\t\t\n\tint aaa; \n\t\t\nint bbb; int ccc;";
    check_removeBodyDeclaration(code, 0, "\n\t\t\nint bbb; int ccc;");
  }

  public void test_removeBodyDeclaration_EOLC_before() throws Exception {
    // EOL comment after previous
    String code = "int aaa; // EOL comment\nint bbb;\nint ccc;";
    check_removeBodyDeclaration(code, 1, "int aaa; // EOL comment\nint ccc;");
  }

  public void test_removeBodyDeclaration_EOLC_after() throws Exception {
    // EOL comment after this
    String code = "int aaa;\nint bbb; // EOL comment\nint ccc;";
    check_removeBodyDeclaration(code, 1, "int aaa;\nint ccc;");
  }

  private void check_removeBodyDeclaration(String code, int index, String expectedSource)
      throws Exception {
    TypeDeclaration typeDeclaration = createTypeDeclaration_TestC(code);
    // remove declaration
    BodyDeclaration declaration = (BodyDeclaration) typeDeclaration.bodyDeclarations().get(index);
    m_lastEditor.removeBodyDeclaration(declaration);
    // check result
    int start =
        m_lastEditor.getSource().indexOf("{", typeDeclaration.getStartPosition()) + "{".length();
    int end = typeDeclaration.getStartPosition() + typeDeclaration.getLength() - "}".length();
    assertEquals(expectedSource, m_lastEditor.getSource(start, end - start));
    assertAST(m_lastEditor);
  }

  /**
   * Test for {@link AstEditor#removeBodyDeclaration(BodyDeclaration)} for
   * {@link AnonymousClassDeclaration} .
   */
  public void test_removeBodyDeclaration_anonymous() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "class Test extends javax.swing.JPanel {",
            "  public Test() {",
            "    addKeyListener(new java.awt.event.KeyAdapter() {",
            "      public void keyPressed(java.awt.event.KeyEvent e) {",
            "      }",
            "    });",
            "  }",
            "}");
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    ExpressionStatement addStatement =
        (ExpressionStatement) methodDeclaration.getBody().statements().get(0);
    MethodInvocation addInvocation = (MethodInvocation) addStatement.getExpression();
    ClassInstanceCreation listenerCreation =
        (ClassInstanceCreation) addInvocation.arguments().get(0);
    AnonymousClassDeclaration listenerDeclaration = listenerCreation.getAnonymousClassDeclaration();
    MethodDeclaration methodToRemove =
        (MethodDeclaration) listenerDeclaration.bodyDeclarations().get(0);
    // remove
    m_lastEditor.removeBodyDeclaration(methodToRemove);
    assertEditor(
        getSourceDQ(
            "package test;",
            "class Test extends javax.swing.JPanel {",
            "  public Test() {",
            "    addKeyListener(new java.awt.event.KeyAdapter() {",
            "    });",
            "  }",
            "}"),
        m_lastEditor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // removeVariableDeclaration()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Removing field declaration fragments.
   */
  public void test_removeVariableDeclaration_field() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "import javax.swing.*;",
            "class Test extends JPanel {",
            "  private JButton button_1 = new JButton(), button_2, button_3 = new JButton(), button_4;",
            "  Test() {",
            "  }",
            "}");
    FieldDeclaration fieldDeclaration = typeDeclaration.getFields()[0];
    List<VariableDeclarationFragment> fragments = DomGenerics.fragments(fieldDeclaration);
    // remove first
    m_lastEditor.removeVariableDeclaration(fragments.get(0));
    assertEditor(
        getSourceDQ(
            "package test;",
            "import javax.swing.*;",
            "class Test extends JPanel {",
            "  private JButton button_2, button_3 = new JButton(), button_4;",
            "  Test() {",
            "  }",
            "}"),
        m_lastEditor);
    // remove middle
    m_lastEditor.removeVariableDeclaration(fragments.get(1));
    assertEditor(
        getSourceDQ(
            "package test;",
            "import javax.swing.*;",
            "class Test extends JPanel {",
            "  private JButton button_2, button_4;",
            "  Test() {",
            "  }",
            "}"),
        m_lastEditor);
    // remove last
    m_lastEditor.removeVariableDeclaration(fragments.get(1));
    assertEditor(
        getSourceDQ(
            "package test;",
            "import javax.swing.*;",
            "class Test extends JPanel {",
            "  private JButton button_2;",
            "  Test() {",
            "  }",
            "}"),
        m_lastEditor);
    // remove one only
    m_lastEditor.removeVariableDeclaration(fragments.get(0));
    assertEditor(
        getSourceDQ(
            "package test;",
            "import javax.swing.*;",
            "class Test extends JPanel {",
            "  Test() {",
            "  }",
            "}"),
        m_lastEditor);
  }

  /**
   * Removing local variable declaration fragments.
   */
  public void test_removeVariableDeclaration_local() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "import javax.swing.*;",
            "class Test extends JPanel {",
            "  Test() {",
            "    JButton button_1 = new JButton(), button_2, button_3 = new JButton(), button_4;",
            "  }",
            "}");
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    VariableDeclarationStatement variableDeclaration =
        (VariableDeclarationStatement) methodDeclaration.getBody().statements().get(0);
    List<VariableDeclarationFragment> fragments = DomGenerics.fragments(variableDeclaration);
    // remove first
    m_lastEditor.removeVariableDeclaration(fragments.get(0));
    assertEditor(
        getSourceDQ(
            "package test;",
            "import javax.swing.*;",
            "class Test extends JPanel {",
            "  Test() {",
            "    JButton button_2, button_3 = new JButton(), button_4;",
            "  }",
            "}"),
        m_lastEditor);
    // remove middle
    m_lastEditor.removeVariableDeclaration(fragments.get(1));
    assertEditor(
        getSourceDQ(
            "package test;",
            "import javax.swing.*;",
            "class Test extends JPanel {",
            "  Test() {",
            "    JButton button_2, button_4;",
            "  }",
            "}"),
        m_lastEditor);
    // remove last
    m_lastEditor.removeVariableDeclaration(fragments.get(1));
    assertEditor(
        getSourceDQ(
            "package test;",
            "import javax.swing.*;",
            "class Test extends JPanel {",
            "  Test() {",
            "    JButton button_2;",
            "  }",
            "}"),
        m_lastEditor);
    // remove one only
    m_lastEditor.removeVariableDeclaration(fragments.get(0));
    assertEditor(
        getSourceDQ(
            "package test;",
            "import javax.swing.*;",
            "class Test extends JPanel {",
            "  Test() {",
            "  }",
            "}"),
        m_lastEditor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // removeDanglingJavadoc()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstEditor#removeDanglingJavadoc()}.
   * <p>
   * No anything other than spaces until end of {@link TypeDeclaration}.
   */
  public void test_removeDanglingJavadoc_attachedJavaDoc() throws Exception {
    createTypeDeclaration_Test(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "class Test {",
        "  /** not dangling */",
        "  private int javaDocTarget;",
        "}");
    //
    m_lastEditor.removeDanglingJavadoc();
    assertEditor(
        getSource(
            "package test;",
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "class Test {",
            "  /** not dangling */",
            "  private int javaDocTarget;",
            "}"),
        m_lastEditor);
  }

  /**
   * Test for {@link AstEditor#removeDanglingJavadoc()}.
   * <p>
   * No anything other than spaces until end of {@link TypeDeclaration}.
   */
  public void test_removeDanglingJavadoc_onlyWhitespaces() throws Exception {
    createTypeDeclaration_Test(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "class Test {",
        "  /** dangling */",
        "}");
    //
    m_lastEditor.removeDanglingJavadoc();
    assertEditor(
        getSource(
            "package test;",
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "class Test {",
            "}"),
        m_lastEditor);
  }

  /**
   * Test for {@link AstEditor#removeDanglingJavadoc()}.
   * <p>
   * Has line comments between end of {@link Javadoc} and end of {@link TypeDeclaration}.
   */
  public void test_removeDanglingJavadoc_lineComments() throws Exception {
    createTypeDeclaration_Test(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "class Test {",
        "  /** dangling */",
        "  // line comment A",
        "  // line comment B",
        "}");
    //
    m_lastEditor.removeDanglingJavadoc();
    assertEditor(
        getSource(
            "package test;",
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "class Test {",
            "}"),
        m_lastEditor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setJavadoc
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_setJavadoc_setNew() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestD("public class Test {", "  public void foo() {", "  }", "}");
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    // set new JavaDoc
    Javadoc newJavadoc =
        m_lastEditor.setJavadoc(methodDeclaration, new String[]{"first line", "second line"});
    assertNotNull(newJavadoc);
    assertEditor(
        getSource(
            "public class Test {",
            "\t/**",
            "\t * first line",
            "\t * second line",
            "\t */",
            "  public void foo() {",
            "  }",
            "}"),
        m_lastEditor);
  }

  public void test_setJavadoc_replaceExisting() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestD(
            "public class Test {",
            "\t/**",
            "\t * old comment",
            "\t */",
            "  public void foo() {",
            "  }",
            "}");
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    // set new JavaDoc
    Javadoc newJavadoc =
        m_lastEditor.setJavadoc(methodDeclaration, new String[]{"first line", "second line"});
    assertNotNull(newJavadoc);
    assertEditor(
        getSourceDQ(
            "public class Test {",
            "\t/**",
            "\t * first line",
            "\t * second line",
            "\t */",
            "  public void foo() {",
            "  }",
            "}"),
        m_lastEditor);
  }

  public void test_setJavadoc_removeExisting() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestD(
            "// filler filler filler",
            "public class Test {",
            "\t/**",
            "\t * old comment",
            "\t */",
            "  public void foo() {",
            "  }",
            "}");
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    // set new JavaDoc
    Javadoc newJavadoc = m_lastEditor.setJavadoc(methodDeclaration, null);
    assertNull(newJavadoc);
    assertEditor(
        getSourceDQ(
            "// filler filler filler",
            "public class Test {",
            "  public void foo() {",
            "  }",
            "}"),
        m_lastEditor);
  }

  /**
   * No {@link Javadoc}, so remove request is ignored.
   */
  public void test_setJavadoc_removeIgnore() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestD(
            "// filler filler filler",
            "public class Test {",
            "  public void foo() {",
            "  }",
            "}");
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    // set new JavaDoc
    Javadoc newJavadoc = m_lastEditor.setJavadoc(methodDeclaration, null);
    assertNull(newJavadoc);
    assertEditor(
        getSourceDQ(
            "// filler filler filler",
            "public class Test {",
            "  public void foo() {",
            "  }",
            "}"),
        m_lastEditor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setJavadocTagText()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_setJavadocTagText_replaceExisting() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestD(
            "public class Test {",
            "\t/**",
            "\t * @tag aaa bbb ccc",
            "\t */",
            "  public void foo() {",
            "  }",
            "}");
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    // set tag text
    TagElement tagElement =
        m_lastEditor.setJavadocTagText(methodDeclaration, "@tag", " 111 222 333");
    assert_setJavadocTagText_element(tagElement);
    assertEditor(
        getSourceDQ(
            "public class Test {",
            "\t/**",
            "\t * @tag 111 222 333",
            "\t */",
            "  public void foo() {",
            "  }",
            "}"),
        m_lastEditor);
  }

  /**
   * No {@link Javadoc}, add new one with single {@link TagElement}.
   */
  public void test_setJavadocTagText_addJavadoc() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestD(
            "// filler filler filler",
            "public class Test {",
            "  public void foo() {",
            "  }",
            "}");
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    // set tag text
    TagElement tagElement =
        m_lastEditor.setJavadocTagText(methodDeclaration, "@tag", " 111 222 333");
    assert_setJavadocTagText_element(tagElement);
    assertEditor(
        getSourceDQ(
            "// filler filler filler",
            "public class Test {",
            "\t/**",
            "\t * @tag 111 222 333",
            "\t */",
            "  public void foo() {",
            "  }",
            "}"),
        m_lastEditor);
  }

  /**
   * No such {@link TagElement}, add new one with single {@link TextElement}.
   */
  public void test_setJavadocTagText_addTagElement() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestD(
            "public class Test {",
            "\t/**",
            "\t * Some text.",
            "\t * More text.",
            "\t * @otherTag the text.",
            "\t */",
            "  public void foo() {",
            "  }",
            "}");
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    // set tag text
    TagElement tagElement =
        m_lastEditor.setJavadocTagText(methodDeclaration, "@tag", " 111 222 333");
    assert_setJavadocTagText_element(tagElement);
    assertEditor(
        getSourceDQ(
            "public class Test {",
            "\t/**",
            "\t * Some text.",
            "\t * More text.",
            "\t * @otherTag the text.",
            "\t * @tag 111 222 333",
            "\t */",
            "  public void foo() {",
            "  }",
            "}"),
        m_lastEditor);
  }

  /**
   * No such {@link TagElement}, add new one with single {@link TextElement}.
   */
  public void test_setJavadocTagText_addTagElement2() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestD(
            "public class Test {",
            "\t/**",
            "\t * Some text.",
            "\t * More text.",
            "\t */",
            "  public void foo() {",
            "  }",
            "}");
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    // set tag text
    TagElement tagElement =
        m_lastEditor.setJavadocTagText(methodDeclaration, "@tag", " 111 222 333");
    assert_setJavadocTagText_element(tagElement);
    assertEditor(
        getSourceDQ(
            "public class Test {",
            "\t/**",
            "\t * Some text.",
            "\t * More text.",
            "\t * @tag 111 222 333",
            "\t */",
            "  public void foo() {",
            "  }",
            "}"),
        m_lastEditor);
  }

  /**
   * No any {@link TagElement}'s.
   */
  public void test_setJavadocTagText_addTagElement_emptyJavaDoc() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestD(
            "public class Test {",
            "\t/**",
            "\t */",
            "  public void foo() {",
            "  }",
            "}");
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    // add tag
    TagElement tagElement =
        m_lastEditor.setJavadocTagText(methodDeclaration, "@tag", " 111 222 333");
    assert_setJavadocTagText_element(tagElement);
    assertEditor(
        getSourceDQ(
            "public class Test {",
            "\t/**",
            "\t * @tag 111 222 333",
            "\t */",
            "  public void foo() {",
            "  }",
            "}"),
        m_lastEditor);
  }

  public void test_setJavadocTagText_removeExisting() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestD(
            "public class Test {",
            "\t/**",
            "\t * Some text.",
            "\t * @tagBefore the text",
            "\t * @tag aaa bbb ccc",
            "\t * @tagAfter the text",
            "\t * More text.",
            "\t */",
            "  public void foo() {",
            "  }",
            "}");
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    // remove tag
    TagElement tagElement = m_lastEditor.setJavadocTagText(methodDeclaration, "@tag", null);
    assertNull(tagElement);
    assertEditor(
        getSourceDQ(
            "public class Test {",
            "\t/**",
            "\t * Some text.",
            "\t * @tagBefore the text",
            "\t * @tagAfter the text",
            "\t * More text.",
            "\t */",
            "  public void foo() {",
            "  }",
            "}"),
        m_lastEditor);
  }

  /**
   * We remove last/only tag, but there is text also text, so we keep {@link Javadoc}.
   */
  public void test_setJavadocTagText_removeExisting_onlyTextLeft() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestD(
            "public class Test {",
            "\t/**",
            "\t * Just some text",
            "\t * @tag aaa bbb ccc",
            "\t */",
            "  public void foo() {",
            "  }",
            "  // filler filler filler",
            "}");
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    // remove tag
    TagElement tagElement = m_lastEditor.setJavadocTagText(methodDeclaration, "@tag", null);
    assertNull(tagElement);
    assertEditor(
        getSourceDQ(
            "public class Test {",
            "\t/**",
            "\t * Just some text",
            "\t */",
            "  public void foo() {",
            "  }",
            "  // filler filler filler",
            "}"),
        m_lastEditor);
  }

  /**
   * When we remove last/only tag, we should remove {@link Javadoc}.
   */
  public void test_setJavadocTagText_removeExisting_noMoreTags() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestD(
            "public class Test {",
            "\t/**",
            "\t * @tag aaa bbb ccc",
            "\t */",
            "  public void foo() {",
            "  }",
            "  // filler filler filler",
            "}");
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    // remove tag
    TagElement tagElement = m_lastEditor.setJavadocTagText(methodDeclaration, "@tag", null);
    assertNull(tagElement);
    assertEditor(
        getSourceDQ(
            "public class Test {",
            "  public void foo() {",
            "  }",
            "  // filler filler filler",
            "}"),
        m_lastEditor);
  }

  /**
   * No such tag, do nothing.
   */
  public void test_setJavadocTagText_removeNoSuchTag() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestD(
            "public class Test {",
            "\t/**",
            "\t * @otherTag aaa bbb ccc",
            "\t */",
            "  public void foo() {",
            "  }",
            "}");
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    // remove tag
    TagElement tagElement = m_lastEditor.setJavadocTagText(methodDeclaration, "@tag", null);
    assertNull(tagElement);
    assertEditor(
        getSourceDQ(
            "public class Test {",
            "\t/**",
            "\t * @otherTag aaa bbb ccc",
            "\t */",
            "  public void foo() {",
            "  }",
            "}"),
        m_lastEditor);
  }

  /**
   * No {@link Javadoc}, do nothing.
   */
  public void test_setJavadocTagText_removeNoJavadoc() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestD(
            "public class Test {",
            "  public void foo() {",
            "    // some inner comment",
            "  }",
            "}");
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    // remove tag
    TagElement tagElement = m_lastEditor.setJavadocTagText(methodDeclaration, "@tag", null);
    assertNull(tagElement);
    assertEditor(
        getSourceDQ(
            "public class Test {",
            "  public void foo() {",
            "    // some inner comment",
            "  }",
            "}"),
        m_lastEditor);
  }

  public void test_setJavadocTagText_noExistingFragments_noNewFragments() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestD(
            "public class Test {",
            "\t/**",
            "\t * @tag",
            "\t */",
            "  public void foo() {",
            "  }",
            "}");
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    // update tag
    TagElement tagElement = m_lastEditor.setJavadocTagText(methodDeclaration, "@tag", "");
    assert_setJavadocTagText_element(tagElement, "");
    assertEditor(
        getSourceDQ(
            "public class Test {",
            "\t/**",
            "\t * @tag",
            "\t */",
            "  public void foo() {",
            "  }",
            "}"),
        m_lastEditor);
  }

  public void test_setJavadocTagText_noExistingFragments_setNewFragments() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestD(
            "public class Test {",
            "\t/**",
            "\t * @tag",
            "\t */",
            "  public void foo() {",
            "  }",
            "}");
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    // update tag
    TagElement tagElement = m_lastEditor.setJavadocTagText(methodDeclaration, "@tag", " text");
    assert_setJavadocTagText_element(tagElement, " text");
    assertEditor(
        getSourceDQ(
            "public class Test {",
            "\t/**",
            "\t * @tag text",
            "\t */",
            "  public void foo() {",
            "  }",
            "}"),
        m_lastEditor);
  }

  /**
   * When we add import, positions of {@link TagElement} may be broken.
   */
  public void test_setJavadocTagText_bug_1() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestD(
            "// filler filler filler",
            "public class Test {",
            "  public void foo() {",
            "  }",
            "}");
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    // add tag
    TagElement tagElement =
        m_lastEditor.setJavadocTagText(methodDeclaration, "@tag", " 111 222 333");
    assert_setJavadocTagText_element(tagElement);
    assertEditor(
        getSourceDQ(
            "// filler filler filler",
            "public class Test {",
            "\t/**",
            "\t * @tag 111 222 333",
            "\t */",
            "  public void foo() {",
            "  }",
            "}"),
        m_lastEditor);
    // add import
    m_lastEditor.ensureClassImport2("java.util.ArrayList");
    // update tag
    m_lastEditor.setJavadocTagText(methodDeclaration, "@tag", " aaa bbbb ccccc");
    assertEditor(
        getSourceDQ(
            "import java.util.ArrayList;",
            "// filler filler filler",
            "public class Test {",
            "\t/**",
            "\t * @tag aaa bbbb ccccc",
            "\t */",
            "  public void foo() {",
            "  }",
            "}"),
        m_lastEditor);
  }

  /**
   * Assert that given {@link TagElement} has name "@tag" and single {@link TextElement}
   * " 111 222 333".
   */
  private static void assert_setJavadocTagText_element(TagElement tagElement) {
    assert_setJavadocTagText_element(tagElement, " 111 222 333");
  }

  /**
   * Assert that given {@link TagElement} has name "@tag" and single {@link TextElement} with given
   * text.
   */
  private static void assert_setJavadocTagText_element(TagElement tagElement, String text) {
    assertNotNull(tagElement);
    assertEquals("@tag", tagElement.getTagName());
    List<ASTNode> fragments = DomGenerics.fragments(tagElement);
    assertEquals(1, fragments.size());
    assertEquals(text, ((TextElement) fragments.get(0)).getText());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ensureClassImport
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_ensureClassImport_1() throws Exception {
    // ignore java.lang
    String source =
        getSourceDQ(
            "// filler filler filler",
            "package test;",
            "public class Test {",
            "  public Test() {",
            "  }",
            "}");
    assert_ensureClassImport(source, "java.lang.Integer", source, "Integer");
  }

  public void test_ensureClassImport_2() throws Exception {
    // already imported, exact
    String source =
        getSourceDQ(
            "package test;",
            "import java.util.List;",
            "public class Test {",
            "  public Test() {",
            "  }",
            "}");
    assert_ensureClassImport(source, "java.util.List", source, "List");
  }

  public void test_ensureClassImport_3() throws Exception {
    // already imported, on demand
    String source =
        getSourceDQ(
            "package test;",
            "import java.util.*;",
            "public class Test {",
            "  public Test() {",
            "  }",
            "}");
    assert_ensureClassImport(source, "java.util.List", source, "List");
  }

  public void test_ensureClassImport_4() throws Exception {
    // new import, no other imports
    String source_1 =
        getSourceDQ("package test;", "public class Test {", "  public Test() {", "  }", "}");
    String source_2 =
        getSourceDQ(
            "package test;",
            "import java.util.List;",
            "public class Test {",
            "  public Test() {",
            "  }",
            "}");
    assert_ensureClassImport(source_1, "java.util.List", source_2, "List");
  }

  public void test_ensureClassImport_5() throws Exception {
    // new import, other imports
    String source_1 =
        getSourceDQ(
            "package test;",
            "import java.util.Set;",
            "public class Test {",
            "  public Test() {",
            "  }",
            "}");
    String source_2 =
        getSourceDQ(
            "package test;",
            "import java.util.Set;",
            "import java.util.List;",
            "public class Test {",
            "  public Test() {",
            "  }",
            "}");
    assert_ensureClassImport(source_1, "java.util.List", source_2, "List");
  }

  public void test_ensureClassImport_6() throws Exception {
    // conflict with exact import
    String source =
        getSourceDQ(
            "package test;",
            "import java.util.List;",
            "public class Test {",
            "  public Test() {",
            "  }",
            "}");
    assert_ensureClassImport(source, "java.awt.List", source, "java.awt.List");
  }

  public void test_ensureClassImport_7() throws Exception {
    // on demand imports, no conflict
    String source_1 =
        getSourceDQ(
            "package test;",
            "import java.util.*;",
            "public class Test {",
            "  public Test() {",
            "  }",
            "}");
    String source_2 =
        getSourceDQ(
            "package test;",
            "import java.util.*;",
            "import java.awt.List;",
            "public class Test {",
            "  public Test() {",
            "  }",
            "}");
    assert_ensureClassImport(source_1, "java.awt.List", source_2, "List");
  }

  public void test_ensureClassImport_8() throws Exception {
    // on demand imports, conflict
    String source =
        getSourceDQ(
            "package test;",
            "import java.util.*;",
            "public class Test {",
            "  private List m_list;",
            "  public Test() {",
            "  }",
            "}");
    assert_ensureClassImport(source, "java.awt.List", source, "java.awt.List");
  }

  /**
   * Import for private inner class.
   */
  public void test_ensureClassImport_9() throws Exception {
    String source =
        getSourceDQ(
            "package test;",
            "import java.util.*;",
            "public class Test {",
            "  private class A {};",
            "  public Test() {",
            "  }",
            "}");
    assert_ensureClassImport(source, "test.Test.A", source, "A");
  }

  /**
   * Ignore import for type in same package.
   */
  public void test_ensureClassImport_10() throws Exception {
    setFileContentSrc("test/A.java", getSourceDQ("package test;", "public class A {", "}"));
    waitForAutoBuild();
    //
    String source =
        getSourceDQ("package test;", "public class Test {", "  public Test() {", "  }", "}");
    assert_ensureClassImport(source, "test.A", source, "A");
  }

  /**
   * Test for unit in default package.
   */
  public void test_ensureClassImport_11() throws Exception {
    createASTCompilationUnit(
        "",
        "Test.java",
        getSourceDQ("public class Test {", "  public Test() {", "  }", "}"));
    m_lastEditor.ensureClassImport2("java.util.List");
    assertEditor(
        getSourceDQ(
            "import java.util.List;",
            "public class Test {",
            "  public Test() {",
            "  }",
            "}"),
        m_lastEditor);
  }

  private void assert_ensureClassImport(String initialSource,
      String classToImport,
      String expectedSource,
      String expectedClassName) throws Exception {
    createASTCompilationUnit("test", "Test.java", initialSource);
    //
    String actualClassName = m_lastEditor.ensureClassImport2(classToImport);
    assertEquals(expectedClassName, actualClassName);
    //
    assertEquals(expectedSource, m_lastEditor.getSource());
    assertAST(m_lastEditor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ASTCodeGeneration.getEndOfLine
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_ASTCG_getEndOfLine_0_default() throws Exception {
    createTypeDeclaration_TestC("private int m_value = 12345;");
    AstCodeGeneration generation = m_lastEditor.getGeneration();
    assertSame(AstCodeGeneration.DEFAULT_END_OF_LINE, generation.getEndOfLine());
  }

  public void test_ASTCG_getEndOfLine_1_r() throws Exception {
    createTypeDeclaration_TestC("private int m_value = 12345;\r");
    AstCodeGeneration generation = m_lastEditor.getGeneration();
    assertEquals("\r", generation.getEndOfLine());
  }

  public void test_ASTCG_getEndOfLine_2_n() throws Exception {
    createTypeDeclaration_TestC("private int m_value = 12345;\n");
    AstCodeGeneration generation = m_lastEditor.getGeneration();
    assertEquals("\n", generation.getEndOfLine());
  }

  public void test_ASTCG_getEndOfLine_3_rn() throws Exception {
    createTypeDeclaration_TestC("private int m_value = 12345;\r\n");
    AstCodeGeneration generation = m_lastEditor.getGeneration();
    assertEquals("\r\n", generation.getEndOfLine());
  }

  public void test_ASTCG_getEndOfLine_4_nr() throws Exception {
    createTypeDeclaration_TestC("private int m_value = 12345;\n\r");
    AstCodeGeneration generation = m_lastEditor.getGeneration();
    assertEquals("\n", generation.getEndOfLine());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ASTCodeGeneration.getIndentation
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_ASTCG_getIndentation_0() throws Exception {
    createTypeDeclaration_TestC("");
    AstCodeGeneration generation = m_lastEditor.getGeneration();
    assertSame("", generation.getIndentation(0));
  }

  public void test_ASTCG_getIndentation_1_tab() throws Exception {
    createTypeDeclaration_TestC("");
    AstCodeGeneration generation = m_lastEditor.getGeneration();
    assertEquals("\t", generation.getIndentation(1));
    assertEquals("\t\t", generation.getIndentation(2));
  }

  public void test_ASTCG_getIndentation_2_space() throws Exception {
    createTypeDeclaration_TestC("");
    AstCodeGeneration generation = m_lastEditor.getGeneration();
    // configure for spaces
    IJavaProject javaProject = m_lastEditor.getModelUnit().getJavaProject();
    javaProject.setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, "space");
    javaProject.setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
    // do checks
    try {
      assertSame("", generation.getIndentation(0));
      assertEquals(StringUtils.repeat(" ", 4), generation.getIndentation(1));
      // increase tab size
      javaProject.setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "8");
      assertEquals(StringUtils.repeat(" ", 8), generation.getIndentation(1));
      // set bad tab size
      javaProject.setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "x");
      assertEquals(StringUtils.repeat(" ", 4), generation.getIndentation(1));
    } finally {
      // restore original formatter settings
      javaProject.setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, "tab");
      javaProject.setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "1");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ASTCodeGeneration.getUseCompactAssignment
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_ASTCG_getUseCompactAssignment() throws Exception {
    createTypeDeclaration_TestC("");
    AstCodeGeneration generation = m_lastEditor.getGeneration();
    IJavaProject javaProject = m_lastEditor.getModelUnit().getJavaProject();
    //
    javaProject.setOption(
        DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATOR,
        JavaCore.INSERT);
    assertEquals(false, generation.getUseCompactAssignment());
    //
    javaProject.setOption(
        DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATOR,
        JavaCore.DO_NOT_INSERT);
    assertEquals(true, generation.getUseCompactAssignment());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ASTCodeGeneration.getMethodBraceSeparator
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_ASTCG_getMethodBraceSeparator() throws Exception {
    createTypeDeclaration_TestC("");
    AstCodeGeneration generation = m_lastEditor.getGeneration();
    //
    assertSame(" ", generation.getMethodBraceSeparator("\t\t"));
    //
    IJavaProject javaProject = m_lastEditor.getModelUnit().getJavaProject();
    javaProject.setOption(
        DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_METHOD_DECLARATION,
        DefaultCodeFormatterConstants.NEXT_LINE);
    assertEquals(Expectations.get(
        "\r\n\t",
        new StrValue("flanker-desktop", "\n\t"),
        new StrValue("scheglov-macpro", "\n\t")), generation.getMethodBraceSeparator("\t"));
    assertEquals(Expectations.get(
        "\r\n\t\t",
        new StrValue("flanker-desktop", "\n\t\t"),
        new StrValue("scheglov-macpro", "\n\t\t")), generation.getMethodBraceSeparator("\t\t"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Templates: {wbp_class}
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for replacing {wbp_class} with "getClass()".
   */
  public void test_classAccess_addStatement_getClass() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestC(getSourceDQ("  public void foo() {", "    int marker;", "  }"));
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    // prepare target for new statement
    StatementTarget target;
    {
      Statement statement = (Statement) methodDeclaration.getBody().statements().get(0);
      target = new StatementTarget(statement, false);
    }
    // check new statement
    m_lastEditor.addStatement("Class clazz = {wbp_class};", target);
    assertEquals(
        getSourceDQ(
            "  public void foo() {",
            "    int marker;",
            "    Class clazz = getClass();",
            "  }").trim(),
        m_lastEditor.getSource(methodDeclaration));
    assertAST(m_lastEditor);
  }

  /**
   * Test for replacing {wbp_class} with {@link TypeLiteral}.
   */
  public void test_classAccess_addStatement_TypeLiteral() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestC(getSourceDQ(
            "  public static void foo() {",
            "    int marker;",
            "  }"));
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    // prepare target for new statement
    StatementTarget target;
    {
      Statement statement = (Statement) methodDeclaration.getBody().statements().get(0);
      target = new StatementTarget(statement, false);
    }
    // check new statement
    m_lastEditor.addStatement("Class clazz = {wbp_class};", target);
    assertEquals(
        getSourceDQ(
            "  public static void foo() {",
            "    int marker;",
            "    Class clazz = Test.class;",
            "  }").trim(),
        m_lastEditor.getSource(methodDeclaration));
    assertAST(m_lastEditor);
  }

  /**
   * Test for {@link AstEditor#replaceExpression(Expression, String)}.
   */
  public void test_classAccess_replaceExpression_TypeLiteral() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_TestC(getSourceDQ(
            "  public void foo() {",
            "    Class clazz = null;",
            "  }"));
    VariableDeclarationStatement statement =
        (VariableDeclarationStatement) typeDeclaration.getMethods()[0].getBody().statements().get(0);
    VariableDeclaration declaration = (VariableDeclaration) statement.fragments().get(0);
    //
    Expression newExpression =
        m_lastEditor.replaceExpression(declaration.getInitializer(), "{wbp_class}");
    assertInstanceOf(MethodInvocation.class, newExpression);
    assertAST(m_lastEditor);
  }

  /**
   * Test for replacing {wbp_classTop} with {@link TypeLiteral} of top-level class.
   */
  public void test_classAccessTop() throws Exception {
    TypeDeclaration testType =
        createTypeDeclaration_Test(
            "public class Test {",
            "  public class Inner {",
            "    public void foo() {",
            "    }",
            "  }",
            "}");
    TypeDeclaration innerType = testType.getTypes()[0];
    MethodDeclaration fooMethod = innerType.getMethods()[0];
    StatementTarget target = new StatementTarget(fooMethod.getBody(), false);
    // check new statement
    m_lastEditor.addStatement("Class clazz = {wbp_classTop};", target);
    assertEditor(
        getSourceDQ(
            "package test;",
            "public class Test {",
            "  public class Inner {",
            "    public void foo() {",
            "      Class clazz = Test.class;",
            "    }",
            "  }",
            "}"),
        m_lastEditor);
    assertAST(m_lastEditor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setIdentifier()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_setIdentifier() throws Exception {
    createTypeDeclaration_Test(
        "// filler filler filler",
        "class Test {",
        "  private int m_value = 0;",
        "}");
    // 
    SimpleName fieldName = (SimpleName) m_lastEditor.getEnclosingNode("m_value");
    m_lastEditor.setIdentifier(fieldName, "foo");
    assertEditor(
        getSourceDQ(
            "package test;",
            "// filler filler filler",
            "class Test {",
            "  private int foo = 0;",
            "}"),
        m_lastEditor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ASTParser
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * "Inner" is invisible type, so we can not declare variables with this type during parsing
   * expression.
   */
  public void test_ASTParser_invisibleType() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  private static class Inner {",
            "  }",
            "  private Inner m_inner;",
            "}");
    waitForAutoBuild();
    //
    int position = typeDeclaration.getFields()[0].getStartPosition();
    m_lastEditor.getParser().parseExpression(position, "12345");
  }

  /**
   * If some type is unknown, i.e. we don't have its {@link ITypeBinding}, then just ignore it.
   */
  public void test_ASTParser_unknownFieldType() throws Exception {
    m_ignoreModelCompileProblems = true;
    createTypeDeclaration(
        "test",
        "Test.java",
        getSource(
            "package test;",
            "public class Test {",
            "  NoSuchType m_field;",
            "  public Test() {",
            "    // marker",
            "  }",
            "}"));
    // parse expression
    int position = m_lastEditor.indexOf("// marker");
    check_ASTParser_parseExpression(position, "123");
  }

  /**
   * If some type is unknown, i.e. we don't have its {@link ITypeBinding}, then just ignore it.
   */
  public void test_ASTParser_unknownMethodType() throws Exception {
    m_ignoreModelCompileProblems = true;
    createTypeDeclaration(
        "test",
        "Test.java",
        getSource(
            "package test;",
            "public class Test {",
            "  public Test() {",
            "    // marker",
            "  }",
            "  private NoSuchType foo() {;",
            "    return null;",
            "  }",
            "}"));
    // parse expression
    int position = m_lastEditor.indexOf("// marker");
    check_ASTParser_parseExpression(position, "123");
  }

  /**
   * Test for correct position for parsed nodes.
   */
  public void test_ASTParser_1_parseField() throws Exception {
    TypeDeclaration typeDeclaration = createTypeDeclaration_TestC("int a;");
    AstParser parser = m_lastEditor.getParser();
    //
    int position = typeDeclaration.getFields()[0].getStartPosition();
    BodyDeclaration newField = parser.parseBodyDeclaration(position, "private int m_value;");
    assertEquals(position, newField.getStartPosition());
  }

  /**
   * Test that imported classes can be referenced during parsing.
   */
  public void test_ASTParser_2_imports() throws Exception {
    ICompilationUnit modelUnit =
        createModelCompilationUnit(
            "test",
            "Test.java",
            "import java.util.List; public class Test {int a;}");
    AstEditor editor = new AstEditor(modelUnit);
    TypeDeclaration typeDeclaration = (TypeDeclaration) editor.getAstUnit().types().get(0);
    AstParser parser = editor.getParser();
    //
    int position = typeDeclaration.getFields()[0].getStartPosition();
    BodyDeclaration newField = parser.parseBodyDeclaration(position, "private List m_values;");
    assertNotNull(newField);
  }

  /**
   * Test that bindings for {@link MethodInvocation} are copied.
   */
  public void test_ASTParser_3_getMethodBinding() throws Exception {
    ICompilationUnit modelUnit =
        createModelCompilationUnit(
            "test",
            "Test.java",
            "import java.util.*; public class Test {int a;}");
    AstEditor editor = new AstEditor(modelUnit);
    TypeDeclaration typeDeclaration = (TypeDeclaration) editor.getAstUnit().types().get(0);
    AstParser parser = editor.getParser();
    //
    int position = typeDeclaration.getFields()[0].getStartPosition();
    FieldDeclaration newField =
        (FieldDeclaration) parser.parseBodyDeclaration(
            position,
            "private List m_values = Collections.singletonList(null);");
    assertNotNull(newField);
    //
    VariableDeclarationFragment fragment =
        (VariableDeclarationFragment) newField.fragments().get(0);
    MethodInvocation invocation = (MethodInvocation) fragment.getInitializer();
    //
    assertNull(invocation.resolveMethodBinding());
    IMethodBinding methodBinding = AstNodeUtils.getMethodBinding(invocation);
    assertNotNull(methodBinding);
    assertEquals("singletonList(java.lang.Object)", AstNodeUtils.getMethodSignature(methodBinding));
  }

  /**
   * Test for {@link IVariableBinding} for newly added {@link FieldAccess}.
   */
  public void test_ASTParser_FieldAccess() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  private int m_value;",
            "  public Test() {",
            "  }",
            "}");
    MethodDeclaration methodDeclaration =
        AstNodeUtils.getMethodBySignature(typeDeclaration, "<init>()");
    StatementTarget statementTarget = new StatementTarget(methodDeclaration, true);
    //
    ExpressionStatement statement =
        (ExpressionStatement) m_lastEditor.addStatement("this.m_value = 0;", statementTarget);
    Assignment assignment = (Assignment) statement.getExpression();
    FieldAccess fieldAccess = (FieldAccess) assignment.getLeftHandSide();
    //
    assertTrue(AstNodeUtils.isVariable(fieldAccess));
    assertEquals(fieldAccess.getName(), AstNodeUtils.getVariableSimpleName(fieldAccess));
    assertEquals("m_value", AstNodeUtils.getVariableName(fieldAccess));
  }

  /**
   * Test that {@link SingleVariableDeclaration} gets {@link IVariableBinding} during parsing.
   */
  public void test_ASTParser_SingleVariableDeclaration_IVariableBinding() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "// filler filler filler",
            "public class Test {",
            "  int field;",
            "}");
    //
    int position = typeDeclaration.getFields()[0].getStartPosition();
    MethodDeclaration method =
        (MethodDeclaration) m_lastEditor.getParser().parseBodyDeclaration(
            position,
            "void foo(int a, String b) {}");
    List<SingleVariableDeclaration> parameters = DomGenerics.parameters(method);
    {
      SingleVariableDeclaration parameter = parameters.get(0);
      IVariableBinding variableBinding = AstNodeUtils.getVariableBinding(parameter);
      assertNotNull(variableBinding);
      assertEquals("a", variableBinding.getName());
    }
    {
      SingleVariableDeclaration parameter = parameters.get(1);
      IVariableBinding variableBinding = AstNodeUtils.getVariableBinding(parameter);
      assertNotNull(variableBinding);
      assertEquals("b", variableBinding.getName());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ASTParser - names
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_ASTParser_parseSimpleName() throws Exception {
    createTypeDeclaration_TestC("");
    AstParser parser = m_lastEditor.getParser();
    //
    SimpleName simpleName = parser.parseSimpleName(10, "abc");
    check_SimpleName(simpleName, "abc", 10, 3);
  }

  public void test_ASTParser_parseQualifiedName() throws Exception {
    createTypeDeclaration_TestC("");
    AstParser parser = m_lastEditor.getParser();
    //
    QualifiedName qualifiedName = parser.parseQualifiedName(10, "a.b.c");
    assertEquals(10, qualifiedName.getStartPosition());
    assertEquals(5, qualifiedName.getLength());
    //
    check_SimpleName(qualifiedName.getName(), "c", 14, 1);
    qualifiedName = (QualifiedName) qualifiedName.getQualifier();
    //
    check_SimpleName(qualifiedName.getName(), "b", 12, 1);
    check_SimpleName((SimpleName) qualifiedName.getQualifier(), "a", 10, 1);
  }

  private void check_SimpleName(SimpleName simpleName, String identifier, int position, int length) {
    assertEquals(identifier, simpleName.getIdentifier());
    assertEquals(position, simpleName.getStartPosition());
    assertEquals(length, simpleName.getLength());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ASTParser - parseType
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_ASTParser_parseType_1() throws Exception {
    // String, from java.lang
    check_ASTParser_parseType("String m_value;");
  }

  public void test_ASTParser_parseType_2() throws Exception {
    // fully qualified name
    check_ASTParser_parseType("java.util.List m_value;");
  }

  public void test_ASTParser_parseType_3() throws Exception {
    // primitive
    check_ASTParser_parseType("int m_value;");
  }

  private void check_ASTParser_parseType(String code) throws Exception {
    // parse, prepare source type
    TypeDeclaration typeDeclaration = createTypeDeclaration_TestC(code);
    Type sourceType = typeDeclaration.getFields()[0].getType();
    // create new type
    Type newType = m_lastEditor.getParser().parseType(10, sourceType);
    // check ranges
    assertEquals(sourceType.toString(), newType.toString());
    assertEquals(10, newType.getStartPosition());
    assertEquals(sourceType.toString().length(), newType.getLength());
    // check type binding
    {
      ITypeBinding sourceBinding = AstNodeUtils.getTypeBinding(sourceType);
      ITypeBinding newBinding = AstNodeUtils.getTypeBinding(newType);
      assertNotNull(newBinding);
      assertEquals(
          AstNodeUtils.getFullyQualifiedName(sourceBinding, false),
          AstNodeUtils.getFullyQualifiedName(newBinding, false));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ASTParser - parseVariable
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_ASTParser_parseVariable() throws Exception {
    TypeDeclaration typeDeclaration = createTypeDeclaration_TestC("String m_value;");
    ITypeBinding sourceTypeBinding = typeDeclaration.getFields()[0].getType().resolveBinding();
    //
    SimpleName variable =
        m_lastEditor.getParser().parseVariable(
            10,
            "abc",
            null,
            sourceTypeBinding,
            false,
            Modifier.FINAL);
    assertEquals("abc", variable.getIdentifier());
    assertEquals(10, variable.getStartPosition());
    assertEquals(3, variable.getLength());
    // check IVariableBinding
    {
      IVariableBinding variableBinding = AstNodeUtils.getVariableBinding(variable);
      assertNotNull(variableBinding);
      assertEquals("abc", variableBinding.getName());
      assertFalse(variableBinding.isField());
      assertEquals(Modifier.FINAL, variableBinding.getModifiers());
    }
    // check ITypeBinding
    {
      ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(variable);
      assertNotNull(typeBinding);
      assertEquals("java.lang.String", AstNodeUtils.getFullyQualifiedName(typeBinding, false));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ASTParser - parseSimpleType
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_ASTParser_parseSimpleType() throws Exception {
    TypeDeclaration typeDeclaration = createTypeDeclaration_TestC("String m_value;");
    ITypeBinding sourceTypeBinding = typeDeclaration.getFields()[0].getType().resolveBinding();
    //
    SimpleType type = m_lastEditor.getParser().parseSimpleType(10, "String", sourceTypeBinding);
    assertEquals(10, type.getStartPosition());
    assertEquals(6, type.getLength());
    //
    // check ITypeBinding
    {
      ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(type);
      assertNotNull(typeBinding);
      assertEquals("java.lang.String", AstNodeUtils.getFullyQualifiedName(typeBinding, false));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ASTParser - parseQualifiedType
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstParser#parseQualifiedType(int, String)}.
   */
  public void test_ASTParser_parseQualifiedType_onlyString() throws Exception {
    createASTCompilationUnit(
        "test",
        "Test.java",
        getSourceDQ(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "package test;",
            "public class Test {",
            "}"));
    int position = getNode("Test {").getStartPosition();
    AstParser parser = m_lastEditor.getParser();
    // raw type
    {
      Type newType = parser.parseQualifiedType(position, "java.lang.String");
      assertEquals(position, newType.getStartPosition());
      assertEquals("java.lang.String", AstNodeUtils.getFullyQualifiedName(newType, false));
    }
    // parametrized
    {
      Type newType = parser.parseQualifiedType(position, "java.util.List<java.lang.String>");
      assertEquals(position, newType.getStartPosition());
      ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(newType);
      assertEquals("java.util.List", AstNodeUtils.getFullyQualifiedName(typeBinding, false));
      assertEquals(
          "java.util.List<java.lang.String>",
          AstNodeUtils.getFullyQualifiedName(typeBinding, false, true));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ASTParser - parseExpression
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Parsing {@link MethodInvocation}.
   */
  public void test_ASTParser_parseExpression_MethodInvocation() throws Exception {
    check_ASTParser_parseExpression("System.out.println()");
  }

  /**
   * Parsing number expression.
   */
  public void test_ASTParser_parseExpression_NumberLiteral() throws Exception {
    check_ASTParser_parseExpression("12345");
  }

  /**
   * Parsing <code>null</code>.
   */
  public void test_ASTParser_parseExpression_NullLiteral() throws Exception {
    check_ASTParser_parseExpression("null");
  }

  /**
   * Parsing for when super class has no default constructors.
   */
  public void test_ASTParser_parseExpression_NoSuperDefaultConstructor() throws Exception {
    // prepare super class
    setFileContentSrc(
        "test/ComplexPanel.java",
        getSourceDQ(
            "package test;",
            "public class ComplexPanel {",
            "  public ComplexPanel(String text, int alignment) {",
            "  }",
            "}"));
    waitForAutoBuild();
    // prepare type
    CompilationUnit compilationUnit =
        createASTCompilationUnit(
            "test",
            "Test.java",
            getSourceDQ(
                "package test;",
                "public class Test extends ComplexPanel {",
                "  public Test() {",
                "    super(null, 0);",
                "  }",
                "}"));
    TypeDeclaration typeDeclaration = (TypeDeclaration) compilationUnit.types().get(0);
    // parse expression
    int position = typeDeclaration.getStartPosition();
    Expression expression = m_lastEditor.getParser().parseExpression(position, "123");
    assertNotNull(expression);
  }

  /**
   * Parsing reference on local {@link FieldDeclaration}.
   */
  public void test_ASTParser_parseExpression_referenceLocalFieldDeclaration() throws Exception {
    // prepare type
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  private static final int ID = 555;",
            "  public Test() {",
            "  }",
            "}");
    // parse expression
    int position = typeDeclaration.getStartPosition();
    SimpleName simpleName = (SimpleName) m_lastEditor.getParser().parseExpression(position, "ID");
    assertNotNull(simpleName);
    {
      IVariableBinding variableBinding = AstNodeUtils.getVariableBinding(simpleName);
      assertNotNull(variableBinding);
      assertNotNull(variableBinding.getDeclaringClass());
      assertEquals(
          "test.Test",
          AstNodeUtils.getFullyQualifiedName(variableBinding.getDeclaringClass(), false));
    }
  }

  public void test_ASTParser_parseExpression_referenceLocalStaticMethod() throws Exception {
    // prepare type
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "import javax.swing.*;",
            "public class Test {",
            "  public Test() {",
            "  }",
            "  private static JButton createButton() {",
            "    return new JButton();",
            "  }",
            "}");
    // parse expression
    int position = typeDeclaration.getStartPosition();
    Expression expression =
        m_lastEditor.getParser().parseExpression(position, "test.Test.createButton()");
    assertNotNull(expression);
  }

  /**
   * Use inner type.
   */
  public void test_ASTParser_parseExpression_innerType() throws Exception {
    CompilationUnit compilationUnit =
        createASTCompilationUnit(
            "test",
            "Test.java",
            getSourceDQ(
                "package test;",
                "public class Test {",
                "  private class Inner extends java.util.ArrayList implements java.util.List, java.io.Serializable {}",
                "}"));
    TypeDeclaration typeDeclaration = (TypeDeclaration) compilationUnit.types().get(0);
    // parse expression
    int position = typeDeclaration.getStartPosition();
    Expression expression = m_lastEditor.getParser().parseExpression(position, "new Inner()");
    assertNotNull(expression);
  }

  /**
   * Parsing of {@link SuperMethodInvocation} and its {@link IMethodBinding}.
   */
  public void test_ASTParser_parseExpression_SuperMethodInvocation() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "// filler filler filler",
            "// filler filler filler",
            "public class Test {",
            "}");
    // parse expression
    int position = typeDeclaration.getStartPosition();
    SuperMethodInvocation invocation =
        (SuperMethodInvocation) m_lastEditor.getParser().parseExpression(
            position,
            "super.toString()");
    assertNotNull(invocation);
    IMethodBinding methodBinding = AstNodeUtils.getMethodBinding(invocation);
    assertEquals("toString()", AstNodeUtils.getMethodSignature(methodBinding));
  }

  /**
   * Use {@link AnonymousClassDeclaration}.
   */
  public void test_ASTParser_parseExpression_anonymous() throws Exception {
    CompilationUnit compilationUnit =
        createASTCompilationUnit(
            "test",
            "Test.java",
            getSourceDQ("package test;", "public class Test {", "}"));
    TypeDeclaration typeDeclaration = (TypeDeclaration) compilationUnit.types().get(0);
    // parse expression
    int position = typeDeclaration.getStartPosition();
    ClassInstanceCreation creation =
        (ClassInstanceCreation) m_lastEditor.getParser().parseExpression(
            position,
            getSourceDQ(
                "new java.awt.event.KeyAdapter() {",
                "  public void keyTyped(java.awt.event.KeyEvent e) {",
                "  }",
                "}"));
    AnonymousClassDeclaration anonymousClassDeclaration = creation.getAnonymousClassDeclaration();
    assertNotNull(anonymousClassDeclaration);
    TypeDeclaration anonymousTypeDeclaration =
        AnonymousTypeDeclaration.create(anonymousClassDeclaration);
    ITypeBinding binding = AstNodeUtils.getTypeBinding(anonymousTypeDeclaration);
    assertEquals(
        "java.awt.event.KeyAdapter",
        AstNodeUtils.getFullyQualifiedName(binding.getSuperclass(), false));
  }

  /**
   * Class may declare final field, but not initialize it at declaration (initialize in
   * constructor). We should correctly parse in this case.
   */
  public void test_ASTParser_parseExpression_notInitializedFinalField() throws Exception {
    // prepare type
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  private final int m_finalField_0;",
            "  private final Object m_finalField_1;",
            "  public Test() {",
            "    m_finalField_0 = 123;",
            "    m_finalField_1 = new Object();",
            "  }",
            "}");
    // parse expression
    int position = typeDeclaration.getStartPosition();
    String expressionSource = "1 + 2";
    check_ASTParser_parseExpression(position, expressionSource);
  }

  public void test_ASTParser_parseExpression_publicStaticFinalField_inInnerClass() throws Exception {
    // prepare type
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  private class Inner {",
            "    private static final int CONS = 555;",
            "  }",
            "  public Test() {",
            "    // marker",
            "  }",
            "}");
    // parse expression
    int position = typeDeclaration.getStartPosition();
    String expressionSource = "Inner.CONS";
    check_ASTParser_parseExpression(position, expressionSource);
  }

  /**
   * When context class is "System", this caused parsing problem.
   */
  public void test_ASTParser_parseExpression_classSystem() throws Exception {
    // prepare type
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  private class System {",
            "  }",
            "  public Test() {",
            "    // marker",
            "  }",
            "}");
    // parse expression
    int position = typeDeclaration.getStartPosition();
    String expressionSource = "123";
    check_ASTParser_parseExpression(position, expressionSource);
  }

  /**
   * If project configured to consider {@link JavaCore#COMPILER_PB_NULL_REFERENCE} as error, we may
   * fail, because we declare {@link Object} variables as <code>null</code>.
   */
  public void test_ASTParser_parseExpression_warning_nullReference() throws Exception {
    createTypeDeclaration_Test(
        "public class Test {",
        "  public Test() {",
        "    Object var = new Object();",
        "    // marker",
        "  }",
        "}");
    // configure IJavaProject to handle warning as error
    {
      IJavaProject javaProject = m_lastEditor.getModelUnit().getJavaProject();
      javaProject.setOption(JavaCore.COMPILER_PB_NULL_REFERENCE, "error");
    }
    // parse expression
    try {
      int position = m_lastEditor.indexOf("// marker");
      String expressionSource = "var.hashCode()";
      check_ASTParser_parseExpression(position, expressionSource);
    } finally {
      do_projectDispose();
    }
  }

  /**
   * If project configured to consider {@link JavaCore#COMPILER_PB_UNUSED_IMPORT} as error, we
   * should ignore this.
   */
  public void test_ASTParser_parseExpression_warning_unusedImport() throws Exception {
    createTypeDeclaration_Test(
        "import java.util.List;",
        "public class Test {",
        "  public Test() {",
        "    // marker",
        "  }",
        "}");
    // configure IJavaProject to handle warning as error
    {
      IJavaProject javaProject = m_lastEditor.getModelUnit().getJavaProject();
      javaProject.setOption(JavaCore.COMPILER_PB_UNUSED_IMPORT, "error");
    }
    // parse expression
    try {
      int position = m_lastEditor.indexOf("// marker");
      String expressionSource = "12345";
      check_ASTParser_parseExpression(position, expressionSource);
    } finally {
      do_projectDispose();
    }
  }

  /**
   * When build context for parsing, "@Override" annotations should be included to prevent
   * {@link JavaCore#COMPILER_PB_MISSING_OVERRIDE_ANNOTATION} warning as error.
   */
  public void test_ASTParser_parseExpression_warning_useOverride() throws Exception {
    setFileContentSrc(
        "test/Test_0.java",
        getSource("package test;", "public class Test_0 {", "  public void fooBar() {", "  }", "}"));
    createTypeDeclaration(
        "test",
        "Test.java",
        getSource(
            "package test;",
            "public class Test extends Test_0 {",
            "  @Override",
            "  public void fooBar() {",
            "    // marker",
            "  }",
            "}"));
    // configure IJavaProject to handle warning as error
    {
      IJavaProject javaProject = m_lastEditor.getModelUnit().getJavaProject();
      javaProject.setOption(JavaCore.COMPILER_PB_MISSING_OVERRIDE_ANNOTATION, "error");
    }
    // parse expression
    try {
      int position = m_lastEditor.indexOf("// marker");
      String expressionSource = "12345";
      check_ASTParser_parseExpression(position, expressionSource);
    } finally {
      do_projectDispose();
    }
  }

  /**
   * If project configured to consider {@link JavaCore#COMPILER_PB_UNNECESSARY_TYPE_CHECK} as error,
   * we may fail, because we declare visible variables with casts.
   */
  public void test_ASTParser_parseExpression_warning_unnecessaryCast() throws Exception {
    createTypeDeclaration_Test(
        "public class Test {",
        "  public Test() {",
        "    Object var;",
        "    // marker",
        "  }",
        "}");
    // configure IJavaProject to handle warning as error
    {
      IJavaProject javaProject = m_lastEditor.getModelUnit().getJavaProject();
      javaProject.setOption(JavaCore.COMPILER_PB_UNNECESSARY_TYPE_CHECK, "error");
    }
    // parse expression
    try {
      int position = m_lastEditor.indexOf("// marker");
      String expressionSource = "var = (java.lang.Object) null";
      check_ASTParser_parseExpression(position, expressionSource);
    } finally {
      do_projectDispose();
    }
  }

  /**
   * Test for handling several {@link VariableDeclarationFragment}'s in {@link FieldDeclaration}.
   */
  public void test_ASTParser_parseExpression_severalVariableDeclarationFragments_inFieldDeclaration()
      throws Exception {
    createTypeDeclaration_Test(
        "public class Test {",
        "  private int a, b;",
        "  public Test() {",
        "    // marker",
        "  }",
        "}");
    // parse expression
    int position = m_lastEditor.indexOf("// marker");
    check_ASTParser_parseExpression(position, "123");
  }

  /**
   * Test for handling generics: in class.
   */
  public void test_ASTParser_parseExpression_generics() throws Exception {
    createTypeDeclaration_Test(
        "public class Test {",
        "  private static class Inner implements Comparable<Inner> {",
        "    public int compareTo(Inner o) {",
        "      return 0;",
        "    }",
        "  }",
        "  public Test() {",
        "    // marker",
        "  }",
        "}");
    // parse expression
    int position = m_lastEditor.indexOf("// marker");
    check_ASTParser_parseExpression(position, "123");
  }

  /**
   * Test for handling generics: in method.
   */
  public void test_ASTParser_parseExpression_generics2() throws Exception {
    createTypeDeclaration_Test(
        "public class Test {",
        "  public Test() {",
        "    // marker",
        "  }",
        "  <T> void foo(T o) {",
        "  }",
        "}");
    // parse expression
    int position = m_lastEditor.indexOf("// marker");
    check_ASTParser_parseExpression(position, "123");
  }

  /**
   * Test for {@link MethodDeclaration} with annotation.
   */
  public void test_ASTParser_parseExpression_annotations() throws Exception {
    createTypeDeclaration_Test(
        "public class Test {",
        "  public Test() {",
        "    // marker",
        "  }",
        "  @SuppressWarnings('unchecked')",
        "  void foo() {",
        "  }",
        "}");
    // parse expression
    int position = m_lastEditor.indexOf("// marker");
    check_ASTParser_parseExpression(position, "123");
  }

  /**
   * We should declared constructors for inner classes, because not all classes have default
   * constructor.
   */
  public void test_ASTParser_parseExpression_innerClass_requiredConstructor() throws Exception {
    setFileContentSrc(
        "test/MySuper.java",
        getSource(
            "package test;",
            "public class MySuper {",
            "  public MySuper(int value) {",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    createTypeDeclaration_Test(
        "public class Test {",
        "  public Test() {",
        "    // marker",
        "  }",
        "  public class MyClass extends MySuper {",
        "    public MyClass() {",
        "      this(0);",
        "    }",
        "    public MyClass(int val) {",
        "      super(val);",
        "    }",
        "  }",
        "}");
    // parse expression
    int position = m_lastEditor.indexOf("// marker");
    check_ASTParser_parseExpression(position, "123");
  }

  /**
   * We don't require "super" constructor invocation.
   */
  public void test_ASTParser_parseExpression_innerClass_noSuperConstructor() throws Exception {
    createTypeDeclaration_Test(
        "public class Test {",
        "  public Test() {",
        "    // marker",
        "  }",
        "  public class MyClass {",
        "    public MyClass(byte val) {",
        "    }",
        "  }",
        "}");
    // parse expression
    int position = m_lastEditor.indexOf("// marker");
    check_ASTParser_parseExpression(position, "123");
  }

  /**
   * Test that we correctly declare abstract classes.
   */
  public void test_ASTParser_parseExpression_innerAbstractClass_abstractMethod() throws Exception {
    createTypeDeclaration_Test(
        "public class Test {",
        "  public Test() {",
        "    // marker",
        "  }",
        "  public abstract class AbstractClass {",
        "    public abstract void foo();",
        "  }",
        "}");
    // parse expression
    int position = m_lastEditor.indexOf("// marker");
    check_ASTParser_parseExpression(position, "123");
  }

  /**
   * Test that we correctly declare abstract methods.
   */
  public void test_ASTParser_parseExpression_innerInterface_abstractMethod() throws Exception {
    createTypeDeclaration_Test(
        "public class Test {",
        "  public Test() {",
        "    // marker",
        "  }",
        "  public interface MyInterface {",
        "    void foo();",
        "  }",
        "}");
    // parse expression
    int position = m_lastEditor.indexOf("// marker");
    check_ASTParser_parseExpression(position, "123");
  }

  /**
   * Constructor {@link MethodDeclaration} has no return type.
   */
  public void test_ASTParser_parseExpression_innerClass_noModifierConstructor() throws Exception {
    createTypeDeclaration_Test(
        "public class Test {",
        "  public Test() {",
        "    // marker",
        "  }",
        "  public class MyClass {",
        "    MyClass(int val) {",
        "    }",
        "  }",
        "}");
    // parse expression
    int position = m_lastEditor.indexOf("// marker");
    check_ASTParser_parseExpression(position, "123");
  }

  /**
   * We should include private inner classes into context.
   */
  public void test_ASTParser_parseExpression_innerClass_privateModifier() throws Exception {
    createTypeDeclaration_Test(
        "public class Test {",
        "  public Test() {",
        "    MyClass o = new MyClass();",
        "    // marker",
        "  }",
        "  public class MyClass {",
        "    public void setVisible(boolean visible) {",
        "    }",
        "  }",
        "}");
    // parse expression
    int position = m_lastEditor.indexOf("// marker");
    check_ASTParser_parseExpression(position, "o.setVisible(false)");
  }

  /**
   * Test that interface declared as interface, not as class.
   */
  public void test_ASTParser_parseExpression_innerInterface_shouldBeInterface() throws Exception {
    createTypeDeclaration_Test(
        "public class Test {",
        "  public interface MyInterface {",
        "    public abstract void foo();",
        "  }",
        "  public Test() {",
        "    // marker",
        "  }",
        "}");
    // parse expression
    int position = m_lastEditor.indexOf("// marker");
    check_ASTParser_parseExpression(position, "123");
  }

  /**
   * We should ignore JavaDoc text.
   */
  public void test_ASTParser_parseExpression_skipJavaDoc() throws Exception {
    createTypeDeclaration_Test(
        "public class Test {",
        "  public Test() {",
        "    // marker",
        "  }",
        "  /**",
        "  * Some text with {} and ().",
        "  */",
        "  public void foo() {",
        "  }",
        "}");
    // parse expression
    int position = m_lastEditor.indexOf("// marker");
    check_ASTParser_parseExpression(position, "123");
  }

  /**
   * We should ignore block comments.
   */
  public void test_ASTParser_parseExpression_skipComment() throws Exception {
    createTypeDeclaration_Test(
        "public class Test {",
        "  public Test() {",
        "    // marker",
        "  }",
        "  /**",
        "  * Some JavaDoc.",
        "  */",
        "  /*",
        "  * Some comment with {} and ().",
        "  */",
        "  public void foo() {",
        "  }",
        "}");
    // parse expression
    int position = m_lastEditor.indexOf("// marker");
    check_ASTParser_parseExpression(position, "123");
  }

  /**
   * Native method also has no body.
   */
  public void test_ASTParser_parseExpression_nativeMethod() throws Exception {
    createTypeDeclaration_Test(
        "public class Test {",
        "  public Test() {",
        "    // marker",
        "  }",
        "  native void someNativeMethod();",
        "}");
    // parse expression
    int position = m_lastEditor.indexOf("// marker");
    check_ASTParser_parseExpression(position, "123");
  }

  /**
   * Helper for testing {@link AstParser#parseExpression(int, String)}.
   */
  private void check_ASTParser_parseExpression(String expressionSource) throws Exception {
    TypeDeclaration typeDeclaration = createTypeDeclaration_TestC("");
    int position = typeDeclaration.getStartPosition();
    check_ASTParser_parseExpression(position, expressionSource);
  }

  private void check_ASTParser_parseExpression(int position, String expressionSource)
      throws Exception {
    Expression expression = m_lastEditor.getParser().parseExpression(position, expressionSource);
    assertNotNull(expression);
    assertEquals(position, expression.getStartPosition());
    assertEquals(expressionSource.length(), expression.getLength());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // replaceExpression
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstEditor#replaceExpression(Expression, String)} and also test for
   * {@link AstEditor#resolveImports(ASTNode)}.
   */
  public void test_replaceExpression() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "// filler filler filler",
            "public class Test {",
            "  Object m_value = new Object();",
            "}");
    FieldDeclaration field = typeDeclaration.getFields()[0];
    VariableDeclarationFragment fragment = (VariableDeclarationFragment) field.fragments().get(0);
    //
    Expression newExpression =
        m_lastEditor.replaceExpression(fragment.getInitializer(), "new java.util.ArrayList()");
    assertSame(newExpression, fragment.getInitializer());
    assertEquals(
        getSourceDQ(
            "package test;",
            "import java.util.ArrayList;",
            "// filler filler filler",
            "public class Test {",
            "  Object m_value = new ArrayList();",
            "}"),
        m_lastEditor.getSource());
    assertAST(m_lastEditor);
  }

  public void test_replaceExpression_noImports() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "// filler filler filler",
            "public class Test {",
            "  Object m_value = new Object();",
            "}");
    FieldDeclaration field = typeDeclaration.getFields()[0];
    VariableDeclarationFragment fragment = (VariableDeclarationFragment) field.fragments().get(0);
    //
    m_lastEditor.setResolveImports(false);
    Expression newExpression =
        m_lastEditor.replaceExpression(fragment.getInitializer(), "new java.util.ArrayList()");
    assertSame(newExpression, fragment.getInitializer());
    assertEquals(
        getSourceDQ(
            "package test;",
            "// filler filler filler",
            "public class Test {",
            "  Object m_value = new java.util.ArrayList();",
            "}"),
        m_lastEditor.getSource());
    assertAST(m_lastEditor);
  }

  public void test_replaceExpression_lines() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "// filler filler filler",
            "public class Test {",
            "  Object m_value = new Object();",
            "}");
    FieldDeclaration field = typeDeclaration.getFields()[0];
    VariableDeclarationFragment fragment = (VariableDeclarationFragment) field.fragments().get(0);
    //
    m_lastEditor.setResolveImports(false);
    Expression newExpression =
        m_lastEditor.replaceExpression(
            fragment.getInitializer(),
            ImmutableList.of("new", "\tjava.util.ArrayList()"));
    assertSame(newExpression, fragment.getInitializer());
    assertEditor(
        getSourceDQ(
            "package test;",
            "// filler filler filler",
            "public class Test {",
            "  Object m_value = new",
            "    java.util.ArrayList();",
            "}"),
        m_lastEditor);
  }

  public void test_replaceExpression_linesInSingle() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "// filler filler filler",
            "public class Test {",
            "  Object m_value = new Object();",
            "}");
    FieldDeclaration field = typeDeclaration.getFields()[0];
    VariableDeclarationFragment fragment = (VariableDeclarationFragment) field.fragments().get(0);
    //
    m_lastEditor.setResolveImports(false);
    Expression newExpression =
        m_lastEditor.replaceExpression(fragment.getInitializer(), "new\n\tjava.util.ArrayList()");
    assertSame(newExpression, fragment.getInitializer());
    assertEditor(
        getSourceDQ(
            "package test;",
            "// filler filler filler",
            "public class Test {",
            "  Object m_value = new",
            "    java.util.ArrayList();",
            "}"),
        m_lastEditor);
  }

  /**
   * Test for {@link AstEditor#resolveImports(ASTNode)} and qualified type name in array creation.
   */
  public void test_resolveImports() throws Exception {
    createTypeDeclaration_Test(
        "// filler filler filler",
        "public class Test {",
        "  Object m_value = new Object();",
        "}");
    Expression expression = getNode("new Object()");
    //
    m_lastEditor.replaceExpression(
        expression,
        "new java.util.ArrayList((new java.lang.Integer[]{}).length)");
    assertEditor(
        getSourceDQ(
            "package test;",
            "import java.util.ArrayList;",
            "// filler filler filler",
            "public class Test {",
            "  Object m_value = new ArrayList((new Integer[]{}).length);",
            "}"),
        m_lastEditor);
  }

  /**
   * Test for {@link AstEditor#resolveImports(ASTNode)}.
   * <p>
   * We should not resolve qualified type if it will have same name as type in same file.
   */
  public void test_resolveImports_typeInSameFile() throws Exception {
    setFileContentSrc(
        "other/pkg/Test.java",
        getSourceDQ(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "package other.pkg;",
            "public class Test {",
            "}"));
    createTypeDeclaration_Test(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test {",
        "  Object value = new Object();",
        "}");
    Expression expression = getNode("new Object()");
    //
    m_lastEditor.replaceExpression(expression, "new other.pkg.Test()");
    assertEditor(
        getSourceDQ(
            "package test;",
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test {",
            "  Object value = new other.pkg.Test();",
            "}"),
        m_lastEditor);
  }

  /**
   * Test for {@link AstEditor#resolveImports(ASTNode)} and inner class.
   */
  public void test_resolveImports_forInnerClass() throws Exception {
    setFileContentSrc(
        "test2/Style.java",
        getSource(
            "// filler filler filler filler filler",
            "package test;",
            "public class Style {",
            "  public enum Orientation {HORIZONTAL, VERTICAL};",
            "}"));
    createTypeDeclaration(
        "test",
        "Test.java",
        getSource(
            "// filler filler filler filler filler",
            "package test;",
            "public class Test {",
            "  Object m_value = new Object();",
            "}"));
    //
    Expression expression = (Expression) getNode("new Object()");
    m_lastEditor.replaceExpression(
        expression,
        ImmutableList.of("test2.Style.Orientation.HORIZONTAL"));
    assertEditor(
        getSource(
            "// filler filler filler filler filler",
            "package test;",
            "import test2.Style.Orientation;",
            "public class Test {",
            "  Object m_value = Orientation.HORIZONTAL;",
            "}"),
        m_lastEditor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // replaceInvocationArgument()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstEditor#replaceInvocationArgument(MethodInvocation, int, String)}.
   */
  public void test_replaceInvocationArgument() throws Exception {
    createTypeDeclaration(
        "test",
        "Test.java",
        getSource(
            "package test;",
            "public class Test {",
            "  void foo() {",
            "    bar(1, 2);",
            "  }",
            "  void bar(int a, int b) {",
            "  }",
            "}"));
    MethodInvocation invocation = getNode("bar(1, 2)", MethodInvocation.class);
    //
    m_lastEditor.replaceInvocationArgument(invocation, 1, "20");
    assertEditor(
        getSource(
            "package test;",
            "public class Test {",
            "  void foo() {",
            "    bar(1, 20);",
            "  }",
            "  void bar(int a, int b) {",
            "  }",
            "}"),
        m_lastEditor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // replace*Type
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstEditor#replaceVariableType(VariableDeclaration, String)}.
   */
  public void test_replaceVariableType_VariableDeclarationStatement() throws Exception {
    createTypeDeclaration_Test(
        "import javax.swing.*;",
        "public class Test {",
        "  public Test() {",
        "    JButton button = null;",
        "  }",
        "}");
    VariableDeclaration variableDeclaration = getNode("button", VariableDeclaration.class);
    m_lastEditor.replaceVariableType(variableDeclaration, "javax.swing.JTextField");
    assertEditor(
        getSourceDQ(
            "package test;",
            "import javax.swing.*;",
            "public class Test {",
            "  public Test() {",
            "    JTextField button = null;",
            "  }",
            "}"),
        m_lastEditor);
  }

  /**
   * Test for {@link AstEditor#replaceVariableType(VariableDeclaration, String)}.
   */
  public void test_replaceVariableType_FieldDeclaration() throws Exception {
    createTypeDeclaration_Test(
        "import javax.swing.*;",
        "public class Test {",
        "  private JButton button;",
        "}");
    VariableDeclaration variableDeclaration = getNode("button", VariableDeclaration.class);
    m_lastEditor.replaceVariableType(variableDeclaration, "javax.swing.JTextField");
    assertEditor(
        getSourceDQ(
            "package test;",
            "import javax.swing.*;",
            "public class Test {",
            "  private JTextField button;",
            "}"),
        m_lastEditor);
  }

  /**
   * Test for {@link AstEditor#replaceVariableType(VariableDeclaration, String)}.
   */
  public void test_replaceVariableType_unknown() throws Exception {
    createTypeDeclaration_Test(
        "// filler filler filler filler filler",
        "import javax.swing.*;",
        "public class Test {",
        "}");
    VariableDeclaration variableDeclaration = EasyMock.createMock(VariableDeclaration.class);
    try {
      m_lastEditor.replaceVariableType(variableDeclaration, "javax.swing.JTextField");
      fail();
    } catch (IllegalArgumentException e) {
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // EOL comments
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getStringLiteralNumberOnLine() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  String m_string_1 = '1' + '2' +",
            "    '3';",
            "}");
    FieldDeclaration field = typeDeclaration.getFields()[0];
    VariableDeclarationFragment fragment = (VariableDeclarationFragment) field.fragments().get(0);
    InfixExpression infixExpression = (InfixExpression) fragment.getInitializer();
    //
    assertEquals(
        0,
        m_lastEditor.getStringLiteralNumberOnLine((StringLiteral) infixExpression.getLeftOperand()));
    assertEquals(
        1,
        m_lastEditor.getStringLiteralNumberOnLine((StringLiteral) infixExpression.getRightOperand()));
    assertEquals(
        0,
        m_lastEditor.getStringLiteralNumberOnLine((StringLiteral) infixExpression.extendedOperands().get(
            0)));
  }

  public void test_addEndOfLineComment() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "// filler filler filler",
            "public class Test {",
            "  int m_value;",
            "}");
    FieldDeclaration field = typeDeclaration.getFields()[0];
    //
    m_lastEditor.addEndOfLineComment(field.getStartPosition(), " // Hello!");
    assertEditor(
        getSourceDQ(
            "package test;",
            "// filler filler filler",
            "public class Test {",
            "  int m_value; // Hello!",
            "}"),
        m_lastEditor);
  }

  /**
   * Test for {@link AstEditor#getEndOfLineComment(int)}
   */
  public void test_getEndOfLineComment() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test {",
            "  int m_value; // 111",
            "  int m_value2;",
            "}");
    // has comment
    {
      FieldDeclaration field = typeDeclaration.getFields()[0];
      int position = AstNodeUtils.getSourceBegin(field);
      assertEquals("// 111", m_lastEditor.getEndOfLineComment(position));
    }
    // no comment
    {
      FieldDeclaration field = typeDeclaration.getFields()[1];
      int position = AstNodeUtils.getSourceBegin(field);
      assertEquals(null, m_lastEditor.getEndOfLineComment(position));
    }
    // bad position
    try {
      m_lastEditor.getChar(-1);
      fail();
    } catch (Throwable e) {
      assertThat(e).isExactlyInstanceOf(BadLocationException.class);
    }
  }

  public void test_removeEndOfLineComment() throws Exception {
    TypeDeclaration typeDeclaration =
        createTypeDeclaration_Test(
            "public class Test {",
            "  int m_value; // 111   // 222  ",
            "  int m_value2; // 333",
            "}");
    FieldDeclaration field = typeDeclaration.getFields()[0];
    // no such comment on this line, ignore
    m_lastEditor.removeEndOfLineComment(field.getStartPosition(), "// 333");
    assertEditor(
        getSourceDQ(
            "package test;",
            "public class Test {",
            "  int m_value; // 111   // 222  ",
            "  int m_value2; // 333",
            "}"),
        m_lastEditor);
    // remove inner
    m_lastEditor.removeEndOfLineComment(field.getStartPosition(), "// 111");
    assertEditor(
        getSourceDQ(
            "package test;",
            "public class Test {",
            "  int m_value; // 222  ",
            "  int m_value2; // 333",
            "}"),
        m_lastEditor);
    // remove last
    m_lastEditor.removeEndOfLineComment(field.getStartPosition(), "// 222");
    assertEditor(
        getSourceDQ(
            "package test;",
            "public class Test {",
            "  int m_value;",
            "  int m_value2; // 333",
            "}"),
        m_lastEditor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // replaceNode
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstEditor#replaceNode(ASTNode, ASTNode)}.
   */
  public void test_replaceNode() throws Exception {
    TypeDeclaration typeDeclaration = createTypeDeclaration_TestC("int m_value = Math.abs(-1);");
    FieldDeclaration field = typeDeclaration.getFields()[0];
    VariableDeclarationFragment fragment = (VariableDeclarationFragment) field.fragments().get(0);
    MethodInvocation invocation = (MethodInvocation) fragment.getInitializer();
    // set for setXXX()
    {
      SimpleName newSimpleName = invocation.getAST().newSimpleName("foo");
      AstEditor.replaceNode(invocation.getName(), newSimpleName);
      assertSame(newSimpleName, invocation.getName());
    }
    // test for List
    {
      SimpleName newSimpleName = invocation.getAST().newSimpleName("X");
      AstEditor.replaceNode((ASTNode) invocation.arguments().get(0), newSimpleName);
      assertSame(newSimpleName, invocation.arguments().get(0));
    }
  }

  /**
   * Test for {@link AstEditor#replaceNode(ASTNode, ASTNode)}.<br>
   * Special case when {@link QualifiedName} should be converted into {@link FieldAccess}.
   */
  public void test_replaceNode_QualifiedName_into_FieldAccess() throws Exception {
    setFileContentSrc(
        "test/MyObject.java",
        getSourceDQ("package test;", "public class MyObject {", "  public int m_value;", "}"));
    createTypeDeclaration_Test(
        "public class Test {",
        "  public Test() {",
        "    MyObject foo = new MyObject();",
        "    foo.m_value = 1;",
        "  }",
        "}");
    int fooIndex = m_lastEditor.getSource().indexOf("foo.");
    Expression fooNode = (Expression) m_lastEditor.getEnclosingNode(fooIndex);
    m_lastEditor.replaceExpression(fooNode, "new test.MyObject()");
    assertEditor(
        getSourceDQ(
            "package test;",
            "public class Test {",
            "  public Test() {",
            "    MyObject foo = new MyObject();",
            "    new MyObject().m_value = 1;",
            "  }",
            "}"),
        m_lastEditor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Source utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AstEditor#getTypeArgumentsSource(ClassInstanceCreation)}.
   */
  public void test_getTypeArgumentsSource() throws Exception {
    createTypeDeclaration_Test(
        "import java.util.*;",
        "public class Test {",
        "  void foo() {",
        "    new Object();",
        "    new ArrayList<Integer>();",
        "    new HashMap<String, Double>();",
        "    new LinkedList<Short>() {};",
        "  }",
        "}");
    // no type arguments
    check_getTypeArgumentsSource("new Object", "");
    // single type argument
    check_getTypeArgumentsSource("new ArrayList", "<java.lang.Integer>");
    // two type arguments
    check_getTypeArgumentsSource("new HashMap", "<java.lang.String, java.lang.Double>");
    // anonymous
    check_getTypeArgumentsSource("new LinkedList", "<java.lang.Short>");
  }

  private void check_getTypeArgumentsSource(String src, String expected) {
    ClassInstanceCreation creation = getNode(src, ClassInstanceCreation.class);
    assertEquals(expected, m_lastEditor.getTypeArgumentsSource(creation));
  }

  /**
   * Test for {@link AstEditor#getMethodStubSource(MethodDeclaration)}.
   */
  public void test_getMethodStubSource() throws Exception {
    createTypeDeclaration_Test(
        "import java.util.*;",
        "public class Test {",
        "  void methodA() {",
        "  }",
        "  public String methodB(int a, String b, Double c) {",
        "    return 'b';",
        "  }",
        "  public static final int methodC() {",
        "    return 42;",
        "  }",
        "}");
    {
      MethodDeclaration method = getNode("methodA", MethodDeclaration.class);
      assertEquals("\tvoid methodA() {\n\t}", m_lastEditor.getMethodStubSource(method));
    }
    {
      MethodDeclaration method = getNode("methodB", MethodDeclaration.class);
      assertEquals(
          "\tpublic java.lang.String methodB(int a, java.lang.String b, java.lang.Double c) {"
              + "\n\t\treturn (java.lang.String) null;\n\t}",
          m_lastEditor.getMethodStubSource(method));
    }
    {
      MethodDeclaration method = getNode("methodC", MethodDeclaration.class);
      assertEquals(
          "\tpublic static final int methodC() {\n\t\treturn 0;\n\t}",
          m_lastEditor.getMethodStubSource(method));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Project disposing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void test_tearDown() throws Exception {
    do_projectDispose();
  }
}
