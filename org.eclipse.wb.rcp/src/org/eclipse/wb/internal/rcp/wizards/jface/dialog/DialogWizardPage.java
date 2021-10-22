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
package org.eclipse.wb.internal.rcp.wizards.jface.dialog;

import org.eclipse.wb.internal.rcp.Activator;
import org.eclipse.wb.internal.rcp.wizards.RcpWizardPage;
import org.eclipse.wb.internal.rcp.wizards.WizardsMessages;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardPage;

import java.io.InputStream;

/**
 * {@link WizardPage} that creates new JFace {@link Dialog}.
 *
 * @author lobas_av
 * @coverage rcp.wizards.ui
 */
public final class DialogWizardPage extends RcpWizardPage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DialogWizardPage() {
    setTitle(WizardsMessages.DialogWizardPage_title);
    setImageDescriptor(Activator.getImageDescriptor("wizard/JFace/Dialog/banner.gif"));
    setDescription(WizardsMessages.DialogWizardPage_description);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // WizardPage
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createTypeMembers(IType newType, ImportsManager imports, IProgressMonitor monitor)
      throws CoreException {
    InputStream file = Activator.getFile("templates/jface/Dialog.jvt");
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
    setSuperClass("org.eclipse.jface.dialogs.Dialog", true);
  }
}