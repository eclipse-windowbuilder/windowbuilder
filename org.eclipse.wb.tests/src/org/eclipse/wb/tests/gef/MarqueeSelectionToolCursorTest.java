/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
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

import org.eclipse.draw2d.Cursors;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.tools.MarqueeSelectionTool;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author lobas_av
 *
 */
public class MarqueeSelectionToolCursorTest extends GefCursorTestCase {

	////////////////////////////////////////////////////////////////////////////
	//
	// SetUp
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();
		// configure
		m_domain.setActiveTool(new MarqueeSelectionTool());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_Move_Click_InvalidInput() throws Exception {
		EditPart shellEditPart =
				createEditPart(m_viewer.getRootEditPart(), 20, 20, 460, 360, null, null);
		m_viewer.select(shellEditPart);
		CursorLogger expectedLogger = new CursorLogger();
		//
		// first update cursor after mouse enter into viewer
		{
			m_actualLogger.assertEmpty();
			//
			m_sender.mouseEnter(0, 0);
			//
			expectedLogger.setCursor(Cursors.CROSS);
			m_actualLogger.assertEquals(expectedLogger);
		}
		// move to "ShellEditPart_NORTH_WEST_ResizeHandle"
		{
			m_sender.moveTo(20, 20);
			//
			m_actualLogger.assertEmpty();
		}
		// click
		{
			m_sender.click(30, 30, 1);
			//
			expectedLogger.setCursor(null);
			m_actualLogger.assertEquals(expectedLogger);
		}
		//
		m_viewer.select(shellEditPart);
		m_defaultToolProvider = () -> new MarqueeSelectionTool();
		m_domain.setActiveTool(new MarqueeSelectionTool());
		//
		// move to "ShellEditPart_MoveHandle"
		{
			m_sender.moveTo(40, 20);
			//
			expectedLogger.setCursor(Cursors.CROSS);
			expectedLogger.setCursor(Cursors.SIZEALL);
			expectedLogger.setCursor(Cursors.NO);
			expectedLogger.setCursor(Cursors.CROSS);
			m_actualLogger.assertEquals(expectedLogger);
		}
		// start drag
		{
			m_sender.startDrag(40, 20, 1);
			m_sender.dragTo(45, 30);
			//
			m_actualLogger.assertEmpty();
		}
		// invalid input
		{
			m_sender.startDrag(45, 30, 2);
			//
			m_actualLogger.assertEmpty();
		}
		// drag
		{
			m_sender.dragTo(50, 35);
			//
			m_actualLogger.assertEmpty();
		}
		// end drag
		{
			m_sender.endDrag();
			//
			expectedLogger.setCursor(null);
			m_actualLogger.assertEquals(expectedLogger);
		}
	}
}