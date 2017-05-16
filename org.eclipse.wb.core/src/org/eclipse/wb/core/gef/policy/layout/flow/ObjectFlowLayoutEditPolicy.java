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
package org.eclipse.wb.core.gef.policy.layout.flow;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.model.IObjectInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.core.utils.state.IPasteComponentProcessor;

/**
 * Abstract {@link LayoutEditPolicy} for typical {@link ObjectInfo} flow container.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public abstract class ObjectFlowLayoutEditPolicy<C> extends AbstractFlowLayoutEditPolicy {
  private final ObjectInfo m_host;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ObjectFlowLayoutEditPolicy(IObjectInfo host) {
    this(host.getUnderlyingModel());
  }

  public ObjectFlowLayoutEditPolicy(ObjectInfo host) {
    m_host = host;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command getCreateCommand(final Object newObject, final Object referenceObject) {
    return new EditCommand(m_host) {
      @Override
      protected void executeEdit() throws Exception {
        command_CREATE(getObjectModel(newObject), getReferenceObjectModel(referenceObject));
      }
    };
  }

  @Override
  protected Command getPasteCommand(PasteRequest request, Object referenceObject) {
    final C referenceModel = getReferenceObjectModel(referenceObject);
    return GlobalState.getPasteRequestProcessor().getPasteCommand(
        request,
        new IPasteComponentProcessor() {
          public void process(Object component) throws Exception {
            command_CREATE(getObjectModel(component), referenceModel);
          }
        });
  }

  @Override
  protected Command getMoveCommand(final Object moveObject, final Object referenceObject) {
    return new EditCommand(m_host) {
      @Override
      protected void executeEdit() throws Exception {
        command_MOVE(getObjectModel(moveObject), getReferenceObjectModel(referenceObject));
      }
    };
  }

  @Override
  protected Command getAddCommand(final Object addObject, final Object referenceObject) {
    return new EditCommand(m_host) {
      @Override
      protected void executeEdit() throws Exception {
        command_ADD(getObjectModel(addObject), getReferenceObjectModel(referenceObject));
      }
    };
  }

  @SuppressWarnings("unchecked")
  protected C getObjectModel(Object object) {
    return (C) object;
  }

  @SuppressWarnings("unchecked")
  protected C getReferenceObjectModel(Object referenceObject) {
    return (C) referenceObject;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation of commands
  //
  ////////////////////////////////////////////////////////////////////////////
  protected abstract void command_CREATE(C component, C referenceComponent) throws Exception;

  protected abstract void command_MOVE(C component, C referenceComponent) throws Exception;

  protected void command_ADD(C component, C referenceComponent) throws Exception {
    command_MOVE(component, referenceComponent);
  }
}
