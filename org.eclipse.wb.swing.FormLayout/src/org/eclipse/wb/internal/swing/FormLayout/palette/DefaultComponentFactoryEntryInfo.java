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
package org.eclipse.wb.internal.swing.FormLayout.palette;

import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.editor.palette.model.entry.ToolEntryInfo;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;

import com.jgoodies.forms.factories.DefaultComponentFactory;

import org.osgi.framework.Bundle;

/**
 * {@link EntryInfo} for factory methods from {@link DefaultComponentFactory}.
 * 
 * @author scheglov_ke
 * @coverage swing.FormLayout.model
 */
public abstract class DefaultComponentFactoryEntryInfo extends ToolEntryInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Ensures that jar/source with {@link DefaultComponentFactory} is added to {@link IJavaProject}.
   */
  protected final void ensureLibrary() throws Exception {
    if (!ProjectUtils.hasType(m_javaProject, "com.jgoodies.forms.factories.DefaultComponentFactory")) {
      Bundle libBundle = Platform.getBundle("org.eclipse.wb.swing.FormLayout.lib");
      ProjectUtils.addJar(
          m_javaProject,
          libBundle,
          "jgoodies-common-1.8.0.jar",
          "jgoodies-common-1.8.0-sources.jar");
      ProjectUtils.addJar(
          m_javaProject,
          libBundle,
          "jgoodies-forms-1.8.0.jar",
          "jgoodies-forms-1.8.0-sources.jar");
    }
  }
}
