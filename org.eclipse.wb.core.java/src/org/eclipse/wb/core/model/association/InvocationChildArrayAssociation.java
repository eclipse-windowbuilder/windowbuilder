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

import org.eclipse.wb.internal.core.model.nonvisual.ArrayObjectInfo;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.List;

/**
 * Implementation of {@link Association} for array of child objects.
 *
 * @author sablin_aa
 * @coverage core.model.association
 */
public final class InvocationChildArrayAssociation extends InvocationAssociation {
  private final ArrayObjectInfo m_arrayInfo;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public InvocationChildArrayAssociation(MethodInvocation invocation, ArrayObjectInfo arrayInfo) {
    super(invocation);
    m_arrayInfo = arrayInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean remove() throws Exception {
    int elementIndex = m_arrayInfo.getItems().indexOf(m_javaInfo);
    Assert.isTrue(elementIndex >= 0);
    ArrayInitializer initializer = m_arrayInfo.getCreation().getInitializer();
    m_editor.removeArrayElement(initializer, elementIndex);
    // check then empty
    if (((List<?>) initializer.expressions()).isEmpty()) {
      // if no items...
      if (m_arrayInfo.isRemoveOnEmpty() && m_invocation.getParent() instanceof ExpressionStatement) {
        // ... and single invocation then remove invocation
        m_editor.removeEnclosingStatement(m_invocation);
      }
    }
    // yes, association removed
    return super.remove();
  }
}