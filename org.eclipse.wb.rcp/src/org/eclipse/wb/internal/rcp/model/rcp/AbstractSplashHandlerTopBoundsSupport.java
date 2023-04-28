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
package org.eclipse.wb.internal.rcp.model.rcp;

import org.eclipse.wb.internal.core.model.TopBoundsSupport;
import org.eclipse.wb.internal.swt.model.widgets.CompositeTopBoundsSupport;
import org.eclipse.wb.internal.swt.support.ControlSupport;

import org.eclipse.draw2d.geometry.Dimension;

/**
 * Implementation of {@link TopBoundsSupport} for {@link AbstractSplashHandlerInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public class AbstractSplashHandlerTopBoundsSupport extends TopBoundsSupport {
  private final AbstractSplashHandlerInfo m_splash;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractSplashHandlerTopBoundsSupport(AbstractSplashHandlerInfo splash) {
    super(splash);
    m_splash = splash;
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
      ControlSupport.setSize(m_splash.getShell(), size.width, size.height);
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
    CompositeTopBoundsSupport.show(m_splash, m_splash.getShell());
    return true;
  }
}