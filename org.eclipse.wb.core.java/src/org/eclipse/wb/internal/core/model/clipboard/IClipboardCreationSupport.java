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
import org.eclipse.wb.internal.core.model.creation.CreationSupport;

import java.io.Serializable;

/**
 * {@link CreationSupport} return implementation of this class to create {@link CreationSupport} for
 * pasting its {@link JavaInfo}.
 *
 * @author scheglov_ke
 * @coverage core.model.clipboard
 */
public abstract class IClipboardCreationSupport implements Serializable {
  private static final long serialVersionUID = 0L;

  /**
   * @param rootObject
   *          the root {@link JavaInfo} to which new {@link JavaInfo} will be added.
   *
   * @return the {@link CreationSupport} for creating {@link JavaInfo}.
   */
  public abstract CreationSupport create(JavaInfo rootObject) throws Exception;

  /**
   * Notification that {@link JavaInfo} was created using this {@link CreationSupport}.
   */
  public void apply(JavaInfo javaInfo) throws Exception {
  }
}
