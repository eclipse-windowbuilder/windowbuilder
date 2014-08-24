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
package org.eclipse.wb.tests.designer.swing.model.layout.FormLayout;

import org.eclipse.wb.internal.swing.FormLayout.model.FormLayoutConverter;
import org.eclipse.wb.internal.swing.FormLayout.model.FormLayoutInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import com.jgoodies.forms.layout.FormLayout;

/**
 * Test for {@link FormLayoutConverter}.
 * 
 * @author scheglov_ke
 */
public class FormLayoutConverterTest extends AbstractFormLayoutTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_empty() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(null);",
            "    setSize(450, 300);",
            "  }",
            "}");
    //
    panel.refresh();
    try {
      setLayout(panel, FormLayout.class);
    } finally {
      panel.refresh_dispose();
    }
    //
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setSize(450, 300);",
        "    setLayout(new FormLayout(new ColumnSpec[] {},",
        "      new RowSpec[] {}));",
        "  }",
        "}");
  }

  public void test_oneRow() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(null);",
            "    setSize(450, 300);",
            "    {",
            "      JButton button = new JButton('000');",
            "      button.setBounds(4, 10, 100, 30);",
            "      add(button);",
            "    }",
            "    {",
            "      JButton button2 = new JButton('222');",
            "      button2.setBounds(120, 11, 80, 20);",
            "      add(button2);",
            "    }",
            "  }",
            "}");
    //
    panel.refresh();
    try {
      setLayout(panel, FormLayout.class);
    } finally {
      panel.refresh_dispose();
    }
    //
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setSize(450, 300);",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,",
        "        ColumnSpec.decode('100px'),",
        "        ColumnSpec.decode('16px'),",
        "        ColumnSpec.decode('80px'),},",
        "      new RowSpec[] {",
        "        FormSpecs.UNRELATED_GAP_ROWSPEC,",
        "        RowSpec.decode('30px'),}));",
        "    {",
        "      JButton button = new JButton('000');",
        "      add(button, '2, 2, fill, fill');",
        "    }",
        "    {",
        "      JButton button2 = new JButton('222');",
        "      add(button2, '4, 2, fill, top');",
        "    }",
        "  }",
        "}");
    {
      FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
      assertEquals(4, layout.getColumns().size());
      assertEquals(2, layout.getRows().size());
    }
  }

  public void test_twoRows_spanColumns() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(null);",
            "    setSize(450, 300);",
            "    {",
            "      JButton button = new JButton();",
            "      button.setBounds(0, 10, 100, 40);",
            "      add(button);",
            "    }",
            "    {",
            "      JButton button2 = new JButton();",
            "      button2.setBounds(108, 10, 80, 20);",
            "      add(button2);",
            "    }",
            "    {",
            "      JButton button3 = new JButton();",
            "      button3.setBounds(45, 60, 90, 40);",
            "      add(button3);",
            "    }",
            "  }",
            "}");
    //
    panel.refresh();
    try {
      setLayout(panel, FormLayout.class);
    } finally {
      panel.refresh_dispose();
    }
    //
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setSize(450, 300);",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        ColumnSpec.decode('100px'),",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        ColumnSpec.decode('80px'),},",
        "      new RowSpec[] {",
        "        FormSpecs.UNRELATED_GAP_ROWSPEC,",
        "        RowSpec.decode('40px'),",
        "        FormSpecs.UNRELATED_GAP_ROWSPEC,",
        "        RowSpec.decode('40px'),}));",
        "    {",
        "      JButton button = new JButton();",
        "      add(button, '1, 2, fill, fill');",
        "    }",
        "    {",
        "      JButton button2 = new JButton();",
        "      add(button2, '3, 2, fill, top');",
        "    }",
        "    {",
        "      JButton button3 = new JButton();",
        "      add(button3, '1, 4, 3, 1, center, fill');",
        "    }",
        "  }",
        "}");
  }

  public void test_Switching_fromGridBagLayout() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout gridBagLayout = new GridBagLayout();",
            "    gridBagLayout.columnWidths = new int[] { 0, 0, 0, 0 };",
            "    gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0 };",
            "    gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };",
            "    gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };",
            "    setLayout(gridBagLayout);",
            "    {",
            "      JComboBox comboBox = new JComboBox();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.insets = new Insets(0, 0, 5, 5);",
            "      gbc.fill = GridBagConstraints.HORIZONTAL;",
            "      gbc.gridx = 1;",
            "      gbc.gridy = 0;",
            "      add(comboBox, gbc);",
            "    }",
            "    {",
            "      JLabel label = new JLabel('New label');",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.insets = new Insets(0, 0, 5, 5);",
            "      gbc.anchor = GridBagConstraints.EAST;",
            "      gbc.gridx = 0;",
            "      gbc.gridy = 1;",
            "      add(label, gbc);",
            "    }",
            "    {",
            "      JTextField textField = new JTextField();",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridwidth = 2;",
            "      gbc.insets = new Insets(0, 0, 5, 5);",
            "      gbc.fill = GridBagConstraints.HORIZONTAL;",
            "      gbc.gridx = 1;",
            "      gbc.gridy = 1;",
            "      add(textField, gbc);",
            "      textField.setColumns(10);",
            "    }",
            "    {",
            "      JButton button = new JButton('New button');",
            "      GridBagConstraints gbc = new GridBagConstraints();",
            "      gbc.gridx = 2;",
            "      gbc.gridy = 2;",
            "      add(button, gbc);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // set FormLayout
    try {
      setLayout(panel, FormLayout.class);
    } finally {
      panel.refresh_dispose();
    }
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        ColumnSpec.decode('46px'),",
        "        FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,",
        "        ColumnSpec.decode('305px'),",
        "        FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,",
        "        ColumnSpec.decode('89px'),},",
        "      new RowSpec[] {",
        "        RowSpec.decode('20px'),",
        "        FormSpecs.LINE_GAP_ROWSPEC,",
        "        RowSpec.decode('20px'),",
        "        FormSpecs.LINE_GAP_ROWSPEC,",
        "        RowSpec.decode('23px'),}));",
        "    {",
        "      JComboBox comboBox = new JComboBox();",
        "      add(comboBox, '3, 1, fill, center');",
        "    }",
        "    {",
        "      JLabel label = new JLabel('New label');",
        "      add(label, '1, 3, right, center');",
        "    }",
        "    {",
        "      JTextField textField = new JTextField();",
        "      add(textField, '3, 3, 3, 1, fill, center');",
        "      textField.setColumns(10);",
        "    }",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button, '5, 5, center, center');",
        "    }",
        "  }",
        "}");
  }
}
