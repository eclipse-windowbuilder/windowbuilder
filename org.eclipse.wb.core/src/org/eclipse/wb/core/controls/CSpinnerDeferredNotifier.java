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
import org.eclipse.swt.widgets.Listener;

/**
 * Helper for sending single {@link SWT#Selection} event after some timeout.
 *
 * @author scheglov_ke
 * @author lobas_av
 * @coverage core.control
 * @deprecated Use {@link SpinnerDeferredNotifier} instead. This class will be
 *             removed after the 2028-03 release.
 */
@Deprecated(since = "2026-03", forRemoval = true)
public final class CSpinnerDeferredNotifier extends SpinnerDeferredNotifier {

	@Deprecated
	public CSpinnerDeferredNotifier(@SuppressWarnings("removal") CSpinner spinner, int timeout, Listener listener) {
		super(spinner, timeout, listener);
	}
}
