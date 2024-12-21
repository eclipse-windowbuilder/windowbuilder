/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.support;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.model.menu.MenuVisualData;
import org.eclipse.wb.internal.swt.support.IToolkitSupport;
import org.eclipse.wb.os.OSSupport;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
	@Override
	public void makeShots(Control controlObject) throws Exception {
		OSSupport.get().makeShots(controlObject);
	}

	@Override
	public Image getShotImage(Control controlObject) throws Exception {
		return (Image) controlObject.getData(OSSupport.WBP_IMAGE);
	}

	@Override
	public MenuVisualData fetchMenuVisualData(Menu menu) throws Exception {
		MenuVisualData menuInfo = new MenuVisualData();
		if ((menu.getStyle() & SWT.BAR) != 0) {
			// menu bar
			List<org.eclipse.swt.graphics.Rectangle> itemBounds = new ArrayList<>();
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
			menuInfo.m_itemBounds = new ArrayList<>(menu.getItemCount());
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

	@Override
	public int getDefaultMenuBarHeight() throws Exception {
		return OSSupport.get().getDefaultMenuBarHeight();
	}

	@Override
	public void beginShot(Control controlObject) {
		OSSupport.get().beginShot(controlObject);
	}

	@Override
	public void endShot(Control controlObject) {
		OSSupport.get().endShot(controlObject);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Shell
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void showShell(Shell shell) throws Exception {
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
					@Override
					public void run() {
						shell.setVisible(false);
					}
				});
			}
		};
		shell.setVisible(true);
		shell.setActive();
		shell.setFocus();
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
	@Override
	public String[] getFontFamilies(boolean scalable) throws Exception {
		Set<String> families = new TreeSet<>();
		//
		FontData[] fontList = Display.getDefault().getFontList(null, scalable);
		for (FontData fontData : fontList) {
			families.add(fontData.getName());
		}
		//
		return families.toArray(new String[families.size()]);
	}

	@Override
	public Image getFontPreview(Font font) throws Exception {
		m_fontPreviewShell.updateFont(font);
		return OSSupport.get().makeShot(m_fontPreviewShell);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Converts the {@link List} of {@link org.eclipse.swt.graphics.Rectangle} into {@link List} of
	 * {@link org.eclipse.draw2d.geometry.Rectangle}.
	 */
	private List<Rectangle> convertRectangles(List<org.eclipse.swt.graphics.Rectangle> rectangles) {
		List<Rectangle> result = new ArrayList<>(rectangles.size());
		for (org.eclipse.swt.graphics.Rectangle rectangle : rectangles) {
			result.add(new Rectangle(rectangle));
		}
		return result;
	}
}
