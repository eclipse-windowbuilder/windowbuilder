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
package org.eclipse.wb.tests.designer.XWT.gef;

import org.eclipse.wb.core.editor.IDesignerEditor;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.editor.actions.SwitchAction;
import org.eclipse.wb.internal.core.editor.errors.WarningComposite;
import org.eclipse.wb.internal.xwt.parser.XwtJavaParseValidator;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ide.IDE;

import org.junit.After;
import org.junit.Test;

/**
 * Test for {@link XwtJavaParseValidator}.
 *
 * @author scheglov_ke
 */
public class XwtJavaParseValidatorTest extends XwtGefTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@After
	public void tearDown() throws Exception {
		DesignerPlugin.setDisplayExceptionOnConsole(true);
		EnvironmentUtils.setTestingTime(true);
		super.tearDown();
	}

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
	public void test_showWarning() throws Exception {
		removeExceptionsListener();
		DesignerPlugin.setDisplayExceptionOnConsole(false);
		//
		IFile javaFile =
				setFileContentSrc(
						"test/Test.java",
						getJavaSource(
								"// filler filler filler filler filler",
								"public class Test {",
								"  public static void main(String args[]) throws Exception {",
								"    XWT.load((java.net.URL) null);",
								"  }",
								"}"));
		IEditorPart javaEditor =
				IDE.openEditor(DesignerPlugin.getActivePage(), javaFile, IDesignerEditor.ID);
		// switch to Design
		{
			SwitchAction switchAction = new SwitchAction();
			switchAction.setActiveEditor(null, javaEditor);
			switchAction.run();
		}
		// prepare UIContext
		UiContext context = new UiContext();
		// WarningComposite is visible
		Composite warningComposite = context.findFirstWidget(WarningComposite.class);
		assertNotNull(warningComposite);
		assertTrue(warningComposite.isVisible());
	}
}
