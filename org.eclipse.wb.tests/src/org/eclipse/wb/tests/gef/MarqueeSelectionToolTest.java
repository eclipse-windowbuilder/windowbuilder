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

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.tools.MarqueeSelectionTool;
import org.eclipse.gef.util.EditPartUtilities;
import org.eclipse.swt.SWT;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

/**
 * @author lobas_av
 *
 */
public class MarqueeSelectionToolTest extends RequestTestCase {
	private MarqueeSelectionTool m_tool;
	private Request m_request;

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
		m_tool = new TestMarqueeSelectionTool();
		m_domain.setActiveTool(m_tool);
		//
		m_request = new Request(RequestConstants.REQ_SELECTION);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_Selection() throws Exception {
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
		// start drag process of selection "ChildEditPart1"
		{
			m_sender.startDrag(95, 95, 1);
			//
			assertTrue(m_viewer.getSelectedEditParts().isEmpty());
			actualLogger.assertEmpty();
		}
		// drag
		{
			m_sender.dragTo(175, 175);
			//
			assertTrue(m_viewer.getSelectedEditParts().isEmpty());
			//
			expectedLogger.log(
					childEditPart1,
					new String[]{"getTargetEditPart", "showTargetFeedback"},
					m_request);
			assertLoggers(expectedLogger, actualLogger);
		}
		// drag
		{
			m_sender.dragTo(177, 177);
			//
			assertTrue(m_viewer.getSelectedEditParts().isEmpty());
			//
			expectedLogger.log(childEditPart1, new String[]{
					"eraseTargetFeedback",
					"getTargetEditPart",
			"showTargetFeedback"}, m_request);
			assertLoggers(expectedLogger, actualLogger);
		}
		// end drag process
		{
			m_sender.endDrag();
			//
			assertEquals(1, m_viewer.getSelectedEditParts().size());
			assertSame(childEditPart1, m_viewer.getSelectedEditParts().get(0));
			assertEquals(EditPart.SELECTED_PRIMARY, childEditPart1.getSelected());
			//
			expectedLogger.log(
					childEditPart1,
					new String[]{"eraseTargetFeedback", "getTargetEditPart"},
					m_request);
			assertLoggers(expectedLogger, actualLogger);
		}
		// start drag process of append selection "ChildEditPart2"
		{
			m_domain.setActiveTool(m_tool);
			m_sender.setStateMask(SWT.SHIFT);
			m_sender.startDrag(295, 60, 1);
			//
			actualLogger.assertEmpty();
		}
		// drag
		{
			m_sender.dragTo(405, 235);
			//
			expectedLogger.log(childEditPart1, "eraseTargetFeedback", m_request);
			expectedLogger.log(
					childEditPart2,
					new String[]{"getTargetEditPart", "showTargetFeedback"},
					m_request);
			assertLoggers(expectedLogger, actualLogger);
		}
		// end drag process
		{
			m_sender.endDrag();
			m_sender.setStateMask(0);
			//
			assertEquals(2, m_viewer.getSelectedEditParts().size());
			assertSame(childEditPart2, m_viewer.getSelectedEditParts().get(0));
			assertSame(childEditPart1, m_viewer.getSelectedEditParts().get(1));
			assertEquals(EditPart.SELECTED_PRIMARY, childEditPart1.getSelected());
			assertEquals(EditPart.SELECTED, childEditPart2.getSelected());
			//
			expectedLogger.log(
					childEditPart2,
					new String[]{"eraseTargetFeedback", "getTargetEditPart"},
					m_request);
			assertLoggers(expectedLogger, actualLogger);
		}
		// start drag process of all selection
		{
			m_domain.setActiveTool(m_tool);
			m_sender.startDrag(10, 10, 1);
			//
			actualLogger.assertEmpty();
		}
		// drag
		{
			m_sender.dragTo(460, 360);
			//
			expectedLogger.log(childEditPart2, "eraseTargetFeedback", m_request);
			expectedLogger.log(editPart, "getTargetEditPart", m_request);
			expectedLogger.log(childEditPart1, "getTargetEditPart", m_request);
			expectedLogger.log(childEditPart2, "getTargetEditPart", m_request);
			expectedLogger.log(editPart, "showTargetFeedback", m_request);
			expectedLogger.log(childEditPart1, "showTargetFeedback", m_request);
			expectedLogger.log(childEditPart2, "showTargetFeedback", m_request);
			assertLoggers(expectedLogger, actualLogger);
		}
		// end drag process
		{
			m_sender.endDrag();
			//
			assertEquals(3, m_viewer.getSelectedEditParts().size());
			assertSame(editPart, m_viewer.getSelectedEditParts().get(0));
			assertSame(childEditPart1, m_viewer.getSelectedEditParts().get(1));
			assertSame(childEditPart2, m_viewer.getSelectedEditParts().get(2));
			assertEquals(EditPart.SELECTED, editPart.getSelected());
			assertEquals(EditPart.SELECTED, childEditPart1.getSelected());
			assertEquals(EditPart.SELECTED_PRIMARY, childEditPart2.getSelected());
			//
			expectedLogger.log(editPart, "eraseTargetFeedback", m_request);
			expectedLogger.log(childEditPart1, "eraseTargetFeedback", m_request);
			expectedLogger.log(childEditPart2, "eraseTargetFeedback", m_request);
			expectedLogger.log(editPart, "getTargetEditPart", m_request);
			expectedLogger.log(childEditPart1, "getTargetEditPart", m_request);
			expectedLogger.log(childEditPart2, "getTargetEditPart", m_request);
			assertLoggers(expectedLogger, actualLogger);
		}
		// start drag process of deselection "ChildEditPart1" and "ChildEditPart2"
		{
			m_domain.setActiveTool(m_tool);
			m_sender.setStateMask(SWT.CONTROL);
			m_sender.startDrag(95, 60, 1);
			//
			actualLogger.assertEmpty();
		}
		// drag
		{
			m_sender.dragTo(405, 235);
			//
			expectedLogger.log(editPart, "eraseTargetFeedback", m_request);
			expectedLogger.log(childEditPart1, "eraseTargetFeedback", m_request);
			expectedLogger.log(childEditPart2, "eraseTargetFeedback", m_request);
			expectedLogger.log(childEditPart1, "getTargetEditPart", m_request);
			expectedLogger.log(childEditPart2, "getTargetEditPart", m_request);
			expectedLogger.log(childEditPart1, "showTargetFeedback", m_request);
			expectedLogger.log(childEditPart2, "showTargetFeedback", m_request);
			assertLoggers(expectedLogger, actualLogger);
		}
		// end drag process
		{
			m_sender.endDrag();
			m_sender.setStateMask(0);
			//
			assertEquals(1, m_viewer.getSelectedEditParts().size());
			assertSame(editPart, m_viewer.getSelectedEditParts().get(0));
			assertEquals(EditPart.SELECTED_PRIMARY, editPart.getSelected());
			assertEquals(EditPart.SELECTED_NONE, childEditPart1.getSelected());
			assertEquals(EditPart.SELECTED_NONE, childEditPart2.getSelected());
			//
			expectedLogger.log(childEditPart1, "eraseTargetFeedback", m_request);
			expectedLogger.log(childEditPart2, "eraseTargetFeedback", m_request);
			expectedLogger.log(childEditPart1, "getTargetEditPart", m_request);
			expectedLogger.log(childEditPart2, "getTargetEditPart", m_request);
			assertLoggers(expectedLogger, actualLogger);
		}
		// start drag process of selection "ChildEditPart1" and deselection "ChildEditPart2"
		m_viewer.appendSelection(childEditPart2);
		assertEquals(EditPart.SELECTED_PRIMARY, childEditPart2.getSelected());
		//
		{
			m_domain.setActiveTool(m_tool);
			m_sender.setStateMask(SWT.CONTROL);
			m_sender.startDrag(95, 60, 1);
			//
			actualLogger.assertEmpty();
		}
		// drag
		{
			m_sender.dragTo(405, 235);
			//
			expectedLogger.log(childEditPart1, "eraseTargetFeedback", m_request);
			expectedLogger.log(childEditPart2, "eraseTargetFeedback", m_request);
			expectedLogger.log(childEditPart1, "getTargetEditPart", m_request);
			expectedLogger.log(childEditPart2, "getTargetEditPart", m_request);
			expectedLogger.log(childEditPart1, "showTargetFeedback", m_request);
			expectedLogger.log(childEditPart2, "showTargetFeedback", m_request);
			assertLoggers(expectedLogger, actualLogger);
		}
		// end drag process
		{
			m_sender.endDrag();
			m_sender.setStateMask(0);
			//
			assertEquals(2, m_viewer.getSelectedEditParts().size());
			assertSame(childEditPart1, m_viewer.getSelectedEditParts().get(0));
			assertSame(editPart, m_viewer.getSelectedEditParts().get(1));
			assertEquals(EditPart.SELECTED_PRIMARY, editPart.getSelected());
			assertEquals(EditPart.SELECTED, childEditPart1.getSelected());
			assertEquals(EditPart.SELECTED_NONE, childEditPart2.getSelected());
			//
			expectedLogger.log(childEditPart1, "eraseTargetFeedback", m_request);
			expectedLogger.log(childEditPart2, "eraseTargetFeedback", m_request);
			expectedLogger.log(childEditPart1, "getTargetEditPart", m_request);
			expectedLogger.log(childEditPart2, "getTargetEditPart", m_request);
			assertLoggers(expectedLogger, actualLogger);
		}
		// check not work after deactivate tool
		{
			m_sender.startDrag(10, 10, 1);
			m_sender.dragTo(100, 100);
			m_sender.endDrag();
			//
			assertEquals(0, m_viewer.getSelectedEditParts().size());
			assertEquals(EditPart.SELECTED_NONE, childEditPart1.getSelected());
			assertEquals(EditPart.SELECTED_NONE, editPart.getSelected());
			assertEquals(EditPart.SELECTED_NONE, childEditPart2.getSelected());
			//
			expectedLogger.log(childEditPart1, "eraseTargetFeedback", m_request);
			expectedLogger.log(childEditPart2, "eraseTargetFeedback", m_request);
			assertLoggers(expectedLogger, actualLogger);
		}
	}

	@Test
	public void test_Selection_isSelectable() throws Exception {
		RequestsLogger actualLogger = new RequestsLogger();
		//
		RequestTestCaseEditPart editPart =
				addEditPart(m_viewer.getRootEditPart(), "ParentEditPart", actualLogger, 50, 50, 400, 300);
		//
		RequestTestCaseEditPart childEditPart1 =
				addEditPart(editPart, "ChildEditPart1", actualLogger, 50, 50, 70, 50);
		//
		RequestTestCaseEditPart childEditPart2 =
				new RequestTestCaseEditPart("ChildEditPart2", actualLogger) {
			@Override
			public boolean isSelectable() {
				return false;
			}
		};
		childEditPart2.getFigure().setBounds(new Rectangle(250, 25, 100, 155));
		addChildEditPart(editPart, childEditPart2);
		//
		RequestsLogger expectedLogger = new RequestsLogger();
		//
		// start drag process over "ChildEditPart1" and "ChildEditPart2"
		{
			m_sender.startDrag(95, 60, 1);
			//
			assertTrue(m_viewer.getSelectedEditParts().isEmpty());
			actualLogger.assertEmpty();
		}
		// drag
		{
			m_sender.dragTo(405, 235);
			//
			expectedLogger.log(childEditPart1, "getTargetEditPart", m_request);
			expectedLogger.log(childEditPart2, "getTargetEditPart", m_request);
			expectedLogger.log(childEditPart1, "showTargetFeedback", m_request);
			assertLoggers(expectedLogger, actualLogger);
		}
		// end drag process
		{
			m_sender.endDrag();
			//
			assertEquals(1, m_viewer.getSelectedEditParts().size());
			assertSame(childEditPart1, m_viewer.getSelectedEditParts().get(0));
			assertEquals(EditPart.SELECTED_PRIMARY, childEditPart1.getSelected());
			assertEquals(EditPart.SELECTED_NONE, editPart.getSelected());
			assertEquals(EditPart.SELECTED_NONE, childEditPart2.getSelected());
			//
			expectedLogger.log(childEditPart1, "eraseTargetFeedback", m_request);
			expectedLogger.log(childEditPart1, "getTargetEditPart", m_request);
			expectedLogger.log(childEditPart2, "getTargetEditPart", m_request);
			assertLoggers(expectedLogger, actualLogger);
		}
	}

	@Test
	public void test_Selection_InvalidInput() throws Exception {
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
		// start drag process of selection "ChildEditPart1"
		{
			assertTrue(m_viewer.getSelectedEditParts().isEmpty());
			//
			m_sender.startDrag(95, 95, 1);
			//
			actualLogger.assertEmpty();
		}
		// drag
		{
			m_sender.dragTo(175, 175);
			//
			expectedLogger.log(
					childEditPart,
					new String[]{"getTargetEditPart", "showTargetFeedback"},
					m_request);
			assertLoggers(expectedLogger, actualLogger);
		}
		// invalid input
		{
			m_sender.click(175, 175, 3);
			//
			assertTrue(m_viewer.getSelectedEditParts().isEmpty());
			//
			expectedLogger.log(childEditPart, "eraseTargetFeedback", m_request);
			assertLoggers(expectedLogger, actualLogger);
		}
		// deactivate
		{
			m_domain.setActiveTool(m_tool);
			expectedLogger.assertEmpty();
			//
			m_sender.startDrag(95, 95, 1);
			m_sender.dragTo(175, 175);
			//
			expectedLogger.log(
					childEditPart,
					new String[] { "eraseTargetFeedback", "getTargetEditPart", "showTargetFeedback" },
					m_request);
			assertLoggers(expectedLogger, actualLogger);
			//
			m_domain.loadDefaultTool();
			//
			expectedLogger.log(childEditPart, "eraseTargetFeedback", m_request);
			assertLoggers(expectedLogger, actualLogger);
		}
	}

	/**
	 * Marquee selection tool with a deterministic algorithm for calculation the
	 * selected edit-parts.
	 */
	private static class TestMarqueeSelectionTool extends MarqueeSelectionTool {
		@Override
		protected Collection<? extends GraphicalEditPart> calculateMarqueeSelectedEditParts() {
			return EditPartUtilities.getAllChildren((GraphicalEditPart) getCurrentViewer().getRootEditPart()).stream() //
					.filter(this::isPrimaryMarqueeSelectable) //
					.filter(this::isMarqueeSelectable) //
					.toList();
		}

		/**
		 * Determines which edit parts are directly affected by the current marquee
		 * selection. Calculation is performed by regarding the current marquee
		 * selection rectangle ( {@link #getCurrentMarqueeSelectionRectangle()}).
		 *
		 * @param editPart the {@link EditPart} whose state is to be determined
		 * @return {@code true} if the {@link EditPart} should be regarded as being
		 *         included in the current marquee selection, {@code false} otherwise
		 */
		private boolean isPrimaryMarqueeSelectable(EditPart editPart) {
			// figure bounds are used to determine if edit part is included in selection
			IFigure figure = ((GraphicalEditPart) editPart).getFigure();
			Rectangle r = figure.getBounds().getCopy();
			figure.translateToAbsolute(r);

			Rectangle marqueeSelectionRectangle = getCurrentMarqueeSelectionRectangle();
			return marqueeSelectionRectangle.contains(r);
		}
	}
}