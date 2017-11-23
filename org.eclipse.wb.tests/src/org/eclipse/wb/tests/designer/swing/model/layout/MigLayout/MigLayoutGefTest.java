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
package org.eclipse.wb.tests.designer.swing.model.layout.MigLayout;

import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.swing.MigLayout.model.MigColumnInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.MigLayoutInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.MigRowInfo;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;
import org.eclipse.wb.tests.designer.swing.model.layout.AbstractLayoutTest;
import org.eclipse.wb.tests.gef.GraphicalRobot;

import net.miginfocom.swing.MigLayout;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.GridBagLayout;
import java.util.List;

/**
 * Test {@link MigLayoutInfo} in GEF.
 * 
 * @author scheglov_ke
 */
public class MigLayoutGefTest extends SwingGefTest {
  private static int COL_GAP = 7;
  private static int ROW_GAP = 7;
  private static int COL_VIRTUAL_SIZE = 25;
  private static int ROW_VIRTUAL_SIZE = 25;
  private static int COL_INSET = 7;
  private static int ROW_INSET = 7;
  private ContainerInfo panel;
  private MigLayoutInfo layout;
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
    AbstractMigLayoutTest.do_configureNewProject();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Set MigLayout
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
    loadCreationTool("net.miginfocom.swing.MigLayout");
    canvas.moveTo(panel, 0.5, 0.5).click();
    assertEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[]', '[]'));",
        "  }",
        "}");
  }

  /**
   * When we replace {@link GridBagLayout} with {@link MigLayout} there was bug that we applied new
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
    loadCreationTool("net.miginfocom.swing.MigLayout");
    canvas.moveTo(panel, 0.5, 0.5).click();
    // no exceptions
    String source = m_lastEditor.getSource();
    assertThat(source).contains("new MigLayout(");
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
        "    setLayout(new MigLayout());",
        "    {",
        "      JButton button = new JButton('Existing JButton');",
        "      add(button, 'cell 0 0');",
        "    }",
        "  }",
        "}");
    ComponentInfo button = getJavaInfoByName("button");
    //
    loadButtonWithText();
    canvas.moveTo(button, 0.5, 0.55).click();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new MigLayout());",
        "    {",
        "      JButton button = new JButton('Existing JButton');",
        "      add(button, 'flowy,cell 0 0');",
        "    }",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button, 'cell 0 0');",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_empty() throws Exception {
    openPanel(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new MigLayout());",
        "  }",
        "}");
    //
    loadButtonWithText();
    canvas.moveTo(panel, COL_INSET, ROW_INSET).click();
    assertEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new MigLayout());",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button, 'cell 0 0');",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_virtual_1x1() throws Exception {
    openPanel(
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new MigLayout());",
        "  }",
        "}");
    //
    loadButtonWithText();
    int x = COL_INSET + COL_VIRTUAL_SIZE + COL_GAP + COL_VIRTUAL_SIZE / 2;
    int y = ROW_INSET + ROW_VIRTUAL_SIZE + ROW_GAP + ROW_VIRTUAL_SIZE / 2;
    canvas.moveTo(panel, x, y);
    canvas.click();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[][]', '[][]'));",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button, 'cell 1 1');",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_virtual_1x0() throws Exception {
    openPanel(
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new MigLayout());",
        "  }",
        "}");
    //
    loadButtonWithText();
    int x = COL_INSET + COL_VIRTUAL_SIZE + COL_GAP + COL_VIRTUAL_SIZE / 2;
    int y = ROW_INSET + ROW_VIRTUAL_SIZE / 2;
    canvas.moveTo(panel, x, y);
    canvas.click();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[][]', '[]'));",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button, 'cell 1 0');",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_virtual_0x1() throws Exception {
    openPanel(
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new MigLayout());",
        "  }",
        "}");
    //
    loadButtonWithText();
    int x = COL_INSET + COL_VIRTUAL_SIZE / 2;
    int y = ROW_INSET + ROW_VIRTUAL_SIZE + ROW_GAP + ROW_VIRTUAL_SIZE / 2;
    canvas.moveTo(panel, x, y);
    canvas.click();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[]', '[][]'));",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button, 'cell 0 1');",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_appendToColumn_0x1() throws Exception {
    openPanel(
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new MigLayout());",
        "    {",
        "      JButton existing = new JButton('Existing JButton');",
        "      add(existing, 'cell 0 0');",
        "    }",
        "  }",
        "}");
    ComponentInfo existing = getJavaInfoByName("existing");
    //
    loadButtonWithText();
    canvas.target(existing).inX(0.5).outY(ROW_GAP + 1).move();
    canvas.click();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[]', '[][]'));",
        "    {",
        "      JButton existing = new JButton('Existing JButton');",
        "      add(existing, 'cell 0 0');",
        "    }",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button, 'cell 0 1');",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_appendToRow_1x0() throws Exception {
    openPanel(
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new MigLayout());",
        "    {",
        "      JButton existing = new JButton('Existing JButton');",
        "      add(existing, 'cell 0 0');",
        "    }",
        "  }",
        "}");
    ComponentInfo existing = getJavaInfoByName("existing");
    //
    loadButtonWithText();
    canvas.target(existing).inY(0.5).outX(COL_GAP + 1).move();
    canvas.click();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[][]', '[]'));",
        "    {",
        "      JButton existing = new JButton('Existing JButton');",
        "      add(existing, 'cell 0 0');",
        "    }",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button, 'cell 1 0');",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_beforeFirstRow() throws Exception {
    openPanel(
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new MigLayout());",
        "    {",
        "      JButton existing = new JButton('Existing JButton');",
        "      add(existing, 'cell 0 0');",
        "    }",
        "  }",
        "}");
    ComponentInfo existing = getJavaInfoByName("existing");
    //
    loadButtonWithText();
    canvas.target(existing).inX(0.5).outY(-2).move();
    canvas.click();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[]', '[][]'));",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button, 'cell 0 0');",
        "    }",
        "    {",
        "      JButton existing = new JButton('Existing JButton');",
        "      add(existing, 'cell 0 1');",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_beforeFirstColumn() throws Exception {
    openPanel(
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new MigLayout());",
        "    {",
        "      JButton existing = new JButton('Existing JButton');",
        "      add(existing, 'cell 0 0');",
        "    }",
        "  }",
        "}");
    ComponentInfo existing = getJavaInfoByName("existing");
    //
    loadButtonWithText();
    canvas.target(existing).inY(0.5).outX(-2).move();
    canvas.click();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[][]', '[]'));",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button, 'cell 0 0');",
        "    }",
        "    {",
        "      JButton existing = new JButton('Existing JButton');",
        "      add(existing, 'cell 1 0');",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_insertRow_endOfComponent() throws Exception {
    openPanel(
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new MigLayout());",
        "    {",
        "      JButton button_1 = new JButton('Button 1');",
        "      add(button_1, 'cell 0 0');",
        "    }",
        "    {",
        "      JButton button_2 = new JButton('Button 2');",
        "      add(button_2, 'cell 0 1');",
        "    }",
        "  }",
        "}");
    ComponentInfo button_1 = getJavaInfoByName("button_1");
    //
    loadButtonWithText();
    canvas.target(button_1).inX(0.5).inY(-2).move();
    canvas.click();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[]', '[][][]'));",
        "    {",
        "      JButton button_1 = new JButton('Button 1');",
        "      add(button_1, 'cell 0 0');",
        "    }",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button, 'cell 0 1');",
        "    }",
        "    {",
        "      JButton button_2 = new JButton('Button 2');",
        "      add(button_2, 'cell 0 2');",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_insertColumn_endOfComponent() throws Exception {
    openPanel(
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new MigLayout());",
        "    {",
        "      JButton button_1 = new JButton('Button 1');",
        "      add(button_1, 'cell 0 0');",
        "    }",
        "    {",
        "      JButton button_2 = new JButton('Button 2');",
        "      add(button_2, 'cell 1 0');",
        "    }",
        "  }",
        "}");
    ComponentInfo button_1 = getJavaInfoByName("button_1");
    //
    loadButtonWithText();
    canvas.target(button_1).inY(0.5).inX(-2).move();
    canvas.click();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[][][]', '[]'));",
        "    {",
        "      JButton button_1 = new JButton('Button 1');",
        "      add(button_1, 'cell 0 0');",
        "    }",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button, 'cell 1 0');",
        "    }",
        "    {",
        "      JButton button_2 = new JButton('Button 2');",
        "      add(button_2, 'cell 2 0');",
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
   * When {@link MigLayoutInfo} is inherited, we can not change its dimensions.
   */
  public void test_CREATE_inherited_columnOperations() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends JPanel {",
            "  public MyPanel() {",
            "    setLayout(new MigLayout('', '[][]', '[]'));",
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
        "      add(button_1, 'cell 0 0');",
        "    }",
        "    {",
        "      JButton button_2 = new JButton('JButton 2');",
        "      add(button_2, 'cell 1 0');",
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
      canvas.target(button_2).inY(0.5).outX(COL_GAP + 1).move();
      canvas.assertCommandNull();
    }
  }

  /**
   * When {@link MigLayoutInfo} is inherited, we can not change its dimensions.
   */
  public void test_CREATE_inherited_rowOperations() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends JPanel {",
            "  public MyPanel() {",
            "    setLayout(new MigLayout('', '[]', '[][]'));",
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
        "      add(button_1, 'cell 0 0');",
        "    }",
        "    {",
        "      JButton button_2 = new JButton('JButton 2');",
        "      add(button_2, 'cell 0 1');",
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
  public void test_PASTE_virtual_1x0() throws Exception {
    openPanel(
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new MigLayout());",
        "    {",
        "      JButton existing = new JButton('My JButton');",
        "      add(existing, 'cell 0 0');",
        "    }",
        "  }",
        "}");
    ComponentInfo existing = getJavaInfoByName("existing");
    // do copy/paste
    doCopyPaste(existing);
    {
      canvas.target(existing).inY(0.5).outX(COL_GAP + 1).move();
      canvas.click();
    }
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[][]', '[]'));",
        "    {",
        "      JButton existing = new JButton('My JButton');",
        "      add(existing, 'cell 0 0');",
        "    }",
        "    {",
        "      JButton button = new JButton('My JButton');",
        "      add(button, 'cell 1 0');",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_MOVE_virtual_1x0() throws Exception {
    openPanel(
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new MigLayout());",
        "    {",
        "      JButton button = new JButton('My JButton');",
        "      add(button, 'cell 0 0');",
        "    }",
        "  }",
        "}");
    ComponentInfo button = getJavaInfoByName("button");
    //
    canvas.beginDrag(button);
    canvas.target(button).inY(0.5).outX(COL_GAP + 1).drag();
    canvas.endDrag();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[][]', '[]'));",
        "    {",
        "      JButton button = new JButton('My JButton');",
        "      add(button, 'cell 1 0');",
        "    }",
        "  }",
        "}");
  }

  public void test_ADD_virtual_0x0() throws Exception {
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
        "      inner.setLayout(new MigLayout());",
        "    }",
        "  }",
        "}");
    ComponentInfo button = getJavaInfoByName("button");
    panel = getJavaInfoByName("inner");
    //
    canvas.beginDrag(button);
    canvas.target(panel).in(COL_INSET, ROW_INSET).drag();
    canvas.endDrag();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new BorderLayout());",
        "    {",
        "      JPanel inner = new JPanel();",
        "      add(inner, BorderLayout.CENTER);",
        "      inner.setLayout(new MigLayout());",
        "      {",
        "        JButton button = new JButton('My JButton');",
        "        inner.add(button, 'cell 0 0');",
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
   * When {@link MigLayoutInfo} is inherited, we can not change its dimensions.
   */
  public void test_headerColumn_MOVE_inherited() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends JPanel {",
            "  public MyPanel() {",
            "    setLayout(new MigLayout('', '[150px][100px]', ''));",
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
      List<MigColumnInfo> columns = layout.getColumns();
      MigColumnInfo sourceColumn = columns.get(0);
      MigColumnInfo relativeColumn = columns.get(1);
      horizontalRobot.beginDrag(sourceColumn).dragTo(relativeColumn, -5, 0.5);
      horizontalRobot.assertCommandNull();
    }
  }

  public void test_headerColumn_MOVE_forward_beforeOther() throws Exception {
    openPanel(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[150px][100px][50px]', ''));",
        "  }",
        "}");
    // select panel to show headers
    canvas.select(panel);
    // animate headers
    {
      List<MigColumnInfo> columns = layout.getColumns();
      MigColumnInfo sourceColumn = columns.get(0);
      MigColumnInfo relativeColumn = columns.get(1);
      horizontalRobot.beginDrag(sourceColumn).dragTo(relativeColumn, -5, 0.5);
      horizontalRobot.endDrag();
    }
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[100px][150px][50px]', '[]'));",
        "  }",
        "}");
  }

  public void test_headerColumn_MOVE_forward_afterLast() throws Exception {
    openPanel(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[150px][100px][50px]', ''));",
        "  }",
        "}");
    // select panel to show headers
    canvas.select(panel);
    // animate headers
    {
      List<MigColumnInfo> columns = layout.getColumns();
      MigColumnInfo sourceColumn = columns.get(0);
      MigColumnInfo relativeColumn = columns.get(2);
      horizontalRobot.beginDrag(sourceColumn).dragTo(relativeColumn, -5, 0.5);
      horizontalRobot.endDrag();
    }
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[100px][50px][150px]', '[]'));",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Row headers
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When {@link MigLayoutInfo} is inherited, we can not change its dimensions.
   */
  public void test_headerRow_MOVE_inherited() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends JPanel {",
            "  public MyPanel() {",
            "    setLayout(new MigLayout('', '', '[100px][75px]'));",
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
      List<MigRowInfo> rows = layout.getRows();
      MigRowInfo sourceRow = rows.get(0);
      MigRowInfo relativeRow = rows.get(1);
      verticalRobot.beginDrag(sourceRow).dragTo(relativeRow, 0.5, -5);
      verticalRobot.assertCommandNull();
    }
  }

  public void test_headerRow_MOVE_forward_beforeOther() throws Exception {
    openPanel(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '', '[100px][75px][50px]'));",
        "  }",
        "}");
    // select panel to show headers
    canvas.select(panel);
    // animate headers
    {
      List<MigRowInfo> rows = layout.getRows();
      MigRowInfo sourceRow = rows.get(0);
      MigRowInfo relativeRow = rows.get(1);
      verticalRobot.beginDrag(sourceRow).dragTo(relativeRow, 0.5, -5);
      verticalRobot.endDrag();
    }
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[]', '[75px][100px][50px]'));",
        "  }",
        "}");
  }

  public void test_headerRow_MOVE_forward_afterLast() throws Exception {
    openPanel(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '', '[100px][75px][50px]'));",
        "  }",
        "}");
    // select panel to show headers
    canvas.select(panel);
    // animate headers
    {
      List<MigRowInfo> rows = layout.getRows();
      MigRowInfo sourceRow = rows.get(0);
      MigRowInfo relativeRow = rows.get(2);
      verticalRobot.beginDrag(sourceRow).dragTo(relativeRow, 0.5, -5);
      verticalRobot.endDrag();
    }
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[]', '[75px][50px][100px]'));",
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
            "import net.miginfocom.layout.*;",
            "import net.miginfocom.swing.*;"}, lines);
    return super.getTestSource(lines);
  }

  private void openPanel(String... lines) throws Exception {
    panel = openContainer(lines);
    if (panel.getLayout() instanceof MigLayoutInfo) {
      layout = (MigLayoutInfo) panel.getLayout();
      IGridInfo grid = layout.getGridInfo();
      COL_VIRTUAL_SIZE = grid.getVirtualColumnSize();
      ROW_VIRTUAL_SIZE = grid.getVirtualRowSize();
      COL_GAP = grid.getVirtualColumnGap();
      ROW_GAP = grid.getVirtualRowGap();
      COL_INSET = grid.getColumnIntervals()[0].begin();
      ROW_INSET = grid.getRowIntervals()[0].begin();
    }
    horizontalRobot = new GraphicalRobot(m_headerHorizontal);
    verticalRobot = new GraphicalRobot(m_headerVertical);
  }
}
