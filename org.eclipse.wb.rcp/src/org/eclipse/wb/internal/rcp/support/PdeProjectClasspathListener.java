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
package org.eclipse.wb.internal.rcp.support;

import org.eclipse.wb.internal.core.utils.jdt.core.IProjectClasspathListener;
import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swt.graphics.Font;

/**
 * Shell for {@link Font} preview.
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
  public void addClasspathEntry(IJavaProject javaProject, String jarPathString, String srcPathString)
      throws Exception {
    IProject project = javaProject.getProject();
    if (PdeUtils.hasPDENature(project)) {
      PdeUtils utils = PdeUtils.get(project);
      utils.addLibrary(jarPathString);
    }
  }
}
