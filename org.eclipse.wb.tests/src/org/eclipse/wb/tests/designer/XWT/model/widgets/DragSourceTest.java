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
package org.eclipse.wb.tests.designer.XWT.model.widgets;

import org.eclipse.wb.internal.xwt.model.widgets.DragSourceInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

/**
 * Test for {@link DragSourceInfo}.
 *
 * @author scheglov_ke
 */
public class DragSourceTest extends XwtModelTest {
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
	public void test_0() throws Exception {
		parse(
				"<Shell xmlns:p1='clr-namespace:org.eclipse.swt.dnd'>",
				"  <p1:DragSource wbp:name='dragSource'/>",
				"</Shell>");
		refresh();
		DragSourceInfo dragSource = getObjectByName("dragSource");
		assertNotNull(dragSource);
	}
}