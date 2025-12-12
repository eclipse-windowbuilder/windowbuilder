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
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.check.AssertionFailedException;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.DrawUtils;
import org.eclipse.wb.internal.swt.VisualDataMockupProvider;
import org.eclipse.wb.os.OSSupport;

import org.eclipse.draw2d.ColorConstants;
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
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;

import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public abstract class OSSupportLinux extends OSSupport {
	private static Version MINIMUM_VERSION = new Version(3, 126, 0);
	// constants
	private static final Color TITLE_BORDER_COLOR_DARKEST = DrawUtils.getShiftedColor(ColorConstants.titleBackground,
			-24);
	private static final Color TITLE_BORDER_COLOR_DARKER = DrawUtils.getShiftedColor(ColorConstants.titleBackground,
			-16);

	static {
		System.loadLibrary("wbp3");
	}

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
	private Map<Long, Control> m_controlsRegistry;
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
		m_controlsRegistry = new HashMap<>();
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
		if (control instanceof Composite composite) {
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
		long handle = getHandleValue(control, handleName);
		if (handle != 0) {
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
	public void beginShot(Control control) {
		Shell shell = layoutShell(control);
		// setup key title to be used by compiz WM (if enabled)
		if (!isWorkaroundsDisabled()) {
			// prepare
			_gtk_widget_show_now(getShellHandle(shell));
			try {
				Version currentVersion = FrameworkUtil.getBundle(SWT.class).getVersion();
				// Bug/feature is SWT: since the widget is already shown, the Shell.setVisible() invocation
				// has no effect, so we've end up with wrong shell trimming.
				// The workaround is to call adjustTrim() explicitly.
				if (currentVersion.compareTo(MINIMUM_VERSION) < 0) {
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
				_gtk_window_set_keep_above(getShellHandle(m_eclipseShell), true);
			}
		}
		shell.setLocation(10000, 10000);
		shell.setVisible(true);
	}

	@Override
	public void endShot(Control control) {
		// hide shell. The shell should be visible during all the period of fetching visual data.
		super.endShot(control);
		Shell shell = getShell(control);
		if (!isWorkaroundsDisabled()) {
			_gtk_widget_hide(getShellHandle(shell));
			if (m_eclipseShell != null) {
				_gtk_window_set_keep_above(getShellHandle(m_eclipseShell), false);
			}
		}
	}

	@Override
	public void makeShots(Control control) throws Exception {
		Shell shell = getShell(control);
		makeShots0(shell);
		// check for decorations and draw if needed
		drawDecorations(shell, shell.getDisplay());
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
		final Set<Image> disposeImages = new HashSet<>();
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

	/**
	 * Draws decorations if available/applicable.
	 */
	private void drawDecorations(Shell shell, final Display display) {
		Image shellImage = (Image) shell.getData(WBP_IMAGE);
		// draw title if any
		if (shellImage != null && (shell.getStyle() & SWT.TITLE) != 0) {
			Rectangle shellBounds = shell.getBounds();
			Rectangle imageBounds = shellImage.getBounds();
			Point offset = shell.toControl(shell.getLocation());
			offset.x = -offset.x;
			offset.y = -offset.y;
			// adjust by menu bar size
			if (shell.getMenuBar() != null) {
				offset.y -= getWidgetBounds(shell.getMenuBar()).height;
			}
			// draw
			Image decoratedShellImage = new Image(display, shellBounds);
			GC gc = new GC(decoratedShellImage);
			// draw background
			gc.setBackground(ColorConstants.titleBackground);
			gc.fillRectangle(0, 0, shellBounds.width, shellBounds.height);
			// title area gradient
			gc.setForeground(ColorConstants.titleGradient);
			gc.fillGradientRectangle(0, 0, shellBounds.width, offset.y, true);
			int buttonGapX = offset.x - 1;
			int nextPositionX;
			// buttons and title
			{
				// menu button
				Image buttonImage = Activator.getImage("decorations/button-menu-icon.png");
				Rectangle buttonImageBounds = buttonImage.getBounds();
				int buttonOffsetY = offset.y / 2 - buttonImageBounds.height / 2;
				gc.drawImage(buttonImage, buttonGapX, buttonOffsetY);
				nextPositionX = buttonGapX + buttonImageBounds.width + buttonGapX;
			}
			{
				// close button
				Image buttonImage = Activator.getImage("decorations/button-close-icon.png");
				Rectangle buttonImageBounds = buttonImage.getBounds();
				nextPositionX = shellBounds.width - buttonImageBounds.width - buttonGapX;
				int buttonOffsetY = offset.y / 2 - buttonImageBounds.height / 2;
				gc.drawImage(buttonImage, nextPositionX, buttonOffsetY);
				nextPositionX -= buttonGapX + buttonImageBounds.width;
			}
			{
				// maximize button
				Image buttonImage = Activator.getImage("decorations/button-max-icon.png");
				Rectangle buttonImageBounds = buttonImage.getBounds();
				int buttonOffsetY = offset.y / 2 - buttonImageBounds.height / 2;
				gc.drawImage(buttonImage, nextPositionX, buttonOffsetY);
				nextPositionX -= buttonGapX + buttonImageBounds.width;
			}
			{
				// minimize button
				Image buttonImage = Activator.getImage("decorations/button-min-icon.png");
				Rectangle buttonImageBounds = buttonImage.getBounds();
				int buttonOffsetY = offset.y / 2 - buttonImageBounds.height / 2;
				gc.drawImage(buttonImage, nextPositionX, buttonOffsetY);
			}
			// outline
			gc.setForeground(TITLE_BORDER_COLOR_DARKEST);
			gc.drawRectangle(offset.x - 1, offset.y - 1, imageBounds.width + 1, imageBounds.height + 1);
			gc.setForeground(TITLE_BORDER_COLOR_DARKER);
			gc.drawRectangle(offset.x - 2, offset.y - 2, imageBounds.width + 3, imageBounds.height + 3);
			// shell screen shot
			gc.drawImage(shellImage, offset.x, offset.y);
			// done
			gc.dispose();
			shellImage.dispose();
			shell.setData(WBP_IMAGE, decoratedShellImage);
		}
	}

	private boolean bindImage(final Control control, final Image image) {
		return ExecutionUtils.runObject(() -> {
			if (control.getData(WBP_NEED_IMAGE) != null && control.getData(WBP_IMAGE) == null) {
				control.setData(WBP_IMAGE, image);
				return true;
			}
			return false;
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
	protected abstract Image makeShot(Shell shell, BiConsumer<Long, Image> callback) throws Exception;

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
		GtkAllocation rect = new GtkAllocation();
		long widgetHandle = getHandleValue(widget, "handle");
		_gtk_widget_get_allocation(widgetHandle, rect);
		return new Rectangle(rect.x, rect.y, rect.width, rect.height);
	}

	/**
	 * @return the handle value of the {@link Shell} using reflection.
	 */
	protected long getShellHandle(Shell shell) {
		long widgetHandle = getHandleValue(shell, "fixedHandle");
		if (widgetHandle == 0) {
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
	 * @return the widget as native pointer for native handles. Note: returns
	 *         0 if handle cannot be obtained.
	 */
	protected abstract long getHandleValue(Object widget, String fieldName);

	/**
	 * @return the Image instance created by SWT internal method Image.gtk_new which uses external
	 *         GtkPixmap* or cairo_surface_t* pointer.
	 */
	protected abstract Image createImage0(long imageHandle) throws Exception;

	private Image createImage(long imageHandle) throws Exception {
		Image image = createImage0(imageHandle);
		// BUG in SWT: Image instance is not fully initialized
		// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=382175
		Image newImage = new Image(null, image.getImageData());
		image.dispose();
		return newImage;
	}

	private Image createImage(long sourceWindow, int width, int height) throws Exception {
		// Create the Cairo surface on which the snapshot is drawn on
		long /*cairo_surface_t*/ targetSurface = _cairo_image_surface_create(_CAIRO_FORMAT_ARGB32(), width, height);
		long /*cairo_t*/ cr = _cairo_create(targetSurface);
		// Get the visible region of the window
		// Wayland: Trying to take a screenshot of a partially unmapped widget
		// results in a SIGFAULT.
		long /* cairo_region_t */ visibleRegion = _gdk_window_get_visible_region(sourceWindow);
		// Set the visible region as the clip for the Cairo context
		_gdk_cairo_region(cr, visibleRegion);
		_cairo_clip(cr);
		// Paint the surface
		_gdk_cairo_set_source_window(cr, sourceWindow, 0, 0);
		_cairo_set_operator(cr, _CAIRO_OPERATOR_SOURCE());
		_cairo_paint(cr);
		// Cleanup
		_cairo_destroy(cr);
		_cairo_surface_flush(targetSurface);
		_cairo_region_destroy(visibleRegion);
		return createImage(targetSurface);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Menu
	//
	////////////////////////////////////////////////////////////////////////////

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
	public final Rectangle getTabItemBounds(TabItem tabItem) {
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
	// Tree
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean isPlusMinusTreeClick(Tree tree, int x, int y) {
		int[] cell_x = new int[1];
		int[] cell_y = new int[1];
		long[] /* GtkTreePath */ path = new long[1];
		long[] /* GtkTreeViewColumn */ column = new long[1];
		long /* GtkTreeView */ tree_view = getHandleValue(tree, "handle");
		//
		try {
			if (_gtk_tree_view_get_path_at_pos(tree_view, x, y, path, column, cell_x, cell_y)) {
				long expanderColumn = _gtk_tree_view_get_expander_column(tree_view);
				if (expanderColumn == column[0]) {
					GdkRectangle rect = new GdkRectangle();
					_gtk_tree_view_get_cell_area(tree_view, path[0], column[0], rect);
					if (x < rect.x) {
						return true;
					}
				}
			}
			return false;
		} finally {
			if (path[0] != 0) {
				_gtk_tree_path_free(path[0]);
			}
		}
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
	 *
	 * Retrieves the widget’s allocation.
	 *
	 * Note, when implementing a GtkContainer: a widget’s allocation will be its
	 * “adjusted” allocation, that is, the widget’s parent container typically calls
	 * gtk_widget_size_allocate() with an allocation, and that allocation is then
	 * adjusted (to handle margin and alignment for example) before assignment to
	 * the widget. gtk_widget_get_allocation() returns the adjusted allocation that
	 * was actually assigned to the widget. The adjusted allocation is guaranteed to
	 * be completely contained within the gtk_widget_size_allocate() allocation,
	 * however. So a GtkContainer is guaranteed that its children stay inside the
	 * assigned bounds, but not that they have exactly the bounds the container
	 * assigned. There is no way to get the original allocation assigned by
	 * gtk_widget_size_allocate(), since it isn’t stored; if a container
	 * implementation needs that information it will have to track it itself.
	 *
	 *
	 * @param widgetHandle the handle (GtkWidget*) of widget.
	 * @param rect         A pointer to a GtkAllocation to copy to.
	 */
	private static native void _gtk_widget_get_allocation(long widgetHandle, GtkAllocation rect);

	/**
	 *
	 * Asks to keep {@code window} above, so that it stays on top. Note that you
	 * shouldn’t assume the window is definitely above afterward, because other
	 * entities (e.g. the user or [window manager][gtk-X11-arch]) could not keep it
	 * above, and not all window managers support keeping windows above. But
	 * normally the window will end kept above. Just don’t write code that crashes
	 * if not.
	 *
	 * It’s permitted to call this function before showing a window, in which case
	 * the window will be kept above when it appears onscreen initially.
	 *
	 * You can track the above state via the “window-state-event” signal on
	 * GtkWidget.
	 *
	 * Note that, according to the Extended Window Manager Hints Specification, the
	 * above state is mainly meant for user preferences and should not be used by
	 * applications e.g. for drawing attention to their dialogs.
	 *
	 * @param windowHandle the handle (GtkWidget*) of root gtk widget of
	 *                     {@link Shell}.
	 * @param forceToggle  if <code>true</code> then toggling occurred without
	 *                     paying attention to current state.
	 */
	private static native void _gtk_window_set_keep_above(long windowHandle,
			boolean forceToggle);

	/**
	 * Shows a {@code widget}. If the {@code widget} is an unmapped toplevel widget
	 * (i.e. a GtkWindow that has not yet been shown), enter the main loop and wait
	 * for the window to actually be mapped. Be careful; because the main loop is
	 * running, anything can happen during this function.
	 */
	private static native boolean _gtk_widget_show_now(long windowHandle);

	/**
	 * Reverses the effects of {@link #_gtk_widget_show_now(Number)}, causing the
	 * {@code widget} to be hidden (invisible to the user).
	 */
	private static native boolean _gtk_widget_hide(long windowHandle);

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
	 * @param window          cast = (GdkWindow*).
	 * @param update_children Whether to also process updates for child windows.
	 * @deprecated Deprecated since: 3.22
	 */
	@Deprecated
	private static native void _gdk_window_process_updates(long window, boolean update_children);

	/**
	 * Checks whether the window has been mapped (with gdk_window_show() or
	 * gdk_window_show_unraised()).
	 *
	 * @param window cast = (GdkWindow*).
	 * @return {@code true} if the window is mapped.
	 */
	private static native boolean _gdk_window_is_visible(long window);

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
	 * @param window cast = (GdkWindow*)
	 * @param x      cast = (gint*)
	 * @param y      cast = (gint*)
	 * @param width  cast = (gint*)
	 * @param height cast = (gint*)
	 */
	private static native void _gdk_window_get_geometry(long window, int[] x, int[] y, int[] width,
			int[] height);

	/**
	 * <p>Returns the widget’s window if it is realized, {@code null} otherwise.</p>
	 *
	 * @param widget cast = (GtkWidget*)
	 * @return {@code widget}'s window. The data is owned by the instance. The
	 *         return value can be {@code null}.
	 */
	private static native long _gtk_widget_get_window(long widget);

	/**
	 * Whether {@code widget} can rely on having its alpha channel drawn correctly.
	 * On X11 this function returns whether a compositing manager is running for
	 * {@code widget} screen.
	 *
	 * Please note that the semantics of this call will change in the future if used
	 * on a widget that has a composited window in its hierarchy (as set by
	 * gdk_window_set_composited()).
	 *
	 * @return {@code true} if the widget can rely on its alpha channel being drawn
	 *         correctly.
	 * @deprecated Deprecated by {@code gdk_screen_is_composited()}
	 */
	@Deprecated(since = "GTK 3.22")
	private static native boolean _gtk_widget_is_composited(long widget);

	/**
	 * Fetches the requested opacity for this widget. See
	 * {@link #_gtk_widget_set_opacity(Number, int)}.
	 *
	 * @return The requested opacity for this widget.
	 */
	private static native double _gtk_widget_get_opacity(long widget);

	/**
	 * Request the {@code widget} to be rendered partially transparent, with opacity
	 * 0 being fully transparent and 1 fully opaque. (Opacity values are clamped to
	 * the [0,1] range.). This works on both toplevel widget, and child widgets,
	 * although there are some limitations:
	 *
	 * For toplevel widgets this depends on the capabilities of the windowing
	 * system. On X11 this has any effect only on X screens with a compositing
	 * manager running. See gtk_widget_is_composited(). On Windows it should work
	 * always, although setting a window’s opacity after the window has been shown
	 * causes it to flicker once on Windows.
	 *
	 * For child widgets it doesn’t work if any affected widget has a native window,
	 * or disables double buffering.
	 *
	 * @param alpha Desired opacity, between 0 and 1.
	 */
	private static native void _gtk_widget_set_opacity(long widget, double alpha);

	/**
	 * Returns the column that is the current expander column. This column has the
	 * expander arrow drawn next to it.
	 */
	private static native long _gtk_tree_view_get_expander_column(long tree_view);

	/**
	 * Fills the bounding rectangle in bin_window coordinates for the cell at the
	 * row specified by {@code path} and the column specified by {@code column}. If
	 * {@code path} is {@code NULL}, or points to a path not currently displayed,
	 * the {@code y} and {@code height} fields of the rectangle will be filled with
	 * 0. If column is {@code NULL}, the {@code x} and {@code width} fields will be
	 * filled with 0. The sum of all cell rects does not cover the entire tree;
	 * there are extra pixels in between rows, for example. The returned rectangle
	 * is equivalent to the {@code cell_area} passed to gtk_cell_renderer_render().
	 * This function is only valid if {@code tree_view} is realized.
	 */
	private static native void _gtk_tree_view_get_cell_area(long tree_view, long path, long column, GdkRectangle rect);

	/**
	 * Finds the path at the point ({@code x}, {@code y}), relative to bin_window
	 * coordinates (please see gtk_tree_view_get_bin_window()). That is, {@code x}
	 * and {@code y} are relative to an events coordinates. {@code x} and {@code y}
	 * must come from an event on the {@code tree_view} only where
	 * {@code event->window == gtk_tree_view_get_bin_window ()}. It is primarily for
	 * things like popup menus. If {@code path} is non-{@code NULL}, then it will be
	 * filled with the {@code GtkTreePath} at that point. This path should be freed
	 * with {@link #_gtk_tree_path_free()}. If {@code column} is non-{@code NULL},
	 * then it will be filled with the column at that point. {@code cell_x} and
	 * {@code cell_y} return the coordinates relative to the cell background (i.e.
	 * the {@code background_area} passed to gtk_cell_renderer_render()). This
	 * function is only meaningful if {@code tree_view} is realized. Therefore this
	 * function will always return FALSE if tree_view is not realized or does not
	 * have a model.<br>
	 * For converting widget coordinates (eg. the ones you get from
	 * GtkWidget::query-tooltip), please see
	 * gtk_tree_view_convert_widget_to_bin_window_coords().
	 */
	private static native boolean _gtk_tree_view_get_path_at_pos(long tree_view, int x, int y, long[] path,
			long[] column, int[] cell_x, int[] cell_y);

	/**
	 * Frees {@code path}. If {@code path} is {@code NULL}, it simply returns.
	 */
	private static native void _gtk_tree_path_free(long path);

	////////////////////////////////////////////////////////////////////////////
	//
	// GDK
	//
	////////////////////////////////////////////////////////////////////////////

	/**
	 *
	 *
	 * Computes the region of the {@code window} that is potentially visible. This
	 * does not necessarily take into account if the window is obscured by other
	 * windows, but no area outside of this region is visible.
	 *
	 * @return A {@code cairo_region_t}. This must be freed with
	 *         cairo_region_destroy() when you are done.
	 */
	private static native long _gdk_window_get_visible_region(long window);

	/**
	 * Adds the given region to the current path of {@code cr}.
	 */
	private static native void _gdk_cairo_region(long cr, long region);

	/**
	 *
	 *
	 * Sets the given window as the source pattern for {@code cr}.
	 *
	 * The pattern has an extend mode of {@code CAIRO_EXTEND_NONE} and is aligned so
	 * that the origin of {@code window} is {@code x}, {@code y}. The window
	 * contains all its subwindows when rendering.
	 *
	 * Note that the contents of {@code window} are undefined outside of the visible
	 * part of {@code window}, so use this function with care.
	 */
	private static native void _gdk_cairo_set_source_window(long cr, long window, double x, double y);

	////////////////////////////////////////////////////////////////////////////
	//
	// Cairo
	//
	////////////////////////////////////////////////////////////////////////////

	private static native int _CAIRO_FORMAT_ARGB32();

	private static native int _CAIRO_OPERATOR_SOURCE();

	/**
	 * Creates a new cairo_t with all graphics state parameters set to default
	 * values and with {@code target} as a target surface. The target surface should
	 * be constructed with a backend-specific function such as
	 * {@link #_cairo_image_surface_create()} (or any other
	 * cairo_backend_surface_create() variant).
	 *
	 * This function references {@code target} , so you can immediately call
	 * {@link _cairo_surface_destroy()} on it if you don't need to maintain a
	 * separate reference to it.
	 *
	 * @param target target surface for the context
	 * @return a newly allocated {@code cairo_t} with a reference count of 1. The
	 *         initial reference count should be released with
	 *         {@link #_cairo_destroy()} when you are done using the
	 *         {@code cairo_t}. This function never returns {@code NULL}. If memory
	 *         cannot be allocated, a special {@code cairo_t} object will be
	 *         returned on which {@link #_cairo_status()} returns
	 *         {@code CAIRO_STATUS_NO_MEMORY}. If you attempt to target a surface
	 *         which does not support writing (such as cairo_mime_surface_t) then a
	 *         {@code CAIRO_STATUS_WRITE_ERROR} will be raised. You can use this
	 *         object normally, but no drawing will be done.
	 */
	private static native long _cairo_create(long target);

	/**
	 * Creates an image surface of the specified format and dimensions. Initially
	 * the surface contents are set to 0. (Specifically, within each pixel, each
	 * color or alpha channel belonging to format will be 0. The contents of bits
	 * within a pixel, but not belonging to the given format are undefined).
	 *
	 * @param format format of pixels in the surface to create
	 * @param width  width of the surface, in pixels
	 * @param height height of the surface, in pixels
	 * @return a pointer to the newly created surface. The caller owns the surface
	 *         and should call {@link #_cairo_surface_destroy()} when done with it.
	 *
	 *         This function always returns a valid pointer, but it will return a
	 *         pointer to a "nil" surface if an error such as out of memory occurs.
	 *         You can use {@link #_cairo_surface_status()} to check for this.
	 */
	private static native long _cairo_image_surface_create(int format, int width, int height);

	/**
	 * Establishes a new clip region by intersecting the current clip region with
	 * the current path as it would be filled by {@link _cairo_fill()} and according
	 * to the current fill rule (see {@link #_cairo_set_fill_rule()}).
	 *
	 * After {@link #_cairo_clip()}, the current path will be cleared from the cairo
	 * context.
	 *
	 * The current clip region affects all drawing operations by effectively masking
	 * out any changes to the surface that are outside the current clip region.
	 *
	 * Calling {@link #_cairo_clip()} can only make the clip region smaller, never
	 * larger. But the current clip is part of the graphics state, so a temporary
	 * restriction of the clip region can be achieved by calling
	 * {@link #_cairo_clip()} within a
	 * {@link #_cairo_save()}/{@link #_cairo_restore()} pair. The only other means
	 * of increasing the size of the clip region is {@link _cairo_reset_clip()}.
	 *
	 * @param cr a cairo context
	 */
	private static native void _cairo_clip(long cr);

	/**
	 * A drawing operator that paints the current source everywhere within the
	 * current clip region.
	 *
	 * @param cr a cairo context
	 */
	private static native void _cairo_paint(long cr);

	/**
	 * Sets the compositing operator to be used for all drawing operations. See
	 * {@code cairo_operator_t} for details on the semantics of each available
	 * compositing operator.
	 *
	 * The default operator is {@code CAIRO_OPERATOR_OVER}.
	 *
	 * @param cr a cairo_t
	 * @param op a compositing operator, specified as a cairo_operator_t
	 */
	private static native void _cairo_set_operator(long cr, int op);

	/**
	 * Decreases the reference count on {@code cr} by one. If the result is zero,
	 * then {@code cr} and all associated resources are freed. See
	 * {@link #_cairo_reference()}.
	 *
	 * @param cr a cairo_t
	 */
	private static native void _cairo_destroy(long cr);

	/**
	 * Do any pending drawing for the surface and also restore any temporary
	 * modifications cairo has made to the surface's state. This function must be
	 * called before switching from drawing on the surface with cairo to drawing on
	 * it directly with native APIs, or accessing its memory outside of Cairo. If
	 * the surface doesn't support direct access, then this function does nothing.
	 *
	 * @param surface a cairo_surface_t
	 */
	private static native void _cairo_surface_flush(long surface);

	/**
	 * Destroys a {@code cairo_region_t} object created with
	 * {@link #_cairo_region_create()}, {@link #_cairo_region_copy()}, or or
	 * {@link #_cairo_region_create_rectangle()}.
	 *
	 * @param region a cairo_region_t
	 */
	private static native void _cairo_region_destroy(long region);

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
	 * @param methodSignature method signature. e.g. {@code gdk_window_show(long)}.
	 * @param args            method arguments.
	 * @return method return value. {@code null} for {@code void}.
	 */
	protected static final <T> T gdk(String methodSignature, Object... args) {
		return swt("org.eclipse.swt.internal.gtk.GDK", methodSignature, args);
	}

	/**
	 * <p>Utility class for calling interal SWT methods via reflection.</p>
	 * <p>We can't call those methods directly, as their classes aren't available on
	 * Windows and MacOS and thus would result in compile errors, if this workspace
	 * is checked out on those systems.</p>
	 * <p>The classes are loaded using the {@link OSSupportLinux} classloader.</p>
	 *
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
	private static final class Impl64 extends OSSupportLinux {
		private final VisualDataMockupProvider mockupProvider = new VisualDataMockupProvider();

		@Override
		protected long getHandleValue(Object widget, String fieldName) {
			if (ReflectionUtils.getFieldObject(widget, fieldName) instanceof Long longValue) {
				return longValue;
			}
			// field might be shadowed (e.g. in ImageBasedFrame)
			return 0L;
		}

		@Override
		public Image getMenuPopupVisualData(Menu menu, int[] bounds) throws Exception {
			return mockupProvider.mockMenuPopupVisualData(menu, bounds);
		}

		@Override
		protected Image createImage0(long imageHandle) throws Exception {
			return (Image) ReflectionUtils.invokeMethod2(
					Image.class,
					"gtk_new",
					Device.class,
					int.class,
					long.class,
					long.class,
					null,
					SWT.BITMAP,
					imageHandle,
					0);
		}

		private long findHandleValue(Widget widget) {
			if (widget instanceof Shell shell) {
				return getShellHandle(shell);
			}
			return getHandleValue(widget, "handle");
		}

		protected Image getImageSurface(Widget widget, BiConsumer<Long, Image> callback) throws Exception {
			long handle = findHandleValue(widget);
			long window = _gtk_widget_get_window(handle);
			if (!_gdk_window_is_visible(window)) {
				// don't deal with unmapped windows
				return null;
			}

			int[] x = new int[1], y = new int[1], width = new int[1], height = new int[1];
			_gdk_window_get_geometry(window, x, y, width, height);
			// force paint. Note, not all widgets do this completely, known so far is GtkTreeViewer.
			_gdk_window_process_updates(window, true);
			// take screenshot
			Image image = super.createImage(window, width[0], height[0]);
			// get Java code notified
			if (callback != null) {
				callback.accept(handle, image);
			}
			// done
			return image;
		}

		private Image traverse(Widget widget, BiConsumer<Long, Image> callback) throws Exception {
			Image image = getImageSurface(widget, callback);
			if (image == null) {
				return null;
			}
			if (widget instanceof Composite composite) {
				for (Control childWidget : composite.getChildren()) {
					Image childImage = traverse(childWidget, callback);
					if (childImage == null) {
						continue;
					}
					if (callback == null) {
						childImage.dispose();
					}
				}
			}
			return image;
		}

		@Override
		protected Image makeShot(Shell shell, BiConsumer<Long, Image> callback) throws Exception {
			return traverse(shell, callback);
		}
	}
}