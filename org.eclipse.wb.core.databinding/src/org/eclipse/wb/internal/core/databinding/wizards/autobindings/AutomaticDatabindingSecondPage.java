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
package org.eclipse.wb.internal.core.databinding.wizards.autobindings;

import org.eclipse.wb.internal.core.databinding.Activator;
import org.eclipse.wb.internal.core.databinding.Messages;
import org.eclipse.wb.internal.core.databinding.ui.editor.ICompleteListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassAndPropertiesConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassAndPropertiesSupportListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassAndPropertiesUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ICheckboxViewerWrapper;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.PropertyAdapter;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import java.util.List;

/**
 * Automatic bindings wizard page.
 *
 * @author lobas_av
 * @coverage bindings.wizard.auto
 */
public final class AutomaticDatabindingSecondPage extends WizardPage implements ICompleteListener {
  private final AutomaticDatabindingFirstPage m_firstPage;
  private final IAutomaticDatabindingProvider m_databindingProvider;
  private final String m_initialBeanClassName;
  private ChooseClassAndPropertiesUiContentProvider m_chooseClassAndPropertiesProvider;
  private ToolItem m_propertiesFilterButton;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AutomaticDatabindingSecondPage(AutomaticDatabindingFirstPage firstPage,
      IAutomaticDatabindingProvider databindingProvider,
      String initialBeanClassName) {
    super("");
    m_firstPage = firstPage;
    m_databindingProvider = databindingProvider;
    m_initialBeanClassName = initialBeanClassName;
    setPageComplete(false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  public void createControl(Composite parent) {
    // prepare page
    int columns = 4;
    Composite pageComposite = new Composite(parent, SWT.NONE);
    GridLayoutFactory.create(pageComposite).columns(columns);
    setControl(pageComposite);
    // choose class and properties
    m_chooseClassAndPropertiesProvider =
        new ChooseClassAndPropertiesUiContentProvider(createConfiguration()) {
          @Override
          protected void super_createContent(Composite parent, int columns) {
            super.super_createContent(parent, columns - 1);
            // filter ToolBar
            ToolBar toolBar = new ToolBar(parent, SWT.NONE);
            // properties filter
            m_propertiesFilterButton = new ToolItem(toolBar, SWT.CHECK);
            m_propertiesFilterButton.setImage(Activator.getImage("filter_action.gif"));
            m_propertiesFilterButton.setToolTipText(Messages.AutomaticDatabindingSecondPage_filterToolTip);
            m_propertiesFilterButton.setSelection(m_databindingProvider.getPropertiesViewerFilterInitState());
            m_propertiesFilterButton.setEnabled(false);
            m_propertiesFilterButton.addSelectionListener(new SelectionAdapter() {
              @Override
              public void widgetSelected(SelectionEvent e) {
                handlePropertiesFilter(m_propertiesFilterButton.getSelection());
              }
            });
          }

          @Override
          protected List<PropertyAdapter> getProperties(Class<?> choosenClass) throws Exception {
            return m_databindingProvider.getProperties(choosenClass);
          }

          public void updateFromObject() throws Exception {
            // handle initial wizard selection
            if (m_initialBeanClassName != null && m_firstPage.getJavaProject() != null) {
              m_databindingProvider.setCurrentWizardData(
                  m_firstPage,
                  AutomaticDatabindingSecondPage.this);
              setClassNameAndProperty(m_initialBeanClassName, null, true);
            }
          }

          @Override
          protected void saveToObject(Class<?> choosenClass, List<PropertyAdapter> choosenProperties)
              throws Exception {
          }

          @Override
          protected Class<?> loadClass(String className) throws ClassNotFoundException {
            return m_databindingProvider.loadClass(className);
          }

          @Override
          protected IJavaProject getJavaProject() {
            return m_firstPage.getJavaProject();
          }
        };
    m_chooseClassAndPropertiesProvider.createContent(pageComposite, columns);
    m_chooseClassAndPropertiesProvider.setCompleteListener(this);
    getPropertiesViewer().getCheckable().addCheckStateListener(new ICheckStateListener() {
      public void checkStateChanged(CheckStateChangedEvent event) {
        m_databindingProvider.handlePropertyChecked(
            (PropertyAdapter) event.getElement(),
            event.getChecked());
        calculateFinish();
      }
    });
    m_chooseClassAndPropertiesProvider.setSupportListener(new ChooseClassAndPropertiesSupportListener() {
      public void loadProperties(boolean successful) {
        m_propertiesFilterButton.setEnabled(successful);
        if (successful) {
          for (Object element : getPropertiesViewer().getCheckedElements()) {
            m_databindingProvider.handlePropertyChecked((PropertyAdapter) element, true);
          }
        }
      }
    });
    m_databindingProvider.configurePropertiesViewer((CheckboxTableViewer) getPropertiesViewer().getViewer());
    // widgets
    Composite widgetComposite = new Composite(pageComposite, SWT.NONE);
    GridDataFactory.create(widgetComposite).fill().grab().spanH(columns);
    m_databindingProvider.fillWidgetComposite(widgetComposite);
    // sets initial values
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        m_chooseClassAndPropertiesProvider.updateFromObject();
      }
    });
    // enable properties filter
    handlePropertiesFilter(m_databindingProvider.getPropertiesViewerFilterInitState());
  }

  private ChooseClassAndPropertiesConfiguration createConfiguration() {
    ChooseClassAndPropertiesConfiguration configuration =
        new ChooseClassAndPropertiesConfiguration();
    configuration.setDialogFieldLabel(Messages.AutomaticDatabindingSecondPage_beanClassLabel);
    configuration.setChooseInterfaces(true);
    configuration.setEmptyClassErrorMessage(Messages.AutomaticDatabindingSecondPage_beanClassMessage);
    configuration.setErrorMessagePrefix(Messages.AutomaticDatabindingSecondPage_beanClassErrorPrefix);
    configuration.setPropertiesLabel(Messages.AutomaticDatabindingSecondPage_propertiesLabel);
    configuration.setPropertiesMultiChecked(true);
    configuration.setReorderMode(true);
    configuration.setLoadedPropertiesCheckedStrategy(ChooseClassAndPropertiesConfiguration.LoadedPropertiesCheckedStrategy.All);
    configuration.setPropertiesErrorMessage(Messages.AutomaticDatabindingSecondPage_propertiesErrorMessage);
    m_databindingProvider.configure(configuration);
    return configuration;
  }

  private ICheckboxViewerWrapper getPropertiesViewer() {
    return m_chooseClassAndPropertiesProvider.getPropertiesViewer();
  }

  @Override
  public final void setVisible(boolean visible) {
    if (visible) {
      m_databindingProvider.setCurrentWizardData(m_firstPage, this);
      calculateFinish();
    }
    super.setVisible(visible);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handling
  //
  ////////////////////////////////////////////////////////////////////////////
  private void handlePropertiesFilter(boolean enabled) {
    if (enabled) {
      // enable filter
      getPropertiesViewer().getViewer().addFilter(m_databindingProvider.getPropertiesViewerFilter());
    } else {
      // disable filter
      getPropertiesViewer().getViewer().resetFilters();
    }
    calculateFinish();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ICompleteListener
  //
  ////////////////////////////////////////////////////////////////////////////
  public void calculateFinish() {
    // calculate state
    String errorMessage = m_chooseClassAndPropertiesProvider.getErrorMessage();
    if (errorMessage == null) {
      errorMessage = m_databindingProvider.calculateFinish();
    }
    // set state
    setErrorMessage(errorMessage);
    setPageComplete(errorMessage == null);
  }
}