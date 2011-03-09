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

import org.eclipse.wb.core.controls.CTableCombo;
import org.eclipse.wb.internal.core.nls.model.LocaleInfo;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog for selecting new locale.
 * 
 * @author scheglov_ke
 * @coverage core.nls.ui
 */
public class NewLocaleDialog extends ChooseLocaleDialog {
  private final LocaleInfo m_locales[];

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public NewLocaleDialog(Shell parentShell, LocaleInfo locales[]) {
    super(parentShell);
    // store existing locales
    m_locales = locales;
    LocaleUtils.sortByTitle(m_locales);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Control createDialogArea(Composite parent) {
    setTitle("Create new locale");
    setMessage("Specify the language and country for the new locale.");
    // create container
    Composite container = (Composite) super.createDialogArea(parent);
    Composite composite = new Composite(container, SWT.NONE);
    composite.setLayoutData(new GridData(GridData.FILL_BOTH));
    composite.setLayout(new GridLayout());
    // create groups
    createCopyGroup(composite);
    //
    return container;
  }

  @Override
  protected void updateSelectedLocale() {
    super.updateSelectedLocale();
    // check that we don't have such locale
    for (int i = 0; i < m_locales.length; i++) {
      LocaleInfo locale = m_locales[i];
      if (locale.equals(m_selectedLocale)) {
        setErrorMessage("Selected locale name '" + m_selectedLocale.getTitle() + "' already exists");
        getButton(IDialogConstants.OK_ID).setEnabled(false);
        return;
      }
    }
    // ok, we have good locale name
    setErrorMessage(null);
    getButton(IDialogConstants.OK_ID).setEnabled(true);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI: copy from group
  //
  ////////////////////////////////////////////////////////////////////////////
  private CTableCombo m_baseCombo;

  private void createCopyGroup(Composite parent) {
    Group copyGroup = new Group(parent, SWT.NONE);
    copyGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    copyGroup.setLayout(new GridLayout(2, false));
    copyGroup.setText("Copy");
    {
      Label countryLabel = new Label(copyGroup, SWT.NONE);
      countryLabel.setText("Copy strings from:");
    }
    {
      m_baseCombo = new CTableCombo(copyGroup, SWT.BORDER);
      m_baseCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      // fill base combo
      m_baseCombo.add("(none)", FlagImagesRepository.getEmptyFlagImage());
      for (int i = 0; i < m_locales.length; i++) {
        LocaleInfo locale = m_locales[i];
        m_baseCombo.add(locale.getTitle(), LocaleUtils.getImage(locale));
      }
      // add listener
      m_baseCombo.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          int index = m_baseCombo.getSelectionIndex();
          m_baseLocale = index == 0 ? null : m_locales[index - 1];
        }
      });
      // select (default) locale
      m_baseCombo.select(1);
      m_baseLocale = m_locales[0];
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  private LocaleInfo m_baseLocale;

  public LocaleInfo getBaseLocale() {
    return m_baseLocale;
  }
}
