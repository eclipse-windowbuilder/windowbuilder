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
package org.eclipse.wb.tests.designer.swing.model.layout;

import org.eclipse.wb.core.model.association.ImplicitObjectAssociation;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.variable.LocalUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.ImplicitLayoutCreationSupport;
import org.eclipse.wb.internal.swing.model.layout.ImplicitLayoutVariableSupport;
import org.eclipse.wb.internal.swing.model.layout.LayoutInfo;

import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Statement;

import java.awt.FlowLayout;
import java.awt.LayoutManager;

import javax.swing.JPanel;

/**
 * Test for implicit {@link LayoutManager}'s.
 * 
 * @author scheglov_ke
 */
public class ImplicitLayoutTest extends AbstractLayoutTest {
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
   * Test for implicit {@link FlowLayout} for {@link JPanel}.
   */
  public void test_1_implicitLayout_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test {",
            "  public static void main(String args[]) {",
            "    JPanel panel = new JPanel();",
            "  }",
            "}");
    assertEquals(1, panel.getChildren().size());
    assertSame(FlowLayoutInfo.class, panel.getChildren().get(0).getClass());
    assertSame(panel.getLayout(), panel.getChildren().get(0));
    //
    LayoutInfo layout = panel.getLayout();
    assertTrue(layout.canDelete());
    // check association
    assertInstanceOf(ImplicitObjectAssociation.class, layout.getAssociation());
    // check creation support
    {
      CreationSupport creationSupport = layout.getCreationSupport();
      assertInstanceOf(ImplicitLayoutCreationSupport.class, creationSupport);
      assertEquals(panel.getCreationSupport().getNode(), creationSupport.getNode());
      assertEquals("implicit-layout: java.awt.FlowLayout", creationSupport.toString());
    }
    // check variable
    {
      VariableSupport variableSupport = layout.getVariableSupport();
      assertInstanceOf(ImplicitLayoutVariableSupport.class, variableSupport);
      assertEquals("implicit-layout", variableSupport.toString());
      assertEquals("(implicit layout)", variableSupport.getTitle());
      // name
      assertFalse(variableSupport.hasName());
      try {
        variableSupport.getName();
        fail();
      } catch (IllegalStateException e) {
      }
      try {
        variableSupport.setName("foo");
        fail();
      } catch (IllegalStateException e) {
      }
      // conversion
      assertFalse(variableSupport.canConvertLocalToField());
      try {
        variableSupport.convertLocalToField();
        fail();
      } catch (IllegalStateException e) {
      }
      assertFalse(variableSupport.canConvertFieldToLocal());
      try {
        variableSupport.convertFieldToLocal();
        fail();
      } catch (IllegalStateException e) {
      }
      // target
      {
        StatementTarget target = variableSupport.getStatementTarget();
        assertEditor(
            "class Test {",
            "  public static void main(String args[]) {",
            "    JPanel panel = new JPanel();",
            "    FlowLayout flowLayout = (FlowLayout) panel.getLayout();",
            "  }",
            "}");
        //
        ClassInstanceCreation panelCreation =
            ((ConstructorCreationSupport) panel.getCreationSupport()).getCreation();
        Statement expectedStatement =
            getStatement(AstNodeUtils.getEnclosingBlock(panelCreation), new int[]{1});
        assertTarget(target, null, expectedStatement, false);
      }
    }
    // check association
    assertInstanceOf(ImplicitObjectAssociation.class, layout.getAssociation());
  }

  /**
   * Test for materializing implicit {@link FlowLayout}.
   */
  public void test_1_implicitLayout_2() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    LayoutInfo layout = panel.getLayout();
    assertInstanceOf(ImplicitLayoutCreationSupport.class, layout.getCreationSupport());
    // materialize by asking for expression
    {
      NodeTarget target = getNodeBlockTarget(panel, false);
      String accessExpression = layout.getVariableSupport().getAccessExpression(target);
      assertEquals("flowLayout.", accessExpression);
    }
    // check creation/variable/association
    assertInstanceOf(ImplicitLayoutCreationSupport.class, layout.getCreationSupport());
    assertInstanceOf(LocalUniqueVariableSupport.class, layout.getVariableSupport());
    assertInstanceOf(ImplicitObjectAssociation.class, layout.getAssociation());
    // check source
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    FlowLayout flowLayout = (FlowLayout) getLayout();",
        "  }",
        "}");
  }

  // XXX
  /**
   * Test for parsing materialized implicit layout (with {@link CastExpression}).
   */
  public void test_1_implicitLayout_3() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "class Test extends JPanel {",
            "  public Test() {",
            "    FlowLayout flowLayout = (FlowLayout) getLayout();",
            "  }",
            "}");
    {
      LayoutInfo layout = panel.getLayout();
      assertInstanceOf(ImplicitLayoutCreationSupport.class, layout.getCreationSupport());
      {
        VariableSupport variableSupport = layout.getVariableSupport();
        assertInstanceOf(LocalUniqueVariableSupport.class, variableSupport);
        assertEquals("flowLayout", variableSupport.getName());
      }
      assertInstanceOf(ImplicitObjectAssociation.class, layout.getAssociation());
    }
    // check for "de-materializing" implicit layout
    {
      panel.getLayout().delete();
      assertEditor(
          "// filler filler filler",
          "class Test extends JPanel {",
          "  public Test() {",
          "  }",
          "}");
      //
      LayoutInfo layout = panel.getLayout();
      assertInstanceOf(ImplicitLayoutCreationSupport.class, layout.getCreationSupport());
      assertInstanceOf(ImplicitLayoutVariableSupport.class, layout.getVariableSupport());
      assertInstanceOf(ImplicitObjectAssociation.class, layout.getAssociation());
    }
  }
}
