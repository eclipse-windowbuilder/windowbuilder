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
package org.eclipse.wb.internal.core.editor.multi;

import org.eclipse.wb.core.editor.IEditorPage;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.TabFolderDecorator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;

/**
 * The default mode for presentation source/design parts of {@link DesignerEditor}.
 *
 * @author scheglov_ke
 * @coverage core.editor
 */
abstract class DefaultMultiMode extends MultiMode {
  protected CTabFolder m_folder;
  protected IEditorPage m_activePage;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DefaultMultiMode(DesignerEditor editor) {
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
    TabFolderDecorator.decorate(m_editor, m_folder);
    m_folder.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        showPage((IEditorPage) e.item.getData());
      }
    });
    // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=199499
    // Switching tabs by Ctrl+PageUp/PageDown must not be caught on the inner tab set
    m_folder.addTraverseListener(new TraverseListener() {
      public void keyTraversed(TraverseEvent e) {
        switch (e.detail) {
          case SWT.TRAVERSE_PAGE_NEXT :
          case SWT.TRAVERSE_PAGE_PREVIOUS :
            final int detail = e.detail;
            e.doit = true;
            e.detail = SWT.TRAVERSE_NONE;
            // 3.6+
            ExecutionUtils.runIgnore(new RunnableEx() {
              public void run() throws Exception {
                Control control = m_folder.getParent();
                ReflectionUtils.invokeMethod(
                    control,
                    "traverse(int,org.eclipse.swt.widgets.Event)",
                    detail,
                    new Event());
              }
            });
        }
      }
    });
  }

  /**
   * Add given {@link IEditorPage} page to tab folder.
   */
  protected final CTabItem createTab(IEditorPage page) {
    CTabItem item = new CTabItem(m_folder, SWT.NONE);
    item.setData(page);
    item.setText(page.getName());
    item.setImage(page.getImage());
    item.setControl(page.createControl(m_folder));
    return item;
  }

  /**
   * Show given {@link IEditorPage} page.
   */
  protected final void showPage(IEditorPage page) {
    if (m_activePage == page) {
      return;
    }
    // deactivate old page
    if (m_activePage != null) {
      m_activePage.handleActiveState(false);
    }
    // handle show "Source" or "Design"
    if (m_folder != null) {
      for (CTabItem item : m_folder.getItems()) {
        if (item.getData() == page) {
          m_folder.setSelection(item);
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

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final boolean isSourceActive() {
    return m_activePage == m_sourcePage;
  }

  @Override
  public final boolean isDesignActive() {
    return m_activePage == m_designPage;
  }
}