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
package org.eclipse.wb.internal.ercp.wizards.viewpart;

import org.eclipse.wb.internal.core.wizards.AbstractDesignWizardPage;
import org.eclipse.wb.internal.ercp.wizards.ErcpWizard;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.part.ViewPart;

/**
 * {@link Wizard} that creates new eRCP {@link ViewPart}.
 * 
 * @author lobas_av
 * @coverage ercp.wizards.ui
 */
public final class ViewPartWizard extends ErcpWizard {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ViewPartWizard() {
    setWindowTitle("New Eclipse RCP ViewPart");
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