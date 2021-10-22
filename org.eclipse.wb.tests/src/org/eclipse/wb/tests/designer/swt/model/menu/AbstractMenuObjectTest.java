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

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.menu.AbstractMenuObject;
import org.eclipse.wb.internal.core.model.menu.IMenuObjectInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuObjectListener;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.eclipse.swt.graphics.Image;

import org.apache.commons.lang.NotImplementedException;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;

/**
 * Tests for {@link AbstractMenuObject}.
 *
 * @author scheglov_ke
 */
public class AbstractMenuObjectTest extends DesignerTestCase {
  private IMocksControl m_mocksControl;
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
    m_mocksControl = EasyMock.createStrictControl();
    m_menuObject = new AbstractMenuObject(null) {
      public Object getModel() {
        throw new NotImplementedException();
      }

      public Image getImage() {
        throw new NotImplementedException();
      }

      public Rectangle getBounds() {
        throw new NotImplementedException();
      }

      public IMenuPolicy getPolicy() {
        throw new NotImplementedException();
      }
    };
    m_listener = m_mocksControl.createMock(IMenuObjectListener.class);
  }

  @Override
  protected void tearDown() throws Exception {
    m_mocksControl.verify();
    super.tearDown();
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
    // prepare expectations
    m_listener.refresh();
    m_mocksControl.replay();
    // perform operations
    m_menuObject.addListener(m_listener);
    ReflectionUtils.invokeMethod2(m_menuObject, "fireRefreshListeners");
  }

  /**
   * Test that {@link IMenuObjectListener#refresh()} can be send.
   */
  public void test_deleteEvent() throws Exception {
    Object object = new Object();
    // prepare expectations
    m_listener.deleting(object);
    m_mocksControl.replay();
    // perform operations
    m_menuObject.addListener(m_listener);
    ReflectionUtils.invokeMethod2(m_menuObject, "fireDeleteListeners", Object.class, object);
  }

  /**
   * If {@link IMenuObjectInfo} is not added, it will not receive invocations.
   */
  public void test_noListener_noEvents() throws Exception {
    // prepare expectations
    m_mocksControl.replay();
    // perform operations
    ReflectionUtils.invokeMethod2(m_menuObject, "fireRefreshListeners");
  }

  /**
   * If {@link IMenuObjectInfo} is not added, it will not receive invocations.
   */
  public void test_removeListener_noEvents() throws Exception {
    // prepare expectations
    m_mocksControl.replay();
    // perform operations
    m_menuObject.addListener(m_listener);
    m_menuObject.removeListener(m_listener);
    ReflectionUtils.invokeMethod2(m_menuObject, "fireRefreshListeners");
  }
}
