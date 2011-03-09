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
package org.eclipse.wb.internal.swing.model.layout.gbl;

import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.core.model.association.InvocationSecondaryAssociation;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.variable.AbstractNoNameVariableSupport;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.swing.Activator;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Implementation of {@link VariableSupport} for virtual {@link AbstractGridBagConstraintsInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.model.layout
 */
public final class VirtualConstraintsVariableSupport extends AbstractNoNameVariableSupport {
  final AbstractGridBagConstraintsInfo m_constraints;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public VirtualConstraintsVariableSupport(AbstractGridBagConstraintsInfo constraints) {
    super(constraints);
    m_constraints = constraints;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getTitle() throws Exception {
    return "(virtual GBL constraints)";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Expressions
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getReferenceExpression(NodeTarget target) throws Exception {
    materialize();
    return m_javaInfo.getVariableSupport().getReferenceExpression(target);
  }

  @Override
  public String getAccessExpression(NodeTarget target) throws Exception {
    return getReferenceExpression(target) + ".";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Materializes {@link AbstractGridBagConstraintsInfo} with this
   * {@link VirtualConstraintsVariableSupport} , i.e. ensures that it has {@link ASTNode}, real
   * {@link CreationSupport} and {@link VariableSupport}.
   */
  void materialize() throws Exception {
    ComponentInfo component = (ComponentInfo) m_javaInfo.getParent();
    AstEditor editor = component.getEditor();
    // check for usual code pattern: add(java.awt.Component)
    if (component.getAssociation() instanceof InvocationChildAssociation) {
      InvocationChildAssociation association =
          (InvocationChildAssociation) component.getAssociation();
      MethodInvocation invocation = association.getInvocation();
      if (AstNodeUtils.getMethodSignature(invocation).equals("add(java.awt.Component)")) {
        // prepare constraints source
        String source;
        if (Activator.getDefault().getPreferenceStore().getBoolean(IPreferenceConstants.P_GBC_LONG)) {
          source = m_constraints.newInstanceSourceLong();
        } else {
          source = m_constraints.newInstanceSourceShort();
        }
        // add constraints ASTNode
        Expression constraintsExpression = editor.addInvocationArgument(invocation, 1, source);
        m_javaInfo.addRelatedNode(constraintsExpression);
        // ConstructorCreationSupport was used
        {
          ConstructorCreationSupport creationSupport = new ConstructorCreationSupport();
          m_javaInfo.setCreationSupport(creationSupport);
          creationSupport.add_setSourceExpression(constraintsExpression);
        }
        // use empty variable
        VariableSupport variableSupport =
            new EmptyVariableSupport(m_javaInfo, constraintsExpression);
        m_javaInfo.setVariableSupport(variableSupport);
        // use "secondary" association
        m_javaInfo.setAssociation(new InvocationSecondaryAssociation(invocation));
        // done
        return;
      }
    }
    // XXX
    Assert.fail("Not implemented yet.");
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
    return "virtual-GBL-constraints";
  }
}