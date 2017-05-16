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
package org.eclipse.wb.core.eval;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Container for execution flow in AST.
 *
 * @author scheglov_ke
 * @coverage core.evaluation
 */
public final class ExecutionFlowDescription {
  private final List<MethodDeclaration> m_startMethods;
  private final Map<ASTNode, List<MethodDeclaration>> m_binaryFlowMethodsAfter = Maps.newHashMap();
  private final Map<ASTNode, List<MethodDeclaration>> m_binaryFlowMethodsBefore = Maps.newHashMap();
  private boolean m_binaryFlowLocked;
  private final LinkedList<Statement> m_traceStatements = Lists.newLinkedList();
  private int m_modificationCount;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ExecutionFlowDescription(MethodDeclaration... startMethods) {
    this(Lists.newArrayList(startMethods));
  }

  public ExecutionFlowDescription(List<MethodDeclaration> startMethods) {
    m_startMethods = startMethods;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public int modificationCount() {
    return m_modificationCount;
  }

  /**
   * @return the {@link MethodDeclaration}'s that should be used as entry points for visiting.
   */
  public List<MethodDeclaration> getStartMethods() {
    AstNodeUtils.removeDanglingNodes(m_startMethods);
    return m_startMethods;
  }

  /**
   * Adds new {@link MethodDeclaration} into {@link #getStartMethods()}.
   */
  public void addStartMethod(MethodDeclaration method) {
    m_modificationCount++;
    m_startMethods.add(method);
  }

  /**
   * @return <code>true</code> if execution flow starts in static method.
   */
  public boolean isStatic() {
    return AstNodeUtils.isStatic(getFirstMethod());
  }

  /**
   * @return the {@link TypeDeclaration} of this execution flow.
   */
  public TypeDeclaration geTypeDeclaration() {
    return AstNodeUtils.getEnclosingType(getFirstMethod());
  }

  /**
   * @return the {@link CompilationUnit} of this execution flow.
   */
  public CompilationUnit getCompilationUnit() {
    return (CompilationUnit) getFirstMethod().getRoot();
  }

  /**
   * @return the {@link AST} of this {@link ExecutionFlowDescription}.
   */
  public AST getAST() {
    return getFirstMethod().getAST();
  }

  /**
   * @return the first start {@link MethodDeclaration}, just to access top-level {@link ASTNode}'s.
   */
  private MethodDeclaration getFirstMethod() {
    return m_startMethods.get(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Trace
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link List} of {@link Statement}'s that form execution stack trace.
   */
  public List<Statement> getTraceStatements() {
    return m_traceStatements;
  }

  /**
   * @return the {@link Statement} that is visiting now.
   */
  private Statement getCurrentStatement() {
    Assert.isTrue(!m_traceStatements.isEmpty(), "Statements trace is empty!");
    return m_traceStatements.getFirst();
  }

  /**
   * Informs that given {@link Statement} is going to be visited, so that any binary flow
   * {@link MethodDeclaration} should be associated with it.
   */
  public void enterStatement(Statement statement) {
    m_traceStatements.addFirst(statement);
  }

  /**
   * Informs that given {@link Statement} was visited, so previous {@link Statement} from stack
   * should be used as current.
   */
  public void leaveStatement(Statement statement) {
    Assert.isTrue(!m_traceStatements.isEmpty(), "Statements trace is empty!");
    Statement lastStatement = m_traceStatements.removeFirst();
    Assert.isTrue(
        lastStatement == statement,
        "Execution flow problem. %s expected, but %s found.",
        statement,
        lastStatement);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Binary flow
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if binary flow was already analyzed, so it should not be attempted to
   *         analyze again.
   */
  public boolean isBinaryFlowLocked() {
    return m_binaryFlowLocked;
  }

  /**
   * States that binary flow was analyzed, so should not be changed anymore.<br>
   * Usually we lock binary flow after parsing, because we know that we already visited all
   * {@link ASTNode} 's that should be visited, so no need to loose time on further analysis.
   */
  public void lockBinaryFlow() {
    m_binaryFlowLocked = true;
  }

  /**
   * States that given {@link MethodDeclaration} should be executed before current {@link Statement}
   * .
   */
  public void addBinaryFlowMethodBefore(MethodDeclaration methodDeclaration) {
    m_modificationCount++;
    Statement currentStatement = getCurrentStatement();
    addBinaryFlowMethodBefore(currentStatement, methodDeclaration);
  }

  public void addBinaryFlowMethodBefore(Statement currentStatement,
      MethodDeclaration methodDeclaration) {
    m_modificationCount++;
    List<MethodDeclaration> methods = m_binaryFlowMethodsBefore.get(currentStatement);
    if (methods == null) {
      methods = Lists.newArrayList();
      m_binaryFlowMethodsBefore.put(currentStatement, methods);
    }
    // appends new method
    if (!methods.contains(methodDeclaration)) {
      methods.add(methodDeclaration);
    }
  }

  /**
   * States that given {@link MethodDeclaration} was visited from binary execution flow during
   * executing current {@link Statement}. So, this {@link MethodDeclaration} should be executed
   * (later) after current {@link Statement}.
   */
  public void addBinaryFlowMethodAfter(MethodDeclaration methodDeclaration) {
    m_modificationCount++;
    Statement currentStatement = getCurrentStatement();
    List<MethodDeclaration> methods = m_binaryFlowMethodsAfter.get(currentStatement);
    if (methods == null) {
      methods = Lists.newArrayList();
      m_binaryFlowMethodsAfter.put(currentStatement, methods);
    }
    // appends new method
    if (!methods.contains(methodDeclaration)) {
      methods.add(methodDeclaration);
    }
  }

  public void addBinaryFlowMethodAfter(Statement currentStatement,
      MethodDeclaration methodDeclaration) {
    m_modificationCount++;
    List<MethodDeclaration> methods = m_binaryFlowMethodsAfter.get(currentStatement);
    if (methods == null) {
      methods = Lists.newArrayList();
      m_binaryFlowMethodsAfter.put(currentStatement, methods);
    }
    // appends new method
    if (!methods.contains(methodDeclaration)) {
      methods.add(methodDeclaration);
    }
  }

  /**
   * @return {@link MethodDeclaration}'s that should be visited before given {@link Statement}.
   */
  public List<MethodDeclaration> getBinaryFlowMethodsBefore(Statement statement) {
    return m_binaryFlowMethodsBefore.get(statement);
  }

  /**
   * @return {@link MethodDeclaration}'s that should be visited after given {@link Statement}.
   */
  public List<MethodDeclaration> getBinaryFlowMethodsAfter(Statement statement) {
    return m_binaryFlowMethodsAfter.get(statement);
  }
}
