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

import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.os.OSSupport;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
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

import org.apache.commons.io.IOUtils;

import java.io.InputStream;

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
  public static final class Cocoa32 extends OSSupportMacOSXCocoa<Integer> {
    @Override
    protected Integer getID(Object control, String string) {
      Object fieldObject = ReflectionUtils.getFieldObject(control, string);
      return (Integer) ReflectionUtils.getFieldObject(fieldObject, "id");
    }
  }
  public static final class Cocoa64 extends OSSupportMacOSXCocoa<Long> {
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
      int menuHeight = 4; // 4px menu border
      int menuWidth = 5;
      // calc bounds first
      GC gc = new GC(menu.getDisplay());
      for (int i = 0; i < menu.getItemCount(); ++i) {
        int itemWidth = 24; // initial width as indent + place for check box
        int itemHeight;
        MenuItem item = menu.getItem(i);
        if ((item.getStyle() & SWT.SEPARATOR) != 0) {
          itemHeight = MENU_ITEM_SEPARATOR_HEIGHT;
        } else {
          Image itemImage = item.getImage();
          int imageHeight = 0;
          int textHeight = 0;
          if (itemImage != null) {
            Rectangle itemImageBounds = itemImage.getBounds();
            itemWidth += itemImageBounds.width + 5; // 5px is gap between image and text
            imageHeight = itemImageBounds.height;
          }
          String text = item.getText();
          if (text != null) {
            Point textDimensions = gc.stringExtent(text);
            itemWidth += textDimensions.x;
            textHeight = textDimensions.y;
          }
          itemHeight = 3 + Math.max(imageHeight, textHeight) + 3; // 3px border
        }
        bounds[i * 4 + 0] = 0; // x is always zero
        bounds[i * 4 + 1] = menuHeight; // current menu height
        bounds[i * 4 + 3] = itemHeight;
        menuHeight += itemHeight;
        menuWidth = Math.max(itemWidth, menuWidth);
      }
      menuHeight += 4; // 4px menu border
      menuWidth += 20; // space for 'cascade' image, always present
      // update items' width
      for (int i = 0; i < menu.getItemCount(); ++i) {
        bounds[i * 4 + 2] = menuWidth;
      }
      gc.dispose();
      // draw
      Image image = new Image(menu.getDisplay(), menuWidth, menuHeight);
      gc = new GC(image);
      gc.setBackground(IColorConstants.buttonLightest);
      gc.fillRectangle(image.getBounds());
      for (int i = 0; i < menu.getItemCount(); ++i) {
        MenuItem item = menu.getItem(i);
        int x = bounds[i * 4 + 0];
        int y = bounds[i * 4 + 1] + bounds[i * 4 + 3] / 2; // y-center of the item
        if ((item.getStyle() & SWT.SEPARATOR) != 0) {
          gc.setForeground(IColorConstants.lightGray);
          gc.drawLine(x, y, x + menuWidth, y);
        } else {
          if (item.getEnabled()) {
            gc.setForeground(IColorConstants.menuForeground);
          } else {
            gc.setForeground(IColorConstants.gray);
          }
          if (item.getSelection()) {
            Image checkImage = loadImage("check.png");
            int checkHalfHeight = checkImage.getBounds().height / 2;
            gc.drawImage(checkImage, x + 3, y - checkHalfHeight);
            checkImage.dispose();
          }
          x += 20; // space for the check image should be always added
          Image itemImage = item.getImage();
          if (itemImage != null) {
            Rectangle itemImageBounds = itemImage.getBounds();
            int imageHalfHeight = itemImageBounds.height / 2;
            gc.drawImage(itemImage, x, y - imageHalfHeight);
            x += itemImageBounds.width + 5;
          }
          String text = item.getText();
          if (text != null) {
            Point textDimensions = gc.stringExtent(text);
            gc.drawString(text, x, y - textDimensions.y / 2 - 1, true);
          }
          // draw cascade image if any
          if ((item.getStyle() & SWT.CASCADE) != 0) {
            Image cascadeImage = loadImage("cascade.png");
            Rectangle imageBounds = cascadeImage.getBounds();
            int itemWidth = bounds[i * 4 + 2];
            gc.drawImage(cascadeImage, itemWidth - imageBounds.width, y - imageBounds.height / 2);
            cascadeImage.dispose();
          }
        }
      }
      gc.dispose();
      return image;
    }

    private Image loadImage(String image) {
      InputStream imageStream = null;
      try {
        imageStream = getClass().getResourceAsStream(image);
        return new Image(null, imageStream);
      } catch (Throwable e) {
        // ignore
        return new Image(null, 1, 1);
      } finally {
        IOUtils.closeQuietly(imageStream);
      }
    }
  }
}
