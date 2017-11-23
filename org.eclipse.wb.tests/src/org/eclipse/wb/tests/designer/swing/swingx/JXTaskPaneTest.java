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
package org.eclipse.wb.tests.designer.swing.swingx;

import org.eclipse.wb.internal.core.model.creation.factory.ImplicitFactoryCreationSupport;
import org.eclipse.wb.internal.swing.model.bean.ActionContainerInfo;
import org.eclipse.wb.internal.swing.model.bean.ActionInfo;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.swingx.JXTaskPaneInfo;

import org.eclipse.jdt.core.dom.CompilationUnit;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Component;
import java.util.List;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToolBar;

/**
 * Test for {@link JXTaskPaneInfo}.
 * 
 * @author sablin_aa
 */
public class JXTaskPaneTest extends SwingxModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Invocation of {@link JXTaskPane#add(Action)} creates {@link Component}, so we also should
   * create {@link ComponentInfo} for such invocation.
   */
  public void test_Action_parse() throws Exception {
    createExternalAction();
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  private ExternalAction action = new ExternalAction();",
            "  Test() {",
            "    JXTaskPane pane = new JXTaskPane();",
            "    Component actionComponent = pane.add(action);",
            "    add(pane);",
            "  }",
            "}");
    panel.refresh();
    // 
    assertThat(ActionContainerInfo.getActions(panel).size()).isEqualTo(1);
    List<JXTaskPaneInfo> children = panel.getChildren(JXTaskPaneInfo.class);
    assertThat(children.size()).isEqualTo(1);
    // check JXTaskPane
    JXTaskPaneInfo pane = children.get(0);
    assertThat(pane.getChildrenComponents().size()).isEqualTo(2);// ContentPane & action Component
  }

  /**
   * Use {@link ImplicitFactoryCreationSupport} with {@link JToolBar#add(Action)} to create
   * {@link JButton}.
   */
  public void test_Action_CREATE() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JXTaskPane pane = new JXTaskPane();",
            "    add(pane);",
            "  }",
            "}");
    panel.refresh();
    JXTaskPaneInfo pane = (JXTaskPaneInfo) panel.getChildrenComponents().get(0);
    // create Action
    ActionInfo action = ActionInfo.createInner(pane.getEditor());
    pane.command_CREATE(action, null);
    // check
    assertEditor(
        "class Test extends JPanel {",
        "  private final Action action = new SwingAction();",
        "  Test() {",
        "    JXTaskPane pane = new JXTaskPane();",
        "    add(pane);",
        "    {",
        "      Component component = pane.add(action);",
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
  // Utils
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
}
