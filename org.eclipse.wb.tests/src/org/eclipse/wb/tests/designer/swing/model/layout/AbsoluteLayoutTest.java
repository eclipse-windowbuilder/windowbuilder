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

import org.eclipse.wb.core.model.IJavaInfoInitializationParticipator;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfoUtils;
import org.eclipse.wb.core.model.association.ImplicitObjectAssociation;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.layout.absolute.IPreferenceConstants;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swing.SwingToolkitDescription;
import org.eclipse.wb.internal.swing.ToolkitProvider;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.BorderLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.ImplicitLayoutCreationSupport;
import org.eclipse.wb.internal.swing.model.layout.ImplicitLayoutVariableSupport;
import org.eclipse.wb.internal.swing.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.absolute.AbsoluteLayoutCreationSupport;
import org.eclipse.wb.internal.swing.model.layout.absolute.AbsoluteLayoutInfo;
import org.eclipse.wb.tests.designer.TestUtils;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPreferenceStore;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.BorderLayout;
import java.util.Collections;

import javax.swing.JButton;

/**
 * Tests for {@link AbsoluteLayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class AbsoluteLayoutTest extends AbstractLayoutTest {
  private static final IPreferenceStore preferences =
      SwingToolkitDescription.INSTANCE.getPreferences();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    preferences.setValue(IPreferenceConstants.P_CREATION_FLOW, false);
  }

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
   * Test for {@link AbsoluteLayoutInfo} as object, setBounds().
   */
  public void test_object() throws Exception {
    String buttonText = "The Button";
    ContainerInfo panel =
        parseContainer(
            "class Test {",
            "  public static void main(String[] args) {",
            "    JPanel panel = new JPanel();",
            "    panel.setLayout(null);",
            "    {",
            "      JButton button = new JButton();",
            "      button.setText('" + buttonText + "');",
            "      button.setBounds(10, 10, 100, 100);",
            "      panel.add(button);",
            "    }",
            "  }",
            "}");
    // check layout itself
    {
      AbsoluteLayoutInfo layout = (AbsoluteLayoutInfo) panel.getLayout();
      assertThat(panel.getChildren()).contains(layout);
      // description
      {
        ComponentDescription description = layout.getDescription();
        assertSame(ToolkitProvider.DESCRIPTION, description.getToolkit());
        assertNotNull(description.getIcon());
      }
      // creation
      {
        CreationSupport creationSupport = layout.getCreationSupport();
        assertInstanceOf(AbsoluteLayoutCreationSupport.class, creationSupport);
        assertEquals("panel.setLayout(null)", layout.getCreationSupport().toString());
        assertSame(
            panel.getMethodInvocation("setLayout(java.awt.LayoutManager)"),
            creationSupport.getNode());
        // validation
        assertFalse(creationSupport.canReorder());
        assertFalse(creationSupport.canReparent());
        assertTrue(creationSupport.canDelete());
      }
      // variable
      {
        VariableSupport variableSupport = layout.getVariableSupport();
        assertEquals("absolute", variableSupport.toString());
        // name
        assertFalse(variableSupport.hasName());
        try {
          variableSupport.getName();
          fail();
        } catch (IllegalStateException e) {
        }
        try {
          variableSupport.setName("abc");
          fail();
        } catch (IllegalStateException e) {
        }
        // expressions
        try {
          variableSupport.getReferenceExpression((NodeTarget) null);
          fail();
        } catch (IllegalStateException e) {
        }
        try {
          variableSupport.getAccessExpression((NodeTarget) null);
          fail();
        } catch (IllegalStateException e) {
        }
        // conversion
        assertFalse(variableSupport.canConvertLocalToField());
        try {
          variableSupport.convertLocalToField();
          fail();
        } catch (IllegalStateException e) {
        }
        assertFalse(variableSupport.canConvertFieldToLocal());
        try {
          variableSupport.convertFieldToLocal();
          fail();
        } catch (IllegalStateException e) {
        }
        // target
        try {
          variableSupport.getStatementTarget();
          fail();
        } catch (IllegalStateException e) {
        }
        // title
        try {
          variableSupport.getTitle();
          fail();
        } catch (IllegalStateException e) {
        }
      }
      // association
      assertInstanceOf(InvocationChildAssociation.class, layout.getAssociation());
      // ID
      assertNotNull(ObjectInfoUtils.getId(layout));
    }
    // JButton
    ContainerInfo buttonInfo = (ContainerInfo) panel.getChildrenComponents().get(0);
    //
    panel.refresh();
    try {
      JButton button = (JButton) buttonInfo.getComponent();
      assertEquals(new Rectangle(10, 10, 100, 100), buttonInfo.getBounds());
      assertEquals(buttonText, button.getText());
      assertTrue(button.isVisible());
    } finally {
      panel.refresh_dispose();
    }
  }

  public void test_parseReplacedContentPane() throws Exception {
    ContainerInfo frame =
        parseContainer(
            "public class Test extends JFrame {",
            "  private JPanel m_contentPane;",
            "  public Test() {",
            "    m_contentPane = new JPanel();",
            "    m_contentPane.setLayout(null);",
            "    setContentPane(m_contentPane);",
            "  }",
            "}");
    ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
    assertEquals("new JPanel()", m_lastEditor.getSource(contentPane.getCreationSupport().getNode()));
    // check that "contentPane" has absolute layout
    LayoutInfo layout = contentPane.getLayout();
    assertInstanceOf(AbsoluteLayoutInfo.class, layout);
    assertThat(contentPane.getChildren()).contains(layout);
  }

  /**
   * Test "Layout" property.
   */
  public void test_layoutComplexProperty() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    setLayout(null);",
            "  }",
            "}");
    Property layoutProperty = panel.getPropertyByTitle("Layout");
    assertTrue(layoutProperty instanceof ComplexProperty);
    assertTrue(layoutProperty.isModified());
    //
    String actualText = getPropertyText(layoutProperty);
    assertEquals("(absolute)", actualText);
  }

  /**
   * Test for implicit absolute layout.
   */
  public void test_implicit() throws Exception {
    setFileContentSrc(
        "test/AbsolutePanel.java",
        getTestSource(
            "public class AbsolutePanel extends JPanel {",
            "  public AbsolutePanel() {",
            "    setLayout(null);",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends AbsolutePanel {",
            "  public Test() {",
            "  }",
            "}");
    LayoutInfo layout = panel.getLayout();
    assertInstanceOf(AbsoluteLayoutInfo.class, layout);
    assertInstanceOf(ImplicitLayoutVariableSupport.class, layout.getVariableSupport());
    assertInstanceOf(ImplicitObjectAssociation.class, layout.getAssociation());
    // check creation support
    {
      CreationSupport creationSupport = layout.getCreationSupport();
      assertInstanceOf(ImplicitLayoutCreationSupport.class, creationSupport);
      assertEquals("implicit-layout: absolute", creationSupport.toString());
    }
    // replace with explicit BorderLayout
    {
      setLayout(panel, BorderLayout.class);
      assertEditor(
          "// filler filler filler",
          "public class Test extends AbsolutePanel {",
          "  public Test() {",
          "    setLayout(new BorderLayout(0, 0));",
          "  }",
          "}");
      assertInstanceOf(BorderLayoutInfo.class, panel.getLayout());
    }
  }

  public void test_absoluteOnContentPane() throws Exception {
    ContainerInfo frame =
        parseContainer(
            "class Test extends JFrame {",
            "  Test() {",
            "    getContentPane().setLayout(null);",
            "  }",
            "}");
    ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
    assertEquals(
        "{method: public java.awt.Container javax.swing.JFrame.getContentPane()} {property} {/getContentPane().setLayout(null)/}",
        contentPane.toString());
    //
    LayoutInfo layout = contentPane.getLayout();
    assertInstanceOf(AbsoluteLayoutInfo.class, layout);
    assertInstanceOf(AbsoluteLayoutCreationSupport.class, layout.getCreationSupport());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialization
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that {@link JavaInfo#initialize()} is invoked for {@link AbsoluteLayoutInfo}.<br>
   * Case when we invoke <code>setLayout(null)</code>.
   */
  public void test_initialize_setLayout() throws Exception {
    addParticipatorExtension(AbsoluteLayout_Participator.class.getName());
    try {
      ContainerInfo panel =
          parseContainer(
              "class Test extends JPanel {",
              "  Test() {",
              "    setLayout(null);",
              "  }",
              "}");
      // should have AbsoluteLayoutInfo
      assertInstanceOf(AbsoluteLayoutInfo.class, panel.getLayout());
      // ...and initialized
      assertEquals(1, AbsoluteLayout_Participator.m_initializeCount);
    } finally {
      removeParticipatorExtension();
      AbsoluteLayout_Participator.m_initializeCount = 0;
    }
  }

  /**
   * Test that {@link JavaInfo#initialize()} is invoked for {@link AbsoluteLayoutInfo}.<br>
   * Implicit layout.
   */
  public void test_initialize_implicitLayout() throws Exception {
    addParticipatorExtension(AbsoluteLayout_Participator.class.getName());
    try {
      setFileContentSrc(
          "test/NullPanel.java",
          getTestSource(
              "public class NullPanel extends JPanel {",
              "  public NullPanel() {",
              "    setLayout(null);",
              "  }",
              "}"));
      waitForAutoBuild();
      // parse
      ContainerInfo panel =
          parseContainer(
              "// filler filler filler",
              "public class Test extends NullPanel {",
              "  public Test() {",
              "  }",
              "}");
      // should have AbsoluteLayoutInfo
      assertInstanceOf(AbsoluteLayoutInfo.class, panel.getLayout());
      // ...and initialized
      assertThat(AbsoluteLayout_Participator.m_initializeCount).isPositive();
    } finally {
      removeParticipatorExtension();
      AbsoluteLayout_Participator.m_initializeCount = 0;
    }
  }

  public static final class AbsoluteLayout_Participator
      implements
        IJavaInfoInitializationParticipator {
    private static int m_initializeCount;

    public void process(JavaInfo javaInfo) throws Exception {
      if (javaInfo instanceof AbsoluteLayoutInfo) {
        m_initializeCount++;
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Coordinates
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for setBounds().
   */
  public void test_setBounds() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(null);",
            "    //",
            "    JButton button = new JButton();",
            "    add(button);",
            "    button.setBounds(10, 20, 100, 50);",
            "  }",
            "}");
    ContainerInfo buttonInfo = (ContainerInfo) panel.getChildrenComponents().get(0);
    //
    panel.refresh();
    try {
      assertEquals(new Rectangle(10, 20, 100, 50), buttonInfo.getBounds());
      assertEquals(new Dimension(33, 9), buttonInfo.getPreferredSize());
      //
      JButton button = (JButton) buttonInfo.getComponent();
      assertEquals(10, button.getBounds().x);
      assertEquals(20, button.getBounds().y);
      assertEquals(100, button.getBounds().width);
      assertEquals(50, button.getBounds().height);
    } finally {
      panel.refresh_dispose();
    }
  }

  /**
   * Test for setLocation(Point), setSize(Dimension).
   */
  public void test_setLocationSize() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  Test() {",
            "    setLayout(null);",
            "    //",
            "    JButton button = new JButton();",
            "    add(button);",
            "    button.setLocation(new Point(10, 20));",
            "    button.setSize(new Dimension(100, 50));",
            "  }",
            "}");
    ContainerInfo buttonInfo = (ContainerInfo) panel.getChildrenComponents().get(0);
    //
    panel.refresh();
    try {
      assertEquals(new Rectangle(10, 20, 100, 50), buttonInfo.getBounds());
    } finally {
      panel.refresh_dispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setLayout
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for replacing absolute layout.
   */
  public void test_setLayout() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  Test() {",
            "    setLayout(null);",
            "  }",
            "}");
    assertInstanceOf(AbsoluteLayoutInfo.class, panel.getLayout());
    //
    LayoutInfo borderLayout =
        (LayoutInfo) JavaInfoUtils.createJavaInfo(
            m_lastEditor,
            BorderLayout.class,
            new ConstructorCreationSupport());
    panel.setLayout(borderLayout);
    //
    assertEditor(
        "public class Test extends JPanel {",
        "  Test() {",
        "    setLayout(new BorderLayout(0, 0));",
        "  }",
        "}");
    assertSame(borderLayout, panel.getLayout());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // onSet()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Check for removing absolute layout related method invocations on removing
   * {@link AbsoluteLayoutInfo}.
   */
  public void test_removeComponentConstraints() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  Test() {",
            "    setLayout(null);",
            "    {",
            "      JButton button = new JButton();",
            "      button.setLocation(0, 1);",
            "      button.setLocation(new Point(0, 1));",
            "      button.setSize(2, 3);",
            "      button.setSize(new Dimension(2, 3));",
            "      button.setBounds(0, 1, 2, 3);",
            "      button.setBounds(new Rectangle(0, 1, 2, 3));",
            "      add(button);",
            "    }",
            "  }",
            "}");
    // delete AbsoluteLayoutInfo
    panel.getLayout().delete();
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  Test() {",
        "    {",
        "      JButton button = new JButton();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Set {@link AbsoluteLayoutInfo} using {@link ContainerInfo#setLayout(LayoutInfo)}.
   */
  public void test_onSet_setLayout() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  Test() {",
            "    setSize(450, 300);",
            "    {",
            "      JButton button = new JButton();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // initially "button" in not on absolute layout, so no "Bounds" property
    assertNull(button.getPropertyByTitle("Bounds"));
    // set AbsoluteLayoutInfo
    {
      AbsoluteLayoutInfo absoluteLayoutInfo = AbsoluteLayoutInfo.createExplicit(m_lastEditor);
      panel.setLayout(absoluteLayoutInfo);
    }
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  Test() {",
        "    setSize(450, 300);",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton();",
        "      button.setBounds(208, 5, 33, 9);",
        "      add(button);",
        "    }",
        "  }",
        "}");
    // now "button" in on absolute layout, so has "Bounds" property
    assertNotNull(button.getPropertyByTitle("Bounds"));
  }

  /**
   * Test for removing "real" layout when implicit is "absolute".
   */
  public void test_onSet_implicit() throws Exception {
    setFileContentSrc(
        "test/AbsolutePanel.java",
        getTestSource(
            "public class AbsolutePanel extends JPanel {",
            "  public AbsolutePanel() {",
            "    setLayout(null);",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    ContainerInfo panel =
        parseContainer(
            "public class Test extends AbsolutePanel {",
            "  public Test() {",
            "    setLayout(new FlowLayout());",
            "    setSize(450, 300);",
            "    {",
            "      JButton button = new JButton();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    //
    panel.refresh();
    try {
      // delete current layout, i.e. restore implicit absolute
      assertInstanceOf(FlowLayoutInfo.class, panel.getLayout());
      panel.getLayout().delete();
      // check absolute layout
      {
        assertInstanceOf(AbsoluteLayoutInfo.class, panel.getLayout());
        //
        CreationSupport creationSupport = panel.getLayout().getCreationSupport();
        assertInstanceOf(ImplicitLayoutCreationSupport.class, creationSupport);
        assertEquals("implicit-layout: absolute", creationSupport.toString());
      }
    } finally {
      panel.refresh_dispose();
    }
    // check source
    assertEditor(
        "public class Test extends AbsolutePanel {",
        "  public Test() {",
        "    setSize(450, 300);",
        "    {",
        "      JButton button = new JButton();",
        "      button.setBounds(208, 5, 33, 9);",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Move
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_moveUsingPoint() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  Test() {",
            "    setLayout(null);",
            "    setSize(450, 300);",
            "    {",
            "      JButton button = new JButton();",
            "      button.setBounds(10, 10, 34, 10);",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo buttonInfo = panel.getChildrenComponents().get(0);
    AbsoluteLayoutInfo layoutInfo = (AbsoluteLayoutInfo) panel.getLayout();
    // perform code modifications
    layoutInfo.command_BOUNDS(buttonInfo, new Point(20, 20), null);
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    setSize(450, 300);",
        "    {",
        "      JButton button = new JButton();",
        "      button.setBounds(20, 20, 34, 10);",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_moveAddBounds() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  Test() {",
            "    setLayout(null);",
            "    setSize(450, 300);",
            "    {",
            "      JButton button = new JButton();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo buttonInfo = panel.getChildrenComponents().get(0);
    AbsoluteLayoutInfo layoutInfo = (AbsoluteLayoutInfo) panel.getLayout();
    // perform code modifications
    layoutInfo.command_BOUNDS(buttonInfo, new Point(20, 20), new Dimension(34, 10));
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    setSize(450, 300);",
        "    {",
        "      JButton button = new JButton();",
        "      button.setBounds(20, 20, 34, 10);",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_moveChangeBoundsAsRectangle() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  Test() {",
            "    setLayout(null);",
            "    setSize(450, 300);",
            "    {",
            "      JButton button = new JButton();",
            "      button.setBounds(new Rectangle(10, 10, 34, 10));",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo buttonInfo = panel.getChildrenComponents().get(0);
    AbsoluteLayoutInfo layoutInfo = (AbsoluteLayoutInfo) panel.getLayout();
    // perform code modifications
    layoutInfo.command_BOUNDS(buttonInfo, new Point(20, 20), null);
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    setSize(450, 300);",
        "    {",
        "      JButton button = new JButton();",
        "      button.setBounds(new Rectangle(20, 20, 34, 10));",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_moveUsingSetLocation() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  Test() {",
            "    setLayout(null);",
            "    setSize(450, 300);",
            "    {",
            "      JButton button = new JButton();",
            "      button.setLocation(new Point(10, 10));",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo buttonInfo = panel.getChildrenComponents().get(0);
    AbsoluteLayoutInfo layoutInfo = (AbsoluteLayoutInfo) panel.getLayout();
    // perform code modifications
    layoutInfo.command_BOUNDS(buttonInfo, new Point(20, 20), null);
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    setSize(450, 300);",
        "    {",
        "      JButton button = new JButton();",
        "      button.setLocation(new Point(20, 20));",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_moveAddSetLocation() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  Test() {",
            "    setLayout(null);",
            "    setSize(450, 300);",
            "    {",
            "      JButton button = new JButton();",
            "      button.setSize(new Dimension(10, 10));",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo buttonInfo = panel.getChildrenComponents().get(0);
    AbsoluteLayoutInfo layoutInfo = (AbsoluteLayoutInfo) panel.getLayout();
    // perform code modifications
    layoutInfo.command_BOUNDS(buttonInfo, new Point(20, 20), null);
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    setSize(450, 300);",
        "    {",
        "      JButton button = new JButton();",
        "      button.setLocation(20, 20);",
        "      button.setSize(new Dimension(10, 10));",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Resize
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_resizeUsingPoint() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  Test() {",
            "    setLayout(null);",
            "    setSize(450, 300);",
            "    {",
            "      JButton button = new JButton();",
            "      button.setBounds(10, 10, 34, 10);",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo buttonInfo = panel.getChildrenComponents().get(0);
    AbsoluteLayoutInfo layoutInfo = (AbsoluteLayoutInfo) panel.getLayout();
    // perform code modifications
    layoutInfo.command_BOUNDS(buttonInfo, null, new Dimension(100, 100));
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    setSize(450, 300);",
        "    {",
        "      JButton button = new JButton();",
        "      button.setBounds(10, 10, 100, 100);",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_resizeChangeBoundsAsRectangle() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  Test() {",
            "    setLayout(null);",
            "    setSize(450, 300);",
            "    {",
            "      JButton button = new JButton();",
            "      button.setBounds(new Rectangle(10, 10, 34, 10));",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo buttonInfo = panel.getChildrenComponents().get(0);
    AbsoluteLayoutInfo layoutInfo = (AbsoluteLayoutInfo) panel.getLayout();
    // perform code modifications
    layoutInfo.command_BOUNDS(buttonInfo, null, new Dimension(100, 100));
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    setSize(450, 300);",
        "    {",
        "      JButton button = new JButton();",
        "      button.setBounds(new Rectangle(10, 10, 100, 100));",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_resizeUsingSetSize() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  Test() {",
            "    setLayout(null);",
            "    setSize(450, 300);",
            "    {",
            "      JButton button = new JButton();",
            "      button.setSize(new Dimension(100, 100));",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo buttonInfo = panel.getChildrenComponents().get(0);
    AbsoluteLayoutInfo layoutInfo = (AbsoluteLayoutInfo) panel.getLayout();
    // perform code modifications
    layoutInfo.command_BOUNDS(buttonInfo, null, new Dimension(110, 100));
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    setSize(450, 300);",
        "    {",
        "      JButton button = new JButton();",
        "      button.setSize(new Dimension(110, 100));",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_resizeAddSetSize() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  Test() {",
            "    setLayout(null);",
            "    setSize(450, 300);",
            "    {",
            "      JButton button = new JButton();",
            "      button.setLocation(10, 10);",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo buttonInfo = panel.getChildrenComponents().get(0);
    AbsoluteLayoutInfo layoutInfo = (AbsoluteLayoutInfo) panel.getLayout();
    // perform code modifications
    layoutInfo.command_BOUNDS(buttonInfo, null, new Dimension(100, 100));
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    setSize(450, 300);",
        "    {",
        "      JButton button = new JButton();",
        "      button.setSize(100, 100);",
        "      button.setLocation(10, 10);",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Ordering
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that we have "Order" sub-menu in context menu.
   */
  public void test_ordering() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  Test() {",
            "    setLayout(null);",
            "    {",
            "      JButton button_0 = new JButton();",
            "      add(button_0);",
            "    }",
            "    {",
            "      JButton button_1 = new JButton();",
            "      add(button_1);",
            "    }",
            "  }",
            "}");
    ComponentInfo button_0 = panel.getChildrenComponents().get(0);
    // send "button_0" back
    {
      IMenuManager orderManager = createOrderManager(button_0);
      assertNotNull(orderManager);
      IAction action = findChildAction(orderManager, "Send to Back");
      assertTrue(action.isEnabled());
      action.run();
    }
    // check result
    assertEditor(
        "public class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button_1 = new JButton();",
        "      add(button_1);",
        "    }",
        "    {",
        "      JButton button_0 = new JButton();",
        "      add(button_0);",
        "    }",
        "  }",
        "}");
  }

  /**
   * @return the "order" {@link IMenuManager} for single {@link ComponentInfo}.
   */
  private static IMenuManager createOrderManager(ComponentInfo component) throws Exception {
    IMenuManager manager = getContextMenu(component);
    return findChildMenuManager(manager, "Order");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Auto-size action
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for <code>Autosize component</code> action.
   */
  public void test_autoSize() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  Test() {",
            "    setLayout(null);",
            "    {",
            "      JButton button = new JButton();",
            "      add(button);",
            "      button.setBounds(10, 20, 200, 100);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button = panel.getChildrenComponents().get(0);
    Dimension buttonPrefSize = button.getPreferredSize();
    // prepare action
    IAction autoSizeAction;
    {
      MenuManager manager = getDesignerMenuManager();
      button.getBroadcastObject().addContextMenu(Collections.singletonList(button), button, manager);
      autoSizeAction = findChildAction(manager, "Autosize component");
      assertNotNull(autoSizeAction);
    }
    // perform auto-size
    autoSizeAction.run();
    assertEditor(
        "public class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton();",
        "      add(button);",
        "      button.setBounds(10, 20, "
            + buttonPrefSize.width
            + ", "
            + buttonPrefSize.height
            + ");",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Bounds" property
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Helper method preparing to test "Bounds" property.
   */
  private Property prepareBoundsProperty() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    setLayout(null);",
            "    {",
            "      JButton button = new JButton();",
            "      button.setBounds(10, 30, 100, 200);",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    Property boundsProperty = panel.getChildrenComponents().get(0).getPropertyByTitle("Bounds");
    return boundsProperty;
  }

  /**
   * Helper method returning the sub-property of Bounds property by title.
   */
  private Property getBoundsPropertySubProperty(String title) throws Exception {
    ComplexProperty boundsProperty = (ComplexProperty) prepareBoundsProperty();
    Property[] subProperties = boundsProperty.getProperties();
    return getPropertyByTitle(subProperties, title);
  }

  /**
   * Test "Bounds" property.
   */
  public void test_BoundsProperty() throws Exception {
    Property boundsProperty = prepareBoundsProperty();
    assertNotNull(boundsProperty);
    assertTrue(boundsProperty instanceof ComplexProperty);
    assertTrue(boundsProperty.isModified());
    //
    ComplexProperty boundsComplexProperty = (ComplexProperty) boundsProperty;
    Property[] subProperties = boundsComplexProperty.getProperties();
    assertEquals(subProperties.length, 4);
    //
    String actualText = getPropertyText(boundsProperty);
    assertEquals("(10, 30, 100, 200)", actualText);
  }

  /**
   * Test setting "x" sub-property of "Bounds" property.
   */
  public void test_BoundsProperty_set_x() throws Exception {
    Property subProperty = getBoundsPropertySubProperty("x");
    assertNotNull(subProperty);
    subProperty.setValue(0);
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton();",
        "      button.setBounds(0, 30, 100, 200);",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test getting "x" sub-property of "Bounds" property.
   */
  public void test_BoundsProperty_get_x() throws Exception {
    Property subProperty = getBoundsPropertySubProperty("x");
    assertNotNull(subProperty);
    assertTrue(subProperty.isModified());
    {
      Object value = subProperty.getValue();
      assertInstanceOf(Integer.class, value);
      assertEquals(10, ((Integer) value).intValue());
    }
  }

  /**
   * Test setting "y" sub-property of "Bounds" property.
   */
  public void test_BoundsProperty_set_y() throws Exception {
    Property subProperty = getBoundsPropertySubProperty("y");
    assertNotNull(subProperty);
    subProperty.setValue(5);
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton();",
        "      button.setBounds(10, 5, 100, 200);",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test getting "y" sub-property of "Bounds" property.
   */
  public void test_BoundsProperty_get_y() throws Exception {
    Property subProperty = getBoundsPropertySubProperty("y");
    assertNotNull(subProperty);
    Object value = subProperty.getValue();
    assertInstanceOf(Integer.class, value);
    assertEquals(30, ((Integer) value).intValue());
  }

  /**
   * Test setting "width" sub-property of "Bounds" property.
   */
  public void test_BoundsProperty_set_width() throws Exception {
    Property subProperty = getBoundsPropertySubProperty("width");
    assertNotNull(subProperty);
    subProperty.setValue(150);
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton();",
        "      button.setBounds(10, 30, 150, 200);",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test getting "width" sub-property of "Bounds" property.
   */
  public void test_BoundsProperty_get_width() throws Exception {
    Property subProperty = getBoundsPropertySubProperty("width");
    assertNotNull(subProperty);
    Object value = subProperty.getValue();
    assertInstanceOf(Integer.class, value);
    assertEquals(100, ((Integer) value).intValue());
  }

  /**
   * Test setting "height" sub-property of "Bounds" property.
   */
  public void test_BoundsProperty_set_height() throws Exception {
    Property subProperty = getBoundsPropertySubProperty("height");
    assertNotNull(subProperty);
    subProperty.setValue(220);
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton();",
        "      button.setBounds(10, 30, 100, 220);",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test getting "height" sub-property of "Bounds" property.
   */
  public void test_BoundsProperty_get_height() throws Exception {
    Property subProperty = getBoundsPropertySubProperty("height");
    assertNotNull(subProperty);
    Object value = subProperty.getValue();
    assertInstanceOf(Integer.class, value);
    assertEquals(200, ((Integer) value).intValue());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dynamic IJavaInfoInitializationParticipator extension support
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String POINT_ID = "org.eclipse.wb.core.java.javaInfoInitializationParticipators";

  /**
   * Adds dynamic {@link IJavaInfoInitializationParticipator} extension.
   * 
   * @param className
   *          the name of {@link IJavaInfoInitializationParticipator} class.
   */
  private static void addParticipatorExtension(String className) throws Exception {
    String contribution = "  <participator class='" + className + "'/>";
    TestUtils.addDynamicExtension(POINT_ID, contribution);
  }

  /**
   * Removes dynamic {@link IJavaInfoInitializationParticipator} extension.
   */
  private static void removeParticipatorExtension() throws Exception {
    TestUtils.removeDynamicExtension(POINT_ID);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Permissions
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * On absolute layout components can not be reordered, but can be moved, i.e. bounds can be
   * changed.
   */
  public void test_canMove() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  private JButton m_button = new JButton();",
            "  public MyPanel() {",
            "    setLayout(null);",
            "    add(m_button);",
            "  }",
            "  public JButton getButton() {",
            "    return m_button;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertInstanceOf(AbsoluteLayoutInfo.class, panel.getLayout());
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // check permissions
    assertFalse(button.getCreationSupport().canReorder());
    assertFalse(button.getCreationSupport().canReparent());
    assertTrue(JavaInfoUtils.canMove(button));
    assertFalse(JavaInfoUtils.canReparent(button));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AbsoluteLayoutInfo#command_CREATE(ComponentInfo, ComponentInfo)}.
   */
  public void test_CREATE() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(null);",
            "    {",
            "      JButton buttonA = new JButton();",
            "      add(buttonA);",
            "      buttonA.setBounds(10, 20, 100, 50);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    AbsoluteLayoutInfo layout = (AbsoluteLayoutInfo) panel.getLayout();
    ContainerInfo buttonA = (ContainerInfo) panel.getChildrenComponents().get(0);
    // do CREATE
    ComponentInfo newButton = createJButton();
    layout.command_CREATE(newButton, buttonA);
    layout.command_BOUNDS(newButton, new Point(1, 2), new Dimension(3, 4));
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton();",
        "      button.setBounds(1, 2, 3, 4);",
        "      add(button);",
        "    }",
        "    {",
        "      JButton buttonA = new JButton();",
        "      add(buttonA);",
        "      buttonA.setBounds(10, 20, 100, 50);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link AbsoluteLayoutInfo#command_MOVE(ComponentInfo, ComponentInfo)}.
   */
  public void test_MOVE() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(null);",
            "    {",
            "      JButton buttonA = new JButton();",
            "      add(buttonA);",
            "      buttonA.setBounds(10, 20, 100, 50);",
            "    }",
            "    {",
            "      JButton buttonB = new JButton();",
            "      add(buttonB);",
            "      buttonB.setBounds(20, 100, 50, 20);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    AbsoluteLayoutInfo layout = (AbsoluteLayoutInfo) panel.getLayout();
    ContainerInfo buttonA = (ContainerInfo) panel.getChildrenComponents().get(0);
    ContainerInfo buttonB = (ContainerInfo) panel.getChildrenComponents().get(1);
    // do move
    layout.command_MOVE(buttonB, buttonA);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton buttonB = new JButton();",
        "      add(buttonB);",
        "      buttonB.setBounds(20, 100, 50, 20);",
        "    }",
        "    {",
        "      JButton buttonA = new JButton();",
        "      add(buttonA);",
        "      buttonA.setBounds(10, 20, 100, 50);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation flow
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test applying creation flow order.
   */
  public void test_BOUNDS_CreationFlow() throws Exception {
    preferences.setValue(IPreferenceConstants.P_CREATION_FLOW, true);
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(null);",
            "    {",
            "      JButton button1 = new JButton();",
            "      add(button1);",
            "      button1.setBounds(5, 5, 100, 30);",
            "      button1.setText('Button1');",
            "    }",
            "    {",
            "      JButton button2 = new JButton();",
            "      add(button2);",
            "      button2.setBounds(110, 50, 100, 30);",
            "      button2.setText('Button2');",
            "    }",
            "  }",
            "}");
    panel.refresh();
    AbsoluteLayoutInfo layout = (AbsoluteLayoutInfo) panel.getLayout();
    ContainerInfo button1 = getJavaInfoByName("button1");
    // move button1 under button2
    layout.command_BOUNDS(button1, new Point(110, 90), null);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button2 = new JButton();",
        "      add(button2);",
        "      button2.setBounds(110, 50, 100, 30);",
        "      button2.setText('Button2');",
        "    }",
        "    {",
        "      JButton button1 = new JButton();",
        "      add(button1);",
        "      button1.setBounds(110, 90, 100, 30);",
        "      button1.setText('Button1');",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for copy/paste.
   */
  public void test_clipboard() throws Exception {
    String[] lines1 =
        {
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JPanel inner = new JPanel();",
            "      inner.setLayout(null);",
            "      add(inner);",
            "      {",
            "        JButton button = new JButton();",
            "        inner.add(button);",
            "        button.setBounds(1, 2, 3, 4);",
            "      }",
            "    }",
            "  }",
            "}"};
    final ContainerInfo panel = parseContainer(lines1);
    panel.refresh();
    // prepare memento
    final JavaInfoMemento memento;
    {
      ComponentInfo inner = panel.getChildrenComponents().get(0);
      memento = JavaInfoMemento.createMemento(inner);
    }
    // add copy
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        ContainerInfo copy = (ContainerInfo) memento.create(panel);
        ((FlowLayoutInfo) panel.getLayout()).add(copy, null);
        memento.apply();
      }
    });
    String[] lines =
        {
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JPanel inner = new JPanel();",
            "      inner.setLayout(null);",
            "      add(inner);",
            "      {",
            "        JButton button = new JButton();",
            "        inner.add(button);",
            "        button.setBounds(1, 2, 3, 4);",
            "      }",
            "    }",
            "    {",
            "      JPanel panel = new JPanel();",
            "      panel.setLayout(null);",
            "      add(panel);",
            "      {",
            "        JButton button = new JButton();",
            "        button.setBounds(1, 2, 3, 4);",
            "        panel.add(button);",
            "      }",
            "    }",
            "  }",
            "}"};
    assertEditor(lines);
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(inner)/ /add(panel)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JPanel} {local-unique: inner} {/new JPanel()/ /inner.setLayout(null)/ /add(inner)/ /inner.add(button)/}",
        "    {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /inner.add(button)/ /button.setBounds(1, 2, 3, 4)/}",
        "    {inner.setLayout(null)} {absolute} {}",
        "  {new: javax.swing.JPanel} {local-unique: panel} {/new JPanel()/ /add(panel)/ /panel.setLayout(null)/ /panel.add(button)/}",
        "    {panel.setLayout(null)} {absolute} {}",
        "    {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /panel.add(button)/ /button.setBounds(1, 2, 3, 4)/}");
  }
}
