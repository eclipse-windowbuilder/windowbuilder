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

import org.eclipse.wb.internal.core.model.creation.ExposedPropertyCreationSupport;
import org.eclipse.wb.internal.core.model.util.ObjectsLabelProvider;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import javax.swing.JButton;

/**
 * Test for {@link ExposedPropertyCreationSupport}.
 * 
 * @author scheglov_ke
 */
public class ExposedPropertyCreationSupportTest extends SwingModelTest {
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
   * Simplest test for parsing {@link ExposedPropertyCreationSupport}.
   */
  public void test_parse_0() throws Exception {
    ContainerInfo frame =
        parseContainer(
            "// filler filler filler filler filler",
            "public class Test extends JFrame {",
            "  public Test() {",
            "  }",
            "}");
    assertEquals(1, frame.getChildrenComponents().size());
    ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
    // check that call components (including exposed) has objects
    assert_creation(frame);
    // check CreationSupport
    ExposedPropertyCreationSupport creationSupport =
        (ExposedPropertyCreationSupport) contentPane.getCreationSupport();
    assertEquals(frame.getCreationSupport().getNode(), creationSupport.getNode());
    assertSame(frame, creationSupport.getHostJavaInfo());
    assertEquals("getContentPane", creationSupport.getMethod().getName());
    // operations validation
    assertFalse(creationSupport.canReorder());
    assertFalse(creationSupport.canReparent());
  }

  /**
   * Test that we can add components on exposed containers.
   */
  public void test_parse_1() throws Exception {
    setFileContentSrc(
        "test/TitlePanel.java",
        getTestSource(
            "public class TitlePanel extends JPanel {",
            "  private final JLabel m_titleLabel;",
            "  private final JPanel m_container;",
            "  public TitlePanel() {",
            "    setLayout(new BorderLayout());",
            "    {",
            "      m_titleLabel = new JLabel();",
            "      m_titleLabel.setOpaque(true);",
            "      m_titleLabel.setBackground(Color.ORANGE);",
            "      m_titleLabel.setText('Title');",
            "      m_titleLabel.setHorizontalAlignment(SwingConstants.CENTER);",
            "      add(m_titleLabel, BorderLayout.NORTH);",
            "    }",
            "    {",
            "      m_container = new JPanel();",
            "      add(m_container, BorderLayout.CENTER);",
            "    }",
            "  }",
            "  public JPanel getContainer() {",
            "    return m_container;",
            "  }",
            "  public Component getNullComponent() {",
            "    return null;",
            "  }",
            "}"));
    waitForAutoBuild();
    // JFrame
    ContainerInfo frame =
        parseContainer(
            "class Test extends JFrame {",
            "  public Test() {",
            "    TitlePanel titlePanel = new TitlePanel();",
            "    getContentPane().add(titlePanel, BorderLayout.CENTER);",
            "    {",
            "      JButton button = new JButton('JButton on exposed container');",
            "      titlePanel.getContainer().add(button);",
            "    }",
            "  }",
            "}");
    assert_isCleanHierarchy(frame);
    assertHierarchy(
        "{this: javax.swing.JFrame} {this} {}",
        "  {method: public java.awt.Container javax.swing.JFrame.getContentPane()} {property} {/getContentPane().add(titlePanel, BorderLayout.CENTER)/}",
        "    {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}",
        "    {new: test.TitlePanel} {local-unique: titlePanel} {/new TitlePanel()/ /getContentPane().add(titlePanel, BorderLayout.CENTER)/ /titlePanel.getContainer()/}",
        "      {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}",
        "      {method: public javax.swing.JPanel test.TitlePanel.getContainer()} {property} {/titlePanel.getContainer().add(button)/}",
        "        {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "        {new: javax.swing.JButton} {local-unique: button} {/new JButton('JButton on exposed container')/ /titlePanel.getContainer().add(button)/}");
    //
    assert_creation(frame);
  }

  /**
   * Test that during parsing we restore "logical" parent/child hierarchy.
   */
  public void test_parse_2() throws Exception {
    setFileContentSrc(
        "test/PanelExpose.java",
        getTestSource(
            "public class PanelExpose extends JPanel {",
            "  private final JLabel m_label;",
            "  private final JPanel m_container;",
            "  private final JButton m_button;",
            "  private final JCheckBox m_check;",
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
            "      {",
            "        m_check = new JCheckBox();",
            "        m_container.add(m_check);",
            "        m_check.setBounds(10, 50, 200, 30);",
            "      }",
            "    }",
            "    {",
            "      m_label = new JLabel('Label');",
            "      add(m_label, BorderLayout.NORTH);",
            "    }",
            "  }",
            "  public JLabel getLabel() {",
            "    return m_label;",
            "  }",
            "  public JPanel getContainer() {",
            "    return m_container;",
            "  }",
            "  public JButton getButton() {",
            "    return m_button;",
            "  }",
            "  public JCheckBox getCheck() {",
            "    return m_check;",
            "  }",
            "}"));
    waitForAutoBuild();
    // JFrame
    ContainerInfo frame =
        parseContainer(
            "public class Test extends JFrame {",
            "  public Test() {",
            "    PanelExpose titlePanel = new PanelExpose();",
            "    getContentPane().add(titlePanel, BorderLayout.CENTER);",
            "    {",
            "      JButton button = new JButton('JButton on exposed container');",
            "      titlePanel.getContainer().add(button);",
            "      button.setBounds(10, 100, 300, 40);",
            "    }",
            "  }",
            "}");
    assert_isCleanHierarchy(frame);
    assertHierarchy(
        "{this: javax.swing.JFrame} {this} {}",
        "  {method: public java.awt.Container javax.swing.JFrame.getContentPane()} {property} {/getContentPane().add(titlePanel, BorderLayout.CENTER)/}",
        "    {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}",
        "    {new: test.PanelExpose} {local-unique: titlePanel} {/new PanelExpose()/ /getContentPane().add(titlePanel, BorderLayout.CENTER)/ /titlePanel.getContainer()/}",
        "      {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}",
        "      {method: public javax.swing.JPanel test.PanelExpose.getContainer()} {property} {/titlePanel.getContainer().add(button)/}",
        "        {implicit-layout: absolute} {implicit-layout} {}",
        "        {method: public javax.swing.JButton test.PanelExpose.getButton()} {property} {}",
        "        {method: public javax.swing.JCheckBox test.PanelExpose.getCheck()} {property} {}",
        "        {new: javax.swing.JButton} {local-unique: button} {/new JButton('JButton on exposed container')/ /titlePanel.getContainer().add(button)/ /button.setBounds(10, 100, 300, 40)/}",
        "      {method: public javax.swing.JLabel test.PanelExpose.getLabel()} {property} {}");
    //
    assert_creation(frame);
  }

  /**
   * Test that we can resolve "logical" exposed children.
   */
  public void test_parse_3() throws Exception {
    setFileContentSrc(
        "test/PanelExpose.java",
        getTestSource(
            "public class PanelExpose extends JPanel {",
            "  private final JPanel m_container;",
            "  private final JButton m_button;",
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
            "  public JPanel getContainer() {",
            "    return m_container;",
            "  }",
            "  public JButton getButton() {",
            "    return m_button;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "public class Test extends PanelExpose {",
        "  public Test() {",
        "    getButton().setText('foo');",
        "  }",
        "}");
    assertHierarchy(
        "{this: test.PanelExpose} {this} {}",
        "  {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}",
        "  {method: public javax.swing.JPanel test.PanelExpose.getContainer()} {property} {}",
        "    {implicit-layout: absolute} {implicit-layout} {}",
        "    {method: public javax.swing.JButton test.PanelExpose.getButton()} {property} {/getButton().setText('foo')/}");
  }

  /**
   * There are cases when sub-class overrides method to return some container, and super-class
   * assigns this method into field and has getter to access some component from this container. We
   * handle this getter as exposed component
   */
  public void test_parse_getExposedWhileCreateItsContainer() throws Exception {
    setFileContentSrc(
        "test/MyContainer.java",
        getTestSource(
            "public class MyContainer extends JPanel {",
            "  private final JButton m_button = new JButton();",
            "  public MyContainer() {",
            "    add(m_button);",
            "  }",
            "  public JButton getButton() {",
            "    return m_button;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public abstract class MyPanel extends JPanel {",
            "  private final MyContainer m_container;",
            "  public MyPanel() {",
            "    m_container = createContainer();",
            "    add(m_container);",
            "  }",
            "  public JButton getButton() {",
            "    return m_container.getButton();",
            "  }",
            "  protected abstract MyContainer createContainer();",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "  protected MyContainer createContainer() {",
            "    return new MyContainer();",
            "  }",
            "}");
    assertHierarchy(
        "{this: test.MyPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: test.MyContainer} {empty} {/new MyContainer()/}",
        "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "    {method: public javax.swing.JButton test.MyContainer.getButton()} {property} {}",
        "  {method: public javax.swing.JButton test.MyPanel.getButton()} {property} {}");
    // refresh
    refresh();
    assertNoErrors(m_lastParseInfo);
    // check that getButton() has object
    ComponentInfo button = panel.getChildrenComponents().get(1);
    assertEquals(
        "method: public javax.swing.JButton test.MyPanel.getButton()",
        button.getCreationSupport().toString());
    assertInstanceOf(JButton.class, button.getObject());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Components with {@link ExposedPropertyCreationSupport} can be "deleted", but for them this
   * means that their delete children and related nodes, but keep themselves in parent.
   */
  public void test_delete() throws Exception {
    ContainerInfo frame =
        parseContainer(
            "public final class Test extends JFrame {",
            "  public Test() {",
            "    getContentPane().setEnabled(false);",
            "    setTitle('My frame');",
            "    //",
            "    JButton button = new JButton();",
            "    getContentPane().add(button);",
            "  }",
            "}");
    // prepare content pane
    assertEquals(1, frame.getChildrenComponents().size());
    ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
    // do delete
    assertTrue(contentPane.canDelete());
    contentPane.delete();
    assertEditor(
        "public final class Test extends JFrame {",
        "  public Test() {",
        "    setTitle('My frame');",
        "  }",
        "}");
    // content pane is still here, but has no children
    assertFalse(contentPane.isDeleted());
    assertTrue(contentPane.getChildrenComponents().isEmpty());
  }

  /**
   * Test that we can replace component exposed as <code>getX()</code> using method
   * <code>setX()</code>, and when we delete replacement, original exposed component is restored.
   */
  public void test_setContentPane_addDirectly() throws Exception {
    ContainerInfo frame =
        parseContainer(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public final class Test extends JFrame {",
            "  Test() {",
            "    JPanel panel = new JPanel();",
            "    setContentPane(panel);",
            "    panel.add(new JButton('My button'));",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JFrame} {this} {/setContentPane(panel)/}",
        "  {new: javax.swing.JPanel} {local-unique: panel} {/new JPanel()/ /setContentPane(panel)/ /panel.add(new JButton('My button'))/}",
        "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "    {new: javax.swing.JButton} {empty} {/panel.add(new JButton('My button'))/}");
    // JFrame is not replacement for anything
    assertFalse(ExposedPropertyCreationSupport.isReplacementForExposed(frame));
    // delete custom content pane, exposed default content pane should be restored
    ContainerInfo customContentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
    assertTrue(ExposedPropertyCreationSupport.isReplacementForExposed(customContentPane));
    customContentPane.delete();
    // check after delete
    assertEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public final class Test extends JFrame {",
        "  Test() {",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JFrame} {this} {}",
        "  {method: public java.awt.Container javax.swing.JFrame.getContentPane()} {property} {}",
        "    {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}");
  }

  /**
   * Test that we can replace component exposed as <code>getX()</code> using method
   * <code>setX()</code>, we still can add children using to <code>getX()</code>.
   * <p>
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=352568
   */
  public void test_setContentPane_addUsingGetter() throws Exception {
    parseContainer(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public final class Test extends JFrame {",
        "  Test() {",
        "    setContentPane(new JPanel());",
        "    getContentPane().add(new JButton());",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JFrame} {this} {/setContentPane(new JPanel())/}",
        "  {new: javax.swing.JPanel} {empty} {/setContentPane(new JPanel())/ /getContentPane().add(new JButton())/}",
        "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "    {new: javax.swing.JButton} {empty} {/getContentPane().add(new JButton())/}");
    refresh();
    assertNoErrors(m_lastParseInfo);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Decoration
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that icon of {@link ExposedPropertyCreationSupport} component is decorated.
   */
  public void test_decorateIcon() throws Exception {
    ContainerInfo frame =
        parseContainer(
            "public class Test extends JFrame {",
            "  public Test() {",
            "    getContentPane().add(new Container());",
            "  }",
            "}");
    assertEquals(1, frame.getChildrenComponents().size());
    // prepare exposed "content pane" and just Container instance
    ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
    ContainerInfo container = (ContainerInfo) contentPane.getChildrenComponents().get(0);
    // both components are Container's...
    assertSame(contentPane.getDescription(), container.getDescription());
    // ...but their icons are different, because (probably) decorator applied
    assertSame(
        container.getPresentation().getIcon(),
        ObjectsLabelProvider.INSTANCE.getImage(container));
    assertNotSame(
        contentPane.getPresentation().getIcon(),
        ObjectsLabelProvider.INSTANCE.getImage(contentPane));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // isDirect()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ExposedPropertyCreationSupport#isDirect()}
   */
  public void test_isDirect_true() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  private final JButton m_button = new JButton();",
            "  public MyPanel() {",
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
            "// filler filler filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertHierarchy(
        "{this: test.MyPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {method: public javax.swing.JButton test.MyPanel.getButton()} {property} {}");
    // try isDirect()
    ComponentInfo button = panel.getChildrenComponents().get(0);
    ExposedPropertyCreationSupport creationSupport =
        (ExposedPropertyCreationSupport) button.getCreationSupport();
    assertTrue(creationSupport.isDirect());
  }

  /**
   * Test for {@link ExposedPropertyCreationSupport#isDirect()}
   */
  public void test_isDirect_false() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  private final JButton m_button = new JButton();",
            "  public MyPanel() {",
            "    JPanel inner = new JPanel();",
            "    add(inner);",
            "    inner.add(m_button);",
            "  }",
            "  public JButton getButton() {",
            "    return m_button;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertHierarchy(
        "{this: test.MyPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {method: public javax.swing.JButton test.MyPanel.getButton()} {property} {}");
    // try isDirect()
    ComponentInfo button = panel.getChildrenComponents().get(0);
    ExposedPropertyCreationSupport creationSupport =
        (ExposedPropertyCreationSupport) button.getCreationSupport();
    assertFalse(creationSupport.isDirect());
  }
}
