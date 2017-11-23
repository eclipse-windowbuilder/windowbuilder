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
package org.eclipse.wb.tests.designer.core.model.util.generic;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.util.generic.CopyPropertyTopChildSupport;
import org.eclipse.wb.internal.core.utils.state.EditorWarning;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link CopyPropertyTopChildSupport}.
 * 
 * @author scheglov_ke
 */
public class CopyPropertyTopChildTest extends SwingModelTest {
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
  public void test_copyExisting() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public void addStack(String stackText, Component component) {",
            "    add(component);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <!-- METHODS -->",
            "  <methods>",
            "    <method name='addStack'>",
            "      <parameter type='java.lang.String'/>",
            "      <parameter type='java.awt.Component' child='true'/>",
            "    </method>",
            "  </methods>",
            "  <!-- PARAMETERS -->",
            "  <parameters>",
            "    <parameter name='copyChildPropertyTop from=Association/stackText to=StackText category=normal'/>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    addStack('My text', new JButton());",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // test StackText property
    Property property = button.getPropertyByTitle("StackText");
    assertNotNull(property);
    assertSame(PropertyCategory.NORMAL, property.getCategory());
    assertEquals("My text", property.getValue());
    // next time same Property should be returned
    assertSame(property, button.getPropertyByTitle("StackText"));
  }

  public void test_ignoreNotExisting() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends JPanel {",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <!-- PARAMETERS -->",
            "  <parameters>",
            "    <parameter name='copyChildPropertyTop from=noSuchProperty to=anyTitle'/>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    add(new JButton());",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // test "anyTitle" property
    Property property = button.getPropertyByTitle("anyTitle");
    assertNull(property);
  }

  public void test_noParameter_from() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends JPanel {",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='copyChildPropertyTop to=fooBar'/>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "// filler filler filler",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "  }",
        "}");
    // check warnings
    List<EditorWarning> warnings = m_lastState.getWarnings();
    assertThat(warnings).hasSize(1);
    assertThat(warnings.get(0).getMessage()).contains("'from'");
  }

  public void test_noParameter_to() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends JPanel {",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='copyChildPropertyTop from=fooBar'/>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "// filler filler filler",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "  }",
        "}");
    // check warnings
    List<EditorWarning> warnings = m_lastState.getWarnings();
    assertThat(warnings).hasSize(1);
    assertThat(warnings.get(0).getMessage()).contains("'to'");
  }
}