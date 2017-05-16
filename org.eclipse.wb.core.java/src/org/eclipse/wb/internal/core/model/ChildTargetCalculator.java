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
package org.eclipse.wb.internal.core.model;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.eval.ExecutionFlowUtils;
import org.eclipse.wb.core.eval.ExecutionFlowUtils.ExecutionFlowFrameVisitor;
import org.eclipse.wb.core.eval.ExecutionFlowUtils.VisitingContext;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.InvocationVoidAssociation;
import org.eclipse.wb.core.model.association.UnknownAssociation;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.IImplicitCreationSupport;
import org.eclipse.wb.internal.core.model.creation.ThisCreationSupport;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.model.order.MethodOrder;
import org.eclipse.wb.internal.core.model.order.MethodOrderAfterChildren;
import org.eclipse.wb.internal.core.model.order.MethodOrderAfterParentChildren;
import org.eclipse.wb.internal.core.model.variable.LazyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.ThisVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;

import java.util.LinkedList;
import java.util.List;

/**
 * Calculator for {@link StatementTarget} to add child {@link JavaInfo} to parent.
 *
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage core.model
 */
public final class ChildTargetCalculator {
  private final JavaInfo m_parent;
  private final JavaInfo m_child;
  private final JavaInfo m_nextChild;
  private VariableSupport m_parentVariable;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ChildTargetCalculator(JavaInfo parent, JavaInfo child, JavaInfo nextChild) {
    m_parent = parent;
    m_child = child;
    m_nextChild = nextChild;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return new {@link StatementTarget} where {@link Statement}'s for new {@link JavaInfo} should
   *         be added to place them before given <code>nextChild</code>.
   */
  public StatementTarget getTarget() throws Exception {
    // check for special case: lazy and implicit factory
    {
      StatementTarget target = getTarget_Lazy_ImplicitFactory();
      if (target != null) {
        return target;
      }
    }
    // target before some existing child
    if (m_nextChild != null) {
      Statement associationStatement = m_nextChild.getAssociation().getStatement();
      Statement targetStatement = trackRelatedStatementsUp(m_nextChild, associationStatement);
      return new StatementTarget(targetStatement, true);
    }
    // check for "forced" method
    {
      StatementTarget target = ThisVariableSupport.getForcedTarget(m_parent);
      if (target != null) {
        return target;
      }
    }
    // materialize "parent"
    JavaInfoUtils.materializeVariable(m_parent);
    m_parentVariable = m_parent.getVariableSupport();
    // target as last child
    {
      Statement targetStatement = getLastTargetStatement();
      // usually we have statement for parent, in other case use variable target
      if (targetStatement != null) {
        targetStatement = trackTargetStatementsDown(targetStatement);
        return new StatementTarget(targetStatement, false);
      } else {
        return m_parentVariable.getChildTarget();
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When {@link LazyVariableSupport} is used for "implicit factory", we create/associate component
   * using artificial invocation of accessor. So, target "before component" should be before this
   * invocation, not before "create" method invocation.
   */
  private StatementTarget getTarget_Lazy_ImplicitFactory() {
    if (m_nextChild != null
        && m_nextChild.getAssociation() instanceof InvocationVoidAssociation
        && m_nextChild.getVariableSupport() instanceof LazyVariableSupport) {
      LazyVariableSupport lazy = (LazyVariableSupport) m_nextChild.getVariableSupport();
      List<ASTNode> invocations =
          ExecutionFlowUtils.getInvocations(
              JavaInfoUtils.getState(m_parent).getFlowDescription(),
              lazy.m_accessor);
      if (!invocations.isEmpty()) {
        ASTNode targetInvocation = invocations.get(0);
        Statement targetStatement = AstNodeUtils.getEnclosingStatement(targetInvocation);
        return new StatementTarget(targetStatement, true);
      }
    }
    return null;
  }

  /**
   * @return the first {@link Statement} related with given {@link JavaInfo} and accessible starting
   *         from given {@link Statement}.
   */
  private Statement trackRelatedStatementsUp(JavaInfo javaInfo, Statement targetStatement)
      throws Exception {
    Block targetBlock = (Block) targetStatement.getParent();
    List<Statement> statements = DomGenerics.statements(targetBlock);
    int index = statements.indexOf(targetStatement);
    // track inside of target block
    while (index-- != 0) {
      Statement newTargetStatement = statements.get(index);
      // check that new target is related
      if (!isRelatedStatement(javaInfo, newTargetStatement)) {
        return targetStatement;
      }
      // use new target
      targetStatement = newTargetStatement;
    }
    // we've reached first statement of block, go to the enclosing block
    if (targetBlock.getParent() instanceof Block) {
      return trackRelatedStatementsUp(javaInfo, targetBlock);
    }
    // we've reached first statement of method, use it as target
    return targetStatement;
  }

  /**
   * @return the last {@link Statement} related with given {@link JavaInfo} and accessible starting
   *         from given {@link Statement}.
   */
  private Statement trackTargetStatementsDown(Statement targetStatement) throws Exception {
    Block targetBlock = (Block) targetStatement.getParent();
    List<Statement> statements = DomGenerics.statements(targetBlock);
    boolean isLastStatement = statements.get(statements.size() - 1) == targetStatement;
    // last statement of block, go to the enclosing block
    if (isLastStatement && targetBlock.getParent() instanceof Block) {
      //  ask variable if we can leave block, for example that javaInfo is still visible
      if (!m_parentVariable.isValidStatementForChild(targetBlock)) {
        return targetStatement;
      }
      // track inside of enclosing block
      return trackTargetStatementsDown(targetBlock);
    }
    // we've reached last statement of method, use it as target
    return targetStatement;
  }

  /**
   * @return the last {@link Statement} that is related with given {@link JavaInfo} (or any of its
   *         children) and can be used as target for child.
   */
  private Statement getLastTargetStatement() throws Exception {
    final Statement[] lastStatement = new Statement[1];
    ExecutionFlowDescription flowDescription =
        JavaInfoUtils.getState(m_parent).getFlowDescription();
    ExecutionFlowUtils.visit(
        new VisitingContext(true),
        flowDescription,
        new ExecutionFlowFrameVisitor() {
          private final LinkedList<TargetMethodInformation> m_methodsStack = Lists.newLinkedList();
          private boolean m_anyChildFound = false;
          private boolean m_terminalFound = false;

          @Override
          public void postVisit(ASTNode node) {
            if (node instanceof Statement) {
              Statement statement = (Statement) node;
              postVisit(statement);
            }
          }

          private void postVisit(Statement statement) {
            // check for terminal
            {
              if (m_terminalFound) {
                return;
              }
              m_terminalFound = isTerminalStatement(statement);
              if (m_terminalFound) {
                return;
              }
            }
            // check if interesting Statement (creates parent or associates child)
            if (isParentCreation(statement) || isAssociationForSomeChild(statement)) {
              m_anyChildFound = true;
              m_methodsStack.getFirst().interesting = true;
            }
            // check for target
            if (m_parentVariable.isValidStatementForChild(statement)
                && isTargetStatement(statement)) {
              lastStatement[0] = statement;
            }
          }

          @Override
          public boolean enterFrame(ASTNode node) {
            if (node instanceof MethodDeclaration) {
              TargetMethodInformation methodInformation = new TargetMethodInformation();
              methodInformation.lastStatement = lastStatement[0];
              m_methodsStack.addFirst(methodInformation);
            }
            return true;
          }

          @Override
          public void leaveFrame(ASTNode node) {
            if (node instanceof MethodDeclaration) {
              TargetMethodInformation methodInformation = m_methodsStack.removeFirst();
              // if this Method is not interesting, ignore its related Statement's
              if (m_anyChildFound && !methodInformation.interesting) {
                lastStatement[0] = methodInformation.lastStatement;
              }
              // if child Method is interesting, parent is interesting too
              if (!m_methodsStack.isEmpty()) {
                m_methodsStack.getFirst().interesting |= methodInformation.interesting;
              }
            }
          }
        });
    return lastStatement[0];
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Configure" case support
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final class TargetMethodInformation {
    boolean interesting = false;
    Statement lastStatement;
  }

  private boolean isParentCreation(Statement statement) {
    CreationSupport parentCreation = m_parent.getCreationSupport();
    ASTNode parentNode = parentCreation.getNode();
    if (parentCreation instanceof ThisCreationSupport) {
      return AstNodeUtils.contains(parentNode, statement);
    }
    return statement == AstNodeUtils.getEnclosingStatement(parentNode);
  }

  private boolean isAssociationForSomeChild(Statement statement) {
    for (JavaInfo child : m_parent.getChildrenJava()) {
      Association association = child.getAssociation();
      if (association != null
          && !(child.getCreationSupport() instanceof IImplicitCreationSupport)
          && association.getStatement() == statement) {
        return true;
      }
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Target statement validation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @param statement
   *          the {@link Statement} to check.
   * @return <code>true</code> if given {@link Statement} can be used as target. Particularly it
   *         should be related with {@link #m_parent}.
   */
  private boolean isTargetStatement(Statement statement) {
    // check for "last" method invocation,
    //   for all JavaInfo starting from given and its parents
    {
      JavaInfo javaInfo = m_parent;
      while (javaInfo != null) {
        // check this JavaInfo
        MethodOrder methodOrder = getMethodOrder(javaInfo, statement);
        if (methodOrder != null && !methodOrder.canReference(javaInfo)) {
          return false;
        }
        // go to parent
        if (javaInfo.getParent() instanceof JavaInfo) {
          javaInfo = (JavaInfo) javaInfo.getParent();
        } else {
          break;
        }
      }
    }
    // check that Statement is related
    return isRelatedStatement(m_parent, statement);
  }

  /**
   * @return <code>true</code> if given {@link Statement} should not be used as target, and also no
   *         other {@link Statement}'s after it (on execution flow) should be checked as target.
   *         I.e. target is directly before given {@link Statement}.
   */
  private boolean isTerminalStatement(Statement statement) {
    boolean[] terminal = new boolean[]{false};
    if (m_child != null) {
      // support for MethodOrder_AfterChildren
      {
        MethodOrder methodOrder = getMethodOrder(m_parent, statement);
        if (methodOrder instanceof MethodOrderAfterChildren) {
          if (((MethodOrderAfterChildren) methodOrder).isTargetChild(m_child)) {
            terminal[0] = true;
          }
        }
      }
      // support for MethodOrder_AfterParentChildren
      if (m_parent != null) {
        for (JavaInfo parentChildInfo : m_parent.getChildrenJava()) {
          MethodOrder methodOrder = getMethodOrder(parentChildInfo, statement);
          if (methodOrder instanceof MethodOrderAfterParentChildren) {
            if (((MethodOrderAfterParentChildren) methodOrder).isTargetChild(m_child)) {
              terminal[0] = true;
            }
          }
        }
      }
    }
    m_parent.getBroadcastJava().target_isTerminalStatement(m_parent, m_child, statement, terminal);
    return terminal[0];
  }

  private MethodInvocation getMethodInvocation(JavaInfo javaInfo, Statement statement) {
    if (statement instanceof ExpressionStatement) {
      ExpressionStatement expressionStatement = (ExpressionStatement) statement;
      if (expressionStatement.getExpression() instanceof MethodInvocation) {
        MethodInvocation invocation = (MethodInvocation) expressionStatement.getExpression();
        if (javaInfo.isRepresentedBy(invocation.getExpression())) {
          return invocation;
        }
      }
    }
    return null;
  }

  private MethodDescription getMethodDescription(JavaInfo javaInfo, Statement statement) {
    MethodInvocation invocation = getMethodInvocation(javaInfo, statement);
    if (invocation != null) {
      return javaInfo.getDescription().getMethod(AstNodeUtils.getMethodBinding(invocation));
    }
    return null;
  }

  private MethodOrder getMethodOrder(JavaInfo javaInfo, Statement statement) {
    MethodDescription description = getMethodDescription(javaInfo, statement);
    if (description != null) {
      return description.getOrder();
    }
    return null;
  }

  /**
   * @param javaInfo
   *          the {@link JavaInfo} which related {@link ASTNode}'s we should check.
   * @param statement
   *          the {@link Statement} to check for being related.
   *
   * @return <code>true</code> if given {@link Statement} contains {@link ASTNode} related with
   *         given {@link JavaInfo} or any of its children.
   */
  private boolean isRelatedStatement(JavaInfo javaInfo, Statement statement) {
    // check, may be given Statement contains related ASTNode's
    for (ASTNode node : javaInfo.getRelatedNodes()) {
      // check statement
      {
        Statement relatedStatement = AstNodeUtils.getEnclosingStatement(node);
        // ignore "return component;"
        if (relatedStatement instanceof ReturnStatement) {
          continue;
        }
        // not our statement
        if (relatedStatement != statement) {
          continue;
        }
      }
      // check invocation
      {
        MethodInvocation invocation = javaInfo.getMethodInvocation(node);
        if (invocation != null) {
          // non-executable method can not be reference,
          // for example in "SWT Application" pattern shell.open() should be ignored
          if (!javaInfo.shouldEvaluateInvocation(invocation)) {
            continue;
          }
        }
      }
      // OK, given statement is related and valid
      return true;
    }
    // check children
    for (JavaInfo child : javaInfo.getChildrenJava()) {
      Association association = child.getAssociation();
      // each JavaInfo should have association, if there no association this means that we move it now
      boolean hasAssociation = association != null;
      // association may be invisible, for example performed in custom binary code of super-class
      boolean hasGoodAssociation = hasAssociation && !(association instanceof UnknownAssociation);
      // analyze child if:
      //  1. we move it;
      //  2. or we see its association.
      if (!hasAssociation || hasGoodAssociation) {
        if (isRelatedStatement(child, statement)) {
          return true;
        }
      }
    }
    // not related
    return false;
  }
}
