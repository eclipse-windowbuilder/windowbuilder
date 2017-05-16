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

import org.eclipse.wb.core.model.IWrapperInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;

/**
 * {@link MethodOrder} to add {@link MethodInvocation} as last {@link Statement}, after all other
 * invocations and {@link JavaInfo} children.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class MethodOrderLast extends MethodOrder {
  ////////////////////////////////////////////////////////////////////////////
  //
  // MethodOrder
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canReference(JavaInfo javaInfo) {
    return false;
  }

  @Override
  protected StatementTarget getSpecificTarget(JavaInfo javaInfo, String newSignature)
      throws Exception {
    if (javaInfo instanceof IWrapperInfo) {
      javaInfo = javaInfo.getParentJava();
    }
    return JavaInfoUtils.getTarget(javaInfo);
  }
}
