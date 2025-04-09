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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Handler for build events in {@link WbBuilder}.
 *
 * @author scheglov_ke
 */
public interface BuilderHandler {
	void fullBuild(IProject project, IProgressMonitor monitor) throws CoreException;

	void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException;
}
