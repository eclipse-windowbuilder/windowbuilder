/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.gef.core;

import org.eclipse.draw2d.Cursors;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.ImageData;

/**
 * A shared collection of Cursors.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public class SharedCursors extends Cursors {
	public static final Cursor CURSOR_TREE_ADD = new Cursor(null,
			new ImageData(SharedCursors.class.getResourceAsStream("icons/Tree_Add_Mask.gif")),
			new ImageData(SharedCursors.class.getResourceAsStream("icons/Tree_Add.gif")),
			0,
			0);
	public static final Cursor CURSOR_TREE_ADD_MAC = new Cursor(null,
			new ImageData(SharedCursors.class.getResourceAsStream("icons/Tree_Add_Cursor2.gif")),
			0,
			0);
	public static final Cursor CURSOR_ADD = new Cursor(null,
			new ImageData(SharedCursors.class.getResourceAsStream("icons/add_cursor.gif")),
			0,
			0);
	public static final Cursor CURSOR_MOVE = new Cursor(null,
			new ImageData(SharedCursors.class.getResourceAsStream("icons/move_cursor.gif")),
			0,
			0);
	public static final Cursor CURSOR_NO = new Cursor(null,
			new ImageData(SharedCursors.class.getResourceAsStream("icons/no_cursor.gif")),
			0,
			0);
}