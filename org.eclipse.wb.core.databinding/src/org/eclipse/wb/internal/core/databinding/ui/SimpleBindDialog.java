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

import org.eclipse.wb.internal.core.databinding.ui.editor.IPageListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.UiContentProviderComposite;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ResizableTitleAreaDialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import java.util.List;

/**
 * Dialog for binding target and model observable's.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public final class SimpleBindDialog extends ResizableTitleAreaDialog implements IPageListener {
  private final List<IUiContentProvider> m_providers;
  private final String m_dialogTitle;
  private UiContentProviderComposite m_providerComposite;
  private final String m_title;
  private final String m_message;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SimpleBindDialog(Shell parentShell,
      AbstractUIPlugin plugin,
      List<IUiContentProvider> providers,
      String dialogTitle,
      String title,
      String message) {
    super(parentShell, plugin);
    m_providers = providers;
    m_dialogTitle = dialogTitle;
    m_title = title;
    m_message = message;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Control createDialogArea(Composite parent) {
    // dialog area
    Composite container = (Composite) super.createDialogArea(parent);
    //
    m_providerComposite = new UiContentProviderComposite(this, m_providers, container, SWT.NONE);
    GridDataFactory.create(m_providerComposite).fill().grab();
    //
    if (m_title != null) {
      setTitle(m_title);
    }
    if (m_message != null) {
      setMessage(m_message);
    }
    //
    return container;
  }

  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    super.createButtonsForButtonBar(parent);
    // initial state
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        m_providerComposite.performInitialize();
      }
    });
  }

  @Override
  protected void configureShell(Shell newShell) {
    // set title
    newShell.setText(m_dialogTitle);
    super.configureShell(newShell);
  }

  @Override
  protected void okPressed() {
    // handle finish
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        m_providerComposite.performFinish();
      }
    });
    super.okPressed();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IPageListener
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setPageComplete(boolean complete) {
    getButton(IDialogConstants.OK_ID).setEnabled(complete);
  }
}