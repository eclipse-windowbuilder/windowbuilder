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

import com.google.common.collect.Lists;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.core.requests.Request;

import java.util.List;

/**
 * The {@link PasteTool} creates new {@link EditPart EditParts} via from memento.
 *
 * @author lobas_av
 * @author scheglov_ke
 * @coverage gef.core
 */
public class PasteTool extends AbstractCreationTool {
  private final Object m_memento;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PasteTool(Object memento) {
    m_memento = memento;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns object with paste info.
   */
  public final Object getMemento() {
    return m_memento;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Request
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates a {@link PasteRequest} and sets this memento object on the request.
   */
  @Override
  protected Request createTargetRequest() {
    return new PasteRequest(m_memento);
  }

  @Override
  protected void selectAddedObjects() {
    final IEditPartViewer viewer = getViewer();
    // prepare pasted EditPart's
    List<EditPart> editParts = Lists.newArrayList();
    {
      PasteRequest request = (PasteRequest) getTargetRequest();
      for (Object model : request.getObjects()) {
        editParts.add(viewer.getEditPartByModel(model));
      }
    }
    // select EditPart's
    viewer.setSelection(editParts);
  }
}