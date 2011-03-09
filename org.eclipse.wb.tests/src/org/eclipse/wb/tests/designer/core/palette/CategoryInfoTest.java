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
import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.editor.palette.model.PaletteInfo;
import org.eclipse.wb.core.editor.palette.model.entry.ComponentEntryInfo;

/**
 * Tests for {@link CategoryInfo}.
 * 
 * @author scheglov_ke
 */
public class CategoryInfoTest extends AbstractPaletteTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_toString() throws Exception {
    CategoryInfo category = new CategoryInfo();
    category.setId("1");
    category.setName("category 1");
    // add entry
    {
      ComponentEntryInfo component = new ComponentEntryInfo();
      component.setComponentClassName("javax.swing.JButton");
      category.addEntry(component);
    }
    // check
    assertEquals(
        "Category(id='1', name='category 1', entries=[Component(class='javax.swing.JButton')])",
        category.toString());
  }

  /**
   * Test for "open" property.
   */
  public void test_open() throws Exception {
    CategoryInfo category = new CategoryInfo();
    assertFalse(category.isOpen());
    category.setOpen(true);
    assertTrue(category.isOpen());
  }

  /**
   * Test for "entries" operations.
   */
  public void test_entries() throws Exception {
    CategoryInfo category = new CategoryInfo();
    assertTrue(category.getEntries().isEmpty());
    // prepare entries
    EntryInfo entry_1 = new ComponentEntryInfo();
    entry_1.setId("1");
    EntryInfo entry_2 = new ComponentEntryInfo();
    entry_2.setId("2");
    // add entries
    {
      category.addEntry(entry_1);
      category.addEntry(entry_2);
      assertEquals(2, category.getEntries().size());
      assertSame(entry_1, category.getEntries().get(0));
      assertSame(entry_2, category.getEntries().get(1));
      // clean up
      category.getEntries().clear();
    }
    // add with index
    {
      category.addEntry(entry_1);
      category.addEntry(0, entry_2);
      assertEquals(2, category.getEntries().size());
      assertSame(entry_2, category.getEntries().get(0));
      assertSame(entry_1, category.getEntries().get(1));
    }
    // remove one by one
    {
      category.removeEntry(null);
      assertEquals(2, category.getEntries().size());
      //
      category.removeEntry(entry_1);
      assertEquals(1, category.getEntries().size());
      assertSame(entry_2, category.getEntries().get(0));
      //
      category.removeEntry(entry_2);
      assertEquals(0, category.getEntries().size());
    }
  }

  public void test_parse() throws Exception {
    addPaletteExtension(new String[]{"<category id='id_1' name='name 1' description='description 1'/>"});
    PaletteInfo palette = loadPalette();
    // check category
    CategoryInfo category = palette.getCategory("id_1");
    assertEquals("id_1", category.getId());
    assertEquals("name 1", category.getName());
    assertEquals("description 1", category.getDescription());
    assertTrue(category.isVisible());
    assertTrue(category.isOpen());
    assertFalse(category.isOptional());
  }

  public void test_parse_notDefault() throws Exception {
    addPaletteExtension(new String[]{"<category id='id_1'"
        + " name='name 1'"
        + " visible='false'"
        + " open='false'"
        + " optional='true'/>"});
    PaletteInfo palette = loadPalette();
    // check category
    CategoryInfo category = palette.getCategory("id_1");
    assertEquals("id_1", category.getId());
    assertFalse(category.isVisible());
    assertFalse(category.isOpen());
    assertTrue(category.isOptional());
  }
}
