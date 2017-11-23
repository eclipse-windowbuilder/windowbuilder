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
package org.eclipse.wb.tests.designer.rcp.model.forms;

import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.rcp.model.forms.layout.column.ColumnLayoutDataInfo;
import org.eclipse.wb.internal.rcp.model.forms.layout.column.ColumnLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.FillLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.forms.widgets.ColumnLayout;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link ColumnLayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class ColumnLayoutTest extends AbstractFormsTest {
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
  public void test_LayoutData_implicit() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new ColumnLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ControlInfo button = shell.getChildrenControls().get(0);
    ColumnLayoutDataInfo columnData = ColumnLayoutInfo.getColumnData(button);
    columnData.setWidthHint(100);
    columnData.setHeightHint(200);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new ColumnLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      {",
        "        ColumnLayoutData columnLayoutData = new ColumnLayoutData();",
        "        columnLayoutData.heightHint = 200;",
        "        columnLayoutData.widthHint = 100;",
        "        button.setLayoutData(columnLayoutData);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  public void test_LayoutData_explicit() throws Exception {
    CompositeInfo shell =
        parseJavaInfo(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new ColumnLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setLayoutData(new ColumnLayoutData(100, 200));",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ControlInfo button = shell.getChildrenControls().get(0);
    ColumnLayoutDataInfo columnData = ColumnLayoutInfo.getColumnData(button);
    assertInstanceOf(ConstructorCreationSupport.class, columnData.getCreationSupport());
  }

  public void test_copyPaste() throws Exception {
    CompositeInfo shell =
        parseJavaInfo(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    {",
            "      Composite composite = new Composite(this, SWT.NONE);",
            "      composite.setLayout(new ColumnLayout());",
            "      {",
            "        Button button = new Button(composite, SWT.NONE);",
            "        button.setLayoutData(new ColumnLayoutData(100, 200));",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    FillLayoutInfo fillLayout = (FillLayoutInfo) shell.getLayout();
    // do copy
    JavaInfoMemento memento;
    {
      CompositeInfo composite = (CompositeInfo) shell.getChildrenControls().get(0);
      memento = JavaInfoMemento.createMemento(composite);
    }
    // do paste
    {
      CompositeInfo newComposite = (CompositeInfo) memento.create(shell);
      fillLayout.command_CREATE(newComposite, null);
      memento.apply();
    }
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    {",
        "      Composite composite = new Composite(this, SWT.NONE);",
        "      composite.setLayout(new ColumnLayout());",
        "      {",
        "        Button button = new Button(composite, SWT.NONE);",
        "        button.setLayoutData(new ColumnLayoutData(100, 200));",
        "      }",
        "    }",
        "    {",
        "      Composite composite = new Composite(this, SWT.NONE);",
        "      composite.setLayout(new ColumnLayout());",
        "      {",
        "        Button button = new Button(composite, SWT.NONE);",
        "        {",
        "          button.setLayoutData(new ColumnLayoutData(100, 200));",
        "        }",
        "      }",
        "    }",
        "  }",
        "}");
  }

  public void test_selectionActions_1() throws Exception {
    CompositeInfo shell =
        parseJavaInfo(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new ColumnLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      {",
            "        ColumnLayoutData columnLayoutData = new ColumnLayoutData();",
            "        button.setLayoutData(columnLayoutData);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ControlInfo button = shell.getChildrenControls().get(0);
    // prepare actions
    List<Object> actions = getSelectionActions(button);
    // check actions
    assertThat(actions).hasSize(5); // separator, 4 action's
    assertNotNull(findAction(actions, "Left"));
    assertNotNull(findAction(actions, "Center"));
    assertNotNull(findAction(actions, "Right"));
    assertNotNull(findAction(actions, "Fill"));
    //
    assertTrue(findAction(actions, "Fill").isChecked());
    // set "Left" alignment
    {
      IAction leftAction = findAction(actions, "Left");
      assertFalse(leftAction.isChecked());
      //
      leftAction.setChecked(true);
      leftAction.run();
      assertEditor(
          "public class Test extends Shell {",
          "  public Test() {",
          "    setLayout(new ColumnLayout());",
          "    {",
          "      Button button = new Button(this, SWT.NONE);",
          "      {",
          "        ColumnLayoutData columnLayoutData = new ColumnLayoutData();",
          "        columnLayoutData.horizontalAlignment = ColumnLayoutData.LEFT;",
          "        button.setLayoutData(columnLayoutData);",
          "      }",
          "    }",
          "  }",
          "}");
    }
  }

  /**
   * No selection.
   */
  public void test_selectionActions_2() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new ColumnLayout());",
            "  }",
            "}");
    shell.refresh();
    // prepare actions
    List<Object> actions = getSelectionActions();
    // no actions
    assertThat(actions).isEmpty();
  }

  /**
   * Invalid selection.
   */
  public void test_selectionActions_3() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new ColumnLayout());",
            "    Button button = new Button(this, SWT.NONE);",
            "  }",
            "}");
    shell.refresh();
    ControlInfo button = shell.getChildrenControls().get(0);
    // prepare actions
    List<Object> actions = getSelectionActions(shell, button);
    // no actions
    assertThat(actions).isEmpty();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Conversion
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Conversion from {@link ColumnLayout} should keep horizontal alignment.
   */
  public void test_convertFrom() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new ColumnLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      {",
            "        ColumnLayoutData columnLayoutData = new ColumnLayoutData();",
            "        columnLayoutData.horizontalAlignment = ColumnLayoutData.RIGHT;",
            "        button.setLayoutData(columnLayoutData);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    //
    LayoutInfo gridLayout = createJavaInfo("org.eclipse.swt.layout.GridLayout");
    shell.setLayout(gridLayout);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new GridLayout(1, false));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));",
        "    }",
        "  }",
        "}");
  }

  /**
   * Conversion to {@link ColumnLayout} should keep horizontal alignment.
   */
  public void test_convertTo() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new GridLayout(1, false));",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));",
            "    }",
            "  }",
            "}");
    shell.refresh();
    //
    LayoutInfo columnLayout = createJavaInfo("org.eclipse.ui.forms.widgets.ColumnLayout");
    shell.setLayout(columnLayout);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new ColumnLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      {",
        "        ColumnLayoutData columnLayoutData = new ColumnLayoutData();",
        "        columnLayoutData.horizontalAlignment = ColumnLayoutData.RIGHT;",
        "        button.setLayoutData(columnLayoutData);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Source
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String[] getTestSource_decorate(String... lines) {
    lines =
        CodeUtils.join(new String[]{
            "package test;",
            "import org.eclipse.swt.SWT;",
            "import org.eclipse.swt.widgets.*;",
            "import org.eclipse.swt.layout.*;",
            "import org.eclipse.ui.forms.widgets.*;"}, lines);
    return lines;
  }
}