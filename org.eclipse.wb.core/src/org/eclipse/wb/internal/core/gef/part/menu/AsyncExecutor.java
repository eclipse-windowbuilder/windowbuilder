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
package org.eclipse.wb.internal.core.gef.part.menu;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.DisplayEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.utils.state.GlobalState;

import org.eclipse.swt.widgets.Display;

/**
 * Helper for executing given {@link Runnable} in {@link Display#asyncExec(Runnable)}, that takes
 * care about delaying execution if this was requested by
 * {@link DisplayEventListener#beforeMessagesLoop()}.
 *
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage core.gef.menu
 */
public final class AsyncExecutor {
  public static void schedule(Runnable runnable) {
    new AsyncExecutor(runnable);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Display m_display = Display.getDefault();
  private final ObjectInfo m_activeObject = GlobalState.getActiveObject();
  private final Runnable m_runnable;
  private boolean m_shouldExecute = true;
  private final Object m_broadcastListener = new ObjectEventListener() {
    @Override
    public void refreshBeforeCreate() throws Exception {
      m_shouldExecute = false;
    }
  };
  private final Runnable m_schedulingRunnable = new Runnable() {
    public void run() {
      m_activeObject.removeBroadcastListener(m_broadcastListener);
      if (m_shouldExecute) {
        m_runnable.run();
      }
    }
  };

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private AsyncExecutor(Runnable runnable) {
    m_runnable = runnable;
    m_activeObject.addBroadcastListener(m_broadcastListener);
    m_display.asyncExec(m_schedulingRunnable);
  }
}
