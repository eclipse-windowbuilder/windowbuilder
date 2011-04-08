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
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.utils.jdt.core.SubtypesScope;
import org.eclipse.wb.internal.core.utils.jdt.ui.JdtUiUtils;
import org.eclipse.wb.internal.swing.Activator;
import org.eclipse.wb.internal.swing.model.bean.ActionInfo;

import org.eclipse.jdt.core.IType;
import org.eclipse.swt.graphics.Image;

import javax.swing.Action;

/**
 * Implementation of {@link EntryInfo} that allows user select some {@link Action} and drop it.
 * 
 * @author scheglov_ke
 * @coverage swing.editor.palette
 */
public final class ActionExternalEntryInfo extends ToolEntryInfo {
  private static final Image ICON = Activator.getImage("info/Action/action_open.gif");

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ActionExternalEntryInfo() {
    setName(PaletteMessages.ActionExternalEntryInfo_name);
    setDescription(PaletteMessages.ActionExternalEntryInfo_description);
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
    IType actionType = m_javaProject.findType("javax.swing.Action");
    SubtypesScope scope = new SubtypesScope(actionType);
    IType selectedActionType = JdtUiUtils.selectType(DesignerPlugin.getShell(), scope);
    if (selectedActionType != null) {
      ActionInfo actionInfo =
          (ActionInfo) JavaInfoUtils.createJavaInfo(
              m_editor,
              selectedActionType.getFullyQualifiedName(),
              new ConstructorCreationSupport());
      return ActionUseEntryInfo.createActionTool(actionInfo);
    }
    return null;
  }
}
