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
package org.eclipse.wb.tests.designer.core.model.nonvisual;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.internal.core.model.generation.statement.lazy.LazyStatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.nonvisual.NonVisualAssociation;
import org.eclipse.wb.internal.core.model.nonvisual.NonVisualBeanContainerInfo;
import org.eclipse.wb.internal.core.model.nonvisual.NonVisualBeanInfo;
import org.eclipse.wb.internal.core.model.variable.description.LazyVariableDescription;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;
import org.eclipse.wb.tests.designer.swing.SwingTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Tests for <i>non-visual beans</i>.
 * 
 * @author lobas_av
 */
public class NonVisualBeansTest extends SwingModelTest {
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
  public void test_noBeans() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "import java.util.ArrayList;",
            "class Test extends JPanel {",
            "  private ArrayList m_testBean = new ArrayList();",
            "  Test() {",
            "  }",
            "}");
    // check
    assertNull(NonVisualBeanContainerInfo.find(panel));
  }

  public void test_constructorBean() throws Exception {
    // prepare source
    ContainerInfo panel =
        parseContainer(
            "import java.util.ArrayList;",
            "class Test extends JPanel {",
            "  /**",
            "  * @wbp.nonvisual location=10,20",
            "  */",
            "  private ArrayList m_testBean = new ArrayList();",
            "  Test() {",
            "  }",
            "}");
    // check
    test_nonVisual(panel);
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {NonVisualBeans}",
        "    {new: java.util.ArrayList} {field-initializer: m_testBean} {/new ArrayList()/}");
    // check association
    JavaInfo bean = getNVO(panel);
    assertThat(bean.getAssociation()).isInstanceOf(NonVisualAssociation.class);
  }

  public void test_swingBean() throws Exception {
    // prepare source
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  /**",
            "  * @wbp.nonvisual location=10,20",
            "  */",
            "  private JButton m_testBean = new JButton();",
            "  Test() {",
            "  }",
            "}");
    panel.refresh();
    // check
    test_nonVisual(panel);
  }

  /**
   * Test for {@link NonVisualBeanInfo#isNVO(JavaInfo)}.
   */
  public void test_isNVO() throws Exception {
    JavaInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  /**",
            "  * @wbp.nonvisual location=10,20",
            "  */",
            "  private JButton button = new JButton();",
            "  Test() {",
            "  }",
            "}");
    JavaInfo button = getJavaInfoByName("button");
    //
    assertFalse(NonVisualBeanInfo.isNVO(panel));
    assertTrue(NonVisualBeanInfo.isNVO(button));
  }

  public void test_staticFactory() throws Exception {
    setFileContentSrc(
        "test/MyStaticFactory.java",
        getTestSource(
            "public final class MyStaticFactory {",
            "  public static Object createTestBean() {",
            "    return new Object();",
            "  }",
            "}"));
    waitForAutoBuild();
    // prepare source
    ContainerInfo panel =
        parseContainer(
            "import java.util.ArrayList;",
            "class Test extends JPanel {",
            "  /**",
            "  * @wbp.nonvisual location=10,20",
            "  */",
            "  private Object m_testBean = MyStaticFactory.createTestBean();",
            "  Test() {",
            "  }",
            "}");
    // check
    test_nonVisual(panel);
  }

  public void test_instanceFactory() throws Exception {
    setFileContentSrc(
        "test/MyInstanceFactory.java",
        getTestSource(
            "public final class MyInstanceFactory {",
            "  public Object createTestBean() {",
            "    return new Object();",
            "  }",
            "}"));
    waitForAutoBuild();
    // prepare source
    ContainerInfo panel =
        parseContainer(
            "import java.util.ArrayList;",
            "class Test extends JPanel {",
            "  private MyInstanceFactory factory = new MyInstanceFactory();",
            "  /**",
            "  * @wbp.nonvisual location=10,20",
            "  */",
            "  private Object m_testBean = factory.createTestBean();",
            "  Test() {",
            "  }",
            "}");
    // check
    test_nonVisual(panel);
  }

  public void test_CREATE() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "class Test extends JPanel {",
            "  Test() {",
            "  }",
            "}");
    // check
    assertNull(NonVisualBeanContainerInfo.find(panel));
    //
    JavaInfo newInfo = createJavaInfo("java.util.ArrayList");
    NonVisualBeanContainerInfo.add(panel, newInfo, new Point(10, 20));
    // check
    test_nonVisual(panel);
    assertEditor(
        "import java.util.ArrayList;",
        "// filler filler filler",
        "class Test extends JPanel {",
        "  /**",
        "\t * @wbp.nonvisual location=10,20",
        "\t */",
        "  private final ArrayList arrayList = new ArrayList();",
        "  Test() {",
        "  }",
        "}");
  }

  public void test_moveBean() throws Exception {
    // prepare source
    ContainerInfo panel =
        parseContainer(
            "import java.util.ArrayList;",
            "class Test extends JPanel {",
            "  /**",
            "  * @wbp.nonvisual location=10,20",
            "  */",
            "  private ArrayList m_testBean = new ArrayList();",
            "  Test() {",
            "  }",
            "}");
    // check
    NonVisualBeanInfo beanInfo = test_nonVisual(panel);
    //
    beanInfo.moveLocation(new Point(5, 7));
    assertEquals(new Point(15, 27), beanInfo.getLocation());
    //
    assertEditor(
        "import java.util.ArrayList;",
        "class Test extends JPanel {",
        "  /**",
        "  * @wbp.nonvisual location=15,27",
        "  */",
        "  private ArrayList m_testBean = new ArrayList();",
        "  Test() {",
        "  }",
        "}");
  }

  /**
   * We should be able to move NVO to container, so make it VO.
   */
  public void test_convertInto_VO() throws Exception {
    // prepare source
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  /**",
            "  * @wbp.nonvisual location=10,20",
            "  */",
            "  private JButton m_button = new JButton();",
            "  Test() {",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {NonVisualBeans}",
        "    {new: javax.swing.JButton} {field-initializer: m_button} {/new JButton()/}");
    panel.refresh();
    ComponentInfo button = (ComponentInfo) getNVO(panel);
    assertThat(button.getAssociation()).isInstanceOf(NonVisualAssociation.class);
    // move "button" to "panel"
    ((FlowLayoutInfo) panel.getLayout()).move(button, null);
    assertEditor(
        "class Test extends JPanel {",
        "  private JButton m_button = new JButton();",
        "  Test() {",
        "    add(m_button);",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(m_button)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {NonVisualBeans}",
        "  {new: javax.swing.JButton} {field-initializer: m_button} {/new JButton()/ /add(m_button)/}");
    assertThat(button.getAssociation()).isInstanceOf(InvocationChildAssociation.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Lazy
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_lazy_parse() throws Exception {
    // prepare source
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  private Object m_testBean;",
            "  Test() {",
            "  }",
            "  /**",
            "  * @wbp.nonvisual location=10,20",
            "  */",
            "  private Object getTestBean() {",
            "    if (m_testBean == null) {",
            "      m_testBean = new Object();",
            "    }",
            "    return m_testBean;",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {NonVisualBeans}",
        "    {new: java.lang.Object} {lazy: m_testBean getTestBean()} {/new Object()/ /m_testBean/}");
    test_nonVisual(panel);
    // check association
    JavaInfo bean = getNVO(panel);
    assertThat(bean.getAssociation()).isInstanceOf(NonVisualAssociation.class);
  }

  public void test_lazy_CREATE() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    // initially no NVO container
    assertNull(NonVisualBeanContainerInfo.find(panel));
    // add new NVO
    JavaInfo newNVO = createJavaInfo("java.lang.Object");
    SwingTestUtils.setGenerations(
        LazyVariableDescription.INSTANCE,
        LazyStatementGeneratorDescription.INSTANCE);
    NonVisualBeanContainerInfo.add(panel, newNVO, new Point(10, 20));
    // now we have NVO in container
    test_nonVisual(panel);
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  private Object object;",
        "  public Test() {",
        "  }",
        "  /**",
        "\t * @wbp.nonvisual location=10,20",
        "\t */",
        "  private Object getObject() {",
        "    if (object == null) {",
        "      object = new Object();",
        "    }",
        "    return object;",
        "  }",
        "}");
    // there was problem that NVO not included into execution flow
    panel.refresh();
    assertNotNull(newNVO.getObject());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static NonVisualBeanInfo test_nonVisual(ContainerInfo panel) throws Exception {
    JavaInfo child = getNVO(panel);
    // check association
    assertThat(child.getAssociation()).isInstanceOf(NonVisualAssociation.class);
    // prepare NVO information
    NonVisualBeanInfo beanInfo = NonVisualBeanInfo.getNonVisualInfo(child);
    assertNotNull(beanInfo);
    // check location
    assertEquals(new Point(10, 20), beanInfo.getLocation());
    // return NVO information
    return beanInfo;
  }

  private static JavaInfo getNVO(ContainerInfo panel) {
    // prepare container
    NonVisualBeanContainerInfo container = NonVisualBeanContainerInfo.find(panel);
    assertNotNull(container);
    // prepare NVO-s
    List<JavaInfo> children = container.getChildren(JavaInfo.class);
    assertThat(children).hasSize(1);
    return children.get(0);
  }
}