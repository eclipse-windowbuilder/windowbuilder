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

import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.editor.palette.model.CategoryInfo;
import org.eclipse.wb.internal.core.xml.editor.palette.model.EntryInfo;
import org.eclipse.wb.internal.core.xml.editor.palette.model.IPaletteSite;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.tests.gef.EmptyEditPartViewer;

import org.eclipse.swt.graphics.Image;

/**
 * Tests for abstract {@link EntryInfo}.
 * 
 * @author scheglov_ke
 */
public class EntryInfoTest extends AbstractPaletteTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_0() throws Exception {
    XmlObjectInfo panel = parseEmptyPanel();
    IEditPartViewer editPartViewer = new EmptyEditPartViewer();
    // prepare ToolEntryInfo
    EntryInfo toolEntry = new EntryInfo() {
      @Override
      public Image getIcon() {
        return null;
      }

      @Override
      public boolean activate(boolean reload) {
        return false;
      }
    };
    assertTrue(toolEntry.isEnabled());
    // initialize
    assertTrue(toolEntry.initialize(editPartViewer, panel));
    // site
    {
      IPaletteSite.Empty site = new IPaletteSite.Empty();
      IPaletteSite.Helper.setSite(panel, site);
      assertSame(site, ReflectionUtils.invokeMethod(toolEntry, "getSite()"));
    }
    // can not activate
    assertFalse(toolEntry.activate(false));
    // category
    {
      CategoryInfo category = new CategoryInfo();
      toolEntry.setCategory(category);
      assertSame(category, toolEntry.getCategory());
    }
  }
}
