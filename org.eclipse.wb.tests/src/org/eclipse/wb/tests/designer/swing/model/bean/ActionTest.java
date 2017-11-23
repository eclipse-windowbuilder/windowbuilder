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
package org.eclipse.wb.tests.designer.swing.model.bean;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.eclipse.wb.core.editor.palette.PaletteEventListener;
import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.ImplicitFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.ConstructorDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.generation.statement.lazy.LazyStatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.variable.description.LazyVariableDescription;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.swing.IExceptionConstants;
import org.eclipse.wb.internal.swing.model.bean.ActionContainerInfo;
import org.eclipse.wb.internal.swing.model.bean.ActionExpressionAccessor;
import org.eclipse.wb.internal.swing.model.bean.ActionInfo;
import org.eclipse.wb.internal.swing.model.bean.ActionInnerCreationSupport;
import org.eclipse.wb.internal.swing.model.bean.IActionSupport;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.component.JToolBarInfo;
import org.eclipse.wb.internal.swing.palette.ActionUseEntryInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;
import org.eclipse.wb.tests.designer.swing.SwingTestUtils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.graphics.Image;

import org.assertj.core.api.Assertions;

import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToolBar;

/**
 * Test for {@link ActionInfo}.
 * 
 * @author scheglov_ke
 */
public class ActionTest extends SwingModelTest {
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
  public void test_noDesign_forActions() throws Exception {
    try {
      parseJavaInfo(
          "public class Test extends AbstractAction {",
          "  public Test() {",
          "    putValue(NAME, 'My name');",
          "    putValue(SHORT_DESCRIPTION, 'My short description');",
          "  }",
          "  public void actionPerformed(ActionEvent e) {",
          "  }",
          "}");
      fail();
    } catch (DesignerException e) {
      assertEquals(IExceptionConstants.NO_DESIGN_ACTION, e.getCode());
    }
  }

  /**
   * No {@link AbstractAction}'s - no {@link ActionContainerInfo} and {@link ActionInfo}'s.
   */
  public void test_noActions() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    // no ActionContainerInfo and ActionInfo's
    assertEquals(0, panel.getChildren(ActionContainerInfo.class).size());
    assertEquals(0, ActionContainerInfo.getActions(panel).size());
    // still no ActionContainerInfo
    assertEquals(0, panel.getChildren(ActionContainerInfo.class).size());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // External
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Create {@link CompilationUnit} with external {@link Action}.
   */
  private void createExternalAction() throws Exception {
    setFileContentSrc(
        "test/ExternalAction.java",
        getTestSource(
            "public class ExternalAction extends AbstractAction {",
            "  public ExternalAction() {",
            "    putValue(NAME, 'My name');",
            "    putValue(SHORT_DESCRIPTION, 'My short description');",
            "  }",
            "  public void actionPerformed(ActionEvent e) {",
            "  }",
            "}"));
    waitForAutoBuild();
  }

  /**
   * Test for external {@link Action}'s, in separate top-level class.<br>
   * Test that we can parse {@link Action} creation.
   */
  public void test_external_parse() throws Exception {
    createExternalAction();
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private ExternalAction action = new ExternalAction();",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    add(button);",
            "    button.setAction(action);",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
    // test ActionContainerInfo
    {
      ActionContainerInfo container = ActionContainerInfo.get(panel);
      assertNotNull(container);
      // ActionContainerInfo can not be deleted
      assertFalse(container.canDelete());
      container.delete();
      // presentation
      assertEquals("(actions)", container.getPresentation().getText());
      assertNotNull(container.getPresentation().getIcon());
      // there is Action model
      assertEquals(1, container.getChildren().size());
    }
    // test Action
    ActionInfo action;
    {
      List<ActionInfo> actions = ActionContainerInfo.getActions(panel);
      assertEquals(1, actions.size());
      action = actions.get(0);
      assertNotNull(action.getAssociation());
      // check values
      Action actionObject = (Action) action.getObject();
      assertEquals("My name", actionObject.getValue(Action.NAME));
      assertEquals("My short description", actionObject.getValue(Action.SHORT_DESCRIPTION));
    }
    // test that Action is bound to JButton
    {
      ComponentInfo button = panel.getChildrenComponents().get(0);
      JButton buttonObject = (JButton) button.getObject();
      // Action is bound to JButton
      assertSame(action.getObject(), buttonObject.getAction());
      // ...and this means that JButton has corresponding text/tooltip
      assertEquals("My name", buttonObject.getText());
    }
  }

  /**
   * Test for external {@link Action}'s, in separate top-level class.<br>
   * Test that when we set property for Action, it is set in beginning of method, not mixed with
   * {@link JButton}.
   */
  public void test_external_setProperty() throws Exception {
    createExternalAction();
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private ExternalAction action = new ExternalAction();",
            "  public Test() {",
            "    {",
            "      JButton button = new JButton();",
            "      add(button);",
            "      button.setAction(action);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
    // test Action
    ActionInfo action = ActionContainerInfo.getActions(panel).get(0);
    action.getPropertyByTitle("enabled").setValue(Boolean.FALSE);
    assertEditor(
        "public class Test extends JPanel {",
        "  private ExternalAction action = new ExternalAction();",
        "  public Test() {",
        "    action.setEnabled(false);",
        "    {",
        "      JButton button = new JButton();",
        "      add(button);",
        "      button.setAction(action);",
        "    }",
        "  }",
        "}");
    // no putValue() properties
    assertNull(action.getPropertyByTitle("name"));
  }

  /**
   * Test for new instance of external {@link ActionInfo}.
   * <p>
   * We should support "lazy" code generation.
   */
  public void test_external_new_lazy() throws Exception {
    createExternalAction();
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
    // no Action's yet
    assertTrue(ActionContainerInfo.getActions(panel).isEmpty());
    // add new ActionInfo
    ActionInfo newAction;
    {
      newAction =
          (ActionInfo) JavaInfoUtils.createJavaInfo(
              m_lastEditor,
              m_lastLoader.loadClass("test.ExternalAction"),
              new ConstructorCreationSupport());
      SwingTestUtils.setGenerations(
          LazyVariableDescription.INSTANCE,
          LazyStatementGeneratorDescription.INSTANCE);
      ActionContainerInfo.add(panel, newAction);
    }
    // check
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  private ExternalAction externalAction;",
        "  public Test() {",
        "  }",
        "  private ExternalAction getExternalAction() {",
        "    if (externalAction == null) {",
        "      externalAction = new ExternalAction();",
        "    }",
        "    return externalAction;",
        "  }",
        "}");
    Assertions.assertThat(ActionContainerInfo.getActions(panel)).containsOnly(newAction);
    // refresh
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * Test for new instance of external {@link ActionInfo}.
   */
  public void test_external_new() throws Exception {
    createExternalAction();
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
    // no Action's yet
    assertTrue(ActionContainerInfo.getActions(panel).isEmpty());
    // add new ActionInfo
    ActionInfo newAction;
    {
      newAction =
          (ActionInfo) JavaInfoUtils.createJavaInfo(
              m_lastEditor,
              m_lastLoader.loadClass("test.ExternalAction"),
              new ConstructorCreationSupport());
      ActionContainerInfo.add(panel, newAction);
    }
    // check
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  private final ExternalAction externalAction = new ExternalAction();",
        "  public Test() {",
        "  }",
        "}");
    Assertions.assertThat(ActionContainerInfo.getActions(panel)).containsOnly(newAction);
  }

  /**
   * Add new {@link ActionInfo} using {@link JToolBar#add(Action)}.
   */
  public void test_external_addOnJToolBar() throws Exception {
    createExternalAction();
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JToolBar bar = new JToolBar();",
            "    add(bar);",
            "  }",
            "}");
    JToolBarInfo bar = (JToolBarInfo) panel.getChildrenComponents().get(0);
    // prepare new ActionInfo
    ActionInfo newAction =
        (ActionInfo) JavaInfoUtils.createJavaInfo(
            m_lastEditor,
            m_lastLoader.loadClass("test.ExternalAction"),
            new ConstructorCreationSupport());
    // add new JButton using ActionInfo
    ComponentInfo newButton = bar.command_CREATE(newAction, null);
    assertEditor(
        "class Test extends JPanel {",
        "  private final ExternalAction externalAction = new ExternalAction();",
        "  Test() {",
        "    JToolBar bar = new JToolBar();",
        "    add(bar);",
        "    {",
        "      JButton button = bar.add(externalAction);",
        "    }",
        "  }",
        "}");
    // ImplicitFactoryCreationSupport
    {
      ImplicitFactoryCreationSupport creationSupport =
          (ImplicitFactoryCreationSupport) newButton.getCreationSupport();
      assertEquals("bar.add(externalAction)", m_lastEditor.getSource(creationSupport.getNode()));
    }
  }

  /**
   * Set new {@link ActionInfo} on {@link JButton}.
   */
  public void test_external_setOnJButton() throws Exception {
    createExternalAction();
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JButton button = new JButton();",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // prepare new ActionInfo
    ActionInfo newAction =
        (ActionInfo) JavaInfoUtils.createJavaInfo(
            m_lastEditor,
            m_lastLoader.loadClass("test.ExternalAction"),
            new ConstructorCreationSupport());
    // add new JButton using ActionInfo
    ActionInfo.setAction(button, newAction);
    assertEditor(
        "class Test extends JPanel {",
        "  private final ExternalAction externalAction = new ExternalAction();",
        "  Test() {",
        "    JButton button = new JButton();",
        "    button.setAction(externalAction);",
        "    add(button);",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Attached and not
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for external {@link Action}'s not attached to any {@link AbstractButton}, in "lazy"
   * pattern.
   */
  public void test_notAttached_lazy() throws Exception {
    createExternalAction();
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private ExternalAction externalAction;",
            "  public Test() {",
            "  }",
            "  private ExternalAction getExternalAction() {",
            "    if (externalAction == null) {",
            "      externalAction = new ExternalAction();",
            "    }",
            "    return externalAction;",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {org.eclipse.wb.internal.swing.model.bean.ActionContainerInfo}",
        "    {new: test.ExternalAction} {lazy: externalAction getExternalAction()} {/new ExternalAction()/ /externalAction/}");
    //
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * Test for external {@link Action}'s attached to {@link JButton}, in "lazy" pattern.
   */
  public void test_attached_lazy() throws Exception {
    createExternalAction();
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private ExternalAction externalAction;",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    add(button);",
            "    button.setAction(getExternalAction());",
            "  }",
            "  private ExternalAction getExternalAction() {",
            "    if (externalAction == null) {",
            "      externalAction = new ExternalAction();",
            "    }",
            "    return externalAction;",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(button)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /add(button)/ /button.setAction(getExternalAction())/}",
        "  {org.eclipse.wb.internal.swing.model.bean.ActionContainerInfo}",
        "    {new: test.ExternalAction} {lazy: externalAction getExternalAction()} {/new ExternalAction()/ /externalAction/ /button.setAction(getExternalAction())/}");
    // first refresh()
    panel.refresh();
    assertNoErrors(panel);
    // delete Action
    ActionContainerInfo.getActions(panel).get(0).delete();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JButton button = new JButton();",
        "    add(button);",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(button)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /add(button)/}",
        "  {org.eclipse.wb.internal.swing.model.bean.ActionContainerInfo}");
  }

  /**
   * Test for external {@link Action}'s not attached to any {@link AbstractButton}.
   */
  public void test_notAttached_fieldInitializer() throws Exception {
    createExternalAction();
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private ExternalAction action = new ExternalAction();",
            "  public Test() {",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {org.eclipse.wb.internal.swing.model.bean.ActionContainerInfo}",
        "    {new: test.ExternalAction} {field-initializer: action} {/new ExternalAction()/}");
    //
    panel.refresh();
    assertNoErrors(panel);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link ActionInfo} contributes itself to <code>org.eclipse.wb.internal.swing.actions</code>
   * {@link CategoryInfo}.
   */
  public void test_contributeToPalette() throws Exception {
    createExternalAction();
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private ExternalAction action = new ExternalAction();",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
    // prepare category/entries
    CategoryInfo category = new CategoryInfo();
    category.setId("org.eclipse.wb.internal.swing.actions");
    List<EntryInfo> entries = Lists.newArrayList();
    // send palette broadcast
    PaletteEventListener listener = panel.getBroadcast(PaletteEventListener.class);
    listener.entries(category, entries);
    // we should have exactly one entry
    assertEquals(1, entries.size());
    assertInstanceOf(ActionUseEntryInfo.class, entries.get(0));
  }

  /**
   * Test for {@link ActionInfo#setAction(ComponentInfo, ActionInfo)}.
   */
  public void test_setAction() throws Exception {
    createExternalAction();
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private ExternalAction action_1 = new ExternalAction();",
            "  private ExternalAction action_2 = new ExternalAction();",
            "  public Test() {",
            "    {",
            "      JButton button = new JButton();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
    // prepare components
    ComponentInfo button = panel.getChildrenComponents().get(0);
    ActionInfo action_1 = ActionContainerInfo.getActions(panel).get(0);
    ActionInfo action_2 = ActionContainerInfo.getActions(panel).get(1);
    // set "action_1"
    ActionInfo.setAction(button, action_1);
    assertEditor(
        "public class Test extends JPanel {",
        "  private ExternalAction action_1 = new ExternalAction();",
        "  private ExternalAction action_2 = new ExternalAction();",
        "  public Test() {",
        "    {",
        "      JButton button = new JButton();",
        "      button.setAction(action_1);",
        "      add(button);",
        "    }",
        "  }",
        "}");
    // set "action_2"
    ActionInfo.setAction(button, action_2);
    assertEditor(
        "public class Test extends JPanel {",
        "  private ExternalAction action_1 = new ExternalAction();",
        "  private ExternalAction action_2 = new ExternalAction();",
        "  public Test() {",
        "    {",
        "      JButton button = new JButton();",
        "      button.setAction(action_2);",
        "      add(button);",
        "    }",
        "  }",
        "}");
    // unset Action
    ActionInfo.setAction(button, null);
    assertEditor(
        "public class Test extends JPanel {",
        "  private ExternalAction action_1 = new ExternalAction();",
        "  private ExternalAction action_2 = new ExternalAction();",
        "  public Test() {",
        "    {",
        "      JButton button = new JButton();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Factory
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for creating {@link Action} using static factory.
   */
  public void test_factory() throws Exception {
    prepare_ActionFactory();
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private AbstractAction action = ActionFactory.createAction();",
            "  public Test() {",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {org.eclipse.wb.internal.swing.model.bean.ActionContainerInfo}",
        "    {static factory: test.ActionFactory createAction()} {field-initializer: action} {/ActionFactory.createAction()/}");
    //
    panel.refresh();
    assertNoErrors(panel);
  }

  private void prepare_ActionFactory() throws Exception {
    setFileContentSrc(
        "test/ActionFactory.java",
        getTestSource(
            "public class ActionFactory {",
            "  public static AbstractAction createAction() {",
            "    return new AbstractAction() {",
            "      public void actionPerformed(ActionEvent e) {",
            "      }",
            "    };",
            "  }",
            "}"));
    waitForAutoBuild();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Inner
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that we can parse inner-class {@link AbstractAction}.
   */
  public void test_inner_parse() throws Exception {
    m_waitForAutoBuild = true;
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private class Action_1 extends AbstractAction {",
            "    public Action_1() {",
            "      putValue(NAME, 'My name');",
            "      putValue(SHORT_DESCRIPTION, 'My short description');",
            "    }",
            "    public void actionPerformed(ActionEvent e) {",
            "    }",
            "  }",
            "  private Action_1 action_1 = new Action_1();",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {org.eclipse.wb.internal.swing.model.bean.ActionContainerInfo}",
        "    {innerAction} {field-initializer: action_1} {/new Action_1()/}");
    // test ActionContainerInfo
    {
      ActionContainerInfo container = ActionContainerInfo.get(panel);
      assertNotNull(container);
      // ActionContainerInfo can not be deleted
      assertFalse(container.canDelete());
      container.delete();
      // presentation
      assertEquals("(actions)", container.getPresentation().getText());
      assertNotNull(container.getPresentation().getIcon());
      // there is Action model
      assertEquals(1, container.getChildren().size());
    }
    // test for Action's
    {
      List<ActionInfo> actions = ActionContainerInfo.getActions(panel);
      assertEquals(1, actions.size());
      ActionInfo actionInfo = actions.get(0);
      // check values
      {
        Action action = (Action) actionInfo.getObject();
        assertEquals("My name", action.getValue(Action.NAME));
        assertEquals("My short description", action.getValue(Action.SHORT_DESCRIPTION));
      }
      // check ActionInnerCreationSupport
      ActionInnerCreationSupport creationSupport =
          (ActionInnerCreationSupport) actionInfo.getCreationSupport();
      assertEquals("new Action_1()", m_lastEditor.getSource(creationSupport.getNode()));
      assertFalse(creationSupport.canReorder());
      assertFalse(creationSupport.canReparent());
    }
  }

  /**
   * Test that we can parse inner-class {@link AbstractAction}.
   * <p>
   * {@link StringLiteral} as key in {@link Action#putValue(String, Object)} invocation.
   */
  public void test_inner_parse2() throws Exception {
    m_waitForAutoBuild = true;
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private class Action_1 extends AbstractAction {",
            "    public Action_1() {",
            "      putValue('Name', 'My name');",
            "    }",
            "    public void actionPerformed(ActionEvent e) {",
            "    }",
            "  }",
            "  private Action_1 action_1 = new Action_1();",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
    // test Action
    ActionInfo actionInfo = ActionContainerInfo.getActions(panel).get(0);
    // check values
    {
      Action action = (Action) actionInfo.getObject();
      assertEquals("My name", action.getValue(Action.NAME));
    }
  }

  /**
   * Test that we can parse inner-class {@link AbstractAction}.
   * <p>
   * {@link QualifiedName} as key in {@link Action#putValue(String, Object)} invocation.
   */
  public void test_inner_parse3() throws Exception {
    m_waitForAutoBuild = true;
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private class Action_1 extends AbstractAction {",
            "    public Action_1() {",
            "      putValue(Action.NAME, 'My name');",
            "    }",
            "    public void actionPerformed(ActionEvent e) {",
            "    }",
            "  }",
            "  private Action_1 action_1 = new Action_1();",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
    // test Action
    ActionInfo actionInfo = ActionContainerInfo.getActions(panel).get(0);
    // check values
    {
      Action action = (Action) actionInfo.getObject();
      assertEquals("My name", action.getValue(Action.NAME));
    }
    // check "name" property
    {
      Property nameProperty = actionInfo.getPropertyByTitle("name");
      assertTrue(nameProperty.isModified());
      assertEquals("My name", nameProperty.getValue());
    }
  }

  /**
   * Test that we can parse inner-class {@link AbstractAction}.
   * <p>
   * No constructor, so no properties to apply.
   */
  public void test_inner_parse_noConstructor() throws Exception {
    m_waitForAutoBuild = true;
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private class Action_1 extends AbstractAction {",
            "    public void actionPerformed(ActionEvent e) {",
            "    }",
            "  }",
            "  private Action_1 action_1 = new Action_1();",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
    // no properties
    ActionInfo action = getJavaInfoByName("action_1");
    assertNull(action.getPropertyByTitle("name"));
  }

  /**
   * Test for new instance of inner {@link ActionInfo}.
   */
  public void test_inner_new() throws Exception {
    m_waitForAutoBuild = true;
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
    // no Action's yet
    assertTrue(ActionContainerInfo.getActions(panel).isEmpty());
    // add new ActionInfo
    ActionInfo newAction;
    {
      newAction = ActionInfo.createInner(m_lastEditor);
      ActionContainerInfo.add(panel, newAction);
      // note, this refresh() means that inner Action can be used only after refresh()
      panel.refresh();
    }
    // check
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  private final Action action = new SwingAction();",
        "  public Test() {",
        "  }",
        "  private class SwingAction extends AbstractAction {",
        "    public SwingAction() {",
        "      putValue(NAME, 'SwingAction');",
        "      putValue(SHORT_DESCRIPTION, 'Some short description');",
        "    }",
        "    public void actionPerformed(ActionEvent e) {",
        "    }",
        "  }",
        "}");
    Assertions.assertThat(ActionContainerInfo.getActions(panel)).containsOnly(newAction);
    // try to use getClass() - there was bug http://mail.google.com/mail/#inbox/117fbef0d63f30e3
    {
      // prepare Action constructor
      MethodDeclaration constructor;
      {
        TypeDeclaration mainType =
            AstNodeUtils.getEnclosingType(panel.getCreationSupport().getNode());
        TypeDeclaration actionType = mainType.getTypes()[0];
        constructor = actionType.getMethods()[0];
      }
      //
      ActionExpressionAccessor accessor =
          new ActionExpressionAccessor(new LazyActionSupport(constructor), "NAME");
      accessor.setExpression(panel, "{wbp_classTop}.getName()");
      assertEditor(
          "// filler filler filler",
          "public class Test extends JPanel {",
          "  private final Action action = new SwingAction();",
          "  public Test() {",
          "  }",
          "  private class SwingAction extends AbstractAction {",
          "    public SwingAction() {",
          "      putValue(NAME, Test.class.getName());",
          "      putValue(SHORT_DESCRIPTION, 'Some short description');",
          "    }",
          "    public void actionPerformed(ActionEvent e) {",
          "    }",
          "  }",
          "}");
    }
  }

  /**
   * Test for new instance of inner {@link ActionInfo}.<br>
   * There is already other inner {@link Action}, so new one should be added after existing.
   */
  public void test_inner_new2() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private final Action action = new SwingAction();",
            "  public Test() {",
            "  }",
            "  private class SwingAction extends AbstractAction {",
            "    public SwingAction() {",
            "    }",
            "    public void actionPerformed(ActionEvent e) {",
            "    }",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
    // add new ActionInfo
    ActionInfo newAction;
    {
      newAction = ActionInfo.createInner(m_lastEditor);
      ActionContainerInfo.add(panel, newAction);
    }
    // check
    assertEditor(
        "public class Test extends JPanel {",
        "  private final Action action = new SwingAction();",
        "  private final Action action_1 = new SwingAction_1();",
        "  public Test() {",
        "  }",
        "  private class SwingAction extends AbstractAction {",
        "    public SwingAction() {",
        "    }",
        "    public void actionPerformed(ActionEvent e) {",
        "    }",
        "  }",
        "  private class SwingAction_1 extends AbstractAction {",
        "    public SwingAction_1() {",
        "      putValue(NAME, 'SwingAction_1');",
        "      putValue(SHORT_DESCRIPTION, 'Some short description');",
        "    }",
        "    public void actionPerformed(ActionEvent e) {",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test that we can delete inner-class {@link AbstractAction}.
   */
  public void test_inner_delete() throws Exception {
    m_waitForAutoBuild = true;
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private class Action_1 extends AbstractAction {",
            "    public Action_1() {",
            "    }",
            "    public void actionPerformed(ActionEvent e) {",
            "    }",
            "  }",
            "  private Action_1 action_1 = new Action_1();",
            "  public Test() {",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {org.eclipse.wb.internal.swing.model.bean.ActionContainerInfo}",
        "    {innerAction} {field-initializer: action_1} {/new Action_1()/}");
    //
    ActionInfo action = ActionContainerInfo.getActions(panel).get(0);
    assertTrue(action.canDelete());
    action.delete();
    String[] lines =
        {
            "public class Test extends JPanel {",
            "  private class Action_1 extends AbstractAction {",
            "    public Action_1() {",
            "    }",
            "    public void actionPerformed(ActionEvent e) {",
            "    }",
            "  }",
            "  public Test() {",
            "  }",
            "}"};
    assertEditor(lines);
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {org.eclipse.wb.internal.swing.model.bean.ActionContainerInfo}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Inner properties
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ActionExpressionAccessor}.
   */
  public void test_inner_ActionExpressionAccessor_1() throws Exception {
    m_waitForAutoBuild = true;
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private class Action_1 extends AbstractAction {",
            "    public Action_1() {",
            "      putValue(NAME, 'My name');",
            "      putValue('ShortDescription', 'My short description');",
            "    }",
            "    public void actionPerformed(ActionEvent e) {",
            "    }",
            "  }",
            "  private Action_1 action_1 = new Action_1();",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    ActionInfo action = ActionContainerInfo.getActions(panel).get(0);
    // prepare Action_1 constructor
    MethodDeclaration constructor;
    {
      TypeDeclaration mainType =
          AstNodeUtils.getEnclosingType(panel.getCreationSupport().getNode());
      TypeDeclaration actionType = mainType.getTypes()[0];
      constructor = actionType.getMethods()[0];
    }
    // getExpression()
    {
      // can find existing "NAME"
      {
        ActionExpressionAccessor accessor =
            new ActionExpressionAccessor(new LazyActionSupport(constructor), "NAME");
        assertEquals("\"My name\"", m_lastEditor.getSource(accessor.getExpression(action)));
      }
      // "SHORT_DESCRIPTION" is inlined as StringLiteral, but can be found
      {
        ActionExpressionAccessor accessor =
            new ActionExpressionAccessor(new LazyActionSupport(constructor), "SHORT_DESCRIPTION");
        assertEquals(
            "\"My short description\"",
            m_lastEditor.getSource(accessor.getExpression(action)));
      }
      // no "LONG_DESCRIPTION" - no expression
      {
        ActionExpressionAccessor accessor =
            new ActionExpressionAccessor(new LazyActionSupport(constructor), "LONG_DESCRIPTION");
        assertNull(accessor.getExpression(action));
      }
    }
    // setExpression()
    {
      // replace value for existing "NAME"
      {
        ActionExpressionAccessor accessor =
            new ActionExpressionAccessor(new LazyActionSupport(constructor), "NAME");
        accessor.setExpression(action, "\"New name\"");
        assertEditor(
            "public class Test extends JPanel {",
            "  private class Action_1 extends AbstractAction {",
            "    public Action_1() {",
            "      putValue(NAME, 'New name');",
            "      putValue('ShortDescription', 'My short description');",
            "    }",
            "    public void actionPerformed(ActionEvent e) {",
            "    }",
            "  }",
            "  private Action_1 action_1 = new Action_1();",
            "  public Test() {",
            "  }",
            "}");
      }
      // remove value of existing "NAME"
      {
        ActionExpressionAccessor accessor =
            new ActionExpressionAccessor(new LazyActionSupport(constructor), "NAME");
        accessor.setExpression(action, null);
        assertEditor(
            "public class Test extends JPanel {",
            "  private class Action_1 extends AbstractAction {",
            "    public Action_1() {",
            "      putValue('ShortDescription', 'My short description');",
            "    }",
            "    public void actionPerformed(ActionEvent e) {",
            "    }",
            "  }",
            "  private Action_1 action_1 = new Action_1();",
            "  public Test() {",
            "  }",
            "}");
      }
      // set value for not existing yet "SMALL_ICON"
      {
        ActionExpressionAccessor accessor =
            new ActionExpressionAccessor(new LazyActionSupport(constructor), "SMALL_ICON");
        accessor.setExpression(action, "null");
        assertEditor(
            "public class Test extends JPanel {",
            "  private class Action_1 extends AbstractAction {",
            "    public Action_1() {",
            "      putValue(SMALL_ICON, null);",
            "      putValue('ShortDescription', 'My short description');",
            "    }",
            "    public void actionPerformed(ActionEvent e) {",
            "    }",
            "  }",
            "  private Action_1 action_1 = new Action_1();",
            "  public Test() {",
            "  }",
            "}");
      }
    }
  }

  /**
   * Test for {@link ActionExpressionAccessor}.<br>
   * Expression in {@link SuperConstructorInvocation}.
   */
  public void test_inner_ActionExpressionAccessor_2() throws Exception {
    m_waitForAutoBuild = true;
    final ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private class Action_1 extends AbstractAction {",
            "    public Action_1() {",
            "      super('My name');",
            "    }",
            "    public void actionPerformed(ActionEvent e) {",
            "    }",
            "  }",
            "  private Action_1 action_1 = new Action_1();",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    ActionInfo action = ActionContainerInfo.getActions(panel).get(0);
    // check that "Name" is applied to Action object
    {
      Action actionObject = (Action) action.getObject();
      assertEquals("My name", actionObject.getValue(Action.NAME));
    }
    // prepare Action_1 constructor
    MethodDeclaration constructor;
    {
      TypeDeclaration mainType =
          AstNodeUtils.getEnclosingType(panel.getCreationSupport().getNode());
      TypeDeclaration actionType = mainType.getTypes()[0];
      constructor = actionType.getMethods()[0];
    }
    // getExpression()
    {
      // can find existing "NAME"
      {
        ActionExpressionAccessor accessor =
            new ActionExpressionAccessor(new LazyActionSupport(constructor) {
              @Override
              public ConstructorDescription getConstructorDescription() throws Exception {
                ComponentDescription description =
                    ComponentDescriptionHelper.getDescription(
                        panel.getEditor(),
                        AbstractAction.class);
                return description.getConstructor("<init>(java.lang.String)");
              }
            }, "NAME");
        assertEquals("\"My name\"", m_lastEditor.getSource(accessor.getExpression(action)));
      }
    }
    // setExpression()
    {
      // replace value for existing "NAME"
      {
        ActionExpressionAccessor accessor =
            new ActionExpressionAccessor(new LazyActionSupport(constructor) {
              @Override
              public ConstructorDescription getConstructorDescription() throws Exception {
                ComponentDescription description =
                    ComponentDescriptionHelper.getDescription(
                        panel.getEditor(),
                        AbstractAction.class);
                return description.getConstructor("<init>(java.lang.String)");
              }
            }, "NAME");
        accessor.setExpression(action, "\"New name\"");
        assertEditor(
            "public class Test extends JPanel {",
            "  private class Action_1 extends AbstractAction {",
            "    public Action_1() {",
            "      super('New name');",
            "    }",
            "    public void actionPerformed(ActionEvent e) {",
            "    }",
            "  }",
            "  private Action_1 action_1 = new Action_1();",
            "  public Test() {",
            "  }",
            "}");
      }
      // remove value of existing "NAME"
      {
        ActionExpressionAccessor accessor =
            new ActionExpressionAccessor(new LazyActionSupport(constructor), "NAME");
        accessor.setExpression(action, null);
        assertEditor(
            "public class Test extends JPanel {",
            "  private class Action_1 extends AbstractAction {",
            "    public Action_1() {",
            "      super('New name');",
            "    }",
            "    public void actionPerformed(ActionEvent e) {",
            "    }",
            "  }",
            "  private Action_1 action_1 = new Action_1();",
            "  public Test() {",
            "  }",
            "}");
      }
      // set value for not existing yet "SMALL_ICON"
      {
        ActionExpressionAccessor accessor =
            new ActionExpressionAccessor(new LazyActionSupport(constructor), "SMALL_ICON");
        accessor.setExpression(action, "null");
        assertEditor(
            "public class Test extends JPanel {",
            "  private class Action_1 extends AbstractAction {",
            "    public Action_1() {",
            "      super('New name');",
            "      putValue(SMALL_ICON, null);",
            "    }",
            "    public void actionPerformed(ActionEvent e) {",
            "    }",
            "  }",
            "  private Action_1 action_1 = new Action_1();",
            "  public Test() {",
            "  }",
            "}");
      }
    }
  }

  /**
   * Test for {@link Property}'s.
   * <p>
   * Property that is set using {@link Action#putValue(String, Object)}.
   */
  public void test_inner_properties_putValue() throws Exception {
    m_waitForAutoBuild = true;
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private class Action_1 extends AbstractAction {",
            "    public Action_1() {",
            "      putValue(NAME, 'My name');",
            "    }",
            "    public void actionPerformed(ActionEvent e) {",
            "    }",
            "  }",
            "  private Action_1 action_1 = new Action_1();",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    ActionInfo action = ActionContainerInfo.getActions(panel).get(0);
    // check "name" property
    Property nameProperty = action.getPropertyByTitle("name");
    assertEquals("My name", nameProperty.getValue());
    nameProperty.setValue("New name");
    assertEditor(
        "public class Test extends JPanel {",
        "  private class Action_1 extends AbstractAction {",
        "    public Action_1() {",
        "      putValue(NAME, 'New name');",
        "    }",
        "    public void actionPerformed(ActionEvent e) {",
        "    }",
        "  }",
        "  private Action_1 action_1 = new Action_1();",
        "  public Test() {",
        "  }",
        "}");
  }

  /**
   * Test for {@link Property}'s.
   * <p>
   * Property that is set using {@link Action#putValue(String, Object)} in {@link Initializer}.
   */
  public void test_inner_properties_putValue_inInitializer() throws Exception {
    m_waitForAutoBuild = true;
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private class Action_1 extends AbstractAction {",
            "    {",
            "      putValue(NAME, 'My name');",
            "    }",
            "    public void actionPerformed(ActionEvent e) {",
            "    }",
            "  }",
            "  private Action_1 action_1 = new Action_1();",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    ActionInfo action = ActionContainerInfo.getActions(panel).get(0);
    // check that NAME is applied
    {
      Action actionObject = (Action) action.getObject();
      assertEquals("My name", actionObject.getValue(Action.NAME));
    }
    // check "name" property
    Property nameProperty = action.getPropertyByTitle("name");
    assertEquals("My name", nameProperty.getValue());
    nameProperty.setValue("New name");
    assertEditor(
        "public class Test extends JPanel {",
        "  private class Action_1 extends AbstractAction {",
        "    {",
        "      putValue(NAME, 'New name');",
        "    }",
        "    public void actionPerformed(ActionEvent e) {",
        "    }",
        "  }",
        "  private Action_1 action_1 = new Action_1();",
        "  public Test() {",
        "  }",
        "}");
    // has "small icon" property, has initializer block
    assertNotNull(action.getPropertyByTitle("small icon"));
  }

  /**
   * Test for {@link Property}'s.
   * <p>
   * Property that is set using {@link Action#putValue(String, Object)}.
   */
  public void test_inner_properties_superConstructor() throws Exception {
    m_waitForAutoBuild = true;
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private class Action_1 extends AbstractAction {",
            "    public Action_1() {",
            "      super('My name');",
            "    }",
            "    public void actionPerformed(ActionEvent e) {",
            "    }",
            "  }",
            "  private Action_1 action_1 = new Action_1();",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    ActionInfo action = ActionContainerInfo.getActions(panel).get(0);
    // check "name" property
    Property nameProperty = action.getPropertyByTitle("name");
    assertEquals("My name", nameProperty.getValue());
    // set same value
    {
      String oldSource = m_lastEditor.getSource();
      nameProperty.setValue("My name");
      assertEquals(oldSource, m_lastEditor.getSource());
    }
    // set new value
    nameProperty.setValue("New name");
    assertEditor(
        "public class Test extends JPanel {",
        "  private class Action_1 extends AbstractAction {",
        "    public Action_1() {",
        "      super('New name');",
        "    }",
        "    public void actionPerformed(ActionEvent e) {",
        "    }",
        "  }",
        "  private Action_1 action_1 = new Action_1();",
        "  public Test() {",
        "  }",
        "}");
    // remove value
    nameProperty.setValue(Property.UNKNOWN_VALUE);
    assertEditor(
        "public class Test extends JPanel {",
        "  private class Action_1 extends AbstractAction {",
        "    public Action_1() {",
        "      super(null);",
        "    }",
        "    public void actionPerformed(ActionEvent e) {",
        "    }",
        "  }",
        "  private Action_1 action_1 = new Action_1();",
        "  public Test() {",
        "  }",
        "}");
  }

  /**
   * Test that we can parse inner-class {@link AbstractAction}.
   */
  public void test_inner_inGeneric() throws Exception {
    m_waitForAutoBuild = true;
    ContainerInfo panel =
        parseContainer(
            "public class Test<T> extends JPanel {",
            "  private class Action_1 extends AbstractAction {",
            "    public Action_1() {",
            "      putValue(NAME, 'My name');",
            "    }",
            "    public void actionPerformed(ActionEvent e) {",
            "    }",
            "  }",
            "  private Action_1 action_1 = new Action_1();",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
    // test for Action's
    {
      List<ActionInfo> actions = ActionContainerInfo.getActions(panel);
      assertEquals(1, actions.size());
      ActionInfo actionInfo = actions.get(0);
      // check values
      {
        Action action = (Action) actionInfo.getObject();
        assertEquals("My name", action.getValue(Action.NAME));
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // External
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for using external {@link Action}.
   * <p>
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=362706
   */
  public void test_external() throws Exception {
    setFileContentSrc(
        "test/ExternalActions.java",
        getSourceDQ(
            "package test;",
            "import java.awt.event.*;",
            "import javax.swing.*;",
            "public class ExternalActions {",
            "  public static class MyAction extends AbstractAction {",
            "    public MyAction() {",
            "      super('My name');",
            "    }",
            "    public void actionPerformed(ActionEvent e) {",
            "    }",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "public class Test extends JPanel {",
        "  private Action action = new ExternalActions.MyAction();",
        "  public Test() {",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {org.eclipse.wb.internal.swing.model.bean.ActionContainerInfo}",
        "    {new: javax.swing.AbstractAction} {field-initializer: action} {/new ExternalActions.MyAction()/}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Anonymous
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * We should be able to parse anonymous instance of {@link AbstractAction}.
   */
  public void test_anonymous_parse() throws Exception {
    parseContainer(
        "public class Test extends JPanel {",
        "  private AbstractAction m_action = new AbstractAction('Text') {",
        "    public void actionPerformed(ActionEvent e) {",
        "    }",
        "  };",
        "  public Test() {",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {org.eclipse.wb.internal.swing.model.bean.ActionContainerInfo}",
        "    {anonymousAction} {field-initializer: m_action} {/new AbstractAction('Text')/}");
  }

  /**
   * Test for {@link Property}'s.
   * <p>
   * Property that is set using {@link Action#putValue(String, Object)} in {@link Initializer}.
   */
  public void test_anonymous_properties_inInitializer() throws Exception {
    m_waitForAutoBuild = true;
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private AbstractAction action = new AbstractAction() {",
            "    {",
            "      putValue(NAME, 'My name');",
            "    }",
            "    public void actionPerformed(ActionEvent e) {",
            "    }",
            "  };",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    ActionInfo action = ActionContainerInfo.getActions(panel).get(0);
    // check "name" property
    Property nameProperty = action.getPropertyByTitle("name");
    assertEquals("My name", nameProperty.getValue());
    nameProperty.setValue("New name");
    // add property value
    action.getPropertyByTitle("short description").setValue("Test description");
    //
    assertEditor(
        "public class Test extends JPanel {",
        "  private AbstractAction action = new AbstractAction() {",
        "    {",
        "      putValue(SHORT_DESCRIPTION, 'Test description');",
        "      putValue(NAME, 'New name');",
        "    }",
        "    public void actionPerformed(ActionEvent e) {",
        "    }",
        "  };",
        "  public Test() {",
        "  }",
        "}");
  }

  /**
   * Test for {@link Property}'s.
   * <p>
   * Property that is set using {@link Action#putValue(String, Object)} in {@link Initializer}.
   */
  public void test_anonymous_properties_inConstructorArgument() throws Exception {
    m_waitForAutoBuild = true;
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private AbstractAction action = new AbstractAction('My name') {",
            "    public void actionPerformed(ActionEvent e) {",
            "    }",
            "  };",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    ActionInfo action = ActionContainerInfo.getActions(panel).get(0);
    // check "name" property
    Property nameProperty = action.getPropertyByTitle("name");
    assertEquals("My name", nameProperty.getValue());
    nameProperty.setValue("New name");
    assertEditor(
        "public class Test extends JPanel {",
        "  private AbstractAction action = new AbstractAction('New name') {",
        "    public void actionPerformed(ActionEvent e) {",
        "    }",
        "  };",
        "  public Test() {",
        "  }",
        "}");
    // no "small icon" property, no initializer block
    assertNull(action.getPropertyByTitle("small icon"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ActionInfo#getPresentation()}.
   */
  public void test_presentation() throws Exception {
    m_waitForAutoBuild = true;
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private Action action_1 = new Action_1();",
            "  private Action action_2 = new Action_2();",
            "  private class Action_1 extends AbstractAction {",
            "    public Action_1() {",
            "      putValue(SMALL_ICON, new ImageIcon(Test.class.getResource('/javax/swing/plaf/basic/icons/JavaCup16.png')));",
            "    }",
            "    public void actionPerformed(ActionEvent e) {",
            "    }",
            "  }",
            "  private class Action_2 extends AbstractAction {",
            "    public Action_2() {",
            "    }",
            "    public void actionPerformed(ActionEvent e) {",
            "    }",
            "  }",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
    ActionInfo action_1 = ActionContainerInfo.getActions(panel).get(0);
    ActionInfo action_2 = ActionContainerInfo.getActions(panel).get(1);
    // action_1 has SMALL_ICON, so has not default icon, as action_2
    Image icon_1 = action_1.getPresentation().getIcon();
    Image icon_2 = action_2.getPresentation().getIcon();
    assertNotSame(action_1.getDescription().getIcon(), icon_1);
    assertSame(action_2.getDescription().getIcon(), icon_2);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * If selected {@link ComponentInfo} is not {@link AbstractButton}, then no
   * <code>"Set Action"</code> menu.
   */
  public void test_contextMenu_notButton() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    // no "Set Action" expected
    MenuManager designerMenu = getDesignerMenuManager();
    panel.getBroadcastObject().addContextMenu(ImmutableList.of(panel), panel, designerMenu);
    IMenuManager actionsMenu = findChildMenuManager(designerMenu, "Set Action");
    assertNull(actionsMenu);
  }

  /**
   * Set {@link Action} for single {@link ComponentInfo}.
   */
  public void test_contextMenu_setAction_single() throws Exception {
    createExternalAction();
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private ExternalAction m_action = new ExternalAction();",
            "  public Test() {",
            "    {",
            "      JButton button = new JButton();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // prepare models
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // prepare runnable for "m_action"
    IAction runnable;
    {
      MenuManager designerMenu = getDesignerMenuManager();
      panel.getBroadcastObject().addContextMenu(ImmutableList.of(button), button, designerMenu);
      IMenuManager actionsMenu = findChildMenuManager(designerMenu, "Set Action");
      runnable = findChildAction(actionsMenu, "m_action");
      assertNotNull(runnable);
    }
    // set "m_action"
    runnable.setChecked(true);
    runnable.run();
    assertEditor(
        "public class Test extends JPanel {",
        "  private ExternalAction m_action = new ExternalAction();",
        "  public Test() {",
        "    {",
        "      JButton button = new JButton();",
        "      button.setAction(m_action);",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Set {@link Action} for multiple {@link ComponentInfo}.
   */
  public void test_contextMenu_setAction_multiple() throws Exception {
    createExternalAction();
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private ExternalAction m_action = new ExternalAction();",
            "  public Test() {",
            "    {",
            "      JButton button_1 = new JButton();",
            "      add(button_1);",
            "    }",
            "    {",
            "      JButton button_2 = new JButton();",
            "      add(button_2);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // prepare models
    ComponentInfo button_1 = panel.getChildrenComponents().get(0);
    ComponentInfo button_2 = panel.getChildrenComponents().get(1);
    // when we ask context menu for "not first" selected component, returned Action does nothing
    {
      MenuManager designerMenu = getDesignerMenuManager();
      panel.getBroadcastObject().addContextMenu(
          ImmutableList.of(button_1, button_2),
          button_2,
          designerMenu);
      IMenuManager actionsMenu = findChildMenuManager(designerMenu, "Set Action");
      IAction runnable = findChildAction(actionsMenu, "m_action");
      // no changes expected
      String expectedSource = m_lastEditor.getSource();
      runnable.setChecked(true);
      runnable.run();
      assertEditor(expectedSource, m_lastEditor);
    }
    // prepare runnable for "m_action"
    IAction runnable;
    {
      MenuManager designerMenu = getDesignerMenuManager();
      panel.getBroadcastObject().addContextMenu(
          ImmutableList.of(button_1, button_2),
          button_1,
          designerMenu);
      IMenuManager actionsMenu = findChildMenuManager(designerMenu, "Set Action");
      runnable = findChildAction(actionsMenu, "m_action");
      assertNotNull(runnable);
    }
    // set "m_action"
    runnable.setChecked(true);
    runnable.run();
    assertEditor(
        "public class Test extends JPanel {",
        "  private ExternalAction m_action = new ExternalAction();",
        "  public Test() {",
        "    {",
        "      JButton button_1 = new JButton();",
        "      button_1.setAction(m_action);",
        "      add(button_1);",
        "    }",
        "    {",
        "      JButton button_2 = new JButton();",
        "      button_2.setAction(m_action);",
        "      add(button_2);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Remove {@link Action} from {@link AbstractButton}.
   */
  public void test_contextMenu_noAction() throws Exception {
    createExternalAction();
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private ExternalAction m_action = new ExternalAction();",
            "  public Test() {",
            "    {",
            "      JButton button = new JButton();",
            "      button.setAction(m_action);",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // prepare models
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // do exclude
    {
      MenuManager designerMenu = getDesignerMenuManager();
      panel.getBroadcastObject().addContextMenu(ImmutableList.of(button), button, designerMenu);
      IMenuManager actionsMenu = findChildMenuManager(designerMenu, "Set Action");
      IAction runnable = findChildAction(actionsMenu, "None");
      runnable.run();
    }
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  private ExternalAction m_action = new ExternalAction();",
        "  public Test() {",
        "    {",
        "      JButton button = new JButton();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Set new {@link Action} for single {@link AbstractButton}.
   */
  public void test_contextMenu_newGroup() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JButton button = new JButton();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // prepare models
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // prepare runnable for new Action
    IAction runnable;
    {
      MenuManager designerMenu = getDesignerMenuManager();
      panel.getBroadcastObject().addContextMenu(ImmutableList.of(button), button, designerMenu);
      IMenuManager actionsMenu = findChildMenuManager(designerMenu, "Set Action");
      runnable = findChildAction(actionsMenu, "New...");
      assertNotNull(runnable);
    }
    // set new Action
    runnable.run();
    assertEditor(
        "public class Test extends JPanel {",
        "  private final Action action = new SwingAction();",
        "  public Test() {",
        "    {",
        "      JButton button = new JButton();",
        "      button.setAction(action);",
        "      add(button);",
        "    }",
        "  }",
        "  private class SwingAction extends AbstractAction {",
        "    public SwingAction() {",
        "      putValue(NAME, 'SwingAction');",
        "      putValue(SHORT_DESCRIPTION, 'Some short description');",
        "    }",
        "    public void actionPerformed(ActionEvent e) {",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utilities
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class LazyActionSupport implements IActionSupport {
    MethodDeclaration m_constructor;

    LazyActionSupport(MethodDeclaration constructor) {
      m_constructor = constructor;
    }

    public ASTNode getCreation() {
      return null;
    }

    public List<Block> getInitializationBlocks() {
      return Lists.newArrayList(m_constructor.getBody());
    }

    public ConstructorDescription getConstructorDescription() throws Exception {
      return null;
    }
  }
}
