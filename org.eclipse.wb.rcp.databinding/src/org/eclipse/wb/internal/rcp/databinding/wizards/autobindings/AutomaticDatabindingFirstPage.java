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
package org.eclipse.wb.internal.rcp.databinding.wizards.autobindings;

import org.eclipse.wb.internal.core.databinding.wizards.autobindings.IAutomaticDatabindingProvider;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.utils.dialogfields.CheckDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.Separator;
import org.eclipse.wb.internal.rcp.ToolkitProvider;
import org.eclipse.wb.internal.rcp.databinding.Messages;
import org.eclipse.wb.internal.rcp.wizards.RcpWizardPage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Standard "New Java Class" wizard page.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.wizard.auto
 */
public final class AutomaticDatabindingFirstPage
    extends
      org.eclipse.wb.internal.core.databinding.wizards.autobindings.AutomaticDatabindingFirstPage {
  private final CheckDialogField m_controlField = new CheckDialogField();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AutomaticDatabindingFirstPage(IAutomaticDatabindingProvider databindingProvider,
      String initialBeanClassName) {
    super(databindingProvider, initialBeanClassName);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createLocalControls(Composite parent, int columns) {
    super.createLocalControls(parent, columns);
    new Separator(SWT.SEPARATOR | SWT.HORIZONTAL).doFillIntoGrid(parent, columns);
    m_controlField.setLabelText(Messages.AutomaticDatabindingFirstPage_controllerFieldLabel);
    m_controlField.doFillIntoGrid(parent, columns);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Substitution support
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String performSubstitutions(String code, ImportsManager imports) {
    code = super.performSubstitutions(code, imports);
    code = RcpWizardPage.doPerformSubstitutions(this, code, imports);
    return code;
  }

  @Override
  protected ToolkitDescription getToolkitDescription() {
    return ToolkitProvider.DESCRIPTION;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean isCreateControlClass() {
    return m_controlField.getSelection();
  }
}