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
import org.eclipse.wb.os.OSSupport;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

/**
 * Support for MacOSX for SWT Carbon.
 *
 * @author mitin_aa
 */
public final class OSSupportMacOSXCarbon extends OSSupportMacOSX {
  static {
    System.loadLibrary("wbp");
  }

  /**
   * Creates the {@link Image} by given native handles.
   *
   * @param imageHandle
   *          the native handle of the image.
   * @param width
   *          the width of the image.
   * @param height
   *          the height of the image.
   */
  private static Image createImageFromHandle(int imageHandle, int width, int height)
      throws Exception {
    if (imageHandle != 0) {
      // Create a temporary image using the captured image's handle
      Image tempImage = (Image) ReflectionUtils.invokeMethod(
          Image.class,
          "carbon_new(org.eclipse.swt.graphics.Device,int,int,int)",
          new Object[]{
              Display.getCurrent(),
              Integer.valueOf(SWT.BITMAP),
              Integer.valueOf(imageHandle),
              Integer.valueOf(0)});
      // Create the result image
      Image image = new Image(Display.getCurrent(), width, height);
      // Manually copy because the image's data handle isn't available
      GC gc = new GC(tempImage);
      gc.copyArea(image, 0, 0);
      gc.dispose();
      // Dispose of the temporary image allocated in the native call
      tempImage.dispose();
      return image;
    }
    return null;
  }

  @Override
  public Image makeShot(Control control) {
    try {
      Rectangle rect = control.getBounds();
      if (rect.width <= 0 || rect.height <= 0) {
        return null;
      }
      int controlHandle = -1;
      int shellHandle = -1;
      if (control instanceof Shell) {
        shellHandle = getIntegerField(control, "shellHandle");
        if (shellHandle != -1) {
          controlHandle = _HIViewGetRoot(shellHandle);
        }
      } else {
        controlHandle = getHandleField(control);
        shellHandle = getIntegerField(control.getShell(), "shellHandle");
      }
      int imageHandle = _makeShot(controlHandle, shellHandle);
      Image image = createImageFromHandle(imageHandle, rect.width, rect.height);
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

  ////////////////////////////////////////////////////////////////////////////
  //
  // Menu
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Image getMenuPopupVisualData(Menu menu, int[] bounds) throws Exception {
    int handle = getHandleField(menu);
    int menuSize[] = new int[4];
    int itemsBounds[] = new int[bounds.length];
    // mitin_aa: Sometimes calling fetchMenuVisualData native method results processing drawing updates,
    // causing NPEs while drawing menu figures, the possible solution is to call Display.readAndDispatch just before menu visual data cleanup
    int menuImageHandle = _fetchPopupMenuVisualData(handle, menuSize, itemsBounds);
    // for separator items there is no way to get item bounds because the separator item has no custom draw
    // flag and native part doesn't receive draw messages for to get item bounds.
    // the workaround is to approximate separator location based on previous item.
    MenuItem firstItem = menu.getItem(0);
    int itemOffsetX = isSeparatorItem(firstItem) ? DEFAULT_MENU_ITEM_OFFSET_X : itemsBounds[0];
    int itemOffsetY = isSeparatorItem(firstItem) ? DEFAULT_MENU_ITEM_OFFSET_Y : itemsBounds[1] / 2;
    for (int i = 0; i < menu.getItemCount(); ++i) {
      if (isSeparatorItem(menu.getItem(i))) {
        if (i > 0) {
          itemsBounds[i * 4 + 0] = itemsBounds[(i - 1) * 4 + 0];
          itemsBounds[i * 4 + 1] = itemsBounds[(i - 1) * 4 + 1] + itemsBounds[(i - 1) * 4 + 3];
        } else {
          itemsBounds[i * 4 + 0] = itemOffsetX;
          itemsBounds[i * 4 + 1] = itemOffsetY * 2;
        }
        itemsBounds[i * 4 + 2] = menuSize[2];
        itemsBounds[i * 4 + 3] = MENU_ITEM_SEPARATOR_HEIGHT;
      }
      //
      bounds[i * 4 + 0] = itemsBounds[i * 4 + 0] - itemOffsetX;
      bounds[i * 4 + 1] = itemsBounds[i * 4 + 1] - itemOffsetY;
      bounds[i * 4 + 2] = itemsBounds[i * 4 + 2];
      bounds[i * 4 + 3] = itemsBounds[i * 4 + 3];
    }
    return createImageFromHandle(menuImageHandle, menuSize[2], menuSize[3]);
  }

  @Override
  public int getAlpha(Shell shell) {
    return _getAlpha(getHandleField(shell));
  }

  @Override
  public void setAlpha(Shell shell, int alpha) {
    _setAlpha(getHandleField(shell), alpha);
  }

  @Override
  public int getDefaultMenuBarHeight() {
    return _getMenuBarHeight();
  }

  @Override
  public int[] getPushButtonInsets() {
    int insets[] = new int[4];
    Object insetsRect =
        ReflectionUtils.getFieldObject(DesignerPlugin.getStandardDisplay(), "buttonInset");
    insets[0] = ReflectionUtils.getFieldShort(insetsRect, "left");
    insets[1] = ReflectionUtils.getFieldShort(insetsRect, "right");
    insets[2] = ReflectionUtils.getFieldShort(insetsRect, "top");
    insets[3] = ReflectionUtils.getFieldShort(insetsRect, "bottom");
    return insets;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the "handle" field value of given <code>object</code>.
   */
  private int getHandleField(Object object) {
    return getIntegerField(object, "handle");
  }

  /**
   * @return the integer field value with given name of object. Same as
   *         {@link ReflectionUtils#getFieldInt(Object, String)}.
   */
  private int getIntegerField(Object object, String name) {
    try {
      return ReflectionUtils.getFieldInt(object, name);
    } catch (Throwable e) {
      throw ReflectionUtils.propagate(e);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Native
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Calls API function which returns the menu bar height.
   */
  private static native int _getMenuBarHeight();

  /**
   * Fetches the menu data: returns item bounds as plain array and the HBITMAP of menu image.
   *
   * @param menuHandle
   *          the handle of menu.
   * @param menuSize
   *          the bounds of menu.
   * @param bounds
   *          the array of integer with size 4 * menu item count.
   */
  private static native int _fetchPopupMenuVisualData(int menuHandle,
      int[] menuSize,
      int[] itemsSizes);

  /**
   * Simply calls HIViewGetRoot for given shell handle.
   *
   * @param shellHandle
   */
  private static native int _HIViewGetRoot(int shellHandle);

  /**
   * Creates the image of the control.
   *
   * @param controlHandle
   *          the native handle of control, <code>ControlRef</code>.
   * @param shellHandle
   *          the native handle of the root window (shell window), <code>WindowRef</code>.
   * @return the native handle of the image or zero if failed.
   */
  private static native int _makeShot(int controlHandle, int shellHandle);

  private static native void _setAlpha(int handle, int alpha);

  private static native int _getAlpha(int handle);
}
