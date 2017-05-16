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
package org.eclipse.wb.internal.core.model.layout;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.variable.SyncParentChildVariableNameSupport;

/**
 * Support for managing name of <code>Layout</code>, so that it corresponds to the name of its
 * <code>Container</code>.
 *
 * @author sablin_aa
 * @coverage core.model.layout
 */
public abstract class LayoutNameSupport<T extends JavaInfo>
    extends
      SyncParentChildVariableNameSupport<T> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutNameSupport(T layout) {
    super(layout);
  }
}