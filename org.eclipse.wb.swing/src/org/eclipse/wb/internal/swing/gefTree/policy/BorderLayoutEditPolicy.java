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
package org.eclipse.wb.internal.swing.gefTree.policy;

import org.eclipse.wb.core.gefTree.policy.ObjectLayoutEditPolicy;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.swing.gef.ComponentsLayoutRequestValidator;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.layout.BorderLayoutInfo;

import java.util.List;

/**
 * Implementation of {@link LayoutEditPolicy} for {@link BorderLayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.gefTree.policy
 */
public final class BorderLayoutEditPolicy extends ObjectLayoutEditPolicy<ComponentInfo> {
  private final BorderLayoutInfo m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BorderLayoutEditPolicy(BorderLayoutInfo layout) {
    super(layout);
    m_layout = layout;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Requests
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean isGoodReferenceChild(Request request, EditPart editPart) {
    return editPart.getModel() instanceof ComponentInfo;
  }

  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return ComponentsLayoutRequestValidator.INSTANCE;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command getCreateCommand(Object newObject, Object referenceObject) {
    if (m_layout.getEmptyRegion() == null) {
      return null;
    }
    return super.getCreateCommand(newObject, referenceObject);
  }

  @Override
  protected Command getAddCommand(List<EditPart> addParts, Object referenceObject) {
    if (m_layout.getEmptyRegion() == null) {
      return null;
    }
    return super.getAddCommand(addParts, referenceObject);
  }

  @Override
  protected void command_CREATE(ComponentInfo component, ComponentInfo reference) throws Exception {
    String region = m_layout.getEmptyRegion();
    m_layout.command_CREATE(component, region, reference);
  }

  @Override
  protected void command_MOVE(ComponentInfo component, ComponentInfo reference) throws Exception {
    m_layout.command_MOVE(component, reference);
  }

  @Override
  protected void command_ADD(ComponentInfo component, ComponentInfo reference) throws Exception {
    String region = m_layout.getEmptyRegion();
    m_layout.command_MOVE(component, reference);
    m_layout.command_REGION(component, region);
  }
}