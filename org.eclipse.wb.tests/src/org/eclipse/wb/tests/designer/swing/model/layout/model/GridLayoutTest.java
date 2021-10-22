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
package org.eclipse.wb.tests.designer.swing.model.layout.model;

import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.GridLayoutInfo;
import org.eclipse.wb.tests.designer.swing.model.layout.AbstractLayoutTest;

import java.awt.GridLayout;

/**
 * Test for {@link GridLayoutInfo}.
 *
 * @author scheglov_ke
 */
public class GridLayoutTest extends AbstractLayoutTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for installing.
   */
  public void test_setLayout() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    setLayout(panel, GridLayout.class);
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new GridLayout(1, 0, 0, 0));",
        "  }",
        "}");
  }

  /**
   * {@link GridLayout} uses number of specified columns only when number of "rows" is zero.
   */
  public void test_setColumns() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new GridLayout(2, 0));",
            "  }",
            "}");
    GridLayoutInfo layout = (GridLayoutInfo) panel.getLayout();
    // set "columns"
    layout.getPropertyByTitle("columns").setValue(5);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new GridLayout(0, 5));",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Switching layouts
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test switching layouts from {@link AbsoluteLayout} to {@link GridLayout}, and restore component
   * positions & alignments.
   */
  public void test_Switching_fromNullLayout() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(null);",
            "    {",
            "      JLabel label = new JLabel('New label');",
            "      label.setBounds(12, 50, 61, 15);",
            "      add(label);",
            "    }",
            "    {",
            "      JButton button = new JButton('New button');",
            "      button.setBounds(229, 82, 85, 27);",
            "      add(button);",
            "    }",
            "    {",
            "      JTextField textField = new JTextField();",
            "      textField.setBounds(85, 45, 140, 25);",
            "      add(textField);",
            "      textField.setColumns(10);",
            "    }",
            "    {",
            "      JComboBox comboBox = new JComboBox();",
            "      comboBox.setBounds(85, 12, 134, 27);",
            "      add(comboBox);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    setLayout(panel, GridLayout.class);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new GridLayout(0, 3, 0, 0));",
        "    {",
        "      JLabel label = new JLabel('');",
        "      add(label);",
        "    }",
        "    {",
        "      JComboBox comboBox = new JComboBox();",
        "      add(comboBox);",
        "    }",
        "    {",
        "      JLabel label = new JLabel('');",
        "      add(label);",
        "    }",
        "    {",
        "      JLabel label = new JLabel('New label');",
        "      add(label);",
        "    }",
        "    {",
        "      JTextField textField = new JTextField();",
        "      add(textField);",
        "      textField.setColumns(10);",
        "    }",
        "    {",
        "      JLabel label = new JLabel('');",
        "      add(label);",
        "    }",
        "    {",
        "      JLabel label = new JLabel('');",
        "      add(label);",
        "    }",
        "    {",
        "      JLabel label = new JLabel('');",
        "      add(label);",
        "    }",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }
}
