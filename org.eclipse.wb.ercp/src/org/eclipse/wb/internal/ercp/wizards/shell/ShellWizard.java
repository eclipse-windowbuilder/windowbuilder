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
package org.eclipse.wb.internal.ercp.wizards.shell;

import org.eclipse.wb.internal.core.wizards.AbstractDesignWizardPage;
import org.eclipse.wb.internal.ercp.wizards.ErcpWizard;
import org.eclipse.wb.internal.ercp.wizards.WizardsMessages;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;

/**
 * {@link Wizard} that creates new eSWT {@link Shell}.
 * 
 * @author lobas_av
 * @coverage ercp.wizards.ui
 */
public final class ShellWizard extends ErcpWizard {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ShellWizard() {
    setWindowTitle(WizardsMessages.ShellWizard_title);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Wizard
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected AbstractDesignWizardPage createMainPage() {
    return new ShellWizardPage();
  }
}