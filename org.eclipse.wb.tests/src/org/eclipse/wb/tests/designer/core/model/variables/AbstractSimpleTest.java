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
package org.eclipse.wb.tests.designer.core.model.variables;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.variable.AbstractSimpleVariableSupport;
import org.eclipse.wb.internal.core.model.variable.FieldInitializerVariableSupport;
import org.eclipse.wb.internal.core.model.variable.LocalUniqueVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.ICoreExceptionConstants;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.GridLayoutInfo;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;

import java.util.List;

/**
 * Test for {@link AbstractSimpleVariableSupport}.
 *
 * @author scheglov_ke
 */
public class AbstractSimpleTest extends AbstractVariableTest {
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
   * Creation code is not valid at all, so has no {@link ITypeBinding}.
   */
  public void test_addBadCode_veryBad() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends JButton {",
            "  public MyButton(String s) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <creation>",
            "    <source><![CDATA[thisIsBAD]]></source>",
            "  </creation>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    FlowLayoutInfo flowLayout = (FlowLayoutInfo) panel.getLayout();
    // try to add
    try {
      ComponentInfo newButton = createJavaInfo("test.MyButton");
      flowLayout.add(newButton, null);
      fail();
    } catch (DesignerException e) {
      assertEquals(ICoreExceptionConstants.GEN_NO_TYPE_BINDING, e.getCode());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Move with/without rename
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Conflict with visible variables.
   */
  public void test_moveRenameConflict_visible() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JPanel panel = new JPanel();",
            "      add(panel);",
            "    }",
            "    {",
            "      JPanel panel = new JPanel();",
            "      add(panel);",
            "    }",
            "  }",
            "}");
    ContainerInfo panel_1 = (ContainerInfo) panel.getChildrenComponents().get(0);
    ContainerInfo panel_2 = (ContainerInfo) panel.getChildrenComponents().get(1);
    FlowLayoutInfo flowLayout_1 = (FlowLayoutInfo) panel_1.getLayout();
    // move and check
    flowLayout_1.move(panel_2, null);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JPanel panel = new JPanel();",
        "      add(panel);",
        "      {",
        "        JPanel panel_1 = new JPanel();",
        "        panel.add(panel_1);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Conflict with visible variables, not only for moved component, but also for its child.
   */
  public void test_moveRenameConflict_visibleChilren() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JPanel panel = new JPanel();",
            "      add(panel);",
            "      JButton button = new JButton();",
            "      panel.add(button);",
            "    }",
            "    {",
            "      JPanel panel = new JPanel();",
            "      add(panel);",
            "      JButton button = new JButton();",
            "      panel.add(button);",
            "    }",
            "  }",
            "}");
    ContainerInfo panel_1 = (ContainerInfo) panel.getChildrenComponents().get(0);
    ContainerInfo panel_2 = (ContainerInfo) panel.getChildrenComponents().get(1);
    FlowLayoutInfo flowLayout_1 = (FlowLayoutInfo) panel_1.getLayout();
    // move and check
    flowLayout_1.move(panel_2, null);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JPanel panel = new JPanel();",
        "      add(panel);",
        "      JButton button = new JButton();",
        "      panel.add(button);",
        "      {",
        "        JPanel panel_1 = new JPanel();",
        "        panel.add(panel_1);",
        "        JButton button_1 = new JButton();",
        "        panel_1.add(button_1);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Conflict with visible variable only for child with {@link LocalUniqueVariableSupport}. Parent
   * of this child is {@link FieldInitializerVariableSupport}, so does not need rename check.
   */
  public void test_moveRenameConflict_visibleChilren2() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private final JPanel panel2 = new JPanel();",
            "  public Test() {",
            "    {",
            "      JPanel panel = new JPanel();",
            "      add(panel);",
            "      JButton button = new JButton();",
            "      panel.add(button);",
            "    }",
            "    {",
            "      add(panel2);",
            "      JButton button = new JButton();",
            "      panel2.add(button);",
            "    }",
            "  }",
            "}");
    ContainerInfo panel_1 = (ContainerInfo) panel.getChildrenComponents().get(0);
    ContainerInfo panel_2 = (ContainerInfo) panel.getChildrenComponents().get(1);
    FlowLayoutInfo flowLayout_1 = (FlowLayoutInfo) panel_1.getLayout();
    // move and check
    flowLayout_1.move(panel_2, null);
    assertEditor(
        "public class Test extends JPanel {",
        "  private final JPanel panel2 = new JPanel();",
        "  public Test() {",
        "    {",
        "      JPanel panel = new JPanel();",
        "      add(panel);",
        "      JButton button = new JButton();",
        "      panel.add(button);",
        "      {",
        "        JButton button_1 = new JButton();",
        "        panel.add(panel2);",
        "        panel2.add(button_1);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Components in blocks, no conflict.
   */
  public void test_moveRenameConflict_noConflict() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JPanel panel = new JPanel();",
            "      add(panel);",
            "      panel.setEnabled(false);",
            "    }",
            "    {",
            "      JPanel panel = new JPanel();",
            "      add(panel);",
            "    }",
            "  }",
            "}");
    ContainerInfo panel_1 = (ContainerInfo) panel.getChildrenComponents().get(0);
    ContainerInfo panel_2 = (ContainerInfo) panel.getChildrenComponents().get(1);
    // move and check
    ((FlowLayoutInfo) panel.getLayout()).move(panel_2, panel_1);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JPanel panel = new JPanel();",
        "      add(panel);",
        "    }",
        "    {",
        "      JPanel panel = new JPanel();",
        "      add(panel);",
        "      panel.setEnabled(false);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Moved component will shadow variable below.
   */
  public void test_moveRenameConflict_shadow_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JPanel panel = new JPanel();",
            "      add(panel);",
            "    }",
            "    JPanel panel = new JPanel();",
            "    add(panel);",
            "  }",
            "}");
    ContainerInfo panel_1 = (ContainerInfo) panel.getChildrenComponents().get(0);
    ContainerInfo panel_2 = (ContainerInfo) panel.getChildrenComponents().get(1);
    // move and check
    ((FlowLayoutInfo) panel.getLayout()).move(panel_2, panel_1);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JPanel panel_1 = new JPanel();",
        "    add(panel_1);",
        "    {",
        "      JPanel panel = new JPanel();",
        "      add(panel);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Moved component will shadow variable below.<br>
   * But there is also possible conflict with child of moved container.
   */
  public void test_moveRenameConflict_shadow_2() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JPanel panel = new JPanel();",
            "      add(panel);",
            "    }",
            "    JPanel panel = new JPanel();",
            "    add(panel);",
            "    {",
            "      JButton panel_1 = new JButton();",
            "      panel.add(panel_1);",
            "    }",
            "  }",
            "}");
    ContainerInfo panel_1 = (ContainerInfo) panel.getChildrenComponents().get(0);
    ContainerInfo panel_2 = (ContainerInfo) panel.getChildrenComponents().get(1);
    // move and check
    ((FlowLayoutInfo) panel.getLayout()).move(panel_2, panel_1);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JPanel panel_2 = new JPanel();",
        "    add(panel_2);",
        "    {",
        "      JButton panel_1 = new JButton();",
        "      panel_2.add(panel_1);",
        "    }",
        "    {",
        "      JPanel panel = new JPanel();",
        "      add(panel);",
        "    }",
        "  }",
        "}");
  }

  /*public void test_moveRenameConflict_renameConflicts() throws Exception {
  	ContainerInfo panel =
  		parseTestSource(new String[]{
  				"public class Test extends JPanel {",
  				"  public Test() {",
  				"    {",
  				"      JPanel panel = new JPanel();",
  				"      add(panel);",
  				"      GridLayout gridLayout = new GridLayout();",
  				"      panel.setLayout(gridLayout);",
  				"    }",
  				"    {",
  				"      JPanel panel = new JPanel();",
  				"      add(panel);",
  				"      GridLayout gridLayout = new GridLayout();",
  				"      panel.setLayout(gridLayout);",
  				"    }",
  				"  }",
  		"}"});
  	ContainerInfo panel_1 = (ContainerInfo) panel.getChildrenComponents().get(0);
  	ContainerInfo panel_2 = (ContainerInfo) panel.getChildrenComponents().get(1);
  	GridLayoutInfo gridLayout_1 = (GridLayoutInfo) panel_1.getLayout();
  	// reparent panel_2 on panel_1, so unique names should be generated
  	gridLayout_1.move(panel_2, null);
  	assertEditor(
  			"public class Test extends JPanel {",
  			"  public Test() {",
  			"    {",
  			"      JPanel panel = new JPanel();",
  			"      add(panel);",
  			"      GridLayout gridLayout = new GridLayout();",
  			"      panel.setLayout(gridLayout);",
  			"      {",
  			"        JPanel panel_1 = new JPanel();",
  			"        panel.add(panel_1);",
  			"        GridLayout gridLayout_1 = new GridLayout();",
  			"        panel_1.setLayout(gridLayout_1);",
  			"      }",
  			"    }",
  			"  }",
  	"}");
  }*/
  /**
   * Move without conflict.
   */
  public void test_move_noConflict() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JPanel panel_first = new JPanel();",
            "      add(panel_first);",
            "      GridLayout gridLayout_first = new GridLayout();",
            "      panel_first.setLayout(gridLayout_first);",
            "    }",
            "    {",
            "      JPanel panel = new JPanel();",
            "      add(panel);",
            "      GridLayout gridLayout = new GridLayout();",
            "      panel.setLayout(gridLayout);",
            "    }",
            "  }",
            "}");
    ContainerInfo panel_1 = (ContainerInfo) panel.getChildrenComponents().get(0);
    ContainerInfo panel_2 = (ContainerInfo) panel.getChildrenComponents().get(1);
    GridLayoutInfo gridLayout_1 = (GridLayoutInfo) panel_1.getLayout();
    // reparent panel_2 on panel_1, no conflicts expected
    gridLayout_1.move(panel_2, null);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JPanel panel_first = new JPanel();",
        "      add(panel_first);",
        "      GridLayout gridLayout_first = new GridLayout();",
        "      panel_first.setLayout(gridLayout_first);",
        "      {",
        "        JPanel panel = new JPanel();",
        "        panel_first.add(panel);",
        "        GridLayout gridLayout = new GridLayout();",
        "        panel.setLayout(gridLayout);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AbstractSimpleVariableSupport#ensureInstanceReadyAt(StatementTarget)}.
   */
  public void test_ensureInstanceReadyAt() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setEnabled(true);",
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
    ComponentInfo button_2 = panel.getChildrenComponents().get(1);
    // prepare target to move into method beginning
    StatementTarget target;
    {
      MethodDeclaration constructor = (MethodDeclaration) panel.getCreationSupport().getNode();
      Statement statement = DomGenerics.statements(constructor.getBody()).get(0);
      target = new StatementTarget(statement, true);
    }
    // add listener
    final int[] broadcastInvoked = new int[]{0};
    panel.addBroadcastListener(new JavaEventListener() {
      @Override
      public void variable_addStatementsToMove(JavaInfo parent, List<JavaInfo> children)
          throws Exception {
        broadcastInvoked[0]++;
      }
    });
    // move "button_2" into target
    button_2.getVariableSupport().ensureInstanceReadyAt(target);
    // check result
    assertEquals(1, broadcastInvoked[0]);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JButton button_2 = new JButton();",
        "      add(button_2);",
        "    }",
        "    setEnabled(true);",
        "    {",
        "      JButton button_1 = new JButton();",
        "      add(button_1);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link AbstractSimpleVariableSupport#ensureInstanceReadyAt(StatementTarget)}.
   * <p>
   * We should not move loosely related nodes (in different method).
   */
  public void test_ensureInstanceReadyAt_relatedNodeLoosely() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private JButton button = new JButton();",
            "  public Test() {",
            "    int a;",
            "    {",
            "      add(button);",
            "      button.setEnabled(true);",
            "    }",
            "    configure();",
            "  }",
            "  private void configure() {",
            "    button.setEnabled(false);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // prepare target to move into method beginning
    StatementTarget target = getBlockTarget(panel, true);
    // move "button" into target
    button.getVariableSupport().ensureInstanceReadyAt(target);
    // check result
    assertEditor(
        "public class Test extends JPanel {",
        "  private JButton button = new JButton();",
        "  public Test() {",
        "    {",
        "      add(button);",
        "      button.setEnabled(true);",
        "    }",
        "    int a;",
        "    configure();",
        "  }",
        "  private void configure() {",
        "    button.setEnabled(false);",
        "  }",
        "}");
  }
}
