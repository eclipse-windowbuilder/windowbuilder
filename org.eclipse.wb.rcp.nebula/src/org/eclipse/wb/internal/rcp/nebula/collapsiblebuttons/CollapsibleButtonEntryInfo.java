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
package org.eclipse.wb.internal.rcp.nebula.collapsiblebuttons;

import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.editor.palette.model.entry.ToolEntryInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.rcp.nebula.Activator;
import org.eclipse.wb.internal.rcp.nebula.Messages;

import org.eclipse.swt.graphics.Image;

/**
 * {@link EntryInfo} that allows user to drop new {@link CustomButton} on {@link CollapsibleButtons}
 * .
 *
 * @author sablin_aa
 * @coverage nebula.palette
 */
public final class CollapsibleButtonEntryInfo extends ToolEntryInfo {
  private static final Image ICON =
      Activator.getImage("wbp-meta/org/eclipse/nebula/widgets/collapsiblebuttons/CustomButton.png");

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CollapsibleButtonEntryInfo() throws Exception {
    setName(Messages.CollapsibleButtonEntryInfo_name);
    setDescription(Messages.CollapsibleButtonEntryInfo_description);
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
  public boolean initialize(IEditPartViewer editPartViewer, JavaInfo rootJavaInfo) {
    super.initialize(editPartViewer, rootJavaInfo);
    return ProjectUtils.hasType(
        m_javaProject,
        "org.eclipse.nebula.widgets.collapsiblebuttons.CollapsibleButtons");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ToolEntryInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Tool createTool() throws Exception {
    return new CollapsibleButtonDropTool();
  }
}
