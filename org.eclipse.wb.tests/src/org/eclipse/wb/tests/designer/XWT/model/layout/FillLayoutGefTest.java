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
package org.eclipse.wb.tests.designer.XWT.model.layout;

import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.xwt.model.layout.FillLayoutInfo;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;

/**
 * Test for {@link FillLayoutInfo} in GEF.
 *
 * @author scheglov_ke
 */
public class FillLayoutGefTest extends XwtGefTest {
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
  public void test_dropNew_FillLayout() throws Exception {
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

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE_noNext() throws Exception {
    CompositeInfo shell =
        openEditor(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <FillLayout/>",
            "  </Shell.layout>",
            "</Shell>");
    //
    loadCreationTool("org.eclipse.swt.widgets.Button");
    canvas.moveTo(shell, 100, 100).click();
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <Button text='New Button'/>",
        "</Shell>");
  }

  public void test_CREATE_withNext() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='reference' text='Reference'/>",
        "</Shell>");
    XmlObjectInfo reference = getObjectByName("reference");
    //
    loadCreationTool("org.eclipse.swt.widgets.Button");
    canvas.moveTo(reference, 0, 0.5).click();
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <Button text='New Button'/>",
        "  <Button wbp:name='reference' text='Reference'/>",
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PASTE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_PASTE() throws Exception {
    CompositeInfo shell =
        openEditor(
            "<Shell>",
            "  <Shell.layout>",
            "    <FillLayout/>",
            "  </Shell.layout>",
            "  <Button wbp:name='button' text='My button'/>",
            "</Shell>");
    ControlInfo button = getObjectByName("button");
    //
    doCopyPaste(button);
    canvas.moveTo(shell, 2, 100).click();
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <Button text='My button'/>",
        "  <Button wbp:name='button' text='My button'/>",
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_MOVE_reorder() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='reference' text='Reference'/>",
        "  <Button wbp:name='button' text='Button'/>",
        "</Shell>");
    XmlObjectInfo button = getObjectByName("button");
    XmlObjectInfo reference = getObjectByName("reference");
    //
    canvas.beginMove(button);
    canvas.dragTo(reference, 0, 0.5).endDrag();
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button' text='Button'/>",
        "  <Button wbp:name='reference' text='Reference'/>",
        "</Shell>");
  }

  public void test_MOVE_reparent() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <Composite wbp:name='target'>",
        "    <Composite.layout>",
        "      <FillLayout/>",
        "    </Composite.layout>",
        "  </Composite>",
        "  <Button wbp:name='button' text='Button'/>",
        "</Shell>");
    ControlInfo button = getObjectByName("button");
    CompositeInfo target = getObjectByName("target");
    //
    canvas.beginMove(button);
    canvas.dragTo(target, 50, 50).endDrag();
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <Composite wbp:name='target'>",
        "    <Composite.layout>",
        "      <FillLayout/>",
        "    </Composite.layout>",
        "    <Button wbp:name='button' text='Button'/>",
        "  </Composite>",
        "</Shell>");
  }
}
