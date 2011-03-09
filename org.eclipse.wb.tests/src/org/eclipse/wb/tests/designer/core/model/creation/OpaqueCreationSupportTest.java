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
package org.eclipse.wb.tests.designer.core.model.creation;

import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.ICreationSupportPermissions;
import org.eclipse.wb.internal.core.model.creation.OpaqueCreationSupport;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;

import static org.easymock.EasyMock.expect;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;

import javax.swing.JButton;

/**
 * Test for {@link OpaqueCreationSupport}.
 * 
 * @author scheglov_ke
 */
public class OpaqueCreationSupportTest extends SwingModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_0() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test {",
            "  public static void main(String[] args) {",
            "    JPanel panel = new JPanel();",
            "  }",
            "}");
    ClassInstanceCreation node = (ClassInstanceCreation) panel.getCreationSupport().getNode();
    //
    CreationSupport creationSupport = new OpaqueCreationSupport(node);
    assertEquals("opaque", creationSupport.toString());
    assertSame(node, creationSupport.getNode());
    // isJavaInfo()
    assertTrue(creationSupport.isJavaInfo(node));
    assertFalse(creationSupport.isJavaInfo(null));
    assertFalse(creationSupport.isJavaInfo(JavaInfoUtils.getTypeDeclaration(panel)));
    // permissions
    assertFalse(creationSupport.canReorder());
    assertFalse(creationSupport.canReparent());
  }

  /**
   * Test for {@link OpaqueCreationSupport#add_getSource(NodeTarget)}.
   */
  public void test_add() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    FlowLayoutInfo flowLayout = (FlowLayoutInfo) panel.getLayout();
    // add new JButton with OpaqueCreationSupport
    ComponentInfo newButton =
        (ComponentInfo) JavaInfoUtils.createJavaInfo(
            m_lastEditor,
            JButton.class,
            new OpaqueCreationSupport("new JButton()"));
    flowLayout.add(newButton, null);
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JButton button = new JButton();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    assertEquals("new JButton()", m_lastEditor.getSource(newButton.getCreationSupport().getNode()));
    // assert that "button" is bound to AST
    {
      ASTNode node = getNode("button = ");
      assertTrue(newButton.isRepresentedBy(node));
    }
  }

  /**
   * Test for using {@link ICreationSupportPermissions} by {@link OpaqueCreationSupport}.
   */
  public void test_permissions() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test {",
            "  public static void main(String[] args) {",
            "    JPanel panel = new JPanel();",
            "  }",
            "}");
    // create OpaqueCreationSupport
    OpaqueCreationSupport creationSupport;
    {
      ClassInstanceCreation node = (ClassInstanceCreation) panel.getCreationSupport().getNode();
      creationSupport = new OpaqueCreationSupport(node);
      creationSupport.setJavaInfo(panel);
    }
    //
    IMocksControl mocksControl = EasyMock.createStrictControl();
    // set ICreationSupportPermissions
    ICreationSupportPermissions permissions;
    {
      permissions = mocksControl.createMock(ICreationSupportPermissions.class);
      creationSupport.setPermissions(permissions);
    }
    // canDelete()
    {
      expect(permissions.canDelete(panel)).andReturn(false);
      mocksControl.replay();
      assertFalse(creationSupport.canDelete());
      mocksControl.verify();
      mocksControl.reset();
    }
    // delete()
    {
      permissions.delete(panel);
      mocksControl.replay();
      creationSupport.delete();
      mocksControl.verify();
      mocksControl.reset();
    }
    // canReorder()
    {
      expect(permissions.canReorder(panel)).andReturn(false);
      mocksControl.replay();
      assertFalse(creationSupport.canReorder());
      mocksControl.verify();
      mocksControl.reset();
    }
    // canReparent()
    {
      expect(permissions.canReparent(panel)).andReturn(false);
      mocksControl.replay();
      assertFalse(creationSupport.canReparent());
      mocksControl.verify();
      mocksControl.reset();
    }
  }
}
