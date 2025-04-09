/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
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
