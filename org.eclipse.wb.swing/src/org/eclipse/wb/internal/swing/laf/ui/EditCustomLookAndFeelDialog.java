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

import org.eclipse.wb.internal.core.utils.dialogfields.DialogFieldUtils;
import org.eclipse.wb.internal.core.utils.dialogfields.StringDialogField;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.swing.laf.LafMessages;
import org.eclipse.wb.internal.swing.laf.command.EditCommand;
import org.eclipse.wb.internal.swing.laf.command.EditNameCommand;
import org.eclipse.wb.internal.swing.laf.command.MoveCommand;
import org.eclipse.wb.internal.swing.laf.model.CategoryInfo;
import org.eclipse.wb.internal.swing.laf.model.LafInfo;
import org.eclipse.wb.internal.swing.laf.model.UserDefinedLafInfo;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.apache.commons.lang.StringUtils;

/**
 * Dialog for editing the Look-n-Feel. Allows editing name, class name, jar file for custom LAFs.
 * Else allows editing just the name.
 * 
 * @author mitin_aa
 * @coverage swing.laf.ui
 */
public class EditCustomLookAndFeelDialog extends AbstractCustomLookAndFeelDialog {
  private final LafInfo m_lafInfo;
  private StringDialogField m_nameField;
  private ComboViewer m_classNameViewer;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EditCustomLookAndFeelDialog(Shell parentShell, LafInfo lafInfo) {
    super(parentShell,
        lafInfo.getCategory(),
        LafMessages.EditCustomLookAndFeelDialog_edit,
        LafMessages.EditCustomLookAndFeelDialog_edit,
        null,
        LafMessages.EditCustomLookAndFeelDialog_editProperties);
    m_lafInfo = lafInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Contents
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createControls(Composite container) {
    GridLayoutFactory.create(container).columns(3);
    // category
    {
      createCategoriesUI(container);
    }
    // name
    {
      m_nameField = new StringDialogField();
      m_nameField.setLabelText(LafMessages.EditCustomLookAndFeelDialog_name);
      DialogFieldUtils.fillControls(container, m_nameField, 2, 40);
      m_nameField.setText(m_lafInfo.getName());
      {
        new Label(container, SWT.NONE);
      }
    }
    // jar
    {
      createJarUI(container);
      m_jarField.setEnabled(m_lafInfo instanceof UserDefinedLafInfo);
      if (m_lafInfo instanceof UserDefinedLafInfo) {
        UserDefinedLafInfo userDefinedLAFInfo = (UserDefinedLafInfo) m_lafInfo;
        m_jarField.setText(userDefinedLAFInfo.getJarFile());
      }
    }
    // class name
    {
      {
        Label label = new Label(container, SWT.NONE);
        label.setText(LafMessages.EditCustomLookAndFeelDialog_className);
        label.setEnabled(m_lafInfo instanceof UserDefinedLafInfo);
      }
      {
        m_classNameViewer = new ComboViewer(container, SWT.READ_ONLY);
        GridDataFactory.create(m_classNameViewer.getControl()).fillH().grabH();
        m_classNameViewer.setContentProvider(new ArrayContentProvider());
        m_classNameViewer.setLabelProvider(new LabelProvider() {
          @Override
          public String getText(Object element) {
            return ((UserDefinedLafInfo) element).getClassName();
          }
        });
        m_classNameViewer.getControl().setEnabled(m_lafInfo instanceof UserDefinedLafInfo);
      }
    }
    {
      createProgressUI(container);
    }
    // configure viewer
    if (m_lafInfo instanceof UserDefinedLafInfo) {
      UserDefinedLafInfo userDefinedLAFInfo = (UserDefinedLafInfo) m_lafInfo;
      Object[] scannedLAFs = scanJarFile(m_progressMonitorPart, userDefinedLAFInfo.getJarFile());
      m_classNameViewer.setInput(scannedLAFs);
      // set selection by searching for class name (this LAFInfo instances is *not* the same as m_lafInfo)
      for (Object lafObject : scannedLAFs) {
        if (((UserDefinedLafInfo) lafObject).getClassName().equals(
            userDefinedLAFInfo.getClassName())) {
          m_classNameViewer.setSelection(new StructuredSelection(lafObject));
          break;
        }
      }
    }
    // set focus to name field
    Text nameTextControl = m_nameField.getTextControl(container);
    nameTextControl.selectAll();
    nameTextControl.setFocus();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handlers
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void handleJarSelected(String jarFileName) {
    m_classNameViewer.getControl().setEnabled(true);
    m_jarField.setText(jarFileName);
    Object[] scannedLAFs = scanJarFile(m_progressMonitorPart, jarFileName);
    m_classNameViewer.setInput(scannedLAFs);
  }

  @Override
  protected void handleJarScanningError() {
    m_classNameViewer.getControl().setEnabled(false);
  }

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
    if (!targetCategory.getID().equals(m_lafInfo.getCategory().getID())) {
      m_commands.add(new MoveCommand(m_lafInfo, targetCategory, null));
    }
    //
    String name = m_nameField.getText();
    if (m_lafInfo instanceof UserDefinedLafInfo) {
      selection = (IStructuredSelection) m_classNameViewer.getSelection();
      UserDefinedLafInfo selectedLAF = (UserDefinedLafInfo) selection.getFirstElement();
      m_commands.add(new EditCommand(m_lafInfo.getID(),
          name,
          selectedLAF.getClassName(),
          selectedLAF.getJarFile()));
    } else {
      m_commands.add(new EditNameCommand(m_lafInfo.getID(), name));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validating
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String validate() throws Exception {
    String name = m_nameField.getText();
    if (StringUtils.isEmpty(name)) {
      return LafMessages.EditCustomLookAndFeelDialog_msgEnterName;
    }
    IStructuredSelection selection = (IStructuredSelection) m_classNameViewer.getSelection();
    if (m_lafInfo instanceof UserDefinedLafInfo && (selection == null || selection.isEmpty())) {
      return LafMessages.EditCustomLookAndFeelDialog_msgEnterClass;
    }
    return super.validate();
  }
}
