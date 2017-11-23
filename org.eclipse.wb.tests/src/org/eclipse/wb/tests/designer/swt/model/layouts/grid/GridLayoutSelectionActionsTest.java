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
package org.eclipse.wb.tests.designer.swt.model.layouts.grid;

import org.eclipse.wb.internal.swt.model.layout.grid.GridLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Control;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link GridLayoutInfo} selection action's.
 * 
 * @author lobas_av
 */
public class GridLayoutSelectionActionsTest extends RcpModelTest {
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
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout());",
            "  }",
            "}");
    shell.refresh();
    // prepare actions
    List<Object> actions = getSelectionActions();
    // no actions
    assertThat(actions).isEmpty();
  }

  public void test_selectionActions() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(2, false));",
            "    {",
            "      Label label = new Label(this, SWT.NONE);",
            "      label.setText('Label:');",
            "    }",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      {",
            "        GridData gridData = new GridData();",
            "        gridData.grabExcessHorizontalSpace = true;",
            "        button.setLayoutData(gridData);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    List<Object> actions;
    ControlInfo label = shell.getChildrenControls().get(0);
    ControlInfo button = shell.getChildrenControls().get(1);
    // prepare actions for: button
    actions = getSelectionActions(button);
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
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(2, false));",
            "    {",
            "      Label label = new Label(this, SWT.NONE);",
            "      label.setText('Label:');",
            "    }",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      {",
            "        GridData gridData = new GridData();",
            "        gridData.grabExcessHorizontalSpace = true;",
            "        button.setLayoutData(gridData);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ControlInfo button = shell.getChildrenControls().get(1);
    // prepare actions: button
    List<Object> actions = getSelectionActions(button);
    // check work "grab" actions
    IAction verticalGrab = findAction(actions, "Vertical grab");
    verticalGrab.setChecked(true);
    verticalGrab.run();
    //
    IAction horizontalGrab = findAction(actions, "Horizontal grab");
    horizontalGrab.setChecked(false);
    horizontalGrab.run();
    //
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    {",
        "      Label label = new Label(this, SWT.NONE);",
        "      label.setText('Label:');",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      {",
        "        GridData gridData = new GridData();",
        "        gridData.grabExcessVerticalSpace = true;",
        "        button.setLayoutData(gridData);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  public void test_alignmentAction() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(2, false));",
            "    {",
            "      Label label = new Label(this, SWT.NONE);",
            "      label.setText('Label:');",
            "    }",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      {",
            "        GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);",
            "        button.setLayoutData(gridData);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    List<Object> actions;
    ControlInfo button = shell.getChildrenControls().get(1);
    // prepare actions: button
    actions = getSelectionActions(button);
    // check work "alignment" actions
    IAction rightAlignment = findAction(actions, "Right");
    rightAlignment.setChecked(true);
    rightAlignment.run();
    //
    IAction bottomAction = findAction(actions, "Bottom");
    bottomAction.setChecked(true);
    bottomAction.run();
    //
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    {",
        "      Label label = new Label(this, SWT.NONE);",
        "      label.setText('Label:');",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, false));",
        "    }",
        "  }",
        "}");
    // prepare actions: wrong selection
    {
      actions = getSelectionActions(shell, button);
      assertTrue(actions.isEmpty());
    }
  }

  /**
   * Indirectly exposed {@link Control}'s should be ignored.
   */
  public void test_indirectExposedChild() throws Exception {
    setFileContentSrc(
        "test/ImplicitComposite.java",
        getTestSource(
            "public class ImplicitComposite extends Composite {",
            "  private Button m_button;",
            "  public ImplicitComposite(Composite parent, int style) {",
            "    super(parent, style);",
            "    setLayout(new GridLayout());",
            "    {",
            "      Composite container = new Composite(this, SWT.NONE);",
            "      container.setLayout(new RowLayout());",
            "      {",
            "        m_button = new Button(container, SWT.NONE);",
            "        m_button.setLayoutData(new RowData());",
            "      }",
            "    }",
            "  }",
            "  public Button getButton() {",
            "    return m_button;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    CompositeInfo composite =
        parseComposite(
            "public class Test extends ImplicitComposite {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "}");
    assertHierarchy(
        "{this: test.ImplicitComposite} {this} {}",
        "  {implicit-layout: org.eclipse.swt.layout.GridLayout} {implicit-layout} {}",
        "  {method: public org.eclipse.swt.widgets.Button test.ImplicitComposite.getButton()} {property} {}");
    composite.refresh();
    ControlInfo button = composite.getChildrenControls().get(0);
    // prepare actions: button
    List<Object> actions = getSelectionActions(button);
    // no actions
    assertThat(actions).isEmpty();
  }
}