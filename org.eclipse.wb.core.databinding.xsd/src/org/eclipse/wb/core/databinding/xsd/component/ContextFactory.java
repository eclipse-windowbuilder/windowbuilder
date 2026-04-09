/*******************************************************************************
 * Copyright (c) 2024, 2026 Patrick Ziegler and others
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
package org.eclipse.wb.core.databinding.xsd.component;

import java.util.concurrent.locks.ReentrantLock;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

public final class ContextFactory {
	private static final ReentrantLock LOCK = new ReentrantLock();
	private static JAXBContext CONTEXT;

	private ContextFactory() {
	}

	/**
	 * Returns a {@link JAXBContext} instance shared between all callers, which is
	 * created automatically when called for the first time. This method is
	 * thread-safe.
	 */
	public static JAXBContext getContext() throws JAXBException {
		try {
			LOCK.lock();
			if (CONTEXT == null) {
				CONTEXT = JAXBContext.newInstance(ContextFactory.class.getPackageName(), ContextFactory.class.getClassLoader());
			}
			return CONTEXT;
		} finally {
			LOCK.unlock();
		}
	}
}
