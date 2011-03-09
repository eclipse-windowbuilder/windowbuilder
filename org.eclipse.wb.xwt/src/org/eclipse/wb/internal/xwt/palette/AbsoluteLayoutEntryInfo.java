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
package org.eclipse.wb.internal.xwt.palette;

import org.eclipse.wb.gef.core.requests.ICreationFactory;
import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.core.xml.editor.palette.model.ToolEntryInfo;
import org.eclipse.wb.internal.swt.Activator;
import org.eclipse.wb.internal.xwt.model.layout.AbsoluteLayoutInfo;

import org.eclipse.swt.graphics.Image;

/**
 * {@link ToolEntryInfo} that adds {@link AbsoluteLayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage XWT.editor
 */
public final class AbsoluteLayoutEntryInfo extends ToolEntryInfo {
  private static final Image ICON = Activator.getImage("info/layout/absolute/layout.gif");

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbsoluteLayoutEntryInfo() {
    setName("Absolute layout");
    setDescription("Layout with absolute positioning components.");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // EntryInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Image getIcon() {
    return ICON;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ToolEntryInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Tool createTool() throws Exception {
    // prepare factory
    ICreationFactory factory = new ICreationFactory() {
      private AbsoluteLayoutInfo m_layout;

      public void activate() throws Exception {
        m_layout = AbsoluteLayoutInfo.createExplicitModel(m_context);
        m_layout.setObject(null); // force initialize
      }

      public Object getNewObject() {
        return m_layout;
      }
    };
    // return tool
    return new CreationTool(factory);
  }
}
