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
package org.eclipse.wb.tests.designer.core.model.generic;

import org.eclipse.wb.core.gef.policy.layout.ILayoutEditPolicyFactory;
import org.eclipse.wb.gef.tree.TreeEditPart;
import org.eclipse.wb.internal.core.model.nonvisual.FlowContainerGroupInfo;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.junit.Test;

/**
 * Tests for "flow container" support for "layout manager", created by
 * {@link ILayoutEditPolicyFactory}.
 *
 * @author scheglov_ke
 */
public class FlowContainerGroupGefTest extends FlowContainerAbstractGefTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test hierarchy of created TreeEditParts
	 */
	@Test
	public void test_group_hierarchy() throws Exception {
		prepareFlowPanel();
		ContainerInfo mainPanel = openContainer("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new BorderLayout());
						{
							FlowPanel panel = new FlowPanel();
							add(panel);
							{
								JButton buttonA = new JButton();
								panel.add(buttonA);
							}
							{
								JButton buttonB = new JButton();
								panel.add(buttonB);
							}
						}
					}
				}""");
		ContainerInfo panel = (ContainerInfo) mainPanel.getChildrenComponents().get(0);
		FlowContainerGroupInfo group = panel.getChildren(FlowContainerGroupInfo.class).get(0);
		ComponentInfo buttonA = panel.getChildrenComponents().get(0);
		ComponentInfo buttonB = panel.getChildrenComponents().get(1);
		// edit parts
		TreeEditPart panelPart = tree.getEditPart(panel);
		TreeEditPart buttonApart = tree.getEditPart(buttonA);
		TreeEditPart buttonBpart = tree.getEditPart(buttonB);
		TreeEditPart groupPart = tree.getEditPart(group);
		// check parts hierarchy
		assertSame(buttonApart.getParent(), groupPart);
		assertSame(buttonBpart.getParent(), groupPart);
		assertSame(groupPart.getParent(), panelPart);
	}

	/**
	 * Test group object policies
	 */
	@Test
	public void test_group_add() throws Exception {
		prepareFlowPanel();
		ContainerInfo mainPanel = openContainer("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new BorderLayout());
						{
							FlowPanel panel = new FlowPanel();
							add(panel);
							{
								JButton existingButton = new JButton();
								panel.add(existingButton);
							}
						}
						{
							JButton rootButton = new JButton("A");
							add(rootButton, BorderLayout.NORTH);
						}
					}
				}""");
		ContainerInfo panel = (ContainerInfo) mainPanel.getChildrenComponents().get(0);
		ComponentInfo rootButton = mainPanel.getChildrenComponents().get(1);
		FlowContainerGroupInfo group = panel.getChildren(FlowContainerGroupInfo.class).get(0);
		// drab buttonB on FlowPanel
		tree.startDrag(rootButton);
		tree.dragOn(group);
		tree.assertFeedback_on(group);
		tree.assertCommandNotNull();
		// done drag, so finish MOVE
		tree.endDrag();
		tree.assertFeedback_empty();
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new BorderLayout());
						{
							FlowPanel panel = new FlowPanel();
							add(panel);
							{
								JButton existingButton = new JButton();
								panel.add(existingButton);
							}
							{
								JButton rootButton = new JButton("A");
								panel.add(rootButton);
							}
						}
					}
				}""");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void prepareFlowPanel() throws Exception {
		FlowContainerModelTest.prepareFlowPanel_classes();
		setFileContentSrc(
				"test/FlowPanel.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <methods>",
						"    <method name='add'>",
						"      <parameter type='java.awt.Component' child='true'/>",
						"    </method>",
						"  </methods>",
						"  <parameters>",
						"    <parameter name='flowContainer'>true</parameter>",
						"    <parameter name='flowContainer.horizontal'>true</parameter>",
						"    <parameter name='flowContainer.association'>%parent%.add(%child%)</parameter>",
						"    <parameter name='flowContainer.component'>java.awt.Component</parameter>",
						"    <parameter name='flowContainer.reference'>java.awt.Component</parameter>",
						"    <parameter name='flowContainer.group'>Group</parameter>",
						"  </parameters>",
						"</component>"));
		waitForAutoBuild();
	}
}
