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
package org.eclipse.wb.tests.gef;

import org.eclipse.wb.draw2d.ICursorConstants;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.gef.graphical.tools.MarqueeSelectionTool;

/**
 * @author lobas_av
 *
 */
public class MarqueeSelectionToolCursorTest extends GefCursorTestCase {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MarqueeSelectionToolCursorTest() {
		super(MarqueeSelectionTool.class);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// SetUp
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// configure
		m_domain.setActiveTool(new MarqueeSelectionTool());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
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
			expectedLogger.setCursor(ICursorConstants.CROSS);
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
			m_actualLogger.assertEmpty();
		}
		//
		m_viewer.select(shellEditPart);
		m_defaultToolProvider = new IDefaultToolProvider() {
			@Override
			public Tool getDefaultTool() {
				return new MarqueeSelectionTool();
			}
		};
		m_domain.setActiveTool(new MarqueeSelectionTool());
		//
		// move to "ShellEditPart_MoveHandle"
		{
			m_sender.moveTo(40, 20);
			//
			m_actualLogger.assertEmpty();
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
			expectedLogger.setCursor(ICursorConstants.NO);
			m_actualLogger.assertEquals(expectedLogger);
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
			m_actualLogger.assertEmpty();
		}
	}
}