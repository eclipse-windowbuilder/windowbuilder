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

import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;

import java.util.List;

import javax.swing.Box;

/**
 * Abstract {@link SelectionEditPolicy} for any glue from {@link Box}.
 * 
 * @author scheglov_ke
 * @coverage swing.gef.policy
 */
public final class GlueSelectionEditPolicy extends SelectionEditPolicy {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Handles
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Handle> createSelectionHandles() {
    List<Handle> handles = Lists.newArrayList();
    // create move handle
    MoveHandle moveHandle = new MoveHandle(getHost());
    moveHandle.setForeground(IColorConstants.red);
    handles.add(moveHandle);
    //
    return handles;
  }
}