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

import org.eclipse.wb.internal.swing.FormLayout.model.FormLayoutInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.FormRowInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

/**
 * Test for {@link FormRowInfo}.
 * 
 * @author scheglov_ke
 */
public class FormRowInfoTest extends AbstractFormLayoutTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Insert
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_insert_onRow() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.DEFAULT_ROWSPEC,",
            "        FormSpecs.UNRELATED_GAP_ROWSPEC,",
            "        FormSpecs.PREF_ROWSPEC,}));",
            "    {",
            "      JButton button = new JButton();",
            "      add(button, '1, 3');",
            "    }",
            "  }",
            "}");
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    //
    panel.refresh();
    try {
      layout.insertRow(2);
    } finally {
      panel.refresh_dispose();
    }
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        FormSpecs.UNRELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.PREF_ROWSPEC,}));",
        "    {",
        "      JButton button = new JButton();",
        "      add(button, '1, 5');",
        "    }",
        "  }",
        "}");
  }

  public void test_insert_onGap() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC},",
            "      new RowSpec[] {",
            "        FormSpecs.DEFAULT_ROWSPEC,",
            "        FormSpecs.UNRELATED_GAP_ROWSPEC,",
            "        FormSpecs.PREF_ROWSPEC,}));",
            "    {",
            "      JButton button = new JButton();",
            "      add(button, '1, 3');",
            "    }",
            "  }",
            "}");
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    //
    panel.refresh();
    try {
      layout.insertRow(1);
    } finally {
      panel.refresh_dispose();
    }
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        FormSpecs.UNRELATED_GAP_ROWSPEC,",
        "        FormSpecs.PREF_ROWSPEC,}));",
        "    {",
        "      JButton button = new JButton();",
        "      add(button, '1, 5');",
        "    }",
        "  }",
        "}");
  }

  public void test_insert_lastWithGap() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.DEFAULT_ROWSPEC,",
            "        FormSpecs.UNRELATED_GAP_ROWSPEC,}));",
            "  }",
            "}");
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    //
    panel.refresh();
    try {
      layout.insertRow(2);
    } finally {
      panel.refresh_dispose();
    }
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        FormSpecs.UNRELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "  }",
        "}");
  }

  public void test_insert_lastWithoutGap() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.PREF_ROWSPEC,}));",
            "  }",
            "}");
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    //
    panel.refresh();
    try {
      layout.insertRow(1);
    } finally {
      panel.refresh_dispose();
    }
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.PREF_ROWSPEC,",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_deleteRow_nextGap() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.DEFAULT_ROWSPEC,",
            "        FormSpecs.RELATED_GAP_ROWSPEC,",
            "        FormSpecs.PREF_ROWSPEC,}));",
            "    {",
            "      JButton button = new JButton('111');",
            "      add(button, '1, 1');",
            "    }",
            "    {",
            "      JButton button = new JButton('333');",
            "      add(button, '1, 3');",
            "    }",
            "  }",
            "}");
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    //
    panel.refresh();
    try {
      layout.deleteRow(0);
    } finally {
      panel.refresh_dispose();
    }
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.PREF_ROWSPEC,}));",
        "    {",
        "      JButton button = new JButton('333');",
        "      add(button, '1, 1');",
        "    }",
        "  }",
        "}");
  }

  public void test_deleteRow_span() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.DEFAULT_ROWSPEC,",
            "        FormSpecs.MIN_ROWSPEC,",
            "        FormSpecs.PREF_ROWSPEC,}));",
            "    {",
            "      JButton button = new JButton('111');",
            "      add(button, '1, 1, 1, 2');",
            "    }",
            "    {",
            "      JButton button = new JButton('333');",
            "      add(button, '1, 3');",
            "    }",
            "  }",
            "}");
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    //
    panel.refresh();
    try {
      layout.deleteRow(1);
    } finally {
      panel.refresh_dispose();
    }
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        FormSpecs.PREF_ROWSPEC,}));",
        "    {",
        "      JButton button = new JButton('111');",
        "      add(button, '1, 1');",
        "    }",
        "    {",
        "      JButton button = new JButton('333');",
        "      add(button, '1, 2');",
        "    }",
        "  }",
        "}");
  }

  public void test_deleteRow_prevGap() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.RELATED_GAP_ROWSPEC,",
            "        FormSpecs.DEFAULT_ROWSPEC,",
            "        FormSpecs.UNRELATED_GAP_ROWSPEC,",
            "        FormSpecs.PREF_ROWSPEC,}));",
            "    {",
            "      JButton button = new JButton('111');",
            "      add(button, '1, 2');",
            "    }",
            "    {",
            "      JButton button = new JButton('333');",
            "      add(button, '1, 4');",
            "    }",
            "  }",
            "}");
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    //
    panel.refresh();
    try {
      layout.deleteRow(1);
    } finally {
      panel.refresh_dispose();
    }
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.UNRELATED_GAP_ROWSPEC,",
        "        FormSpecs.PREF_ROWSPEC,}));",
        "    {",
        "      JButton button = new JButton('333');",
        "      add(button, '1, 2');",
        "    }",
        "  }",
        "}");
  }

  public void test_deleteGap() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.DEFAULT_ROWSPEC,",
            "        FormSpecs.UNRELATED_GAP_ROWSPEC,",
            "        FormSpecs.PREF_ROWSPEC,}));",
            "    {",
            "      JButton button = new JButton('111');",
            "      add(button, '1, 1');",
            "    }",
            "    {",
            "      JButton button = new JButton('333');",
            "      add(button, '1, 3');",
            "    }",
            "  }",
            "}");
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    //
    panel.refresh();
    try {
      layout.deleteRow(1);
    } finally {
      panel.refresh_dispose();
    }
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        FormSpecs.PREF_ROWSPEC,}));",
        "    {",
        "      JButton button = new JButton('111');",
        "      add(button, '1, 1');",
        "    }",
        "    {",
        "      JButton button = new JButton('333');",
        "      add(button, '1, 2');",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete contents
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_deleteContents() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.DEFAULT_ROWSPEC,}));",
            "    {",
            "      JButton button = new JButton();",
            "      add(button, '1, 1');",
            "    }",
            "  }",
            "}");
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    //
    panel.refresh();
    try {
      layout.deleteRowContents(0);
    } finally {
      panel.refresh_dispose();
    }
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Split
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_split() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.PREF_ROWSPEC,",
            "        FormSpecs.MIN_ROWSPEC,}));",
            "    {",
            "      JButton button = new JButton();",
            "      add(button, '1, 1');",
            "    }",
            "    {",
            "      JButton button = new JButton();",
            "      add(button, '1, 2');",
            "    }",
            "  }",
            "}");
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    //
    panel.refresh();
    try {
      layout.splitRow(0);
    } finally {
      panel.refresh_dispose();
    }
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.PREF_ROWSPEC,",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.PREF_ROWSPEC,",
        "        FormSpecs.MIN_ROWSPEC,}));",
        "    {",
        "      JButton button = new JButton();",
        "      add(button, '1, 1, 1, 3');",
        "    }",
        "    {",
        "      JButton button = new JButton();",
        "      add(button, '1, 4');",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE row
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_move_noOp() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.DEFAULT_ROWSPEC,}));",
            "    {",
            "      JButton button = new JButton();",
            "      add(button, '1, 1');",
            "    }",
            "  }",
            "}");
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    // move
    panel.refresh();
    try {
      String expectedSource = m_lastEditor.getSource();
      layout.command_MOVE_ROW(0, 0);
      assertEditor(expectedSource, m_lastEditor);
    } finally {
      panel.refresh_dispose();
    }
  }

  public void test_move_backward_gap() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.PARAGRAPH_GAP_ROWSPEC,",
            "        FormSpecs.MIN_ROWSPEC,",
            "        FormSpecs.UNRELATED_GAP_ROWSPEC,}));",
            "    {",
            "      JButton button = new JButton();",
            "      add(button, '1, 2');",
            "    }",
            "  }",
            "}");
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    // move
    panel.refresh();
    try {
      layout.command_MOVE_ROW(2, 1);
    } finally {
      panel.refresh_dispose();
    }
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.PARAGRAPH_GAP_ROWSPEC,",
        "        FormSpecs.UNRELATED_GAP_ROWSPEC,",
        "        FormSpecs.MIN_ROWSPEC,}));",
        "    {",
        "      JButton button = new JButton();",
        "      add(button, '1, 3');",
        "    }",
        "  }",
        "}");
  }

  public void test_move_forward_gap() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.PARAGRAPH_GAP_ROWSPEC,",
            "        FormSpecs.MIN_ROWSPEC,",
            "        FormSpecs.UNRELATED_GAP_ROWSPEC,}));",
            "    {",
            "      JButton button = new JButton();",
            "      add(button, '1, 2');",
            "    }",
            "  }",
            "}");
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    // move
    panel.refresh();
    try {
      layout.command_MOVE_ROW(0, 2);
    } finally {
      panel.refresh_dispose();
    }
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.MIN_ROWSPEC,",
        "        FormSpecs.PARAGRAPH_GAP_ROWSPEC,",
        "        FormSpecs.UNRELATED_GAP_ROWSPEC,}));",
        "    {",
        "      JButton button = new JButton();",
        "      add(button, '1, 1');",
        "    }",
        "  }",
        "}");
  }

  public void test_move_noGaps() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.DEFAULT_ROWSPEC,",
            "        FormSpecs.MIN_ROWSPEC,",
            "        FormSpecs.PREF_ROWSPEC,}));",
            "  }",
            "}");
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    // backward
    {
      panel.refresh();
      try {
        layout.command_MOVE_ROW(2, 1);
      } finally {
        panel.refresh_dispose();
      }
      // check source
      assertEditor(
          "public class Test extends JPanel {",
          "  public Test() {",
          "    setLayout(new FormLayout(new ColumnSpec[] {",
          "        FormSpecs.DEFAULT_COLSPEC,},",
          "      new RowSpec[] {",
          "        FormSpecs.DEFAULT_ROWSPEC,",
          "        FormSpecs.PREF_ROWSPEC,",
          "        FormSpecs.MIN_ROWSPEC,}));",
          "  }",
          "}");
    }
    // forward
    {
      panel.refresh();
      try {
        layout.command_MOVE_ROW(0, 2);
      } finally {
        panel.refresh_dispose();
      }
      // check source
      assertEditor(
          "public class Test extends JPanel {",
          "  public Test() {",
          "    setLayout(new FormLayout(new ColumnSpec[] {",
          "        FormSpecs.DEFAULT_COLSPEC,},",
          "      new RowSpec[] {",
          "        FormSpecs.PREF_ROWSPEC,",
          "        FormSpecs.DEFAULT_ROWSPEC,",
          "        FormSpecs.MIN_ROWSPEC,}));",
          "  }",
          "}");
    }
  }

  public void test_move_backward_Last2Inner() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.PARAGRAPH_GAP_ROWSPEC,",
            "        FormSpecs.MIN_ROWSPEC,",
            "        FormSpecs.RELATED_GAP_ROWSPEC,",
            "        FormSpecs.PREF_ROWSPEC,",
            "        FormSpecs.UNRELATED_GAP_ROWSPEC,",
            "        FormSpecs.GLUE_ROWSPEC,}));",
            "    {",
            "      JButton button = new JButton('222');",
            "      add(button, '1, 2');",
            "    }",
            "    {",
            "      JButton button = new JButton('444');",
            "      add(button, '1, 4');",
            "    }",
            "    {",
            "      JButton button = new JButton('666');",
            "      add(button, '1, 6');",
            "    }",
            "  }",
            "}");
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    // move
    panel.refresh();
    try {
      layout.command_MOVE_ROW(5, 2);
    } finally {
      panel.refresh_dispose();
    }
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.PARAGRAPH_GAP_ROWSPEC,",
        "        FormSpecs.MIN_ROWSPEC,",
        "        FormSpecs.UNRELATED_GAP_ROWSPEC,",
        "        FormSpecs.GLUE_ROWSPEC,",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.PREF_ROWSPEC,}));",
        "    {",
        "      JButton button = new JButton('222');",
        "      add(button, '1, 2');",
        "    }",
        "    {",
        "      JButton button = new JButton('444');",
        "      add(button, '1, 6');",
        "    }",
        "    {",
        "      JButton button = new JButton('666');",
        "      add(button, '1, 4');",
        "    }",
        "  }",
        "}");
  }

  public void test_move_backward_Last2First() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.RELATED_GAP_ROWSPEC,",
            "        FormSpecs.PREF_ROWSPEC,",
            "        FormSpecs.UNRELATED_GAP_ROWSPEC,",
            "        FormSpecs.GLUE_ROWSPEC,}));",
            "    {",
            "      JButton button = new JButton('222');",
            "      add(button, '1, 2');",
            "    }",
            "    {",
            "      JButton button = new JButton('444');",
            "      add(button, '1, 4');",
            "    }",
            "  }",
            "}");
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    // move
    panel.refresh();
    try {
      layout.command_MOVE_ROW(3, 0);
    } finally {
      panel.refresh_dispose();
    }
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.UNRELATED_GAP_ROWSPEC,",
        "        FormSpecs.GLUE_ROWSPEC,",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.PREF_ROWSPEC,}));",
        "    {",
        "      JButton button = new JButton('222');",
        "      add(button, '1, 4');",
        "    }",
        "    {",
        "      JButton button = new JButton('444');",
        "      add(button, '1, 2');",
        "    }",
        "  }",
        "}");
  }

  public void test_move_backward_2FirstNoGap() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.MIN_ROWSPEC,",
            "        FormSpecs.PARAGRAPH_GAP_ROWSPEC,",
            "        FormSpecs.PREF_ROWSPEC,}));",
            "  }",
            "}");
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    // move
    panel.refresh();
    try {
      layout.command_MOVE_ROW(2, 0);
    } finally {
      panel.refresh_dispose();
    }
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.PREF_ROWSPEC,",
        "        FormSpecs.PARAGRAPH_GAP_ROWSPEC,",
        "        FormSpecs.MIN_ROWSPEC,}));",
        "  }",
        "}");
  }

  public void test_move_spanned_backwardExpand_forwardCollapse() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.RELATED_GAP_ROWSPEC,",
            "        FormSpecs.DEFAULT_ROWSPEC,",
            "        FormSpecs.RELATED_GAP_ROWSPEC,",
            "        FormSpecs.DEFAULT_ROWSPEC,",
            "        FormSpecs.RELATED_GAP_ROWSPEC,",
            "        FormSpecs.DEFAULT_ROWSPEC,}));",
            "    {",
            "      JButton button = new JButton('spanned');",
            "      add(button, '1, 2, 1, 3');",
            "    }",
            "    {",
            "      JButton button = new JButton();",
            "      add(button, '1, 6');",
            "    }",
            "  }",
            "}");
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    // backward expand
    {
      panel.refresh();
      try {
        layout.command_MOVE_ROW(5, 2);
      } finally {
        panel.refresh_dispose();
      }
      // check source
      assertEditor(
          "public class Test extends JPanel {",
          "  public Test() {",
          "    setLayout(new FormLayout(new ColumnSpec[] {",
          "        FormSpecs.DEFAULT_COLSPEC,",
          "        FormSpecs.DEFAULT_COLSPEC,},",
          "      new RowSpec[] {",
          "        FormSpecs.RELATED_GAP_ROWSPEC,",
          "        FormSpecs.DEFAULT_ROWSPEC,",
          "        FormSpecs.RELATED_GAP_ROWSPEC,",
          "        FormSpecs.DEFAULT_ROWSPEC,",
          "        FormSpecs.RELATED_GAP_ROWSPEC,",
          "        FormSpecs.DEFAULT_ROWSPEC,}));",
          "    {",
          "      JButton button = new JButton('spanned');",
          "      add(button, '1, 2, 1, 5');",
          "    }",
          "    {",
          "      JButton button = new JButton();",
          "      add(button, '1, 4');",
          "    }",
          "  }",
          "}");
    }
    // forward collapse
    {
      panel.refresh();
      try {
        layout.command_MOVE_ROW(3, 6);
      } finally {
        panel.refresh_dispose();
      }
      // check source
      assertEditor(
          "public class Test extends JPanel {",
          "  public Test() {",
          "    setLayout(new FormLayout(new ColumnSpec[] {",
          "        FormSpecs.DEFAULT_COLSPEC,",
          "        FormSpecs.DEFAULT_COLSPEC,},",
          "      new RowSpec[] {",
          "        FormSpecs.RELATED_GAP_ROWSPEC,",
          "        FormSpecs.DEFAULT_ROWSPEC,",
          "        FormSpecs.RELATED_GAP_ROWSPEC,",
          "        FormSpecs.DEFAULT_ROWSPEC,",
          "        FormSpecs.RELATED_GAP_ROWSPEC,",
          "        FormSpecs.DEFAULT_ROWSPEC,}));",
          "    {",
          "      JButton button = new JButton('spanned');",
          "      add(button, '1, 2, 1, 3');",
          "    }",
          "    {",
          "      JButton button = new JButton();",
          "      add(button, '1, 6');",
          "    }",
          "  }",
          "}");
    }
  }

  public void test_move_spanned_backwardCollapse_forwardExpand() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.RELATED_GAP_ROWSPEC,",
            "        FormSpecs.DEFAULT_ROWSPEC,",
            "        FormSpecs.RELATED_GAP_ROWSPEC,",
            "        FormSpecs.DEFAULT_ROWSPEC,",
            "        FormSpecs.RELATED_GAP_ROWSPEC,",
            "        FormSpecs.DEFAULT_ROWSPEC,",
            "        FormSpecs.RELATED_GAP_ROWSPEC,",
            "        FormSpecs.DEFAULT_ROWSPEC,}));",
            "    {",
            "      JButton button = new JButton('spanned');",
            "      add(button, '1, 4, 1, 5');",
            "    }",
            "    {",
            "      JButton button = new JButton();",
            "      add(button, '1, 6');",
            "    }",
            "  }",
            "}");
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    // backward collapse
    {
      panel.refresh();
      try {
        layout.command_MOVE_ROW(5, 2);
      } finally {
        panel.refresh_dispose();
      }
      // check source
      assertEditor(
          "public class Test extends JPanel {",
          "  public Test() {",
          "    setLayout(new FormLayout(new ColumnSpec[] {",
          "        FormSpecs.DEFAULT_COLSPEC,",
          "        FormSpecs.DEFAULT_COLSPEC,},",
          "      new RowSpec[] {",
          "        FormSpecs.RELATED_GAP_ROWSPEC,",
          "        FormSpecs.DEFAULT_ROWSPEC,",
          "        FormSpecs.RELATED_GAP_ROWSPEC,",
          "        FormSpecs.DEFAULT_ROWSPEC,",
          "        FormSpecs.RELATED_GAP_ROWSPEC,",
          "        FormSpecs.DEFAULT_ROWSPEC,",
          "        FormSpecs.RELATED_GAP_ROWSPEC,",
          "        FormSpecs.DEFAULT_ROWSPEC,}));",
          "    {",
          "      JButton button = new JButton('spanned');",
          "      add(button, '1, 6, 1, 3');",
          "    }",
          "    {",
          "      JButton button = new JButton();",
          "      add(button, '1, 4');",
          "    }",
          "  }",
          "}");
    }
    // forward expand
    {
      panel.refresh();
      try {
        layout.command_MOVE_ROW(3, 6);
      } finally {
        panel.refresh_dispose();
      }
      // check source
      assertEditor(
          "public class Test extends JPanel {",
          "  public Test() {",
          "    setLayout(new FormLayout(new ColumnSpec[] {",
          "        FormSpecs.DEFAULT_COLSPEC,",
          "        FormSpecs.DEFAULT_COLSPEC,},",
          "      new RowSpec[] {",
          "        FormSpecs.RELATED_GAP_ROWSPEC,",
          "        FormSpecs.DEFAULT_ROWSPEC,",
          "        FormSpecs.RELATED_GAP_ROWSPEC,",
          "        FormSpecs.DEFAULT_ROWSPEC,",
          "        FormSpecs.RELATED_GAP_ROWSPEC,",
          "        FormSpecs.DEFAULT_ROWSPEC,",
          "        FormSpecs.RELATED_GAP_ROWSPEC,",
          "        FormSpecs.DEFAULT_ROWSPEC,}));",
          "    {",
          "      JButton button = new JButton('spanned');",
          "      add(button, '1, 4, 1, 5');",
          "    }",
          "    {",
          "      JButton button = new JButton();",
          "      add(button, '1, 6');",
          "    }",
          "  }",
          "}");
    }
  }

  /**
   * Backward inner move.
   */
  public void test_move_forward_firstGapPrev2Last() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.PARAGRAPH_GAP_ROWSPEC,",
            "        FormSpecs.MIN_ROWSPEC,",
            "        FormSpecs.RELATED_GAP_ROWSPEC,",
            "        FormSpecs.GLUE_ROWSPEC,",
            "        FormSpecs.UNRELATED_GAP_ROWSPEC,",
            "        FormSpecs.PREF_ROWSPEC,}));",
            "    {",
            "      JButton button = new JButton('222');",
            "      add(button, '1, 2');",
            "    }",
            "    {",
            "      JButton button = new JButton('444');",
            "      add(button, '1, 4');",
            "    }",
            "    {",
            "      JButton button = new JButton('666');",
            "      add(button, '1, 6');",
            "    }",
            "  }",
            "}");
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    // move
    panel.refresh();
    try {
      layout.command_MOVE_ROW(1, 6);
    } finally {
      panel.refresh_dispose();
    }
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.GLUE_ROWSPEC,",
        "        FormSpecs.UNRELATED_GAP_ROWSPEC,",
        "        FormSpecs.PREF_ROWSPEC,",
        "        FormSpecs.PARAGRAPH_GAP_ROWSPEC,",
        "        FormSpecs.MIN_ROWSPEC,}));",
        "    {",
        "      JButton button = new JButton('222');",
        "      add(button, '1, 6');",
        "    }",
        "    {",
        "      JButton button = new JButton('444');",
        "      add(button, '1, 2');",
        "    }",
        "    {",
        "      JButton button = new JButton('666');",
        "      add(button, '1, 4');",
        "    }",
        "  }",
        "}");
  }

  public void test_move_forward_firstGapNext2Inner() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.MIN_ROWSPEC,",
            "        FormSpecs.PARAGRAPH_GAP_ROWSPEC,",
            "        FormSpecs.PREF_ROWSPEC,",
            "        FormSpecs.DEFAULT_ROWSPEC,}));",
            "  }",
            "}");
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    // move
    panel.refresh();
    try {
      layout.command_MOVE_ROW(0, 3);
    } finally {
      panel.refresh_dispose();
    }
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.PREF_ROWSPEC,",
        "        FormSpecs.MIN_ROWSPEC,",
        "        FormSpecs.PARAGRAPH_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "  }",
        "}");
  }

  public void test_move_forward_first2Last() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.MIN_ROWSPEC,",
            "        FormSpecs.PARAGRAPH_GAP_ROWSPEC,",
            "        FormSpecs.PREF_ROWSPEC,",
            "        FormSpecs.DEFAULT_ROWSPEC,}));",
            "  }",
            "}");
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    // move
    panel.refresh();
    try {
      layout.command_MOVE_ROW(0, 4);
    } finally {
      panel.refresh_dispose();
    }
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.PREF_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        FormSpecs.PARAGRAPH_GAP_ROWSPEC,",
        "        FormSpecs.MIN_ROWSPEC,}));",
        "  }",
        "}");
  }

  public void test_move_forward_firstNextGap2BeforeGap() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.MIN_ROWSPEC,",
            "        FormSpecs.PARAGRAPH_GAP_ROWSPEC,",
            "        FormSpecs.PREF_ROWSPEC,",
            "        FormSpecs.RELATED_GAP_ROWSPEC,",
            "        FormSpecs.DEFAULT_ROWSPEC,}));",
            "  }",
            "}");
    FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
    // move
    panel.refresh();
    try {
      layout.command_MOVE_ROW(0, 3);
    } finally {
      panel.refresh_dispose();
    }
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.PREF_ROWSPEC,",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.MIN_ROWSPEC,",
        "        FormSpecs.PARAGRAPH_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "  }",
        "}");
  }
}
