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
package org.eclipse.wb.internal.swing.utils;

import com.google.common.collect.Lists;

import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.model.menu.MenuVisualData;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.ImageUtils;
import org.eclipse.wb.internal.swing.Activator;
import org.eclipse.wb.internal.swing.model.CoordinateUtils;
import org.eclipse.wb.os.OSSupport;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

import org.apache.commons.lang.StringUtils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.util.Hashtable;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

/**
 * Utilities for Swing images/shots.
 * 
 * @author mitin_aa
 * @author scheglov_ke
 * @coverage swing.utils
 */
public class SwingImageUtils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Shot
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Image} of given {@link Component}.
   */
  public static Image createComponentShot(final Component component) throws Exception {
    return SwingUtils.runObjectLaterAndWait(new RunnableObjectEx<Image>() {
      public Image runObject() throws Exception {
        return convertImage_AWT_to_SWT(createComponentShotAWT(component));
      }
    });
  }

  /**
   * @return the {@link java.awt.Image} of given {@link Component}. Must be called from AWT disp
   *         thread.
   */
  static java.awt.Image createComponentShotAWT(final Component component) throws Exception {
    Assert.isNotNull(component);
    // prepare sizes
    final int componentWidth = component.getWidth();
    final int componentHeight = component.getHeight();
    final int imageWidth = Math.max(1, componentWidth);
    final int imageHeight = Math.max(1, componentHeight);
    // prepare empty image
    final BufferedImage componentImage =
        new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
    // If actual size on component is zero, then we are done.
    if (componentWidth == 0 || componentHeight == 0) {
      return componentImage;
    }
    // some components like JLabel are transparent by default and printing it separately gives
    // bad results like invalid background color. The workaround is to set opacity property and
    // restore old value back when all done.
    ComponentShotConfigurator shotConfigurator = new ComponentShotConfigurator(component);
    try {
      // Linux only: it seems that printAll() should be invoked in AWT dispatch thread 
      // to prevent deadlocks between main thread and AWT event queue.
      // See also SwingUtils.invokeLaterAndWait().
      runInDispatchThread(new Runnable() {
        public void run() {
          component.printAll(componentImage.getGraphics());
        }
      });
    } finally {
      shotConfigurator.dispose();
    }
    // convert into SWT image
    return componentImage;
  }

  static Image createOSXImage(Window window, Image windowImage) throws Exception {
    // draw decorations manually, because it becomes slow if using SWT shell to capture decorations. 
    int width = Math.max(1, window.getWidth());
    int height = Math.max(1, window.getHeight());
    Image fullImage = new Image(null, width, height);
    try {
      GC gc = new GC(fullImage);
      //
      Insets windowInsets = getWindowInsets(window);
      int offsetY = windowInsets.top;
      int offsetX = windowInsets.left;
      // draw caption background
      {
        Image image = Activator.getImage("decorations/osx/background.png");
        int imageWidth = image.getBounds().width;
        int x = 0;
        while (x < width) {
          gc.drawImage(image, x, 0);
          x += imageWidth;
        }
      }
      // draw left corner
      {
        Image image = Activator.getImage("decorations/osx/background-left.png");
        gc.drawImage(image, 0, 0);
      }
      // draw right corner
      {
        Image image = Activator.getImage("decorations/osx/background-right.png");
        gc.drawImage(image, width - image.getBounds().width, 0);
      }
      // draw close button
      {
        Image image = Activator.getImage("decorations/osx/button-close-icon.png");
        gc.drawImage(image, 8, 3);
      }
      // draw minimize button
      {
        Image image = Activator.getImage("decorations/osx/button-minimize-icon.png");
        gc.drawImage(image, 29, 3);
      }
      // draw contents button
      {
        Image image = Activator.getImage("decorations/osx/button-contents-icon.png");
        gc.drawImage(image, 50, 3);
      }
      // draw title
      {
        String windowTitle = getWindowTitle(window);
        if (!StringUtils.isEmpty(windowTitle)) {
          gc.setClipping(70, 0, width - 80, offsetY);
          gc.setForeground(IColorConstants.titleForeground);
          org.eclipse.swt.graphics.Point titleExtent = gc.stringExtent(windowTitle);
          gc.drawString(
              windowTitle,
              width / 2 - titleExtent.x / 2,
              offsetY / 2 - titleExtent.y / 2,
              true);
          gc.setClipping((org.eclipse.swt.graphics.Rectangle) null);
        }
      }
      // draw SWING contents
      try {
        gc.drawImage(
            windowImage,
            offsetX,
            offsetY,
            width - offsetX * 2,
            height - offsetY - offsetX,
            offsetX,
            offsetY,
            width - offsetX * 2,
            height - offsetY - offsetX);
      } finally {
        gc.dispose();
      }
    } finally {
      windowImage.dispose();
    }
    return fullImage;
  }

  private static Insets getWindowInsets(final Window window) throws Exception {
    return SwingUtils.runObjectLaterAndWait(new RunnableObjectEx<Insets>() {
      public Insets runObject() throws Exception {
        return window.getInsets();
      }
    });
  }

  private static String getWindowTitle(final Window window) {
    try {
      return SwingUtils.runObjectLaterAndWait(new RunnableObjectEx<String>() {
        public String runObject() throws Exception {
          String title = (String) ReflectionUtils.invokeMethod(window, "getTitle()");
          return title != null ? title : "";
        }
      });
    } catch (Throwable e) {
      // ignore and return empty string
      return "";
    }
  }

  /**
   * Traverses through components hierarchy and prepares screen shot for every component passed in
   * <code>componentImages</code> map except for branch root if <code>isRoot</code> is
   * <code>true</code>.
   * 
   * @param component
   *          the branch hierarchy root component.
   * @param componentImages
   *          the {@link Map} of components which screen shots should be made for. This map would be
   *          filled by prepared {@link java.awt.Image} instances.
   * @param rootComponent
   *          this branch hierarchy root component.
   */
  static void makeShotsHierarchy(Component component,
      Map<Component, java.awt.Image> componentImages,
      Component rootComponent) throws Exception {
    if (componentImages.containsKey(component) && component != rootComponent) {
      BufferedImage thisComponentImage = (BufferedImage) createComponentShotAWT(component);
      // BUG in OS X (Java 1.6.0_24-b07-334-10M3326): Component.printAll() returns no image 
      // for AWT components and these components are not drawn on the JComponent container 
      // using the same printAll() method. 
      // The workaround is to hack into a native peer, get the native image and then paint it. 
      if (EnvironmentUtils.IS_MAC && !(component instanceof JComponent)) {
        int width = Math.max(1, component.getWidth());
        int height = Math.max(1, component.getHeight());
        Image nativeImage = OSSupport.get().makeShotAwt(component, width, height);
        if (nativeImage != null) {
          BufferedImage rootImage = (BufferedImage) componentImages.get(rootComponent);
          Point rootLocation = rootComponent.getLocationOnScreen();
          Point componentLocation = component.getLocationOnScreen();
          thisComponentImage = ImageUtils.convertToAWT(nativeImage.getImageData());
          rootImage.getGraphics().drawImage(
              thisComponentImage,
              componentLocation.x - rootLocation.x,
              componentLocation.y - rootLocation.y,
              null);
        }
      }
      componentImages.put(component, thisComponentImage);
    }
    if (component instanceof Container) {
      Container container = (Container) component;
      for (Component childComponent : container.getComponents()) {
        makeShotsHierarchy(childComponent, componentImages, rootComponent);
      }
    }
  }

  /**
   * Keep weak references to Window for save/restore it's focusable state.
   */
  private static Map<Window, Boolean> m_fosucableStates = new WeakHashMap<Window, Boolean>();

  /**
   * Prepares {@link Component} for printing.
   * 
   * mitin_aa: Linux: for Metacity window manager (as recommended to use with the Designer) to
   * prevent preview window flickering a better place for preview window is right-bottom screen
   * direction.
   * 
   * TODO: add a preference (Linux only) allowing the user to explicitly set preview window location
   */
  public static void prepareForPrinting(Component component) throws Exception {
    component.setLocation(10000, 10000);
    // don't grab focus during printing
    if (component instanceof Window) {
      Window window = (Window) component;
      m_fosucableStates.put(window, window.getFocusableWindowState());
      window.setFocusableWindowState(false);
    }
    // make visible
    setVisible(component, true);
    {
      // workaround to prevent window from flashing if the Window Manager 
      // doesn't allow the window to appear off-screen. 
      if (component instanceof Window) {
        Window window = (Window) component;
        window.toBack();
        // do the location change once again, because sometimes setLocation() 
        // for invisible windows could be ignored.
        component.setLocation(10000, 10000);
      }
    }
  }

  /**
   * Disposes given Window with trying to restore it's focusable state.
   */
  public static void disposeWindow(final Window window) throws Exception {
    SwingUtils.runLaterAndWait(new RunnableEx() {
      public void run() throws Exception {
        // restore focusable state
        Boolean focusable = m_fosucableStates.get(window);
        if (focusable == null) {
          focusable = true;
        }
        window.setFocusableWindowState(focusable);
        window.dispose();
      }
    });
  }

  /**
   * Set "visible" property of {@link Component} using dispatch thread.
   * 
   * @param component
   *          A {@link Component} which property would be set.
   * @param visible
   *          A "visible" property value to set.
   */
  static void setVisible(final Component component, final boolean visible) throws Exception {
    // set "visible" property in AWT Queue
    SwingUtils.runLaterAndWait(new RunnableEx() {
      public void run() throws Exception {
        component.setVisible(visible);
        if (!visible) {
          if (EnvironmentUtils.IS_LINUX) {
            component.removeNotify();
          }
        }
      }
    });
  }

  /**
   * Removes modal state if given <code>component</code> is instance of {@link Dialog}.
   */
  static void checkForDialog(Component component) {
    // set modal to "false" to prevent lock after setVisible(true)
    if (component instanceof Dialog) {
      ((Dialog) component).setModal(false);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ComponentShotConfigurator
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Helper for configuring {@link Component} before/after shot making.
   */
  private static class ComponentShotConfigurator {
    private final Component m_component;
    private boolean m_oldOpaque;
    private Color m_oldBackground;

    /**
     * Creates {@link ComponentShotConfigurator} instance and configures given {@link Component} for
     * shot making.
     */
    public ComponentShotConfigurator(Component component) {
      m_component = component;
      if (m_component instanceof JComponent) {
        JComponent jcomponent = (JComponent) m_component;
        m_oldOpaque = jcomponent.isOpaque();
        m_oldBackground = jcomponent.getBackground();
        if (!m_oldOpaque) {
          jcomponent.setOpaque(true);
          // use background of parent to fill background of our Component
          // not ideal (may be parent has background image), but good enough in most cases
          for (Component parent = m_component.getParent(); parent != null; parent =
              parent.getParent()) {
            if (!(parent instanceof JComponent) || ((JComponent) parent).isOpaque()) {
              jcomponent.setBackground(parent.getBackground());
              break;
            }
          }
        }
      }
    }

    /**
     * Performs "undo" for any changes made in {@link Component} in
     * {@link ComponentShotConfigurator} constructor.
     */
    public void dispose() {
      if (m_component instanceof JComponent) {
        JComponent jcomponent = (JComponent) m_component;
        jcomponent.setOpaque(m_oldOpaque);
        jcomponent.setBackground(m_oldBackground);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Menu
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link MenuVisualData} for given "menu" object, such as {@link JMenuBar},
   *         {@link JMenu} or {@link JPopupMenu}. Must be called from AWT disp thread.
   */
  public static MenuVisualData fetchMenuVisualData(Container menuObject, Container parent)
      throws Exception {
    MenuVisualData menuData = new MenuVisualData();
    if (menuObject instanceof JMenuBar) {
      fetchMenuVisualData_JMenuBar(menuData, menuObject, parent);
    } else {
      fetchMenuVisualData_JMenu_JPopupMenu(menuData, menuObject);
    }
    return menuData;
  }

  private static void fetchMenuVisualData_JMenuBar(MenuVisualData menuData,
      Container menuObject,
      Container parent) throws Exception {
    if (parent != null) {
      menuData.m_menuBounds =
          CoordinateUtils.get(SwingUtilities.convertRectangle(
              menuObject,
              menuObject.getBounds(),
              parent));
    } else {
      // image
      {
        JFrame frame = new JFrame();
        frame.setBounds(menuObject.getBounds());
        frame.setJMenuBar((JMenuBar) menuObject);
        frame.pack();
        prepareForPrinting(frame);
        try {
          menuData.m_menuImage = createComponentShot(menuObject);
        } finally {
          setVisible(frame, false);
          frame.dispose();
        }
      }
      // bounds
      menuData.m_menuBounds = CoordinateUtils.get(menuObject.getBounds());
    }
    // items
    fetchMenuVisualData_items(menuData, menuObject);
  }

  private static void fetchMenuVisualData_JMenu_JPopupMenu(MenuVisualData menuData,
      Container menuObject) throws Exception {
    JPopupMenu popupMenu =
        menuObject instanceof JPopupMenu
            ? (JPopupMenu) menuObject
            : ((JMenu) menuObject).getPopupMenu();
    // image
    {
      prepareForPrinting(popupMenu);
      // OSX Java since jdk 1.6.0_20 requires menu invoker to be visible.
      // traverse parents until null or already visible and make sure that all are visible.
      // CHECK: it could flash on Windows.
      Point parentLocation = null;
      Component parent = popupMenu.getInvoker();
      while (parent != null && !parent.isShowing()) {
        Container parent2 = parent.getParent();
        if (parent2 != null) {
          parent = parent2;
        } else {
          break;
        }
      }
      if (parent != null) {
        parentLocation = parent.getLocation();
        prepareForPrinting(parent);
      }
      // fetch image
      try {
        Container popupMenuParent = popupMenu.getParent();
        if (popupMenuParent != null) {
          popupMenuParent.doLayout();
        }
        popupMenu.doLayout();
        menuData.m_menuImage = createComponentShot(popupMenu);
      } finally {
        setVisible(popupMenu, false);
        if (parent != null) {
          parent.setLocation(parentLocation);
          if (parent instanceof JPopupMenu) {
            setVisible(parent, false);
          }
        }
      }
    }
    // bounds
    {
      org.eclipse.swt.graphics.Rectangle imageBounds = menuData.m_menuImage.getBounds();
      menuData.m_menuBounds = new Rectangle(0, 0, imageBounds.width, imageBounds.height);
    }
    // items
    fetchMenuVisualData_items(menuData, popupMenu);
  }

  private static void fetchMenuVisualData_items(MenuVisualData menuData, Container menuObject) {
    menuData.m_itemBounds = Lists.newArrayList();
    for (Component menuComponent : menuObject.getComponents()) {
      menuData.m_itemBounds.add(CoordinateUtils.get(menuComponent.getBounds()));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AWT -> SWT image conversion
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Converts AWT image into SWT one. Yours, C.O. ;-)
   */
  public static Image convertImage_AWT_to_SWT(final java.awt.Image image) throws Exception {
    return SwingUtils.runObjectLaterAndWait(new RunnableObjectEx<Image>() {
      public Image runObject() throws Exception {
        BufferedImage bufferedImage = (BufferedImage) image;
        int imageWidth = bufferedImage.getWidth();
        int imageHeight = bufferedImage.getHeight();
        Image swtImage = new Image(null, imageWidth, imageHeight);
        final ImageData swtImageData = swtImage.getImageData();
        try {
          ImageProducer source = image.getSource();
          source.startProduction(new AwtToSwtImageConverter(bufferedImage, swtImageData));
          return new Image(null, swtImageData);
        } catch (Throwable e) {
          // fallback to ImageIO.
          return ImageUtils.convertToSWT(image);
        } finally {
          swtImage.dispose();
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Runs given runnable in dispatch thread.
   */
  public static void runInDispatchThread(final Runnable runnable) throws Exception {
    if (EventQueue.isDispatchThread()) {
      runnable.run();
    } else {
      EventQueue.invokeAndWait(runnable);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AWT -> SWT converter
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @author mitin_aa
   */
  private static final class AwtToSwtImageConverter implements ImageConsumer {
    private final java.awt.image.BufferedImage m_image;
    private final ImageData m_swtImageData;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    private AwtToSwtImageConverter(java.awt.image.BufferedImage image, ImageData swtImageData) {
      m_image = image;
      m_swtImageData = swtImageData;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // ImageConsumer
    //
    ////////////////////////////////////////////////////////////////////////////
    public final void imageComplete(int status) {
      m_image.getSource().removeConsumer(this);
    }

    public final void setHints(int hintflags) {
    }

    public final void setDimensions(int width, int height) {
    }

    public final void setPixels(int x,
        int y,
        int w,
        int h,
        ColorModel model,
        byte[] pixels,
        int off,
        int scansize) {
      SWT.error(SWT.ERROR_NOT_IMPLEMENTED);
    }

    public final void setPixels(int x,
        int y,
        int w,
        int h,
        ColorModel model,
        int[] pixels,
        int off,
        int scansize) {
      if (m_swtImageData.depth == 16) {
        int index = y * m_swtImageData.bytesPerLine;
        for (int i = 0; i < w; i++) {
          int rgb = model.getRGB(pixels[i]);
          int r = rgb >>> 19 & 0x1F;
          int g = rgb >>> 11 & 0x1F;
          int b = rgb >>> 3 & 0x1F;
          rgb = (r << 10) + (g << 5) + b;
          //
          m_swtImageData.data[index + 0] = (byte) (rgb & 0x00FF);
          m_swtImageData.data[index + 1] = (byte) (rgb >> 8);
          index += 2;
        }
        return;
      }
      if (m_swtImageData.depth == 24) {
        int index = y * m_swtImageData.bytesPerLine;
        // there are different RGB orders in SWT ImageData in Linux and Windows, hack it
        if (m_swtImageData.palette.blueShift != 0) {
          // Windows
          for (int i = 0; i < w; i++) {
            int pixel = pixels[i];
            m_swtImageData.data[index + 0] = (byte) (pixel & 0xFF);
            m_swtImageData.data[index + 1] = (byte) (pixel >> 8 & 0xFF);
            m_swtImageData.data[index + 2] = (byte) (pixel >> 16 & 0xFF);
            index += 3;
          }
        } else {
          // Linux
          for (int i = 0; i < w; i++) {
            int pixel = pixels[i];
            m_swtImageData.data[index + 0] = (byte) (pixel >> 16 & 0xFF);
            m_swtImageData.data[index + 1] = (byte) (pixel >> 8 & 0xFF);
            m_swtImageData.data[index + 2] = (byte) (pixel & 0xFF);
            index += 3;
          }
        }
        return;
      }
      if (m_swtImageData.depth == 32) {
        int index = y * w * 4;
        if (m_swtImageData.palette.blueShift != 0) {
          for (int i = 0; i < w; i++) {
            int pixel = pixels[i];
            m_swtImageData.data[index + 0] = (byte) (pixel & 0xFF);
            m_swtImageData.data[index + 1] = (byte) (pixel >> 8 & 0xFF);
            m_swtImageData.data[index + 2] = (byte) (pixel >> 16 & 0xFF);
            m_swtImageData.data[index + 3] = (byte) (pixel >> 24 & 0xFF);
            index += 4;
          }
        } else {
          for (int i = 0; i < w; i++) {
            int pixel = pixels[i];
            m_swtImageData.data[index + 0] = (byte) (pixel >> 24 & 0xFF);
            m_swtImageData.data[index + 1] = (byte) (pixel >> 16 & 0xFF);
            m_swtImageData.data[index + 2] = (byte) (pixel >> 8 & 0xFF);
            m_swtImageData.data[index + 3] = (byte) (pixel & 0xFF);
            index += 4;
          }
        }
        return;
      }
      SWT.error(SWT.ERROR_UNSUPPORTED_DEPTH);
    }

    public final void setColorModel(ColorModel model) {
    }

    public final void setProperties(Hashtable<?, ?> props) {
    }
  }
}
