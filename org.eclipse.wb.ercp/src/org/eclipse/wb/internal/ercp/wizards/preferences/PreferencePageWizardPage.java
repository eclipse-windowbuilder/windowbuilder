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
package org.eclipse.wb.internal.ercp.wizards.preferences;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.ercp.Activator;
import org.eclipse.wb.internal.ercp.wizards.ERcpWizardPage;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.wizard.WizardPage;

import java.io.InputStream;
import java.util.List;

/**
 * {@link WizardPage} that creates new eRCP {@link PreferencePage}.
 * 
 * @author lobas_av
 * @coverage ercp.wizards.ui
 */
public final class PreferencePageWizardPage extends ERcpWizardPage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PreferencePageWizardPage() {
    setTitle("Create Eclipse PreferencePage");
    setImageDescriptor(Activator.getImageDescriptor("wizard/PreferencePage/banner.gif"));
    setDescription("Create empty PreferencePage.");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // WizardPage
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createTypeMembers(IType newType, ImportsManager imports, IProgressMonitor monitor)
      throws CoreException {
    InputStream file = Activator.getFile("templates/PreferencePage.jvt");
    fillTypeFromTemplate(newType, imports, monitor, file);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void initTypePage(IJavaElement elem) {
    super.initTypePage(elem);
    setSuperClass("org.eclipse.jface.preference.PreferencePage", true);
    List<String> interfacesNames = Lists.newArrayList();
    interfacesNames.add("org.eclipse.ui.IWorkbenchPreferencePage");
    setSuperInterfaces(interfacesNames, false);
  }
}