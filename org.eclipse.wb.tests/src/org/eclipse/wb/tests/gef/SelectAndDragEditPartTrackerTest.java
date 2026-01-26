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

import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.DragPermissionRequest;
import org.eclipse.wb.gef.core.tools.DragEditPartTracker;
import org.eclipse.wb.gef.core.tools.SelectEditPartTracker;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.requests.GroupRequest;
import org.eclipse.gef.requests.SelectionRequest;
import org.eclipse.swt.SWT;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author lobas_av
 *
 */
public class SelectAndDragEditPartTrackerTest extends RequestTestCase {

	////////////////////////////////////////////////////////////////////////////
	//
	// SelectEditPartTracker
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_Selection() throws Exception {
		RequestsLogger actualLogger = new RequestsLogger();
		//
		RequestTestCaseEditPart editPart1 = new RequestTestCaseEditPart("editPart1", actualLogger);
		editPart1.activate();
		RequestTestCaseEditPart editPart2 = new RequestTestCaseEditPart("editPart2", actualLogger);
		editPart2.activate();
		RequestTestCaseEditPart editPart3 = new RequestTestCaseEditPart("editPart3", actualLogger);
		editPart3.activate();
		RequestTestCaseEditPart editPart4 = new RequestTestCaseEditPart("editPart4", actualLogger);
		editPart4.activate();
		RequestTestCaseEditPart editPart5 = new RequestTestCaseEditPart("editPart5", actualLogger);
		editPart5.activate();
		//
		SelectEditPartTracker tracker = new SelectEditPartTracker(editPart1);
		m_domain.setActiveTool(tracker);
		//
		assertTrue(m_viewer.getSelectedEditParts().isEmpty());
		assertEquals(EditPart.SELECTED_NONE, editPart1.getSelected());
		// click invalid button
		{
			m_sender.click(0, 0, 4);
			//
			assertTrue(m_viewer.getSelectedEditParts().isEmpty());
			assertEquals(EditPart.SELECTED_NONE, editPart1.getSelected());
			actualLogger.assertEmpty();
		}
		// selection click (Button<1>) on "editPart1"
		{
			m_domain.setActiveTool(tracker);
			m_sender.click(0, 0, 1);
			//
			assertEquals(1, m_viewer.getSelectedEditParts().size());
			assertSame(editPart1, m_viewer.getSelectedEditParts().get(0));
			assertEquals(EditPart.SELECTED_PRIMARY, editPart1.getSelected());
			actualLogger.assertEmpty();
		}
		// selection click (Button<3>) on "editPart2"
		{
			tracker = new SelectEditPartTracker(editPart2);
			m_domain.setActiveTool(tracker);
			m_sender.click(0, 0, 3);
			//
			assertEquals(1, m_viewer.getSelectedEditParts().size());
			assertSame(editPart2, m_viewer.getSelectedEditParts().get(0));
			assertEquals(EditPart.SELECTED_PRIMARY, editPart2.getSelected());
			assertEquals(EditPart.SELECTED_NONE, editPart1.getSelected());
			actualLogger.assertEmpty();
		}
		// append selection click (use Shift) on "editPart3"
		{
			tracker = new SelectEditPartTracker(editPart3);
			m_domain.setActiveTool(tracker);
			m_sender.setStateMask(SWT.SHIFT);
			m_sender.click(0, 0, 1);
			m_sender.setStateMask(0);
			//
			assertEquals(2, m_viewer.getSelectedEditParts().size());
			assertSame(editPart2, m_viewer.getSelectedEditParts().get(0));
			assertSame(editPart3, m_viewer.getSelectedEditParts().get(1));
			assertEquals(EditPart.SELECTED, editPart2.getSelected());
			assertEquals(EditPart.SELECTED_PRIMARY, editPart3.getSelected());
			actualLogger.assertEmpty();
		}
		// append selection click (use Ctrl) on "editPart4"
		{
			tracker = new SelectEditPartTracker(editPart4);
			m_domain.setActiveTool(tracker);
			m_sender.setStateMask(SWT.CONTROL);
			m_sender.click(0, 0, 1);
			m_sender.setStateMask(0);
			//
			assertEquals(3, m_viewer.getSelectedEditParts().size());
			assertSame(editPart2, m_viewer.getSelectedEditParts().get(0));
			assertSame(editPart3, m_viewer.getSelectedEditParts().get(1));
			assertSame(editPart4, m_viewer.getSelectedEditParts().get(2));
			assertEquals(EditPart.SELECTED, editPart2.getSelected());
			assertEquals(EditPart.SELECTED, editPart3.getSelected());
			assertEquals(EditPart.SELECTED_PRIMARY, editPart4.getSelected());
			actualLogger.assertEmpty();
		}
		// deselection click (use Ctrl) on "editPart2"
		{
			tracker = new SelectEditPartTracker(editPart2);
			m_domain.setActiveTool(tracker);
			m_sender.setStateMask(SWT.CONTROL);
			m_sender.click(0, 0, 1);
			m_sender.setStateMask(0);
			//
			assertEquals(2, m_viewer.getSelectedEditParts().size());
			assertSame(editPart3, m_viewer.getSelectedEditParts().get(0));
			assertSame(editPart4, m_viewer.getSelectedEditParts().get(1));
			assertEquals(EditPart.SELECTED_NONE, editPart2.getSelected());
			assertEquals(EditPart.SELECTED, editPart3.getSelected());
			assertEquals(EditPart.SELECTED_PRIMARY, editPart4.getSelected());
			actualLogger.assertEmpty();
		}
		// double click on "editPart5"
		{
			tracker = new SelectEditPartTracker(editPart5);
			m_domain.setActiveTool(tracker);
			m_sender.doubleClick(1, 1, 1);
			//
			RequestsLogger expectedLogger = new RequestsLogger();
			//
			SelectionRequest request = new SelectionRequest();
			request.setType(RequestConstants.REQ_OPEN);
			request.setLocation(new Point(1, 1));
			//
			expectedLogger.log(editPart5, "performRequest", request);
			assertLoggers(expectedLogger, actualLogger);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// DragEditPartTracker
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_Drag_1() throws Exception {
		RequestsLogger actualLogger = new RequestsLogger();
		//
		RequestTestCaseEditPart editPart =
				addEditPart(m_viewer.getRootEditPart(), "ParentEditPart", actualLogger, 50, 50, 400, 300);
		//
		RequestTestCaseEditPart childEditPart1 =
				addEditPart(editPart, "ChildEditPart1", actualLogger, 50, 50, 70, 50);
		//
		RequestTestCaseEditPart childEditPart2 =
				addEditPart(editPart, "ChildEditPart2", actualLogger, 250, 25, 100, 155);
		//
		RequestsLogger expectedLogger = new RequestsLogger();
		//
		DragEditPartTracker tracker = new DragEditPartTracker(childEditPart1);
		m_domain.setActiveTool(tracker);
		//
		assertTrue(m_viewer.getSelectedEditParts().isEmpty());
		//
		// start drag "ChildEditPart1"
		{
			m_sender.startDrag(120, 120, 1);
			//
			assertEquals(1, m_viewer.getSelectedEditParts().size());
			assertSame(childEditPart1, m_viewer.getSelectedEditParts().get(0));
			assertEquals(EditPart.SELECTED_PRIMARY, childEditPart1.getSelected());
			//
			actualLogger.assertEmpty();
		}
		// drag
		{
			m_sender.dragTo(130, 120);
			//
			ChangeBoundsRequest request = new ChangeBoundsRequest(RequestConstants.REQ_ADD);
			//
			request.setEditParts(childEditPart1);
			//
			expectedLogger.log(childEditPart1, "performRequest", new DragPermissionRequest());
			//
			request.setLocation(new Point(130, 120));
			request.setMoveDelta(new Point(10, 0));
			request.setType(RequestConstants.REQ_MOVE);
			//
			expectedLogger.log(editPart, new String[]{"getTargetEditPart", "getTargetEditPart"}, request);
			//
			request.setType(RequestConstants.REQ_MOVE);
			//
			expectedLogger.log(editPart, "showTargetFeedback", request);
			//
			expectedLogger.log(editPart, new String[]{
					"getTargetEditPart",
					"getTargetEditPart",
					"showTargetFeedback",
			"getCommand"}, request);
			//
			assertLoggers(expectedLogger, actualLogger);
		}
		// drag
		{
			m_sender.dragTo(140, 120);
			//
			ChangeBoundsRequest request = new ChangeBoundsRequest(RequestConstants.REQ_MOVE);
			request.setEditParts(childEditPart1);
			request.setLocation(new Point(140, 120));
			request.setMoveDelta(new Point(20, 0));
			//
			expectedLogger.log(editPart, new String[]{
					"getTargetEditPart",
					"getTargetEditPart",
					"showTargetFeedback",
			"getCommand"}, request);
			//
			assertLoggers(expectedLogger, actualLogger);
		}
		// drag over "ChildEditPart2"
		{
			m_sender.dragTo(330, 120);
			//
			ChangeBoundsRequest request = new ChangeBoundsRequest(RequestConstants.REQ_ADD);
			request.setEditParts(childEditPart1);
			request.setLocation(new Point(330, 120));
			request.setMoveDelta(new Point(210, 0));
			//
			expectedLogger.log(
					childEditPart2,
					new String[]{"getTargetEditPart", "getTargetEditPart"},
					request);
			//
			expectedLogger.log(editPart, "eraseTargetFeedback", request);
			//
			request.setType(RequestConstants.REQ_ADD);
			//
			expectedLogger.log(childEditPart2, new String[]{
					"showTargetFeedback",
			"showTargetFeedback" }, request);
			//
			expectedLogger.log(editPart, "getCommand", new GroupRequest(RequestConstants.REQ_ORPHAN));
			//
			expectedLogger.log(childEditPart2, "getCommand", request);
			//
			assertLoggers(expectedLogger, actualLogger);
		}
		// end drag process
		{
			m_sender.endDrag();
			//
			ChangeBoundsRequest request = new ChangeBoundsRequest(RequestConstants.REQ_ADD);
			request.setEditParts(childEditPart1);
			request.setLocation(new Point(330, 120));
			request.setMoveDelta(new Point(210, 0));
			//
			expectedLogger.log(childEditPart2, "eraseTargetFeedback", request);
			assertLoggers(expectedLogger, actualLogger);
		}
		// check not work after deactivate tool
		{
			m_sender.startDrag(120, 120, 1);
			m_sender.dragTo(140, 120);
			m_sender.endDrag();
			actualLogger.assertEmpty();
		}
	}

	@Test
	public void test_Drag_2() throws Exception {
		RequestsLogger actualLogger = new RequestsLogger();
		//
		RequestTestCaseEditPart editPart =
				addEditPart(m_viewer.getRootEditPart(), "ParentEditPart", actualLogger, 50, 50, 400, 300);
		//
		RequestTestCaseEditPart childEditPart1 =
				new RequestTestCaseEditPart("ChildEditPart1", actualLogger) {
			@Override
			public void performRequest(Request request) {
				if (request instanceof DragPermissionRequest permissionRequest) {
					permissionRequest.setMove(false);
					permissionRequest.setReparent(false);
				}
				super.performRequest(request);
			}
		};
		editPart.getFigure().setBounds(new Rectangle(50, 50, 70, 50));
		addChildEditPart(editPart, childEditPart1);
		//
		addEditPart(editPart, "ChildEditPart2", actualLogger, 250, 25, 100, 155);
		//
		RequestsLogger expectedLogger = new RequestsLogger();
		//
		DragEditPartTracker tracker = new DragEditPartTracker(childEditPart1);
		m_domain.setActiveTool(tracker);
		//
		assertTrue(m_viewer.getSelectedEditParts().isEmpty());
		//
		// start drag "ChildEditPart1"
		{
			m_sender.startDrag(120, 120, 1);
			//
			assertEquals(1, m_viewer.getSelectedEditParts().size());
			assertSame(childEditPart1, m_viewer.getSelectedEditParts().get(0));
			assertEquals(EditPart.SELECTED_PRIMARY, childEditPart1.getSelected());
			//
			actualLogger.assertEmpty();
		}
		// drag
		{
			m_sender.dragTo(130, 120);
			//
			ChangeBoundsRequest request = new ChangeBoundsRequest(RequestConstants.REQ_MOVE);
			//
			request.setEditParts(childEditPart1);
			//
			DragPermissionRequest permissionRequest = new DragPermissionRequest();
			permissionRequest.setMove(false);
			permissionRequest.setReparent(false);
			expectedLogger.log(childEditPart1, "performRequest", permissionRequest);
			//
			assertLoggers(expectedLogger, actualLogger);
		}
		// drag
		{
			m_sender.dragTo(140, 120);
			actualLogger.assertEmpty();
		}
		// drag over "ChildEditPart2"
		{
			m_sender.dragTo(330, 120);
			actualLogger.assertEmpty();
		}
		// end drag process
		{
			m_sender.endDrag();
			actualLogger.assertEmpty();
		}
		// check not work after deactivate tool
		{
			m_sender.startDrag(120, 120, 1);
			m_sender.dragTo(140, 120);
			m_sender.endDrag();
			actualLogger.assertEmpty();
		}
	}

	@Test
	public void test_Drag_InvalidInput() throws Exception {
		RequestsLogger actualLogger = new RequestsLogger();
		//
		RequestTestCaseEditPart editPart =
				addEditPart(m_viewer.getRootEditPart(), "ParentEditPart", actualLogger, 50, 50, 400, 300);
		//
		RequestTestCaseEditPart childEditPart =
				addEditPart(editPart, "ChildEditPart", actualLogger, 50, 50, 70, 50);
		//
		RequestsLogger expectedLogger = new RequestsLogger();
		//
		DragEditPartTracker tracker = new DragEditPartTracker(childEditPart);
		m_domain.setActiveTool(tracker);
		// start drag "ChildEditPart"
		{
			m_sender.startDrag(120, 120, 1);
			actualLogger.assertEmpty();
		}
		// drag
		{
			m_sender.dragTo(130, 120);
			//
			ChangeBoundsRequest request = new ChangeBoundsRequest(RequestConstants.REQ_MOVE);
			//
			request.setEditParts(childEditPart);
			//
			expectedLogger.log(childEditPart, "performRequest", new DragPermissionRequest());
			//
			request.setLocation(new Point(130, 120));
			request.setMoveDelta(new Point(10, 0));
			//
			expectedLogger.log(editPart, new String[]{"getTargetEditPart", "getTargetEditPart"}, request);
			//
			request.setType(RequestConstants.REQ_MOVE);
			//
			expectedLogger.log(editPart, "showTargetFeedback", request);
			//
			expectedLogger.log(editPart, new String[]{
					"getTargetEditPart",
					"getTargetEditPart",
					"showTargetFeedback",
			"getCommand"}, request);
			//
			assertLoggers(expectedLogger, actualLogger);
		}
		// invalid input
		{
			m_sender.click(131, 121, 3);
			//
			ChangeBoundsRequest request = new ChangeBoundsRequest(RequestConstants.REQ_MOVE);
			request.setEditParts(childEditPart);
			request.setLocation(new Point(130, 120));
			request.setMoveDelta(new Point(10, 0));
			//
			expectedLogger.log(editPart, "eraseTargetFeedback", request);
			assertLoggers(expectedLogger, actualLogger);
		}
		// drag
		{
			m_sender.dragTo(140, 120);
			actualLogger.assertEmpty();
		}
		// end drag process
		{
			m_sender.endDrag();
			actualLogger.assertEmpty();
		}
	}

	@Test
	public void test_Drag_AncestorSelection() throws Exception {
		RequestsLogger actualLogger = new RequestsLogger();
		//
		RequestTestCaseEditPart editPart =
				addEditPart(m_viewer.getRootEditPart(), "ParentEditPart", actualLogger, 50, 50, 400, 300);
		//
		RequestTestCaseEditPart childEditPart1 =
				addEditPart(editPart, "ChildEditPart1", actualLogger, 50, 50, 70, 50);
		//
		RequestTestCaseEditPart childEditPart2 =
				addEditPart(childEditPart1, "ChildEditPart2", actualLogger, 10, 10, 50, 30);
		//
		RequestsLogger expectedLogger = new RequestsLogger();
		//
		m_viewer.appendSelection(childEditPart1);
		m_viewer.appendSelection(childEditPart2);
		m_domain.setActiveTool(new DragEditPartTracker(childEditPart1));
		//
		// start drag "ChildEditPart1" and "ChildEditPart2"
		{
			m_sender.startDrag(120, 120, 1);
			actualLogger.assertEmpty();
		}
		// drag
		{
			m_sender.dragTo(130, 120);
			//
			ChangeBoundsRequest request = new ChangeBoundsRequest(RequestConstants.REQ_MOVE);
			//
			request.setEditParts(childEditPart1);
			//
			expectedLogger.log(childEditPart1, "performRequest", new DragPermissionRequest());
			//
			request.setLocation(new Point(130, 120));
			request.setMoveDelta(new Point(10, 0));
			//
			expectedLogger.log(editPart, new String[]{"getTargetEditPart", "getTargetEditPart"}, request);
			//
			request.setType(RequestConstants.REQ_MOVE);
			//
			expectedLogger.log(editPart, "showTargetFeedback", request);
			//
			expectedLogger.log(editPart, new String[]{
					"getTargetEditPart",
					"getTargetEditPart",
					"showTargetFeedback",
			"getCommand"}, request);
			//
			assertLoggers(expectedLogger, actualLogger);
		}
		// end drag process
		{
			m_sender.endDrag();
			//
			ChangeBoundsRequest request = new ChangeBoundsRequest(RequestConstants.REQ_MOVE);
			request.setEditParts(childEditPart1);
			request.setLocation(new Point(130, 120));
			request.setMoveDelta(new Point(10, 0));
			//
			expectedLogger.log(editPart, "eraseTargetFeedback", request);
			assertLoggers(expectedLogger, actualLogger);
		}
	}

	@Test
	public void test_Drag_WrongSelection() throws Exception {
		RequestsLogger actualLogger = new RequestsLogger();
		//
		RequestTestCaseEditPart editPart =
				addEditPart(m_viewer.getRootEditPart(), "ParentEditPart", actualLogger, 50, 50, 400, 300);
		//
		RequestTestCaseEditPart childEditPart1 =
				addEditPart(editPart, "ChildEditPart1", actualLogger, 50, 50, 70, 50);
		//
		RequestTestCaseEditPart childEditPart2 =
				addEditPart(childEditPart1, "ChildEditPart2", actualLogger, 10, 10, 50, 30);
		//
		RequestTestCaseEditPart childEditPart3 =
				addEditPart(editPart, "ChildEditPart3", actualLogger, 250, 50, 70, 50);
		//
		m_viewer.appendSelection(childEditPart2);
		m_viewer.appendSelection(childEditPart3);
		m_domain.setActiveTool(new DragEditPartTracker(childEditPart3));
		//
		// start drag "ChildEditPart3" and "ChildEditPart2"
		{
			m_sender.startDrag(310, 120, 1);
			actualLogger.assertEmpty();
		}
		// drag
		{
			m_sender.dragTo(320, 120);
			//
			actualLogger.assertEmpty();
		}
		// end drag process
		{
			m_sender.endDrag();
			actualLogger.assertEmpty();
		}
	}

	@Test
	public void test_Drag_MultiSelection() throws Exception {
		RequestsLogger actualLogger = new RequestsLogger();
		//
		RequestTestCaseEditPart editPart =
				addEditPart(m_viewer.getRootEditPart(), "ParentEditPart", actualLogger, 50, 50, 400, 300);
		//
		RequestTestCaseEditPart childEditPart1 =
				addEditPart(editPart, "ChildEditPart1", actualLogger, 50, 50, 70, 50);
		//
		RequestTestCaseEditPart childEditPart2 =
				addEditPart(editPart, "ChildEditPart2", actualLogger, 250, 50, 70, 50);
		//
		RequestsLogger expectedLogger = new RequestsLogger();
		//
		m_viewer.appendSelection(childEditPart1);
		m_viewer.appendSelection(childEditPart2);
		m_domain.setActiveTool(new DragEditPartTracker(childEditPart2));
		//
		// start drag "ChildEditPart2" and "ChildEditPart1"
		{
			m_sender.startDrag(310, 120, 1);
			actualLogger.assertEmpty();
		}
		// drag
		{
			m_sender.dragTo(320, 120);
			//
			ChangeBoundsRequest request = new ChangeBoundsRequest(RequestConstants.REQ_MOVE);
			//
			request.setEditParts(List.of(childEditPart1, childEditPart2));
			//
			DragPermissionRequest permissionRequest = new DragPermissionRequest();
			expectedLogger.log(childEditPart1, "performRequest", permissionRequest);
			expectedLogger.log(childEditPart2, "performRequest", permissionRequest);
			//
			request.setLocation(new Point(320, 120));
			request.setMoveDelta(new Point(10, 0));
			//
			expectedLogger.log(editPart, new String[]{"getTargetEditPart", "getTargetEditPart"}, request);
			//
			request.setType(RequestConstants.REQ_MOVE);
			//
			expectedLogger.log(editPart, "showTargetFeedback", request);
			expectedLogger.log(editPart, new String[]{
					"getTargetEditPart",
					"getTargetEditPart",
					"showTargetFeedback",
			"getCommand"}, request);
			assertLoggers(expectedLogger, actualLogger);
		}
		// end drag process
		{
			m_sender.endDrag();
			//
			ChangeBoundsRequest request = new ChangeBoundsRequest(RequestConstants.REQ_MOVE);
			request.setEditParts(List.of(childEditPart1, childEditPart2));
			request.setLocation(new Point(320, 120));
			request.setMoveDelta(new Point(10, 0));
			//
			expectedLogger.log(editPart, "eraseTargetFeedback", request);
			assertLoggers(expectedLogger, actualLogger);
		}
	}
}