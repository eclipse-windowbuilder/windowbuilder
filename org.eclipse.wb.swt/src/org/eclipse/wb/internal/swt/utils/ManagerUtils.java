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
package org.eclipse.wb.internal.swt.utils;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;

import org.eclipse.jdt.core.IJavaProject;

/**
 * Helper for adding SWT/JFace resource managers.
 * 
 * @author scheglov_ke
 * @coverage swt.utils
 */
public class ManagerUtils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private ManagerUtils() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Ensures that {@link IJavaProject} has type <code>org.eclipse.wb.swt.SWTResourceManager</code>.
   * 
   * @param component
   *          the component to access {@link IJavaProject}.
   */
  public static void ensure_SWTResourceManager(JavaInfo component) throws Exception {
    IJavaProject javaProject = component.getEditor().getJavaProject();
    ToolkitDescription toolkit = component.getDescription().getToolkit();
    ensure_SWTResourceManager(javaProject, toolkit);
  }

  /**
   * Ensures that {@link IJavaProject} has type <code>org.eclipse.wb.swt.SWTResourceManager</code>.
   */
  public static void ensure_SWTResourceManager(IJavaProject javaProject, ToolkitDescription toolkit)
      throws Exception {
    ProjectUtils.ensureResourceType(
        javaProject,
        toolkit.getBundle(),
        "org.eclipse.wb.swt.SWTResourceManager");
  }

  /**
   * Ensures that {@link IJavaProject} has type <code>org.eclipse.wb.swt.ResourceManager</code>.
   * 
   * @param component
   *          the component to access {@link IJavaProject}.
   */
  public static void ensure_ResourceManager(JavaInfo component) throws Exception {
    IJavaProject javaProject = component.getEditor().getJavaProject();
    ToolkitDescription toolkit = component.getDescription().getToolkit();
    ensure_ResourceManager(javaProject, toolkit);
  }

  /**
   * Ensures that {@link IJavaProject} has type <code>org.eclipse.wb.swt.ResourceManager</code>.
   */
  public static void ensure_ResourceManager(IJavaProject javaProject, ToolkitDescription toolkit)
      throws Exception {
    ensure_SWTResourceManager(javaProject, toolkit);
    ProjectUtils.ensureResourceType(
        javaProject,
        toolkit.getBundle(),
        "org.eclipse.wb.swt.ResourceManager");
  }
}
