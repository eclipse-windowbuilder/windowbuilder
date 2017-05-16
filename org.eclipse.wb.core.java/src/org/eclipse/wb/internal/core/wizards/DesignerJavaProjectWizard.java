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
package org.eclipse.wb.internal.core.wizards;

import org.eclipse.wb.internal.core.DesignerPlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageOne;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageTwo;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import java.lang.reflect.InvocationTargetException;

/**
 * Non internal version for org.eclipse.jdt.internal.ui.wizards.JavaProjectWizard.
 *
 * @author lobas_av
 * @coverage core.wizards.ui
 */
public class DesignerJavaProjectWizard extends DesignerNewElementWizard
    implements
      IExecutableExtension {
  private NewJavaProjectWizardPageOne fFirstPage;
  private NewJavaProjectWizardPageTwo fSecondPage;
  private IConfigurationElement fConfigElement;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DesignerJavaProjectWizard() {
    setDialogSettings(DesignerPlugin.getDefault().getDialogSettings());
    setWindowTitle(Messages.DesignerJavaProjectWizard_title);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Pages
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void addPages() {
    {
      fFirstPage = new NewJavaProjectWizardPageOne();
      addPage(fFirstPage);
    }
    {
      fSecondPage = new NewJavaProjectWizardPageTwo(fFirstPage);
      addPage(fSecondPage);
    }
    fFirstPage.init(getSelection(), getActivePart());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // DesignerNewElementWizard
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException {
    fSecondPage.performFinish(monitor);
  }

  @Override
  public boolean performFinish() {
    boolean res = super.performFinish();
    if (res) {
      {
        org.eclipse.ui.IWorkingSet[] workingSets = fFirstPage.getWorkingSets();
        if (workingSets.length > 0) {
          IJavaProject newElement = getCreatedElement();
          org.eclipse.ui.PlatformUI.getWorkbench().getWorkingSetManager().addToWorkingSets(
              newElement,
              workingSets);
        }
      }
      BasicNewProjectResourceWizard.updatePerspective(fConfigElement);
      BasicNewResourceWizard.selectAndReveal(
          fSecondPage.getJavaProject().getProject(),
          DesignerPlugin.getActiveWorkbenchWindow());
    }
    return res;
  }

  private org.eclipse.ui.IWorkbenchPart getActivePart() {
    org.eclipse.ui.IWorkbenchPage activePage = DesignerPlugin.getActivePage();
    if (activePage != null) {
      return activePage.getActivePart();
    }
    return null;
  }

  @Override
  protected void handleFinishException(Shell shell, InvocationTargetException e) {
    String title = Messages.DesignerJavaProjectWizard_errorTitle;
    String message = Messages.DesignerJavaProjectWizard_errorMessage;
    ExceptionHandler.perform(e, getShell(), title, message);
  }

  public void setInitializationData(IConfigurationElement cfig, String propertyName, Object data) {
    fConfigElement = cfig;
  }

  @Override
  public boolean performCancel() {
    fSecondPage.performCancel();
    return super.performCancel();
  }

  public IJavaProject getCreatedElement() {
    return fSecondPage.getJavaProject();
  }
}