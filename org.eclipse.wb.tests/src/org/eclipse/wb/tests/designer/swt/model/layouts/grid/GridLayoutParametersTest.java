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

import org.eclipse.wb.internal.rcp.ToolkitProvider;
import org.eclipse.wb.internal.swt.model.layout.grid.GridLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.grid.IPreferenceConstants;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.core.PreferencesRepairer;
import org.eclipse.wb.tests.designer.rcp.BTestUtils;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

/**
 * Test for {@link GridLayoutInfo} and special parameters for grab/alignment.
 * 
 * @author scheglov_ke
 */
public class GridLayoutParametersTest extends RcpModelTest {
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
   * {@link Text} widget is marked as required horizontal grab/fill.
   */
  public void test_CREATE_Text() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(1, false));",
            "  }",
            "}");
    refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    //
    ControlInfo newText = BTestUtils.createControl("org.eclipse.swt.widgets.Text");
    layout.command_CREATE(newText, 0, false, 0, false);
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Text text = new Text(this, SWT.BORDER);",
        "      text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test that horizontal grab/fill {@link Text} can be disabled.
   */
  public void test_CREATE_Text_disabled() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(1, false));",
            "  }",
            "}");
    refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    //
    PreferencesRepairer preferences =
        new PreferencesRepairer(ToolkitProvider.DESCRIPTION.getPreferences());
    try {
      preferences.setValue(IPreferenceConstants.P_ENABLE_GRAB, false);
      ControlInfo newText = BTestUtils.createControl("org.eclipse.swt.widgets.Text");
      layout.command_CREATE(newText, 0, false, 0, false);
      assertEditor(
          "class Test extends Shell {",
          "  Test() {",
          "    setLayout(new GridLayout(1, false));",
          "    {",
          "      Text text = new Text(this, SWT.BORDER);",
          "    }",
          "  }",
          "}");
    } finally {
      preferences.restore();
    }
  }

  /**
   * {@link Table} widget is marked as required horizontal/vertical grab/fill.
   */
  public void test_CREATE_Table() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(1, false));",
            "  }",
            "}");
    refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    //
    ControlInfo newTable = BTestUtils.createControl("org.eclipse.swt.widgets.Table");
    layout.command_CREATE(newTable, 0, false, 0, false);
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Table table = new Table(this, SWT.BORDER | SWT.FULL_SELECTION);",
        "      table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));",
        "      table.setHeaderVisible(true);",
        "      table.setLinesVisible(true);",
        "    }",
        "  }",
        "}");
  }

  /**
   * {@link Label} widget is marked as "right" aligned and next widget is {@link Text}, so when add
   * {@link Label} before {@link Text}, use {@link GridData#END} alignment.
   */
  public void test_CREATE_LabelBeforeText() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(2, false));",
            "    new Label(this, SWT.NONE);",
            "    {",
            "      Text text = new Text(this, SWT.BORDER);",
            "    }",
            "  }",
            "}");
    refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    //
    ControlInfo newLabel = BTestUtils.createControl("org.eclipse.swt.widgets.Label");
    layout.command_CREATE(newLabel, 0, false, 0, false);
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    {",
        "      Label label = new Label(this, SWT.NONE);",
        "      label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));",
        "      label.setText('New Label');",
        "    }",
        "    {",
        "      Text text = new Text(this, SWT.BORDER);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Check that automatic "right alignment" feature for {@link Label} can be disabled.
   */
  public void test_CREATE_LabelBeforeText_disabled() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(2, false));",
            "    new Label(this, SWT.NONE);",
            "    {",
            "      Text text = new Text(this, SWT.BORDER);",
            "    }",
            "  }",
            "}");
    refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    //
    PreferencesRepairer preferences =
        new PreferencesRepairer(ToolkitProvider.DESCRIPTION.getPreferences());
    try {
      preferences.setValue(IPreferenceConstants.P_ENABLE_RIGHT_ALIGNMENT, false);
      ControlInfo newLabel = BTestUtils.createControl("org.eclipse.swt.widgets.Label");
      layout.command_CREATE(newLabel, 0, false, 0, false);
      assertEditor(
          "class Test extends Shell {",
          "  Test() {",
          "    setLayout(new GridLayout(2, false));",
          "    {",
          "      Label label = new Label(this, SWT.NONE);",
          "      label.setText('New Label');",
          "    }",
          "    {",
          "      Text text = new Text(this, SWT.BORDER);",
          "    }",
          "  }",
          "}");
    } finally {
      preferences.restore();
    }
  }

  /**
   * {@link Label} widget is marked as "right" aligned and next widget is {@link Text}, so when add
   * {@link Text} after {@link Label}, use {@link GridData#END} alignment for {@link Label}.
   */
  public void test_CREATE_Text_afterLabel() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(2, false));",
            "    {",
            "      Label label = new Label(this, SWT.NONE);",
            "    }",
            "    new Label(this, SWT.NONE);",
            "  }",
            "}");
    refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    //
    ControlInfo newText = createJavaInfo("org.eclipse.swt.widgets.Text");
    layout.command_CREATE(newText, 1, false, 0, false);
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    {",
        "      Label label = new Label(this, SWT.NONE);",
        "      label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));",
        "    }",
        "    {",
        "      Text text = new Text(this, SWT.BORDER);",
        "      text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));",
        "    }",
        "  }",
        "}");
  }

  /**
   * When we add {@link Text} after "filler" {@link Label}, we should not change its alignment.
   */
  public void test_CREATE_Text_afterFiller() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new GridLayout(2, false));",
            "    new Label(this, SWT.NONE);",
            "    new Label(this, SWT.NONE);",
            "  }",
            "}");
    refresh();
    GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
    //
    ControlInfo newText = createJavaInfo("org.eclipse.swt.widgets.Text");
    layout.command_CREATE(newText, 1, false, 0, false);
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new GridLayout(2, false));",
        "    new Label(this, SWT.NONE);",
        "    {",
        "      Text text = new Text(this, SWT.BORDER);",
        "      text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));",
        "    }",
        "  }",
        "}");
  }
}