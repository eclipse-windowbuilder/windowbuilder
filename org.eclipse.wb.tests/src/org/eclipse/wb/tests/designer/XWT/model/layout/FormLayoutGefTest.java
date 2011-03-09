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

import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.internal.xwt.model.layout.form.FormLayoutInfo;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;

/**
 * Tests for {@link FormLayoutInfo} in GEF.
 * 
 * @author scheglov_ke
 */
public class FormLayoutGefTest extends XwtGefTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    prepareBox();
  }

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
  public void test_set() throws Exception {
    CompositeInfo composite =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Composite/>");
    // use canvas
    loadCreationTool("org.eclipse.swt.layout.FormLayout");
    canvas.moveTo(composite, 100, 100).click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Composite>",
        "  <Composite.layout>",
        "    <FormLayout/>",
        "  </Composite.layout>",
        "</Composite>");
  }

  public void test_canvas_CREATE() throws Exception {
    CompositeInfo composite =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Composite>",
            "  <Composite.layout>",
            "    <FormLayout/>",
            "  </Composite.layout>",
            "</Composite>");
    // create Box
    loadBox();
    // use canvas
    canvas.sideMode().create(100, 50);
    canvas.target(composite).in(30, 40).move();
    canvas.click();
    assertXML(
        "<Composite>",
        "  <Composite.layout>",
        "    <FormLayout/>",
        "  </Composite.layout>",
        "  <t:Box>",
        "    <t:Box.layoutData>",
        "      <FormData>",
        "        <FormData.top>",
        "          <FormAttachment numerator='0' offset='40'/>",
        "        </FormData.top>",
        "        <FormData.left>",
        "          <FormAttachment numerator='0' offset='30'/>",
        "        </FormData.left>",
        "      </FormData>",
        "    </t:Box.layoutData>",
        "  </t:Box>",
        "</Composite>");
  }

  public void test_canvas_PASTE() throws Exception {
    CompositeInfo composite =
        openEditor(
            "<Composite>",
            "  <Composite.layout>",
            "    <FormLayout/>",
            "  </Composite.layout>",
            "  <t:Box wbp:name='box'>",
            "    <t:Box.layoutData>",
            "      <FormData>",
            "        <FormData.top>",
            "          <FormAttachment numerator='0' offset='40'/>",
            "        </FormData.top>",
            "        <FormData.left>",
            "          <FormAttachment numerator='0' offset='30'/>",
            "        </FormData.left>",
            "      </FormData>",
            "    </t:Box.layoutData>",
            "  </t:Box>",
            "</Composite>");
    // copy/paste "box"
    {
      ControlInfo box = getObjectByName("box");
      doCopyPaste(box);
    }
    // move
    canvas.sideMode().create(100, 50);
    canvas.target(composite).inX(200).inY(150).move();
    canvas.click();
    assertXML(
        "<Composite>",
        "  <Composite.layout>",
        "    <FormLayout/>",
        "  </Composite.layout>",
        "  <t:Box wbp:name='box'>",
        "    <t:Box.layoutData>",
        "      <FormData>",
        "        <FormData.top>",
        "          <FormAttachment numerator='0' offset='40'/>",
        "        </FormData.top>",
        "        <FormData.left>",
        "          <FormAttachment numerator='0' offset='30'/>",
        "        </FormData.left>",
        "      </FormData>",
        "    </t:Box.layoutData>",
        "  </t:Box>",
        "  <t:Box>",
        "    <t:Box.layoutData>",
        "      <FormData>",
        "        <FormData.bottom>",
        "          <FormAttachment numerator='100' offset='-100'/>",
        "        </FormData.bottom>",
        "        <FormData.right>",
        "          <FormAttachment numerator='100' offset='-150'/>",
        "        </FormData.right>",
        "      </FormData>",
        "    </t:Box.layoutData>",
        "  </t:Box>",
        "</Composite>");
  }

  public void test_canvas_MOVE() throws Exception {
    CompositeInfo composite =
        openEditor(
            "<Composite>",
            "  <Composite.layout>",
            "    <FormLayout/>",
            "  </Composite.layout>",
            "  <t:Box wbp:name='box'>",
            "    <t:Box.layoutData>",
            "      <FormData>",
            "        <FormData.top>",
            "          <FormAttachment numerator='0' offset='40'/>",
            "        </FormData.top>",
            "        <FormData.left>",
            "          <FormAttachment numerator='0' offset='30'/>",
            "        </FormData.left>",
            "      </FormData>",
            "    </t:Box.layoutData>",
            "  </t:Box>",
            "</Composite>");
    ControlInfo button = getObjectByName("box");
    // move
    canvas.sideMode().beginMove(button);
    canvas.target(composite).inX(50).inY(80).drag();
    canvas.endDrag();
    assertXML(
        "<Composite>",
        "  <Composite.layout>",
        "    <FormLayout/>",
        "  </Composite.layout>",
        "  <t:Box wbp:name='box'>",
        "    <t:Box.layoutData>",
        "      <FormData>",
        "        <FormData.top>",
        "          <FormAttachment numerator='0' offset='80'/>",
        "        </FormData.top>",
        "        <FormData.left>",
        "          <FormAttachment numerator='0' offset='50'/>",
        "        </FormData.left>",
        "      </FormData>",
        "    </t:Box.layoutData>",
        "  </t:Box>",
        "</Composite>");
  }

  public void test_canvas_RESIZE() throws Exception {
    openEditor(
        "<Composite>",
        "  <Composite.layout>",
        "    <FormLayout/>",
        "  </Composite.layout>",
        "  <t:Box wbp:name='box'>",
        "    <t:Box.layoutData>",
        "      <FormData>",
        "        <FormData.top>",
        "          <FormAttachment numerator='0' offset='40'/>",
        "        </FormData.top>",
        "        <FormData.left>",
        "          <FormAttachment numerator='0' offset='30'/>",
        "        </FormData.left>",
        "      </FormData>",
        "    </t:Box.layoutData>",
        "  </t:Box>",
        "</Composite>");
    ControlInfo button = getObjectByName("box");
    //
    canvas.beginResize(button, IPositionConstants.SOUTH_EAST);
    canvas.dragTo(button, 150, 100).endDrag();
    assertXML(
        "<Composite>",
        "  <Composite.layout>",
        "    <FormLayout/>",
        "  </Composite.layout>",
        "  <t:Box wbp:name='box'>",
        "    <t:Box.layoutData>",
        "      <FormData>",
        "        <FormData.bottom>",
        "          <FormAttachment numerator='0' offset='140'/>",
        "        </FormData.bottom>",
        "        <FormData.right>",
        "          <FormAttachment numerator='0' offset='180'/>",
        "        </FormData.right>",
        "        <FormData.top>",
        "          <FormAttachment numerator='0' offset='40'/>",
        "        </FormData.top>",
        "        <FormData.left>",
        "          <FormAttachment numerator='0' offset='30'/>",
        "        </FormData.left>",
        "      </FormData>",
        "    </t:Box.layoutData>",
        "  </t:Box>",
        "</Composite>");
  }

  public void test_canvas_ADD() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <Composite wbp:name='target'>",
        "    <Composite.layout>",
        "      <FormLayout/>",
        "    </Composite.layout>",
        "    <Composite.layoutData>",
        "      <RowData width='300' height='250'/>",
        "    </Composite.layoutData>",
        "  </Composite>",
        "  <t:Box wbp:name='box' text='Box'/>",
        "</Shell>");
    CompositeInfo target = getObjectByName("target");
    ControlInfo box = getObjectByName("box");
    // move
    canvas.sideMode().beginMove(box);
    canvas.target(target).inX(30).inY(20).drag();
    canvas.endDrag();
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <Composite wbp:name='target'>",
        "    <Composite.layout>",
        "      <FormLayout/>",
        "    </Composite.layout>",
        "    <Composite.layoutData>",
        "      <RowData width='300' height='250'/>",
        "    </Composite.layoutData>",
        "    <t:Box wbp:name='box' text='Box'>",
        "      <t:Box.layoutData>",
        "        <FormData>",
        "          <FormData.top>",
        "            <FormAttachment numerator='0' offset='20'/>",
        "          </FormData.top>",
        "          <FormData.left>",
        "            <FormAttachment numerator='0' offset='30'/>",
        "          </FormData.left>",
        "        </FormData>",
        "      </t:Box.layoutData>",
        "    </t:Box>",
        "  </Composite>",
        "</Shell>");
  }

  public void test_delete() throws Exception {
    openEditor(
        "<Composite>",
        "  <Composite.layout>",
        "    <FormLayout/>",
        "  </Composite.layout>",
        "  <t:Box wbp:name='box'>",
        "    <t:Box.layoutData>",
        "      <FormData>",
        "        <FormData.top>",
        "          <FormAttachment numerator='0' offset='40'/>",
        "        </FormData.top>",
        "        <FormData.left>",
        "          <FormAttachment numerator='0' offset='30'/>",
        "        </FormData.left>",
        "      </FormData>",
        "    </t:Box.layoutData>",
        "  </t:Box>",
        "</Composite>");
    ControlInfo box = getObjectByName("box");
    // delete
    box.delete();
    // test
    assertXML(
        "<Composite>",
        "  <Composite.layout>",
        "    <FormLayout/>",
        "  </Composite.layout>",
        "</Composite>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tree
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_tree_CREATE() throws Exception {
    CompositeInfo composite =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Composite>",
            "  <Composite.layout>",
            "    <FormLayout/>",
            "  </Composite.layout>",
            "</Composite>");
    // create Box
    ControlInfo newBox = loadBox();
    // use tree
    tree.moveOn(composite);
    tree.assertFeedback_on(composite);
    tree.click();
    assertXML(
        "// filler filler filler filler filler",
        "<Composite>",
        "  <Composite.layout>",
        "    <FormLayout/>",
        "  </Composite.layout>",
        "  <t:Box/>",
        "</Composite>");
    tree.assertPrimarySelected(newBox);
  }

  public void test_tree_PASTE() throws Exception {
    CompositeInfo composite =
        openEditor(
            "<Composite>",
            "  <Composite.layout>",
            "    <FormLayout/>",
            "  </Composite.layout>",
            "  <t:Box wbp:name='box' text='MyButton'>",
            "    <t:Box.layoutData>",
            "      <FormData>",
            "        <FormData.top>",
            "          <FormAttachment numerator='0' offset='40'/>",
            "        </FormData.top>",
            "        <FormData.left>",
            "          <FormAttachment numerator='0' offset='30'/>",
            "        </FormData.left>",
            "      </FormData>",
            "    </t:Box.layoutData>",
            "  </t:Box>",
            "</Composite>");
    // copy/paste "box"
    {
      ControlInfo box = getObjectByName("box");
      doCopyPaste(box);
    }
    // use tree
    tree.moveOn(composite);
    tree.assertFeedback_on(composite);
    tree.click();
    assertXML(
        "// filler filler filler filler filler",
        "<Composite>",
        "  <Composite.layout>",
        "    <FormLayout/>",
        "  </Composite.layout>",
        "  <t:Box wbp:name='box' text='MyButton'>",
        "    <t:Box.layoutData>",
        "      <FormData>",
        "        <FormData.top>",
        "          <FormAttachment numerator='0' offset='40'/>",
        "        </FormData.top>",
        "        <FormData.left>",
        "          <FormAttachment numerator='0' offset='30'/>",
        "        </FormData.left>",
        "      </FormData>",
        "    </t:Box.layoutData>",
        "  </t:Box>",
        "  <t:Box text='MyButton'/>",
        "</Composite>");
  }

  public void test_tree_MOVE() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<Composite>",
        "  <Composite.layout>",
        "    <FormLayout/>",
        "  </Composite.layout>",
        "  <t:Box wbp:name='box_1'/>",
        "  <t:Box wbp:name='box_2'/>",
        "</Composite>");
    ControlInfo box_1 = getObjectByName("box_1");
    ControlInfo box_2 = getObjectByName("box_2");
    // use tree
    tree.startDrag(box_2);
    tree.dragBefore(box_1);
    tree.assertFeedback_before(box_1);
    tree.endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Composite>",
        "  <Composite.layout>",
        "    <FormLayout/>",
        "  </Composite.layout>",
        "  <t:Box wbp:name='box_2'/>",
        "  <t:Box wbp:name='box_1'/>",
        "</Composite>");
  }

  public void test_tree_ADD() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <Composite wbp:name='target'>",
        "    <Composite.layout>",
        "      <FormLayout/>",
        "    </Composite.layout>",
        "  </Composite>",
        "  <t:Box wbp:name='box' text='Box'/>",
        "</Shell>");
    CompositeInfo target = getObjectByName("target");
    ControlInfo box = getObjectByName("box");
    // use tree
    tree.startDrag(box);
    tree.dragOn(target);
    tree.assertFeedback_on(target);
    tree.endDrag();
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <Composite wbp:name='target'>",
        "    <Composite.layout>",
        "      <FormLayout/>",
        "    </Composite.layout>",
        "    <t:Box wbp:name='box' text='Box'/>",
        "  </Composite>",
        "</Shell>");
  }
}
