/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.tests.gef;

import org.eclipse.wb.gef.core.requests.ICreationFactory;
import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.gef.graphical.tools.SelectionTool;
import org.eclipse.wb.internal.gef.core.SharedCursors;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.swt.graphics.Cursor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author lobas_av
 *
 */
public class CreationToolCursorTest extends GefCursorTestCase {
	private CreationTool m_tool;

	////////////////////////////////////////////////////////////////////////////
	//
	// Cursor constants
	//
	////////////////////////////////////////////////////////////////////////////
	public static final Cursor CURSOR_ADD() {
		return SharedCursors.CURSOR_ADD;
	}

	public static final Cursor CURSOR_NO() {
		return SharedCursors.CURSOR_NO;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// SetUp
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();
		// create test factory
		ICreationFactory factory = new ICreationFactory() {
			@Override
			public void activate() {
			}

			@Override
			public Object getNewObject() {
				return "_NewObject_";
			}

			@Override
			public String toString() {
				return "TestFactory";
			}
		};
		// set CreationTool
		m_tool = new CreationTool(factory);
		m_domain.setActiveTool(m_tool);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Test
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_work_updateCursors_Move() throws Exception {
		EditPart shellEditPart =
				createEditPart(m_viewer.getRootEditPart(), 20, 20, 460, 360, new Command(){});
		EditPart buttonEditPart = createEditPart(shellEditPart, 100, 100, 200, 100, null);
		m_viewer.select(buttonEditPart);
		CursorLogger expectedLogger = new CursorLogger();
		//
		// first update cursor after mouse enter into viewer
		{
			m_actualLogger.assertEmpty();
			//
			m_sender.mouseEnter(0, 0);
			//
			expectedLogger.setCursor(CURSOR_NO());
			m_actualLogger.assertEquals(expectedLogger);
		}
		// move to "RootEditPart"
		{
			m_sender.moveTo(10, 10);
			//
			m_actualLogger.assertEmpty();
		}
		// move to "ShellEditPart"
		{
			m_sender.moveTo(30, 30);
			//
			expectedLogger.setCursor(CURSOR_ADD());
			m_actualLogger.assertEquals(expectedLogger);
		}
		// move to "RootEditPart"
		{
			m_sender.moveTo(10, 40);
			//
			expectedLogger.setCursor(CURSOR_NO());
			m_actualLogger.assertEquals(expectedLogger);
		}
		// move to "ShellEditPart"
		{
			m_sender.moveTo(90, 90);
			//
			expectedLogger.setCursor(CURSOR_ADD());
			m_actualLogger.assertEquals(expectedLogger);
		}
		// move to "ButtonEditPart"
		{
			m_sender.moveTo(130, 130);
			//
			expectedLogger.setCursor(CURSOR_NO());
			m_actualLogger.assertEquals(expectedLogger);
		}
		// move to "ButtonEditPart_NORTH_WEST_ResizeHandle"
		{
			m_sender.moveTo(120, 120);
			//
			m_actualLogger.assertEmpty();
		}
		// move to "ButtonEditPart_MoveHandle"
		{
			m_sender.moveTo(140, 120);
			//
			m_actualLogger.assertEmpty();
		}
		// move to "ShellEditPart"
		{
			m_sender.moveTo(450, 350);
			//
			expectedLogger.setCursor(CURSOR_ADD());
			m_actualLogger.assertEquals(expectedLogger);
		}
	}

	@Test
	public void test_work_updateCursors_Accept_Click() throws Exception {
		EditPart shellEditPart =
				createEditPart(m_viewer.getRootEditPart(), 20, 20, 460, 360, new Command(){});
		EditPart buttonEditPart = createEditPart(shellEditPart, 100, 100, 200, 100, null);
		m_viewer.select(buttonEditPart);
		CursorLogger expectedLogger = new CursorLogger();
		//
		// first update cursor after mouse enter into viewer
		{
			m_actualLogger.assertEmpty();
			//
			m_sender.mouseEnter(0, 0);
			//
			expectedLogger.setCursor(CURSOR_NO());
			m_actualLogger.assertEquals(expectedLogger);
		}
		// move to "ShellEditPart"
		{
			m_sender.moveTo(30, 30);
			//
			expectedLogger.setCursor(CURSOR_ADD());
			m_actualLogger.assertEquals(expectedLogger);
		}
		// start drag on "ShellEditPart"
		{
			m_sender.startDrag(50, 50, 1);
			//
			m_actualLogger.assertEmpty();
		}
		// drag on "ShellEditPart"
		{
			m_sender.dragTo(60, 60);
			//
			m_actualLogger.assertEmpty();
		}
		// drag on "ButtonEditPart"
		{
			// "ButtonEditPart_NORTH_WEST_ResizeHandle"
			m_sender.dragTo(120, 120);
			// "ButtonEditPart_MoveHandle"
			m_sender.dragTo(140, 120);
			// "ButtonEditPart"
			m_sender.dragTo(140, 130);
			//
			m_actualLogger.assertEmpty();
		}
		// end drag
		{
			m_sender.endDrag();
			//
			// during executeCommand ivoke setCommand(null) then cursor == NO
			expectedLogger.setCursor(CURSOR_NO());
			m_actualLogger.assertEquals(expectedLogger);
		}
	}

	@Test
	public void test_work_updateCursors_Not_Accept_Click() throws Exception {
		EditPart shellEditPart =
				createEditPart(m_viewer.getRootEditPart(), 20, 20, 460, 360, new Command(){});
		EditPart buttonEditPart = createEditPart(shellEditPart, 100, 100, 200, 100, null);
		m_viewer.select(buttonEditPart);
		//
		// first update cursor after mouse enter into viewer
		{
			m_actualLogger.assertEmpty();
			//
			m_sender.mouseEnter(0, 0);
			//
			CursorLogger expectedLogger = new CursorLogger();
			expectedLogger.setCursor(CURSOR_NO());
			m_actualLogger.assertEquals(expectedLogger);
		}
		// move to "ButtonEditPart"
		{
			m_sender.moveTo(130, 130);
			//
			m_actualLogger.assertEmpty();
		}
		// start drag on "ButtonEditPart"
		{
			m_sender.startDrag(130, 130, 1);
			//
			m_actualLogger.assertEmpty();
		}
		// drag on "ButtonEditPart"
		{
			// "ButtonEditPart_MoveHandle"
			m_sender.dragTo(130, 120);
			// "ButtonEditPart_NORTH_WEST_ResizeHandle"
			m_sender.dragTo(120, 120);
			//
			m_actualLogger.assertEmpty();
		}
		// drag on "ShellEditPart"
		{
			m_sender.dragTo(110, 110);
			//
			m_actualLogger.assertEmpty();
		}
		// end drag
		{
			m_sender.endDrag();
			//
			m_actualLogger.assertEmpty();
		}
	}

	@Test
	public void test_work_updateCursors_Accept_Move_InvalidInputClick() throws Exception {
		createEditPart(m_viewer.getRootEditPart(), 20, 20, 460, 360, new Command(){});
		CursorLogger expectedLogger = new CursorLogger();
		// first update cursor after mouse enter into viewer
		{
			m_actualLogger.assertEmpty();
			//
			m_sender.mouseEnter(0, 0);
			//
			expectedLogger.setCursor(CURSOR_NO());
			m_actualLogger.assertEquals(expectedLogger);
		}
		// move to "ShellEditPart"
		{
			m_sender.moveTo(50, 50);
			//
			expectedLogger.setCursor(CURSOR_ADD());
			m_actualLogger.assertEquals(expectedLogger);
		}
		// invalid click
		{
			m_sender.click(60, 60, 2);
			//
			expectedLogger.setCursor(CURSOR_NO());
			m_actualLogger.assertEquals(expectedLogger);
		}
	}

	@Test
	public void test_work_updateCursors_Accept_Drag_InvalidInputClick() throws Exception {
		createEditPart(m_viewer.getRootEditPart(), 20, 20, 460, 360, new Command(){});
		CursorLogger expectedLogger = new CursorLogger();
		// first update cursor after mouse enter into viewer
		{
			m_actualLogger.assertEmpty();
			//
			m_sender.mouseEnter(0, 0);
			//
			expectedLogger.setCursor(CURSOR_NO());
			m_actualLogger.assertEquals(expectedLogger);
		}
		// move to "ShellEditPart"
		{
			m_sender.moveTo(50, 50);
			//
			expectedLogger.setCursor(CURSOR_ADD());
			m_actualLogger.assertEquals(expectedLogger);
		}
		// start drag on "ShellEditPart"
		{
			m_sender.startDrag(60, 60, 1);
			//
			m_actualLogger.assertEmpty();
		}
		// drag on "ShellEditPart"
		{
			m_sender.dragTo(70, 70);
			//
			m_actualLogger.assertEmpty();
		}
		// invalid click
		{
			m_sender.click(70, 70, 2);
			//
			expectedLogger.setCursor(CURSOR_NO());
			m_actualLogger.assertEquals(expectedLogger);
		}
		// end drag
		{
			m_sender.endDrag();
			//
			m_actualLogger.assertEmpty();
		}
	}

	@Test
	public void test_work_updateCursors_Accept_Drag_InvalidInputDrag() throws Exception {
		createEditPart(m_viewer.getRootEditPart(), 20, 20, 460, 360, new Command(){});
		CursorLogger expectedLogger = new CursorLogger();
		// first update cursor after mouse enter into viewer
		{
			m_actualLogger.assertEmpty();
			//
			m_sender.mouseEnter(0, 0);
			//
			expectedLogger.setCursor(CURSOR_NO());
			m_actualLogger.assertEquals(expectedLogger);
		}
		// move to "ShellEditPart"
		{
			m_sender.moveTo(50, 50);
			//
			expectedLogger.setCursor(CURSOR_ADD());
			m_actualLogger.assertEquals(expectedLogger);
		}
		// start drag on "ShellEditPart"
		{
			m_sender.startDrag(60, 60, 1);
			//
			m_actualLogger.assertEmpty();
		}
		// drag on "ShellEditPart"
		{
			m_sender.dragTo(70, 70);
			//
			m_actualLogger.assertEmpty();
		}
		// invalid start drag
		{
			m_sender.startDrag(70, 70, 2);
			//
			expectedLogger.setCursor(CURSOR_NO());
			m_actualLogger.assertEquals(expectedLogger);
		}
		// drag
		{
			m_sender.dragTo(80, 80);
			//
			m_actualLogger.assertEmpty();
		}
		// end drag
		{
			m_sender.endDrag();
			//
			m_actualLogger.assertEmpty();
		}
	}

	@Test
	public void test_work_updateCursors_Accept_Click_Again_Activate_SelectionTool() throws Exception {
		createEditPart(m_viewer.getRootEditPart(), 20, 20, 460, 360, new Command(){});
		CursorLogger expectedLogger = new CursorLogger();
		// first update cursor after mouse enter into viewer
		{
			m_actualLogger.assertEmpty();
			//
			m_sender.mouseEnter(0, 0);
			//
			expectedLogger.setCursor(CURSOR_NO());
			m_actualLogger.assertEquals(expectedLogger);
		}
		// move to "ShellEditPart"
		{
			m_sender.moveTo(50, 50);
			//
			expectedLogger.setCursor(CURSOR_ADD());
			m_actualLogger.assertEquals(expectedLogger);
		}
		//
		m_defaultToolProvider = new IDefaultToolProvider() {
			@Override
			public Tool getDefaultTool() {
				return new SelectionTool();
			}
		};
		// click to "ShellEditPart"
		{
			m_sender.click(60, 60, 1);
			//
			// during executeCommand ivoke setCommand(null) then cursor == NO
			expectedLogger.setCursor(CURSOR_NO());
			//
			expectedLogger.setCursor(null);
			//
			m_actualLogger.assertEquals(expectedLogger);
		}
	}

	@Test
	public void test_work_updateCursors_Accept_Click_Again_Activate_CreationTool() throws Exception {
		createEditPart(m_viewer.getRootEditPart(), 20, 20, 460, 360, new Command(){});
		CursorLogger expectedLogger = new CursorLogger();
		// first update cursor after mouse enter into viewer
		{
			m_actualLogger.assertEmpty();
			//
			m_sender.mouseEnter(0, 0);
			//
			expectedLogger.setCursor(CURSOR_NO());
			m_actualLogger.assertEquals(expectedLogger);
		}
		// move to "ShellEditPart"
		{
			m_sender.moveTo(50, 50);
			//
			expectedLogger.setCursor(CURSOR_ADD());
			m_actualLogger.assertEquals(expectedLogger);
		}
		//
		m_tool = new CreationTool(m_tool.getFactory());
		m_defaultToolProvider = new IDefaultToolProvider() {
			@Override
			public Tool getDefaultTool() {
				return m_tool;
			}
		};
		// click to "ShellEditPart"
		{
			m_sender.click(60, 60, 1);
			//
			// during executeCommand ivoke setCommand(null) then cursor == NO
			expectedLogger.setCursor(CURSOR_NO());
			expectedLogger.setCursor(CURSOR_ADD());
			//
			m_actualLogger.assertEquals(expectedLogger);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private static final EditPart createEditPart(EditPart parentEditPart,
			int x,
			int y,
			int width,
			int height,
			final Command acceptCreateCommand) throws Exception {
		return createEditPart(parentEditPart, x, y, width, height, acceptCreateCommand, null);
	}
}