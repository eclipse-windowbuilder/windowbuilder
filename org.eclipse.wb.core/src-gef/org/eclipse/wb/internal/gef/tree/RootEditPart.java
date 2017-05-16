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
package org.eclipse.wb.internal.gef.tree;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.tree.TreeEditPart;
import org.eclipse.wb.internal.gef.core.IRootContainer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 *
 * @author lobas_av
 * @coverage gef.tree
 */
class RootEditPart extends TreeEditPart implements IRootContainer {
  private final IEditPartViewer m_viewer;
  private TreeEditPart m_contentEditPart;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RootEditPart(IEditPartViewer viewer) {
    m_viewer = viewer;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // EditPart
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the root's {@link EditPartViewer}.
   */
  @Override
  public IEditPartViewer getViewer() {
    return m_viewer;
  }

  @Override
  protected void addChildVisual(EditPart childPart, int index) {
    m_contentEditPart.setWidget(new TreeItem(getTreeControl(), SWT.NONE));
  }

  private Tree getTreeControl() {
    return (Tree) m_viewer.getControl();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IRootEditPart
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the <i>content</i> {@link EditPart}.
   */
  public EditPart getContent() {
    return m_contentEditPart;
  }

  /**
   * Sets the <i>content</i> {@link EditPart}. A IRootEditPart only has a single child, called its
   * <i>contents</i>.
   */
  public void setContent(EditPart contentEditPart) {
    if (m_contentEditPart != null) {
      // remove content
      removeChild(m_contentEditPart);
    }
    //
    m_contentEditPart = (TreeEditPart) contentEditPart;
    //
    if (m_contentEditPart != null) {
      addChild(m_contentEditPart, -1);
    }
  }
}