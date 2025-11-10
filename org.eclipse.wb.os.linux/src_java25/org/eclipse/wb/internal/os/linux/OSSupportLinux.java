/*******************************************************************************
 * Copyright (c) 2025 Patrick Ziegler and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.os.linux;

import org.eclipse.wb.internal.os.linux.gtk3.GTK3;
import org.eclipse.wb.internal.swt.VisualDataMockupProvider;
import org.eclipse.wb.os.OSSupport;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;

import java.lang.foreign.Arena;
import java.util.List;

public final class OSSupportLinux extends OSSupport {
	private final VisualDataMockupProvider mockupProvider = new VisualDataMockupProvider();

	////////////////////////////////////////////////////////////////////////////
	//
	// Screen shot
	//
	////////////////////////////////////////////////////////////////////////////

	@Override
	public void makeShots(Object control) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public Image makeShot(Control control) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Menu
	//
	////////////////////////////////////////////////////////////////////////////

	@Override
	public Image getMenuPopupVisualData(Menu menu, int[] bounds) throws Exception {
		return mockupProvider.mockMenuPopupVisualData(menu, bounds);
	}

	@Override
	public Image getMenuBarVisualData(Menu menu, List<Rectangle> bounds) {
		for (int i = 0; i < menu.getItemCount(); ++i) {
			MenuItem item = menu.getItem(i);
			bounds.add(getWidgetBounds(item));
		}
		return null;
	}

	@Override
	public Rectangle getMenuBarBounds(Menu menu) {
		Rectangle bounds = getWidgetBounds(menu);
		Shell shell = menu.getShell();
		Point p = shell.toControl(shell.getLocation());
		p.x = -p.x;
		p.y = -p.y - bounds.height;
		return new Rectangle(p.x, p.y, bounds.width, bounds.height);
	}

	@Override
	public int getDefaultMenuBarHeight() {
		// no way :(
		return 24;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// TabItem
	//
	////////////////////////////////////////////////////////////////////////////

	@Override
	public Rectangle getTabItemBounds(TabItem item) {
		return getWidgetBounds(item);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Alpha
	//
	////////////////////////////////////////////////////////////////////////////

	@Override
	@SuppressWarnings("deprecation")
	public void setAlpha(Shell shell, int alpha) {
		GtkWidget widget = new GtkWidget(shell);
		if (GTK3.gtk_widget_is_composited(widget)) {
			GTK3.gtk_widget_set_opacity(widget, alpha / 255.0);
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public int getAlpha(Shell shell) {
		GtkWidget widget = new GtkWidget(shell);
		if (GTK3.gtk_widget_is_composited(widget)) {
			return (int) (GTK3.gtk_widget_get_opacity(widget) * 255);
		}
		return 255;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tree
	//
	////////////////////////////////////////////////////////////////////////////

	@Override
	public boolean isPlusMinusTreeClick(Tree tree, int x, int y) {
		// TODO Auto-generated method stub
		return false;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private Rectangle getWidgetBounds(Widget w) {
		GtkWidget widget = new GtkWidget(w);
		try (Arena arena = Arena.ofConfined()) {
			GtkAllocation allocation = new GtkAllocation(arena);
			GTK3.gtk_widget_get_allocation(widget, allocation);
			return new Rectangle(allocation.x(), allocation.y(), allocation.width(), allocation.height());
		}
	}
}
