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
package org.eclipse.wb.tests.designer.swt.model.menu;

import org.eclipse.wb.internal.core.model.menu.AbstractMenuObject;
import org.eclipse.wb.internal.core.model.menu.IMenuObjectInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuObjectListener;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.ImageDescriptor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AbstractMenuObject}.
 *
 * @author scheglov_ke
 */
public class AbstractMenuObjectTest extends DesignerTestCase {
	private AbstractMenuObject m_menuObject;
	private IMenuObjectListener m_listener;

	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();
		m_menuObject = new AbstractMenuObject(null) {
			@Override
			public Object getModel() {
				throw new NotImplementedException();
			}

			@Override
			public ImageDescriptor getImageDescriptor() {
				throw new NotImplementedException();
			}

			@Override
			public Rectangle getBounds() {
				throw new NotImplementedException();
			}

			@Override
			public IMenuPolicy getPolicy() {
				throw new NotImplementedException();
			}
		};
		m_listener = mock(IMenuObjectListener.class);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test that {@link IMenuObjectListener#refresh()} can be send.
	 */
	@Test
	public void test_refreshEvent() throws Exception {
		// perform operations
		m_menuObject.addListener(m_listener);
		ReflectionUtils.invokeMethod2(m_menuObject, "fireRefreshListeners");
		//
		verify(m_listener).refresh();
		verifyNoMoreInteractions(m_listener);
	}

	/**
	 * Test that {@link IMenuObjectListener#refresh()} can be send.
	 */
	@Test
	public void test_deleteEvent() throws Exception {
		Object object = new Object();
		// perform operations
		m_menuObject.addListener(m_listener);
		ReflectionUtils.invokeMethod2(m_menuObject, "fireDeleteListeners", Object.class, object);
		//
		verify(m_listener).deleting(object);
		verifyNoMoreInteractions(m_listener);
	}

	/**
	 * If {@link IMenuObjectInfo} is not added, it will not receive invocations.
	 */
	@Test
	public void test_noListener_noEvents() throws Exception {
		// perform operations
		ReflectionUtils.invokeMethod2(m_menuObject, "fireRefreshListeners");
		//
		verifyNoInteractions(m_listener);
	}

	/**
	 * If {@link IMenuObjectInfo} is not added, it will not receive invocations.
	 */
	@Test
	public void test_removeListener_noEvents() throws Exception {
		// perform operations
		m_menuObject.addListener(m_listener);
		m_menuObject.removeListener(m_listener);
		ReflectionUtils.invokeMethod2(m_menuObject, "fireRefreshListeners");
		//
		verifyNoInteractions(m_listener);
	}
}
