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
package org.eclipse.wb.internal.ercp.wizards;

import org.eclipse.wb.internal.core.model.description.ToolkitDescriptionJava;
import org.eclipse.wb.internal.core.wizards.TemplateDesignWizardPage;
import org.eclipse.wb.internal.ercp.ToolkitProvider;

import org.eclipse.swt.widgets.Composite;

import org.apache.commons.lang.StringUtils;

/**
 * General wizard page for eRCP wizard's.
 * 
 * @author lobas_av
 * @coverage ercp.wizards.ui
 */
public class ERcpWizardPage extends TemplateDesignWizardPage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Substitution support
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String performSubstitutions(String code, ImportsManager imports) {
    code = super.performSubstitutions(code, imports);
    code = StringUtils.replace(code, "%CreateMethod%", getCreateMethod("createContents"));
    code = StringUtils.replace(code, "%SWTLayout%", getLayoutCode("", imports));
    code = StringUtils.replace(code, "%shell.SWTLayout%", getLayoutCode("shell.", imports));
    code = StringUtils.replace(code, "%container.SWTLayout%", getLayoutCode("container.", imports));
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