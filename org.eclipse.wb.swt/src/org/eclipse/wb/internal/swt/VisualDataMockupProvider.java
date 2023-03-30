package org.eclipse.wb.internal.swt;

import org.eclipse.wb.draw2d.IColorConstants;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import java.io.InputStream;

/**
 * Used for emulating the visual data of SWT widgets, which may otherwise not be
 * available in the platform-specific framework. Examples being Cocoa or GTK3.
 *
 */
public class VisualDataMockupProvider {
  public static final int MENU_ITEM_SEPARATOR_HEIGHT = 11;

  /**
   * Emulates the screen shot of the popup menu in case this information is not
   * available.
   *
   * Required for both 64-bit Cocoa on MacOS and GTK3 on Linux.
   *
   * @param menu   the {@link Menu} with style {@link SWT#BAR}.
   * @param bounds the bounds of the given menu item.
   * @return the {@link Image} of popup or drop-down menu and fills the menu items
   *         bounds array.
   */
  public Image mockMenuPopupVisualData(Menu menu, int[] bounds) {
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
      try (InputStream imageStream = getClass().getResourceAsStream(image)) {
        return new Image(null, imageStream);
      } catch (Throwable e) {
        // ignore
        return new Image(null, 1, 1);
      }
    }
}
