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
package org.eclipse.wb.tests.designer.editor;

import org.eclipse.wb.core.gef.policy.selection.TopSelectionEditPolicy;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Rectangle;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link TopSelectionEditPolicy}.
 *
 * @author scheglov_ke
 */
public class TopSelectionEditPolicyTest extends SwingGefTest {
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
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_resizeBoth() throws Exception {
		ComponentInfo shell = openContainer("""
				// filler filler filler filler filler
				// filler filler filler filler filler
				public class Test extends JPanel {
					public Test() {
					}
				}""");
		// initial size
		{
			Rectangle bounds = shell.getBounds();
			assertEquals(450, bounds.width);
			assertEquals(300, bounds.height);
		}
		// select end resize
		canvas.beginResize(shell, PositionConstants.SOUTH_EAST);
		canvas.dragOn(50, 30).endDrag();
		{
			Rectangle bounds = shell.getBounds();
			assertEquals(500, bounds.width);
			assertEquals(330, bounds.height);
		}
	}

	@Test
	public void test_resizeEast_toNegative() throws Exception {
		ComponentInfo shell = openContainer("""
				// filler filler filler filler filler
				// filler filler filler filler filler
				public class Test extends JPanel {
					public Test() {
					}
				}""");
		// initial size
		{
			Rectangle bounds = shell.getBounds();
			assertEquals(450, bounds.width);
			assertEquals(300, bounds.height);
		}
		// drag so that size is negative, but no exception
		canvas.beginResize(shell, PositionConstants.EAST);
		canvas.dragOn(-455, 0);
		canvas.endDrag();
		// size is reasonable
		{
			Rectangle bounds = shell.getBounds();
			Assertions.assertThat(bounds.width).isGreaterThan(9);
			assertEquals(bounds.height, 300);
		}
	}

	@Test
	public void test_resizeSouth_toNegative() throws Exception {
		ComponentInfo shell = openContainer("""
				// filler filler filler filler filler
				// filler filler filler filler filler
				public class Test extends JPanel {
					public Test() {
					}
				}""");
		// initial size
		{
			Rectangle bounds = shell.getBounds();
			assertEquals(450, bounds.width);
			assertEquals(300, bounds.height);
		}
		// drag so that size is negative, but no exception
		canvas.beginResize(shell, PositionConstants.SOUTH);
		canvas.dragOn(0, -305);
		canvas.endDrag();
		// size is reasonable
		{
			Rectangle bounds = shell.getBounds();
			assertEquals(bounds.width, 450);
			Assertions.assertThat(bounds.height).isGreaterThan(0);
		}
	}
}
