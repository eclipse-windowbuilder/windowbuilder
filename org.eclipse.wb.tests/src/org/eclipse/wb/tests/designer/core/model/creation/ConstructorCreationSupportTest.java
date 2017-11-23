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
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.ConstructorParentAssociation;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ConstructorDescription;
import org.eclipse.wb.internal.core.model.description.CreationDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.ObjectPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.StaticFieldPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.style.StylePropertyEditor;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.ICoreExceptionConstants;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jface.preference.FieldEditor;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang.ArrayUtils;

import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Test for {@link ConstructorCreationSupport}.
 * 
 * @author scheglov_ke
 */
public class ConstructorCreationSupportTest extends SwingModelTest {
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
   * Test for access methods of {@link ConstructorCreationSupport}.
   */
  public void test_access() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test {",
            "  public static void main(String[] args) {",
            "    JPanel panel = new JPanel();",
            "  }",
            "}");
    ConstructorCreationSupport creationSupport =
        (ConstructorCreationSupport) panel.getCreationSupport();
    // check node
    assertSame(creationSupport.getCreation(), creationSupport.getNode());
    assertEquals("new JPanel()", m_lastEditor.getSource(creationSupport.getCreation()));
    assertEquals("new: javax.swing.JPanel", creationSupport.toString());
    // check for IMethodBinding
    {
      IMethodBinding binding = creationSupport.getBinding();
      assertEquals("<init>()", AstNodeUtils.getMethodSignature(binding));
    }
    // check ConstructorDescription
    {
      ConstructorDescription description = creationSupport.getDescription();
      assertNotNull(description);
      assertEquals(0, description.getParameters().size());
    }
    // operations validation
    assertTrue(creationSupport.canReorder());
    assertTrue(creationSupport.canReparent());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_delete_simpleComponent() throws Exception {
    parseContainer(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JButton button = new JButton();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    ComponentInfo button = getJavaInfoByName("button");
    // delete
    assertTrue(button.getAssociation().canDelete());
    assertTrue(button.getCreationSupport().canDelete());
    assertTrue(button.canDelete());
    button.delete();
    assertEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
  }

  public void test_delete_rootComponent() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test {",
            "  public static void main(String[] args) {",
            "    JPanel rootPanel = new JPanel();",
            "    rootPanel.setBackground(Color.green);",
            "    {",
            "      JButton button = new JButton();",
            "      rootPanel.add(button);",
            "    }",
            "  }",
            "}");
    assertThat(panel.getCreationSupport()).isInstanceOf(ConstructorCreationSupport.class);
    // delete
    assertTrue(panel.getAssociation().canDelete());
    assertTrue(panel.getCreationSupport().canDelete());
    assertTrue(panel.canDelete());
    panel.delete();
    assertEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test {",
        "  public static void main(String[] args) {",
        "    JPanel rootPanel = new JPanel();",
        "  }",
        "}");
    // no problem with properties
    panel.getProperties();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for "Constructor" complex property.
   */
  public void test_properties_good() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JLabel label = new JLabel('111', SwingConstants.RIGHT);",
            "    add(label);",
            "  }",
            "}");
    ComponentInfo label = panel.getChildrenComponents().get(0);
    assertEquals(6, label.getDescription().getConstructors().size());
    //
    Property constructorProperty = label.getPropertyByTitle("Constructor");
    assertNotNull(constructorProperty);
    //
    Property[] subProperties = getSubProperties(constructorProperty);
    assertEquals(2, subProperties.length);
    assertEquals("text", subProperties[0].getTitle());
    // check alignment sub-property
    {
      Property alignmentProperty = subProperties[1];
      assertEquals("horizontalAlignment", alignmentProperty.getTitle());
      assertInstanceOf(StaticFieldPropertyEditor.class, alignmentProperty.getEditor());
    }
  }

  /**
   * Test for "Constructor" complex property.
   * <p>
   * We show property for parent parameter.
   */
  public void test_properties_parentProperty() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends JButton {",
            "  public MyButton(Container parent, int value) {",
            "    parent.add(this);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <constructors>",
            "    <constructor>",
            "      <parameter type='java.awt.Container' parent='true'/>",
            "      <parameter type='int'/>",
            "    </constructor>",
            "  </constructors>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    new MyButton(this, 123);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // check properties
    assertNotNull(PropertyUtils.getByPath(button, "Constructor"));
    assertNotNull(PropertyUtils.getByPath(button, "Constructor/value"));
    {
      Property parentProperty = PropertyUtils.getByPath(button, "Constructor/parent");
      assertNotNull(parentProperty);
      Property[] parentProperties = getSubProperties(parentProperty);
      assertThat(parentProperties).isEqualTo(panel.getProperties());
    }
  }

  /**
   * Test for "Constructor" complex property.
   * <p>
   * We show property for parent parameter.
   * <p>
   * But sometimes this causes bad effects, so we want to disable this.
   */
  public void test_properties_parentProperty_disable() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends JButton {",
            "  public MyButton(Container parent, int value) {",
            "    parent.add(this);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <constructors>",
            "    <constructor>",
            "      <parameter type='java.awt.Container' parent='true'>",
            "        <tag name='property.no' value='true'/>",
            "      </parameter>",
            "      <parameter type='int'/>",
            "    </constructor>",
            "  </constructors>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    new MyButton(this, 123);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // check properties
    assertNotNull(PropertyUtils.getByPath(button, "Constructor"));
    assertNotNull(PropertyUtils.getByPath(button, "Constructor/value"));
    assertNull(PropertyUtils.getByPath(button, "Constructor/parent"));
  }

  /**
   * Test for "Constructor" complex property.
   * <p>
   * Constructor argument is some {@link JavaInfo}, but it is not bound to hierarchy, so we should
   * not show it. In reality attempt to do this causes {@link NullPointerException} because no
   * {@link Association}.
   */
  public void test_properties_notBoundJavaInfo() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends JButton {",
            "  public MyButton(Component someComponent) {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "class Test extends JPanel {",
        "  Test() {",
        "    JButton someComponent = new JButton();",
        "    add(new MyButton(someComponent));",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(new MyButton(someComponent))/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: test.MyButton} {empty} {/add(new MyButton(someComponent))/}");
  }

  /**
   * Test for "Constructor" complex property.
   * <p>
   * User asked to include also "Object" properties into "Constructor".
   */
  public void test_properties_objectProperty() throws Exception {
    setFileContentSrc(
        "test/MyComponent.java",
        getTestSource(
            "// filler filler filler filler filler",
            "public class MyComponent extends JButton {",
            "  public MyComponent(JButton button) {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "public class Test extends JPanel {",
        "  private JButton button = new JButton();",
        "  private MyComponent component = new MyComponent(button);",
        "  public Test() {",
        "    add(button);",
        "    add(component);",
        "  }",
        "}");
    ComponentInfo component = getJavaInfoByName("component");
    // check properties
    Property buttonProperty = PropertyUtils.getByPath(component, "Constructor/button");
    assertNotNull(buttonProperty);
    assertSame(ObjectPropertyEditor.INSTANCE, buttonProperty.getEditor());
  }

  /**
   * Test {@link ParameterDescription} {@link PropertyEditor}.
   */
  public void test_constructorParameterEditor() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new TextArea('', 10, 50, TextArea.SCROLLBARS_BOTH));",
            "  }",
            "}");
    ComponentInfo textArea = panel.getChildrenComponents().get(0);
    // check for "Constructor" complex property exists
    Property constructorProperty = textArea.getPropertyByTitle("Constructor");
    assertNotNull(constructorProperty);
    // check that there are 4 sub-properties
    Property[] subProperties = getSubProperties(constructorProperty);
    assertEquals(4, subProperties.length);
    assertEquals("text", subProperties[0].getTitle());
    assertEquals("rows", subProperties[1].getTitle());
    assertEquals("columns", subProperties[2].getTitle());
    assertEquals("scrollbars", subProperties[3].getTitle());
    // check that last property has static field editor
    StaticFieldPropertyEditor editor = (StaticFieldPropertyEditor) subProperties[3].getEditor();
    assertTrue(ArrayUtils.isEquals(new String[]{
        "SCROLLBARS_BOTH",
        "SCROLLBARS_VERTICAL_ONLY",
        "SCROLLBARS_HORIZONTAL_ONLY",
        "SCROLLBARS_NONE"}, getFieldValue(editor, "m_titles")));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CreationDescription
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that {@link CreationDescription} parameters are set during creation of new
   * {@link JavaInfo}. For example <code>variable.name</code> can be set to generate creation
   * specific variable name.
   * <p>
   * In GWT <code>ListBox</code> is generic widget that is used for lists itself, but also for Combo
   * widget, so would be nice to name Combo widgets as <code>"combo"</code>.
   */
  public void test_CreationDescription_parameters() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends JButton {",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <creation id='withParameters'>",
            "    <source><![CDATA[new test.MyButton()]]></source>",
            "    <parameter name='name_1'>value_1</parameter>",
            "    <parameter name='name_2'>value_2</parameter>",
            "  </creation>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    // create new MyButton
    JavaInfo myButton = createJavaInfo("test.MyButton", "withParameters");
    assertEquals("value_1", JavaInfoUtils.getParameter(myButton, "name_1"));
    assertEquals("value_2", JavaInfoUtils.getParameter(myButton, "name_2"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ConstructorCreationSupport#getClipboard()}.<br>
   * Normal Swing component.
   */
  public void test_clipboard_normalProperties() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new JButton('Some text', null));",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button = panel.getChildrenComponents().get(0);
    assertClipboardSource(button, "new javax.swing.JButton(\"Some text\", null)");
  }

  /**
   * Test for {@link ConstructorCreationSupport#getClipboard()}.<br>
   * Swing component with parent in constructor.
   */
  public void test_clipboard_parentAsArgument() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends JButton {",
            "  public MyButton(Container parent) {",
            "    parent.add(this);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <constructors>",
            "    <constructor>",
            "      <parameter type='java.awt.Container' parent='true'/>",
            "    </constructor>",
            "  </constructors>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    new MyButton(this);",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button = panel.getChildrenComponents().get(0);
    assertClipboardSource(button, "new test.MyButton(%parent%)");
  }

  /**
   * Test for {@link ConstructorCreationSupport#getClipboard()}.<br>
   * Swing component with some special pattern in constructor parameter.<br>
   * We use this for RCP {@link FieldEditor}'s.
   */
  public void test_clipboard_specialPatternUsingBroadcast() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends JButton {",
            "  public MyButton(Container someSpecial) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <constructors>",
            "    <constructor>",
            "      <parameter type='java.awt.Container'>",
            "        <tag name='myTag' value='generateSpecialPattern'/>",
            "      </parameter>",
            "    </constructor>",
            "  </constructors>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new MyButton(this));",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // set listener that will return %someSpecialPattern% for tagged parameter
    panel.addBroadcastListener(new JavaEventListener() {
      @Override
      public void clipboardCopy_Argument(JavaInfo javaInfo,
          ParameterDescription parameter,
          Expression argument,
          String[] source) throws Exception {
        if ("generateSpecialPattern".equals(parameter.getTag("myTag"))) {
          source[0] = "%someSpecialPattern%";
        }
      }
    });
    assertClipboardSource(button, "new test.MyButton(%someSpecialPattern%)");
  }

  /**
   * Test for {@link ConstructorCreationSupport#getClipboard()}.<br>
   * Use {@link StylePropertyEditor}
   */
  public void test_clipboard_styleProperty() throws Exception {
    setFileContentSrc(
        "test/Styles.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public interface Styles {",
            "  int NONE = 0;",
            "  int BORDER = 1;",
            "}"));
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends JButton {",
            "  public MyButton(int style) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <constructors>",
            "    <constructor>",
            "      <parameter type='int' defaultSource='test.Styles.NONE'>",
            "        <editor id='style'>",
            "          <parameter name='class'>test.Styles</parameter>",
            "          <parameter name='set'>BORDER</parameter>",
            "        </editor>",
            "      </parameter>",
            "    </constructor>",
            "  </constructors>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new MyButton(Styles.BORDER));",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // check with Styles.BORDER
    assertClipboardSource(button, "new test.MyButton(test.Styles.BORDER)");
    // set Styles.NONE
    {
      Property styleProperty = PropertyUtils.getByPath(button, "Constructor/style");
      ((GenericProperty) styleProperty).setExpression("test.Styles.NONE", Property.UNKNOWN_VALUE);
      assertClipboardSource(button, "new test.MyButton(test.Styles.NONE)");
    }
  }

  /**
   * {@link ConstructorCreationSupport} should include type arguments into clipboard source.
   */
  public void test_clipboard_typeArguments() throws Exception {
    // prepare generic MyButton
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton<T, S> extends JButton {",
            "  public MyButton() {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <creation>",
            "    <source><![CDATA[new test.MyButton<%T%>()]]></source>",
            "    <typeParameters>",
            "      <typeParameter name='T' type='java.lang.Object' title='arg T'/>",
            "      <typeParameter name='S' type='java.lang.Object' title='arg S'/>",
            "    </typeParameters>",
            "  </creation>",
            "</component>"));
    waitForAutoBuild();
    // parse
    final ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      MyButton<Double, String> button = new MyButton<Double, String>();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // do copy/paste
    {
      ComponentInfo button = getJavaInfoByName("button");
      doCopyPaste(button, new PasteProcedure<ComponentInfo>() {
        @Override
        public void run(ComponentInfo copy) throws Exception {
          ((FlowLayoutInfo) panel.getLayout()).add(copy, null);
        }
      });
      assertEditor(
          "public class Test extends JPanel {",
          "  public Test() {",
          "    {",
          "      MyButton<Double, String> button = new MyButton<Double, String>();",
          "      add(button);",
          "    }",
          "    {",
          "      MyButton<Double, String> myButton = new MyButton<Double, String>();",
          "      add(myButton);",
          "    }",
          "  }",
          "}");
    }
  }

  /**
   * {@link ConstructorCreationSupport} should support {@link AnonymousClassDeclaration} in
   * clipboard source.
   */
  public void test_clipboard_anonymousClassDeclaration() throws Exception {
    // prepare generic MyButton
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public abstract class MyButton extends JButton {",
            "  public MyButton() {",
            "  }",
            "  protected abstract String myStringMethod(int a, double b, String c);",
            "  protected abstract void myVoidMethod();",
            "}"));
    waitForAutoBuild();
    // parse
    final ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      MyButton button = new MyButton() {",
            "        protected String myStringMethod(int a, double b, String c) {",
            "          return 'foo';",
            "        }",
            "        protected void myVoidMethod() {",
            "        }",
            "      };",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // do copy/paste
    {
      ComponentInfo button = getJavaInfoByName("button");
      doCopyPaste(button, new PasteProcedure<ComponentInfo>() {
        @Override
        public void run(ComponentInfo copy) throws Exception {
          ((FlowLayoutInfo) panel.getLayout()).add(copy, null);
        }
      });
      assertEditor(
          "public class Test extends JPanel {",
          "  public Test() {",
          "    {",
          "      MyButton button = new MyButton() {",
          "        protected String myStringMethod(int a, double b, String c) {",
          "          return 'foo';",
          "        }",
          "        protected void myVoidMethod() {",
          "        }",
          "      };",
          "      add(button);",
          "    }",
          "    {",
          "      MyButton myButton = new MyButton() {",
          "        protected String myStringMethod(int a, double b, String c) {",
          "          return (String) null;",
          "        }",
          "        protected void myVoidMethod() {",
          "        }",
          "      };",
          "      add(myButton);",
          "    }",
          "  }",
          "}");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ConstructorCreationSupport#add_getSource(NodeTarget)} and
   * {@link ConstructorCreationSupport#ConstructorCreationSupport(String, boolean)}.
   */
  public void test_getSource_forCreationId() throws Exception {
    setJavaContentSrc("test", "MyButton", new String[]{
        "public class MyButton extends JButton {",
        "  public MyButton(boolean b) {",
        "  }",
        "}"}, new String[]{
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
        "  <creation>",
        "    <source><![CDATA[new test.MyButton(false)]]></source>",
        "  </creation>",
        "  <creation id='true'>",
        "    <source><![CDATA[new test.MyButton(true)]]></source>",
        "  </creation>",
        "</component>"});
    String[] lines = {"public class Test extends JPanel {", "  public Test() {", "  }", "}"};
    // parse
    parseContainer(lines);
    Class<?> clazz = m_lastLoader.loadClass("test.MyButton");
    // default
    {
      CreationSupport creationSupport = new ConstructorCreationSupport();
      JavaInfoUtils.createJavaInfo(m_lastEditor, clazz, creationSupport);
      assertEquals("new test.MyButton(false)", creationSupport.add_getSource(null));
    }
    // "true"
    {
      CreationSupport creationSupport = new ConstructorCreationSupport("true", false);
      JavaInfoUtils.createJavaInfo(m_lastEditor, clazz, creationSupport);
      assertEquals("new test.MyButton(true)", creationSupport.add_getSource(null));
    }
  }

  /**
   * Test for {@link ConstructorCreationSupport#add_getSource(NodeTarget)} and
   * {@link ConstructorCreationSupport#forSource(String)}.
   */
  public void test_getSource_forSource() throws Exception {
    setJavaContentSrc("test", "MyButton", new String[]{
        "public class MyButton extends JButton {",
        "  public MyButton(boolean b) {",
        "  }",
        "}"}, null);
    String[] lines = {"public class Test extends JPanel {", "  public Test() {", "  }", "}"};
    // parse
    parseContainer(lines);
    Class<?> clazz = m_lastLoader.loadClass("test.MyButton");
    //
    CreationSupport creationSupport =
        ConstructorCreationSupport.forSource("new test.MyButton(1 == 1)");
    JavaInfoUtils.createJavaInfo(m_lastEditor, clazz, creationSupport);
    assertEquals("new test.MyButton(1 == 1)", creationSupport.add_getSource(null));
  }

  public void test_CREATE_noInvocations() throws Exception {
    setJavaContentSrc("test", "MyButton", new String[]{
        "public class MyButton extends JButton {",
        "  public MyButton() {",
        "  }",
        "}"}, new String[]{
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
        "  <creation>",
        "    <source><![CDATA[new test.MyButton()]]></source>",
        "    <invocation signature='setEnabled(boolean)'><![CDATA[false]]></invocation>",
        "  </creation>",
        "</component>"});
    String[] lines1 = {"public class Test extends JPanel {", "  public Test() {", "  }", "}"};
    // parse
    ContainerInfo panel = parseContainer(lines1);
    // add MyButton
    ConstructorCreationSupport creationSupport = new ConstructorCreationSupport(null, false);
    ComponentInfo newButton =
        (ComponentInfo) JavaInfoUtils.createJavaInfo(
            m_lastEditor,
            m_lastLoader.loadClass("test.MyButton"),
            creationSupport);
    ((FlowLayoutInfo) panel.getLayout()).add(newButton, null);
    String[] lines =
        {
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      MyButton myButton = new MyButton();",
            "      add(myButton);",
            "    }",
            "  }",
            "}"};
    assertEditor(lines);
    // check ConstructorCreationSupport state
    {
      {
        ClassInstanceCreation creation = creationSupport.getCreation();
        assertNotNull(creation);
        assertEquals("new MyButton()", m_lastEditor.getSource(creation));
      }
      assertNotNull(creationSupport.getBinding());
      {
        ConstructorDescription description = creationSupport.getDescription();
        assertNotNull(description);
        assertEquals("<init>()", description.getSignature());
      }
    }
  }

  public void test_CREATE_addInvocations() throws Exception {
    setJavaContentSrc("test", "MyButton", new String[]{
        "public class MyButton extends JButton {",
        "  public MyButton() {",
        "  }",
        "}"}, new String[]{
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
        "  <creation>",
        "    <source><![CDATA[new test.MyButton()]]></source>",
        "    <invocation signature='setEnabled(boolean)'><![CDATA[false]]></invocation>",
        "  </creation>",
        "</component>"});
    String[] lines1 = {"public class Test extends JPanel {", "  public Test() {", "  }", "}"};
    // parse
    ContainerInfo panel = parseContainer(lines1);
    // add MyButton
    ConstructorCreationSupport creationSupport = new ConstructorCreationSupport(null, true);
    ComponentInfo newButton =
        (ComponentInfo) JavaInfoUtils.createJavaInfo(
            m_lastEditor,
            m_lastLoader.loadClass("test.MyButton"),
            creationSupport);
    ((FlowLayoutInfo) panel.getLayout()).add(newButton, null);
    String[] lines =
        {
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      MyButton myButton = new MyButton();",
            "      add(myButton);",
            "      myButton.setEnabled(false);",
            "    }",
            "  }",
            "}"};
    assertEditor(lines);
  }

  public void test_template_index() throws Exception {
    setJavaContentSrc("test", "MyButton", new String[]{
        "public class MyButton extends JButton {",
        "  public MyButton(String value) {",
        "  }",
        "  public void setValue(String value) {",
        "  }",
        "}"}, new String[]{
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
        "  <creation>",
        "    <source><![CDATA[new test.MyButton(\"value_%index%\")]]></source>",
        "  </creation>",
        "</component>"});
    String[] lines1 = {"public class Test extends JPanel {", "  public Test() {", "  }", "}"};
    // parse
    ContainerInfo panel = parseContainer(lines1);
    // add MyButton
    ConstructorCreationSupport creationSupport = new ConstructorCreationSupport(null, true);
    ComponentInfo newButton =
        (ComponentInfo) JavaInfoUtils.createJavaInfo(
            m_lastEditor,
            m_lastLoader.loadClass("test.MyButton"),
            creationSupport);
    ((FlowLayoutInfo) panel.getLayout()).add(newButton, null);
    // check source
    assertEditor(new String[]{
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      MyButton myButton = new MyButton('value_2');",
        "      add(myButton);",
        "    }",
        "  }",
        "}"});
  }

  /**
   * {@link ITypeBinding} for creation exists, but references not existing constructor.
   */
  public void test_CREATE_badConstructur() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends JButton {",
            "  public MyButton() {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <creation>",
            "    <source><![CDATA[new test.MyButton(BAD)]]></source>",
            "  </creation>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    FlowLayoutInfo flowLayout = (FlowLayoutInfo) panel.getLayout();
    // try to add
    try {
      ComponentInfo newButton = createJavaInfo("test.MyButton");
      flowLayout.add(newButton, null);
      fail();
    } catch (DesignerException e) {
      assertEquals(ICoreExceptionConstants.GEN_NO_CONSTRUCTOR_BINDING, e.getCode());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getAssociation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ConstructorCreationSupport#getAssociation()}.
   */
  public void test_getAssociation_noParent() throws Exception {
    String[] lines = {"public class Test extends JPanel {", "  public Test() {", "  }", "}"};
    parseContainer(lines);
    // 
    Class<?> clazz = JButton.class;
    CreationSupport creationSupport = new ConstructorCreationSupport();
    JavaInfoUtils.createJavaInfo(m_lastEditor, clazz, creationSupport);
    assertThat(creationSupport.getAssociation()).isNull();
  }

  /**
   * Test for {@link ConstructorCreationSupport#getAssociation()}.
   */
  public void test_getAssociation_hasParent() throws Exception {
    setJavaContentSrc("test", "MyButton", new String[]{
        "public class MyButton extends JButton {",
        "  public MyButton(JPanel parent) {",
        "  }",
        "}"}, new String[]{
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
        "  <creation>",
        "    <source><![CDATA[new test.MyButton(%parent%)]]></source>",
        "  </creation>",
        "  <constructors>",
        "    <constructor>",
        "      <parameter type='javax.swing.JPanel' parent='true'/>",
        "    </constructor>",
        "  </constructors>",
        "</component>"});
    String[] lines = {"public class Test extends JPanel {", "  public Test() {", "  }", "}"};
    // parse
    parseContainer(lines);
    // 
    Class<?> clazz = m_lastLoader.loadClass("test.MyButton");
    CreationSupport creationSupport = new ConstructorCreationSupport();
    JavaInfoUtils.createJavaInfo(m_lastEditor, clazz, creationSupport);
    assertThat(creationSupport.getAssociation()).isInstanceOf(ConstructorParentAssociation.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ConstructorCreationSupport#canUseParent(JavaInfo)}.
   */
  public void test_canUseParent_MOVE_false() throws Exception {
    prepare_canUseParent_MyButton();
    ContainerInfo frame =
        parseContainer(
            "public class Test extends JFrame {",
            "  public Test() {",
            "    {",
            "      JPanel panel_1 = new JPanel();",
            "      getContentPane().add(panel_1);",
            "      panel_1.add(new MyButton(panel_1));",
            "    }",
            "  }",
            "}");
    ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
    ContainerInfo panel_1 = (ContainerInfo) contentPane.getChildrenComponents().get(0);
    ComponentInfo button = panel_1.getChildrenComponents().get(0);
    // "MyButton" requires JPanel, but we given JFrame
    {
      CreationSupport creationSupport = button.getCreationSupport();
      assertFalse(creationSupport.canUseParent(frame));
      assertFalse(creationSupport.canUseParent(frame));
    }
  }

  /**
   * Test for {@link ConstructorCreationSupport#canUseParent(JavaInfo)}.
   */
  public void test_canUseParent_MOVE_true() throws Exception {
    prepare_canUseParent_MyButton();
    ContainerInfo frame =
        parseContainer(
            "public class Test extends JFrame {",
            "  public Test() {",
            "    {",
            "      JPanel panel_1 = new JPanel();",
            "      getContentPane().add(panel_1);",
            "      panel_1.add(new MyButton(panel_1));",
            "    }",
            "    {",
            "      JPanel panel_2 = new JPanel();",
            "      getContentPane().add(panel_2);",
            "    }",
            "  }",
            "}");
    ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
    ContainerInfo panel_1 = (ContainerInfo) contentPane.getChildrenComponents().get(0);
    ContainerInfo panel_2 = (ContainerInfo) contentPane.getChildrenComponents().get(1);
    ComponentInfo button = panel_1.getChildrenComponents().get(0);
    // "MyButton" requires JPanel, we give it
    {
      CreationSupport creationSupport = button.getCreationSupport();
      assertTrue(creationSupport.canUseParent(panel_2));
      assertTrue(creationSupport.canUseParent(panel_2));
    }
  }

  /**
   * Test for {@link ConstructorCreationSupport#canUseParent(JavaInfo)}.
   */
  public void test_canUseParent_CREATE_false() throws Exception {
    prepare_canUseParent_MyButton();
    ContainerInfo frame =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JFrame {",
            "  public Test() {",
            "  }",
            "}");
    frame.refresh();
    // "MyButton" requires JPanel, but we try to use JFrame
    ComponentInfo button = createComponent("test.MyButton");
    CreationSupport creationSupport = button.getCreationSupport();
    assertFalse(creationSupport.canUseParent(frame));
    assertFalse(creationSupport.canUseParent(frame));
  }

  /**
   * Test for {@link ConstructorCreationSupport#canUseParent(JavaInfo)}.
   */
  public void test_canUseParent_CREATE_true() throws Exception {
    prepare_canUseParent_MyButton();
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    // "MyButton" requires JPanel, we give it
    ComponentInfo button = createComponent("test.MyButton");
    CreationSupport creationSupport = button.getCreationSupport();
    assertTrue(creationSupport.canUseParent(panel));
    assertTrue(creationSupport.canUseParent(panel));
  }

  /**
   * Test for {@link ConstructorCreationSupport#canUseParent(JavaInfo)}.
   */
  public void test_canUseParent_CREATE_noParent() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends JButton {",
            "  public MyButton(JPanel parentForButton) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <creation>",
            "    <source><![CDATA[new test.MyButton(%parentForButton%)]]></source>",
            "  </creation>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    // "MyButton" does not need %parent%, but needs "%parentForButtons%"
    // We don't know what means "%parentForButtons%", so allow.
    ComponentInfo button = createComponent("test.MyButton");
    CreationSupport creationSupport = button.getCreationSupport();
    assertTrue(creationSupport.canUseParent(panel));
    assertTrue(creationSupport.canUseParent(panel));
  }

  /**
   * Prepares "MyButton" that requires {@link JPanel} parent in constructor.
   */
  private void prepare_canUseParent_MyButton() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends JButton {",
            "  public MyButton(JPanel parent) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <creation>",
            "    <source><![CDATA[new test.MyButton(%parent%)]]></source>",
            "  </creation>",
            "  <constructors>",
            "    <constructor>",
            "      <parameter type='javax.swing.JPanel' parent='true'/>",
            "    </constructor>",
            "  </constructors>",
            "</component>"));
    waitForAutoBuild();
  }
}
