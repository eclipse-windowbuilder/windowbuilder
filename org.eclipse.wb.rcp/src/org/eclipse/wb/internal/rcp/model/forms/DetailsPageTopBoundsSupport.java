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
package org.eclipse.wb.internal.rcp.model.forms;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.TopBoundsSupport;
import org.eclipse.wb.internal.swt.model.widgets.CompositeTopBoundsSupport;
import org.eclipse.wb.internal.swt.support.ControlSupport;
import org.eclipse.wb.internal.swt.support.RectangleSupport;

import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Shell;

/**
 * Implementation of {@link TopBoundsSupport} for {@link DetailsPageInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public final class DetailsPageTopBoundsSupport extends TopBoundsSupport {
  private final DetailsPageInfo m_page;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DetailsPageTopBoundsSupport(DetailsPageInfo page) {
    super(page);
    m_page = page;
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
      Shell shell = m_page.getShell();
      // "size" is size of _content_ for "shell", so calculate trim
      Rectangle trim =
          RectangleSupport.getRectangle(shell.computeTrim(0, 0, size.width, size.height));
      // OK, set size from trim
      ControlSupport.setSize(shell, trim.width, trim.height);
    }
  }

  @Override
  public void setSize(int width, int height) throws Exception {
    // remember size in resource properties
    setResourceSize(width, height);
  }

  @Override
  protected Dimension getDefaultSize() {
    return new Dimension(600, 500);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Show
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean show() throws Exception {
    Shell shell = m_page.getShell();
    // don't dispose Shell, DetailsPage_Info reuses it between refresh()
    shell.addShellListener(new ShellAdapter() {
      @Override
      public void shellClosed(ShellEvent e) {
        e.doit = false;
        ((Shell) e.widget).setVisible(false);
      }
    });
    // do show
    CompositeTopBoundsSupport.show(m_page, shell);
    return true;
  }
}