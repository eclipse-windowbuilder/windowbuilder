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
import org.eclipse.wb.internal.core.nls.Messages;
import org.eclipse.wb.internal.core.nls.model.LocaleInfo;
import org.eclipse.wb.internal.core.nls.model.LocalePartInfo;
import org.eclipse.wb.internal.core.nls.model.LocalePartInfos;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

/**
 * Dialog for choosing locale.
 *
 * @author sablin_aa
 * @coverage core.nls.ui
 */
public class ChooseLocaleDialog extends TitleAreaDialog {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ChooseLocaleDialog(Shell parentShell) {
    this(parentShell, null);
  }

  public ChooseLocaleDialog(Shell parentShell, Locale locale) {
    super(parentShell);
    m_selectedLocale = new LocaleInfo(locale);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Control createContents(Composite parent) {
    Control control = super.createContents(parent);
    // set initial value
    if (m_selectedLocale != null) {
      Locale locale = m_selectedLocale.getLocale();
      if (locale != null) {
        updateLocalePartCombo(m_languageCombo, LocalePartInfos.getLanguages(), locale.getLanguage());
        updateLocalePartCombo(m_countryCombo, LocalePartInfos.getCountries(), locale.getCountry());
      }
    }
    updateSelectedLocale();
    return control;
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    setTitle(Messages.ChooseLocaleDialog_title);
    setMessage(Messages.ChooseLocaleDialog_message);
    // create container
    Composite container = (Composite) super.createDialogArea(parent);
    // create groups
    createLocaleGroup(container);
    //
    return container;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI: locale group
  //
  ////////////////////////////////////////////////////////////////////////////
  private CTableCombo m_languageCombo;
  private CTableCombo m_countryCombo;
  private TableViewer m_localesViewer;

  private void createLocaleGroup(Composite parent) {
    Group localeGroup = new Group(parent, SWT.NONE);
    localeGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    localeGroup.setLayout(new GridLayout(2, false));
    localeGroup.setText(Messages.ChooseLocaleDialog_localeGroup);
    // language
    {
      Label languageLabel = new Label(localeGroup, SWT.NONE);
      languageLabel.setText(Messages.ChooseLocaleDialog_languageLabel);
    }
    {
      m_languageCombo = new CTableCombo(localeGroup, SWT.BORDER);
      m_languageCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      // fill languages
      for (LocalePartInfo language : LocalePartInfos.getLanguages()) {
        m_languageCombo.add(language.toString(), language.getFlagImage());
      }
      // add listener
      m_languageCombo.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          updateSelectedLocale();
        }
      });
    }
    // country
    {
      Label countryLabel = new Label(localeGroup, SWT.NONE);
      countryLabel.setText(Messages.ChooseLocaleDialog_countryLabel);
    }
    {
      m_countryCombo = new CTableCombo(localeGroup, SWT.BORDER);
      m_countryCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      // fill countries
      for (LocalePartInfo country : LocalePartInfos.getCountries()) {
        m_countryCombo.add(country.toString(), country.getFlagImage());
      }
      m_countryCombo.select(0);
      // add listener
      m_countryCombo.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          updateSelectedLocale();
        }
      });
    }
    // all locales
    createLocalesViewer(localeGroup);
  }

  private void createLocalesViewer(Group localeGroup) {
    {
      Label countryLabel = new Label(localeGroup, SWT.NONE);
      countryLabel.setText(Messages.ChooseLocaleDialog_allLocalesLabel);
    }
    {
      m_localesViewer = new TableViewer(localeGroup, SWT.BORDER | SWT.FULL_SELECTION);
      // configure table
      Table table = m_localesViewer.getTable();
      {
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.heightHint = convertHeightInCharsToPixels(12);
        table.setLayoutData(gridData);
      }
      table.setLinesVisible(true);
      // configure viewer
      {
        // set content provider
        m_localesViewer.setContentProvider(new IStructuredContentProvider() {
          public Object[] getElements(Object inputElement) {
            Locale[] locales = Locale.getAvailableLocales();
            // sort locales by name
            Arrays.sort(locales, new Comparator<Locale>() {
              public int compare(Locale locale_1, Locale locale_2) {
                return locale_1.toString().compareTo(locale_2.toString());
              }
            });
            //
            return locales;
          }

          public void dispose() {
          }

          public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
          }
        });
        // set label provider
        {
          class LocalesLabelProvider extends LabelProvider implements ITableLabelProvider {
            public Image getColumnImage(Object element, int columnIndex) {
              Locale locale = (Locale) element;
              return FlagImagesRepository.getFlagImage(locale);
            }

            public String getColumnText(Object element, int columnIndex) {
              Locale locale = (Locale) element;
              return locale.toString() + " - " + locale.getDisplayName();
            }
          }
          m_localesViewer.setLabelProvider(new LocalesLabelProvider());
        }
        // fill viewer
        m_localesViewer.setInput(this);
        // add selection listener that updates separate language/country combo's
        m_localesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
          public void selectionChanged(SelectionChangedEvent event) {
            IStructuredSelection selection = (IStructuredSelection) m_localesViewer.getSelection();
            Locale locale = (Locale) selection.getFirstElement();
            if (locale != null) {
              onLocaleSelected(locale);
            }
          }
        });
        // add double click listener for fast select/close
        m_localesViewer.addDoubleClickListener(new IDoubleClickListener() {
          public void doubleClick(DoubleClickEvent event) {
            okPressed();
          }
        });
      }
    }
  }

  private void onLocaleSelected(Locale locale) {
    boolean needLocaleUpdate = false;
    // select language
    {
      String localeLanguage = locale.getLanguage();
      needLocaleUpdate |=
          updateLocalePartCombo(m_languageCombo, LocalePartInfos.getLanguages(), localeLanguage);
    }
    // select country
    {
      String localeCountry = locale.getCountry();
      needLocaleUpdate |=
          updateLocalePartCombo(m_countryCombo, LocalePartInfos.getCountries(), localeCountry);
    }
    // force update selected locale
    if (needLocaleUpdate) {
      updateSelectedLocale();
    }
  }

  private static boolean updateLocalePartCombo(CTableCombo combo,
      LocalePartInfo[] parts,
      String name) {
    int index = LocalePartInfos.indexByName(parts, name);
    if (index != -1) {
      if (index != combo.getSelectionIndex()) {
        combo.select(index);
        return true;
      }
    } else {
      combo.select(0);
    }
    return false;
  }

  protected void updateSelectedLocale() {
    if (m_languageCombo.getSelectionIndex() == -1) {
      getButton(IDialogConstants.OK_ID).setEnabled(false);
      return;
    }
    // prepare selected locale
    {
      // prepare locale
      Locale locale;
      {
        LocalePartInfo language =
            LocalePartInfos.getLanguages()[m_languageCombo.getSelectionIndex()];
        LocalePartInfo country = LocalePartInfos.getCountries()[m_countryCombo.getSelectionIndex()];
        if (country.getName().length() == 0) {
          locale = new Locale(language.getName());
        } else {
          locale = new Locale(language.getName(), country.getName());
        }
      }
      // create selected locale information
      m_selectedLocale = new LocaleInfo(locale);
      // show locale in locales viewer
      m_localesViewer.setSelection(new StructuredSelection(locale));
    }
    // ok, we have good locale name
    setErrorMessage(null);
    getButton(IDialogConstants.OK_ID).setEnabled(true);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dialog: shell
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(Messages.ChooseLocaleDialog_shellTitle);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  protected LocaleInfo m_selectedLocale;

  public LocaleInfo getSelectedLocale() {
    return m_selectedLocale;
  }
}
