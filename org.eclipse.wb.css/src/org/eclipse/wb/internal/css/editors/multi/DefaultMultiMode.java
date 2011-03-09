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
package org.eclipse.wb.internal.css.editors.multi;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;

/**
 * The default mode for presentation source/design parts of {@link MultiPageEditor}.
 * 
 * @author sablin_aa
 * @coverage CSS.editor
 */
abstract class DefaultMultiMode extends MultiMode {
  protected CTabFolder m_folder;
  protected IDesignPage m_activePage;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DefaultMultiMode(MultiPageEditor editor) {
    super(editor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Create tab folder for pages.
   */
  protected final void createTabFolder(Composite parent) {
    m_folder = new CTabFolder(parent, SWT.BOTTOM);
    m_folder.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        showPage((IDesignPage) e.item.getData());
      }
    });
  }

  /**
   * Add given {@link IDesignPage} page to tab folder.
   */
  protected final CTabItem createTab(IDesignPage page) {
    CTabItem item = new CTabItem(m_folder, SWT.NONE);
    item.setData(page);
    item.setText(page.getName());
    item.setImage(page.getImage());
    item.setControl(page.createControl(m_folder));
    return item;
  }

  /**
   * Show given {@link IDesignPage} page.
   */
  protected final void showPage(IDesignPage page) {
    if (m_activePage == page) {
      return;
    }
    // deactivate old page
    if (m_activePage != null) {
      m_activePage.handleActiveState(false);
    }
    // handle show "Source" or "Design"
    if (m_folder != null) {
      CTabItem[] folderItems = m_folder.getItems();
      for (int i = 0; i < folderItems.length; i++) {
        CTabItem folderItem = folderItems[i];
        if (folderItem.getData() == page) {
          m_folder.setSelection(folderItem);
        }
      }
    }
    // activate new page
    m_activePage = page;
    m_activePage.handleActiveState(true);
    m_activePage.setFocus();
    handleShowPage();
  }

  /**
   * Handle change active page.
   */
  protected void handleShowPage() {
  }
}