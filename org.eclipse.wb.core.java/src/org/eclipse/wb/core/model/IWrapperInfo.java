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
package org.eclipse.wb.core.model;

import org.eclipse.wb.internal.core.model.AbstractWrapper;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;

/**
 * Interface for any Java-based wrapper model object (like a viewer in SWT).
 *
 * @author sablin_aa
 * @coverage core.model
 */
public interface IWrapperInfo {
  /**
   * @return the {@link AbstractWrapper}.
   * @see JavaInfoUtils#getWrapped(JavaInfo)
   */
  public IWrapper getWrapper();
}
