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
package org.eclipse.wb.internal.swing.gef.policy.layout;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.draw2d.geometry.Translatable;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.actions.AbstractAlignmentActionsSupport;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.actions.ComplexAlignmentActionsSupport;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.spring.SpringLayoutInfo;

import java.util.List;

/**
 * {@link LayoutEditPolicy} for {@link SpringLayoutInfo}.
 * 
 * @author mitin_aa
 * @author scheglov_ke
 * @coverage swing.gef.policy
 */
public final class SpringLayoutEditPolicy extends AbsoluteBasedLayoutEditPolicySwing {
  private final SpringLayoutInfo m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SpringLayoutEditPolicy(SpringLayoutInfo layout) {
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
      child.installEditPolicy(EditPolicy.SELECTION_ROLE, new SpringSelectionEditPolicy(m_layout));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Coordinates
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Dimension getContainerSize() {
    // TODO check this
    /*ScrollableInfo composite = m_layout.getComposite();
    return composite.getClientArea().getSize();*/
    ContainerInfo container = m_layout.getContainer();
    Rectangle bounds = container.getModelBounds().getCopy();
    bounds.crop(container.getInsets());
    return bounds.getSize();
  }

  @Override
  public Point getClientAreaOffset() {
    // TODO check this
    /*return m_layout.getContainer().getClientArea().getLocation();*/
    //return new Point(0, 0);
    Insets insets = m_layout.getContainer().getInsets();
    return new Point(insets.left, insets.top);
  }

  @Override
  protected void translateAbsoluteToModel(Translatable t) {
    super.translateAbsoluteToModel(t);
    t.translate(getClientAreaOffset().getNegated());
  }

  @Override
  protected void translateModelToFeedback(Translatable t) {
    super.translateModelToFeedback(t);
    t.translate(getClientAreaOffset());
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
        SpringSelectionEditPolicy editPolicy =
            (SpringSelectionEditPolicy) child.getEditPolicy(EditPolicy.SELECTION_ROLE);
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
        SpringSelectionEditPolicy editPolicy =
            (SpringSelectionEditPolicy) child.getEditPolicy(EditPolicy.SELECTION_ROLE);
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
          m_layout.command_ADD(component, null);
        }
        placementsSupport.commitAdd();
      }
    };
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
