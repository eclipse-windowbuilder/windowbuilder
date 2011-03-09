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
package org.eclipse.wb.tests.designer.core.palette;

import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.editor.palette.model.PaletteInfo;
import org.eclipse.wb.internal.core.editor.palette.PaletteManager;
import org.eclipse.wb.internal.core.editor.palette.command.CategoryAddCommand;
import org.eclipse.wb.internal.core.editor.palette.command.Command;
import org.eclipse.wb.internal.core.editor.palette.command.factory.FactoryAddCommand;
import org.eclipse.wb.internal.core.editor.palette.command.factory.FactoryEditCommand;
import org.eclipse.wb.internal.core.editor.palette.model.entry.FactoryEntryInfo;
import org.eclipse.wb.internal.core.editor.palette.model.entry.InstanceFactoryEntryInfo;
import org.eclipse.wb.internal.core.editor.palette.model.entry.StaticFactoryEntryInfo;

/**
 * Tests for palette {@link FactoryEntryInfo} {@link Command}'s.
 * 
 * @author scheglov_ke
 */
public class FactoryCommandsTest extends AbstractPaletteTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Add
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_add_static() throws Exception {
    PaletteManager manager = loadManager();
    // use loaded palette
    {
      PaletteInfo palette = manager.getPalette();
      CategoryInfo category = addCategory(manager, "0");
      // add components
      manager.commands_add(new FactoryAddCommand("new id",
          "new name",
          "new description",
          true,
          "clazz",
          "signature",
          true,
          category));
      manager.commands_write();
      // check components
      StaticFactoryEntryInfo entry = (StaticFactoryEntryInfo) palette.getEntry("new id");
      assertEquals("new name", entry.getName());
      assertEquals("new description", entry.getDescription());
      assertTrue(entry.isVisible());
      assertEquals("clazz", entry.getFactoryClassName());
      assertEquals("signature", entry.getMethodSignature());
    }
    // reload palette, but commands were written, so palette still in same state
    {
      manager.reloadPalette();
      PaletteInfo palette = manager.getPalette();
      // check
      StaticFactoryEntryInfo entry = (StaticFactoryEntryInfo) palette.getEntry("new id");
      assertEquals("new name", entry.getName());
      assertEquals("new description", entry.getDescription());
      assertTrue(entry.isVisible());
      assertEquals("clazz", entry.getFactoryClassName());
      assertEquals("signature", entry.getMethodSignature());
    }
  }

  public void test_add_instance() throws Exception {
    PaletteManager manager = loadManager();
    // use loaded palette
    {
      PaletteInfo palette = manager.getPalette();
      CategoryInfo category = addCategory(manager, "0");
      // add components
      manager.commands_add(new FactoryAddCommand("0", "0", "0", true, "0", "0", false, category));
      manager.commands_write();
      // check components
      InstanceFactoryEntryInfo entry = (InstanceFactoryEntryInfo) palette.getEntry("0");
      assertEquals("0", entry.getName());
    }
    // reload palette, but commands were written, so palette still in same state
    {
      manager.reloadPalette();
      PaletteInfo palette = manager.getPalette();
      // check
      InstanceFactoryEntryInfo entry = (InstanceFactoryEntryInfo) palette.getEntry("0");
      assertEquals("0", entry.getName());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Edit
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_edit() throws Exception {
    PaletteManager manager = loadManager();
    // use loaded palette
    {
      PaletteInfo palette = manager.getPalette();
      CategoryInfo category = addCategory(manager, "0");
      // add components
      manager.commands_add(new FactoryAddCommand("0", "0", "0", true, "0", "0", true, category));
      {
        StaticFactoryEntryInfo entry = (StaticFactoryEntryInfo) palette.getEntry("0");
        assertEquals("0", entry.getName());
      }
      // edit
      manager.commands_add(new FactoryEditCommand("0", "1", "1", false, "1", "1", true));
      {
        StaticFactoryEntryInfo entry = (StaticFactoryEntryInfo) palette.getEntry("0");
        assertEquals("1", entry.getName());
        assertEquals("1", entry.getDescription());
        assertFalse(entry.isVisible());
        assertEquals("1", entry.getFactoryClassName());
        assertEquals("1", entry.getMethodSignature());
      }
      //
      manager.commands_write();
    }
    // reload palette, but commands were written, so palette still in same state
    {
      manager.reloadPalette();
      PaletteInfo palette = manager.getPalette();
      // check
      StaticFactoryEntryInfo entry = (StaticFactoryEntryInfo) palette.getEntry("0");
      assertEquals("1", entry.getName());
      assertEquals("1", entry.getDescription());
      assertFalse(entry.isVisible());
      assertEquals("1", entry.getFactoryClassName());
      assertEquals("1", entry.getMethodSignature());
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
