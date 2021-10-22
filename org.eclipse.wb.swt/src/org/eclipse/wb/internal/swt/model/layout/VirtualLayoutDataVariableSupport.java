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
package org.eclipse.wb.internal.swt.model.layout;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.variable.AbstractNoNameVariableSupport;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.LocalUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.swt.widgets.Control;

/**
 * Implementation of {@link VariableSupport} for virtual {@link LayoutDataInfo}.
 *
 * "Virtual" is state when there are no layout data at all, i.e. {@link Control#getLayoutData()}
 * returns <code>null</code>.
 *
 * "Implicit" is state when {@link Control#getLayoutData()} returns some not <code>null</code>
 * value, but layout data was not created it this {@link CompilationUnit}.
 *
 * @author lobas_av
 * @coverage swt.model.layout
 */
public final class VirtualLayoutDataVariableSupport extends AbstractNoNameVariableSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public VirtualLayoutDataVariableSupport(JavaInfo javaInfo) {
    super(javaInfo);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isDefault() {
    return true;
  }

  @Override
  public String getTitle() throws Exception {
    return "(virtual layout data)";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Expressions
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getReferenceExpression(NodeTarget target) throws Exception {
    return materialize().getReferenceExpression(target);
  }

  @Override
  public String getAccessExpression(NodeTarget target) throws Exception {
    return getReferenceExpression(target) + ".";
  }

  /**
   * Ensures that this {@link LayoutDataInfo} has {@link LocalUniqueVariableSupport}.
   */
  VariableSupport materialize() throws Exception {
    ControlInfo control = (ControlInfo) m_javaInfo.getParent();
    //
    MethodInvocation invocation;
    Expression expression;
    {
      String source = m_javaInfo.getDescription().getCreation(null).getSource();
      invocation = control.addMethodInvocation("setLayoutData(java.lang.Object)", source);
      expression = DomGenerics.arguments(invocation).get(0);
    }
    // set CreationSupport
    {
      m_javaInfo.setCreationSupport(new ConstructorCreationSupport((ClassInstanceCreation) expression));
      m_javaInfo.bindToExpression(expression);
      m_javaInfo.addRelatedNode(expression);
    }
    // set Association
    m_javaInfo.setAssociation(new InvocationChildAssociation(invocation));
    // set VariableSupport
    VariableSupport variableSupport = new EmptyVariableSupport(m_javaInfo, expression);
    m_javaInfo.setVariableSupport(variableSupport);
    return variableSupport;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Target
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public StatementTarget getStatementTarget() throws Exception {
    throw new IllegalStateException();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "virtual-layout-data";
  }
}