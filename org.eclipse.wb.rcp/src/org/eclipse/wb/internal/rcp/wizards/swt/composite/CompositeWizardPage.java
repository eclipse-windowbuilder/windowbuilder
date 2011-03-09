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
package org.eclipse.wb.internal.rcp.wizards.swt.composite;

import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.rcp.Activator;
import org.eclipse.wb.internal.rcp.wizards.RcpWizardPage;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import java.io.InputStream;

/**
 * {@link WizardPage} that creates new SWT {@link Composite}.
 * 
 * @author lobas_av
 * @coverage rcp.wizards.ui
 */
public final class CompositeWizardPage extends RcpWizardPage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CompositeWizardPage() {
    setTitle("Create SWT Composite");
    setImageDescriptor(Activator.getImageDescriptor("wizard/Composite/banner.gif"));
    setDescription("Create empty SWT Composite. Composites can be reused later in complex forms.");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // WizardPage
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createTypeMembers(IType newType, ImportsManager imports, IProgressMonitor monitor)
      throws CoreException {
    InputStream file = Activator.getFile("templates/swt/Composite.jvt");
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
    setSuperClass("org.eclipse.swt.widgets.Composite", true);
  }

  @Override
  protected void createLocalControls(Composite parent, int columns) {
    Composite superClassComposite = new Composite(parent, SWT.NONE);
    GridLayoutFactory.create(superClassComposite).margins(0);
    GridDataFactory.create(superClassComposite).fillH().spanH(columns);
    //
    Label label = new Label(superClassComposite, SWT.NONE);
    label.setText("Select superclass:");
    //
    final Button compositeButton = new Button(superClassComposite, SWT.RADIO);
    compositeButton.setText("org.eclipse.swt.widgets.&Composite");
    compositeButton.setSelection(true);
    GridDataFactory.create(compositeButton).indentH(24);
    compositeButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        if (compositeButton.getSelection()) {
          setSuperClass("org.eclipse.swt.widgets.Composite", true);
        }
      }
    });
    //
    final Button groupButton = new Button(superClassComposite, SWT.RADIO);
    groupButton.setText("org.eclipse.swt.widgets.&Group");
    GridDataFactory.create(groupButton).indentH(24);
    groupButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        if (groupButton.getSelection()) {
          setSuperClass("org.eclipse.swt.widgets.Group", true);
        }
      }
    });
    //
    createSeparator(parent, columns);
  }
}