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
package org.eclipse.wb.internal.swing.wizards.application;

import org.eclipse.wb.internal.swing.Activator;
import org.eclipse.wb.internal.swing.wizards.Messages;
import org.eclipse.wb.internal.swing.wizards.SwingWizardPage;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

import java.io.InputStream;

/**
 * {@link WizardPage} that creates new Swing application.
 * 
 * @author lobas_av
 * @coverage swing.wizards.ui
 */
public final class NewSwingApplicationWizardPage extends SwingWizardPage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public NewSwingApplicationWizardPage() {
    setTitle(Messages.NewSwingApplicationWizardPage_title);
    setImageDescriptor(Activator.getImageDescriptor("wizard/SwingApplication/banner.gif"));
    setDescription(Messages.NewSwingApplicationWizardPage_description);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // WizardPage
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createTypeMembers(IType newType, ImportsManager imports, IProgressMonitor monitor)
      throws CoreException {
    InputStream file = Activator.getFile("templates/SwingApplication.jvt");
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
}