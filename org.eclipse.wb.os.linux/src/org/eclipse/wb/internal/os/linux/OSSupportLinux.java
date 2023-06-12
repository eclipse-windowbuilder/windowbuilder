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
package org.eclipse.wb.internal.os.linux;

/**
 * OSSupport for Linux.
 *
 * @author mitin_aa
 *
 * @coverage os.linux
 */
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.check.AssertionFailedException;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.DrawUtils;
import org.eclipse.wb.internal.swt.VisualDataMockupProvider;
import org.eclipse.wb.os.OSSupport;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public abstract class OSSupportLinux<H extends Number> extends OSSupport {
	static {
		System.loadLibrary("wbp3");
	}

	// constants
	private static final Color TITLE_BORDER_COLOR_DARKEST =
			DrawUtils.getShiftedColor(IColorConstants.titleBackground, -24);
	private static final Color TITLE_BORDER_COLOR_DARKER =
			DrawUtils.getShiftedColor(IColorConstants.titleBackground, -16);
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	protected static final OSSupport INSTANCE = new Impl64();
	////////////////////////////////////////////////////////////////////////////
	//
	// Screen shot
	//
	////////////////////////////////////////////////////////////////////////////
	private Map<H, Control> m_controlsRegistry;
	private boolean m_eclipseToggledOnTop;
	private Shell m_eclipseShell;

	/**
	 * Prepares to screen shot: register controls. See {@link #registerControl(Control)} for details.
	 */
	private void prepareScreenshot(Shell shell) throws Exception {
		createRegistry();
		registerControl(shell);
		registerByHandle(shell, "shellHandle");
	}

	/**
	 * Creates the registry of {@link Control}s.
	 */
	private void createRegistry() {
		m_controlsRegistry = Maps.newHashMap();
	}

	/**
	 * Registers the control to be checked in screen shot callback. Every control can be registered
	 * multiple times. The first image handle received for this control in callback is "root" for this
	 * control and should be bound as {@link Image}.
	 */
	private void registerControl(Control control) throws Exception {
		// check size
		Point size = control.getSize();
		if (size.x == 0 || size.y == 0) {
			return;
		}
		{
			registerByHandle(control, "fixedHandle");
			registerByHandle(control, "handle");
		}
		control.setData(WBP_IMAGE, null);
		// traverse children
		if (control instanceof Composite) {
			Composite composite = (Composite) control;
			for (Control child : composite.getChildren()) {
				registerControl(child);
			}
		}
	}

	/**
	 * Tries to get the <code>handleName</code> {@link Field} from <code>control</code>. If the field
	 * exists, fills <code>m_needsImage</code>.
	 */
	private void registerByHandle(Control control, String handleName) throws Exception {
		H handle = getHandleValue(control, handleName);
		if (handle != null) {
			m_controlsRegistry.put(handle, control);
		}
	}

	/**
	 * Gets the {@link Shell} of given {@link Control}.
	 *
	 * @return the found parent {@link Shell} or throws {@link AssertionFailedException} if the given
	 *         <code>controlObject</code> is not instance of {@link Control}.
	 */
	private Shell getShell(Object controlObject) {
		Assert.instanceOf(Control.class, controlObject);
		Control control = (Control) controlObject;
		return control.getShell();
	}

	@Override
	public void beginShot(Object controlObject) {
		Shell shell = layoutShell(controlObject);
		// setup key title to be used by compiz WM (if enabled)
		if (!isWorkaroundsDisabled()) {
			// prepare
			_begin_shot(getShellHandle(shell));
			try {
				// Bug/feature is SWT: since the widget is already shown, the Shell.setVisible() invocation
				// has no effect, so we've end up with wrong shell trimming.
				// The workaround is to call adjustTrim() explicitly.
				ReflectionUtils.invokeMethod(shell, "adjustTrim()", new Object[0]);
			} catch (Throwable e) {
				DesignerPlugin.log(e);
			}
			m_eclipseShell = DesignerPlugin.getShell();
			// sometimes can be null, don't know why.
			if (m_eclipseShell != null) {
				m_eclipseToggledOnTop = _toggle_above(getShellHandle(m_eclipseShell), false);
			}
		}
		shell.setLocation(10000, 10000);
		shell.setVisible(true);
	}

	@Override
	public void endShot(Object controlObject) {
		// hide shell. The shell should be visible during all the period of fetching visual data.
		super.endShot(controlObject);
		Shell shell = getShell(controlObject);
		if (!isWorkaroundsDisabled()) {
			_end_shot(getShellHandle(shell));
			if (m_eclipseShell != null) {
				_toggle_above(getShellHandle(m_eclipseShell), m_eclipseToggledOnTop);
			}
		}
	}

	@Override
	public void makeShots(Object controlObject) throws Exception {
		Shell shell = getShell(controlObject);
		makeShots0(shell);
	}

	/**
	 * Screen shot algorithm is the following:
	 *
	 * <pre>
	 * 1. Register controls which requires the image. See {@link #registerControl(Control)}.
	 * 2. Create the callback, binding the screenshot to the model. See {@link #makeShot(Shell, BiConsumer)
	 * 3. While traversing the gtk widgets/gdk windows, the callback returns native widget handle and the
	 *    image. If the control corresponding to the widget handle has been found in the registry the
	 *    received image is bound to the control (see {@link #bindImage(Display, Control, int)}).
	 *    Otherwise, the image is disposed later (because it may be used in drawing).
	 * </pre>
	 */
	private void makeShots0(final Shell shell) throws Exception {
		prepareScreenshot(shell);
		final Set<Image> disposeImages = Sets.newHashSet();
		// apply shot magic
		makeShot(shell, (handle, image) -> {
			// get the registered control by handle
			Control imageForControl = m_controlsRegistry.get(handle);
			if (imageForControl == null || !bindImage(imageForControl, image)) {
				// this means given image handle used to draw the gtk widget internally
				disposeImages.add(image);
			}
		});
		// done, dispose image handles needed to draw internally.
		for (Image image : disposeImages) {
			image.dispose();
		}
	}

	private boolean bindImage(final Control control, final Image image) {
		return ExecutionUtils.runObject(new RunnableObjectEx<Boolean>() {
			@Override
			public Boolean runObject() throws Exception {
				if (control.getData(WBP_NEED_IMAGE) != null && control.getData(WBP_IMAGE) == null) {
					control.setData(WBP_IMAGE, image);
					return true;
				}
				return false;
			}
		});
	}

	/**
	 * Warning: single component only! Do not use for creating screen shot of hierarchy!
	 */
	@Override
	public Image makeShot(Control control) throws Exception {
		Shell shell = getShell(control);
		// get the handle for the control
		shell.setLocation(10000, 10000);
		shell.setVisible(true);
		Rectangle controlBounds = control.getBounds();
		if (controlBounds.width == 0 || controlBounds.height == 0) {
			return null;
		}
		try {
			// apply shot magic
			return makeShot(shell, null);
		} finally {
			shell.setVisible(false);
		}
	}

	/**
	 * Causes taking the screen shot.
	 *
	 * @param shell    the root {@link Shell} to capture.
	 * @param callback the callback instance for binding the snapshot to the data
	 *                 model. Can be <code>null</code>.
	 * @return the GdkPixmap* or cairo_surface_t* of {@link Shell}.
	 */
	protected abstract Image makeShot(Shell shell, BiConsumer<H, Image> callback);

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Calls native code, pass there the handle of {@link Control} and returns widget's bounds as
	 * {@link Rectangle}.
	 *
	 * @return the widget's bounds as {@link Rectangle}.
	 */
	private Rectangle getWidgetBounds(Object widget) {
		H widgetHandle = getHandleValue(widget, "handle");
		int[] sizes = new int[4];
		_getWidgetBounds(widgetHandle, sizes);
		return new Rectangle(sizes[0], sizes[1], sizes[2], sizes[3]);
	}

	/**
	 * @return the handle value of the {@link Shell} using reflection.
	 */
	protected H getShellHandle(Shell shell) {
		H widgetHandle = getHandleValue(shell, "fixedHandle");
		if (widgetHandle == null) {
			// may be null, roll back to "shellHandle"
			widgetHandle = getHandleValue(shell, "shellHandle");
		}
		return widgetHandle;
	}

	/**
	 * <p>
	 * If we are still using GTK3, we have to manually handle the GTK locks, in case we plan to run
	 * AWT operations within the SWT event queue. Otherwise there is the risk of a deadlock, in case
	 * SWT already holds a lock on the GDK threads and AWT attempts to acquire it as well.
	 * </p>
	 * <p>
	 * Access has to be done via reflection, in order to avoid compilation errors when checking out
	 * the workspace on Windows or MacOS, as the GTK classes are only available on a Linux system.
	 * </p>
	 *
	 * @return {@code true}, if WindowBuilder is already using GTK4. Otherwise {@code false}.
	 * @see #gdkThreadsEnter()
	 * @see #gdkThreadsLeave()
	 */
	private static boolean isGtk4() {
		try {
			return ReflectionUtils.getFieldBoolean(
					OSSupportLinux.class.getClassLoader().loadClass("org.eclipse.swt.internal.gtk.GTK"),
					"GTK4");
		} catch (ReflectiveOperationException e) {
			DesignerPlugin.log(e.getMessage(), e);
			return false;
		}
	}

	/**
	 * <p>
	 * Calls the native {@code gdk_threads_enter} method. Has to be done <b>after</b> performing an
	 * AWT operation to re-acquire the lock for the SWT event queue.
	 * </p>
	 * <p>
	 * Access has to be done via reflection, in order to avoid compilation errors when checking out
	 * the workspace on Windows or MacOS, as the GTK classes are only available on a Linux system.
	 * </p>
	 * <p>
	 * This method may <b>only</b> be called when using GTK3!
	 * </p>
	 *
	 * @see #isGtk4()
	 * @see <a href="https://docs.gtk.org/gdk3/func.threads_enter.html">GTK3</a>
	 */
	private static void gdkThreadsEnter() {
		gdk("gdk_threads_enter()");
	}

	/**
	 * <p>
	 * Calls the native {@code gdk_threads_leave} method. Has to be done <b>before</b> performing an
	 * AWT operation, to release the lock currently held by the SWT event queue.
	 * </p>
	 * <p>
	 * Access has to be done via reflection, in order to avoid compilation errors when checking out
	 * the workspace on Windows or MacOS, as the GTK classes are only available on a Linux system.
	 * </p>
	 * <p>
	 * This method may <b>only</b> be called when using GTK3!
	 * </p>
	 *
	 * @see #isGtk4()
	 * @see <a href="https://docs.gtk.org/gdk3/func.threads_leave.html">GTK3</a>
	 */
	private static void gdkThreadsLeave() {
		gdk("gdk_threads_leave()");
	}

	/**
	 * @return the H extends Number value as native pointer for native handles. Note: returns
	 *         <code>null</code> if handle is 0 or cannot be obtained.
	 */
	protected abstract H getHandleValue(Object widget, String fieldName);

	/**
	 * @return the Image instance created by SWT internal method Image.gtk_new which uses external
	 *         GtkPixmap* or cairo_surface_t* pointer.
	 */
	protected abstract Image createImage0(H imageHandle) throws Exception;

	private Image createImage(H imageHandle) throws Exception {
		Image image = createImage0(imageHandle);
		// BUG in SWT: Image instance is not fully initialized
		// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=382175
		Image newImage = new Image(null, image.getImageData());
		image.dispose();
		return newImage;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Menu
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Image getMenuPopupVisualData(Menu menu, int[] bounds) throws Exception {
		// create new image and fetch item sizes
		H handle = getHandleValue(menu, "handle");
		H imageHandle = _fetchMenuVisualData(handle, bounds);
		// set new handle to image
		return createImage(imageHandle);
	}

	/**
	 * Fetches the all menu bar item's bounds and returns as {@link List} of {@link Rectangle}.
	 */
	@Override
	public Image getMenuBarVisualData(Menu menu, List<Rectangle> bounds) {
		for (int i = 0; i < menu.getItemCount(); ++i) {
			MenuItem item = menu.getItem(i);
			bounds.add(getWidgetBounds(item));
		}
		return null;
	}

	/**
	 * Fetches the menu bar bounds.
	 */
	@Override
	public final Rectangle getMenuBarBounds(Menu menu) {
		Rectangle bounds = getWidgetBounds(menu);
		Shell shell = menu.getShell();
		Point p = shell.toControl(shell.getLocation());
		p.x = -p.x;
		p.y = -p.y - bounds.height;
		return new Rectangle(p.x, p.y, bounds.width, bounds.height);
	}

	@Override
	public final int getDefaultMenuBarHeight() {
		// no way :(
		return 24;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// TabItem
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final Rectangle getTabItemBounds(Object tabItem) {
		return getWidgetBounds(tabItem);
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
	// Alpha
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void setAlpha(Shell shell, int alpha) {
		_setAlpha(getShellHandle(shell), alpha);
	}

	@Override
	public int getAlpha(Shell shell) {
		return _getAlpha(getShellHandle(shell));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tree
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean isPlusMinusTreeClick(Tree tree, int x, int y) {
		return _isPlusMinusTreeClick(getHandleValue(tree, "handle"), x, y);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// AWT/Swing
	//
	////////////////////////////////////////////////////////////////////////////

	/**
	 * <p>
	 * Executes the given runnable synchronously within the AWT event queue. If the
	 * current thread is already the AWT thread, the job is executed directly.
	 * </p>
	 * <p>
	 * Linux synchronizes GTK calls. Meaning if this method is called from within
	 * the SWT UI thread, then we may have already acquired the lock. If we then
	 * execute the job in the AWT UI thread and wait for its completion, we risk a
	 * deadlock, as the AWT thread may also try to acquire the same lock.
	 * </p>
	 * <p>
	 * In order to avoid this problem, the SWT thread has to explicitly leave the
	 * critical region before and re-enter it, immediately after the AWT job has
	 * been executed. This operation is safe, as the current thread blocks any
	 * further SWT updates, meaning the AWT thread is the only one who can interact
	 * with GDK for this brief duration.
	 * </p>
	 * <p>
	 * Note that this behavior is only relevant for GTK3. Those methods have been
	 * marked as deprecated in GTK 3.6 and removed in GTK4. Threads are assumed to
	 * always be executed in the main thread, rendering this problem obsolete.
	 * </p>
	 *
	 * @param job The runnable to be executed in the AWT UI thread
	 */
	@Override
	public void runAwt(Runnable job) {
		Display display = Display.getCurrent();
		try {
			if (display != null && !isGtk4()) {
				gdkThreadsLeave();
			}
			super.runAwt(job);
		} finally {
			if (display != null && !isGtk4()) {
				gdkThreadsEnter();
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Native
	//
	////////////////////////////////////////////////////////////////////////////
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
	 * Fills the given array of int with bounds as x, y, width, height sequence.
	 *
	 * @param widgetHandle
	 *          the handle (GtkWidget*) of widget.
	 * @param bounds
	 *          the array of integer with size 4.
	 */
	private static native <H extends Number> void _getWidgetBounds(H widgetHandle, int[] bounds);

	/**
	 * Fetches the menu data: returns item bounds as plain array and the image handle of menu image.
	 *
	 * @param menuHandle
	 *          the handle (GtkWidget*) of menu.
	 * @param bounds
	 *          the array of integer with size 4 * menu item count.
	 * @return the GdkPixmap* or cairo_surface_t* of menu widget.
	 */
	private static native <H extends Number> H _fetchMenuVisualData(H menuHandle, int[] bounds);

	/**
	 * Toggles the "above" X Window property. If <code>forceToggle</code> is <code>false</code> then
	 * no toggling if window already has the "above" property set.
	 *
	 * @param windowHandle
	 *          the handle (GtkWidget*) of root gtk widget of {@link Shell}.
	 * @param forceToggle
	 *          if <code>true</code> then toggling occurred without paying attention to current state.
	 * @return <code>true</code> if toggling occurred.
	 */
	private static native <H extends Number> boolean _toggle_above(H windowHandle,
			boolean forceToggle);

	/**
	 * Prepares the preview window to screen shot.
	 */
	private static native <H extends Number> boolean _begin_shot(H windowHandle);

	/**
	 * Finalizes the process of screen shot.
	 */
	private static native <H extends Number> boolean _end_shot(H windowHandle);

	/**
	 * <p>Sends one or more expose events to window. The areas in each expose event
	 * will cover the entire update area for the window (see
	 * gdk_window_invalidate_region() for details). Normally GDK calls
	 * gdk_window_process_all_updates() on your behalf, so there’s no need to call
	 * this function unless you want to force expose events to be delivered
	 * immediately and synchronously (vs. the usual case, where GDK delivers them in
	 * an idle handler). Occasionally this is useful to produce nicer scrolling
	 * behavior, for example.</p>
	 *
	 * @param <H>             {@link Long} on a 64bit system, otherwise
	 *                        {@link Integer}.
	 * @param window          cast = (GdkWindow*).
	 * @param update_children Whether to also process updates for child windows.
	 * @deprecated Deprecated since: 3.22
	 */
	@Deprecated
	private static native <H extends Number> void _gdk_window_process_updates(H window, boolean update_children);

	/**
	 * Checks whether the window has been mapped (with gdk_window_show() or
	 * gdk_window_show_unraised()).
	 *
	 * @param <H>    {@link Long} on a 64bit system, otherwise {@link Integer}.
	 * @param window cast = (GdkWindow*).
	 * @return {@code true} if the window is mapped.
	 */
	private static native <H extends Number> boolean _gdk_window_is_visible(H window);

	/**
	 * <p>Any of the return location arguments to this function may be {@code null},
	 * if you aren’t interested in getting the value of that field.</p>
	 *
	 * <p>The X and Y coordinates returned are relative to the parent window of
	 * window, which for toplevels usually means relative to the window decorations
	 * (titlebar, etc.) rather than relative to the root window (screen-size
	 * background window).</p>
	 *
	 * <p>On the X11 platform, the geometry is obtained from the X server, so
	 * reflects the latest position of window; this may be out-of-sync with the
	 * position of window delivered in the most-recently-processed
	 * GdkEventConfigure. gdk_window_get_position() in contrast gets the position
	 * from the most recent configure event.</p>
	 *
	 * <p>Note: If window is not a toplevel, it is much better to call
	 * gdk_window_get_position(), gdk_window_get_width() and gdk_window_get_height()
	 * instead, because it avoids the roundtrip to the X server and because these
	 * functions support the full 32-bit coordinate space, whereas
	 * gdk_window_get_geometry() is restricted to the 16-bit coordinates of X11.</p>
	 *
	 * @param <H>    {@link Long} on a 64bit system, otherwise {@link Integer}.
	 * @param window cast = (GdkWindow*)
	 * @param x      cast = (gint*)
	 * @param y      cast = (gint*)
	 * @param width  cast = (gint*)
	 * @param height cast = (gint*)
	 */
	private static native <H extends Number> void _gdk_window_get_geometry(H window, int[] x, int[] y, int[] width,
			int[] height);

	/**
	 * <p>Returns the widget’s window if it is realized, {@code null} otherwise.</p>
	 *
	 * @param <H>    {@link Long} on a 64bit system, otherwise {@link Integer}.
	 * @param widget cast = (GtkWidget*)
	 * @return {@code widget}'s window. The data is owned by the instance. The
	 *         return value can be {@code null}.
	 */
	private static native <H extends Number> H _gtk_widget_get_window(H widget);

	////////////////////////////////////////////////////////////////////////////
	//
	// GDK/GTK wrappers
	//
	////////////////////////////////////////////////////////////////////////////

	/**
	 * <p>Utility class for calling the internal GDK methods of SWT.</p>
	 * <p>We can't call those methods directly, as the GDK class isn't available on
	 * Windows and MacOS and thus would result in compile errors, if this workspace
	 * is checked out on those systems.</p>
	 *
	 * @param <H>             {@link Long} on a 64bit system, otherwise
	 *                        {@link Integer}.
	 * @param methodSignature method signature. e.g. {@code gdk_window_show(long)}.
	 * @param args            method arguments.
	 * @return method return value. {@code null} for {@code void}.
	 */
	protected static final <T> T gdk(String methodSignature, Object... args) {
		return swt("org.eclipse.swt.internal.gtk.GDK", methodSignature, args);
	}

	/**
	 * <p>Utility class for calling the internal GTK methods of SWT.</p>
	 * <p>We can't call those methods directly, as the GTK class isn't available on
	 * Windows and MacOS and thus would result in compile errors, if this workspace
	 * is checked out on those systems.</p>
	 *
	 * @param <H>             {@link Long} on a 64bit system, otherwise
	 *                        {@link Integer}.
	 * @param methodSignature method signature. e.g. {@code g_object_unref(long)}.
	 * @param args            method arguments.
	 * @return method return value. {@code null} for {@code void}.
	 */
	protected static final <T> T gtk(String methodSignature, Object... args) {
		return swt("org.eclipse.swt.internal.gtk.GTK", methodSignature, args);
	}

	/**
	 * <p>Utility class for calling interal SWT methods via reflection.</p>
	 * <p>We can't call those methods directly, as their classes aren't available on
	 * Windows and MacOS and thus would result in compile errors, if this workspace
	 * is checked out on those systems.</p>
	 * <p>The classes are loaded using the {@link OSSupportLinux} classloader.</p>
	 *
	 * @param <H>             {@link Long} on a 64bit system, otherwise
	 *                        {@link Integer}.
	 * @param fullClassName   fully qualified class name.
	 * @param methodSignature method signature. e.g. {@code g_object_unref(long)}.
	 * @param args            method arguments.
	 * @return method return value. {@code null} for {@code void}.
	 */
	@SuppressWarnings("unchecked")
	private static final <T> T swt(String fullClassName, String methodSignature, Object... args) {
		return ExecutionUtils.runObject(() -> (T) ReflectionUtils.invokeMethod(
				OSSupportLinux.class.getClassLoader().loadClass(fullClassName), methodSignature, args)
				);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Implementations
	//
	////////////////////////////////////////////////////////////////////////////
	private static final class Impl64 extends OSSupportLinux<Long> {
		private final VisualDataMockupProvider mockupProvider = new VisualDataMockupProvider();

		@Override
		protected Long getHandleValue(Object widget, String fieldName) {
			long value = ReflectionUtils.getFieldLong(widget, fieldName);
			if (value != 0) {
				return value;
			}
			return null;
		}

		@Override
		public Image getMenuPopupVisualData(Menu menu, int[] bounds) throws Exception {
			return mockupProvider.mockMenuPopupVisualData(menu, bounds);
		}

		@Override
		protected Image createImage0(Long imageHandle) throws Exception {
			return (Image) ReflectionUtils.invokeMethod2(
					Image.class,
					"gtk_new",
					Device.class,
					int.class,
					long.class,
					long.class,
					null,
					SWT.BITMAP,
					imageHandle.longValue(),
					0);
		}

		protected Image getImageSurface(Shell shell, Long window, BiConsumer<Long, Image> callback) {
			if (!_gdk_window_is_visible(window)) {
				// don't deal with unmapped windows
				return null;
			}

			int[] x = new int[1], y = new int[1], width = new int[1], height = new int[1];
			_gdk_window_get_geometry(window, x, y, width, height);
			// force paint. Note, not all widgets do this completely, known so far is GtkTreeViewer.
			_gdk_window_process_updates(window, true);
			// access a widget registered with the window
			long[] widget = new long[1];
			gdk("gdk_window_get_user_data(long,long[])", window, widget);
			// take screenshot
			Image image = getImageSurface(shell, widget[0], new Rectangle(x[0], y[0], width[0], height[0]));
			// get Java code notified
			if (callback != null) {
				callback.accept(widget[0], image);
			}
			// done
			return image;
		}

		private Image getImageSurface(Shell shell, Long widget, Rectangle surface) {
			// Take a shot of the entire shell, not just the client area.
			if (shell.getDisplay().findWidget(widget) instanceof Shell) {
				return getImageSurface(shell);
			}

			return getImageSurface(shell, surface);
		}

		private Image getImageSurface(Shell shell) {
			Image image = new Image(shell.getDisplay(), shell.getBounds());
			Point location = shell.getLocation();
			GC gc = new GC(shell.getDisplay());
			gc.copyArea(image, location.x, location.y);
			gc.dispose();
			return image;
		}

		private Image getImageSurface(Shell shell, Rectangle surface) {
			// Wayland: Trying to take a screenshot of a partially unmapped widget
			// results in a SIGFAULT.
			Rectangle visibleSurface = shell.getClientArea().intersection(surface);

			// Create "dummy" image in case of unmapped widget
			if (visibleSurface.width <= 0 || visibleSurface.height <= 0) {
				return new Image(shell.getDisplay(), 1, 1);
			}

			// Wayland: Trying to use the shell as a drawable results in a SIGFAULT.
			// Use the display instead.
			Point location = shell.toDisplay(visibleSurface.x, visibleSurface.y);

			Image image = new Image(shell.getDisplay(), visibleSurface);
			GC gc = new GC(shell.getDisplay());
			gc.copyArea(image, location.x, location.y);
			gc.dispose();
			return image;

		}

		private Image traverse(Shell shell, Long window, BiConsumer<Long, Image> callback) {
			Image image = getImageSurface(shell, window, callback);
			if (image == null) {
				return null;
			}
			/* GList */ Long children = gdk("gdk_window_get_children(long)", window);
			int length = gtk("g_list_length(long)", children);
			for (int i = 0; i < length; ++i) {
				Long childWindow = gtk("g_list_nth_data(long,int)", children, i);
				Image childImage = traverse(shell, childWindow, callback);
				if (childImage == null) {
					continue;
				}
				if (callback == null) {
					childImage.dispose();
				}
			}
			gtk("g_list_free(long)", children);
			return image;
		}

		@Override
		protected Image makeShot(Shell shell, BiConsumer<Long, Image> callback) {
			return traverse(shell, _gtk_widget_get_window(getShellHandle(shell)), callback);
		}
	}
}