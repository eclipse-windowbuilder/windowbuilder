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
package org.eclipse.wb.tests.designer.swing.model.component;

import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.junit.Test;

import javax.swing.JComboBox;

/**
 * Tests for {@link JComboBox} support.
 *
 * @author scheglov_ke
 */
public class JComboBoxTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test that {@link JComboBox#addItem(Object)} is executable.
	 */
	@Test
	public void test_addItem() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JComboBox combo = new JComboBox();",
						"    add(combo);",
						"    combo.addItem('a');",
						"    combo.addItem('b');",
						"    combo.setSelectedIndex(1);",
						"  }",
						"}");
		assertHierarchy(
				"{this: javax.swing.JPanel} {this} {/add(combo)/}",
				"  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
				"  {new: javax.swing.JComboBox} {local-unique: combo} {/new JComboBox()/ /add(combo)/ /combo.addItem('a')/ /combo.addItem('b')/ /combo.setSelectedIndex(1)/}");
		panel.refresh();
		assertNoErrors(panel);
	}
}
