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

import com.google.common.collect.Lists;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.core.utils.state.IPasteComponentProcessor;

import java.util.List;

/**
 * Abstract {@link LayoutEditPolicy} for typical {@link ObjectInfo} container.
 *
 * @author scheglov_ke
 * @coverage core.gefTree.policy
 */
public abstract class ObjectLayoutEditPolicy<C> extends LayoutEditPolicy {
  private final ObjectInfo m_host;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ObjectLayoutEditPolicy(ObjectInfo host) {
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
        command_CREATE(getObjectModel(newObject), getReferenceModel(referenceObject));
      }
    };
  }

  @Override
  protected Command getPasteCommand(final PasteRequest request, Object referenceObject) {
    final C reference = getReferenceModel(referenceObject);
    return GlobalState.getPasteRequestProcessor().getPasteCommand(
        request,
        new IPasteComponentProcessor() {
          public void process(Object component) throws Exception {
            command_CREATE(getObjectModel(component), reference);
          }
        });
  }

  @Override
  protected Command getMoveCommand(final List<EditPart> moveParts, final Object referenceObject) {
    return new EditCommand(m_host) {
      @Override
      protected void executeEdit() throws Exception {
        List<C> objects = getModels(moveParts);
        command_MOVE(objects, getReferenceModel(referenceObject));
      }
    };
  }

  @Override
  protected Command getAddCommand(final List<EditPart> addParts, final Object referenceObject) {
    return new EditCommand(m_host) {
      @Override
      protected void executeEdit() throws Exception {
        List<C> objects = getModels(addParts);
        command_ADD(objects, getReferenceModel(referenceObject));
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation of commands
  //
  ////////////////////////////////////////////////////////////////////////////
  protected void command_CREATE(C newObject, C referenceObject) throws Exception {
  }

  protected void command_MOVE(List<C> objects, C referenceObject) throws Exception {
    for (C object : objects) {
      command_MOVE(object, referenceObject);
    }
  }

  protected void command_ADD(List<C> objects, C referenceObject) throws Exception {
    for (C object : objects) {
      command_ADD(object, referenceObject);
    }
  }

  protected void command_MOVE(C object, C referenceObject) throws Exception {
  }

  protected void command_ADD(C object, C referenceObject) throws Exception {
    command_MOVE(object, referenceObject);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  @SuppressWarnings("unchecked")
  private C getObjectModel(Object object) {
    return (C) object;
  }

  @SuppressWarnings("unchecked")
  private C getReferenceModel(Object referenceObject) {
    return (C) referenceObject;
  }

  private List<C> getModels(List<EditPart> editParts) {
    List<C> objects = Lists.newArrayList();
    for (EditPart editPart : editParts) {
      Object rawModel = editPart.getModel();
      C objectModel = getObjectModel(rawModel);
      objects.add(objectModel);
    }
    return objects;
  }
}