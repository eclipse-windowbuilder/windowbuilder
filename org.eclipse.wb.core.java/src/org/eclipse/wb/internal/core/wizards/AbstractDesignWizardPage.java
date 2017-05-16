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
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Base class for wizard page responsible to create Java elements.
 *
 * @author lobas_av
 * @coverage core.wizards.ui
 */
public abstract class AbstractDesignWizardPage extends NewTypeWizardPage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractDesignWizardPage() {
    super(true, "");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * The wizard owning this page is responsible for calling this method with the current selection.
   * The selection is used to initialize the fields of the wizard page.
   */
  public void setInitialSelection(IStructuredSelection selection) {
    IJavaElement element = getInitialJavaElement(selection);
    initContainerPage(element);
    initTypePage(element);
    doStatusUpdate();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  protected void doStatusUpdate() {
    DesignerPlugin.getStandardDisplay().asyncExec(new Runnable() {
      public void run() {
        doPageStatusUpdate();
      }
    });
  }

  protected void doPageStatusUpdate() {
    updateStatus(new IStatus[]{fContainerStatus, fPackageStatus, fTypeNameStatus, fSuperClassStatus});
  }

  @Override
  protected void handleFieldChanged(String fieldName) {
    super.handleFieldChanged(fieldName);
    doStatusUpdate();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  public void createControl(Composite parent) {
    initializeDialogUnits(parent);
    // create page control
    int nColumns = 4;
    Composite composite = new Composite(parent, SWT.NONE);
    GridLayoutFactory.create(composite).columns(nColumns);
    // create page fields
    createContainerControls(composite, nColumns);
    createPackageControls(composite, nColumns);
    createTypeNameControls(composite, nColumns);
    createDesignSuperClassControls(composite, nColumns);
    createSeparator(composite, nColumns);
    createLocalControls(composite, nColumns);
    // set page control
    setControl(composite);
  }

  /**
   * Creates the controls for the superclass name field.
   */
  protected void createDesignSuperClassControls(Composite composite, int nColumns) {
  }

  /**
   * Create the another controls.
   */
  protected void createLocalControls(Composite parent, int nColumns) {
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
    if (visible) {
      setFocus();
    }
  }
}