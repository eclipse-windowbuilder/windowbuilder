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
package org.eclipse.wb.internal.core.model.clipboard;

import org.eclipse.wb.core.model.JavaInfo;

/**
 * Generic version of {@link ClipboardCommand}.
 *
 * @author scheglov_ke
 * @coverage core.model.clipboard
 */
public abstract class ComponentClipboardCommand<T> extends ClipboardCommand {
  private static final long serialVersionUID = 0L;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Execute
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  @SuppressWarnings("unchecked")
  public final void execute(JavaInfo javaInfo) throws Exception {
    T container = (T) javaInfo;
    execute(container);
  }

  /**
   * Implementation of {@link #execute(JavaInfo)} for <code>T</code>.
   */
  protected abstract void execute(T component) throws Exception;
}
