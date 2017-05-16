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
package org.eclipse.wb.internal.gef.core;

import org.eclipse.wb.draw2d.ICursorConstants;

import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.ImageData;

/**
 * A shared collection of Cursors.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public interface ISharedCursors extends ICursorConstants {
  Cursor CURSOR_TREE_ADD = new Cursor(null,
      new ImageData(ISharedCursors.class.getResourceAsStream("icons/Tree_Add_Mask.gif")),
      new ImageData(ISharedCursors.class.getResourceAsStream("icons/Tree_Add.gif")),
      0,
      0);
  Cursor CURSOR_TREE_ADD_MAC = new Cursor(null,
      new ImageData(ISharedCursors.class.getResourceAsStream("icons/Tree_Add_Cursor2.gif")),
      0,
      0);
  Cursor CURSOR_ADD = new Cursor(null,
      new ImageData(ISharedCursors.class.getResourceAsStream("icons/add_cursor.gif")),
      0,
      0);
  Cursor CURSOR_MOVE = new Cursor(null,
      new ImageData(ISharedCursors.class.getResourceAsStream("icons/move_cursor.gif")),
      0,
      0);
  Cursor CURSOR_NO = new Cursor(null,
      new ImageData(ISharedCursors.class.getResourceAsStream("icons/no_cursor.gif")),
      0,
      0);
}