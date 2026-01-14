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
package org.eclipse.wb.core.controls;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;

/**
 * Helper for sending single {@link SWT#Selection} event after some timeout.
 *
 * @author scheglov_ke
 * @author lobas_av
 * @coverage core.control
 */
@SuppressWarnings("removal")
public sealed class SpinnerDeferredNotifier permits CSpinnerDeferredNotifier {
	private final Control m_spinner;
	private final Display m_display;
	private final int m_timeout;
	private final Listener m_listener;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SpinnerDeferredNotifier(Spinner spinner, int timeout, Listener listener) {
		m_spinner = spinner;
		m_display = m_spinner.getDisplay();
		m_timeout = timeout;
		m_listener = listener;
		addListener();
	}

	/* package */ SpinnerDeferredNotifier(CSpinner spinner, int timeout, Listener listener) {
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
			@Override
			public void handleEvent(final Event event) {
				m_eventId[0]++;
				m_display.timerExec(m_timeout, new Runnable() {
					int m_id = m_eventId[0];

					@Override
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
