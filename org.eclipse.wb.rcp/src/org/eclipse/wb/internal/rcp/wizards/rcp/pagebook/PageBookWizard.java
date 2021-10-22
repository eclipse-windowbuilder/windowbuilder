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
package org.eclipse.wb.internal.rcp.wizards.rcp.pagebook;

import org.eclipse.wb.internal.core.wizards.AbstractDesignWizardPage;
import org.eclipse.wb.internal.rcp.wizards.RcpWizard;
import org.eclipse.wb.internal.rcp.wizards.WizardsMessages;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.part.PageBook;

/**
 * {@link Wizard} that creates new RCP {@link PageBook}.
 *
 * @author lobas_av
 * @coverage rcp.wizards.ui
 */
public final class PageBookWizard extends RcpWizard {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PageBookWizard() {
    setWindowTitle(WizardsMessages.PageBookWizard_title);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Wizard
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected AbstractDesignWizardPage createMainPage() {
    return new PageBookWizardPage();
  }
}