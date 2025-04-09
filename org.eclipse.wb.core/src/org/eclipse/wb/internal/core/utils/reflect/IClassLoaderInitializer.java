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
package org.eclipse.wb.internal.core.utils.reflect;

/**
 * Initializer for created project {@link ClassLoader}.
 *
 * @author scheglov_ke
 * @coverage core.util
 */
public interface IClassLoaderInitializer {
	/**
	 * Project {@link ClassLoader} was created and should be initialized.
	 */
	void initialize(ClassLoader classLoader);

	/**
	 * Clear all resources association with project {@link ClassLoader}.
	 */
	void deinitialize(ClassLoader classLoader);
}
