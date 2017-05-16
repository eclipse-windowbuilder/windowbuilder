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
package org.eclipse.wb.internal.core.nls;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.nls.model.AbstractSource;
import org.eclipse.wb.internal.core.nls.model.LocaleInfo;
import org.eclipse.wb.internal.core.nls.ui.LocaleUtils;
import org.eclipse.wb.internal.core.nls.ui.NlsDialog;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import org.apache.commons.lang.ArrayUtils;

import java.util.Calendar;

/**
 * Implementation of {@link ContributionItem} for working with NLS.
 *
 * @author scheglov_ke
 * @coverage core.nls.ui
 */
public final class ExternalizeStringsContributionItem extends ContributionItem {
  private JavaInfo m_root;
  private ToolItem m_toolItem;
  private Menu m_menu;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the root {@link JavaInfo} displayed currently in editor.
   */
  public void setRoot(JavaInfo root) {
    m_root = root;
    displayCurrentLocale();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ContributionItem
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void fill(final ToolBar parent, int index) {
    m_toolItem = new ToolItem(parent, SWT.DROP_DOWN);
    if (isSpecialTime()) {
      m_toolItem.setImage(DesignerPlugin.getImage("nls/ms16.png"));
      m_toolItem.setToolTipText("Back in USSR");
    } else {
      m_toolItem.setImage(DesignerPlugin.getImage("nls/globe3.png"));
      m_toolItem.setToolTipText(Messages.ExternalizeStringsContributionItem_externalizeToolTip);
    }
    // listener
    m_toolItem.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        handleClick(parent, event);
      }
    });
  }

  @Override
  public void dispose() {
    super.dispose();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Menu
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Handles click on {@link #m_toolItem}, select {@link LocaleInfo} or open {@link NlsDialog}.
   */
  private void handleClick(final ToolBar parent, final Event event) {
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        if (event.detail == SWT.ARROW && !ArrayUtils.isEmpty(getLocales())) {
          Rectangle bounds = m_toolItem.getBounds();
          Point point = parent.toDisplay(bounds.x, bounds.y + bounds.height);
          createMenu(parent);
          m_menu.setLocation(point);
          m_menu.setVisible(true);
        } else {
          openNLSDialog();
        }
      }
    });
  }

  /**
   * Creates {@link #m_menu} with {@link MenuItem} for each {@link LocaleInfo}.
   */
  private void createMenu(Composite parent) throws Exception {
    // dispose old menu
    if (m_menu != null) {
      m_menu.dispose();
    }
    // create new menu
    m_menu = new Menu(parent);
    {
      LocaleInfo currentLocale = AbstractSource.getLocaleInfo(m_root);
      //
      LocaleInfo[] locales = getLocales();
      for (final LocaleInfo locale : locales) {
        final MenuItem menuItem = new MenuItem(m_menu, SWT.RADIO);
        // presentation
        menuItem.setImage(LocaleUtils.getImage(locale));
        menuItem.setText(locale.getTitle());
        menuItem.setSelection(locale.equals(currentLocale));
        // listener
        menuItem.addListener(SWT.Selection, new Listener() {
          public void handleEvent(Event event) {
            if (menuItem.getSelection()) {
              AbstractSource.setLocaleInfo(m_root, locale);
              setCurrentLocale(locale);
            }
          }
        });
      }
    }
  }

  /**
   * @return the array of {@link LocaleInfo} associated with current root.
   */
  private LocaleInfo[] getLocales() throws Exception {
    NlsSupport support = NlsSupport.get(m_root);
    return support.getLocales();
  }

  /**
   * Opens {@link NlsDialog}.
   */
  private void openNLSDialog() throws Exception {
    boolean isDefaultPackage =
        m_root.getEditor().getModelUnit().getPackageDeclarations().length == 0;
    if (isDefaultPackage) {
      MessageDialog.openError(
          DesignerPlugin.getShell(),
          Messages.ExternalizeStringsContributionItem_defaultPackageTitle,
          Messages.ExternalizeStringsContributionItem_defaultPackageMessage);
    } else {
      NlsDialog dialog = new NlsDialog(DesignerPlugin.getShell(), m_root);
      // open and expect OK
      if (dialog.open() != Window.OK) {
        return;
      }
      // may be some error happened, so hierarchy was disposed
      if (JavaInfoUtils.getState(m_root).isDisposed()) {
        return;
      }
      // do update
      updateCurrentLocale();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Current locale
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Displays current locale on {@link #m_toolItem}.
   */
  private void displayCurrentLocale() {
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        if (NlsSupport.get(m_root).getLocales().length == 0) {
          m_toolItem.setImage(DesignerPlugin.getImage(!isSpecialTime()
              ? "nls/globe3.png"
              : "nls/ms16.png"));
          m_toolItem.setText("");
        } else {
          LocaleInfo locale = AbstractSource.getLocaleInfo(m_root);
          m_toolItem.setImage(LocaleUtils.getImage(locale));
          m_toolItem.setText(locale.getTitle());
        }
      }
    });
  }

  /**
   * Ensures that current locale exists (uses first locale, if current one does not exist).
   */
  private void updateCurrentLocale() {
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        NlsSupport support = NlsSupport.get(m_root);
        // do nothing, if no sources/locales
        if (support.getSources().length == 0) {
          return;
        }
        // prepare current locale
        LocaleInfo locale = AbstractSource.getLocaleInfo(m_root);
        // check that current locale is still alive
        LocaleInfo[] locales = support.getLocales();
        if (!ArrayUtils.contains(locales, locale)) {
          locale = locales[0];
        }
        // in any case, show with current locale
        setCurrentLocale(locale);
      }
    });
  }

  /**
   * Sets new current locale.
   */
  private void setCurrentLocale(final LocaleInfo locale) {
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        AbstractSource.setLocaleInfo(m_root, locale);
        displayCurrentLocale();
        m_root.refresh();
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Egg
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if current date is May, 1st and time after 5:00pm. :-)
   */
  private boolean isSpecialTime() {
    int month = Calendar.getInstance().get(Calendar.MONTH);
    int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    return month == Calendar.MAY && day == 1 && hour > 16;
  }
}