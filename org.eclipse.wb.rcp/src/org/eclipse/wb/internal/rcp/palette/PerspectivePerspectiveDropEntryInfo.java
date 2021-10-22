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
import org.eclipse.wb.internal.rcp.gef.policy.rcp.perspective.PerspectiveDropTool;
import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils.PerspectiveInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.PageLayoutInfo;

import org.eclipse.swt.graphics.Image;

/**
 * {@link EntryInfo} that allows user to drop new perspective on {@link PageLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.editor.palette
 */
public final class PerspectivePerspectiveDropEntryInfo extends ToolEntryInfo {
  private final PerspectiveInfo m_perspective;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PerspectivePerspectiveDropEntryInfo(PerspectiveInfo perspective) {
    m_perspective = perspective;
    setId(perspective.getId());
    setName(m_perspective.getName());
    setDescription(m_perspective.getId() + "\n" + m_perspective.getClassName());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // EntryInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Image getIcon() {
    return m_perspective.getIcon();
  }

  @Override
  public Tool createTool() throws Exception {
    return new PerspectiveDropTool(m_perspective);
  }
}
