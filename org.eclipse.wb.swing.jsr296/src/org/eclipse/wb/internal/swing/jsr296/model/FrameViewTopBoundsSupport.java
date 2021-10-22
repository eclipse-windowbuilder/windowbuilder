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
package org.eclipse.wb.internal.swing.jsr296.model;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.internal.core.model.TopBoundsSupport;
import org.eclipse.wb.internal.swing.model.component.top.SwingTopBoundsSupport;

import javax.swing.JFrame;

/**
 * {@link TopBoundsSupport} for {@link FrameViewInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.jsr296
 */
public class FrameViewTopBoundsSupport extends TopBoundsSupport {
  private final FrameViewInfo m_view;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FrameViewTopBoundsSupport(FrameViewInfo view) {
    super(view);
    m_view = view;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TopBoundsSupport
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void apply() throws Exception {
    // set size from resource properties (or default)
    {
      Dimension size = getResourceSize();
      m_view.getFrame().setSize(size.width, size.height);
    }
  }

  @Override
  public void setSize(int width, int height) throws Exception {
    // remember size in resource properties
    setResourceSize(width, height);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Show
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean show() throws Exception {
    JFrame frame = m_view.getFrame();
    SwingTopBoundsSupport.show(frame);
    return false;
  }
}