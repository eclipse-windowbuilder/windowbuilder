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
package org.eclipse.wb.internal.core.databinding.ui;

import org.eclipse.wb.internal.core.databinding.Messages;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.ui.property.Context;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;

import org.eclipse.jface.wizard.Wizard;

/**
 * Wizard for binding target and model observable's.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public class BindWizard extends Wizard {
  private final Context m_context;
  private final ObserveElementsWizardPage m_firstPage;
  private final BindWizardPage m_secondPage;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BindWizard(Context context, IObserveInfo observeProperty) {
    m_context = context;
    m_firstPage = new ObserveElementsWizardPage(context, observeProperty);
    m_secondPage = new BindWizardPage(context, m_firstPage);
    setWindowTitle(Messages.BindWizard_title);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Pages
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void addPages() {
    addPage(m_firstPage);
    addPage(m_secondPage);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Wizard
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean performFinish() {
    return ExecutionUtils.runObjectLog(new RunnableObjectEx<Boolean>() {
      public Boolean runObject() throws Exception {
        m_context.provider.addBinding(m_secondPage.performFinish());
        return true;
      }
    }, true);
  }
}