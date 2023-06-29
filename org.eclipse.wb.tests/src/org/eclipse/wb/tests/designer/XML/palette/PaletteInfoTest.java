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

import org.eclipse.wb.internal.core.xml.editor.palette.model.CategoryInfo;
import org.eclipse.wb.internal.core.xml.editor.palette.model.ComponentEntryInfo;
import org.eclipse.wb.internal.core.xml.editor.palette.model.EntryInfo;
import org.eclipse.wb.internal.core.xml.editor.palette.model.PaletteInfo;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

/**
 * Tests for {@link PaletteInfo}.
 *
 * @author scheglov_ke
 */
public class PaletteInfoTest extends DesignerTestCase {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_toString() throws Exception {
		PaletteInfo palette = new PaletteInfo();
		// toString() without categories
		assertEquals("", palette.toString());
		// toString() with single category
		palette.addCategory(new CategoryInfo("category_1"));
		assertEquals("Category(id='category_1', name='(unknown)', entries=[])", palette.toString());
	}

	public void test_categories() throws Exception {
		PaletteInfo palette = new PaletteInfo();
		// prepare categories
		CategoryInfo category_1 = new CategoryInfo("category_1");
		CategoryInfo category_2 = new CategoryInfo("category_2");
		// add categories
		assertEquals(0, palette.getCategories().size());
		palette.addCategory(category_1);
		palette.addCategory(category_2);
		assertEquals(2, palette.getCategories().size());
		assertSame(category_1, palette.getCategories().get(0));
		assertSame(category_2, palette.getCategories().get(1));
		// find
		assertSame(category_1, palette.getCategory("category_1"));
		assertSame(category_2, palette.getCategory("category_2"));
		assertNull(palette.getCategory("no-such-category"));
	}

	/**
	 * Test for {@link PaletteInfo#moveCategory(String, String)}.
	 */
	public void test_moveCategory() throws Exception {
		PaletteInfo palette = new PaletteInfo();
		// prepare categories
		CategoryInfo category_1 = new CategoryInfo("category_1");
		CategoryInfo category_2 = new CategoryInfo("category_2");
		CategoryInfo category_3 = new CategoryInfo("category_3");
		// add categories
		palette.addCategory(category_1);
		palette.addCategory(category_2);
		palette.addCategory(category_3);
		// case 1: no category no move - ignore
		{
			palette.moveCategory("noSuchCategory", null);
			assertSame(category_1, palette.getCategories().get(0));
			assertSame(category_2, palette.getCategories().get(1));
			assertSame(category_3, palette.getCategories().get(2));
		}
		// case 2: move before itself - ignore
		{
			palette.moveCategory("category_1", "category_1");
			assertSame(category_1, palette.getCategories().get(0));
			assertSame(category_2, palette.getCategories().get(1));
			assertSame(category_3, palette.getCategories().get(2));
		}
		// case 3: no next category, so move to end
		{
			palette.moveCategory("category_1", "noSuchCategory");
			assertSame(category_2, palette.getCategories().get(0));
			assertSame(category_3, palette.getCategories().get(1));
			assertSame(category_1, palette.getCategories().get(2));
		}
		// case 4: move before existing category
		{
			palette.moveCategory("category_1", "category_2");
			assertSame(category_1, palette.getCategories().get(0));
			assertSame(category_2, palette.getCategories().get(1));
			assertSame(category_3, palette.getCategories().get(2));
		}
	}

	public void test_getEntry() throws Exception {
		PaletteInfo palette = new PaletteInfo();
		CategoryInfo category = new CategoryInfo("category_1");
		palette.addCategory(category);
		// add entries
		EntryInfo entry_1 = new ComponentEntryInfo();
		entry_1.setId("1");
		EntryInfo entry_2 = new ComponentEntryInfo();
		entry_2.setId("2");
		category.addEntry(entry_1);
		category.addEntry(entry_2);
		// find entries
		assertSame(entry_1, palette.getEntry("1"));
		assertSame(entry_2, palette.getEntry("2"));
		assertNull(palette.getEntry("no-such-entry"));
	}
}
