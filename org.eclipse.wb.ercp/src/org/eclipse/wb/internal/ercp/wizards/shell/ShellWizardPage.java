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
package org.eclipse.wb.internal.ercp.wizards.shell;

import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.IDialogFieldListener;
import org.eclipse.wb.internal.core.utils.dialogfields.SelectionButtonDialogFieldGroup;
import org.eclipse.wb.internal.ercp.Activator;
import org.eclipse.wb.internal.ercp.wizards.ERcpWizardPage;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import java.io.InputStream;

/**
 * {@link WizardPage} that creates new eSWT {@link Shell}.
 * 
 * @author lobas_av
 * @coverage ercp.wizards.ui
 */
public final class ShellWizardPage extends ERcpWizardPage {
  private static final String SHELL = "org.eclipse.swt.widgets.Shell";
  private static final String MOBILE_SHELL = "org.eclipse.ercp.swt.mobile.MobileShell";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ShellWizardPage() {
    setTitle("Create eSWT Shell");
    setImageDescriptor(Activator.getImageDescriptor("wizard/Shell/banner.gif"));
    setDescription("Create empty eSWT Shell.");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // WizardPage
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createTypeMembers(IType newType, ImportsManager imports, IProgressMonitor monitor)
      throws CoreException {
    String templateName;
    if (MOBILE_SHELL.equals(getSuperClass())) {
      templateName = "MobileShell.jvt";
    } else {
      templateName = "Shell.jvt";
    }
    InputStream file = Activator.getFile("templates/" + templateName);
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
    setSuperClass("org.eclipse.swt.widgets.Shell", true);
  }

  @Override
  protected void createLocalControls(Composite parent, int columns) {
    final SelectionButtonDialogFieldGroup superClassGroupField =
        new SelectionButtonDialogFieldGroup(SWT.RADIO, new String[]{
            "org.eclipse.swt.widgets.&Shell",
            "org.eclipse.ercp.swt.mobile.&MobileShell"}, 1);
    superClassGroupField.setLabelText("Select superclass:");
    superClassGroupField.setSelection(0, true);
    superClassGroupField.setDialogFieldListener(new IDialogFieldListener() {
      public void dialogFieldChanged(DialogField field) {
        if (superClassGroupField.isSelected(0)) {
          setSuperClass(SHELL, true);
        } else {
          setSuperClass(MOBILE_SHELL, true);
        }
      }
    });
    superClassGroupField.doFillIntoGrid(parent, columns);
  }
}