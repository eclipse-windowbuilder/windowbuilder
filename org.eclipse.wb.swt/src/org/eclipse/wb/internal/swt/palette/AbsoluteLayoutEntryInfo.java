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
package org.eclipse.wb.internal.swt.palette;

import org.eclipse.wb.core.editor.palette.model.entry.ToolEntryInfo;
import org.eclipse.wb.gef.core.requests.ICreationFactory;
import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.swt.Activator;
import org.eclipse.wb.internal.swt.model.ModelMessages;
import org.eclipse.wb.internal.swt.model.layout.absolute.AbsoluteLayoutCreationSupport;
import org.eclipse.wb.internal.swt.model.layout.absolute.AbsoluteLayoutInfo;

import org.eclipse.swt.graphics.Image;

/**
 * Implementation of {@link ToolEntryInfo} that adds {@link AbsoluteLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swt.editor.palette
 */
public final class AbsoluteLayoutEntryInfo extends ToolEntryInfo {
  private static final Image ICON = Activator.getImage("info/layout/absolute/layout.gif");

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbsoluteLayoutEntryInfo() {
    setName(ModelMessages.AbsoluteLayoutEntryInfo_name);
    setDescription(ModelMessages.AbsoluteLayoutEntryInfo_description);
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
        ToolkitDescription toolkit = m_rootJavaInfo.getDescription().getToolkit();
        AbsoluteLayoutCreationSupport creationSupport = new AbsoluteLayoutCreationSupport();
        m_layout = new AbsoluteLayoutInfo(m_editor, toolkit, creationSupport);
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
