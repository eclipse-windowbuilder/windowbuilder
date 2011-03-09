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
package org.eclipse.wb.internal.swing.model.property.editor.models.tree;

import com.google.common.collect.Maps;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.eval.IExpressionEvaluator;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.util.Map;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

/**
 * Implementation of {@link IExpressionEvaluator} for evaluating {@link TreeModel} for {@link JTree}
 * .
 * 
 * @author scheglov_ke
 * @coverage swing.model
 */
public final class TreeModelEvaluator implements IExpressionEvaluator {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IExpressionEvaluator
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object evaluate(EvaluationContext context,
      Expression expression,
      ITypeBinding typeBinding,
      String typeQualifiedName) throws Exception {
    AnonymousClassDeclaration rootDeclaration = findRootNodeDeclaration(expression);
    if (rootDeclaration != null) {
      // create root node
      final DefaultMutableTreeNode rootNode;
      {
        ClassInstanceCreation rootNodeCreation =
            (ClassInstanceCreation) rootDeclaration.getParent();
        StringLiteral rootTextLiteral = (StringLiteral) rootNodeCreation.arguments().get(0);
        rootNode = new DefaultMutableTreeNode(rootTextLiteral.getLiteralValue());
      }
      // create nodes
      final Map<String, DefaultMutableTreeNode> nameToNode = Maps.newTreeMap();
      rootDeclaration.accept(new ASTVisitor() {
        private DefaultMutableTreeNode m_lastNode;

        @Override
        public void endVisit(ClassInstanceCreation creation) {
          if (AstNodeUtils.getFullyQualifiedName(creation, false).equals(
              "javax.swing.tree.DefaultMutableTreeNode")
              && creation.arguments().size() == 1
              && creation.arguments().get(0) instanceof StringLiteral) {
            StringLiteral stringLiteral = (StringLiteral) creation.arguments().get(0);
            DefaultMutableTreeNode node =
                new DefaultMutableTreeNode(stringLiteral.getLiteralValue());
            if (creation.getLocationInParent() == VariableDeclarationFragment.INITIALIZER_PROPERTY) {
              String name =
                  ((VariableDeclarationFragment) creation.getParent()).getName().getIdentifier();
              nameToNode.put(name, node);
            } else if (creation.getLocationInParent() == Assignment.RIGHT_HAND_SIDE_PROPERTY
                && ((Assignment) creation.getParent()).getLeftHandSide() instanceof SimpleName) {
              Assignment assignment = (Assignment) creation.getParent();
              SimpleName variable = (SimpleName) assignment.getLeftHandSide();
              String name = variable.getIdentifier();
              nameToNode.put(name, node);
            } else {
              m_lastNode = node;
            }
          }
        }

        @Override
        public void endVisit(MethodInvocation invocation) {
          if (AstNodeUtils.getMethodSignature(invocation).equals(
              "add(javax.swing.tree.MutableTreeNode)")) {
            // prepare node
            DefaultMutableTreeNode node = null;
            {
              Object argument = invocation.arguments().get(0);
              if (argument instanceof SimpleName) {
                SimpleName variable = (SimpleName) argument;
                node = nameToNode.get(variable.getIdentifier());
              } else if (argument instanceof ClassInstanceCreation) {
                node = m_lastNode;
              }
            }
            // prepare parent
            DefaultMutableTreeNode parentNode = null;
            if (invocation.getExpression() instanceof SimpleName) {
              SimpleName variable = (SimpleName) invocation.getExpression();
              parentNode = nameToNode.get(variable.getIdentifier());
            } else if (invocation.getExpression() == null) {
              parentNode = rootNode;
            }
            // add node to parent
            if (parentNode != null && node != null) {
              parentNode.add(node);
            }
            // clear last node
            m_lastNode = null;
          }
        }
      });
      // OK, return model
      return new DefaultTreeModel(rootNode);
    }
    // we don't understand given expression
    return AstEvaluationEngine.UNKNOWN;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link AnonymousClassDeclaration} of {@link DefaultMutableTreeNode} for root node
   *         or <code>null</code> if no such model can be found.
   */
  private static AnonymousClassDeclaration findRootNodeDeclaration(Expression expression) {
    if (expression instanceof ClassInstanceCreation) {
      ClassInstanceCreation modelCreation = (ClassInstanceCreation) expression;
      ITypeBinding modelBinding = AstNodeUtils.getTypeBinding(modelCreation);
      if (AstNodeUtils.isSuccessorOf(modelBinding, DefaultTreeModel.class)
          && modelCreation.arguments().size() == 1
          && modelCreation.arguments().get(0) instanceof ClassInstanceCreation) {
        ClassInstanceCreation nodeCreation =
            (ClassInstanceCreation) modelCreation.arguments().get(0);
        ITypeBinding nodeBinding = AstNodeUtils.getTypeBinding(nodeCreation);
        if (nodeCreation.getAnonymousClassDeclaration() != null
            && AstNodeUtils.isSuccessorOf(nodeBinding, DefaultMutableTreeNode.class)) {
          return nodeCreation.getAnonymousClassDeclaration();
        }
      }
    }
    // no valid model
    return null;
  }
}
