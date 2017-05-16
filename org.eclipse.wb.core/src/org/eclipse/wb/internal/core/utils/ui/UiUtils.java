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
package org.eclipse.wb.internal.core.utils.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;

import java.util.List;
import java.util.Map;

/**
 * Utilities for UI.
 *
 * @author scheglov_ke
 */
public class UiUtils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Shell utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Run modal loop for given shell.
   */
  public static void runModalShell(Shell shell) {
    Display display = shell.getDisplay();
    shell.open();
    while (!shell.isDisposed() && shell.getVisible()) {
      try {
        if (!display.readAndDispatch()) {
          display.sleep();
        }
      } catch (Throwable t) {
      }
    }
  }

  /**
   * Centers <code>shell</code> in <code>parentShell</code>.
   */
  public static void centerShell(Shell parentShell, Shell shell) {
    Point size = shell.getSize();
    Rectangle parentBounds = parentShell.getBounds();
    int x = parentBounds.x + parentBounds.width / 2 - size.x / 2;
    int y = parentBounds.y + parentBounds.height / 2 - size.y / 2;
    shell.setLocation(x, y);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Combo
  //
  ////////////////////////////////////////////////////////////////////////////
  public static void setVisibleAll(Combo combo) {
    setVisibleItemCount(combo, combo.getItemCount());
  }

  /**
   * Sets count of items visible after drop-down.
   */
  public static void setVisibleItemCount(Combo combo, int count) {
    combo.setVisibleItemCount(count);
    combo.getParent().layout();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Menu
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Shows {@link Menu} and disposed it when user hides it (selects some item, or just clicks
   * somewhere).
   */
  public static void showAndDisposeOnHide(final Menu menu) {
    menu.addMenuListener(new MenuAdapter() {
      @Override
      public void menuHidden(MenuEvent e) {
        e.display.asyncExec(new Runnable() {
          public void run() {
            menu.dispose();
          }
        });
      }
    });
    menu.setVisible(true);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Focus utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds given {@link FocusListener} for controls tree starting from specified control.
   */
  public static void addFocusListenerForControlTree(Control control, FocusListener focusListener) {
    control.addFocusListener(focusListener);
    if (control instanceof Composite) {
      Composite composite = (Composite) control;
      Control[] children = composite.getChildren();
      for (int i = 0; i < children.length; i++) {
        Control child = children[i];
        addFocusListenerForControlTree(child, focusListener);
      }
    }
  }

  /**
   * @return <code>true</code> if given control or any of its direct and indirect children have
   *         focus.
   */
  public static boolean hasFocusInControlTree(Control control) {
    if (control.isDisposed()) {
      return false;
    }
    if (control.isFocusControl()) {
      return true;
    }
    if (control instanceof Composite) {
      Composite composite = (Composite) control;
      Control[] children = composite.getChildren();
      for (int i = 0; i < children.length; i++) {
        Control child = children[i];
        if (hasFocusInControlTree(child)) {
          return true;
        }
      }
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if given {@link Widget}s are parent and child.
   */
  public static boolean isChildOf(Widget parent, Widget child) {
    if (child instanceof Control) {
      Control childControl = (Control) child;
      while (childControl != null) {
        if (childControl == parent) {
          return true;
        }
        childControl = childControl.getParent();
      }
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Table utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the index of {@link TableColumn} in given {@link Table} that is under mouse cursor.
   */
  public static int getColumnUnderCursor(Table table) {
    Point p = Display.getCurrent().getCursorLocation();
    p = table.toControl(p);
    return getColumnAt(table, p);
  }

  /**
   * @return the index of {@link TableColumn} in given {@link Table} that is under given
   *         {@link Point}.
   */
  public static int getColumnAt(Table table, Point p) {
    int x = -table.getHorizontalBar().getSelection();
    TableColumn[] columns = table.getColumns();
    for (int i = 0; i < columns.length; i++) {
      TableColumn column = columns[i];
      int width = column.getWidth();
      if (x <= p.x && p.x < x + width) {
        return i;
      }
      x += width;
    }
    return -1;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tooltip for table items
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Provider for {@link TableItem} tooltip control.
   *
   * @author scheglov_ke
   */
  public interface ITableTooltipProvider {
    Control createTooltipControl(TableItem item, Composite parent, int column);
  }

  /**
   * Adds tooltips support with given {@link ITableTooltipProvider}.
   */
  public static void installTableTooltipProvider(final Table table,
      final ITableTooltipProvider provider) {
    final String TAB_ITEM_KEY = "_TABLEITEM";
    final Shell tableShell = table.getShell();
    //
    final Listener tipControlListener = new Listener() {
      public void handleEvent(Event event) {
        Control tipControl = (Control) event.widget;
        Shell tipShell = tipControl.getShell();
        switch (event.type) {
          case SWT.MouseDown :
            Event e = new Event();
            e.item = (TableItem) tipControl.getData(TAB_ITEM_KEY);
            // dispose tooltip
            tipShell.dispose();
            // Assuming table is single select, set the selection as if
            // the mouse down event went through to the table
            table.setSelection(new TableItem[]{(TableItem) e.item});
            table.notifyListeners(SWT.Selection, e);
            break;
          case SWT.MouseExit :
            tipShell.dispose();
            break;
        }
      }
    };
    //
    Listener tableListener = new Listener() {
      private Shell m_tipShell = null;
      private Control m_tipControl = null;

      public void handleEvent(Event event) {
        switch (event.type) {
          case SWT.Dispose :
          case SWT.KeyDown :
          case SWT.MouseMove : {
            if (m_tipShell == null) {
              break;
            }
            m_tipShell.dispose();
            m_tipShell = null;
            m_tipControl = null;
            break;
          }
          case SWT.MouseHover : {
            Point hoverLocation = new Point(event.x, event.y);
            TableItem item = table.getItem(hoverLocation);
            if (item != null) {
              if (m_tipShell != null && !m_tipShell.isDisposed()) {
                m_tipShell.dispose();
              }
              m_tipShell = new Shell(tableShell, SWT.ON_TOP | SWT.TOOL);
              m_tipShell.setLayout(new FillLayout());
              //
              int column = getColumnAt(table, hoverLocation);
              m_tipControl = provider.createTooltipControl(item, m_tipShell, column);
              if (m_tipControl != null) {
                m_tipControl.setData(TAB_ITEM_KEY, item);
                m_tipControl.addListener(SWT.MouseExit, tipControlListener);
                m_tipControl.addListener(SWT.MouseDown, tipControlListener);
                //
                Point size = m_tipShell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                Rectangle rect = item.getBounds(0);
                Point p = table.toDisplay(new Point(rect.x, rect.y));
                m_tipShell.setBounds(p.x, p.y, size.x, size.y);
                m_tipShell.setVisible(true);
              }
            }
          }
        }
      }
    };
    table.addListener(SWT.Dispose, tableListener);
    table.addListener(SWT.KeyDown, tableListener);
    table.addListener(SWT.MouseMove, tableListener);
    table.addListener(SWT.MouseHover, tableListener);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tree - expand/collapse all
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Expands all {@link TreeItem}'s in given {@link Tree}.
   */
  public static void expandAll(Tree tree) {
    setExpanded(tree.getItems(), true);
  }

  /**
   * Collapses all {@link TreeItem}'s in given {@link Tree}.
   */
  public static void collapseAll(Tree tree) {
    setExpanded(tree.getItems(), false);
  }

  /**
   * Set expansion state for given {@link TreeItem}'s and all its children.
   */
  private static void setExpanded(TreeItem[] treeItems, boolean expanded) {
    for (int i = 0; i < treeItems.length; i++) {
      TreeItem treeItem = treeItems[i];
      treeItem.setExpanded(expanded);
      setExpanded(treeItem.getItems(), expanded);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tree - get/set expanded
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the array of expanded {@link TreeItem}'s.
   */
  public static TreeItem[] getExpanded(Tree tree) {
    List<TreeItem> expandedItems = Lists.newArrayList();
    addExpanded(expandedItems, tree.getItems());
    return expandedItems.toArray(new TreeItem[expandedItems.size()]);
  }

  /**
   * Adds expanded {@link TreeItem}'s for given {@link TreeItem}'s and all their children.
   */
  private static void addExpanded(List<TreeItem> expandedItems, TreeItem[] treeItems) {
    for (int i = 0; i < treeItems.length; i++) {
      TreeItem treeItem = treeItems[i];
      if (treeItem.getExpanded()) {
        expandedItems.add(treeItem);
      }
      addExpanded(expandedItems, treeItem.getItems());
    }
  }

  /**
   * Expands {@link TreeItem}'s for which {@link TreeItem#getData()} returns object contained in
   * given elements.
   *
   * @param tree
   *          the {@link Tree} to set expansion.
   * @param expandedElements
   *          the array elements to expand.
   */
  public static void setExpandedByData(Tree tree, Object[] expandedElements) {
    tree.setRedraw(false);
    try {
      setExpandedByData(tree.getItems(), expandedElements);
    } finally {
      tree.setRedraw(true);
    }
  }

  /**
   * Expands {@link TreeItem}'s for given {@link TreeItem}'s and all their children.
   */
  private static void setExpandedByData(TreeItem[] treeItems, Object[] expandedElements) {
    for (int i = 0; i < treeItems.length; i++) {
      TreeItem treeItem = treeItems[i];
      // expand/collapse this item
      boolean expand = ArrayUtils.contains(expandedElements, treeItem.getData());
      treeItem.setExpanded(expand);
      // process children
      setExpandedByData(treeItem.getItems(), expandedElements);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bounds utilities
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the bounds of given <code>control</code> relative to given <code>target</code>.
   */
  public static Rectangle getBoundsRelativeTo(Control control, Composite target) {
    Rectangle bounds = control.getBounds();
    return convertFromTo(bounds, control.getParent(), target);
  }

  /**
   * @return the {@link Rectangle} converted from <code>source</code> {@link Control} to
   *         <code>target</code> .
   */
  public static Rectangle convertFromTo(Rectangle bounds, Control source, Control target) {
    Point p = source.toDisplay(new Point(bounds.x, bounds.y));
    p = target.toControl(p);
    bounds.x = p.x;
    bounds.y = p.y;
    return bounds;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Controls enable state
  //
  ////////////////////////////////////////////////////////////////////////////
  public static String KEY_IGNORE_HIERARCHY_ENABLED =
      "Ignore this Control when set 'enabled' property for hierarchy";

  /**
   * Changes enablement for given control and all its children.
   */
  public static void changeControlEnable(Control control, boolean enable) {
    if (control.getData(KEY_IGNORE_HIERARCHY_ENABLED) != null) {
      return;
    }
    if (control.getEnabled() != enable) {
      control.setEnabled(enable);
    }
    if (control instanceof Composite) {
      Composite composite = (Composite) control;
      Control[] children = composite.getChildren();
      for (int i = 0; i < children.length; i++) {
        Control child = children[i];
        changeControlEnable(child, enable);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Images
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the copy of given {@link Image} or <code>null</code> if <code>image</code> is
   *         <code>null</code> .
   */
  public static Image getCopy(Image image) {
    return image == null ? null : new Image(null, image.getImageData());
  }

  /**
   * Draws {@link Image} on {@link GC} centered in given {@link Rectangle}. If {@link Image} is
   * bigger that {@link Rectangle}, {@link Image} will be scaled down as needed with keeping
   * proportions.
   */
  public static void drawScaledImage(GC gc, Image image, Rectangle targetRectangle) {
    int imageWidth = image.getBounds().width;
    int imageHeight = image.getBounds().height;
    // prepare scaled image size
    int newImageWidth;
    int newImageHeight;
    if (imageWidth <= targetRectangle.width && imageHeight <= targetRectangle.height) {
      newImageWidth = imageWidth;
      newImageHeight = imageHeight;
    } else {
      // prepare minimal scale
      double k;
      {
        double k_w = targetRectangle.width / (double) imageWidth;
        double k_h = targetRectangle.height / (double) imageHeight;
        k = Math.min(k_w, k_h);
      }
      // calculate scaled image size
      newImageWidth = (int) (imageWidth * k);
      newImageHeight = (int) (imageHeight * k);
    }
    // draw image centered in target rectangle
    int destX = targetRectangle.x + (targetRectangle.width - newImageWidth) / 2;
    int destY = targetRectangle.y + (targetRectangle.height - newImageHeight) / 2;
    gc.drawImage(image, 0, 0, imageWidth, imageHeight, destX, destY, newImageWidth, newImageHeight);
  }

  /**
   * Returns part of given {@link Image} and disposes original {@link Image}.
   */
  public static Image getCroppedImage(Image fullImage, Rectangle cropBounds) {
    Rectangle fullImageBounds = fullImage.getBounds();
    int fullWidth = fullImageBounds.width;
    int fullHeight = fullImageBounds.height;
    int cropWidth = Math.max(Math.min(cropBounds.width, fullWidth - cropBounds.x), 1);
    int cropHeight = Math.max(Math.min(cropBounds.height, fullHeight - cropBounds.y), 1);
    Image croppedImage = new Image(Display.getCurrent(), cropWidth, cropHeight);
    GC gc = new GC(croppedImage);
    try {
      gc.drawImage(
          fullImage,
          cropBounds.x,
          cropBounds.y,
          cropWidth,
          cropHeight,
          0,
          0,
          cropWidth,
          cropHeight);
    } catch (Throwable e) {
      String message = "fullImage.getBounds()=" + fullImageBounds + " cropBounds=" + cropBounds;
      throw new Error(message, e);
    } finally {
      gc.dispose();
      fullImage.dispose();
    }
    return croppedImage;
  }

  /**
   * @return <code>true</code> if two {@link Image}'s are equal.
   */
  public static boolean equals(Image image_1, Image image_2) {
    // try to compare as plain Object's
    if (ObjectUtils.equals(image_1, image_2)) {
      return true;
    }
    // compare bounds
    if (!image_1.getBounds().equals(image_2.getBounds())) {
      return false;
    }
    // check ImageData's
    ImageData imageData_1 = image_1.getImageData();
    ImageData imageData_2 = image_2.getImageData();
    if (imageData_1.depth != imageData_2.depth) {
      return false;
    }
    // compare pixels
    for (int i = 0; i < imageData_1.data.length; i++) {
      if (imageData_1.data[i] != imageData_2.data[i]) {
        return false;
      }
    }
    // OK, Image's has same size and pixels
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // File icons
  //
  ////////////////////////////////////////////////////////////////////////////
  private static Map<String, Image> m_extensionToIcon = Maps.newHashMap();

  /**
   * @return icon that is associated with given file extension in the operating system
   */
  public static Image getIcon(String extension) {
    Image icon = m_extensionToIcon.get(extension);
    if (icon == null) {
      Program program = Program.findProgram(extension);
      if (program != null) {
        ImageData imageData = program.getImageData();
        if (imageData != null) {
          icon = new Image(Display.getDefault(), imageData);
          m_extensionToIcon.put(extension, icon);
        }
      }
    }
    return icon;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Message dialogs
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Opens standard warning dialog.
   */
  public static void openWarning(Shell parent, String title, String message) {
    MessageDialog dialog =
        new MessageDialog(parent,
            title,
            null,
            message,
            MessageDialog.WARNING,
            new String[]{IDialogConstants.OK_LABEL},
            0);
    dialog.open();
  }

  /**
   * Opens standard error dialog.
   */
  public static void openError(Shell parent, String title, String message) {
    MessageDialog dialog =
        new MessageDialog(parent,
            title,
            null,
            message,
            MessageDialog.ERROR,
            new String[]{IDialogConstants.OK_LABEL},
            0);
    dialog.open();
  }
}