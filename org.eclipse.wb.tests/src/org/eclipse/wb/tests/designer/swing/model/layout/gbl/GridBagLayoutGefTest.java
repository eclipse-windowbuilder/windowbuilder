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
package org.eclipse.wb.tests.designer.swing.model.layout.gbl;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.draw2d.FigureCanvas;
import org.eclipse.wb.internal.gef.graphical.GraphicalViewer;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.component.menu.JPopupMenuInfo;
import org.eclipse.wb.internal.swing.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.GridBagLayoutInfo;
import org.eclipse.wb.os.OSSupport;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;

import org.eclipse.swt.graphics.Image;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test {@link GridBagLayoutInfo} in GEF.
 *
 * @author scheglov_ke
 */
public class GridBagLayoutGefTest extends SwingGefTest {
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
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		AbstractGridBagLayoutTest.configureForTest();
	}

	@Override
	@After
	public void tearDown() throws Exception {
		AbstractGridBagLayoutTest.configureDefaults();
		super.tearDown();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * If we run events loop at moment when model already changed, so {@link GridBagLayoutInfo} is not
	 * active, then headers may be will try to paint. But header needs information from model, which
	 * is not active anymore.
	 */
	@Test
	public void test_replaceWithOther_andPaintDuringThis() throws Exception {
		mainPanel = openContainer("""
				public class Test extends JPanel {
					public Test() {
						GridBagLayout gridBagLayout = new GridBagLayout();
						gridBagLayout.columnWeights = new double[]{1.0};
						gridBagLayout.rowWeights = new double[]{1.0};
						setLayout(gridBagLayout);
						{
							JButton button = new JButton("New JButton");
							add(button);
						}
					}
				}""");
		button = getJavaInfoByName("button");
		canvas.select(button);
		waitEventLoop(0);
		// emulate paint loop during edit
		// Mitin implemented Swing correctly, using AWT thread, but this requires SWT event loop
		ExecutionUtils.runAsync(new RunnableEx() {
			@Override
			public void run() throws Exception {
				ExecutionUtils.runLogUI(new RunnableEx() {
					@Override
					public void run() throws Exception {
						forcePaint(m_headerHorizontal);
						forcePaint(m_headerVertical);
					}

					private void forcePaint(GraphicalViewer viewer) throws Exception {
						FigureCanvas control = viewer.getControl();
						Image image = OSSupport.get().makeShot(control);
						image.dispose();
						// Shell is set to invisible on Linux, causing successive tests to fail...
						control.getShell().setVisible(true);
					}
				});
			}
		});
		// replace layout
		LayoutInfo layout = createJavaInfo("java.awt.FlowLayout");
		mainPanel.setLayout(layout);
	}

	/**
	 * {@link JPopupMenuInfo} is not managed by {@link LayoutInfo}.
	 */
	@Test
	public void test_JPopupMenu_select() throws Exception {
		openContainer("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new GridBagLayout());
						{
							JButton button = new JButton("button");
							add(button, new GridBagConstraints());
						}
						{
							JPopupMenu popupMenu = new JPopupMenu();
							addPopup(this, popupMenu);
						}
					}
					private static void addPopup(Component component, JPopupMenu popup) {
					}
				}""");
		ComponentInfo popup = getJavaInfoByName("popupMenu");
		//
		canvas.select(popup);
		// no exceptions expected
	}

	/**
	 * Test for dropping {@link JPopupMenuInfo}.
	 */
	@Test
	public void test_JPopupMenu_drop() throws Exception {
		mainPanel = openContainer("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new GridBagLayout());
					}
					private static void addPopup(Component component, JPopupMenu popup) {
					}
				}""");
		//
		ComponentInfo newPopup = loadCreationTool("javax.swing.JPopupMenu");
		{
			canvas.moveTo(mainPanel, 100, 100);
			canvas.assertFeedbacks(canvas.getTargetPredicate(mainPanel));
			canvas.click();
		}
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						{
							JPopupMenu popupMenu = new JPopupMenu();
							addPopup(this, popupMenu);
						}
						setLayout(new GridBagLayout());
					}
					private static void addPopup(Component component, JPopupMenu popup) {
					}
				}""");
		canvas.assertPrimarySelected(newPopup);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// CREATE
	//
	////////////////////////////////////////////////////////////////////////////
	ContainerInfo mainPanel;
	ContainerInfo panel_1;
	ComponentInfo button;

	@Test
	public void test_CREATE_inTree_empty() throws Exception {
		mainPanel = openContainer("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new GridBagLayout());
					}
				}""");
		// create JButton
		loadCreationTool("javax.swing.JButton", "empty");
		tree.moveOn(mainPanel);
		tree.assertCommandNotNull();
		tree.click();
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new GridBagLayout());
						{
							JButton button = new JButton();
							GridBagConstraints gbc = new GridBagConstraints();
							gbc.gridx = 0;
							gbc.gridy = 0;
							add(button, gbc);
						}
					}
				}""");
	}

	@Test
	public void test_DELETE_afterSelect() throws Exception {
		final ContainerInfo panel = openContainer("""
				public class Test extends JPanel {
					public Test() {
						setSize(50, 50);
						GridBagLayout gridBagLayout = new GridBagLayout();
						setLayout(gridBagLayout);

						JPanel panel = new JPanel();
						GridBagConstraints gbc_panel = new GridBagConstraints();
						gbc_panel.ipady = 10;
						gbc_panel.ipadx = 10;
						gbc_panel.fill = GridBagConstraints.BOTH;
						gbc_panel.gridx = 0;
						gbc_panel.gridy = 0;
						add(panel, gbc_panel);
						panel.setLayout(null);

						JPanel panel_1 = new JPanel();
						GridBagConstraints gbc_panel_1 = new GridBagConstraints();
						gbc_panel_1.ipady = 10;
						gbc_panel_1.ipadx = 10;
						gbc_panel_1.fill = GridBagConstraints.BOTH;
						gbc_panel_1.gridx = 1;
						gbc_panel_1.gridy = 1;
						add(panel_1, gbc_panel_1);
						panel_1.setLayout(null);
					}
				}
				""");
		panel.refresh();
		GridBagLayoutInfo layout = (GridBagLayoutInfo) panel.getLayout();
		assertEquals(2, layout.getColumns().size());
		assertEquals(2, layout.getRows().size());
		// do select
		canvas.select(panel.getChildrenComponents().get(1));
		// do delete
		panel.getChildrenComponents().get(1).delete();
		// check result
		assertEquals(1, layout.getColumns().size());
		assertEquals(1, layout.getRows().size());
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setSize(50, 50);
						GridBagLayout gridBagLayout = new GridBagLayout();
						setLayout(gridBagLayout);

						JPanel panel = new JPanel();
						GridBagConstraints gbc_panel = new GridBagConstraints();
						gbc_panel.ipady = 10;
						gbc_panel.ipadx = 10;
						gbc_panel.fill = GridBagConstraints.BOTH;
						gbc_panel.gridx = 0;
						gbc_panel.gridy = 0;
						add(panel, gbc_panel);
						panel.setLayout(null);
					}
				}
				""");
	}
}
