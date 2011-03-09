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
package org.eclipse.wb.tests.designer.XWT.model.widgets;

import org.eclipse.wb.internal.xwt.model.widgets.CTabFolderInfo;
import org.eclipse.wb.internal.xwt.model.widgets.CTabItemInfo;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;

import org.eclipse.swt.custom.CTabItem;

/**
 * Test for {@link CTabFolderInfo} in GEF.
 * 
 * @author scheglov_ke
 */
public class CTabFolderGefTest extends XwtGefTest {
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
  /**
   * We should be able to select {@link CTabItem} using double click.
   */
  public void test_canvas_selectItem() throws Exception {
    CTabFolderInfo folder =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<CTabFolder>",
            "  <CTabItem wbp:name='item_1'/>",
            "  <CTabItem wbp:name='item_2'/>",
            "</CTabFolder>");
    refresh();
    CTabItemInfo item_1 = getObjectByName("item_1");
    CTabItemInfo item_2 = getObjectByName("item_2");
    // initially "item_1" is selected
    assertSame(item_1, folder.getSelectedItem());
    // double click "item_2"
    canvas.doubleClick(item_2);
    assertSame(item_2, folder.getSelectedItem());
  }
}
