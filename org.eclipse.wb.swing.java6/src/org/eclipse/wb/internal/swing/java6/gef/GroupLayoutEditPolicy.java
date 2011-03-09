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
package org.eclipse.wb.internal.swing.java6.gef;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.actions.AbstractAlignmentActionsSupport;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.actions.ComplexAlignmentActionsSupport;
import org.eclipse.wb.internal.swing.gef.policy.layout.AbsoluteBasedLayoutEditPolicySwing;
import org.eclipse.wb.internal.swing.java6.model.GroupLayoutInfo;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import java.util.List;

/**
 * {@link LayoutEditPolicy} for {@link GroupLayoutInfo}.
 * 
 * @author mitin_aa
 * @coverage swing.gef.policy
 */
public final class GroupLayoutEditPolicy extends AbsoluteBasedLayoutEditPolicySwing {
  private final GroupLayoutInfo m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GroupLayoutEditPolicy(GroupLayoutInfo layout) {
    super(layout);
    m_layout = layout;
    createPlacementsSupport(m_layout);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Decorate Child
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void decorateChild(EditPart child) {
    Object model = child.getModel();
    if (model instanceof ComponentInfo) {
      child.installEditPolicy(EditPolicy.SELECTION_ROLE, new GroupSelectionEditPolicy(m_layout));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Coordinates
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Dimension getContainerSize() {
    return m_layout.getContainer().getModelBounds().getSize();
  }

  @Override
  public Point getClientAreaOffset() {
    return new Point(0, 0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Move
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void eraseSelectionFeedbacks() {
    super.eraseSelectionFeedbacks();
    for (EditPart child : getHost().getChildren()) {
      if (child.getModel() instanceof ComponentInfo) {
        GroupSelectionEditPolicy editPolicy =
            (GroupSelectionEditPolicy) child.getEditPolicy(EditPolicy.SELECTION_ROLE);
        editPolicy.hideSelection();
      }
    }
  }

  @Override
  protected void showSelectionFeedbacks() {
    super.showSelectionFeedbacks();
    for (EditPart child : getHost().getChildren()) {
      if (child.getModel() instanceof ComponentInfo
          && child.getSelected() != EditPart.SELECTED_NONE) {
        GroupSelectionEditPolicy editPolicy =
            (GroupSelectionEditPolicy) child.getEditPolicy(EditPolicy.SELECTION_ROLE);
        editPolicy.showSelection();
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Create
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command getCreateCommand(final CreateRequest request) {
    return new EditCommand(m_layout) {
      @Override
      protected void executeEdit() throws Exception {
        ComponentInfo component = (ComponentInfo) request.getNewObject();
        m_layout.command_CREATE(component, null);
        placementsSupport.commitAdd();
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Move
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command getMoveCommand(final ChangeBoundsRequest request) {
    return new EditCommand(m_layout) {
      @Override
      protected void executeEdit() throws Exception {
        placementsSupport.commit();
      }
    };
  }

  @Override
  protected Command getAddCommand(ChangeBoundsRequest request) {
    final List<EditPart> editParts = request.getEditParts();
    //
    return new EditCommand(m_layout) {
      @Override
      protected void executeEdit() throws Exception {
        for (EditPart editPart : editParts) {
          ComponentInfo component = (ComponentInfo) editPart.getModel();
          m_layout.command_MOVE(component, null);
        }
        placementsSupport.commitAdd();
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Resize
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command getResizeCommand(ChangeBoundsRequest request) {
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Paste
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void doPasteComponent(Point pasteLocation, PastedComponentInfo pastedWidget)
      throws Exception {
    ComponentInfo control = (ComponentInfo) pastedWidget.getComponent();
    m_layout.command_CREATE(control, null);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection Actions
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected AbstractAlignmentActionsSupport<ComponentInfo> getAlignmentActionsSupport() {
    return new ComplexAlignmentActionsSupport<ComponentInfo>(placementsSupport) {
      @Override
      protected boolean isComponentInfo(ObjectInfo object) {
        return object instanceof ComponentInfo;
      }

      @Override
      protected AbstractComponentInfo getLayoutContainer() {
        return m_layout.getContainer();
      }
    };
  }
}
