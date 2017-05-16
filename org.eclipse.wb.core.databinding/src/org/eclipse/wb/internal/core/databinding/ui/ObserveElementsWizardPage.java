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

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.databinding.Messages;
import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.ui.property.Context;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Wizard page for viewers Beans or Widgets or other objects with properties.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public class ObserveElementsWizardPage extends WizardPage {
  private final Context m_context;
  private final IObserveInfo m_observeProperty;
  private ObserveElementsComposite m_composite;
  private BindWizardPage m_secondPage;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ObserveElementsWizardPage(Context context, IObserveInfo observeProperty) {
    super("first");
    m_context = context;
    m_observeProperty = observeProperty;
    setTitle(Messages.ObserveElementsWizardPage_title);
    setMessage(Messages.ObserveElementsWizardPage_message);
    setPageComplete(false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setSecondPage(BindWizardPage page) {
    m_secondPage = page;
  }

  public IBindingInfo getBinding() throws Exception {
    IStructuredSelection modelSelection = EditComposite.getSelection(m_composite, true);
    IStructuredSelection modelPropertySelection = EditComposite.getSelection(m_composite, false);
    // prepare model
    IObserveInfo model = (IObserveInfo) modelSelection.getFirstElement();
    IObserveInfo modelProperty = (IObserveInfo) modelPropertySelection.getFirstElement();
    //
    return m_context.provider.createBinding(
        m_context.observeObject,
        m_observeProperty,
        model,
        modelProperty);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // WizardPage
  //
  ////////////////////////////////////////////////////////////////////////////
  public void createControl(Composite parent) {
    Composite container = new Composite(parent, SWT.NONE);
    GridLayoutFactory.create(container).noMargins().noSpacing();
    //
    Composite titleComposite = new Composite(container, SWT.NONE);
    GridLayoutFactory.create(titleComposite).columns(2);
    GridDataFactory.create(titleComposite).fillH().grabH();
    // create title label
    Label titleLable = new Label(titleComposite, SWT.NONE);
    titleLable.setText(Messages.ObserveElementsWizardPage_targetLabel);
    // create value bold label
    Label valueLabel = new Label(titleComposite, SWT.NONE);
    GridDataFactory.create(valueLabel).fillH().grabH();
    UiUtils.setBoldFont(valueLabel);
    valueLabel.setText(ExecutionUtils.runObjectLog(new RunnableObjectEx<String>() {
      public String runObject() throws Exception {
        String text = m_context.observeObject.getPresentation().getTextForBinding();
        String propertyText = m_observeProperty.getPresentation().getTextForBinding();
        if (propertyText.length() > 0) {
          text += "." + propertyText;
        }
        return text;
      }
    }, "<exception, see log>"));
    //
    ISelectionChangedListener listener = new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        handleObserveSelection();
      }
    };
    //
    m_composite =
        new ObserveElementsComposite(container,
            SWT.NONE,
            Messages.ObserveElementsWizardPage_modelLabel,
            m_context.provider,
            false);
    GridDataFactory.create(m_composite).fill().grab();
    m_composite.showPage(m_context.provider.getModelStartType());
    m_composite.getMasterViewer().addPostSelectionChangedListener(listener);
    m_composite.getPropertiesViewer().addPostSelectionChangedListener(listener);
    //
    setControl(container);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handle
  //
  ////////////////////////////////////////////////////////////////////////////
  private void handleObserveSelection() {
    IStructuredSelection modelSelection = EditComposite.getSelection(m_composite, true);
    IStructuredSelection modelPropertySelection = EditComposite.getSelection(m_composite, false);
    //
    if (UiUtils.isEmpty(modelSelection) || UiUtils.isEmpty(modelPropertySelection)) {
      setPageComplete(false);
    } else {
      // prepare model
      IObserveInfo model = (IObserveInfo) modelSelection.getFirstElement();
      IObserveInfo modelProperty = (IObserveInfo) modelPropertySelection.getFirstElement();
      // find represented observe
      for (IBindingInfo binding : m_context.provider.getBindings()) {
        if (m_context.observeObject == binding.getTarget()
            && m_observeProperty == binding.getTargetProperty()
            && model == binding.getModel()
            && modelProperty == binding.getModelProperty()) {
          setPageComplete(false);
          return;
        }
      }
      // validate target and model
      try {
        setPageComplete(m_context.provider.validate(
            m_context.observeObject,
            m_observeProperty,
            model,
            modelProperty));
        if (isPageComplete()) {
          m_secondPage.calculateFinish();
        }
      } catch (Throwable e) {
        DesignerPlugin.log(e);
      }
    }
  }
}