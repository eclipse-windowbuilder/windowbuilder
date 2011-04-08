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
package org.eclipse.wb.internal.swing.palette;

import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.editor.palette.model.entry.ToolEntryInfo;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.swing.Activator;
import org.eclipse.wb.internal.swing.model.bean.ActionInfo;

import org.eclipse.swt.graphics.Image;

import javax.swing.Action;

/**
 * Implementation of {@link EntryInfo} that allows user to create new inner {@link Action} and drop
 * it.
 * 
 * @author scheglov_ke
 * @coverage swing.editor.palette
 */
public final class ActionNewEntryInfo extends ToolEntryInfo {
  private static final Image ICON = Activator.getImage("info/Action/action_new.gif");

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ActionNewEntryInfo() {
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
    ActionInfo action = ActionInfo.createInner(m_editor);
    return ActionUseEntryInfo.createActionTool(action);
  }
}
