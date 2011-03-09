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
package org.eclipse.wb.internal.ercp.model.rcp;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.internal.core.model.TopBoundsSupport;
import org.eclipse.wb.internal.swt.model.widgets.CompositeTopBoundsSupport;
import org.eclipse.wb.internal.swt.support.ControlSupport;

/**
 * Implementation of {@link TopBoundsSupport} for {@link PreferencePageInfo}.
 * 
 * @author scheglov_ke
 * @coverage swt.model.jface
 */
public final class PreferencePageTopBoundsSupport extends TopBoundsSupport {
  private final PreferencePageInfo m_page;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PreferencePageTopBoundsSupport(PreferencePageInfo preferencePage) {
    super(preferencePage);
    m_page = preferencePage;
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
      ControlSupport.setSize(m_page.getShell(), size.width, size.height);
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
    CompositeTopBoundsSupport.show(m_page, m_page.getShell());
    return false;
  }
}