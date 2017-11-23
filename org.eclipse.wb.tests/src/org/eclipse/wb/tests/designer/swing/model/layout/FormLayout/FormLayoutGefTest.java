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

import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.swing.FormLayout.model.FormColumnInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.FormLayoutInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.FormRowInfo;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;
import org.eclipse.wb.tests.designer.swing.model.layout.AbstractLayoutTest;
import org.eclipse.wb.tests.gef.GraphicalRobot;

import com.jgoodies.forms.layout.FormLayout;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.GridBagLayout;
import java.util.List;

/**
 * Test {@link FormLayoutInfo} in GEF.
 * 
 * @author scheglov_ke
 */
public class FormLayoutGefTest extends SwingGefTest {
  private static int COLUMN_GAP = 8;
  private static int ROW_GAP = 7;
  private static int V_COLUMN_SIZE = 22;
  private static int V_ROW_SIZE = 22;
  private ContainerInfo panel;
  private FormLayoutInfo layout;
  private GraphicalRobot horizontalRobot;
  private GraphicalRobot verticalRobot;

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
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    AbstractLayoutTest.configureForTest();
  }

  @Override
  protected void tearDown() throws Exception {
    AbstractLayoutTest.configureDefaults();
    super.tearDown();
  }

  @Override
  protected void configureNewProject() throws Exception {
    super.configureNewProject();
    AbstractFormLayoutTest.do_configureNewProject();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Set FormLayout
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_setLayout_empty() throws Exception {
    openPanel(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    //
    loadCreationTool("com.jgoodies.forms.layout.FormLayout");
    canvas.moveTo(panel, 0.5, 0.5).click();
    assertEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {},",
        "      new RowSpec[] {}));",
        "  }",
        "}");
  }

  /**
   * When we replace {@link GridBagLayout} with {@link FormLayout} there was bug that we applied new
   * columns/rows {@link LayoutEditPolicy} for exiting columns/rows from previous layout. We test
   * that they play safely now.
   */
  public void test_setLayout_replaceGridBagLayout() throws Exception {
    openPanel(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new GridBagLayout());",
        "    {",
        "      JButton button = new JButton();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    canvas.select(panel);
    //
    loadCreationTool("com.jgoodies.forms.layout.FormLayout");
    canvas.moveTo(panel, 0.5, 0.5).click();
    // no exceptions
    String source = m_lastEditor.getSource();
    assertThat(source).contains("new FormLayout(");
    assertThat(source).contains("new ColumnSpec[] {");
    assertThat(source).contains("new RowSpec[] {");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE_filled() throws Exception {
    openPanel(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    add(new JButton('Existing JButton'), '2, 2');",
        "  }",
        "}");
    //
    loadButtonWithText();
    canvas.moveTo(panel, COLUMN_GAP, ROW_GAP);
    canvas.assertCommandNull();
  }

  public void test_CREATE_empty() throws Exception {
    openPanel(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "  }",
        "}");
    //
    loadButtonWithText();
    canvas.moveTo(panel, COLUMN_GAP, ROW_GAP);
    canvas.assertCommandNotNull();
    canvas.click();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button, '2, 2');",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_virtual_2x2() throws Exception {
    openPanel(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout());",
        "  }",
        "}");
    //
    loadButtonWithText();
    canvas.moveTo(panel, COLUMN_GAP, ROW_GAP);
    canvas.assertCommandNotNull();
    canvas.click();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button, '2, 2');",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_virtual_4x2() throws Exception {
    openPanel(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout());",
        "  }",
        "}");
    //
    loadButtonWithText();
    canvas.moveTo(panel, COLUMN_GAP + V_COLUMN_SIZE + COLUMN_GAP, ROW_GAP);
    canvas.assertCommandNotNull();
    canvas.click();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button, '4, 2');",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_virtual_2x4() throws Exception {
    openPanel(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout());",
        "  }",
        "}");
    //
    loadButtonWithText();
    canvas.moveTo(panel, COLUMN_GAP, ROW_GAP + V_ROW_SIZE + ROW_GAP);
    canvas.assertCommandNotNull();
    canvas.click();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button, '2, 4');",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_appendToColumn_2x4() throws Exception {
    openPanel(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    {",
        "      JButton existing = new JButton('Existing JButton');",
        "      add(existing, '2, 2');",
        "    }",
        "  }",
        "}");
    ComponentInfo existing = getJavaInfoByName("existing");
    //
    loadButtonWithText();
    canvas.target(existing).inX(0.5).outY(ROW_GAP + 1).move();
    canvas.click();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    {",
        "      JButton existing = new JButton('Existing JButton');",
        "      add(existing, '2, 2');",
        "    }",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button, '2, 4');",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_appendToRow_4x2() throws Exception {
    openPanel(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    {",
        "      JButton existing = new JButton('Existing JButton');",
        "      add(existing, '2, 2');",
        "    }",
        "  }",
        "}");
    ComponentInfo existing = getJavaInfoByName("existing");
    //
    loadButtonWithText();
    canvas.target(existing).inY(0.5).outX(COLUMN_GAP + 1).move();
    canvas.click();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    {",
        "      JButton existing = new JButton('Existing JButton');",
        "      add(existing, '2, 2');",
        "    }",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button, '4, 2');",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_beforeFirstRow() throws Exception {
    openPanel(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    {",
        "      JButton existing = new JButton('Existing JButton');",
        "      add(existing, '2, 2');",
        "    }",
        "  }",
        "}");
    ComponentInfo existing = getJavaInfoByName("existing");
    //
    loadButtonWithText();
    canvas.target(existing).inX(0.5).outY(-2).move();
    canvas.click();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button, '2, 2');",
        "    }",
        "    {",
        "      JButton existing = new JButton('Existing JButton');",
        "      add(existing, '2, 4');",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_beforeFirstColumn() throws Exception {
    openPanel(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    {",
        "      JButton existing = new JButton('Existing JButton');",
        "      add(existing, '2, 2');",
        "    }",
        "  }",
        "}");
    ComponentInfo existing = getJavaInfoByName("existing");
    //
    loadButtonWithText();
    canvas.target(existing).inY(0.5).outX(-2).move();
    canvas.click();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button, '2, 2');",
        "    }",
        "    {",
        "      JButton existing = new JButton('Existing JButton');",
        "      add(existing, '4, 2');",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_insertRow_endOfComponent_noGapNext() throws Exception {
    openPanel(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    {",
        "      JButton button_1 = new JButton('JButton 1');",
        "      add(button_1, '2, 1');",
        "    }",
        "    {",
        "      JButton button_2 = new JButton('JButton 2');",
        "      add(button_2, '2, 2');",
        "    }",
        "  }",
        "}");
    ComponentInfo button_1 = getJavaInfoByName("button_1");
    //
    loadButtonWithText();
    canvas.target(button_1).inX(0.5).inY(-2).move();
    canvas.click();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    {",
        "      JButton button_1 = new JButton('JButton 1');",
        "      add(button_1, '2, 1');",
        "    }",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button, '2, 3');",
        "    }",
        "    {",
        "      JButton button_2 = new JButton('JButton 2');",
        "      add(button_2, '2, 4');",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_insertRow_beginOfComponent_noGapPrev() throws Exception {
    openPanel(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    {",
        "      JButton button_1 = new JButton('JButton 1');",
        "      add(button_1, '2, 1');",
        "    }",
        "    {",
        "      JButton button_2 = new JButton('JButton 2');",
        "      add(button_2, '2, 2');",
        "    }",
        "  }",
        "}");
    ComponentInfo button_2 = getJavaInfoByName("button_2");
    //
    loadButtonWithText();
    canvas.target(button_2).inX(0.5).inY(2).move();
    canvas.click();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    {",
        "      JButton button_1 = new JButton('JButton 1');",
        "      add(button_1, '2, 1');",
        "    }",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button, '2, 3');",
        "    }",
        "    {",
        "      JButton button_2 = new JButton('JButton 2');",
        "      add(button_2, '2, 4');",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_insertColumn_endOfComponent_noGapNext() throws Exception {
    openPanel(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    {",
        "      JButton button_1 = new JButton('JButton 1');",
        "      add(button_1, '1, 2');",
        "    }",
        "    {",
        "      JButton button_2 = new JButton('JButton 2');",
        "      add(button_2, '2, 2');",
        "    }",
        "  }",
        "}");
    ComponentInfo button_1 = getJavaInfoByName("button_1");
    //
    loadButtonWithText();
    canvas.target(button_1).inY(0.5).inX(-2).move();
    canvas.click();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    {",
        "      JButton button_1 = new JButton('JButton 1');",
        "      add(button_1, '1, 2');",
        "    }",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button, '3, 2');",
        "    }",
        "    {",
        "      JButton button_2 = new JButton('JButton 2');",
        "      add(button_2, '4, 2');",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_insertColumn_beginOfComponent_noGapPrev() throws Exception {
    openPanel(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    {",
        "      JButton button_1 = new JButton('JButton 1');",
        "      add(button_1, '1, 2');",
        "    }",
        "    {",
        "      JButton button_2 = new JButton('JButton 2');",
        "      add(button_2, '2, 2');",
        "    }",
        "  }",
        "}");
    ComponentInfo button_2 = getJavaInfoByName("button_2");
    //
    loadButtonWithText();
    canvas.target(button_2).inY(0.5).inX(2).move();
    canvas.click();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    {",
        "      JButton button_1 = new JButton('JButton 1');",
        "      add(button_1, '1, 2');",
        "    }",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button, '3, 2');",
        "    }",
        "    {",
        "      JButton button_2 = new JButton('JButton 2');",
        "      add(button_2, '4, 2');",
        "    }",
        "  }",
        "}");
  }

  /**
   * There was problem in special case when there are columns, but no rows.
   */
  public void test_CREATE_whenNoRows() throws Exception {
    openPanel(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {}));",
        "  }",
        "}");
    //
    loadButtonWithText();
    canvas.moveTo(panel, COLUMN_GAP, ROW_GAP);
    canvas.assertCommandNotNull();
    canvas.click();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button, '2, 2');",
        "    }",
        "  }",
        "}");
  }

  /**
   * There was problem in special case when there are rows, but no columns.
   */
  public void test_CREATE_whenNoColumns() throws Exception {
    openPanel(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {},",
        "      new RowSpec[] {",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "  }",
        "}");
    //
    loadButtonWithText();
    canvas.moveTo(panel, COLUMN_GAP, ROW_GAP);
    canvas.assertCommandNotNull();
    canvas.click();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button, '2, 2');",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE and inherited layout (can not change dimensions)
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When {@link FormLayoutInfo} is inherited, we can not change its dimensions.
   */
  public void test_CREATE_inherited_columnOperations() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends JPanel {",
            "  public MyPanel() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.DEFAULT_ROWSPEC,}));",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    openPanel(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    {",
        "      JButton button_1 = new JButton('JButton 1');",
        "      add(button_1, '1, 1');",
        "    }",
        "    {",
        "      JButton button_2 = new JButton('JButton 2');",
        "      add(button_2, '2, 1');",
        "    }",
        "  }",
        "}");
    loadButton();
    // can not insert column
    {
      ComponentInfo button_1 = getJavaInfoByName("button_1");
      canvas.target(button_1).inY(0.5).inX(-2).move();
      canvas.assertCommandNull();
    }
    // can not append column
    {
      ComponentInfo button_2 = getJavaInfoByName("button_2");
      canvas.target(button_2).inY(0.5).outX(COLUMN_GAP + 1).move();
      canvas.assertCommandNull();
    }
  }

  /**
   * When {@link FormLayoutInfo} is inherited, we can not change its dimensions.
   */
  public void test_CREATE_inherited_rowOperations() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends JPanel {",
            "  public MyPanel() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.DEFAULT_ROWSPEC,",
            "        FormSpecs.DEFAULT_ROWSPEC,}));",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    openPanel(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    {",
        "      JButton button_1 = new JButton('JButton 1');",
        "      add(button_1, '1, 1');",
        "    }",
        "    {",
        "      JButton button_2 = new JButton('JButton 2');",
        "      add(button_2, '1, 2');",
        "    }",
        "  }",
        "}");
    loadButton();
    // can not insert row
    {
      ComponentInfo button_1 = getJavaInfoByName("button_1");
      canvas.target(button_1).inX(0.5).inY(-2).move();
      canvas.assertCommandNull();
    }
    // can not append row
    {
      ComponentInfo button_2 = getJavaInfoByName("button_2");
      canvas.target(button_2).inX(0.5).outY(ROW_GAP + 1).move();
      waitEventLoop(1000 * 5);
      canvas.assertCommandNull();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PASTE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_PASTE_virtual_4x2() throws Exception {
    openPanel(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    {",
        "      JButton existing = new JButton('My JButton');",
        "      add(existing, '2, 2');",
        "    }",
        "  }",
        "}");
    ComponentInfo existing = getJavaInfoByName("existing");
    // do copy/paste
    doCopyPaste(existing);
    // bad target
    {
      canvas.target(existing).in(0.5, 0.5).move();
      canvas.assertCommandNull();
    }
    // good target
    {
      canvas.target(existing).inY(0.5).outX(ROW_GAP + 1).move();
      canvas.click();
    }
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    {",
        "      JButton existing = new JButton('My JButton');",
        "      add(existing, '2, 2');",
        "    }",
        "    {",
        "      JButton button = new JButton('My JButton');",
        "      add(button, '4, 2');",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_MOVE_virtual_4x2() throws Exception {
    openPanel(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    {",
        "      JButton button = new JButton('My JButton');",
        "      add(button, '2, 2');",
        "    }",
        "  }",
        "}");
    ComponentInfo button = getJavaInfoByName("button");
    //
    canvas.beginDrag(button);
    canvas.target(button).inY(0.5).outX(ROW_GAP + 1).drag();
    canvas.endDrag();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    {",
        "      JButton button = new JButton('My JButton');",
        "      add(button, '4, 2');",
        "    }",
        "  }",
        "}");
  }

  public void test_ADD_virtual_2x2() throws Exception {
    openPanel(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new BorderLayout());",
        "    {",
        "      JButton button = new JButton('My JButton');",
        "      add(button, BorderLayout.NORTH);",
        "    }",
        "    {",
        "      JPanel inner = new JPanel();",
        "      add(inner, BorderLayout.CENTER);",
        "      inner.setLayout(new FormLayout());",
        "    }",
        "  }",
        "}");
    ComponentInfo button = getJavaInfoByName("button");
    panel = getJavaInfoByName("inner");
    //
    canvas.beginDrag(button);
    canvas.target(panel).in(COLUMN_GAP, ROW_GAP).drag();
    canvas.endDrag();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new BorderLayout());",
        "    {",
        "      JPanel inner = new JPanel();",
        "      add(inner, BorderLayout.CENTER);",
        "      inner.setLayout(new FormLayout(new ColumnSpec[] {",
        "          FormSpecs.RELATED_GAP_COLSPEC,",
        "          FormSpecs.DEFAULT_COLSPEC,},",
        "        new RowSpec[] {",
        "          FormSpecs.RELATED_GAP_ROWSPEC,",
        "          FormSpecs.DEFAULT_ROWSPEC,}));",
        "      {",
        "        JButton button = new JButton('My JButton');",
        "        inner.add(button, '2, 2');",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Column headers
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When {@link FormLayoutInfo} is inherited, we can not change its dimensions.
   */
  public void test_headerColumn_MOVE_inherited() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends JPanel {",
            "  public MyPanel() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        ColumnSpec.decode('150px'),",
            "        ColumnSpec.decode('100px'),},",
            "      new RowSpec[] {",
            "        FormSpecs.DEFAULT_ROWSPEC,}));",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    openPanel(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "  }",
        "}");
    // select panel to show headers
    canvas.select(panel);
    // animate headers
    {
      List<FormColumnInfo> columns = layout.getColumns();
      FormColumnInfo sourceColumn = columns.get(0);
      FormColumnInfo relativeColumn = columns.get(1);
      horizontalRobot.beginDrag(sourceColumn).dragTo(relativeColumn, -5, 0.5);
      horizontalRobot.assertCommandNull();
    }
  }

  public void test_headerColumn_MOVE_forward_targetGap() throws Exception {
    openPanel(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        new ColumnSpec('150px'),",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        new ColumnSpec('100px'),",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        new ColumnSpec('50px'),},",
        "      new RowSpec[] {",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "  }",
        "}");
    // select panel to show headers
    canvas.select(panel);
    // animate headers
    {
      List<FormColumnInfo> columns = layout.getColumns();
      FormColumnInfo sourceColumn = columns.get(1);
      FormColumnInfo relativeColumn = columns.get(3);
      horizontalRobot.beginDrag(sourceColumn).dragTo(relativeColumn, -5, 0.5);
      horizontalRobot.endDrag();
    }
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        ColumnSpec.decode('100px'),",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        ColumnSpec.decode('150px'),",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        ColumnSpec.decode('50px'),},",
        "      new RowSpec[] {",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "  }",
        "}");
  }

  public void test_headerColumn_MOVE_forward_targetNotGap() throws Exception {
    openPanel(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        new ColumnSpec('150px'),",
        "        new ColumnSpec('100px'),",
        "        new ColumnSpec('50px'),},",
        "      new RowSpec[] {",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "  }",
        "}");
    // select panel to show headers
    canvas.select(panel);
    // animate headers
    {
      List<FormColumnInfo> columns = layout.getColumns();
      FormColumnInfo sourceColumn = columns.get(0);
      FormColumnInfo relativeColumn = columns.get(1);
      horizontalRobot.beginDrag(sourceColumn).dragTo(relativeColumn, -5, 0.5);
      horizontalRobot.endDrag();
    }
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        ColumnSpec.decode('100px'),",
        "        ColumnSpec.decode('150px'),",
        "        ColumnSpec.decode('50px'),},",
        "      new RowSpec[] {",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "  }",
        "}");
  }

  public void test_headerColumn_MOVE_forward_targetBeforeGap() throws Exception {
    openPanel(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        new ColumnSpec('150px'),",
        "        new ColumnSpec('100px'),",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        new ColumnSpec('50px'),},",
        "      new RowSpec[] {",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "  }",
        "}");
    // select panel to show headers
    canvas.select(panel);
    // animate headers
    {
      List<FormColumnInfo> columns = layout.getColumns();
      FormColumnInfo sourceColumn = columns.get(0);
      FormColumnInfo relativeColumn = columns.get(3);
      horizontalRobot.beginDrag(sourceColumn).dragTo(relativeColumn, +5, 0.5);
      horizontalRobot.endDrag();
    }
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        ColumnSpec.decode('100px'),",
        "        ColumnSpec.decode('150px'),",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        ColumnSpec.decode('50px'),},",
        "      new RowSpec[] {",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "  }",
        "}");
  }

  public void test_headerColumn_MOVE_forward_targetLast() throws Exception {
    openPanel(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        new ColumnSpec('150px'),",
        "        new ColumnSpec('100px'),",
        "        new ColumnSpec('50px'),},",
        "      new RowSpec[] {",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "  }",
        "}");
    // select panel to show headers
    canvas.select(panel);
    // animate headers
    {
      List<FormColumnInfo> columns = layout.getColumns();
      FormColumnInfo sourceColumn = columns.get(0);
      FormColumnInfo relativeColumn = columns.get(2);
      horizontalRobot.beginDrag(sourceColumn).dragTo(relativeColumn, -5, 0.5);
      horizontalRobot.endDrag();
    }
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        ColumnSpec.decode('100px'),",
        "        ColumnSpec.decode('50px'),",
        "        ColumnSpec.decode('150px'),},",
        "      new RowSpec[] {",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Row headers
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When {@link FormLayoutInfo} is inherited, we can not change its dimensions.
   */
  public void test_headerRow_MOVE_inherited() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends JPanel {",
            "  public MyPanel() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        RowSpec.decode('100px'),",
            "        RowSpec.decode('75px'),}));",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    openPanel(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "  }",
        "}");
    // select panel to show headers
    canvas.select(panel);
    // animate headers
    {
      List<FormRowInfo> rows = layout.getRows();
      FormRowInfo sourceRow = rows.get(0);
      FormRowInfo relativeRow = rows.get(1);
      verticalRobot.beginDrag(sourceRow).dragTo(relativeRow, 0.5, -5);
      verticalRobot.assertCommandNull();
    }
  }

  public void test_headerRow_MOVE_forward_targetGap() throws Exception {
    openPanel(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        new RowSpec('100px'),",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        new RowSpec('75px'),",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        new RowSpec('50px'),}));",
        "  }",
        "}");
    // select panel to show headers
    canvas.select(panel);
    // animate headers
    {
      List<FormRowInfo> rows = layout.getRows();
      FormRowInfo sourceRow = rows.get(1);
      FormRowInfo relativeRow = rows.get(3);
      verticalRobot.beginDrag(sourceRow).dragTo(relativeRow, 0.5, -5);
      verticalRobot.endDrag();
    }
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        RowSpec.decode('75px'),",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        RowSpec.decode('100px'),",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        RowSpec.decode('50px'),}));",
        "  }",
        "}");
  }

  public void test_headerRow_MOVE_forward_targetNotGap() throws Exception {
    openPanel(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        new RowSpec('100px'),",
        "        new RowSpec('75px'),",
        "        new RowSpec('50px'),}));",
        "  }",
        "}");
    // select panel to show headers
    canvas.select(panel);
    // animate headers
    {
      List<FormRowInfo> rows = layout.getRows();
      FormRowInfo sourceRow = rows.get(0);
      FormRowInfo relativeRow = rows.get(1);
      verticalRobot.beginDrag(sourceRow).dragTo(relativeRow, 0.5, -5);
      verticalRobot.endDrag();
    }
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        RowSpec.decode('75px'),",
        "        RowSpec.decode('100px'),",
        "        RowSpec.decode('50px'),}));",
        "  }",
        "}");
  }

  public void test_headerRow_MOVE_forward_targetBeforeGap() throws Exception {
    openPanel(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        new RowSpec('100px'),",
        "        new RowSpec('75px'),",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        new RowSpec('50px'),}));",
        "  }",
        "}");
    // select panel to show headers
    canvas.select(panel);
    // animate headers
    {
      List<FormRowInfo> rows = layout.getRows();
      FormRowInfo sourceRow = rows.get(0);
      FormRowInfo relativeRow = rows.get(3);
      verticalRobot.beginDrag(sourceRow).dragTo(relativeRow, 0.5, +5);
      verticalRobot.endDrag();
    }
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        RowSpec.decode('75px'),",
        "        RowSpec.decode('100px'),",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        RowSpec.decode('50px'),}));",
        "  }",
        "}");
  }

  public void test_headerRow_MOVE_forward_targetLast() throws Exception {
    openPanel(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        new RowSpec('100px'),",
        "        new RowSpec('75px'),",
        "        new RowSpec('50px'),}));",
        "  }",
        "}");
    // select panel to show headers
    canvas.select(panel);
    // animate headers
    {
      List<FormRowInfo> rows = layout.getRows();
      FormRowInfo sourceRow = rows.get(0);
      FormRowInfo relativeRow = rows.get(2);
      verticalRobot.beginDrag(sourceRow).dragTo(relativeRow, 0.5, -5);
      verticalRobot.endDrag();
    }
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        RowSpec.decode('75px'),",
        "        RowSpec.decode('50px'),",
        "        RowSpec.decode('100px'),}));",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getTestSource(String... lines) {
    lines =
        CodeUtils.join(new String[]{
            "import com.jgoodies.forms.layout.*;",
            "import com.jgoodies.forms.factories.*;"}, lines);
    return super.getTestSource(lines);
  }

  private void openPanel(String... lines) throws Exception {
    panel = openContainer(lines);
    if (panel.getLayout() instanceof FormLayoutInfo) {
      layout = (FormLayoutInfo) panel.getLayout();
      IGridInfo grid = layout.getGridInfo();
      V_COLUMN_SIZE = grid.getVirtualColumnSize();
      V_ROW_SIZE = grid.getVirtualRowSize();
      COLUMN_GAP = grid.getVirtualColumnGap();
      ROW_GAP = grid.getVirtualRowGap();
    }
    horizontalRobot = new GraphicalRobot(m_headerHorizontal);
    verticalRobot = new GraphicalRobot(m_headerVertical);
  }
}
