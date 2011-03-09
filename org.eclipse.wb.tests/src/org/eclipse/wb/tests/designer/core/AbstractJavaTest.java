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
package org.eclipse.wb.tests.designer.core;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * @author scheglov_ke
 */
public class AbstractJavaTest extends AbstractJavaProjectTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void tearDown() throws Exception {
    // don't ignore model compilation problems
    m_ignoreModelCompileProblems = false;
    // clear "last"
    m_lastEditor = null;
    m_lastModelUnit = null;
    // continue
    super.tearDown();
  }

  @Override
  public void test_tearDown() throws Exception {
    do_projectDispose();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Project operations
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void do_projectDispose() throws Exception {
    super.do_projectDispose();
    // clear "last"
    m_lastEditor = null;
    m_lastModelUnit = null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AST utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Statement} with given index's in each {@link Block}.
   */
  protected static final Statement getStatement(Block block, int... indexes) {
    Statement statement = block;
    for (int i = 0; i < indexes.length; i++) {
      int index = indexes[i];
      if (index == -1) {
        // handle -1 as "this", i.e. just ignore it, keep old statement
      } else {
        Block nextBlock;
        if (statement instanceof IfStatement) {
          nextBlock = (Block) ((IfStatement) statement).getThenStatement();
        } else {
          nextBlock = (Block) statement;
        }
        statement = (Statement) nextBlock.statements().get(index);
      }
    }
    return statement;
  }

  /**
   * @return the {@link ASTNode} with given source in current {@link AstEditor}.
   */
  protected final <T extends ASTNode> T getNode(String src, Class<T> clazz) {
    ASTNode enclosingNode = m_lastEditor.getEnclosingNode(src);
    return AstNodeUtils.getEnclosingNode(enclosingNode, clazz);
  }

  /**
   * @return the {@link ASTNode} with given source in current {@link AstEditor}.
   */
  @SuppressWarnings("unchecked")
  protected final <T extends ASTNode> T getNode(String src) {
    return (T) m_lastEditor.getEnclosingNode(src);
  }

  /**
   * @return the {@link MethodDeclaration} in top level {@link TypeDeclaration} of last
   *         {@link AstEditor}.
   */
  protected final MethodDeclaration getMethod(String methodSignature) {
    TypeDeclaration typeDeclaration = DomGenerics.types(m_lastEditor.getAstUnit()).get(0);
    return AstNodeUtils.getMethodBySignature(typeDeclaration, methodSignature);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Reflection
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the value of field (accessible or not).
   */
  protected static Object getFieldValue(Object o, String fieldName) throws Exception {
    return ReflectionUtils.getFieldByName(o.getClass(), fieldName).get(o);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  protected boolean m_ignoreModelCompileProblems = false;
  protected ICompilationUnit m_lastModelUnit;
  protected AstEditor m_lastEditor;

  /**
   * Creates unit Test.java with given source, parses it and returns single {@link TypeDeclaration}.
   */
  protected final TypeDeclaration createTypeDeclaration_TestC(String code) throws Exception {
    String source = "package test;public class Test{" + code + "}";
    CompilationUnit astUnit = createASTCompilationUnit("test", "Test.java", source);
    // return type
    assertEquals(1, astUnit.types().size());
    return (TypeDeclaration) astUnit.types().get(0);
  }

  /**
   * Creates unit Test.java with given source, parses it and returns single {@link TypeDeclaration}.
   */
  protected final TypeDeclaration createTypeDeclaration_TestD(String... lines) throws Exception {
    String source = getSource(lines);
    CompilationUnit astUnit = createASTCompilationUnit("test", "Test.java", source);
    // return type
    assertEquals(1, astUnit.types().size());
    return (TypeDeclaration) astUnit.types().get(0);
  }

  /**
   * Creates unit Test.java with given source, parses it and returns single {@link TypeDeclaration}.
   */
  protected final TypeDeclaration createTypeDeclaration_Test(String... lines) throws Exception {
    String source = getSource2(new String[]{"package test;"}, getDoubleQuotes(lines));
    CompilationUnit astUnit = createASTCompilationUnit("test", "Test.java", source);
    // return type
    assertEquals(1, astUnit.types().size());
    return (TypeDeclaration) astUnit.types().get(0);
  }

  /**
   * @return the {@link TypeDeclaration} for given source.
   */
  protected final TypeDeclaration createTypeDeclaration(String packageName,
      String unitName,
      String source) throws Exception {
    CompilationUnit compilationUnit = createASTCompilationUnit(packageName, unitName, source);
    return (TypeDeclaration) compilationUnit.types().get(0);
  }

  /**
   * Creates {@link ICompilationUnit} with given name and source, parses it and returns
   * {@link CompilationUnit}.
   */
  protected final CompilationUnit createASTCompilationUnit(String packageName,
      String unitName,
      String code) throws Exception {
    ICompilationUnit modelUnit = createModelCompilationUnit(packageName, unitName, code);
    return createASTCompilationUnit(modelUnit);
  }

  protected final CompilationUnit createASTCompilationUnit(ICompilationUnit modelUnit)
      throws Exception {
    // prepare and check ASTEditor
    AstEditor editor = new AstEditor(modelUnit);
    assertSame(modelUnit, editor.getModelUnit());
    // check AST unit
    CompilationUnit astUnit = editor.getAstUnit();
    assertNotNull(astUnit);
    // check for compile errors
    if (!m_ignoreModelCompileProblems) {
      IProblem[] problems = astUnit.getProblems();
      for (int i = 0; i < problems.length; i++) {
        IProblem problem = problems[i];
        if (problem.isError()) {
          fail("Error found: " + problem);
        }
      }
    }
    //
    m_lastModelUnit = modelUnit;
    m_lastEditor = editor;
    return astUnit;
  }

  /**
   * @return the single {@link VariableDeclaration} of {@link FieldDeclaration} with given index.
   */
  protected final static VariableDeclaration getFieldFragment(TypeDeclaration typeDeclaration,
      int fieldIndex) {
    FieldDeclaration fieldDeclaration = typeDeclaration.getFields()[fieldIndex];
    return (VariableDeclarationFragment) fieldDeclaration.fragments().get(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Checks
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Asserts that given {@link JavaInfo} has tightly related nodes with given source.
   */
  protected static void assertRelatedNodes(JavaInfo javaInfo, String[] sourceTightlyNodes)
      throws Exception {
    List<ASTNode> nodes = javaInfo.getRelatedNodes();
    assertEquals(
        StringUtils.join(sourceTightlyNodes, ", ")
            + " != "
            + StringUtils.join(nodes.iterator(), ", "),
        sourceTightlyNodes.length,
        nodes.size());
    for (int i = 0; i < nodes.size(); i++) {
      ASTNode node = nodes.get(i);
      // prepare node to compare source
      ASTNode sourceNode = JavaInfo.getRelatedNodeForSource(node);
      // compare sources
      assertEquals(sourceTightlyNodes[i], javaInfo.getEditor().getSource(sourceNode));
    }
  }

  /**
   * Asserts that {@link AstEditor} has valid AST tree and expected source.
   */
  protected static void assertEditor(String expectedSource, AstEditor editor) {
    assertEquals(expectedSource, editor.getSource());
    assertAST(editor);
  }

  /**
   * Checks several things about AST tree. <li>all {@link SimpleName}'s in given {@link AstEditor}
   * has valid source;</li> <li>variables have {@link IVariableBinding} with not-null
   * {@link ITypeBinding};</li> <li>
   * import, types and variable declaration have not-null {@link ITypeBinding};</li> <li>statements
   * of know types begin and start with known characters;</li> <br/>
   * Of course this does not mean that ALL is good, but this is better than nothing. ;-)
   */
  protected static void assertAST(final AstEditor editor) {
    final AST editorAST = editor.getAstUnit().getAST();
    editor.getAstUnit().accept(new ASTVisitor(true) {
      @Override
      public void endVisit(SimpleName node) {
        assertEquals(node.getIdentifier(), getSource(node));
        boolean isVariable = false;
        if (node.getParent() instanceof MethodInvocation) {
          MethodInvocation invocation = (MethodInvocation) node.getParent();
          IMethodBinding binding = AstNodeUtils.getMethodBinding(invocation);
          if (!Modifier.isStatic(binding.getModifiers())) {
            isVariable |= invocation.getExpression() == node;
            isVariable |= invocation.arguments().contains(node);
          }
        }
        isVariable |= node.getParent() instanceof InfixExpression;
        if (isVariable) {
          IVariableBinding variableBinding = AstNodeUtils.getVariableBinding(node);
          assertNotNull(variableBinding);
          assertNotNull(variableBinding.getType());
        }
      }

      @Override
      public void endVisit(TextElement node) {
        assertEquals(node.getText(), getSource(node));
      }

      @Override
      public void endVisit(NumberLiteral node) {
        assertEquals(node.getToken(), getSource(node));
      }

      @Override
      public void endVisit(VariableDeclarationFragment node) {
        SimpleName name = node.getName();
        assertEquals(name.getStartPosition(), node.getStartPosition());
      }

      @Override
      public void preVisit(ASTNode node) {
        assertSame(editorAST, node.getAST());
        // check positions for parent/child
        {
          ASTNode parent = node.getParent();
          if (parent != null) {
            int begin = AstNodeUtils.getSourceBegin(node);
            int end = AstNodeUtils.getSourceEnd(node);
            int parent_begin = AstNodeUtils.getSourceBegin(parent);
            int parent_end = AstNodeUtils.getSourceEnd(parent);
            assertTrue(begin >= parent_begin);
            assertTrue(end <= parent_end);
          }
        }
        // check bindings
        {
          if (node instanceof ImportDeclaration) {
            ImportDeclaration declaration = (ImportDeclaration) node;
            if (!declaration.isOnDemand()) {
              assertNotNull(AstNodeUtils.getTypeBinding(declaration.getName()));
            }
          }
          if (node instanceof Type) {
            Type type = (Type) node;
            assertNotNull(AstNodeUtils.getTypeBinding(type));
          }
          if (node instanceof VariableDeclaration) {
            VariableDeclaration declaration = (VariableDeclaration) node;
            assertNotNull(AstNodeUtils.getTypeBinding(declaration.getName()));
            if (declaration.getInitializer() != null) {
              assertNotNull(AstNodeUtils.getTypeBinding(declaration.getInitializer()));
            }
          }
          if (node instanceof MethodInvocation) {
            MethodInvocation invocation = (MethodInvocation) node;
            IMethodBinding methodBinding = AstNodeUtils.getMethodBinding(invocation);
            assertNotNull(methodBinding);
            // check that names in MethodInvocation and IMethodBinding are same
            assertEquals(invocation.getName().getIdentifier(), methodBinding.getName());
            //
            // check that types of parameters and arguments are compatible
            ITypeBinding[] parameterTypes = methodBinding.getParameterTypes();
            List<?> arguments = invocation.arguments();
            for (int i = 0; i < parameterTypes.length; i++) {
              ITypeBinding parameterType = parameterTypes[i];
              if (i == parameterTypes.length - 1 && parameterType.isArray()) {
                // ellipsis support (last parameter is array)
                if (i == arguments.size()) {
                  // ellipsis parameter skipped
                  break;
                }
                {
                  // check for exactly array
                  Expression argument = (Expression) arguments.get(i);
                  String argumentClassName = AstNodeUtils.getFullyQualifiedName(argument, false);
                  if (AstNodeUtils.isSuccessorOf(parameterType, argumentClassName)) {
                    // parameter is exactly array
                    break;
                  }
                }
                // check all other arguments
                ITypeBinding parameterElementType = parameterType.getElementType();
                for (int j = i; j < arguments.size(); j++) {
                  Expression argument = (Expression) arguments.get(j);
                  String argumentClassName = AstNodeUtils.getFullyQualifiedName(argument, false);
                  assertTrue(AstNodeUtils.isSuccessorOf(parameterElementType, argumentClassName));
                }
                break;
              }
              Expression argument = (Expression) arguments.get(i);
              String argumentClassName = AstNodeUtils.getFullyQualifiedName(argument, false);
              // FIXME
              //assertTrue(
              AstNodeUtils.isSuccessorOf(parameterType, argumentClassName);
              //	);
            }
          }
          if (node instanceof MethodDeclaration) {
            MethodDeclaration declaration = (MethodDeclaration) node;
            IMethodBinding methodBinding = AstNodeUtils.getMethodBinding(declaration);
            assertNotNull(methodBinding);
            // check that names in MethodDeclaration and IMethodBinding are same
            assertEquals(declaration.getName().getIdentifier(), methodBinding.getName());
            // check return type
            {
              Type returnType = declaration.getReturnType2();
              if (returnType != null) {
                ITypeBinding bindingType = methodBinding.getReturnType();
                ITypeBinding declarationType = AstNodeUtils.getTypeBinding(returnType);
                assertEqualTypes(bindingType, declarationType);
              }
            }
            // check parameters
            ITypeBinding[] bindingTypes = methodBinding.getParameterTypes();
            List<SingleVariableDeclaration> parameters = DomGenerics.parameters(declaration);
            assertEquals(parameters.size(), bindingTypes.length);
            // check that types of parameters and arguments are same
            for (int i = 0; i < bindingTypes.length; i++) {
              ITypeBinding bindingType = bindingTypes[i];
              SingleVariableDeclaration parameter = parameters.get(i);
              ITypeBinding parameterType = AstNodeUtils.getTypeBinding(parameter);
              assertEqualTypes(bindingType, parameterType);
            }
          }
        }
        // check source prefix/suffix
        {
          if (node instanceof ExpressionStatement
              || node instanceof VariableDeclarationStatement
              || node instanceof FieldDeclaration
              || node instanceof ImportDeclaration
              || node instanceof PackageDeclaration) {
            checkNodeSuffix(node, ";");
          }
          if (node instanceof MethodInvocation) {
            checkNodeSuffix(node, ")");
          }
          if (node instanceof ParenthesizedExpression) {
            checkNodePrefix(node, "(");
            checkNodeSuffix(node, ")");
          }
          if (node instanceof Block) {
            checkNodePrefix(node, "{");
            checkNodeSuffix(node, "}");
          }
          if (node instanceof TypeDeclaration || node instanceof AnonymousClassDeclaration) {
            checkNodeSuffix(node, "}");
          }
          if (node instanceof StringLiteral) {
            checkNodePrefix(node, "\"");
            checkNodeSuffix(node, "\"");
          }
        }
        // check Block: statements order
        if (node instanceof Block) {
          Block block = (Block) node;
          int lastStatementEnd = -1;
          for (Statement statement : DomGenerics.statements(block)) {
            assertTrue(lastStatementEnd <= statement.getStartPosition());
            lastStatementEnd = AstNodeUtils.getSourceEnd(statement);
          }
        }
        // check TypeDeclaration: declarations order
        if (node instanceof TypeDeclaration) {
          TypeDeclaration typeDeclaration = (TypeDeclaration) node;
          int lastDeclarationEnd = -1;
          for (BodyDeclaration declaration : DomGenerics.bodyDeclarations(typeDeclaration)) {
            assertTrue(lastDeclarationEnd <= declaration.getStartPosition());
            lastDeclarationEnd = AstNodeUtils.getSourceEnd(declaration);
          }
        }
      }

      private void assertEqualTypes(ITypeBinding bindingType, ITypeBinding parameterType) {
        String bindingTypeName = AstNodeUtils.getFullyQualifiedName(bindingType, false);
        String parameterTypeName = AstNodeUtils.getFullyQualifiedName(parameterType, false);
        assertEquals(bindingTypeName, parameterTypeName);
      }

      ////////////////////////////////////////////////////////////////////////////
      //
      // Checks
      //
      ////////////////////////////////////////////////////////////////////////////
      private void checkNodePrefix(ASTNode node, String prefix) {
        assertEquals(prefix, getSource(node.getStartPosition(), prefix.length()));
      }

      private void checkNodeSuffix(ASTNode node, String suffix) {
        int end = node.getStartPosition() + node.getLength();
        assertEquals(suffix, getSource(end - suffix.length(), suffix.length()));
      }

      ////////////////////////////////////////////////////////////////////////////
      //
      // Source access
      //
      ////////////////////////////////////////////////////////////////////////////
      private String getSource(int start, int length) {
        try {
          return editor.getSource(start, length);
        } catch (Throwable e) {
          throw ReflectionUtils.propagate(e);
        }
      }

      private String getSource(ASTNode node) {
        return getSource(node.getStartPosition(), node.getLength());
      }
    });
  }
}
