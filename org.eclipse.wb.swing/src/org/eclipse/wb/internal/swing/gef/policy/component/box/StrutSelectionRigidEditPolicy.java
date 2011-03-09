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
package org.eclipse.wb.internal.swing.gef.policy.component.box;

import com.google.common.collect.Lists;

import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.internal.swing.gef.part.box.BoxStrutHorizontalEditPart;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.property.converter.DimensionConverter;

import java.util.List;

/**
 * The {@link SelectionEditPolicy} for resizing {@link BoxStrutHorizontalEditPart}.
 * 
 * @author scheglov_ke
 * @coverage swing.gef.policy
 */
public final class StrutSelectionRigidEditPolicy extends StrutSelectionEditPolicy {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StrutSelectionRigidEditPolicy(ComponentInfo strut) {
    super(strut);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handles
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Handle> createStaticHandles() {
    List<Handle> handles = Lists.newArrayList();
    handles.add(createResizeHandle(IPositionConstants.LEFT, IPositionConstants.WEST));
    handles.add(createResizeHandle(IPositionConstants.RIGHT, IPositionConstants.EAST));
    handles.add(createResizeHandle(IPositionConstants.TOP, IPositionConstants.NORTH));
    handles.add(createResizeHandle(IPositionConstants.BOTTOM, IPositionConstants.SOUTH));
    return handles;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Resize
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getTooltip(int width, int height) {
    return "(" + Integer.toString(width) + ", " + Integer.toString(height) + ")";
  }

  @Override
  protected String getSource(ComponentInfo strut, int width, int height) throws Exception {
    return DimensionConverter.INSTANCE.toJavaSource(strut, new java.awt.Dimension(width, height));
  }
}