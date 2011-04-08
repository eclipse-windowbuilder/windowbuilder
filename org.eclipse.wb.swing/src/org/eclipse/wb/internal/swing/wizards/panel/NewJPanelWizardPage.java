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
package org.eclipse.wb.internal.swing.wizards.panel;

import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.swing.Activator;
import org.eclipse.wb.internal.swing.wizards.Messages;
import org.eclipse.wb.internal.swing.wizards.SwingWizardPage;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

import java.io.InputStream;

import javax.swing.JPanel;

/**
 * {@link WizardPage} that creates new Swing {@link JPanel}.
 * 
 * @author lobas_av
 * @coverage swing.wizards.ui
 */
public final class NewJPanelWizardPage extends SwingWizardPage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public NewJPanelWizardPage() {
    setTitle(Messages.NewJPanelWizardPage_title);
    setImageDescriptor(Activator.getImageDescriptor("wizard/JPanel/banner.gif"));
    setDescription(Messages.NewJPanelWizardPage_description);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // WizardPage
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createLocalControls(Composite parent, int nColumns) {
    super.createLocalControls(parent, nColumns);
    // I always use same names during tests
    if (EnvironmentUtils.DEVELOPER_HOST) {
      setTypeName("JPanel_1", true);
    }
  }

  @Override
  protected void createTypeMembers(IType newType, ImportsManager imports, IProgressMonitor monitor)
      throws CoreException {
    InputStream file = Activator.getFile("templates/JPanel.jvt");
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
    setSuperClass("javax.swing.JPanel", true);
  }
}