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

import org.eclipse.wb.internal.core.model.nonvisual.EllipsisObjectInfo;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Implementation of {@link Association} for ellipsis-array of child objects.
 *
 * @author sablin_aa
 * @coverage core.model.association
 */
public final class InvocationChildEllipsisAssociation extends InvocationAssociation {
  private final EllipsisObjectInfo m_ellipsisInfo;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public InvocationChildEllipsisAssociation(MethodInvocation invocation,
      EllipsisObjectInfo ellipsisInfo) {
    super(invocation);
    m_ellipsisInfo = ellipsisInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean remove() throws Exception {
    int elementIndex = m_ellipsisInfo.getItems().indexOf(m_javaInfo);
    Assert.isTrue(elementIndex >= 0);
    int parameterIndex = m_ellipsisInfo.getParameterIndex();
    m_editor.removeInvocationArgument(m_invocation, parameterIndex + elementIndex);
    // check then empty
    if (m_invocation.arguments().size() <= parameterIndex) {
      // if no items...
      if (m_ellipsisInfo.isRemoveOnEmpty()
          && m_invocation.getParent() instanceof ExpressionStatement) {
        // ... and single invocation then remove invocation
        m_editor.removeEnclosingStatement(m_invocation);
      } else if (parameterIndex == 0) {
        // ... and no more arguments
        m_editor.replaceExpression(m_invocation, m_ellipsisInfo.getOnEmptySource());
        m_ellipsisInfo.setInvocation(null);
      }
    }
    // yes, association removed
    return super.remove();
  }
}