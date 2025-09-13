/*******************************************************************************
 * Copyright (c) 2025 Patrick Ziegler and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swing.model;

import org.eclipse.core.databinding.observable.Realm;

import javax.swing.SwingUtilities;

/**
 * This class is used to get a {@link Realm} for the Swing event dispatcher
 * thread.
 */
public final class SwingRealm extends Realm {
	private static final Realm INSTANCE = new SwingRealm();
	private SwingRealm() {
		// Singleton
	}

	/**
	 * Returns the shared realm representing the Swing UI thread.
	 *
	 * @return the realm representing the Swing UI thread.
	 */
	public static Realm getRealm() {
		return INSTANCE;
	}

	@Override
	public boolean isCurrent() {
		return SwingUtilities.isEventDispatchThread();
	}
}
