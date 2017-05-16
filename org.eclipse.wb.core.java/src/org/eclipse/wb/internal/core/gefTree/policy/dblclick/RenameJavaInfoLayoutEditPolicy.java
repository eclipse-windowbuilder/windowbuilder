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
package org.eclipse.wb.internal.core.gefTree.policy.dblclick;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.util.RenameConvertSupport;

import java.util.Collections;

/**
 * An instance of {@link DoubleClickLayoutEditPolicy} which opens rename/convert dialog by
 * double-clicking in widgets tree.
 *
 * @author mitin_aa
 * @coverage core.gefTree.policy
 */
final class RenameJavaInfoLayoutEditPolicy extends DoubleClickLayoutEditPolicy {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RenameJavaInfoLayoutEditPolicy(JavaInfo javaInfo) {
    super(javaInfo);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // DoubleClickLayoutEditPolicy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void performDoubleClick() {
    RenameConvertSupport.rename(Collections.singletonList(m_javaInfo));
  }
}
