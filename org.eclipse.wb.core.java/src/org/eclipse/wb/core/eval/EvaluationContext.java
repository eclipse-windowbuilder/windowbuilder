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

import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.ICoreExceptionConstants;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import java.util.List;
import java.util.Map;

/**
 * The context that is used by {@link AstEvaluationEngine} during evaluating AST.
 *
 * @author scheglov_ke
 * @coverage core.evaluation
 */
public class EvaluationContext {
  private final ClassLoader m_classLoader;
  private final ExecutionFlowDescription m_flowDescription;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EvaluationContext(ClassLoader classLoader, ExecutionFlowDescription flowDescription) {
    Assert.isNotNull(classLoader);
    Assert.isNotNull(flowDescription);
    m_classLoader = classLoader;
    m_flowDescription = flowDescription;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link ClassLoader} that should be used for acessing classes during evaluation.
   */
  public ClassLoader getClassLoader() {
    return m_classLoader;
  }

  /**
   * @return the {@link ExecutionFlowDescription} for evaluation.
   */
  public final ExecutionFlowDescription getFlowDescription() {
    return m_flowDescription;
  }

  /**
   * @return the source of given {@link ASTNode}.
   */
  public String getSource(ASTNode node) {
    return node.toString();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Additional evaluators
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<IExpressionEvaluator> m_evaluators = Lists.newArrayList();

  /**
   * @return the list of additional {@link IExpressionEvaluator}'s, that should be temporary added.
   */
  public final List<IExpressionEvaluator> getEvaluators() {
    return m_evaluators;
  }

  /**
   * Adds given {@link IExpressionEvaluator}.
   */
  public final void addEvaluator(IExpressionEvaluator evaluator) {
    m_evaluators.add(evaluator);
  }

  /**
   * Removes given {@link IExpressionEvaluator}.
   */
  public final void removeEvaluator(IExpressionEvaluator evaluator) {
    m_evaluators.remove(evaluator);
  }

  /**
   * Allows "pure" {@link Expression} evaluation, without any previous type binding, etc preparing.
   * We need this for example to support "null/this" expression in {@link MethodInvocation}.
   */
  public Object evaluate(Expression expression) throws Exception {
    return AstEvaluationEngine.UNKNOWN;
  }

  /**
   * This method is invoked when we need to evaluate {@link SingleVariableDeclaration}, i.e.
   * parameter of some {@link MethodDeclaration}, however we can not find single invocation of this
   * method.
   *
   * @return the evaluated value
   * @throws Exception
   *           if parameter can not be evaluated
   */
  public Object evaluateUnknownParameter(MethodDeclaration methodDeclaration,
      SingleVariableDeclaration parameter) throws Exception {
    throw new DesignerException(ICoreExceptionConstants.EVAL_NO_METHOD_INVOCATION,
        getSource(methodDeclaration),
        getSource(parameter));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Events
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * This method is invoked for each {@link Expression} requested for evaluation.
   */
  public void evaluationRequested(Expression expression) throws Exception {
  }

  /**
   * This method is invoked for each successfully evaluated {@link Expression}.
   */
  public void evaluationSuccessful(Expression expression, Object value) throws Exception {
  }

  /**
   * This method is invoked when evaluation of {@link Expression} was failed.
   *
   * @return the value to use instead of failed, or {@link AstEvaluationEngine#UNKNOWN} if exception
   *         should be thrown.
   */
  public Object evaluationFailed(Expression expression, Throwable e) throws Exception {
    return AstEvaluationEngine.UNKNOWN;
  }

  /**
   * This method is used to log "ignored" exceptions.
   */
  public void addException(ASTNode node, Throwable e) {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Arbitrary values map
  //
  ////////////////////////////////////////////////////////////////////////////
  private Map<Object, Object> m_arbitraryMap;

  /**
   * Associates the given value with the given key.
   */
  public final void putArbitraryValue(Object key, Object value) {
    if (m_arbitraryMap == null) {
      m_arbitraryMap = Maps.newHashMap();
    }
    m_arbitraryMap.put(key, value);
  }

  /**
   * @return the value to which the given key is mapped, or <code>null</code>.
   */
  public final Object getArbitraryValue(Object key) {
    if (m_arbitraryMap != null) {
      return m_arbitraryMap.get(key);
    }
    return null;
  }
}
