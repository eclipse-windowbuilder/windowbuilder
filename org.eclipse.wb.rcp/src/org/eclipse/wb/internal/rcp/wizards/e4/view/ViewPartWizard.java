/*******************************************************************************
 * Copyright (c) 2012 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.wizards.e4.view;

import org.eclipse.wb.internal.core.wizards.AbstractDesignWizardPage;
import org.eclipse.wb.internal.rcp.wizards.RcpWizard;
import org.eclipse.wb.internal.rcp.wizards.WizardsMessages;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.part.ViewPart;

/**
 * {@link Wizard} that creates new E4 {@link ViewPart}.
 *
 * @author scheglov_ke
 * @coverage rcp.wizards.ui
 */
public final class ViewPartWizard extends RcpWizard {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ViewPartWizard() {
    setWindowTitle(WizardsMessages.E4ViewPartWizard_title);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Wizard
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected AbstractDesignWizardPage createMainPage() {
    return new ViewPartWizardPage();
  }
}