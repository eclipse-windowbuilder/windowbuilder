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
package org.eclipse.wb.tests.designer.core.model.creation;

import org.eclipse.wb.internal.core.model.clipboard.IClipboardImplicitCreationSupport;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.creation.ExposedFieldCreationSupport;
import org.eclipse.wb.internal.core.model.util.ObjectsLabelProvider;
import org.eclipse.wb.internal.core.model.variable.ExposedFieldVariableSupport;
import org.eclipse.wb.internal.core.model.variable.LocalUniqueVariableSupport;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

import static org.assertj.core.api.Assertions.assertThat;

import javax.swing.JFrame;

/**
 * Test for {@link ExposedFieldCreationSupport}.
 * 
 * @author scheglov_ke
 */
public class ExposedFieldCreationSupportTest extends SwingModelTest {
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
  // Parse
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Simplest test for parsing {@link ExposedFieldCreationSupport}.
   */
  public void test_parse_simplestCase() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public JButton button = new JButton();",
            "  public MyPanel() {",
            "    add(button);",
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
    assertHierarchy(
        "{this: test.MyPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {field: javax.swing.JButton} {button} {}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // check that all components (including exposed) have objects
    assert_creation(panel);
    // check CreationSupport
    ExposedFieldCreationSupport creationSupport =
        (ExposedFieldCreationSupport) button.getCreationSupport();
    assertEquals(panel.getCreationSupport().getNode(), creationSupport.getNode());
    assertSame(panel, creationSupport.getHostJavaInfo());
    assertEquals("button", creationSupport.getField().getName());
    assertTrue(creationSupport.isDirect());
    // operations validation
    assertFalse(creationSupport.canReorder());
    assertFalse(creationSupport.canReparent());
  }

  /**
   * Private fields can not be used for exposing.
   */
  public void test_parse_noPrivateField() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  private JButton button = new JButton();",
            "  public MyPanel() {",
            "    add(button);",
            "  }",
            "}"));
    waitForAutoBuild();
    String[] lines = {"public class Test extends MyPanel {", "  public Test() {", "  }", "}"};
    // parse
    parseContainer(lines);
    assertHierarchy(
        "{this: test.MyPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
  }

  /**
   * If field has value <code>null</code>, it is not exposed.
   */
  public void test_parse_noNullValue() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public JButton button = null;",
            "  public MyPanel() {",
            "  }",
            "}"));
    waitForAutoBuild();
    String[] lines = {"public class Test extends MyPanel {", "  public Test() {", "  }", "}"};
    // parse
    parseContainer(lines);
    assertHierarchy(
        "{this: test.MyPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
  }

  /**
   * If field component is not connected to host, it is not exposed.
   */
  public void test_parse_noDisconnected() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public JButton button = new JButton();",
            "  public MyPanel() {",
            "  }",
            "}"));
    waitForAutoBuild();
    String[] lines = {"public class Test extends MyPanel {", "  public Test() {", "  }", "}"};
    // parse
    parseContainer(lines);
    assertHierarchy(
        "{this: test.MyPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
  }

  /**
   * If field exposes its host, we should stop on it.
   */
  public void test_parse_noRecursion() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public JPanel m_this = this;",
            "  public MyPanel() {",
            "  }",
            "}"));
    waitForAutoBuild();
    String[] lines = {"public class Test extends MyPanel {", "  public Test() {", "  }", "}"};
    // parse
    parseContainer(lines);
    assertHierarchy(
        "{this: test.MyPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
  }

  /**
   * Test that there are no conflict with {@link JFrame} parsing.
   */
  public void test_parse_JFrame() throws Exception {
    String[] lines = {"public class Test extends JFrame {", "  public Test() {", "  }", "}"};
    parseContainer(lines);
    assertHierarchy(
        "{this: javax.swing.JFrame} {this} {}",
        "  {method: public java.awt.Container javax.swing.JFrame.getContentPane()} {property} {}",
        "    {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}");
  }

  /**
   * Test that during parsing we restore "logical" parent/child hierarchy.
   */
  public void test_parse_logicalHierarchy() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public final JLabel m_label;",
            "  public final JPanel m_container;",
            "  public final JButton m_button;",
            "  public final JCheckBox m_check;",
            "  public MyPanel() {",
            "    setLayout(new BorderLayout());",
            "    {",
            "      m_container = new JPanel();",
            "      add(m_container, BorderLayout.CENTER);",
            "      {",
            "        m_button = new JButton();",
            "        m_container.add(m_button);",
            "      }",
            "      {",
            "        m_check = new JCheckBox();",
            "        m_container.add(m_check);",
            "      }",
            "    }",
            "    {",
            "      m_label = new JLabel();",
            "      add(m_label, BorderLayout.NORTH);",
            "    }",
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
    assertHierarchy(
        "{this: test.MyPanel} {this} {}",
        "  {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}",
        "  {field: javax.swing.JPanel} {m_container} {}",
        "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "    {field: javax.swing.JButton} {m_button} {}",
        "    {field: javax.swing.JCheckBox} {m_check} {}",
        "  {field: javax.swing.JLabel} {m_label} {}");
    // check that all components (including exposed) have objects
    assert_creation(panel);
  }

  /**
   * Test that we can access exposed component, using {@link SimpleName}.
   */
  public void test_reference_SimpleName() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public final JPanel m_container;",
            "  public MyPanel() {",
            "    setLayout(new BorderLayout());",
            "    {",
            "      m_container = new JPanel();",
            "      add(m_container, BorderLayout.CENTER);",
            "    }",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    m_container.setEnabled(false);",
        "  }",
        "}");
    assertHierarchy(
        "{this: test.MyPanel} {this} {}",
        "  {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}",
        "  {field: javax.swing.JPanel} {m_container} {/m_container.setEnabled(false)/}",
        "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
  }

  /**
   * Test that we can access exposed component, using {@link QualifiedName}.
   */
  public void test_reference_QualifiedName() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public final JPanel m_container;",
            "  public MyPanel() {",
            "    setLayout(new BorderLayout());",
            "    {",
            "      m_container = new JPanel();",
            "      add(m_container, BorderLayout.CENTER);",
            "    }",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    MyPanel myPanel = new MyPanel();",
            "    add(myPanel);",
            "    myPanel.m_container.setEnabled(false);",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(myPanel)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: test.MyPanel} {local-unique: myPanel} {/new MyPanel()/ /add(myPanel)/ /myPanel.m_container/}",
        "    {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}",
        "    {field: javax.swing.JPanel} {m_container} {/myPanel.m_container.setEnabled(false)/}",
        "      {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
    ContainerInfo myPanel = (ContainerInfo) panel.getChildrenComponents().get(0);
    ComponentInfo container = myPanel.getChildrenComponents().get(0);
    // check that "myPanel.m_container.setEnabled(false)" was executed
    panel.refresh();
    assertFalse(container.getComponent().isEnabled());
  }

  /**
   * Test that we can resolve "logical" exposed children.
   */
  public void test_reference_logicalChild() throws Exception {
    setFileContentSrc(
        "test/PanelExpose.java",
        getTestSource(
            "public class PanelExpose extends JPanel {",
            "  protected final JPanel m_container;",
            "  protected final JButton m_button;",
            "  public PanelExpose() {",
            "    setLayout(new BorderLayout());",
            "    {",
            "      m_container = new JPanel();",
            "      add(m_container, BorderLayout.CENTER);",
            "      m_container.setLayout(null);",
            "      {",
            "        m_button = new JButton('Button');",
            "        m_container.add(m_button);",
            "        m_button.setBounds(10, 10, 200, 30);",
            "      }",
            "    }",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "public class Test extends PanelExpose {",
        "  public Test() {",
        "    m_button.setEnabled(false);",
        "  }",
        "}");
    assertHierarchy(
        "{this: test.PanelExpose} {this} {}",
        "  {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}",
        "  {field: javax.swing.JPanel} {m_container} {}",
        "    {implicit-layout: absolute} {implicit-layout} {}",
        "    {field: javax.swing.JButton} {m_button} {/m_button.setEnabled(false)/}");
  }

  /**
   * Superclass field hidden by local variable.
   */
  public void test_isJavaInfo_fieldHidden() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public JButton button = new JButton();",
            "  public MyPanel() {",
            "    add(button);",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  Test() {",
            "    {",
            "      JButton button = new JButton();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: test.MyPanel} {this} {/add(button)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {field: javax.swing.JButton} {button} {}",
        "  {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /add(button)/}");
    SimpleName useName = (SimpleName) m_lastEditor.getEnclosingNode("button);");
    // field "button"
    {
      ComponentInfo fieldButton = panel.getChildrenComponents().get(0);
      assertThat(fieldButton.getVariableSupport()).isInstanceOf(ExposedFieldVariableSupport.class);
      assertFalse(fieldButton.isRepresentedBy(useName));
    }
    // local "button"
    {
      ComponentInfo localButton = panel.getChildrenComponents().get(1);
      assertThat(localButton.getVariableSupport()).isInstanceOf(LocalUniqueVariableSupport.class);
      assertTrue(localButton.isRepresentedBy(useName));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Component with {@link ExposedFieldCreationSupport} can be "deleted" - delete its children and
   * related nodes, but keep itself in parent.
   */
  public void test_delete() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public final JPanel m_container;",
            "  public MyPanel() {",
            "    setLayout(new BorderLayout());",
            "    {",
            "      m_container = new JPanel();",
            "      add(m_container, BorderLayout.CENTER);",
            "    }",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    setEnabled(true);",
            "    m_container.setEnabled(false);",
            "    m_container.add(new JButton());",
            "  }",
            "}");
    assertHierarchy(
        "{this: test.MyPanel} {this} {/setEnabled(true)/}",
        "  {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}",
        "  {field: javax.swing.JPanel} {m_container} {/m_container.setEnabled(false)/ /m_container.add(new JButton())/}",
        "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "    {new: javax.swing.JButton} {empty} {/m_container.add(new JButton())/}");
    // do delete
    ComponentInfo container = panel.getChildrenComponents().get(0);
    assertTrue(container.getCreationSupport().canDelete());
    assertTrue(container.canDelete());
    container.delete();
    assertHierarchy(
        "{this: test.MyPanel} {this} {/setEnabled(true)/}",
        "  {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}",
        "  {field: javax.swing.JPanel} {m_container} {}",
        "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
    assertEditor(
        "public class Test extends MyPanel {",
        "  public Test() {",
        "    setEnabled(true);",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Decoration
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that icon of {@link ExposedFieldCreationSupport} component is decorated.
   */
  public void test_decorateIcon() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public final JPanel m_container;",
            "  public MyPanel() {",
            "    setLayout(new BorderLayout());",
            "    {",
            "      m_container = new JPanel();",
            "      add(m_container, BorderLayout.CENTER);",
            "    }",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    add(new JPanel());",
            "  }",
            "}");
    assertHierarchy(
        "{this: test.MyPanel} {this} {/add(new JPanel())/}",
        "  {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}",
        "  {field: javax.swing.JPanel} {m_container} {}",
        "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JPanel} {empty} {/add(new JPanel())/}",
        "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
    ComponentInfo exposedContainer = panel.getChildrenComponents().get(0);
    ComponentInfo innerPanel = panel.getChildrenComponents().get(1);
    // both components are JPanel's...
    assertSame(exposedContainer.getDescription(), innerPanel.getDescription());
    // ...but their icons are different, because (probably) decorator applied
    assertSame(
        innerPanel.getPresentation().getIcon(),
        ObjectsLabelProvider.INSTANCE.getImage(innerPanel));
    assertNotSame(
        exposedContainer.getPresentation().getIcon(),
        ObjectsLabelProvider.INSTANCE.getImage(exposedContainer));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // isDirect()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ExposedFieldCreationSupport#isDirect()}.
   */
  public void test_isDirect_true() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public final JPanel m_container;",
            "  public final JButton m_button;",
            "  public MyPanel() {",
            "    setLayout(new BorderLayout());",
            "    {",
            "      m_container = new JPanel();",
            "      add(m_container, BorderLayout.CENTER);",
            "    }",
            "    {",
            "      m_button = new JButton();",
            "      add(m_button, BorderLayout.WEST);",
            "    }",
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
    assertHierarchy(
        "{this: test.MyPanel} {this} {}",
        "  {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}",
        "  {field: javax.swing.JPanel} {m_container} {}",
        "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {field: javax.swing.JButton} {m_button} {}");
    ComponentInfo button = panel.getChildrenComponents().get(1);
    // try isDirect()
    ExposedFieldCreationSupport creationSupport =
        (ExposedFieldCreationSupport) button.getCreationSupport();
    assertSame(panel, creationSupport.getHostJavaInfo());
    assertTrue(creationSupport.isDirect());
  }

  /**
   * Test for {@link ExposedFieldCreationSupport#isDirect()}.
   */
  public void test_isDirect_false() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public final JPanel m_container;",
            "  public final JButton m_button;",
            "  public MyPanel() {",
            "    setLayout(new BorderLayout());",
            "    {",
            "      m_container = new JPanel();",
            "      add(m_container, BorderLayout.CENTER);",
            "      {",
            "        m_button = new JButton();",
            "        m_container.add(m_button);",
            "      }",
            "    }",
            "  }",
            "}"));
    waitForAutoBuild();
    String[] lines = {"public class Test extends MyPanel {", "  public Test() {", "  }", "}"};
    // parse
    ContainerInfo panel = parseContainer(lines);
    assertHierarchy(
        "{this: test.MyPanel} {this} {}",
        "  {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}",
        "  {field: javax.swing.JPanel} {m_container} {}",
        "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "    {field: javax.swing.JButton} {m_button} {}");
    ContainerInfo container = (ContainerInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button = container.getChildrenComponents().get(0);
    // try isDirect()
    ExposedFieldCreationSupport creationSupport =
        (ExposedFieldCreationSupport) button.getCreationSupport();
    assertSame(panel, creationSupport.getHostJavaInfo());
    assertFalse(creationSupport.isDirect());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link IClipboardImplicitCreationSupport} implementation.
   */
  public void test_clipboard() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public final JButton m_button;",
            "  public MyPanel() {",
            "    {",
            "      m_button = new JButton();",
            "      add(m_button);",
            "    }",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      MyPanel myPanel_0 = new MyPanel();",
            "      add(myPanel_0);",
            "      myPanel_0.m_button.setEnabled(false);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(myPanel_0)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: test.MyPanel} {local-unique: myPanel_0} {/new MyPanel()/ /add(myPanel_0)/ /myPanel_0.m_button/}",
        "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "    {field: javax.swing.JButton} {m_button} {/myPanel_0.m_button.setEnabled(false)/}");
    ComponentInfo myPanel_0 = panel.getChildrenComponents().get(0);
    panel.refresh();
    // create MyPanel copy
    JavaInfoMemento memento = JavaInfoMemento.createMemento(myPanel_0);
    ComponentInfo myPanel_copy = (ComponentInfo) memento.create(panel);
    // add MyPanel copy
    ((FlowLayoutInfo) panel.getLayout()).add(myPanel_copy, null);
    memento.apply();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      MyPanel myPanel_0 = new MyPanel();",
        "      add(myPanel_0);",
        "      myPanel_0.m_button.setEnabled(false);",
        "    }",
        "    {",
        "      MyPanel myPanel = new MyPanel();",
        "      myPanel.m_button.setEnabled(false);",
        "      add(myPanel);",
        "    }",
        "  }",
        "}");
  }
}
