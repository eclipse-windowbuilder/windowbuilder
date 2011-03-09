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
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.utils.jdt.ui.JdtUiUtils;
import org.eclipse.wb.internal.rcp.Activator;
import org.eclipse.wb.internal.rcp.gef.policy.jface.action.ActionDropTool;
import org.eclipse.wb.internal.rcp.gef.policy.rcp.perspective.ViewDropTool;
import org.eclipse.wb.internal.rcp.model.jface.action.ActionInfo;

import org.eclipse.jdt.core.IType;
import org.eclipse.swt.graphics.Image;

import javax.swing.Action;

/**
 * Implementation of {@link EntryInfo} that allows user select some {@link Action} and loads
 * {@link ViewDropTool}.
 * 
 * @author scheglov_ke
 * @coverage rcp.editor.palette
 */
public final class ActionExternalEntryInfo extends ToolEntryInfo {
  private static final Image ICON = Activator.getImage("info/Action/action_open.gif");

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ActionExternalEntryInfo(String id) {
    setId(id);
    setName("External...");
    setDescription("Allows to select some existing Action type (in separate external class) and drop it on design canvas.");
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
    IType selectedActionType =
        JdtUiUtils.selectSubType(
            getSite().getShell(),
            m_javaProject,
            "org.eclipse.jface.action.IAction");
    if (selectedActionType != null) {
      ClassLoader editorLoader = m_state.getEditorLoader();
      Class<?> selectedActionClass =
          editorLoader.loadClass(selectedActionType.getFullyQualifiedName());
      ActionInfo actionInfo =
          (ActionInfo) JavaInfoUtils.createJavaInfo(
              m_editor,
              selectedActionClass,
              new ConstructorCreationSupport());
      return new ActionDropTool(actionInfo);
    }
    return null;
  }
}
