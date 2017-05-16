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
import org.eclipse.wb.gef.graphical.tools.MarqueeSelectionTool;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.Messages;

import org.eclipse.swt.graphics.Image;

/**
 * Implementation of {@link EntryInfo} that activates {@link MarqueeSelectionTool}.
 *
 * @author lobas_av
 * @coverage core.editor.palette
 */
public final class MarqueeSelectionToolEntryInfo extends ToolEntryInfo {
  private static final Image ICON = DesignerPlugin.getImage("palette/MarqueeSelectionTool.png");
  private final MarqueeSelectionTool m_marqueeSelectionTool = new MarqueeSelectionTool();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MarqueeSelectionToolEntryInfo() {
    setName(Messages.MarqueeSelectionToolEntryInfo_name);
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
    return m_marqueeSelectionTool;
  }
}