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

import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;
import org.eclipse.wb.internal.core.databinding.ui.editor.IPageListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.UiContentProviderComposite;
import org.eclipse.wb.internal.core.databinding.ui.property.Context;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;

import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import java.util.Collections;
import java.util.List;

/**
 * Wizard page for binding properties.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public class BindWizardPage extends WizardPage implements IPageListener {
  private final Context m_context;
  private final ObserveElementsWizardPage m_firstPage;
  private Composite m_composite;
  private UiContentProviderComposite m_providerComposite;
  private IBindingInfo m_binding;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BindWizardPage(Context context, ObserveElementsWizardPage firstPage) {
    super("second");
    m_context = context;
    m_firstPage = firstPage;
    m_firstPage.setSecondPage(this);
    setPageComplete(false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IPageListener
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setTitleImage(Image image) {
  }

  @Override
  public void setPageComplete(boolean complete) {
    super.setPageComplete(complete);
    IWizardContainer container = getContainer();
    if (container != null) {
      container.updateButtons();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public IBindingInfo performFinish() throws Exception {
    m_providerComposite.performFinish();
    return m_binding;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // WizardPage
  //
  ////////////////////////////////////////////////////////////////////////////
  public void createControl(Composite parent) {
    m_composite = new Composite(parent, SWT.NONE);
    m_composite.setLayout(new FillLayout());
    setControl(m_composite);
  }

  public void calculateFinish() {
    if (m_providerComposite != null) {
      m_providerComposite.dispose();
      m_providerComposite = null;
    }
    List<IUiContentProvider> providers =
        ExecutionUtils.runObjectLog(new RunnableObjectEx<List<IUiContentProvider>>() {
          public List<IUiContentProvider> runObject() throws Exception {
            m_binding = m_firstPage.getBinding();
            return m_context.provider.getContentProviders(m_binding, BindWizardPage.this);
          }
        }, Collections.<IUiContentProvider>emptyList());
    m_providerComposite = new UiContentProviderComposite(this, providers, m_composite, SWT.NONE);
    // initial state
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        m_providerComposite.performInitialize();
      }
    });
  }

  @Override
  public void setVisible(boolean visible) {
    if (visible) {
      m_composite.layout();
    }
    super.setVisible(visible);
  }
}