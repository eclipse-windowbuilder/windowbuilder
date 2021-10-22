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
package org.eclipse.wb.internal.rcp.palette;

import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.editor.palette.model.entry.ToolEntryInfo;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.rcp.Activator;
import org.eclipse.wb.internal.rcp.gef.policy.jface.action.ActionDropTool;
import org.eclipse.wb.internal.rcp.model.jface.action.ActionContainerInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.ActionInfo;

import org.eclipse.swt.graphics.Image;

import javax.swing.Action;

/**
 * Implementation of {@link EntryInfo} that allows user to create new inner {@link Action} and loads
 * new {@link ActionDropTool}.
 *
 * @author scheglov_ke
 * @coverage rcp.editor.palette
 */
public final class ActionNewEntryInfo extends ToolEntryInfo {
  private static final Image ICON = Activator.getImage("info/Action/action_new.gif");

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ActionNewEntryInfo(String id) {
    setId(id);
    setName(PaletteMessages.ActionNewEntryInfo_name);
    setDescription(PaletteMessages.ActionNewEntryInfo_description);
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
    ActionInfo action = ActionContainerInfo.createNew(m_rootJavaInfo);
    return new ActionDropTool(action);
  }
}
