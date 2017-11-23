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
package org.eclipse.wb.tests.designer.core.model.property.editor;

import com.google.common.collect.Maps;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.InstanceListPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.complex.InstanceObjectPropertyEditor;
import org.eclipse.wb.internal.core.utils.check.AssertionFailedException;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;
import org.eclipse.wb.tests.gef.UIPredicate;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

/**
 * Test for {@link InstanceListPropertyEditor}.
 * 
 * @author sablin_aa
 */
public class InstanceObjectPropertyEditorTest extends SwingModelTest {
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
   * Test editor parameters validation.
   */
  public void test_configure() throws Exception {
    parseContainer(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    Map<String, Object> parameters = getEditorParameters();
    {
      // full configuration
      /*InstanceObjectPropertyEditor editor = */createEditor(parameters);
    }
    {
      // valid (no source) configuration
      parameters.remove("source");
      /*InstanceObjectPropertyEditor editor = */createEditor(parameters);
    }
    {
      // wrong (no class) configuration
      parameters.remove("class");
      parameters.put("source", "new javax.swing.AbstractButton() {}");
      try {
        /*InstanceObjectPropertyEditor editor = */createEditor(parameters);
        fail();
      } catch (AssertionFailedException e) {
        if (!e.getMessage().contains("'class'")) {
          throw e;
        }
      }
    }
  }

  /**
   * Test creating property & editor with empty value.
   */
  public void test_parse_noValue() throws Exception {
    configureContents();
    ContainerInfo container =
        parseContainer(
            "// filler filler filler",
            "public class Test extends TestPanel {",
            "  public Test() {",
            "  }",
            "}");
    // property
    Property property = container.getPropertyByTitle("property");
    assertThat(property).isNotNull();
    assertThat(getPropertyText(property)).isNull();
    assertThat(property.getEditor()).isInstanceOf(InstanceObjectPropertyEditor.class);
    //editor
    InstanceObjectPropertyEditor editor = (InstanceObjectPropertyEditor) property.getEditor();
    assertThat(editor.getInstanceExpression(property)).isNull();
    assertThat(editor.getProperties(property)).isEmpty();
  }

  /**
   * Test creating property & editor with not empty value.
   */
  public void test_parse_withValue() throws Exception {
    configureContents();
    ContainerInfo container =
        parseContainer(
            "public class Test extends TestPanel {",
            "  public Test() {",
            "    setProperty(new JButton());",
            "  }",
            "}");
    // property instance info
    assertThat(container.getChildrenComponents()).hasSize(1);
    ComponentInfo childInfo = container.getChildrenComponents().get(0);
    // property
    Property property = container.getPropertyByTitle("property");
    assertThat(property).isNotNull();
    assertThat(getPropertyText(property)).isEqualTo("javax.swing.JButton");
    assertThat(property.getEditor()).isInstanceOf(InstanceObjectPropertyEditor.class);
    //editor
    InstanceObjectPropertyEditor editor = (InstanceObjectPropertyEditor) property.getEditor();
    assertThat(editor.getInstanceExpression(property)).isNotNull();
    assertThat(editor.getProperties(property)).isNotEmpty();
    // property info
    JavaInfo propertyInfo = editor.getInstanceInfo(property);
    assertThat(propertyInfo).isNotNull();
    assertThat(propertyInfo).isSameAs(childInfo);
  }

  /**
   * Test setting property value using dialog.
   */
  public void test_dialog() throws Exception {
    configureContents();
    ContainerInfo container =
        parseContainer(
            "// filler filler filler",
            "public class Test extends TestPanel {",
            "  public Test() {",
            "  }",
            "}");
    // property instance info
    assertThat(container.getChildrenComponents()).hasSize(0);
    // property
    final Property property = container.getPropertyByTitle("property");
    final InstanceObjectPropertyEditor editor = (InstanceObjectPropertyEditor) property.getEditor();
    // use GUI to set "ExternalLabelProvider"
    {
      // open dialog and animate it
      new UiContext().executeAndCheck(new UIRunnable() {
        public void run(UiContext context) throws Exception {
          openPropertyDialog(property);
        }
      }, new UIRunnable() {
        public void run(UiContext context) throws Exception {
          // set filter
          {
            context.useShell("Open type");
            Text filterText = context.findFirstWidget(Text.class);
            filterText.setText("JButton");
          }
          // wait for types
          {
            final Table typesTable = context.findFirstWidget(Table.class);
            context.waitFor(new UIPredicate() {
              public boolean check() {
                return typesTable.getItems().length != 0;
              }
            });
          }
          // click OK
          context.clickButton("OK");
        }
      });
      // check source
      assertEditor(
          "// filler filler filler",
          "public class Test extends TestPanel {",
          "  public Test() {",
          "    setProperty(new JButton('New button'));",
          "  }",
          "}");
      assertEquals("javax.swing.JButton", getPropertyText(property));
      assertThat(container.getChildrenComponents()).hasSize(1);
      assertThat(container.getChildrenComponents().get(0)).isSameAs(
          editor.getInstanceInfo(property));
    }
  }

  /**
   * Test setting property value using double click and 'source' as template for anonymous class
   * instance.
   */
  public void test_doubleClick() throws Exception {
    configureContents();
    ContainerInfo container =
        parseContainer(
            "// filler filler filler",
            "public class Test extends TestPanel {",
            "  public Test() {",
            "  }",
            "}");
    // property instance info
    assertThat(container.getChildrenComponents()).hasSize(0);
    // property
    Property property = container.getPropertyByTitle("property");
    //editor
    InstanceObjectPropertyEditor editor = (InstanceObjectPropertyEditor) property.getEditor();
    // kick "doubleClick"
    editor.doubleClick(property, null);
    assertEditor(
        "// filler filler filler",
        "public class Test extends TestPanel {",
        "  public Test() {",
        "    setProperty(",
        "            new AbstractButton() {",
        "            }",
        "            );",
        "  }",
        "}");
    assertEquals("<anonymous>", getPropertyText(property));
    assertThat(container.getChildrenComponents()).isEmpty();
  }

  /**
   * Test property "Restore default value".
   */
  public void test_restore_default() throws Exception {
    configureContents();
    ContainerInfo container =
        parseContainer(
            "// filler filler filler",
            "public class Test extends TestPanel {",
            "  public Test() {",
            "    setProperty(new JButton());",
            "  }",
            "}");
    // property instance info
    assertThat(container.getChildrenComponents()).hasSize(1);
    // property
    Property property = container.getPropertyByTitle("property");
    //editor
    InstanceObjectPropertyEditor editor = (InstanceObjectPropertyEditor) property.getEditor();
    JavaInfo instanceInfo = editor.getInstanceInfo(property);
    assertThat(instanceInfo).isSameAs(container.getChildrenComponents().get(0));
    // manual set listener for property
    InstanceObjectPropertyEditor.installListenerForProperty(instanceInfo);
    // set to default
    property.setValue(Property.UNKNOWN_VALUE);
    // check source
    assertEditor(
        "// filler filler filler",
        "public class Test extends TestPanel {",
        "  public Test() {",
        "  }",
        "}");
    assertThat(container.getChildrenComponents()).isEmpty();
  }

  /**
   * Test for sub properties
   */
  public void test_sub_properties() throws Exception {
    configureContents();
    ContainerInfo container =
        parseContainer(
            "public class Test extends TestPanel {",
            "  public Test() {",
            "    setProperty(new JButton());",
            "  }",
            "}");
    // property
    Property property = container.getPropertyByTitle("property");
    //editor
    InstanceObjectPropertyEditor editor = (InstanceObjectPropertyEditor) property.getEditor();
    JavaInfo instanceInfo = editor.getInstanceInfo(property);
    // sub property
    Property subProperty = getPropertyByTitle(editor.getProperties(property), "text");
    assertThat(subProperty).isSameAs(instanceInfo.getPropertyByTitle("text"));
    subProperty.setValue("value");
    // check source
    assertEditor(
        "public class Test extends TestPanel {",
        "  public Test() {",
        "    JButton button = new JButton();",
        "    button.setText('value');",
        "    setProperty(button);",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private Map<String, Object> getEditorParameters() {
    //<editor id="instanceObject">
    //	<parameter name="class"></parameter-list>
    //	<parameter name="source">
    //    new javax.swing.AbstractButton() {
    //    }
    //  </parameter>
    //</editor>
    HashMap<String, Object> params = Maps.newHashMap();
    params.put("class", "");
    params.put("source", getSourceDQ("new javax.swing.AbstractButton() {", "}"));
    return params;
  }

  protected InstanceObjectPropertyEditor createEditor(Map<String, Object> parameters)
      throws Exception {
    InstanceObjectPropertyEditor editor = new InstanceObjectPropertyEditor();
    editor.configure(m_lastState, parameters);
    return editor;
  }

  private void configureContents() throws Exception {
    setJavaContentSrc("test", "TestPanel", new String[]{
        "public class TestPanel extends JPanel {",
        "  public TestPanel(){",
        "  }",
        "  public void setProperty(AbstractButton value){",
        "  }",
        "}"}, new String[]{
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
        "  <methods>",
        "    <method name='setProperty'>",
        "      <parameter type='javax.swing.AbstractButton' child='true'/>",
        "    </method>",
        "  </methods>",
        "  <property id='setProperty(javax.swing.AbstractButton)'>",
        "    <editor id='instanceObject'>",
        "      <parameter name='class'>javax.swing.AbstractButton</parameter>",
        "      <parameter name='source'><![CDATA[",
        "        new javax.swing.AbstractButton() {",
        "        }",
        "        ]]></parameter>",
        "    </editor>",
        "  </property>",
        "</component>"});
    waitForAutoBuild();
  }
}
