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
package org.eclipse.wb.internal.core.preferences.code;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.generation.GenerationDescription;
import org.eclipse.wb.internal.core.utils.binding.IDataEditor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import org.apache.commons.lang.ArrayUtils;

/**
 * Implementation of {@link IDataEditor} for {@link TabFolder} with {@link GenerationDescription}
 * elements on {@link TabItem}'s.
 *
 * @author scheglov_ke
 * @coverage core.preferences.ui
 */
public final class GenerationDescriptionEditor implements IDataEditor {
  private final TabFolder m_tabFolder;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GenerationDescriptionEditor(TabFolder tabFolder) {
    m_tabFolder = tabFolder;
    trackTabFolderSelection();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IDialogField
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setValue(Object value) {
    for (TabItem tabItem : m_tabFolder.getItems()) {
      if (tabItem.getData() == value) {
        m_tabFolder.setSelection(tabItem);
        updateTabFolderSelection();
      }
    }
  }

  public Object getValue() {
    return m_tabFolder.getSelection()[0].getData();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selected TabItem decoration
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final Image TAB_IMAGE_SELECTION_FALSE =
      DesignerPlugin.getImage("preferences/tab_selection_false.gif");
  private static final Image TAB_IMAGE_SELECTION_TRUE =
      DesignerPlugin.getImage("preferences/tab_selection_true.gif");

  /**
   * Installs {@link Listener} for highlighting selected {@link TabItem} in given {@link TabFolder}.
   */
  private void trackTabFolderSelection() {
    updateTabFolderSelection();
    m_tabFolder.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        updateTabFolderSelection();
      }
    });
  }

  /**
   * Updates images for {@link TabItem}'s of given {@link TabFolder} to highlight selected
   * {@link TabItem}.
   */
  private void updateTabFolderSelection() {
    TabItem[] selectedItems = m_tabFolder.getSelection();
    for (TabItem tabItem : m_tabFolder.getItems()) {
      tabItem.setImage(ArrayUtils.contains(selectedItems, tabItem)
          ? TAB_IMAGE_SELECTION_TRUE
          : TAB_IMAGE_SELECTION_FALSE);
    }
  }
}
