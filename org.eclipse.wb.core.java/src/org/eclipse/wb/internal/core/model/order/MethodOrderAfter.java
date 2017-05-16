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
package org.eclipse.wb.internal.core.model.order;

import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;

import java.util.List;

/**
 * {@link MethodOrder} to add {@link MethodInvocation} after invocation of some other method.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class MethodOrderAfter extends MethodOrder {
  private final String m_targetSignature;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MethodOrderAfter(String targetSignature) {
    m_targetSignature = targetSignature;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MethodOrder
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canReference(JavaInfo javaInfo) {
    return true;
  }

  @Override
  protected StatementTarget getSpecificTarget(JavaInfo javaInfo, String newSignature)
      throws Exception {
    List<MethodInvocation> targetInvocations = javaInfo.getMethodInvocations(m_targetSignature);
    if (!targetInvocations.isEmpty()) {
      // "after" means 'after all such invocations', so sort to find last one
      {
        AstEditor editor = javaInfo.getEditor();
        ExecutionFlowDescription flowDescription = EditorState.get(editor).getFlowDescription();
        JavaInfoUtils.sortNodesByFlow(flowDescription, false, targetInvocations);
      }
      // use last invocation as target
      MethodInvocation targetInvocation = targetInvocations.get(targetInvocations.size() - 1);
      Statement targetStatement = AstNodeUtils.getEnclosingStatement(targetInvocation);
      return new StatementTarget(targetStatement, false);
    }
    // no "target" invocation, but may be "target" has its own "order"
    {
      MethodDescription targetMethod = javaInfo.getDescription().getMethod(m_targetSignature);
      MethodOrder targetOrder = targetMethod.getOrder();
      return targetOrder.getTarget(javaInfo, newSignature);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if given signature is target one.
   */
  boolean isTarget(String newSignature) {
    return m_targetSignature.equals(newSignature);
  }
}
