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

import org.eclipse.wb.internal.swt.gef.policy.layout.grid.header.edit.DimensionHeaderEditPart;
import org.eclipse.wb.internal.swt.model.layout.grid.GridDimensionInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * {@link Action} for modifying grab of {@link GridDimensionInfo}.
 *
 * @author scheglov_ke
 * @coverage swt.gef.GridLayout
 */
public final class SetGrabAction<C extends IControlInfo> extends DimensionHeaderAction<C> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SetGrabAction(DimensionHeaderEditPart<C> header,
      String text,
      ImageDescriptor imageDescriptor) {
    super(header, text, imageDescriptor, AS_CHECK_BOX);
    setChecked(header.getDimension().getGrab());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Run
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void run(GridDimensionInfo<C> dimension) throws Exception {
    dimension.setGrab(!dimension.getGrab());
  }
}