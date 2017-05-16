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