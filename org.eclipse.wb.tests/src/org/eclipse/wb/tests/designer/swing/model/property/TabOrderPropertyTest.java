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
package org.eclipse.wb.tests.designer.swing.model.property;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.order.TabOrderInfo;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipProvider;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipTextProvider;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.Activator;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.internal.swing.model.property.TabOrderProperty;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.util.List;

/**
 * Test for {@link TabOrderProperty}.
 *
 * @author lobas_av
 */
public class TabOrderPropertyTest extends SwingModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // SetUp
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    if (m_testProject != null) {
      ProjectUtils.ensureResourceType(
          m_testProject.getJavaProject(),
          Activator.getDefault().getBundle(),
          "org.eclipse.wb.swing.FocusTraversalOnArray");
    }
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
  public void test_common() throws Exception {
    // create panel
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    // property
    TabOrderProperty property = (TabOrderProperty) panel.getPropertyByTitle("tab order");
    assertNotNull(property);
    assertFalse(property.isModified());
    // tooltip
    assertNull(property.getAdapter(Object.class));
    PropertyTooltipProvider tooltipProvider = property.getAdapter(PropertyTooltipProvider.class);
    assertInstanceOf(PropertyTooltipTextProvider.class, tooltipProvider);
    assertNotNull(ReflectionUtils.invokeMethod(
        tooltipProvider,
        "getText(org.eclipse.wb.internal.core.model.property.Property)",
        property));
  }

  public void test_getValue_noValue() throws Exception {
    // create panel
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton('Button');",
            "    add(button);",
            "    JPanel panel = new JPanel();",
            "    add(panel);",
            "    JLabel label = new JLabel('Label');",
            "    panel.add(label);",
            "  }",
            "}");
    panel.refresh();
    // property
    TabOrderProperty property = (TabOrderProperty) panel.getPropertyByTitle("tab order");
    TabOrderInfo info = (TabOrderInfo) property.getValue();
    //
    List<ComponentInfo> components = panel.getChildrenComponents();
    ComponentInfo button = components.get(0);
    ContainerInfo subPanel = (ContainerInfo) components.get(1);
    ComponentInfo label = subPanel.getChildrenComponents().get(0);
    //
    assertEquals(3, info.getInfos().size());
    assertSame(button, info.getInfos().get(0));
    assertSame(subPanel, info.getInfos().get(1));
    assertSame(label, info.getInfos().get(2));
    //
    assertEquals(3, info.getOrderedInfos().size());
    assertSame(button, info.getOrderedInfos().get(0));
    assertSame(subPanel, info.getOrderedInfos().get(1));
    assertSame(label, info.getOrderedInfos().get(2));
  }

  public void test_getValue() throws Exception {
    // create panel
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton('Button');",
            "    add(button);",
            "    JComboBox combo = new JComboBox();",
            "    add(combo);",
            "    setFocusTraversalPolicy(new org.eclipse.wb.swing.FocusTraversalOnArray(new Component[]{button}));",
            "  }",
            "}");
    panel.refresh();
    // property
    TabOrderProperty property = (TabOrderProperty) panel.getPropertyByTitle("tab order");
    assertTrue(property.isModified());
    TabOrderInfo info = (TabOrderInfo) property.getValue();
    //
    List<ComponentInfo> components = panel.getChildrenComponents();
    ComponentInfo button = components.get(0);
    ComponentInfo combo = components.get(1);
    //
    assertEquals(2, info.getInfos().size());
    assertSame(button, info.getInfos().get(0));
    assertSame(combo, info.getInfos().get(1));
    //
    assertEquals(1, info.getOrderedInfos().size());
    assertSame(button, info.getOrderedInfos().get(0));
  }

  public void test_setValue_UNKNOWN_VALUE() throws Exception {
    test_setValue(
        new String[]{
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton('Button');",
            "    add(button);",
            "    JComboBox combo = new JComboBox();",
            "    add(combo);",
            "    setFocusTraversalPolicy(new org.eclipse.wb.swing.FocusTraversalOnArray(new Component[]{button}));",
            "  }",
            "}"},
        Property.UNKNOWN_VALUE,
        new String[]{
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton('Button');",
            "    add(button);",
            "    JComboBox combo = new JComboBox();",
            "    add(combo);",
            "  }",
            "}"});
  }

  public void test_setValue_noValue() throws Exception {
    test_setValue(
        new String[]{
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton('Button');",
            "    add(button);",
            "    JComboBox combo = new JComboBox();",
            "    add(combo);",
            "    setFocusTraversalPolicy(new org.eclipse.wb.swing.FocusTraversalOnArray(new Component[]{button}));",
            "  }",
            "}"},
        new TabOrderInfo(),
        new String[]{
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton('Button');",
            "    add(button);",
            "    JComboBox combo = new JComboBox();",
            "    add(combo);",
            "  }",
            "}"});
  }

  private void test_setValue(String[] startSource, Object value, String[] newSource)
      throws Exception {
    // create panel
    ContainerInfo container = parseContainer(startSource);
    container.refresh();
    // property
    TabOrderProperty property = (TabOrderProperty) container.getPropertyByTitle("tab order");
    property.setValue(value);
    // check source
    assertEditor(newSource);
  }

  public void test_setValue_noExisting() throws Exception {
    ContainerInfo container =
        parseContainer(
            "public class Test extends JPanel {",
            "  private JButton button_1;",
            "  private JButton button_2;",
            "  public Test() {",
            "    {",
            "      button_1 = new JButton();",
            "      add(button_1);",
            "    }",
            "    {",
            "      button_2 = new JButton();",
            "      add(button_2);",
            "    }",
            "  }",
            "}");
    container.refresh();
    ComponentInfo button_1 = getJavaInfoByName("button_1");
    TabOrderProperty property = (TabOrderProperty) container.getPropertyByTitle("tab order");
    // set
    {
      TabOrderInfo newValue = new TabOrderInfo();
      newValue.addOrderedInfo(button_1);
      property.setValue(newValue);
    }
    assertEditor(
        "import org.eclipse.wb.swing.FocusTraversalOnArray;",
        "public class Test extends JPanel {",
        "  private JButton button_1;",
        "  private JButton button_2;",
        "  public Test() {",
        "    {",
        "      button_1 = new JButton();",
        "      add(button_1);",
        "    }",
        "    {",
        "      button_2 = new JButton();",
        "      add(button_2);",
        "    }",
        "    setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{button_1}));",
        "  }",
        "}");
  }

  /**
   * If {@link Container#setFocusTraversalPolicy(FocusTraversalPolicy)} was direct after first
   * component, but before second one, and we include second component, this caused exception.
   */
  public void test_setValue_hasExisting() throws Exception {
    ContainerInfo container =
        parseContainer(
            "import org.eclipse.wb.swing.FocusTraversalOnArray;",
            "public class Test extends JPanel {",
            "  private JButton button_1;",
            "  private JButton button_2;",
            "  public Test() {",
            "    {",
            "      button_1 = new JButton();",
            "      add(button_1);",
            "    }",
            "    setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{button_1}));",
            "    {",
            "      button_2 = new JButton();",
            "      add(button_2);",
            "    }",
            "  }",
            "}");
    container.refresh();
    ComponentInfo button_1 = getJavaInfoByName("button_1");
    ComponentInfo button_2 = getJavaInfoByName("button_2");
    TabOrderProperty property = (TabOrderProperty) container.getPropertyByTitle("tab order");
    // set
    {
      TabOrderInfo newValue = new TabOrderInfo();
      newValue.addOrderedInfo(button_1);
      newValue.addOrderedInfo(button_2);
      property.setValue(newValue);
    }
    assertEditor(
        "import org.eclipse.wb.swing.FocusTraversalOnArray;",
        "public class Test extends JPanel {",
        "  private JButton button_1;",
        "  private JButton button_2;",
        "  public Test() {",
        "    {",
        "      button_1 = new JButton();",
        "      add(button_1);",
        "    }",
        "    {",
        "      button_2 = new JButton();",
        "      add(button_2);",
        "    }",
        "    setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{button_1, button_2}));",
        "  }",
        "}");
  }

  /**
   * {@link Container#setFocusTraversalPolicy(FocusTraversalPolicy)} should be last method, this
   * should be kept even when we add new component.
   */
  public void test_hasValue_addNewComponent() throws Exception {
    ContainerInfo container =
        parseContainer(
            "import org.eclipse.wb.swing.FocusTraversalOnArray;",
            "public class Test extends JPanel {",
            "  private JButton button_1;",
            "  private JButton button_2;",
            "  public Test() {",
            "    {",
            "      button_1 = new JButton();",
            "      add(button_1);",
            "    }",
            "    {",
            "      button_2 = new JButton();",
            "      add(button_2);",
            "    }",
            "    setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{button_1}));",
            "  }",
            "}");
    container.refresh();
    // add new JButton
    {
      ComponentInfo newButton = createJButton();
      ((FlowLayoutInfo) container.getLayout()).add(newButton, null);
    }
    assertEditor(
        "import org.eclipse.wb.swing.FocusTraversalOnArray;",
        "public class Test extends JPanel {",
        "  private JButton button_1;",
        "  private JButton button_2;",
        "  public Test() {",
        "    {",
        "      button_1 = new JButton();",
        "      add(button_1);",
        "    }",
        "    {",
        "      button_2 = new JButton();",
        "      add(button_2);",
        "    }",
        "    {",
        "      JButton button = new JButton();",
        "      add(button);",
        "    }",
        "    setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{button_1}));",
        "  }",
        "}");
  }

  public void test_delete_JPanel() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "import org.eclipse.wb.swing.FocusTraversalOnArray;",
            "public class Test extends JPanel {",
            "  private JButton button;",
            "  private JComboBox combo;",
            "  public Test() {",
            "    setLayout(new FlowLayout());",
            "    button = new JButton('Button');",
            "    add(button);",
            "    combo = new JComboBox();",
            "    add(combo);",
            "    JTextField text = new JTextField();",
            "    add(text);",
            "    setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{button, combo}));",
            "  }",
            "}");
    panel.refresh();
    test_delete(panel, new String[]{
        "import org.eclipse.wb.swing.FocusTraversalOnArray;",
        "public class Test extends JPanel {",
        "  private JButton button;",
        "  private JComboBox combo;",
        "  public Test() {",
        "    setLayout(new FlowLayout());",
        "    button = new JButton('Button');",
        "    add(button);",
        "    combo = new JComboBox();",
        "    add(combo);",
        "    setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{button, combo}));",
        "  }",
        "}"}, new String[]{
        "import org.eclipse.wb.swing.FocusTraversalOnArray;",
        "public class Test extends JPanel {",
        "  private JButton button;",
        "  public Test() {",
        "    setLayout(new FlowLayout());",
        "    button = new JButton('Button');",
        "    add(button);",
        "    setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{button}));",
        "  }",
        "}"}, new String[]{
        "import org.eclipse.wb.swing.FocusTraversalOnArray;",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FlowLayout());",
        "  }",
        "}"}, panel.getChildrenComponents());
  }

  public void test_delete_JFrame() throws Exception {
    ContainerInfo frame =
        parseContainer(
            "import org.eclipse.wb.swing.FocusTraversalOnArray;",
            "public class Test extends JFrame {",
            "  private JPanel m_contentPane;",
            "  private JButton button;",
            "  private JComboBox combo;",
            "  public Test() {",
            "    m_contentPane = new JPanel();",
            "    m_contentPane.setLayout(new FlowLayout());",
            "    setContentPane(m_contentPane);",
            "    button = new JButton('Button');",
            "    m_contentPane.add(button);",
            "    combo = new JComboBox();",
            "    m_contentPane.add(combo);",
            "    JTextField text = new JTextField();",
            "    m_contentPane.add(text);",
            "    setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{button, combo}));",
            "  }",
            "}");
    frame.refresh();
    ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
    test_delete(frame, new String[]{
        "import org.eclipse.wb.swing.FocusTraversalOnArray;",
        "public class Test extends JFrame {",
        "  private JPanel m_contentPane;",
        "  private JButton button;",
        "  private JComboBox combo;",
        "  public Test() {",
        "    m_contentPane = new JPanel();",
        "    m_contentPane.setLayout(new FlowLayout());",
        "    setContentPane(m_contentPane);",
        "    button = new JButton('Button');",
        "    m_contentPane.add(button);",
        "    combo = new JComboBox();",
        "    m_contentPane.add(combo);",
        "    setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{button, combo}));",
        "  }",
        "}"}, new String[]{
        "import org.eclipse.wb.swing.FocusTraversalOnArray;",
        "public class Test extends JFrame {",
        "  private JPanel m_contentPane;",
        "  private JButton button;",
        "  public Test() {",
        "    m_contentPane = new JPanel();",
        "    m_contentPane.setLayout(new FlowLayout());",
        "    setContentPane(m_contentPane);",
        "    button = new JButton('Button');",
        "    m_contentPane.add(button);",
        "    setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{button}));",
        "  }",
        "}"}, new String[]{
        "import org.eclipse.wb.swing.FocusTraversalOnArray;",
        "public class Test extends JFrame {",
        "  private JPanel m_contentPane;",
        "  public Test() {",
        "    m_contentPane = new JPanel();",
        "    m_contentPane.setLayout(new FlowLayout());",
        "    setContentPane(m_contentPane);",
        "  }",
        "}"}, contentPane.getChildrenComponents());
  }

  private void test_delete(ContainerInfo container,
      String[] start,
      String[] middle,
      String[] end,
      List<ComponentInfo> components) throws Exception {
    // property
    TabOrderProperty property = (TabOrderProperty) container.getPropertyByTitle("tab order");
    TabOrderInfo info = (TabOrderInfo) property.getValue();
    //
    ComponentInfo button = components.get(0);
    ComponentInfo combo = components.get(1);
    ComponentInfo text = components.get(2);
    //
    assertEquals(2, info.getOrderedInfos().size());
    assertSame(button, info.getOrderedInfos().get(0));
    assertSame(combo, info.getOrderedInfos().get(1));
    //
    text.delete();
    assertEditor(start);
    //
    combo.delete();
    assertEditor(middle);
    //
    button.delete();
    assertEditor(end);
  }
}