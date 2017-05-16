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
import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.IPageListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.UiContentProviderComposite;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ResizableTitleAreaDialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import java.util.Collections;
import java.util.List;

/**
 * Dialog for binding target and model observable's.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public final class BindDialog extends ResizableTitleAreaDialog implements IPageListener {
  private final IDatabindingsProvider m_databindingsProvider;
  private final IBindingInfo m_binding;
  private final boolean m_canCreate;
  private final boolean m_autoFinish;
  private UiContentProviderComposite m_providerComposite;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BindDialog(Shell parentShell,
      AbstractUIPlugin plugin,
      IDatabindingsProvider databindingsProvider,
      IBindingInfo binding,
      boolean canCreate,
      boolean autoFinish) {
    super(parentShell, plugin);
    m_databindingsProvider = databindingsProvider;
    m_binding = binding;
    m_canCreate = canCreate;
    m_autoFinish = autoFinish;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Control createDialogArea(Composite parent) {
    // dialog area
    final ScrolledComposite container =
        new ScrolledComposite((Composite) super.createDialogArea(parent), SWT.BORDER | SWT.V_SCROLL);
    container.setExpandHorizontal(true);
    //
    List<IUiContentProvider> providers =
        ExecutionUtils.runObjectLog(new RunnableObjectEx<List<IUiContentProvider>>() {
          public List<IUiContentProvider> runObject() throws Exception {
            return m_databindingsProvider.getContentProviders(m_binding, BindDialog.this);
          }
        }, Collections.<IUiContentProvider>emptyList());
    m_providerComposite = new UiContentProviderComposite(this, providers, container, SWT.NONE);
    container.setContent(m_providerComposite);
    container.addControlListener(new ControlAdapter() {
      @Override
      public void controlResized(ControlEvent e) {
        Rectangle bounds = container.getClientArea();
        Point size = m_providerComposite.computeSize(bounds.width, SWT.DEFAULT);
        m_providerComposite.setBounds(bounds.x, bounds.y, size.x, size.y);
      }
    });
    GridDataFactory.create(container).fill().grab();
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
    if (m_canCreate) {
      newShell.setText(Messages.BindDialog_titleCreate);
    } else {
      newShell.setText(Messages.BindDialog_titleEdit);
    }
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

  @Override
  public int open() {
    // prepare Shell
    if (getShell() == null) {
      create();
    }
    //
    constrainShellSize();
    // handle auto finish
    if (m_autoFinish && getButton(IDialogConstants.OK_ID).getEnabled()) {
      okPressed();
    }
    Shell shell = getShell();
    //
    if (shell != null && !shell.isDisposed()) {
      // open the window
      shell.open();
      Display display = shell.getDisplay();
      while (!shell.isDisposed()) {
        if (!display.readAndDispatch()) {
          display.sleep();
        }
      }
      if (!display.isDisposed()) {
        display.update();
      }
    }
    // result code
    return getReturnCode();
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