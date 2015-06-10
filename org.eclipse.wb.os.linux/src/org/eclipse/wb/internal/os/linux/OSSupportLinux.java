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
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.check.AssertionFailedException;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.DrawUtils;
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

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class OSSupportLinux<H extends Number> extends OSSupport {
  static {
    String libName;
    try {
      libName = isGtk3() ? "wbp3" : "wbp";
    } catch (Throwable e) {
      libName = "wbp";
    }
    System.loadLibrary(libName);
  }

  private static boolean isGtk3() throws Exception {
    Class<?> OSClass = Class.forName("org.eclipse.swt.internal.gtk.OS");
    boolean isGtk3 = ReflectionUtils.getFieldBoolean(OSClass, "GTK3");
    return isGtk3;
  }

  // constants
  private static final Color TITLE_BORDER_COLOR_DARKEST = DrawUtils.getShiftedColor(
      IColorConstants.titleBackground,
      -24);
  private static final Color TITLE_BORDER_COLOR_DARKER = DrawUtils.getShiftedColor(
      IColorConstants.titleBackground,
      -16);
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  protected static final OSSupport INSTANCE = EnvironmentUtils.IS_64BIT_OS
      ? new Impl64()
      : new Impl32();
  private String m_oldShellText;
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
    Shell shell = getShell(controlObject);
    // setup key title to be used by compiz WM (if enabled)
    changeTitle(shell);
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
    } else {
      shell.setLocation(10000, 10000);
      shell.setVisible(true);
    }
  }

  @Override
  public void endShot(Object controlObject) {
    // hide shell. The shell should be visible during all the period of fetching visual data.
    Shell shell = getShell(controlObject);
    shell.setVisible(false);
    // restore title
    restoreTitle(shell);
    if (!isWorkaroundsDisabled()) {
      _end_shot(getShellHandle(shell));
      if (m_eclipseShell != null) {
        _toggle_above(getShellHandle(m_eclipseShell), m_eclipseToggledOnTop);
      }
    }
  }

  private void changeTitle(Shell shell) {
    m_oldShellText = shell.getText();
    shell.setText("__wbp_preview_window");
  }

  private void restoreTitle(Shell shell) {
    shell.setText(m_oldShellText);
  }

  @Override
  public void makeShots(Object controlObject) throws Exception {
    Shell shell = getShell(controlObject);
    makeShots0(shell);
    // check for decorations and draw if needed
    drawDecorations(shell, shell.getDisplay());
  }

  /**
   * Screen shot algorithm is the following:
   *
   * <pre>
   * 1. Register controls which requires the image. See {@link #registerControl(Control)}.
   * 2. Create the callback, which should be passed into native code. See {@link #_makeShot(int, IScreenshotCallback)}.
   * 3. While traversing the gtk widgets/gdk windows in native code, the callback returns native widget handle and the image handle
   *    for it (see {@link IScreenshotCallback}). At this time if the control found in registry, the received image handle converted
   *    into {@link Image} and bound to control (see {@link #bindImage(Display, Control, int)}).
   *    Otherwise, the image handle is disposed later (because it may be used in drawing in native code).
   * 4. Since its not possible to capture window decorations, it needs to be drawn manually. The root shell image replaced with
   *    the one with decorations (if applicable/available to draw).
   * </pre>
   */
  private void makeShots0(final Shell shell) throws Exception {
    prepareScreenshot(shell);
    // get the handle for the root window
    H shellHandle = getShellHandle(shell);
    final Set<H> disposeImageHandles = Sets.newHashSet();
    // apply shot magic
    _makeShot(shellHandle, new IScreenshotCallback<H>() {
      public void storeImage(H handle, H imageHandle) {
        // get the registered control by handle
        Control imageForControl = m_controlsRegistry.get(handle);
        if (imageForControl == null || !bindImage(imageForControl, imageHandle)) {
          // this means given image handle used to draw the gtk widget internally
          disposeImageHandles.add(imageHandle);
        }
      }
    });
    // done, dispose image handles needed to draw internally.
    for (H imageHandle : disposeImageHandles) {
      _disposeImageHandle(imageHandle);
    }
  }

  private boolean bindImage(final Control control, final H imageHandle) {
    return ExecutionUtils.runObject(new RunnableObjectEx<Boolean>() {
      public Boolean runObject() throws Exception {
        if (control.getData(WBP_NEED_IMAGE) != null && control.getData(WBP_IMAGE) == null) {
          Image image = createImage(imageHandle);
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
    changeTitle(shell);
    Rectangle controlBounds = control.getBounds();
    if (controlBounds.width == 0 || controlBounds.height == 0) {
      return null;
    }
    try {
      H widgetHandle = getHandleValue(shell, "fixedHandle");
      if (widgetHandle == null) {
        // may be null, roll back to "handle"
        widgetHandle = getHandleValue(shell, "handle");
      }
      // apply shot magic
      H imageHandle = _makeShot(widgetHandle, null);
      return createImage(imageHandle);
    } finally {
      shell.setVisible(false);
      restoreTitle(shell);
    }
  }

  /**
   * Draws decorations if available/applicable.
   */
  private void drawDecorations(Shell shell, final Display display) {
    Rectangle shellBounds = shell.getBounds();
    Image shellImage = (Image) shell.getData(WBP_IMAGE);
    if (shellImage != null) {
      // 27.02.2008: while using some window managers, such as compiz, the returned Shell
      // bounds are not include the window decorations geometry.
      // 17.11.2008: compiz works fine with GTK 2.14 and later
      Rectangle imageBounds = shellImage.getBounds();
      if (imageBounds.width != shellBounds.width || imageBounds.height != shellBounds.height) {
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
        gc.setBackground(IColorConstants.titleBackground);
        gc.fillRectangle(0, 0, shellBounds.width, shellBounds.height);
        // draw title if any
        if ((shell.getStyle() & SWT.TITLE) != 0) {
          // title area gradient
          gc.setForeground(IColorConstants.titleGradient);
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
            // Shell title
            String shellTitle = m_oldShellText;
            if (!StringUtils.isEmpty(shellTitle)) {
              gc.setForeground(IColorConstants.titleForeground);
              Point titleExtent = gc.stringExtent(shellTitle);
              gc.drawString(shellTitle, nextPositionX, offset.y / 2 - titleExtent.y / 2, true);
            }
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
  }

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
  private H getShellHandle(Shell shell) {
    H widgetHandle = getHandleValue(shell, "fixedHandle");
    if (widgetHandle == null) {
      // may be null, roll back to "shellHandle"
      widgetHandle = getHandleValue(shell, "shellHandle");
    }
    return widgetHandle;
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
    if (!isGtk3()) {
      // for gtk2 it's required to return an image as is, because there is another bug in SWT:
      // new Image(null, image.getImageData()) produces a garbage image if a source
      // image is created using gtk_new().
      return image;
    }
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
   * Causes taking the screen shot.
   *
   * @param windowHandle
   *          the handle (GtkWidget*) of root gtk widget of {@link Shell}.
   * @param callback
   *          the instance of {@link IScreenshotCallback}. Can be <code>null</code>.
   * @return the GdkPixmap* or cairo_surface_t* of {@link Shell}.
   */
  private static native <H extends Number> H _makeShot(H windowHandle,
      IScreenshotCallback<H> callback);

  /**
   * Do dispose for given <code>imageHandle</code>.
   */
  private static native <H extends Number> void _disposeImageHandle(H imageHandle);

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
  private static native <H extends Number> boolean _toggle_above(H windowHandle, boolean forceToggle);

  /**
   * Prepares the preview window to screen shot.
   */
  private static native <H extends Number> boolean _begin_shot(H windowHandle);

  /**
   * Finalizes the process of screen shot.
   */
  private static native <H extends Number> boolean _end_shot(H windowHandle);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementations
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final class Impl64 extends OSSupportLinux<Long> {
    @Override
    protected Long getHandleValue(Object widget, String fieldName) {
      long value = ReflectionUtils.getFieldLong(widget, fieldName);
      if (value != 0) {
        return value;
      }
      return null;
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
  }
  private static final class Impl32 extends OSSupportLinux<Integer> {
    @Override
    protected Integer getHandleValue(Object widget, String fieldName) {
      int value = ReflectionUtils.getFieldInt(widget, fieldName);
      if (value != 0) {
        return value;
      }
      return null;
    }

    @Override
    protected Image createImage0(Integer imageHandle) throws Exception {
      return (Image) ReflectionUtils.invokeMethod2(
          Image.class,
          "gtk_new",
          Device.class,
          int.class,
          int.class,
          int.class,
          null,
          SWT.BITMAP,
          imageHandle.intValue(),
          0);
    }
  }
}