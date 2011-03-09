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
package org.eclipse.wb.internal.swing.gef.policy.layout.gbl.header.actions;

import com.google.common.collect.Lists;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.swing.gef.policy.layout.gbl.header.edit.DimensionHeaderEditPart;
import org.eclipse.wb.internal.swing.model.layout.gbl.DimensionInfo;

import org.eclipse.jface.resource.ImageDescriptor;

import java.util.List;

/**
 * Abstract action for manipulating selected {@link DimensionHeaderEditPart}'s.
 * 
 * @author scheglov_ke
 * @coverage swing.gef.policy
 */
public abstract class DimensionHeaderAction<T extends DimensionInfo> extends ObjectInfoAction {
  private final IEditPartViewer m_viewer;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DimensionHeaderAction(DimensionHeaderEditPart<T> editPart, String text) {
    this(editPart, text, null);
  }

  public DimensionHeaderAction(DimensionHeaderEditPart<T> editPart,
      String text,
      ImageDescriptor imageDescriptor) {
    this(editPart, text, imageDescriptor, AS_PUSH_BUTTON);
  }

  public DimensionHeaderAction(DimensionHeaderEditPart<T> editPart,
      String text,
      ImageDescriptor imageDescriptor,
      int style) {
    super(editPart.getLayout(), text, style);
    m_viewer = editPart.getViewer();
    setImageDescriptor(imageDescriptor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final int hashCode() {
    return 0;
  }

  @Override
  public boolean equals(Object obj) {
    return obj != null && getClass() == obj.getClass();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Run
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected final void runEx() throws Exception {
    // prepare selection
    List<T> dimensions = Lists.newArrayList();
    {
      List<EditPart> editParts = m_viewer.getSelectedEditParts();
      for (EditPart editPart : editParts) {
        if (editPart instanceof DimensionHeaderEditPart) {
          @SuppressWarnings("unchecked")
          DimensionHeaderEditPart<T> headerEditPart = (DimensionHeaderEditPart<T>) editPart;
          dimensions.add(headerEditPart.getDimension());
        }
      }
    }
    // run over them
    run(dimensions);
  }

  /**
   * Does some operation on {@link List} of selected {@link FormDimensionInfo}'s.
   */
  protected void run(List<T> dimensions) throws Exception {
    for (T dimension : dimensions) {
      run(dimension);
    }
  }

  /**
   * Does some operation on selected {@link FormDimensionInfo}'s.
   */
  protected void run(T dimension) throws Exception {
  }
}
