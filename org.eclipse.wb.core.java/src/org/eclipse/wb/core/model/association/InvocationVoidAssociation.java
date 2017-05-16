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
package org.eclipse.wb.core.model.association;

import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.eval.ExecutionFlowUtils;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.variable.LazyVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;

import java.util.List;

import javax.swing.JToolBar;

/**
 * Special case of {@link InvocationAssociation} when child is <em>created</em> and associated with
 * parent using {@link MethodInvocation} of parent {@link JavaInfo}. For example
 * {@link JToolBar#addSeparator()}.
 *
 * @author scheglov_ke
 * @coverage core.model.association
 */
public final class InvocationVoidAssociation extends InvocationAssociation {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public InvocationVoidAssociation() {
  }

  public InvocationVoidAssociation(MethodInvocation invocation) {
    super(invocation);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setJavaInfo(JavaInfo javaInfo) throws Exception {
    super.setJavaInfo(javaInfo);
    // get MethodInvocation from CreationSupport
    CreationSupport creationSupport = m_javaInfo.getCreationSupport();
    m_invocation = (MethodInvocation) creationSupport.getNode();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void move(StatementTarget target) throws Exception {
    if (m_javaInfo.getVariableSupport() instanceof LazyVariableSupport) {
      moveLazy(target);
    } else {
      super.move(target);
    }
  }

  private void moveLazy(StatementTarget target) throws Exception {
    // prepare invocation of getX() lazy accessor, probably in parent
    ASTNode invocationInParent;
    {
      LazyVariableSupport lazy = (LazyVariableSupport) m_javaInfo.getVariableSupport();
      ExecutionFlowDescription flow = JavaInfoUtils.getState(m_javaInfo).getFlowDescription();
      List<ASTNode> invocations = ExecutionFlowUtils.getInvocations(flow, lazy.m_accessor);
      Assert.isTrue2(!invocations.isEmpty(), "No invocation for {0}", lazy.m_accessor);
      invocationInParent = invocations.get(0);
    }
    // move its Statement into target
    Statement invocationStatement = AstNodeUtils.getEnclosingStatement(invocationInParent);
    m_editor.moveStatement(invocationStatement, target);
  }
}
