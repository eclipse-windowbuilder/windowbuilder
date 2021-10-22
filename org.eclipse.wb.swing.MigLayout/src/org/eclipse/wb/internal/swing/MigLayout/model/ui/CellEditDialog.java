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
package org.eclipse.wb.internal.swing.MigLayout.model.ui;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ResizableDialog;
import org.eclipse.wb.internal.swing.MigLayout.Activator;
import org.eclipse.wb.internal.swing.MigLayout.model.CellConstraintsSupport;
import org.eclipse.wb.internal.swing.MigLayout.model.MigLayoutInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.ModelMessages;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * The dialog for editing {@link CellConstraintsSupport}.
 *
 * @author scheglov_ke
 * @coverage swing.MigLayout.ui
 */
public final class CellEditDialog extends ResizableDialog {
  private final MigLayoutInfo m_layout;
  private final CellConstraintsSupport m_cell;
  private final String m_cellString;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CellEditDialog(Shell parentShell, MigLayoutInfo layout, CellConstraintsSupport cell) {
    super(parentShell, Activator.getDefault());
    setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
    m_layout = layout;
    m_cell = cell;
    m_cellString = m_cell.getString();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI fields
  //
  ////////////////////////////////////////////////////////////////////////////
  // specification
  private CellSpecificationComposite m_specificationComposite;

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void create() {
    super.create();
    showCell();
    m_specificationComposite.setFocus();
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite container = (Composite) super.createDialogArea(parent);
    createHeaderComposite(container);
    return container;
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(ModelMessages.CellEditDialog_cellProperties);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Buttons
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void buttonPressed(int buttonId) {
    if (buttonId == IDialogConstants.OK_ID) {
      commitChanges();
    }
    if (buttonId == IDialogConstants.CANCEL_ID) {
      rollbackChanges();
    }
    super.buttonPressed(buttonId);
  }

  /**
   * Enable/disable OK button.
   */
  private void updateButtons(boolean valid) {
    getButton(IDialogConstants.OK_ID).setEnabled(valid);
  }

  /**
   * Saves {@link CellConstraintsSupport} changes into source and refreshes GUI.
   */
  private void commitChanges() {
    ExecutionUtils.run(m_layout, new RunnableEx() {
      public void run() throws Exception {
        m_cell.write();
      }
    });
  }

  /**
   * Rolls back changes in {@link CellConstraintsSupport}.
   */
  private void rollbackChanges() {
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        m_cell.setString(m_cellString);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Composites
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Create the header displaying {@link Composite}.
   */
  private void createHeaderComposite(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);
    GridDataFactory.create(composite).grabH().fill();
    GridLayoutFactory.create(composite).noMargins().columns(4);
    // specification
    {
      new Label(composite, SWT.NONE).setText(ModelMessages.CellEditDialog_specification);
      {
        m_specificationComposite = new CellSpecificationComposite(composite);
        GridDataFactory.create(m_specificationComposite).spanH(3).indentHC(3).hintHC(30);
        m_specificationComposite.addListener(SWT.Modify, new Listener() {
          public void handleEvent(Event e) {
            updateButtons(e.doit);
            showCell();
          }
        });
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Shows the {@link CellConstraintsSupport} in UI controls.
   */
  private void showCell() {
    // specification
    m_specificationComposite.fromCell(m_cell);
  }
}
