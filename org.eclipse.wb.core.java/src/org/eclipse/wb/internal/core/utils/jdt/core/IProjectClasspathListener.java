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
package org.eclipse.wb.internal.core.utils.jdt.core;

import org.eclipse.jdt.core.IJavaProject;

/**
 * Listener for {@link IJavaProject} classpath changes.
 *
 * @author scheglov_ke
 * @coverage core.util.jdt
 */
public interface IProjectClasspathListener {
	/**
	 * Adding JAR with ZIP source attachment to the {@link IJavaProject}.
	 *
	 * @param javaProject
	 *          the {@link IJavaProject} to add JAR to.
	 * @param jarPathString
	 *          the JAR file name relative to project.
	 * @param srcPathString
	 *          the path to ZIP file of sources, relative to project. May be <code>null</code>.
	 */
	void addClasspathEntry(IJavaProject javaProject, String jarPathString, String srcPathString)
			throws Exception;
}