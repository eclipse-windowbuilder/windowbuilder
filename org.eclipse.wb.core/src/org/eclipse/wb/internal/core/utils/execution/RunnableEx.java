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
package org.eclipse.wb.internal.core.utils.execution;

/**
 * Analog of {@link Runnable} where method <code>run</code> can throw {@link Exception}.
 *
 * @author scheglov_ke
 * @coverage core.util
 */
public interface RunnableEx {
	/**
	 * Executes operation that can cause {@link Exception}.
	 */
	void run() throws Exception;
}
