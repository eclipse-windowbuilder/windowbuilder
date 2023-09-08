/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.XML.editor;

import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.internal.core.xml.gef.policy.TopSelectionEditPolicy;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;

import org.eclipse.draw2d.geometry.Rectangle;

import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test for {@link TopSelectionEditPolicy}.
 *
 * @author scheglov_ke
 */
public class TopSelectionEditPolicyTest extends XwtGefTest {
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
		CompositeInfo shell = openEditor("<Shell/>");
		// initial size
		{
			Rectangle bounds = shell.getBounds();
			assertEquals(450, bounds.width);
			assertEquals(300, bounds.height);
		}
		// select end resize
		canvas.beginResize(shell, IPositionConstants.SOUTH_EAST);
		canvas.dragOn(50, 30).endDrag();
		{
			Rectangle bounds = shell.getBounds();
			assertEquals(500, bounds.width);
			assertEquals(330, bounds.height);
		}
	}

	@Test
	@Ignore
	public void test_resizeEast_toNegative() throws Exception {
		CompositeInfo shell = openEditor("<Shell/>");
		// initial size
		{
			Rectangle bounds = shell.getBounds();
			assertEquals(450, bounds.width);
			assertEquals(300, bounds.height);
		}
		// drag so that size is negative, but no exception
		canvas.beginResize(shell, IPositionConstants.EAST);
		canvas.dragOn(-455, 0);
		canvas.endDrag();
		// size is reasonable
		{
			Rectangle bounds = shell.getBounds();
			Assertions.assertThat(bounds.width).isGreaterThan(10);
			Assertions.assertThat(bounds.height).isEqualTo(300);
		}
	}

	@Test
	public void test_resizeSouth_toNegative() throws Exception {
		CompositeInfo shell = openEditor("<Shell/>");
		// initial size
		{
			Rectangle bounds = shell.getBounds();
			assertEquals(450, bounds.width);
			assertEquals(300, bounds.height);
		}
		// drag so that size is negative, but no exception
		canvas.beginResize(shell, IPositionConstants.SOUTH);
		canvas.dragOn(0, -305);
		canvas.endDrag();
		// size is reasonable
		{
			Rectangle bounds = shell.getBounds();
			Assertions.assertThat(bounds.width).isEqualTo(450);
			Assertions.assertThat(bounds.height).isGreaterThan(10);
		}
	}
}
