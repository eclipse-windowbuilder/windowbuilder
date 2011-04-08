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

import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.swing.laf.LafMessages;
import org.eclipse.wb.internal.swing.laf.command.AddCommand;
import org.eclipse.wb.internal.swing.laf.model.CategoryInfo;
import org.eclipse.wb.internal.swing.laf.model.LafEntryInfo;
import org.eclipse.wb.internal.swing.laf.model.UserDefinedLafInfo;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog for entering new Look-n-Feel.
 * 
 * @author mitin_aa
 * @coverage swing.laf.ui
 */
public final class AddCustomLookAndFeelDialog extends AbstractCustomLookAndFeelDialog {
  // fields
  CheckboxTableViewer m_classTable;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AddCustomLookAndFeelDialog(Shell parentShell, CategoryInfo targetCategory) {
    super(parentShell,
        targetCategory,
        LafMessages.AddCustomLookAndFeelDialog_add,
        LafMessages.AddCustomLookAndFeelDialog_add,
        null,
        LafMessages.AddCustomLookAndFeelDialog_message);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Contents
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createControls(Composite container) {
    {
      GridLayoutFactory.create(container).columns(3);
      // category
      {
        createCategoriesUI(container);
      }
      // separator label
      {
        Label label = new Label(container, SWT.NONE);
        GridDataFactory.create(label).spanH(3);
      }
      // jar
      {
        createJarUI(container);
      }
      // LAF classes
      {
        Label label = new Label(container, SWT.NONE);
        GridDataFactory.create(label).spanH(3).fillH();
        label.setText(LafMessages.AddCustomLookAndFeelDialog_classesLabel);
      }
      {
        m_classTable =
            CheckboxTableViewer.newCheckList(container, SWT.BORDER
                | SWT.FULL_SELECTION
                | SWT.V_SCROLL);
        GridDataFactory.create(m_classTable.getControl()).spanH(2).grab().fill().hintVC(6);
      }
      // buttons composite
      {
        Composite composite = new Composite(container, SWT.NONE);
        GridDataFactory.create(composite).fill();
        GridLayoutFactory.create(composite).noMargins();
        {
          Button button = new Button(composite, SWT.NONE);
          GridDataFactory.create(button).grabH().alignHF();
          button.setText(LafMessages.AddCustomLookAndFeelDialog_selectAll);
          button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
              if (m_classTable.getTable().isEnabled()) {
                m_classTable.setAllChecked(true);
              }
            }
          });
        }
        {
          Button button = new Button(composite, SWT.NONE);
          GridDataFactory.create(button).grabH().alignHF();
          button.setText(LafMessages.AddCustomLookAndFeelDialog_deselectAll);
          button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
              if (m_classTable.getTable().isEnabled()) {
                m_classTable.setAllChecked(false);
              }
            }
          });
        }
      }
    }
    // progress
    {
      createProgressUI(container);
    }
    // configure LAF class table viewer 
    m_classTable.setContentProvider(new IStructuredContentProvider() {
      public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      }

      public void dispose() {
      }

      public Object[] getElements(Object inputElement) {
        return scanJarFile(m_progressMonitorPart, (String) inputElement);
      }
    });
    m_classTable.setLabelProvider(new LabelProvider() {
      @Override
      public String getText(Object element) {
        if (element instanceof LafEntryInfo) {
          return ((LafEntryInfo) element).getName();
        }
        return element.toString();
      }
    });
  }

  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handlers
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void okPressed() {
    handleApply();
    super.okPressed();
  }

  /**
   * Applies user selections (adds created LAF, stores in persistence).
   */
  private void handleApply() {
    IStructuredSelection selection = (IStructuredSelection) m_categoriesCombo.getSelection();
    CategoryInfo targetCategory = (CategoryInfo) selection.getFirstElement();
    Object[] checkedLafs = m_classTable.getCheckedElements();
    if (checkedLafs.length > 0) {
      for (Object lafInfo : checkedLafs) {
        m_commands.add(new AddCommand(targetCategory, (UserDefinedLafInfo) lafInfo));
      }
    }
  }

  @Override
  protected void handleJarSelected(String jarFileName) {
    m_classTable.getTable().setEnabled(true);
    m_jarField.setText(jarFileName);
    enableButtons(false);
    m_classTable.setInput(jarFileName);
    enableButtons(true);
  }

  @Override
  protected void handleJarScanningError() {
    m_classTable.getTable().setEnabled(false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils/Misc
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Enables/Disables dialog buttons.
   */
  void enableButtons(boolean enabled) {
    getButton(IDialogConstants.CANCEL_ID).setEnabled(enabled);
    getButton(IDialogConstants.OK_ID).setEnabled(enabled);
  }
}
