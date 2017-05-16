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
package org.eclipse.wb.internal.core.model.creation;

import org.eclipse.wb.core.model.WrapperByMethod;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.WrappedObjectAssociation;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;

/**
 * Implementation of {@link WrapperMethodCreationSupport} with {@link ILiveCreationSupport}.
 *
 * @author sablin_aa
 * @author scheglov_ke
 * @coverage core.model.creation
 */
public class WrapperMethodLiveCreationSupport extends WrapperMethodCreationSupport
    implements
      ILiveCreationSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public WrapperMethodLiveCreationSupport(WrapperByMethod viewer) {
    super(viewer);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public CreationSupport getLiveComponentCreation() {
    return new LiveCreationSupport();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // LiveCreationSupport
  //
  ////////////////////////////////////////////////////////////////////////////
  private class LiveCreationSupport extends CreationSupport {
    private Expression m_expression;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public ASTNode getNode() {
      return m_expression;
    }

    @Override
    public boolean isJavaInfo(ASTNode node) {
      return node == m_expression;
    }

    @Override
    public Association getAssociation() throws Exception {
      return new WrappedObjectAssociation(m_wrapper);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Add
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public String add_getSource(NodeTarget target) throws Exception {
      String controlSource = "." + m_wrapper.getControlMethod().getName() + "()";
      return m_wrapper.getWrapperInfo().getCreationSupport().add_getSource(target) + controlSource;
    }

    @Override
    public void add_setSourceExpression(Expression expression) throws Exception {
      m_expression = expression;
      m_javaInfo.bindToExpression(expression);
    }
  }
}
