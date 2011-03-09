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
package org.eclipse.wb.internal.rcp.wizards.rcp.view;

import org.eclipse.wb.internal.rcp.Activator;
import org.eclipse.wb.internal.rcp.wizards.rcp.AbstractViewPartWizardPage;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ui.part.ViewPart;

/**
 * {@link WizardPage} that creates new RCP {@link ViewPart}.
 * 
 * @author lobas_av
 * @coverage rcp.wizards.ui
 */
public final class ViewPartWizardPage extends AbstractViewPartWizardPage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ViewPartWizardPage() {
    setTitle("Create ViewPart");
    setImageDescriptor(Activator.getImageDescriptor("wizard/ViewPart/banner.gif"));
    setDescription("Create empty Eclipse RCP ViewPart.");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // WizardPage
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getCreateTemplate() {
    return "templates/rcp/ViewPart.jvt";
  }
}