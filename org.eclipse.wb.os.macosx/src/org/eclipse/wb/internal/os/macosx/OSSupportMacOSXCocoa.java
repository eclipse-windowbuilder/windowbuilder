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
package org.eclipse.wb.internal.os.macosx;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.VisualDataMockupProvider;
import org.eclipse.wb.os.OSSupport;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

/**
 * Support for MacOSX for SWT based on Cocoa framework.
 *
 * Generic version.
 *
 * @author mitin_aa
 */
public abstract class OSSupportMacOSXCocoa<H extends Number> extends OSSupportMacOSX {
	static {
		try {
			System.loadLibrary("wbp-cocoa");
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Shot
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void makeShellVisible(Shell shell) {
		shell.setVisible(true);
		// calling shell.setVisible() brings the window to front by calling -[NSWindow orderFront]
		// which causes flickering. The workaround is to send the window back immediately,
		// so window manager won't display it at the screen, but window views remains visible.
		_orderOut(getID(shell, "window"));
	}

	@Override
	public Image makeShot(Control control) throws Exception {
		try {
			Rectangle bounds = control.getBounds();
			if (bounds.width <= 0 || bounds.height <= 0) {
				return null;
			}
			H view = getID(control, "view");
			Image image = new Image(control.getDisplay(), bounds);
			GC gc = new GC(image);
			H context = getID(gc, "handle");
			if (control instanceof Shell) {
				_makeWindowShot(view, context);
			} else {
				Composite parent = control.getParent();
				_makeShot(view, getID(parent, "view"), context);
			}
			gc.dispose();
			control.setData(WBP_IMAGE, image);
			// process children if any
			if (control instanceof Composite && !(control instanceof Browser)) {
				Composite composite = (Composite) control;
				Control[] children = composite.getChildren();
				for (int i = 0; i < children.length; i++) {
					Control child = children[children.length - 1 - i];
					if (!child.isVisible()) {
						continue;
					}
					Image childImage = makeShot(child);
					if (childImage == null) {
						continue;
					}
					child.setData(OSSupport.WBP_IMAGE, childImage);
				}
			}
			// all done
			return image;
		} catch (Throwable e) {
			throw ReflectionUtils.propagate(e);
		}
	}

	/**
	 * @return the Cocoa id field.
	 */
	protected abstract H getID(Object control, String string);

	////////////////////////////////////////////////////////////////////////////
	//
	// Menu
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public int getDefaultMenuBarHeight() {
		return _getMenuBarHeight();
	}

	@Override
	public Image getMenuPopupVisualData(Menu menu, int[] bounds) throws Exception {
		H handle = getID(menu, "nsMenu");
		int menuSize[] = new int[4];
		int itemsBounds[] = new int[bounds.length];
		_fetchPopupMenuBounds(handle, menuSize);
		Image image = new Image(menu.getDisplay(), menuSize[2], menuSize[3]);
		GC gc = new GC(image);
		_fetchPopupMenuVisualData(handle, getID(gc, "handle"), itemsBounds);
		fixupSeparatorItems(menu, bounds, menuSize, itemsBounds);
		gc.dispose();
		return image;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Misc
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public int getAlpha(Shell shell) {
		return _getAlpha(getID(shell, "window"));
	}

	@Override
	public void setAlpha(Shell shell, int alpha) {
		_setAlpha(getID(shell, "window"), alpha);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// AWT/Swing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Image makeShotAwt(final Object component, final int width, final int height) {
		final Image[] toReturn = new Image[]{null};
		final Display display = DesignerPlugin.getStandardDisplay();
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				toReturn[0] = makeShotAwt0(display, component, width, height);
			}
		});
		return toReturn[0];
	}

	private Image makeShotAwt0(Display display, Object component, int width, int height) {
		GC gc = null;
		try {
			Image image = new Image(display, width, height);
			gc = new GC(image);
			H context = getID(gc, "handle");
			Number peerId = getComponentPeerId(component);
			Number parentId = findParentComponentPeerId(component);
			if (peerId == null || parentId == null || peerId.equals(parentId)) {
				return null;
			}
			_makeShot(peerId, parentId, context);
			return image;
		} catch (Throwable e) {
			// ignore and return null;
		} finally {
			if (gc != null) {
				gc.dispose();
			}
		}
		return null;
	}

	private static Number findParentComponentPeerId(Object component) throws Exception {
		for (component = getParentComponent(component); component != null; component =
				getParentComponent(component)) {
			Number peerId = getComponentPeerId(component);
			if (peerId != null) {
				return peerId;
			}
		}
		return null;
	}

	private static Object getParentComponent(Object component) throws Exception {
		return ReflectionUtils.invokeMethod2(component, "getParent");
	}

	private static Number getComponentPeerId(Object component) {
		try {
			boolean hasPeer = (Boolean) ReflectionUtils.invokeMethod2(component, "isDisplayable");
			if (hasPeer) {
				Object peer = ReflectionUtils.getFieldObject(component, "peer");
				if (peer != null) {
					return (Number) ReflectionUtils.invokeMethod2(peer, "getViewPtr");
				}
			}
		} catch (Throwable e) {
			// ignore and return null
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Native code
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Removes the window from screen by invoking -[NSWindow orderOut:].
	 *
	 * @param window
	 *          the native handle of the window, <code>NSWindow*</code>.
	 */
	private static native <H extends Number> void _orderOut(H window);

	/**
	 * Creates the image of the control.
	 *
	 * @param view
	 *          the native handle of the view of the control, <code>NSView*</code>.
	 * @param parentView
	 *          the native handle of the parent view of the control, <code>NSView*</code>.
	 * @param context
	 *          the native handle to the graphics context on which view should be drawn,
	 *          <code>NSGraphicsContext*</code>.
	 */
	private static native <H extends Number> void _makeShot(H view, H parentView, H context);

	/**
	 * Creates the image of the shell as NSView.
	 *
	 * @param view
	 *          the native handle of the root view of the shell, <code>NSView*</code>.
	 * @param context
	 *          the native handle to the graphics context on which view should be drawn,
	 *          <code>NSGraphicsContext*</code>.
	 */
	private static native <H extends Number> void _makeWindowShot(H view, H context);

	/**
	 * Calls API function which returns the menu bar height.
	 */
	private static native int _getMenuBarHeight();

	/**
	 * Fetches the menu data: returns item bounds as plain array and the draws the menu image on the
	 * given context
	 *
	 * @param menuHandle
	 *          the handle of menu.
	 * @param itemsSizes
	 *          the bounds of menu items (output).
	 * @param context
	 *          the native handle to the graphics context on which menu should be drawn,
	 *          <code>NSGraphicsContext*</code>.
	 */
	private static native <H extends Number> void _fetchPopupMenuVisualData(H menuHandle,
			H context,
			int[] itemsSizes);

	/**
	 * Fetches the menu bounds.
	 *
	 * @param menuHandle
	 *          the handle of menu.
	 * @param menuSize
	 *          the bounds of menu (output).
	 */
	private static native <H extends Number> void _fetchPopupMenuBounds(H menuHandle, int[] menuSize);

	/**
	 * Sets alpha value to NSWindow.
	 */
	private static native <H extends Number> void _setAlpha(H handle, int alpha);

	/**
	 * Gets alpha value from NSWindow.
	 */
	private static native <H extends Number> int _getAlpha(H handle);

	////////////////////////////////////////////////////////////////////////////
	//
	// Implementations
	//
	////////////////////////////////////////////////////////////////////////////
	public static final class Cocoa64 extends OSSupportMacOSXCocoa<Long> {
		private final VisualDataMockupProvider mockupProvider = new VisualDataMockupProvider();

		@Override
		protected Long getID(Object control, String string) {
			Object fieldObject = ReflectionUtils.getFieldObject(control, string);
			return (Long) ReflectionUtils.getFieldObject(fieldObject, "id");
		}

		/**
		 * 64-bit Cocoa has no way to get the screen shot of the popup menu.
		 */
		@Override
		public Image getMenuPopupVisualData(Menu menu, int[] bounds) throws Exception {
			return mockupProvider.mockMenuPopupVisualData(menu, bounds);
		}
	}
}
