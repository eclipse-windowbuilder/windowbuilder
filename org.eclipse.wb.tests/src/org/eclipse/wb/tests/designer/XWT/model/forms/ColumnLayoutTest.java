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
package org.eclipse.wb.tests.designer.XWT.model.forms;

import org.eclipse.wb.internal.xwt.model.forms.layout.column.ColumnLayoutDataInfo;
import org.eclipse.wb.internal.xwt.model.forms.layout.column.ColumnLayoutInfo;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

import org.eclipse.jface.action.IAction;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link ColumnLayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class ColumnLayoutTest extends XwtModelTest {
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
  public void test_isHorizontal() throws Exception {
    parse(
        "<!-- Forms API -->",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:ColumnLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "</Shell>");
    refresh();
    //
    ColumnLayoutInfo layout = getObjectByName("layout");
    assertTrue(layout.isHorizontal());
  }

  public void test_LayoutData_implicit() throws Exception {
    parse(
        "<!-- Forms API -->",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:ColumnLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <f:ColumnLayout>",
        "  <Button wbp:name='button'>",
        "    virtual-LayoutData: org.eclipse.ui.forms.widgets.ColumnLayoutData");
    refresh();
    //
    ControlInfo button = getObjectByName("button");
    ColumnLayoutDataInfo columnData = ColumnLayoutInfo.getColumnData(button);
    columnData.setWidthHint(100);
    columnData.setHeightHint(200);
    assertXML(
        "<!-- Forms API -->",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:ColumnLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'>",
        "    <Button.layoutData>",
        "      <f:ColumnLayoutData widthHint='100' heightHint='200'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
  }

  public void test_copyPaste() throws Exception {
    final CompositeInfo shell =
        parse(
            "<Shell>",
            "  <Shell.layout>",
            "    <FillLayout/>",
            "  </Shell.layout>",
            "  <Composite wbp:name='composite'>",
            "    <Composite.layout>",
            "      <f:ColumnLayout/>",
            "    </Composite.layout>",
            "    <Button>",
            "      <Button.layoutData>",
            "        <f:ColumnLayoutData widthHint='100' heightHint='200'/>",
            "      </Button.layoutData>",
            "    </Button>",
            "  </Composite>",
            "</Shell>");
    refresh();
    // do copy/paste
    {
      CompositeInfo composite = getObjectByName("composite");
      doCopyPaste(composite, new PasteProcedure<ControlInfo>() {
        public void run(ControlInfo copy) throws Exception {
          shell.getLayout().command_CREATE(copy, null);
        }
      });
    }
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <Composite wbp:name='composite'>",
        "    <Composite.layout>",
        "      <f:ColumnLayout/>",
        "    </Composite.layout>",
        "    <Button>",
        "      <Button.layoutData>",
        "        <f:ColumnLayoutData widthHint='100' heightHint='200'/>",
        "      </Button.layoutData>",
        "    </Button>",
        "  </Composite>",
        "  <Composite>",
        "    <Composite.layout>",
        "      <f:ColumnLayout/>",
        "    </Composite.layout>",
        "    <Button>",
        "      <Button.layoutData>",
        "        <f:ColumnLayoutData heightHint='200' widthHint='100'/>",
        "      </Button.layoutData>",
        "    </Button>",
        "  </Composite>",
        "</Shell>");
  }

  public void test_selectionActions_1() throws Exception {
    parse(
        "<!-- Forms API -->",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:ColumnLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    refresh();
    ControlInfo button = getObjectByName("button");
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
      assertXML(
          "<!-- Forms API -->",
          "<Shell>",
          "  <Shell.layout>",
          "    <f:ColumnLayout/>",
          "  </Shell.layout>",
          "  <Button wbp:name='button'>",
          "    <Button.layoutData>",
          "      <f:ColumnLayoutData horizontalAlignment='(org.eclipse.ui.forms.widgets.ColumnLayoutData).LEFT'/>",
          "    </Button.layoutData>",
          "  </Button>",
          "</Shell>");
    }
  }

  /**
   * No selection.
   */
  public void test_selectionActions_2() throws Exception {
    parse(
        "<!-- Forms API -->",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:ColumnLayout/>",
        "  </Shell.layout>",
        "</Shell>");
    refresh();
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
        parse(
            "<!-- Forms API -->",
            "<Shell>",
            "  <Shell.layout>",
            "    <f:ColumnLayout/>",
            "  </Shell.layout>",
            "  <Button wbp:name='button'/>",
            "</Shell>");
    refresh();
    ControlInfo button = getObjectByName("button");
    // prepare actions
    List<Object> actions = getSelectionActions(shell, button);
    // no actions
    assertThat(actions).isEmpty();
  }
}