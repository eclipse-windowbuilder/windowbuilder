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

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.swing.FormLayout.Activator;
import org.eclipse.wb.internal.swing.FormLayout.model.FormLayoutInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.IPreferenceConstants;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.eclipse.jface.preference.IPreferenceStore;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * Tests for {@link FormLayoutInfo} and automatic alignment.
 *
 * @author scheglov_ke
 */
public class FormLayoutParametersTest extends AbstractFormLayoutTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void tearDown() throws Exception {
    IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
    preferences.setToDefault(IPreferenceConstants.P_ENABLE_GRAB);
    preferences.setToDefault(IPreferenceConstants.P_ENABLE_RIGHT_ALIGNMENT);
    super.tearDown();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link JTextField} marked as required horizontal grab/fill.
   */
  public void test_CREATE_Text() throws Exception {
    final ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.DEFAULT_ROWSPEC,}));",
            "  }",
            "}");
    // create
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
        ComponentInfo newComponent = createComponent(JTextField.class);
        layout.command_CREATE(newComponent, 1, false, 1, false);
      }
    });
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        ColumnSpec.decode('default:grow'),},",
        "      new RowSpec[] {",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    {",
        "      JTextField textField = new JTextField();",
        "      add(textField, '1, 1, fill, default');",
        "      textField.setColumns(10);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test that horizontal grab/fill for {@link JTextField} can be disabled.
   */
  public void test_CREATE_Text_disabled() throws Exception {
    final ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.DEFAULT_ROWSPEC,}));",
            "  }",
            "}");
    // create
    Activator.getDefault().getPreferenceStore().setValue(IPreferenceConstants.P_ENABLE_GRAB, false);
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
        ComponentInfo newComponent = createComponent(JTextField.class);
        layout.command_CREATE(newComponent, 1, false, 1, false);
      }
    });
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,},",
        "      new RowSpec[] {",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    {",
        "      JTextField textField = new JTextField();",
        "      add(textField, '1, 1');",
        "      textField.setColumns(10);",
        "    }",
        "  }",
        "}");
  }

  /**
   * For {@link JTable} marked as required horizontal/vertical grab/fill.
   */
  public void test_CREATE_Table() throws Exception {
    final ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.DEFAULT_ROWSPEC,}));",
            "  }",
            "}");
    // create
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
        ComponentInfo newComponent = createComponent(JTable.class);
        layout.command_CREATE(newComponent, 1, false, 1, false);
      }
    });
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        ColumnSpec.decode('default:grow'),},",
        "      new RowSpec[] {",
        "        RowSpec.decode('default:grow'),}));",
        "    {",
        "      JTable table = new JTable();",
        "      add(table, '1, 1, fill, fill');",
        "    }",
        "  }",
        "}");
  }

  /**
   * For {@link JTable} marked as required horizontal/vertical grab/fill.
   * <p>
   * However here we drop {@link JTable} on "implicit" layout, so can not change its dimensions.
   */
  public void test_CREATE_Table_onImplicit() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public MyPanel() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,},",
            "      new RowSpec[] {",
            "        FormSpecs.DEFAULT_ROWSPEC,}));",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    final ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    // create
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
        ComponentInfo newComponent = createComponent(JTable.class);
        layout.command_CREATE(newComponent, 1, false, 1, false);
      }
    });
    // check source
    assertEditor(
        "// filler filler filler",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    {",
        "      JTable table = new JTable();",
        "      add(table, '1, 1, fill, fill');",
        "    }",
        "  }",
        "}");
  }

  /**
   * {@link JLabel} is marked as "right" aligned and next widget is {@link JTextField}, so when add
   * {@link JLabel} before {@link JTextField}, use "right" alignment.
   */
  public void test_CREATE_LabelBeforeText() throws Exception {
    final ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,",
            "        ColumnSpec.decode('default:grow'),},",
            "      new RowSpec[] {",
            "        FormSpecs.DEFAULT_ROWSPEC,",
            "        RowSpec.decode('default:grow'),}));",
            "    {",
            "      JTextField textField = new JTextField();",
            "      add(textField, '2, 1, fill, default');",
            "    }",
            "  }",
            "}");
    // create
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
        ComponentInfo newComponent = createComponent(JLabel.class);
        layout.command_CREATE(newComponent, 1, false, 1, false);
      }
    });
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,",
        "        ColumnSpec.decode('default:grow'),},",
        "      new RowSpec[] {",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        RowSpec.decode('default:grow'),}));",
        "    {",
        "      JLabel label = new JLabel('New label');",
        "      add(label, '1, 1, right, default');",
        "    }",
        "    {",
        "      JTextField textField = new JTextField();",
        "      add(textField, '2, 1, fill, default');",
        "    }",
        "  }",
        "}");
  }

  /**
   * {@link JLabel} is marked as "right" aligned and next widget is {@link JTextField}, so when add
   * {@link JLabel} before {@link JTextField}, use "right" alignment.<br>
   * Variant with gap between {@link JLabel} and {@link JTextField}.
   */
  public void test_CREATE_LabelBeforeText_gap() throws Exception {
    final ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,",
            "        FormSpecs.RELATED_GAP_COLSPEC,",
            "        ColumnSpec.decode('default:grow'),},",
            "      new RowSpec[] {",
            "        FormSpecs.DEFAULT_ROWSPEC,",
            "        RowSpec.decode('default:grow'),}));",
            "    {",
            "      JTextField textField = new JTextField();",
            "      add(textField, '3, 1, fill, default');",
            "    }",
            "  }",
            "}");
    // create
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
        ComponentInfo newComponent = createComponent(JLabel.class);
        layout.command_CREATE(newComponent, 1, false, 1, false);
      }
    });
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        ColumnSpec.decode('default:grow'),},",
        "      new RowSpec[] {",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        RowSpec.decode('default:grow'),}));",
        "    {",
        "      JLabel label = new JLabel('New label');",
        "      add(label, '1, 1, right, default');",
        "    }",
        "    {",
        "      JTextField textField = new JTextField();",
        "      add(textField, '3, 1, fill, default');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Check that automatic "right alignment" feature for {@link JLabel} can be disabled.
   */
  public void test_CREATE_LabelBeforeText_disabled() throws Exception {
    final ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,",
            "        ColumnSpec.decode('default:grow'),},",
            "      new RowSpec[] {",
            "        FormSpecs.DEFAULT_ROWSPEC,",
            "        RowSpec.decode('default:grow'),}));",
            "    {",
            "      JTextField textField = new JTextField();",
            "      add(textField, '2, 1, fill, default');",
            "    }",
            "  }",
            "}");
    // create
    Activator.getDefault().getPreferenceStore().setValue(
        IPreferenceConstants.P_ENABLE_RIGHT_ALIGNMENT,
        false);
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
        ComponentInfo newComponent = createComponent(JLabel.class);
        layout.command_CREATE(newComponent, 1, false, 1, false);
      }
    });
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,",
        "        ColumnSpec.decode('default:grow'),},",
        "      new RowSpec[] {",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        RowSpec.decode('default:grow'),}));",
        "    {",
        "      JLabel label = new JLabel('New label');",
        "      add(label, '1, 1');",
        "    }",
        "    {",
        "      JTextField textField = new JTextField();",
        "      add(textField, '2, 1, fill, default');",
        "    }",
        "  }",
        "}");
  }

  /**
   * {@link JLabel} is marked as "right" aligned and next widget is {@link JTextField}, so when add
   * {@link JTextField} after {@link JLabel} , use "right" alignment for {@link JLabel}.
   */
  public void test_CREATE_TextAfterLabel() throws Exception {
    final ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,",
            "        ColumnSpec.decode('default:grow'),},",
            "      new RowSpec[] {",
            "        FormSpecs.DEFAULT_ROWSPEC,",
            "        RowSpec.decode('default:grow'),}));",
            "    {",
            "      JLabel label = new JLabel('New label');",
            "      add(label, '1, 1, right, default');",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // add new component
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
        ComponentInfo newComponent = createComponent(JTextField.class);
        layout.command_CREATE(newComponent, 2, false, 1, false);
      }
    });
    // check result
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,",
        "        ColumnSpec.decode('default:grow'),},",
        "      new RowSpec[] {",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        RowSpec.decode('default:grow'),}));",
        "    {",
        "      JLabel label = new JLabel('New label');",
        "      add(label, '1, 1, right, default');",
        "    }",
        "    {",
        "      JTextField textField = new JTextField();",
        "      add(textField, '2, 1, fill, default');",
        "      textField.setColumns(10);",
        "    }",
        "  }",
        "}");
  }

  /**
   * {@link JLabel} is marked as "right" aligned and next widget is {@link JTextField}, so when add
   * {@link JTextField} after {@link JLabel} , use "right" alignment for {@link JLabel}.<br>
   * Variant with gap between {@link JLabel} and {@link JTextField}.
   */
  public void test_CREATE_TextAfterLabel_gap() throws Exception {
    final ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {",
            "        FormSpecs.DEFAULT_COLSPEC,",
            "        FormSpecs.RELATED_GAP_COLSPEC,",
            "        ColumnSpec.decode('default:grow'),},",
            "      new RowSpec[] {",
            "        FormSpecs.DEFAULT_ROWSPEC,",
            "        RowSpec.decode('default:grow'),}));",
            "    {",
            "      JLabel label = new JLabel('New label');",
            "      add(label, '1, 1, right, default');",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // add new component
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        FormLayoutInfo layout = (FormLayoutInfo) panel.getLayout();
        ComponentInfo newComponent = createComponent(JTextField.class);
        layout.command_CREATE(newComponent, 3, false, 1, false);
      }
    });
    // check result
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.DEFAULT_COLSPEC,",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        ColumnSpec.decode('default:grow'),},",
        "      new RowSpec[] {",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        RowSpec.decode('default:grow'),}));",
        "    {",
        "      JLabel label = new JLabel('New label');",
        "      add(label, '1, 1, right, default');",
        "    }",
        "    {",
        "      JTextField textField = new JTextField();",
        "      add(textField, '3, 1, fill, default');",
        "      textField.setColumns(10);",
        "    }",
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
}
