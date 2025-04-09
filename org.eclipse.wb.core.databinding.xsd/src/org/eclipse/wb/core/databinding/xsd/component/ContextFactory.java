/*******************************************************************************
 * Copyright (c) 2024 Patrick Ziegler and others
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

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

public final class ContextFactory {
	private ContextFactory() {
	}

	public static JAXBContext createContext() throws JAXBException {
		return JAXBContext.newInstance(ContextFactory.class.getPackageName(), ContextFactory.class.getClassLoader());
	}
}
