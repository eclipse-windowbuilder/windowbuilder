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
package org.eclipse.wb.gef.core.tools;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.ICreationFactory;
import org.eclipse.wb.gef.core.requests.Request;

/**
 * The {@link CreationTool} creates new {@link EditPart EditParts} via a {@link ICreationFactory}.
 * If the user simply clicks on the viewer, the default sized {@link EditPart} will be created at
 * that point. If the user clicks and drags, the created {@link EditPart} will be sized based on
 * where the user clicked and dragged.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public class CreationTool extends AbstractCreationTool {
  private final ICreationFactory m_factory;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CreationTool(ICreationFactory factory) {
    m_factory = factory;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void activate() {
    super.activate();
    try {
      m_factory.activate();
    } catch (Throwable e) {
      getDomain().loadDefaultTool();
    }
  }

  /**
   * @return the {@link ICreationFactory} used to create the new {@link EditPart}'s.
   */
  public final ICreationFactory getFactory() {
    return m_factory;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Request
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates a {@link CreateRequest} and sets this tool's factory on the request.
   */
  @Override
  protected Request createTargetRequest() {
    return new CreateRequest(m_factory);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void selectAddedObjects() {
    CreateRequest request = (CreateRequest) getTargetRequest();
    Object model = request.getSelectObject();
    if (model != null) {
      IEditPartViewer viewer = getViewer();
      if (viewer != null) {
        EditPart editPart = viewer.getEditPartByModel(model);
        if (editPart != null) {
          viewer.select(editPart);
        }
      }
    }
  }
}