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
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;

import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * {@link MethodOrder} to add {@link MethodInvocation} after children {@link JavaInfo}'s of
 * specified types.
 *
 * @author sablin_aa
 * @author scheglov_ke
 * @coverage core.model.description
 */
public class MethodOrderAfterChildren extends MethodOrderChildren {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MethodOrderAfterChildren(String childrenTypeNames) {
    super(childrenTypeNames);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MethodOrder
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected StatementTarget getSpecificTarget(JavaInfo javaInfo, String newSignature)
      throws Exception {
    JavaInfo nextChild =
        GenericsUtils.getNextOrNull(javaInfo.getChildrenJava(), getLastChild(javaInfo));
    return JavaInfoUtils.getTarget(javaInfo, nextChild);
  }
}
