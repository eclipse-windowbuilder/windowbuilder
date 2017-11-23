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

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.InvocationChildAssociationAccessor;
import org.eclipse.wb.internal.swing.model.CoordinateUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.component.JTabbedPaneInfo;
import org.eclipse.wb.internal.swing.model.component.JTabbedPaneTabInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang.StringUtils;

import java.awt.Color;
import java.awt.Component;
import java.util.List;

import javax.swing.JTabbedPane;

/**
 * Test for {@link JTabbedPane}.
 * 
 * @author scheglov_ke
 */
public class JTabbedPaneTest extends SwingModelTest {
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
  public void test_association() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JTabbedPane tabbed = new JTabbedPane();",
            "    add(tabbed);",
            "    //",
            "    JButton button = new JButton();",
            "    tabbed.addTab('New tab', button);",
            "  }",
            "}");
    refresh();
    JTabbedPaneInfo tabbed = (JTabbedPaneInfo) panel.getChildrenComponents().get(0);
    assertEquals(1, tabbed.getChildrenComponents().size());
    // check association
    ComponentInfo button = tabbed.getChildrenComponents().get(0);
    assertInstanceOf(InvocationChildAssociation.class, button.getAssociation());
    // check active component
    assertSame(button, tabbed.getActiveComponent());
    //
    {
      List<JTabbedPaneTabInfo> tabs = tabbed.getTabs();
      assertThat(tabs).hasSize(1);
      JTabbedPaneTabInfo tab = tabs.get(0);
      //
      assertSame(tabbed, tab.getPane());
      assertSame(button, tab.getComponent());
      {
        Rectangle tabBounds =
            CoordinateUtils.get(((JTabbedPane) tabbed.getObject()).getBoundsAt(0));
        assertEquals(tabBounds, tab.getBounds());
      }
      // object
      {
        JTabbedPaneTabInfo tab2 = new JTabbedPaneTabInfo(tabbed, button, null);
        assertNotSame(tab, tab2);
        assertEquals(tab, tab2);
        assertFalse(tab.equals(tabbed));
        assertEquals(tab.hashCode(), tab2.hashCode());
      }
    }
  }

  /**
   * Test for using {@link JTabbedPane#insertTab(String, javax.swing.Icon, Component, String, int)}.
   */
  public void test_association_insert() throws Exception {
    parseContainer(
        "class Test extends JPanel {",
        "  Test() {",
        "    JTabbedPane tabbed = new JTabbedPane();",
        "    add(tabbed);",
        "    {",
        "      JButton button = new JButton();",
        "      tabbed.insertTab('title', null, button, 'tip', 0);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(tabbed)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JTabbedPane} {local-unique: tabbed} {/new JTabbedPane()/ /add(tabbed)/ /tabbed.insertTab('title', null, button, 'tip', 0)/}",
        "    {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /tabbed.insertTab('title', null, button, 'tip', 0)/}");
  }

  /**
   * Test for {@link JTabbedPaneInfo#isHorizontal()}.
   */
  public void test_isHorizontal() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    {",
            "      JTabbedPane tabbed_1 = new JTabbedPane(SwingConstants.TOP);",
            "      add(tabbed_1);",
            "    }",
            "    {",
            "      JTabbedPane tabbed_2 = new JTabbedPane(SwingConstants.LEFT);",
            "      add(tabbed_2);",
            "    }",
            "  }",
            "}");
    refresh();
    JTabbedPaneInfo tabbed_1 = (JTabbedPaneInfo) panel.getChildrenComponents().get(0);
    JTabbedPaneInfo tabbed_2 = (JTabbedPaneInfo) panel.getChildrenComponents().get(1);
    // checks
    assertTrue(tabbed_1.isHorizontal());
    assertFalse(tabbed_2.isHorizontal());
  }

  /**
   * If we can not evaluate {@link Component} for some tab, we should ignore it.
   */
  public void test_badTab() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JTabbedPane tabbed = new JTabbedPane();",
            "    add(tabbed);",
            "    tabbed.addTab('title', null);",
            "  }",
            "}");
    refresh();
    JTabbedPaneInfo tabbed = (JTabbedPaneInfo) panel.getChildrenComponents().get(0);
    assertNoErrors(panel);
    // ask tabs
    assertThat(tabbed.getTabs()).isEmpty();
  }

  /**
   * {@link JTabbedPane} can have more tabs that number of {@link ComponentInfo}s which we see.
   */
  public void test_getTabs_noComponentModels() throws Exception {
    setFileContentSrc(
        "test/MyTabbedPane.java",
        getTestSource(
            "public class MyTabbedPane extends JTabbedPane {",
            "  public MyTabbedPane() {",
            "    addTab('tab_1', new JButton());",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    MyTabbedPane tabbed = new MyTabbedPane();",
            "    add(tabbed);",
            "  }",
            "}");
    refresh();
    assertNoErrors(panel);
    // ask tabs
    JTabbedPaneInfo tabbed = getJavaInfoByName("tabbed");
    assertThat(tabbed.getTabs()).isEmpty();
  }

  /**
   * Test for using {@link JTabbedPane#indexOfComponent(Component)} in "at" invocation.
   */
  public void test_parse_indexOfComponent() throws Exception {
    parseContainer(
        "class Test extends JPanel {",
        "  Test() {",
        "    JTabbedPane tabbed = new JTabbedPane(SwingConstants.TOP);",
        "    add(tabbed);",
        "    {",
        "      JButton button = new JButton();",
        "      tabbed.addTab('111', button);",
        "      tabbed.setForegroundAt(tabbed.indexOfComponent(button), Color.RED);",
        "    }",
        "  }",
        "}");
    refresh();
    ComponentInfo button = getJavaInfoByName("button");
    // 
    GenericProperty property = (GenericProperty) getTabPropertyByTitle(button, "foreground");
    assertEquals("RED", getPropertyText(property));
  }

  /**
   * We can not evaluate {@link JTabbedPane} model, so we should not execute
   * {@link JTabbedPane#setModel(javax.swing.SingleSelectionModel)}. {@link JTabbedPane} can have
   * more tabs
   */
  public void test_disable_setModel() throws Exception {
    parseContainer(
        "class Test extends JPanel {",
        "  Test() {",
        "    JTabbedPane tabbed = new JTabbedPane();",
        "    add(tabbed);",
        "    tabbed.setModel(null);",
        "    {",
        "      JButton button = new JButton();",
        "      tabbed.addTab('tab', button);",
        "    }",
        "  }",
        "}");
    refresh();
    assertNoErrors(m_lastParseInfo);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Active component
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link JTabbedPaneInfo#getActiveComponent()}, for {@link JTabbedPane} without
   * components.
   */
  public void test_getActiveComponent_null() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JTabbedPane tabbed = new JTabbedPane(SwingConstants.TOP);",
            "    add(tabbed);",
            "  }",
            "}");
    refresh();
    JTabbedPaneInfo tabbed = (JTabbedPaneInfo) panel.getChildrenComponents().get(0);
    IObjectPresentation presentation = tabbed.getPresentation();
    // active component
    {
      assertThat(tabbed.getTabs()).isEmpty();
      assertNull(tabbed.getActiveComponent());
    }
    // empty presentation
    {
      assertThat(presentation.getChildrenGraphical()).isEmpty();
    }
  }

  /**
   * Test for {@link JTabbedPaneInfo#getActiveComponent()}, for {@link JTabbedPane} with components.
   */
  public void test_getActiveComponent_set() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JTabbedPane tabbed = new JTabbedPane(SwingConstants.TOP);",
            "    add(tabbed);",
            "    tabbed.addTab('111', new JButton('111'));",
            "    tabbed.addTab('222', new JButton('222'));",
            "  }",
            "}");
    refresh();
    JTabbedPaneInfo tabbed = (JTabbedPaneInfo) panel.getChildrenComponents().get(0);
    IObjectPresentation presentation = tabbed.getPresentation();
    ComponentInfo button_1 = tabbed.getChildrenComponents().get(0);
    ComponentInfo button_2 = tabbed.getChildrenComponents().get(1);
    // tabs
    assertThat(tabbed.getTabs()).hasSize(2);
    // active component
    {
      assertSame(button_1, tabbed.getActiveComponent());
      assertThat(presentation.getChildrenGraphical()).contains(button_1).doesNotContain(button_2);
    }
    // set new active
    {
      tabbed.setActiveComponent(button_2);
      assertSame(button_2, tabbed.getActiveComponent());
      assertThat(presentation.getChildrenGraphical()).contains(button_2).doesNotContain(button_1);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Tab" properties
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for sub-properties of "Tab" property.
   */
  public void test_property_list() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JTabbedPane tabbed = new JTabbedPane(SwingConstants.TOP);",
            "    add(tabbed);",
            "    tabbed.addTab('111', new JButton());",
            "  }",
            "}");
    refresh();
    JTabbedPaneInfo tabbed = (JTabbedPaneInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button = tabbed.getChildrenComponents().get(0);
    //
    ComplexProperty tabProperty = (ComplexProperty) button.getPropertyByTitle("Tab");
    assertNotNull(tabProperty);
    // check for caching "Tab" property
    assertSame(tabProperty, button.getPropertyByTitle("Tab"));
    // check for sub-properties
    {
      Property[] properties = tabProperty.getProperties();
      String[] extectedTitles =
          new String[]{
              "background",
              "disabledIcon",
              "enabled",
              "foreground",
              "icon",
              "mnemonic",
              "mnemonicIndex",
              "title",
              "tooltip"};
      for (int i = 0; i < properties.length; i++) {
        Property property = properties[i];
        assertEquals(extectedTitles[i], property.getTitle());
      }
    }
  }

  /**
   * Test for sub-properties with {@link InvocationChildAssociationAccessor}.
   */
  public void test_property_subAssociation() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JTabbedPane tabbed = new JTabbedPane(SwingConstants.TOP);",
            "    add(tabbed);",
            "    tabbed.addTab('111', new JButton());",
            "  }",
            "}");
    refresh();
    JTabbedPaneInfo tabbed = (JTabbedPaneInfo) panel.getChildrenComponents().get(0);
    // prepare button/property
    ComponentInfo button = tabbed.getChildrenComponents().get(0);
    Property property = getTabPropertyByTitle(button, "title");
    // check current value
    assertTrue(property.isModified());
    assertEquals("111", property.getValue());
    // check modification
    {
      String expectedSource = StringUtils.replace(m_lastEditor.getSource(), "111", "222");
      // modify value
      property.setValue("222");
      assertEditor(expectedSource, m_lastEditor);
      // reset to default
      expectedSource = StringUtils.replace(expectedSource, "\"222\"", "(String) null");
      property.setValue(Property.UNKNOWN_VALUE);
      assertEditor(expectedSource, m_lastEditor);
    }
  }

  /**
   * Test for sub-properties with {@link JTabbedPane_AtAccessor}.
   */
  public void test_property_subAt() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JTabbedPane tabbed = new JTabbedPane(SwingConstants.TOP);",
            "    add(tabbed);",
            "    tabbed.addTab('111', new JButton());",
            "    tabbed.setForegroundAt(0, Color.RED);",
            "  }",
            "}");
    refresh();
    JTabbedPaneInfo tabbed = (JTabbedPaneInfo) panel.getChildrenComponents().get(0);
    // 
    ComponentInfo button = tabbed.getChildrenComponents().get(0);
    GenericProperty property = (GenericProperty) getTabPropertyByTitle(button, "foreground");
    // check current value
    assertTrue(property.isModified());
    assertInstanceOf(Color.class, property.getValue());
    // modify value
    {
      String expectedSource = StringUtils.replace(m_lastEditor.getSource(), "RED", "BLUE");
      property.setExpression("java.awt.Color.BLUE", null);
      assertEditor(expectedSource, m_lastEditor);
    }
    // remove value
    {
      // can not remove, ignore "set"
      property.setValue(Property.UNKNOWN_VALUE);
      assertEditor(
          "class Test extends JPanel {",
          "  Test() {",
          "    JTabbedPane tabbed = new JTabbedPane(SwingConstants.TOP);",
          "    add(tabbed);",
          "    tabbed.addTab('111', new JButton());",
          "  }",
          "}");
    }
  }

  /**
   * Test for sub-properties with {@link JTabbedPane_AtAccessor}.
   */
  public void test_property_subAtSet() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JTabbedPane tabbed = new JTabbedPane(SwingConstants.TOP);",
            "    add(tabbed);",
            "    //",
            "    JButton button = new JButton();",
            "    tabbed.addTab('111', button);",
            "    tabbed.setEnabledAt(0, false);",
            "  }",
            "}");
    refresh();
    JTabbedPaneInfo tabbed = (JTabbedPaneInfo) panel.getChildrenComponents().get(0);
    // 
    ComponentInfo button = tabbed.getChildrenComponents().get(0);
    GenericProperty property = (GenericProperty) getTabPropertyByTitle(button, "foreground");
    // check current value
    assertFalse(property.isModified());
    assertSame(Property.UNKNOWN_VALUE, property.getValue());
    // add value
    {
      property.setExpression("java.awt.Color.RED", null);
      assertEditor(
          "class Test extends JPanel {",
          "  Test() {",
          "    JTabbedPane tabbed = new JTabbedPane(SwingConstants.TOP);",
          "    add(tabbed);",
          "    //",
          "    JButton button = new JButton();",
          "    tabbed.addTab('111', button);",
          "    tabbed.setForegroundAt(0, Color.RED);",
          "    tabbed.setEnabledAt(0, false);",
          "  }",
          "}");
    }
  }

  /**
   * Test for sub-properties with {@link JTabbedPane_AtAccessor}.<br>
   * This checks {@link GenericPropertyImpl#setExpression(String, Object)}, when we iterate over
   * {@link ExpressionAccessor}'s until some of them will able to set expression.
   */
  public void test_property_subAtSetConflict() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JTabbedPane tabbed = new JTabbedPane(SwingConstants.TOP);",
            "    add(tabbed);",
            "    tabbed.addTab('111', new JButton());",
            "  }",
            "}");
    refresh();
    JTabbedPaneInfo tabbed = (JTabbedPaneInfo) panel.getChildrenComponents().get(0);
    // 
    ComponentInfo button = tabbed.getChildrenComponents().get(0);
    GenericProperty property = (GenericProperty) getTabPropertyByTitle(button, "tooltip");
    // check current value
    assertFalse(property.isModified());
    assertSame(Property.UNKNOWN_VALUE, property.getValue());
    // add value
    {
      property.setValue("ToolTip");
      assertEditor(
          "class Test extends JPanel {",
          "  Test() {",
          "    JTabbedPane tabbed = new JTabbedPane(SwingConstants.TOP);",
          "    add(tabbed);",
          "    tabbed.addTab('111', new JButton());",
          "    tabbed.setToolTipTextAt(0, 'ToolTip');",
          "  }",
          "}");
    }
  }

  /**
   * Test for sub-properties with {@link JTabbedPane_AtAccessor}.
   */
  public void test_property_subAtRemove() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JTabbedPane tabbed = new JTabbedPane(SwingConstants.TOP);",
            "    add(tabbed);",
            "    //",
            "    tabbed.addTab('111', new JButton());",
            "    tabbed.setEnabledAt(0, false);",
            "  }",
            "}");
    refresh();
    JTabbedPaneInfo tabbed = (JTabbedPaneInfo) panel.getChildrenComponents().get(0);
    // 
    ComponentInfo button = tabbed.getChildrenComponents().get(0);
    GenericProperty property = (GenericProperty) getTabPropertyByTitle(button, "enabled");
    // check current value
    assertTrue(property.isModified());
    assertEquals(Boolean.FALSE, property.getValue());
    // add value
    {
      property.setValue(Boolean.TRUE);
      assertEditor(
          "class Test extends JPanel {",
          "  Test() {",
          "    JTabbedPane tabbed = new JTabbedPane(SwingConstants.TOP);",
          "    add(tabbed);",
          "    //",
          "    tabbed.addTab('111', new JButton());",
          "  }",
          "}");
    }
  }

  /**
   * @return the sub-property of "Tab" property.
   */
  private Property getTabPropertyByTitle(ComponentInfo component, String title) throws Exception {
    ComplexProperty tabProperty = (ComplexProperty) component.getPropertyByTitle("Tab");
    Property[] properties = tabProperty.getProperties();
    for (int i = 0; i < properties.length; i++) {
      Property property = properties[i];
      if (property.getTitle().equals(title)) {
        return property;
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JTabbedPane tabbed = new JTabbedPane();",
            "    add(tabbed);",
            "  }",
            "}");
    JTabbedPaneInfo tabbed = (JTabbedPaneInfo) panel.getChildrenComponents().get(0);
    //
    panel.refresh();
    try {
      // no active component initially
      assertNull(tabbed.getActiveComponent());
      // add component
      ComponentInfo button;
      {
        button = createJButton();
        try {
          tabbed.startEdit();
          tabbed.command_CREATE(button, null);
        } finally {
          tabbed.endEdit();
        }
        assertInstanceOf(InvocationChildAssociation.class, button.getAssociation());
      }
      // check source
      assertEditor(
          "class Test extends JPanel {",
          "  Test() {",
          "    JTabbedPane tabbed = new JTabbedPane();",
          "    add(tabbed);",
          "    {",
          "      JButton button = new JButton();",
          "      tabbed.addTab('New tab', null, button, null);",
          "    }",
          "  }",
          "}");
      // new component is active
      assertSame(button, tabbed.getActiveComponent());
    } finally {
      panel.refresh_dispose();
    }
  }

  /**
   * Test for CREATE with "set*At()".
   */
  public void test_CREATE_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JTabbedPane tabbed = new JTabbedPane();",
            "    add(tabbed);",
            "    {",
            "      tabbed.addTab('111', new JButton());",
            "      tabbed.setEnabledAt(0, false);",
            "    }",
            "  }",
            "}");
    refresh();
    JTabbedPaneInfo tabbed = (JTabbedPaneInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button = tabbed.getChildrenComponents().get(0);
    // add component
    ComponentInfo newButton;
    {
      newButton = createJButton();
      try {
        tabbed.startEdit();
        tabbed.command_CREATE(newButton, button);
      } finally {
        tabbed.endEdit();
      }
    }
    // check source
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    JTabbedPane tabbed = new JTabbedPane();",
        "    add(tabbed);",
        "    {",
        "      JButton button = new JButton();",
        "      tabbed.addTab('New tab', null, button, null);",
        "    }",
        "    {",
        "      tabbed.addTab('111', new JButton());",
        "      tabbed.setEnabledAt(1, false);",
        "    }",
        "  }",
        "}");
    // new component is active
    assertSame(newButton, tabbed.getActiveComponent());
  }

  /**
   * Test for CREATE via flow container".
   */
  public void test_CREATE_2() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JTabbedPane tabbed = new JTabbedPane();",
            "    add(tabbed);",
            "    {",
            "      tabbed.addTab('tab1', new JLabel());",
            "      tabbed.setEnabledAt(0, false);",
            "    }",
            "  }",
            "}");
    refresh();
    JTabbedPaneInfo tabbed = (JTabbedPaneInfo) panel.getChildrenComponents().get(0);
    ComponentInfo label = tabbed.getChildrenComponents().get(0);
    // add component
    ComponentInfo newButton;
    {
      newButton = createJButton();
      FlowContainer flowContainer = new FlowContainerFactory(tabbed, false).get().get(0);
      assertTrue(flowContainer.validateComponent(newButton));
      flowContainer.command_CREATE(newButton, label);
    }
    // check source
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    JTabbedPane tabbed = new JTabbedPane();",
        "    add(tabbed);",
        "    {",
        "      JButton button = new JButton();",
        "      tabbed.addTab('New tab', null, button, null);",
        "    }",
        "    {",
        "      tabbed.addTab('tab1', new JLabel());",
        "      tabbed.setEnabledAt(1, false);",
        "    }",
        "  }",
        "}");
    // new component is active
    assertSame(newButton, tabbed.getActiveComponent());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Move
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_DELETE() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JTabbedPane tabbed = new JTabbedPane();",
            "    add(tabbed);",
            "    {",
            "      JButton button = new JButton();",
            "      tabbed.addTab('000', button);",
            "      tabbed.setEnabledAt(0, false);",
            "    }",
            "    {",
            "      JButton button = new JButton();",
            "      tabbed.addTab('111', button);",
            "      tabbed.setEnabledAt(1, false);",
            "    }",
            "  }",
            "}");
    // prepare source
    JTabbedPaneInfo tabbed = (JTabbedPaneInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button = tabbed.getChildrenComponents().get(0);
    // do move
    button.delete();
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    JTabbedPane tabbed = new JTabbedPane();",
        "    add(tabbed);",
        "    {",
        "      JButton button = new JButton();",
        "      tabbed.addTab('111', button);",
        "      tabbed.setEnabledAt(0, false);",
        "    }",
        "  }",
        "}");
  }

  public void test_OUT() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JTabbedPane tabbed = new JTabbedPane();",
            "    add(tabbed);",
            "    {",
            "      JButton button = new JButton();",
            "      tabbed.addTab('000', button);",
            "      tabbed.setEnabledAt(0, false);",
            "    }",
            "    {",
            "      JButton button = new JButton();",
            "      tabbed.addTab('111', button);",
            "      tabbed.setEnabledAt(1, false);",
            "    }",
            "    {",
            "      JPanel innerPanel = new JPanel();",
            "      add(innerPanel);",
            "    }",
            "  }",
            "}");
    // prepare source
    JTabbedPaneInfo tabbed = (JTabbedPaneInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button = tabbed.getChildrenComponents().get(0);
    // prepare target
    ContainerInfo innerPanel = (ContainerInfo) panel.getChildrenComponents().get(1);
    FlowLayoutInfo innerLayout = (FlowLayoutInfo) innerPanel.getLayout();
    // do move
    innerLayout.move(button, null);
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    JTabbedPane tabbed = new JTabbedPane();",
        "    add(tabbed);",
        "    {",
        "      JButton button = new JButton();",
        "      tabbed.addTab('111', button);",
        "      tabbed.setEnabledAt(0, false);",
        "    }",
        "    {",
        "      JPanel innerPanel = new JPanel();",
        "      add(innerPanel);",
        "      {",
        "        JButton button = new JButton();",
        "        innerPanel.add(button);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  public void test_MOVE_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JTabbedPane tabbed = new JTabbedPane();",
            "    add(tabbed);",
            "    {",
            "      JButton button_1 = new JButton();",
            "      tabbed.addTab('111', button_1);",
            "    }",
            "    {",
            "      JButton button_2 = new JButton();",
            "      tabbed.addTab('222', button_2);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    JTabbedPaneInfo tabbed = (JTabbedPaneInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button_1 = tabbed.getChildrenComponents().get(0);
    ComponentInfo button_2 = tabbed.getChildrenComponents().get(1);
    assertSame(button_1, tabbed.getActiveComponent());
    // do move
    try {
      tabbed.startEdit();
      tabbed.command_MOVE(button_2, button_1);
    } finally {
      tabbed.endEdit();
    }
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    JTabbedPane tabbed = new JTabbedPane();",
        "    add(tabbed);",
        "    {",
        "      JButton button_2 = new JButton();",
        "      tabbed.addTab('222', button_2);",
        "    }",
        "    {",
        "      JButton button_1 = new JButton();",
        "      tabbed.addTab('111', button_1);",
        "    }",
        "  }",
        "}");
    // active tab still should be "111"
    assertSame(button_1, tabbed.getActiveComponent());
  }

  public void test_MOVE_2() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JTabbedPane tabbed = new JTabbedPane();",
            "    add(tabbed);",
            "    {",
            "      JButton button_1 = new JButton();",
            "      tabbed.addTab('111', button_1);",
            "    }",
            "    {",
            "      JButton button_2 = new JButton();",
            "      tabbed.addTab('222', button_2);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    JTabbedPaneInfo tabbed = (JTabbedPaneInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button_1 = tabbed.getChildrenComponents().get(0);
    ComponentInfo button_2 = tabbed.getChildrenComponents().get(1);
    assertSame(button_1, tabbed.getActiveComponent());
    // do move
    {
      FlowContainer flowContainer = new FlowContainerFactory(tabbed, false).get().get(0);
      flowContainer.command_MOVE(button_2, button_1);
    }
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    JTabbedPane tabbed = new JTabbedPane();",
        "    add(tabbed);",
        "    {",
        "      JButton button_2 = new JButton();",
        "      tabbed.addTab('222', button_2);",
        "    }",
        "    {",
        "      JButton button_1 = new JButton();",
        "      tabbed.addTab('111', button_1);",
        "    }",
        "  }",
        "}");
    // active tab still should be "111"
    assertSame(button_1, tabbed.getActiveComponent());
  }

  public void test_MOVE_atForward() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JTabbedPane tabbed = new JTabbedPane();",
            "    add(tabbed);",
            "    //",
            "    JButton button_0 = new JButton();",
            "    tabbed.addTab('000', button_0);",
            "    tabbed.setToolTipTextAt(0, '000');",
            "    //",
            "    JButton button_1 = new JButton();",
            "    tabbed.addTab('111', button_1);",
            "    tabbed.setToolTipTextAt(1, '111');",
            "    //",
            "    JButton button_2 = new JButton();",
            "    tabbed.addTab('222', button_2);",
            "    tabbed.setToolTipTextAt(2, '222');",
            "    //",
            "    JButton button_3 = new JButton();",
            "    tabbed.addTab('333', button_3);",
            "    tabbed.setToolTipTextAt(3, '333');",
            "  }",
            "}");
    // prepare source
    JTabbedPaneInfo tabbed = (JTabbedPaneInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button_1 = tabbed.getChildrenComponents().get(1);
    ComponentInfo button_3 = tabbed.getChildrenComponents().get(3);
    // do move
    try {
      tabbed.startEdit();
      tabbed.command_MOVE(button_1, button_3);
    } finally {
      tabbed.endEdit();
    }
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    JTabbedPane tabbed = new JTabbedPane();",
        "    add(tabbed);",
        "    //",
        "    JButton button_0 = new JButton();",
        "    tabbed.addTab('000', button_0);",
        "    tabbed.setToolTipTextAt(0, '000');",
        "    //",
        "    JButton button_2 = new JButton();",
        "    tabbed.addTab('222', button_2);",
        "    tabbed.setToolTipTextAt(1, '222');",
        "    //",
        "    JButton button_1 = new JButton();",
        "    tabbed.addTab('111', button_1);",
        "    tabbed.setToolTipTextAt(2, '111');",
        "    //",
        "    JButton button_3 = new JButton();",
        "    tabbed.addTab('333', button_3);",
        "    tabbed.setToolTipTextAt(3, '333');",
        "  }",
        "}");
  }

  public void test_MOVE_atBackward() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JTabbedPane tabbed = new JTabbedPane();",
            "    add(tabbed);",
            "    //",
            "    JButton button_0 = new JButton();",
            "    tabbed.addTab('000', button_0);",
            "    tabbed.setToolTipTextAt(0, '000');",
            "    //",
            "    JButton button_1 = new JButton();",
            "    tabbed.addTab('111', button_1);",
            "    tabbed.setToolTipTextAt(1, '111');",
            "    //",
            "    JButton button_2 = new JButton();",
            "    tabbed.addTab('222', button_2);",
            "    tabbed.setToolTipTextAt(2, '222');",
            "    //",
            "    JButton button_3 = new JButton();",
            "    tabbed.addTab('333', button_3);",
            "    tabbed.setToolTipTextAt(3, '333');",
            "  }",
            "}");
    // prepare source
    JTabbedPaneInfo tabbed = (JTabbedPaneInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button_1 = tabbed.getChildrenComponents().get(1);
    ComponentInfo button_2 = tabbed.getChildrenComponents().get(2);
    // do move
    try {
      tabbed.startEdit();
      tabbed.command_MOVE(button_2, button_1);
    } finally {
      tabbed.endEdit();
    }
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    JTabbedPane tabbed = new JTabbedPane();",
        "    add(tabbed);",
        "    //",
        "    JButton button_0 = new JButton();",
        "    tabbed.addTab('000', button_0);",
        "    tabbed.setToolTipTextAt(0, '000');",
        "    //",
        "    JButton button_2 = new JButton();",
        "    tabbed.addTab('222', button_2);",
        "    tabbed.setToolTipTextAt(1, '222');",
        "    //",
        "    JButton button_1 = new JButton();",
        "    tabbed.addTab('111', button_1);",
        "    tabbed.setToolTipTextAt(2, '111');",
        "    //",
        "    JButton button_3 = new JButton();",
        "    tabbed.addTab('333', button_3);",
        "    tabbed.setToolTipTextAt(3, '333');",
        "  }",
        "}");
  }

  public void test_MOVE_atSeveral() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JTabbedPane tabbed = new JTabbedPane();",
            "    add(tabbed);",
            "    //",
            "    JButton button_0 = new JButton();",
            "    tabbed.addTab('000', button_0);",
            "    tabbed.setToolTipTextAt(0, '000');",
            "    //",
            "    JButton button_1 = new JButton();",
            "    tabbed.addTab('111', button_1);",
            "    tabbed.setToolTipTextAt(1, '111');",
            "    tabbed.setEnabledAt(1, false);",
            "  }",
            "}");
    // prepare source
    JTabbedPaneInfo tabbed = (JTabbedPaneInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button_0 = tabbed.getChildrenComponents().get(0);
    ComponentInfo button_1 = tabbed.getChildrenComponents().get(1);
    // do move
    try {
      tabbed.startEdit();
      tabbed.command_MOVE(button_1, button_0);
    } finally {
      tabbed.endEdit();
    }
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    JTabbedPane tabbed = new JTabbedPane();",
        "    add(tabbed);",
        "    //",
        "    JButton button_1 = new JButton();",
        "    tabbed.addTab('111', button_1);",
        "    tabbed.setToolTipTextAt(0, '111');",
        "    tabbed.setEnabledAt(0, false);",
        "    //",
        "    JButton button_0 = new JButton();",
        "    tabbed.addTab('000', button_0);",
        "    tabbed.setToolTipTextAt(1, '000');",
        "  }",
        "}");
  }

  /**
   * We should not perform NOOP moves.
   */
  public void test_MOVE_beforeAlreadyNext() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JTabbedPane tabbed = new JTabbedPane();",
            "    add(tabbed);",
            "    {",
            "      JButton button = new JButton();",
            "      tabbed.addTab('tab', button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    JTabbedPaneInfo tabbed = (JTabbedPaneInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button = tabbed.getChildrenComponents().get(0);
    // do move
    tabbed.command_MOVE(button, null);
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    JTabbedPane tabbed = new JTabbedPane();",
        "    add(tabbed);",
        "    {",
        "      JButton button = new JButton();",
        "      tabbed.addTab('tab', button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * We should not perform NOOP moves.
   */
  public void test_MOVE_beforeItself() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JTabbedPane tabbed = new JTabbedPane();",
            "    add(tabbed);",
            "    {",
            "      JButton button = new JButton();",
            "      tabbed.addTab('tab', button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    JTabbedPaneInfo tabbed = (JTabbedPaneInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button = tabbed.getChildrenComponents().get(0);
    // do move
    tabbed.command_MOVE(button, button);
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    JTabbedPane tabbed = new JTabbedPane();",
        "    add(tabbed);",
        "    {",
        "      JButton button = new JButton();",
        "      tabbed.addTab('tab', button);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ADD
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_ADD_beforeExisting() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JTabbedPane tabbed = new JTabbedPane();",
            "    add(tabbed);",
            "    {",
            "      JButton button_0 = new JButton();",
            "      tabbed.addTab('000', button_0);",
            "      tabbed.setToolTipTextAt(0, '000');",
            "    }",
            "    {",
            "      JPanel innerPanel = new JPanel();",
            "      add(innerPanel);",
            "      {",
            "        JButton button = new JButton();",
            "        innerPanel.add(button);",
            "      }",
            "    }",
            "  }",
            "}");
    panel.refresh();
    JTabbedPaneInfo tabbed = getJavaInfoByName("tabbed");
    ComponentInfo button_0 = getJavaInfoByName("button_0");
    assertSame(button_0, tabbed.getActiveComponent());
    // 
    ComponentInfo button = getJavaInfoByName("button");
    tabbed.command_ADD(button, button_0);
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    JTabbedPane tabbed = new JTabbedPane();",
        "    add(tabbed);",
        "    {",
        "      JButton button = new JButton();",
        "      tabbed.addTab('New tab', null, button, null);",
        "    }",
        "    {",
        "      JButton button_0 = new JButton();",
        "      tabbed.addTab('000', button_0);",
        "      tabbed.setToolTipTextAt(1, '000');",
        "    }",
        "    {",
        "      JPanel innerPanel = new JPanel();",
        "      add(innerPanel);",
        "    }",
        "  }",
        "}");
    assertSame(button, tabbed.getActiveComponent());
  }

  public void test_ADD_first() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    {",
            "      JTabbedPane tabbed = new JTabbedPane();",
            "      add(tabbed);",
            "    }",
            "    {",
            "      JButton button = new JButton();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    JTabbedPaneInfo tabbed = getJavaInfoByName("tabbed");
    ComponentInfo button = getJavaInfoByName("button");
    // 
    tabbed.command_ADD(button, null);
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    {",
        "      JTabbedPane tabbed = new JTabbedPane();",
        "      add(tabbed);",
        "      {",
        "        JButton button = new JButton();",
        "        tabbed.addTab('New tab', null, button, null);",
        "      }",
        "    }",
        "  }",
        "}");
    assertSame(button, tabbed.getActiveComponent());
  }

  public void test_ADD_tree() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JTabbedPane tabbed = new JTabbedPane();",
            "    add(tabbed);",
            "    {",
            "      JButton button_0 = new JButton();",
            "      tabbed.addTab('000', button_0);",
            "      tabbed.setToolTipTextAt(0, '000');",
            "    }",
            "    {",
            "      JPanel innerPanel = new JPanel();",
            "      add(innerPanel);",
            "      {",
            "        JButton button = new JButton();",
            "        innerPanel.add(button);",
            "      }",
            "    }",
            "  }",
            "}");
    panel.refresh();
    JTabbedPaneInfo tabbed = getJavaInfoByName("tabbed");
    ComponentInfo button_0 = getJavaInfoByName("button_0");
    assertSame(button_0, tabbed.getActiveComponent());
    // 
    ComponentInfo button = getJavaInfoByName("button");
    {
      FlowContainer flowContainer = new FlowContainerFactory(tabbed, false).get().get(0);
      flowContainer.command_MOVE(button, button_0);
    }
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    JTabbedPane tabbed = new JTabbedPane();",
        "    add(tabbed);",
        "    {",
        "      JButton button = new JButton();",
        "      tabbed.addTab('New tab', null, button, null);",
        "    }",
        "    {",
        "      JButton button_0 = new JButton();",
        "      tabbed.addTab('000', button_0);",
        "      tabbed.setToolTipTextAt(1, '000');",
        "    }",
        "    {",
        "      JPanel innerPanel = new JPanel();",
        "      add(innerPanel);",
        "    }",
        "  }",
        "}");
    assertSame(button, tabbed.getActiveComponent());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selecting
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ObjectEventListener#selecting(ObjectInfo, boolean[])}.
   */
  public void test_selecting() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JTabbedPane tabbed = new JTabbedPane();",
            "    add(tabbed);",
            "    {",
            "      JButton button_0 = new JButton();",
            "      tabbed.addTab('000', button_0);",
            "    }",
            "    {",
            "      JPanel innerPanel = new JPanel();",
            "      tabbed.add(innerPanel);",
            "      {",
            "        JButton button = new JButton();",
            "        innerPanel.add(button);",
            "      }",
            "    }",
            "  }",
            "}");
    //
    JTabbedPaneInfo tabbed = (JTabbedPaneInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button_0 = tabbed.getChildrenComponents().get(0);
    //
    ContainerInfo innerPanel = (ContainerInfo) tabbed.getChildrenComponents().get(1);
    ComponentInfo button = innerPanel.getChildrenComponents().get(0);
    // 
    panel.refresh();
    try {
      assertSame(button_0, tabbed.getActiveComponent());
      // select "button_0"
      {
        boolean[] refreshFlag = new boolean[1];
        panel.getBroadcastObject().selecting(button_0, refreshFlag);
        assertFalse(refreshFlag[0]);
        assertSame(button_0, tabbed.getActiveComponent());
      }
      // select "button" on "innerPanel"
      {
        boolean[] refreshFlag = new boolean[1];
        panel.getBroadcastObject().selecting(button, refreshFlag);
        assertTrue(refreshFlag[0]);
        assertSame(innerPanel, tabbed.getActiveComponent());
      }
    } finally {
      panel.refresh_dispose();
    }
  }
}
