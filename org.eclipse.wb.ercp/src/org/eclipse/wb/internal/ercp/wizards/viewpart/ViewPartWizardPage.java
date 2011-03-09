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
package org.eclipse.wb.internal.ercp.wizards.viewpart;

import org.eclipse.wb.internal.core.utils.dialogfields.StringDialogField;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.ercp.Activator;
import org.eclipse.wb.internal.ercp.wizards.ERcpWizardPage;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import org.apache.commons.lang.StringUtils;

import java.io.InputStream;

/**
 * {@link WizardPage} that creates new eRCP {@link ViewPart}.
 * 
 * @author lobas_av
 * @coverage ercp.wizards.ui
 */
public final class ViewPartWizardPage extends ERcpWizardPage {
  private StringDialogField m_perspectiveNameField;
  private IType m_creationType;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ViewPartWizardPage() {
    setTitle("Create ViewPart");
    setImageDescriptor(Activator.getImageDescriptor("wizard/ViewPart/banner.gif"));
    setDescription("Create empty Eclipse RCP ViewPart.");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // WizardPage
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createTypeMembers(IType newType, ImportsManager imports, IProgressMonitor monitor)
      throws CoreException {
    m_creationType = newType;
    InputStream file = Activator.getFile("templates/ViewPart.jvt");
    fillTypeFromTemplate(newType, imports, monitor, file);
    // XXX
  }

  @Override
  protected String performSubstitutions(String code, ImportsManager imports) {
    code = super.performSubstitutions(code, imports);
    code = StringUtils.replace(code, "%VIEW_ID%", m_creationType.getFullyQualifiedName());
    return code;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void initTypePage(IJavaElement elem) {
    super.initTypePage(elem);
    setSuperClass("org.eclipse.ui.part.ViewPart", true);
  }

  @Override
  protected void createLocalControls(Composite parent, int columns) {
    m_perspectiveNameField = new StringDialogField();
    m_perspectiveNameField.setLabelText("View name (&title):");
    m_perspectiveNameField.setText("New ViewPart");
    m_perspectiveNameField.doFillIntoGrid(parent, columns);
    GridDataFactory.modify(m_perspectiveNameField.getTextControl(null)).hintH(getMaxFieldWidth());
  }
}