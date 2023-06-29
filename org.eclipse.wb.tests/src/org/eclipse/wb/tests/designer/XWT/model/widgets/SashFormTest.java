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

import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.xwt.model.widgets.SashFormInfo;
import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.IntValue;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

/**
 * Test for {@link SashFormInfo}.
 *
 * @author scheglov_ke
 */
public class SashFormTest extends XwtModelTest {
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
	// isHorizontal()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link SashFormInfo#isHorizontal()}.
	 */
	public void test_isHorizontal_true() throws Exception {
		parse(
				"<Shell>",
				"  <Shell.layout>",
				"    <FillLayout/>",
				"  </Shell.layout>",
				"  <SashForm wbp:name='sashForm'/>",
				"</Shell>");
		refresh();
		SashFormInfo sashForm = getObjectByName("sashForm");
		assertTrue(sashForm.isHorizontal());
	}

	/**
	 * Test for {@link SashFormInfo#isHorizontal()}.
	 */
	public void test_isHorizontal_false() throws Exception {
		parse(
				"<Shell>",
				"  <Shell.layout>",
				"    <FillLayout/>",
				"  </Shell.layout>",
				"  <SashForm wbp:name='sashForm' x:style='VERTICAL'/>",
				"</Shell>");
		refresh();
		SashFormInfo sashForm = getObjectByName("sashForm");
		assertFalse(sashForm.isHorizontal());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link SashFormInfo#command_CREATE(ControlInfo, ControlInfo)}.
	 * <p>
	 * No existing children yet.
	 */
	public void test_CREATE_0() throws Exception {
		parse(
				"<Shell>",
				"  <Shell.layout>",
				"    <FillLayout/>",
				"  </Shell.layout>",
				"  <SashForm wbp:name='sashForm'/>",
				"</Shell>");
		refresh();
		SashFormInfo sashForm = getObjectByName("sashForm");
		//
		ControlInfo button = createButton();
		sashForm.command_CREATE(button, null);
		assertXML(
				"<Shell>",
				"  <Shell.layout>",
				"    <FillLayout/>",
				"  </Shell.layout>",
				"  <SashForm wbp:name='sashForm' weights='1'>",
				"    <Button/>",
				"  </SashForm>",
				"</Shell>");
	}

	/**
	 * Test for {@link SashFormInfo#command_CREATE(ControlInfo, ControlInfo)}.
	 * <p>
	 * Two existing children with weights.
	 */
	public void test_CREATE_2() throws Exception {
		parse(
				"<Shell>",
				"  <Shell.layout>",
				"    <FillLayout/>",
				"  </Shell.layout>",
				"  <SashForm wbp:name='sashForm' weights='2, 4'>",
				"    <Button wbp:name='button_1'/>",
				"    <Button wbp:name='button_2'/>",
				"  </SashForm>",
				"</Shell>");
		refresh();
		SashFormInfo sashForm = getObjectByName("sashForm");
		ControlInfo button_2 = getObjectByName("button_2");
		//
		ControlInfo button = createButton();
		sashForm.command_CREATE(button, button_2);
		assertXML(
				"<Shell>",
				"  <Shell.layout>",
				"    <FillLayout/>",
				"  </Shell.layout>",
				"  <SashForm wbp:name='sashForm' weights='2, 3, 4'>",
				"    <Button wbp:name='button_1'/>",
				"    <Button/>",
				"    <Button wbp:name='button_2'/>",
				"  </SashForm>",
				"</Shell>");
	}

	/**
	 * Test for {@link SashFormInfo#command_MOVE(ControlInfo, ControlInfo)}.
	 * <p>
	 * Two existing children with weights.
	 */
	public void test_MOVE_inner() throws Exception {
		parse(
				"<Shell>",
				"  <Shell.layout>",
				"    <FillLayout/>",
				"  </Shell.layout>",
				"  <SashForm wbp:name='sashForm' weights='2, 4'>",
				"    <Button wbp:name='button_1'/>",
				"    <Button wbp:name='button_2'/>",
				"  </SashForm>",
				"</Shell>");
		refresh();
		SashFormInfo sashForm = getObjectByName("sashForm");
		ControlInfo button_1 = getObjectByName("button_1");
		ControlInfo button_2 = getObjectByName("button_2");
		//
		sashForm.command_MOVE(button_2, button_1);
		assertXML(
				"<Shell>",
				"  <Shell.layout>",
				"    <FillLayout/>",
				"  </Shell.layout>",
				"  <SashForm wbp:name='sashForm' weights='4, 2'>",
				"    <Button wbp:name='button_2'/>",
				"    <Button wbp:name='button_1'/>",
				"  </SashForm>",
				"</Shell>");
	}

	/**
	 * Test for {@link SashFormInfo#command_MOVE(ControlInfo, ControlInfo)}.
	 * <p>
	 * Move {@link ControlInfo} in.
	 */
	public void test_MOVE_in() throws Exception {
		parse(
				"<Shell>",
				"  <Shell.layout>",
				"    <FillLayout/>",
				"  </Shell.layout>",
				"  <SashForm wbp:name='sashForm'>",
				"    <Button wbp:name='button_1'/>",
				"  </SashForm>",
				"  <Button wbp:name='button_2'/>",
				"</Shell>");
		refresh();
		SashFormInfo sashForm = getObjectByName("sashForm");
		ControlInfo button_2 = getObjectByName("button_2");
		//
		sashForm.command_MOVE(button_2, null);
		assertXML(
				"<Shell>",
				"  <Shell.layout>",
				"    <FillLayout/>",
				"  </Shell.layout>",
				"  <SashForm wbp:name='sashForm' weights='1, 1'>",
				"    <Button wbp:name='button_1'/>",
				"    <Button wbp:name='button_2'/>",
				"  </SashForm>",
				"</Shell>");
	}

	/**
	 * Test for {@link SashFormInfo#command_MOVE(ControlInfo, ControlInfo)}.
	 * <p>
	 * Move {@link ControlInfo} out.
	 */
	public void test_MOVE_out() throws Exception {
		CompositeInfo shell =
				parse(
						"<Shell>",
						"  <Shell.layout>",
						"    <FillLayout/>",
						"  </Shell.layout>",
						"  <SashForm wbp:name='sashForm' weights='1, 2'>",
						"    <Button wbp:name='button_1'/>",
						"    <Button wbp:name='button_2'/>",
						"  </SashForm>",
						"</Shell>");
		refresh();
		ControlInfo button_2 = getObjectByName("button_2");
		//
		shell.getLayout().command_MOVE(button_2, null);
		assertXML(
				"<Shell>",
				"  <Shell.layout>",
				"    <FillLayout/>",
				"  </Shell.layout>",
				"  <SashForm wbp:name='sashForm' weights='1'>",
				"    <Button wbp:name='button_1'/>",
				"  </SashForm>",
				"  <Button wbp:name='button_2'/>",
				"</Shell>");
	}

	/**
	 * Delete child {@link ControlInfo}.
	 */
	public void test_DELETE() throws Exception {
		parse(
				"<Shell>",
				"  <Shell.layout>",
				"    <FillLayout/>",
				"  </Shell.layout>",
				"  <SashForm wbp:name='sashForm' weights='1, 2'>",
				"    <Button wbp:name='button_1'/>",
				"    <Button wbp:name='button_2'/>",
				"  </SashForm>",
				"</Shell>");
		refresh();
		ControlInfo button_2 = getObjectByName("button_2");
		//
		button_2.delete();
		assertXML(
				"<Shell>",
				"  <Shell.layout>",
				"    <FillLayout/>",
				"  </Shell.layout>",
				"  <SashForm wbp:name='sashForm' weights='1'>",
				"    <Button wbp:name='button_1'/>",
				"  </SashForm>",
				"</Shell>");
	}

	/**
	 * Test for {@link SashFormInfo#command_RESIZE(ControlInfo, int)}.
	 */
	public void test_RESIZE() throws Exception {
		parse(
				"<Shell>",
				"  <Shell.layout>",
				"    <FillLayout/>",
				"  </Shell.layout>",
				"  <SashForm wbp:name='sashForm' weights='1, 2'>",
				"    <Button wbp:name='button_1'/>",
				"    <Button wbp:name='button_2'/>",
				"  </SashForm>",
				"</Shell>");
		refresh();
		SashFormInfo sashForm = getObjectByName("sashForm");
		ControlInfo button_1 = getObjectByName("button_1");
		//
		sashForm.command_RESIZE(button_1, 150);
		int expectedRightWeight =
				Expectations.get(281, new IntValue[]{
						new IntValue("flanker-win", 289),
						new IntValue("kosta-home", 289),
						new IntValue("scheglov-win", 281)});
		assertXML(
				"<Shell>",
				"  <Shell.layout>",
				"    <FillLayout/>",
				"  </Shell.layout>",
				"  <SashForm wbp:name='sashForm' weights='150, " + expectedRightWeight + "'>",
				"    <Button wbp:name='button_1'/>",
				"    <Button wbp:name='button_2'/>",
				"  </SashForm>",
				"</Shell>");
	}
}