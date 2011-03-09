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
package org.eclipse.wb.tests.designer.core.model.description;

import org.eclipse.wb.internal.core.model.description.LayoutDescription;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.description.helpers.LayoutDescriptionHelper;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import java.util.List;

/**
 * Tests for {@link LayoutDescription} and {@link LayoutDescriptionHelper}.
 * 
 * @author scheglov_ke
 */
public class LayoutDescriptionTest extends SwingModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Exit zone :-) XXX
  //
  ////////////////////////////////////////////////////////////////////////////
  public void _test_exit() throws Exception {
    System.exit(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_get_Swing() throws Exception {
    ToolkitDescription toolkit = org.eclipse.wb.internal.swing.ToolkitProvider.DESCRIPTION;
    List<LayoutDescription> layouts = LayoutDescriptionHelper.get(toolkit);
    // check accessors for one layout
    {
      LayoutDescription layout = getLayoutById(layouts, "flowLayout");
      assertSame(toolkit, layout.getToolkit());
      assertEquals("flowLayout", layout.getId());
      assertEquals("FlowLayout", layout.getName());
      assertEquals("java.awt.FlowLayout", layout.getLayoutClassName());
      assertEquals("new FlowLayout()", layout.getSourceSmart());
    }
    // check Swing layouts
    assertNotNull(getLayoutById(layouts, "flowLayout"));
    assertNotNull(getLayoutById(layouts, "borderLayout"));
    assertNotNull(getLayoutById(layouts, "gridLayout"));
    assertNotNull(getLayoutById(layouts, "cardLayout"));
    assertNotNull(getLayoutById(layouts, "gridBagLayout"));
    // try SWT layout
    assertNull(getLayoutById(layouts, "rowLayout"));
    // use get(toolkit,id) from helper
    assertNotNull(LayoutDescriptionHelper.get(toolkit, "flowLayout"));
    assertNull(LayoutDescriptionHelper.get(toolkit, "rowLayout"));
  }

  /**
   * @return the {@link LayoutDescription} with given id, or <code>null</code> if not found.
   */
  private static LayoutDescription getLayoutById(List<LayoutDescription> layouts, String id) {
    for (LayoutDescription layout : layouts) {
      if (layout.getId().equals(id)) {
        return layout;
      }
    }
    return null;
  }
}
