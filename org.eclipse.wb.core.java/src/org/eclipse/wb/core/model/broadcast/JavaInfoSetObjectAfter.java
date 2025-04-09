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
package org.eclipse.wb.core.model.broadcast;

import org.eclipse.wb.core.model.JavaInfo;

/**
 * Listener for {@link JavaInfo} events.
 *
 * Used to get notified after object of {@link JavaInfo} becomes set using
 * {@link JavaInfo#setObject(Object)}
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public interface JavaInfoSetObjectAfter {
	/**
	 * After object of {@link JavaInfo} set using {@link JavaInfo#setObject(Object)}.
	 */
	void invoke(JavaInfo target, Object o) throws Exception;
}