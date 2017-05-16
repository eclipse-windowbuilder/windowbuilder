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
package org.eclipse.wb.internal.core.model.variable;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.variable.LazyVariableSupport.LazyVariableInformation;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Utilities for migrate to {@link LazyVariableSupport}.
 *
 * @author sablin_aa
 * @coverage core.model.variable
 */
public final class LazyVariableSupportUtils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Check
  //
  ////////////////////////////////////////////////////////////////////////////
  public static boolean canConvert(JavaInfo javaInfo) {
    boolean canConvert = true;
    // check variable support
    VariableSupport variableSupport = javaInfo.getVariableSupport();
    canConvert &=
        variableSupport.canConvertLocalToField()
            || variableSupport instanceof LazyVariableSupport
            || variableSupport instanceof FieldVariableSupport;
    //
    return canConvert;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Conversion
  //
  ////////////////////////////////////////////////////////////////////////////
  public static void convert(JavaInfo javaInfo) throws Exception {
    VariableSupport variableSupport = javaInfo.getVariableSupport();
    if (variableSupport instanceof LazyVariableSupport) {
      return;
    }
    // convert to field if can
    if (variableSupport.canConvertLocalToField()) {
      variableSupport.convertLocalToField();
      variableSupport = javaInfo.getVariableSupport();
    }
    // converting
    LazyVariableInformation variableInformation;
    if (variableSupport instanceof FieldVariableSupport) {
      variableInformation = convertAsField(javaInfo);
    } else {
      throw new IllegalStateException();
    }
    // change variable support
    LazyVariableSupport lazyVariableSupport =
        new LazyVariableSupport(javaInfo, variableInformation);
    javaInfo.setVariableSupport(lazyVariableSupport);
  }

  /**
   * Conversion for {@link FieldVariableSupport}.
   */
  private static LazyVariableInformation convertAsField(JavaInfo javaInfo) throws Exception {
    FieldVariableSupport fieldVariableSupport =
        (FieldVariableSupport) javaInfo.getVariableSupport();
    String fieldName = fieldVariableSupport.getName();
    Expression creation = (Expression) javaInfo.getCreationSupport().getNode();
    // add method by template
    MethodDeclaration accessor =
        addMethod(javaInfo, AstNodeUtils.getEnclosingType(creation), fieldName);
    // initialize variable and creation
    Assignment assignment;
    {
      IfStatement ifStatement = (IfStatement) accessor.getBody().statements().get(0);
      Block thenBlock = (Block) ifStatement.getThenStatement();
      ExpressionStatement expressionStatement = (ExpressionStatement) thenBlock.statements().get(0);
      assignment = (Assignment) expressionStatement.getExpression();
    }
    AstEditor editor = javaInfo.getEditor();
    StatementTarget target = new StatementTarget(assignment, false);
    // process related nodes
    List<Statement> moveStatements = Lists.newArrayList();
    List<ASTNode> replaceNodes = Lists.newArrayList();
    // prepare node lists...
    collectNodesToEdit(javaInfo, moveStatements, replaceNodes, target);
    moveStatements.remove(AstNodeUtils.getEnclosingStatement(creation));
    replaceNodes.remove(creation);
    // move creation directly
    {
      // check
      Assert.isTrue(canMoveNode(target, javaInfo, creation));
      // remove creation at old location
      String replacementSource = editor.getSource(creation);
      {
        // replace by "null" and remove statement
        NullLiteral newNullLiteral = creation.getAST().newNullLiteral();
        AstEditor.replaceNode(creation, newNullLiteral);
        editor.removeEnclosingStatement(newNullLiteral);
      }
      // replace "null" expression in template by creation
      ASTNode originalNode = assignment.getRightHandSide();
      int startPosition = originalNode.getStartPosition();
      editor.replaceExpression((Expression) originalNode, replacementSource);
      AstNodeUtils.moveNode(creation, startPosition);
      assignment.setRightHandSide(creation);
    }
    // replace statements with blocks
    {
      // prepare unique list of blocks
      List<Block> blocks = Lists.newArrayList();
      for (Statement statement : moveStatements) {
        Block block = AstNodeUtils.getEnclosingBlock(statement);
        if (!blocks.contains(block)) {
          blocks.add(block);
        }
      }
      // replace statements with blocks
      Collections.sort(blocks, AstNodeUtils.SORT_BY_REVERSE_POSITION);
      for (Block block : blocks) {
        List<Statement> blockStatements = DomGenerics.statements(block);
        if (moveStatements.containsAll(blockStatements) && block.getParent() instanceof Block) {
          moveStatements.removeAll(blockStatements);
          moveStatements.add(block);
        }
      }
    }
    // sort the resulting list of statements
    {
      Statement[] statements = moveStatements.toArray(new Statement[moveStatements.size()]);
      Arrays.sort(statements, AstNodeUtils.SORT_BY_POSITION);
      moveStatements = Lists.newArrayList(statements);
    }
    // move statements
    for (Statement moveStatement : moveStatements) {
      editor.moveStatement(moveStatement, target);
      target = new StatementTarget(moveStatement, false);
    }
    // replace field access nodes to access by invocation nodes
    String invocationSource = accessor.getName().getIdentifier() + "()";
    List<ASTNode> relatedNodes = javaInfo.getRelatedNodes();
    for (ASTNode replaceNode : replaceNodes) {
      if (relatedNodes.contains(replaceNode)) {
        Expression replaceExpression =
            editor.replaceExpression((Expression) replaceNode, invocationSource);
        relatedNodes.remove(replaceNode);
        javaInfo.addRelatedNode(replaceExpression);
      }
    }
    // ready
    return new LazyVariableInformation(accessor, assignment.getLeftHandSide(), creation);
  }

  /**
   * Fills nodes that should be move/replace with this component and any of its children.
   */
  private static void collectNodesToEdit(JavaInfo javaInfo,
      List<Statement> moveStatements,
      List<ASTNode> replaceNodes,
      StatementTarget target) throws Exception {
    // add related nodes
    for (ASTNode relatedNode : javaInfo.getRelatedNodes()) {
      Statement statement = AstNodeUtils.getEnclosingStatement(relatedNode);
      if (moveStatements.contains(statement)) {
        continue;
      }
      if (canMoveNode(target, javaInfo, relatedNode)) {
        moveStatements.add(statement);
      } else {
        replaceNodes.add(relatedNode);
        continue;
      }
    }
    // add children
    List<JavaInfo> children = Lists.newArrayList(javaInfo.getChildrenJava());
    javaInfo.getBroadcastJava().variable_addStatementsToMove(javaInfo, children);
    for (JavaInfo child : children) {
      collectNodesToEdit(child, moveStatements, replaceNodes, target);
    }
  }

  /**
   * We should not move {@link ASTNode} if it is not ready enclosing parameters, for example
   * invocation expression or arguments not ready at target.
   */
  private static boolean canMoveNode(StatementTarget target, JavaInfo javaInfo, ASTNode node) {
    if (!(node instanceof Expression)) {
      return true;
    }
    if (node instanceof MethodInvocation) {
      MethodInvocation invocation = (MethodInvocation) node;
      if (!canMoveInvocation(target, javaInfo, invocation)) {
        return false;
      }
    }
    return canMoveNode(target, javaInfo, node.getParent());
  }

  private static boolean canMoveInvocation(StatementTarget target,
      JavaInfo javaInfo,
      MethodInvocation invocation) {
    Expression invocationExpression = invocation.getExpression();
    if (invocationExpression == null) {
      // skip "this" invocation
      return false;
    }
    if (!javaInfo.isRepresentedBy(invocationExpression)) {
      // skip non-self invocation
      return false;
    }
    List<Expression> expressions = DomGenerics.arguments(invocation);
    // do not move invocations with parameters not ready at target
    for (Expression expression : expressions) {
      if (isRepresentedByOrChild(javaInfo, expression)) {
        continue;
      }
      JavaInfo expressionJavaInfo = javaInfo.getRootJava().getChildRepresentedBy(expression);
      if (expressionJavaInfo != null) {
        if (expressionJavaInfo.getVariableSupport() instanceof LazyVariableSupport) {
          continue;
        }
        if (!JavaInfoUtils.isCreatedAtTarget(expressionJavaInfo, new NodeTarget(target))) {
          return false;
        }
      }
    }
    return true;
  }

  private static boolean isRepresentedByOrChild(JavaInfo javaInfo, ASTNode node) {
    boolean represented = javaInfo.isRepresentedBy(node);
    for (JavaInfo childJavaInfo : javaInfo.getChildrenJava()) {
      if (represented) {
        return true;
      }
      represented |= isRepresentedByOrChild(childJavaInfo, node);
    }
    return represented;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the added "lazy" {@link MethodDeclaration} by template.
   */
  private static MethodDeclaration addMethod(JavaInfo javaInfo,
      TypeDeclaration typeDeclaration,
      String fieldName) throws Exception {
    AstEditor editor = javaInfo.getEditor();
    String methodName = getExpectedMethodName(javaInfo, fieldName);
    methodName = editor.getUniqueMethodName(methodName);
    // prepare target
    String className =
        ReflectionUtils.getCanonicalName(javaInfo.getDescription().getComponentClass());
    BodyDeclarationTarget bodyTarget = new BodyDeclarationTarget(typeDeclaration, false);
    // prepare modifiers
    String modifiers = LazyVariableSupport.prefMethodModifier(javaInfo);
    if (isStaticContext(javaInfo, javaInfo.getCreationSupport().getNode().getStartPosition())) {
      modifiers += "static ";
    }
    //
    String header = modifiers + className + " " + methodName + "()";
    List<String> bodyLines = Lists.newArrayList();
    bodyLines.add("if (" + fieldName + " == null) {");
    bodyLines.add("\t" + fieldName + " = null;");
    bodyLines.add("}");
    bodyLines.add("return " + fieldName + ";");
    return editor.addMethodDeclaration(header, bodyLines, bodyTarget);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utilities
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * We are going to create/access field and need to know if at position of assignment we work
   * within static or instance context (for example in <code>main</code>).
   *
   * @param position
   *          the position in source where we are going to access field.
   *
   * @return <code>true</code> if we work within static context.
   */
  public static boolean isStaticContext(JavaInfo javaInfo, int position) {
    MethodDeclaration method = javaInfo.getEditor().getEnclosingMethod(position);
    return AstNodeUtils.isStatic(method);
  }

  /**
   * @return the name of method that corresponds to given name of field.
   */
  public static String getExpectedMethodName(JavaInfo javaInfo, String fieldName) {
    String strippedFieldName =
        new VariableUtils(javaInfo).stripPrefixSuffix(
            fieldName,
            JavaCore.CODEASSIST_FIELD_PREFIXES,
            JavaCore.CODEASSIST_FIELD_SUFFIXES);
    return "get" + StringUtils.capitalize(strippedFieldName);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // LazyVariableInformation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Checks if given {@link JavaInfo} uses "lazy" pattern and sets {@link LazyVariableSupport}.
   */
  public static void setLazyVariable(JavaInfo javaInfo) throws Exception {
    ASTNode creationNode = javaInfo.getCreationSupport().getNode();
    MethodDeclaration method = AstNodeUtils.getEnclosingMethod(creationNode);
    if (method != null) {
      LazyVariableInformation lazyInformation = getInformation(method);
      if (lazyInformation != null && lazyInformation.getCreation() == creationNode) {
        javaInfo.setVariableSupport(new LazyVariableSupport(javaInfo, lazyInformation));
      }
    }
  }

  /**
   * @return the {@link LazyVariableInformation} if given {@link MethodDeclaration} is access of
   *         component using "lazy creation" pattern, or <code>null</code> in other case.
   */
  public static LazyVariableInformation getInformation(MethodDeclaration method) {
    if (method.parameters().isEmpty() && isNotAbstract(method)) {
      List<Statement> statements = DomGenerics.statements(method.getBody());
      // check for: [if () {}, return someExpression;]
      if (statements.size() == 2
          && statements.get(0) instanceof IfStatement
          && statements.get(1) instanceof ReturnStatement) {
        IfStatement ifStatement = (IfStatement) statements.get(0);
        // prepare "return expression;"
        Expression returnExpression;
        {
          ReturnStatement returnStatement = (ReturnStatement) statements.get(1);
          returnExpression = returnStatement.getExpression();
          if (returnExpression == null) {
            return null;
          }
        }
        // check for: [if (infixExpression) {}, return simpleName;]
        if (ifStatement.getThenStatement() instanceof Block
            && ifStatement.getElseStatement() == null
            && ifStatement.getExpression() instanceof InfixExpression) {
          IVariableBinding variableBinding = AstNodeUtils.getVariableBinding(returnExpression);
          InfixExpression condition = (InfixExpression) ifStatement.getExpression();
          // check for: [if (simpleName == null) {}, return field;]
          if (variableBinding != null
              && variableBinding.isField()
              && condition.getOperator() == InfixExpression.Operator.EQUALS
              && condition.getRightOperand() instanceof NullLiteral) {
            // check for: [if (field == null) {}, return field;]
            if (sameVariables(condition.getLeftOperand(), returnExpression)) {
              Block block = (Block) ifStatement.getThenStatement();
              return getInformation(method, block, returnExpression);
            }
          }
        }
      }
    }
    // no "lazy creation" detected
    return null;
  }

  private static boolean isNotAbstract(MethodDeclaration method) {
    return method.getBody() != null;
  }

  private static LazyVariableInformation getInformation(MethodDeclaration method,
      Block thenBlock,
      Expression variable) {
    List<Statement> thenStatements = DomGenerics.statements(thenBlock);
    for (Statement thenStatement : thenStatements) {
      LazyVariableInformation information = getInformation(method, variable, thenStatement);
      if (information != null) {
        return information;
      }
    }
    return null;
  }

  private static LazyVariableInformation getInformation(MethodDeclaration method,
      Expression variable,
      Statement thenStatement) {
    // try {}
    if (thenStatement instanceof TryStatement) {
      TryStatement tryStatement = (TryStatement) thenStatement;
      return getInformation(method, tryStatement.getBody(), variable);
    }
    // variable = new Component();
    if (thenStatement instanceof ExpressionStatement) {
      ExpressionStatement expressionStatement = (ExpressionStatement) thenStatement;
      if (expressionStatement.getExpression() instanceof Assignment) {
        Assignment assignment = (Assignment) expressionStatement.getExpression();
        Expression assignmentVariable = assignment.getLeftHandSide();
        if (sameVariables(assignmentVariable, variable)) {
          return new LazyVariableInformation(method,
              assignmentVariable,
              assignment.getRightHandSide());
        }
      }
    }
    // fail
    return null;
  }

  /**
   * Checks if two given {@link Expression}-s are same variables.
   */
  private static boolean sameVariables(Expression expression_1, Expression expression_2) {
    String source_1 = expression_1.toString();
    String source_2 = expression_2.toString();
    return source_1.equals(source_2);
  }
}
