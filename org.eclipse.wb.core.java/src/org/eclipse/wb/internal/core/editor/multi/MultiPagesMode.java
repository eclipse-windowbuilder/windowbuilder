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
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPage;

/**
 * Implementation of {@link MultiMode} for pages mode.
 *
 * @author scheglov_ke
 * @coverage core.editor
 */
final class MultiPagesMode extends DefaultMultiMode {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MultiPagesMode(DesignerEditor editor) {
    super(editor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  void setFocus() {
    int selectionIndex = m_folder.getSelectionIndex();
    if (isSourcePageFirst() && selectionIndex == 0 || !isSourcePageFirst() && selectionIndex == 1) {
      m_sourcePage.setFocus();
    } else {
      m_designPage.setFocus();
    }
  }

  @Override
  public void showSource() {
    if (m_activePage != m_sourcePage) {
      showPage(m_sourcePage);
    }
  }

  @Override
  public void showDesign() {
    if (m_activePage != m_designPage) {
      showPage(m_designPage);
    }
  }

  @Override
  public void switchSourceDesign() {
    showPage(m_activePage == m_sourcePage ? m_designPage : m_sourcePage);
  }

  @Override
  public void onSetInput() {
    if (m_activePage == m_designPage) {
      m_designPage.handleActiveState(false);
      m_designPage.handleActiveState(true);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  void create(Composite parent) {
    super.create(parent);
    createTabFolder(parent);
    // create "Source" & "Design" tabs
    if (isSourcePageFirst()) {
      createTab(m_sourcePage);
      createTab(m_designPage);
    } else {
      createTab(m_designPage);
      createTab(m_sourcePage);
    }
    // create additional pages
    for (IEditorPage page : m_additionalPages) {
      createTab(page);
    }
    // initially show Source
    showSource();
  }

  @Override
  void editorActivatedFirstTime() {
    if (!isSourcePageFirst()) {
      showDesign();
    }
  }

  @Override
  protected void handleShowPage() {
    super.handleShowPage();
    if (m_activePage == m_designPage) {
      maximizeOnActivation();
    }
  }

  private void maximizeOnActivation() {
    if (DesignerPlugin.getPreferences().getBoolean(IPreferenceConstants.P_EDITOR_MAX_DESIGN)) {
      DesignerPlugin.getStandardDisplay().asyncExec(new Runnable() {
        public void run() {
          IWorkbenchPage page = m_editor.getSite().getPage();
          if (!page.isPageZoomed()) {
            page.toggleZoom(page.getActivePartReference());
          }
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if "Source" page should be displayed first.
   */
  private boolean isSourcePageFirst() {
    int layout = DesignerPlugin.getPreferences().getInt(IPreferenceConstants.P_EDITOR_LAYOUT);
    return layout == IPreferenceConstants.V_EDITOR_LAYOUT_PAGES_SOURCE;
  }
}