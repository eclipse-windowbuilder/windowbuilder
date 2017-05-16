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
package org.eclipse.wb.internal.gef.core;

import com.google.common.collect.Lists;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.internal.core.utils.ui.MenuIntersector;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;

import java.util.List;

/**
 * {@link ContextMenuProvider} with supporting merge menu action's for multi selection.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public abstract class MultiSelectionContextMenuProvider extends ContextMenuProvider {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MultiSelectionContextMenuProvider(IEditPartViewer viewer) {
    super(viewer);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Context Menu
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected final void buildContextMenu() {
    List<EditPart> editParts = m_viewer.getSelectedEditParts();
    preprocessSelection(editParts);
    // check empty
    if (editParts.isEmpty()) {
      return;
    }
    // check single selection
    if (editParts.size() == 1) {
      EditPart editPart = editParts.get(0);
      buildContextMenu(editPart, this);
      return;
    }
    // handle multi selection
    List<IMenuManager> managers = Lists.newArrayList();
    for (EditPart editPart : editParts) {
      IMenuManager manager = new MenuManager();
      buildContextMenu(editPart, manager);
      managers.add(manager);
    }
    // select common parts
    MenuIntersector.merge(this, managers);
  }

  /**
   * Notifies that given {@link EditPart}'s are selected and we are going to call later
   * {@link #buildContextMenu(EditPart, IMenuManager)} for each of them.
   */
  protected void preprocessSelection(List<EditPart> editParts) {
  }

  /**
   * Create menu items for given {@link EditPart} and fill given <code>menu</code>.
   */
  protected abstract void buildContextMenu(EditPart editPart, IMenuManager manager);
}