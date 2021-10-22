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
package org.eclipse.wb.tests.designer.swing.model.layout.gbl;

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.GridBagConstraintsInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.GridBagLayoutInfo;
import org.eclipse.wb.tests.designer.swing.model.util.SurroundSupportTest;

import javax.swing.JScrollPane;
import javax.swing.JTable;

/**
 * Tests for {@link GridBagLayout_SurroundSupport}.
 *
 * @author scheglov_ke
 */
public class GridBagLayoutSurroundSupportTest extends AbstractGridBagLayoutTest {
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
   * Bad: two components on diagonal, and other component in same rectangle.
   */
  public void test_GridBagLayout_0() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout gridBagLayout = new GridBagLayout();",
            "    setLayout(gridBagLayout);",
            "    {",
            "      JButton button_00 = new JButton();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 0;",
            "      gbc.gridy = 0;",
            "      add(button_00, gbc);",
            "    }",
            "    {",
            "      JButton button_10_BAD = new JButton();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 1;",
            "      gbc.gridy = 0;",
            "      add(button_10_BAD, gbc);",
            "    }",
            "    {",
            "      JButton button_11 = new JButton();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 1;",
            "      gbc.gridy = 1;",
            "      add(button_11, gbc);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button_00 = getJavaInfoByName("button_00");
    ComponentInfo button_11 = getJavaInfoByName("button_11");
    // no surround
    SurroundSupportTest.assertNoSurroundManager(panel, ImmutableList.of(button_00, button_11));
  }

  /**
   * Wrap {@link JTable} with {@link JScrollPane}.
   */
  public void test_GridBagLayout_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout gridBagLayout = new GridBagLayout();",
            "    setLayout(gridBagLayout);",
            "    {",
            "      JTable table = new JTable();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 0;",
            "      gbc.gridy = 0;",
            "      add(table, gbc);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo table = panel.getChildrenComponents().get(0);
    // run action
    SurroundSupportTest.runSurround("javax.swing.JScrollPane", table);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    GridBagLayout gridBagLayout = new GridBagLayout();",
        "    setLayout(gridBagLayout);",
        "    {",
        "      JScrollPane scrollPane = new JScrollPane();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.fill = GridBagConstraints.BOTH;",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 0;",
        "      add(scrollPane, gbc);",
        "      {",
        "        JTable table = new JTable();",
        "        scrollPane.setViewportView(table);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Good: two components in single row, no other components.
   */
  public void test_GridBagLayout_2() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout gridBagLayout = new GridBagLayout();",
            "    setLayout(gridBagLayout);",
            "    {",
            "      JButton button_00 = new JButton();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 0;",
            "      gbc.gridy = 0;",
            "      add(button_00, gbc);",
            "    }",
            "    {",
            "      JButton button_10 = new JButton();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 1;",
            "      gbc.gridy = 0;",
            "      add(button_10, gbc);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button_00 = getJavaInfoByName("button_00");
    ComponentInfo button_10 = getJavaInfoByName("button_10");
    // run action
    SurroundSupportTest.runSurround_JPanel(button_00, button_10);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    GridBagLayout gridBagLayout = new GridBagLayout();",
        "    setLayout(gridBagLayout);",
        "    {",
        "      JPanel panel = new JPanel();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.fill = GridBagConstraints.BOTH;",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 0;",
        "      add(panel, gbc);",
        "      GridBagLayout gridBagLayout_1 = new GridBagLayout();",
        "      gridBagLayout_1.columnWidths = new int[]{0, 0, 0};",
        "      gridBagLayout_1.rowHeights = new int[]{0, 0};",
        "      gridBagLayout_1.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};",
        "      gridBagLayout_1.rowWeights = new double[]{0.0, Double.MIN_VALUE};",
        "      panel.setLayout(gridBagLayout_1);",
        "      {",
        "        JButton button_00 = new JButton();",
        "        GridBagConstraints gbc_1 = new GridBagConstraints();",
        "        gbc_1.insets = new Insets(0, 0, 0, 5);",
        "        gbc_1.gridx = 0;",
        "        gbc_1.gridy = 0;",
        "        panel.add(button_00, gbc_1);",
        "      }",
        "      {",
        "        JButton button_10 = new JButton();",
        "        GridBagConstraints gbc_1 = new GridBagConstraints();",
        "        gbc_1.gridx = 1;",
        "        gbc_1.gridy = 0;",
        "        panel.add(button_10, gbc_1);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Good: two components on diagonal, other components on sides.
   */
  public void test_GridBagLayout_3() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout gridBagLayout = new GridBagLayout();",
            "    setLayout(gridBagLayout);",
            "    {",
            "      JButton button_00 = new JButton();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 0;",
            "      gbc.gridy = 0;",
            "      add(button_00, gbc);",
            "    }",
            "    {",
            "      JButton button_10 = new JButton();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 1;",
            "      gbc.gridy = 0;",
            "      add(button_10, gbc);",
            "    }",
            "    {",
            "      JButton button_20 = new JButton();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 2;",
            "      gbc.gridy = 0;",
            "      add(button_20, gbc);",
            "    }",
            "    {",
            "      JButton button_01 = new JButton();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 0;",
            "      gbc.gridy = 1;",
            "      add(button_01, gbc);",
            "    }",
            "    {",
            "      JButton button_02 = new JButton();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 0;",
            "      gbc.gridy = 2;",
            "      add(button_02, gbc);",
            "    }",
            "    {",
            "      JButton button_11 = new JButton();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 1;",
            "      gbc.gridy = 1;",
            "      add(button_11, gbc);",
            "    }",
            "    {",
            "      JButton button_22 = new JButton();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 2;",
            "      gbc.gridy = 2;",
            "      add(button_22, gbc);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button_11 = getJavaInfoByName("button_11");
    ComponentInfo button_22 = getJavaInfoByName("button_22");
    // check location of buttons
    {
      {
        GridBagConstraintsInfo constraints = GridBagLayoutInfo.getConstraintsFor(button_11);
        assertEquals(1, constraints.getX());
        assertEquals(1, constraints.getY());
      }
      {
        GridBagConstraintsInfo constraints = GridBagLayoutInfo.getConstraintsFor(button_22);
        assertEquals(2, constraints.getX());
        assertEquals(2, constraints.getY());
      }
    }
    // run action
    SurroundSupportTest.runSurround_JPanel(button_11, button_22);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    GridBagLayout gridBagLayout = new GridBagLayout();",
        "    setLayout(gridBagLayout);",
        "    {",
        "      JButton button_00 = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.insets = new Insets(0, 0, 5, 5);",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 0;",
        "      add(button_00, gbc);",
        "    }",
        "    {",
        "      JButton button_10 = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.insets = new Insets(0, 0, 5, 5);",
        "      gbc.gridx = 1;",
        "      gbc.gridy = 0;",
        "      add(button_10, gbc);",
        "    }",
        "    {",
        "      JButton button_20 = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.insets = new Insets(0, 0, 5, 0);",
        "      gbc.gridx = 2;",
        "      gbc.gridy = 0;",
        "      add(button_20, gbc);",
        "    }",
        "    {",
        "      JButton button_01 = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.insets = new Insets(0, 0, 5, 5);",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 1;",
        "      add(button_01, gbc);",
        "    }",
        "    {",
        "      JPanel panel = new JPanel();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.fill = GridBagConstraints.BOTH;",
        "      gbc.gridheight = 2;",
        "      gbc.gridwidth = 2;",
        "      gbc.insets = new Insets(0, 0, 5, 5);",
        "      gbc.gridx = 1;",
        "      gbc.gridy = 1;",
        "      add(panel, gbc);",
        "      GridBagLayout gridBagLayout_1 = new GridBagLayout();",
        "      gridBagLayout_1.columnWidths = new int[]{0, 0, 0};",
        "      gridBagLayout_1.rowHeights = new int[]{0, 0, 0};",
        "      gridBagLayout_1.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};",
        "      gridBagLayout_1.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};",
        "      panel.setLayout(gridBagLayout_1);",
        "      {",
        "        JButton button_11 = new JButton();",
        "        GridBagConstraints gbc_1 = new GridBagConstraints();",
        "        gbc_1.insets = new Insets(0, 0, 5, 5);",
        "        gbc_1.gridx = 0;",
        "        gbc_1.gridy = 0;",
        "        panel.add(button_11, gbc_1);",
        "      }",
        "      {",
        "        JButton button_22 = new JButton();",
        "        GridBagConstraints gbc_1 = new GridBagConstraints();",
        "        gbc_1.gridx = 1;",
        "        gbc_1.gridy = 1;",
        "        panel.add(button_22, gbc_1);",
        "      }",
        "    }",
        "    {",
        "      JButton button_02 = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.insets = new Insets(0, 0, 0, 5);",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 2;",
        "      add(button_02, gbc);",
        "    }",
        "  }",
        "}");
  }
}
