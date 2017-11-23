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

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.menu.IMenuPopupInfo;
import org.eclipse.wb.internal.rcp.model.forms.FormInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.ActionContainerInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.ActionInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.MenuManagerInfo;
import org.eclipse.wb.internal.swt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.RowLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.BTestUtils;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link FormInfo}.
 * 
 * @author scheglov_ke
 */
public class FormTest extends AbstractFormsTest {
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
  public void test_properties() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    Form form = new Form(this, SWT.BORDER);",
            "  }",
            "}");
    shell.refresh();
    FormInfo form = (FormInfo) shell.getChildrenControls().get(0);
    assertNotNull(form.getPropertyByTitle("Style"));
  }

  /**
   * Section has methods "getHead()" and "getBody()", that should be exposed as
   * {@link CompositeInfo}.<br>
   * But there is one important difference: "head" is just "for creating only", you should not set
   * {@link LayoutInfo} for it, you can just use it as parent for creating <em>single</em>
   * {@link ControlInfo}. In contrast "body" is rightful {@link CompositeInfo}, that can have
   * {@link LayoutInfo} and accept many {@link ControlInfo}'s.
   */
  public void test_getHead_getBody() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    Form form = new Form(this, SWT.NONE);",
            "  }",
            "}");
    shell.refresh();
    FormInfo form = (FormInfo) shell.getChildrenControls().get(0);
    // getHead() and getBody() should be exposed
    List<ControlInfo> childrenControls = form.getChildrenControls();
    assertEquals(2, childrenControls.size());
    {
      CompositeInfo head = (CompositeInfo) childrenControls.get(0);
      assertSame(head, form.getHead());
      assertThat(head.toString()).contains("getHead()");
      assertFalse(head.hasLayout());
    }
    {
      CompositeInfo body = (CompositeInfo) childrenControls.get(1);
      assertSame(body, form.getBody());
      assertThat(body.toString()).contains("getBody()");
      assertTrue(body.hasLayout());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Head
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link FormInfo#getHeadClient()}.
   */
  public void test_head_getHeadClient() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    Form form = new Form(this, SWT.NONE);",
            "    {",
            "      Button buttonHead = new Button(form.getHead(), SWT.NONE);",
            "      form.setHeadClient(buttonHead);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    FormInfo form = (FormInfo) shell.getChildrenControls().get(0);
    CompositeInfo head = (CompositeInfo) form.getChildrenControls().get(0);
    // "head" has single child
    List<ControlInfo> headControls = head.getChildrenControls();
    assertEquals(1, headControls.size());
    ControlInfo buttonHead = headControls.get(0);
    assertEquals("buttonHead", buttonHead.getVariableSupport().getName());
    assertSame(buttonHead, form.getHeadClient());
  }

  /**
   * Test for {@link FormInfo#setHeadClient(ControlInfo)}.
   */
  public void test_head_setHeadClient() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    Form form = new Form(this, SWT.NONE);",
            "    form.setEnabled(true);",
            "  }",
            "}");
    shell.refresh();
    FormInfo form = (FormInfo) shell.getChildrenControls().get(0);
    // set "head client"
    ControlInfo button = BTestUtils.createButton();
    form.setHeadClient(button);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    Form form = new Form(this, SWT.NONE);",
        "    form.setEnabled(true);",
        "    {",
        "      Button button = new Button(form.getHead(), SWT.NONE);",
        "      form.setHeadClient(button);",
        "    }",
        "  }",
        "}");
    assertSame(button, form.getHeadClient());
  }

  /**
   * Test for moving "head client" out.
   */
  public void test_head_MoveOut() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    Form form = new Form(this, SWT.NONE);",
            "    {",
            "      Button button = new Button(form.getHead(), SWT.NONE);",
            "      form.setHeadClient(button);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    FormInfo form = (FormInfo) shell.getChildrenControls().get(0);
    ControlInfo button = form.getHeadClient();
    // do move
    RowLayoutInfo rowLayout = (RowLayoutInfo) shell.getLayout();
    rowLayout.command_MOVE(button, null);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    Form form = new Form(this, SWT.NONE);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
    assertNull(form.getHeadClient());
  }

  /**
   * Test for moving "head client" in.
   */
  public void test_head_MoveIn() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    Form form = new Form(this, SWT.NONE);",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    FormInfo form = (FormInfo) shell.getChildrenControls().get(0);
    ControlInfo button = shell.getChildrenControls().get(1);
    // do move
    form.setHeadClient(button);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    Form form = new Form(this, SWT.NONE);",
        "    {",
        "      Button button = new Button(form.getHead(), SWT.NONE);",
        "      form.setHeadClient(button);",
        "    }",
        "  }",
        "}");
    assertSame(button, form.getHeadClient());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Exposed ToolBarManager/MenuManager
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * We should support {@link Form#getToolBarManager()} and {@link Form#getMenuManager()}.
   */
  public void test_exposedManagers_0() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import org.eclipse.jface.action.*;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    Form form = new Form(this, SWT.BORDER);",
            "  }",
            "}");
    shell.refresh();
    // check hierarchy
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(new FillLayout())/ /new Form(this, SWT.BORDER)/}",
        "  {new: org.eclipse.swt.layout.FillLayout} {empty} {/setLayout(new FillLayout())/}",
        "  {new: org.eclipse.ui.forms.widgets.Form} {local-unique: form} {/new Form(this, SWT.BORDER)/}",
        "    {method: public org.eclipse.jface.action.IToolBarManager org.eclipse.ui.forms.widgets.Form.getToolBarManager()} {property} {}",
        "    {method: public org.eclipse.jface.action.IMenuManager org.eclipse.ui.forms.widgets.Form.getMenuManager()} {property} {}",
        "    {method: public org.eclipse.swt.widgets.Composite org.eclipse.ui.forms.widgets.Form.getHead()} {property} {}",
        "    {method: public org.eclipse.swt.widgets.Composite org.eclipse.ui.forms.widgets.Form.getBody()} {property} {}",
        "      {implicit-layout: absolute} {implicit-layout} {}");
  }

  /**
   * We should support {@link Form#getToolBarManager()} and {@link Form#getMenuManager()}.
   */
  public void test_exposedManagers_toolBarManager() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import org.eclipse.jface.action.*;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    Form form = new Form(this, SWT.BORDER);",
            "  }",
            "}");
    shell.refresh();
    FormInfo form = (FormInfo) shell.getChildrenControls().get(0);
    // exposed (and now empty) ToolBarManager should be not very wide, because Form has not so much space
    {
      assertNotNull(form.getToolBarManager());
      Rectangle bounds = form.getToolBarManager().getBounds();
      assertThat(bounds.width).isLessThan(75);
    }
  }

  /**
   * We should support {@link Form#getToolBarManager()} and {@link Form#getMenuManager()}.
   */
  public void test_exposedManagers_menuManager() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import org.eclipse.jface.action.*;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    Form form = new Form(this, SWT.BORDER);",
            "    form.setText('Some text');",
            "  }",
            "}");
    shell.refresh();
    FormInfo form = (FormInfo) shell.getChildrenControls().get(0);
    // exposed MenuManager and its IMenuPopupInfo 
    {
      MenuManagerInfo menuManager = form.getMenuManager();
      IMenuPopupInfo popupInfo = form.getMenuImpl(menuManager);
      assertSame(menuManager, popupInfo.getModel());
      Rectangle bounds = popupInfo.getBounds();
      assertThat(bounds.width).isGreaterThan(10);
      assertThat(bounds.height).isGreaterThan(10);
    }
  }

  /**
   * We should support {@link Form#getToolBarManager()} and {@link Form#getMenuManager()}.
   */
  public void test_exposedManagers_createAction() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import org.eclipse.jface.action.*;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    Form form = new Form(this, SWT.BORDER);",
            "  }",
            "}");
    shell.refresh();
    FormInfo form = (FormInfo) shell.getChildrenControls().get(0);
    // add new Action
    ActionInfo newAction = ActionContainerInfo.createNew(shell);
    form.getToolBarManager().command_CREATE(newAction, null);
    assertEditor(
        "import org.eclipse.jface.action.*;",
        "public class Test extends Shell {",
        "  private Action action;",
        "  public Test() {",
        "    createActions();",
        "    setLayout(new FillLayout());",
        "    Form form = new Form(this, SWT.BORDER);",
        "    form.getToolBarManager().add(action);",
        "  }",
        "  private void createActions() {",
        "    {",
        "      action = new Action('New Action') {",
        "      };",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link FormInfo} should have in context menu action to add/remove
   * {@link FormToolkit#decorateFormHeading(Form)}.
   */
  public void test_FormToolkit_decorateFormHeading() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  private FormToolkit m_toolkit = new FormToolkit(Display.getCurrent());",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    Form form = new Form(this, SWT.BORDER);",
            "  }",
            "}");
    shell.refresh();
    FormInfo form = (FormInfo) shell.getChildrenControls().get(0);
    // decoration: false -> true
    {
      IAction decorateAction = getDecorateAction(form);
      assertFalse(decorateAction.isChecked());
      // decorate := true
      decorateAction.setChecked(true);
      decorateAction.run();
      assertEditor(
          "public class Test extends Shell {",
          "  private FormToolkit m_toolkit = new FormToolkit(Display.getCurrent());",
          "  public Test() {",
          "    setLayout(new FillLayout());",
          "    Form form = new Form(this, SWT.BORDER);",
          "    m_toolkit.decorateFormHeading(form);",
          "  }",
          "}");
    }
    // decoration: true -> false
    {
      IAction decorateAction = getDecorateAction(form);
      assertTrue(decorateAction.isChecked());
      // decorate := false
      decorateAction.setChecked(false);
      decorateAction.run();
      assertEditor(
          "public class Test extends Shell {",
          "  private FormToolkit m_toolkit = new FormToolkit(Display.getCurrent());",
          "  public Test() {",
          "    setLayout(new FillLayout());",
          "    Form form = new Form(this, SWT.BORDER);",
          "  }",
          "}");
    }
  }

  private IAction getDecorateAction(FormInfo form) throws Exception {
    IMenuManager manager = getDesignerMenuManager();
    form.getBroadcastObject().addContextMenu(ImmutableList.of(form), form, manager);
    IAction decorateAction = findChildAction(manager, "Decorate heading");
    assertNotNull(decorateAction);
    return decorateAction;
  }
}