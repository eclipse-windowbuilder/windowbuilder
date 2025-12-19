/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.os.linux;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.os.linux.gtk3.GTK3;
import org.eclipse.wb.internal.os.linux.gtk3.GTK3ScreenshotMaker;
import org.eclipse.wb.internal.os.linux.gtk3.GtkWindow;
import org.eclipse.wb.internal.swt.VisualDataMockupProvider;
import org.eclipse.wb.os.OSSupport;

import org.eclipse.swt.SWT;
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

import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;

import java.lang.foreign.Arena;
import java.util.List;

public final class OSSupportLinux extends OSSupport {
	private static Version SWT_VERSION_3_126 = new Version(3, 126, 0);
	private final VisualDataMockupProvider mockupProvider = new VisualDataMockupProvider();
	private final ScreenshotMaker screenshotMaker = new GTK3ScreenshotMaker();
	private Shell m_eclipseShell;

	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	protected static final OSSupport INSTANCE = new OSSupportLinux();

	////////////////////////////////////////////////////////////////////////////
	//
	// Screen shot
	//
	////////////////////////////////////////////////////////////////////////////

	@Override
	public void beginShot(Control control) {
		Shell shell = layoutShell(control);
		// setup key title to be used by compiz WM (if enabled)
		if (!isWorkaroundsDisabled()) {
			// prepare
			GTK3.gtk_widget_show_now(new GtkWidget(shell));
			try {
				Version currentVersion = FrameworkUtil.getBundle(SWT.class).getVersion();
				// Bug/feature is SWT: since the widget is already shown, the Shell.setVisible()
				// invocation
				// has no effect, so we've end up with wrong shell trimming.
				// The workaround is to call adjustTrim() explicitly.
				if (currentVersion.compareTo(SWT_VERSION_3_126) < 0) {
					ReflectionUtils.invokeMethod(shell, "adjustTrim()", new Object[0]);
				} else {
					ReflectionUtils.invokeMethod(shell, "adjustTrim(int,int)",
							new Object[] { SWT.DEFAULT, SWT.DEFAULT });
				}
			} catch (Throwable e) {
				DesignerPlugin.log(e);
			}
			m_eclipseShell = DesignerPlugin.getShell();
			// sometimes can be null, don't know why.
			if (m_eclipseShell != null) {
				GTK3.gtk_window_set_keep_above(new GtkWindow(m_eclipseShell), true);
			}
		}
		shell.setLocation(10000, 10000);
		shell.setVisible(true);
	}

	@Override
	public void endShot(Control control) {
		// hide shell. The shell should be visible during all the period of fetching visual data.
		super.endShot(control);
		Shell shell = control.getShell();
		if (!isWorkaroundsDisabled()) {
			GTK.gtk_widget_hide(new GtkWidget(shell));
			if (m_eclipseShell != null) {
				GTK3.gtk_window_set_keep_above(new GtkWindow(m_eclipseShell), false);
			}
		}
	}

	@Override
	public void makeShots(Control control) throws Exception {
		screenshotMaker.makeShots(control);
	}

	@Override
	public Image makeShot(Control control) throws Exception {
		Shell shell = control.getShell();
		shell.setLocation(10000, 10000);
		shell.setVisible(true);

		Rectangle controlBounds = control.getBounds();
		if (controlBounds.width == 0 || controlBounds.height == 0) {
			return null;
		}

		try {
			// apply shot magic
			return screenshotMaker.makeShot(shell, null);
		} finally {
			shell.setVisible(false);
		}
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
	// Tree
	//
	////////////////////////////////////////////////////////////////////////////

	@Override
	public boolean isPlusMinusTreeClick(Tree tree, int x, int y) {
		return false;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Troubleshooting
	//
	////////////////////////////////////////////////////////////////////////////
	private boolean isWorkaroundsDisabled() {
		return Boolean.parseBoolean(System.getProperty("__wbp.linux.disableScreenshotWorkarounds"));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	public static Rectangle getWidgetBounds(Widget w) {
		GtkWidget widget = new GtkWidget(w);
		try (Arena arena = Arena.ofConfined()) {
			GtkAllocation allocation = new GtkAllocation(arena);
			GTK.gtk_widget_get_allocation(widget, allocation);
			return new Rectangle(allocation.x(), allocation.y(), allocation.width(), allocation.height());
		}
	}
}
