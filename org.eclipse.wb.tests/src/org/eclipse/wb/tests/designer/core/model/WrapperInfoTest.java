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
package org.eclipse.wb.tests.designer.core.model;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.WrapperMethodInfo;
import org.eclipse.wb.core.model.association.WrappedObjectAssociation;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.JavaInfoUtils.HierarchyProvider;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.WrapperMethodControlCreationSupport;
import org.eclipse.wb.internal.core.model.creation.WrapperMethodCreationSupport;
import org.eclipse.wb.internal.core.model.generation.GenerationSettings;
import org.eclipse.wb.internal.core.model.variable.WrapperMethodControlVariableSupport;
import org.eclipse.wb.internal.core.model.variable.description.FieldUniqueVariableDescription;
import org.eclipse.wb.internal.core.parser.JavaInfoResolver;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.component.JPanelInfo;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodInvocation;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link WrapperMethodInfo}.
 * 
 * @author sablin_aa
 * @author scheglov_ke
 */
public class WrapperInfoTest extends SwingModelTest {
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
   * Test for {@link WrapperMethodInfo#isWrapper(AstEditor, Class)}.
   */
  public void test_isWrapper_forInterface() throws Exception {
    parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "      // filler",
        "  }",
        "}");
    assertFalse(WrapperMethodInfo.isWrapper(m_lastEditor, java.util.List.class));
  }

  public void test_parse_noControl() throws Exception {
    configureWrapperContents();
    ContainerInfo container =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    TestWrapper wrapper = new TestWrapper(this);",
            "  }",
            "}");
    // hierarchy
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/new TestWrapper(this)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {viewer: public javax.swing.JButton test.TestWrapper.getControl()} {viewer} {}",
        "    {new: test.TestWrapper} {local-unique: wrapper} {/new TestWrapper(this)/}");
    // container has JButton
    ComponentInfo wrappedComponent = container.getChildrenComponents().get(0);
    assertInstanceOf(WrappedObjectAssociation.class, wrappedComponent.getAssociation());
    assertInstanceOf(
        WrapperMethodControlCreationSupport.class,
        wrappedComponent.getCreationSupport());
    assertInstanceOf(
        WrapperMethodControlVariableSupport.class,
        wrappedComponent.getVariableSupport());
    // ... with wrapper
    WrapperMethodInfo wrapper = wrappedComponent.getChildren(WrapperMethodInfo.class).get(0);
    assertThat(wrapper.getWrapper().getWrappedInfo()).isSameAs(wrappedComponent);
  }

  /**
   * If we delete wrapper, then wrapped component should be deleted.
   */
  public void test_deleteWrapper() throws Exception {
    configureWrapperContents();
    ContainerInfo container =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    TestWrapper wrapper = new TestWrapper(this);",
            "    JButton button = wrapper.getControl();",
            "    button.setEnabled(false);",
            "  }",
            "  // filler filler filler",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/new TestWrapper(this)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {viewer: public javax.swing.JButton test.TestWrapper.getControl()} {local-unique: button} {/wrapper.getControl()/ /button.setEnabled(false)/}",
        "    {new: test.TestWrapper} {local-unique: wrapper} {/new TestWrapper(this)/ /wrapper.getControl()/}");
    ComponentInfo wrapped = container.getChildrenComponents().get(0);
    WrapperMethodInfo wrapper = wrapped.getChildren(WrapperMethodInfo.class).get(0);
    // delete wrapper
    assertTrue(wrapper.canDelete());
    wrapper.delete();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "  // filler filler filler",
        "}");
  }

  /**
   * We should be able to delete wrapped.
   */
  public void test_deleteWrapped() throws Exception {
    configureWrapperContents();
    ContainerInfo container =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    TestWrapper wrapper = new TestWrapper(this);",
            "    JButton button = wrapper.getControl();",
            "    button.setEnabled(false);",
            "  }",
            "  // filler filler filler",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/new TestWrapper(this)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {viewer: public javax.swing.JButton test.TestWrapper.getControl()} {local-unique: button} {/wrapper.getControl()/ /button.setEnabled(false)/}",
        "    {new: test.TestWrapper} {local-unique: wrapper} {/new TestWrapper(this)/ /wrapper.getControl()/}");
    ComponentInfo wrapped = container.getChildrenComponents().get(0);
    // delete wrapper
    assertTrue(wrapped.canDelete());
    wrapped.delete();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "  // filler filler filler",
        "}");
  }

  public void test_parse_aroundControl() throws Exception {
    configureWrapperContents();
    ContainerInfo container =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton panel = new JButton();",
            "    TestWrapper wrapper = new TestWrapper(panel);",
            "    add(panel);",
            "  }",
            "}");
    // container contains no wrappers in children
    assertThat(container.getChildren(WrapperMethodInfo.class)).isEmpty();
    // it contains JButton
    ContainerInfo wrappedComponent = container.getChildren(ContainerInfo.class).get(0);
    // ... with wrapper
    WrapperMethodInfo wrapper = wrappedComponent.getChildren(WrapperMethodInfo.class).get(0);
    assertThat(wrapper.getWrapper().getWrappedInfo()).isSameAs(wrappedComponent);
    // hierarchy
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(panel)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {local-unique: panel} {/new JButton()/ /new TestWrapper(panel)/ /add(panel)/}",
        "    {new: test.TestWrapper} {local-unique: wrapper} {/new TestWrapper(panel)/}");
  }

  public void test_materialize() throws Exception {
    configureWrapperContents();
    ContainerInfo container =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    TestWrapper wrapper = new TestWrapper(this);",
            "  }",
            "}");
    // container contains no wrappers in children
    assertThat(container.getChildren(WrapperMethodInfo.class)).isEmpty();
    // it contains JButton
    ContainerInfo wrappedComponent = container.getChildren(ContainerInfo.class).get(0);
    // ... with wrapper
    /*WrapperMethodInfo wrapper = */wrappedComponent.getChildren(WrapperMethodInfo.class).get(0);
    // set property
    wrappedComponent.getPropertyByTitle("text").setValue("test");
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    TestWrapper wrapper = new TestWrapper(this);",
        "    JButton button = wrapper.getControl();",
        "    button.setText('test');",
        "  }",
        "}");
  }

  public void test_CREATE() throws Exception {
    configureWrapperContents();
    ContainerInfo container =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    // create wrapper
    WrapperMethodInfo wrapperInfo = createJavaInfo("test.TestWrapper");
    JavaInfo wrappedInfo = wrapperInfo.getWrapper().getWrappedInfo();
    // check current CreationSupport
    CreationSupport creationSupport = wrappedInfo.getCreationSupport();
    assertInstanceOf(WrapperMethodCreationSupport.class, creationSupport);
    {
      assertEquals(
          "method: public javax.swing.JButton test.TestWrapper.getControl()",
          creationSupport.toString());
      // no node yet, and we can not it, because when it is set, we replace this CreationSupport
      {
        ASTNode node = creationSupport.getNode();
        assertNull(node);
      }
      // isJavaInfo()
      assertFalse(creationSupport.isJavaInfo(null));
      // permissions
      assertFalse(creationSupport.canReorder());
      assertFalse(creationSupport.canReparent());
    }
    // add wrapped control on panel
    JavaInfoUtils.add(wrappedInfo, null, container, null);
    // check editor
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      TestWrapper testWrapper = new TestWrapper(this);",
        "      JButton button = testWrapper.getControl();",
        "    }",
        "  }",
        "}");
  }

  /**
   * Viewers should use code generation settings.
   */
  public void test_CREATE_useFieldVariable() throws Exception {
    configureWrapperContents();
    String[] lines =
        {"public class Test extends JPanel {", "  public Test() {", "  }", "  // filler", "}"};
    ContainerInfo container = parseContainer(lines);
    // create wrapper
    WrapperMethodInfo wrapper = createJavaInfo("test.TestWrapper");
    JavaInfo wrapped = wrapper.getWrapper().getWrappedInfo();
    // add wrapped control on panel
    GenerationSettings settings = wrapper.getDescription().getToolkit().getGenerationSettings();
    settings.setVariable(FieldUniqueVariableDescription.INSTANCE);
    JavaInfoUtils.add(wrapped, null, container, null);
    assertEditor(
        "public class Test extends JPanel {",
        "  private JButton button;",
        "  private TestWrapper testWrapper;",
        "  public Test() {",
        "    {",
        "      testWrapper = new TestWrapper(this);",
        "      button = testWrapper.getControl();",
        "    }",
        "  }",
        "  // filler",
        "}");
  }

  public void test_MOVE_noControl() throws Exception {
    configureWrapperContents();
    ContainerInfo container =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    TestWrapper wrapper = new TestWrapper(this);",
            "    {",
            "      JPanel panel = new JPanel();",
            "      add(panel);",
            "    }",
            "  }",
            "}");
    // wrapper
    ContainerInfo wrappedComponent = container.getChildren(ContainerInfo.class).get(0);
    WrapperMethodInfo wrapper = wrappedComponent.getChildren(WrapperMethodInfo.class).get(0);
    // another panel
    JPanelInfo panel = container.getChildren(JPanelInfo.class).get(0);
    // move
    JavaInfoUtils.move(wrapper, null, panel, null);
    // check editor
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JPanel panel = new JPanel();",
        "      add(panel);",
        "      TestWrapper wrapper = new TestWrapper(panel);",
        "    }",
        "  }",
        "}");
  }

  public void test_MOVE_withControl() throws Exception {
    configureWrapperContents();
    ContainerInfo container =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    TestWrapper wrapper = new TestWrapper(this);",
            "    JButton button = wrapper.getControl();",
            "    {",
            "      JPanel panel = new JPanel();",
            "      add(panel);",
            "    }",
            "  }",
            "}");
    // wrapper
    ContainerInfo wrappedComponent = container.getChildren(ContainerInfo.class).get(0);
    WrapperMethodInfo wrapper = wrappedComponent.getChildren(WrapperMethodInfo.class).get(0);
    // another panel
    JPanelInfo panel = container.getChildren(JPanelInfo.class).get(0);
    // move
    JavaInfoUtils.move(wrapper, null, panel, null);
    // check editor
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JPanel panel = new JPanel();",
        "      add(panel);",
        "      TestWrapper wrapper = new TestWrapper(panel);",
        "      JButton button = wrapper.getControl();",
        "    }",
        "  }",
        "}");
  }

  public void test_clipboard() throws Exception {
    configureWrapperContents();
    ContainerInfo container =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JPanel panel_1 = new JPanel();",
            "      add(panel_1);",
            "      TestWrapper wrapper = new TestWrapper(panel_1);",
            "      JButton button = wrapper.getControl();",
            "    }",
            "    {",
            "      JPanel panel_2 = new JPanel();",
            "      add(panel_2);",
            "    }",
            "  }",
            "}");
    container.refresh();
    // panels
    JPanelInfo panel_1 = container.getChildren(JPanelInfo.class).get(0);
    ContainerInfo wrappedComponent = panel_1.getChildren(ContainerInfo.class).get(0);
    JPanelInfo panel_2 = container.getChildren(JPanelInfo.class).get(1);
    // create memento
    JavaInfoMemento memento = JavaInfoMemento.createMemento(wrappedComponent);
    // restore from memento
    ComponentInfo newComponent = (ComponentInfo) memento.create(container);
    JavaInfoUtils.add(newComponent, null, panel_2, null);
    memento.apply();
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JPanel panel_1 = new JPanel();",
        "      add(panel_1);",
        "      TestWrapper wrapper = new TestWrapper(panel_1);",
        "      JButton button = wrapper.getControl();",
        "    }",
        "    {",
        "      JPanel panel_2 = new JPanel();",
        "      add(panel_2);",
        "      {",
        "        TestWrapper testWrapper = new TestWrapper(panel_2);",
        "        JButton button = testWrapper.getControl();",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Exposing
  //
  ////////////////////////////////////////////////////////////////////////////
  // XXX
  /**
   * Test for {@link JavaInfo#isRepresentedBy(ASTNode)}.
   * <p>
   * When container exposes viewer.
   * <p>
   * Problem is that wrapped component is child of container, so when we try to resolve
   * "container.getViewer()" and iterate over "container" children, and see that child is wrapped
   * component, we should check also children of "child" if one of them is "viewer".
   */
  public void test_exposed() throws Exception {
    // Viewer
    // Note, that it extends java.awt.Component to be exposable in Swing.
    setFileContentSrc(
        "test/MyViewer.java",
        getTestSource(
            "public class MyViewer extends java.awt.Component {",
            "  private JButton button;",
            "  public MyViewer(Container container) {",
            "    button = new JButton();",
            "    container.add(button);",
            "  }",
            "  public JButton getButton() {",
            "    return button;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyViewer.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <model class='" + WrapperMethodInfo.class.getName() + "'/>",
            "  <constructors>",
            "    <constructor>",
            "      <parameter type='java.awt.Container' parent='true'/>",
            "    </constructor>",
            "  </constructors>",
            "  <parameters>",
            "    <parameter name='Wrapper.method'>getButton</parameter>",
            "  </parameters>",
            "</component>"));
    // MyPanel
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  private MyViewer viewer;",
            "  public MyPanel() {",
            "    viewer = new MyViewer(this);",
            "  }",
            "  public MyViewer getViewer() {",
            "    return viewer;",
            "  }",
            "}"));
    waitForAutoBuild();
    // contribute special {@link HierarchyProvider}
    TestUtils.addDynamicExtension(COMPONENTS_HIERARCHY_PROVIDERS_POINT_ID, // 
        "  <provider class='" + SwingViewer_HierarchyProvider.class.getName() + "'/>");
    //
    try {
      parseContainer(
          "public class Test extends JPanel {",
          "  public Test() {",
          "    MyPanel myPanel = new MyPanel();",
          "    add(myPanel);",
          "    myPanel.getViewer();",
          "  }",
          "}");
      assertHierarchy(
          "{this: javax.swing.JPanel} {this} {/add(myPanel)/}",
          "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
          "  {new: test.MyPanel} {local-unique: myPanel} {/new MyPanel()/ /add(myPanel)/ /myPanel.getViewer()/}",
          "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
          "    {viewer: public javax.swing.JButton test.MyViewer.getButton()} {viewer} {}",
          "      {method: public test.MyViewer test.MyPanel.getViewer()} {property} {/myPanel.getViewer()/}");
      refresh();
      // test viewer
      JavaInfo viewer = getJavaInfoByName("getViewer()");
      MethodInvocation invocation = getNode("getViewer()", MethodInvocation.class);
      assertSame(viewer, JavaInfoResolver.getJavaInfo(m_lastParseInfo, invocation));
      // can not delete exposed JButton, i.e. no changes
      {
        ComponentInfo button = (ComponentInfo) viewer.getParent();
        button.delete();
        assertHierarchy(
            "{this: javax.swing.JPanel} {this} {/add(myPanel)/}",
            "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
            "  {new: test.MyPanel} {local-unique: myPanel} {/new MyPanel()/ /add(myPanel)/}",
            "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
            "    {viewer: public javax.swing.JButton test.MyViewer.getButton()} {viewer} {}",
            "      {method: public test.MyViewer test.MyPanel.getViewer()} {property} {}");
      }
    } finally {
      TestUtils.removeDynamicExtension(COMPONENTS_HIERARCHY_PROVIDERS_POINT_ID);
    }
  }

  private static String COMPONENTS_HIERARCHY_PROVIDERS_POINT_ID =
      "org.eclipse.wb.core.componentsHierarchyProviders";

  /**
   * {@link HierarchyProvider} which supports exposing Swing based viewers (just like we do for
   * SWT).
   */
  public static final class SwingViewer_HierarchyProvider extends HierarchyProvider {
    @Override
    public Object getParentObject(Object object) throws Exception {
      if (ReflectionUtils.isSuccessorOf(object.getClass(), "test.MyViewer")) {
        Object control = ReflectionUtils.invokeMethod(object, "getButton()");
        return ReflectionUtils.invokeMethod(control, "getParent()");
      }
      return null;
    }

    @Override
    public void add(JavaInfo host, JavaInfo exposed) throws Exception {
      Class<?> componentClass = exposed.getDescription().getComponentClass();
      if (ReflectionUtils.isSuccessorOf(componentClass, "test.MyViewer")) {
        ((WrapperMethodInfo) exposed).getWrapper().configureHierarchy(host);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  public static void configureWrapperContents() throws Exception {
    setFileContentSrc(
        "test/TestWrapper.java",
        getSource(
            "package test;",
            "import java.awt.*;",
            "import javax.swing.*;",
            "public class TestWrapper {",
            "  JButton m_control;",
            "  public TestWrapper(Container parent){",
            "    m_control = new JButton();",
            "    m_control.setBounds(10, 10, 10, 10);",
            "    parent.add(m_control);",
            "  }",
            "  public TestWrapper(JButton button){",
            "    m_control = button;",
            "  }",
            "  public JButton getControl(){",
            "    return m_control;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/TestWrapper.wbp-component.xml",
        getSource(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <toolkit id='org.eclipse.wb.swing'/>",
            "  <model class='" + WrapperMethodInfo.class.getName() + "'/>",
            "  <creation>",
            "    <source><![CDATA[new test.TestWrapper(%parent%)]]></source>",
            "  </creation>",
            "  <constructors>",
            "    <constructor>",
            "      <parameter type='java.awt.Container' parent='true'/>",
            "    </constructor>",
            "    <constructor>",
            "      <parameter type='javax.swing.JButton' parent='true'>",
            "        <tag name='Wrapper.wrapped' value='true'/>",
            "      </parameter>",
            "    </constructor>",
            "  </constructors>",
            "  <parameters>",
            "    <parameter name='Wrapper.method'>getControl</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
  }
}
