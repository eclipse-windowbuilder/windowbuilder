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
package org.eclipse.wb.internal.ercp.support;

import com.google.common.collect.Lists;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.menu.MenuVisualData;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.support.ControlSupport;
import org.eclipse.wb.internal.swt.support.DisplaySupport;
import org.eclipse.wb.internal.swt.support.IToolkitSupport;
import org.eclipse.wb.internal.swt.support.SwtSupport;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.internal.win32.MENUBARINFO;
import org.eclipse.swt.internal.win32.OS;
import org.eclipse.swt.internal.win32.RECT;
import org.eclipse.swt.widgets.Display;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link IToolkitSupport} for eRCP.
 * 
 * @author lobas_av
 * @author mitin_aa
 * @coverage ercp.support
 */
@SuppressWarnings("restriction")
public final class ToolkitSupportImpl implements IToolkitSupport {
  private final Class<?> ImageClass;
  private final Class<?> displayClass;
  //
  private final Method m_makeShots;
  private final Method m_makeShot;
  private final Method m_getSWTImageHandle;
  private final Method m_updateFont;
  private final Object m_fontPreviewShell;
  private final Method m_OS_Device_GetTypefaces;
  private final Field m_internal_handle;
  // menu related
  private final Method m_fetchPopupMenuVisualData;
  private final Method m_getNativeMenuBarHandle;
  private final Method m_getNativeMenuBarParentHandle;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ToolkitSupportImpl(ClassLoader classLoader) throws Exception {
    ImageClass = classLoader.loadClass("org.eclipse.swt.graphics.Image");
    //
    Class<?> embeddedClass =
        classLoader.loadClass("org.eclipse.wb.internal.ercp.eswt.EmbeddedScreenShotMaker");
    m_makeShots =
        ReflectionUtils.getMethodBySignature(
            embeddedClass,
            "makeShots(org.eclipse.swt.widgets.Control)");
    m_makeShot =
        ReflectionUtils.getMethodBySignature(
            embeddedClass,
            "makeShot(org.eclipse.swt.widgets.Control)");
    m_getSWTImageHandle = ReflectionUtils.getMethod(embeddedClass, "getSWTImageHandle", ImageClass);
    // menu related
    m_fetchPopupMenuVisualData =
        ReflectionUtils.getMethodBySignature(
            embeddedClass,
            "fetchPopupMenuVisualData(org.eclipse.swt.widgets.Menu)");
    m_getNativeMenuBarHandle =
        ReflectionUtils.getMethodBySignature(
            embeddedClass,
            "getNativeMenuBarHandle(org.eclipse.swt.widgets.Menu)");
    m_getNativeMenuBarParentHandle =
        ReflectionUtils.getMethodBySignature(
            embeddedClass,
            "getNativeMenuBarParentHandle(org.eclipse.swt.widgets.Menu)");
    //
    Class<?> fontPreviewClass =
        classLoader.loadClass("org.eclipse.wb.internal.ercp.eswt.FontPreviewShell");
    m_updateFont =
        ReflectionUtils.getMethodBySignature(
            fontPreviewClass,
            "updateFont(org.eclipse.swt.graphics.Font)");
    m_fontPreviewShell = fontPreviewClass.newInstance();
    //
    Class<?> osClass = classLoader.loadClass("com.ibm.ugl.p3ml.OS");
    m_OS_Device_GetTypefaces =
        ReflectionUtils.getMethodBySignature(osClass, "Device_GetTypefaces(int,boolean)");
    //
    displayClass = classLoader.loadClass("org.eclipse.swt.widgets.Display");
    m_internal_handle = displayClass.getField("internal_handle");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Screen shot
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String WBP_IMAGE_HANDLE = "WBP_IMAGE_HANDLE";

  public void makeShots(Object control) throws Exception {
    m_makeShots.invoke(null, control);
  }

  public Image getShotImage(Object control) throws Exception {
    // get handle
    Integer handle = (Integer) ControlSupport.getData(control, WBP_IMAGE_HANDLE);
    if (handle == null) {
      return null;
    }
    // prepare image
    return getImageWithHandle(handle);
  }

  public void beginShot(Object control) {
  }

  public void endShot(Object control) {
    // do nothing
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Images
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object createToolkitImage(Image image) throws Exception {
    // save SWT image to byte stream
    ImageLoader loader = new ImageLoader();
    loader.data = new ImageData[]{image.getImageData()};
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    loader.save(outStream, SWT.IMAGE_PNG);
    // load eSWT image from byte stream
    ByteArrayInputStream inStream = new ByteArrayInputStream(outStream.toByteArray());
    return ReflectionUtils.getConstructorBySignature(
        ImageClass,
        "<init>(org.eclipse.swt.graphics.Device,java.io.InputStream)").newInstance(null, inStream);
  }

  public Image createSWTImage(Object image) throws Exception {
    Integer handle = (Integer) m_getSWTImageHandle.invoke(null, image);
    return getImageWithHandle(handle);
  }

  /**
   * @return the {@link Image} that has "internal" handle with given value
   */
  private static Image getImageWithHandle(int handle) {
    Image image = new Image(Display.getCurrent(), 1, 1);
    if (handle != 0) {
      OS.DeleteObject(image.handle);
      image.handle = handle;
    }
    return image;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Menu visual data
  //
  ////////////////////////////////////////////////////////////////////////////
  public int getDefaultMenuBarHeight() throws Exception {
    return OS.GetSystemMetrics(OS.SM_CYMENU);
  }

  public MenuVisualData fetchMenuVisualData(Object menu) throws Exception {
    MenuVisualData menuData = new MenuVisualData();
    if (ControlSupport.isStyle(menu, SwtSupport.BAR)) {
      // menu bar
      menuData.m_menuBounds = getMenuBarBounds(menu);
      menuData.m_itemBounds = getMenuBarItemBounds(menu);
      menuData.m_menuImage = null; // not used for menu bar
    } else {
      // prepare image
      Image image = new Image(Display.getCurrent(), 1, 1);
      OS.DeleteObject(image.handle);
      // create shot
      int[] visualData = (int[]) m_fetchPopupMenuVisualData.invoke(null, menu);
      image.handle = visualData[0];
      menuData.m_menuImage = image;
      menuData.m_menuBounds = new Rectangle(image.getBounds());
      menuData.m_itemBounds = new ArrayList<Rectangle>(visualData.length - 1);
      // create rectangles from array
      for (int i = 1; i < visualData.length; i += 4) {
        Rectangle itemRect =
            new Rectangle(visualData[i + 0],
                visualData[i + 1],
                visualData[i + 2],
                visualData[i + 3]);
        menuData.m_itemBounds.add(itemRect);
      }
    }
    return menuData;
  }

  /**
   * @return A menu bar bounds. Menu should be visible on Shell.
   * @throws Exception
   */
  private Rectangle getMenuBarBounds(Object menu) throws Exception {
    int hwndShell = getNativeMenuBarParentHandle(menu);
    MENUBARINFO info = new MENUBARINFO();
    info.cbSize = MENUBARINFO.sizeof;
    if (OS.GetMenuBarInfo(hwndShell, OS.OBJID_MENU, 0, info)) {
      int width = info.right - info.left;
      int height = info.bottom - info.top;
      //
      RECT rect = new RECT();
      OS.GetWindowRect(hwndShell, rect);
      Point shellLocation = new Point(rect.left, rect.top);
      return new Rectangle(info.left - shellLocation.x, info.top - shellLocation.y, width, height);
    }
    throw new IllegalStateException("OS function call failed.");
  }

  /**
   * @return an ArrayList of menu bar item bounds. Menu should be visible on Shell.
   * @throws Exception
   */
  private List<Rectangle> getMenuBarItemBounds(Object menu) throws Exception {
    List<Rectangle> dimensions = Lists.newArrayList();
    MENUBARINFO offsetBarInfo = new MENUBARINFO();
    offsetBarInfo.cbSize = MENUBARINFO.sizeof;
    int hwndShell = getNativeMenuBarParentHandle(menu);
    if (!OS.GetMenuBarInfo(hwndShell, OS.OBJID_MENU, 1, offsetBarInfo)) {
      throw new IllegalStateException("OS function call failed.");
    }
    int itemsCount = getMenuItemsCount(menu);
    for (int index = 0; index < itemsCount; ++index) {
      MENUBARINFO barInfo = new MENUBARINFO();
      barInfo.cbSize = MENUBARINFO.sizeof;
      if (!OS.GetMenuBarInfo(hwndShell, OS.OBJID_MENU, index + 1, barInfo)) {
        throw new IllegalStateException("OS function call failed.");
      }
      int x = barInfo.left - offsetBarInfo.left;
      int y = barInfo.top - offsetBarInfo.top;
      int width = barInfo.right - barInfo.left;
      int height = barInfo.bottom - barInfo.top;
      dimensions.add(new Rectangle(x, y, width, height));
    }
    return dimensions;
  }

  /**
   * Returns native platform handle of menu bar's parent shell.
   * 
   * @param menu
   *          A menu bar instance as Object.
   * @return Native platform handle of menu bar's parent shell.
   */
  private int getNativeMenuBarParentHandle(Object menu) throws Exception {
    return (Integer) m_getNativeMenuBarParentHandle.invoke(null, menu);
  }

  /**
   * Returns menu bar items count using native platform handle of menu bar.
   * 
   * @param menu
   *          A menu bar instance as Object.
   * @return Menu bar items count using native platform handle of menu bar.
   */
  private int getMenuItemsCount(Object menu) throws Exception {
    int hwndMenu = (Integer) m_getNativeMenuBarHandle.invoke(null, menu);
    return OS.GetMenuItemCount(hwndMenu);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Shell
  //
  ////////////////////////////////////////////////////////////////////////////
  public void showShell(Object shell) throws Exception {
    ReflectionUtils.invokeMethod2(shell, "setVisible", boolean.class, true);
    // run events loop to make Shell modal
    Object display = ReflectionUtils.invokeMethod(displayClass, "getCurrent()");
    while (!(Boolean) ReflectionUtils.invokeMethod(shell, "isDisposed()")) {
      if (!(Boolean) ReflectionUtils.invokeMethod(display, "readAndDispatch()")) {
        ReflectionUtils.invokeMethod(display, "sleep()");
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Font
  //
  ////////////////////////////////////////////////////////////////////////////
  public String[] getFontFamilies(boolean scalable) throws Exception {
    Object display = DisplaySupport.getDefault();
    Object handle = m_internal_handle.get(display);
    return (String[]) m_OS_Device_GetTypefaces.invoke(display, handle, scalable);
  }

  public Image getFontPreview(Object font) throws Exception {
    m_updateFont.invoke(m_fontPreviewShell, font);
    m_makeShot.invoke(null, m_fontPreviewShell);
    return getShotImage(m_fontPreviewShell);
  }
}