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
package org.eclipse.wb.internal.core.editor.palette.model.entry;

import org.eclipse.wb.core.editor.palette.model.entry.ComponentEntryInfo;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jdt.core.IJavaProject;

import org.osgi.framework.Bundle;

/**
 * Library is description of JAR and optional source ZIP that should be added when
 * {@link ComponentEntryInfo} selected from palette.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette
 */
public final class LibraryInfo {
  private final String m_typeName;
  private final String m_bundleId;
  private final String m_jarPath;
  private final String m_srcPath;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LibraryInfo(IConfigurationElement element) {
    m_typeName = ExternalFactoriesHelper.getRequiredAttribute(element, "type");
    m_bundleId = ExternalFactoriesHelper.getRequiredAttribute(element, "bundle");
    m_jarPath = ExternalFactoriesHelper.getRequiredAttribute(element, "jar");
    m_srcPath = element.getAttribute("src");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Ensures that required type exists in {@link IJavaProject}, if needed adds JAR/ZIP to classpath.
   */
  public void ensure(IJavaProject javaProject) throws Exception {
    if (javaProject.findType(m_typeName) == null) {
      Bundle bundle = ExternalFactoriesHelper.getRequiredBundle(m_bundleId);
      // add JAR
      ProjectUtils.addJar(javaProject, bundle, m_jarPath, m_srcPath);
      ProjectUtils.waitForAutoBuild();
    }
  }
}
