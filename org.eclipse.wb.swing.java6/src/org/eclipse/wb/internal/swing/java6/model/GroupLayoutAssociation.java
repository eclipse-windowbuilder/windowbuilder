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
package org.eclipse.wb.internal.swing.java6.model;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.layout.group.model.GroupLayoutCodeSupport;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;

/**
 * GroupLayout-specific association.
 * 
 * @author mitin_aa
 */
public final class GroupLayoutAssociation extends Association {
  private Statement m_statement;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setJavaInfo(JavaInfo javaInfo) throws Exception {
    super.setJavaInfo(javaInfo);
    // extract association expression
    Expression expression =
        (Expression) javaInfo.getArbitraryValue(GroupLayoutCodeSupport.ASSOCIATION_EXPRESSION_KEY);
    if (expression != null) {
      m_statement = AstNodeUtils.getEnclosingStatement(expression);
    } else {
      m_statement = null;
    }
  }

  @Override
  public Statement getStatement() {
    return m_statement;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clone
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Association getCopy() {
    return new GroupLayoutAssociation();
  }
}
