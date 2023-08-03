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
package org.eclipse.wb.tests.designer.core.palette;

import org.eclipse.wb.core.editor.palette.model.entry.ToolEntryInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.gef.graphical.tools.SelectionTool;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.tests.gef.EmptyEditPartViewer;

import org.eclipse.jface.resource.ImageDescriptor;

import org.junit.Test;

/**
 * Tests for abstract {@link ToolEntryInfo}.
 *
 * @author scheglov_ke
 */
public class ToolEntryInfoTest extends AbstractPaletteTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Activate without tool.
	 */
	@Test
	public void test_activateNoTool() throws Exception {
		JavaInfo panel = parseEmptyPanel();
		IEditPartViewer editPartViewer = new EmptyEditPartViewer();
		// prepare ToolEntryInfo
		ToolEntryInfo toolEntry = new ToolEntryInfo() {
			@Override
			public ImageDescriptor getIcon() {
				return null;
			}

			@Override
			public Tool createTool() throws Exception {
				return null;
			}
		};
		// initialize
		assertTrue(toolEntry.initialize(editPartViewer, panel));
		// activate
		assertFalse(toolEntry.activate(false));
	}

	/**
	 * Activate with good tool.
	 */
	@Test
	public void test_activateTool() throws Exception {
		JavaInfo panel = parseEmptyPanel();
		IEditPartViewer editPartViewer = new EmptyEditPartViewer();
		// prepare ToolEntryInfo
		ToolEntryInfo toolEntry = new ToolEntryInfo() {
			@Override
			public ImageDescriptor getIcon() {
				return null;
			}

			@Override
			public Tool createTool() throws Exception {
				return new SelectionTool();
			}
		};
		// initialize
		assertTrue(toolEntry.initialize(editPartViewer, panel));
		// activate
		assertTrue(toolEntry.activate(false));
	}

	/**
	 * Activate, but throw exception when create tool.
	 */
	@Test
	public void test_activateException() throws Exception {
		JavaInfo panel = parseEmptyPanel();
		IEditPartViewer editPartViewer = new EmptyEditPartViewer();
		// prepare ToolEntryInfo
		ToolEntryInfo toolEntry = new ToolEntryInfo() {
			@Override
			public ImageDescriptor getIcon() {
				return null;
			}

			@Override
			public Tool createTool() throws Exception {
				throw new Exception();
			}
		};
		// initialize
		assertTrue(toolEntry.initialize(editPartViewer, panel));
		// activate
		try {
			DesignerPlugin.setDisplayExceptionOnConsole(false);
			assertFalse(toolEntry.activate(false));
		} finally {
			DesignerPlugin.setDisplayExceptionOnConsole(true);
		}
	}
}
