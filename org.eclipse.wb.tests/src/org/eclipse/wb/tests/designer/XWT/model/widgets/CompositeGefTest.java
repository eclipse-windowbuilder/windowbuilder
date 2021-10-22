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

import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;

/**
 * Test for {@link CompositeInfo} in GEF.
 *
 * @author scheglov_ke
 */
public class CompositeGefTest extends XwtGefTest {
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
  // Drop Layout
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_canvas_dropFillLayout() throws Exception {
    CompositeInfo shell =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Shell/>");
    loadCreationTool("org.eclipse.swt.layout.FillLayout");
    //
    canvas.moveTo(shell, 100, 100);
    canvas.assertFeedbacks(canvas.getTargetPredicate(shell));
    canvas.click();
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "</Shell>");
  }

  public void test_tree_dropFillLayout() throws Exception {
    CompositeInfo shell =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Shell/>");
    loadCreationTool("org.eclipse.swt.layout.FillLayout");
    //
    tree.moveOn(shell);
    tree.assertFeedback_on(shell);
    tree.click();
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "</Shell>");
  }
}
