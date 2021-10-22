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
package org.eclipse.wb.tests.designer.XML.model.generic;

import org.eclipse.wb.internal.core.model.generic.SimpleContainer;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;

/**
 * Tests for "simple container" support, such as {@link SimpleContainer} interface.
 *
 * @author scheglov_ke
 */
public abstract class SimpleContainerAbstractGefTest extends XwtGefTest {
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
  public void test_canvas_CREATE_filled() throws Exception {
    prepareSimplePanel();
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <t:SimplePanel wbp:name='panel'>",
        "    <Button/>",
        "  </t:SimplePanel>",
        "</Shell>");
    XmlObjectInfo panel = getObjectByName("panel");
    // begin creating Button
    loadButton();
    // move on "panel": feedback appears, no command
    canvas.moveTo(panel, 0, 0);
    canvas.assertFeedbacks(canvas.getTargetPredicate(panel));
    canvas.assertCommandNull();
  }

  public void test_canvas_CREATE_empty() throws Exception {
    prepareSimplePanel();
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <t:SimplePanel wbp:name='panel'/>",
        "</Shell>");
    XmlObjectInfo panel = getObjectByName("panel");
    // begin creating Button
    XmlObjectInfo newButton = loadButton();
    // move on "panel": feedback appears, command not null
    canvas.moveTo(panel, 0, 0);
    canvas.assertFeedbacks(canvas.getTargetPredicate(panel));
    canvas.assertCommandNotNull();
    // click, so finish creation
    canvas.click();
    canvas.assertNoFeedbacks();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <t:SimplePanel wbp:name='panel'>",
        "    <Button/>",
        "  </t:SimplePanel>",
        "</Shell>");
    canvas.assertPrimarySelected(newButton);
  }

  public void test_canvas_PASTE() throws Exception {
    prepareSimplePanel();
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <t:SimplePanel wbp:name='panel'/>",
        "  <Button wbp:name='rootButton'/>",
        "</Shell>");
    XmlObjectInfo panel = getObjectByName("panel");
    XmlObjectInfo rootButton = getObjectByName("rootButton");
    // copy/paste "rootButton"
    doCopyPaste(rootButton);
    // move on "panel": feedback appears, command not null
    canvas.moveTo(panel, 0, 0);
    canvas.assertFeedbacks(canvas.getTargetPredicate(panel));
    canvas.assertCommandNotNull();
    // click, so finish creation
    canvas.click();
    canvas.assertNoFeedbacks();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <t:SimplePanel wbp:name='panel'>",
        "    <Button/>",
        "  </t:SimplePanel>",
        "  <Button wbp:name='rootButton'/>",
        "</Shell>");
    // EditPart for "newButton" exists and selected
    {
      XmlObjectInfo newButton = GenericsUtils.getLast(panel.getChildrenXML());
      canvas.assertPrimarySelected(newButton);
    }
  }

  public void test_canvas_ADD_1() throws Exception {
    prepareSimplePanel();
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <t:SimplePanel wbp:name='panel'/>",
        "  <Button wbp:name='rootButton'/>",
        "</Shell>");
    XmlObjectInfo panel = getObjectByName("panel");
    XmlObjectInfo rootButton = getObjectByName("rootButton");
    // drag "rootButton"
    canvas.beginDrag(rootButton).dragTo(panel);
    canvas.assertFeedbacks(canvas.getTargetPredicate(panel));
    canvas.assertCommandNotNull();
    // done drag, so finish ADD
    canvas.endDrag();
    canvas.assertNoFeedbacks();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <t:SimplePanel wbp:name='panel'>",
        "    <Button wbp:name='rootButton'/>",
        "  </t:SimplePanel>",
        "</Shell>");
    canvas.assertPrimarySelected(rootButton);
  }

  public void test_canvas_ADD_2() throws Exception {
    prepareSimplePanel();
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <t:SimplePanel wbp:name='panel'/>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_2'/>",
        "</Shell>");
    XmlObjectInfo panel = getObjectByName("panel");
    XmlObjectInfo button_1 = getObjectByName("button_1");
    XmlObjectInfo button_2 = getObjectByName("button_2");
    // drag "button_1" and "button_2"
    canvas.select(button_2, button_1);
    canvas.beginDrag(button_1, 100, 5).dragTo(panel, 10, 10);
    canvas.assertFeedbacks(canvas.getTargetPredicate(panel));
    canvas.assertCommandNull();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tree
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_tree_CREATE_filled_1() throws Exception {
    prepareSimplePanel();
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <t:SimplePanel wbp:name='panel'>",
        "    <Button wbp:name='existingButton'/>",
        "  </t:SimplePanel>",
        "</Shell>");
    XmlObjectInfo existingButton = getObjectByName("existingButton");
    // begin creating Button
    loadButton();
    // move before "existingButton": feedback appears, command null
    tree.moveBefore(existingButton);
    tree.assertFeedback_before(existingButton);
    tree.assertCommandNull();
  }

  public void test_tree_CREATE_filled_2() throws Exception {
    prepareSimplePanel();
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <t:SimplePanel wbp:name='panel'>",
        "    <Button wbp:name='existingButton'/>",
        "  </t:SimplePanel>",
        "</Shell>");
    XmlObjectInfo panel = getObjectByName("panel");
    // begin creating Button
    loadButton();
    // move on "panel": feedback appears, command null
    tree.moveOn(panel);
    tree.assertFeedback_on(panel);
    tree.assertCommandNull();
  }

  public void test_tree_CREATE_empty() throws Exception {
    prepareSimplePanel();
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <t:SimplePanel wbp:name='panel'/>",
        "</Shell>");
    XmlObjectInfo panel = getObjectByName("panel");
    // begin creating Button
    XmlObjectInfo newButton = loadButton();
    // move on "panel": feedback appears, command not null
    tree.moveOn(panel);
    tree.assertFeedback_on(panel);
    tree.assertCommandNotNull();
    // click, so finish creation
    tree.click();
    tree.assertFeedback_empty();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <t:SimplePanel wbp:name='panel'>",
        "    <Button/>",
        "  </t:SimplePanel>",
        "</Shell>");
    tree.assertPrimarySelected(newButton);
  }

  public void test_tree_PASTE() throws Exception {
    prepareSimplePanel();
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <t:SimplePanel wbp:name='panel'/>",
        "  <Button wbp:name='rootButton'/>",
        "</Shell>");
    XmlObjectInfo panel = getObjectByName("panel");
    XmlObjectInfo rootButton = getObjectByName("rootButton");
    // copy/paste "rootButton"
    doCopyPaste(rootButton);
    // move on "panel": feedback appears, command not null
    tree.moveOn(panel);
    tree.assertFeedback_on(panel);
    tree.assertCommandNotNull();
    // click, so finish creation
    tree.click();
    tree.assertFeedback_empty();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <t:SimplePanel wbp:name='panel'>",
        "    <Button/>",
        "  </t:SimplePanel>",
        "  <Button wbp:name='rootButton'/>",
        "</Shell>");
    // EditPart for "newButton" exists and selected
    {
      XmlObjectInfo newButton = GenericsUtils.getLast(panel.getChildrenXML());
      tree.assertPrimarySelected(newButton);
    }
  }

  public void test_tree_MOVE() throws Exception {
    prepareSimplePanel();
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <t:SimplePanel wbp:name='panel'>",
        "    <Button wbp:name='button'/>",
        "  </t:SimplePanel>",
        "</Shell>");
    XmlObjectInfo panel = getObjectByName("panel");
    XmlObjectInfo button = getObjectByName("button");
    // drag "button"
    tree.startDrag(button);
    tree.dragOn(panel);
    tree.assertFeedback_on(panel);
    tree.assertCommandNull();
  }

  public void test_tree_ADD() throws Exception {
    prepareSimplePanel();
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <t:SimplePanel wbp:name='panel'/>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    XmlObjectInfo panel = getObjectByName("panel");
    XmlObjectInfo button = getObjectByName("button");
    // drag "button"
    tree.startDrag(button);
    tree.dragOn(panel);
    tree.assertFeedback_on(panel);
    tree.assertCommandNotNull();
    // done drag, so finish ADD
    tree.endDrag();
    tree.assertFeedback_empty();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <t:SimplePanel wbp:name='panel'>",
        "    <Button wbp:name='button'/>",
        "  </t:SimplePanel>",
        "</Shell>");
    tree.assertPrimarySelected(button);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  protected abstract void prepareSimplePanel() throws Exception;
}
