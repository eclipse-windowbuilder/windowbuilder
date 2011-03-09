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
package org.eclipse.wb.gef.tree;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.gef.tree.tools.DoubleClickEditPartTracker;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.TreeItem;

import java.util.List;

/**
 * @author lobas_av
 * @coverage gef.tree
 */
public abstract class TreeEditPart extends EditPart {
  private TreeItem m_widget;
  private boolean m_expandedShouldRestore;
  private boolean m_expanded;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Widget
  //
  ////////////////////////////////////////////////////////////////////////////
  public TreeItem getWidget() {
    return m_widget;
  }

  public void setWidget(TreeItem widget) {
    m_widget = widget;
    //
    List<EditPart> children = getChildren();
    if (m_widget == null) {
      for (EditPart editPart : children) {
        TreeEditPart treePart = (TreeEditPart) editPart;
        treePart.setWidget(null);
      }
    } else {
      m_widget.setData(this);
      m_widget.addDisposeListener(new DisposeListener() {
        public void widgetDisposed(DisposeEvent e) {
          m_expandedShouldRestore = true;
          m_expanded = m_widget.getExpanded();
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // EditPart
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addChildVisual(EditPart childPart, int index) {
    TreeEditPart treePart = (TreeEditPart) childPart;
    treePart.setWidget(new TreeItem(getWidget(), SWT.NONE, index));
  }

  @Override
  protected void removeChildVisual(EditPart childPart) {
    TreeEditPart treePart = (TreeEditPart) childPart;
    if (treePart.getWidget() != null) {
      treePart.getWidget().dispose();
      treePart.setWidget(null);
    }
  }

  @Override
  protected void updateChildVisual(EditPart childPart, int index) {
    TreeEditPart treePart = (TreeEditPart) childPart;
    if (treePart.getWidget() == null) {
      treePart.setWidget(new TreeItem(getWidget(), SWT.NONE, index));
    }
  }

  @Override
  public void refresh() {
    super.refresh();
    if (m_expandedShouldRestore) {
      m_expandedShouldRestore = false;
      TreeItem widget = getWidget();
      if (widget != null && !widget.isDisposed()) {
        widget.setExpanded(m_expanded);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createEditPolicies() {
    //installEditPolicy("TreeToolAdapterEditPolicy", new TreeToolAdapterEditPolicy());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // DragTracking
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Tool getDragTrackerTool(Request request) {
    return new DoubleClickEditPartTracker(this);
  }
}