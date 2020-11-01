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
package org.eclipse.wb.tests.designer.swing.model.layout.FormLayout;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.FormLayoutInfo;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.eclipse.jface.action.IAction;

import java.util.ArrayList;
import java.util.List;

/**
 * Test for {@link FormLayoutInfo} selection action's.
 *
 * @author lobas_av
 */
public class FormLayoutSelectionActionsTest extends AbstractFormLayoutTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_selectionActions() throws Exception {
    ContainerInfo panel = parseContainer(
        "import com.jgoodies.forms.layout.*;",
        "import com.jgoodies.forms.factories.*;",
        "class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        ColumnSpec.decode('default:grow'),},",
        "      new RowSpec[] {",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button, '6, 6');",
        "    }",
        "    {",
        "      JTextField textField = new JTextField();",
        "      add(textField, '12, 12, left, default');",
        "    }",
        "  }",
        "}");
    //
    panel.refresh();
    //
    ComponentInfo button = panel.getChildrenComponents().get(0);
    ComponentInfo text = panel.getChildrenComponents().get(1);
    // prepare "text" selection
    List<ObjectInfo> selectedObjects = new ArrayList<>();
    selectedObjects.add(text);
    // prepare actions
    List<Object> actions = new ArrayList<>();
    panel.getBroadcastObject().addSelectionActions(selectedObjects, actions);
    // check actions
    assertEquals(12, actions.size()); // 10 action's, 2 separator's
    assertNotNull(findAction(actions, "Left"));
    assertNotNull(findAction(actions, "Center"));
    assertNotNull(findAction(actions, "Right"));
    assertNotNull(findAction(actions, "Fill"));
    assertNotNull(findAction(actions, "Top"));
    assertNotNull(findAction(actions, "Bottom"));
    assertNotNull(findAction(actions, "Default"));
    //
    assertTrue(findAction(actions, "Left").isChecked());
    // prepare "button text" selection
    selectedObjects.clear();
    selectedObjects.add(button);
    selectedObjects.add(text);
    // prepare actions
    actions.clear();
    panel.getBroadcastObject().addSelectionActions(selectedObjects, actions);
    // check calculate common properties
    IAction leftAction = findAction(actions, "Left");
    assertFalse(leftAction.isChecked());
    //
    leftAction.setChecked(true);
    leftAction.run();
    //
    assertEditor(
        "import com.jgoodies.forms.layout.*;",
        "import com.jgoodies.forms.factories.*;",
        "class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new FormLayout(new ColumnSpec[] {",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        FormSpecs.DEFAULT_COLSPEC,",
        "        FormSpecs.RELATED_GAP_COLSPEC,",
        "        ColumnSpec.decode('default:grow'),},",
        "      new RowSpec[] {",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,",
        "        FormSpecs.RELATED_GAP_ROWSPEC,",
        "        FormSpecs.DEFAULT_ROWSPEC,}));",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button, '6, 6, left, default');",
        "    }",
        "    {",
        "      JTextField textField = new JTextField();",
        "      add(textField, '12, 12, left, default');",
        "    }",
        "  }",
        "}");
  }
}