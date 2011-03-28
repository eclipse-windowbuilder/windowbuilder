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
package org.eclipse.wb.internal.swing.model.property.editor.models.list;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.eval.IExpressionEvaluator;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;

import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclaration;

import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.ListModel;

/**
 * Implementation of {@link IExpressionEvaluator} for evaluating {@link ListModel} for {@link JList}
 * .
 * 
 * @author scheglov_ke
 * @coverage swing.model
 */
public final class ListModelEvaluator implements IExpressionEvaluator {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IExpressionEvaluator
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object evaluate(EvaluationContext context,
      Expression expression,
      ITypeBinding typeBinding,
      String typeQualifiedName) throws Exception {
    // check for anonymous AbstractListModel
    if (expression instanceof ClassInstanceCreation) {
      ClassInstanceCreation creation = (ClassInstanceCreation) expression;
      if (creation.getAnonymousClassDeclaration() != null
          && AstNodeUtils.isSuccessorOf(expression, AbstractListModel.class)) {
        AnonymousClassDeclaration declaration = creation.getAnonymousClassDeclaration();
        List<BodyDeclaration> declarations = DomGenerics.bodyDeclarations(declaration);
        for (BodyDeclaration bodyDeclaration : declarations) {
          if (bodyDeclaration instanceof FieldDeclaration) {
            FieldDeclaration fieldDeclaration = (FieldDeclaration) bodyDeclaration;
            VariableDeclaration fragment = DomGenerics.fragments(fieldDeclaration).get(0);
            Type fieldType = fieldDeclaration.getType();
            String fieldTypeName = AstNodeUtils.getFullyQualifiedName(fieldType, false);
            if (fieldTypeName.equals("java.lang.String[]")) {
              final String[] values =
                  (String[]) AstEvaluationEngine.evaluate(context, fragment.getInitializer());
              if (values != null) {
                return new AbstractListModel() {
                  private static final long serialVersionUID = 0L;

                  public int getSize() {
                    return values.length;
                  }

                  public Object getElementAt(int index) {
                    return values[index];
                  }
                };
              }
            }
          }
        }
      }
    }
    // we don't understand given expression
    return AstEvaluationEngine.UNKNOWN;
  }
}
