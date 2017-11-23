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
package org.eclipse.wb.tests.designer.XWT.model.layout.grid;

import org.eclipse.wb.internal.xwt.model.layout.grid.GridLayoutInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

import org.eclipse.jface.action.IAction;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link GridLayoutInfo} selection action's.
 * 
 * @author scheglov_ke
 */
public class GridLayoutSelectionActionsTest extends XwtModelTest {
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
  public void test_emptySelection() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout/>",
        "  </Shell.layout>",
        "</Shell>");
    refresh();
    // prepare actions
    List<Object> actions = getSelectionActions();
    // no actions
    assertThat(actions).isEmpty();
  }

  public void test_selectionActions() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Label wbp:name='label'/>",
        "  <Button wbp:name='button'>",
        "    <Button.layoutData>",
        "      <GridData grabExcessHorizontalSpace='true'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
    refresh();
    ControlInfo label = getObjectByName("label");
    ControlInfo button = getObjectByName("button");
    // prepare actions for: button
    List<Object> actions = getSelectionActions(button);
    // check actions
    assertEquals(13, actions.size()); // 10 action's, 3 separator's
    assertNotNull(findAction(actions, "Left"));
    assertTrue(findAction(actions, "Left").isChecked());
    assertNotNull(findAction(actions, "Center"));
    assertFalse(findAction(actions, "Center").isChecked());
    assertNotNull(findAction(actions, "Right"));
    assertFalse(findAction(actions, "Right").isChecked());
    assertNotNull(findAction(actions, "Fill"));
    assertFalse(findAction(actions, "Fill").isChecked());
    assertNotNull(findAction(actions, "Top"));
    assertNotNull(findAction(actions, "Bottom"));
    assertNotNull(findAction(actions, "Horizontal grab"));
    assertNotNull(findAction(actions, "Vertical grab"));
    assertTrue(findAction(actions, "Horizontal grab").isChecked());
    assertFalse(findAction(actions, "Vertical grab").isChecked());
    // prepare actions for: label, button
    actions = getSelectionActions(label, button);
    // check calculate common properties
    assertFalse(findAction(actions, "Horizontal grab").isChecked());
  }

  public void test_grabAction() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'>",
        "    <Button.layoutData>",
        "      <GridData grabExcessHorizontalSpace='true'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
    refresh();
    ControlInfo button = getObjectByName("button");
    // prepare actions: button
    List<Object> actions = getSelectionActions(button);
    // use "grab" actions
    {
      IAction verticalGrab = findAction(actions, "Vertical grab");
      verticalGrab.setChecked(true);
      verticalGrab.run();
    }
    {
      IAction horizontalGrab = findAction(actions, "Horizontal grab");
      horizontalGrab.setChecked(false);
      horizontalGrab.run();
    }
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'>",
        "    <Button.layoutData>",
        "      <GridData grabExcessVerticalSpace='true'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
  }

  public void test_alignmentAction() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    refresh();
    ControlInfo button = getObjectByName("button");
    // prepare actions: button
    List<Object> actions = getSelectionActions(button);
    // use "alignment" actions
    {
      IAction rightAlignment = findAction(actions, "Right");
      rightAlignment.setChecked(true);
      rightAlignment.run();
    }
    {
      IAction bottomAction = findAction(actions, "Bottom");
      bottomAction.setChecked(true);
      bottomAction.run();
    }
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <GridLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'>",
        "    <Button.layoutData>",
        "      <GridData horizontalAlignment='RIGHT' verticalAlignment='BOTTOM'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
  }
}