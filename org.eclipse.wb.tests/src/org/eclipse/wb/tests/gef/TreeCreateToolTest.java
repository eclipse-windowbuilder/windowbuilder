/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
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
import org.eclipse.wb.gef.tree.TreeEditPart;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;

import org.eclipse.draw2d.geometry.Point;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author lobas_av
 *
 */
public class TreeCreateToolTest extends TreeToolTest {
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
		m_domain.setActiveTool(new CreationTool(factory));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_Move_1() throws Exception {
		RequestsLogger actualLogger = new RequestsLogger();
		//
		ILayoutEditPolicy ipolicy = (request, editPart) -> true;
		//
		TreeEditPart parent = addEditPart(m_viewer.getRootEditPart(), "parent", actualLogger, ipolicy);
		TreeEditPart child1 = addEditPart(parent, "child1", actualLogger, ipolicy);
		TreeEditPart parent1 = addEditPart(parent, "parent1", actualLogger, ipolicy);
		TreeEditPart parent2 = addEditPart(parent, "parent2", actualLogger, ipolicy);
		//
		refreshTreeParst(parent);
		UiUtils.expandAll(m_viewer.getTree());
		//
		RequestsLogger expectedLogger = new RequestsLogger();
		//
		// move outside of any EditPart
		{
			m_sender.moveTo(470, 370);
			actualLogger.assertEmpty();
		}
		//----------------------------------------------------------------------------------
		{
			Point dropLocation = getBeforeLocation(parent);
			m_sender.moveTo(dropLocation.x, dropLocation.y);
			actualLogger.assertEmpty();
		}
		//
		{
			Point dropLocation = getOnLocation(parent);
			m_sender.moveTo(dropLocation.x, dropLocation.y);
			expectedLogger.log(parent, "getCreateCommand(object=_NewObject_, next=null)");
			assertLoggers(expectedLogger, actualLogger);
		}
		//
		{
			Point dropLocation = getAfterLocation(parent);
			m_sender.moveTo(dropLocation.x, dropLocation.y);
			actualLogger.assertEmpty();
		}
		//----------------------------------------------------------------------------------
		{
			Point dropLocation = getBeforeLocation(child1);
			m_sender.moveTo(dropLocation.x, dropLocation.y);
			expectedLogger.log(parent, "getCreateCommand(object=_NewObject_, next=child1)");
			assertLoggers(expectedLogger, actualLogger);
		}
		//
		{
			Point dropLocation = getOnLocation(child1);
			m_sender.moveTo(dropLocation.x, dropLocation.y);
			expectedLogger.log(child1, "getCreateCommand(object=_NewObject_, next=null)");
			assertLoggers(expectedLogger, actualLogger);
		}
		//
		{
			Point dropLocation = getAfterLocation(child1);
			m_sender.moveTo(dropLocation.x, dropLocation.y);
			expectedLogger.log(parent, "getCreateCommand(object=_NewObject_, next=parent1)");
			assertLoggers(expectedLogger, actualLogger);
		}
		//----------------------------------------------------------------------------------
		{
			Point dropLocation = getBeforeLocation(parent1);
			m_sender.moveTo(dropLocation.x, dropLocation.y);
			expectedLogger.log(parent, "getCreateCommand(object=_NewObject_, next=parent1)");
			assertLoggers(expectedLogger, actualLogger);
		}
		//
		{
			Point dropLocation = getOnLocation(parent1);
			m_sender.moveTo(dropLocation.x, dropLocation.y);
			expectedLogger.log(parent1, "getCreateCommand(object=_NewObject_, next=null)");
			assertLoggers(expectedLogger, actualLogger);
		}
		//
		{
			Point dropLocation = getAfterLocation(parent1);
			m_sender.moveTo(dropLocation.x, dropLocation.y);
			expectedLogger.log(parent, "getCreateCommand(object=_NewObject_, next=parent2)");
			assertLoggers(expectedLogger, actualLogger);
		}
		//----------------------------------------------------------------------------------
		{
			Point dropLocation = getAfterLocation(parent2);
			m_sender.moveTo(dropLocation.x, dropLocation.y);
			expectedLogger.log(parent, "getCreateCommand(object=_NewObject_, next=null)");
			assertLoggers(expectedLogger, actualLogger);
		}
	}

	@Test
	public void test_Move_2() throws Exception {
		RequestsLogger actualLogger = new RequestsLogger();
		//
		ILayoutEditPolicy ipolicy = (request, editPart) -> true;
		//
		TreeEditPart parent = addEditPart(m_viewer.getRootEditPart(), "parent", actualLogger, ipolicy);
		TreeEditPart child1 = addEditPart(parent, "child1", actualLogger, null);
		addEditPart(parent, "parent1", actualLogger, ipolicy);
		//
		refreshTreeParst(parent);
		UiUtils.expandAll(m_viewer.getTree());
		//
		RequestsLogger expectedLogger = new RequestsLogger();
		//
		// move outside of any EditPart
		{
			m_sender.moveTo(470, 370);
			actualLogger.assertEmpty();
		}
		//
		{
			Point dropLocation = getBeforeLocation(child1);
			m_sender.moveTo(dropLocation.x, dropLocation.y);
			expectedLogger.log(parent, "getCreateCommand(object=_NewObject_, next=child1)");
			assertLoggers(expectedLogger, actualLogger);
		}
		//
		{
			Point dropLocation = getOnLocation(child1);
			m_sender.moveTo(dropLocation.x, dropLocation.y);
			expectedLogger.log(parent, "getCreateCommand(object=_NewObject_, next=parent1)");
			assertLoggers(expectedLogger, actualLogger);
		}
		//
		{
			Point dropLocation = getAfterLocation(child1);
			m_sender.moveTo(dropLocation.x, dropLocation.y);
			expectedLogger.log(parent, "getCreateCommand(object=_NewObject_, next=parent1)");
			assertLoggers(expectedLogger, actualLogger);
		}
	}

	@Test
	public void test_Move_3() throws Exception {
		RequestsLogger actualLogger = new RequestsLogger();
		//
		ILayoutEditPolicy ipolicy = (request, editPart) -> true;
		//
		TreeEditPart parent = addEditPart(m_viewer.getRootEditPart(), "parent", actualLogger, (request, editPart) -> !"child1".equals(editPart.getModel()));
		TreeEditPart child1 = addEditPart(parent, "child1", actualLogger, ipolicy);
		addEditPart(parent, "parent1", actualLogger, ipolicy);
		//
		refreshTreeParst(parent);
		UiUtils.expandAll(m_viewer.getTree());
		//
		RequestsLogger expectedLogger = new RequestsLogger();
		//
		// move outside of any EditPart
		{
			m_sender.moveTo(470, 370);
			actualLogger.assertEmpty();
		}
		//
		{
			Point dropLocation = getBeforeLocation(child1);
			m_sender.moveTo(dropLocation.x, dropLocation.y);
			actualLogger.assertEmpty();
		}
		//
		{
			Point dropLocation = getOnLocation(child1);
			m_sender.moveTo(dropLocation.x, dropLocation.y);
			expectedLogger.log(child1, "getCreateCommand(object=_NewObject_, next=null)");
			assertLoggers(expectedLogger, actualLogger);
		}
		//
		{
			Point dropLocation = getAfterLocation(child1);
			m_sender.moveTo(dropLocation.x, dropLocation.y);
			actualLogger.assertEmpty();
		}
	}
}