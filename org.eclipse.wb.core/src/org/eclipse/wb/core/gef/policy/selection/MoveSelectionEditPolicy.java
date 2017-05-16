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
package org.eclipse.wb.core.gef.policy.selection;

import com.google.common.collect.Lists;

import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;

import org.eclipse.swt.graphics.Color;

import java.util.List;

/**
 * Implementation of {@link SelectionEditPolicy} that shows only {@link MoveHandle} as selection
 * feedback.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public final class MoveSelectionEditPolicy extends SelectionEditPolicy {
  private final Color m_color;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MoveSelectionEditPolicy() {
    this(IColorConstants.black);
  }

  public MoveSelectionEditPolicy(Color color) {
    m_color = color;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handles
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Handle> createSelectionHandles() {
    List<Handle> handles = Lists.newArrayList();
    {
      MoveHandle moveHandle = new MoveHandle(getHost());
      moveHandle.setForeground(m_color);
      handles.add(moveHandle);
    }
    return handles;
  }
}
