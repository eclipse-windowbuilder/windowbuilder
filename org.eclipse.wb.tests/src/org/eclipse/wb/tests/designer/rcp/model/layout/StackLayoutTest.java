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
package org.eclipse.wb.tests.designer.rcp.model.layout;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;
import org.eclipse.wb.internal.rcp.model.layout.StackLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.FillLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.BTestUtils;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.swt.custom.StackLayout;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link StackLayoutInfo}
 * 
 * @author scheglov_ke
 */
public class StackLayoutTest extends RcpModelTest {
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
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new StackLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    assertInstanceOf(StackLayoutInfo.class, shell.getLayout());
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(new StackLayout())/ /new Button(this, SWT.NONE)/}",
        "  {new: org.eclipse.swt.custom.StackLayout} {empty} {/setLayout(new StackLayout())/}",
        "  {new: org.eclipse.swt.widgets.Button} {local-unique: button} {/new Button(this, SWT.NONE)/}");
  }

  public void test_parseEmpty() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new StackLayout());",
            "  }",
            "}");
    shell.refresh();
  }

  /**
   * During setting {@link StackLayout} there is time when we don't have yet container.
   */
  public void test_setLayout_wasGridLayout() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new GridLayout());",
            "  }",
            "}");
    shell.refresh();
    //
    StackLayoutInfo layout =
        (StackLayoutInfo) BTestUtils.createLayout("org.eclipse.swt.custom.StackLayout");
    shell.setLayout(layout);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new StackLayout());",
        "  }",
        "}");
    assertActiveControl(layout, null);
  }

  /**
   * Only "topControl" should be visible on design canvas.
   */
  public void test_visibilityGraphical() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new StackLayout());",
            "    {",
            "      Button button_1 = new Button(this, SWT.NONE);",
            "    }",
            "    {",
            "      Button button_2 = new Button(this, SWT.NONE);",
            "    }",
            "    {",
            "      Button button_3 = new Button(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    StackLayoutInfo layout = (StackLayoutInfo) shell.getLayout();
    ControlInfo button_1 = getJavaInfoByName("button_1");
    // "button_1" is top Control 
    assertSame(button_1, layout.getActiveControl());
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
  public void test_flowContainer() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new StackLayout());",
            "  }",
            "}");
    StackLayoutInfo layout = (StackLayoutInfo) shell.getLayout();
    // StackLayout is "flow container"
    List<FlowContainer> canvasContainers = new FlowContainerFactory(layout, true).get();
    List<FlowContainer> treeContainers = new FlowContainerFactory(layout, false).get();
    assertThat(canvasContainers).isNotEmpty();
    assertThat(treeContainers).isNotEmpty();
  }

  public void test_flowContainer_CREATE_asFirst() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new StackLayout());",
            "  }",
            "}");
    StackLayoutInfo layout = (StackLayoutInfo) shell.getLayout();
    //
    ControlInfo button = BTestUtils.createButton();
    flowContainer_CREATE(layout, button, null);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new StackLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
    assertActiveControl(layout, button);
  }

  public void test_flowContainer_CREATE_andActivate() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new StackLayout());",
            "    {",
            "      Button button_1 = new Button(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    StackLayoutInfo layout = (StackLayoutInfo) shell.getLayout();
    // initially "button_1" is active
    assertActiveControl(layout, shell.getChildrenControls().get(0));
    //
    ControlInfo button = BTestUtils.createButton();
    flowContainer_CREATE(layout, button, null);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new StackLayout());",
        "    {",
        "      Button button_1 = new Button(this, SWT.NONE);",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
    assertActiveControl(layout, button);
  }

  public void test_flowContainer_MOVE() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new StackLayout());",
            "    {",
            "      Button button_1 = new Button(this, SWT.NONE);",
            "    }",
            "    {",
            "      Button button_2 = new Button(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    StackLayoutInfo layout = (StackLayoutInfo) shell.getLayout();
    ControlInfo button_1 = shell.getChildrenControls().get(0);
    ControlInfo button_2 = shell.getChildrenControls().get(1);
    //
    flowContainer_MOVE(layout, button_2, button_1);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new StackLayout());",
        "    {",
        "      Button button_2 = new Button(this, SWT.NONE);",
        "    }",
        "    {",
        "      Button button_1 = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
    assertActiveControl(layout, button_2);
  }

  public void test_flowContainer_ADD() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    {",
            "      Composite composite = new Composite(this, SWT.NONE);",
            "      composite.setLayout(new StackLayout());",
            "      {",
            "        Button button_1 = new Button(composite, SWT.NONE);",
            "      }",
            "    }",
            "    {",
            "      Button button_2 = new Button(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    CompositeInfo composite = (CompositeInfo) shell.getChildrenControls().get(0);
    StackLayoutInfo stackLayout = (StackLayoutInfo) composite.getLayout();
    ControlInfo button_2 = shell.getChildrenControls().get(1);
    //
    flowContainer_MOVE(stackLayout, button_2, null);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    {",
        "      Composite composite = new Composite(this, SWT.NONE);",
        "      composite.setLayout(new StackLayout());",
        "      {",
        "        Button button_1 = new Button(composite, SWT.NONE);",
        "      }",
        "      {",
        "        Button button_2 = new Button(composite, SWT.NONE);",
        "      }",
        "    }",
        "  }",
        "}");
    assertActiveControl(stackLayout, button_2);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_clipboard() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    {",
            "      Composite c = new Composite(this, SWT.NONE);",
            "      c.setLayout(new StackLayout());",
            "      {",
            "        Button button_1 = new Button(c, SWT.NONE);",
            "      }",
            "      {",
            "        Button button_2 = new Button(c, SWT.NONE);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    FillLayoutInfo fillLayout = (FillLayoutInfo) shell.getLayout();
    // prepare memento
    JavaInfoMemento memento;
    {
      CompositeInfo composite = (CompositeInfo) shell.getChildrenControls().get(0);
      memento = JavaInfoMemento.createMemento(composite);
    }
    // do paste
    CompositeInfo newComposite = (CompositeInfo) memento.create(shell);
    fillLayout.command_CREATE(newComposite, null);
    memento.apply();
    //
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    {",
        "      Composite c = new Composite(this, SWT.NONE);",
        "      c.setLayout(new StackLayout());",
        "      {",
        "        Button button_1 = new Button(c, SWT.NONE);",
        "      }",
        "      {",
        "        Button button_2 = new Button(c, SWT.NONE);",
        "      }",
        "    }",
        "    {",
        "      Composite composite = new Composite(this, SWT.NONE);",
        "      composite.setLayout(new StackLayout());",
        "      {",
        "        Button button = new Button(composite, SWT.NONE);",
        "      }",
        "      {",
        "        Button button = new Button(composite, SWT.NONE);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // activeControl
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_activeControl() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new StackLayout());",
            "    {",
            "      Button button_1 = new Button(this, SWT.NONE);",
            "    }",
            "    {",
            "      Button button_2 = new Button(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    StackLayoutInfo layout = (StackLayoutInfo) shell.getLayout();
    List<ControlInfo> buttons = shell.getChildrenControls();
    ControlInfo button_1 = buttons.get(0);
    ControlInfo button_2 = buttons.get(1);
    // initially "button_1" is active
    assertActiveControl(layout, button_1);
    // notify about "button_2"
    {
      boolean shouldRefresh = notifySelecting(button_2);
      assertTrue(shouldRefresh);
      shell.refresh();
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
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new StackLayout());",
            "    {",
            "      Button button_1 = new Button(this, SWT.NONE);",
            "    }",
            "    {",
            "      Button button_2 = new Button(this, SWT.NONE);",
            "    }",
            "    {",
            "      Button button_3 = new Button(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    StackLayoutInfo layout = (StackLayoutInfo) shell.getLayout();
    List<ControlInfo> buttons = shell.getChildrenControls();
    ControlInfo button_1 = buttons.get(0);
    ControlInfo button_2 = buttons.get(1);
    ControlInfo button_3 = buttons.get(2);
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
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new StackLayout());",
            "    {",
            "      Button button_1 = new Button(this, SWT.NONE);",
            "    }",
            "    {",
            "      Button button_2 = new Button(this, SWT.NONE);",
            "    }",
            "    {",
            "      Button button_3 = new Button(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    StackLayoutInfo layout = (StackLayoutInfo) shell.getLayout();
    ControlInfo button_1 = getJavaInfoByName("button_1");
    ControlInfo button_2 = getJavaInfoByName("button_2");
    ControlInfo button_3 = getJavaInfoByName("button_3");
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