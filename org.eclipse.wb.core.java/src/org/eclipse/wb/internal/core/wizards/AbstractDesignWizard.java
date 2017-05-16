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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;

/**
 * Base class for wizard responsible to create Java elements.
 *
 * @author lobas_av
 * @coverage core.wizards.ui
 */
public abstract class AbstractDesignWizard extends DesignerNewElementWizard {
  // TODO(scheglov) move to shared place
  private static final String EDITOR_ID = "org.eclipse.wb.core.guiEditor";
  protected AbstractDesignWizardPage m_mainPage;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Pages
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void addPages() {
    if (!validateSelection()) {
      return;
    }
    m_mainPage = createMainPage();
    addPage(m_mainPage);
    m_mainPage.setInitialSelection(getSelection());
  }

  /**
   * Create main wizard page for this wizard.
   */
  protected abstract AbstractDesignWizardPage createMainPage();

  /**
   * @return <code>true</code> if current selection is valid, or adds error {@link WizardPage} and
   *         return <code>false</code>.
   */
  protected boolean validateSelection() {
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Finish
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean performFinish() {
    boolean canFinish = super.performFinish();
    if (canFinish) {
      openEditor();
    }
    return canFinish;
  }

  @Override
  protected void finishPage(IProgressMonitor monitor) throws Exception {
    m_mainPage.createType(monitor);
  }

  public final IJavaElement getCreatedElement() {
    return m_mainPage.getCreatedType();
  }

  /**
   * Opens creates UI in editor.
   */
  protected void openEditor() {
    IFile file = (IFile) m_mainPage.getModifiedResource();
    openEditor(file);
  }

  /**
   * Opens created {@link IFile} in editor.
   */
  protected void openEditor(IFile file) {
    openResource(file, EDITOR_ID);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link IJavaProject} of selection, may be <code>null</code>.
   */
  protected final IJavaProject getJavaProject() {
    IStructuredSelection selection = getSelection();
    return WizardUtils.getJavaProject(selection);
  }
}