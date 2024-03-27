/*******************************************************************************
 * Copyright (c) 2024 Patrick Ziegler and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
