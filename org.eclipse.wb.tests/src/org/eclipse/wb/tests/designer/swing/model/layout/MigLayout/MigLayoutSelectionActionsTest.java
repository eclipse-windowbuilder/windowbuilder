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
package org.eclipse.wb.tests.designer.swing.model.layout.MigLayout;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.FormLayoutInfo;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.eclipse.jface.action.IAction;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link FormLayoutInfo} selection action's.
 * 
 * @author scheglov_ke
 */
public class MigLayoutSelectionActionsTest extends AbstractMigLayoutTest {
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
  public void test_selectionActions_ALL() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "    add(new JButton(), 'cell 0 0');",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // prepare actions
    List<Object> actions = Lists.newArrayList();
    panel.getBroadcastObject().addSelectionActions(ImmutableList.<ObjectInfo>of(button), actions);
    // check actions: 13 action's, 2 separator's
    assertEquals(15, actions.size());
    assertNotNull(findAction(actions, "Default"));
    assertNotNull(findAction(actions, "Left"));
    assertNotNull(findAction(actions, "Center"));
    assertNotNull(findAction(actions, "Right"));
    assertNotNull(findAction(actions, "Fill"));
    assertNotNull(findAction(actions, "Leading"));
    assertNotNull(findAction(actions, "Trailing"));
    assertNotNull(findAction(actions, "Top"));
    assertNotNull(findAction(actions, "Bottom"));
    assertNotNull(findAction(actions, "Baseline"));
  }

  public void test_selectionActions_noSelection() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "  }",
            "}");
    panel.refresh();
    // prepare actions
    List<Object> actions = Lists.newArrayList();
    panel.getBroadcastObject().addSelectionActions(ImmutableList.<ObjectInfo>of(), actions);
    // no selection, so no actions
    assertThat(actions).isEmpty();
  }

  public void test_selectionActions_invalidSelection() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "  }",
            "}");
    panel.refresh();
    // prepare actions
    List<Object> actions = Lists.newArrayList();
    List<ObjectInfo> selectedObjects = ImmutableList.<ObjectInfo>of(panel.getLayout());
    panel.getBroadcastObject().addSelectionActions(selectedObjects, actions);
    // not Component on MigLayout selected, so no actions
    assertThat(actions).isEmpty();
  }

  public void test_horizontalAlignment() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "    add(new JButton(), 'cell 0 0,alignx leading');",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // prepare actions
    List<Object> actions = Lists.newArrayList();
    panel.getBroadcastObject().addSelectionActions(ImmutableList.<ObjectInfo>of(button), actions);
    // "Leading" should be checked
    {
      IAction leadingAction = findAction(actions, "Leading");
      assertTrue(leadingAction.isChecked());
    }
    // use "Right" action
    {
      IAction rightAction = findAction(actions, "Right");
      rightAction.setChecked(true);
      rightAction.run();
    }
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout());",
        "    add(new JButton(), 'cell 0 0,alignx right');",
        "  }",
        "}");
  }

  public void test_verticalAlignment() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel implements IConstants {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "    add(new JButton(), 'cell 0 0,aligny top');",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // prepare actions
    List<Object> actions = Lists.newArrayList();
    panel.getBroadcastObject().addSelectionActions(ImmutableList.<ObjectInfo>of(button), actions);
    // "Top" should be checked
    {
      IAction topAction = findAction(actions, "Top");
      assertTrue(topAction.isChecked());
    }
    // use "Bottom" action
    {
      IAction bottomAction = findAction(actions, "Bottom");
      bottomAction.setChecked(true);
      bottomAction.run();
    }
    assertEditor(
        "public class Test extends JPanel implements IConstants {",
        "  public Test() {",
        "    setLayout(new MigLayout());",
        "    add(new JButton(), 'cell 0 0,aligny bottom');",
        "  }",
        "}");
  }
}