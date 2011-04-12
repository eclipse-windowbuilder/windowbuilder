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
package org.eclipse.wb.internal.rcp.wizards.project;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.core.wizards.DesignerJavaProjectWizard;
import org.eclipse.wb.internal.rcp.Activator;
import org.eclipse.wb.internal.rcp.wizards.WizardsMessages;

import org.eclipse.jdt.core.IJavaProject;

/**
 * Wizard that creates new RCP project.
 * 
 * @author lobas_av
 * @coverage rcp.wizards
 */
public class NewProjectWizard extends DesignerJavaProjectWizard {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public NewProjectWizard() {
    setDefaultPageImageDescriptor(Activator.getImageDescriptor("wizard/Project/banner.gif"));
    setWindowTitle(WizardsMessages.NewProjectWizard_title);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Wizard
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean performFinish() {
    boolean result = super.performFinish();
    if (result) {
      try {
        addRequiredLibraries(getCreatedElement());
      } catch (Throwable e) {
        DesignerPlugin.log(e);
      }
    }
    return result;
  }

  private static void addRequiredLibraries(IJavaProject javaProject) throws Exception {
    ProjectUtils.addPluginLibraries(javaProject, "org.eclipse.osgi");
    ProjectUtils.addPluginLibraries(javaProject, "org.eclipse.core.commands");
    ProjectUtils.addPluginLibraries(javaProject, "org.eclipse.equinox.common");
    ProjectUtils.addPluginLibraries(javaProject, "org.eclipse.equinox.registry");
    ProjectUtils.addPluginLibraries(javaProject, "org.eclipse.core.runtime");
    ProjectUtils.addPluginLibraries(javaProject, "org.eclipse.text");
    ProjectUtils.addSWTLibrary(javaProject);
    ProjectUtils.addPluginLibraries(javaProject, "org.eclipse.jface");
    ProjectUtils.addPluginLibraries(javaProject, "org.eclipse.jface.text");
    ProjectUtils.addPluginLibraries(javaProject, "org.eclipse.ui.workbench");
    ProjectUtils.addPluginLibraries(javaProject, "com.ibm.icu");
    ProjectUtils.addPluginLibraries(javaProject, "org.eclipse.ui.forms");
    if (EnvironmentUtils.IS_MAC && !EnvironmentUtils.IS_MAC_COCOA) {
      ProjectUtils.addPluginLibraries(javaProject, "org.eclipse.swt.carbon.macosx");
    }
  }
}