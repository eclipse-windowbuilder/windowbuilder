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
package org.eclipse.wb.internal.swt.gef.policy.layout;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.AbsoluteLayoutSelectionEditPolicy;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.actions.AbstractAlignmentActionsSupport;
import org.eclipse.wb.internal.core.gef.policy.snapping.IAbsoluteLayoutCommands;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.swt.model.layout.absolute.IAbsoluteLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.absolute.SelectionActionsSupport;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import java.util.List;

/**
 * Implementation of {@link LayoutEditPolicy} for absolute (null) layout.
 *
 * @author mitin_aa
 * @author lobas_av
 * @coverage swt.gef.policy
 */
public final class AbsoluteLayoutEditPolicy<C extends IControlInfo>
    extends
      AbsoluteBasedLayoutEditPolicySWT<C> {
  private final IAbsoluteLayoutInfo<C> m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbsoluteLayoutEditPolicy(IAbsoluteLayoutInfo<C> layout) {
    super(layout);
    m_layout = layout;
    createPlacementsSupport(IAbsoluteLayoutCommands.EMPTY);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command getMoveCommand(ChangeBoundsRequest request) {
    final List<EditPart> editParts = request.getEditParts();
    return new EditCommand(m_layout) {
      @SuppressWarnings("unchecked")
      @Override
      protected void executeEdit() throws Exception {
        List<C> models = Lists.newArrayList();
        for (EditPart editPart : editParts) {
          models.add((C) editPart.getModel());
        }
        placementsSupport.commit();
        for (C widget : models) {
          Rectangle bounds = widget.getModelBounds();
          m_layout.commandChangeBounds(widget, bounds.getLocation(), null);
        }
      }
    };
  }

  @Override
  protected Command getAddCommand(ChangeBoundsRequest request) {
    final List<EditPart> editParts = request.getEditParts();
    //
    return new EditCommand(m_layout) {
      @SuppressWarnings("unchecked")
      @Override
      protected void executeEdit() throws Exception {
        List<C> models = Lists.newArrayList();
        for (EditPart editPart : editParts) {
          models.add((C) editPart.getModel());
        }
        placementsSupport.commitAdd();
        for (C widget : models) {
          Rectangle bounds = widget.getModelBounds();
          m_layout.commandMove(widget, null);
          m_layout.commandChangeBounds(
              widget,
              bounds.getLocation(),
              widget.getModelBounds().getSize());
        }
      }
    };
  }

  @SuppressWarnings("unchecked")
  @Override
  protected Command getCreateCommand(CreateRequest request) {
    final C control = (C) request.getNewObject();
    return new EditCommand(m_layout) {
      @Override
      protected void executeEdit() throws Exception {
        placementsSupport.commitAdd();
        Rectangle bounds = control.getModelBounds();
        m_layout.commandCreate(control, null);
        m_layout.commandChangeBounds(control, bounds.getLocation(), bounds.getSize());
      }
    };
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void doPasteComponent(Point pasteLocation, PastedComponentInfo pastedWidget)
      throws Exception {
    C control = (C) pastedWidget.getComponent();
    // create
    m_layout.commandCreate(control, null);
    // set bounds
    Point relativeLocation = pastedWidget.getBounds().getLocation();
    m_layout.commandChangeBounds(
        control,
        pasteLocation.getTranslated(relativeLocation),
        pastedWidget.getBounds().getSize());
  }

  @Override
  protected Command getResizeCommand(final ChangeBoundsRequest request) {
    return new EditCommand(m_layout) {
      @SuppressWarnings("unchecked")
      @Override
      protected void executeEdit() throws Exception {
        for (EditPart editPart : request.getEditParts()) {
          C widget = (C) editPart.getModel();
          Rectangle bounds = widget.getModelBounds();
          m_layout.commandChangeBounds(widget, bounds.getLocation(), bounds.getSize());
        }
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Decorate Child
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void decorateChild(EditPart child) {
    if (m_layout.getControls().contains(child.getModel())) {
      child.installEditPolicy(EditPolicy.SELECTION_ROLE, new AbsoluteLayoutSelectionEditPolicy());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection actions
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected AbstractAlignmentActionsSupport<C> getAlignmentActionsSupport() {
    return new SelectionActionsSupport<C>(m_layout);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Misc
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ToolkitDescription getToolkit() {
    return GlobalState.getToolkit();
  }
}