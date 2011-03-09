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
package org.eclipse.wb.internal.xwt.model.widgets;

import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.xwt.support.ControlSupport;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.ToolBar;

import java.util.List;

/**
 * Model for {@link ToolBar}.
 * 
 * @author scheglov_ke
 * @coverage XWT.model.widgets
 */
public final class ToolBarInfo extends CompositeInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ToolBarInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if this {@link ToolBarInfo} has horizontal layout.
   */
  public boolean isHorizontal() {
    return ControlSupport.hasStyle(getControl(), SWT.HORIZONTAL);
  }

  /**
   * @return the {@link ToolItemInfo} children.
   */
  public List<ToolItemInfo> getItems() {
    return getChildren(ToolItemInfo.class);
  }
}
