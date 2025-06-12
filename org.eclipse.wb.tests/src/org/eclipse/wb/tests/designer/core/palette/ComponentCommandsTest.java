/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.core.palette;

import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.editor.palette.model.PaletteInfo;
import org.eclipse.wb.core.editor.palette.model.entry.ComponentEntryInfo;
import org.eclipse.wb.internal.core.editor.palette.PaletteManager;
import org.eclipse.wb.internal.core.editor.palette.command.CategoryAddCommand;
import org.eclipse.wb.internal.core.editor.palette.command.Command;
import org.eclipse.wb.internal.core.editor.palette.command.ComponentAddCommand;
import org.eclipse.wb.internal.core.editor.palette.command.ComponentEditCommand;
import org.eclipse.wb.internal.core.editor.palette.command.ElementVisibilityCommand;
import org.eclipse.wb.internal.core.editor.palette.command.EntryMoveCommand;
import org.eclipse.wb.internal.core.editor.palette.command.EntryRemoveCommand;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Tests for palette {@link ComponentEntryInfo} {@link Command}'s.
 *
 * @author scheglov_ke
 */
public class ComponentCommandsTest extends AbstractPaletteTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Add
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_add() throws Exception {
		PaletteManager manager = loadManager();
		// use loaded palette
		{
			PaletteInfo palette = manager.getPalette();
			CategoryInfo category = addCategory(manager, "0");
			// add components
			manager.commands_add(new ComponentAddCommand("new id",
					"new name",
					"new description",
					true,
					"javax.swing.JButton",
					category));
			manager.commands_write();
			// check components
			ComponentEntryInfo entry = (ComponentEntryInfo) palette.getEntry("new id");
			assertEquals("new name", entry.getName());
			assertEquals("new description", entry.getDescription());
			assertEquals("javax.swing.JButton", entry.getClassName());
			assertTrue(entry.isVisible());
		}
		// reload palette, but commands were written, so palette still in same state
		{
			manager.reloadPalette();
			PaletteInfo palette = manager.getPalette();
			// check
			ComponentEntryInfo entry = (ComponentEntryInfo) palette.getEntry("new id");
			assertEquals("new name", entry.getName());
			assertEquals("new description", entry.getDescription());
			assertEquals("javax.swing.JButton", entry.getClassName());
			assertTrue(entry.isVisible());
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
			CategoryInfo category = addCategory(manager, "0");
			// add component
			manager.commands_add(new ComponentAddCommand("0", "0", "0", true, "0", category));
			{
				ComponentEntryInfo entry = (ComponentEntryInfo) palette.getEntry("0");
				assertEquals("0", entry.getName());
				assertEquals("0", entry.getDescription());
				assertEquals("0", entry.getClassName());
				assertTrue(entry.isVisible());
			}
			// edit component
			manager.commands_add(new ComponentEditCommand("0", "1", "1", false, "1"));
			{
				ComponentEntryInfo entry = (ComponentEntryInfo) palette.getEntry("0");
				assertEquals("1", entry.getName());
				assertEquals("1", entry.getDescription());
				assertEquals("1", entry.getClassName());
				assertFalse(entry.isVisible());
			}
			// write
			manager.commands_write();
		}
		// reload palette, but commands were written, so palette still in same state
		{
			manager.reloadPalette();
			PaletteInfo palette = manager.getPalette();
			// check
			ComponentEntryInfo entry = (ComponentEntryInfo) palette.getEntry("0");
			assertEquals("1", entry.getName());
			assertEquals("1", entry.getDescription());
			assertEquals("1", entry.getClassName());
			assertFalse(entry.isVisible());
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
			CategoryInfo category = addCategory(manager, "0");
			// add components
			manager.commands_add(new ComponentAddCommand("0", "0", "0", true, "0", category));
			manager.commands_add(new ComponentAddCommand("1", "1", "1", true, "1", category));
			assertEquals("0", category.getEntries().get(0).getId());
			assertEquals("1", category.getEntries().get(1).getId());
			// move component
			manager.commands_add(new EntryMoveCommand(category.getEntries().get(1),
					category,
					category.getEntries().get(0)));
			assertEquals("1", category.getEntries().get(0).getId());
			assertEquals("0", category.getEntries().get(1).getId());
			// write
			manager.commands_write();
		}
		// reload palette, but commands were written, so palette still in same state
		{
			manager.reloadPalette();
			PaletteInfo palette = manager.getPalette();
			CategoryInfo category = palette.getCategory("0");
			// check
			assertEquals("1", category.getEntries().get(0).getId());
			assertEquals("0", category.getEntries().get(1).getId());
		}
	}

	@Test
	public void test_move_last() throws Exception {
		PaletteManager manager = loadManager();
		// use loaded palette
		{
			CategoryInfo category = addCategory(manager, "0");
			// add components
			manager.commands_add(new ComponentAddCommand("0", "0", "0", true, "0", category));
			manager.commands_add(new ComponentAddCommand("1", "1", "1", true, "1", category));
			assertEquals("0", category.getEntries().get(0).getId());
			assertEquals("1", category.getEntries().get(1).getId());
			// move component
			manager.commands_add(new EntryMoveCommand(category.getEntries().get(0), category, null));
			assertEquals("1", category.getEntries().get(0).getId());
			assertEquals("0", category.getEntries().get(1).getId());
			// write
			manager.commands_write();
		}
		// reload palette, but commands were written, so palette still in same state
		{
			manager.reloadPalette();
			PaletteInfo palette = manager.getPalette();
			CategoryInfo category = palette.getCategory("0");
			// check
			assertEquals("1", category.getEntries().get(0).getId());
			assertEquals("0", category.getEntries().get(1).getId());
		}
	}

	@Test
	public void test_move_noop() throws Exception {
		PaletteManager manager = loadManager();
		// use loaded palette
		{
			CategoryInfo category = addCategory(manager, "0");
			// add components
			manager.commands_add(new ComponentAddCommand("0", "0", "0", true, "0", category));
			manager.commands_add(new ComponentAddCommand("1", "1", "1", true, "1", category));
			assertEquals("0", category.getEntries().get(0).getId());
			assertEquals("1", category.getEntries().get(1).getId());
			// move component
			manager.commands_add(new EntryMoveCommand(category.getEntries().get(1),
					category,
					category.getEntries().get(1)));
			assertEquals("0", category.getEntries().get(0).getId());
			assertEquals("1", category.getEntries().get(1).getId());
			// write
			manager.commands_write();
		}
		// reload palette, but commands were written, so palette still in same state
		{
			manager.reloadPalette();
			PaletteInfo palette = manager.getPalette();
			CategoryInfo category = palette.getCategory("0");
			// check
			assertEquals("0", category.getEntries().get(0).getId());
			assertEquals("1", category.getEntries().get(1).getId());
		}
	}

	/**
	 * Remove old "move" commands if same entry is moved several times.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void test_move_twoTimes() throws Exception {
		PaletteManager manager = loadManager();
		List<Command> commands = (List<Command>) ReflectionUtils.getFieldObject(manager, "m_commands");
		// use loaded palette
		{
			CategoryInfo category = addCategory(manager, "0");
			// add components
			manager.commands_add(new ComponentAddCommand("0", "0", "0", true, "0", category));
			manager.commands_add(new ComponentAddCommand("1", "1", "1", true, "1", category));
			assertEquals("0", category.getEntries().get(0).getId());
			assertEquals("1", category.getEntries().get(1).getId());
			// move component
			assertEquals(3, commands.size());
			manager.commands_add(new EntryMoveCommand(category.getEntries().get(1), category, null));
			assertEquals(4, commands.size());
			manager.commands_add(new EntryMoveCommand(category.getEntries().get(1), category, null));
			assertEquals(4, commands.size()); // add new "move", remove old "move"
			assertEquals("0", category.getEntries().get(0).getId());
			assertEquals("1", category.getEntries().get(1).getId());
			// write
			manager.commands_write();
		}
		// reload palette, but commands were written, so palette still in same state
		{
			manager.reloadPalette();
			PaletteInfo palette = manager.getPalette();
			CategoryInfo category = palette.getCategory("0");
			// check
			assertEquals("0", category.getEntries().get(0).getId());
			assertEquals("1", category.getEntries().get(1).getId());
		}
	}

	/**
	 * Stop "move" optimization if entry used as target.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void test_move_stopIfTarget() throws Exception {
		PaletteManager manager = loadManager();
		List<Command> commands = (List<Command>) ReflectionUtils.getFieldObject(manager, "m_commands");
		// use loaded palette
		{
			CategoryInfo category = addCategory(manager, "0");
			// add components
			manager.commands_add(new ComponentAddCommand("0", "0", "0", true, "0", category));
			manager.commands_add(new ComponentAddCommand("1", "1", "1", true, "1", category));
			assertEquals("0", category.getEntries().get(0).getId());
			assertEquals("1", category.getEntries().get(1).getId());
			assertEquals(3, commands.size());
			// step 1
			{
				manager.commands_add(new EntryMoveCommand(category.getEntries().get(0), category, null));
				assertEquals(4, commands.size());
				assertEquals("1", category.getEntries().get(0).getId());
				assertEquals("0", category.getEntries().get(1).getId());
			}
			// step 2
			{
				manager.commands_add(new EntryMoveCommand(category.getEntries().get(0),
						category,
						category.getEntries().get(1)));
				assertEquals(5, commands.size());
				assertEquals("1", category.getEntries().get(0).getId());
				assertEquals("0", category.getEntries().get(1).getId());
			}
			// step 3, no optimization
			{
				manager.commands_add(new EntryMoveCommand(category.getEntries().get(1), category, null));
				assertEquals(6, commands.size());
				assertEquals("1", category.getEntries().get(0).getId());
				assertEquals("0", category.getEntries().get(1).getId());
			}
			// write
			manager.commands_write();
		}
		// reload palette, but commands were written, so palette still in same state
		{
			manager.reloadPalette();
			PaletteInfo palette = manager.getPalette();
			CategoryInfo category = palette.getCategory("0");
			// check
			assertEquals("1", category.getEntries().get(0).getId());
			assertEquals("0", category.getEntries().get(1).getId());
		}
	}

	@Test
	public void test_move_noSource() throws Exception {
		PaletteManager manager = loadManager();
		// use loaded palette
		{
			CategoryInfo category = addCategory(manager, "0");
			// add components
			manager.commands_add(new ComponentAddCommand("0", "0", "0", true, "0", category));
			manager.commands_add(new ComponentAddCommand("1", "1", "1", true, "1", category));
			assertEquals("0", category.getEntries().get(0).getId());
			assertEquals("1", category.getEntries().get(1).getId());
			// move component
			{
				EntryMoveCommand moveCommand =
						new EntryMoveCommand(category.getEntries().get(1), category, null);
				ReflectionUtils.setField(moveCommand, "m_id", "no-such-entry");
				manager.commands_add(moveCommand);
			}
			assertEquals("0", category.getEntries().get(0).getId());
			assertEquals("1", category.getEntries().get(1).getId());
			// write
			manager.commands_write();
		}
		// reload palette, but commands were written, so palette still in same state
		{
			manager.reloadPalette();
			PaletteInfo palette = manager.getPalette();
			CategoryInfo category = palette.getCategory("0");
			// check
			assertEquals("0", category.getEntries().get(0).getId());
			assertEquals("1", category.getEntries().get(1).getId());
		}
	}

	@Test
	public void test_move_noTargetCategory() throws Exception {
		PaletteManager manager = loadManager();
		// use loaded palette
		{
			CategoryInfo category = addCategory(manager, "0");
			// add components
			manager.commands_add(new ComponentAddCommand("0", "0", "0", true, "0", category));
			manager.commands_add(new ComponentAddCommand("1", "1", "1", true, "1", category));
			assertEquals("0", category.getEntries().get(0).getId());
			assertEquals("1", category.getEntries().get(1).getId());
			// move component
			{
				EntryMoveCommand moveCommand =
						new EntryMoveCommand(category.getEntries().get(1), category, null);
				ReflectionUtils.setField(moveCommand, "m_categoryId", "no-such-category");
				manager.commands_add(moveCommand);
			}
			assertEquals("0", category.getEntries().get(0).getId());
			assertEquals("1", category.getEntries().get(1).getId());
			// write
			manager.commands_write();
		}
		// reload palette, but commands were written, so palette still in same state
		{
			manager.reloadPalette();
			PaletteInfo palette = manager.getPalette();
			CategoryInfo category = palette.getCategory("0");
			// check
			assertEquals("0", category.getEntries().get(0).getId());
			assertEquals("1", category.getEntries().get(1).getId());
		}
	}

	@Test
	public void test_move_otherCategory() throws Exception {
		PaletteManager manager = loadManager();
		// use loaded palette
		{
			CategoryInfo category_0 = addCategory(manager, "0");
			CategoryInfo category_1 = addCategory(manager, "1");
			// add components
			manager.commands_add(new ComponentAddCommand("0", "0", "0", true, "0", category_0));
			manager.commands_add(new ComponentAddCommand("1", "1", "1", true, "1", category_1));
			assertEquals("0", category_0.getEntries().get(0).getId());
			assertEquals("1", category_1.getEntries().get(0).getId());
			// move component
			manager.commands_add(new EntryMoveCommand(category_1.getEntries().get(0), category_0, null));
			assertEquals("0", category_0.getEntries().get(0).getId());
			assertEquals("1", category_0.getEntries().get(1).getId());
			// write
			manager.commands_write();
		}
		// reload palette, but commands were written, so palette still in same state
		{
			manager.reloadPalette();
			PaletteInfo palette = manager.getPalette();
			CategoryInfo category = palette.getCategory("0");
			assertNotNull(palette.getCategory("1"));
			// check
			assertEquals("0", category.getEntries().get(0).getId());
			assertEquals("1", category.getEntries().get(1).getId());
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
			CategoryInfo category = addCategory(manager, "0");
			// add component
			manager.commands_add(new ComponentAddCommand("0", "0", "0", true, "0", category));
			assertEquals(1, category.getEntries().size());
			// remove component
			manager.commands_add(new EntryRemoveCommand(category.getEntries().get(0)));
			assertEquals(0, category.getEntries().size());
			// write
			manager.commands_write();
		}
		// reload palette, but commands were written, so palette still in same state
		{
			manager.reloadPalette();
			PaletteInfo palette = manager.getPalette();
			// check
			assertNull(palette.getEntry("0"));
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
			CategoryInfo category = addCategory(manager, "0");
			// add component
			manager.commands_add(new ComponentAddCommand("1", "1", "1", true, "1", category));
			EntryInfo entry = category.getEntries().get(0);
			assertTrue(entry.isVisible());
			// set invisible
			manager.commands_add(new ElementVisibilityCommand(entry, false));
			assertFalse(entry.isVisible());
			// write
			manager.commands_write();
		}
		// reload palette, but commands were written, so palette still in same state
		{
			manager.reloadPalette();
			PaletteInfo palette = manager.getPalette();
			assertFalse(palette.getEntry("1").isVisible());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the new {@link CategoryInfo} with given "id", added using {@link CategoryAddCommand}.
	 */
	private static CategoryInfo addCategory(PaletteManager manager, String id) {
		PaletteInfo palette = manager.getPalette();
		manager.commands_add(new CategoryAddCommand(id, "0", "0", true, true, null));
		return palette.getCategory(id);
	}
}
