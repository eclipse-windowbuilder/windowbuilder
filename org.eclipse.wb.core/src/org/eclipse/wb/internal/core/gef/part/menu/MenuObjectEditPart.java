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
package org.eclipse.wb.internal.core.gef.part.menu;

import org.eclipse.wb.core.gef.part.menu.IMenuObjectEditPart;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.requests.DragPermissionRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.model.menu.IMenuObjectInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuObjectListener;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.gef.core.EditPartVisitor;
import org.eclipse.wb.internal.gef.core.IActiveToolListener;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import java.util.List;

/**
 * {@link EditPart} for any {@link IMenuObjectInfo}.
 *
 * @author mitin_aa
 * @author scheglov_ke
 * @coverage core.gef.menu
 */
public abstract class MenuObjectEditPart extends GraphicalEditPart implements IMenuObjectEditPart {
  private final IMenuObjectInfo m_object;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MenuObjectEditPart(Object toolkitModel, IMenuObjectInfo menuModel) {
    setModel(toolkitModel);
    m_object = menuModel;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Model
  //
  ////////////////////////////////////////////////////////////////////////////
  public IMenuObjectInfo getMenuModel() {
    return m_object;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void activate() {
    super.activate();
    addListeners();
  }

  @Override
  public void deactivate() {
    removeListeners();
    super.deactivate();
  }

  @Override
  public void removeNotify() {
    // usually GEF considers that child edit parts figures are
    // children of parent edit part figure, but we add submenu's to
    // popup layer, so we have to notify children to remove their figures.
    for (EditPart child : getChildren()) {
      removeChildVisual(child);
    }
    super.removeNotify();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Listeners
  //
  ////////////////////////////////////////////////////////////////////////////
  private ISelectionChangedListener m_selectionListener;
  private IActiveToolListener m_activeToolListener;
  private IMenuObjectListener m_objectListener;

  private void addListeners() {
    if (isRootMenuEditPart()) {
      createListeners();
      getViewer().addSelectionChangedListener(m_selectionListener);
      getViewer().getEditDomain().addActiveToolListener(m_activeToolListener);
      m_object.addListener(m_objectListener);
    }
  }

  private void removeListeners() {
    if (isRootMenuEditPart()) {
      getViewer().removeSelectionChangedListener(m_selectionListener);
      getViewer().getEditDomain().removeActiveToolListener(m_activeToolListener);
      m_object.removeListener(m_objectListener);
    }
  }

  private boolean isRootMenuEditPart() {
    return !(getParent() instanceof MenuObjectEditPart);
  }

  /**
   * Creates listeners for viewer and model.
   */
  private void createListeners() {
    if (m_selectionListener != null) {
      return;
    }
    // selection
    m_selectionListener = new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        refresh();
      }
    };
    // active tool
    m_activeToolListener = new IActiveToolListener() {
      public void toolActivated(Tool tool) {
        // when new Tool loaded, so for example we don't create new Item anymore,
        // we should hide any temporary displayed sub-menus
        refresh();
      }
    };
    // model listener
    m_objectListener = new IMenuObjectListener() {
      private EditPart m_pendingSelection;

      public void refresh() {
        if (m_pendingSelection != null) {
          getViewer().select(m_pendingSelection);
          m_pendingSelection = null;
        } else {
          MenuObjectEditPart.this.refresh();
        }
      }

      public void deleting(Object toolkitModel) {
        EditPart objectPart = getViewer().getEditPartByModel(toolkitModel);
        if (objectPart != null) {
          EditPart parentPart = objectPart.getParent();
          List<EditPart> siblings = parentPart.getChildren();
          int index = siblings.indexOf(objectPart);
          // move selection on sibling or parent item
          if (siblings.size() == 1) {
            m_pendingSelection = parentPart;
            if (m_pendingSelection instanceof MenuEditPart
                || m_pendingSelection instanceof MenuPopupEditPart) {
              m_pendingSelection = m_pendingSelection.getParent();
            }
          } else if (index == 0) {
            m_pendingSelection = siblings.get(index + 1);
          } else {
            m_pendingSelection = siblings.get(index - 1);
          }
        }
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public EditPart getTargetEditPart(Request request) {
    request = processRequestProcessors(request);
    EditPart target = super.getTargetEditPart(request);
    boolean isOperationRequest =
        request.getType() == Request.REQ_CREATE
            || request.getType() == Request.REQ_PASTE
            || request.getType() == Request.REQ_ADD;
    if (target == this && isOperationRequest) {
      // Refresh _all_ root MenuObjectEditPart's to close any previously shown drop-downs.
      // Do this in "async" to don't break normal GEF life cycle.
      AsyncExecutor.schedule(new Runnable() {
        public void run() {
          try {
            MenuObjectInfoUtils.m_selectingObject = m_object;
            getViewer().getRootEditPart().accept(new EditPartVisitor() {
              @Override
              public boolean visit(EditPart editPart) {
                if (editPart instanceof MenuObjectEditPart) {
                  editPart.refresh();
                  return false;
                }
                return true;
              }
            });
          } finally {
            MenuObjectInfoUtils.m_selectingObject = null;
          }
        }
      });
    }
    return target;
  }

  @Override
  public void refresh() {
    removeListeners();
    try {
      super.refresh();
    } finally {
      addListeners();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Requests
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void performRequest(Request request) {
    super.performRequest(request);
    if (request instanceof DragPermissionRequest) {
      DragPermissionRequest permissionRequest = (DragPermissionRequest) request;
      permissionRequest.setMove(m_object.canMove());
      permissionRequest.setReparent(m_object.canReparent());
    }
  }
}
