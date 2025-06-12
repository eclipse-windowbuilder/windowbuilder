/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.tests.designer.swing.model.layout.group;

import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;

import org.junit.jupiter.api.Test;

import javax.swing.GroupLayout;

/**
 * Test for {@link GroupLayout}.
 *
 * @author mitin_aa
 */
public class GroupLayoutGefTest extends SwingGefTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Exit zone :-) XXX
	//
	////////////////////////////////////////////////////////////////////////////
	public void _test_exit() throws Exception {
		System.exit(0);
	}

	@Test
	public void test_reparent_while_child_created_after_layout() throws Exception {
		prepareBox(50, 25);
		ContainerInfo panel = openContainer("""
				import javax.swing.GroupLayout.Alignment;
				public class Test extends JPanel {
					public Test() {
						setLayout(null);
						JPanel panel = new JPanel();
						panel.setBounds(50, 50, 200, 200);
						add(panel);
						GroupLayout groupLayout_1_1 = new GroupLayout(panel);
						groupLayout_1_1.setHorizontalGroup(
							groupLayout_1_1.createParallelGroup(Alignment.LEADING)
								.addGap(0, 200, Short.MAX_VALUE)
						);
						groupLayout_1_1.setVerticalGroup(
							groupLayout_1_1.createParallelGroup(Alignment.LEADING)
								.addGap(0, 200, Short.MAX_VALUE)
						);
						panel.setLayout(groupLayout_1_1);
						Box box_1 = new Box();
						box_1.setBounds(0, 0, 60, 30);
						add(box_1);
					}
				}""");
		ContainerInfo panel1 = (ContainerInfo) panel.getChildrenComponents().get(0);
		ComponentInfo box = panel.getChildrenComponents().get(1);
		// do move
		canvas.sideMode();
		canvas.beginMove(box).target(panel1).in(40, 30).drag().endDrag();
		assertEditor("""
				import javax.swing.GroupLayout.Alignment;
				public class Test extends JPanel {
					public Test() {
						setLayout(null);
						JPanel panel = new JPanel();
						panel.setBounds(50, 50, 200, 200);
						add(panel);
						Box box_1 = new Box();
						GroupLayout groupLayout_1_1 = new GroupLayout(panel);
						groupLayout_1_1.setHorizontalGroup(
							groupLayout_1_1.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout_1_1.createSequentialGroup()
									.addGap(40)
									.addComponent(box_1, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
									.addContainerGap(100, Short.MAX_VALUE))
						);
						groupLayout_1_1.setVerticalGroup(
							groupLayout_1_1.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout_1_1.createSequentialGroup()
									.addGap(30)
									.addComponent(box_1, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
									.addContainerGap(140, Short.MAX_VALUE))
						);
						panel.setLayout(groupLayout_1_1);
					}
				}""");
	}
}
