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
package org.eclipse.wb.core.gef.policy.layout.position;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.core.utils.state.IPasteComponentProcessor;

/**
 * {@link LayoutEditPolicy} for placing children on fixed areas on parent.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public abstract class ObjectPositionLayoutEditPolicy<C, D> extends AbstractPositionLayoutEditPolicy {
  private final ObjectInfo m_host;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ObjectPositionLayoutEditPolicy(ObjectInfo host) {
    m_host = host;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command getCreateCommand(final Object newObject, final Object data) {
    return new EditCommand(m_host) {
      @Override
      @SuppressWarnings("unchecked")
      protected void executeEdit() throws Exception {
        command_CREATE((C) newObject, (D) data);
      }
    };
  }

  @Override
  protected Command getPasteCommand(PasteRequest request, final Object data) {
    return GlobalState.getPasteRequestProcessor().getPasteCommand(
        request,
        new IPasteComponentProcessor() {
          @SuppressWarnings("unchecked")
          public void process(Object component) throws Exception {
            command_CREATE((C) component, (D) data);
          }
        });
  }

  @Override
  protected Command getMoveCommand(final Object moveObject, final Object data) {
    return new EditCommand(m_host) {
      @Override
      @SuppressWarnings("unchecked")
      protected void executeEdit() throws Exception {
        command_MOVE((C) moveObject, (D) data);
      }
    };
  }

  @Override
  protected Command getAddCommand(final Object addObject, final Object data) {
    return new EditCommand(m_host) {
      @Override
      @SuppressWarnings("unchecked")
      protected void executeEdit() throws Exception {
        command_ADD((C) addObject, (D) data);
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation of commands
  //
  ////////////////////////////////////////////////////////////////////////////
  protected abstract void command_CREATE(C component, D data) throws Exception;

  protected abstract void command_MOVE(C component, D data) throws Exception;

  protected void command_ADD(C component, D data) throws Exception {
    command_MOVE(component, data);
  }
}
