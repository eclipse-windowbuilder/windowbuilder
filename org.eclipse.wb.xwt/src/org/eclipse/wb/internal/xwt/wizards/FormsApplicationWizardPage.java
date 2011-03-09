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

import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.xwt.Activator;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

/**
 * {@link WizardPage} for {@link ApplicationWizard}, with Forms API support.
 * 
 * @author scheglov_ke
 * @coverage XWT.wizards
 */
public final class FormsApplicationWizardPage extends XwtWizardPage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FormsApplicationWizardPage() {
    setTitle("Create XWT Application");
    setImageDescriptor(Activator.getImageDescriptor("wizard/Application/banner.png"));
    setDescription("Create a simple XWT application with Shell.");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Create
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getTemplatePath_Java() {
    return "templates/FormsApplication.jvt";
  }

  @Override
  protected String getTemplatePath_XWT() {
    return "templates/FormsApplication.xwt";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createLocalControls(Composite parent, int columns) {
    // I always use same names during tests
    if (EnvironmentUtils.DEVELOPER_HOST) {
      setTypeName("Application_1", true);
    }
  }
}