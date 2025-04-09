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

import org.eclipse.wb.gef.core.events.IEditPolicyListener;
import org.eclipse.wb.gef.core.policies.EditPolicy;

import org.assertj.core.util.Lists;
import org.junit.Test;

import java.util.List;

/**
 * @author lobas_av
 *
 */
public class EditPolicyTest extends GefTestCase {

	////////////////////////////////////////////////////////////////////////////
	//
	// Event tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_Add_Remove_EditPolicyListener() throws Exception {
		EditPolicy testPolicy = new EditPolicy() {
		};
		// check init state of listener for new EditPolicy
		assertNull(testPolicy.getListeners(IEditPolicyListener.class));
		//
		IEditPolicyListener listener1 = new IEditPolicyListener() {
			@Override
			public void activatePolicy(EditPolicy policy) {
			}

			@Override
			public void deactivatePolicy(EditPolicy policy) {
			}
		};
		testPolicy.addEditPolicyListener(listener1);
		// check add IEditPolicyListener
		List<IEditPolicyListener> list = Lists.newArrayList(testPolicy.getListeners(IEditPolicyListener.class));
		assertNotNull(list);
		assertEquals(1, list.size());
		assertSame(listener1, list.get(0));
		//
		IEditPolicyListener listener2 = new IEditPolicyListener() {
			@Override
			public void activatePolicy(EditPolicy policy) {
			}

			@Override
			public void deactivatePolicy(EditPolicy policy) {
			}
		};
		testPolicy.addEditPolicyListener(listener2);
		// again check add IEditPolicyListener
		list = Lists.newArrayList(testPolicy.getListeners(IEditPolicyListener.class));
		assertNotNull(list);
		assertEquals(2, list.size());
		assertSame(listener1, list.get(0));
		assertSame(listener2, list.get(1));
		// check remove IEditPolicyListener
		testPolicy.removeEditPolicyListener(listener1);
		list = Lists.newArrayList(testPolicy.getListeners(IEditPolicyListener.class));
		assertNotNull(list);
		assertEquals(1, list.size());
		assertSame(listener2, list.get(0));
		// again check remove IEditPolicyListener
		testPolicy.removeEditPolicyListener(listener2);
		list = Lists.newArrayList(testPolicy.getListeners(IEditPolicyListener.class));
		assertNotNull(list);
		assertTrue(list.isEmpty());
	}

	@Test
	public void test_Invoke_EditPolicyListener() throws Exception {
		final TestLogger actualLogger = new TestLogger();
		TestLogger expectedLogger = new TestLogger();
		IEditPolicyListener listener = new IEditPolicyListener() {
			@Override
			public void activatePolicy(EditPolicy policy) {
				actualLogger.log("activate = " + policy);
			}

			@Override
			public void deactivatePolicy(EditPolicy policy) {
				actualLogger.log("deactivate = " + policy);
			}
		};
		EditPolicy testPolicy = new EditPolicy() {
		};
		//
		testPolicy.addEditPolicyListener(listener);
		actualLogger.assertEmpty();
		//
		testPolicy.activate();
		expectedLogger.log("activate = " + testPolicy);
		actualLogger.assertEquals(expectedLogger);
		//
		testPolicy.deactivate();
		expectedLogger.log("deactivate = " + testPolicy);
		actualLogger.assertEquals(expectedLogger);
		//
		actualLogger.clear();
		expectedLogger.clear();
		//
		testPolicy.removeEditPolicyListener(listener);
		testPolicy.activate();
		testPolicy.deactivate();
		actualLogger.assertEmpty();
	}
}