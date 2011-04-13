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
package org.eclipse.wb.internal.ercp.wizards.project.swt;

import org.eclipse.wb.internal.core.utils.dialogfields.AbstractValidationComposite;
import org.eclipse.wb.internal.core.utils.dialogfields.BooleanDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.IMessageContainer;
import org.eclipse.wb.internal.core.utils.dialogfields.IStringButtonAdapter;
import org.eclipse.wb.internal.core.utils.dialogfields.StringButtonDialogField;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.ercp.wizards.WizardsMessages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

import org.apache.commons.lang.StringUtils;

/**
 * {@link WizardPage} for creating new eRCP project.
 * 
 * @author scheglov_ke
 * @coverage ercp.wizards.ui
 */
public final class NewProjectCreationPage extends WizardNewProjectCreationPage {
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
  private PageComposite m_pageComposite;

  @Override
  public void createControl(Composite parent) {
    super.createControl(parent);
    Composite container = (Composite) getControl();
    GridLayoutFactory.create(container);
    {
      m_pageComposite = new PageComposite(container, SWT.NONE, new IMessageContainer() {
        public void setErrorMessage(String message) {
          boolean valid = validatePage();
          setPageComplete(valid);
        }
      });
      GridDataFactory.create(m_pageComposite).grab().fill();
      m_pageComposite.validateAll();
    }
    // apply defaults
    Dialog.applyDialogFont(container);
    setControl(container);
  }

  @Override
  protected boolean validatePage() {
    // validate using JDT
    if (!super.validatePage()) {
      return false;
    }
    // validate fields
    String message = m_pageComposite.validate();
    setErrorMessage(message);
    return message == null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the selected eRCP location.
   */
  public String getLocation() {
    return m_pageComposite.m_locationField.getText();
  }

  /**
   * @return <code>true</code> if sample content should be generated.
   */
  public boolean shouldGenerateSampleProject() {
    return m_pageComposite.m_sampleField.getSelection();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PageComposite
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Container for GUI with {@link DialogField}'s.
   */
  private class PageComposite extends AbstractValidationComposite {
    private final StringButtonDialogField m_locationField;
    private final BooleanDialogField m_sampleField;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public PageComposite(Composite parent, int style, IMessageContainer messageContainer) {
      super(parent, style, messageContainer);
      GridLayoutFactory.create(this).columns(3);
      // location
      {
        m_locationField = new StringButtonDialogField(new IStringButtonAdapter() {
          public void changeControlPressed(DialogField field) {
            // prepare dialog
            DirectoryDialog directoryDialog;
            {
              directoryDialog = new DirectoryDialog(getShell());
              directoryDialog.setFilterPath(m_locationField.getText());
              directoryDialog.setText(WizardsMessages.NewProjectCreationPage_locationText);
              directoryDialog.setMessage(WizardsMessages.NewProjectCreationPage_locationMessage);
            }
            // select new location
            String newLocation = directoryDialog.open();
            if (newLocation != null) {
              m_locationField.setText(newLocation);
            }
          }
        });
        m_locationField.setButtonLabel(WizardsMessages.NewProjectCreationPage_browseButton);
        doCreateField(m_locationField, WizardsMessages.NewProjectCreationPage_browseLabel, 40);
      }
      // sample content
      {
        m_sampleField = new BooleanDialogField();
        doCreateField(m_sampleField, WizardsMessages.NewProjectCreationPage_createSample, 40);
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Validation
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected String validate() {
      // validate project name
      {
        String projectName = getProjectName();
        if (StringUtils.isEmpty(projectName)) {
          return WizardsMessages.NewProjectCreationPage_validateNoProjectName;
        }
      }
      // validate location
      try {
        String location = m_locationField.getText();
        NewProjectCreationOperation.getAbsoluteJars(location);
      } catch (Throwable e) {
        return e.getMessage();
      }
      // OK, no problems
      return null;
    }
  }
}
