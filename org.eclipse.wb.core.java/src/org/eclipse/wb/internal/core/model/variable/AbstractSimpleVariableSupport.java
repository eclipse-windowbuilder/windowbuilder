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
import com.google.common.collect.Sets;

import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.eval.ExecutionFlowUtils;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.GenericPropertySetValue;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.ObjectInfoVisitor;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.model.order.MethodOrder;
import org.eclipse.wb.internal.core.model.order.MethodOrderBeforeAssociation;
import org.eclipse.wb.internal.core.model.order.MethodOrderDefault;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.ICoreExceptionConstants;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * {@link VariableSupport} implementation for variable represented by {@link SimpleName}.
 *
 * @author scheglov_ke
 * @coverage core.model.variable
 */
public abstract class AbstractSimpleVariableSupport extends AbstractNamedVariableSupport {
  private final AbstractSimpleVariableSupport m_this = this;
  protected final VariableUtils m_utils;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractSimpleVariableSupport(JavaInfo javaInfo) {
    this(javaInfo, null);
  }

  public AbstractSimpleVariableSupport(JavaInfo javaInfo, Expression variable) {
    super(javaInfo, variable);
    m_utils = new VariableUtils(m_javaInfo);
    hookTextRenameEvent();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Expressions
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean hasExpression(NodeTarget target) {
    return JavaInfoUtils.isCreatedAtTarget(m_javaInfo, target);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Target
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public StatementTarget getStatementTarget() throws Exception {
    Statement assignmentStatement = AstNodeUtils.getEnclosingStatement(m_variable);
    return new StatementTarget(assignmentStatement, false);
  }

  @Override
  public void ensureInstanceReadyAt(StatementTarget target) throws Exception {
    moveStatements(target);
  }

  @Override
  public StatementTarget getAssociationTarget(StatementTarget target) throws Exception {
    // check, may be we should place association after "beforeAssociation" invocations
    {
      StatementTarget associationTarget = getAssociationTarget_beforeAssocitation();
      if (associationTarget != null) {
        return associationTarget;
      }
    }
    // use default target
    return getStatementTarget();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils for expressions
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final void assertJavaInfoCreatedAt(NodeTarget target) {
    boolean isCreated = JavaInfoUtils.isCreatedAtTarget(m_javaInfo, target);
    if (!isCreated) {
      Assert.fail(
          "{0} is not created at {1} in {2}",
          m_javaInfo,
          target,
          m_javaInfo.getEditor().getSource());
    }
  }

  protected final boolean isVisibleAtTarget(NodeTarget nodeTarget) {
    // Statement
    {
      StatementTarget target = nodeTarget.getStatementTarget();
      if (target != null) {
        Statement statement = target.getStatement();
        if (statement != null) {
          if (target.isBefore()) {
            return isVisible_before(statement);
          } else {
            return isVisible_after(statement);
          }
        } else {
          Block block = target.getBlock();
          if (target.isBefore()) {
            return isVisible_before(block);
          } else {
            return isVisible_end(block);
          }
        }
      }
    }
    // BodyDeclaration
    // Only field can be visible, but field variables use different implementation.
    // So, at this point we always "local variable" and it can not be visible in type.
    return false;
  }

  private boolean isVisible_before(Statement statement) {
    // check that "local" variable can be visible here at all
    if (isVisible_isLocalVariable_outsideOfItsBlock(statement)) {
      return false;
    }
    // check that variable already assigned
    return areInSuchOrderInBlock(m_variable, statement, false);
  }

  private boolean isVisible_after(Statement statement) {
    // check that "local" variable can be visible here at all
    if (isVisible_isLocalVariable_outsideOfItsBlock(statement)) {
      return false;
    }
    // check that variable already assigned
    if (areInSuchOrderInBlock(m_variable, statement, true)) {
      // ...and not reassigned later
      if (isVisible_isReuseVariable_reassignedLater(statement)) {
        return false;
      }
      // OK
      return true;
    }
    // bad situation - not assigned yet, we should not use this target at all
    return false;
  }

  private boolean isVisible_end(Block block) {
    if (AstNodeUtils.contains(block, m_variable)) {
      return !isVisible_isReuseVariable_reassignedLater(block);
    }
    return isVisible_after(block);
  }

  private boolean isVisible_isLocalVariable_outsideOfItsBlock(Statement statement) {
    if (!getClass().getName().contains(".Local")) {
      return false;
    }
    // check that Statement is inside of Block which defines variable
    Block declarationBlock = AstNodeUtils.getEnclosingBlock(m_declaration);
    return !AstNodeUtils.contains(declarationBlock, statement);
  }

  private boolean isVisible_isReuseVariable_reassignedLater(Statement statement) {
    if (!getClass().getName().contains("Reuse")) {
      return false;
    }
    // check all assignments
    List<Expression> allAssignments =
        ExecutionFlowUtils.getAssignments(getFlowDescription(), m_variable);
    for (ASTNode assignment : allAssignments) {
      if (AstNodeUtils.contains(assignment, m_variable)) {
        continue;
      }
      if (areInSuchOrderInBlock(assignment, statement, true)) {
        return true;
      }
    }
    // no
    return false;
  }

  private static boolean areInSuchOrderInBlock(ASTNode node_1, ASTNode node_2, boolean equals) {
    Block commonBlock = AstNodeUtils.getCommonBlock(node_1, node_2);
    if (commonBlock != null) {
      Statement statement_1 = AstNodeUtils.getStatementWithinBlock(commonBlock, node_1);
      Statement statement_2 = AstNodeUtils.getStatementWithinBlock(commonBlock, node_2);
      int index_1 = DomGenerics.statements(commonBlock).indexOf(statement_1);
      int index_2 = DomGenerics.statements(commonBlock).indexOf(statement_2);
      if (equals) {
        return index_1 <= index_2;
      } else {
        return index_1 < index_2;
      }
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "beforeAssociation" support
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return some {@link StatementTarget} if there are {@link MethodOrderBeforeAssociation}
   *         invocations or <code>null</code> if there are no such special invocations.
   */
  private StatementTarget getAssociationTarget_beforeAssocitation() {
    LinkedList<MethodInvocation> beforeAssociationInvocations = Lists.newLinkedList();
    for (ASTNode node : m_javaInfo.getRelatedNodes()) {
      MethodInvocation invocation = m_javaInfo.getMethodInvocation(node);
      if (invocation != null) {
        String signature = AstNodeUtils.getMethodSignature(invocation);
        MethodDescription description = m_javaInfo.getDescription().getMethod(signature);
        if (description != null) {
          if (isBeforeAssociation(description)) {
            beforeAssociationInvocations.add(invocation);
          }
        }
      }
    }
    // if there are "beforeAssociation" invocations, use last one as target
    if (!beforeAssociationInvocations.isEmpty()) {
      ExecutionFlowDescription flowDescription =
          JavaInfoUtils.getState(m_javaInfo).getFlowDescription();
      JavaInfoUtils.sortNodesByFlow(flowDescription, false, beforeAssociationInvocations);
      // use last invocation as target
      MethodInvocation targetInvocation = beforeAssociationInvocations.getLast();
      Statement targetStatement = AstNodeUtils.getEnclosingStatement(targetInvocation);
      return new StatementTarget(targetStatement, false);
    }
    // no "beforeAssociation"
    return null;
  }

  /**
   * @return <code>true</code> if invocations of given {@link MethodDescription} have effective
   *         order {@link MethodOrderBeforeAssociation}.
   */
  private boolean isBeforeAssociation(MethodDescription description) {
    MethodOrder order = description.getOrder();
    if (order instanceof MethodOrderDefault) {
      order = m_javaInfo.getDescription().getDefaultMethodOrder();
    }
    return order instanceof MethodOrderBeforeAssociation;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Adding
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets variable and initializer {@link Expression} after component adding.
   */
  protected final void add_setVariableAndInitializer(Expression variable, Expression initializer)
      throws Exception {
    m_variable = variable;
    rememberDeclaration();
    // process initializer
    if (AstNodeUtils.getTypeBinding(initializer) == null) {
      throw new DesignerException(ICoreExceptionConstants.GEN_NO_TYPE_BINDING,
          m_javaInfo.getEditor().getSource(initializer));
    }
    m_javaInfo.addRelatedNode(initializer);
    m_javaInfo.getCreationSupport().add_setSourceExpression(initializer);
    add_setVariableParameterizedType(initializer);
    // notify about variable name (later, when association will be done)
    m_javaInfo.addBroadcastListener(new JavaEventListener() {
      @Override
      public void addAfter(JavaInfo parent, JavaInfo child) throws Exception {
        if (child == m_javaInfo) {
          m_javaInfo.removeBroadcastListener(this);
          m_javaInfo.getBroadcastJava().variable_setName(m_this, null, getName());
        }
      }
    });
  }

  /**
   * At the time when we generate {@link VariableDeclarationStatement} with variable declaration and
   * initializer, we don't know yet what is exact {@link ITypeBinding} of initializer. So, we
   * generate raw type and replace it later with actual type arguments.
   */
  protected void add_setVariableParameterizedType(Expression initializer) throws Exception {
    ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(initializer);
    if (typeBinding.isAnonymous()) {
      typeBinding = typeBinding.getSuperclass();
    }
    if (typeBinding.isParameterizedType()) {
      AstEditor editor = m_javaInfo.getEditor();
      String genericTypeName = editor.getTypeBindingSource(typeBinding);
      editor.replaceVariableType(m_declaration, genericTypeName);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Moving
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Moves statements of this component into given {@link StatementTarget}.
   */
  protected final void moveStatements(StatementTarget target) throws Exception {
    AstEditor editor = m_javaInfo.getEditor();
    Statement[] statementsToMove = getStatementsToMove(target);
    // ensure unique names
    ensureUniqueVariablesDuringMove(target.getPosition(), statementsToMove);
    // do move Statement's
    for (Statement statement : statementsToMove) {
      editor.moveStatement(statement, target);
      target = new StatementTarget(statement, false);
    }
  }

  /**
   * When we move component, it is possible that at new position there are already visible
   * variables, so that moving component or any of it direct/indirect child has same name. So, we
   * check this and rename moving component.
   * <p>
   * Note, that we use {@link Statement}'s that will be moved to decide if moved
   * {@link VariableDeclaration} will shadow any subsequent variables or not.
   *
   * @param statementsToMove
   *          the {@link Statement}'s that will be moved.
   */
  private void ensureUniqueVariablesDuringMove(final int position,
      final Statement[] statementsToMove) throws Exception {
    m_javaInfo.accept(new ObjectInfoVisitor() {
      @Override
      public void endVisit(ObjectInfo objectInfo) throws Exception {
        if (objectInfo instanceof JavaInfo) {
          JavaInfo javaInfo = (JavaInfo) objectInfo;
          if (javaInfo.getVariableSupport() instanceof LocalVariableSupport) {
            LocalVariableSupport variableSupport =
                (LocalVariableSupport) javaInfo.getVariableSupport();
            variableSupport.ensureUniqueVariableDuringMove(position, statementsToMove);
          }
        }
      }
    });
  }

  /**
   * @param target
   *          the target to move {@link Statement}'s to.
   *
   * @return {@link Statement}'s that should be move with this component.
   */
  private Statement[] getStatementsToMove(StatementTarget target) throws Exception {
    // prepare statements to move
    Set<Statement> statementSet;
    {
      statementSet = Sets.newHashSet();
      addStatementsToMove(statementSet, target, m_javaInfo);
    }
    // replace statements with blocks
    {
      // prepare unique list of blocks
      List<Block> blocks = Lists.newArrayList();
      for (Statement statement : statementSet) {
        Block block = AstNodeUtils.getEnclosingBlock(statement);
        if (!blocks.contains(block)) {
          blocks.add(block);
        }
      }
      // replace statements with blocks
      Collections.sort(blocks, AstNodeUtils.SORT_BY_REVERSE_POSITION);
      for (Block block : blocks) {
        List<Statement> blockStatements = DomGenerics.statements(block);
        if (statementSet.containsAll(blockStatements) && block.getParent() instanceof Block) {
          statementSet.removeAll(blockStatements);
          statementSet.add(block);
        }
      }
    }
    // sort the resulting list of statements
    Statement[] statements = statementSet.toArray(new Statement[statementSet.size()]);
    Arrays.sort(statements, AstNodeUtils.SORT_BY_POSITION);
    return statements;
  }

  /**
   * Adds {@link Statement}'s related to given {@link JavaInfo} and any of its children.
   *
   * @param target
   */
  private static void addStatementsToMove(Set<Statement> statements,
      StatementTarget target,
      JavaInfo javaInfo) throws Exception {
    // add related nodes
    for (ASTNode relatedNode : javaInfo.getRelatedNodes()) {
      Statement statement = AstNodeUtils.getEnclosingStatement(relatedNode);
      if (!canMoveStatement(target, javaInfo, statement)) {
        continue;
      }
      if (statement != null) {
        statements.add(statement);
      }
    }
    // add children
    List<JavaInfo> children = Lists.newArrayList(javaInfo.getChildrenJava());
    javaInfo.getBroadcastJava().variable_addStatementsToMove(javaInfo, children);
    for (JavaInfo child : children) {
      addStatementsToMove(statements, target, child);
    }
  }

  /**
   * We should not move {@link Statement} if it is not in same method, for example in some separate
   * "configure()" method.
   */
  private static boolean canMoveStatement(StatementTarget target,
      JavaInfo javaInfo,
      Statement statement) {
    MethodDeclaration statementMethod = AstNodeUtils.getEnclosingMethod(statement);
    // move Statement, if it is same method as creation
    {
      ASTNode creationNode = javaInfo.getCreationSupport().getNode();
      MethodDeclaration creationMethod = AstNodeUtils.getEnclosingMethod(creationNode);
      if (statementMethod == creationMethod) {
        return true;
      }
    }
    // move Statement, if it is in same method as target
    MethodDeclaration targetMethod = AstNodeUtils.getEnclosingMethod(target.getNode());
    return targetMethod == null || statementMethod == targetMethod;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Morphing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Modifies {@link VariableDeclaration} of this variable to use new type.<br>
   * We need this during morphing components.<br>
   * No any validations for existing {@link MethodInvocation}, {@link Assignment}'s, etc are
   * performed.
   */
  public abstract void setType(String newTypeName) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rename on "text" property modification
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds listener for "text" property modification.
   */
  private void hookTextRenameEvent() {
    m_javaInfo.addBroadcastListener(new GenericPropertySetValue() {
      public void invoke(GenericPropertyImpl property, Object[] value, boolean[] shouldSetValue)
          throws Exception {
        if (property.getJavaInfo().getVariableSupport() == m_this && value[0] instanceof String) {
          GenericPropertyDescription description = property.getDescription();
          if (description != null && description.hasTrueTag("isText")) {
            NamesManager.renameForText(m_this, property, (String) value[0]);
          }
        }
      }
    });
  }

  /**
   * @return the new name of variable decorated with prefix/suffix.
   *
   * @param newName
   *          the new name generate using "text" property.
   */
  String decorateTextName(String newName) {
    return newName;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Conversion
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the added {@link FieldDeclaration} with given source.
   */
  protected final FieldDeclaration addField(String fieldSource) throws Exception {
    AstEditor editor = m_javaInfo.getEditor();
    TypeDeclaration targetType = getTypeDeclaration();
    boolean addFieldBefore = true;
    // try to find target field (add after it)
    FieldDeclaration targetField = null;
    for (BodyDeclaration bodyDeclaration : DomGenerics.bodyDeclarations(targetType)) {
      if (bodyDeclaration instanceof FieldDeclaration) {
        targetField = (FieldDeclaration) bodyDeclaration;
        addFieldBefore = false;
      }
    }
    // add field
    BodyDeclarationTarget target =
        new BodyDeclarationTarget(targetType, targetField, addFieldBefore);
    return editor.addFieldDeclaration(fieldSource, target);
  }
}
