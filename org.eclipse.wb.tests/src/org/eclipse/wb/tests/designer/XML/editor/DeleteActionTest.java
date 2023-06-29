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
package org.eclipse.wb.tests.designer.XML.editor;

import org.eclipse.wb.internal.core.editor.actions.DeleteAction;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;

import org.eclipse.jface.action.IAction;

/**
 * Test for {@link DeleteAction}.
 *
 * @author scheglov_ke
 */
public class DeleteActionTest extends XwtGefTest {
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
	/**
	 * We can delete even "root" component, but this works as clearing it.
	 */
	public void test_canRootComponent() throws Exception {
		XmlObjectInfo shell =
				openEditor(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"<Shell text='My text'>",
						"  <Button wbp:name='button'/>",
						"</Shell>");
		// select "shell"
		canvas.select(shell);
		// delete "shell"
		IAction deleteAction = getDeleteAction();
		assertTrue(deleteAction.isEnabled());
		deleteAction.run();
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell text='My text'/>");
	}

	public void test_deleteSingleComponent() throws Exception {
		openEditor(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Button wbp:name='button'/>",
				"</Shell>");
		// select "button"
		XmlObjectInfo button = getObjectByName("button");
		canvas.select(button);
		// delete "button"
		IAction deleteAction = getDeleteAction();
		assertTrue(deleteAction.isEnabled());
		deleteAction.run();
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell/>");
	}

	public void test_deleteComponent_andItsParent() throws Exception {
		openEditor(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Composite wbp:name='composite'>",
				"    <Button wbp:name='button'/>",
				"  </Composite>",
				"</Shell>");
		// select "composite" and "button"
		XmlObjectInfo composite = getObjectByName("composite");
		XmlObjectInfo button = getObjectByName("button");
		canvas.select(composite, button);
		// delete "button"
		IAction deleteAction = getDeleteAction();
		assertTrue(deleteAction.isEnabled());
		deleteAction.run();
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell/>");
	}
}
