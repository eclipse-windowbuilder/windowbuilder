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

import org.eclipse.wb.internal.swing.gef.policy.layout.gbl.header.edit.DimensionHeaderEditPart;
import org.eclipse.wb.internal.swing.model.layout.gbl.DimensionInfo;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * {@link Action} for modifying grow of {@link DimensionInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.gef.policy
 */
public final class SetGrowAction<T extends DimensionInfo> extends DimensionHeaderAction<T> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SetGrowAction(DimensionHeaderEditPart<T> header,
      String text,
      ImageDescriptor imageDescriptor) {
    super(header, text, imageDescriptor, AS_CHECK_BOX);
    setChecked(header.getDimension().hasWeight());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Run
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void run(T dimension) throws Exception {
    double weight = dimension.hasWeight() ? 0.0 : 1.0;
    dimension.setWeight(weight);
  }
}