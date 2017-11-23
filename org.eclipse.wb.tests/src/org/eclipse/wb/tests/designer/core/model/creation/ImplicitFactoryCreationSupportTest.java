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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.JavaInfoUtils.HierarchyProvider;
import org.eclipse.wb.internal.core.model.clipboard.IClipboardCreationSupport;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.creation.factory.ImplicitFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Component;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Test for {@link ImplicitFactoryCreationSupport}.
 * 
 * @author scheglov_ke
 */
public class ImplicitFactoryCreationSupportTest extends SwingModelTest {
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
   * Factory method that accepts {@link String} as argument.
   */
  public void test_String_parse() throws Exception {
    ContainerInfo panel = prepare_String_parse();
    // check hierarchy
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(bar)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: test.MyBar} {local-unique: bar} {/new MyBar()/ /add(bar)/ /bar.addItem('Some text')/}",
        "    {implicit-factory} {local-unique: component} {/bar.addItem('Some text')/ /component.setEnabled(false)/}");
    ContainerInfo bar = (ContainerInfo) panel.getChildrenComponents().get(0);
    JavaInfo component = bar.getChildrenJava().get(0);
    // ImplicitFactoryCreationSupport
    ImplicitFactoryCreationSupport creationSupport =
        (ImplicitFactoryCreationSupport) component.getCreationSupport();
    assertEquals("bar.addItem(\"Some text\")", m_lastEditor.getSource(creationSupport.getNode()));
    assertEquals("implicit-factory", creationSupport.toString());
    assertTrue(creationSupport.canReorder());
    assertFalse(creationSupport.canReparent());
    {
      MethodDescription description = creationSupport.getDescription();
      assertEquals("addItem(java.lang.String)", description.getSignature());
    }
    // check properties
    {
      Property factoryProperty = component.getPropertyByTitle("Factory");
      Property[] factoryProperties = getSubProperties(factoryProperty);
      assertEquals(1, factoryProperties.length);
      {
        Property textProperty = factoryProperties[0];
        assertEquals("text", textProperty.getTitle());
        assertEquals("Some text", textProperty.getValue());
      }
    }
  }

  /**
   * Factory method that accepts {@link String} as argument.
   */
  public void test_String_clipboard() throws Exception {
    ContainerInfo panel = prepare_String_parse();
    ContainerInfo bar = (ContainerInfo) panel.getChildrenComponents().get(0);
    JavaInfo component = bar.getChildrenJava().get(0);
    // clipboard source
    assertClipboardSource(component, "%parent%.addItem(\"Some text\")");
    // add
    {
      JavaInfoMemento memento = JavaInfoMemento.createMemento(component);
      assertSerializabled(memento);
      //
      ComponentInfo newComponent = (ComponentInfo) memento.create(panel);
      JavaInfoUtils.add(newComponent, null, bar, null);
      memento.apply();
      assertEditor(
          "class Test extends JPanel {",
          "  Test() {",
          "    MyBar bar = new MyBar();",
          "    add(bar);",
          "    {",
          "      JComponent component = bar.addItem('Some text');",
          "      component.setEnabled(false);",
          "    }",
          "    {",
          "      JComponent component = bar.addItem('Some text');",
          "      component.setEnabled(false);",
          "    }",
          "  }",
          "}");
    }
  }

  private static void assertSerializabled(JavaInfoMemento memento) throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(baos));
    oos.writeObject(memento);
    oos.close();
  }

  private ContainerInfo prepare_String_parse() throws Exception {
    setFileContentSrc(
        "test/MyBar.java",
        getTestSource(
            "public class MyBar extends JPanel {",
            "  public JComponent addItem(String text) {",
            "    JButton button = new JButton(text);",
            "    add(button);",
            "    return button;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyBar.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='addItem'>",
            "      <parameter type='java.lang.String'/>",
            "      <tag name='implicitFactory' value='true'/>",
            "    </method>",
            "  </methods>",
            "  <parameters>",
            "    <parameter name='layout.has'>false</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    MyBar bar = new MyBar();",
            "    add(bar);",
            "    {",
            "      JComponent component = bar.addItem('Some text');",
            "      component.setEnabled(false);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    return panel;
  }

  /**
   * Factory method that accepts {@link Component} as argument. The {@link JavaInfo} of this
   * {@link Component} is added as child. This happens because of binary binding (we just see that
   * returned component object is parent of argument component object), see
   * {@link HierarchyProvider}.
   */
  public void test_Component_parse() throws Exception {
    setFileContentSrc(
        "test/MyBar.java",
        getTestSource(
            "public class MyBar extends JPanel {",
            "  public JComponent addItem(Component content) {",
            "    JPanel wrapper = new JPanel();",
            "    add(wrapper);",
            "    wrapper.add(content);",
            "    return wrapper;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyBar.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='addItem'>",
            "      <parameter type='java.awt.Component'/>",
            "      <tag name='implicitFactory' value='true'/>",
            "    </method>",
            "  </methods>",
            "  <parameters>",
            "    <parameter name='layout.has'>false</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "class Test extends JPanel {",
        "  Test() {",
        "    MyBar bar = new MyBar();",
        "    add(bar);",
        "    //",
        "    JButton button = new JButton('my JButton');",
        "    JComponent component = bar.addItem(button);",
        "    component.setEnabled(false);",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(bar)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: test.MyBar} {local-unique: bar} {/new MyBar()/ /add(bar)/ /bar.addItem(button)/}",
        "    {implicit-factory} {local-unique: component} {/bar.addItem(button)/ /component.setEnabled(false)/}",
        "      {new: javax.swing.JButton} {local-unique: button} {/new JButton('my JButton')/ /bar.addItem(button)/}");
    refresh();
    ComponentInfo component = getJavaInfoByName("component");
    ComponentInfo button = getJavaInfoByName("button");
    // check properties
    {
      Property contentProperty = PropertyUtils.getByPath(component, "Factory/content");
      assertNotNull(contentProperty);
      Property[] contentProperties = getSubProperties(contentProperty);
      assertThat(contentProperties).isEqualTo(button.getProperties());
    }
  }

  public void test_sameMethodAndParent_CREATE() throws Exception {
    setFileContentSrc(
        "test/MyBar.java",
        getTestSource(
            "public class MyBar extends JPanel {",
            "  public JButton addButton() {",
            "    JButton button = new JButton();",
            "    add(button);",
            "    return button;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyBar.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='addButton'>",
            "      <tag name='implicitFactory' value='true'/>",
            "    </method>",
            "  </methods>",
            "</component>"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "class Test extends JPanel {",
        "  Test() {",
        "    MyBar bar = new MyBar();",
        "    add(bar);",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(bar)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: test.MyBar} {local-unique: bar} {/new MyBar()/ /add(bar)/}",
        "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
    ContainerInfo bar = getJavaInfoByName("bar");
    // prepare CreationSupport
    ImplicitFactoryCreationSupport creationSupport;
    {
      String signature = "addButton()";
      String invocationSource = "addButton()";
      creationSupport = new ImplicitFactoryCreationSupport(signature, invocationSource);
    }
    // add "implicit" JButton
    ComponentInfo newButton =
        (ComponentInfo) JavaInfoUtils.createJavaInfo(
            m_lastEditor,
            "javax.swing.JButton",
            creationSupport);
    ((FlowLayoutInfo) bar.getLayout()).add(newButton, null);
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    MyBar bar = new MyBar();",
        "    add(bar);",
        "    {",
        "      JButton button = bar.addButton();",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(bar)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: test.MyBar} {local-unique: bar} {/new MyBar()/ /add(bar)/ /bar.addButton()/}",
        "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "    {implicit-factory} {local-unique: button} {/bar.addButton()/}");
    // check MethodDescription
    {
      MethodDescription description = creationSupport.getDescription();
      assertNotNull(description);
      assertEquals("addButton()", description.toString());
    }
    // ...and check properties
    {
      Property[] properties = newButton.getProperties();
      assertThat(properties.length).isGreaterThan(10);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Separate factory declaration and parent
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link JavaInfo} which method is implicit factory is not always parent, in JFace Dialog parent
   * is passed separately.
   */
  public void test_separateMethodAndParent_parse() throws Exception {
    prepare_separateMethodAndParent();
    ContainerInfo panel =
        parseContainer(
            "class Test extends MyDialog {",
            "  Test() {",
            "    JPanel bar = new JPanel();",
            "    add(bar);",
            "    //",
            "    addButton(bar);",
            "  }",
            "}");
    assertHierarchy(
        "{this: test.MyDialog} {this} {/add(bar)/ /addButton(bar)/}",
        "  {new: javax.swing.JPanel} {local-unique: bar} {/new JPanel()/ /add(bar)/ /addButton(bar)/}",
        "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "    {implicit-factory} {empty} {/addButton(bar)/}");
    ContainerInfo bar = (ContainerInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button = bar.getChildrenComponents().get(0);
    ImplicitFactoryCreationSupport creationSupport =
        (ImplicitFactoryCreationSupport) button.getCreationSupport();
    // check MethodDescription
    {
      MethodDescription description = creationSupport.getDescription();
      assertNotNull(description);
      assertEquals("addButton({java.awt.Container,parent})", description.toString());
    }
    // no copy/paste (at least now we don't support this)
    {
      IClipboardCreationSupport clipboard = creationSupport.getClipboard();
      assertNull(clipboard);
    }
  }

  /**
   * We should be able to add new components with such separate declaration/parent.
   */
  public void test_separateMethodAndParent_CREATE() throws Exception {
    prepare_separateMethodAndParent();
    ContainerInfo panel =
        parseContainer(
            "class Test extends MyDialog {",
            "  Test() {",
            "    JPanel bar = new JPanel();",
            "    add(bar);",
            "  }",
            "}");
    panel.refresh();
    ContainerInfo bar = (ContainerInfo) panel.getChildrenComponents().get(0);
    // prepare CreationSupport
    ImplicitFactoryCreationSupport creationSupport;
    {
      String signature = "addButton(java.awt.Container)";
      String invocationSource = TemplateUtils.format("addButton({0})", bar);
      creationSupport = new ImplicitFactoryCreationSupport(panel, signature, invocationSource);
    }
    // add "implicit" JButton
    ComponentInfo newButton =
        (ComponentInfo) JavaInfoUtils.createJavaInfo(
            m_lastEditor,
            "javax.swing.JButton",
            creationSupport);
    ((FlowLayoutInfo) bar.getLayout()).add(newButton, null);
    // check source/hierarchy
    assertEditor(
        "class Test extends MyDialog {",
        "  Test() {",
        "    JPanel bar = new JPanel();",
        "    add(bar);",
        "    {",
        "      JButton button = addButton(bar);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: test.MyDialog} {this} {/add(bar)/}",
        "  {new: javax.swing.JPanel} {local-unique: bar} {/new JPanel()/ /add(bar)/ /addButton(bar)/}",
        "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "    {implicit-factory} {local-unique: button} {/addButton(bar)/}");
    // check MethodDescription
    {
      MethodDescription description = creationSupport.getDescription();
      assertNotNull(description);
      assertEquals("addButton({java.awt.Container,parent})", description.toString());
    }
    // ...and check properties
    {
      Property[] properties = newButton.getProperties();
      assertThat(properties.length).isGreaterThan(10);
    }
  }

  private void prepare_separateMethodAndParent() throws Exception {
    setFileContentSrc(
        "test/MyDialog.java",
        getTestSource(
            "public class MyDialog extends JPanel {",
            "  public JButton addButton(Container parent) {",
            "    JButton button = new JButton();",
            "    parent.add(button);",
            "    return button;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyDialog.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='addButton'>",
            "      <parameter type='java.awt.Container' parent='true'/>",
            "      <tag name='implicitFactory' value='true'/>",
            "    </method>",
            "  </methods>",
            "  <parameters>",
            "    <parameter name='layout.has'>false</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Other features
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Factory method can have arguments that are bound to some {@link Property} of created
   * {@link JavaInfo}.
   */
  public void test_argumentToProperty() throws Exception {
    setFileContentSrc(
        "test/MyBar.java",
        getTestSource(
            "public class MyBar extends JPanel {",
            "  public JButton addButton(String text) {",
            "    JButton button = new JButton(text);",
            "    add(button);",
            "    return button;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyBar.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='addButton'>",
            "      <parameter type='java.lang.String' name='text' property='setText(java.lang.String)'/>",
            "      <tag name='implicitFactory' value='true'/>",
            "    </method>",
            "  </methods>",
            "  <parameters>",
            "    <parameter name='layout.has'>false</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    MyBar bar = new MyBar();",
            "    add(bar);",
            "    //",
            "    JButton button = bar.addButton('Some text');",
            "    button.setEnabled(false);",
            "  }",
            "}");
    // check hierarchy
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(bar)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: test.MyBar} {local-unique: bar} {/new MyBar()/ /add(bar)/ /bar.addButton('Some text')/}",
        "    {implicit-factory} {local-unique: button} {/bar.addButton('Some text')/ /button.setEnabled(false)/}");
    ContainerInfo bar = (ContainerInfo) panel.getChildrenComponents().get(0);
    JavaInfo button = bar.getChildrenJava().get(0);
    // check "text" property
    Property textProperty = button.getPropertyByTitle("text");
    assertEquals("Some text", textProperty.getValue());
    textProperty.setValue("New text");
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    MyBar bar = new MyBar();",
        "    add(bar);",
        "    //",
        "    JButton button = bar.addButton('New text');",
        "    button.setEnabled(false);",
        "  }",
        "}");
  }

  /**
   * Materialize variable when set property.
   */
  public void test_materializeVariable() throws Exception {
    setFileContentSrc(
        "test/MyBar.java",
        getTestSource(
            "public class MyBar extends JPanel {",
            "  public JButton addButton() {",
            "    JButton button = new JButton();",
            "    add(button);",
            "    return button;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyBar.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='addButton'>",
            "      <tag name='implicitFactory' value='true'/>",
            "    </method>",
            "  </methods>",
            "  <parameters>",
            "    <parameter name='layout.has'>false</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    MyBar bar = new MyBar();",
            "    add(bar);",
            "    {",
            "      bar.addButton();",
            "    }",
            "  }",
            "}");
    ContainerInfo bar = (ContainerInfo) panel.getChildrenComponents().get(0);
    JavaInfo button = bar.getChildrenJava().get(0);
    // check "text" property
    Property textProperty = button.getPropertyByTitle("text");
    textProperty.setValue("New text");
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    MyBar bar = new MyBar();",
        "    add(bar);",
        "    {",
        "      JButton button = bar.addButton();",
        "      button.setText('New text');",
        "    }",
        "  }",
        "}");
  }
}
