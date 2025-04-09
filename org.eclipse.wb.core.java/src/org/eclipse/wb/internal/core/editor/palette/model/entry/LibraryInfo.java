/*******************************************************************************
 * Copyright (c) 2024 Patrick Ziegler and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.editor.palette.model.entry;

import org.eclipse.jdt.core.IJavaProject;

/**
 * Base interface for additional libraries that need to be added to the
 * workspace in order to handle custom widgets. Supported are either OSGi
 * bundles or plain jars.
 */
public sealed interface LibraryInfo permits BundleLibraryInfo, JarLibraryInfo {
	/**
	 * Ensures that required type exists in {@link IJavaProject}, if needed adds
	 * JAR/ZIP to classpath.
	 */
	void ensure(IJavaProject javaProject) throws Exception;
}
