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
package org.eclipse.wb.tests.designer.core.model.nonvisual;

import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;

import org.junit.Test;

/**
 * Tests for non visual beans.
 *
 * @author scheglov_ke
 */
public class NonVisualBeansGefTest extends SwingGefTest {
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
	// Canvas
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Layout-s expect too much about its parent, so we should not allow to drop them as NVO.
	 */
	@Test
	public void test_canNotDropLayout_asNVO() throws Exception {
		ContainerInfo panel = openContainer("""
				// filler filler filler
				public class Test extends JPanel {
					public Test() {
					}
				}""");
		loadCreationTool("java.awt.FlowLayout");
		// use canvas
		canvas.sideMode().create(10, 10);
		canvas.target(panel).inX(100).outY(100).move();
		canvas.assertCommandNull();
	}
}
