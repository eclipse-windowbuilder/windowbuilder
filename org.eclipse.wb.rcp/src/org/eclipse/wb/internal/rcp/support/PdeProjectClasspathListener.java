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
package org.eclipse.wb.internal.rcp.support;

import org.eclipse.wb.internal.core.utils.jdt.core.IProjectClasspathListener;
import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;

/**
 * {@link IProjectClasspathListener} for PDE.
 *
 * @author scheglov_ke
 * @coverage rcp.support
 */
public class PdeProjectClasspathListener implements IProjectClasspathListener {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final IProjectClasspathListener INSTANCE = new PdeProjectClasspathListener();

	private PdeProjectClasspathListener() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IProjectClasspathListener
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void addClasspathEntry(IJavaProject javaProject, String jarPathString, String srcPathString)
			throws Exception {
		IProject project = javaProject.getProject();
		if (PdeUtils.hasPDENature(project)) {
			PdeUtils utils = PdeUtils.get(project);
			utils.addLibrary(jarPathString);
		}
	}
}
