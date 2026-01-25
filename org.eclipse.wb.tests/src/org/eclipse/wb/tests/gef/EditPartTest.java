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

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.graphical.DesignEditPart;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.draw2d.EventListenerList;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartListener;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lobas_av
 *
 */
public class EditPartTest extends GefTestCase {

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_init() throws Exception {
		TestEditPart templatePart = new TestEditPart();
		IFigure templateFigure = templatePart.getFigure();
		EditPartViewer templateViewer = templatePart.getViewer();
		//
		// check new EditPart
		assertNull(templatePart.getParent());
		assertTrue(templatePart.getChildren().isEmpty());
		assertNotNull(templatePart.getFigure());
		assertNull(templateViewer.getVisualPartMap().get(templateFigure));
		assertNull(templatePart.getFigure().getParent());
		assertEquals(templatePart.getFigure(), templatePart.getContentPane());
		assertEquals(org.eclipse.gef.EditPart.SELECTED_NONE, templatePart.getSelected());
	}

	@Test
	public void test_Model() throws Exception {
		TestEditPart testEditPart = new TestEditPart();
		//
		// check new EditPart
		assertNull(testEditPart.getModel());
		assertNotNull(testEditPart.test_access_getModelChildren());
		assertTrue(testEditPart.test_access_getModelChildren().isEmpty());
		//
		// check setModel
		Object model = 9;
		testEditPart.setModel(model);
		assertSame(model, testEditPart.getModel());
		assertTrue(testEditPart.test_access_getModelChildren().isEmpty());
		//
		// check assert setModel
		model = "___ZZZZZZZ_";
		testEditPart.setModel(model);
		assertSame(model, testEditPart.getModel());
		assertTrue(testEditPart.test_access_getModelChildren().isEmpty());
	}

	@Test
	public void test_Figure() throws Exception {
		final Figure figure = new Figure();
		GraphicalEditPart testEditPart = new DesignEditPart() {
			@Override
			protected Figure createFigure() {
				return figure;
			}
		};
		//
		// check get and create figure
		assertSame(figure, testEditPart.getFigure());
		assertSame(figure, testEditPart.getContentPane());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Parent/Children tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_addChild() throws Exception {
		TestEditPart templatePart = new TestEditPart();
		IEditPartViewer templateViewer = templatePart.getViewer();
		assertTrue(templatePart.getChildren().isEmpty());
		/*
		 * check add 'null' child EditPart
		 */
		try {
			templatePart.test_access_addChild(null, 0);
			fail();
		} catch (RuntimeException e) {
			assertEquals("null argument:", e.getMessage());
		}
		/*
		 * check 'null' parent on new EditPart
		 */
		TestEditPart testPart1 = new TestEditPart(templateViewer);
		assertNull(testPart1.getParent());
		/*
		 * check add EditPart from wrong index (positive)
		 */
		try {
			templatePart.test_access_addChild(testPart1, 1);
			fail();
		} catch (IndexOutOfBoundsException e) {
			assertTrue("Index: 1, Size: 0".equals(e.getMessage()));
		}
		//
		assertTrue(templatePart.getChildren().isEmpty());
		assertNull(testPart1.getParent());
		/*
		 * check add EditPart from wrong index (negative)
		 */
		try {
			templatePart.test_access_addChild(testPart1, -2);
			fail();
		} catch (IndexOutOfBoundsException e) {
			assertTrue("Index: -2, Size: 0".equals(e.getMessage()));
		}
		//
		assertTrue(templatePart.getChildren().isEmpty());
		assertNull(testPart1.getParent());
		/*
		 * check add EditPart before activate()
		 */
		templatePart.test_access_addChild(testPart1, 0);
		assertSame(templatePart, testPart1.getParent());
		assertEquals(1, templatePart.getChildren().size());
		assertEquals(templatePart.getChildren().get(0), testPart1);
		assertEquals(templatePart.getFigure(), testPart1.getFigure().getParent());
		assertEquals(testPart1, templateViewer.getVisualPartMap().get(testPart1.getFigure()));
		assertFalse(templatePart.isActive());
		assertFalse(testPart1.isActive());
		/*
		 * check add EditPart after activate()
		 */
		templatePart.activate();
		TestEditPart testPart2 = new TestEditPart(templateViewer);
		assertNull(testPart2.getParent());
		templatePart.test_access_addChild(testPart2, -1);
		assertSame(templatePart, testPart2.getParent());
		assertEquals(2, templatePart.getChildren().size());
		assertEquals(templatePart.getChildren().get(1), testPart2);
		assertEquals(templatePart.getFigure(), testPart2.getFigure().getParent());
		assertEquals(testPart2, templateViewer.getVisualPartMap().get(testPart2.getFigure()));
		assertTrue(templatePart.isActive());
		assertTrue(testPart1.isActive());
	}

	@Test
	public void test_removeChild() throws Exception {
		TestEditPart templatePart = new TestEditPart();
		EditPartViewer templateViewer = templatePart.getViewer();
		TestEditPart testPart1 = new TestEditPart();
		//
		// check remove EditPart before activate()
		testPart1.activate();
		templatePart.test_access_addChild(testPart1, -1);
		templatePart.test_access_removeChild(testPart1);
		assertTrue(templatePart.getChildren().isEmpty());
		assertTrue(testPart1.isActive());
		assertNull(testPart1.getParent());
		assertNull(testPart1.getFigure().getParent());
		assertNull(templateViewer.getVisualPartMap().get(testPart1.getFigure()));
		//
		// check remove EditPart after activate()
		templatePart.activate();
		templatePart.test_access_addChild(testPart1, -1);
		templatePart.test_access_removeChild(testPart1);
		assertTrue(templatePart.getChildren().isEmpty());
		assertFalse(testPart1.isActive());
		assertNull(testPart1.getParent());
		assertNull(testPart1.getFigure().getParent());
		assertNull(templateViewer.getVisualPartMap().get(testPart1.getFigure()));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// EditPolicy tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_installEditPolicy() throws Exception {
		TestEditPart testEditPart = new TestEditPart();
		//
		// check EditPolicies for new EditPart
		assertNotNull(testEditPart.getEditPolicies());
		assertTrue(testEditPart.getEditPolicies().isEmpty());
		//
		// check install EditPolicy use 'null' key
		try {
			testEditPart.installEditPolicy(null, new TestEditPolicy());
			fail();
		} catch (Throwable t) {
			assertEquals("null argument:Edit Policies must be installed with keys", t.getMessage());
		}
		//
		// check install EditPolicy
		TestEditPolicy policy1 = new TestEditPolicy();
		assertNull(policy1.getHost());
		assertFalse(policy1.isActive());
		//
		testEditPart.installEditPolicy("_Policy1_", policy1);
		assertEquals(1, testEditPart.getEditPolicies().size());
		assertEquals(policy1, testEditPart.getEditPolicies().get(0));
		assertEquals(policy1, testEditPart.getEditPolicy("_Policy1_"));
		assertFalse(policy1.isActive());
		assertSame(testEditPart, policy1.getHost());
		//
		// again check install EditPolicy
		TestEditPolicy policy2 = new TestEditPolicy();
		assertNull(policy2.getHost());
		assertFalse(policy2.isActive());
		//
		testEditPart.activate();
		testEditPart.installEditPolicy("_Policy2_", policy2);
		assertEquals(2, testEditPart.getEditPolicies().size());
		assertEquals(policy1, testEditPart.getEditPolicies().get(0));
		assertEquals(policy2, testEditPart.getEditPolicies().get(1));
		assertEquals(policy2, testEditPart.getEditPolicy("_Policy2_"));
		assertSame(testEditPart, policy2.getHost());
		assertTrue(policy2.isActive());
		//
		// check install EditPolicy use exist key
		TestEditPolicy policy2_new = new TestEditPolicy();
		assertNull(policy2_new.getHost());
		assertFalse(policy2_new.isActive());
		//
		testEditPart.installEditPolicy("_Policy2_", policy2_new);
		assertEquals(2, testEditPart.getEditPolicies().size());
		assertEquals(policy1, testEditPart.getEditPolicies().get(0));
		assertEquals(policy2_new, testEditPart.getEditPolicies().get(1));
		assertEquals(policy2_new, testEditPart.getEditPolicy("_Policy2_"));
		assertSame(testEditPart, policy2_new.getHost());
		assertTrue(policy2_new.isActive());
		assertFalse(policy2.isActive());
	}

	@Test
	public void test_RouteRequests() throws Exception {
		//
		// check route Request's
		//
		TestEditPart testEditPart = new TestEditPart();
		Command dummy = new Command() {};
		//
		final RequestsLogger actualLogger = new RequestsLogger();
		TestEditPolicy editPolicy = new TestEditPolicy() {
			@Override
			public Command getCommand(Request request) {
				actualLogger.log(getHost(), "getCommand", request);
				return dummy;
			}

			@Override
			public EditPart getTargetEditPart(Request request) {
				actualLogger.log(getHost(), "getTargetEditPart", request);
				return getHost();
			}

			@Override
			public boolean understandsRequest(Request request) {
				actualLogger.log(getHost(), "understandsRequest", request);
				return true;
			}

			@Override
			public void showSourceFeedback(Request request) {
				actualLogger.log(getHost(), "showSourceFeedback", request);
			}

			@Override
			public void eraseSourceFeedback(Request request) {
				actualLogger.log(getHost(), "eraseSourceFeedback", request);
			}

			@Override
			public void showTargetFeedback(Request request) {
				actualLogger.log(getHost(), "showTargetFeedback", request);
			}

			@Override
			public void eraseTargetFeedback(Request request) {
				actualLogger.log(getHost(), "eraseTargetFeedback", request);
			}
		};
		//
		Request request = new Request(RequestConstants.REQ_MOVE);
		//
		assertNull(testEditPart.getCommand(request));
		assertNull(testEditPart.getTargetEditPart(request));
		//
		testEditPart.installEditPolicy("", editPolicy);
		//
		// check route before activate()
		assertSame(dummy, testEditPart.getCommand(request));
		assertSame(testEditPart, testEditPart.getTargetEditPart(request));
		testEditPart.showSourceFeedback(request);
		testEditPart.eraseSourceFeedback(request);
		testEditPart.showTargetFeedback(request);
		testEditPart.eraseTargetFeedback(request);
		//
		RequestsLogger expectedLogger = new RequestsLogger();
		expectedLogger.log(testEditPart, new String[]{
				"understandsRequest",
				"getCommand",
				"understandsRequest",
		"getTargetEditPart"}, request);
		actualLogger.assertEquals(expectedLogger);
		actualLogger.clear();
		expectedLogger.clear();
		//
		// check route after activate()
		testEditPart.activate();
		assertSame(dummy, testEditPart.getCommand(request));
		assertSame(testEditPart, testEditPart.getTargetEditPart(request));
		testEditPart.showSourceFeedback(request);
		testEditPart.eraseSourceFeedback(request);
		testEditPart.showTargetFeedback(request);
		testEditPart.eraseTargetFeedback(request);
		//
		expectedLogger.log(testEditPart, new String[]{
				//
				"understandsRequest",
				"getCommand",
				//
				"understandsRequest",
				//
				"getTargetEditPart",
				//
				"understandsRequest",
				"showSourceFeedback",
				//
				"understandsRequest",
				"eraseSourceFeedback",
				//
				"understandsRequest",
				"showTargetFeedback",
				//
				"understandsRequest",
		"eraseTargetFeedback"}, request);
		actualLogger.assertEquals(expectedLogger);
		actualLogger.clear();
		expectedLogger.clear();
	}

	private static class TestEditPolicy extends EditPolicy {
		private boolean m_isActive;

		////////////////////////////////////////////////////////////////////////////
		//
		// EditPolicy
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public void activate() {
			super.activate();
			m_isActive = true;
		}

		@Override
		public void deactivate() {
			m_isActive = false;
			super.deactivate();
		}

		@Override
		public boolean isActive() {
			return m_isActive;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// add\removeNotyfy/Activate\Deactivate tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_commonAdd() throws Exception {
		TestEditPart parent = new TestEditPart();
		//
		// check create EditPolicies during add child EditPart
		TestEditPart child = new TestEditPart() {
			@Override
			protected void createEditPolicies() {
				installEditPolicy("", new TestEditPolicy());
			}
		};
		assertFalse(parent.isActive());
		assertFalse(child.isActive());
		assertNull(child.getEditPolicy(""));
		//
		parent.test_access_addChild(child, -1);
		//
		assertFalse(child.isActive());
		assertNotNull(child.getEditPolicy(""));
		assertFalse(((TestEditPolicy) child.getEditPolicy("")).isActive());
		assertSame(child, child.getEditPolicy("").getHost());
		//
		// check activate child EditPart and it EditPolicies
		parent.activate();
		//
		assertTrue(parent.isActive());
		assertTrue(child.isActive());
		assertTrue(((TestEditPolicy) child.getEditPolicy("")).isActive());
	}

	@Test
	public void test_commonAdd_activate() throws Exception {
		TestEditPart parent = new TestEditPart();
		parent.activate();
		//
		// check create and activate EditPolicies during add child EditPart if parent already activate
		TestEditPart child = new TestEditPart() {
			@Override
			protected void createEditPolicies() {
				installEditPolicy("", new TestEditPolicy());
			}
		};
		assertTrue(parent.isActive());
		assertFalse(child.isActive());
		assertNull(child.getEditPolicy(""));
		//
		parent.test_access_addChild(child, -1);
		//
		assertTrue(child.isActive());
		assertNotNull(child.getEditPolicy(""));
		assertTrue(((TestEditPolicy) child.getEditPolicy("")).isActive());
		assertSame(child, child.getEditPolicy("").getHost());
	}

	@Test
	public void test_commonRemove() throws Exception {
		TestEditPart parent = new TestEditPart();
		parent.activate();
		//
		// check deactivate EditPolicies from child EditPart after it remove
		TestEditPart child = new TestEditPart() {
			@Override
			protected void createEditPolicies() {
				installEditPolicy("", new TestEditPolicy());
			}
		};
		assertTrue(parent.isActive());
		assertFalse(child.isActive());
		assertNull(child.getEditPolicy(""));
		//
		parent.test_access_addChild(child, -1);
		//
		assertTrue(child.isActive());
		assertNotNull(child.getEditPolicy(""));
		assertTrue(((TestEditPolicy) child.getEditPolicy("")).isActive());
		assertSame(child, child.getEditPolicy("").getHost());
		//
		parent.test_access_removeChild(child);
		assertFalse(child.isActive());
		assertFalse(((TestEditPolicy) child.getEditPolicy("")).isActive());
		assertSame(child, child.getEditPolicy("").getHost());
	}

	@Test
	public void test_refreshChildren_1() throws Exception {
		TestEditPart child1 = new TestEditPart();
		child1.setModel("_child1_Model");
		//
		TestEditPart child2 = new TestEditPart();
		child2.setModel("_child2_Model");
		//
		TestEditPart parent = new TestEditPart() {
			@Override
			protected EditPart createChild(Object model) {
				TestEditPart child = new TestEditPart();
				child.setModel(model);
				return child;
			}

			@Override
			protected List<Object> getModelChildren() {
				List<Object> modelList = new ArrayList<>();
				modelList.add("_child3_Model");
				modelList.add("_child1_Model");
				modelList.add("_child5_Model");
				return modelList;
			}
		};
		parent.activate();
		parent.test_access_addChild(child1, -1);
		parent.test_access_addChild(child2, -1);
		//
		// check add child1 and child2
		List<? extends EditPart> children = parent.getChildren();
		assertNotNull(children);
		assertEquals(2, children.size());
		assertSame(child1, children.get(0));
		assertSame(child2, children.get(1));
		assertSame(parent, child1.getParent());
		assertSame(parent, child2.getParent());
		//
		// check remove child2, move child1 and cread new EditPart
		parent.refresh();
		assertEquals(3, children.size());
		assertEquals("_child3_Model", children.get(0).getModel());
		assertSame(child1, children.get(1));
		assertEquals("_child5_Model", children.get(2).getModel());
		assertNull(child2.getParent());
		assertFalse(child2.isActive());
	}

	@Test
	public void test_refreshChildren_2() throws Exception {
		TestEditPart parent = new TestEditPart() {
			@Override
			protected EditPart createChild(Object model) {
				if ("_child3_Model".equals(model)) {
					return null;
				}
				TestEditPart child = new TestEditPart();
				child.setModel(model);
				return child;
			}

			@Override
			protected List<?> getModelChildren() {
				List<Object> modelList = new ArrayList<>();
				modelList.add("_child2_Model");
				modelList.add("_child3_Model");
				modelList.add("_child5_Model");
				modelList.add("_child1_Model");
				modelList.add("_child7_Model");
				return modelList;
			}
		};
		parent.activate();
		parent.refresh();
		List<? extends EditPart> children = parent.getChildren();
		assertEquals(4, children.size());
		assertEquals("_child2_Model", children.get(0).getModel());
		assertEquals("_child5_Model", children.get(1).getModel());
		assertEquals("_child1_Model", children.get(2).getModel());
		assertEquals("_child7_Model", children.get(3).getModel());
	}

	@Test
	public void test_refreshChildren_3() throws Exception {
		TestEditPart child1 = new TestEditPart();
		child1.setModel("_child1_Model");
		//
		TestEditPart child2 = new TestEditPart();
		child2.setModel("_child2_Model");
		//
		TestEditPart child4 = new TestEditPart();
		child4.setModel("_child4_Model");
		//
		TestEditPart parent = new TestEditPart() {
			@Override
			protected EditPart createChild(Object model) {
				if ("_child3_Model".equals(model)) {
					return null;
				}
				TestEditPart child = new TestEditPart();
				child.setModel(model);
				return child;
			}

			@Override
			protected List<?> getModelChildren() {
				List<Object> modelList = new ArrayList<>();
				modelList.add("_child2_Model");
				modelList.add("_child3_Model");
				modelList.add("_child5_Model");
				modelList.add("_child1_Model");
				modelList.add("_child7_Model");
				return modelList;
			}
		};
		parent.activate();
		parent.test_access_addChild(child1, -1);
		parent.test_access_addChild(child2, -1);
		parent.test_access_addChild(child4, -1);
		//
		// check add child1 and child2
		List<? extends EditPart> children = parent.getChildren();
		assertNotNull(children);
		assertEquals(3, children.size());
		assertSame(child1, children.get(0));
		assertSame(child2, children.get(1));
		assertSame(child4, children.get(2));
		assertSame(parent, child1.getParent());
		assertSame(parent, child2.getParent());
		assertSame(parent, child4.getParent());
		//
		parent.refresh();
		assertEquals(4, children.size());
		assertSame(child2, children.get(0));
		assertEquals("_child5_Model", children.get(1).getModel());
		assertSame(child1, children.get(2));
		assertEquals("_child7_Model", children.get(3).getModel());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Listener tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_Add_Remove_SelectionListener() throws Exception {
		TestEditPart testEditPart = new TestEditPart();
		EventListenerList eventListeners = (EventListenerList) ReflectionUtils.getFieldObject(testEditPart,
				"eventListeners");
		//
		// check init state of listener for new EditPart
		assertFalse(eventListeners.getListeners(EditPartListener.class).hasNext());
		//
		EditPartListener listener1 = new EditPartListener.Stub() {
			@Override
			public void selectedStateChanged(org.eclipse.gef.EditPart editPart) {
			}
		};
		testEditPart.addEditPartListener(listener1);
		//
		// check add EditPartListener
		List<EditPartListener> list = Lists.newArrayList(eventListeners.getListeners(EditPartListener.class));
		assertNotNull(list);
		assertEquals(1, list.size());
		assertSame(listener1, list.get(0));
		//
		EditPartListener listener2 = new EditPartListener.Stub() {
			@Override
			public void selectedStateChanged(org.eclipse.gef.EditPart editPart) {
			}
		};
		testEditPart.addEditPartListener(listener2);
		//
		// again check add EditPartListener
		list = Lists.newArrayList(eventListeners.getListeners(EditPartListener.class));
		assertNotNull(list);
		assertEquals(2, list.size());
		assertSame(listener1, list.get(0));
		assertSame(listener2, list.get(1));
		//
		testEditPart.removeEditPartListener(listener1);
		//
		// check remove EditPartListener
		list = Lists.newArrayList(eventListeners.getListeners(EditPartListener.class));
		assertNotNull(list);
		assertEquals(1, list.size());
		assertSame(listener2, list.get(0));
		//
		testEditPart.removeEditPartListener(listener2);
		//
		// again check remove EditPartListener
		list = Lists.newArrayList(eventListeners.getListeners(EditPartListener.class));
		assertNotNull(list);
		assertEquals(0, list.size());
	}

	@Test
	public void test_Selection() throws Exception {
		final TestLogger actualLogger = new TestLogger();
		EditPartListener listener = new EditPartListener.Stub() {
			@Override
			public void selectedStateChanged(org.eclipse.gef.EditPart editPart) {
				actualLogger.log("selectionChanged(" + editPart + ")");
			}
		};
		TestEditPart testEditPart = new TestEditPart();
		testEditPart.addEditPartListener(listener);
		//
		// check not invoke EditPartListener.selectedStateChanged(EditPart) during addSelectionListener()
		assertTrue(testEditPart.isSelectable());
		assertEquals(org.eclipse.gef.EditPart.SELECTED_NONE, testEditPart.getSelected());
		actualLogger.assertEmpty();
		//
		// check not invoke EditPartListener.selectedStateChanged(EditPart) if selection value not update
		testEditPart.setSelected(org.eclipse.gef.EditPart.SELECTED_NONE);
		assertTrue(testEditPart.isSelectable());
		assertEquals(org.eclipse.gef.EditPart.SELECTED_NONE, testEditPart.getSelected());
		actualLogger.assertEmpty();
		//
		testEditPart.setSelected(org.eclipse.gef.EditPart.SELECTED);
		assertTrue(testEditPart.isSelectable());
		assertEquals(org.eclipse.gef.EditPart.SELECTED, testEditPart.getSelected());
		//
		// check invoke EditPartListener.selectedStateChanged(EditPart) if selection value update
		TestLogger expectedLogger = new TestLogger();
		expectedLogger.log("selectionChanged(" + testEditPart + ")");
		actualLogger.assertEquals(expectedLogger);
		//
		testEditPart.setSelected(org.eclipse.gef.EditPart.SELECTED_PRIMARY);
		//
		// check invoke EditPartListener.selectedStateChanged(EditPart) if selection value update
		assertTrue(testEditPart.isSelectable());
		assertEquals(org.eclipse.gef.EditPart.SELECTED_PRIMARY, testEditPart.getSelected());
		expectedLogger.log("selectionChanged(" + testEditPart + ")");
		actualLogger.assertEquals(expectedLogger);
		//
		// check not invoke EditPartListener.selectedStateChanged(EditPart) if selection value not update
		testEditPart.setSelected(org.eclipse.gef.EditPart.SELECTED_PRIMARY);
		assertTrue(testEditPart.isSelectable());
		assertEquals(org.eclipse.gef.EditPart.SELECTED_PRIMARY, testEditPart.getSelected());
		actualLogger.assertEmpty();
	}

	@Test
	public void test_Add_Remove_EditPartListener() throws Exception {
		TestEditPart testEditPart = new TestEditPart();
		EventListenerList eventListeners = (EventListenerList) ReflectionUtils.getFieldObject(testEditPart,
				"eventListeners");
		//
		// check init state of listener for new EditPart
		assertFalse(eventListeners.getListeners(EditPartListener.class).hasNext());
		//
		EditPartListener listener1 = new EditPartListener.Stub() {
			@Override
			public void removingChild(org.eclipse.gef.EditPart child, int index) {
			}

			@Override
			public void childAdded(org.eclipse.gef.EditPart child, int index) {
			}
		};
		testEditPart.addEditPartListener(listener1);
		//
		// check add EditPartListener
		List<EditPartListener> list = Lists.newArrayList(eventListeners.getListeners(EditPartListener.class));
		assertNotNull(list);
		assertEquals(1, list.size());
		assertSame(listener1, list.get(0));
		//
		EditPartListener listener2 = new EditPartListener.Stub() {
			@Override
			public void removingChild(org.eclipse.gef.EditPart child, int index) {
			}

			@Override
			public void childAdded(org.eclipse.gef.EditPart child, int index) {
			}
		};
		testEditPart.addEditPartListener(listener2);
		//
		// again check add EditPartListener
		list = Lists.newArrayList(eventListeners.getListeners(EditPartListener.class));
		assertNotNull(list);
		assertEquals(2, list.size());
		assertSame(listener1, list.get(0));
		assertSame(listener2, list.get(1));
		//
		testEditPart.removeEditPartListener(listener1);
		//
		// check remove EditPartListener
		list = Lists.newArrayList(eventListeners.getListeners(EditPartListener.class));
		assertNotNull(list);
		assertEquals(1, list.size());
		assertSame(listener2, list.get(0));
		//
		testEditPart.removeEditPartListener(listener2);
		//
		// again check remove EditPartListener
		list = Lists.newArrayList(eventListeners.getListeners(EditPartListener.class));
		assertNotNull(list);
		assertEquals(0, list.size());
	}

	@Test
	public void test_Invoke_EditPartListener() throws Exception {
		final TestLogger actualLogger = new TestLogger();
		EditPartListener listener = new EditPartListener.Stub() {
			@Override
			public void childAdded(org.eclipse.gef.EditPart child, int index) {
				actualLogger.log("childAdded(" + child + ", " + index + ")");
			}

			@Override
			public void removingChild(org.eclipse.gef.EditPart child, int index) {
				actualLogger.log("removingChild(" + child + ", " + index + ")");
			}
		};
		//
		TestEditPart parent = new TestEditPart();
		parent.addEditPartListener(listener);
		actualLogger.assertEmpty();
		//
		TestEditPart child = new TestEditPart();
		parent.test_access_addChild(child, -1);
		//
		// check invoke EditPartListener.childAdded() from child
		TestLogger expectedLogger = new TestLogger();
		expectedLogger.log("childAdded(" + child + ", 0)");
		actualLogger.assertEquals(expectedLogger);
		//
		TestEditPart child2 = new TestEditPart();
		parent.test_access_addChild(child2, -1);
		//
		// check invoke EditPartListener.childAdded() from child2
		expectedLogger.log("childAdded(" + child2 + ", 1)");
		actualLogger.assertEquals(expectedLogger);
		//
		parent.test_access_removeChild(child2);
		//
		// check invoke EditPartListener.removingChild() from child2
		expectedLogger.log("removingChild(" + child2 + ", 1)");
		actualLogger.assertEquals(expectedLogger);
		//
		parent.test_access_removeChild(child);
		//
		// check invoke EditPartListener.removingChild() from child
		expectedLogger.log("removingChild(" + child + ", 0)");
		actualLogger.assertEquals(expectedLogger);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// EditPart implementation
	//
	////////////////////////////////////////////////////////////////////////////
	private static class TestEditPart extends DesignEditPart {
		private final IEditPartViewer m_viewer;

		private TestEditPart() {
			this(new EmptyEditPartViewer());
		}

		private TestEditPart(IEditPartViewer viewer) {
			m_viewer = viewer;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Test Access
		//
		////////////////////////////////////////////////////////////////////////////
		@Test
		public void test_access_addChild(EditPart childPart, int index) {
			addChild(childPart, index);
		}

		@Test
		public void test_access_removeChild(EditPart childPart) {
			removeChild(childPart);
		}

		public List<?> test_access_getModelChildren() {
			return super.getModelChildren();
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// EditPart
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		protected Figure createFigure() {
			return new Figure();
		}

		@Override
		public IEditPartViewer getViewer() {
			return m_viewer;
		}
	}
}