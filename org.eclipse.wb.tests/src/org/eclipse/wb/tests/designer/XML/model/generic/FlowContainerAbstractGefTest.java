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

import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;

/**
 * Tests for "flow container" support for container itself or for its "layout manager".
 *
 * @author scheglov_ke
 */
public abstract class FlowContainerAbstractGefTest extends XwtGefTest {
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
  public void test_canvas_CREATE_empty() throws Exception {
    prepareFlowPanel();
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <t:FlowPanel wbp:name='panel'/>",
        "</Shell>");
    XmlObjectInfo panel = getObjectByName("panel");
    // begin creating Button
    XmlObjectInfo newButton = loadButton();
    // move on "panel": feedback appears, command not null
    canvas.moveTo(panel, 100, 100);
    canvas.assertEmptyFlowContainerFeedback(panel, true);
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
        "  <t:FlowPanel wbp:name='panel'>",
        "    <Button/>",
        "  </t:FlowPanel>",
        "</Shell>");
    canvas.assertPrimarySelected(newButton);
  }

  public void test_canvas_CREATE() throws Exception {
    prepareFlowPanel();
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <t:FlowPanel wbp:name='panel'>",
        "    <Button wbp:name='existingButton' text='existing'/>",
        "  </t:FlowPanel>",
        "</Shell>");
    XmlObjectInfo existingButton = getObjectByName("existingButton");
    // begin creating Button
    XmlObjectInfo newButton = loadButton();
    // move on "panel": feedback appears, command not null
    canvas.moveTo(existingButton, 0, 0);
    canvas.assertFeedbacks(canvas.getLinePredicate(existingButton, IPositionConstants.LEFT));
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
        "  <t:FlowPanel wbp:name='panel'>",
        "    <Button/>",
        "    <Button wbp:name='existingButton' text='existing'/>",
        "  </t:FlowPanel>",
        "</Shell>");
    canvas.assertPrimarySelected(newButton);
  }

  public void test_canvas_PASTE() throws Exception {
    prepareFlowPanel();
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <t:FlowPanel wbp:name='panel'>",
        "    <Button wbp:name='existingButton' text='existing'/>",
        "  </t:FlowPanel>",
        "  <Button wbp:name='rootButton' text='root'/>",
        "</Shell>");
    XmlObjectInfo existingButton = getObjectByName("existingButton");
    XmlObjectInfo rootButton = getObjectByName("rootButton");
    // copy "rootButton"
    doCopyPaste(rootButton);
    // move on "panel": feedback appears, command not null
    canvas.moveTo(existingButton, 0, 0);
    canvas.assertFeedbacks(canvas.getLinePredicate(existingButton, IPositionConstants.LEFT));
    canvas.assertCommandNotNull();
    // click, so finish creation
    canvas.click();
    canvas.assertNoFeedbacks();
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <t:FlowPanel wbp:name='panel'>",
        "    <Button text='root'/>",
        "    <Button wbp:name='existingButton' text='existing'/>",
        "  </t:FlowPanel>",
        "  <Button wbp:name='rootButton' text='root'/>",
        "</Shell>");
    // EditPart for "newButton" exists and selected
    {
      XmlObjectInfo panel = getObjectByName("panel");
      XmlObjectInfo newButton = GenericsUtils.getPrevOrNull(panel.getChildrenXML(), existingButton);
      canvas.assertPrimarySelected(newButton);
    }
  }

  public void test_canvas_MOVE() throws Exception {
    prepareFlowPanel();
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <t:FlowPanel wbp:name='panel'>",
        "    <Button wbp:name='buttonA' text='A'/>",
        "    <Button wbp:name='buttonB' text='B'/>",
        "  </t:FlowPanel>",
        "</Shell>");
    XmlObjectInfo buttonA = getObjectByName("buttonA");
    XmlObjectInfo buttonB = getObjectByName("buttonB");
    // drag "buttonB"
    canvas.beginDrag(buttonB, 10, 10).dragTo(buttonA);
    canvas.assertFeedbacks(canvas.getLinePredicate(buttonA, IPositionConstants.LEFT));
    canvas.assertCommandNotNull();
    // done drag, so finish MOVE
    canvas.endDrag();
    canvas.assertNoFeedbacks();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <t:FlowPanel wbp:name='panel'>",
        "    <Button wbp:name='buttonB' text='B'/>",
        "    <Button wbp:name='buttonA' text='A'/>",
        "  </t:FlowPanel>",
        "</Shell>");
  }

  public void test_canvas_ADD() throws Exception {
    prepareFlowPanel();
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <t:FlowPanel wbp:name='panel'>",
        "    <Button wbp:name='existingButton' text='existing'/>",
        "  </t:FlowPanel>",
        "  <Button wbp:name='rootButton' text='root'/>",
        "</Shell>");
    XmlObjectInfo existingButton = getObjectByName("existingButton");
    XmlObjectInfo rootButton = getObjectByName("rootButton");
    // drag "rootButton"
    canvas.beginDrag(rootButton).dragTo(existingButton);
    canvas.assertFeedbacks(canvas.getLinePredicate(existingButton, IPositionConstants.LEFT));
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
        "  <t:FlowPanel wbp:name='panel'>",
        "    <Button wbp:name='rootButton' text='root'/>",
        "    <Button wbp:name='existingButton' text='existing'/>",
        "  </t:FlowPanel>",
        "</Shell>");
    canvas.assertPrimarySelected(rootButton);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tree
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_tree_CREATE() throws Exception {
    prepareFlowPanel();
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <t:FlowPanel wbp:name='panel'>",
        "    <Button wbp:name='existingButton' text='existing'/>",
        "  </t:FlowPanel>",
        "</Shell>");
    XmlObjectInfo existingButton = getObjectByName("existingButton");
    // begin creating Button
    XmlObjectInfo newButton = loadButton();
    // move before "existingButton": feedback appears, command not null
    tree.moveBefore(existingButton);
    tree.assertFeedback_before(existingButton);
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
        "  <t:FlowPanel wbp:name='panel'>",
        "    <Button/>",
        "    <Button wbp:name='existingButton' text='existing'/>",
        "  </t:FlowPanel>",
        "</Shell>");
    tree.assertPrimarySelected(newButton);
  }

  public void test_tree_PASTE() throws Exception {
    prepareFlowPanel();
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <t:FlowPanel wbp:name='panel'>",
        "    <Button wbp:name='existingButton' text='existing'/>",
        "  </t:FlowPanel>",
        "  <Button wbp:name='rootButton' text='root'/>",
        "</Shell>");
    XmlObjectInfo panel = getObjectByName("panel");
    XmlObjectInfo existingButton = getObjectByName("existingButton");
    XmlObjectInfo rootButton = getObjectByName("rootButton");
    // copy "rootButton"
    doCopyPaste(rootButton);
    // move before "existingButton": feedback appears, command not null
    tree.moveOn(panel);
    tree.moveBefore(existingButton);
    tree.assertFeedback_before(existingButton);
    tree.assertCommandNotNull();
    // click, so finish creation
    tree.click();
    tree.assertFeedback_empty();
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <t:FlowPanel wbp:name='panel'>",
        "    <Button text='root'/>",
        "    <Button wbp:name='existingButton' text='existing'/>",
        "  </t:FlowPanel>",
        "  <Button wbp:name='rootButton' text='root'/>",
        "</Shell>");
    // EditPart for "newButton" exists and selected
    {
      XmlObjectInfo newButton = GenericsUtils.getPrevOrNull(panel.getChildrenXML(), existingButton);
      tree.assertPrimarySelected(newButton);
    }
  }

  public void test_tree_MOVE() throws Exception {
    prepareFlowPanel();
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <t:FlowPanel wbp:name='panel'>",
        "    <Button wbp:name='buttonA' text='A'/>",
        "    <Button wbp:name='buttonB' text='B'/>",
        "  </t:FlowPanel>",
        "</Shell>");
    XmlObjectInfo buttonA = getObjectByName("buttonA");
    XmlObjectInfo buttonB = getObjectByName("buttonB");
    // select "buttonB", so ensure that it has EditPart
    canvas.select(buttonB);
    // drag "buttonB"
    tree.startDrag(buttonB);
    tree.dragBefore(buttonA);
    tree.assertFeedback_before(buttonA);
    tree.assertCommandNotNull();
    // done drag, so finish MOVE
    tree.endDrag();
    tree.assertFeedback_empty();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <t:FlowPanel wbp:name='panel'>",
        "    <Button wbp:name='buttonB' text='B'/>",
        "    <Button wbp:name='buttonA' text='A'/>",
        "  </t:FlowPanel>",
        "</Shell>");
  }

  public void test_tree_ADD() throws Exception {
    prepareFlowPanel();
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <t:FlowPanel wbp:name='panel'>",
        "    <Button wbp:name='existingButton' text='existing'/>",
        "  </t:FlowPanel>",
        "  <Button wbp:name='rootButton' text='root'/>",
        "</Shell>");
    XmlObjectInfo existingButton = getObjectByName("existingButton");
    XmlObjectInfo rootButton = getObjectByName("rootButton");
    // drag "rootButton"
    tree.startDrag(rootButton);
    tree.dragBefore(existingButton);
    tree.endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <t:FlowPanel wbp:name='panel'>",
        "    <Button wbp:name='rootButton' text='root'/>",
        "    <Button wbp:name='existingButton' text='existing'/>",
        "  </t:FlowPanel>",
        "</Shell>");
    tree.assertPrimarySelected(rootButton);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates <code>FlowPanel</code> component with <code>MyLayout</code> layout manager. One of them
   * should be configured to have "flow container" description.
   */
  protected abstract void prepareFlowPanel() throws Exception;
}
