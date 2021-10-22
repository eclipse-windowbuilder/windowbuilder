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
package org.eclipse.wb.internal.rcp.model.widgets;

import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import org.eclipse.swt.widgets.Table;

/**
 * Model for "big" SWT {@link Table}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.widgets
 */
public final class TableInfo extends org.eclipse.wb.internal.swt.model.widgets.TableInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TableInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TableCursor_Info
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if this {@link TableInfo} already has {@link TableCursorInfo}.
   */
  public boolean hasTableCursor() {
    return !getChildren(TableCursorInfo.class).isEmpty();
  }

  /**
   * Adds new {@link TableCursorInfo} to this table.
   */
  public void command_CREATE(TableCursorInfo tableCursor) throws Exception {
    JavaInfoUtils.add(tableCursor, null, this, null);
  }
}
