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

import org.eclipse.wb.internal.core.wizards.AbstractDesignWizardPage;
import org.eclipse.wb.internal.rcp.wizards.RcpWizard;
import org.eclipse.wb.internal.rcp.wizards.WizardsMessages;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;

/**
 * {@link Wizard} that creates new SWT {@link Composite}.
 *
 * @author lobas_av
 * @coverage rcp.wizards.ui
 */
public final class CompositeWizard extends RcpWizard {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CompositeWizard() {
    setWindowTitle(WizardsMessages.CompositeWizard_title);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Pages
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected AbstractDesignWizardPage createMainPage() {
    return new CompositeWizardPage();
  }
}