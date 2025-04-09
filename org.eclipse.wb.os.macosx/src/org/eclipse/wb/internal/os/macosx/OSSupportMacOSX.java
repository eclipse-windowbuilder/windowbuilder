/*******************************************************************************
 * Copyright (c) 2011, 2020 Google, Inc. and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *    OPCoach, Olivier Prouvost - Bug 526091 - Display design upside down with MacosX High Sierra
 *    OPCoach, Olivier Prouvost - Bug 539850 - Display design upside down with MacosX Mojave
 *    OPCoach, Olivier Prouvost - Bug 559596 - Display design upside down with MacosX Catalina and following
 *******************************************************************************/
package org.eclipse.wb.internal.os.macosx;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.swt.VisualDataMockupProvider;
import org.eclipse.wb.os.OSSupport;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Tree;

import org.apache.commons.lang3.NotImplementedException;

import java.util.List;

/**
 * Mac OS X implementation for {@link OSSupport}.
 *
 * @author mitin_aa
 * @coverage os.macosx
 */
public abstract class OSSupportMacOSX extends OSSupport {
	// constants
	private static final String FLIP_FLAG = "windowbuilder.osx.flip";//$NON-NLS-1$
	protected static final int DEFAULT_MENU_ITEM_OFFSET_X = 6;
	protected static final int DEFAULT_MENU_ITEM_OFFSET_Y = 5;
	private static final int TAB_ITEM_OFFSET_Y = 8;
	private static final int TAB_ITEM_EXTRA_WIDTH = 25;
	protected static final OSSupport INSTANCE = new OSSupportMacOSXCocoa.Cocoa64();
	private static boolean mustFlipImage; // Initialized in static block below
	static {
		String osVersion = System.getProperties().get("os.version").toString();
		Object flip_flag_is_set = System.getProperties().get(FLIP_FLAG);
		// OS_VERSION IS composed of : version.mod.patch
		int version = Integer.parseInt(osVersion.split("\\.")[0]);//$NON-NLS-1$
		int mod = Integer.parseInt(osVersion.split("\\.")[1]);//$NON-NLS-1$
		boolean isHighSierraOrMore = version > 10 || version == 10 && mod >= 13;
		// Flip image for HighSierra, Mojave, Catalina and next OS X versions
		mustFlipImage = flip_flag_is_set != null ? Boolean.getBoolean(FLIP_FLAG) : isHighSierraOrMore;
	}

	protected OSSupportMacOSX() {
	}

	@Override
	public void beginShot(Object controlObject) {
		// disabling shell redraw prevents painting events
		// to be dispatched to design canvas. These events can
		// cause painting already disposed images, ex., for action instances,
		// which are already disposed but image references are still alive
		// in it's presentation in widgets tree (see Case 40141).
		DesignerPlugin.getShell().setRedraw(false);
		super.beginShot(controlObject);
	}

	@Override
	public void endShot(Object controlObject) {
		super.endShot(controlObject);
		DesignerPlugin.getShell().setRedraw(true);
	}

	@Override
	public void makeShots(Object controlObject) throws Exception {
		// do create shots
		Control control = (Control) controlObject;
		try {
			//			reverseDrawingOrder(control);
			Image sourceShot = makeShot(control);
			if (mustFlipImage) {
				Image reverseShot = reverseImage(sourceShot);
				sourceShot.dispose();
				control.setData(WBP_IMAGE, reverseShot);
			}
			control.getShell().setVisible(false);
		} finally {
			//			reverseDrawingOrder(control);
		}
	}

	/**
	 * Reverse image which is upside down on Macos X High Sierra See bug #526091
	 *
	 * @param image
	 * @return the reverse Image
	 */
	public Image reverseImage(Image image) {
		// Image must be flipped and then rotated twice ! BUT ONLY ON HIGH SIERRA
		ImageData flip = flip(image.getImageData(), false);
		ImageData rotate1 = rotate(flip, SWT.LEFT);
		ImageData rotate = rotate(rotate1, SWT.LEFT);
		final Image result = new Image(image.getDevice(), rotate);
		return result;
	}

	/*
   The following 'flip' and 'rotate' methods have been copied from the Snippet139.
   They are needed to fix the bug #526091, 539850 and 559596
	 */
	static ImageData rotate(ImageData srcData, int direction) {
		int bytesPerPixel = srcData.bytesPerLine / srcData.width;
		int destBytesPerLine =
				direction == SWT.DOWN ? srcData.width * bytesPerPixel : srcData.height * bytesPerPixel;
		byte[] newData = new byte[direction == SWT.DOWN
				? srcData.height * destBytesPerLine
						: srcData.width * destBytesPerLine];
		int width = 0, height = 0;
		for (int srcY = 0; srcY < srcData.height; srcY++) {
			for (int srcX = 0; srcX < srcData.width; srcX++) {
				int destX = 0, destY = 0, destIndex = 0, srcIndex = 0;
				switch (direction) {
				case SWT.LEFT : // left 90 degrees
					destX = srcY;
					destY = srcData.width - srcX - 1;
					width = srcData.height;
					height = srcData.width;
					break;
				case SWT.RIGHT : // right 90 degrees
					destX = srcData.height - srcY - 1;
					destY = srcX;
					width = srcData.height;
					height = srcData.width;
					break;
				case SWT.DOWN : // 180 degrees
					destX = srcData.width - srcX - 1;
					destY = srcData.height - srcY - 1;
					width = srcData.width;
					height = srcData.height;
					break;
				}
				destIndex = destY * destBytesPerLine + destX * bytesPerPixel;
				srcIndex = srcY * srcData.bytesPerLine + srcX * bytesPerPixel;
				System.arraycopy(srcData.data, srcIndex, newData, destIndex, bytesPerPixel);
			}
		}
		// destBytesPerLine is used as scanlinePad to ensure that no padding is required
		return new ImageData(width,
				height,
				srcData.depth,
				srcData.palette,
				srcData.scanlinePad,
				newData);
	}

	static ImageData flip(ImageData srcData, boolean vertical) {
		int bytesPerPixel = srcData.bytesPerLine / srcData.width;
		int destBytesPerLine = srcData.width * bytesPerPixel;
		byte[] newData = new byte[srcData.data.length];
		for (int srcY = 0; srcY < srcData.height; srcY++) {
			for (int srcX = 0; srcX < srcData.width; srcX++) {
				int destX = 0, destY = 0, destIndex = 0, srcIndex = 0;
				if (vertical) {
					destX = srcX;
					destY = srcData.height - srcY - 1;
				} else {
					destX = srcData.width - srcX - 1;
					destY = srcY;
				}
				destIndex = destY * destBytesPerLine + destX * bytesPerPixel;
				srcIndex = srcY * srcData.bytesPerLine + srcX * bytesPerPixel;
				System.arraycopy(srcData.data, srcIndex, newData, destIndex, bytesPerPixel);
			}
		}
		// destBytesPerLine is used as scanlinePad to ensure that no padding is required
		return new ImageData(srcData.width,
				srcData.height,
				srcData.depth,
				srcData.palette,
				srcData.scanlinePad,
				newData);
	}

	/**
	 * @return <code>true</code> if the given menu item is the separator item.
	 */
	protected static boolean isSeparatorItem(MenuItem item) {
		return (item.getStyle() & SWT.SEPARATOR) != 0;
	}

	@Override
	public Image getMenuBarVisualData(Menu menu, List<Rectangle> bounds) {
		int height = getDefaultMenuBarHeight();
		Image image = new Image(menu.getDisplay(), menu.getParent().getSize().x, height);
		GC gc = new GC(image);
		// calc bounds first
		int menuWidth = 5;
		for (int i = 0; i < menu.getItemCount(); ++i) {
			MenuItem item = menu.getItem(i);
			String text = item.getText();
			int itemWidth = 5;
			if (text != null) {
				Point textDimensions = gc.stringExtent(text);
				itemWidth = textDimensions.x + 10;
			}
			bounds.add(new Rectangle(menuWidth, 0, itemWidth, height));
			menuWidth += itemWidth;
		}
		gc.dispose();
		if (image.getBounds().width < menuWidth) {
			image.dispose();
			image = new Image(menu.getDisplay(), menuWidth, height);
		}
		// draw
		gc = new GC(image);
		gc.setBackground(ColorConstants.menuBackground);
		gc.fillRectangle(image.getBounds());
		gc.setForeground(ColorConstants.menuForeground);
		for (int i = 0; i < menu.getItemCount(); ++i) {
			MenuItem item = menu.getItem(i);
			String text = item.getText();
			if (text != null) {
				Rectangle itemBounds = bounds.get(i);
				gc.drawString(text, itemBounds.x + 5, itemBounds.y + 2, true);
			}
		}
		gc.dispose();
		return image;
	}

	@Override
	public Rectangle getMenuBarBounds(Menu menu) {
		throw new NotImplementedException("OSX menu bar bounds should be get using menu bar image.");
	}

	/**
	 * For separator items there is no way to get item bounds because the separator item has no custom
	 * draw flag and native part doesn't receive draw messages for to get item bounds. the workaround
	 * is to approximate separator location based on previous item.
	 */
	protected void fixupSeparatorItems(Menu menu, int[] bounds, int[] menuSize, int[] itemsBounds) {
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
				itemsBounds[i * 4 + 3] = VisualDataMockupProvider.MENU_ITEM_SEPARATOR_HEIGHT;
			}
			//
			bounds[i * 4 + 0] = itemsBounds[i * 4 + 0] - itemOffsetX;
			bounds[i * 4 + 1] = itemsBounds[i * 4 + 1] - itemOffsetY;
			bounds[i * 4 + 2] = itemsBounds[i * 4 + 2];
			bounds[i * 4 + 3] = itemsBounds[i * 4 + 3];
		}
	}

	@Override
	public Rectangle getTabItemBounds(Object item) {
		TabItem tabItem = (TabItem) item;
		TabFolder folder = tabItem.getParent();
		GC gc = new GC(folder);
		Point folderSize = folder.getSize();
		int thisItemOffsetX = 0;
		int thisItemWidth = 0;
		int thisItemHeight = 0;
		int itemsWidth = 0;
		for (int i = 0; i < folder.getItemCount(); i++) {
			TabItem childItem = folder.getItem(i);
			Point itemSize = calculateItemSize(childItem, gc);
			if (childItem == tabItem) {
				thisItemOffsetX = itemsWidth;
				thisItemWidth = itemSize.x;
				thisItemHeight = itemSize.y;
			}
			itemsWidth += itemSize.x;
		}
		gc.dispose();
		return new Rectangle(folderSize.x / 2 - itemsWidth / 2 + thisItemOffsetX,
				TAB_ITEM_OFFSET_Y,
				thisItemWidth,
				thisItemHeight);
	}

	/**
	 * Returns the empiric size of this {@link TabItem}. Based on {@link TabItem#calculateWidth())
	 * function.
	 *
	 * @param item
	 *          the instance of {@link TabItem}.
	 * @param gc
	 *          the GC of parent TabFolder.
	 * @return the empiric size of this {@link TabItem}.
	 */
	private Point calculateItemSize(TabItem item, GC gc) {
		int width = 0;
		int imageHeight = 0;
		int textHeight = 0;
		Image image = item.getImage();
		String text = item.getText();
		if (image != null) {
			Rectangle imageBounds = image.getBounds();
			width = imageBounds.width + 2;
			imageHeight = imageBounds.height;
		}
		if (text != null && text.length() > 0) {
			Point stringExtent = gc.stringExtent(text);
			width += stringExtent.x;
			textHeight = stringExtent.y + 2;
		}
		return new Point(width + TAB_ITEM_EXTRA_WIDTH, Math.max(imageHeight, textHeight));
	}

	@Override
	public boolean isPlusMinusTreeClick(Tree tree, int x, int y) {
		return false;
	}

	@Override
	public int[] getPushButtonInsets() {
		return new int[4];
	}
}
