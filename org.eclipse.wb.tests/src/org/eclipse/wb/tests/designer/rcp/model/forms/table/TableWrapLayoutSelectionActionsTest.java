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
package org.eclipse.wb.tests.designer.rcp.model.forms.table;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.TableWrapLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.model.forms.AbstractFormsTest;

import org.eclipse.jface.action.IAction;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link TableWrapLayoutInfo} selection action's.
 * 
 * @author scheglov_ke
 */
public class TableWrapLayoutSelectionActionsTest extends AbstractFormsTest {
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
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    {",
            "      TableWrapLayout layout = new TableWrapLayout();",
            "      setLayout(layout);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // prepare actions
    List<Object> actions;
    {
      actions = Lists.newArrayList();
      List<ObjectInfo> selectedObjects = ImmutableList.<ObjectInfo>of();
      shell.getBroadcastObject().addSelectionActions(selectedObjects, actions);
    }
    // no actions
    assertThat(actions).isEmpty();
  }

  public void test_selectionActions_invalidSelection() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    {",
            "      TableWrapLayout layout = new TableWrapLayout();",
            "      setLayout(layout);",
            "    }",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ControlInfo button = shell.getChildrenControls().get(0);
    // prepare actions
    List<Object> actions;
    {
      actions = Lists.newArrayList();
      List<ObjectInfo> selectedObjects = ImmutableList.<ObjectInfo>of(button, shell);
      shell.getBroadcastObject().addSelectionActions(selectedObjects, actions);
    }
    // no actions
    assertThat(actions).isEmpty();
  }

  public void test_selectionActions_state() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    {",
            "      TableWrapLayout layout = new TableWrapLayout();",
            "      layout.numColumns = 2;",
            "      setLayout(layout);",
            "    }",
            "    {",
            "      Label label = new Label(this, SWT.NONE);",
            "      label.setText(\"Label:\");",
            "    }",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      {",
            "        TableWrapData tableWrapData = new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP);",
            "        tableWrapData.grabHorizontal = true;",
            "        button.setLayoutData(tableWrapData);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ControlInfo label = shell.getChildrenControls().get(0);
    ControlInfo button = shell.getChildrenControls().get(1);
    // actions for "button"
    {
      List<Object> actions = Lists.newArrayList();
      List<ObjectInfo> selectedObjects = ImmutableList.<ObjectInfo>of(button);
      shell.getBroadcastObject().addSelectionActions(selectedObjects, actions);
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
      List<Object> actions = Lists.newArrayList();
      List<ObjectInfo> selectedObjects = ImmutableList.<ObjectInfo>of(label, button);
      shell.getBroadcastObject().addSelectionActions(selectedObjects, actions);
      // check actions
      hasAction(actions, "Left", true);
      hasAction(actions, "Top", true);
      hasAction(actions, "Horizontal grab", false);
    }
  }

  public void test_grabAction() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    {",
            "      TableWrapLayout layout = new TableWrapLayout();",
            "      setLayout(layout);",
            "    }",
            "    {",
            "      Label label = new Label(this, SWT.NONE);",
            "    }",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      {",
            "        TableWrapData tableWrapData = new TableWrapData();",
            "        tableWrapData.grabHorizontal = true;",
            "        button.setLayoutData(tableWrapData);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ControlInfo button = shell.getChildrenControls().get(1);
    // prepare actions
    List<Object> actions;
    {
      actions = Lists.newArrayList();
      List<ObjectInfo> selectedObjects = ImmutableList.<ObjectInfo>of(button);
      shell.getBroadcastObject().addSelectionActions(selectedObjects, actions);
    }
    // use "vertical grab" action
    {
      IAction verticalGrab = findAction(actions, "Vertical grab");
      assertFalse(verticalGrab.isChecked());
      verticalGrab.setChecked(true);
      verticalGrab.run();
      assertEditor(
          "class Test extends Shell {",
          "  Test() {",
          "    {",
          "      TableWrapLayout layout = new TableWrapLayout();",
          "      setLayout(layout);",
          "    }",
          "    {",
          "      Label label = new Label(this, SWT.NONE);",
          "    }",
          "    {",
          "      Button button = new Button(this, SWT.NONE);",
          "      {",
          "        TableWrapData tableWrapData = new TableWrapData();",
          "        tableWrapData.grabVertical = true;",
          "        tableWrapData.grabHorizontal = true;",
          "        button.setLayoutData(tableWrapData);",
          "      }",
          "    }",
          "  }",
          "}");
    }
    // use "horizontal grab" action
    {
      IAction horizontalGrab = findAction(actions, "Horizontal grab");
      assertTrue(horizontalGrab.isChecked());
      horizontalGrab.setChecked(false);
      horizontalGrab.run();
      assertEditor(
          "class Test extends Shell {",
          "  Test() {",
          "    {",
          "      TableWrapLayout layout = new TableWrapLayout();",
          "      setLayout(layout);",
          "    }",
          "    {",
          "      Label label = new Label(this, SWT.NONE);",
          "    }",
          "    {",
          "      Button button = new Button(this, SWT.NONE);",
          "      {",
          "        TableWrapData tableWrapData = new TableWrapData();",
          "        tableWrapData.grabVertical = true;",
          "        button.setLayoutData(tableWrapData);",
          "      }",
          "    }",
          "  }",
          "}");
    }
  }

  public void test_alignmentAction() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    {",
            "      TableWrapLayout layout = new TableWrapLayout();",
            "      layout.numColumns = 2;",
            "      setLayout(layout);",
            "    }",
            "    {",
            "      Label label = new Label(this, SWT.NONE);",
            "    }",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      {",
            "        TableWrapData tableWrapData = new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP);",
            "        button.setLayoutData(tableWrapData);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ControlInfo button = shell.getChildrenControls().get(1);
    // prepare actions
    List<Object> actions;
    {
      actions = Lists.newArrayList();
      List<ObjectInfo> selectedObjects = ImmutableList.<ObjectInfo>of(button);
      shell.getBroadcastObject().addSelectionActions(selectedObjects, actions);
    }
    // set "right" alignment
    {
      IAction rightAlignment = findAction(actions, "Right");
      rightAlignment.setChecked(true);
      rightAlignment.run();
      assertEditor(
          "class Test extends Shell {",
          "  Test() {",
          "    {",
          "      TableWrapLayout layout = new TableWrapLayout();",
          "      layout.numColumns = 2;",
          "      setLayout(layout);",
          "    }",
          "    {",
          "      Label label = new Label(this, SWT.NONE);",
          "    }",
          "    {",
          "      Button button = new Button(this, SWT.NONE);",
          "      button.setLayoutData(new TableWrapData(TableWrapData.RIGHT, TableWrapData.TOP));",
          "    }",
          "  }",
          "}");
    }
    // set "bottom" alignment
    {
      IAction bottomAction = findAction(actions, "Bottom");
      bottomAction.setChecked(true);
      bottomAction.run();
      assertEditor(
          "class Test extends Shell {",
          "  Test() {",
          "    {",
          "      TableWrapLayout layout = new TableWrapLayout();",
          "      layout.numColumns = 2;",
          "      setLayout(layout);",
          "    }",
          "    {",
          "      Label label = new Label(this, SWT.NONE);",
          "    }",
          "    {",
          "      Button button = new Button(this, SWT.NONE);",
          "      button.setLayoutData(new TableWrapData(TableWrapData.RIGHT, TableWrapData.BOTTOM));",
          "    }",
          "  }",
          "}");
    }
  }
}