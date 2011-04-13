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
package org.eclipse.wb.internal.ercp.wizards.perspective;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.utils.dialogfields.StringDialogField;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.ercp.Activator;
import org.eclipse.wb.internal.ercp.wizards.ERcpWizardPage;
import org.eclipse.wb.internal.ercp.wizards.WizardsMessages;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

import java.io.InputStream;
import java.util.List;

/**
 * {@link WizardPage} that creates new eRCP perspective.
 * 
 * @author lobas_av
 * @coverage ercp.wizards.ui
 */
public final class PerspectiveWizardPage extends ERcpWizardPage {
  private StringDialogField m_perspectiveNameField;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PerspectiveWizardPage() {
    setTitle(WizardsMessages.PerspectiveWizardPage_title);
    setImageDescriptor(Activator.getImageDescriptor("wizard/Perspective/banner.gif"));
    setDescription(WizardsMessages.PerspectiveWizardPage_description);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // WizardPage
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createTypeMembers(IType newType, ImportsManager imports, IProgressMonitor monitor)
      throws CoreException {
    InputStream file = Activator.getFile("templates/PerspectiveFactory.jvt");
    fillTypeFromTemplate(newType, imports, monitor, file);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
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
    List<String> interfacesNames = Lists.newArrayList();
    interfacesNames.add("org.eclipse.ui.IPerspectiveFactory");
    setSuperInterfaces(interfacesNames, false);
  }

  @Override
  protected void createLocalControls(Composite parent, int columns) {
    m_perspectiveNameField = new StringDialogField();
    m_perspectiveNameField.setLabelText(WizardsMessages.PerspectiveWizardPage_nameLabel);
    m_perspectiveNameField.setText(WizardsMessages.PerspectiveWizardPage_nameDefault);
    m_perspectiveNameField.doFillIntoGrid(parent, columns);
    GridDataFactory.modify(m_perspectiveNameField.getTextControl(null)).hintH(getMaxFieldWidth());
  }
}