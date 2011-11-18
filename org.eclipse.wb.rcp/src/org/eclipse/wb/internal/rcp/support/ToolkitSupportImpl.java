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
package org.eclipse.wb.internal.rcp.support;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.model.menu.MenuVisualData;
import org.eclipse.wb.internal.swt.support.IToolkitSupport;
import org.eclipse.wb.os.OSSupport;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Set;

/**
 * Implementation of {@link IToolkitSupport} for RCP.
 * 
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage rcp.support
 */
public final class ToolkitSupportImpl implements IToolkitSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ToolkitSupportImpl(ClassLoader classLoader) {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Screen shot
  //
  ////////////////////////////////////////////////////////////////////////////
  public void makeShots(Object controlObject) throws Exception {
    OSSupport.get().makeShots(controlObject);
  }

  public Image getShotImage(Object controlObject) throws Exception {
    Widget widget = (Widget) controlObject;
    return (Image) widget.getData(OSSupport.WBP_IMAGE);
  }

  public MenuVisualData fetchMenuVisualData(Object menuObject) throws Exception {
    Menu menu = (Menu) menuObject;
    MenuVisualData menuInfo = new MenuVisualData();
    if ((menu.getStyle() & SWT.BAR) != 0) {
      // menu bar
      List<org.eclipse.swt.graphics.Rectangle> itemBounds = Lists.newArrayList();
      menuInfo.m_menuImage = OSSupport.get().getMenuBarVisualData(menu, itemBounds);
      if (menuInfo.m_menuImage == null) {
        menuInfo.m_menuBounds = new Rectangle(OSSupport.get().getMenuBarBounds(menu));
      } else {
        // OSX way
        menuInfo.m_menuBounds = new Rectangle(menuInfo.m_menuImage.getBounds());
      }
      menuInfo.m_itemBounds = convertRectangles(itemBounds);
    } else {
      // popup/cascade menu
      int[] bounds = new int[menu.getItemCount() * 4];
      // prepare returned data
      menuInfo.m_menuImage = OSSupport.get().getMenuPopupVisualData(menu, bounds);
      menuInfo.m_menuBounds = new Rectangle(menuInfo.m_menuImage.getBounds());
      menuInfo.m_itemBounds = Lists.newArrayListWithCapacity(menu.getItemCount());
      // create rectangles from array
      for (int i = 0; i < menu.getItemCount(); ++i) {
        Rectangle itemRect =
            new Rectangle(bounds[i * 4 + 0],
                bounds[i * 4 + 1],
                bounds[i * 4 + 2],
                bounds[i * 4 + 3]);
        menuInfo.m_itemBounds.add(itemRect);
      }
    }
    // done
    return menuInfo;
  }

  public int getDefaultMenuBarHeight() throws Exception {
    return OSSupport.get().getDefaultMenuBarHeight();
  }

  public void beginShot(Object controlObject) {
    OSSupport.get().beginShot(controlObject);
  }

  public void endShot(Object controlObject) {
    OSSupport.get().endShot(controlObject);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Images
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object createToolkitImage(Image image) throws Exception {
    return image;
  }

  public Image createSWTImage(Object image) throws Exception {
    // save image to byte stream
    ImageLoader loader = new ImageLoader();
    loader.data = new ImageData[]{((Image) image).getImageData()};
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    loader.save(outStream, SWT.IMAGE_PNG);
    // load image from byte stream
    ByteArrayInputStream inStream = new ByteArrayInputStream(outStream.toByteArray());
    return new Image(null, inStream);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Shell
  //
  ////////////////////////////////////////////////////////////////////////////
  public void showShell(Object shellObject) throws Exception {
    final Shell shell = (Shell) shellObject;
    final Shell mainShell = DesignerPlugin.getShell();
    // [Linux] feature in SWT/GTK: since we cannot use Test/Preview shell as modal 
    // and if the 'main' shell of the application is disabled, switching to other 
    // application (and even to this Eclipse itself) and back will hide 
    // Test/Preview shell behind the 'main' shell.
    // The workaround is to forcibly hide Test/Preview window.
    ShellAdapter shellAdapter = new ShellAdapter() {
      @Override
      public void shellActivated(ShellEvent e) {
        mainShell.removeShellListener(this);
        shell.getDisplay().asyncExec(new Runnable() {
          public void run() {
            shell.setVisible(false);
          }
        });
      }
    };
    shell.setVisible(true);
    shell.setActive();
    try {
      if (EnvironmentUtils.IS_LINUX) {
        mainShell.addShellListener(shellAdapter);
      }
      // run events loop
      Display display = shell.getDisplay();
      while (!shell.isDisposed() && shell.isVisible()) {
        if (!display.readAndDispatch()) {
          display.sleep();
        }
      }
    } finally {
      if (EnvironmentUtils.IS_LINUX) {
        mainShell.removeShellListener(shellAdapter);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Font
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final FontPreviewShell m_fontPreviewShell = new FontPreviewShell();

  /*public Object getFontPreviewShell() {
  	return m_fontPreviewShell;
  }
  public void updatePreviewFont(Object font) throws Exception {
  	m_fontPreviewShell.updateFont((Font) font);
  }*/
  public String[] getFontFamilies(boolean scalable) throws Exception {
    Set<String> families = Sets.newTreeSet();
    //
    FontData[] fontList = Display.getDefault().getFontList(null, scalable);
    for (FontData fontData : fontList) {
      families.add(fontData.getName());
    }
    //
    return families.toArray(new String[families.size()]);
  }

  public Image getFontPreview(Object font) throws Exception {
    m_fontPreviewShell.updateFont((Font) font);
    return OSSupport.get().makeShot(m_fontPreviewShell);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Converts the {@link List} of {@link org.eclipse.swt.graphics.Rectangle} into {@link List} of
   * {@link org.eclipse.wb.draw2d.geometry.Rectangle}.
   */
  private List<Rectangle> convertRectangles(List<org.eclipse.swt.graphics.Rectangle> rectangles) {
    List<Rectangle> result = Lists.newArrayListWithCapacity(rectangles.size());
    for (org.eclipse.swt.graphics.Rectangle rectangle : rectangles) {
      result.add(new Rectangle(rectangle));
    }
    return result;
  }
}
