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

import org.eclipse.swt.widgets.Composite;

/**
 * Implementation of {@link MultiMode} for pages mode.
 * 
 * @author sablin_aa
 * @coverage CSS.editor
 */
final class MultiPagesMode extends DefaultMultiMode {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MultiPagesMode(MultiPageEditor editor) {
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
    if (m_activePage == m_sourcePage) {
      showPage(m_designPage);
    } else {
      showPage(m_sourcePage);
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
    // initially show Source
    showSource();
  }

  @Override
  void editorActivatedFirstTime() {
    if (!isSourcePageFirst()) {
      showDesign();
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
    return true;
  }
}