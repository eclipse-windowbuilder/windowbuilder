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
package org.eclipse.wb.internal.os.win32;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.os.OSSupport;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import org.apache.commons.lang3.ArrayUtils;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Win32 implementation for {@link OSSupport}.
 *
 * @author mitin_aa
 * @coverage os.win32
 */
public abstract class OSSupportWin32<H extends Number> extends OSSupport {
	private static Version SWT_VERSION = FrameworkUtil.getBundle(SWT.class).getVersion();
	static {
		System.loadLibrary("wbp");
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	protected static final OSSupport INSTANCE = new Impl64();

	////////////////////////////////////////////////////////////////////////////
	//
	// Screen Shot
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final void makeShots(Control control) throws Exception {
		try {
			reverseDrawingOrder(control);
			makeShotsHierarchy(control);
		} finally {
			reverseDrawingOrder(control);
		}
	}

	/**
	 * For unknown reason, when we do screen shot using WM_PRINT, <em>last</em> {@link Control} is
	 * displayed on the top of drawing order, however when we run application, Windows displays
	 * <em>first</em> on the top of drawing order. So, we reverse drawing order before making screen
	 * shot, and restore drawing order after this.
	 */
	private static void reverseDrawingOrder(Control control) {
		// 20130423(scheglov) disabled because of http://www.eclipse.org/forums/index.php/t/476687/
		//    if (control instanceof Composite) {
		//      Composite composite = (Composite) control;
		//      for (Control child : composite.getChildren()) {
		//        child.moveAbove(null);
		//        reverseDrawingOrder(child);
		//      }
		//    }
	}

	/**
	 * Creates screen shots for all {@link Control}'s in hierarchy marked with
	 * <code>WBP_NEED_IMAGE</code>.
	 */
	private void makeShotsHierarchy(Control control) throws Exception {
		if (control.getData(WBP_NEED_IMAGE) != null) {
			// check size
			Point size = control.getSize();
			if (size.x == 0 || size.y == 0) {
				return;
			}
			// set image
			control.setData(WBP_IMAGE, makeShot(control));
			// create images for children
			if (control instanceof Composite composite) {
				for (Control child : composite.getChildren()) {
					makeShotsHierarchy(child);
				}
			}
		}
	}

	@Override
	public final Image makeShot(Control control) throws Exception {
		Rectangle bounds = control.getBounds();
		if (bounds.width == 0 || bounds.height == 0) {
			return null;
		}
		Image image = new Image(Display.getCurrent(), bounds.width, bounds.height);
		GC gc = new GC(image);
		try {
			makeShotImpl(control, gc);
		} finally {
			gc.dispose();
		}
		//
		return image;
	}

	protected void makeShotImpl(Control control, GC gc) {
		_makeShot(getHandleField(control), getHandleField(gc));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// TabItem
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final Rectangle getTabItemBounds(TabItem tabItem) {
		TabFolder tabFolder = tabItem.getParent();
		int index = ArrayUtils.indexOf(tabFolder.getItems(), tabItem);
		int[] bounds = new int[4];
		getTabItemBounds(tabFolder, index, bounds);
		// convert into Rectangle
		int borderOffset = (tabFolder.getStyle() & SWT.BORDER) != 0 ? 2 : 0;
		int x = bounds[0]/*itemRect.left*/ + borderOffset;
		int y = bounds[2]/*itemRect.top*/ + borderOffset;
		int width = bounds[1]/*itemRect.right*/ - bounds[0]/*itemRect.left*/;
		int height = bounds[3]/*itemRect.bottom*/ - bounds[2]/*itemRect.top*/;
		return new Rectangle(x, y, width, height);
	}

	private void getTabItemBounds(TabFolder tabFolder, int index, int[] bounds) {
		_getTabItemBounds(getHandleField(tabFolder), index, bounds);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Menu
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final Rectangle getMenuBarBounds(Menu menu) {
		Decorations shell = getMenuParent(menu);
		int[] bounds = new int[4];
		if (getMenuBarOrItemBounds(shell, 0, bounds)) {
			int width = bounds[1] - bounds[0]; // info.right - info.left;
			int height = bounds[3] - bounds[2]; // info.bottom - info.top;
			//
			Point shellLocation = shell.getLocation();
			return new Rectangle(bounds[0] /*info.left*/ - shellLocation.x,
					bounds[2] /*info.top*/
							- shellLocation.y,
							width,
							height);
		}
		throw new RuntimeException("OS function call failed.");
	}

	@Override
	public Image getMenuBarVisualData(Menu menu, List<Rectangle> dimensions) {
		Decorations shell = getMenuParent(menu);
		int[] offsetBounds = new int[4];
		if (!getMenuBarOrItemBounds(shell, 1, offsetBounds)) {
			throw new RuntimeException("OS function call failed.");
		}
		for (int index = 0; index < menu.getItemCount(); ++index) {
			int[] bounds = new int[4];
			if (!getMenuBarOrItemBounds(shell, index + 1, bounds)) {
				throw new IllegalStateException("OS function call failed.");
			}
			int x = bounds[0] - offsetBounds[0]; // barInfo.left - offsetBarInfo.left;
			int y = bounds[2] - offsetBounds[2]; // barInfo.top - offsetBarInfo.top;
			int width = bounds[1] - bounds[0]; // barInfo.right - barInfo.left;
			int height = bounds[3] - bounds[2]; // barInfo.bottom - barInfo.top;
			dimensions.add(new Rectangle(x, y, width, height));
		}
		return null;
	}

	/**
	 * Checks this menu parent, it should have menu bar set for the given menu.
	 */
	private Decorations getMenuParent(Menu menu) {
		Decorations shell = menu.getParent();
		if (shell.getMenuBar() != menu) {
			throw new IllegalArgumentException("Invalid menu parent.");
		}
		return shell;
	}

	private boolean getMenuBarOrItemBounds(Decorations shell, int index, int[] bounds) {
		return _getMenuBarOrItemBounds(getHandleField(shell), index, bounds);
	}

	@Override
	public Image getMenuPopupVisualData(Menu menu, int[] bounds) throws Exception {
		// create fake image
		Image image = new Image(Display.getCurrent(), 1, 1);
		// free system resource
		_DeleteObject(getHandleField(image));
		H handle =
				_fetchPopupMenuVisualData(getHandleField(menu.getShell()), getHandleField(menu), bounds);
		// set new handle to image
		ReflectionUtils.setField(image, "handle", handle);
		return image;
	}

	@Override
	public int getDefaultMenuBarHeight() {
		return _getDefaultMenuBarHeight();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tree click
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean isPlusMinusTreeClick(Tree tree, int x, int y) {
		return _isPlusMinusTreeClick(getHandleField(tree), x, y);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Misc
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void scroll(Control cursorControl, int count) {
		_scroll(getHandleField(cursorControl), count);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the "handle" field value of given <code>object</code>.
	 */
	protected abstract H getHandleField(Object object);

	////////////////////////////////////////////////////////////////////////////
	//
	// Native
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Calls GetMenuBarInfo and fills the given array of int with bounds in a left, right, top, bottom
	 * sequence.
	 *
	 * @param shellHandle
	 *          the handle of {@link Shell} which bar menu belongs to.
	 * @param itemIndex
	 *          the item index to fetch bounds of. Passing zero value returns the bounds of menu bar.
	 * @param bounds
	 *          the array of integer with size 4.
	 */
	private static native <H extends Number> boolean _getMenuBarOrItemBounds(H shellHandle,
			int itemIndex,
			int[] bounds);

	/**
	 * Fetches the menu data: returns item bounds as plain array and the HBITMAP of menu image.
	 *
	 * @param shellHandle
	 *          the handle of menu parent shell.
	 * @param menuHandle
	 *          the handle of menu.
	 * @param bounds
	 *          the array of integer with size 4 * menu item count.
	 * @return the HBITMAP of menu widget.
	 */
	private static native <H extends Number> H _fetchPopupMenuVisualData(H shellHandle,
			H menuHandle,
			int[] itemBounds);

	/**
	 * @return the result of GetSystemMetrics(SM_CYMENU) invocation;
	 */
	private static native int _getDefaultMenuBarHeight();

	/**
	 * Scrolls by <code>count</code> positions.
	 */
	private static native <H extends Number> void _scroll(H handle, int count);

	/**
	 * Causes taking the screen shot.
	 *
	 * @param windowHandle
	 *          the handle of {@link Shell}.
	 * @param dcHandle
	 *          the handle {@link GC} to paint to.
	 */
	private static native <H extends Number> void _makeShot(H windowHandle, H dcHandle);

	/**
	 * @return <code>true</code> if pointer is over {@link TreeItem} plus/minus sign.
	 */
	private static native <H extends Number> boolean _isPlusMinusTreeClick(H handle, int x, int y);

	/**
	 * Sets the <code>alpha</code> value for given <code>shell</code>.
	 *
	 * @param shellHandle
	 *          the handle of {@link Shell}.
	 * @param alpha
	 *          the value of alpha, 0-255, not validated.
	 */
	private static native <H extends Number> void _setAlpha(H shellHandle, int alpha);

	/**
	 * Returns the current alpha value for given <code>shellHandle</code>.
	 *
	 * @param shellHandle
	 *          the handle of {@link Shell}.
	 * @return the alpha value.
	 */
	private static native <H extends Number> int _getAlpha(H shellHandle);

	/**
	 * Fills the given array of int with bounds in a left, right, top, bottom sequence.
	 *
	 * @param tabFolderHandle
	 *          the handle of {@link TabFolder}.
	 * @param itemIndex
	 *          the {@link TabItem} index to fetch bounds of.
	 * @param bounds
	 *          the array of integer with size 4.
	 */
	private static native <H extends Number> void _getTabItemBounds(H tabFolderHandle,
			int itemIndex,
			int[] bounds);

	/**
	 * Simply calls DeleteObject() for given <code>handle</code>.
	 */
	private static native <H extends Number> void _DeleteObject(H handle);
	////////////////////////////////////////////////////////////////////////////
	//
	// Implementations
	//
	////////////////////////////////////////////////////////////////////////////

	private static final class Impl64 extends OSSupportWin32<Long> {
		@Override
		protected Long getHandleField(Object object) {
			return ReflectionUtils.getFieldLong(object, "handle");
		}

		@Override
		public Image getMenuPopupVisualData(Menu menu, int[] bounds) throws Exception {
			// With 3.129.0, the single "handle" field has been removed from the Image class
			// to better handle displays with different zoom levels
			if (SWT_VERSION.compareTo(new Version(3, 129, 0)) >= 0) {
				int initialNativeZoom = Display.getCurrent().getPrimaryMonitor().getZoom();
				Long handle = _fetchPopupMenuVisualData(getHandleField(menu.getShell()), getHandleField(menu), bounds);
				Method method = ReflectionUtils.getMethod(Image.class, "win32_new", Device.class, int.class, long.class, int.class);
				return (Image) method.invoke(null, Display.getCurrent(), SWT.BITMAP, handle, initialNativeZoom);
			}
			return super.getMenuPopupVisualData(menu, bounds);
		}
	}
}
