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
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;

/**
 * Same as {@link MethodOrderAfterCreation}, but before it.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class MethodOrderFirst extends MethodOrder {
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
    return javaInfo.getVariableSupport().getStatementTarget();
  }
}
