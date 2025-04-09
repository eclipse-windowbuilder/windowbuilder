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
package org.eclipse.wb.internal.dev.builder;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * {@link IncrementalProjectBuilder} that validates XML description files on XML file changes or
 * corresponding XSD changes.
 *
 * @author scheglov_ke
 */
public final class WbBuilder extends IncrementalProjectBuilder {
	public static final String ID = "org.eclipse.wb.dev.wbBuilder";
	private final List<BuilderHandler> m_handlers = new ArrayList<>();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public WbBuilder() {
		m_handlers.add(new ComponentDescriptionValidatorHandler());
		m_handlers.add(new MetaDataModificationHandler());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Builder
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@SuppressWarnings("rawtypes")
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	private void fullBuild(IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		for (BuilderHandler handler : m_handlers) {
			handler.fullBuild(project, monitor);
		}
	}

	private void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor)
			throws CoreException {
		for (BuilderHandler handler : m_handlers) {
			handler.incrementalBuild(delta, monitor);
		}
	}
}
