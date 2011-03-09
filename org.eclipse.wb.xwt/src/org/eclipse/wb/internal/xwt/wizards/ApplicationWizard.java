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
package org.eclipse.wb.internal.xwt.wizards;

import org.eclipse.jface.wizard.Wizard;

/**
 * {@link Wizard} for new XWT application.
 * 
 * @author scheglov_ke
 * @coverage XWT.wizards
 */
public final class ApplicationWizard extends XwtWizard {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ApplicationWizard() {
    setWindowTitle("New XWT Application");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Wizard
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected XwtWizardPage createMainPage() {
    return new ApplicationWizardPage();
  }
}