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

import org.eclipse.wb.internal.core.model.property.order.TabOrderInfo;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.internal.swing.model.property.TabOrderProperty;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jdt.core.IJavaProject;

import java.util.List;

import javax.swing.JLabel;

/**
 * Test for {@link TabOrderProperty}.
 *
 * @author lobas_av
 */
public class TabOrderPropertyValueTest extends SwingModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    do_projectDispose();
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
  public void test_getValue_1() throws Exception {
    // create panel
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FlowLayout());",
            "    JButton button = new JButton('Button');",
            "    add(button);",
            "    JComboBox combo = new JComboBox();",
            "    add(combo);",
            "  }",
            "}");
    panel.refresh();
    // property
    TabOrderProperty property = (TabOrderProperty) panel.getPropertyByTitle("tab order");
    Object value = property.getValue();
    assertNotNull(value);
    assertInstanceOf(TabOrderInfo.class, value);
    //
    TabOrderInfo tabOrderInfo = (TabOrderInfo) value;
    assertEquals(2, tabOrderInfo.getInfos().size());
    assertEquals(2, tabOrderInfo.getOrderedInfos().size());
    //
    List<ComponentInfo> components = panel.getChildrenComponents();
    assertEquals(2, components.size());
    //
    assertSame(components.get(0), tabOrderInfo.getInfos().get(0));
    assertSame(components.get(1), tabOrderInfo.getInfos().get(1));
    //
    assertSame(components.get(0), tabOrderInfo.getOrderedInfos().get(0));
    assertSame(components.get(1), tabOrderInfo.getOrderedInfos().get(1));
  }

  public void DISABLE_test_getValue_2() throws Exception {
    ProjectUtils.ensureResourceType(
        m_javaProject,
        org.eclipse.wb.internal.swing.Activator.getDefault().getBundle(),
        "org.eclipse.wb.swing.FocusTraversalOnArray");
    // create panel
    ContainerInfo panel =
        parseContainer(
            "import org.eclipse.wb.swing.FocusTraversalOnArray;",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FlowLayout());",
            "    JButton button = new JButton('Button');",
            "    add(button);",
            "    JComboBox combo = new JComboBox();",
            "    add(combo);",
            "    setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{combo}));",
            "  }",
            "}");
    panel.refresh();
    // property
    TabOrderProperty property = (TabOrderProperty) panel.getPropertyByTitle("tab order");
    Object value = property.getValue();
    assertNotNull(value);
    assertInstanceOf(TabOrderInfo.class, value);
    //
    TabOrderInfo tabOrderInfo = (TabOrderInfo) value;
    assertEquals(2, tabOrderInfo.getInfos().size());
    assertEquals(1, tabOrderInfo.getOrderedInfos().size());
    //
    List<ComponentInfo> components = panel.getChildrenComponents();
    assertEquals(2, components.size());
    //
    assertSame(components.get(0), tabOrderInfo.getInfos().get(1));
    assertSame(components.get(1), tabOrderInfo.getInfos().get(0));
    //
    assertSame(components.get(1), tabOrderInfo.getOrderedInfos().get(0));
  }

  public void DISABLE_test_setValue() throws Exception {
    // create panel
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FlowLayout());",
            "    JButton button = new JButton('Button');",
            "    add(button);",
            "    JComboBox combo = new JComboBox();",
            "    add(combo);",
            "  }",
            "}");
    panel.refresh();
    // property
    TabOrderProperty property = (TabOrderProperty) panel.getPropertyByTitle("tab order");
    assertFalse(property.isModified());
    //
    IJavaProject javaProject = m_testProject.getJavaProject();
    assertTrue(javaProject.findType("org.eclipse.wb.swing.FocusTraversalOnArray") == null);
    //
    TabOrderInfo newValue = new TabOrderInfo();
    newValue.addOrderedInfo(panel.getChildrenComponents().get(1));
    //
    property.setValue(newValue);
    assertTrue(javaProject.findType("org.eclipse.wb.swing.FocusTraversalOnArray") != null);
    // check source
    assertEditor(
        "import org.eclipse.wb.swing.FocusTraversalOnArray;",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FlowLayout());",
        "    JButton button = new JButton('Button');",
        "    add(button);",
        "    JComboBox combo = new JComboBox();",
        "    add(combo);",
        "    setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{combo}));",
        "  }",
        "}");
    assertTrue(property.isModified());
    // add new component
    FlowLayoutInfo layout = (FlowLayoutInfo) panel.getLayout();
    ComponentInfo newComponent = createComponent(JLabel.class);
    layout.add(newComponent, null);
    // check source
    assertEditor(
        "import org.eclipse.wb.swing.FocusTraversalOnArray;",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FlowLayout());",
        "    JButton button = new JButton('Button');",
        "    add(button);",
        "    JComboBox combo = new JComboBox();",
        "    add(combo);",
        "    {",
        "      JLabel label = new JLabel('New label');",
        "      add(label);",
        "    }",
        "    setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{combo}));",
        "  }",
        "}");
  }
}