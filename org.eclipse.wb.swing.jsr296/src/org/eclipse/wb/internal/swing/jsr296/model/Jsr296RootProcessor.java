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
package org.eclipse.wb.internal.swing.jsr296.model;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.model.IRootProcessor;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.EvaluationEventListener;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.description.AbstractDescription;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;

import java.util.List;

/**
 * Support for <code>org.jdesktop.application.ResourceMap.injectComponents(Component)</code>
 * invocations.
 * 
 * @author sablin_aa
 * @coverage swing.jsr296
 */
public final class Jsr296RootProcessor implements IRootProcessor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IRootProcessor INSTANCE = new Jsr296RootProcessor();

  private Jsr296RootProcessor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IRootProcessor
  //
  ////////////////////////////////////////////////////////////////////////////
  public void process(final JavaInfo root, List<JavaInfo> components) throws Exception {
    // evaluation <code>ResourceMap.injectComponents(Component)</code>
    root.addBroadcastListener(new EvaluationEventListener() {
      private boolean evaluating = false;

      @Override
      public void evaluateAfter(EvaluationContext context, ASTNode node) throws Exception {
        if (!evaluating && isInjectComponentsNode(node)) {
          try {
            evaluating = true;
            AstEvaluationEngine.evaluate(context, (MethodInvocation) node);
            // 
            Expression javaInfoExpression = DomGenerics.arguments(node).get(0);
            JavaInfo javaInfo = root.getChildRepresentedBy(javaInfoExpression);
            if (javaInfo != null) {
              reNewJavaInfoPropertyDefaults(javaInfo);
            } else {
              reNewJavaInfoPropertyDefaults(root);
            }
          } finally {
            evaluating = false;
          }
        }
      }

      /**
       * Process renew properties values.
       * 
       * @param the
       *          {@link JavaInfo}.
       */
      private void reNewJavaInfoPropertyDefaults(JavaInfo javaInfo) throws Exception {
        if (javaInfo.isObjectReady()) {
          javaInfo.getDescription().visit(javaInfo, AbstractDescription.STATE_OBJECT_READY);
        }
        for (JavaInfo childJava : javaInfo.getChildrenJava()) {
          reNewJavaInfoPropertyDefaults(childJava);
        }
      }
    });
    // <code>ResourceMap.injectComponents(Component)</code> invocation is terminal for children
    root.addBroadcastListener(new JavaEventListener() {
      @Override
      public void target_isTerminalStatement(JavaInfo parent,
          JavaInfo child,
          Statement statement,
          boolean[] terminal) {
        if (statement instanceof ExpressionStatement) {
          Expression expression = ((ExpressionStatement) statement).getExpression();
          if (isInjectComponentsNode(expression)) {
            Expression javaInfoExpression = DomGenerics.arguments(expression).get(0);
            JavaInfo javaInfo = root.getChildRepresentedBy(javaInfoExpression);
            if (javaInfo != null && javaInfo.isItOrParentOf(parent)) {
              terminal[0] = true;
            }
          }
        }
      }
    });
  }

  private boolean isInjectComponentsNode(ASTNode node) {
    return AstNodeUtils.isMethodInvocation(
        node,
        "org.jdesktop.application.ResourceMap",
        "injectComponents(java.awt.Component)");
  }
}
