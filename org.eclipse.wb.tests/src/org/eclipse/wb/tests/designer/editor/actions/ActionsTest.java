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
package org.eclipse.wb.tests.designer.editor.actions;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.multi.DesignerEditor;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link IAction} management in {@link DesignerEditor}.
 *
 * @author scheglov_ke
 */
public class ActionsTest extends SwingGefTest {
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
	 * There was bug that actions become disabled when we switch to other Designer editor and then
	 * return to original one. Problem is that we should implement
	 * {@link ITextEditor#getAction(String)} and return actions from "Source" or "Design" pages.
	 */
	@Test
	public void test_deactiveEditor_thenActivateAgain() throws Exception {
		IWorkbenchPage page = DesignerPlugin.getActivePage();
		// open Design
		openContainer("""
				// filler filler filler
				public class Test extends JPanel {
					public Test() {
					}
				}""");
		// "Copy" action exists
		IAction copyAction = getCopyAction();
		assertNotNull(copyAction);
		// open other Designer editor
		{
			IFile otherFile =
					setFileContentSrc(
							"test/Other.java",
							getTestSource("""
									// filler filler filler filler filler
									// filler filler filler filler filler
									public class Other extends JPanel {
										public Other() {
										}
									}"""));
			IDE.openEditor(page, otherFile);
		}
		// switch back to our Designer editor
		page.activate(m_designerEditor);
		// "Copy" action still exists
		assertSame(copyAction, getCopyAction());
	}
}
