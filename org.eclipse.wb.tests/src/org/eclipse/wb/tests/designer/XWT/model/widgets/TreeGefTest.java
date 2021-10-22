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

import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.xwt.model.widgets.TreeInfo;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;

/**
 * Test for {@link TreeInfo} in GEF.
 *
 * @author scheglov_ke
 */
public class TreeGefTest extends XwtGefTest {
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
  // Canvas
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_canvas_RESIZE_column() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Tree headerVisible='true'>",
        "    <TreeColumn wbp:name='column' width='100'/>",
        "  </Tree>",
        "</Shell>");
    XmlObjectInfo column = getObjectByName("column");
    //
    canvas.target(column).outX(1).inY(0.5);
    canvas.beginDrag().dragOn(50, 0).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Tree headerVisible='true'>",
        "    <TreeColumn wbp:name='column' width='150'/>",
        "  </Tree>",
        "</Shell>");
  }
}
