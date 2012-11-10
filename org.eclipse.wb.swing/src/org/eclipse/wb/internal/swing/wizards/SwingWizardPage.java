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
package org.eclipse.wb.internal.swing.wizards;

import org.eclipse.wb.internal.core.model.description.ToolkitDescriptionJava;
import org.eclipse.wb.internal.core.wizards.TemplateDesignWizardPage;
import org.eclipse.wb.internal.swing.ToolkitProvider;

import org.eclipse.swt.widgets.Composite;

import org.apache.commons.lang.StringUtils;

/**
 * General wizard page for Swing wizard's.
 * 
 * @author lobas_av
 * @coverage swing.wizards.ui
 */
public class SwingWizardPage extends TemplateDesignWizardPage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Substitution support
  //
  ////////////////////////////////////////////////////////////////////////////
  public static String doPerformSubstitutions(TemplateDesignWizardPage page,
      String code,
      ImportsManager imports) {
    code = StringUtils.replace(code, "%SwingLayout%", page.getLayoutCode("", imports));
    code =
        StringUtils.replace(
            code,
            "%ContentPane.SwingLayout%",
            page.getLayoutCode("getContentPane().", imports));
    return code;
  }

  @Override
  protected String performSubstitutions(String code, ImportsManager imports) {
    code = super.performSubstitutions(code, imports);
    code = doPerformSubstitutions(this, code, imports);
    return code;
  }

  @Override
  protected ToolkitDescriptionJava getToolkitDescription() {
    return ToolkitProvider.DESCRIPTION;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createDesignSuperClassControls(Composite composite, int nColumns) {
    createSuperClassControls(composite, nColumns);
  }
}