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
package org.eclipse.wb.internal.swing.laf.ui;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.dialogfields.AbstractValidationTitleAreaDialog;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogFieldUtils;
import org.eclipse.wb.internal.core.utils.dialogfields.IStringButtonAdapter;
import org.eclipse.wb.internal.core.utils.dialogfields.StringButtonDialogField;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.swing.Activator;
import org.eclipse.wb.internal.swing.laf.LafMessages;
import org.eclipse.wb.internal.swing.laf.LafSupport;
import org.eclipse.wb.internal.swing.laf.LafUtils;
import org.eclipse.wb.internal.swing.laf.command.AddCategoryCommand;
import org.eclipse.wb.internal.swing.laf.command.Command;
import org.eclipse.wb.internal.swing.laf.model.CategoryInfo;
import org.eclipse.wb.internal.swing.laf.model.LafInfo;
import org.eclipse.wb.internal.swing.laf.model.UserDefinedLafInfo;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import org.apache.commons.lang.ArrayUtils;

import java.text.MessageFormat;
import java.util.List;

/**
 * Abstract {@link Dialog} for working with Look-n-Feel.
 * 
 * @author mitin_aa
 * @coverage swing.laf.ui
 */
public abstract class AbstractCustomLookAndFeelDialog extends AbstractValidationTitleAreaDialog {
  protected final List<Command> m_commands = Lists.newArrayList();
  private final CategoryInfo m_targetCategory;
  // ui
  protected ComboViewer m_categoriesCombo;
  protected StringButtonDialogField m_jarField;
  protected ProgressMonitorPart m_progressMonitorPart;
  // choose jar workspace dialog settings
  private Object m_initSelection;
  private Object[] m_initExpanded;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractCustomLookAndFeelDialog(Shell parentShell,
      CategoryInfo targetCategory,
      String shellText,
      String titleText,
      Image titleImage,
      String titleMessage) {
    super(parentShell, Activator.getDefault(), shellText, titleText, titleImage, titleMessage);
    setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
    m_targetCategory = targetCategory;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Contents
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates the UI for managing LAF categories.
   */
  protected void createCategoriesUI(final Composite parent) {
    {
      Label label = new Label(parent, SWT.NONE);
      label.setText(LafMessages.AbstractCustomLookAndFeelDialog_category);
    }
    {
      m_categoriesCombo = new ComboViewer(parent, SWT.READ_ONLY);
      GridDataFactory.create(m_categoriesCombo.getControl()).grabH().fillH();
      m_categoriesCombo.setContentProvider(new IStructuredContentProvider() {
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        public void dispose() {
        }

        public Object[] getElements(Object inputElement) {
          return LafSupport.getLAFCategoriesList().toArray();
        }
      });
      m_categoriesCombo.setLabelProvider(new LabelProvider() {
        @Override
        public String getText(Object element) {
          return ((CategoryInfo) element).getName();
        }
      });
      m_categoriesCombo.setInput(m_categoriesCombo);
      m_categoriesCombo.setSelection(new StructuredSelection(m_targetCategory));
    }
    {
      Button newCategoryButton = new Button(parent, SWT.NONE);
      GridDataFactory.create(newCategoryButton).alignHF();
      newCategoryButton.setText(LafMessages.AbstractCustomLookAndFeelDialog_new);
      newCategoryButton.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          InputDialog inputDialog =
              new InputDialog(getShell(),
                  LafMessages.AbstractCustomLookAndFeelDialog_newCategory,
                  LafMessages.AbstractCustomLookAndFeelDialog_newCategoryEnterName,
                  "",
                  null);
          if (inputDialog.open() == Window.OK) {
            String newCategoryID = "category_" + System.currentTimeMillis();
            AddCategoryCommand command =
                new AddCategoryCommand(newCategoryID, inputDialog.getValue());
            command.execute();
            m_categoriesCombo.refresh();
            m_categoriesCombo.setSelection(new StructuredSelection(LafSupport.getCategory(newCategoryID)));
            // add command to be stored
            m_commands.add(command);
          }
        }
      });
    }
  }

  /**
   * Creates the {@link StringButtonDialogField} for selecting JAR file.
   */
  protected void createJarUI(final Composite parent) {
    JarFileSelectionButtonAdapter adapter = new JarFileSelectionButtonAdapter(parent);
    m_jarField = new StringButtonDialogField(adapter);
    adapter.setField(m_jarField);
    m_jarField.setButtonLabel(LafMessages.AbstractCustomLookAndFeelDialog_browse);
    m_jarField.setLabelText(LafMessages.AbstractCustomLookAndFeelDialog_jarPath);
    DialogFieldUtils.fillControls(parent, m_jarField, 3, 60);
    m_jarField.getTextControl(parent).setEditable(false);
  }

  /**
   * Creates the {@link ProgressMonitorPart}.
   */
  protected void createProgressUI(Composite container) {
    final GridLayout layout = new GridLayout();
    layout.marginHeight = 0;
    m_progressMonitorPart = new ProgressMonitorPart(container, layout);
    GridDataFactory.modify(m_progressMonitorPart).alignHF().spanH(3);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handlers
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Opens JAR selection dialog browsing workspace JARs, if the user selection is valid, updates JAR
   * selection label and schedules to parse selected JAR.
   */
  private void handleBrowseWorkspace() {
    JarSelectionDialog dialog = new JarSelectionDialog(getShell());
    if (m_initSelection != null) {
      dialog.setInitialSelection(m_initSelection);
    }
    dialog.setInitialExpanded(m_initExpanded);
    dialog.setTitle(LafMessages.AbstractCustomLookAndFeelDialog_jarTitle);
    dialog.setMessage(LafMessages.AbstractCustomLookAndFeelDialog_jarMessage);
    dialog.setInput(ResourcesPlugin.getWorkspace());
    int openResult = dialog.open();
    if (openResult == Window.OK) {
      IFile path = dialog.getSelectedJarFile();
      m_initSelection = dialog.getSelection();
      m_initExpanded = dialog.getExpandedElements();
      if (path != null) {
        String jarFileName = path.getLocation().toOSString();
        handleJarSelected(jarFileName);
      }
    }
  }

  /**
   * Opens JAR selection dialog browsing JARs in file system, if the user selection is valid,
   * updates JAR selection label and schedules to parse selected JAR.
   */
  private void handleBrowseFileSystem() {
    FileDialog dialog = new FileDialog(getShell());
    dialog.setFilterExtensions(new String[]{"*.jar"});
    String jarFileName = dialog.open();
    if (jarFileName != null) {
      handleJarSelected(jarFileName);
    }
  }

  /**
   * Called when the user selects jar file either using workspace or file-system.
   * 
   * @param jarFileName
   *          the path to selected jar file.
   */
  protected abstract void handleJarSelected(String jarFileName);

  /**
   * Called when the error occurred during scanning the jar file.
   */
  protected abstract void handleJarScanningError();

  /**
   * Performs scanning of JAR file using progress monitor and performs the error handling.
   * 
   * @param monitor
   *          the instance of {@link IProgressMonitor}, cannot be <code>null</code>.
   * @param jarFileName
   *          the full path of JAR file.
   * @return the array of {@link UserDefinedLafInfo} as array of objects.
   */
  protected Object[] scanJarFile(IProgressMonitor monitor, String jarFileName) {
    monitor.beginTask(
        LafMessages.AbstractCustomLookAndFeelDialog_jarScanning,
        IProgressMonitor.UNKNOWN);
    try {
      // proceed with scanning
      try {
        LafInfo[] lafsFound = LafUtils.scanJarForLookAndFeels(jarFileName, monitor);
        if (ArrayUtils.isEmpty(lafsFound)) {
          handleJarScanningError();
          return new String[]{LafMessages.AbstractCustomLookAndFeelDialog_jarEmpty};
        }
        return lafsFound;
      } catch (Throwable e) {
        DesignerPlugin.log(e);
        handleJarScanningError();
        return new String[]{MessageFormat.format(
            LafMessages.AbstractCustomLookAndFeelDialog_jarError,
            jarFileName)};
      }
    } finally {
      monitor.done();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Inner class 
  //
  ////////////////////////////////////////////////////////////////////////////
  private final class JarFileSelectionButtonAdapter implements IStringButtonAdapter {
    private StringButtonDialogField m_jarField;
    private final Composite m_parent;
    private Menu m_menu;

    private JarFileSelectionButtonAdapter(Composite parent) {
      m_parent = parent;
    }

    /**
     * Creates the popup menu showing by pressing "Browse" button
     */
    private Menu getPopupMenu() {
      if (m_menu == null) {
        m_menu = new Menu(m_jarField.getChangeControl(m_parent));
        {
          MenuItem item = new MenuItem(m_menu, SWT.NONE);
          item.setText(LafMessages.AbstractCustomLookAndFeelDialog_searchWorkspace);
          item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
              // open jar selection dialog selecting from workspace
              handleBrowseWorkspace();
            }
          });
        }
        {
          MenuItem item = new MenuItem(m_menu, SWT.NONE);
          item.setText(LafMessages.AbstractCustomLookAndFeelDialog_searchFileSystem);
          item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
              // open file system jar selection dialog 
              handleBrowseFileSystem();
            }
          });
        }
      }
      return m_menu;
    }

    public void changeControlPressed(DialogField field) {
      getPopupMenu();
      // prepare location
      Point menuLocation;
      {
        Rectangle bounds = m_jarField.getChangeControl(m_parent).getBounds();
        menuLocation = m_parent.toDisplay(bounds.x, bounds.y + bounds.height);
      }
      // show menu
      m_menu.setLocation(menuLocation);
      m_menu.setVisible(true);
    }

    /**
     * Sets the {@link StringButtonDialogField} for this adapter.
     */
    public void setField(StringButtonDialogField jarField) {
      m_jarField = jarField;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Command
  //
  ////////////////////////////////////////////////////////////////////////////
  public List<Command> getCommands() {
    return m_commands;
  }
}