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
package org.eclipse.wb.internal.ercp.eswt;

import org.eclipse.ercp.swt.mobile.Command;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

import java.lang.reflect.Field;

/**
 * @author lobas_av
 * 
 */
public class EmbeddedScreenShotMaker {
  static {
    /*
     * [mitin_aa] I'll keep two dlls for backward compatibility.
     */
    {
      String libraryPath = System.getProperty("SWTDesigner-613C5105-E42D-4b7c-B97B-AE127B880576");
      if (libraryPath == null) {
        System.loadLibrary("SWTDesigner");
      } else {
        System.load(libraryPath);
      }
    }
    {
      String libraryPath = System.getProperty("EmbeddedShot-A4D28DAF-F036-4219-A942-C8244AC5595D");
      if (libraryPath == null) {
        System.loadLibrary("EmbeddedShot");
      } else {
        System.load(libraryPath);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ScreenShot
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates screen shots for all {@link Control}'s in hierarchy marked with
   * <code>WBP_NEED_IMAGE</code>. Handles of created {@link Image}'s are located in
   * <code>WBP_IMAGE_HANDLE</code> data.
   */
  public static void makeShots(Control control) {
    Shell shell = control.getShell();
    // if shell has no layout call layout manually
    //System.out.println("shell.getLayout(): " + shell.getLayout() + "   " + control);
    if (shell.getLayout() == null) {
      Control[] children = shell.getChildren();
      for (int i = 0; i < children.length; i++) {
        if (children[i] instanceof Composite) {
          Composite composite = (Composite) children[i];
          composite.layout();
        }
      }
    } else {
      shell.layout();
    }
    // do create shots
    shell.setLocation(-10000, -10000);
    showShell(shell);
    try {
      makeShotsHierarchy(control);
    } finally {
      hideShell(shell);
    }
  }

  /**
   * Creates screen shots for all {@link Control}'s in hierarchy marked with
   * <code>WBP_NEED_IMAGE</code>.
   */
  private static void makeShotsHierarchy(Control control) {
    if (control.getData("WBP_NEED_IMAGE") != null) {
      makeShot(control);
      // create images for children
      if (control instanceof Composite) {
        Composite composite = (Composite) control;
        Control[] children = composite.getChildren();
        if (children != null) {
          for (int i = 0; i < children.length; i++) {
            Control child = children[i];
            makeShotsHierarchy(child);
          }
        }
      }
    }
  }

  /**
   * Creates shot {@link Image} for single given {@link Control}.
   * 
   * @param control
   *          the {@link Control} to make shot from
   */
  public static void makeShot(Control control) {
    // check size
    Point size = control.getSize();
    if (size.x == 0 || size.y == 0) {
      return;
    }
    // prepare image
    Image image = new Image(null, size.x, size.y);
    try {
      // fill image
      {
        GC gc = new GC(image);
        try {
          embeddedMakeShot(control.internal_handle, gc.internal_handle);
        } finally {
          gc.dispose();
        }
      }
      // remember (big) SWT handle
      {
        int imageHandle = getSWTImageHandle(image);
        control.setData("WBP_IMAGE_HANDLE", new Integer(imageHandle));
      }
    } finally {
      image.dispose();
    }
  }

  /**
   * Converts eSWT {@link Image} into handle of "big" SWT {@link Image}.<br>
   * Note that we create copy of {@link Image} because we reuse its internal "handle" in "big" SWT
   * {@link Image}.
   */
  public static int getSWTImageHandle(Image image) {
    Image imageCopy = new Image(null, image.getImageData());
    return embeddedImageToSwt(imageCopy.internal_handle);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Shell utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Shows given {@link Shell}.<br>
   * Also removes {@link Command} filter from {@link Display} to avoid {@link Command}'s menu
   * displaying on screen shot.
   */
  private static void showShell(Shell shell) {
    // remove Command.cmdHandle filter
    try {
      Listener cmdHandle = getCmdHandleListener();
      shell.getDisplay().removeFilter(SWT.FocusIn, cmdHandle);
      shell.getDisplay().removeFilter(SWT.FocusOut, cmdHandle);
    } catch (Throwable e) {
      throw new Error(e);
    }
    // do show
    shell.setVisible(true);
  }

  /**
   * Hides given {@link Shell}.<br>
   * Also restores {@link Command} filter in {@link Display}.
   */
  private static void hideShell(Shell shell) {
    // restore Command.cmdHandle filter
    try {
      Listener cmdHandle = getCmdHandleListener();
      shell.getDisplay().addFilter(SWT.FocusIn, cmdHandle);
      shell.getDisplay().addFilter(SWT.FocusOut, cmdHandle);
    } catch (Throwable e) {
      throw new Error(e);
    }
    // do hide
    shell.setVisible(false);
  }

  /**
   * @return the {@link Command} "cmdHandle" filter {@link Listener}.
   */
  private static Listener getCmdHandleListener() throws NoSuchFieldException,
      IllegalAccessException {
    Field cmdHandleField = Command.class.getDeclaredField("cmdHandle");
    cmdHandleField.setAccessible(true);
    return (Listener) cmdHandleField.get(null);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Menu
  //
  ////////////////////////////////////////////////////////////////////////////
  public static int[] fetchPopupMenuVisualData(int shellHandle, int menuHandle, int itemCount) {
    // create new image and fetch item sizes
    int[] sizes = new int[itemCount * 4];
    int handle = fetchPopupMenuVisualData(shellHandle, menuHandle, sizes);
    // make result
    int[] result = new int[sizes.length + 1];
    System.arraycopy(sizes, 0, result, 1, sizes.length);
    result[0] = handle;
    return result;
  }

  /**
   * Fetch popup menu visual data, converting handles as needed.
   * 
   * @param menu
   *          A menu instance.
   * @return An array of int containing menu visual data. The first element contains menu image
   *         handle. And the rest are items bounds.
   */
  public static int[] fetchPopupMenuVisualData(Menu menu) {
    // convert eSwt handles to Swt handles
    int shellHandle = embeddedControlToSwt(menu.getShell().internal_handle);
    int menuHandle = embeddedMenuToSwt(menu.internal_handle);
    // fetch menu data
    return fetchPopupMenuVisualData(shellHandle, menuHandle, menu.getItemCount());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Native, SWTDesigner.dll
  //
  ////////////////////////////////////////////////////////////////////////////
  private static native int fetchPopupMenuVisualData(int shellHandle,
      int menuHandle,
      int[] itemSizes);

  /**
   * Get native handle of menu bar's parent shell.
   * 
   * @param menu
   *          A menu bar.
   * @return An integer of menu bar parent shell's handle.
   */
  public static int getNativeMenuBarParentHandle(Menu menu) {
    Decorations shell = menu.getParent();
    if (shell.getMenuBar() != menu) {
      throw new IllegalArgumentException("Invalid menu parent.");
    }
    return embeddedControlToSwt(menu.getShell().internal_handle);
  }

  /**
   * Get native handle of menu bar.
   * 
   * @param menu
   *          A menu bar.
   * @return An integer of menu bar handle.
   */
  public static int getNativeMenuBarHandle(Menu menu) {
    return embeddedMenuToSwt(menu.internal_handle);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Native
  //
  ////////////////////////////////////////////////////////////////////////////
  private static native void embeddedMakeShot(int handle, int image);

  private static native int embeddedControlToSwt(int control);

  private static native int embeddedMenuToSwt(int menu);

  private static native int embeddedImageToSwt(int image);
}