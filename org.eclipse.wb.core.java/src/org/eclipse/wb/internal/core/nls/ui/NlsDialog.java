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
package org.eclipse.wb.internal.core.nls.ui;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.nls.Messages;
import org.eclipse.wb.internal.core.nls.NlsSupport;
import org.eclipse.wb.internal.core.nls.edit.IEditableSource;
import org.eclipse.wb.internal.core.nls.edit.IEditableSupport;
import org.eclipse.wb.internal.core.nls.edit.IEditableSupportListener;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ResizableDialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Main dialog for editing NLS information.
 *
 * @author scheglov_ke
 * @coverage core.nls.ui
 */
public final class NlsDialog extends ResizableDialog {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Model objects
  //
  ////////////////////////////////////////////////////////////////////////////
  private final JavaInfo m_root;
  private final NlsSupport m_support;
  private final IEditableSupport m_editableSupport;
  ////////////////////////////////////////////////////////////////////////////
  //
  // UI objects
  //
  ////////////////////////////////////////////////////////////////////////////
  private TabFolder m_tabFolder;
  private List<SourceComposite> m_composites;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public NlsDialog(Shell parentShell, JavaInfo root) throws Exception {
    super(parentShell, DesignerPlugin.getDefault());
    m_root = root;
    m_support = NlsSupport.get(root);
    m_editableSupport = m_support.getEditable();
    m_editableSupport.addListener(new IEditableSupportListener() {
      @Override
      public void sourceAdded(final IEditableSource source) {
        ExecutionUtils.runLog(new RunnableEx() {
          public void run() throws Exception {
            int tabIndex = m_tabFolder.getItemCount() - 1;
            createStringsTab(source, tabIndex);
          }
        });
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dialog: creation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Control createDialogArea(Composite parent) {
    Composite area = (Composite) super.createDialogArea(parent);
    area.setLayout(new GridLayout());
    //
    m_tabFolder = new TabFolder(area, SWT.NONE);
    m_tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
    //
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        createStringsTabs();
        createPropertiesTab();
        // if there are no "real" sources, open "Properties" tab
        if (!m_editableSupport.hasExistingSources()) {
          m_tabFolder.setSelection(m_tabFolder.getItemCount() - 1);
        }
      }
    });
    //
    return area;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Strings tabs
  //
  ////////////////////////////////////////////////////////////////////////////
  private void createStringsTabs() throws Exception {
    m_composites = Lists.newArrayList();
    // create composite for each source
    List<IEditableSource> sources = getSortedSourcesList();
    for (IEditableSource editableSource : sources) {
      int tabIndex = m_tabFolder.getItemCount();
      SourceComposite composite = createStringsTab(editableSource, tabIndex);
      m_composites.add(composite);
    }
  }

  /**
   * getEditableSources() returns unsorted collection, but we want to show sources sorted by title,
   * so prepare sorted list.
   */
  private List<IEditableSource> getSortedSourcesList() {
    List<IEditableSource> sources = m_editableSupport.getEditableSources();
    List<IEditableSource> sourcesList = new ArrayList<IEditableSource>(sources);
    Collections.sort(sourcesList, new Comparator<IEditableSource>() {
      public int compare(IEditableSource source_1, IEditableSource source_2) {
        return source_1.getShortTitle().compareTo(source_2.getShortTitle());
      }
    });
    return sourcesList;
  }

  private SourceComposite createStringsTab(IEditableSource editableSource, int tabIndex)
      throws Exception {
    TabItem item = new TabItem(m_tabFolder, SWT.NONE, tabIndex);
    item.setText(editableSource.getShortTitle());
    // create composite for tab
    SourceComposite composite = new SourceComposite(m_tabFolder, SWT.NONE, editableSource);
    item.setControl(composite);
    //
    return composite;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tab: Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  private void createPropertiesTab() {
    TabItem item = new TabItem(m_tabFolder, SWT.NONE);
    item.setText(Messages.NlsDialog_propertiesPage);
    //
    PropertiesComposite composite =
        new PropertiesComposite(m_tabFolder, SWT.NONE, m_editableSupport);
    item.setControl(composite);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dialog: shell
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(Messages.NlsDialog_title);
  }

  @Override
  protected Point getDefaultSize() {
    Display display = Display.getCurrent();
    Rectangle displayArea = display.getClientArea();
    return new Point(displayArea.width / 2, displayArea.height / 2);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dialog: buttons
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false);
    createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
  }

  @Override
  protected void okPressed() {
    getButton(IDialogConstants.OK_ID).setEnabled(false);
    try {
      ExecutionUtils.run(m_root, new RunnableEx() {
        public void run() throws Exception {
          m_support.applyEditable(m_editableSupport);
        }
      });
    } finally {
      getButton(IDialogConstants.OK_ID).setEnabled(true);
    }
    super.okPressed();
  }
}