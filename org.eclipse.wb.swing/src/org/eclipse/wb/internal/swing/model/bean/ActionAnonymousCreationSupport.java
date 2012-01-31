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
package org.eclipse.wb.internal.swing.model.bean;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ConstructorDescription;
import org.eclipse.wb.internal.core.utils.ast.AnonymousTypeDeclaration;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;

import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * Implementation of {@link CreationSupport} for {@link Action} as inner class.
 * 
 * @author scheglov_ke
 * @coverage swing.model
 */
public final class ActionAnonymousCreationSupport extends ActionAbstractCreationSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public ActionAnonymousCreationSupport() throws Exception {
    super();
  }

  public ActionAnonymousCreationSupport(ClassInstanceCreation creation) throws Exception {
    super(creation);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "anonymousAction";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // State
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setCreationEx() {
    super.setCreationEx();
    m_typeDeclaration = AnonymousTypeDeclaration.create(m_creation.getAnonymousClassDeclaration());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Evaluation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected AbstractAction create_createObject(EvaluationContext context) throws Exception {
    return (AbstractAction) AstEvaluationEngine.createClassInstanceCreationDirectly(
        context,
        m_creation);
  }

  @Override
  protected void create_evaluateInitialization(EvaluationContext context, AbstractAction action)
      throws Exception {
    {
      ConstructorDescription constructor = getConstructorDescription();
      if (constructor != null) {
        List<Expression> arguments = DomGenerics.arguments(m_creation);
        evaluateConstructorArguments(context, action, constructor, arguments);
      }
    }
    super.create_evaluateInitialization(context, action);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IActionSupport
  //
  ////////////////////////////////////////////////////////////////////////////
  public ConstructorDescription getConstructorDescription() {
    IMethodBinding binding = AstNodeUtils.getCreationBinding(m_creation);
    return m_typeDescription.getConstructor(binding);
  }
}
