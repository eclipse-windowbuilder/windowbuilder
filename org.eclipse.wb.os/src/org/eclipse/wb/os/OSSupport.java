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
package org.eclipse.wb.os;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import org.osgi.framework.Bundle;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Abstract class to provide cross-platform functionality.
 * 
 * @author mitin_aa
 * @coverage os.core
 */
public abstract class OSSupport {
  private static final OSSupport INSTANCE = getInstance();

  /**
   * @return the {@link OSSupport} for current platform using extension point.
   */
  public static OSSupport get() {
    return INSTANCE;
  }

  private static OSSupport getInstance() {
    try {
      IConfigurationElement configurationElements[] =
          Platform.getExtensionRegistry().getConfigurationElementsFor(
              "org.eclipse.wb.os",
              "OSSupport");
      for (IConfigurationElement element : configurationElements) {
        IExtension extension = element.getDeclaringExtension();
        String id = extension.getContributor().getName();
        Bundle bundle = Platform.getBundle(id);
        String className = element.getAttribute("class");
        Class<?> loadedClass = bundle.loadClass(className);
        Field field = loadedClass.getDeclaredField("INSTANCE");
        field.setAccessible(true);
        return (OSSupport) field.get(null);
      }
      throw new OSSupportError("Can't find the support for current OS.");
    } catch (Throwable e) {
      throw new OSSupportError("Can't initialize the support for current OS: ", e);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Screen Shot
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final String WBP_DISABLED_REDRAW = "WBP_DISABLED_REDRAW";
  public static final String WBP_NEED_IMAGE = "WBP_NEED_IMAGE";
  public static final String WBP_IMAGE = "WBP_IMAGE";

  /**
   * Prepares shots for all {@link Control}'s in hierarchy that have flag {@link #WBP_NEED_IMAGE}.
   * Created image can be requested using {@link ToolkitSupport#getShotImage(Object)}.
   * 
   * Note: the control may have <code>null</code> as image, ex. if the control has the invalid size.
   */
  public abstract void makeShots(Object control) throws Exception;

  /**
   * Prepares the process of taking screen shot. Overridden in Linux.
   * 
   * @param control
   *          the {@link Control}.
   */
  public void beginShot(Object controlObject) {
    Shell shell = layoutShell(controlObject);
    // make visible
    makeShellVisible(shell);
  }

  /**
   * Makes the top level control visible.
   */
  protected void makeShellVisible(Shell shell) {
    shell.setLocation(-10000, -10000);
    shell.setVisible(true);
  }

  protected final Shell layoutShell(Object controlObject) {
    Control control = (Control) controlObject;
    Shell shell = control.getShell();
    doLayout(shell);
    fixZeroSizes_begin(shell);
    return shell;
  }

  /**
   * Ensures that layout is performed for all {@link Composite}-s, even if some of them are in
   * "null" layout, so layout request is not propagated to them automatically.
   * 
   * @param control
   *          the {@link Control} to layout if required and is {@link Composite}.
   */
  private static void doLayout(Control control) {
    if (control instanceof Composite) {
      Composite composite = (Composite) control;
      // layout children
      for (Control child : composite.getChildren()) {
        doLayout(child);
      }
      // layout this Composite
      composite.layout();
    }
  }

  /**
   * Finalizes the process of taking screen shot. Overridden in Linux.
   * 
   * @param control
   *          the {@link Control}.
   */
  public void endShot(Object controlObject) {
    Control control = (Control) controlObject;
    fixZeroSizes_end(control);
    Shell shell = control.getShell();
    shell.setVisible(false);
  }

  /**
   * Return the {@link Image} of given {@link Control}.
   * 
   * Note: may return <code>null</code> as image, ex. if the control has the invalid size. See also
   * {@link OSSupport#makeShots(Object)}.
   * 
   * @return the {@link Image} of given {@link Control}.
   */
  public abstract Image makeShot(Control control) throws Exception;

  /**
   * @return the default height of single-line menu bar according to system metrics (if available).
   */
  public abstract int getDefaultMenuBarHeight();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Fix zero size
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Some controls can have zero sizes, and we can't draw it. The fix is to cancel drawing.
   */
  private static void fixZeroSizes_begin(Control control) {
    // fix this Control
    if (control.getBounds().isEmpty()) {
      control.setData(WBP_DISABLED_REDRAW, Boolean.TRUE);
      control.setRedraw(false);
    }
    // fix children
    if (control instanceof Composite) {
      Composite composite = (Composite) control;
      for (Control child : composite.getChildren()) {
        fixZeroSizes_begin(child);
      }
    }
  }

  /**
   * "End" operation for {@link #fixZeroSizes_begin(Control)}. We should enable drawing after screen
   * shot, because some containers (such as {@link TabFolder}) manage bounds of its children
   * dynamically. So, we should not disable drawing forever, only when we need this.
   */
  private static void fixZeroSizes_end(Control control) {
    // fix this Control
    if (control.getData(WBP_DISABLED_REDRAW) != null) {
      control.setData(WBP_DISABLED_REDRAW, null);
      control.setRedraw(true);
    }
    // fix children
    if (control instanceof Composite) {
      Composite composite = (Composite) control;
      for (Control child : composite.getChildren()) {
        fixZeroSizes_end(child);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TabItem bounds
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the bounds of {@link TabItem}.
   */
  public abstract Rectangle getTabItemBounds(Object item);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Shell Opacity
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the <code>alpha</code> value for given <code>shell</code>.
   * 
   * @param shell
   *          the instance of {@link Shell} to set the alpha.
   * @param alpha
   *          the value of alpha, 0-255, not validated.
   */
  public abstract void setAlpha(Shell shell, int alpha);

  /**
   * Returns the current alpha value for given <code>shell</code>.
   * 
   * @param shell
   *          the instance of {@link Shell} to get the alpha.
   * @return the alpha value.
   */
  public abstract int getAlpha(Shell shell);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Menu
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the {@link Image} of popup or drop-down menu and fills the menu items bounds array.
   * 
   * @return the {@link Image} of popup or drop-down menu and fills the menu items bounds array.
   */
  public abstract Image getMenuPopupVisualData(Menu menu, int[] bounds) throws Exception;

  /**
   * Returns <code>null</code> in most cases except MacOSX. Fills the {@link List} of
   * {@link Rectangle} with items bounds of bar menu in <code>bounds</code> parameter.
   * 
   * @param menu
   *          the {@link Menu} with style {@link SWT#BAR}.
   * @param bounds
   *          the {@link List} of {@link Rectangle} to fill with items bounds values.
   * @return under MacOSX returns the image of menu bar, otherwise returns <code>null</code>.
   */
  public abstract Image getMenuBarVisualData(Menu menu, List<Rectangle> bounds);

  /**
   * Returns the bounds of given bar menu.
   * 
   * @param menu
   *          the {@link Menu} with style {@link SWT#BAR}.
   * @return the bounds of given bar menu.
   */
  public abstract Rectangle getMenuBarBounds(Menu menu);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tree
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if pointer is over {@link TreeItem} plus/minus sign.
   */
  public abstract boolean isPlusMinusTreeClick(Tree tree, int x, int y);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Control
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the insets for push button, useful in MacOSX. Insets values order is left, right, top,
   *         bottom.
   */
  public int[] getPushButtonInsets() {
    return new int[]{};
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Misc
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Scrolls the <code>cursorControl</code> by <code>count</code> positions. Implemented as
   * Windows-only.
   * 
   * @param cursorControl
   *          the {@link Control} to scroll.
   * @param count
   *          the scroll count.
   */
  public void scroll(Control cursorControl, int count) {
    // does nothing
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AWT/Swing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Attempts to take shot from AWT component's native peer. Returns <code>null</code> if no peer,
   * cannot determine native handle or any other low-level error or improper state determined.
   */
  public Image makeShotAwt(Object component, int width, int height) {
    // do nothing by default
    return null;
  }
}
