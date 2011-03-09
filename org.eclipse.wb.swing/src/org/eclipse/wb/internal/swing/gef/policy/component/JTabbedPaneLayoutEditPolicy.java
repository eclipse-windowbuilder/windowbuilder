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

import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.swing.gef.part.JTabbedPaneTabEditPart;
import org.eclipse.wb.internal.swing.gef.policy.ComponentFlowLayoutEditPolicy;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.JTabbedPaneInfo;
import org.eclipse.wb.internal.swing.model.component.JTabbedPaneTabInfo;

/**
 * Implementation of {@link LayoutEditPolicy} for {@link JTabbedPaneInfo} manipulating
 * {@link ComponentInfo}.
 * 
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage swing.gef.policy
 */
public final class JTabbedPaneLayoutEditPolicy extends ComponentFlowLayoutEditPolicy {
  private final JTabbedPaneInfo m_pane;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JTabbedPaneLayoutEditPolicy(JTabbedPaneInfo component) {
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
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command getMoveCommand(Object moveObject, Object referenceObject) {
    final ComponentInfo component = getComponent(moveObject);
    if (component == null || !component.getCreationSupport().canReorder()) {
      return null;
    }
    return super.getMoveCommand(moveObject, referenceObject);
  }

  @Override
  protected Command getAddCommand(Object addObject, Object referenceObject) {
    final ComponentInfo component = getComponent(addObject);
    if (component == null || !component.getCreationSupport().canReparent()) {
      return null;
    }
    return super.getAddCommand(addObject, referenceObject);
  }

  @Override
  protected void command_CREATE(ComponentInfo newObject, ComponentInfo referenceObject)
      throws Exception {
    m_pane.command_CREATE(newObject, referenceObject);
  }

  @Override
  protected void command_MOVE(ComponentInfo object, ComponentInfo referenceObject) throws Exception {
    m_pane.command_MOVE(object, referenceObject);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ComponentInfo getObjectModel(Object object) {
    return getComponent(object);
  };

  @Override
  protected ComponentInfo getReferenceObjectModel(Object referenceObject) {
    return getComponent(referenceObject);
  }

  /**
   * @return the {@link ComponentInfo} for given {@link JTabbedPaneTabInfo} object.
   */
  private static ComponentInfo getComponent(Object o) {
    if (o instanceof ComponentInfo) {
      return (ComponentInfo) o;
    }
    return o != null ? ((JTabbedPaneTabInfo) o).getComponent() : null;
  }
}
