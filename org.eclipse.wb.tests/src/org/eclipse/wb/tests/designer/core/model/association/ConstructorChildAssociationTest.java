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
package org.eclipse.wb.tests.designer.core.model.association;

import org.eclipse.wb.core.model.association.ConstructorChildAssociation;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.accessor.ConstructorAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import java.util.List;

/**
 * Tests for {@link ConstructorChildAssociation}.
 *
 * @author scheglov_ke
 */
public class ConstructorChildAssociationTest extends SwingModelTest {
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
  public void test_parse() throws Exception {
    configureProject();
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    MyPanel myPanel = new MyPanel(button, true);",
            "    add(myPanel);",
            "  }",
            "}");
    ContainerInfo myPanel = (ContainerInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button = myPanel.getChildrenComponents().get(0);
    // check association
    ConstructorChildAssociation association = (ConstructorChildAssociation) button.getAssociation();
    assertSame(button, association.getJavaInfo());
    assertEquals("new MyPanel(button, true)", association.getSource());
    assertEquals("new MyPanel(button, true)", m_lastEditor.getSource(association.getCreation()));
    assertEquals(
        "MyPanel myPanel = new MyPanel(button, true);",
        m_lastEditor.getSource(association.getStatement()));
    // check constructor properties for "myPanel"
    {
      Property constructorProperty = myPanel.getPropertyByTitle("Constructor");
      GenericProperty valueProperty =
          (GenericProperty) getPropertyByTitle(getSubProperties(constructorProperty), "value");
      // check accessors
      {
        List<ExpressionAccessor> accessors = getGenericPropertyAccessors(valueProperty);
        assertEquals(1, accessors.size());
        assertInstanceOf(ConstructorAccessor.class, accessors.get(0));
      }
      // check value
      assertSame(Boolean.TRUE, valueProperty.getValue());
      // set new value
      valueProperty.setValue(Boolean.FALSE);
      assertEditor(
          "public class Test extends JPanel {",
          "  public Test() {",
          "    JButton button = new JButton();",
          "    MyPanel myPanel = new MyPanel(button, false);",
          "    add(myPanel);",
          "  }",
          "}");
    }
  }

  public void test_delete() throws Exception {
    configureProject();
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    MyPanel myPanel = new MyPanel(button, true);",
            "    add(myPanel);",
            "  }",
            "}");
    ContainerInfo myPanel = (ContainerInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button = myPanel.getChildrenComponents().get(0);
    // do delete
    assertTrue(button.canDelete());
    button.delete();
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    MyPanel myPanel = new MyPanel(true);",
        "    add(myPanel);",
        "  }",
        "}");
    // check constructor properties for "myPanel"
    {
      Property constructorProperty = myPanel.getPropertyByTitle("Constructor");
      GenericProperty valueProperty =
          (GenericProperty) getPropertyByTitle(getSubProperties(constructorProperty), "value");
      // check accessors
      {
        List<ExpressionAccessor> accessors = getGenericPropertyAccessors(valueProperty);
        assertEquals(1, accessors.size());
        assertInstanceOf(ConstructorAccessor.class, accessors.get(0));
      }
      // check value
      assertSame(Boolean.TRUE, valueProperty.getValue());
      // set new value
      valueProperty.setValue(Boolean.FALSE);
      assertEditor(
          "public class Test extends JPanel {",
          "  public Test() {",
          "    MyPanel myPanel = new MyPanel(false);",
          "    add(myPanel);",
          "  }",
          "}");
    }
  }

  /**
   * Can not delete because only one constructor in parent, so it can not be created without child.
   */
  public void test_delete_noDelete() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public MyPanel(Component component, boolean value) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <!-- CONSTRUCTORS -->",
            "  <constructors>",
            "    <constructor>",
            "      <parameter type='java.awt.Component' child='true'/>",
            "      <parameter type='boolean'/>",
            "    </constructor>",
            "  </constructors>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    MyPanel myPanel = new MyPanel(button, true);",
            "    add(myPanel);",
            "  }",
            "}");
    ContainerInfo myPanel = (ContainerInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button = myPanel.getChildrenComponents().get(0);
    // can not delete "button"
    assertFalse(button.getAssociation().canDelete());
    assertFalse(button.canDelete());
    // but "myPanel" itself can be deleted
    assertTrue(myPanel.canDelete());
    myPanel.delete();
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
  }

  /**
   * Can not delete because only one constructor in parent, so it can not be created without child.
   */
  public void DISABLE_test_delete_noDelete_withGenerics() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel<T extends Component> extends JPanel {",
            "  public MyPanel(T component, boolean value) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <!-- CONSTRUCTORS -->",
            "  <constructors>",
            "    <constructor>",
            "      <parameter type='java.awt.Component' child='true'/>",
            "      <parameter type='boolean'/>",
            "    </constructor>",
            "  </constructors>",
            "</component>"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JButton button = new JButton();",
        "    MyPanel myPanel = new MyPanel(button, true);",
        "    add(myPanel);",
        "  }",
        "}");
    refresh();
    ContainerInfo myPanel = getJavaInfoByName("myPanel");
    ComponentInfo button = getJavaInfoByName("button");
    // can not delete "button"
    assertFalse(button.getAssociation().canDelete());
    assertFalse(button.canDelete());
    // but "myPanel" itself can be deleted
    assertTrue(myPanel.canDelete());
    myPanel.delete();
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private void configureProject() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public MyPanel(boolean value) {",
            "  }",
            "  public MyPanel(Component component, boolean value) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <!-- CONSTRUCTORS -->",
            "  <constructors>",
            "    <constructor>",
            "      <parameter type='boolean'/>",
            "    </constructor>",
            "    <constructor>",
            "      <parameter type='java.awt.Component' child='true'/>",
            "      <parameter type='boolean'/>",
            "    </constructor>",
            "  </constructors>",
            "</component>"));
    waitForAutoBuild();
  }
}
