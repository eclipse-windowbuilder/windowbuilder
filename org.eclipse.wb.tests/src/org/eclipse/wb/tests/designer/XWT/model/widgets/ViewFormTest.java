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

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.util.ObjectsLabelProvider;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.xwt.model.widgets.AbstractPositionInfo;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ViewFormInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

import java.util.List;

/**
 * Test for {@link ViewFormInfo}.
 * 
 * @author scheglov_ke
 */
public class ViewFormTest extends XwtModelTest {
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
  public void test_defaultProperties() throws Exception {
    ViewFormInfo viewForm =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ViewForm/>");
    refresh();
    // check default property values
    assertSame(Boolean.FALSE, viewForm.getPropertyByTitle("borderVisible").getValue());
    assertSame(Boolean.FALSE, viewForm.getPropertyByTitle("topCenterSeparate").getValue());
  }

  /**
   * No any children {@link ControlInfo}'s, so for all positions <code>null</code>.
   */
  public void test_childrenNo() throws Exception {
    ViewFormInfo viewForm =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ViewForm/>");
    // no "real" Control's
    assertNull(viewForm.getControl("noSuchMethod"));
    assertNull(viewForm.getControl("content"));
    assertNull(viewForm.getControl("topLeft"));
    assertNull(viewForm.getControl("topCenter"));
    assertNull(viewForm.getControl("topRight"));
  }

  /**
   * Test for {@link ViewFormInfo#getControl(String)}.
   */
  public void test_children() throws Exception {
    ViewFormInfo viewForm =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ViewForm>",
            "  <ViewForm.content>",
            "    <Button wbp:name='button'/>",
            "  </ViewForm.content>",
            "</ViewForm>");
    ControlInfo button = getObjectByName("button");
    assertSame(button, viewForm.getControl("content"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Each {@link ControlInfo} text is decorated with its position method.
   */
  public void test_presentation_decorateText() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ViewForm>",
        "  <ViewForm.content>",
        "    <Button wbp:name='button'/>",
        "  </ViewForm.content>",
        "</ViewForm>");
    ControlInfo button = getObjectByName("button");
    assertEquals("content - Button", ObjectsLabelProvider.INSTANCE.getText(button));
  }

  /**
   * Even when no "real" {@link ControlInfo} children, tree still has {@link AbstractPositionInfo}
   * placeholders.
   */
  public void test_AbstractPositionInfo_getChildrenTree_placeholders() throws Exception {
    ViewFormInfo viewForm =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ViewForm/>");
    // no "real" Control's, but in "tree" we have position placeholder children
    List<ObjectInfo> children = viewForm.getPresentation().getChildrenTree();
    assertEquals(4, children.size());
    assertEquals(4, GenericsUtils.select(children, AbstractPositionInfo.class).size());
    // prepare "topLeft" position
    AbstractPositionInfo positionTopLeft = (AbstractPositionInfo) children.get(0);
    assertSame(viewForm, positionTopLeft.getComposite());
    // check AbstractPosition_Info presentation
    {
      IObjectPresentation presentation = positionTopLeft.getPresentation();
      assertNotNull(presentation.getIcon());
      assertEquals("topLeft", presentation.getText());
    }
  }

  /**
   * "Tree" children of {@link ViewFormInfo} should be sorted in same order as "set" methods array
   * passed to constructor.
   */
  public void test_AbstractPositionInfo_getChildrenTree_sortChildren() throws Exception {
    ViewFormInfo viewForm =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ViewForm>",
            "  <ViewForm.topCenter>",
            "    <Button wbp:name='button'/>",
            "  </ViewForm.topCenter>",
            "</ViewForm>");
    ControlInfo button = getObjectByName("button");
    // check "tree" children
    List<ObjectInfo> children = viewForm.getPresentation().getChildrenTree();
    assertEquals(4, children.size());
    // index: 0
    {
      AbstractPositionInfo position = (AbstractPositionInfo) children.get(0);
      assertEquals("topLeft", ObjectsLabelProvider.INSTANCE.getText(position));
    }
    // index: 1
    assertSame(button, children.get(1));
    // index: 2
    {
      AbstractPositionInfo position = (AbstractPositionInfo) children.get(2);
      assertEquals("topRight", ObjectsLabelProvider.INSTANCE.getText(position));
    }
    // index: 3
    {
      AbstractPositionInfo position = (AbstractPositionInfo) children.get(3);
      assertEquals("content", ObjectsLabelProvider.INSTANCE.getText(position));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ViewFormInfo#command_CREATE(ControlInfo, String)}.
   */
  public void test_CREATE() throws Exception {
    ViewFormInfo viewForm =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ViewForm/>");
    //
    ControlInfo button = createButton();
    viewForm.command_CREATE(button, "content");
    assertSame(button, viewForm.getControl("content"));
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ViewForm>",
        "  <ViewForm.content>",
        "    <Button/>",
        "  </ViewForm.content>",
        "</ViewForm>");
  }

  /**
   * Test for {@link ViewFormInfo#command_MOVE(ControlInfo, String)}.
   * <p>
   * "Move" into different position.
   */
  public void test_MOVE_1() throws Exception {
    ViewFormInfo viewForm =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ViewForm>",
            "  <ViewForm.content>",
            "    <Button wbp:name='button'/>",
            "  </ViewForm.content>",
            "</ViewForm>");
    ControlInfo button = getObjectByName("button");
    // initially "button" has position "content"
    assertSame(button, viewForm.getControl("content"));
    // do move
    viewForm.command_MOVE(button, "topLeft");
    // now "button" is in "topLeft"
    assertNull(viewForm.getControl("content"));
    assertSame(button, viewForm.getControl("topLeft"));
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ViewForm>",
        "  <ViewForm.topLeft>",
        "    <Button wbp:name='button'/>",
        "  </ViewForm.topLeft>",
        "</ViewForm>");
  }

  /**
   * Test for {@link ViewFormInfo#command_MOVE(ControlInfo, String)}.
   * <p>
   * Move into {@link ViewFormInfo}.
   */
  public void test_MOVE_2() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <ViewForm wbp:name='viewForm'/>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    ViewFormInfo viewForm = getObjectByName("viewForm");
    ControlInfo button = getObjectByName("button");
    //
    viewForm.command_MOVE(button, "topLeft");
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <ViewForm wbp:name='viewForm'>",
        "    <ViewForm.topLeft>",
        "      <Button wbp:name='button'/>",
        "    </ViewForm.topLeft>",
        "  </ViewForm>",
        "</Shell>");
  }

  /**
   * Test for {@link ViewFormInfo#command_MOVE(ControlInfo, String)}.
   * <p>
   * Move from {@link ViewFormInfo}.
   */
  public void test_MOVE_3() throws Exception {
    CompositeInfo shell =
        parse(
            "<Shell>",
            "  <Shell.layout>",
            "    <RowLayout/>",
            "  </Shell.layout>",
            "  <ViewForm wbp:name='viewForm'>",
            "    <ViewForm.topLeft>",
            "      <Button wbp:name='button'/>",
            "    </ViewForm.topLeft>",
            "  </ViewForm>",
            "</Shell>");
    refresh();
    ControlInfo button = getObjectByName("button");
    //
    shell.getLayout().command_MOVE(button, null);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <ViewForm wbp:name='viewForm'/>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
  }

  /**
   * Test for {@link ViewFormInfo#command_MOVE(ControlInfo, String)}.
   * <p>
   * After moving of {@link ControlInfo}'s into new position it should be places in same order, as
   * "position" properties.
   */
  public void test_MOVE_4() throws Exception {
    ViewFormInfo viewForm =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ViewForm>",
            "  <ViewForm.topCenter>",
            "    <Button/>",
            "  </ViewForm.topCenter>",
            "  <ViewForm.topRight>",
            "    <Button wbp:name='button'/>",
            "  </ViewForm.topRight>",
            "</ViewForm>");
    ControlInfo button = getObjectByName("button");
    //
    viewForm.command_MOVE(button, "topLeft");
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ViewForm>",
        "  <ViewForm.topLeft>",
        "    <Button wbp:name='button'/>",
        "  </ViewForm.topLeft>",
        "  <ViewForm.topCenter>",
        "    <Button/>",
        "  </ViewForm.topCenter>",
        "</ViewForm>");
  }
}