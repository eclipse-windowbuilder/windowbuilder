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
package org.eclipse.wb.internal.rcp.wizards.jface.application;

import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.rcp.Activator;
import org.eclipse.wb.internal.rcp.wizards.RcpWizardPage;
import org.eclipse.wb.internal.rcp.wizards.WizardsMessages;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import java.io.InputStream;

/**
 * {@link WizardPage} that creates new JFace application.
 * 
 * @author lobas_av
 * @coverage rcp.wizards.ui
 */
public final class JFaceApplicationWizardPage extends RcpWizardPage {
  private Button m_toolBarButton;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JFaceApplicationWizardPage() {
    setTitle(WizardsMessages.JFaceApplicationWizardPage_title);
    setImageDescriptor(Activator.getImageDescriptor("wizard/JFace/ApplicationWindow/banner.gif"));
    setDescription(WizardsMessages.JFaceApplicationWizardPage_description);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // WizardPage
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createTypeMembers(IType newType, ImportsManager imports, IProgressMonitor monitor)
      throws CoreException {
    InputStream file = Activator.getFile("templates/jface/" + getTemplateName());
    fillTypeFromTemplate(newType, imports, monitor, file);
  }

  private String getTemplateName() {
    final boolean selection[] = new boolean[1];
    getShell().getDisplay().syncExec(new Runnable() {
      public void run() {
        selection[0] = m_toolBarButton.getSelection();
      }
    });
    if (selection[0]) {
      return "ApplicationWindow_ToolBar.jvt";
    }
    return "ApplicationWindow_CoolBar.jvt";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void initTypePage(IJavaElement elem) {
    super.initTypePage(elem);
    setSuperClass("org.eclipse.jface.window.ApplicationWindow", true);
  }

  @Override
  protected void createLocalControls(Composite parent, int columns) {
    Composite superClassComposite = new Composite(parent, SWT.NONE);
    GridLayoutFactory.create(superClassComposite).margins(0);
    GridDataFactory.create(superClassComposite).fillH().spanH(columns);
    //
    Label label = new Label(superClassComposite, SWT.NONE);
    label.setText(WizardsMessages.JFaceApplicationWizardPage_typeSelect);
    //
    m_toolBarButton = new Button(superClassComposite, SWT.RADIO);
    m_toolBarButton.setText(WizardsMessages.JFaceApplicationWizardPage_typeWithToolBar);
    m_toolBarButton.setSelection(true);
    GridDataFactory.create(m_toolBarButton).indentH(24);
    //
    Button coolBarButton = new Button(superClassComposite, SWT.RADIO);
    coolBarButton.setText(WizardsMessages.JFaceApplicationWizardPage_typeWithCoolBar);
    GridDataFactory.create(coolBarButton).indentH(24);
    //
    createSeparator(parent, columns);
  }
}