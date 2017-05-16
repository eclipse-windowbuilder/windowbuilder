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
package org.eclipse.wb.core.editor.palette.model.entry;

import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.gef.graphical.tools.SelectionTool;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.Messages;
import org.eclipse.wb.internal.core.editor.palette.model.entry.IDefaultEntryInfo;

import org.eclipse.swt.graphics.Image;

/**
 * Implementation of {@link EntryInfo} that activates {@link SelectionTool}.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette
 */
public class SelectionToolEntryInfo extends ToolEntryInfo implements IDefaultEntryInfo {
  private static final Image ICON = DesignerPlugin.getImage("palette/SelectionTool.gif");
  private final SelectionTool m_selectionTool = new SelectionTool();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SelectionToolEntryInfo() {
    setName(Messages.SelectionToolEntryInfo_name);
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

  @Override
  public Tool createTool() throws Exception {
    return m_selectionTool;
  }
}
