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
package org.eclipse.wb.internal.swing.wizards.dialog;

import org.eclipse.wb.internal.core.utils.dialogfields.CheckDialogField;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.swing.Activator;
import org.eclipse.wb.internal.swing.wizards.Messages;
import org.eclipse.wb.internal.swing.wizards.SwingWizardPage;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import java.io.InputStream;

import javax.swing.JDialog;

/**
 * {@link WizardPage} that creates new Swing {@link JDialog}.
 * 
 * @author lobas_av
 * @coverage swing.wizards.ui
 */
public final class NewJDialogWizardPage extends SwingWizardPage {
  private CheckDialogField m_buttonsField;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public NewJDialogWizardPage() {
    setTitle(Messages.NewJDialogWizardPage_title);
    setImageDescriptor(Activator.getImageDescriptor("wizard/JDialog/banner.gif"));
    setDescription(Messages.NewJDialogWizardPage_description);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // WizardPage
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createTypeMembers(IType newType, ImportsManager imports, IProgressMonitor monitor)
      throws CoreException {
    InputStream file =
        Activator.getFile(m_buttonsField.getSelection()
            ? "templates/JDialog_buttons.jvt"
            : "templates/JDialog.jvt");
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
    setSuperClass("javax.swing.JDialog", true);
  }

  @Override
  protected void createLocalControls(Composite parent, int columns) {
    Composite composite = new Composite(parent, SWT.NONE);
    GridLayoutFactory.create(composite).noMargins();
    GridDataFactory.create(composite).fillH().grabH().spanH(columns);
    //
    m_buttonsField = new CheckDialogField();
    m_buttonsField.setLabelText(Messages.NewJDialogWizardPage_generateButtons);
    m_buttonsField.setSelection(true);
    m_buttonsField.doFillIntoGrid(composite, 1);
  }
}