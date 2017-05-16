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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.IImplicitCreationSupport;
import org.eclipse.wb.internal.core.model.variable.LazyVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;

/**
 * {@link MethodOrder} that targets {@link MethodInvocation}'s at relative of association.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public abstract class MethodOrderRelativeAssociation extends MethodOrder {
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
    if (canUseAssociation(javaInfo)) {
      JavaInfoUtils.materializeVariable(javaInfo);
      Statement statement = javaInfo.getAssociation().getStatement();
      boolean before = this instanceof MethodOrderBeforeAssociation;
      return new StatementTarget(statement, before);
    }
    return javaInfo.getVariableSupport().getStatementTarget();
  }

  private static boolean canUseAssociation(JavaInfo javaInfo) {
    if (javaInfo.getCreationSupport() instanceof IImplicitCreationSupport) {
      return canUseAssociation(javaInfo.getParentJava());
    }
    if (javaInfo.getAssociation() == null) {
      return false;
    }
    if (javaInfo.getAssociation().getStatement() == null) {
      return false;
    }
    if (javaInfo.getVariableSupport() instanceof LazyVariableSupport) {
      return false;
    }
    return true;
  }
}
