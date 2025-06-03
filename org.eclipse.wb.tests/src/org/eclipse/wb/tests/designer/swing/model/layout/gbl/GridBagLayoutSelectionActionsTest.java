/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.swing.model.layout.gbl;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.GridBagLayoutInfo;

import org.eclipse.jface.action.IAction;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Test for {@link GridBagLayoutInfo} selection action's.
 *
 * @author lobas_av
 */
public class GridBagLayoutSelectionActionsTest extends AbstractGridBagLayoutTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_selectionActions() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  public Test() {",
						"    GridBagLayout gridBagLayout = new GridBagLayout();",
						"    gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};",
						"    gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0};",
						"    gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};",
						"    gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};",
						"    setLayout(gridBagLayout);",
						"    {",
						"      JButton button = new JButton('New button');",
						"      GridBagConstraints gbc = new GridBagConstraints();",
						"      gbc.anchor = GridBagConstraints.WEST;",
						"      gbc.insets = new Insets(8, 0, 5, 25);",
						"      gbc.gridx = 1;",
						"      gbc.gridy = 1;",
						"      add(button, gbc);",
						"    }",
						"    {",
						"      JLabel label = new JLabel('New label');",
						"      GridBagConstraints gbc = new GridBagConstraints();",
						"      gbc.fill = GridBagConstraints.VERTICAL;",
						"      gbc.insets = new Insets(0, 0, 0, 5);",
						"      gbc.gridx = 2;",
						"      gbc.gridy = 3;",
						"      add(label, gbc);",
						"    }",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		ComponentInfo label = panel.getChildrenComponents().get(1);
		//
		panel.refresh();
		// prepare "button" selection
		List<ObjectInfo> selectedObjects = new ArrayList<>();
		selectedObjects.add(button);
		// prepare actions
		List<Object> actions = new ArrayList<>();
		panel.getBroadcastObject().addSelectionActions(selectedObjects, actions);
		// check actions
		assertEquals(16, actions.size()); // 13 action's, 3 separator's
		assertNotNull(findAction(actions, "Left"));
		assertNotNull(findAction(actions, "Center"));
		assertNotNull(findAction(actions, "Right"));
		assertNotNull(findAction(actions, "Fill"));
		assertNotNull(findAction(actions, "Top"));
		assertNotNull(findAction(actions, "Bottom"));
		assertNotNull(findAction(actions, "Baseline"));
		assertNotNull(findAction(actions, "Above baseline"));
		assertNotNull(findAction(actions, "Below baseline"));
		assertNotNull(findAction(actions, "Horizontal grow"));
		assertNotNull(findAction(actions, "Vertical grow"));
		//
		assertTrue(findAction(actions, "Left").isChecked());
		// prepare "label button" selection
		selectedObjects.clear();
		selectedObjects.add(label);
		selectedObjects.add(button);
		// prepare actions
		actions.clear();
		panel.getBroadcastObject().addSelectionActions(selectedObjects, actions);
		// check calculate common properties
		assertFalse(findAction(actions, "Left").isChecked());
	}

	@Test
	public void test_alignmentAction() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  public Test() {",
						"    GridBagLayout gridBagLayout = new GridBagLayout();",
						"    gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};",
						"    gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0};",
						"    gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};",
						"    gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};",
						"    setLayout(gridBagLayout);",
						"    {",
						"      JButton button = new JButton('New button');",
						"      GridBagConstraints gbc = new GridBagConstraints();",
						"      gbc.anchor = GridBagConstraints.WEST;",
						"      gbc.insets = new Insets(8, 0, 5, 25);",
						"      gbc.gridx = 1;",
						"      gbc.gridy = 1;",
						"      add(button, gbc);",
						"    }",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		//
		panel.refresh();
		// prepare "button" selection
		List<ObjectInfo> selectedObjects = new ArrayList<>();
		selectedObjects.add(button);
		// prepare actions
		List<Object> actions = new ArrayList<>();
		panel.getBroadcastObject().addSelectionActions(selectedObjects, actions);
		//
		IAction action = findAction(actions, "Right");
		action.setChecked(true);
		action.run();
		//
		assertEditor(
				"class Test extends JPanel {",
				"  public Test() {",
				"    GridBagLayout gridBagLayout = new GridBagLayout();",
				"    gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};",
				"    gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0};",
				"    gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};",
				"    gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};",
				"    setLayout(gridBagLayout);",
				"    {",
				"      JButton button = new JButton('New button');",
				"      GridBagConstraints gbc = new GridBagConstraints();",
				"      gbc.anchor = GridBagConstraints.EAST;",
				"      gbc.insets = new Insets(8, 0, 5, 25);",
				"      gbc.gridx = 1;",
				"      gbc.gridy = 1;",
				"      add(button, gbc);",
				"    }",
				"  }",
				"}");
	}
}