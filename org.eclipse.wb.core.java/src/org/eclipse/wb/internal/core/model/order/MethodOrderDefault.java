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
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;

/**
 * {@link MethodOrder} for single {@link MethodDescription} that redirects into default
 * {@link MethodOrder} of {@link JavaInfo}.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class MethodOrderDefault extends MethodOrder {
  ////////////////////////////////////////////////////////////////////////////
  //
  // MethodOrder
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canReference(JavaInfo javaInfo) {
    return javaInfo.getDescription().getDefaultMethodOrder().canReference(javaInfo);
  }

  @Override
  protected StatementTarget getSpecificTarget(JavaInfo javaInfo, String newSignature)
      throws Exception {
    return javaInfo.getDescription().getDefaultMethodOrder().getTarget(javaInfo, newSignature);
  }
}
