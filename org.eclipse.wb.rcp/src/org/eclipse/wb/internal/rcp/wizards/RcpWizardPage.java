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
package org.eclipse.wb.internal.rcp.wizards;

import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.wizards.TemplateDesignWizardPage;
import org.eclipse.wb.internal.rcp.ToolkitProvider;

import org.eclipse.swt.widgets.Composite;

import org.apache.commons.lang.StringUtils;

/**
 * General wizard page for RCP wizard's.
 * 
 * @author lobas_av
 * @coverage rcp.wizards.ui
 */
public class RcpWizardPage extends TemplateDesignWizardPage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Substitution support
  //
  ////////////////////////////////////////////////////////////////////////////
  public static String doPerformSubstitutions(TemplateDesignWizardPage page,
      String code,
      ImportsManager imports) {
    code = StringUtils.replace(code, "%CreateMethod%", page.getCreateMethod("createContents"));
    code = StringUtils.replace(code, "%SWTLayout%", page.getLayoutCode("", imports));
    code = StringUtils.replace(code, "%shell.SWTLayout%", page.getLayoutCode("shell.", imports));
    code =
        StringUtils.replace(
            code,
            "%container.SWTLayout%",
            page.getLayoutCode("container.", imports));
    code =
        StringUtils.replace(
            code,
            "%field-prefix-shell.SWTLayout%",
            page.getLayoutCode("%field-prefix%shell.", imports));
    code =
        StringUtils.replace(
            code,
            "%field-prefix-container.SWTLayout%",
            page.getLayoutCode("%field-prefix%container.", imports));
    code = performFieldPrefixesSubstitutions(code);
    return code;
  }

  @Override
  protected String performSubstitutions(String code, ImportsManager imports) {
    code = super.performSubstitutions(code, imports);
    code = doPerformSubstitutions(this, code, imports);
    return code;
  }

  @Override
  protected ToolkitDescription getToolkitDescription() {
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