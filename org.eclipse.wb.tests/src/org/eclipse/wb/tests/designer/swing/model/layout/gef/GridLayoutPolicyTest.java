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
package org.eclipse.wb.tests.designer.swing.model.layout.gef;

import org.eclipse.wb.internal.swing.gef.policy.layout.GridLayoutEditPolicy;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.junit.Test;

/**
 * Test for {@link GridLayoutEditPolicy}.
 *
 * @author scheglov_ke
 */
public class GridLayoutPolicyTest extends AbstractLayoutPolicyTest {
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
	// setLayout
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_setLayout() throws Exception {
		String source = """
				// filler filler filler
				public class Test extends JPanel {
					public Test() {
					}
				}""";
		String source2 = """
				// filler filler filler
				public class Test extends JPanel {
					public Test() {
						setLayout(new GridLayout(1, 0, 0, 0));
					}
				}""";
		check_setLayout(source, "java.awt.GridLayout", source2, 10, 10);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// CREATE
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_CREATE() throws Exception {
		ContainerInfo panel = openContainer("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new GridLayout(0, 3));
					}
				}""");
		//
		loadCreationTool("javax.swing.JButton", "empty");
		canvas.moveTo(panel, 10, 10);
		canvas.click();
		canvas.assertFeedbackFigures(0);
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new GridLayout(0, 3));
						{
							JButton button = new JButton();
							add(button);
						}
					}
				}""");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// MOVE
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_MOVE() throws Exception {
		ContainerInfo panel = openContainer("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new GridLayout(0, 3));
						{
							JButton button = new JButton("Button 1");
							add(button);
						}
						{
							JButton button = new JButton("Button 2");
							add(button);
						}
					}
				}""");
		ComponentInfo button_1 = panel.getChildrenComponents().get(0);
		ComponentInfo button_2 = panel.getChildrenComponents().get(1);
		// move
		canvas.beginDrag(button_2);
		canvas.dragTo(button_1, 10, 0);
		canvas.endDrag();
		canvas.assertNoFeedbackFigures();
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new GridLayout(0, 3));
						{
							JButton button = new JButton("Button 2");
							add(button);
						}
						{
							JButton button = new JButton("Button 1");
							add(button);
						}
					}
				}""");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ADD
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_ADD() throws Exception {
		ContainerInfo panel = openContainer("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new BorderLayout());
						{
							JButton button = new JButton("Button");
							add(button, BorderLayout.NORTH);
						}
						{
							JPanel panel = new JPanel();
							panel.setLayout(new GridLayout(0, 3));
							panel.setBackground(Color.PINK);
							panel.setPreferredSize(new Dimension(0, 150));
							add(panel, BorderLayout.SOUTH);
						}
					}
				}""");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		ComponentInfo inner = panel.getChildrenComponents().get(1);
		//
		canvas.beginDrag(button);
		canvas.dragTo(inner, 10, 10);
		canvas.endDrag();
		canvas.assertNoFeedbackFigures();
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new BorderLayout());
						{
							JPanel panel = new JPanel();
							panel.setLayout(new GridLayout(0, 3));
							panel.setBackground(Color.PINK);
							panel.setPreferredSize(new Dimension(0, 150));
							add(panel, BorderLayout.SOUTH);
							{
								JButton button = new JButton("Button");
								panel.add(button);
							}
						}
					}
				}""");
	}
}
