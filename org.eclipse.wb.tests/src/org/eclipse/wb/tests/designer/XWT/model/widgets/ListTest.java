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

import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

import org.eclipse.swt.widgets.List;

import org.junit.Test;

/**
 * Test for {@link List} widget in XWT.
 *
 * @author scheglov_ke
 */
public class ListTest extends XwtModelTest {
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
	public void test_noModels_forItems() throws Exception {
		parse(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell xmlns:p1='clr-namespace:java.lang'>",
				"  <List wbp:name='widget'>",
				"    <List.items>",
				"      <p1:String>aaa</p1:String>",
				"      <p1:String>bbb</p1:String>",
				"      <p1:String>ccc</p1:String>",
				"    </List.items>",
				"  </List>",
				"</Shell>");
		assertHierarchy(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  implicit-layout: absolute",
				"  <List wbp:name='widget'>");
	}
}