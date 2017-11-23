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
package org.eclipse.wb.tests.designer.XWT.model.forms.table;

import org.eclipse.wb.internal.xwt.model.forms.layout.table.TableWrapLayoutInfo;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

import org.eclipse.jface.action.IAction;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link TableWrapLayoutInfo} selection action's.
 * 
 * @author scheglov_ke
 */
public class TableWrapLayoutSelectionActionsTest extends XwtModelTest {
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
    return super.getTestSource_namespaces()
        + " xmlns:f='clr-namespace:org.eclipse.ui.forms.widgets'";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Asserts the there is {@link IAction} with given text, and it has expected <code>checked</code>
   * state.
   */
  private static void hasAction(List<Object> actions, String title, boolean checked) {
    IAction action = findAction(actions, title);
    assertNotNull(action);
    assertEquals(checked, action.isChecked());
  }

  public void test_selectionActions_emptySelection() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    refresh();
    // prepare actions
    List<Object> actions = getSelectionActions();
    // no actions
    assertThat(actions).isEmpty();
  }

  public void test_selectionActions_invalidSelection() throws Exception {
    CompositeInfo shell =
        parse(
            "<Shell>",
            "  <Shell.layout>",
            "    <f:TableWrapLayout wbp:name='layout'/>",
            "  </Shell.layout>",
            "  <Button wbp:name='button'/>",
            "</Shell>");
    refresh();
    ControlInfo button = getObjectByName("button");
    // prepare actions
    List<Object> actions = getSelectionActions(button, shell);
    // no actions
    assertThat(actions).isEmpty();
  }

  public void test_selectionActions_state() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Label wbp:name='label'/>",
        "  <Button wbp:name='button'>",
        "    <Button.layoutData>",
        "      <f:TableWrapData grabHorizontal='true'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
    refresh();
    ControlInfo label = getObjectByName("label");
    ControlInfo button = getObjectByName("button");
    // actions for "button"
    {
      List<Object> actions = getSelectionActions(button);
      // check actions
      hasAction(actions, "Left", true);
      hasAction(actions, "Center", false);
      hasAction(actions, "Right", false);
      hasAction(actions, "Fill horizontal", false);
      hasAction(actions, "Top", true);
      hasAction(actions, "Middle", false);
      hasAction(actions, "Bottom", false);
      hasAction(actions, "Fill vertical", false);
      hasAction(actions, "Horizontal grab", true);
      hasAction(actions, "Vertical grab", false);
    }
    // actions for "label", "button"
    {
      List<Object> actions = getSelectionActions(label, button);
      // check actions
      hasAction(actions, "Left", true);
      hasAction(actions, "Top", true);
      hasAction(actions, "Horizontal grab", false);
    }
  }

  public void test_grabAction() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'>",
        "    <Button.layoutData>",
        "      <f:TableWrapData grabHorizontal='true'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
    refresh();
    ControlInfo button = getObjectByName("button");
    // prepare actions
    List<Object> actions = getSelectionActions(button);
    // use "vertical grab" action
    {
      IAction verticalGrab = findAction(actions, "Vertical grab");
      assertFalse(verticalGrab.isChecked());
      verticalGrab.setChecked(true);
      verticalGrab.run();
      assertXML(
          "<Shell>",
          "  <Shell.layout>",
          "    <f:TableWrapLayout wbp:name='layout'/>",
          "  </Shell.layout>",
          "  <Button wbp:name='button'>",
          "    <Button.layoutData>",
          "      <f:TableWrapData grabHorizontal='true' grabVertical='true'/>",
          "    </Button.layoutData>",
          "  </Button>",
          "</Shell>");
    }
    // use "horizontal grab" action
    {
      IAction horizontalGrab = findAction(actions, "Horizontal grab");
      assertTrue(horizontalGrab.isChecked());
      horizontalGrab.setChecked(false);
      horizontalGrab.run();
      assertXML(
          "<Shell>",
          "  <Shell.layout>",
          "    <f:TableWrapLayout wbp:name='layout'/>",
          "  </Shell.layout>",
          "  <Button wbp:name='button'>",
          "    <Button.layoutData>",
          "      <f:TableWrapData grabVertical='true'/>",
          "    </Button.layoutData>",
          "  </Button>",
          "</Shell>");
    }
  }

  public void test_alignmentAction() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    refresh();
    ControlInfo button = getObjectByName("button");
    // prepare actions
    List<Object> actions = getSelectionActions(button);
    // set "right" alignment
    {
      IAction rightAlignment = findAction(actions, "Right");
      rightAlignment.setChecked(true);
      rightAlignment.run();
      assertXML(
          "<Shell>",
          "  <Shell.layout>",
          "    <f:TableWrapLayout wbp:name='layout'/>",
          "  </Shell.layout>",
          "  <Button wbp:name='button'>",
          "    <Button.layoutData>",
          "      <f:TableWrapData align='(org.eclipse.ui.forms.widgets.TableWrapData).RIGHT'/>",
          "    </Button.layoutData>",
          "  </Button>",
          "</Shell>");
    }
    // set "bottom" alignment
    {
      IAction bottomAction = findAction(actions, "Bottom");
      bottomAction.setChecked(true);
      bottomAction.run();
      assertXML(
          "// filler filler filler filler filler",
          "// filler filler filler filler filler",
          "<Shell>",
          "  <Shell.layout>",
          "    <f:TableWrapLayout wbp:name='layout'/>",
          "  </Shell.layout>",
          "  <Button wbp:name='button'>",
          "    <Button.layoutData>",
          "      <f:TableWrapData"
              + " align='(org.eclipse.ui.forms.widgets.TableWrapData).RIGHT'"
              + " valign='(org.eclipse.ui.forms.widgets.TableWrapData).BOTTOM'/>",
          "    </Button.layoutData>",
          "  </Button>",
          "</Shell>");
    }
  }
}