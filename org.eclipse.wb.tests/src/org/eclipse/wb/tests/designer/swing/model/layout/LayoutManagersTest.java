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

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildAddAfter;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.generation.statement.StatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.generation.statement.block.BlockStatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.generation.statement.flat.FlatStatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.generation.statement.lazy.LazyStatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.model.variable.description.FieldInitializerVariableDescription;
import org.eclipse.wb.internal.core.model.variable.description.FieldUniqueVariableDescription;
import org.eclipse.wb.internal.core.model.variable.description.LazyVariableDescription;
import org.eclipse.wb.internal.core.model.variable.description.LocalUniqueVariableDescription;
import org.eclipse.wb.internal.core.model.variable.description.VariableSupportDescription;
import org.eclipse.wb.internal.swing.Activator;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.BorderLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.GridLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.LayoutInfo;
import org.eclipse.wb.tests.designer.swing.SwingTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.LayoutManager;

import javax.swing.JButton;

/**
 * Test for {@link LayoutManager}'s.
 * 
 * @author scheglov_ke
 */
public class LayoutManagersTest extends AbstractLayoutTest {
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
  // isActive()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link LayoutInfo#isActive()}.
   */
  public void test_isActive() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    // prepare implicit layout
    LayoutInfo oldLayout = panel.getLayout();
    assertTrue(oldLayout.isActive());
    // set new Layout
    LayoutInfo newLayout = createJavaInfo("java.awt.BorderLayout");
    panel.setLayout(newLayout);
    assertFalse(oldLayout.isActive());
    assertTrue(newLayout.isActive());
    // remove new layout
    newLayout.delete();
    assertTrue(oldLayout.isActive());
    assertFalse(newLayout.isActive());
  }

  /**
   * Test for {@link LayoutInfo#isActive()}.
   */
  public void test_isActive_forDisconnected() throws Exception {
    parseContainer(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // prepare new Layout
    LayoutInfo newLayout = createJavaInfo("java.awt.FlowLayout");
    assertFalse(newLayout.isActive());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getComponents()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link LayoutInfo#getComponents()}.
   */
  public void test_getComponents() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JButton button = new JButton();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    LayoutInfo layout = panel.getLayout();
    ComponentInfo button = getJavaInfoByName("button");
    assertThat(layout.getComponents()).containsExactly(button);
  }

  /**
   * Test for {@link LayoutInfo#getComponents()}.
   */
  public void test_getComponents_ifNotActive() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JButton button = new JButton();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    ComponentInfo button = getJavaInfoByName("button");
    // prepare layouts
    LayoutInfo oldLayout = panel.getLayout();
    LayoutInfo newLayout = createJavaInfo("java.awt.BorderLayout");
    // set new Layout
    panel.setLayout(newLayout);
    assertFalse(oldLayout.isActive());
    assertTrue(newLayout.isActive());
    // "oldLayout" does not manage
    assertThat(oldLayout.getComponents()).isEmpty();
    // "newLayout" manages 
    assertThat(newLayout.getComponents()).containsExactly(button);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // isManagedObject()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link LayoutInfo#isManagedObject(ObjectInfo)}.
   */
  public void test_isManagedObject_simpleFalse() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    LayoutInfo layout = panel.getLayout();
    // not ComponentInfo
    {
      ObjectInfo newObject = createJavaInfo("java.lang.Object");
      assertFalse(layout.isManagedObject(newObject));
    }
    // not child on Composite
    {
      ObjectInfo newObject = createJButton();
      assertFalse(layout.isManagedObject(newObject));
    }
  }

  /**
   * Test for {@link LayoutInfo#isManagedObject(ObjectInfo)}.
   */
  public void test_isManagedObject_simpleTrue() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JButton button = new JButton();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    LayoutInfo layout = panel.getLayout();
    ComponentInfo button = getJavaInfoByName("button");
    //
    assertTrue(layout.isManagedObject(button));
  }

  /**
   * Test for {@link LayoutInfo#isManagedObject(ObjectInfo)}.
   */
  public void test_isManagedObject_falseBecauseNotActive() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JButton button = new JButton();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    ComponentInfo button = getJavaInfoByName("button");
    // prepare layouts
    LayoutInfo oldLayout = panel.getLayout();
    LayoutInfo newLayout = createJavaInfo("java.awt.BorderLayout");
    // set new Layout
    panel.setLayout(newLayout);
    assertFalse(oldLayout.isActive());
    assertTrue(newLayout.isActive());
    // "oldLayout" does not manage 
    assertFalse(oldLayout.isManagedObject(button));
    // "newLayout" manages 
    assertTrue(newLayout.isManagedObject(button));
  }

  /**
   * Test for {@link LayoutInfo#isManagedObject(ObjectInfo)}.
   * <p>
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=366824
   */
  public void test_isManagedObject_falseBecauseIndirectlyExposed() throws Exception {
    setFileContentSrc(
        "test/Super.java",
        getTestSource(
            "public class Super extends JPanel {",
            "  private JPanel panel = new JPanel();",
            "  private JButton button = new JButton();",
            "  public Super() {",
            "    setLayout(new GridBagLayout());",
            "    add(panel);",
            "    panel.add(button);",
            "  }",
            "  public JButton getButton() {",
            "    return button;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "public class Test extends Super {",
            "  public Test() {",
            "  }",
            "}");
    ComponentInfo button = getJavaInfoByName("getButton()");
    assertNotNull(button);
    // prepare layouts
    LayoutInfo layout = panel.getLayout();
    // indirectly exposed, so not managed 
    assertFalse(layout.isManagedObject(button));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // BorderLayout
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for adding new component on {@link BorderLayout}: local variable, flat.
   */
  public void test_3_BorderLayout_add_1_local_flat() throws Exception {
    String[] initialLines =
        new String[]{
            "class Test {",
            "  public static void main(String args[]) {",
            "    JPanel panel = new JPanel();",
            "    panel.setLayout(new BorderLayout());",
            "  }",
            "}"};
    String[] expectedLines =
        new String[]{
            "class Test {",
            "  public static void main(String args[]) {",
            "    JPanel panel = new JPanel();",
            "    panel.setLayout(new BorderLayout());",
            "    ",
            "    JButton button = new JButton(\"New button\");",
            "    panel.add(button, BorderLayout.NORTH);",
            "  }",
            "}"};
    //
    check_BorderLayout_add(
        initialLines,
        LocalUniqueVariableDescription.INSTANCE,
        FlatStatementGeneratorDescription.INSTANCE,
        expectedLines,
        new String[]{"new JButton(\"New button\")", "panel.add(button, BorderLayout.NORTH)"},
        new String[]{
            "new JPanel()",
            "panel.setLayout(new BorderLayout())",
            "panel.add(button, BorderLayout.NORTH)"});
  }

  /**
   * Test for setting {@link BorderLayout} on {@link Container}.
   */
  public void test_2_BorderLayout() throws Exception {
    ContainerInfo panel =
        (ContainerInfo) parseSource(
            "test",
            "Test.java",
            getSourceDQ(
                "package test;",
                "import java.awt.*;",
                "import javax.swing.*;",
                "class Test {",
                "  public static void main(String args[]) {",
                "    JPanel panel = new JPanel();",
                "    panel.setLayout(new BorderLayout());",
                "  }",
                "}"));
    assertEquals(1, panel.getChildren().size());
    assertSame(BorderLayoutInfo.class, panel.getChildren().get(0).getClass());
    assertSame(panel.getLayout(), panel.getChildren().get(0));
  }

  /**
   * Test for adding new component on {@link BorderLayout}: field, flat.
   */
  public void test_3_BorderLayout_add_2_field_flat() throws Exception {
    String[] initialLines =
        new String[]{
            "class Test {",
            "  public static void main(String args[]) {",
            "    JPanel panel = new JPanel();",
            "    panel.setLayout(new BorderLayout());",
            "  }",
            "}"};
    String[] expectedLines =
        new String[]{
            "class Test {",
            "  private static JButton button;",
            "  public static void main(String args[]) {",
            "    JPanel panel = new JPanel();",
            "    panel.setLayout(new BorderLayout());",
            "    ",
            "    button = new JButton(\"New button\");",
            "    panel.add(button, BorderLayout.NORTH);",
            "  }",
            "}"};
    check_BorderLayout_add(
        initialLines,
        FieldUniqueVariableDescription.INSTANCE,
        FlatStatementGeneratorDescription.INSTANCE,
        expectedLines,
        new String[]{"new JButton(\"New button\")", "panel.add(button, BorderLayout.NORTH)"},
        new String[]{
            "new JPanel()",
            "panel.setLayout(new BorderLayout())",
            "panel.add(button, BorderLayout.NORTH)"});
  }

  /**
   * Test for adding new component on {@link BorderLayout}: field with initializer, flat.
   */
  public void test_3_BorderLayout_add_fieldInitializer_flat() throws Exception {
    String[] initialLines =
        new String[]{
            "class Test {",
            "  public static void main(String args[]) {",
            "    JPanel panel = new JPanel();",
            "    panel.setLayout(new BorderLayout());",
            "  }",
            "}"};
    String[] expectedLines =
        new String[]{
            "class Test {",
            "  private static final JButton button = new JButton(\"New button\");",
            "  public static void main(String args[]) {",
            "    JPanel panel = new JPanel();",
            "    panel.setLayout(new BorderLayout());",
            "    ",
            "    panel.add(button, BorderLayout.NORTH);",
            "  }",
            "}"};
    check_BorderLayout_add(
        initialLines,
        FieldInitializerVariableDescription.INSTANCE,
        FlatStatementGeneratorDescription.INSTANCE,
        expectedLines,
        new String[]{"new JButton(\"New button\")", "panel.add(button, BorderLayout.NORTH)"},
        new String[]{
            "new JPanel()",
            "panel.setLayout(new BorderLayout())",
            "panel.add(button, BorderLayout.NORTH)"});
  }

  /**
   * Test for adding new component on {@link BorderLayout}: local variable, block.
   */
  public void test_3_BorderLayout_add_4_local_block() throws Exception {
    String[] initialLines =
        new String[]{
            "class Test {",
            "  public static void main(String args[]) {",
            "    JPanel panel = new JPanel();",
            "    panel.setLayout(new BorderLayout());",
            "  }",
            "}"};
    String[] expectedLines =
        new String[]{
            "class Test {",
            "  public static void main(String args[]) {",
            "    JPanel panel = new JPanel();",
            "    panel.setLayout(new BorderLayout());",
            "    {",
            "      JButton button = new JButton(\"New button\");",
            "      panel.add(button, BorderLayout.NORTH);",
            "    }",
            "  }",
            "}"};
    check_BorderLayout_add(
        initialLines,
        LocalUniqueVariableDescription.INSTANCE,
        BlockStatementGeneratorDescription.INSTANCE,
        expectedLines,
        new String[]{"new JButton(\"New button\")", "panel.add(button, BorderLayout.NORTH)"},
        new String[]{
            "new JPanel()",
            "panel.setLayout(new BorderLayout())",
            "panel.add(button, BorderLayout.NORTH)"});
  }

  /**
   * Test for adding new component on {@link BorderLayout}: lazy.
   */
  public void test_3_BorderLayout_add_5_lazy() throws Exception {
    String[] initialLines =
        new String[]{
            "class Test {",
            "  public static void main(String args[]) {",
            "    JPanel panel = new JPanel();",
            "    panel.setLayout(new BorderLayout());",
            "  }",
            "}"};
    String[] expectedLines =
        new String[]{
            "class Test {",
            "  private static JButton button;",
            "  public static void main(String args[]) {",
            "    JPanel panel = new JPanel();",
            "    panel.setLayout(new BorderLayout());",
            "    panel.add(getButton(), BorderLayout.NORTH);",
            "  }",
            "  private static JButton getButton() {",
            "    if (button == null) {",
            "      button = new JButton(\"New button\");",
            "    }",
            "    return button;",
            "  }",
            "}"};
    //
    ContainerInfo panel =
        check_BorderLayout_add(
            initialLines,
            LazyVariableDescription.INSTANCE,
            LazyStatementGeneratorDescription.INSTANCE,
            expectedLines,
            new String[]{
                "new JButton(\"New button\")",
                "panel.add(getButton(), BorderLayout.NORTH)"},
            new String[]{
                "new JPanel()",
                "panel.setLayout(new BorderLayout())",
                "panel.add(getButton(), BorderLayout.NORTH)"});
    //
    assert_creation(panel);
  }

  private ContainerInfo check_BorderLayout_add(String[] initialLines,
      VariableSupportDescription variable,
      StatementGeneratorDescription statement,
      String[] expectedLines,
      String[] expectedChildNodes,
      String[] expectedParentNodes) throws Exception {
    ContainerInfo panel = parseContainer(initialLines);
    BorderLayoutInfo layout = (BorderLayoutInfo) panel.getChildren().get(0);
    assertEquals(0, panel.getChildrenComponents().size());
    // prepare new component
    ComponentInfo newComponent =
        (ComponentInfo) JavaInfoUtils.createJavaInfo(
            m_lastEditor,
            JButton.class,
            new ConstructorCreationSupport());
    // add component
    SwingTestUtils.setGenerations(variable, statement);
    try {
      layout.command_CREATE(newComponent, java.awt.BorderLayout.NORTH);
    } finally {
      SwingTestUtils.setGenerationDefaults();
    }
    // check source/AST
    assertEquals(getTestSource(expectedLines), m_lastEditor.getSource());
    assertAST(m_lastEditor);
    // check children
    assertEquals(1, panel.getChildrenComponents().size());
    assertSame(newComponent, panel.getChildrenComponents().get(0));
    assertSame(panel, newComponent.getParent());
    assertRelatedNodes(newComponent, expectedChildNodes);
    assertRelatedNodes(panel, expectedParentNodes);
    //
    return panel;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that layout {@link IObjectPresentation} is not visible.
   */
  public void test_getPresentation() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    LayoutInfo layout = panel.getLayout();
    assertVisible(layout, false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Delete directly, using {@link LayoutInfo#delete()}.
   */
  public void test_delete() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new java.awt.BorderLayout());",
            "  }",
            "}");
    // check explicit layout
    {
      LayoutInfo layout = panel.getLayout();
      assertTrue(layout instanceof BorderLayoutInfo);
      // delete
      layout.delete();
    }
    // check implicit layout
    {
      LayoutInfo layout = panel.getLayout();
      assertTrue(layout instanceof FlowLayoutInfo);
      assertEditor(
          "// filler filler filler",
          "public class Test extends JPanel {",
          "  public Test() {",
          "  }",
          "}");
    }
  }

  /**
   * Delete using "Layout" property.
   */
  public void test_delete2() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new java.awt.BorderLayout());",
            "  }",
            "}");
    //
    Property layoutProperty = panel.getPropertyByTitle("Layout");
    layoutProperty.setValue(Property.UNKNOWN_VALUE);
    // check source
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Replace
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When we replace existing (not implicit) {@link LayoutInfo} with different {@link LayoutInfo} we
   * should not set temporary implicit layout as part of process, because of performance.
   */
  public void test_replace() throws Exception {
    final ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertInstanceOf(FlowLayoutInfo.class, panel.getLayout());
    // set GridLayout
    LayoutInfo gridLayout =
        (LayoutInfo) JavaInfoUtils.createJavaInfo(
            m_lastEditor,
            GridLayout.class,
            new ConstructorCreationSupport());
    panel.setLayout(gridLayout);
    // check source
    assertInstanceOf(GridLayoutInfo.class, panel.getLayout());
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new GridLayout(1, 0, 0, 0));",
        "  }",
        "}");
  }

  /**
   * When we replace existing (not implicit) {@link LayoutInfo} with different {@link LayoutInfo} we
   * should not set temporary implicit layout as part of process, because of performance.
   */
  public void test_replace2() throws Exception {
    final ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "  }",
            "}");
    // set logger for adding layouts
    final StringBuffer buffer = new StringBuffer();
    panel.addBroadcastListener(new ObjectInfoChildAddAfter() {
      public void invoke(ObjectInfo parent, ObjectInfo child) throws Exception {
        if (parent == panel && child instanceof LayoutInfo) {
          LayoutInfo newLayout = (LayoutInfo) child;
          buffer.append("layout added: " + newLayout.getDescription().getComponentClass().getName());
        }
      }
    });
    // set GridLayout
    LayoutInfo gridLayout =
        (LayoutInfo) JavaInfoUtils.createJavaInfo(
            m_lastEditor,
            GridLayout.class,
            new ConstructorCreationSupport());
    panel.setLayout(gridLayout);
    // check for added layouts (events)
    assertEquals("layout added: java.awt.GridLayout", buffer.toString());
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new GridLayout(1, 0, 0, 0));",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Layout" property
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for support complex "Layout" property and its sub-properties.
   */
  public void test_layoutComplexProperty() throws Exception {
    ContainerInfo panel =
        (ContainerInfo) parseSource(
            "test",
            "Test.java",
            getSourceDQ(
                "package test;",
                "import javax.swing.*;",
                "class Test {",
                "  public static void main(String args[]) {",
                "    JPanel panel = new JPanel();",
                "  }",
                "}"));
    Property layoutProperty = panel.getPropertyByTitle("Layout");
    assertTrue(layoutProperty instanceof ComplexProperty);
    assertTrue(layoutProperty.isModified());
    // check text
    assertEquals("(java.awt.FlowLayout)", getPropertyText(layoutProperty));
    // check properties
    {
      Property[] subProperties = getSubProperties(layoutProperty);
      {
        Property hgapProperty = getPropertyByTitle(subProperties, "hgap");
        assertNotNull(hgapProperty);
        assertFalse(hgapProperty.isModified());
        assertEquals(5, hgapProperty.getValue());
      }
      {
        Property vgapProperty = getPropertyByTitle(subProperties, "vgap");
        assertNotNull(vgapProperty);
        assertFalse(vgapProperty.isModified());
        assertEquals(5, vgapProperty.getValue());
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constraints
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_removeConstraints_whenReplaceLayout() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, BorderLayout.WEST);",
            "    }",
            "  }",
            "}");
    refresh();
    //
    panel.getLayout().delete();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Using deprecated {@link Container#add(String, Component)} method.
   */
  public void test_removeConstraints_whenReplaceLayout_addDeprecated() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new CardLayout());",
            "    {",
            "      Button button = new Button();",
            "      add('name', button);",
            "    }",
            "  }",
            "}");
    refresh();
    //
    panel.getLayout().delete();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_removeConstraints_whenReparent() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JPanel inner = new JPanel();",
            "      inner.setLayout(null);",
            "      add(inner);",
            "      {",
            "        Button button = new Button();",
            "        button.setBounds(10, 20, 100, 50);",
            "        inner.add(button);",
            "      }",
            "    }",
            "  }",
            "}");
    ContainerInfo inner = (ContainerInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button = inner.getChildrenComponents().get(0);
    //
    ((FlowLayoutInfo) panel.getLayout()).move(button, null);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JPanel inner = new JPanel();",
        "      inner.setLayout(null);",
        "      add(inner);",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation: name, based on template
  //
  ////////////////////////////////////////////////////////////////////////////
  private void check_nameTemplate(String template, String... lines) throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(null);",
            "    {",
            "      JPanel panel = new JPanel();",
            "      panel.setLayout(new FlowLayout(FlowLayout.CENTER));",
            "      add(panel);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ContainerInfo innerPanel = (ContainerInfo) panel.getChildrenComponents().get(0);
    LayoutInfo layout = innerPanel.getLayout();
    // set template
    Activator.getDefault().getPreferenceStore().setValue(
        org.eclipse.wb.internal.swing.preferences.IPreferenceConstants.P_LAYOUT_NAME_TEMPLATE,
        template);
    //
    layout.getPropertyByTitle("hgap").setValue(7);
    assertEditor(lines);
  }

  /**
   * Template "${defaultName}" means that name should be based on name of type.
   */
  public void test_nameTemplate_useDefaultName() throws Exception {
    check_nameTemplate(
        org.eclipse.wb.internal.core.model.variable.SyncParentChildVariableNameSupport.TEMPLATE_FOR_DEFAULT,
        "class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(null);",
        "    {",
        "      JPanel panel = new JPanel();",
        "      FlowLayout flowLayout = new FlowLayout(FlowLayout.CENTER);",
        "      flowLayout.setHgap(7);",
        "      panel.setLayout(flowLayout);",
        "      add(panel);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Generate name using "${layoutAcronym}${containerName-cap}" template.
   */
  public void test_nameTemplate_alternativeTemplate_1() throws Exception {
    check_nameTemplate(
        "${layoutAcronym}${containerName-cap}",
        "class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(null);",
        "    {",
        "      JPanel panel = new JPanel();",
        "      FlowLayout flPanel = new FlowLayout(FlowLayout.CENTER);",
        "      flPanel.setHgap(7);",
        "      panel.setLayout(flPanel);",
        "      add(panel);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Generate name using "${containerName}${layoutClassName}" template.
   */
  public void test_nameTemplate_alternativeTemplate_2() throws Exception {
    check_nameTemplate(
        "${containerName}${layoutClassName}",
        "class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(null);",
        "    {",
        "      JPanel panel = new JPanel();",
        "      FlowLayout panelFlowLayout = new FlowLayout(FlowLayout.CENTER);",
        "      panelFlowLayout.setHgap(7);",
        "      panel.setLayout(panelFlowLayout);",
        "      add(panel);",
        "    }",
        "  }",
        "}");
  }

  /**
   * When we parse {@link LayoutManager}, which has parent {@link Container} in constructor, it
   * happens that parent is set before {@link VariableSupport}, so we attempt to set name. So, we
   * should check that we are at parsing time, so ignore setting of {@link VariableSupport}.
   */
  public void test_nameTemplate_ignoreDuringParsing() throws Exception {
    setFileContentSrc(
        "test/MyLayout.java",
        getTestSource(
            "public class MyLayout extends FlowLayout {",
            "  public MyLayout(Container container) {",
            "    container.setLayout(this);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyLayout.wbp-component.xml",
        getSource(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <constructors>",
            "    <constructor>",
            "      <parameter type='java.awt.Container' parent='true'/>",
            "    </constructor>",
            "  </constructors>",
            "</component>"));
    waitForAutoBuild();
    // set template
    Activator.getDefault().getPreferenceStore().setValue(
        org.eclipse.wb.internal.swing.preferences.IPreferenceConstants.P_LAYOUT_NAME_TEMPLATE,
        "${containerName}${layoutClassName}");
    // parse and check that source was not changed
    String[] lines =
        {
            "class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(null);",
            "    {",
            "      JPanel panel = new JPanel();",
            "      MyLayout layout = new MyLayout(panel);",
            "      panel.setLayout(layout);",
            "      add(panel);",
            "    }",
            "  }",
            "}"};
    parseJavaInfo(lines);
    assertEditor(lines);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Special
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * One user tried to use direct anonymous implementation of {@link LayoutManager}.
   */
  public void test_customImplementationOf_LayoutManager() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public MyPanel() {",
            "    final LayoutManager layout = new FlowLayout();",
            "    setLayout(new LayoutManager() {",
            "      public void addLayoutComponent(String name, Component comp) {",
            "        layout.addLayoutComponent(name, comp);",
            "      }",
            "      public void removeLayoutComponent(Component comp) {",
            "        layout.removeLayoutComponent(comp);",
            "      }",
            "      public Dimension preferredLayoutSize(Container parent) {",
            "        return layout.preferredLayoutSize(parent);",
            "      }",
            "      public Dimension minimumLayoutSize(Container parent) {",
            "        return layout.minimumLayoutSize(parent);",
            "      }",
            "      public void layoutContainer(Container parent) {",
            "        layout.layoutContainer(parent);",
            "      }",
            "    });",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parseJavaInfo(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "  }",
        "}");
    assertHierarchy(
        "{this: test.MyPanel} {this} {}",
        "  {implicit-layout: test.MyPanel$1} {implicit-layout} {}");
  }
}
