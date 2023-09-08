/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.XML.palette;

import org.eclipse.wb.gef.core.requests.ICreationFactory;
import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.editor.palette.PaletteManager;
import org.eclipse.wb.internal.core.xml.editor.palette.command.Command;
import org.eclipse.wb.internal.core.xml.editor.palette.model.CategoryInfo;
import org.eclipse.wb.internal.core.xml.editor.palette.model.ChooseComponentEntryInfo;
import org.eclipse.wb.internal.core.xml.editor.palette.model.IPaletteSite;
import org.eclipse.wb.internal.core.xml.editor.palette.model.PaletteInfo;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.tests.gef.UIPredicate;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for {@link ChooseComponentEntryInfo}.
 *
 * @author scheglov_ke
 */
@Ignore
public class ChooseComponentEntryInfoTest extends AbstractPaletteTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_access() throws Exception {
		ChooseComponentEntryInfo entry = new ChooseComponentEntryInfo();
		assertNotNull(entry.getIcon());
		assertNotNull(entry.getName());
		assertNotNull(entry.getDescription());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Parse
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_parse() throws Exception {
		addPaletteExtension(new String[]{
				"<category id='category_1' name='category 1'>",
				"  <x-entry id='system.chooseComponent' name='my name'"
						+ " class='org.eclipse.wb.internal.core.xml.editor.palette.model.ChooseComponentEntryInfo'/>",
		"</category>"});
		PaletteInfo palette = loadPalette();
		// prepare entry
		CategoryInfo category = palette.getCategory("category_1");
		ChooseComponentEntryInfo entry = (ChooseComponentEntryInfo) category.getEntries().get(0);
		// check component
		assertSame(category, entry.getCategory());
		assertEquals("system.chooseComponent", entry.getId());
		assertEquals("my name", entry.getName());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tool
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_createTool_select() throws Exception {
		addPaletteExtension(new String[]{
				"<category id='category_1' name='category 1'>",
				"  <x-entry id='system.chooseComponent' class='org.eclipse.wb.internal.core.xml.editor.palette.model.ChooseComponentEntryInfo'/>",
		"</category>"});
		XmlObjectInfo panel = parseEmptyPanel();
		final PaletteManager manager = new PaletteManager(panel, TOOLKIT_ID);
		manager.reloadPalette();
		// set palette site
		IPaletteSite.Helper.setSite(panel, new IPaletteSite.Empty() {
			@Override
			public Shell getShell() {
				return DesignerPlugin.getShell();
			}

			@Override
			public PaletteInfo getPalette() {
				return manager.getPalette();
			}

			@Override
			public void addCommand(Command command) {
				manager.commands_add(command);
			}
		});
		// prepare entry
		final ChooseComponentEntryInfo entry;
		{
			PaletteInfo palette = manager.getPalette();
			CategoryInfo category = palette.getCategory("category_1");
			entry = (ChooseComponentEntryInfo) category.getEntries().get(0);
		}
		// initialize
		assertTrue(entry.initialize(null, panel));
		// create tool
		CreationTool creationTool;
		{
			final CreationTool[] tools = new CreationTool[1];
			new UiContext().executeAndCheck(new UIRunnable() {
				@Override
				public void run(UiContext context) throws Exception {
					tools[0] = (CreationTool) entry.createTool();
				}
			}, new UIRunnable() {
				@Override
				public void run(UiContext context) throws Exception {
					// set filter
					{
						Text filterText = context.findFirstWidget(Text.class);
						filterText.setText("org.eclipse.swt.widgets.Button");
					}
					// wait for types
					{
						final Table typesTable = context.findFirstWidget(Table.class);
						context.waitFor(new UIPredicate() {
							@Override
							public boolean check() {
								return typesTable.getItems().length != 0;
							}
						});
					}
					// click OK
					context.clickButton("OK");
				}
			});
			creationTool = tools[0];
		}
		// check tool
		{
			ICreationFactory creationFactory = creationTool.getFactory();
			creationFactory.activate();
			// check new object
			XmlObjectInfo newObject = (XmlObjectInfo) creationFactory.getNewObject();
			{
				DocumentElement newElement = getNewCreationElement(newObject);
				assertEquals("Button", newElement.getTag());
				assertEquals("New Button", newElement.getAttribute("text"));
			}
		}
	}

	@Test
	public void test_createTool_cancel() throws Exception {
		addPaletteExtension(new String[]{
				"<category id='category_1' name='category 1'>",
				"  <x-entry id='system.chooseComponent' class='org.eclipse.wb.internal.core.xml.editor.palette.model.ChooseComponentEntryInfo'/>",
		"</category>"});
		XmlObjectInfo panel = parseEmptyPanel();
		final PaletteManager manager = new PaletteManager(panel, TOOLKIT_ID);
		manager.reloadPalette();
		// set palette site
		IPaletteSite.Helper.setSite(panel, new IPaletteSite.Empty() {
			@Override
			public Shell getShell() {
				return DesignerPlugin.getShell();
			}
		});
		// prepare entry
		final ChooseComponentEntryInfo entry;
		{
			PaletteInfo palette = manager.getPalette();
			CategoryInfo category = palette.getCategory("category_1");
			entry = (ChooseComponentEntryInfo) category.getEntries().get(0);
		}
		// initialize
		assertTrue(entry.initialize(null, panel));
		// create tool
		CreationTool creationTool;
		{
			final CreationTool[] tools = new CreationTool[1];
			new UiContext().executeAndCheck(new UIRunnable() {
				@Override
				public void run(UiContext context) throws Exception {
					tools[0] = (CreationTool) entry.createTool();
				}
			}, new UIRunnable() {
				@Override
				public void run(UiContext context) throws Exception {
					context.useShell("Open type");
					context.clickButton("Cancel");
				}
			});
			creationTool = tools[0];
		}
		// check tool
		assertNull(creationTool);
	}
}
