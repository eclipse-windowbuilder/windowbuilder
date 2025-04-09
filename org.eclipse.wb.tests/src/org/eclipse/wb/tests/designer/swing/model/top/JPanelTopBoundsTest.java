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
package org.eclipse.wb.tests.designer.swing.model.top;

import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.component.top.JPanelTopBoundsSupport;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;

import org.eclipse.core.resources.IResource;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jdt.core.ICompilationUnit;

import org.junit.Test;

import javax.swing.JPanel;

/**
 * Basic tests for {@link JPanelTopBoundsSupport}.
 *
 * @author scheglov_ke
 */
public class JPanelTopBoundsTest extends SwingGefTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Size in properties of {@link IResource}.
	 */
	@Test
	public void test_resize_properties() throws Exception {
		Dimension oldSize = new Dimension(450, 300);
		Dimension newSize = new Dimension(400, 350);
		ICompilationUnit unit = check_resize_JPanel("// no size", oldSize, newSize, "// no size");
		// close editor, reopen and check for size - it should be same as we set
		{
			TestUtils.closeAllEditors();
			openDesign(unit);
			assertEquals(newSize, canvas.getSize(m_contentEditPart));
		}
	}

	/**
	 * Size in setPreferredSize(java.awt.Dimension)
	 */
	@Test
	public void test_resize_setPreferredSize() throws Exception {
		Dimension oldSize = new Dimension(300, 200);
		Dimension newSize = new Dimension(400, 300);
		check_resize_JPanel(
				"setPreferredSize(new Dimension(300, 200));",
				oldSize,
				newSize,
				"setPreferredSize(new Dimension(400, 300));");
	}

	/**
	 * Size in setSize(java.awt.Dimension)
	 */
	@Test
	public void test_resize_setSize_Dimension() throws Exception {
		Dimension oldSize = new Dimension(300, 200);
		Dimension newSize = new Dimension(400, 300);
		check_resize_JPanel(
				"setSize(new Dimension(300, 200));",
				oldSize,
				newSize,
				"setSize(new Dimension(400, 300));");
	}

	/**
	 * Size in setSize(int,int)
	 */
	@Test
	public void test_resize_setSize_ints() throws Exception {
		Dimension oldSize = new Dimension(300, 200);
		Dimension newSize = new Dimension(400, 300);
		check_resize_JPanel("setSize(300, 200);", oldSize, newSize, "setSize(400, 300);");
	}

	/**
	 * The size of the JPanel should be able to exceed the display resolution.
	 */
	@Test
	public void test_resize_veryBig() throws Exception {
		// expand horizontally
		Dimension oldSize = new Dimension(500, 400);
		Dimension newSize = new Dimension(5000, 400);
		check_resize_JPanel(
				"setSize(new Dimension(500, 400));",
				oldSize,
				newSize,
				"setSize(new Dimension(5000, 400));");
		// expand vertically
		oldSize = new Dimension(5000, 400);
		newSize = new Dimension(500, 4000);
		check_resize_JPanel(
				"setSize(new Dimension(5000, 400));",
				oldSize,
				newSize,
				"setSize(new Dimension(500, 4000));");
	}

	/**
	 * Test resize of {@link JPanel}.
	 */
	private ICompilationUnit check_resize_JPanel(String oldSizeLine,
			Dimension oldSize,
			Dimension newSize,
			String newSizeLine) throws Exception {
		ContainerInfo panel = openContainer("""
				public class Test extends JPanel {
					public Test() {
						%s
						add(new JButton("Swing JButton"));
						add(new Button("AWT Button"));
					}
				}""".formatted(oldSizeLine));
		// check size
		assertEquals(oldSize, canvas.getSize(panel));
		waitEventLoop(50);
		// change size
		canvas.beginResize(panel, PositionConstants.EAST);
		canvas.dragTo(panel, newSize.width, 0).endDrag();
		canvas.beginResize(panel, PositionConstants.SOUTH);
		canvas.dragTo(panel, 0, newSize.height).endDrag();
		// check new size
		assertEquals(newSize, canvas.getSize(panel));
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						%s
						add(new JButton("Swing JButton"));
						add(new Button("AWT Button"));
					}
				}""".formatted(newSizeLine));
		//
		return m_lastEditor.getModelUnit();
	}
}
