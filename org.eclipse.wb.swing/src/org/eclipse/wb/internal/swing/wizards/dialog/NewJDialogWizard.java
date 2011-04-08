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

import org.eclipse.wb.internal.core.wizards.AbstractDesignWizardPage;
import org.eclipse.wb.internal.swing.wizards.Messages;
import org.eclipse.wb.internal.swing.wizards.SwingWizard;

import org.eclipse.jface.wizard.Wizard;

import javax.swing.JDialog;

/**
 * {@link Wizard} that creates new Swing {@link JDialog}.
 * 
 * @author lobas_av
 * @coverage swing.wizards.ui
 */
public final class NewJDialogWizard extends SwingWizard {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public NewJDialogWizard() {
    setWindowTitle(Messages.NewJDialogWizard_title);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Pages
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected AbstractDesignWizardPage createMainPage() {
    return new NewJDialogWizardPage();
  }
}