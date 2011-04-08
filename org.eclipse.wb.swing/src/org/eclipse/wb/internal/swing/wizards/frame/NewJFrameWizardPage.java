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
package org.eclipse.wb.internal.swing.wizards.frame;

import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.utils.dialogfields.CheckDialogField;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.swing.Activator;
import org.eclipse.wb.internal.swing.wizards.Messages;
import org.eclipse.wb.internal.swing.wizards.SwingWizardPage;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import java.io.InputStream;

import javax.swing.JFrame;

/**
 * {@link WizardPage} that creates new Swing {@link JFrame}.
 * 
 * @author lobas_av
 * @coverage swing.wizards.ui
 */
public final class NewJFrameWizardPage extends SwingWizardPage {
  private CheckDialogField m_advancedField;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public NewJFrameWizardPage() {
    setTitle(Messages.NewJFrameWizardPage_title);
    setImageDescriptor(Activator.getImageDescriptor("wizard/JFrame/banner.gif"));
    setDescription(Messages.NewJFrameWizardPage_description);
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
        Activator.getFile(m_advancedField.getSelection()
            ? "templates/JFrame_advanced.jvt"
            : "templates/JFrame.jvt");
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
    setSuperClass("javax.swing.JFrame", true);
  }

  @Override
  protected void createLocalControls(Composite parent, int columns) {
    Composite composite = new Composite(parent, SWT.NONE);
    GridLayoutFactory.create(composite).noMargins();
    GridDataFactory.create(composite).fillH().grabH().spanH(columns);
    //
    m_advancedField = new CheckDialogField();
    m_advancedField.setLabelText(Messages.NewJFrameWizardPage_useAdvancedTemplate);
    m_advancedField.setSelection(true);
    m_advancedField.doFillIntoGrid(composite, 1);
    // I always use same names during tests
    if (EnvironmentUtils.DEVELOPER_HOST) {
      setTypeName("JFrame_1", true);
    }
  }

  @Override
  protected void updateStatus(IStatus status) {
    super.updateStatus(status);
    if (isDirectlyJFrame()) {
      m_advancedField.setEnabled(true);
      m_advancedField.setSelection(true);
    } else {
      m_advancedField.setEnabled(false);
      m_advancedField.setSelection(false);
    }
    // I don't like advanced template anymore :-(
    if (EnvironmentUtils.DEVELOPER_HOST) {
      m_advancedField.setSelection(false);
    }
  }

  private boolean isDirectlyJFrame() {
    return "javax.swing.JFrame".equals(getSuperClass());
  }
}