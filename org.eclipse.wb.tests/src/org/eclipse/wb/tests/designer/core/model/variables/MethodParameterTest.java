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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.UnknownAssociation;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.MethodParameterCreationSupport;
import org.eclipse.wb.internal.core.model.creation.ThisCreationSupport;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.variable.MethodParameterVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import javax.swing.JButton;

/**
 * Test for {@link MethodParameterVariableSupport}.
 * 
 * @author scheglov_ke
 */
public class MethodParameterTest extends AbstractVariableTest {
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
  /**
   * Test for {@link MethodParameterCreationSupport}.
   */
  public void test_creationSupport() throws Exception {
    ContainerInfo panel =
        parsePanel(
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "  public void createPart(Container parent) {",
            "  }",
            "}");
    ContainerInfo parent = (ContainerInfo) panel.getChildrenComponents().get(0);
    // creation
    {
      MethodParameterCreationSupport creation =
          (MethodParameterCreationSupport) parent.getCreationSupport();
      assertEquals("parameter", creation.toString());
      // SingleVariableDeclaration does not create really object, so always return "false"
      assertFalse(creation.isJavaInfo(null));
      // position
      {
        SingleVariableDeclaration declaration = (SingleVariableDeclaration) creation.getNode();
        assertEquals("parent", declaration.getName().getIdentifier());
      }
    }
  }

  /**
   * Test for {@link MethodParameterVariableSupport}.
   */
  public void test_variableSupport() throws Exception {
    ContainerInfo panel =
        parsePanel(
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "  public void createPart(Container parent) {",
            "  }",
            "}");
    ContainerInfo parent = (ContainerInfo) panel.getChildrenComponents().get(0);
    // creation
    {
      MethodParameterCreationSupport creation =
          (MethodParameterCreationSupport) parent.getCreationSupport();
      assertFalse(creation.canReorder());
      assertFalse(creation.canReparent());
    }
    // variable
    {
      MethodParameterVariableSupport variable =
          (MethodParameterVariableSupport) parent.getVariableSupport();
      assertEquals("parent", variable.toString());
      assertEquals("parent in createPart(...)", variable.getTitle());
      // target
      {
        MethodDeclaration methodDeclaration =
            AstNodeUtils.getEnclosingMethod(parent.getCreationSupport().getNode());
        assertTarget(variable.getStatementTarget(), methodDeclaration.getBody(), null, true);
      }
      // name
      {
        assertTrue(variable.hasName());
        assertEquals("parent", variable.getName());
      }
      // expression in createPart() method
      {
        Block targetBlock = getTypeDeclaration(panel).getMethods()[1].getBody();
        NodeTarget target = new NodeTarget(new StatementTarget(targetBlock, true));
        assertTrue(variable.hasExpression(target));
        assertEquals("parent", variable.getReferenceExpression(target));
        assertEquals("parent.", variable.getAccessExpression(target));
      }
      // no expression outside of createPart() method, in Test() constructor
      {
        NodeTarget target = getNodeBlockTarget(panel, true);
        assertFalse(variable.hasExpression(target));
        try {
          variable.getReferenceExpression(target);
          fail();
        } catch (IllegalArgumentException e) {
        }
      }
      // no expression outside of createPart() method, in Test type beginning
      {
        TypeDeclaration targetType = getTypeDeclaration(panel);
        NodeTarget target = new NodeTarget(new BodyDeclarationTarget(targetType, true));
        assertFalse(variable.hasExpression(target));
        try {
          variable.getReferenceExpression(target);
          fail();
        } catch (IllegalArgumentException e) {
        }
      }
      // conversion - not supported
      {
        assertFalse(variable.canConvertLocalToField());
        try {
          variable.convertLocalToField();
          fail();
        } catch (IllegalStateException e) {
        }
        //
        assertFalse(variable.canConvertFieldToLocal());
        try {
          variable.convertFieldToLocal();
          fail();
        } catch (IllegalStateException e) {
        }
      }
    }
    // modify name of "parent"
    {
      Property variableProperty = parent.getPropertyByTitle("Variable");
      assertEquals("parent", variableProperty.getValue());
      //
      parent.getVariableSupport().setName("newName");
      assertEditor(
          "public class Test extends JPanel {",
          "  public Test() {",
          "  }",
          "  public void createPart(Container newName) {",
          "  }",
          "}");
      assertEquals("newName", variableProperty.getValue());
    }
  }

  /**
   * Test for {@link MethodParameterVariableSupport#isJavaInfo(ASTNode)}.
   */
  public void test_variableSupport_isJavaInfo() throws Exception {
    ContainerInfo panel =
        parsePanel(
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "  public void createPart(Container parent) {",
            "    parent.add(new JButton());",
            "  }",
            "}");
    ContainerInfo parent = (ContainerInfo) panel.getChildrenComponents().get(0);
    // variable
    MethodParameterVariableSupport variable =
        (MethodParameterVariableSupport) parent.getVariableSupport();
    // prepare ASTNode to check as "parent"
    Expression expectedParentNode;
    {
      MethodDeclaration createPartMethod =
          AstNodeUtils.getEnclosingMethod(parent.getCreationSupport().getNode());
      ExpressionStatement statement =
          (ExpressionStatement) DomGenerics.statements(createPartMethod.getBody()).get(0);
      MethodInvocation invocation = (MethodInvocation) statement.getExpression();
      expectedParentNode = invocation.getExpression();
    }
    // do check
    assertTrue(variable.isJavaInfo(expectedParentNode));
    // but some random ASTNode, for example CompilationUnit will return "false"
    assertFalse(variable.isJavaInfo(m_lastEditor.getAstUnit()));
  }

  /**
   * Test for {@link MethodParameterVariableSupport#getReferenceExpression(NodeTarget)}.
   * <p>
   * Sometimes we want to get expression for target in other method, invoked from declaring one.
   */
  public void test_variableSupport_getExpression_invokedMethod() throws Exception {
    ContainerInfo panel =
        parsePanel(
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "  public void createPart(Container parent) {",
            "    fillParent(parent);",
            "    noParent();",
            "  }",
            "  public void fillParent(Container otherParentName) {",
            "  }",
            "  public void noParent() {",
            "  }",
            "}");
    ContainerInfo parent = (ContainerInfo) panel.getChildrenComponents().get(0);
    VariableSupport variable = parent.getVariableSupport();
    // in createPart()
    {
      NodeTarget target = getNodeStatementTarget(panel, "createPart(java.awt.Container)", false);
      assertTrue(variable.hasExpression(target));
      assertEquals("parent", variable.getReferenceExpression(target));
    }
    // in fillParent()
    {
      NodeTarget target = getNodeStatementTarget(panel, "fillParent(java.awt.Container)", false);
      assertTrue(variable.hasExpression(target));
      assertEquals("otherParentName", variable.getReferenceExpression(target));
    }
    // in noParent()
    {
      NodeTarget target = getNodeStatementTarget(panel, "noParent()", false);
      assertFalse(variable.hasExpression(target));
      try {
        variable.getReferenceExpression(target);
        fail();
      } catch (IllegalArgumentException e) {
      }
    }
  }

  /**
   * Test for {@link MethodParameterVariableSupport#isValidStatementForChild(Statement)}.
   */
  public void test_variableSupport_isValidStatementForChild() throws Exception {
    ContainerInfo panel =
        parsePanel(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    int statementOutsiteOfMethod;",
            "  }",
            "  public void createPart(Container parent) {",
            "    int statementInMethod;",
            "  }",
            "}");
    ContainerInfo parent = (ContainerInfo) panel.getChildrenComponents().get(0);
    TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(panel);
    // variable
    MethodParameterVariableSupport variable =
        (MethodParameterVariableSupport) parent.getVariableSupport();
    // invalid Statement, if outside of createPart()
    {
      Statement statementOutsiteOfMethod =
          getStatement(typeDeclaration.getMethods()[0].getBody(), 0);
      assertFalse(variable.isValidStatementForChild(statementOutsiteOfMethod));
    }
    // valid Statement, if inside of createPart()
    {
      Statement statementInMethod = getStatement(typeDeclaration.getMethods()[1].getBody(), 0);
      assertTrue(variable.isValidStatementForChild(statementInMethod));
    }
  }

  /**
   * When parameter is in constructor, it is possible that first {@link Statement} is
   * {@link SuperConstructorInvocation}, so it should be used as {@link StatementTarget} in
   * {@link MethodParameterVariableSupport#getStatementTarget()}.
   */
  public void test_variableSupport_superConstructor() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test(JButton button) {",
            "    super(null);",
            "  }",
            "}");
    // prepare "button" parameter
    MethodDeclaration constructor;
    SingleVariableDeclaration parameter;
    {
      TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(panel);
      constructor =
          AstNodeUtils.getMethodBySignature(typeDeclaration, "<init>(javax.swing.JButton)");
      parameter = DomGenerics.parameters(constructor).get(0);
    }
    // prepare JButton as parameter of constructor
    JavaInfo button;
    {
      CreationSupport creationSupport = new MethodParameterCreationSupport(parameter);
      button = JavaInfoUtils.createJavaInfo(m_lastEditor, JButton.class, creationSupport);
    }
    // create VariableSupport for "button" parameter
    VariableSupport variableSupport = new MethodParameterVariableSupport(button, parameter);
    Statement expectedStatement = DomGenerics.statements(constructor.getBody()).get(0);
    assertTarget(variableSupport.getStatementTarget(), null, expectedStatement, false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private ContainerInfo parsePanel(String... lines) throws Exception {
    ContainerInfo panel = parseContainer(lines);
    panel.refresh();
    // create "parent" model
    ContainerInfo parent;
    MethodDeclaration method_createPart;
    {
      // prepare method/parameter
      SingleVariableDeclaration parentParameter;
      {
        MethodDeclaration constructor =
            ((ThisCreationSupport) panel.getCreationSupport()).getConstructor();
        TypeDeclaration typeDeclaration = (TypeDeclaration) constructor.getParent();
        method_createPart =
            AstNodeUtils.getMethodBySignature(typeDeclaration, "createPart(java.awt.Container)");
        parentParameter = (SingleVariableDeclaration) method_createPart.parameters().get(0);
      }
      // create model
      CreationSupport creationSupport = new MethodParameterCreationSupport(parentParameter);
      parent =
          (ContainerInfo) JavaInfoUtils.createJavaInfo(
              m_lastEditor,
              m_lastLoader.loadClass("java.awt.Container"),
              creationSupport);
      parent.bindToExpression(parentParameter.getName());
      // set variable support
      parent.setVariableSupport(new MethodParameterVariableSupport(parent, parentParameter));
      // add as child
      panel.addChild(parent);
      parent.setAssociation(new UnknownAssociation());
      // include createPart() into execution flow
      JavaInfoUtils.getState(panel).getFlowDescription().addStartMethod(method_createPart);
    }
    // return root panel
    return panel;
  }
}
