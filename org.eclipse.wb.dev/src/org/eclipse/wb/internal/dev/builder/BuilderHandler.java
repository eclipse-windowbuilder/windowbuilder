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
