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
package org.eclipse.wb.internal.xwt.model.layout;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.swt.model.layout.IRowDataInfo;
import org.eclipse.wb.internal.swt.model.layout.IRowLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.RowLayoutAssistant;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;

import java.util.List;

/**
 * Model for {@link RowLayout}.
 *
 * @author scheglov_ke
 * @coverage XWT.model.layout
 */
public final class RowLayoutInfo extends GenericFlowLayoutInfo
    implements
      IRowLayoutInfo<ControlInfo> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RowLayoutInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
    new RowLayoutAssistant(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isHorizontal() {
    return ReflectionUtils.getFieldInt(getObject(), "type") == SWT.HORIZONTAL;
  }

  /**
   * @return {@link RowDataInfo} associated with given {@link ControlInfo}.
   */
  public static RowDataInfo getRowData(ControlInfo control) {
    return (RowDataInfo) getLayoutData(control);
  }

  public IRowDataInfo getRowData2(ControlInfo control) {
    return getRowData(control);
  }

  //////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void clipboardCopy_addControlCommands(ControlInfo control,
      List<ClipboardCommand> commands) throws Exception {
    super.clipboardCopy_addControlCommands(control, commands);
    commands.add(new LayoutDataClipboardCommand(this, control));
  }
}
