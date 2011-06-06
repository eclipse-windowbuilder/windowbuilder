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
package org.eclipse.wb.internal.discovery.ui.preferences;

import org.eclipse.wb.internal.discovery.core.WBDiscoveryCorePlugin;
import org.eclipse.wb.internal.discovery.core.WBToolkit;
import org.eclipse.wb.internal.discovery.core.WBToolkitRegistry;
import org.eclipse.wb.internal.discovery.core.WBToolkitRegistry.IRegistryChangeListener;
import org.eclipse.wb.internal.discovery.ui.Messages;
import org.eclipse.wb.internal.discovery.ui.WBDiscoveryUiPlugin;
import org.eclipse.wb.internal.discovery.ui.util.BorderPainter;
import org.eclipse.wb.internal.discovery.ui.util.ProgressBarMonitor;
import org.eclipse.wb.internal.discovery.ui.wizard.DynamicRegistryHelper;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import java.util.ArrayList;
import java.util.List;

/**
 * The preference page used to manage the available WindowBuilder toolkits. Normally found in
 * Preferences > WindowBuilder > UI Toolkits.
 */
public class ToolkitsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, IRegistryChangeListener {
  static final String PREFERENCE_PAGE_ID =
      "org.eclipse.wb.internal.discovery.ui.preferences.ToolkitsPreferencePage";
  private List<ToolkitControl> controls = new ArrayList<ToolkitControl>();
  private Button contributeWizardEntriesButton;
  private Button installButton;
  private Button uninstallButton;
  private ProgressBar progressBar;
  private Composite scrolledContents;
  
  /**
   * Create a new ToolkitsPreferencePage.
   */
  public ToolkitsPreferencePage() {
    super();
    noDefaultAndApplyButton();
  }

  /**
   * Create a new ToolkitsPreferencePage.
   * 
   * @param title
   *          the preference page title
   */
  public ToolkitsPreferencePage(String title) {
    this();
    setTitle(title);
  }

  public void init(IWorkbench workbench) {
  }

  @Override
  protected Control createContents(Composite parent) {
    final Composite body = new Composite(parent, SWT.NULL);
    GridLayoutFactory.fillDefaults().applyTo(body);
    body.setFont(parent.getFont());
    Color bkColor = Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
    Label label = new Label(body, SWT.NONE);
    label.setText(Messages.ToolkitsPreferencePage_additionalToolkitsLabel);
    label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    final ScrolledComposite scrolledComposite =
        new ScrolledComposite(body, SWT.V_SCROLL | SWT.BORDER);
    scrolledComposite.setBackground(bkColor);
    scrolledComposite.setAlwaysShowScrollBars(true);
    GridDataFactory.fillDefaults().grab(true, true).hint(100, 100).applyTo(scrolledComposite);
    Composite buttonPanel = new Composite(body, SWT.NONE);
    GridLayoutFactory.fillDefaults().numColumns(2).applyTo(buttonPanel);
    buttonPanel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
    installButton = new Button(buttonPanel, SWT.PUSH);
    installButton.setText(Messages.ToolkitsPreferencePage_installButton);
    installButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        installToolkits();
      }
    });
    uninstallButton = new Button(buttonPanel, SWT.PUSH);
    uninstallButton.setText(Messages.ToolkitsPreferencePage_uninstallButton);
    uninstallButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        uninstallToolkits();
      }
    });
    progressBar = new ProgressBar(body, SWT.NONE);
    progressBar.setMaximum(100);
    progressBar.setSelection(35);
    progressBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    progressBar.setVisible(false);
    Label separator = new Label(body, SWT.SEPARATOR | SWT.HORIZONTAL);
    separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    contributeWizardEntriesButton = new Button(body, SWT.CHECK);
    contributeWizardEntriesButton.setText(Messages.ToolkitsPreferencePage_showUninstalled);
    contributeWizardEntriesButton.setSelection(WBDiscoveryUiPlugin.getPlugin().getContributeToWizards());
    if (WBDiscoveryUiPlugin.DEBUG) {
      separator = new Label(body, SWT.SEPARATOR | SWT.HORIZONTAL);
      separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      Button checkForUpdatesButton = new Button(body, SWT.PUSH);
      checkForUpdatesButton.setText(Messages.ToolkitsPreferencePage_checkForUpdates);
      checkForUpdatesButton.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          WBDiscoveryCorePlugin.getPlugin().checkForRegistryUpdates();
        }
      });
    }
    scrolledContents = new Composite(scrolledComposite, SWT.NONE);
    scrolledContents.setBackground(bkColor);
    scrolledContents.setRedraw(false);
    try {
      createToolkitsContents(scrolledContents);
    } finally {
      scrolledContents.layout(true);
      scrolledContents.setRedraw(true);
    }
    Point size = scrolledContents.computeSize(body.getSize().x, SWT.DEFAULT, true);
    scrolledContents.setSize(size);
    scrolledComposite.setExpandHorizontal(true);
    scrolledComposite.setMinWidth(100);
    scrolledComposite.setMinHeight(100);
    scrolledComposite.setExpandVertical(true);
    scrolledComposite.setMinHeight(1);
    scrolledComposite.addControlListener(new ControlAdapter() {
      @Override
      public void controlResized(ControlEvent e) {
        Point size = scrolledContents.computeSize(body.getSize().x - 20, SWT.DEFAULT, true);
        scrolledContents.setSize(size);
        scrolledComposite.setMinHeight(size.y);
        updateScrollIncrements(scrolledComposite);
      }
    });
    scrolledComposite.setContent(scrolledContents);
    Dialog.applyDialogFont(body);
    updateInstallButtons();
    body.layout(true);
    
    WBToolkitRegistry.getRegistry().addRegistryListener(this);
    
    WBDiscoveryCorePlugin.getPlugin().checkForRegistryUpdates();
    
    return body;
  }

  private void updateScrollIncrements(ScrolledComposite scrolledComposite) {
    int pageInc = scrolledComposite.getBounds().height;
    scrolledComposite.getVerticalBar().setIncrement(20);
    scrolledComposite.getVerticalBar().setPageIncrement(pageInc);
  }

  public void handleRegistryChange() {
    Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        controls.clear();
        
        for (Control control : scrolledContents.getChildren()) {
          control.dispose();
        }
        
        createToolkitsContents(scrolledContents);
      }
    });
  }
  
  @Override
  public void dispose() {
    WBToolkitRegistry.getRegistry().removeRegistryListener(this);
    
    super.dispose();
  }

  @Override
  public boolean performOk() {
    boolean contributeToWizards = contributeWizardEntriesButton.getSelection();
    boolean oldValue = WBDiscoveryUiPlugin.getPlugin().getContributeToWizards();
    if (contributeToWizards != oldValue) {
      WBDiscoveryUiPlugin.getPlugin().getPreferenceStore().setValue(
          WBDiscoveryUiPlugin.CONTRIBUTE_WIZARD_ENTRIES_PREF,
          contributeToWizards);
      if (contributeToWizards) {
        DynamicRegistryHelper.getRegistryHelper().registerWizards();
      } else {
        DynamicRegistryHelper.getRegistryHelper().removeRegistrations();
      }
    }
    return super.performOk();
  }

  private void createToolkitsContents(Composite composite) {
    GridLayoutFactory.fillDefaults().spacing(0, 0).applyTo(composite);
    List<WBToolkit> toolkits = WBToolkitRegistry.getRegistry().getToolkits();
    for (WBToolkit toolkit : toolkits) {
      final ToolkitControl toolkitControl = new ToolkitControl(composite, toolkit);
      controls.add(toolkitControl);
      toolkitControl.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          toolkitSelected(toolkitControl, e.stateMask);
        }
      });
      // a separator between connector descriptors
      Composite border = new Composite(composite, SWT.NULL);
      GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, 1).applyTo(border);
      GridLayoutFactory.fillDefaults().applyTo(border);
      border.addPaintListener(new BorderPainter());
    }
    composite.layout(true);
    composite.redraw();
  }

  void toolkitSelected(ToolkitControl toolkitControl, int stateMask) {
    if (ctrlClicked(stateMask)) {
      toolkitControl.setSelected(!toolkitControl.isSelected());
    } else {
      toolkitControl.setSelected(true);
      for (ToolkitControl tk : controls) {
        if (tk != toolkitControl) {
          tk.setSelected(false);
        }
      }
    }
    updateInstallButtons();
  }

  private void updateInstallButtons() {
    boolean installEnabled = false;
    boolean uninstallEnabled = false;
    if (getSelectionCount() > 0) {
      boolean installSelected = true;
      boolean uninstallSelected = true;
      for (ToolkitControl toolkitControl : controls) {
        if (toolkitControl.isSelected()) {
          if (toolkitControl.getToolkit().isInstalled()) {
            installSelected = false;
          } else {
            uninstallSelected = false;
          }
        }
      }
      installEnabled = installSelected;
      uninstallEnabled = uninstallSelected;
    }
    installButton.setEnabled(installEnabled);
    uninstallButton.setEnabled(uninstallEnabled);
  }

  private int getSelectionCount() {
    int count = 0;
    for (ToolkitControl toolkitControl : controls) {
      if (toolkitControl.isSelected()) {
        count++;
      }
    }
    return count;
  }

  private boolean ctrlClicked(int stateMask) {
    return (SWT.MOD1 & stateMask) != 0;
  }

  private void installToolkits() {
    progressBar.setVisible(true);
    List<WBToolkit> toolkits = new ArrayList<WBToolkit>();
    for (ToolkitControl control : controls) {
      if (control.isSelected()) {
        toolkits.add(control.getToolkit());
      }
    }
    
    IProgressMonitor monitor = new ProgressBarMonitor(progressBar);
    
    try {
      WBDiscoveryUiPlugin.getPlugin().installToolkits(toolkits, monitor);
      closePreferencesDialog();
    } catch (ProvisionException e) {
      monitor.done();
      
      MessageDialog.openError(
          getShell(),
          Messages.ToolkitsPreferencePage_errorInstalling,
          e.getMessage());
    } catch (OperationCanceledException e) {
      // ignore
    }
  }

  private void uninstallToolkits() {
    progressBar.setVisible(true);
    List<WBToolkit> toolkits = new ArrayList<WBToolkit>();
    for (ToolkitControl control : controls) {
      if (control.isSelected()) {
        toolkits.add(control.getToolkit());
      }
    }
    
    IProgressMonitor monitor = new ProgressBarMonitor(progressBar);
    
    try {
      WBDiscoveryUiPlugin.getPlugin().uninstallToolkits(
          toolkits, monitor);
      closePreferencesDialog();
    } catch (ProvisionException e) {
      monitor.done();
        
      MessageDialog.openError(
          getShell(),
          Messages.ToolkitsPreferencePage_errorUninstalling,
          e.getCause().getMessage());
    } catch (OperationCanceledException e) {
      // ignore
    }
  }

  private void closePreferencesDialog() {
    if (getContainer() instanceof PreferenceDialog) {
      PreferenceDialog dialog = (PreferenceDialog) getContainer();
      dialog.close();
    }
  }

}
