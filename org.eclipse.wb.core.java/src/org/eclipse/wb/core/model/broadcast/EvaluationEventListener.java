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
package org.eclipse.wb.core.model.broadcast;

import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.parser.JavaInfoParser;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

/**
 * Listener for events during evaluating AST, in {@link JavaInfoParser} and {@link JavaInfo}.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public abstract class EvaluationEventListener {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Visiting
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When parser {@link ASTVisitor} leaves given {@link ASTNode} frame.
   */
  public void leaveFrame(ASTNode node) throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Evaluation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Given {@link ASTNode} is going to be evaluated.
   */
  public void evaluateBefore(EvaluationContext context, ASTNode node) throws Exception {
  }

  /**
   * Given {@link ASTNode} was just evaluated, so underlying toolkit objects may be changed.
   */
  public void evaluateAfter(EvaluationContext context, ASTNode node) throws Exception {
  }
}
