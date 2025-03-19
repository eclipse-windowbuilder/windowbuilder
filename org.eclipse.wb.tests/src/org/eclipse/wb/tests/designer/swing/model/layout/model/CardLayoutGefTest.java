/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.swing.model.layout.model;

import org.eclipse.wb.internal.swing.gef.policy.layout.CardNavigationFigure;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.CardLayoutInfo;
import org.eclipse.wb.tests.designer.swing.model.layout.gef.AbstractLayoutPolicyTest;

import org.junit.Test;

/**
 * Test for {@link CardLayoutInfo} in GEF.
 *
 * @author scheglov_ke
 */
public class CardLayoutGefTest extends AbstractLayoutPolicyTest {
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
	// CREATE on canvas
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_CREATE_onCanvas_empty() throws Exception {
		ContainerInfo panel = openContainer("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new CardLayout());
					}
				}""");
		//
		ComponentInfo newButton = loadCreationTool("javax.swing.JButton");
		canvas.moveTo(panel, 100, 100).click();
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new CardLayout());
						{
							JButton button = new JButton("New button");
							add(button, "%s");
						}
					}
				}""".formatted(CardLayoutTest.getAssociationName(newButton)));
	}

	@Test
	public void test_CREATE_onCanvas_beforeExisting() throws Exception {
		ContainerInfo panel = openContainer("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new CardLayout());
						{
							JButton button_1 = new JButton();
							add(button_1, "111");
						}
					}
				}""");
		ComponentInfo button_1 = getJavaInfoByName("button_1");
		// select "panel", so "button_1" will be transparent on borders
		canvas.select(panel);
		// create new JButton
		ComponentInfo newButton = loadCreationTool("javax.swing.JButton");
		canvas.moveTo(button_1, 2, 100).click();
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new CardLayout());
						{
							JButton button = new JButton("New button");
							add(button, "%s");
						}
						{
							JButton button_1 = new JButton();
							add(button_1, "111");
						}
					}
				}""".formatted(CardLayoutTest.getAssociationName(newButton)));
	}

	@Test
	public void test_CREATE_onCanvas_afterExisting() throws Exception {
		ContainerInfo panel = openContainer("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new CardLayout());
						{
							JButton button_1 = new JButton();
							add(button_1, "111");
						}
					}
				}""");
		ComponentInfo button_1 = getJavaInfoByName("button_1");
		// select "panel", so "button_1" will be transparent on borders
		canvas.select(panel);
		// create new JButton
		ComponentInfo newButton = loadCreationTool("javax.swing.JButton");
		canvas.moveTo(button_1, -2, 100).click();
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new CardLayout());
						{
							JButton button_1 = new JButton();
							add(button_1, "111");
						}
						{
							JButton button = new JButton("New button");
							add(button, "%s");
						}
					}
				}""".formatted(CardLayoutTest.getAssociationName(newButton)));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// CREATE in tree
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_CREATE_inTree_empty() throws Exception {
		ContainerInfo panel = openContainer("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new CardLayout());
					}
				}""");
		//
		ComponentInfo newButton = loadCreationTool("javax.swing.JButton");
		tree.moveOn(panel);
		tree.assertCommandNotNull();
		tree.click();
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new CardLayout());
						{
							JButton button = new JButton("New button");
							add(button, "%s");
						}
					}
				}""".formatted(CardLayoutTest.getAssociationName(newButton)));
	}

	@Test
	public void test_CREATE_inTree_beforeExisting() throws Exception {
		openContainer("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new CardLayout());
						{
							JButton button_1 = new JButton();
							add(button_1, "111");
						}
					}
				}""");
		ComponentInfo button_1 = getJavaInfoByName("button_1");
		// create new JButton
		ComponentInfo newButton = loadCreationTool("javax.swing.JButton");
		tree.moveBefore(button_1).click();
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new CardLayout());
						{
							JButton button = new JButton("New button");
							add(button, "%s");
						}
						{
							JButton button_1 = new JButton();
							add(button_1, "111");
						}
					}
				}""".formatted(CardLayoutTest.getAssociationName(newButton)));
	}

	@Test
	public void test_CREATE_inTree_afterExisting() throws Exception {
		openContainer("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new CardLayout());
						{
							JButton button_1 = new JButton();
							add(button_1, "111");
						}
					}
				}""");
		ComponentInfo button_1 = getJavaInfoByName("button_1");
		// create new JButton
		ComponentInfo newButton = loadCreationTool("javax.swing.JButton");
		tree.moveAfter(button_1).click();
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new CardLayout());
						{
							JButton button_1 = new JButton();
							add(button_1, "111");
						}
						{
							JButton button = new JButton("New button");
							add(button, "%s");
						}
					}
				}""".formatted(CardLayoutTest.getAssociationName(newButton)));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// MOVE in tree
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_MOVE_inTree() throws Exception {
		openContainer("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new CardLayout());
						{
							JButton button_1 = new JButton();
							add(button_1, "111");
						}
						{
							JButton button_2 = new JButton();
							add(button_2, "222");
						}
					}
				}""");
		ComponentInfo button_1 = getJavaInfoByName("button_1");
		ComponentInfo button_2 = getJavaInfoByName("button_2");
		//
		tree.startDrag(button_2).dragBefore(button_1).endDrag();
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new CardLayout());
						{
							JButton button_2 = new JButton();
							add(button_2, "222");
						}
						{
							JButton button_1 = new JButton();
							add(button_1, "111");
						}
					}
				}""");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Navigation
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_navigation_next() throws Exception {
		openContainer("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new CardLayout());
						{
							JButton button_1 = new JButton("111");
							add(button_1, "111");
						}
						{
							JButton button_2 = new JButton("222");
							add(button_2, "222");
						}
						{
							JButton button_3 = new JButton("333");
							add(button_3, "333");
						}
					}
				}""");
		ComponentInfo button_1 = getJavaInfoByName("button_1");
		ComponentInfo button_2 = getJavaInfoByName("button_2");
		ComponentInfo button_3 = getJavaInfoByName("button_3");
		// initially "button_1" visible
		canvas.assertNotNullEditPart(button_1);
		canvas.assertNullEditPart(button_2);
		canvas.assertNullEditPart(button_3);
		// click "next", select "button_2"
		canvas.select(button_1);
		navigateNext(button_1);
		canvas.assertNullEditPart(button_1);
		canvas.assertNotNullEditPart(button_2);
		canvas.assertNullEditPart(button_3);
		// click "next", select "button_3"
		navigateNext(button_2);
		canvas.assertNullEditPart(button_1);
		canvas.assertNullEditPart(button_2);
		canvas.assertNotNullEditPart(button_3);
		// click "next", select "button_1"
		navigateNext(button_3);
		canvas.assertNotNullEditPart(button_1);
		canvas.assertNullEditPart(button_2);
		canvas.assertNullEditPart(button_3);
	}

	@Test
	public void test_navigation_prev() throws Exception {
		openContainer("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new CardLayout());
						{
							JButton button_1 = new JButton("111");
							add(button_1, "111");
						}
						{
							JButton button_2 = new JButton("222");
							add(button_2, "222");
						}
						{
							JButton button_3 = new JButton("333");
							add(button_3, "333");
						}
					}
				}""");
		ComponentInfo button_1 = getJavaInfoByName("button_1");
		ComponentInfo button_2 = getJavaInfoByName("button_2");
		ComponentInfo button_3 = getJavaInfoByName("button_3");
		// initially "button_1" visible
		canvas.assertNotNullEditPart(button_1);
		canvas.assertNullEditPart(button_2);
		canvas.assertNullEditPart(button_3);
		// click "prev", select "button_3"
		canvas.select(button_1);
		navigatePrev(button_1);
		canvas.assertNullEditPart(button_1);
		canvas.assertNullEditPart(button_2);
		canvas.assertNotNullEditPart(button_3);
		// click "prev", select "button_2"
		navigatePrev(button_3);
		canvas.assertNullEditPart(button_1);
		canvas.assertNotNullEditPart(button_2);
		canvas.assertNullEditPart(button_3);
		// click "prev", select "button_1"
		navigatePrev(button_2);
		canvas.assertNotNullEditPart(button_1);
		canvas.assertNullEditPart(button_2);
		canvas.assertNullEditPart(button_3);
	}

	private void navigateNext(ComponentInfo component) {
		canvas.moveTo(component, -3 - 1, 0).click();
	}

	private void navigatePrev(ComponentInfo component) {
		canvas.moveTo(component, -3 - CardNavigationFigure.WIDTH - 1, 0).click();
	}
}
