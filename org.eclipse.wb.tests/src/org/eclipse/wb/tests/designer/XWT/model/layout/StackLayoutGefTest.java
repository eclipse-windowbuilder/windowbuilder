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
package org.eclipse.wb.tests.designer.XWT.model.layout;

import org.eclipse.wb.internal.rcp.gef.policy.layout.StackLayoutNavigationFigure;
import org.eclipse.wb.internal.xwt.model.layout.StackLayoutInfo;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;

/**
 * Test for {@link StackLayoutInfo} in GEF.
 *
 * @author scheglov_ke
 */
public class StackLayoutGefTest extends XwtGefTest {
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
	public void test_CREATE_onCanvas_empty() throws Exception {
		CompositeInfo shell =
				openEditor(
						"// filler filler filler filler filler",
						"<Shell>",
						"  <Shell.layout>",
						"    <StackLayout wbp:name='layout'/>",
						"  </Shell.layout>",
						"</Shell>");
		//
		loadButton();
		canvas.moveTo(shell, 100, 100).click();
		assertXML(
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <StackLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"  <Button/>",
				"</Shell>");
	}

	public void test_CREATE_onCanvas_beforeExisting() throws Exception {
		CompositeInfo shell =
				openEditor(
						"// filler filler filler filler filler",
						"<Shell>",
						"  <Shell.layout>",
						"    <StackLayout wbp:name='layout'/>",
						"  </Shell.layout>",
						"  <Button wbp:name='button_1'/>",
						"</Shell>");
		ControlInfo button_1 = getObjectByName("button_1");
		// select "shell", so "button_1" will be transparent on borders
		canvas.select(shell);
		// create new Button
		loadButton();
		canvas.moveTo(button_1, 2, 100).click();
		assertXML(
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <StackLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"  <Button/>",
				"  <Button wbp:name='button_1'/>",
				"</Shell>");
	}

	public void test_CREATE_onCanvas_afterExisting() throws Exception {
		CompositeInfo shell =
				openEditor(
						"// filler filler filler filler filler",
						"<Shell>",
						"  <Shell.layout>",
						"    <StackLayout wbp:name='layout'/>",
						"  </Shell.layout>",
						"  <Button wbp:name='button_1'/>",
						"</Shell>");
		ControlInfo button_1 = getObjectByName("button_1");
		// select "shell", so "button_1" will be transparent on borders
		canvas.select(shell);
		// create new Button
		loadButton();
		canvas.moveTo(button_1, -2, 100).click();
		assertXML(
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <StackLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button_1'/>",
				"  <Button/>",
				"</Shell>");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// CREATE in tree
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_CREATE_inTree_empty() throws Exception {
		CompositeInfo shell =
				openEditor(
						"// filler filler filler filler filler",
						"<Shell>",
						"  <Shell.layout>",
						"    <StackLayout wbp:name='layout'/>",
						"  </Shell.layout>",
						"</Shell>");
		// create new Button
		loadButton();
		tree.moveOn(shell);
		tree.assertCommandNotNull();
		tree.click();
		assertXML(
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <StackLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"  <Button/>",
				"</Shell>");
	}

	public void test_CREATE_inTree_beforeExisting() throws Exception {
		openEditor(
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <StackLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button_1'/>",
				"</Shell>");
		ControlInfo button_1 = getObjectByName("button_1");
		// create new Button
		loadButton();
		tree.moveBefore(button_1).click();
		assertXML(
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <StackLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"  <Button/>",
				"  <Button wbp:name='button_1'/>",
				"</Shell>");
	}

	public void test_CREATE_inTree_afterExisting() throws Exception {
		openEditor(
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <StackLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button_1'/>",
				"</Shell>");
		ControlInfo button_1 = getObjectByName("button_1");
		// create new Button
		loadButton();
		tree.moveAfter(button_1).click();
		assertXML(
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <StackLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button_1'/>",
				"  <Button/>",
				"</Shell>");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// MOVE in tree
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_MOVE_inTree() throws Exception {
		openEditor(
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <StackLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button_1'/>",
				"  <Button wbp:name='button_2'/>",
				"</Shell>");
		ControlInfo button_1 = getObjectByName("button_1");
		ControlInfo button_2 = getObjectByName("button_2");
		//
		tree.startDrag(button_2).dragBefore(button_1).endDrag();
		assertXML(
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <StackLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button_2'/>",
				"  <Button wbp:name='button_1'/>",
				"</Shell>");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Navigation
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_navigation_next() throws Exception {
		openEditor(
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <StackLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button_1'/>",
				"  <Button wbp:name='button_2'/>",
				"  <Button wbp:name='button_3'/>",
				"</Shell>");
		ControlInfo button_1 = getObjectByName("button_1");
		ControlInfo button_2 = getObjectByName("button_2");
		ControlInfo button_3 = getObjectByName("button_3");
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

	public void test_navigation_prev() throws Exception {
		openEditor(
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <StackLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button_1'/>",
				"  <Button wbp:name='button_2'/>",
				"  <Button wbp:name='button_3'/>",
				"</Shell>");
		ControlInfo button_1 = getObjectByName("button_1");
		ControlInfo button_2 = getObjectByName("button_2");
		ControlInfo button_3 = getObjectByName("button_3");
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

	private void navigateNext(ControlInfo component) {
		canvas.moveTo(component, -3 - 1, 0).click();
	}

	private void navigatePrev(ControlInfo component) {
		canvas.moveTo(component, -3 - StackLayoutNavigationFigure.WIDTH - 1, 0).click();
	}
}
