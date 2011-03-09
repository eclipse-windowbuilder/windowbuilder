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
package org.eclipse.wb.internal.ercp.wizards.application;

import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.ercp.Activator;
import org.eclipse.wb.internal.ercp.wizards.ERcpWizardPage;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import java.io.InputStream;

/**
 * {@link WizardPage} that creates new eSWT application.
 * 
 * @author lobas_av
 * @coverage ercp.wizards.ui
 */
public final class SwtApplicationWizardPage extends ERcpWizardPage {
  protected Button m_createContentsButton;
  protected Button m_openButton;
  protected Button m_mainButton;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SwtApplicationWizardPage() {
    setTitle("Create eSWT application");
    setImageDescriptor(Activator.getImageDescriptor("wizard/ApplicationWindow/wizard.gif"));
    setDescription("Create a simple eSWT application with Shell and event loop.");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // WizardPage
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createTypeMembers(IType newType, ImportsManager imports, IProgressMonitor monitor)
      throws CoreException {
    InputStream file = Activator.getFile("templates/" + getTemplateName() + ".jvt");
    fillTypeFromTemplate(newType, imports, monitor, file);
  }

  private String getTemplateName() {
    final boolean selection[] = new boolean[2];
    getShell().getDisplay().syncExec(new Runnable() {
      public void run() {
        selection[0] = m_openButton.getSelection();
        selection[1] = m_mainButton.getSelection();
      }
    });
    if (selection[0]) {
      return "Application2";
    }
    if (selection[1]) {
      return "Application3";
    }
    return "Application1";
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
  protected void createLocalControls(Composite parent, int columns) {
    Composite methodsComposite = new Composite(parent, SWT.NONE);
    GridLayoutFactory.create(methodsComposite).margins(0);
    GridDataFactory.create(methodsComposite).fillH().spanH(columns);
    //
    Label label = new Label(methodsComposite, SWT.NONE);
    label.setText("Create contents in:");
    //
    m_createContentsButton = new Button(methodsComposite, SWT.RADIO);
    m_createContentsButton.setText("protected " + getCreateMethod("createContents") + "() method");
    m_createContentsButton.setSelection(true);
    GridDataFactory.create(m_createContentsButton).indentH(24);
    //
    m_openButton = new Button(methodsComposite, SWT.RADIO);
    m_openButton.setText("public open() method");
    GridDataFactory.create(m_openButton).indentH(24);
    //
    m_mainButton = new Button(methodsComposite, SWT.RADIO);
    m_mainButton.setText("public static main() method");
    GridDataFactory.create(m_mainButton).indentH(24);
    //
    createSeparator(parent, columns);
  }
}