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
package org.eclipse.wb.internal.swt.gef.policy.layout.grid.header.actions;

import com.google.common.collect.Lists;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.swt.gef.policy.layout.grid.header.edit.DimensionHeaderEditPart;
import org.eclipse.wb.internal.swt.model.layout.grid.GridDimensionInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.jface.resource.ImageDescriptor;

import java.util.List;

/**
 * Abstract action for manipulating selected {@link GridDimensionInfo}'s.
 *
 * @author scheglov_ke
 * @coverage swt.gef.GridLayout
 */
public abstract class DimensionHeaderAction<C extends IControlInfo> extends ObjectInfoAction {
  private final IEditPartViewer m_viewer;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DimensionHeaderAction(DimensionHeaderEditPart<C> editPart, String text) {
    this(editPart, text, null);
  }

  public DimensionHeaderAction(DimensionHeaderEditPart<C> editPart,
      String text,
      ImageDescriptor imageDescriptor) {
    this(editPart, text, imageDescriptor, AS_PUSH_BUTTON);
  }

  public DimensionHeaderAction(DimensionHeaderEditPart<C> editPart,
      String text,
      ImageDescriptor imageDescriptor,
      int style) {
    super(editPart.getLayout().getUnderlyingModel(), text, style);
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
    return getClass() == obj.getClass();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Run
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  @SuppressWarnings("unchecked")
  protected final void runEx() throws Exception {
    // prepare selection
    List<GridDimensionInfo<C>> dimensions = Lists.newArrayList();
    {
      for (EditPart editPart : m_viewer.getSelectedEditParts()) {
        if (editPart instanceof DimensionHeaderEditPart<?>) {
          DimensionHeaderEditPart<C> headerEditPart = (DimensionHeaderEditPart<C>) editPart;
          dimensions.add(headerEditPart.getDimension());
        }
      }
    }
    // run over them
    run(dimensions);
  }

  /**
   * Does some operation on {@link List} of selected {@link GridDimensionInfo}'s.
   */
  protected void run(List<GridDimensionInfo<C>> dimensions) throws Exception {
    for (GridDimensionInfo<C> dimension : dimensions) {
      run(dimension);
    }
  }

  /**
   * Does some operation on selected {@link GridDimensionInfo}'s.
   */
  protected void run(GridDimensionInfo<C> dimension) throws Exception {
  }
}
