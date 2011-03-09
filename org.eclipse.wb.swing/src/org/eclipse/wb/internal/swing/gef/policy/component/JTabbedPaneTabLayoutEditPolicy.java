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
package org.eclipse.wb.internal.swing.gef.policy.component;

import org.eclipse.wb.core.gef.policy.validator.LayoutRequestValidators;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.swing.gef.part.JTabbedPaneTabEditPart;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.JTabbedPaneInfo;
import org.eclipse.wb.internal.swing.model.component.JTabbedPaneTabInfo;

/**
 * Implementation of {@link LayoutEditPolicy} for {@link JTabbedPaneInfo} manipulating
 * {@link JTabbedPaneTabInfo}.
 * 
 * @author sablin_aa
 * @coverage swing.gef.policy
 */
public final class JTabbedPaneTabLayoutEditPolicy
    extends
      org.eclipse.wb.core.gef.policy.layout.flow.ObjectFlowLayoutEditPolicy<JTabbedPaneTabInfo> {
  private final JTabbedPaneInfo m_pane;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JTabbedPaneTabLayoutEditPolicy(JTabbedPaneInfo component) {
    super(component);
    m_pane = component;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean isHorizontal(Request request) {
    return m_pane.isHorizontal();
  }

  @Override
  protected boolean isGoodReferenceChild(Request request, EditPart editPart) {
    return editPart instanceof JTabbedPaneTabEditPart;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Requests
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final ILayoutRequestValidator INSTANCE =
      LayoutRequestValidators.modelType(JTabbedPaneTabInfo.class);

  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return INSTANCE;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command getCreateCommand(Object newObject, Object referenceObject) {
    return null;
  }

  @Override
  protected Command getMoveCommand(Object moveObject, Object referenceObject) {
    return getComponent(moveObject) != null
        ? super.getMoveCommand(moveObject, referenceObject)
        : null;
  }

  @Override
  protected Command getAddCommand(Object addObject, Object referenceObject) {
    return getComponent(addObject) != null ? super.getAddCommand(addObject, referenceObject) : null;
  }

  @Override
  protected void command_CREATE(JTabbedPaneTabInfo newObject, JTabbedPaneTabInfo referenceObject)
      throws Exception {
  }

  @Override
  protected void command_MOVE(JTabbedPaneTabInfo object, JTabbedPaneTabInfo referenceObject)
      throws Exception {
    ComponentInfo component = object.getComponent();
    m_pane.command_MOVE(component, referenceObject != null ? referenceObject.getComponent() : null);
    m_pane.setActiveComponent(component);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected JTabbedPaneTabInfo getObjectModel(Object object) {
    return getComponent(object);
  };

  @Override
  protected JTabbedPaneTabInfo getReferenceObjectModel(Object referenceObject) {
    return getComponent(referenceObject);
  }

  /**
   * @return the {@link ComponentInfo} for given {@link JTabbedPaneTabInfo} object.
   */
  private static JTabbedPaneTabInfo getComponent(Object o) {
    return o instanceof JTabbedPaneTabInfo ? (JTabbedPaneTabInfo) o : null;
  }
}
