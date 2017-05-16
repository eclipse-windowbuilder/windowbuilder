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
package org.eclipse.wb.core.controls;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * Helper for sending single {@link SWT#Selection} event after some timeout.
 *
 * @author scheglov_ke
 * @author lobas_av
 * @coverage core.control
 */
public final class CSpinnerDeferredNotifier {
  private final CSpinner m_spinner;
  private final Display m_display;
  private final int m_timeout;
  private final Listener m_listener;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CSpinnerDeferredNotifier(CSpinner spinner, int timeout, Listener listener) {
    m_spinner = spinner;
    m_display = m_spinner.getDisplay();
    m_timeout = timeout;
    m_listener = listener;
    addListener();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Listener
  //
  ////////////////////////////////////////////////////////////////////////////
  private final int[] m_eventId = new int[1];

  /**
   * Handler for single {@link SWT#Selection} event.
   */
  private void addListener() {
    m_spinner.addListener(SWT.Selection, new Listener() {
      public void handleEvent(final Event event) {
        m_eventId[0]++;
        m_display.timerExec(m_timeout, new Runnable() {
          int m_id = m_eventId[0];

          public void run() {
            if (m_id == m_eventId[0]) {
              m_listener.handleEvent(event);
            }
          }
        });
      }
    });
  }
}
