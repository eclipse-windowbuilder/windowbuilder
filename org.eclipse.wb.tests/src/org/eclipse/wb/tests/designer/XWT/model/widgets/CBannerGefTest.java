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

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.xwt.model.widgets.CBannerInfo;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;

/**
 * Test for {@link CBannerInfo} in GEF.
 * 
 * @author scheglov_ke
 */
public class CBannerGefTest extends XwtGefTest {
  private CBannerInfo banner;

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
  // Source
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getTestSource_namespaces() {
    return super.getTestSource_namespaces() + " xmlns:c='clr-namespace:org.eclipse.swt.custom'";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Canvas, CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_canvas_CREATE_left() throws Exception {
    prepare_canvas_CREATE();
    // use canvas
    canvas.target(banner).in(0.1, 0.1).move();
    canvas.click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<CBanner>",
        "  <CBanner.left>",
        "    <Button/>",
        "  </CBanner.left>",
        "</CBanner>");
  }

  public void test_canvas_CREATE_right() throws Exception {
    prepare_canvas_CREATE();
    // use canvas
    canvas.target(banner).in(-0.1, 0.1).move();
    canvas.click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<CBanner>",
        "  <CBanner.right>",
        "    <Button/>",
        "  </CBanner.right>",
        "</CBanner>");
  }

  public void test_canvas_CREATE_bottom() throws Exception {
    prepare_canvas_CREATE();
    // use canvas
    canvas.target(banner).in(0.5, -0.1).move();
    canvas.click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<CBanner>",
        "  <CBanner.bottom>",
        "    <Button/>",
        "  </CBanner.bottom>",
        "</CBanner>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  private CBannerInfo prepare_canvas_CREATE() throws Exception {
    banner =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<CBanner/>");
    // create Button
    loadButton();
    canvas.create(0, 0);
    // use this CBanner_Info
    return banner;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tree
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_tree_CREATE_left() throws Exception {
    prepare_canvas_CREATE();
    // use tree
    EditPart position = tree.getEditPart(banner).getChildren().get(0);
    tree.moveBefore(position);
    tree.moveOn(position).click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<CBanner>",
        "  <CBanner.left>",
        "    <Button/>",
        "  </CBanner.left>",
        "</CBanner>");
  }
}
