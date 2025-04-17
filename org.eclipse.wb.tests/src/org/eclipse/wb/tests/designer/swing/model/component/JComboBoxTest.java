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
