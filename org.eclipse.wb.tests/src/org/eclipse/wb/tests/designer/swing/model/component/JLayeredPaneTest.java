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
package org.eclipse.wb.tests.designer.swing.model.component;

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.component.JLayeredPaneInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Container;
import java.util.List;

import javax.swing.JLayeredPane;

/**
 * Test for {@link JLayeredPaneInfo}.
 * 
 * @author scheglov_ke
 */
public class JLayeredPaneTest extends SwingModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for "Layer" property.<br>
   * Value is in constraints of {@link Container#add(java.awt.Component, Object)} method.
   */
  public void test_layer_Constraints() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JLayeredPane pane = new JLayeredPane();",
            "    add(pane);",
            "    {",
            "      JButton button = new JButton();",
            "      pane.add(button, new Integer(10));",
            "    }",
            "  }",
            "}");
    panel.refresh();
    JLayeredPaneInfo pane = (JLayeredPaneInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button = pane.getChildrenComponents().get(0);
    // check value from JLayeredPane (to check that it was executed)
    assertEquals(10, ((JLayeredPane) pane.getObject()).getLayer(button.getComponent()));
    // check "Layer" property
    Property layerProperty = button.getPropertyByTitle("Layer");
    assertNotNull(layerProperty);
    assertTrue(layerProperty.isModified());
    assertEquals(10, layerProperty.getValue());
    // update "Layer" property
    layerProperty.setValue(20);
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    JLayeredPane pane = new JLayeredPane();",
        "    add(pane);",
        "    {",
        "      JButton button = new JButton();",
        "      pane.setLayer(button, 20);",
        "      pane.add(button);",
        "    }",
        "  }",
        "}");
    assertEquals(20, layerProperty.getValue());
  }

  /**
   * Test for "Layer" property.<br>
   * Constraints of {@link Container#add(java.awt.Component, Object)} has no layer.
   */
  public void test_layer_noConstraints() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JLayeredPane pane = new JLayeredPane();",
            "    add(pane);",
            "    {",
            "      JButton button = new JButton();",
            "      pane.add(button, BorderLayout.NORTH);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    JLayeredPaneInfo pane = (JLayeredPaneInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button = pane.getChildrenComponents().get(0);
    // check value from JLayeredPane (to check that it was executed)
    assertEquals(0, ((JLayeredPane) pane.getObject()).getLayer(button.getComponent()));
    // check "Layer" property
    Property layerProperty = button.getPropertyByTitle("Layer");
    assertNotNull(layerProperty);
    assertTrue(layerProperty.isModified());
    assertEquals(0, layerProperty.getValue());
    // update "Layer" property
    layerProperty.setValue(20);
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    JLayeredPane pane = new JLayeredPane();",
        "    add(pane);",
        "    {",
        "      JButton button = new JButton();",
        "      pane.setLayer(button, 20);",
        "      pane.add(button, BorderLayout.NORTH);",
        "    }",
        "  }",
        "}");
    assertEquals(20, layerProperty.getValue());
  }

  /**
   * Test for "Layer" property.<br>
   * Value is in {@link JLayeredPane#setLayer(java.awt.Component, int)}.
   */
  public void test_layer_setLayer_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JLayeredPane pane = new JLayeredPane();",
            "    add(pane);",
            "    {",
            "      JButton button = new JButton();",
            "      pane.add(button);",
            "      pane.setLayer(button, 10);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    JLayeredPaneInfo pane = (JLayeredPaneInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button = pane.getChildrenComponents().get(0);
    // check value from JLayeredPane (to check that it was executed)
    assertEquals(10, ((JLayeredPane) pane.getObject()).getLayer(button.getComponent()));
    // check "Layer" property
    Property layerProperty = button.getPropertyByTitle("Layer");
    assertNotNull(layerProperty);
    assertTrue(layerProperty.isModified());
    assertEquals(10, layerProperty.getValue());
    // update "Layer" property
    layerProperty.setValue(20);
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    JLayeredPane pane = new JLayeredPane();",
        "    add(pane);",
        "    {",
        "      JButton button = new JButton();",
        "      pane.add(button);",
        "      pane.setLayer(button, 20);",
        "    }",
        "  }",
        "}");
    assertEquals(20, layerProperty.getValue());
  }

  /**
   * Test for "Layer" property.<br>
   * Value is in {@link JLayeredPane#setLayer(java.awt.Component, int, int)}.
   */
  public void test_layer_setLayer_2() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JLayeredPane pane = new JLayeredPane();",
            "    add(pane);",
            "    {",
            "      JButton button = new JButton();",
            "      pane.add(button);",
            "      pane.setLayer(button, 10, 5);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    JLayeredPaneInfo pane = (JLayeredPaneInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button = pane.getChildrenComponents().get(0);
    // check value from JLayeredPane (to check that it was executed)
    assertEquals(10, ((JLayeredPane) pane.getObject()).getLayer(button.getComponent()));
    // check "Layer" property
    Property layerProperty = button.getPropertyByTitle("Layer");
    assertNotNull(layerProperty);
    assertTrue(layerProperty.isModified());
    assertEquals(10, layerProperty.getValue());
    // update "Layer" property
    layerProperty.setValue(20);
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    JLayeredPane pane = new JLayeredPane();",
        "    add(pane);",
        "    {",
        "      JButton button = new JButton();",
        "      pane.add(button);",
        "      pane.setLayer(button, 20, 5);",
        "    }",
        "  }",
        "}");
    assertEquals(20, layerProperty.getValue());
  }

  /**
   * Test for "Layer" property.<br>
   * Value is in {@link JLayeredPane#setLayer(java.awt.Component, int)}.
   */
  public void test_layer_setLayer_remove() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JLayeredPane pane = new JLayeredPane();",
            "    add(pane);",
            "    {",
            "      JButton button = new JButton();",
            "      pane.add(button);",
            "      pane.setLayer(button, 10);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    JLayeredPaneInfo pane = (JLayeredPaneInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button = pane.getChildrenComponents().get(0);
    // check value from JLayeredPane (to check that it was executed)
    assertEquals(10, ((JLayeredPane) pane.getObject()).getLayer(button.getComponent()));
    // check "Layer" property
    Property layerProperty = button.getPropertyByTitle("Layer");
    assertNotNull(layerProperty);
    assertTrue(layerProperty.isModified());
    assertEquals(10, layerProperty.getValue());
    // update "Layer" property
    layerProperty.setValue(Property.UNKNOWN_VALUE);
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    JLayeredPane pane = new JLayeredPane();",
        "    add(pane);",
        "    {",
        "      JButton button = new JButton();",
        "      pane.add(button);",
        "    }",
        "  }",
        "}");
    assertEquals(0, layerProperty.getValue());
  }

  /**
   * Graphical children should be sorted according to layer.
   */
  public void test_getGraphicalChildren() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JLayeredPane pane = new JLayeredPane();",
            "    add(pane);",
            "    {",
            "      JButton button_1 = new JButton();",
            "      pane.add(button_1);",
            "      pane.setLayer(button_1, 5);",
            "    }",
            "    {",
            "      JButton button_2 = new JButton();",
            "      pane.add(button_2);",
            "      pane.setLayer(button_2, 10);",
            "    }",
            "    {",
            "      JButton button_3 = new JButton();",
            "      pane.add(button_3);",
            "      pane.setLayer(button_3, 10);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    JLayeredPaneInfo pane = (JLayeredPaneInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button_1 = pane.getChildrenComponents().get(0);
    ComponentInfo button_2 = pane.getChildrenComponents().get(1);
    ComponentInfo button_3 = pane.getChildrenComponents().get(2);
    //
    IObjectPresentation presentation = pane.getPresentation();
    List<ObjectInfo> graphical = presentation.getChildrenGraphical();
    assertThat(graphical).isEqualTo(ImmutableList.of(button_2, button_3, button_1));
  }
}
