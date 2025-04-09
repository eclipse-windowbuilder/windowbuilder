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
package org.eclipse.wb.tests.designer.editor.validator;

import org.eclipse.wb.core.gef.policy.validator.BorderOfChildLayoutRequestValidator;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.jface.action.IAction;

import org.junit.Test;

/**
 * Test {@link BorderOfChildLayoutRequestValidator}.
 *
 * @author scheglov_ke
 */
public class BorderOfChildLayoutRequestValidatorTest extends SwingGefTest {
	private ContainerInfo mainPanel;
	private ContainerInfo panel_1;
	private ComponentInfo button;

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
	// CREATE
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_CREATE_inside() throws Exception {
		prepare_CREATE();
		// begin creating JButton
		canvas.select(mainPanel);
		loadCreationTool("javax.swing.JButton", "empty");
		// move on "panel_1": inner part, so not "transparent"
		canvas.moveTo(panel_1, 10, 10);
		canvas.click();
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new GridLayout());
						{
							JPanel panel_1 = new JPanel();
							add(panel_1);
							{
								JButton button = new JButton();
								panel_1.add(button);
							}
						}
					}
				}""");
	}

	@Test
	public void test_CREATE_targetIsNotChildOfSelected_onBorder() throws Exception {
		prepare_CREATE();
		// begin creating JButton
		canvas.select(panel_1);
		loadCreationTool("javax.swing.JButton", "empty");
		// move on border "panel_1": but it is not "transparent", because _it_ is selected, not its parent
		canvas.moveTo(panel_1, 10, 10);
		canvas.click();
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new GridLayout());
						{
							JPanel panel_1 = new JPanel();
							add(panel_1);
							{
								JButton button = new JButton();
								panel_1.add(button);
							}
						}
					}
				}""");
	}

	@Test
	public void test_CREATE_targetIsChildOfSelected_onBorder() throws Exception {
		prepare_CREATE();
		// begin creating JButton
		canvas.select(mainPanel);
		loadCreationTool("javax.swing.JButton", "empty");
		// move on border "panel_1": it is "transparent", because its parent is selected
		canvas.moveTo(panel_1, 1, 10);
		canvas.click();
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new GridLayout());
						{
							JButton button = new JButton();
							add(button);
						}
						{
							JPanel panel_1 = new JPanel();
							add(panel_1);
						}
					}
				}""");
	}

	/**
	 * No selection at all, but "target" marked as always transparent on borders.
	 *
	 * @throws Exception
	 */
	@Test
	public void test_CREATE_targetMarkedAsTransparentOnBorders_noSelection_onBorder()
			throws Exception {
		prepare_CREATE();
		// mark as transparent on borders
		JavaInfoUtils.setParameter(panel_1, "GEF.transparentOnBorders.always", "true");
		// begin creating JButton
		loadCreationTool("javax.swing.JButton", "empty");
		// move on border "panel_1": it is "transparent" always
		canvas.moveTo(panel_1, 1, 10);
		canvas.click();
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new GridLayout());
						{
							JButton button = new JButton();
							add(button);
						}
						{
							JPanel panel_1 = new JPanel();
							add(panel_1);
						}
					}
				}""");
	}

	private void prepare_CREATE() throws Exception {
		mainPanel = openContainer("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new GridLayout());
						{
							JPanel panel_1 = new JPanel();
							add(panel_1);
						}
					}
				}""");
		panel_1 = (ContainerInfo) mainPanel.getChildrenComponents().get(0);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// PASTE
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_PASTE_inside() throws Exception {
		prepare_PASTE();
		// move on "panel": inner part, so not "transparent"
		canvas.select(mainPanel);
		canvas.moveTo(panel_1, 10, 10);
		canvas.click();
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new GridLayout());
						{
							JPanel panel_1 = new JPanel();
							add(panel_1);
							{
								JButton button = new JButton();
								panel_1.add(button);
							}
						}
						{
							JButton button = new JButton();
							add(button);
						}
					}
				}""");
	}

	@Test
	public void test_PASTE_targetIsNotChildOfSelected_onBorder() throws Exception {
		prepare_PASTE();
		// move on border "panel": it is "transparent", because its parent is selected
		canvas.select(panel_1);
		canvas.moveTo(panel_1, 2, 10);
		canvas.click();
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new GridLayout());
						{
							JPanel panel_1 = new JPanel();
							add(panel_1);
							{
								JButton button = new JButton();
								panel_1.add(button);
							}
						}
						{
							JButton button = new JButton();
							add(button);
						}
					}
				}""");
	}

	@Test
	public void test_PASTE_targetIsChildOfSelected_onBorder() throws Exception {
		prepare_PASTE();
		// move on border "panel": it is "transparent", because its parent is selected
		canvas.select(mainPanel);
		canvas.moveTo(panel_1, 2, 10);
		canvas.click();
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new GridLayout());
						{
							JButton button = new JButton();
							add(button);
						}
						{
							JPanel panel_1 = new JPanel();
							add(panel_1);
						}
						{
							JButton button = new JButton();
							add(button);
						}
					}
				}""");
	}

	private void prepare_PASTE() throws Exception {
		mainPanel = openContainer("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new GridLayout());
						{
							JPanel panel_1 = new JPanel();
							add(panel_1);
						}
						{
							JButton button = new JButton();
							add(button);
						}
					}
				}""");
		panel_1 = (ContainerInfo) mainPanel.getChildrenComponents().get(0);
		button = mainPanel.getChildrenComponents().get(1);
		// copy "button"
		{
			// select "button"
			canvas.select(button);
			// do copy
			IAction copyAction = getCopyAction();
			assertTrue(copyAction.isEnabled());
			copyAction.run();
		}
		// paste
		{
			IAction pasteAction = getPasteAction();
			assertTrue(pasteAction.isEnabled());
			pasteAction.run();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ADD
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_ADD_inside() throws Exception {
		openContainer("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new GridLayout());
						{
							JPanel panel_1 = new JPanel();
							add(panel_1);
						}
						{
							JButton button = new JButton();
							add(button);
						}
					}
				}""");
		panel_1 = getJavaInfoByName("panel_1");
		button = getJavaInfoByName("button");
		// drag "button"
		canvas.beginDrag(button, 10, 10).dragTo(panel_1, 10, 10);
		canvas.assertEmptyFlowContainerFeedback(panel_1, true);
		canvas.assertCommandNotNull();
		// done drag, so finish ADD
		canvas.endDrag();
		canvas.assertNoFeedbacks();
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new GridLayout());
						{
							JPanel panel_1 = new JPanel();
							add(panel_1);
							{
								JButton button = new JButton();
								panel_1.add(button);
							}
						}
					}
				}""");
	}

	@Test
	public void test_ADD_onBorder() throws Exception {
		mainPanel = openContainer("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new GridLayout());
						{
							JPanel panel_1 = new JPanel();
							add(panel_1);
						}
						{
							JButton button = new JButton();
							add(button);
						}
					}
				}""");
		panel_1 = (ContainerInfo) mainPanel.getChildrenComponents().get(0);
		button = mainPanel.getChildrenComponents().get(1);
		// drag "button"
		canvas.beginDrag(button, 10, 10).dragTo(panel_1, 1, 10);
		canvas.assertFeedbacks(canvas.getLinePredicate(panel_1, PositionConstants.LEFT));
		canvas.assertCommandNotNull();
		// done drag, so finish MOVE
		canvas.endDrag();
		canvas.assertNoFeedbacks();
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new GridLayout());
						{
							JButton button = new JButton();
							add(button);
						}
						{
							JPanel panel_1 = new JPanel();
							add(panel_1);
						}
					}
				}""");
	}
}
