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
package org.eclipse.wb.tests.designer.XML.palette;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.editor.palette.PaletteManager;
import org.eclipse.wb.internal.core.xml.editor.palette.command.CategoryAddCommand;
import org.eclipse.wb.internal.core.xml.editor.palette.command.CategoryEditCommand;
import org.eclipse.wb.internal.core.xml.editor.palette.command.CategoryMoveCommand;
import org.eclipse.wb.internal.core.xml.editor.palette.command.CategoryRemoveCommand;
import org.eclipse.wb.internal.core.xml.editor.palette.command.Command;
import org.eclipse.wb.internal.core.xml.editor.palette.command.ElementVisibilityCommand;
import org.eclipse.wb.internal.core.xml.editor.palette.model.CategoryInfo;
import org.eclipse.wb.internal.core.xml.editor.palette.model.PaletteInfo;

import org.junit.Test;

import java.util.List;

/**
 * Tests for palette {@link CategoryInfo} {@link Command}'s.
 *
 * @author scheglov_ke
 */
public class CategoryCommandsTest extends AbstractPaletteTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Add
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_add_last() throws Exception {
		PaletteManager manager = loadManager();
		// use loaded palette
		{
			PaletteInfo palette = manager.getPalette();
			// initially palette is empty
			assertEquals(0, palette.getCategories().size());
			// add category using command
			manager.commands_add(new CategoryAddCommand("new id",
					"new name",
					"new description",
					true,
					true,
					null));
			manager.commands_write();
			// check new category
			assertEquals(1, palette.getCategories().size());
			CategoryInfo category = palette.getCategory("new id");
			assertEquals("new name", category.getName());
			assertEquals("new description", category.getDescription());
			assertTrue(category.isVisible());
			assertTrue(category.isOpen());
		}
		// reload palette, but commands were written, so palette still in same state
		{
			manager.reloadPalette();
			PaletteInfo palette = manager.getPalette();
			assertEquals(1, palette.getCategories().size());
			// check
			CategoryInfo category = palette.getCategory("new id");
			assertNotNull(category);
			assertEquals("new name", category.getName());
			assertEquals("new description", category.getDescription());
			assertTrue(category.isVisible());
			assertTrue(category.isOpen());
		}
	}

	@Test
	public void test_add_before() throws Exception {
		PaletteManager manager = loadManager();
		// use loaded palette
		{
			PaletteInfo palette = manager.getPalette();
			// initially palette is empty
			assertEquals(0, palette.getCategories().size());
			// add categories using command
			manager.commands_add(new CategoryAddCommand("0", "0", "0", true, true, null));
			manager.commands_add(new CategoryAddCommand("1", "1", "1", true, true, "0"));
			manager.commands_write();
			// check new category
			assertEquals(2, palette.getCategories().size());
			assertEquals("1", palette.getCategories().get(0).getId());
			assertEquals("0", palette.getCategories().get(1).getId());
		}
		// reload palette, but commands were written, so palette still in same state
		{
			manager.reloadPalette();
			PaletteInfo palette = manager.getPalette();
			assertEquals(2, palette.getCategories().size());
			// check
			assertEquals("1", palette.getCategories().get(0).getId());
			assertEquals("0", palette.getCategories().get(1).getId());
		}
	}

	/**
	 * Check for special characters in text attributes.
	 */
	@Test
	public void test_add_specialCharacters() throws Exception {
		PaletteManager manager = loadManager();
		// use loaded palette
		{
			PaletteInfo palette = manager.getPalette();
			// initially palette is empty
			assertEquals(0, palette.getCategories().size());
			// add category using command
			manager.commands_add(new CategoryAddCommand("id",
					"name\nsecond line",
					"description",
					true,
					true,
					null));
			manager.commands_write();
		}
		// reload palette, but commands were written, so palette still in same state
		{
			manager.reloadPalette();
			PaletteInfo palette = manager.getPalette();
			assertEquals(1, palette.getCategories().size());
			// check
			CategoryInfo category = palette.getCategory("id");
			assertEquals("name\nsecond line", category.getName());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Edit
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_edit() throws Exception {
		PaletteManager manager = loadManager();
		// use loaded palette
		{
			PaletteInfo palette = manager.getPalette();
			// initially palette is empty
			assertEquals(0, palette.getCategories().size());
			// check new category
			manager.commands_add(new CategoryAddCommand("0", "0", "0", true, true, null));
			{
				assertEquals(1, palette.getCategories().size());
				CategoryInfo category = palette.getCategory("0");
				assertEquals("0", category.getName());
				assertEquals("0", category.getDescription());
				assertTrue(category.isVisible());
				assertTrue(category.isOpen());
			}
			// check updated category
			manager.commands_add(new CategoryEditCommand("0", "name", "description", false, false));
			{
				assertEquals(1, palette.getCategories().size());
				CategoryInfo category = palette.getCategory("0");
				assertEquals("name", category.getName());
				assertEquals("description", category.getDescription());
				assertFalse(category.isVisible());
				assertFalse(category.isOpen());
			}
			// write
			manager.commands_write();
		}
		// reload palette, but commands were written, so palette still in same state
		{
			manager.reloadPalette();
			PaletteInfo palette = manager.getPalette();
			assertEquals(1, palette.getCategories().size());
			// check
			CategoryInfo category = palette.getCategory("0");
			assertEquals("name", category.getName());
			assertEquals("description", category.getDescription());
			assertFalse(category.isVisible());
			assertFalse(category.isOpen());
		}
	}

	/**
	 * When we do second "edit", first "edit" command can be removed.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void test_edit_twoTimes() throws Exception {
		PaletteManager manager = loadManager();
		List<Command> commands = (List<Command>) ReflectionUtils.getFieldObject(manager, "m_commands");
		// use loaded palette
		{
			PaletteInfo palette = manager.getPalette();
			// initially palette is empty
			assertEquals(0, palette.getCategories().size());
			// check new category
			{
				manager.commands_add(new CategoryAddCommand("0", "0", "0", true, true, null));
				assertEquals(1, commands.size());
				CategoryInfo category = palette.getCategory("0");
				assertEquals("0", category.getName());
			}
			// edit: 1
			{
				manager.commands_add(new CategoryEditCommand("0", "1", "1", false, false));
				assertEquals(2, commands.size());
				CategoryInfo category = palette.getCategory("0");
				assertEquals("1", category.getName());
			}
			// edit: 2, same count of commands
			{
				manager.commands_add(new CategoryEditCommand("0", "2", "2", false, false));
				assertEquals(2, commands.size());
				CategoryInfo category = palette.getCategory("0");
				assertEquals("2", category.getName());
			}
			// write
			manager.commands_write();
		}
		// reload palette, but commands were written, so palette still in same state
		{
			manager.reloadPalette();
			PaletteInfo palette = manager.getPalette();
			CategoryInfo category = palette.getCategory("0");
			assertEquals("2", category.getName());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Move
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_move_before() throws Exception {
		PaletteManager manager = loadManager();
		// use loaded palette
		{
			PaletteInfo palette = manager.getPalette();
			// initially palette is empty
			assertEquals(0, palette.getCategories().size());
			// add categories
			manager.commands_add(new CategoryAddCommand("0", "0", "0", true, true, null));
			manager.commands_add(new CategoryAddCommand("1", "1", "1", true, true, null));
			// check new categories
			assertEquals(2, palette.getCategories().size());
			CategoryInfo category_0 = palette.getCategories().get(0);
			CategoryInfo category_1 = palette.getCategories().get(1);
			assertEquals("0", category_0.getId());
			assertEquals("1", category_1.getId());
			// move category
			manager.commands_add(new CategoryMoveCommand(category_1, category_0));
			assertEquals("1", palette.getCategories().get(0).getId());
			assertEquals("0", palette.getCategories().get(1).getId());
			// write
			manager.commands_write();
		}
		// reload palette, but commands were written, so palette still in same state
		{
			manager.reloadPalette();
			PaletteInfo palette = manager.getPalette();
			assertEquals(2, palette.getCategories().size());
			// check
			assertEquals("1", palette.getCategories().get(0).getId());
			assertEquals("0", palette.getCategories().get(1).getId());
		}
	}

	@Test
	public void test_move_last() throws Exception {
		PaletteManager manager = loadManager();
		// use loaded palette
		{
			PaletteInfo palette = manager.getPalette();
			// initially palette is empty
			assertEquals(0, palette.getCategories().size());
			// add categories
			manager.commands_add(new CategoryAddCommand("0", "0", "0", true, true, null));
			manager.commands_add(new CategoryAddCommand("1", "1", "1", true, true, null));
			// check new categories
			assertEquals(2, palette.getCategories().size());
			CategoryInfo category_0 = palette.getCategories().get(0);
			CategoryInfo category_1 = palette.getCategories().get(1);
			assertEquals("0", category_0.getId());
			assertEquals("1", category_1.getId());
			// move category
			manager.commands_add(new CategoryMoveCommand(category_0, null));
			assertEquals("1", palette.getCategories().get(0).getId());
			assertEquals("0", palette.getCategories().get(1).getId());
			// write
			manager.commands_write();
		}
		// reload palette, but commands were written, so palette still in same state
		{
			manager.reloadPalette();
			PaletteInfo palette = manager.getPalette();
			assertEquals(2, palette.getCategories().size());
			// check
			assertEquals("1", palette.getCategories().get(0).getId());
			assertEquals("0", palette.getCategories().get(1).getId());
		}
	}

	/**
	 * Don't move before self.
	 */
	@Test
	public void test_move_noop() throws Exception {
		PaletteManager manager = loadManager();
		// use loaded palette
		{
			PaletteInfo palette = manager.getPalette();
			// initially palette is empty
			assertEquals(0, palette.getCategories().size());
			// add categories
			manager.commands_add(new CategoryAddCommand("0", "0", "0", true, true, null));
			// check new categories
			assertEquals(1, palette.getCategories().size());
			CategoryInfo category_0 = palette.getCategories().get(0);
			assertEquals("0", category_0.getId());
			// move category
			manager.commands_add(new CategoryMoveCommand(category_0, category_0));
			assertEquals("0", palette.getCategories().get(0).getId());
			// write
			manager.commands_write();
		}
		// reload palette, but commands were written, so palette still in same state
		{
			manager.reloadPalette();
			PaletteInfo palette = manager.getPalette();
			assertEquals(1, palette.getCategories().size());
			assertEquals("0", palette.getCategories().get(0).getId());
		}
	}

	/**
	 * Update {@link CategoryMoveCommand} so that source {@link CategoryInfo} can not be found.
	 */
	@Test
	public void test_move_noSource() throws Exception {
		PaletteManager manager = loadManager();
		// use loaded palette
		{
			PaletteInfo palette = manager.getPalette();
			// initially palette is empty
			assertEquals(0, palette.getCategories().size());
			// add categories
			manager.commands_add(new CategoryAddCommand("0", "0", "0", true, true, null));
			manager.commands_add(new CategoryAddCommand("1", "1", "1", true, true, null));
			// check new categories
			assertEquals(2, palette.getCategories().size());
			CategoryInfo category_0 = palette.getCategories().get(0);
			CategoryInfo category_1 = palette.getCategories().get(1);
			assertEquals("0", category_0.getId());
			assertEquals("1", category_1.getId());
			// move category
			{
				CategoryMoveCommand moveCommand = new CategoryMoveCommand(category_0, null);
				ReflectionUtils.getFieldByName(CategoryMoveCommand.class, "m_id").set(
						moveCommand,
						"no-such-category");
				manager.commands_add(moveCommand);
				assertEquals("0", palette.getCategories().get(0).getId());
				assertEquals("1", palette.getCategories().get(1).getId());
			}
			// write
			manager.commands_write();
		}
		// reload palette, but commands were written, so palette still in same state
		{
			manager.reloadPalette();
			PaletteInfo palette = manager.getPalette();
			assertEquals(2, palette.getCategories().size());
			// check
			assertEquals("0", palette.getCategories().get(0).getId());
			assertEquals("1", palette.getCategories().get(1).getId());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Remove
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_remove() throws Exception {
		PaletteManager manager = loadManager();
		// use loaded palette
		{
			PaletteInfo palette = manager.getPalette();
			// initially palette is empty
			assertEquals(0, palette.getCategories().size());
			// add categories
			manager.commands_add(new CategoryAddCommand("0", "0", "0", true, true, null));
			// check new categories
			assertEquals(1, palette.getCategories().size());
			CategoryInfo category_0 = palette.getCategories().get(0);
			assertEquals("0", category_0.getId());
			// remove category
			manager.commands_add(new CategoryRemoveCommand(category_0));
			assertEquals(0, palette.getCategories().size());
			// write
			manager.commands_write();
		}
		// reload palette, but commands were written, so palette still in same state
		{
			manager.reloadPalette();
			PaletteInfo palette = manager.getPalette();
			assertEquals(0, palette.getCategories().size());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Visibility
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_visibility() throws Exception {
		PaletteManager manager = loadManager();
		// use loaded palette
		{
			CategoryInfo category;
			{
				manager.commands_add(new CategoryAddCommand("0", "0", "0", true, true, null));
				category = manager.getPalette().getCategory("0");
			}
			assertTrue(category.isVisible());
			// set invisible
			manager.commands_add(new ElementVisibilityCommand(category, false));
			assertFalse(category.isVisible());
			// write
			manager.commands_write();
		}
		// reload palette, but commands were written, so palette still in same state
		{
			manager.reloadPalette();
			PaletteInfo palette = manager.getPalette();
			assertFalse(palette.getCategory("0").isVisible());
		}
	}
}
