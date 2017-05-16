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
import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Bindings main editor container.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public class EditComposite extends Composite {
  private static final String MAIN_SASH_KEY = "Main_Sash";
  private static final String OBSERVES_SASH_KEY = "Observes_Sash";
  private final AbstractUIPlugin m_plugin;
  private final IDatabindingsProvider m_databindingsProvider;
  private final IDialogSettings m_settings;
  private final SashForm m_mainSashForm;
  private final SashForm m_observeSashForm;
  private final BindingElementsComposite m_bindingComposite;
  private final ObserveElementsComposite m_targetObserveComposite;
  private final ObserveElementsComposite m_modelObserveComposite;
  private final EditSelection m_selection = new EditSelection();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EditComposite(Composite parent,
      int style,
      AbstractUIPlugin plugin,
      IDatabindingsProvider databindingsProvider,
      IDialogSettings settings) {
    super(parent, style);
    m_plugin = plugin;
    m_databindingsProvider = databindingsProvider;
    m_settings = settings;
    //
    setLayout(new FillLayout());
    //
    m_mainSashForm = new SashForm(this, SWT.VERTICAL);
    // bindings
    m_bindingComposite =
        new BindingElementsComposite(m_mainSashForm, SWT.NONE, m_databindingsProvider, m_settings);
    //
    m_observeSashForm = new SashForm(m_mainSashForm, SWT.HORIZONTAL);
    // target observes
    m_targetObserveComposite =
        new ObserveElementsComposite(m_observeSashForm,
            SWT.NONE,
            "Target",
            m_databindingsProvider,
            true);
    // model observes
    m_modelObserveComposite =
        new ObserveElementsComposite(m_observeSashForm,
            SWT.NONE,
            "Model",
            m_databindingsProvider,
            true);
    // configure target-model-bindings links
    new SelectionSynchronizer();
    m_bindingComposite.setEditBindingListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        editBinding();
      }
    });
    m_targetObserveComposite.getBindAction().addSelectionListener(m_createBindingListener);
    m_modelObserveComposite.getBindAction().addSelectionListener(m_createBindingListener);
    new ExpandedListener(m_targetObserveComposite) {
      @Override
      protected void handle() {
        m_selection.setTarget(m_databindingsProvider, m_targetObserveComposite);
      }
    };
    new ExpandedListener(m_modelObserveComposite) {
      @Override
      protected void handle() {
        m_selection.setModel(m_databindingsProvider, m_modelObserveComposite);
      }
    };
    // configure sashe's
    UiUtils.loadSashWeights(m_mainSashForm, m_settings, MAIN_SASH_KEY, new int[]{1, 3});
    UiUtils.loadSashWeights(m_observeSashForm, m_settings, OBSERVES_SASH_KEY, new int[]{1, 1});
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets new content to this container if <code>refresh</code> is <code>false</code> or update
   * existing content.
   */
  public void setInput(boolean refresh, EditSelection selection) {
    m_bindingComposite.setInput(refresh);
    if (refresh) {
      m_targetObserveComposite.refresh();
      m_modelObserveComposite.refresh();
    } else if (selection == null
        || !selection.setSelection(
            m_databindingsProvider,
            m_bindingComposite,
            m_targetObserveComposite,
            m_modelObserveComposite)) {
      m_targetObserveComposite.showPage(m_databindingsProvider.getTargetStartType());
      m_modelObserveComposite.showPage(m_databindingsProvider.getModelStartType());
    }
  }

  public EditSelection getEditSelection() {
    return m_selection;
  }

  public void performFinish() {
    // save sash weights
    UiUtils.saveSashWeights(m_mainSashForm, m_settings, MAIN_SASH_KEY);
    UiUtils.saveSashWeights(m_observeSashForm, m_settings, OBSERVES_SASH_KEY);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  private final SelectionListener m_createBindingListener = new SelectionAdapter() {
    @Override
    public void widgetSelected(SelectionEvent e) {
      createBinding();
    }
  };

  protected final void createBinding() {
    try {
      // check auto binding
      boolean isCtrlPressed = DesignerPlugin.isCtrlPressed();
      if (isCtrlPressed) {
        getShell().getDisplay().beep();
      }
      // prepare target
      IObserveInfo target = m_targetObserveComposite.getMasterObserve();
      IObserveInfo targetProperty = m_targetObserveComposite.getPropertyObserve();
      // prepare model
      IObserveInfo model = m_modelObserveComposite.getMasterObserve();
      IObserveInfo modelProperty = m_modelObserveComposite.getPropertyObserve();
      // create binding
      IBindingInfo binding =
          m_databindingsProvider.createBinding(target, targetProperty, model, modelProperty);
      // show edit dialog
      BindDialog dialog =
          new BindDialog(getShell(), m_plugin, m_databindingsProvider, binding, true, isCtrlPressed);
      // handle done
      if (dialog.open() == Window.OK) {
        m_databindingsProvider.addBinding(binding);
        if (!isDisposed()) {
          m_targetObserveComposite.refresh();
          m_modelObserveComposite.refresh();
          m_bindingComposite.getViewer().refresh();
          m_bindingComposite.getViewer().setSelection(new StructuredSelection(binding), true);
          m_bindingComposite.setDeleteAllActionEnabled(true);
        }
      }
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }

  protected final void editBinding() {
    // prepare binding
    IBindingInfo binding = m_bindingComposite.getSelectionBinding();
    // show edit dialog
    BindDialog dialog =
        new BindDialog(getShell(), m_plugin, m_databindingsProvider, binding, false, false);
    // handle done
    if (dialog.open() == Window.OK) {
      m_databindingsProvider.editBinding(binding);
      if (!isDisposed()) {
        m_targetObserveComposite.refresh();
        m_modelObserveComposite.refresh();
        m_bindingComposite.getViewer().refresh();
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection
  //
  ////////////////////////////////////////////////////////////////////////////
  private class SelectionSynchronizer {
    private final IBindingSelectionListener m_bindingListener = new IBindingSelectionListener() {
      public void selectionChanged(IBindingInfo binding) {
        try {
          setObserveListener(false);
          handleBindingSelection(binding);
        } finally {
          setObserveListener(true);
        }
      }
    };
    private final ISelectionChangedListener m_observeListener = new ISelectionChangedListener() {
      boolean m_skipEvent;

      public void selectionChanged(SelectionChangedEvent event) {
        // check self-invoke
        if (m_skipEvent) {
          return;
        }
        m_skipEvent = true;
        //
        try {
          setBindingListener(false);
          handleObserveSelection();
        } finally {
          m_skipEvent = false;
          setBindingListener(true);
        }
      }
    };

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public SelectionSynchronizer() {
      setBindingListener(true);
      setObserveListener(true);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Handle
    //
    ////////////////////////////////////////////////////////////////////////////
    private void setBindingListener(boolean add) {
      m_bindingComposite.setBindingSelectionListener(add ? m_bindingListener : null);
    }

    private void setObserveListener(boolean add) {
      if (add) {
        m_targetObserveComposite.getMasterViewer().addPostSelectionChangedListener(
            m_observeListener);
        m_targetObserveComposite.getPropertiesViewer().addPostSelectionChangedListener(
            m_observeListener);
        m_modelObserveComposite.getMasterViewer().addPostSelectionChangedListener(m_observeListener);
        m_modelObserveComposite.getPropertiesViewer().addPostSelectionChangedListener(
            m_observeListener);
      } else {
        m_targetObserveComposite.getMasterViewer().removePostSelectionChangedListener(
            m_observeListener);
        m_targetObserveComposite.getPropertiesViewer().removePostSelectionChangedListener(
            m_observeListener);
        m_modelObserveComposite.getMasterViewer().removePostSelectionChangedListener(
            m_observeListener);
        m_modelObserveComposite.getPropertiesViewer().removePostSelectionChangedListener(
            m_observeListener);
      }
    }

    protected final void handleBindingSelection(IBindingInfo binding) {
      if (binding == null) {
        m_selection.clearBinding();
        // refresh all
        m_targetObserveComposite.refresh();
        m_modelObserveComposite.refresh();
      } else {
        m_selection.setBinding(m_databindingsProvider, binding);
        // refresh target
        m_targetObserveComposite.setSelection(binding.getTarget(), binding.getTargetProperty());
        // refresh model
        m_modelObserveComposite.setSelection(binding.getModel(), binding.getModelProperty());
      }
    }

    protected final void handleObserveSelection() {
      // store selection
      m_selection.clearBinding();
      m_selection.setTarget(m_databindingsProvider, m_targetObserveComposite);
      m_selection.setModel(m_databindingsProvider, m_modelObserveComposite);
      // prepare target selection
      IStructuredSelection targetSelection = getSelection(m_targetObserveComposite, true);
      IStructuredSelection targetPropertySelection = getSelection(m_targetObserveComposite, false);
      // prepare model selection
      IStructuredSelection modelSelection = getSelection(m_modelObserveComposite, true);
      IStructuredSelection modelPropertySelection = getSelection(m_modelObserveComposite, false);
      // check selection
      if (UiUtils.isEmpty(targetSelection)
          || UiUtils.isEmpty(targetPropertySelection)
          || UiUtils.isEmpty(modelSelection)
          || UiUtils.isEmpty(modelPropertySelection)) {
        setActionsEnabled(false);
      } else {
        // prepare target
        IObserveInfo target = (IObserveInfo) targetSelection.getFirstElement();
        IObserveInfo targetProperty = (IObserveInfo) targetPropertySelection.getFirstElement();
        // prepare model
        IObserveInfo model = (IObserveInfo) modelSelection.getFirstElement();
        IObserveInfo modelProperty = (IObserveInfo) modelPropertySelection.getFirstElement();
        // find represented observe
        for (IBindingInfo binding : m_databindingsProvider.getBindings()) {
          if (target == binding.getTarget()
              && targetProperty == binding.getTargetProperty()
              && model == binding.getModel()
              && modelProperty == binding.getModelProperty()) {
            StructuredSelection selection = new StructuredSelection(binding);
            if (!selection.equals(m_bindingComposite.getViewer().getSelection())) {
              m_bindingComposite.getViewer().setSelection(selection, true);
            }
            setActionsEnabled(false);
            m_selection.setBinding(m_databindingsProvider, binding);
            return;
          }
        }
        // validate target and model
        try {
          setActionsEnabled(m_databindingsProvider.validate(
              target,
              targetProperty,
              model,
              modelProperty));
        } catch (Throwable e) {
          DesignerPlugin.log(e);
        }
      }
    }

    private void setActionsEnabled(boolean enabled) {
      m_targetObserveComposite.getBindAction().setEnabled(enabled);
      m_modelObserveComposite.getBindAction().setEnabled(enabled);
    }
  }
  private static abstract class ExpandedListener implements ITreeViewerListener {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public ExpandedListener(ObserveElementsComposite observeElementsComposite) {
      observeElementsComposite.getMasterViewer().addTreeListener(this);
      observeElementsComposite.getPropertiesViewer().addTreeListener(this);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // TreeListener
    //
    ////////////////////////////////////////////////////////////////////////////
    public void treeCollapsed(TreeExpansionEvent event) {
      handle();
    }

    public void treeExpanded(TreeExpansionEvent event) {
      handle();
    }

    protected abstract void handle();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  static IStructuredSelection getSelection(ObserveElementsComposite observeComposite,
      boolean forMaster) {
    Viewer viewer =
        forMaster ? observeComposite.getMasterViewer() : observeComposite.getPropertiesViewer();
    return (IStructuredSelection) viewer.getSelection();
  }
}