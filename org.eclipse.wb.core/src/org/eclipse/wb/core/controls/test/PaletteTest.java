/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *    DSA - Add icons type to parameter to addPopupActions
 *******************************************************************************/
package org.eclipse.wb.core.controls.test;

import org.eclipse.wb.core.controls.flyout.FlyoutControlComposite;
import org.eclipse.wb.core.controls.flyout.IFlyoutPreferences;
import org.eclipse.wb.core.controls.flyout.MemoryFlyoutPreferences;
import org.eclipse.wb.core.controls.palette.DesignerContainer;
import org.eclipse.wb.core.controls.palette.DesignerEntry;
import org.eclipse.wb.core.controls.palette.ICategory;
import org.eclipse.wb.core.controls.palette.IEntry;
import org.eclipse.wb.core.controls.palette.IPalette;
import org.eclipse.wb.core.controls.palette.PaletteComposite;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import java.util.ArrayList;
import java.util.List;

/**
 * Application for testing {@link PaletteComposite}.
 *
 * @author scheglov_ke
 * @coverage core.test
 */
public class PaletteTest implements ColorConstants {
	////////////////////////////////////////////////////////////////////////////
	//
	// Main
	//
	////////////////////////////////////////////////////////////////////////////
	public static void main(String[] args) {
		try {
			PaletteTest window = new PaletteTest();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void open() {
		final Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GUI
	//
	////////////////////////////////////////////////////////////////////////////
	protected Shell shell;

	protected void createContents() {
		shell = new Shell();
		shell.setBounds(600, 300, 800, 600);
		shell.setText("SWT Application");
		shell.setLayout(new FillLayout());
		// create PaletteComposite
		PaletteComposite paletteComposite;
		{
			IFlyoutPreferences preferences =
					new MemoryFlyoutPreferences(IFlyoutPreferences.DOCK_WEST,
							IFlyoutPreferences.STATE_OPEN,
							200);
			FlyoutControlComposite flyoutControlComposite =
					new FlyoutControlComposite(shell, SWT.NONE, preferences);
			flyoutControlComposite.setTitleText("Palette");
			// palette
			{
				paletteComposite = new PaletteComposite(flyoutControlComposite.getFlyoutParent(), SWT.NONE);
				GridDataFactory.create(paletteComposite).grabV().hintHC(30).fill();
			}
			// filler
			{
				new Composite(flyoutControlComposite.getClientParent(), SWT.BORDER);
			}
		}
		// create palette model
		PaletteImpl palette;
		{
			palette = new PaletteImpl();
			{
				CategoryImpl category = new CategoryImpl("First category", true);
				palette.addCategory(category);
				category.add(new EntryImpl(true, createIcon(red), "AAAAAAAAAAA"));
				category.add(new EntryImpl(false, createIcon(green), "BBBBB"));
				category.add(new EntryImpl(true, createIcon(blue), "CCCCCCCCCCCCCCC"));
				category.add(new EntryImpl(true, createIcon(yellow), "DDDDDDDDD"));
				category.add(new EntryImpl(false, createIcon(orange), "EEEEEEEEEEE"));
				category.add(new EntryImpl(true, createIcon(cyan), "FFFFF"));
			}
			{
				CategoryImpl category = new CategoryImpl("Second category", false);
				palette.addCategory(category);
				category.add(new EntryImpl(true, createIcon(red), "0123456789"));
				category.add(new EntryImpl(true, createIcon(green), "012345"));
				category.add(new EntryImpl(true, createIcon(blue), "0123456789123"));
			}
			{
				CategoryImpl category = new CategoryImpl("Third category", true);
				palette.addCategory(category);
				category.add(new EntryImpl(true, createIcon(red), "0123456789"));
				category.add(new EntryImpl(true, createIcon(green), "012345"));
				category.add(new EntryImpl(true, createIcon(blue), "0123456789123"));
			}
		}
		// set palette
		paletteComposite.setPalette(palette);
	}

	/**
	 * @return the test icon.
	 */
	private final ImageDescriptor createIcon(Color color) {
		int size = 16;
		Image image = new Image(shell.getDisplay(), size, size);
		GC gc = new GC(image);
		gc.setBackground(color);
		gc.fillRectangle(0, 0, size, size);
		gc.dispose();
		return ImageDescriptor.createFromImage(image);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Palette implementation
	//
	////////////////////////////////////////////////////////////////////////////
	private static final class PaletteImpl implements IPalette {
		private final List<ICategory> m_categories = new ArrayList<>();

		////////////////////////////////////////////////////////////////////////////
		//
		// Access
		//
		////////////////////////////////////////////////////////////////////////////
		public void addCategory(ICategory category) {
			m_categories.add(category);
		}

		@Override
		public List<ICategory> getCategories() {
			return m_categories;
		}

		/** {@inheritDoc} */
		@Override
		public void addPopupActions(IMenuManager menuManager, Object target, int iconType) {
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Operations
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public void selectDefault() {
		}

		@Override
		public void moveCategory(ICategory category, ICategory nextCategory) {
		}

		@Override
		public void moveEntry(IEntry entry, ICategory targetCategory, IEntry nextEntry) {
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Category implementation
	//
	////////////////////////////////////////////////////////////////////////////
	private static final class CategoryImpl extends DesignerContainer {
		private boolean m_open;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public CategoryImpl(String text, boolean open) {
			super(text, null);
			m_open = open;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// ICategory
		//
		////////////////////////////////////////////////////////////////////////////

		@Override
		public boolean isOpen() {
			return m_open;
		}

		@Override
		public void setOpen(boolean b) {
			m_open = b;
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Entry implementation
	//
	////////////////////////////////////////////////////////////////////////////
	private static final class EntryImpl extends DesignerEntry {
		private final boolean m_enabled;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public EntryImpl(boolean enabled, ImageDescriptor icon, String text) {
			super(text, null, icon);
			m_enabled = enabled;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Access
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public boolean isEnabled() {
			return m_enabled;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Activation
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public boolean activate(boolean reload) {
			return true;
		}
	}
}
