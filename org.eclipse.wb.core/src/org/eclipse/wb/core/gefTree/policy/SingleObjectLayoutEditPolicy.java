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
package org.eclipse.wb.core.gefTree.policy;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;

import java.util.List;

/**
 * {@link LayoutEditPolicy} for {@link ObjectInfo} container which accepts only one child.
 *
 * @author scheglov_ke
 * @coverage core.gefTree.policy
 */
public abstract class SingleObjectLayoutEditPolicy<C> extends ObjectLayoutEditPolicy<C> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SingleObjectLayoutEditPolicy(ObjectInfo host) {
    super(host);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Routing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected final boolean isGoodReferenceChild(Request request, EditPart editPart) {
    return false;
  }

  /**
   * @return <code>true</code> if this container is empty, so we can drop new component.
   */
  protected abstract boolean isEmpty();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Command
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final Command getCommand(Request request) {
    if (!isEmpty()) {
      return null;
    }
    return super.getCommand(request);
  }

  @Override
  protected final Command getMoveCommand(List<EditPart> moveParts, Object referenceObject) {
    return null;
  }

  @Override
  protected final Command getAddCommand(List<EditPart> addParts, Object referenceObject) {
    if (addParts.size() != 1) {
      return null;
    }
    return super.getAddCommand(addParts, referenceObject);
  }

  @Override
  protected final void command_CREATE(C component, C reference) throws Exception {
    command_CREATE(component);
  }

  @Override
  protected final void command_ADD(C component, C reference) throws Exception {
    command_ADD(component);
  }

  protected abstract void command_CREATE(C component) throws Exception;

  protected abstract void command_ADD(C component) throws Exception;
}