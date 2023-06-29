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
package org.eclipse.wb.tests.designer.swt.model.menu;

import org.eclipse.wb.internal.core.model.menu.AbstractMenuObject;
import org.eclipse.wb.internal.core.model.menu.IMenuObjectInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuObjectListener;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Image;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.apache.commons.lang.NotImplementedException;

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
	protected void setUp() throws Exception {
		super.setUp();
		m_menuObject = new AbstractMenuObject(null) {
			@Override
			public Object getModel() {
				throw new NotImplementedException();
			}

			@Override
			public Image getImage() {
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
	public void test_noListener_noEvents() throws Exception {
		// perform operations
		ReflectionUtils.invokeMethod2(m_menuObject, "fireRefreshListeners");
		//
		verifyNoInteractions(m_listener);
	}

	/**
	 * If {@link IMenuObjectInfo} is not added, it will not receive invocations.
	 */
	public void test_removeListener_noEvents() throws Exception {
		// perform operations
		m_menuObject.addListener(m_listener);
		m_menuObject.removeListener(m_listener);
		ReflectionUtils.invokeMethod2(m_menuObject, "fireRefreshListeners");
		//
		verifyNoInteractions(m_listener);
	}
}
