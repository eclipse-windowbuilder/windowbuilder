/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
				category.addEntry(new EntryImpl(true, createIcon(red), "AAAAAAAAAAA"));
				category.addEntry(new EntryImpl(false, createIcon(green), "BBBBB"));
				category.addEntry(new EntryImpl(true, createIcon(blue), "CCCCCCCCCCCCCCC"));
				category.addEntry(new EntryImpl(true, createIcon(yellow), "DDDDDDDDD"));
				category.addEntry(new EntryImpl(false, createIcon(orange), "EEEEEEEEEEE"));
				category.addEntry(new EntryImpl(true, createIcon(cyan), "FFFFF"));
			}
			{
				CategoryImpl category = new CategoryImpl("Second category", false);
				palette.addCategory(category);
				category.addEntry(new EntryImpl(true, createIcon(red), "0123456789"));
				category.addEntry(new EntryImpl(true, createIcon(green), "012345"));
				category.addEntry(new EntryImpl(true, createIcon(blue), "0123456789123"));
			}
			{
				CategoryImpl category = new CategoryImpl("Third category", true);
				palette.addCategory(category);
				category.addEntry(new EntryImpl(true, createIcon(red), "0123456789"));
				category.addEntry(new EntryImpl(true, createIcon(green), "012345"));
				category.addEntry(new EntryImpl(true, createIcon(blue), "0123456789123"));
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
	private static final class CategoryImpl implements ICategory {
		private final String m_text;
		private boolean m_open;
		private final List<IEntry> m_entries = new ArrayList<>();

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public CategoryImpl(String text, boolean open) {
			m_text = text;
			m_open = open;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Access
		//
		////////////////////////////////////////////////////////////////////////////
		public void addEntry(IEntry entry) {
			m_entries.add(entry);
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// ICategory
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public String getText() {
			return m_text;
		}

		@Override
		public String getToolTipText() {
			return null;
		}

		@Override
		public boolean isOpen() {
			return m_open;
		}

		@Override
		public void setOpen(boolean b) {
			m_open = b;
		}

		@Override
		public List<IEntry> getEntries() {
			return m_entries;
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Entry implementation
	//
	////////////////////////////////////////////////////////////////////////////
	private static final class EntryImpl implements IEntry {
		private final boolean m_enabled;
		private final ImageDescriptor m_icon;
		private final String m_text;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public EntryImpl(boolean enabled, ImageDescriptor icon, String text) {
			m_enabled = enabled;
			m_icon = icon;
			m_text = text;
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

		@Override
		public ImageDescriptor getIcon() {
			return m_icon;
		}

		@Override
		public String getText() {
			return m_text;
		}

		@Override
		public String getToolTipText() {
			return null;
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
