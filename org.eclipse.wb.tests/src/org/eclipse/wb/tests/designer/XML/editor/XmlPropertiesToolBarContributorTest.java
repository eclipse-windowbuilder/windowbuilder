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

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.xml.editor.XmlPropertiesToolBarContributor;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.ToolItem;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test for {@link XmlPropertiesToolBarContributor}.
 *
 * @author scheglov_ke
 */
public class XmlPropertiesToolBarContributorTest extends XwtGefTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@After
	public void tearDown() throws Exception {
		DesignerExceptionUtils.flushErrorEntriesCache();
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
	// Test
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for "Goto definition" action.
	 */
	@Test
	@Ignore
	public void test_gotoDefinition() throws Exception {
		openEditor(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <RowLayout/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button'/>",
				"</Shell>");
		XmlObjectInfo button = getObjectByName("button");
		// prepare UiContext
		UiContext context = new UiContext();
		// no selection initially, so no action
		{
			ToolItem toolItem = context.getToolItem("Goto definition");
			assertNull(toolItem);
		}
		// select "button", show actions
		canvas.select(button);
		// use action
		{
			ToolItem toolItem = context.getToolItem("Goto definition");
			assertNotNull(toolItem);
			context.click(toolItem, SWT.NONE);
			waitEventLoop(0);
		}
		// assert that position in XML source was opened
		{
			int expectedPosition = button.getElement().getOffset();
			assertXMLSelection(expectedPosition, 0);
			assertEquals(0, m_designerEditor.getActivePage());
		}
	}
}
