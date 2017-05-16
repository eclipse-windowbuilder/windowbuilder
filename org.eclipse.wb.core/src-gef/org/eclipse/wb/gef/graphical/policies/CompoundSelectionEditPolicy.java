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
package org.eclipse.wb.gef.graphical.policies;

import com.google.common.collect.Lists;

import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.handles.Handle;

import java.util.List;

/**
 * {@link SelectionEditPolicy} which consists of several other {@link SelectionEditPolicy}s.
 *
 * @author scheglov_ke
 * @coverage gef.graphical
 */
public class CompoundSelectionEditPolicy extends SelectionEditPolicy {
  private final List<SelectionEditPolicy> m_policies;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CompoundSelectionEditPolicy(List<SelectionEditPolicy> policies) {
    m_policies = policies;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // SelectionEditPolicy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setHost(EditPart host) {
    super.setHost(host);
    for (SelectionEditPolicy policy : m_policies) {
      policy.setHost(host);
    }
  }

  @Override
  protected List<Handle> createSelectionHandles() {
    List<Handle> handles = Lists.newArrayList();
    for (SelectionEditPolicy policy : m_policies) {
      policy.createSelectionHandles();
    }
    return handles;
  }

  @Override
  protected void showSelection() {
    for (SelectionEditPolicy policy : m_policies) {
      policy.showSelection();
    }
  }

  @Override
  protected void hideSelection() {
    for (SelectionEditPolicy policy : m_policies) {
      policy.hideSelection();
    }
  }

  @Override
  public void showSourceFeedback(Request request) {
    for (SelectionEditPolicy policy : m_policies) {
      if (policy.understandsRequest(request)) {
        policy.showSourceFeedback(request);
      }
    }
  }

  @Override
  public void eraseSourceFeedback(Request request) {
    for (SelectionEditPolicy policy : m_policies) {
      if (policy.understandsRequest(request)) {
        policy.eraseSourceFeedback(request);
      }
    }
  }

  @Override
  public void performRequest(Request request) {
    for (SelectionEditPolicy policy : m_policies) {
      policy.performRequest(request);
    }
  }

  @Override
  public boolean understandsRequest(Request request) {
    for (SelectionEditPolicy policy : m_policies) {
      if (policy.understandsRequest(request)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public EditPart getTargetEditPart(Request request) {
    for (SelectionEditPolicy policy : m_policies) {
      EditPart targetEditPart = policy.getTargetEditPart(request);
      if (targetEditPart != null) {
        return targetEditPart;
      }
    }
    return null;
  }

  @Override
  public Command getCommand(Request request) {
    for (SelectionEditPolicy policy : m_policies) {
      if (policy.understandsRequest(request)) {
        Command command = policy.getCommand(request);
        if (command != null) {
          return command;
        }
      }
    }
    return null;
  }
}
