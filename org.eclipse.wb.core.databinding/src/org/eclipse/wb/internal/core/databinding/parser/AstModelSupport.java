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
package org.eclipse.wb.internal.core.databinding.parser;

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.IASTObjectInfo2;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * Default {@link AstObjectInfo} resolver that work over variable (
 * {@link VariableDeclarationFragment}, {@link Assignment} and etc.) otherwise resolve only by
 * creation {@link Expression}.
 *
 * @author lobas_av
 * @coverage bindings.parser
 */
public class AstModelSupport implements IModelSupport {
  private static final String REFERENCE_VALUE_KEY = "Reference value for this Expression";
  private final AstObjectInfo m_model;
  private Expression m_expression;
  private String m_nameReference;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  protected AstModelSupport(AstObjectInfo model) {
    m_model = model;
  }

  public AstModelSupport(AstObjectInfo model, Expression creation) {
    m_model = model;
    m_expression = creation;
    calculateNameReference(m_expression);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final void calculateNameReference(Expression expression) {
    if (m_nameReference == null) {
      // prepare parent
      ASTNode parent = expression.getParent();
      // handle variables and assignments
      if (parent instanceof VariableDeclarationFragment) {
        VariableDeclarationFragment fragment = (VariableDeclarationFragment) parent;
        m_nameReference = fragment.getName().getIdentifier();
      } else if (parent instanceof Assignment) {
        Assignment assignment = (Assignment) parent;
        m_nameReference = CoreUtils.getNodeReference(assignment.getLeftHandSide());
        //
        if (m_nameReference != null && m_model instanceof IASTObjectInfo2) {
          TypeDeclaration typeDeclaration = AstNodeUtils.getEnclosingTypeTop(expression);
          VariableDeclarationFragment fragment =
              AstNodeUtils.getFieldFragmentByName(typeDeclaration, m_nameReference);
          if (fragment != null) {
            IASTObjectInfo2 model = (IASTObjectInfo2) m_model;
            model.setField();
          }
        }
      }
      // configure model variable
      if (m_nameReference != null) {
        m_model.setVariableIdentifier(m_nameReference);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IModelSupport
  //
  ////////////////////////////////////////////////////////////////////////////
  public final AstObjectInfo getModel() {
    return m_model;
  }

  public boolean isRepresentedBy(Expression expression) throws Exception {
    // check object references
    if (m_expression == expression) {
      return true;
    }
    // check code references
    return isRepresentedOverReference(expression);
  }

  protected final boolean isRepresentedOverReference(Expression expression) throws Exception {
    if (m_nameReference != null) {
      // prepare cached value
      String nameReference = (String) expression.getProperty(REFERENCE_VALUE_KEY);
      if (nameReference != null) {
        return m_nameReference.equals(nameReference);
      }
      // check variable references
      if (AstNodeUtils.isVariable(expression)) {
        nameReference = CoreUtils.getNodeReference(expression);
        expression.setProperty(REFERENCE_VALUE_KEY, nameReference);
        //
        return m_nameReference.equals(nameReference);
      }
    }
    return false;
  }
}