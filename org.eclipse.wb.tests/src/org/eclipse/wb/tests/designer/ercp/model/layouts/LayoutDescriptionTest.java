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
package org.eclipse.wb.tests.designer.ercp.model.layouts;

import org.eclipse.wb.internal.core.model.description.LayoutDescription;
import org.eclipse.wb.internal.core.model.description.helpers.LayoutDescriptionHelper;
import org.eclipse.wb.tests.designer.ercp.ErcpModelTest;

import java.util.List;

/**
 * Tests for {@link LayoutDescription} and {@link LayoutDescriptionHelper}.
 * 
 * @author scheglov_ke
 */
public class LayoutDescriptionTest extends ErcpModelTest {
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
  public void test_get_eRCP() throws Exception {
    List<LayoutDescription> layouts =
        LayoutDescriptionHelper.get(org.eclipse.wb.internal.ercp.ToolkitProvider.DESCRIPTION);
    // check eRCP layouts
    assertNotNull(getLayoutById(layouts, "rowLayout"));
    assertNotNull(getLayoutById(layouts, "gridLayout"));
    assertNotNull(getLayoutById(layouts, "fillLayout"));
    assertNotNull(getLayoutById(layouts, "formLayout"));
    // try Swing layout
    assertNull(getLayoutById(layouts, "borderLayout"));
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
