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
package org.eclipse.wb.internal.swt.model.layout;

import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.clipboard.ComponentClipboardCommand;
import org.eclipse.wb.internal.core.model.clipboard.PropertiesClipboardCommand;
import org.eclipse.wb.internal.core.model.creation.IImplicitCreationSupport;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

/**
 * {@link ClipboardCommand} for applying {@link LayoutDataInfo} during pasting {@link ControlInfo}.
 * 
 * @author scheglov_ke
 * @coverage swt.model.layout
 */
public final class LayoutDataClipboardCommand extends ComponentClipboardCommand<CompositeInfo> {
  private static final long serialVersionUID = 0L;
  private final int m_index;
  private final PropertiesClipboardCommand m_propertiesCommand;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutDataClipboardCommand(LayoutInfo layout, ControlInfo control) throws Exception {
    LayoutDataInfo layoutData = LayoutInfo.getLayoutData(control);
    if (layoutData.getCreationSupport() instanceof IImplicitCreationSupport) {
      m_index = -1;
      m_propertiesCommand = null;
    } else {
      m_index = layout.getControls().indexOf(control);
      m_propertiesCommand = new PropertiesClipboardCommand(layoutData);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Execute
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void execute(CompositeInfo composite) throws Exception {
    if (m_index >= 0) {
      ControlInfo control = composite.getLayout().getControls().get(m_index);
      // apply properties
      LayoutDataInfo rowData = LayoutInfo.getLayoutData(control);
      m_propertiesCommand.execute(rowData);
    }
  }
}
