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
package org.eclipse.wb.internal.core.gef.tools;

import org.eclipse.wb.core.gef.policy.TabOrderContainerEditPolicy;
import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.draw2d.ICursorConstants;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.core.tools.TargetingTool;
import org.eclipse.wb.internal.core.DesignerPlugin;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;

import java.util.List;

/**
 * Tool allowing reordering container children.
 *
 * @author scheglov_ke
 * @author lobas_av
 * @coverage core.gef
 */
public final class TabOrderTool extends TargetingTool {
  private final TabOrderContainerRequest m_containerRequest =
      new TabOrderContainerRequest(TabOrderContainerEditPolicy.REQ_CONTAINER_TAB_ORDER);
  private final IEditPartViewer m_viewer;
  private EditPolicy m_containerPolicy;
  private int m_currentIndex;
  private boolean m_saveTabOrder;
  private boolean m_changingContainer;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TabOrderTool(EditPart part) {
    setDefaultCursor(ICursorConstants.ARROW);
    m_viewer = part.getViewer();
    m_viewer.addSelectionChangedListener(m_selectionListener);
    m_containerPolicy = getContainerRole(part);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tool
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void activate() {
    super.activate();
    activateTabContainerPolicy();
  }

  @Override
  public void deactivate() {
    super.deactivate();
    m_viewer.removeSelectionChangedListener(m_selectionListener);
    if (m_viewer.getControl() != null) {
      deactivateTabContainerPolicy();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Request
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Request getTargetRequest() {
    return TabOrderContainerEditPolicy.TAB_ORDER_REQUEST;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection
  //
  ////////////////////////////////////////////////////////////////////////////
  private final ISelectionChangedListener m_selectionListener = new ISelectionChangedListener() {
    public void selectionChanged(SelectionChangedEvent event) {
      // we can receive selection change event during changing to new container, ignore it
      if (m_changingContainer) {
        return;
      }
      //
      List<EditPart> selectedParts = m_viewer.getSelectedEditParts();
      if (selectedParts.size() == 1) {
        if (m_containerPolicy != null) {
          deactivateTabContainerPolicy();
        }
        activateTabContainerPolicy(selectedParts.get(0));
      } else {
        m_containerPolicy = null;
      }
    }
  };

  ////////////////////////////////////////////////////////////////////////////
  //
  // High-Level handle MouseEvent
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void handleButtonDown(int button) {
    if (m_containerPolicy != null
        && m_containerRequest.getPossibleChildren() != null
        && m_containerRequest.getChildren() != null) {
      updateTargetUnderMouse();
      EditPart editPart = getTargetEditPart();
      if (editPart != null) {
        AbstractComponentInfo model = (AbstractComponentInfo) editPart.getModel();
        List<AbstractComponentInfo> children = m_containerRequest.getChildren();
        List<AbstractComponentInfo> possibleChildren = m_containerRequest.getPossibleChildren();
        if (possibleChildren.contains(model)) {
          if (children.contains(model)) {
            if (DesignerPlugin.isCtrlPressed()) {
              // mark current child as selected
              m_containerRequest.setSelectedChild(editPart.getModel());
            } else if (DesignerPlugin.isShiftPressed()) {
              children.remove(model);
              if (m_containerRequest.getSelectedChild() == model) {
                m_containerRequest.setSelectedChild(null);
              }
              m_saveTabOrder = true;
            } else if (m_containerRequest.getSelectedChild() != null) {
              // prepare index of selected child
              Object selectedChild = m_containerRequest.getSelectedChild();
              int selectedIndex = children.indexOf(selectedChild);
              m_containerRequest.setSelectedChild(null);
              // move clicked child to selected index
              children.remove(model);
              children.add(selectedIndex, model);
              m_saveTabOrder = true;
            } else {
              // move clicked child to current index and move index forward
              children.remove(model);
              children.add(m_currentIndex++, model);
              m_currentIndex = Math.min(m_currentIndex, children.size() - 1);
              m_saveTabOrder = true;
            }
            //
            m_containerPolicy.showTargetFeedback(m_containerRequest);
          } else if (DesignerPlugin.isShiftPressed()) {
            children.add(model);
            m_containerPolicy.showTargetFeedback(m_containerRequest);
          }
        } else {
          // this was click on EditPart outside of active container
          EditPart activateOnPart = null;
          // find, may be we click on new container or child on this container
          if (hasContainerRole(editPart)) {
            activateOnPart = editPart;
          } else if (hasContainerRole(editPart.getParent())) {
            activateOnPart = editPart.getParent();
          }
          // activate tool for new container
          if (activateOnPart != null) {
            try {
              m_changingContainer = true;
              deactivateTabContainerPolicy();
              activateTabContainerPolicy(activateOnPart);
            } finally {
              m_changingContainer = false;
            }
          }
        }
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handling Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void updateTargetUnderMouse() {
    // find on clickable layer
    EditPart editPart =
        getViewer().findTargetEditPart(
            m_currentScreenX,
            m_currentScreenY,
            getExclusionSet(),
            getTargetingConditional(),
            IEditPartViewer.CLICKABLE_LAYER);
    // common find target part
    if (editPart == null) {
      editPart =
          getViewer().findTargetEditPart(
              m_currentScreenX,
              m_currentScreenY,
              getExclusionSet(),
              getTargetingConditional());
    }
    if (editPart != null) {
      editPart = editPart.getTargetEditPart(getTargetRequest());
    }
    setTargetEditPart(editPart);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handle KeyEvent
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void keyPressed(KeyEvent event, IEditPartViewer viewer) {
    if (event.keyCode == SWT.ESC) {
      viewer.getEditDomain().loadDefaultTool();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private void activateTabContainerPolicy(EditPart editPart) {
    m_containerPolicy = getContainerRole(editPart);
    activateTabContainerPolicy();
  }

  private void activateTabContainerPolicy() {
    if (isActive() && m_containerPolicy != null) {
      m_containerRequest.setChildren(null);
      m_containerPolicy.showTargetFeedback(m_containerRequest);
    }
    m_currentIndex = 0;
    m_saveTabOrder = false;
  }

  private void deactivateTabContainerPolicy() {
    if (m_containerPolicy != null) {
      m_containerPolicy.eraseTargetFeedback(m_containerRequest);
      if (m_saveTabOrder && m_containerRequest.getCommand() != null) {
        getDomain().executeCommand(m_containerRequest.getCommand());
      }
    }
  }

  private static EditPolicy getContainerRole(EditPart part) {
    if (part != null) {
      EditPolicy policy = part.getEditPolicy(TabOrderContainerEditPolicy.TAB_CONTAINER_ROLE);
      if (policy != null) {
        return policy;
      }
      return getContainerRole(part.getParent());
    }
    return null;
  }

  public static boolean hasContainerRole(EditPart part) {
    return getContainerRole(part) != null;
  }
}