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
package org.eclipse.wb.internal.ercp.wizards.project.rcp;

import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.ercp.wizards.WizardsMessages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

/**
 * {@link WizardPage} for creating new eRCP project.
 * 
 * @author scheglov_ke
 * @coverage ercp.wizards.ui
 */
public final class NewProjectCreationPage extends WizardNewProjectCreationPage {
  private Button m_sampleProjectButton;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public NewProjectCreationPage(String pageName) {
    super(pageName);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // WizardPage
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void createControl(Composite parent) {
    super.createControl(parent);
    Composite container = (Composite) getControl();
    GridLayoutFactory.create(container);
    {
      Composite composite = new Composite(container, SWT.NONE);
      GridDataFactory.create(composite).grabH().fill();
      GridLayoutFactory.create(composite);
      {
        m_sampleProjectButton = new Button(composite, SWT.CHECK);
        m_sampleProjectButton.setText(WizardsMessages.NewProjectCreationPage_sampleButton);
      }
    }
    Dialog.applyDialogFont(container);
    setControl(container);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if sample project should be generated.
   */
  public boolean shouldGenerateSampleProject() {
    return m_sampleProjectButton.getSelection();
  }
}
