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

import com.google.common.collect.Lists;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.AbsoluteBasedLayoutEditPolicy;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.AbsoluteLayoutSelectionEditPolicy;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.actions.AbstractAlignmentActionsSupport;
import org.eclipse.wb.internal.core.gef.policy.snapping.IAbsoluteLayoutCommands;
import org.eclipse.wb.internal.core.laf.BaselineSupportHelper;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.swing.ToolkitProvider;
import org.eclipse.wb.internal.swing.gef.ComponentsLayoutRequestValidator;
import org.eclipse.wb.internal.swing.laf.ILayoutStyleSupport;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.layout.absolute.AbstractAbsoluteLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.absolute.IPreferenceConstants;
import org.eclipse.wb.internal.swing.model.layout.absolute.SelectionActionsSupport;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.UIManager;

/**
 * Implementation of {@link LayoutEditPolicy} for SWING absolute (null) layout.
 * 
 * @author mitin_aa
 * @coverage swing.gef.policy
 */
public final class AbsoluteLayoutEditPolicy extends AbsoluteBasedLayoutEditPolicy<ComponentInfo> {
  private final AbstractAbsoluteLayoutInfo m_layout;
  private final ILayoutStyleSupport m_layoutStyleSupport =
      ExternalFactoriesHelper.getElementsInstances(
          ILayoutStyleSupport.class,
          ILayoutStyleSupport.LAYOUT_STYLE_POINT,
          "support").get(0);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbsoluteLayoutEditPolicy(AbstractAbsoluteLayoutInfo layout) {
    super(layout);
    m_layout = layout;
    m_layoutStyleSupport.setLayoutStyle(UIManager.getLookAndFeel());
    createPlacementsSupport(IAbsoluteLayoutCommands.EMPTY);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Requests
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return ComponentsLayoutRequestValidator.INSTANCE;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection Policy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void decorateChild(EditPart child) {
    Object childModel = child.getModel();
    if (childModel instanceof ComponentInfo) {
      EditPolicy policy = new AbsoluteLayoutSelectionEditPolicy<ComponentInfo>();
      child.installEditPolicy(EditPolicy.SELECTION_ROLE, policy);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IVisualDataProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public List<ComponentInfo> getAllComponents() {
    List<ComponentInfo> components = m_layout.getContainer().getChildrenComponents();
    return Lists.newArrayList(components);
  }

  public int getBaseline(IAbstractComponentInfo component) {
    return BaselineSupportHelper.getBaseline(((ComponentInfo) component).getComponent());
  }

  public Dimension getComponentPreferredSize(IAbstractComponentInfo component) {
    ComponentInfo componentInfo = (ComponentInfo) component;
    return componentInfo.getPreferredSize();
  }

  @Override
  public int getComponentGapValue(IAbstractComponentInfo component1,
      IAbstractComponentInfo component2,
      int direction) {
    ComponentInfo swingComponentInfo1 = (ComponentInfo) component1;
    ComponentInfo swingComponentInfo2 = (ComponentInfo) component2;
    // don't use LS if disabled or one of components are not instance of JComponent
    if (!useLayoutStyle()
        || !(swingComponentInfo1.getComponent() instanceof JComponent)
        || !(swingComponentInfo2.getComponent() instanceof JComponent)) {
      return super.getComponentGapValue(component1, component2, direction);
    } else {
      JComponent componentObject1 = (JComponent) swingComponentInfo1.getComponent();
      JComponent componentObject2 = (JComponent) swingComponentInfo2.getComponent();
      return m_layoutStyleSupport.getPreferredGap(componentObject1, componentObject2, 1, // use UNRELATED for AbsoluteLayout 
          direction,
          componentObject1.getParent());
    }
  }

  @Override
  public int getContainerGapValue(IAbstractComponentInfo component, int direction) {
    ComponentInfo swingComponentInfo = (ComponentInfo) component;
    // don't use LS if disabled or component is not instance of JComponent
    if (!useLayoutStyle() || !(swingComponentInfo.getComponent() instanceof JComponent)) {
      return super.getContainerGapValue(component, direction);
    } else {
      JComponent jcomponentObject = (JComponent) swingComponentInfo.getComponent();
      return m_layoutStyleSupport.getContainerGap(
          jcomponentObject,
          direction,
          jcomponentObject.getParent());
    }
  }

  public Dimension getContainerSize() {
    // TODO: insets?
    return m_layout.getContainer().getModelBounds().getSize();
  }

  protected final boolean useLayoutStyle() {
    return getPreferenceStore().getBoolean(IPreferenceConstants.P_USE_JDK_LAYOUT_STYLE);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Paste
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void doPasteComponent(Point pasteLocation, PastedComponentInfo pastedWidget)
      throws Exception {
    ComponentInfo component = (ComponentInfo) pastedWidget.getComponent();
    // create
    Point location = pasteLocation.getTranslated(pastedWidget.getBounds().getLocation());
    Dimension size = pastedWidget.getBounds().getSize();
    m_layout.command_CREATE(component, null);
    m_layout.command_BOUNDS(component, location, size);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command getMoveCommand(ChangeBoundsRequest request) {
    final List<EditPart> editParts = request.getEditParts();
    //
    return new EditCommand(m_layout) {
      @Override
      protected void executeEdit() throws Exception {
        ArrayList<IAbstractComponentInfo> models = Lists.newArrayList();
        for (EditPart editPart : editParts) {
          models.add((IAbstractComponentInfo) editPart.getModel());
        }
        placementsSupport.commit();
        for (IAbstractComponentInfo widget : models) {
          Rectangle bounds = widget.getModelBounds();
          m_layout.command_BOUNDS((ComponentInfo) widget, bounds.getLocation(), null);
        }
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
        ArrayList<IAbstractComponentInfo> models = Lists.newArrayList();
        for (EditPart editPart : editParts) {
          models.add((IAbstractComponentInfo) editPart.getModel());
        }
        placementsSupport.commitAdd();
        for (IAbstractComponentInfo widget : models) {
          ComponentInfo component = (ComponentInfo) widget;
          Rectangle bounds = widget.getModelBounds();
          m_layout.command_MOVE(component, null);
          m_layout.command_BOUNDS(
              component,
              bounds.getLocation(),
              widget.getModelBounds().getSize());
        }
      }
    };
  }

  @Override
  protected Command getCreateCommand(CreateRequest request) {
    final ComponentInfo component = (ComponentInfo) request.getNewObject();
    return new EditCommand(m_layout) {
      @Override
      protected void executeEdit() throws Exception {
        placementsSupport.commitAdd();
        Rectangle bounds = component.getModelBounds();
        m_layout.command_CREATE(component, null);
        m_layout.command_BOUNDS(component, bounds.getLocation(), bounds.getSize());
      }
    };
  }

  @Override
  protected Command getResizeCommand(final ChangeBoundsRequest request) {
    return new EditCommand(m_layout) {
      @Override
      protected void executeEdit() throws Exception {
        for (EditPart editPart : request.getEditParts()) {
          IAbstractComponentInfo widget = (IAbstractComponentInfo) editPart.getModel();
          Rectangle bounds = widget.getModelBounds();
          m_layout.command_BOUNDS((ComponentInfo) widget, bounds.getLocation(), bounds.getSize());
        }
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection Actions
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected AbstractAlignmentActionsSupport<ComponentInfo> getAlignmentActionsSupport() {
    return new SelectionActionsSupport(m_layout);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Misc 
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ToolkitDescription getToolkit() {
    return ToolkitProvider.DESCRIPTION;
  }
}
