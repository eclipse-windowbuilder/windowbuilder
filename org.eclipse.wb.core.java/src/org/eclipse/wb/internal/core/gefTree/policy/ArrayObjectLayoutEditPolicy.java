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
package org.eclipse.wb.internal.core.gefTree.policy;

import org.eclipse.wb.core.gef.command.CompoundEditCommand;
import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.model.nonvisual.AbstractArrayObjectInfo;

import java.util.List;

/**
 * Implementation of {@link LayoutEditPolicy} for {@link AbstractArrayObjectInfo}.
 *
 * @author sablin_aa
 * @coverage core.gefTree.policy
 */
public final class ArrayObjectLayoutEditPolicy extends LayoutEditPolicy {
  private final AbstractArrayObjectInfo m_arrayInfo;
  private final ArrayObjectRequestValidator m_validator;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ArrayObjectLayoutEditPolicy(AbstractArrayObjectInfo object) {
    m_arrayInfo = object;
    m_validator = new ArrayObjectRequestValidator(m_arrayInfo);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Reference children
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean isGoodReferenceChild(Request request, EditPart editPart) {
    return m_validator.isValidModel(editPart.getModel());
  }

  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return m_validator;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command getCreateCommand(Object newObject, Object referenceObject) {
    final JavaInfo item = (JavaInfo) newObject;
    final JavaInfo nextItem = (JavaInfo) referenceObject;
    return new EditCommand(m_arrayInfo) {
      @Override
      protected void executeEdit() throws Exception {
        m_arrayInfo.command_CREATE(item, nextItem);
      }
    };
  }

  @Override
  protected Command getMoveCommand(List<EditPart> moveParts, Object referenceObject) {
    CompoundEditCommand command = new CompoundEditCommand(m_arrayInfo);
    for (EditPart editPart : moveParts) {
      final JavaInfo item = (JavaInfo) editPart.getModel();
      final JavaInfo nextItem = (JavaInfo) referenceObject;
      command.add(new EditCommand(m_arrayInfo) {
        @Override
        protected void executeEdit() throws Exception {
          m_arrayInfo.command_MOVE(item, nextItem);
        }
      });
    }
    return command;
  }

  @Override
  protected Command getAddCommand(List<EditPart> addParts, Object referenceObject) {
    return getMoveCommand(addParts, referenceObject);
  }
}
