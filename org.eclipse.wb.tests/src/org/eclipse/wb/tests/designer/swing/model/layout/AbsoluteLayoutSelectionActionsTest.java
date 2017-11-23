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
package org.eclipse.wb.tests.designer.swing.model.layout;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.absolute.AbsoluteLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.absolute.SelectionActionsSupport;
import org.eclipse.wb.tests.designer.core.model.TestObjectInfo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.swing.JPanel;

/**
 * Test for {@link AbsoluteLayoutInfo} selection action's.
 * 
 * @author lobas_av
 */
public class AbsoluteLayoutSelectionActionsTest extends AbstractLayoutTest {
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
  // Common
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_selectionActions() throws Exception {
    final ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    setLayout(null);",
            "    {",
            "      JButton button = new JButton('New Button1');",
            "      button.setBounds(70, 27, 83, 22);",
            "      add(button);",
            "    }",
            "    {",
            "      JButton button = new JButton('New Button');",
            "      button.setBounds(41, 129, 134, 84);",
            "      add(button);",
            "    }",
            "    {",
            "      JPanel subPanel = new JPanel();",
            "      subPanel.setLayout(null);",
            "      subPanel.setBounds(286, 135, 134, 120);",
            "      add(subPanel);",
            "      {",
            "        JLabel label = new JLabel('New Label');",
            "        label.setBounds(41, 53, 51, 13);",
            "        subPanel.add(label);",
            "      }",
            "    }",
            "  }",
            "}");
    setupSelectionActions(panel);
    ComponentInfo button = panel.getChildrenComponents().get(0);
    ContainerInfo subPanel = (ContainerInfo) panel.getChildrenComponents().get(2);
    ComponentInfo label = subPanel.getChildrenComponents().get(0);
    panel.refresh();
    // prepare "button" selection
    List<ObjectInfo> selectedObjects = Lists.newArrayList();
    selectedObjects.add(button);
    // prepare actions
    List<Object> actions = Lists.newArrayList();
    panel.getBroadcastObject().addSelectionActions(selectedObjects, actions);
    // check actions
    assertEquals(17, actions.size()); // 12 action's, 5 separator's
    assertNotNull(findAction(actions, "Align left edges"));
    assertNotNull(findAction(actions, "Align horizontal centers"));
    assertNotNull(findAction(actions, "Align right edges"));
    assertNotNull(findAction(actions, "Align top edges"));
    assertNotNull(findAction(actions, "Align vertical centers"));
    assertNotNull(findAction(actions, "Align bottom edges"));
    assertNotNull(findAction(actions, "Replicate width"));
    assertNotNull(findAction(actions, "Replicate height"));
    assertNotNull(findAction(actions, "Space equally, horizontally"));
    assertNotNull(findAction(actions, "Space equally, vertically"));
    assertNotNull(findAction(actions, "Center horizontally in window"));
    assertNotNull(findAction(actions, "Center vertically in window"));
    // check enabled
    assertFalse(findAction(actions, "Align left edges").isEnabled());
    assertFalse(findAction(actions, "Align horizontal centers").isEnabled());
    assertFalse(findAction(actions, "Align right edges").isEnabled());
    assertFalse(findAction(actions, "Align top edges").isEnabled());
    assertFalse(findAction(actions, "Align vertical centers").isEnabled());
    assertFalse(findAction(actions, "Align bottom edges").isEnabled());
    assertFalse(findAction(actions, "Replicate width").isEnabled());
    assertFalse(findAction(actions, "Replicate height").isEnabled());
    assertFalse(findAction(actions, "Space equally, horizontally").isEnabled());
    assertFalse(findAction(actions, "Space equally, vertically").isEnabled());
    assertTrue(findAction(actions, "Center horizontally in window").isEnabled());
    assertTrue(findAction(actions, "Center vertically in window").isEnabled());
    // prepare "button panel" selection
    selectedObjects.clear();
    selectedObjects.add(button);
    selectedObjects.add(subPanel);
    // prepare actions
    actions.clear();
    panel.getBroadcastObject().addSelectionActions(selectedObjects, actions);
    //
    assertTrue(findAction(actions, "Align left edges").isEnabled());
    assertTrue(findAction(actions, "Align horizontal centers").isEnabled());
    assertTrue(findAction(actions, "Align right edges").isEnabled());
    assertTrue(findAction(actions, "Align top edges").isEnabled());
    assertTrue(findAction(actions, "Align vertical centers").isEnabled());
    assertTrue(findAction(actions, "Align bottom edges").isEnabled());
    assertTrue(findAction(actions, "Replicate width").isEnabled());
    assertTrue(findAction(actions, "Replicate height").isEnabled());
    assertTrue(findAction(actions, "Space equally, horizontally").isEnabled());
    assertTrue(findAction(actions, "Space equally, vertically").isEnabled());
    assertTrue(findAction(actions, "Center horizontally in window").isEnabled());
    assertTrue(findAction(actions, "Center vertically in window").isEnabled());
    // prepare "button label" selection, they are in different parents, this is error
    selectedObjects.clear();
    selectedObjects.add(button);
    selectedObjects.add(label);
    // prepare actions
    actions.clear();
    panel.getBroadcastObject().addSelectionActions(selectedObjects, actions);
    //
    assertTrue(findAction(actions, "Align left edges").isEnabled());
    assertTrue(findAction(actions, "Align horizontal centers").isEnabled());
    assertTrue(findAction(actions, "Align right edges").isEnabled());
    assertTrue(findAction(actions, "Align top edges").isEnabled());
    assertTrue(findAction(actions, "Align vertical centers").isEnabled());
    assertTrue(findAction(actions, "Align bottom edges").isEnabled());
    assertTrue(findAction(actions, "Replicate width").isEnabled());
    assertTrue(findAction(actions, "Replicate height").isEnabled());
    assertFalse(findAction(actions, "Space equally, horizontally").isEnabled());
    assertFalse(findAction(actions, "Space equally, vertically").isEnabled());
    assertTrue(findAction(actions, "Center horizontally in window").isEnabled());
    assertTrue(findAction(actions, "Center vertically in window").isEnabled());
    // check wrong selection
    selectedObjects.clear();
    selectedObjects.add(button);
    selectedObjects.add(label);
    selectedObjects.add(new TestObjectInfo());
    // prepare actions
    actions.clear();
    panel.getBroadcastObject().addSelectionActions(selectedObjects, actions);
    assertTrue(actions.isEmpty());
  }

  /**
   * First select object is target. All others - should be aligned. However we can not align "this"
   * {@link JPanel}. It seems that right now situation is even worse - we try to change bounds of
   * component using "layout" of <em>target</em>. This is incorrect, you should ask container/layout
   * of <em>component</em> to set required bounds.
   */
  public void test_rootComponentInSelection() throws Exception {
    String[] lines =
        {
            "class Test extends JPanel {",
            "  Test() {",
            "    setLayout(null);",
            "    {",
            "      JButton button = new JButton();",
            "      button.setBounds(10, 10, 150, 30);",
            "      add(button);",
            "    }",
            "  }",
            "}"};
    final ContainerInfo panel = parseContainer(lines);
    setupSelectionActions(panel);
    panel.refresh();
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // bad selection: "button" and "panel" itself
    List<ObjectInfo> selectedObjects = Lists.newArrayList();
    selectedObjects.add(button);
    selectedObjects.add(panel);
    // prepare actions
    List<Object> actions = Lists.newArrayList();
    panel.getBroadcastObject().addSelectionActions(selectedObjects, actions);
    // bad selection, so no actions
    assertThat(actions).isEmpty();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Horizontal
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * One parent selection objects, order: Bottom-Up.
   */
  public void test_align_left_edges_1a() throws Exception {
    check_align_horizontal(new String[]{
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton(\"000\");",
        "      button.setBounds(20, 10, 100, 20);",
        "      add(button);",
        "    }",
        "    {",
        "      JButton button = new JButton(\"111\");",
        "      button.setBounds(20, 50, 150, 30);",
        "      add(button);",
        "    }",
        "  }",
        "}"}, "Align left edges", true);
  }

  /**
   * One parent selection objects, order: Top-Down.
   */
  public void test_align_left_edges_1b() throws Exception {
    check_align_horizontal(new String[]{
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton(\"000\");",
        "      button.setBounds(10, 10, 100, 20);",
        "      add(button);",
        "    }",
        "    {",
        "      JButton button = new JButton(\"111\");",
        "      button.setBounds(10, 50, 150, 30);",
        "      add(button);",
        "    }",
        "  }",
        "}"}, "Align left edges", false);
  }

  /**
   * Two parent's selection objects, order: Bottom-Up.
   */
  public void test_align_left_edges_2a() throws Exception {
    check_align_horizontal2(new String[]{
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton(\"000\");",
        "      button.setBounds(30, 10, 100, 20);",
        "      add(button);",
        "    }",
        "    {",
        "      JPanel subPanel = new JPanel();",
        "      subPanel.setLayout(null);",
        "      subPanel.setBounds(5, 40, 300, 100);",
        "      add(subPanel);",
        "      {",
        "        JButton button = new JButton(\"111\");",
        "        button.setBounds(25, 55, 150, 30);",
        "        subPanel.add(button);",
        "      }",
        "    }",
        "  }",
        "}"}, "Align left edges", true);
  }

  /**
   * Two parent's selection objects, order: Top-Down.
   */
  public void test_align_left_edges_2b() throws Exception {
    check_align_horizontal2(new String[]{
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton(\"000\");",
        "      button.setBounds(10, 10, 100, 20);",
        "      add(button);",
        "    }",
        "    {",
        "      JPanel subPanel = new JPanel();",
        "      subPanel.setLayout(null);",
        "      subPanel.setBounds(5, 40, 300, 100);",
        "      add(subPanel);",
        "      {",
        "        JButton button = new JButton(\"111\");",
        "        button.setBounds(5, 55, 150, 30);",
        "        subPanel.add(button);",
        "      }",
        "    }",
        "  }",
        "}"}, "Align left edges", false);
  }

  /**
   * One parent selection objects, order: Bottom-Up.
   */
  public void test_align_right_edges_1a() throws Exception {
    check_align_horizontal(new String[]{
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton(\"000\");",
        "      button.setBounds(70, 10, 100, 20);",
        "      add(button);",
        "    }",
        "    {",
        "      JButton button = new JButton(\"111\");",
        "      button.setBounds(20, 50, 150, 30);",
        "      add(button);",
        "    }",
        "  }",
        "}"}, "Align right edges", true);
  }

  /**
   * One parent selection objects, order: Top-Down.
   */
  public void test_align_right_edges_1b() throws Exception {
    check_align_horizontal(new String[]{
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton(\"000\");",
        "      button.setBounds(10, 10, 100, 20);",
        "      add(button);",
        "    }",
        "    {",
        "      JButton button = new JButton(\"111\");",
        "      button.setBounds(-40, 50, 150, 30);",
        "      add(button);",
        "    }",
        "  }",
        "}"}, "Align right edges", false);
  }

  /**
   * Two parent's selection objects, order: Bottom-Up.
   */
  public void test_align_right_edges_2a() throws Exception {
    check_align_horizontal2(new String[]{
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton(\"000\");",
        "      button.setBounds(80, 10, 100, 20);",
        "      add(button);",
        "    }",
        "    {",
        "      JPanel subPanel = new JPanel();",
        "      subPanel.setLayout(null);",
        "      subPanel.setBounds(5, 40, 300, 100);",
        "      add(subPanel);",
        "      {",
        "        JButton button = new JButton(\"111\");",
        "        button.setBounds(25, 55, 150, 30);",
        "        subPanel.add(button);",
        "      }",
        "    }",
        "  }",
        "}"}, "Align right edges", true);
  }

  /**
   * Two parent's selection objects, order: Top-Down.
   */
  public void test_align_right_edges_2b() throws Exception {
    check_align_horizontal2(new String[]{
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton(\"000\");",
        "      button.setBounds(10, 10, 100, 20);",
        "      add(button);",
        "    }",
        "    {",
        "      JPanel subPanel = new JPanel();",
        "      subPanel.setLayout(null);",
        "      subPanel.setBounds(5, 40, 300, 100);",
        "      add(subPanel);",
        "      {",
        "        JButton button = new JButton(\"111\");",
        "        button.setBounds(-45, 55, 150, 30);",
        "        subPanel.add(button);",
        "      }",
        "    }",
        "  }",
        "}"}, "Align right edges", false);
  }

  /**
   * One parent selection objects, order: Bottom-Up.
   */
  public void test_align_horizontal_centers_1a() throws Exception {
    check_align_horizontal(new String[]{
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton(\"000\");",
        "      button.setBounds(45, 10, 100, 20);",
        "      add(button);",
        "    }",
        "    {",
        "      JButton button = new JButton(\"111\");",
        "      button.setBounds(20, 50, 150, 30);",
        "      add(button);",
        "    }",
        "  }",
        "}"}, "Align horizontal centers", true);
  }

  /**
   * One parent selection objects, order: Top-Down.
   */
  public void test_align_horizontal_centers_1b() throws Exception {
    check_align_horizontal(new String[]{
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton(\"000\");",
        "      button.setBounds(10, 10, 100, 20);",
        "      add(button);",
        "    }",
        "    {",
        "      JButton button = new JButton(\"111\");",
        "      button.setBounds(-15, 50, 150, 30);",
        "      add(button);",
        "    }",
        "  }",
        "}"}, "Align horizontal centers", false);
  }

  /**
   * Two parent's selection objects, order: Bottom-Up.
   */
  public void test_align_horizontal_centers_2a() throws Exception {
    check_align_horizontal2(new String[]{
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton(\"000\");",
        "      button.setBounds(55, 10, 100, 20);",
        "      add(button);",
        "    }",
        "    {",
        "      JPanel subPanel = new JPanel();",
        "      subPanel.setLayout(null);",
        "      subPanel.setBounds(5, 40, 300, 100);",
        "      add(subPanel);",
        "      {",
        "        JButton button = new JButton(\"111\");",
        "        button.setBounds(25, 55, 150, 30);",
        "        subPanel.add(button);",
        "      }",
        "    }",
        "  }",
        "}"}, "Align horizontal centers", true);
  }

  /**
   * Two parent's selection objects, order: Top-Down.
   */
  public void test_align_horizontal_centers_2b() throws Exception {
    check_align_horizontal2(new String[]{
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton(\"000\");",
        "      button.setBounds(10, 10, 100, 20);",
        "      add(button);",
        "    }",
        "    {",
        "      JPanel subPanel = new JPanel();",
        "      subPanel.setLayout(null);",
        "      subPanel.setBounds(5, 40, 300, 100);",
        "      add(subPanel);",
        "      {",
        "        JButton button = new JButton(\"111\");",
        "        button.setBounds(-20, 55, 150, 30);",
        "        subPanel.add(button);",
        "      }",
        "    }",
        "  }",
        "}"}, "Align horizontal centers", false);
  }

  private void check_align_horizontal(String[] expectedSource, String action, boolean toUp)
      throws Exception {
    check_align(new String[]{
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton(\"000\");",
        "      button.setBounds(10, 10, 100, 20);",
        "      add(button);",
        "    }",
        "    {",
        "      JButton button = new JButton(\"111\");",
        "      button.setBounds(20, 50, 150, 30);",
        "      add(button);",
        "    }",
        "  }",
        "}"}, expectedSource, action, toUp);
  }

  private void check_align_horizontal2(String[] expectedSource, String action, boolean toUp)
      throws Exception {
    check_align2(new String[]{
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton(\"000\");",
        "      button.setBounds(10, 10, 100, 20);",
        "      add(button);",
        "    }",
        "    {",
        "      JPanel subPanel = new JPanel();",
        "      subPanel.setLayout(null);",
        "      subPanel.setBounds(5, 40, 300, 100);",
        "      add(subPanel);",
        "      {",
        "        JButton button = new JButton(\"111\");",
        "        button.setBounds(25, 55, 150, 30);",
        "        subPanel.add(button);",
        "      }",
        "    }",
        "  }",
        "}"}, expectedSource, action, toUp);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Vertical
  //
  ////////////////////////////////////////////////////////////////////////////	
  /**
   * One parent selection objects, order: Bottom-Up.
   */
  public void test_align_top_edges_1a() throws Exception {
    check_align_vertical(new String[]{
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton(\"000\");",
        "      button.setBounds(10, 100, 50, 40);",
        "      add(button);",
        "    }",
        "    {",
        "      JButton button = new JButton(\"111\");",
        "      button.setBounds(70, 100, 100, 80);",
        "      add(button);",
        "    }",
        "  }",
        "}"}, "Align top edges", true);
  }

  /**
   * One parent selection objects, order: Top-Down.
   */
  public void test_align_top_edges_1b() throws Exception {
    check_align_vertical(new String[]{
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton(\"000\");",
        "      button.setBounds(10, 10, 50, 40);",
        "      add(button);",
        "    }",
        "    {",
        "      JButton button = new JButton(\"111\");",
        "      button.setBounds(70, 10, 100, 80);",
        "      add(button);",
        "    }",
        "  }",
        "}"}, "Align top edges", false);
  }

  /**
   * Two parent's selection objects, order: Bottom-Up.
   */
  public void test_align_top_edges_2a() throws Exception {
    check_align_vertical2(new String[]{
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton(\"000\");",
        "      button.setBounds(10, 100, 50, 40);",
        "      add(button);",
        "    }",
        "    {",
        "      JPanel subPanel = new JPanel();",
        "      subPanel.setLayout(null);",
        "      subPanel.setBounds(65, 5, 300, 300);",
        "      add(subPanel);",
        "      {",
        "        JButton button = new JButton(\"111\");",
        "        button.setBounds(5, 95, 100, 80);",
        "        subPanel.add(button);",
        "      }",
        "    }",
        "  }",
        "}"}, "Align top edges", true);
  }

  /**
   * Two parent's selection objects, order: Top-Down.
   */
  public void test_align_top_edges_2b() throws Exception {
    check_align_vertical2(new String[]{
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton(\"000\");",
        "      button.setBounds(10, 10, 50, 40);",
        "      add(button);",
        "    }",
        "    {",
        "      JPanel subPanel = new JPanel();",
        "      subPanel.setLayout(null);",
        "      subPanel.setBounds(65, 5, 300, 300);",
        "      add(subPanel);",
        "      {",
        "        JButton button = new JButton(\"111\");",
        "        button.setBounds(5, 5, 100, 80);",
        "        subPanel.add(button);",
        "      }",
        "    }",
        "  }",
        "}"}, "Align top edges", false);
  }

  /**
   * One parent selection objects, order: Bottom-Up.
   */
  public void test_align_bottom_edges_1a() throws Exception {
    // y2:100 + h2:80 - h1:40 = y1:140
    check_align_vertical(new String[]{
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton(\"000\");",
        "      button.setBounds(10, 140, 50, 40);",
        "      add(button);",
        "    }",
        "    {",
        "      JButton button = new JButton(\"111\");",
        "      button.setBounds(70, 100, 100, 80);",
        "      add(button);",
        "    }",
        "  }",
        "}"}, "Align bottom edges", true);
  }

  /**
   * One parent selection objects, order: Top-Down.
   */
  public void test_align_bottom_edges_1b() throws Exception {
    // y2:100 + h2:80 - h1:40 = y1:140
    check_align_vertical(new String[]{
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton(\"000\");",
        "      button.setBounds(10, 10, 50, 40);",
        "      add(button);",
        "    }",
        "    {",
        "      JButton button = new JButton(\"111\");",
        "      button.setBounds(70, -30, 100, 80);",
        "      add(button);",
        "    }",
        "  }",
        "}"}, "Align bottom edges", false);
  }

  /**
   * Two parent's selection objects, order: Bottom-Up.
   */
  public void test_align_bottom_edges_2a() throws Exception {
    check_align_vertical2(new String[]{
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton(\"000\");",
        "      button.setBounds(10, 140, 50, 40);",
        "      add(button);",
        "    }",
        "    {",
        "      JPanel subPanel = new JPanel();",
        "      subPanel.setLayout(null);",
        "      subPanel.setBounds(65, 5, 300, 300);",
        "      add(subPanel);",
        "      {",
        "        JButton button = new JButton(\"111\");",
        "        button.setBounds(5, 95, 100, 80);",
        "        subPanel.add(button);",
        "      }",
        "    }",
        "  }",
        "}"}, "Align bottom edges", true);
  }

  /**
   * Two parent's selection objects, order: Top-Down.
   */
  public void test_align_bottom_edges_2b() throws Exception {
    check_align_vertical2(new String[]{
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton(\"000\");",
        "      button.setBounds(10, 10, 50, 40);",
        "      add(button);",
        "    }",
        "    {",
        "      JPanel subPanel = new JPanel();",
        "      subPanel.setLayout(null);",
        "      subPanel.setBounds(65, 5, 300, 300);",
        "      add(subPanel);",
        "      {",
        "        JButton button = new JButton(\"111\");",
        "        button.setBounds(5, -35, 100, 80);",
        "        subPanel.add(button);",
        "      }",
        "    }",
        "  }",
        "}"}, "Align bottom edges", false);
  }

  /**
   * One parent selection objects, order: Bottom-Up.
   */
  public void test_align_vertical_centers_1a() throws Exception {
    // y2:100 + (h2:80 / 2) - (h1:40 / 2) = y1:120
    check_align_vertical(new String[]{
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton(\"000\");",
        "      button.setBounds(10, 120, 50, 40);",
        "      add(button);",
        "    }",
        "    {",
        "      JButton button = new JButton(\"111\");",
        "      button.setBounds(70, 100, 100, 80);",
        "      add(button);",
        "    }",
        "  }",
        "}"}, "Align vertical centers", true);
  }

  /**
   * One parent selection objects, order: Top-Down.
   */
  public void test_align_vertical_centers_1b() throws Exception {
    // y2:100 + (h2:80 / 2) - (h1:40 / 2) = y1:120
    check_align_vertical(new String[]{
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton(\"000\");",
        "      button.setBounds(10, 10, 50, 40);",
        "      add(button);",
        "    }",
        "    {",
        "      JButton button = new JButton(\"111\");",
        "      button.setBounds(70, -10, 100, 80);",
        "      add(button);",
        "    }",
        "  }",
        "}"}, "Align vertical centers", false);
  }

  /**
   * Two parent's selection objects, order: Bottom-Up.
   */
  public void test_align_vertical_centers_2a() throws Exception {
    check_align_vertical2(new String[]{
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton(\"000\");",
        "      button.setBounds(10, 120, 50, 40);",
        "      add(button);",
        "    }",
        "    {",
        "      JPanel subPanel = new JPanel();",
        "      subPanel.setLayout(null);",
        "      subPanel.setBounds(65, 5, 300, 300);",
        "      add(subPanel);",
        "      {",
        "        JButton button = new JButton(\"111\");",
        "        button.setBounds(5, 95, 100, 80);",
        "        subPanel.add(button);",
        "      }",
        "    }",
        "  }",
        "}"}, "Align vertical centers", true);
  }

  /**
   * Two parent's selection objects, order: Top-Down.
   */
  public void test_align_vertical_centers_2b() throws Exception {
    check_align_vertical2(new String[]{
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton(\"000\");",
        "      button.setBounds(10, 10, 50, 40);",
        "      add(button);",
        "    }",
        "    {",
        "      JPanel subPanel = new JPanel();",
        "      subPanel.setLayout(null);",
        "      subPanel.setBounds(65, 5, 300, 300);",
        "      add(subPanel);",
        "      {",
        "        JButton button = new JButton(\"111\");",
        "        button.setBounds(5, -15, 100, 80);",
        "        subPanel.add(button);",
        "      }",
        "    }",
        "  }",
        "}"}, "Align vertical centers", false);
  }

  private void check_align_vertical(String[] newSource, String action, boolean toUp)
      throws Exception {
    check_align(new String[]{
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton(\"000\");",
        "      button.setBounds(10, 10, 50, 40);",
        "      add(button);",
        "    }",
        "    {",
        "      JButton button = new JButton(\"111\");",
        "      button.setBounds(70, 100, 100, 80);",
        "      add(button);",
        "    }",
        "  }",
        "}"}, newSource, action, toUp);
  }

  private void check_align_vertical2(String[] newSource, String action, boolean toUp)
      throws Exception {
    check_align2(new String[]{
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton(\"000\");",
        "      button.setBounds(10, 10, 50, 40);",
        "      add(button);",
        "    }",
        "    {",
        "      JPanel subPanel = new JPanel();",
        "      subPanel.setLayout(null);",
        "      subPanel.setBounds(65, 5, 300, 300);",
        "      add(subPanel);",
        "      {",
        "        JButton button = new JButton(\"111\");",
        "        button.setBounds(5, 95, 100, 80);",
        "        subPanel.add(button);",
        "      }",
        "    }",
        "  }",
        "}"}, newSource, action, toUp);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Width/Height
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_align_replicate_width() throws Exception {
    check_align(new String[]{
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton(\"000\");",
        "      button.setBounds(10, 10, 50, 40);",
        "      add(button);",
        "    }",
        "    {",
        "      JButton button = new JButton(\"111\");",
        "      button.setBounds(70, 100, 100, 80);",
        "      add(button);",
        "    }",
        "  }",
        "}"}, new String[]{
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton(\"000\");",
        "      button.setBounds(10, 10, 100, 40);",
        "      add(button);",
        "    }",
        "    {",
        "      JButton button = new JButton(\"111\");",
        "      button.setBounds(70, 100, 100, 80);",
        "      add(button);",
        "    }",
        "  }",
        "}"}, "Replicate width", true);
  }

  public void test_align_replicate_height() throws Exception {
    check_align(new String[]{
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton(\"000\");",
        "      button.setBounds(10, 10, 50, 40);",
        "      add(button);",
        "    }",
        "    {",
        "      JButton button = new JButton(\"111\");",
        "      button.setBounds(70, 100, 100, 80);",
        "      add(button);",
        "    }",
        "  }",
        "}"}, new String[]{
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    {",
        "      JButton button = new JButton(\"000\");",
        "      button.setBounds(10, 10, 50, 80);",
        "      add(button);",
        "    }",
        "    {",
        "      JButton button = new JButton(\"111\");",
        "      button.setBounds(70, 100, 100, 80);",
        "      add(button);",
        "    }",
        "  }",
        "}"}, "Replicate height", true);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Space equally
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for two object's without Ctrl pressed.
   */
  public void test_align_space_equally_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    setLayout(null);",
            "    setSize(600, 400);",
            "    {",
            "      JButton button = new JButton('000');",
            "      button.setBounds(30, 90, 100, 70);",
            "      add(button);",
            "    }",
            "    {",
            "      JButton button = new JButton('111');",
            "      button.setBounds(40, 200, 50, 30);",
            "      add(button);",
            "    }",
            "  }",
            "}");
    setupSelectionActions(panel);
    panel.refresh();
    // prepare selection
    List<ObjectInfo> selectedObjects = Lists.newArrayList();
    selectedObjects.add(panel.getChildrenComponents().get(0));
    selectedObjects.add(panel.getChildrenComponents().get(1));
    // prepare actions
    List<Object> actions = Lists.newArrayList();
    panel.getBroadcastObject().addSelectionActions(selectedObjects, actions);
    //
    findAction(actions, "Space equally, horizontally").run();
    findAction(actions, "Space equally, vertically").run();
    //
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    setSize(600, 400);",
        "    {",
        "      JButton button = new JButton('000');",
        "      button.setBounds(150, 100, 100, 70);",
        "      add(button);",
        "    }",
        "    {",
        "      JButton button = new JButton('111');",
        "      button.setBounds(400, 270, 50, 30);",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for three object's with Ctrl pressed.
   */
  public void test_align_space_equally_2() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    setLayout(null);",
            "    setSize(400, 400);",
            "    {",
            "      JButton button = new JButton('000');",
            "      button.setBounds(10, 10, 50, 50);",
            "      add(button);",
            "    }",
            "    {",
            "      JButton button = new JButton('111');",
            "      button.setBounds(90, 90, 60, 60);",
            "      add(button);",
            "    }",
            "    {",
            "      JButton button = new JButton('222');",
            "      button.setBounds(220, 220, 70, 70);",
            "      add(button);",
            "    }",
            "  }",
            "}");
    setupSelectionActions(panel);
    panel.refresh();
    // prepare selection
    List<ObjectInfo> selectedObjects = Lists.newArrayList();
    selectedObjects.add(panel.getChildrenComponents().get(0));
    selectedObjects.add(panel.getChildrenComponents().get(1));
    selectedObjects.add(panel.getChildrenComponents().get(2));
    // prepare actions
    List<Object> actions = Lists.newArrayList();
    panel.getBroadcastObject().addSelectionActions(selectedObjects, actions);
    //
    try {
      ReflectionUtils.setField(DesignerPlugin.class, "m_ctrlPressed", true);
      findAction(actions, "Space equally, horizontally").run();
      findAction(actions, "Space equally, vertically").run();
    } finally {
      ReflectionUtils.setField(DesignerPlugin.class, "m_ctrlPressed", false);
    }
    //
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    setSize(400, 400);",
        "    {",
        "      JButton button = new JButton('000');",
        "      button.setBounds(10, 10, 50, 50);",
        "      add(button);",
        "    }",
        "    {",
        "      JButton button = new JButton('111');",
        "      button.setBounds(110, 110, 60, 60);",
        "      add(button);",
        "    }",
        "    {",
        "      JButton button = new JButton('222');",
        "      button.setBounds(220, 220, 70, 70);",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Center in window
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_align_center_in_window() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    setLayout(null);",
            "    setSize(600, 400);",
            "    {",
            "      JButton button = new JButton('000');",
            "      button.setBounds(10, 10, 60, 40);",
            "      add(button);",
            "    }",
            "  }",
            "}");
    setupSelectionActions(panel);
    panel.refresh();
    // prepare selection
    List<ObjectInfo> selectedObjects = Lists.newArrayList();
    selectedObjects.add(panel.getChildrenComponents().get(0));
    // prepare actions
    List<Object> actions = Lists.newArrayList();
    panel.getBroadcastObject().addSelectionActions(selectedObjects, actions);
    //
    findAction(actions, "Center horizontally in window").run();
    findAction(actions, "Center vertically in window").run();
    //
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    setLayout(null);",
        "    setSize(600, 400);",
        "    {",
        "      JButton button = new JButton('000');",
        "      button.setBounds(270, 180, 60, 40);",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Cases
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Container without layout on the way to root should not cause exceptions.
   */
  public void test_JTabbedPane_onWayToRoot() throws Exception {
    ContainerInfo root =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JTabbedPane tabbed = new JTabbedPane();",
            "    add(tabbed);",
            "    {",
            "      JPanel panel = new JPanel();",
            "      tabbed.addTab('Tab', panel);",
            "      panel.setLayout(null);",
            "      {",
            "        JButton button_1 = new JButton();",
            "        panel.add(button_1);",
            "        button_1.setBounds(10, 10, 100, 50);",
            "      }",
            "      {",
            "        JButton button_2 = new JButton();",
            "        panel.add(button_2);",
            "        button_2.setBounds(20, 100, 100, 50);",
            "      }",
            "    }",
            "  }",
            "}");
    root.refresh();
    //
    // prepare selection
    List<ObjectInfo> selectedObjects = Lists.newArrayList();
    {
      selectedObjects.add(getJavaInfoByName("button_1"));
      selectedObjects.add(getJavaInfoByName("button_2"));
    }
    // prepare actions
    List<Object> actions;
    {
      ContainerInfo panel = getJavaInfoByName("panel");
      setupSelectionActions(panel);
      actions = Lists.newArrayList();
      root.getBroadcastObject().addSelectionActions(selectedObjects, actions);
    }
    //
    findAction(actions, "Align left edges").run();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JTabbedPane tabbed = new JTabbedPane();",
        "    add(tabbed);",
        "    {",
        "      JPanel panel = new JPanel();",
        "      tabbed.addTab('Tab', panel);",
        "      panel.setLayout(null);",
        "      {",
        "        JButton button_1 = new JButton();",
        "        panel.add(button_1);",
        "        button_1.setBounds(10, 10, 100, 50);",
        "      }",
        "      {",
        "        JButton button_2 = new JButton();",
        "        panel.add(button_2);",
        "        button_2.setBounds(10, 100, 100, 50);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private void check_align(String[] initialSource,
      String[] expectedSource,
      String action,
      boolean toUp) throws Exception {
    ContainerInfo panel = parseContainer(initialSource);
    setupSelectionActions(panel);
    panel.refresh();
    // prepare selection
    List<ObjectInfo> selectedObjects = Lists.newArrayList();
    if (toUp) {
      selectedObjects.add(panel.getChildrenComponents().get(1));
      selectedObjects.add(panel.getChildrenComponents().get(0));
    } else {
      selectedObjects.add(panel.getChildrenComponents().get(0));
      selectedObjects.add(panel.getChildrenComponents().get(1));
    }
    // prepare actions
    List<Object> actions = Lists.newArrayList();
    panel.getBroadcastObject().addSelectionActions(selectedObjects, actions);
    //
    findAction(actions, action).run();
    //
    assertEditor(expectedSource);
  }

  private void check_align2(String[] initialSource,
      String[] expectedSource,
      String action,
      boolean toUp) throws Exception {
    ContainerInfo panel = parseContainer(initialSource);
    setupSelectionActions(panel);
    panel.refresh();
    // prepare selection
    List<ObjectInfo> selectedObjects = Lists.newArrayList();
    ContainerInfo subPanel = (ContainerInfo) panel.getChildrenComponents().get(1);
    setupSelectionActions(subPanel);
    if (toUp) {
      selectedObjects.add(subPanel.getChildrenComponents().get(0));
      selectedObjects.add(panel.getChildrenComponents().get(0));
    } else {
      selectedObjects.add(panel.getChildrenComponents().get(0));
      selectedObjects.add(subPanel.getChildrenComponents().get(0));
    }
    // prepare actions
    List<Object> actions = Lists.newArrayList();
    panel.getBroadcastObject().addSelectionActions(selectedObjects, actions);
    //
    findAction(actions, action).run();
    //
    assertEditor(expectedSource);
  }

  private void setupSelectionActions(final ContainerInfo panel) {
    panel.addBroadcastListener(new ObjectEventListener() {
      @Override
      public void addSelectionActions(List<ObjectInfo> objects, List<Object> actions)
          throws Exception {
        new SelectionActionsSupport((AbsoluteLayoutInfo) panel.getLayout()).addAlignmentActions(
            objects,
            actions);
      }
    });
  }
}