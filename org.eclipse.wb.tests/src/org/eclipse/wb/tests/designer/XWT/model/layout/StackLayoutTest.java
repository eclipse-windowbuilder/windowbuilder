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

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.xwt.model.layout.StackLayoutInfo;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

import org.eclipse.swt.custom.StackLayout;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link StackLayoutInfo}
 * 
 * @author scheglov_ke
 */
public class StackLayoutTest extends XwtModelTest {
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
  public void test_parse() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <StackLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <StackLayout>",
        "  <Button wbp:name='button'>");
  }

  public void test_parseEmpty() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <StackLayout/>",
        "  </Shell.layout>",
        "</Shell>");
    refresh();
  }

  /**
   * During setting {@link StackLayout} there is time when we don't have yet container.
   */
  public void test_setLayout_wasGridLayout() throws Exception {
    CompositeInfo shell =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <RowLayout/>",
            "  </Shell.layout>",
            "</Shell>");
    refresh();
    //
    StackLayoutInfo layout = createObject("org.eclipse.swt.custom.StackLayout");
    shell.setLayout(layout);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <StackLayout/>",
        "  </Shell.layout>",
        "</Shell>");
    assertActiveControl(layout, null);
  }

  /**
   * Only "topControl" should be visible on design canvas.
   */
  public void test_visibilityGraphical() throws Exception {
    CompositeInfo shell =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <StackLayout wbp:name='layout'/>",
            "  </Shell.layout>",
            "  <Button wbp:name='button_1'/>",
            "  <Button wbp:name='button_2'/>",
            "  <Button wbp:name='button_3'/>",
            "</Shell>");
    refresh();
    StackLayoutInfo layout = getObjectByName("layout");
    ControlInfo button_1 = getObjectByName("button_1");
    // "button_1" is top Control
    assertActiveControl(layout, button_1);
    // only "button_1" is in "graphical children"
    {
      List<ObjectInfo> children = shell.getPresentation().getChildrenGraphical();
      assertThat(children).containsExactly(button_1);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Flow container
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_flowContainer_CREATE_asFirst() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <StackLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "</Shell>");
    refresh();
    StackLayoutInfo layout = getObjectByName("layout");
    assertTrue(layout.isHorizontal());
    //
    ControlInfo newButton = createButton();
    flowContainer_CREATE(layout, newButton, null);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <StackLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button/>",
        "</Shell>");
    assertActiveControl(layout, newButton);
  }

  public void test_flowContainer_CREATE_andActivate() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <StackLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='existingButton'/>",
        "</Shell>");
    refresh();
    StackLayoutInfo layout = getObjectByName("layout");
    ControlInfo existingButton = getObjectByName("existingButton");
    // initially "button_1" is active
    assertActiveControl(layout, existingButton);
    //
    ControlInfo newButton = createButton();
    flowContainer_CREATE(layout, newButton, null);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <StackLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='existingButton'/>",
        "  <Button/>",
        "</Shell>");
    assertActiveControl(layout, newButton);
  }

  public void test_flowContainer_MOVE() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <StackLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_2'/>",
        "</Shell>");
    refresh();
    StackLayoutInfo layout = getObjectByName("layout");
    ControlInfo button_1 = getObjectByName("button_1");
    // initially
    assertActiveControl(layout, button_1);
    // move "button_1"
    flowContainer_MOVE(layout, button_1, null);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <StackLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_2'/>",
        "  <Button wbp:name='button_1'/>",
        "</Shell>");
    assertActiveControl(layout, button_1);
  }

  public void test_flowContainer_ADD() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <Composite>",
        "    <Composite.layout>",
        "      <StackLayout wbp:name='layout'/>",
        "    </Composite.layout>",
        "  </Composite>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    refresh();
    StackLayoutInfo stackLayout = getObjectByName("layout");
    ControlInfo button = getObjectByName("button");
    //
    flowContainer_MOVE(stackLayout, button, null);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <Composite>",
        "    <Composite.layout>",
        "      <StackLayout wbp:name='layout'/>",
        "    </Composite.layout>",
        "    <Button wbp:name='button'/>",
        "  </Composite>",
        "</Shell>");
    assertActiveControl(stackLayout, button);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_clipboard() throws Exception {
    final CompositeInfo shell =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <RowLayout/>",
            "  </Shell.layout>",
            "  <Composite wbp:name='composite'>",
            "    <Composite.layout>",
            "      <StackLayout wbp:name='layout'/>",
            "    </Composite.layout>",
            "    <Button text='1'/>",
            "    <Button text='2'/>",
            "  </Composite>",
            "</Shell>");
    refresh();
    // do copy/paste
    {
      CompositeInfo composite = (CompositeInfo) getObjectByName("composite");
      doCopyPaste(composite, new PasteProcedure<ControlInfo>() {
        public void run(ControlInfo copy) throws Exception {
          shell.getLayout().command_CREATE(copy, null);
        }
      });
    }
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <Composite wbp:name='composite'>",
        "    <Composite.layout>",
        "      <StackLayout wbp:name='layout'/>",
        "    </Composite.layout>",
        "    <Button text='1'/>",
        "    <Button text='2'/>",
        "  </Composite>",
        "  <Composite>",
        "    <Composite.layout>",
        "      <StackLayout/>",
        "    </Composite.layout>",
        "    <Button text='1'/>",
        "    <Button text='2'/>",
        "  </Composite>",
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // activeControl
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_activeControl() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <StackLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_2'/>",
        "</Shell>");
    refresh();
    StackLayoutInfo layout = getObjectByName("layout");
    ControlInfo button_1 = getObjectByName("button_1");
    ControlInfo button_2 = getObjectByName("button_2");
    // initially "button_1" is active
    assertActiveControl(layout, button_1);
    // notify about "button_2"
    {
      boolean shouldRefresh = notifySelecting(button_2);
      assertTrue(shouldRefresh);
      refresh();
      // now "button_2" is active
      assertActiveControl(layout, button_2);
    }
    // second notification about "button_2" does not cause refresh()
    {
      boolean shouldRefresh = notifySelecting(button_2);
      assertFalse(shouldRefresh);
    }
  }

  public void test_activeControl_whenDelete() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <StackLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_2'/>",
        "  <Button wbp:name='button_3'/>",
        "</Shell>");
    refresh();
    StackLayoutInfo layout = getObjectByName("layout");
    ControlInfo button_1 = getObjectByName("button_1");
    ControlInfo button_2 = getObjectByName("button_2");
    ControlInfo button_3 = getObjectByName("button_3");
    // initially "button_1" is active
    assertActiveControl(layout, button_1);
    // delete "button_1", so "button_2" should be activated
    button_1.delete();
    assertActiveControl(layout, button_2);
    // delete "button_2", so "button_3" should be activated
    button_2.delete();
    assertActiveControl(layout, button_3);
    // delete "button_3", so no active
    button_3.delete();
    assertActiveControl(layout, null);
  }

  /**
   * Test for {@link StackLayoutInfo#getPrevControl()}, {@link StackLayoutInfo#getNextControl()} and
   * {@link StackLayoutInfo#show(ControlInfo)}.
   */
  public void test_activeControl_showPrevNext() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <StackLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_2'/>",
        "  <Button wbp:name='button_3'/>",
        "</Shell>");
    refresh();
    StackLayoutInfo layout = getObjectByName("layout");
    ControlInfo button_1 = getObjectByName("button_1");
    ControlInfo button_2 = getObjectByName("button_2");
    ControlInfo button_3 = getObjectByName("button_3");
    // initially "button_1" is active
    assertActiveControl(layout, button_1);
    // show previous
    assertSame(button_3, layout.getPrevControl());
    layout.show(button_3);
    assertActiveControl(layout, button_3);
    // show next
    assertSame(button_1, layout.getNextControl());
    layout.show(button_1);
    assertActiveControl(layout, button_1);
    // show next again
    assertSame(button_2, layout.getNextControl());
    layout.show(button_2);
    assertActiveControl(layout, button_2);
  }

  private static void assertActiveControl(StackLayoutInfo layout, ControlInfo expected)
      throws Exception {
    ControlInfo actualControl = layout.getActiveControl();
    assertSame(expected, actualControl);
  }
}