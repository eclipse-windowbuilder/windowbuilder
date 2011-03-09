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
package org.eclipse.wb.internal.rcp.wizards.forms.details;

import org.eclipse.wb.internal.rcp.Activator;
import org.eclipse.wb.internal.rcp.wizards.RcpWizardPage;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IDetailsPage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link WizardPage} that creates new Forms {@link IDetailsPage}.
 * 
 * @author lobas_av
 * @coverage rcp.wizards.ui
 */
public final class DetailsPageWizardPage extends RcpWizardPage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DetailsPageWizardPage() {
    setTitle("Create IDetailsPage");
    setImageDescriptor(Activator.getImageDescriptor("wizard/Forms/IDetailsPage/banner.gif"));
    setDescription("Create empty Forms IDetailsPage.");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // WizardPage
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createTypeMembers(IType newType, ImportsManager imports, IProgressMonitor monitor)
      throws CoreException {
    InputStream file = Activator.getFile("templates/forms/IDetailsPage.jvt");
    fillTypeFromTemplate(newType, imports, monitor, file);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createDesignSuperClassControls(Composite composite, int nColumns) {
  }

  @Override
  protected void initTypePage(IJavaElement elem) {
    super.initTypePage(elem);
    List<String> interfacesNames = new ArrayList<String>();
    interfacesNames.add("org.eclipse.ui.forms.IDetailsPage");
    setSuperInterfaces(interfacesNames, false);
  }
}