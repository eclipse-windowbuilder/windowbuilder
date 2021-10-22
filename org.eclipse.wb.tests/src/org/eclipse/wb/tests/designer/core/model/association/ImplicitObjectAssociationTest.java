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
package org.eclipse.wb.tests.designer.core.model.association;

import org.eclipse.wb.core.model.association.ImplicitObjectAssociation;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.apache.commons.lang.NotImplementedException;

/**
 * Tests for {@link ImplicitObjectAssociation}.
 *
 * @author scheglov_ke
 */
public class ImplicitObjectAssociationTest extends SwingModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test() throws Exception {
    ContainerInfo frame =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JFrame {",
            "  public Test() {",
            "    getContentPane().add(new JButton());",
            "  }",
            "}");
    ComponentInfo contentPane = frame.getChildrenComponents().get(0);
    // check association
    ImplicitObjectAssociation association =
        (ImplicitObjectAssociation) contentPane.getAssociation();
    assertSame(contentPane, association.getJavaInfo());
    assertTrue(association.canDelete());
    // no getSource()
    try {
      association.getSource();
      fail();
    } catch (NotImplementedException e) {
    }
    // no getStatement()
    assertNull(association.getStatement());
    // can not be moved
    try {
      association.move(null);
      fail();
    } catch (NotImplementedException e) {
    }
    // can not be reparented
    try {
      association.setParent(null);
      fail();
    } catch (NotImplementedException e) {
    }
    // delete
    assertTrue(contentPane.canDelete());
    contentPane.delete();
    assertEditor(
        "// filler filler filler",
        "public class Test extends JFrame {",
        "  public Test() {",
        "  }",
        "}");
  }

  /**
   * Test for {@link ImplicitObjectAssociation#getStatement()}.
   */
  public void test_2() throws Exception {
    setFileContentSrc(
        "test/ComplexPanel.java",
        getTestSource(
            "public class ComplexPanel extends JPanel {",
            "  private final JButton button = new JButton();",
            "  public ComplexPanel() {",
            "    add(button);",
            "  }",
            "  public JButton getButton() {",
            "    return button;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      ComplexPanel complexPanel = new ComplexPanel();",
            "      add(complexPanel);",
            "      complexPanel.getButton().setText('text');",
            "    }",
            "  }",
            "}");
    ContainerInfo complexPanel = (ContainerInfo) panel.getChildrenComponents().get(0);
    ComponentInfo exposedButton = complexPanel.getChildrenComponents().get(0);
    // check Statement
    ImplicitObjectAssociation association =
        (ImplicitObjectAssociation) exposedButton.getAssociation();
    assertEquals(
        "ComplexPanel complexPanel = new ComplexPanel();",
        m_lastEditor.getSource(association.getStatement()));
  }
}
